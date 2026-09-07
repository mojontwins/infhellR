package net.minecraft.network;

/**
 * Watchdog thread that detects connections which have stalled without ever
 * completing the Minecraft login handshake.
 *
 * <p>This thread is started by {@link NetworkManager#networkShutdown(String, Object...)}
 * (indirectly, via {@link NetworkManager}) and sleeps for a fixed grace period
 * (two seconds). After waking, if the associated {@link NetworkManager} is
 * still considered "running", it is forcibly torn down under the assumption
 * that the read/write threads are deadlocked or otherwise unresponsive, and
 * the player should be disconnected with the standard
 * {@code "disconnect.closed"} reason so the client UI can show a sensible
 * error message.</p>
 *
 * <p>This is a safety net for connections that never produced any socket
 * activity — it is not part of the steady-state read/write pipeline.</p>
 */
class ThreadMonitorConnection extends Thread {
	/** The {@link NetworkManager} whose liveness this thread is monitoring. */
	final NetworkManager netManager;

	/**
	 * @param networkManager the connection to watch. The monitor does not take
	 *                       ownership of the manager; the manager owns its own
	 *                       lifecycle.
	 */
	ThreadMonitorConnection(NetworkManager networkManager) {
		this.netManager = networkManager;
	}

	/**
	 * Sleeps for the configured grace period, then verifies that the
	 * {@link NetworkManager} it was created for is still alive. If it is, the
	 * write thread is interrupted and the manager is shut down with the
	 * {@code disconnect.closed} reason.
	 *
	 * <p>Any exception thrown while waking or shutting down the connection is
	 * logged but otherwise swallowed so the watchdog itself can exit cleanly.</p>
	 */
	public void run() {
		try {
			// Wait the grace period before deciding the connection is stuck.
			Thread.sleep(2000L);

			// Only act if the manager hasn't already been torn down by other means.
			if(NetworkManager.isRunning(this.netManager)) {
				// The write thread is the most likely culprit for a hung connection,
				// so interrupt it first to give it a chance to unwind gracefully.
				NetworkManager.getWriteThread(this.netManager).interrupt();

				// Hand off to the standard shutdown path so the disconnect reason
				// ("disconnect.closed") is propagated to the client UI.
				this.netManager.networkShutdown("disconnect.closed", new Object[0]);
			}
		} catch (Exception shutdownException) {
			// Any failure here means we couldn't even shut the connection down
			// cleanly — log it so the operator can diagnose, then exit.
			shutdownException.printStackTrace();
		}

	}
}
