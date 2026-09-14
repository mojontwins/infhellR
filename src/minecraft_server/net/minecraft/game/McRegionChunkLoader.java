package net.minecraft.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldInfo;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.CompressedStreamTools;
import net.minecraft.game.world.chunk.loader.ChunkLoader;
import net.minecraft.game.world.chunk.loader.IChunkLoader;
import net.minecraft.game.world.chunk.loader.RegionFileCache;
import net.minecraft.nbt.NBTTagCompound;

public class McRegionChunkLoader implements IChunkLoader {
	private final File saveDirectory;

	public McRegionChunkLoader(File file1) {
		this.saveDirectory = file1;
	}

	public Chunk loadChunk(World world1, int i2, int i3) throws IOException {
		DataInputStream dataInputStream4 = RegionFileCache.getChunkInputStream(this.saveDirectory, i2, i3);
		if(dataInputStream4 != null) {
			NBTTagCompound nBTTagCompound5 = CompressedStreamTools.readNBT(dataInputStream4);
			if(!nBTTagCompound5.hasKey("Level")) {
				System.out.println("Chunk file at " + i2 + "," + i3 + " is missing level data, skipping");
				return null;
			} else if(!nBTTagCompound5.getCompoundTag("Level").hasKey("Blocks") && nBTTagCompound5.getCompoundTag("Level").getInteger("Height") != Chunk.SECTION_HEIGHT) {
				/* New-format chunks store their blocks as a SubchunkMask plus parallel section
				   lists with Height = SECTION_HEIGHT, so the absent "Blocks" tag is expected
				   there. Only reject a chunk when it has neither the classic Blocks array nor
				   the new-format height marker — i.e. it is genuinely missing block data. */
				System.out.println("Chunk file at " + i2 + "," + i3 + " is missing block data, skipping");
				return null;
			} else {
				Chunk chunk6 = ChunkLoader.loadChunkIntoWorldFromCompound(world1, nBTTagCompound5.getCompoundTag("Level"));
				if(!chunk6.isAtLocation(i2, i3)) {
					System.out.println("Chunk file at " + i2 + "," + i3 + " is in the wrong location; relocating. (Expected " + i2 + ", " + i3 + ", got " + chunk6.xPosition + ", " + chunk6.zPosition + ")");
					nBTTagCompound5.setInteger("xPos", i2);
					nBTTagCompound5.setInteger("zPos", i3);
					chunk6 = ChunkLoader.loadChunkIntoWorldFromCompound(world1, nBTTagCompound5.getCompoundTag("Level"));
				}

				chunk6.removeUnknownBlocks();
				chunk6.generateLandSurfaceHeightMap();
				return chunk6;
			}
		} else {
			return null;
		}
	}

	public void saveChunk(World world1, Chunk chunk2) throws IOException {
		world1.checkSessionLock();

		try {
			DataOutputStream dataOutputStream3 = RegionFileCache.getChunkOutputStream(this.saveDirectory, chunk2.xPosition, chunk2.zPosition);
			NBTTagCompound nBTTagCompound4 = new NBTTagCompound();
			NBTTagCompound nBTTagCompound5 = new NBTTagCompound();
			nBTTagCompound4.setTag("Level", nBTTagCompound5);
			ChunkLoader.storeChunkInCompound(chunk2, world1, nBTTagCompound5);
			CompressedStreamTools.writeNBT(nBTTagCompound4, dataOutputStream3);
			dataOutputStream3.close();
			WorldInfo worldInfo6 = world1.getWorldInfo();
			worldInfo6.setSizeOnDisk(worldInfo6.getSizeOnDisk() + (long)RegionFileCache.getSizeDelta(this.saveDirectory, chunk2.xPosition, chunk2.zPosition));
		} catch (Exception exception7) {
			exception7.printStackTrace();
		}

	}

	public void saveExtraChunkData(World world1, Chunk chunk2) throws IOException {
	}

	public void chunkTick() {
	}

	public void saveExtraData() {
	}

	/**
	 * True when a chunk is present in this save directory's region files. A region slot
	 * is only filled after the populate stage wrote the chunk, so its blocks (including
	 * any feature structures) are final.
	 *
	 * <p>Note that {@link RegionFileCache#getRegionFile(File,int,int)} creates the region
	 * file when missing, exactly as {@link #loadChunk(World,int,int)} already does.
	 */
	@Override
	public boolean chunkExists(World world, int chunkX, int chunkZ) {
		return RegionFileCache.getRegionFile(this.saveDirectory, chunkX, chunkZ).isChunkSaved(chunkX & 31, chunkZ & 31);
	}
}
