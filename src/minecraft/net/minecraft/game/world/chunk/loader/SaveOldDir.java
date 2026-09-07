package net.minecraft.game.world.chunk.loader;

import java.io.File;
import java.util.List;

import net.minecraft.game.McRegionChunkLoader;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.WorldInfo;
import net.minecraft.game.world.WorldProvider;
import net.minecraft.game.world.WorldProviderHell;

public class SaveOldDir extends SaveHandler {
	public SaveOldDir(File file1, String string2, boolean z3) {
		super(file1, string2, z3);
	}

	public IChunkLoader getChunkLoader(WorldProvider worldProvider1) {
		File file2 = this.getSaveDirectory();
		if(worldProvider1 instanceof WorldProviderHell) {
			File file3 = new File(file2, "DIM-1");
			file3.mkdirs();
			return new McRegionChunkLoader(file3);
		} else {
			return new McRegionChunkLoader(file2);
		}
	}

	public void saveWorldInfoAndPlayer(WorldInfo worldInfo1, List<EntityPlayer> list2) {
		worldInfo1.setSaveVersion(19132);
		super.saveWorldInfoAndPlayer(worldInfo1, list2);
	}

	public void s_func_22093_e() {
		RegionFileCache.closeRegionFiles();
	}
}
