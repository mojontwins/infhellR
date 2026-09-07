package net.minecraft.server;

/**
 * A daemon thread that simply sleeps indefinitely. Used as a placeholder
 * to prevent the server JVM from exiting immediately when no other non-daemon
 * threads are running.
 */
public class ThreadSleepForeverServer extends Thread {

	/** Reference to the owning MinecraftServer. */
	final MinecraftServer mc;

	/**
	 * Creates the daemon thread. It will start sleeping immediately.
	 *
	 * @param minecraftServer1  the MinecraftServer instance (referenced but not used in run loop)
	 */
	public ThreadSleepForeverServer(MinecraftServer minecraftServer1) {
		this.mc = minecraftServer1;
		this.setDaemon(true);
		this.start();
	}

	/**
	 * Sleeps for Integer.MAX_VALUE milliseconds (approximately 24.8 days).
	 * The loop catches InterruptedException and returns silently, allowing
	 * the thread to be interrupted if the server shuts down.
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(2147483647L);
			} catch (InterruptedException interruptedException2) {
				// Thread was interrupted, exit the loop
			}
		}
	}
}