package net.minecraft.client;

import java.util.Map;

class ThreadStatSyncherSend extends Thread {
	final Map<?, ?> data;
	final StatsSyncher syncher;

	ThreadStatSyncherSend(StatsSyncher syncher, Map<?, ?> data) {
		this.syncher = syncher;
		this.data = data;
	}

	public void run() {
		try {
			StatsSyncher.writeStats(this.syncher, this.data,
				StatsSyncher.getUnsentDataFile(this.syncher),
				StatsSyncher.getUnsentTempFile(this.syncher),
				StatsSyncher.getUnsentOldFile(this.syncher));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			StatsSyncher.setBusy(this.syncher, false);
		}
	}
}
