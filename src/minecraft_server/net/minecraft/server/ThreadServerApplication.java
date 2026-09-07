package net.minecraft.server;

/**
 * Wrapper thread that runs the MinecraftServer's main loop.
 * This is the primary execution thread for the dedicated server,
 * responsible for processing ticks, handling network I/O, and
 * performing world updates.
 */
public final class ThreadServerApplication extends Thread {

	/** Reference to the MinecraftServer instance that runs the main loop. */
	final MinecraftServer mcServer;

	/**
	 * Creates and names the server application thread.
	 *
	 * @param string1  the thread name (passed to Thread constructor)
	 * @param minecraftServer2  the MinecraftServer instance to run
	 */
	public ThreadServerApplication(String string1, MinecraftServer minecraftServer2) {
		super(string1);
		this.mcServer = minecraftServer2;
	}

	/** Executes the server's main run loop. */
	public void run() {
		this.mcServer.run();
	}
}