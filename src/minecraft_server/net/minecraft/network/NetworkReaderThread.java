package net.minecraft.network;

/**
 * Worker thread that pumps incoming packets off the socket into the
 * {@link NetworkManager}'s inbound queue.
 *
 * <p>One of these is started per connection. The thread increments
 * {@link NetworkManager#numReadThreads} on entry and decrements it on exit
 * (both under {@link NetworkManager#threadSyncObject}) so that other parts of
 * the engine can tell when read pumps are still active — typically to defer
 * world shutdown until in-flight packets have drained.</p>
 *
 * <p>The main loop runs as long as the {@link NetworkManager} is alive and the
 * server is not terminating. Each iteration:</p>
 * <ol>
 *     <li>Drains every packet currently available via
 *         {@link NetworkManager#readNetworkPacket(NetworkManager)}.</li>
 *     <li>Sleeps briefly (2&nbsp;ms) to avoid burning a CPU core when the
 *         socket is idle.</li>
 * </ol>
 *
 * <p>An {@link InterruptedException} during the sleep is treated as a benign
 * wakeup and the loop simply continues — there's no separate handling for
 * shutdown signals here because termination is signalled via the
 * {@code isRunning}/{@code isServerTerminating} flags on the
 * {@link NetworkManager}.</p>
 */
class NetworkReaderThread extends Thread {
	/** The {@link NetworkManager} this reader pumps packets for. */
	final NetworkManager netManager;

	/**
	 * @param networkManager the manager whose socket should be drained.
	 * @param threadName     name to give this thread (e.g.
	 *                       {@code "Network read thread"}).
	 */
	NetworkReaderThread(NetworkManager networkManager, String threadName) {
		super(threadName);
		this.netManager = networkManager;
	}

	/**
	 * Main read loop. See the class Javadoc for the full contract; in short,
	 * the thread increments the read-thread counter, then drains packets until
	 * the manager signals shutdown, and finally decrements the counter as it
	 * exits.
	 */
	@Override
	public void run() {
		// Announce that another read worker is live, so NetworkManager (and
		// shutdown code) can wait on numReadThreads draining to zero.
		synchronized(NetworkManager.threadSyncObject) {
			++NetworkManager.numReadThreads;
		}

		while(true) {
			// Tracks whether this iteration actually entered the try block
			// before bailing out. If we bail out via the shutdown checks at
			// the top of the try we must NOT decrement numReadThreads in the
			// finally clause, because the post-loop decrement below handles it.
			boolean enteredTryBlock = false;

			try {
				enteredTryBlock = true;

				// Bail out cleanly if the manager has been shut down.
				if(!NetworkManager.isRunning(this.netManager)) {
					enteredTryBlock = false;
					break;
				}

				// Server is going away — stop draining the socket.
				if(NetworkManager.isServerTerminating(this.netManager)) {
					enteredTryBlock = false;
					break;
				}

				// Pull every packet that is currently buffered on the socket.
				// readNetworkPacket returns true while it keeps finding work,
				// so the empty body drains the queue fully before sleeping.
				while(NetworkManager.readNetworkPacket(this.netManager)) {
				}

				// Brief yield so we don't spin on an idle socket.
				try {
					sleep(2L);
				} catch (InterruptedException sleepException) {
					// Treat an interrupted sleep as a benign no-op — the
					// shutdown flags above will be re-checked on the next
					// iteration anyway.
				}
			} finally {
				// Only decrement here if we entered the try block in this
				// iteration. The shutdown-break paths set enteredTryBlock=false
				// explicitly so they don't double-decrement relative to the
				// post-loop decrement that always runs.
				if(enteredTryBlock) {
					synchronized(NetworkManager.threadSyncObject) {
						--NetworkManager.numReadThreads;
					}
				}
			}
		}

		// Final decrement on normal exit. This pairs with the increment at the
		// top of run() and is reached both via the inner breaks (which set
		// enteredTryBlock=false so the finally clause didn't fire) and via any
		// future loop-exit path that bypasses the finally clause entirely.
		synchronized(NetworkManager.threadSyncObject) {
			--NetworkManager.numReadThreads;
		}
	}
}
