package net.minecraft.network;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.network.packet.Packet;

/**
 * Manages a single network connection between Minecraft client and server.
 * 
 * <p>This class is the core TCP socket manager. It owns the underlying {@link Socket},
 * wraps its streams in {@link DataInputStream}/{@link DataOutputStream}, and coordinates
 * a dedicated read thread and write thread that perform non-blocking I/O.</p>
 * 
 * <h2>Send Queue ({@link #dataPackets} and {@link #chunkDataPackets})</h2>
 * <p>Packets to be sent to the peer are accumulated in two synchronized lists:</p>
 * <ul>
 *   <li>{@link #dataPackets} — normal gameplay packets (movement, chat, entity updates, etc.)</li>
 *   <li>{@link #chunkDataPackets} — large bulk chunk-data packets that are bandwidth-heavy</li>
 * </ul>
 * <p>They are kept separate so that normal packets can flow freely while chunk
 * packets are throttled to one per {@link #field_20100_w} send cycles. The current
 * total byte length of queued packets is tracked in {@link #sendQueueByteLength}
 * (protected by {@link #sendQueueLock}).</p>
 * 
 * <h2>Receive Queue ({@link #readPackets})</h2>
 * <p>The read thread decodes incoming packets from the socket and pushes them onto
 * {@link #readPackets}. The main game thread (via {@link #processReadPackets()})
 * drains that queue and dispatches each packet to the {@link NetHandler}, which
 * applies game logic. The network thread never touches game state; this separation
 * is the primary reason for the read queue.</p>
 * 
 * <h2>{@link #addToSendQueue(Packet)}</h2>
 * <p>Called by game code to enqueue an outgoing packet. Under {@code sendQueueLock}
 * the packet's size is added to {@link #sendQueueByteLength}, and the packet is
 * pushed onto either {@link #chunkDataPackets} (if {@link Packet#isChunkDataPacket})
 * or {@link #dataPackets}.</p>
 * 
 * <h2>{@link #processReadPackets()}</h2>
 * <p>Called once per game tick on the main thread. It:</p>
 * <ol>
 *   <li>Checks {@link #sendQueueByteLength} &gt; 1 MB → shutdown with "overflow".</li>
 *   <li>Maintains a {@link #timeSinceLastRead} tick counter; if no packets arrive
 *       for 1200 ticks (~60s) → shutdown with "timeout".</li>
 *   <li>Drains up to 1000 packets from {@link #readPackets}, calling
 *       {@link Packet#processPacket(NetHandler)} on each — that's the dispatch step.</li>
 *   <li>Calls {@link #wakeThreads()} so the I/O threads can re-check their work.</li>
 *   <li>If a shutdown is pending and the queue is empty, notifies the {@link NetHandler}
 *       of the termination reason.</li>
 * </ol>
 * 
 * <h2>Thread Safety Model</h2>
 * <ul>
 *   <li>The socket I/O threads (NetworkReaderThread, NetworkWriterThread) call
 *       {@link #readPacket()} and {@link #sendPacket()} and may push to or pull
 *       from the queues.</li>
 *   <li>The main thread calls {@link #processReadPackets()} and
 *       {@link #addToSendQueue(Packet)}.</li>
 *   <li>The packet lists are wrapped in {@link Collections#synchronizedList}.</li>
 *   <li>Multi-step mutations (e.g. add-to-queue + update byte length) are protected
 *       by {@link #sendQueueLock} so byte accounting stays consistent.</li>
 *   <li>{@link #chunkDataSendCounter} is a per-packet minimum age (in ms) before
 *       a packet may be sent — it throttles bursty send queues when set &gt; 0.</li>
 * </ul>
 * 
 * <h2>Lifecycle Flags</h2>
 * <ul>
 *   <li>{@link #isRunning} — true while the manager is active. Set false by
 *       {@link #networkShutdown(String, Object...)} after the socket is closed.</li>
 *   <li>{@link #isServerTerminating} — true after {@link #serverShutdown()} is called;
 *       further {@code addToSendQueue} calls are silently ignored.</li>
 *   <li>{@link #isTerminating} — true while a graceful shutdown is in progress;
 *       once the read queue drains, the termination reason is delivered to the handler.</li>
 * </ul>
 * 
 * <h2>{@link #chunkDataSendCounter}</h2>
 * <p>When set to a positive value, a packet must be at least that many milliseconds
 * old before it will be sent. This is used to throttle bursts. A value of 0 disables
 * the age check.</p>
 * 
 * @see NetHandler
 * @see NetworkReaderThread
 * @see NetworkWriterThread
 * @see NetworkMasterThread
 * @see ThreadMonitorConnection
 */
public class NetworkManager {
	/** Shared synchronization object for cross-thread coordination (debugging/legacy). */
	public static final Object threadSyncObject = new Object();
	/** Global counter of active network read threads (diagnostic). */
	public static int numReadThreads;
	/** Global counter of active network write threads (diagnostic). */
	public static int numWriteThreads;
	/** Lock guarding concurrent send-queue operations and byte-length accounting. */
	private Object sendQueueLock = new Object();
	/** The underlying TCP socket for this connection. */
	private Socket networkSocket;
	/** Cached remote address of the peer, captured at construction time. */
	private final SocketAddress remoteSocketAddress;
	/** Input stream wrapper for reading typed data from the socket. */
	private DataInputStream socketInputStream;
	/** Output stream wrapper for writing typed data to the socket (buffered). */
	private DataOutputStream socketOutputStream;
	/** True while the manager is alive; flipped to false in {@link #networkShutdown}. */
	private boolean isRunning = true;
	/** Queue of packets received from the peer, awaiting dispatch on the main thread. */
	private List<Packet> readPackets = Collections.synchronizedList(new ArrayList<Packet>());
	/** Queue of normal outgoing packets waiting to be written to the socket. */
	private List<Packet> dataPackets = Collections.synchronizedList(new ArrayList<Packet>());
	/** Queue of bulk chunk-data packets (throttled separately from dataPackets). */
	private List<Packet> chunkDataPackets = Collections.synchronizedList(new ArrayList<Packet>());
	/** The packet handler that game logic is dispatched through. */
	private NetHandler netHandler;
	/** True after {@link #serverShutdown()} has been called — further sends are dropped. */
	private boolean isServerTerminating = false;
	/** The thread that drains the send queues and writes to the socket. */
	private Thread writeThread;
	/** The thread that reads from the socket and enqueues incoming packets. */
	private Thread readThread;
	/** True while a graceful shutdown is in progress. */
	private boolean isTerminating = false;
	/** Localization key describing the reason the connection was shut down. */
	private String terminationReason = "";
	/** Optional format arguments for the termination reason message. */
	private Object[] aObj;
	/** Tick counter for the last received packet — drives the read-timeout watchdog. */
	private int timeSinceLastRead = 0;
	/** Total byte size of all queued outgoing packets (data + chunk). */
	private int sendQueueByteLength = 0;
	/** Diagnostic: bytes received per packet ID (indexed by packet ID). */
	public static int[] field_28145_d = new int[256];
	/** Diagnostic: bytes sent per packet ID (indexed by packet ID). */
	public static int[] field_28144_e = new int[256];
	/**
	 * Minimum age (milliseconds) a queued packet must reach before it is eligible
	 * to be sent. 0 disables the age check. Used for outbound throttling.
	 */
	public int chunkDataSendCounter = 0;
	/**
	 * Throttle counter for chunk-data packets: a chunk packet is sent at most once
	 * per {@code chunkDataSendInterval} send cycles. Starts at 50.
	 */
	private int chunkDataSendInterval = 50;
	
	/**
	 * Constructs a NetworkManager bound to the given socket and packet handler.
	 * 
	 * <p>Initializes the socket streams with sane defaults (30s read timeout,
	 * IPTOS_LOWDELAY traffic class) and starts the dedicated read and write threads.</p>
	 * 
	 * @param socket the connected TCP socket for this peer
	 * @param threadName descriptive name prefix for the read/write threads (e.g. "Client")
	 * @param netHandler the handler that incoming packets will be dispatched to
	 * @throws IOException if the socket streams cannot be opened
	 */
	public NetworkManager(Socket socket, String threadName, NetHandler netHandler) throws IOException {
		this.networkSocket = socket;
		this.remoteSocketAddress = socket.getRemoteSocketAddress();
		this.netHandler = netHandler;

		// Best-effort socket tuning: 30s SO_TIMEOUT protects the read thread from
		// hanging forever if the peer vanishes silently; IPTOS_LOWDELAY (24) hints
		// the OS to prioritize low-latency delivery.
		try {
			socket.setSoTimeout(30000);
			socket.setTrafficClass(24);
		} catch (SocketException socketException) {
			System.err.println(socketException.getMessage());
		}

		this.socketInputStream = new DataInputStream(socket.getInputStream());
		this.socketOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 5120));
		this.readThread = new NetworkReaderThread(this, threadName + " read thread");
		this.writeThread = new NetworkWriterThread(this, threadName + " write thread");
		this.readThread.start();
		this.writeThread.start();
	}

	/**
	 * @return the number of packets currently waiting to be dispatched from the
	 *         read queue to the {@link NetHandler}.
	 */
	public int getNumReadPackets() {
		return this.readPackets.size();
	}
	
	/**
	 * @return the number of normal outgoing packets currently in the send queue.
	 */
	public int getNumWritePackets() {
		return this.dataPackets.size();
	}
	
	/**
	 * @return the number of bulk chunk-data packets currently in the send queue.
	 */
	public int getNumChunkPackets() {
		return this.chunkDataPackets.size();
	}
	
	/**
	 * Replaces the current packet handler. Used e.g. when transitioning between
	 * login phase and play phase handlers.
	 * 
	 * @param netHandler the new packet handler
	 */
	public void setNetHandler(NetHandler netHandler) {
		this.netHandler = netHandler;
	}

	/**
	 * Enqueues a packet for transmission to the peer.
	 * 
	 * <p>Under {@link #sendQueueLock} this method:</p>
	 * <ol>
	 *   <li>Adds the packet's serialized byte length (+1 for the packet ID byte) to
	 *       {@link #sendQueueByteLength}.</li>
	 *   <li>If {@link Packet#isChunkDataPacket} is true, appends to
	 *       {@link #chunkDataPackets}; otherwise to {@link #dataPackets}.</li>
	 * </ol>
	 * 
	 * <p>If the manager has been told the server is terminating
	 * ({@link #isServerTerminating}), the call is silently ignored.</p>
	 * 
	 * @param packet the packet to enqueue for sending
	 */
	public void addToSendQueue(Packet packet) {
		if(!this.isServerTerminating) {
			synchronized(this.sendQueueLock) {
				// Track total queued bytes for the overflow watchdog in processReadPackets.
				this.sendQueueByteLength += packet.getPacketSize() + 1;
				if(packet.isChunkDataPacket) {
					// Bulk chunk data goes into the throttled queue.
					this.chunkDataPackets.add(packet);
				} else {
					// Everything else goes into the normal send queue.
					this.dataPackets.add(packet);
				}

			}
		}
	}

	/**
	 * Writes the next eligible packet(s) to the socket.
	 * 
	 * <p>Called repeatedly by the write thread. Honors the following rules:</p>
	 * <ul>
	 *   <li>Sends the head of {@link #dataPackets} if non-empty and either
	 *       {@link #chunkDataSendCounter} is 0 or the head packet is old enough.</li>
	 *   <li>After every {@link #chunkDataSendInterval} iterations of the send loop,
	 *       sends the head of {@link #chunkDataPackets} if eligible. This throttles
	 *       bulk chunk data so it doesn't drown normal packets.</li>
	 * </ul>
	 * 
	 * <p>Updates {@link #field_28144_e} (bytes-sent-per-packet-ID stats).</p>
	 * 
	 * @return true if at least one packet was written this call, false otherwise.
	 *         Any I/O exception triggers {@link #onNetworkError(Exception)}.
	 */
	private boolean sendPacket() {
		boolean didSend = false;

		try {
			Packet packet2;
			int[] sentBytesById;
			int packetId;

			// 1) Try to send one normal packet from the head of the data queue.
			if(!this.dataPackets.isEmpty() && (this.chunkDataSendCounter == 0 || System.currentTimeMillis() - ((Packet)this.dataPackets.get(0)).creationTimeMillis >= (long)this.chunkDataSendCounter)) {
				synchronized(this.sendQueueLock) {
					packet2 = (Packet)this.dataPackets.remove(0);
					this.sendQueueByteLength -= packet2.getPacketSize() + 1;
				}

				// Actually serialize the packet onto the wire.
				Packet.writePacket(packet2, this.socketOutputStream);
				// Diagnostic byte counter.
				sentBytesById = field_28144_e;
				packetId = packet2.getPacketId();
				sentBytesById[packetId] += packet2.getPacketSize() + 1;
				didSend = true;
			}

			// 2) Once every chunkDataSendInterval send cycles, allow one chunk packet through.
			if(this.chunkDataSendInterval-- <= 0 && !this.chunkDataPackets.isEmpty() && (this.chunkDataSendCounter == 0 || System.currentTimeMillis() - ((Packet)this.chunkDataPackets.get(0)).creationTimeMillis >= (long)this.chunkDataSendCounter)) {
				synchronized(this.sendQueueLock) {
					packet2 = (Packet)this.chunkDataPackets.remove(0);
					this.sendQueueByteLength -= packet2.getPacketSize() + 1;
				}

				Packet.writePacket(packet2, this.socketOutputStream);
				sentBytesById = field_28144_e;
				packetId = packet2.getPacketId();
				sentBytesById[packetId] += packet2.getPacketSize() + 1;
				// Reset the throttle counter — we just consumed our chunk-send slot.
				this.chunkDataSendInterval = 0;
				didSend = true;
			}

			return didSend;
		} catch (Exception exception) {
			if(!this.isTerminating) {
				this.onNetworkError(exception);
			}

			return false;
		}
	}

	/**
	 * Interrupts both I/O threads so they wake up from blocking calls
	 * (e.g. {@code read()}, {@code sleep()}) and re-check their state.
	 * 
	 * <p>Used when game code adds new work to the queues, or when a shutdown
	 * is being processed.</p>
	 */
	public void wakeThreads() {
		this.readThread.interrupt();
		this.writeThread.interrupt();
	}

	/**
	 * Reads and decodes a single packet from the input stream.
	 * 
	 * <p>Called in a loop by {@link NetworkReaderThread}. The decoded packet is
	 * appended to {@link #readPackets} (unless the manager is shutting down) and
	 * {@link #field_28145_d} is updated with byte-received statistics.</p>
	 * 
	 * <p>An end-of-stream ({@code null}) return from the packet decoder is treated
	 * as a clean disconnect and triggers {@link #networkShutdown} with
	 * {@code "disconnect.endOfStream"}.</p>
	 * 
	 * @return true if a packet was read this call, false otherwise. On I/O
	 *         exception {@link #onNetworkError(Exception)} is invoked.
	 */
	private boolean readPacket() {
		boolean didRead = false;

		try {
			// Decode one packet from the wire. The isServerHandler() flag affects
			// packet deserialization (e.g. compression, packet ID handling).
			Packet packet = Packet.readPacket(this.socketInputStream, this.netHandler.isServerHandler());
			if(packet != null) {
				int[] receivedBytesById = field_28145_d;
				int packetId = packet.getPacketId();
				receivedBytesById[packetId] += packet.getPacketSize() + 1;
				if(!this.isServerTerminating) {
					// Enqueue for the main thread to dispatch.
					this.readPackets.add(packet);
				}

				didRead = true;
			} else {
				// Peer closed the connection cleanly.
				this.networkShutdown("disconnect.endOfStream", new Object[0]);
			}

			return didRead;
		} catch (Exception exception) {
			if(!this.isTerminating) {
				this.onNetworkError(exception);
			}

			return false;
		}
	}

	/**
	 * Logs the exception and initiates a graceful shutdown with a generic reason.
	 * 
	 * @param exception the network error that triggered the shutdown
	 */
	private void onNetworkError(Exception exception) {
		exception.printStackTrace();
		this.networkShutdown("disconnect.genericReason", new Object[]{"Internal exception: " + exception.toString()});
	}

	/**
	 * Initiates a graceful shutdown of this connection.
	 * 
	 * <p>Idempotent: if already not running, returns immediately. Otherwise:</p>
	 * <ol>
	 *   <li>Marks the manager as terminating, storing the disconnect reason.</li>
	 *   <li>Starts a {@link NetworkMasterThread} to orchestrate thread joins.</li>
	 *   <li>Flips {@link #isRunning} to false.</li>
	 *   <li>Closes the input stream, output stream, and socket (each in its own
	 *       try/catch so a failure in one doesn't skip the rest).</li>
	 * </ol>
	 * 
	 * <p>The disconnect reason is delivered to {@link NetHandler#handleErrorMessage}
	 * from {@link #processReadPackets()} once the read queue has drained.</p>
	 * 
	 * @param reason localization key describing the disconnect
	 * @param args optional format arguments for the reason
	 */
	public void networkShutdown(String reason, Object... args) {
		if(this.isRunning) {
			this.isTerminating = true;
			this.terminationReason = reason;
			this.aObj = args;
			// NetworkMasterThread orchestrates the actual thread teardown off the caller.
			(new NetworkMasterThread(this)).start();
			this.isRunning = false;

			// Close each resource independently — a failure on one should not
			// prevent us from cleaning up the others.
			try {
				this.socketInputStream.close();
				this.socketInputStream = null;
			} catch (Throwable ignored) {
			}

			try {
				this.socketOutputStream.close();
				this.socketOutputStream = null;
			} catch (Throwable ignored) {
			}

			try {
				this.networkSocket.close();
				this.networkSocket = null;
			} catch (Throwable ignored) {
			}

		}
	}

	/**
	 * Main-thread packet dispatcher and connection watchdog.
	 * 
	 * <p>Called once per game tick. See class-level docs for the full step-by-step
	 * description. The {@code maxPacketsPerTick} cap (1000) bounds the work done
	 * per call so the game thread never falls too far behind the network thread.</p>
	 */
	public void processReadPackets() {
		// 1) Overflow watchdog — too much pending outbound data means we can't keep up.
		if(this.sendQueueByteLength > 1048576) {
			this.networkShutdown("disconnect.overflow", new Object[0]);
		}

		// 2) Read-timeout watchdog — if no packets have arrived in 1200 ticks, give up.
		if(this.readPackets.isEmpty()) {
			if(this.timeSinceLastRead++ == 1200) {
				this.networkShutdown("disconnect.timeout", new Object[0]);
			}
		} else {
			// Reset the timer — we got something.
			this.timeSinceLastRead = 0;
		}

		// 3) Drain up to 1000 packets, dispatching each to the handler. This is the
		//    only place where game logic actually runs in response to network input.
		int maxPacketsPerTick = 1000;

		while(!this.readPackets.isEmpty() && maxPacketsPerTick-- >= 0) {
			//long milis = System.currentTimeMillis();
			Packet packet = (Packet)this.readPackets.remove(0);
			packet.processPacket(this.netHandler);
			/*
			long dfrnz = System.currentTimeMillis() - milis ; if (dfrnz > 50) {
				System.out.print("Took too long " + packet.getPacketId() + " " + dfrnz);
				if (packet instanceof Packet52MultiBlockChange) {
					Packet52MultiBlockChange p = (Packet52MultiBlockChange) packet;
					System.out.println (" -> " + p.size);
				}
			}
			*/
		}

		// 4) Tell the I/O threads to re-check their queues (new work may have appeared).
		this.wakeThreads();

		// 5) If a shutdown was requested and we just finished draining it, surface the
		//    disconnect reason to the handler so it can show it to the user.
		if(this.isTerminating && this.readPackets.isEmpty()) {
			this.netHandler.handleErrorMessage(this.terminationReason, this.aObj);
		}

	}

	/**
	 * @return the cached remote socket address of the peer.
	 */
	public SocketAddress getRemoteAddress() {
		return this.remoteSocketAddress;
	}

	/**
	 * Server-initiated shutdown that signals "no more data should be sent".
	 * 
	 * <p>Wakes the I/O threads and starts a {@link ThreadMonitorConnection} that
	 * watches for clean termination. After this call, {@link #addToSendQueue}
	 * becomes a no-op.</p>
	 */
	public void serverShutdown() {
		if(!this.isServerTerminating) {
			this.wakeThreads();
			this.isServerTerminating = true;
			this.readThread.interrupt();
			(new ThreadMonitorConnection(this)).start();
		}
	}

	/**
	 * @return the number of chunk-data packets currently queued for sending.
	 */
	public int getNumChunkDataPackets() {
		return this.chunkDataPackets.size();
	}

	/**
	 * @return the underlying socket. May be {@code null} if the connection has
	 *         already been shut down.
	 */
	public Socket getSocket() {
		return this.networkSocket;
	}

	/**
	 * Checks whether the given network manager is still active.
	 * 
	 * @param networkManager the manager to inspect
	 * @return true if {@link #isRunning} is still true
	 */
	public static boolean isRunning(NetworkManager networkManager) {
		return networkManager.isRunning;
	}

	/**
	 * Package-private accessor used by the I/O threads to check whether the
	 * server has initiated its shutdown sequence.
	 * 
	 * @param networkManager the manager to inspect
	 * @return true if the server-side shutdown flag is set
	 */
	static boolean isServerTerminating(NetworkManager networkManager) {
		return networkManager.isServerTerminating;
	}

	/**
	 * Package-private accessor used by the read thread to invoke
	 * {@link #readPacket()} on the manager.
	 * 
	 * @param networkManager the manager to drive
	 * @return true if a packet was read
	 */
	static boolean readNetworkPacket(NetworkManager networkManager) {
		return networkManager.readPacket();
	}

	/**
	 * Package-private accessor used by the write thread to invoke
	 * {@link #sendPacket()} on the manager.
	 * 
	 * @param networkManager the manager to drive
	 * @return true if a packet was written
	 */
	static boolean sendNetworkPacket(NetworkManager networkManager) {
		return networkManager.sendPacket();
	}

	/**
	 * Package-private accessor exposing the raw output stream to the I/O threads
	 * for low-level operations outside the normal packet path.
	 * 
	 * @param networkManager the manager whose stream is requested
	 * @return the socket output stream wrapper (may be {@code null} after shutdown)
	 */
	static DataOutputStream getOutputStream(NetworkManager networkManager) {
		return networkManager.socketOutputStream;
	}

	/**
	 * Package-private accessor for the graceful-shutdown flag.
	 * 
	 * @param networkManager the manager to inspect
	 * @return true if a graceful shutdown is in progress
	 */
	static boolean isTerminating(NetworkManager networkManager) {
		return networkManager.isTerminating;
	}

	/**
	 * Package-private accessor for {@link #onNetworkError(Exception)}, used by
	 * the I/O threads when they catch an exception outside of {@link #sendPacket()}
	 * or {@link #readPacket()}.
	 * 
	 * @param networkManager the manager that hit the error
	 * @param exception the exception to report
	 */
	static void sendError(NetworkManager networkManager, Exception exception) {
		networkManager.onNetworkError(exception);
	}

	/**
	 * Package-private accessor exposing the read thread reference.
	 * 
	 * @param networkManager the manager whose read thread is requested
	 * @return the read thread
	 */
	static Thread getReadThread(NetworkManager networkManager) {
		return networkManager.readThread;
	}

	/**
	 * Public accessor exposing the write thread reference.
	 * 
	 * @param networkManager the manager whose write thread is requested
	 * @return the write thread
	 */
	public static Thread getWriteThread(NetworkManager networkManager) {
		return networkManager.writeThread;
	}

	/**
	 * @return the remote socket address of the peer (cached at construction).
	 */
	public SocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}
}