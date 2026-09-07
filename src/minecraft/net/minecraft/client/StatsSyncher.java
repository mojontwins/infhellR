package net.minecraft.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class StatsSyncher {
	private volatile boolean isBusy = false;
	private volatile Map pendingDataWrite = null;
	private volatile Map pendingDataMerge = null;
	private StatFileWriter statFileWriter;
	private File unsentDataFile;
	private File dataFile;
	private File unsentTempFile;
	private File tempFile;
	private File unsentOldFile;
	private File oldFile;
	private Session theSession;
	private int requestCooldown = 0;
	private int pollCooldown = 0;

	@SuppressWarnings("unchecked")
	public StatsSyncher(Session session, StatFileWriter statFileWriter, File statsDir) {
		String username = session.username;
		this.unsentDataFile = new File(statsDir, "stats_" + username.toLowerCase() + "_unsent.dat");
		this.dataFile = new File(statsDir, "stats_" + username.toLowerCase() + ".dat");
		this.unsentOldFile = new File(statsDir, "stats_" + username.toLowerCase() + "_unsent.old");
		this.oldFile = new File(statsDir, "stats_" + username.toLowerCase() + ".old");
		this.unsentTempFile = new File(statsDir, "stats_" + username.toLowerCase() + "_unsent.tmp");
		this.tempFile = new File(statsDir, "stats_" + username.toLowerCase() + ".tmp");
		if (!username.toLowerCase().equals(username)) {
			this.migrateOldFilename(statsDir, "stats_" + username + "_unsent.dat", this.unsentDataFile);
			this.migrateOldFilename(statsDir, "stats_" + username + ".dat", this.dataFile);
			this.migrateOldFilename(statsDir, "stats_" + username + "_unsent.old", this.unsentOldFile);
			this.migrateOldFilename(statsDir, "stats_" + username + ".old", this.oldFile);
			this.migrateOldFilename(statsDir, "stats_" + username + "_unsent.tmp", this.unsentTempFile);
			this.migrateOldFilename(statsDir, "stats_" + username + ".tmp", this.tempFile);
		}

		this.statFileWriter = statFileWriter;
		this.theSession = session;
		if (this.unsentDataFile.exists()) {
			statFileWriter.func_27179_a(this.readStatsFileFallback(this.unsentDataFile, this.unsentTempFile, this.unsentOldFile));
		}
		this.beginReceiveStats();
	}

	private void migrateOldFilename(File dir, String oldName, File target) {
		File old = new File(dir, oldName);
		if (old.exists() && !old.isDirectory() && !target.exists()) {
			old.renameTo(target);
		}
	}

	private Map readStatsFileFallback(File primary, File secondary, File tertiary) {
		if (primary.exists()) return this.readStatsFile(primary);
		if (tertiary.exists()) return this.readStatsFile(tertiary);
		if (secondary.exists()) return this.readStatsFile(secondary);
		return null;
	}

	private Map readStatsFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return StatFileWriter.func_27177_a(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private void writeStatsAtomically(Map data, File target, File temp, File backup) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(temp, false));
		try {
			writer.print(StatFileWriter.func_27185_a(this.theSession.username, "local", data));
		} finally {
			writer.close();
		}
		if (backup.exists()) {
			backup.delete();
		}
		if (target.exists()) {
			target.renameTo(backup);
		}
		temp.renameTo(target);
	}

	public void beginReceiveStats() {
		if (this.isBusy) {
			throw new IllegalStateException("Can't get stats from server while StatsSyncher is busy!");
		}
		this.requestCooldown = 100;
		this.isBusy = true;
		(new ThreadStatSyncherReceive(this)).start();
	}

	public void beginSendStats(Map data) {
		if (this.isBusy) {
			throw new IllegalStateException("Can't save stats while StatsSyncher is busy!");
		}
		this.requestCooldown = 100;
		this.isBusy = true;
		(new ThreadStatSyncherSend(this, data)).start();
	}

	public void syncStatsFileWithMap(Map data) {
		int i = 30;
		while (this.isBusy) {
			--i;
			if (i <= 0) break;
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.isBusy = true;
		try {
			this.writeStatsAtomically(data, this.unsentDataFile, this.unsentTempFile, this.unsentOldFile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.isBusy = false;
		}
	}

	public boolean func_27420_b() {
		return this.requestCooldown <= 0 && !this.isBusy && this.pendingDataMerge == null;
	}

	@SuppressWarnings("unchecked")
	public void func_27425_c() {
		if (this.requestCooldown > 0) {
			--this.requestCooldown;
		}
		if (this.pollCooldown > 0) {
			--this.pollCooldown;
		}
		if (this.pendingDataMerge != null) {
			this.statFileWriter.func_27187_c(this.pendingDataMerge);
			this.pendingDataMerge = null;
		}
		if (this.pendingDataWrite != null) {
			this.statFileWriter.func_27180_b(this.pendingDataWrite);
			this.pendingDataWrite = null;
		}
	}

	static Map getPendingDataWrite(StatsSyncher syncher) {
		return syncher.pendingDataWrite;
	}

	static File getDataFile(StatsSyncher syncher) {
		return syncher.dataFile;
	}

	static File getTempFile(StatsSyncher syncher) {
		return syncher.tempFile;
	}

	static File getOldFile(StatsSyncher syncher) {
		return syncher.oldFile;
	}

	static void writeStats(StatsSyncher syncher, Map data, File target, File temp, File backup) throws IOException {
		syncher.writeStatsAtomically(data, target, temp, backup);
	}

	static Map setPendingDataWrite(StatsSyncher syncher, Map data) {
		return syncher.pendingDataWrite = data;
	}

	static Map readStatsFileFallbackFor(StatsSyncher syncher, File primary, File secondary, File tertiary) {
		return syncher.readStatsFileFallback(primary, secondary, tertiary);
	}

	static boolean setBusy(StatsSyncher syncher, boolean busy) {
		return syncher.isBusy = busy;
	}

	static File getUnsentDataFile(StatsSyncher syncher) {
		return syncher.unsentDataFile;
	}

	static File getUnsentTempFile(StatsSyncher syncher) {
		return syncher.unsentTempFile;
	}

	static File getUnsentOldFile(StatsSyncher syncher) {
		return syncher.unsentOldFile;
	}
}
