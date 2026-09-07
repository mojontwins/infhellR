package net.minecraft.network;

import java.io.IOException;

/**
 * Worker thread that drains the {@link NetworkManager}'s outbound queue and
 * writes each packet to the underlying socket.
 *
 * <p>One of these is started per connection. The thread increments
 * {@link NetworkManager#numWriteThreads} on entry and decrements it on exit
 * (both under {@link NetworkManager#threadSyncObject}) so the engine can tell
 * when write pumps are still active — typically to defer world shutdown until
 * the outbound queue has been flushed.</p>
 *
 * <p>The main loop runs as long as the {@link NetworkManager} is alive. Each
 * iteration:</p>
 * <ol>
 *     <li>Drains every packet currently queued via
 *         {@link NetworkManager#sendNetworkPacket(NetworkManager)}.</li>
 *     <li>Flushes the output stream if it is still open.</li>
 *     <li>Sleeps briefly (2&nbsp;ms) to avoid burning a CPU core when the
 *         outbound queue is empty.</li>
 * </ol>
 *
 * <p>Any {@link IOException} from the flush is forwarded to
 * {@link NetworkManager#sendError(NetworkManager, Exception)} (unless the
 * manager is already shutting down) and also printed to stderr so a human
 * operator can diagnose persistent transport-level failures.</p>
 *
 * <p>An {@link InterruptedException} during the sleep is treated as a benign
 * wakeup and the loop simply continues — there's no separate handling for
 * shutdown signals here because termination is signalled via the
 * {@code isRunning} flag on the {@link NetworkManager}.</p>
 */
class NetworkWriterThread extends Thread {
	/** The {@link NetworkManager} this writer flushes packets for. */
	final NetworkManager netManager;

	/**
	 * @param networkManager the manager whose outbound queue should be
	 *                       flushed.
	 * @param threadName     name to give this thread (e.g.
	 *                       {@code "Network write thread"}).
	 */
	NetworkWriterThread(NetworkManager networkManager, String threadName) {
		super(threadName);
		this.netManager = networkManager;
	}

	/**
	 * Main write loop. See the class Javadoc for the full contract; in short,
	 * the thread increments the write-thread counter, then drains packets and
	 * flushes the socket until the manager signals shutdown, and finally
	 * decrements the counter as it exits.
	 */
	public void run() {
		// Announce that another write worker is live, so NetworkManager (and
		// shutdown code) can wait on numWriteThreads draining to zero.
		synchronized(NetworkManager.threadSyncObject) {
			++NetworkManager.numWriteThreads;
		}

		while(true) {
			// Tracks whether this iteration actually entered the try block
			// before bailing out. If we bail out via the shutdown check at the
			// top of the try we must NOT decrement numWriteThreads in the
			// finally clause, because the post-loop decrement below handles it.
			boolean enteredTryBlock = false;

			try {
				enteredTryBlock = true;

				// Bail out cleanly if the manager has been shut down.
				if(!NetworkManager.isRunning(this.netManager)) {
					enteredTryBlock = false;
					break;
				}

				// Push every packet currently waiting in the outbound queue.
				// sendNetworkPacket returns true while it keeps finding work,
				// so the empty body drains the queue fully before flushing.
				while(NetworkManager.sendNetworkPacket(this.netManager)) {
				}

				// Flush whatever we just wrote. The stream may legitimately
				// be null if the socket has already been closed, so guard the
				// flush call. Any IOException is reported to the manager
				// (unless it is already tearing itself down) and printed so
				// operators can see transport-level failures.
				try {
					if(NetworkManager.getOutputStream(this.netManager) != null) {
						NetworkManager.getOutputStream(this.netManager).flush();
					}
				} catch (IOException flushException) {
					// Only forward to the manager if it hasn't already started
					// its own shutdown — otherwise sendError() would just
					// produce noise on top of an expected disconnect.
					if(!NetworkManager.isTerminating(this.netManager)) {
						NetworkManager.sendError(this.netManager, flushException);
                    }

					// Always log so the operator can see the underlying cause,
					// even if the manager is already tearing down.
					flushException.printStackTrace();
				}

				// Brief yield so we don't spin on an empty outbound queue.
				try {
					sleep(2L);
				} catch (InterruptedException sleepException) {
					// Treat an interrupted sleep as a benign no-op — the
					// shutdown flag above will be re-checked on the next
					// iteration anyway.
				}
			} finally {
				// Only decrement here if we entered the try block in this
				// iteration. The shutdown-break path sets enteredTryBlock=false
				// explicitly so it doesn't double-decrement relative to the
				// post-loop decrement that always runs.
				if(enteredTryBlock) {
					synchronized(NetworkManager.threadSyncObject) {
						--NetworkManager.numWriteThreads;
					}
				}
			}
		}

		// Final decrement on normal exit. This pairs with the increment at the
		// top of run() and is reached both via the inner break (which set
		// enteredTryBlock=false so the finally clause didn't fire) and via any
		// future loop-exit path that bypasses the finally clause entirely.
		synchronized(NetworkManager.threadSyncObject) {
			--NetworkManager.numWriteThreads;
		}
	}
}
