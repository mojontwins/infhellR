package net.minecraft.game.world.chunk.loader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.game.MinecraftException;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.WorldInfo;
import net.minecraft.game.world.WorldProvider;
import net.minecraft.game.world.WorldProviderHell;
import net.minecraft.game.world.chunk.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class SaveHandler implements ISaveHandler {
	private static final Logger logger = Logger.getLogger("Minecraft");
	private final File saveDirectory;
	private final File saveHandler;
	private final File mapDataDir;
	private final long now = System.currentTimeMillis();

	public SaveHandler(File file1, String string2, boolean z3) {
		this.saveDirectory = new File(file1, string2);
		this.saveDirectory.mkdirs();
		this.saveHandler = new File(this.saveDirectory, "players");
		this.mapDataDir = new File(this.saveDirectory, "data");
		this.mapDataDir.mkdirs();
		if(z3) {
			this.saveHandler.mkdirs();
		}

		this.setSessionLock();
	}

	private void setSessionLock() {
		try {
			File file1 = new File(this.saveDirectory, "session.lock");
			DataOutputStream dataOutputStream2 = new DataOutputStream(new FileOutputStream(file1));

			try {
				dataOutputStream2.writeLong(this.now);
			} finally {
				dataOutputStream2.close();
			}

		} catch (IOException iOException7) {
			iOException7.printStackTrace();
			throw new RuntimeException("Failed to check session lock, aborting");
		}
	}

	protected File getSaveDirectory() {
		return this.saveDirectory;
	}

	public void checkSessionLock() {
		try {
			File file1 = new File(this.saveDirectory, "session.lock");
			DataInputStream dataInputStream2 = new DataInputStream(new FileInputStream(file1));

			try {
				if(dataInputStream2.readLong() != this.now) {
					throw new MinecraftException("The save is being accessed from another location, aborting");
				}
			} finally {
				dataInputStream2.close();
			}

		} catch (IOException iOException7) {
			throw new MinecraftException("Failed to check session lock, aborting");
		}
	}

	public IChunkLoader getChunkLoader(WorldProvider worldProvider1) {
		if(worldProvider1 instanceof WorldProviderHell) {
			File file2 = new File(this.saveDirectory, "DIM-1");
			file2.mkdirs();
			return new ChunkLoader(file2, true);
		} else {
			return new ChunkLoader(this.saveDirectory, true);
		}
	}

	public WorldInfo loadWorldInfo() {
		File file1 = new File(this.saveDirectory, "level.dat");
		NBTTagCompound nBTTagCompound2;
		NBTTagCompound nBTTagCompound3;
		if(file1.exists()) {
			try {
				nBTTagCompound2 = CompressedStreamTools.readCompressed(new FileInputStream(file1));
				nBTTagCompound3 = nBTTagCompound2.getCompoundTag("Data");
				return new WorldInfo(nBTTagCompound3);
			} catch (Exception exception5) {
				exception5.printStackTrace();
			}
		}

		file1 = new File(this.saveDirectory, "level.dat_old");
		if(file1.exists()) {
			try {
				nBTTagCompound2 = CompressedStreamTools.readCompressed(new FileInputStream(file1));
				nBTTagCompound3 = nBTTagCompound2.getCompoundTag("Data");
				return new WorldInfo(nBTTagCompound3);
			} catch (Exception exception4) {
				exception4.printStackTrace();
			}
		}

		return null;
	}

	public void saveWorldInfoAndPlayer(WorldInfo worldInfo1, List<EntityPlayer> list2) {
		NBTTagCompound nBTTagCompound3 = worldInfo1.getNBTTagCompoundWithPlayer(list2);
		NBTTagCompound nBTTagCompound4 = new NBTTagCompound();
		nBTTagCompound4.setTag("Data", nBTTagCompound3);

		try {
			File file5 = new File(this.saveDirectory, "level.dat_new");
			File file6 = new File(this.saveDirectory, "level.dat_old");
			File file7 = new File(this.saveDirectory, "level.dat");
			CompressedStreamTools.writeCompressed(nBTTagCompound4, new FileOutputStream(file5));
			if(file6.exists()) {
				file6.delete();
			}

			file7.renameTo(file6);
			if(file7.exists()) {
				file7.delete();
			}

			file5.renameTo(file7);
			if(file5.exists()) {
				file5.delete();
			}
		} catch (Exception exception8) {
			exception8.printStackTrace();
		}

	}

	public void saveWorldInfo(WorldInfo worldInfo1) {
		NBTTagCompound nBTTagCompound2 = worldInfo1.getNBTTagCompound();
		NBTTagCompound nBTTagCompound3 = new NBTTagCompound();
		nBTTagCompound3.setTag("Data", nBTTagCompound2);

		try {
			File file4 = new File(this.saveDirectory, "level.dat_new");
			File file5 = new File(this.saveDirectory, "level.dat_old");
			File file6 = new File(this.saveDirectory, "level.dat");
			CompressedStreamTools.writeCompressed(nBTTagCompound3, new FileOutputStream(file4));
			if(file5.exists()) {
				file5.delete();
			}

			file6.renameTo(file5);
			if(file6.exists()) {
				file6.delete();
			}

			file4.renameTo(file6);
			if(file4.exists()) {
				file4.delete();
			}
		} catch (Exception exception7) {
			exception7.printStackTrace();
		}

	}

	public void writePlayerData(EntityPlayer entityPlayer1) {
		try {
			NBTTagCompound nBTTagCompound2 = new NBTTagCompound();
			entityPlayer1.writeToNBT(nBTTagCompound2);
			File file3 = new File(this.saveHandler, "_tmp_.dat");
			File file4 = new File(this.saveHandler, entityPlayer1.username + ".dat");
			CompressedStreamTools.writeCompressed(nBTTagCompound2, new FileOutputStream(file3));
			if(file4.exists()) {
				file4.delete();
			}

			file3.renameTo(file4);
		} catch (Exception exception5) {
			logger.warning("Failed to save player data for " + entityPlayer1.username);
		}

	}

	public void readPlayerData(EntityPlayer entityPlayer1) {
		NBTTagCompound nBTTagCompound2 = this.getPlayerData(entityPlayer1.username);
		if(nBTTagCompound2 != null) {
			entityPlayer1.readFromNBT(nBTTagCompound2);
		}

	}

	public NBTTagCompound getPlayerData(String string1) {
		try {
			FileInputStream fileInputStream = null;
			try {
				File file2 = new File(this.saveHandler, string1 + ".dat");
				fileInputStream = new FileInputStream(file2);
	
				if(file2.exists()) {
					return CompressedStreamTools.readCompressed(fileInputStream);
				}
			} catch (Exception exception3) {
				logger.warning("Failed to load player data for " + string1);
			} finally {
				if(fileInputStream != null) fileInputStream.close();
			}
		} catch (Exception e) {}

		return null;
	}

	public ISaveHandler getSaveHandler() {
		return this;
	}

	public void s_func_22093_e() {
	}

	public File getMapFileFromName(String string1) {
		return new File(this.mapDataDir, string1 + ".dat");
	}

	public static Logger getLogger() {
		return logger;
	}
}
