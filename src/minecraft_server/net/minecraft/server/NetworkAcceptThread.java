package net.minecraft.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * Accepts incoming TCP connections on the server's listening socket.
 * For each new socket, it checks for recent connection attempts from
 * the same IP (rate limiting), and if allowed, creates a NetLoginHandler
 * and adds it to the pending queue of the NetworkListenThread.
 *
 * This runs as a background thread owned by NetworkListenThread.
 */
class NetworkAcceptThread extends Thread {

	/** Reference to the owning MinecraftServer. */
	final MinecraftServer mcServer;

	/** The NetworkListenThread that owns this acceptor (used for callbacks). */
	final NetworkListenThread networkListenThread;

	/**
	 * Creates the acceptor thread and starts it.
	 *
	 * @param networkListenThread1  the thread that will process accepted connections
	 * @param threadName2           the thread name
	 * @param minecraftServer3      the MinecraftServer instance
	 */
	NetworkAcceptThread(NetworkListenThread networkListenThread1, String threadName2, MinecraftServer minecraftServer3) {
		super(threadName2);
		this.networkListenThread = networkListenThread1;
		this.mcServer = minecraftServer3;
	}

	/**
	 * Main accept loop: blocks on ServerSocket.accept(), applies
	 * IP-based rate limiting, and spawns NetLoginHandlers for new connections.
	 * The loop exits when NetworkListenThread.serverStarted is set to false.
	 */
	public void run() {
		HashMap<InetAddress, Long> connectionTimestamps = new HashMap<InetAddress, Long>();

		while (this.networkListenThread.serverStarted) {
			try {
				Socket socket = NetworkListenThread.getServerSocket(this.networkListenThread).accept();
				if (socket != null) {
					InetAddress clientAddress = socket.getInetAddress();
					long now = System.currentTimeMillis();
					Long lastSeen = connectionTimestamps.get(clientAddress);
					boolean isLocalhost = "127.0.0.1".equals(clientAddress.getHostAddress());
					boolean rateLimited = !isLocalhost && lastSeen != null && (now - lastSeen.longValue()) < 5000L;

					if (rateLimited) {
						connectionTimestamps.put(clientAddress, now);
						socket.close();
					} else {
						connectionTimestamps.put(clientAddress, now);
						NetLoginHandler loginHandler = new NetLoginHandler(this.mcServer, socket,
								"Connection #" + NetworkListenThread.getNextConnectionId(this.networkListenThread));
						NetworkListenThread.addPendingConnection(this.networkListenThread, loginHandler);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}