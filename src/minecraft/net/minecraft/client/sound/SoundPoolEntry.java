package net.minecraft.client.sound;

import java.net.URL;

public class SoundPoolEntry {
	public String soundName;
	public URL soundUrl;

	public SoundPoolEntry(String name, URL url) {
		this.soundName = name;
		this.soundUrl = url;
	}
}
