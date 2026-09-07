package net.minecraft.network;

/**
 * Master / supervisor thread that forcibly terminates a stalled
 * {@link NetworkManager}'s read and write worker threads.
 *
 * <p>This thread is launched by the {@link NetworkManager} constructor and
 * sleeps for a fixed grace period (five seconds). When it wakes it inspects
 * both the read and write worker threads; if either is still alive (which
 * indicates the workers are wedged or otherwise failed to shut down on their
 * own), it invokes the deprecated {@link Thread#stop()} to kill them. The
 * {@code stop()} call is wrapped in a broad {@code catch (Throwable)} so a
 * {@link ThreadDeath}-like outcome cannot propagate out of this supervisor
 * and crash the JVM.</p>
 *
 * <p>This is a last-resort cleanup hook, not part of normal traffic flow.</p>
 */
class NetworkMasterThread extends Thread {
	/** The {@link NetworkManager} whose worker threads this master supervises. */
	final NetworkManager netManager;

	/**
	 * @param networkManager the connection whose worker threads should be
	 *                       killed if they remain alive after the grace
	 *                       period elapses.
	 */
	NetworkMasterThread(NetworkManager networkManager) {
		this.netManager = networkManager;
	}

	/**
	 * Sleeps for the grace period, then forcibly stops the read and write
	 * threads of the associated {@link NetworkManager} if they are still
	 * alive.
	 *
	 * <p>Any {@link InterruptedException} thrown while waiting is logged but
	 * otherwise ignored — the supervisor still attempts to stop both workers
	 * once it can do so.</p>
	 */
	@SuppressWarnings("deprecation")
	public void run() {
		try {
			// Wait out the grace period before declaring the workers stuck.
			Thread.sleep(5000L);

			// If the read thread is still alive, force-stop it. Thread.stop() is
			// deprecated precisely because it can leave shared state in an
			// inconsistent state, but this is the documented escape hatch for a
			// wedged network pipeline — the manager is about to be torn down
			// anyway. Swallow any throwable (ThreadDeath, etc.) so we still try
			// to clean up the write thread next.
			if(NetworkManager.getReadThread(this.netManager).isAlive()) {
				try {
					NetworkManager.getReadThread(this.netManager).stop();
				} catch (Throwable stopReadException) {
				}
			}

			// Same rationale as above for the write thread.
			if(NetworkManager.getWriteThread(this.netManager).isAlive()) {
				try {
					NetworkManager.getWriteThread(this.netManager).stop();
				} catch (Throwable stopWriteException) {
				}
			}
		} catch (InterruptedException interruptedException) {
			interruptedException.printStackTrace();
		}

	}
}
