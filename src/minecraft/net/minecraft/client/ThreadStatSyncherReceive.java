package net.minecraft.client;

class ThreadStatSyncherReceive extends Thread {
	final StatsSyncher syncher;

	ThreadStatSyncherReceive(StatsSyncher syncher) {
		this.syncher = syncher;
	}

	public void run() {
		try {
			if (StatsSyncher.getPendingDataWrite(this.syncher) != null) {
				StatsSyncher.writeStats(this.syncher,
					StatsSyncher.getPendingDataWrite(this.syncher),
					StatsSyncher.getDataFile(this.syncher),
					StatsSyncher.getTempFile(this.syncher),
					StatsSyncher.getOldFile(this.syncher));
			} else if (StatsSyncher.getDataFile(this.syncher).exists()) {
				StatsSyncher.setPendingDataWrite(this.syncher,
					StatsSyncher.readStatsFileFallbackFor(this.syncher,
						StatsSyncher.getDataFile(this.syncher),
						StatsSyncher.getTempFile(this.syncher),
						StatsSyncher.getOldFile(this.syncher)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			StatsSyncher.setBusy(this.syncher, false);
		}
	}
}
