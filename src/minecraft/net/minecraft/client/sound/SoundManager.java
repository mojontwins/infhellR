package net.minecraft.client.sound;

import java.io.File;
import java.net.URL;
import java.util.Random;

import net.minecraft.client.GameSettings;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.EntityLiving;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager {
	private static SoundSystem soundSystem;
	private SoundPool soundPoolSounds = new SoundPool();
	private SoundPool soundPoolStreaming = new SoundPool();
	private SoundPool soundPoolMusic = new SoundPool();
	private int playedSoundsCount = 0;
	private GameSettings options;
	private static boolean loaded = false;
	private Random rand = new Random();
	private int ticksBeforeMusic = this.rand.nextInt(12000);

	public void loadSoundSettings(GameSettings gameSettings) {
		this.soundPoolStreaming.isGetRandomSound = false;
		this.options = gameSettings;
		if(!loaded && (GameSettingsValues.soundVolume != 0.0F || GameSettingsValues.musicVolume != 0.0F)) {
			this.tryToSetLibraryAndCodecs();
		}

	}

	private void tryToSetLibraryAndCodecs() {
		try {
			float savedSoundVolume = GameSettingsValues.soundVolume;
			float savedMusicVolume = GameSettingsValues.musicVolume;
			GameSettingsValues.soundVolume = 0.0F;
			GameSettingsValues.musicVolume = 0.0F;
			this.options.saveOptions();
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setCodec("mus", CodecMus.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
			soundSystem = new SoundSystem();
			GameSettingsValues.soundVolume = savedSoundVolume;
			GameSettingsValues.musicVolume = savedMusicVolume;
			this.options.saveOptions();
		} catch (Throwable error) {
			error.printStackTrace();
			System.err.println("error linking with the LibraryJavaSound plug-in");
		}

		loaded = true;
	}

	public void onSoundOptionsChanged() {
		if(!loaded && (GameSettingsValues.soundVolume != 0.0F || GameSettingsValues.musicVolume != 0.0F)) {
			this.tryToSetLibraryAndCodecs();
		}

		if(loaded) {
			if(GameSettingsValues.musicVolume == 0.0F) {
				soundSystem.stop("BgMusic");
			} else {
				soundSystem.setVolume("BgMusic", GameSettingsValues.musicVolume);
			}
		}

	}

	public void closeMinecraft() {
		if(loaded) {
			soundSystem.cleanup();
		}

	}

	public void addSound(String name, File file) {
		this.soundPoolSounds.addSound(name, file);
	}
	
	public void addSoundURL(String name, URL url) {
		this.soundPoolSounds.addSoundURL(name, url);
	}

	public void addStreaming(String name, File file) {
		this.soundPoolStreaming.addSound(name, file);
	}

	public void addMusic(String name, File file) {
		this.soundPoolMusic.addSound(name, file);
	}

	public void playRandomMusicIfReady() {
		if(loaded && GameSettingsValues.musicVolume != 0.0F) {
			if(!soundSystem.playing("BgMusic") && !soundSystem.playing("streaming")) {
				if(this.ticksBeforeMusic > 0) {
					--this.ticksBeforeMusic;
					return;
				}

				SoundPoolEntry musicEntry = this.soundPoolMusic.getRandomSound();
				if(musicEntry != null) {
					this.ticksBeforeMusic = this.rand.nextInt(12000) + 12000;
					soundSystem.backgroundMusic("BgMusic", musicEntry.soundUrl, musicEntry.soundName, false);
					soundSystem.setVolume("BgMusic", GameSettingsValues.musicVolume);
					soundSystem.play("BgMusic");
				}
			}

		}
	}

	public void setListener(EntityLiving entity, float delta) {
		if(loaded && GameSettingsValues.soundVolume != 0.0F) {
			if(entity != null) {
				float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * delta;
				double interpX = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)delta;
				double interpY = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)delta;
				double interpZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)delta;
				float cosYaw = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
				float sinYaw = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
				float rightX = -sinYaw;
				float upY = 0.0F;
				float forwardX = -cosYaw;
				float forwardZ = 0.0F;
				float upX = 1.0F;
				float upZ = 0.0F;
				soundSystem.setListenerPosition((float)interpX, (float)interpY, (float)interpZ);
				soundSystem.setListenerOrientation(rightX, upY, forwardX, forwardZ, upX, upZ);
			}
		}
	}

	public void playStreaming(String soundName, float x, float y, float z, float volume, float pitch) {
		if(loaded && GameSettingsValues.soundVolume != 0.0F) {
			String sourceName = "streaming";
			if(soundSystem.playing("streaming")) {
				soundSystem.stop("streaming");
			}

			if(soundName != null) {
				SoundPoolEntry streamingEntry = this.soundPoolStreaming.getRandomSoundFromSoundPool(soundName);
				if(streamingEntry != null && volume > 0.0F) {
					if(soundSystem.playing("BgMusic")) {
						soundSystem.stop("BgMusic");
					}

					float distance = 16.0F;
					soundSystem.newStreamingSource(true, sourceName, streamingEntry.soundUrl, streamingEntry.soundName, false, x, y, z, 2, distance * 4.0F);
					soundSystem.setVolume(sourceName, 0.5F * GameSettingsValues.soundVolume);
					soundSystem.play(sourceName);
				}

			}
		}
	}

	public void playSound(String soundName, float x, float y, float z, float volume, float pitch) {
		if(loaded && GameSettingsValues.soundVolume != 0.0F) {
			SoundPoolEntry soundEntry = this.soundPoolSounds.getRandomSoundFromSoundPool(soundName);
			if(soundEntry != null && volume > 0.0F) {
				this.playedSoundsCount = (this.playedSoundsCount + 1) % 256;
				String sourceName = "sound_" + this.playedSoundsCount;
				float distance = 16.0F;
				if(volume > 1.0F) {
					distance *= volume;
				}

				soundSystem.newSource(volume > 1.0F, sourceName, soundEntry.soundUrl, soundEntry.soundName, false, x, y, z, 2, distance);
				soundSystem.setPitch(sourceName, pitch);
				if(volume > 1.0F) {
					volume = 1.0F;
				}

				soundSystem.setVolume(sourceName, volume * GameSettingsValues.soundVolume);
				soundSystem.play(sourceName);
			}

		}
	}

	public void playSoundFX(String soundName, float volume, float pitch) {
		if(loaded && GameSettingsValues.soundVolume != 0.0F) {
			SoundPoolEntry fxEntry = this.soundPoolSounds.getRandomSoundFromSoundPool(soundName);
			if(fxEntry != null) {
				this.playedSoundsCount = (this.playedSoundsCount + 1) % 256;
				String sourceName = "sound_" + this.playedSoundsCount;
				soundSystem.newSource(false, sourceName, fxEntry.soundUrl, fxEntry.soundName, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);
				if(volume > 1.0F) {
					volume = 1.0F;
				}

				volume *= 0.25F;
				soundSystem.setPitch(sourceName, pitch);
				soundSystem.setVolume(sourceName, volume * GameSettingsValues.soundVolume);
				soundSystem.play(sourceName);
			}

		}
	}
}
