package net.minecraft.client;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.json.J_InvalidSyntaxException;
import net.minecraft.client.json.J_JdomParser;
import net.minecraft.client.json.J_JsonNode;
import net.minecraft.client.json.J_JsonRootNode;
import net.minecraft.client.json.J_JsonStringNode;
import net.minecraft.game.achievements.Achievement;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.achievements.StatList;

public class StatFileWriter {
	private Map<StatBase,Integer> statsData = new HashMap<StatBase,Integer>();
	private Map<StatBase,Integer> statsSyncedData = new HashMap<StatBase,Integer>();
	private boolean statsDirty = false;
	private StatsSyncher statsSyncher;

	public StatFileWriter(Session session, File dataDir) {
		File statsDir = new File(dataDir, "stats");
		if(!statsDir.exists()) {
			statsDir.mkdir();
		}

		File[] allFiles = dataDir.listFiles();
		int fileCount = allFiles.length;

		for(int i = 0; i < fileCount; ++i) {
			File file = allFiles[i];
			if(file.getName().startsWith("stats_") && file.getName().endsWith(".dat")) {
				File targetFile = new File(statsDir, file.getName());
				if(!targetFile.exists()) {
					System.out.println("Relocating " + file.getName());
					file.renameTo(targetFile);
				}
			}
		}

		this.statsSyncher = new StatsSyncher(session, this, statsDir);
	}

	public void readStat(StatBase stat, int amount) {
		this.writeStatToMap(this.statsSyncedData, stat, amount);
		this.writeStatToMap(this.statsData, stat, amount);
		this.statsDirty = true;
	}

	private void writeStatToMap(Map<StatBase,Integer> map, StatBase stat, int amount) {
		Integer current = (Integer)map.get(stat);
		int existing = current == null ? 0 : current.intValue();
		map.put(stat, existing + amount);
	}

	public Map<StatBase,Integer> func_27176_a() {
		return new HashMap<StatBase,Integer>(this.statsSyncedData);
	}

	public void func_27179_a(Map<StatBase,Integer> map) {
		if(map != null) {
			this.statsDirty = true;
			Iterator<StatBase> iterator = map.keySet().iterator();

			while(iterator.hasNext()) {
				StatBase stat = (StatBase)iterator.next();
				this.writeStatToMap(this.statsSyncedData, stat, ((Integer)map.get(stat)).intValue());
				this.writeStatToMap(this.statsData, stat, ((Integer)map.get(stat)).intValue());
			}

		}
	}

	public void func_27180_b(Map<StatBase,Integer> map) {
		if(map != null) {
			Iterator<StatBase> iterator = map.keySet().iterator();

			while(iterator.hasNext()) {
				StatBase stat = (StatBase)iterator.next();
				Integer current = (Integer)this.statsSyncedData.get(stat);
				int existing = current == null ? 0 : current.intValue();
				this.statsData.put(stat, ((Integer)map.get(stat)).intValue() + existing);
			}

		}
	}

	public void func_27187_c(Map<StatBase,Integer> map) {
		if(map != null) {
			this.statsDirty = true;
			Iterator<StatBase> iterator = map.keySet().iterator();

			while(iterator.hasNext()) {
				StatBase stat = (StatBase)iterator.next();
				this.writeStatToMap(this.statsSyncedData, stat, ((Integer)map.get(stat)).intValue());
			}

		}
	}

	public static Map<StatBase,Integer> func_27177_a(String json) {
		HashMap<StatBase,Integer> statsMap = new HashMap<StatBase,Integer>();

		try {
			String checksumKey = "local";
			StringBuilder checksumInput = new StringBuilder();
			J_JsonRootNode rootNode = (new J_JdomParser()).parse(json);
			List<J_JsonNode> statsChangeNodes = rootNode.getArrayNode(new Object[]{"stats-change"});
			Iterator<J_JsonNode> iter = statsChangeNodes.iterator();

			while(iter.hasNext()) {
				J_JsonNode changeNode = (J_JsonNode)iter.next();
				Map<?, ?> fields = changeNode.getFields();
				Entry<?, ?> firstEntry = (Entry<?, ?>)fields.entrySet().iterator().next();
				int statId = Integer.parseInt(((J_JsonStringNode)firstEntry.getKey()).getText());
				int statValue = Integer.parseInt(((J_JsonNode)firstEntry.getValue()).getText());
				StatBase stat = StatList.getOneShotStat(statId);
				if(stat == null) {
					System.out.println(statId + " is not a valid stat");
				} else {
					checksumInput.append(StatList.getOneShotStat(statId).statGuid).append(",");
					checksumInput.append(statValue).append(",");
					statsMap.put(stat, statValue);
				}
			}

			MD5String md5 = new MD5String(checksumKey);
			String checksum = md5.getMD5String(checksumInput.toString());
			if(!checksum.equals(rootNode.getStringValue(new Object[]{"checksum"}))) {
				System.out.println("CHECKSUM MISMATCH");
				return null;
			}
		} catch (J_InvalidSyntaxException e) {
			e.printStackTrace();
		}

		return statsMap;
	}

	public static String func_27185_a(String username, String sessionId, Map<?, ?> statsMap) {
		StringBuilder json = new StringBuilder();
		StringBuilder checksumInput = new StringBuilder();
		boolean firstEntry = true;
		json.append("{\r\n");
		if(username != null && sessionId != null) {
			json.append("  \"user\":{\r\n");
			json.append("    \"name\":\"").append(username).append("\",\r\n");
			json.append("    \"sessionid\":\"").append(sessionId).append("\"\r\n");
			json.append("  },\r\n");
		}

		json.append("  \"stats-change\":[");
		Iterator<?> iterator = statsMap.keySet().iterator();

		while(iterator.hasNext()) {
			StatBase stat = (StatBase)iterator.next();
			if(!firstEntry) {
				json.append("},");
			} else {
				firstEntry = false;
			}

			json.append("\r\n    {\"").append(stat.statId).append("\":").append(statsMap.get(stat));
			checksumInput.append(stat.statGuid).append(",");
			checksumInput.append(statsMap.get(stat)).append(",");
		}

		if(!firstEntry) {
			json.append("}");
		}

		MD5String md5 = new MD5String(sessionId);
		json.append("\r\n  ],\r\n");
		json.append("  \"checksum\":\"").append(md5.getMD5String(checksumInput.toString())).append("\"\r\n");
		json.append("}");
		return json.toString();
	}

	public boolean hasAchievementUnlocked(Achievement achievement) {
		return this.statsData.containsKey(achievement);
	}

	public boolean canUnlockAchievement(Achievement achievement) {
		return achievement.parentAchievement == null || this.hasAchievementUnlocked(achievement.parentAchievement);
	}

	public int writeStat(StatBase stat) {
		Integer current = (Integer)this.statsData.get(stat);
		return current == null ? 0 : current.intValue();
	}

	public void func_27175_b() {
	}

	public void syncStats() {
		this.statsSyncher.syncStatsFileWithMap(this.func_27176_a());
	}

	public void func_27178_d() {
		if(this.statsDirty && this.statsSyncher.func_27420_b()) {
			this.statsSyncher.beginSendStats(this.func_27176_a());
		}

		this.statsSyncher.func_27425_c();
	}
}