package net.minecraft.client.sound;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SoundPool {
	private Random rand = new Random();
	private Map<String, ArrayList<SoundPoolEntry>> nameToSoundPoolEntriesMapping = new HashMap<String, ArrayList<SoundPoolEntry>>();
	private List<SoundPoolEntry> allSoundPoolEntries = new ArrayList<SoundPoolEntry>();
	public int numberOfSoundPoolEntries = 0;
	public boolean isGetRandomSound = true;

	public SoundPoolEntry addSound(String name, File file) {
		try {
			String fullName = name;
			name = name.substring(0, name.indexOf("."));
			if(this.isGetRandomSound) {
				while(Character.isDigit(name.charAt(name.length() - 1))) {
					name = name.substring(0, name.length() - 1);
				}
			}

			name = name.replaceAll("/", ".");
			if(!this.nameToSoundPoolEntriesMapping.containsKey(name)) {
				this.nameToSoundPoolEntriesMapping.put(name, new ArrayList<SoundPoolEntry>());
			}

			SoundPoolEntry entry = new SoundPoolEntry(fullName, file.toURI().toURL());
			this.nameToSoundPoolEntriesMapping.get(name).add(entry);
			this.allSoundPoolEntries.add(entry);
			++this.numberOfSoundPoolEntries;
			return entry;
		} catch (MalformedURLException error) {
			error.printStackTrace();
			throw new RuntimeException(error);
		}
	}

	public SoundPoolEntry addSoundURL(String name, URL url) {
		String fullName = name;
		name = name.substring(0, name.indexOf("."));
		if(this.isGetRandomSound) {
			while(Character.isDigit(name.charAt(name.length() - 1))) {
				name = name.substring(0, name.length() - 1);
			}
		}

		name = name.replaceAll("/", ".");
		if(!this.nameToSoundPoolEntriesMapping.containsKey(name)) {
			this.nameToSoundPoolEntriesMapping.put(name, new ArrayList<SoundPoolEntry>());
		}

		SoundPoolEntry entry = new SoundPoolEntry(fullName, url);
		// System.out.println("Adding custom sound for " + fullName + " @ " + url);

		this.nameToSoundPoolEntriesMapping.get(name).add(entry);
		this.allSoundPoolEntries.add(entry);
		++this.numberOfSoundPoolEntries;
		return entry;
	}	

	public SoundPoolEntry getRandomSoundFromSoundPool(String name) {
		List<SoundPoolEntry> entries = this.nameToSoundPoolEntriesMapping.get(name);
		return entries == null ? null : (SoundPoolEntry)entries.get(this.rand.nextInt(entries.size()));
	}

	public SoundPoolEntry getRandomSound() {
		return this.allSoundPoolEntries.size() == 0 ? null : (SoundPoolEntry)this.allSoundPoolEntries.get(this.rand.nextInt(this.allSoundPoolEntries.size()));
	}
}
