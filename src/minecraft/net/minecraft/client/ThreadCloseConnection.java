package net.minecraft.client;

import net.minecraft.network.NetworkManager;

class ThreadCloseConnection extends Thread {
	final NetworkManager networkManager;

	ThreadCloseConnection(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public void run() {
		try {
			Thread.sleep(2000L);
			if (NetworkManager.isRunning(this.networkManager)) {
				NetworkManager.getWriteThread(this.networkManager).interrupt();
				this.networkManager.networkShutdown("disconnect.closed", new Object[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
