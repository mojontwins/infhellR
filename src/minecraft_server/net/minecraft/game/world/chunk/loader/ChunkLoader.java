package net.minecraft.game.world.chunk.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityList;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldInfo;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.CompressedStreamTools;
import net.minecraft.game.world.chunk.NibbleArray;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.game.entity.EntityBlockEntity;

/**
 * Chunk persistence to the classic c.X.Z.dat files.
 *
 * <p>The save format distinguishes itself with a {@code Height} tag exactly like the 256-height
 * reference implementation does:</p>
 *
 * <ul>
 *   <li>{@code Height = 256} (new format) stores the chunk as mask-ordered parallel subchunk lists
 *       ({@code SubchunkBlocks}, {@code SubchunkData}, {@code SubchunkSkyLight},
 *       {@code SubchunkBlockLight}) whose element count is given by the bit population of
 *       {@code SubchunkMask}. Subchunks that were never materialized are simply absent.</li>
 *   <li>{@code Height} missing or {@code 128} (legacy format) stores the historical flat
 *       128-tall {@code Blocks}/{@code Data}/{@code SkyLight}/{@code BlockLight} arrays; loading
 *       slices them into the eager subchunks 0-7 and re-runs the light engine when any lighting
 *       information is missing.</li>
 * </ul>
 */
public class ChunkLoader implements IChunkLoader {
	private File saveDir;
	private boolean createIfNecessary;

	public ChunkLoader(File saveDir, boolean createIfNecessary) {
		this.saveDir = saveDir;
		this.createIfNecessary = createIfNecessary;
	}

	private File chunkFileForXZ(int chunkX, int chunkZ) {
		String string3 = "c." + Integer.toString(chunkX, 36) + "." + Integer.toString(chunkZ) + ".dat";
		String string4 = Integer.toString(chunkX & 63, 36);
		String string5 = Integer.toString(chunkZ & 63, 36);
		File file6 = new File(this.saveDir, string4);
		if(!file6.exists()) {
			if(!this.createIfNecessary) {
				return null;
			}

			file6.mkdir();
		}

		file6 = new File(file6, string5);
		if(!file6.exists()) {
			if(!this.createIfNecessary) {
				return null;
			}

			file6.mkdir();
		}

		file6 = new File(file6, string3);
		return !file6.exists() && !this.createIfNecessary ? null : file6;
	}

	public Chunk loadChunk(World world, int x, int z) throws IOException {
		File file4 = this.chunkFileForXZ(x, z);
		if(file4 != null && file4.exists()) {
			try {
				FileInputStream fileInputStream5 = new FileInputStream(file4);
				NBTTagCompound nBTTagCompound6 = CompressedStreamTools.readCompressed(fileInputStream5);
				if(!nBTTagCompound6.hasKey("Level")) {
					System.out.println("Chunk file at " + x + "," + z + " is missing level data, skipping");
					return null;
				}

				NBTTagCompound nBTTagCompound7 = nBTTagCompound6.getCompoundTag("Level");
				// Accept both legacy (flat "Blocks") and new (subchunk lists) layouts.
				if(!nBTTagCompound7.hasKey("Blocks") && nBTTagCompound7.getInteger("Height") != Chunk.SECTION_HEIGHT) {
					System.out.println("Chunk file at " + x + "," + z + " is missing block data, skipping");
					return null;
				}

				Chunk chunk7 = loadChunkIntoWorldFromCompound(world, nBTTagCompound7);
				if(!chunk7.isAtLocation(x, z)) {
					System.out.println("Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + z + ", got " + chunk7.xPosition + ", " + chunk7.zPosition + ")");
					nBTTagCompound6.setInteger("xPos", x);
					nBTTagCompound6.setInteger("zPos", z);
					chunk7 = loadChunkIntoWorldFromCompound(world, nBTTagCompound6.getCompoundTag("Level"));
				}

				chunk7.removeUnknownBlocks();
				if(!nBTTagCompound7.hasKey("LandSurfaceHeightMap")) {
					chunk7.generateLandSurfaceHeightMap();
				}
				return chunk7;
			} catch (Exception exception8) {
				exception8.printStackTrace();
			}
		}

		return null;
	}

	public void saveChunk(World world, Chunk chunk) throws IOException {
		world.checkSessionLock();
		File file3 = this.chunkFileForXZ(chunk.xPosition, chunk.zPosition);
		if(file3.exists()) {
			WorldInfo worldInfo4 = world.getWorldInfo();
			worldInfo4.setSizeOnDisk(worldInfo4.getSizeOnDisk() - file3.length());
		}

		try {
			File file10 = new File(this.saveDir, "tmp_chunk.dat");
			FileOutputStream fileOutputStream5 = new FileOutputStream(file10);
			NBTTagCompound nBTTagCompound6 = new NBTTagCompound();
			NBTTagCompound nBTTagCompound7 = new NBTTagCompound();
			nBTTagCompound6.setTag("Level", nBTTagCompound7);
			storeChunkInCompound(chunk, world, nBTTagCompound7);
			CompressedStreamTools.writeCompressed(nBTTagCompound6, fileOutputStream5);
			fileOutputStream5.close();
			if(file3.exists()) {
				file3.delete();
			}

			file10.renameTo(file3);
			WorldInfo worldInfo8 = world.getWorldInfo();
			worldInfo8.setSizeOnDisk(worldInfo8.getSizeOnDisk() + file3.length());
		} catch (Exception exception9) {
			exception9.printStackTrace();
		}

	}

	public static void storeChunkInCompound(Chunk chunk0, World world1, NBTTagCompound nBTTagCompound2) {
		world1.checkSessionLock();
		nBTTagCompound2.setBoolean("NewFormat", true);
		nBTTagCompound2.setInteger("xPos", chunk0.xPosition);
		nBTTagCompound2.setInteger("zPos", chunk0.zPosition);
		nBTTagCompound2.setLong("LastUpdate", world1.getWorldTime());
		nBTTagCompound2.setByteArray("HeightMap", chunk0.heightMap);
		nBTTagCompound2.setByteArray("LandSurfaceHeightMap", chunk0.landSurfaceHeightMap);
		nBTTagCompound2.setBoolean("TerrainPopulated", chunk0.isTerrainPopulated);

		if(chunk0.blocks != null) {
			// The flat generation buffers are still alive (terrain is generating); store them in the
			// legacy 128-tall layout. The load path migrates such chunks into subchunks seamlessly.
			nBTTagCompound2.setInteger("Height", 128);
			nBTTagCompound2.setByteArray("Blocks", chunk0.blocks);
			nBTTagCompound2.setByteArray("Data", chunk0.data);
		} else {
			// New format: mask-ordered parallel subchunk lists. A subchunk that was never
			// materialized has no entry and its bit in SubchunkMask is clear.
			nBTTagCompound2.setInteger("Height", Chunk.SECTION_HEIGHT);
			// Versions the light planes so loaders can identify saves that predate a lighting fix.
			// Version 2 marks light produced after the sky-plane pre-fill bug was removed (see
			// Chunk.ensureSubchunk); planes stamped at or below 1, or with no tag at all, are
			// rebuilt exactly once on load.
			nBTTagCompound2.setByte("LightVersion", (byte)2);
			int mask = 0;
			for(int section = 0; section < Chunk.SUBCHUNK_COUNT; ++section) {
				if(chunk0.sectionBlocks[section] != null) {
					mask |= 1 << section;
				}
			}
			nBTTagCompound2.setShort("SubchunkMask", (short)mask);
			NBTTagList blocksList = new NBTTagList();
			NBTTagList dataList = new NBTTagList();
			NBTTagList skyList = new NBTTagList();
			NBTTagList blockLightList = new NBTTagList();
			for(int section = 0; section < Chunk.SUBCHUNK_COUNT; ++section) {
				if((mask & (1 << section)) != 0) {
					blocksList.setTag(new NBTTagByteArray(chunk0.sectionBlocks[section].clone()));
					dataList.setTag(new NBTTagByteArray(chunk0.sectionData[section].clone()));
					skyList.setTag(new NBTTagByteArray(chunk0.skyLightMap[section].data.clone()));
					blockLightList.setTag(new NBTTagByteArray(chunk0.blockLightMap[section].data.clone()));
				}
			}
			nBTTagCompound2.setTag("SubchunkBlocks", blocksList);
			nBTTagCompound2.setTag("SubchunkData", dataList);
			nBTTagCompound2.setTag("SubchunkSkyLight", skyList);
			nBTTagCompound2.setTag("SubchunkBlockLight", blockLightList);
		}

		nBTTagCompound2.setBoolean("hasBuilding", chunk0.hasBuilding);
		nBTTagCompound2.setBoolean("hasRoad", chunk0.hasRoad);
		nBTTagCompound2.setBoolean("isUrbanChunk", chunk0.isUrbanChunk);
		nBTTagCompound2.setBoolean("hasUnderwaterRuin", chunk0.hasUnderwaterRuin);
		nBTTagCompound2.setBoolean("hasFeature", chunk0.hasFeature);
		nBTTagCompound2.setBoolean("isOcean", chunk0.isOcean);

		chunk0.hasEntities = false;

		if(chunk0.biomeGenCache != null) {
			byte[] biomes = new byte[256];
			for(int i = 0; i < 256; ++i) {
				biomes[i] = (byte)chunk0.biomeGenCache[i].biomeCode;
			}
			nBTTagCompound2.setByteArray("Biomes", biomes);
		}
		if(chunk0.temperatureCache != null && chunk0.humidityCache != null) {
			byte[] temperature = new byte[256];
			byte[] humidity = new byte[256];
			for(int i = 0; i < 256; ++i) {
				temperature[i] = (byte)(int)(chunk0.temperatureCache[i] * 255.0F);
				humidity[i] = (byte)(int)(chunk0.humidityCache[i] * 255.0F);
			}
			nBTTagCompound2.setByteArray("Temperature", temperature);
			nBTTagCompound2.setByteArray("Humidity", humidity);
		}

		NBTTagList nBTTagList3 = new NBTTagList();
		NBTTagCompound nBTTagCompound7;
		{
			Iterator<Entity> iterator5;
			for(int i4 = 0; i4 < chunk0.entities.length; ++i4) {
				iterator5 = chunk0.entities[i4].iterator();

				while(iterator5.hasNext()) {
					Entity entity6 = (Entity)iterator5.next();
					chunk0.hasEntities = true;
					nBTTagCompound7 = new NBTTagCompound();
					if(entity6.addEntityID(nBTTagCompound7)) {
						nBTTagList3.setTag(nBTTagCompound7);
					}
				}
			}
		}
		nBTTagCompound2.setTag("Entities", nBTTagList3);

		NBTTagList nBTTagList8 = new NBTTagList();
		{
			Iterator<TileEntity> iterator6 = chunk0.chunkTileEntityMap.values().iterator();

			while(iterator6.hasNext()) {
				TileEntity tileEntity9 = (TileEntity)iterator6.next();
				nBTTagCompound7 = new NBTTagCompound();
				tileEntity9.writeToNBT(nBTTagCompound7);
				nBTTagList8.setTag(nBTTagCompound7);
			}
		}
		nBTTagCompound2.setTag("TileEntities", nBTTagList8);

		NBTTagList nBTTagList = new NBTTagList();
		{
			Iterator<EntityBlockEntity>iterator = chunk0.chunkSpecialEntityMap.values().iterator();

			while(iterator.hasNext()) {
				EntityBlockEntity entity = iterator.next();
				NBTTagCompound nBTTagCompound = new NBTTagCompound();
				if(entity.addEntityID(nBTTagCompound)) {
					nBTTagList.setTag(nBTTagCompound);
				}
			}
		}
		nBTTagCompound2.setTag("SpecialEntities", nBTTagList);
	}

	public static Chunk loadChunkIntoWorldFromCompound(World world0, NBTTagCompound nBTTagCompound1) {
		int i2 = nBTTagCompound1.getInteger("xPos");
		int i3 = nBTTagCompound1.getInteger("zPos");
		Chunk chunk4 = new Chunk(world0, i2, i3);

		// Format detection: new saves write Height == SECTION_HEIGHT; legacy saves have it missing
		// (read as 0) or set to 128.
		int storedHeight = nBTTagCompound1.getInteger("Height");
		boolean subchunkFormat = storedHeight == Chunk.SECTION_HEIGHT;
		boolean hasSkyPlanes = false;
		boolean hasBlockPlanes = false;
		// LightVersion stamps which build produced the light planes: 2 = post pre-fill fix (trusted),
		// 1 = first 256-height build (all-bright from the sky pre-fill) and legacy slices that could
		// be scrambled; anything below 2 makes the Starlight relight below run exactly once to heal.
		boolean hasLightVersion = false;

		if(subchunkFormat) {
			// New format: reconstruct the chunk from the mask-ordered parallel subchunk lists.
			int mask = nBTTagCompound1.getShort("SubchunkMask") & 0xFFFF;
			NBTTagList blocksList = nBTTagCompound1.getTagList("SubchunkBlocks");
			NBTTagList dataList = nBTTagCompound1.getTagList("SubchunkData");
			NBTTagList skyList = nBTTagCompound1.getTagList("SubchunkSkyLight");
			NBTTagList blockLightList = nBTTagCompound1.getTagList("SubchunkBlockLight");
			int cellCount = Chunk.SECTION_SIZE * Chunk.SECTION_SIZE * Chunk.SECTION_SIZE;
			int listIndex = 0;

			for(int section = 0; section < Chunk.SUBCHUNK_COUNT; ++section) {
				if((mask & (1 << section)) != 0) {
					chunk4.ensureSubchunk(section);
					NBTTagByteArray blockTag = (NBTTagByteArray)blocksList.tagAt(listIndex);
					System.arraycopy(blockTag.byteArray, 0, chunk4.sectionBlocks[section], 0, cellCount);
					NBTTagByteArray dataTag = (NBTTagByteArray)dataList.tagAt(listIndex);
					System.arraycopy(dataTag.byteArray, 0, chunk4.sectionData[section], 0, cellCount);

					if(skyList != null && skyList.tagCount() > listIndex) {
						byte[] plane = ((NBTTagByteArray)skyList.tagAt(listIndex)).byteArray;
						System.arraycopy(plane, 0, chunk4.skyLightMap[section].data, 0, Math.min(plane.length, chunk4.skyLightMap[section].data.length));
					}
					if(blockLightList != null && blockLightList.tagCount() > listIndex) {
						byte[] plane = ((NBTTagByteArray)blockLightList.tagAt(listIndex)).byteArray;
						System.arraycopy(plane, 0, chunk4.blockLightMap[section].data, 0, Math.min(plane.length, chunk4.blockLightMap[section].data.length));
					}

					++listIndex;
				}
			}

			// The light planes are complete only if every mask entry has a matching list element.
			hasSkyPlanes = skyList != null && skyList.tagCount() >= listIndex;
			hasBlockPlanes = blockLightList != null && blockLightList.tagCount() >= listIndex;
			// Only light stamped by a post-fix build (version >= 2) is trusted as-is; version 1 or
			// an absent tag means the planes may be all-bright (saved by the pre-fill bug) and are
			// rebuilt once through the relight branch below.
			hasLightVersion = nBTTagCompound1.getByte("LightVersion") >= 2;
			chunk4.recomputeEmptyFlags();
		} else {
			// Legacy format: slice the flat 128-tall arrays into the eager subchunks 0-7.
			byte[] flatBlocks = nBTTagCompound1.getByteArray("Blocks");
			if(flatBlocks.length == 128 * Chunk.SECTION_SIZE * Chunk.SECTION_SIZE) {
				byte[] flatData;
				if(nBTTagCompound1.getBoolean("NewFormat")) {
					flatData = nBTTagCompound1.getByteArray("Data");
				} else {
					System.out.println("Converting chunk metadata from old format");
					flatData = new NibbleArray(nBTTagCompound1.getByteArray("Data")).asByteArray();
					chunk4.isModified = true; 		// So it gets saved right away
				}
				if(flatData == null || flatData.length != flatBlocks.length) {
					flatData = new byte[flatBlocks.length];
				}

				// Re-slice the flat buffers into subchunks and drop the flat storage (same path as
				// terrain generation uses).
				chunk4.loadFlatBlocks(flatBlocks, flatData);

				// Move the saved light nibbles into their per-subchunk planes.
				// The legacy flat planes are column-major: the 128 y-nibbles of a column
				// ((x,z), base (x<<11|z<<7) nibbles / (x<<10|z<<6) bytes) are stored contiguously,
				// so each subchunk's 16-local-y nibbles of a column are 8 *strided* bytes. Copying
				// contiguous section-sized blocks (as done initially) scrambled the columns and
				// blacked out every migrated world.
				int flatSectionCount = 128 >> 4;
				int nibblesPerSection = Chunk.SECTION_SIZE * Chunk.SECTION_SIZE * Chunk.SECTION_SIZE >> 1;
				int flatByteLength = flatSectionCount * nibblesPerSection;
				byte[] flatSky = nBTTagCompound1.getByteArray("SkyLight");
				byte[] flatBlock = nBTTagCompound1.getByteArray("BlockLight");
				if(flatSky.length >= flatByteLength) {
					hasSkyPlanes = true;
					for(int section = 0; section < flatSectionCount; ++section) {
						NibbleArray plane = chunk4.skyLightMap[section];
						for(int columnBase = 0; columnBase < 256; ++columnBase) {
							int x = columnBase >> 4;
							int z = columnBase & 15;
							// Flat source: 8 bytes (16 y-nibbles) at the section's slice of the
							// column. Section-plane destination: the column's 8 nibble bytes
							// (local-y 0-15, packed even-y low like the flat layout).
							int sourceByte = (x << 10) | (z << 6) | (section << 3);
							int destByte = (x << 7) | (z << 3);
							System.arraycopy(flatSky, sourceByte, plane.data, destByte, 8);
						}
					}
				}
				if(flatBlock.length >= flatByteLength) {
					hasBlockPlanes = true;
					for(int section = 0; section < flatSectionCount; ++section) {
						NibbleArray plane = chunk4.blockLightMap[section];
						for(int columnBase = 0; columnBase < 256; ++columnBase) {
							int x = columnBase >> 4;
							int z = columnBase & 15;
							int sourceByte = (x << 10) | (z << 6) | (section << 3);
							int destByte = (x << 7) | (z << 3);
							System.arraycopy(flatBlock, sourceByte, plane.data, destByte, 8);
						}
					}
				}
				// Original legacy files never carried the scrambled-plane state: with the strided
				// slice above their light is fully trustworthy, so skip the automatic relight only
				// when a plane is actually absent (handled by the regen condition below).
				hasLightVersion = true;
			}
		}

		chunk4.isTerrainPopulated = nBTTagCompound1.getBoolean("TerrainPopulated");

		chunk4.hasBuilding = nBTTagCompound1.getBoolean("hasBuilding");
		chunk4.hasRoad = nBTTagCompound1.getBoolean("hasRoad");
		chunk4.isUrbanChunk = nBTTagCompound1.getBoolean("isUrbanChunk");
		chunk4.hasUnderwaterRuin = nBTTagCompound1.getBoolean("hasUnderwaterRuin");
		chunk4.hasFeature = nBTTagCompound1.getBoolean("hasFeature");
		chunk4.isOcean = nBTTagCompound1.getBoolean("isOcean");

		chunk4.heightMap = nBTTagCompound1.getByteArray("HeightMap");
		byte[] landSurfaceHeightMap = nBTTagCompound1.getByteArray("LandSurfaceHeightMap");
		if(landSurfaceHeightMap.length == 256) {
			chunk4.landSurfaceHeightMap = landSurfaceHeightMap;
		}

		// If the height map or any light plane is missing the Starlight stamp, rebuild everything
		// through initLightingForRealNotJustHeightmap: initSkylight descends from y = 256 through
		// the null top subchunks and only writes into the materialized sections, so no top-half
		// allocation occurs during migration.
		if(chunk4.heightMap == null || chunk4.heightMap.length != 256 || !hasSkyPlanes || !hasBlockPlanes || !hasLightVersion) {
			chunk4.heightMap = new byte[256];
			chunk4.generateHeightMap();
			chunk4.generateLandSurfaceHeightMap();
			chunk4.initLightingForRealNotJustHeightmap();
			chunk4.isModified = true; 		// So it gets re-saved in the new format
		}

		byte[] biomeCodes = nBTTagCompound1.getByteArray("Biomes");
		byte[] temperature = nBTTagCompound1.getByteArray("Temperature");
		byte[] humidity = nBTTagCompound1.getByteArray("Humidity");
		if(biomeCodes.length == 256) {
			BiomeGenBase[] biomes = new BiomeGenBase[256];
			for(int i = 0; i < 256; ++i) {
				biomes[i] = BiomeGenBase.getBiomeByCode(biomeCodes[i] & 255);
			}
			chunk4.biomeGenCache = biomes;
		}
		if(temperature.length == 256 && humidity.length == 256) {
			float[] temperatureGrid = new float[256];
			float[] humidityGrid = new float[256];
			for(int i = 0; i < 256; ++i) {
				temperatureGrid[i] = (temperature[i] & 255) / 255.0F;
				humidityGrid[i] = (humidity[i] & 255) / 255.0F;
			}
			chunk4.setClimateCache(temperatureGrid, humidityGrid);
		}

		NBTTagList nBTTagList5 = nBTTagCompound1.getTagList("Entities");
		if(nBTTagList5 != null) {
			for(int i6 = 0; i6 < nBTTagList5.tagCount(); ++i6) {
				NBTTagCompound nBTTagCompound7 = (NBTTagCompound)nBTTagList5.tagAt(i6);
				Entity entity8 = EntityList.createEntityFromNBT(nBTTagCompound7, world0);
				if(entity8 instanceof EntityBlockEntity) {
				} else {
					chunk4.hasEntities = true;
					if(entity8 != null) {
						chunk4.addEntity(entity8);
					}
				}
			}
		}

		NBTTagList nBTTagList10 = nBTTagCompound1.getTagList("TileEntities");
		if(nBTTagList10 != null) {
			for(int i11 = 0; i11 < nBTTagList10.tagCount(); ++i11) {
				NBTTagCompound nBTTagCompound12 = (NBTTagCompound)nBTTagList10.tagAt(i11);
				TileEntity tileEntity9 = TileEntity.createAndLoadEntity(nBTTagCompound12);
				if(tileEntity9 != null) {
					chunk4.addTileEntity(tileEntity9);
				}
			}
		}

		NBTTagList nBTTagList = nBTTagCompound1.getTagList("SpecialEntities");
		if(nBTTagList != null) {
			for(int i = 0; i < nBTTagList.tagCount(); i ++) {
				NBTTagCompound nBTTagCompound = (NBTTagCompound)nBTTagList.tagAt(i);
				EntityBlockEntity entity = (EntityBlockEntity) EntityList.createEntityFromNBT(nBTTagCompound, world0);
				// System.out.println ("Loaded " + entity);
				if(entity != null) {
					chunk4.addSpecialEntity(entity);
				}
			}
		}

		return chunk4;
	}

	public void chunkTick() {
	}

	public void saveExtraData() {
	}

	public void saveExtraChunkData(World world1, Chunk chunk2) throws IOException {
	}
}