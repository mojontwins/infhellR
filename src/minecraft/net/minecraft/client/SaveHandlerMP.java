package net.minecraft.client;

import java.io.File;
import java.util.List;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.WorldInfo;
import net.minecraft.game.world.WorldProvider;
import net.minecraft.game.world.chunk.loader.IChunkLoader;
import net.minecraft.game.world.chunk.loader.ISaveHandler;

public class SaveHandlerMP implements ISaveHandler {
	public WorldInfo loadWorldInfo() {
		return null;
	}

	public void checkSessionLock() {
	}

	public IChunkLoader getChunkLoader(WorldProvider worldProvider) {
		return null;
	}

	public void saveWorldInfoAndPlayer(WorldInfo worldInfo, List<EntityPlayer> players) {
	}

	public void saveWorldInfo(WorldInfo worldInfo) {
	}

	public File getMapFileFromName(String name) {
		return null;
	}

	@Override
	public void writePlayerData(EntityPlayer player) {
	}

	@Override
	public void readPlayerData(EntityPlayer player) {
	}

	@Override
	public ISaveHandler getSaveHandler() {
		return null;
	}

	@Override
	public void s_func_22093_e() {
	}
}
