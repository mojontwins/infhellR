package net.minecraft.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main server network listener. Opens a ServerSocket and starts an
 * acceptor thread (NetworkAcceptThread). Each accepted socket becomes
 * a NetLoginHandler (during connection phase) or a NetServerHandler
 * (once logged in).
 *
 * Handles the dispatch loop: on each tick, all pending login handlers
 * try to complete authentication, and all logged-in players' network
 * handlers process incoming packets.
 */
public class NetworkListenThread {

	/** Logger for network events. */
	public static Logger logger = Logger.getLogger("Minecraft");

	/** The ServerSocket that accepts incoming connections. */
	private ServerSocket serverSocket;

	/** The background thread that accepts new connections. */
	private Thread networkAcceptThread;

	/** Flag that controls the acceptor thread loop. */
	public volatile boolean serverStarted = false;

	/** Monotonically increasing counter used to name connections. */
	private int connectionCounter = 0;

	/** Players currently in the authentication phase (not yet logged in). */
	private ArrayList<NetLoginHandler> pendingConnections = new ArrayList<NetLoginHandler>();

	/** Logged-in players whose network handlers need packet processing. */
	private ArrayList<NetServerHandler> playerList = new ArrayList<NetServerHandler>();

	/** The owning MinecraftServer instance. */
	public MinecraftServer mcServer;

	/**
	 * Creates the network listener and starts the acceptor thread.
	 *
	 * @param minecraftServer1  the MinecraftServer instance
	 * @param inetAddress2      the local InetAddress to bind to (may be null for all interfaces)
	 * @param port3             the TCP port to listen on
	 * @throws IOException if the ServerSocket cannot be created
	 */
	public NetworkListenThread(MinecraftServer minecraftServer1, InetAddress inetAddress2, int port3) throws IOException {
		this.mcServer = minecraftServer1;
		this.serverSocket = new ServerSocket(port3, 0, inetAddress2);
		this.serverSocket.setPerformancePreferences(0, 2, 1);
		this.serverStarted = true;
		this.networkAcceptThread = new NetworkAcceptThread(this, "Listen thread", minecraftServer1);
		this.networkAcceptThread.start();
	}

	/** Adds a NetServerHandler to the player list (called after login completes). */
	public void addPlayer(NetServerHandler netServerHandler1) {
		this.playerList.add(netServerHandler1);
	}

	/** Adds a NetLoginHandler to the pending queue for authentication. */
	private void addPendingConnection(NetLoginHandler netLoginHandler1) {
		if (netLoginHandler1 == null) {
			throw new IllegalArgumentException("Got null pendingconnection!");
		} else {
			this.pendingConnections.add(netLoginHandler1);
		}
	}

	/**
	 * Called each server tick. Processes all pending login handlers
	 * (tries to complete authentication) and all logged-in player
	 * network handlers (reads/writes packets). Removes handlers that
	 * have finished processing or closed their connection.
	 */
	public void handleNetworkListenThread() {
		for (int i = 0; i < this.pendingConnections.size(); ++i) {
			NetLoginHandler loginHandler2 = (NetLoginHandler) this.pendingConnections.get(i);
			try {
				loginHandler2.tryLogin();
			} catch (Exception e) {
				loginHandler2.kickUser("Internal server error");
				logger.log(Level.WARNING, "Failed to handle packet: " + e, e);
			}
			if (loginHandler2.finishedProcessing) {
				this.pendingConnections.remove(i--);
			}
			loginHandler2.netManager.wakeThreads();
		}

		for (int i = 0; i < this.playerList.size(); ++i) {
			NetServerHandler serverHandler6 = (NetServerHandler) this.playerList.get(i);
			try {
				serverHandler6.handlePackets();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to handle packet: " + e, e);
				serverHandler6.kickPlayer("Internal server error");
			}
			if (serverHandler6.connectionClosed) {
				this.playerList.remove(i--);
			}
			serverHandler6.netManager.wakeThreads();
		}
	}

	/** Returns the ServerSocket (used by NetworkAcceptThread). */
	static ServerSocket getServerSocket(NetworkListenThread listener0) {
		return listener0.serverSocket;
	}

	/** Returns and increments the connection counter (used by NetworkAcceptThread). */
	static int getNextConnectionId(NetworkListenThread listener0) {
		return listener0.connectionCounter++;
	}

	/** Adds a login handler to the pending queue (used by NetworkAcceptThread). */
	static void addPendingConnection(NetworkListenThread listener0, NetLoginHandler loginHandler1) {
		listener0.addPendingConnection(loginHandler1);
	}
}