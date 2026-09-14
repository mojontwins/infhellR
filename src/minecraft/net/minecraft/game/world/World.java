package net.minecraft.game.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.game.worldedit.WorldEdit;

import ca.spottedleaf.starlight.StarlightEngine;
import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.MapDataBase;
import net.minecraft.game.MapStorage;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockFluid;
import net.minecraft.game.world.block.BlockPos;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.ChunkProvider;
import net.minecraft.game.world.chunk.ChunkProviderLoadOrGenerate;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.chunk.loader.IChunkLoader;
import net.minecraft.game.world.chunk.loader.ISaveHandler;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.path.PathEntity;
import net.minecraft.game.world.path.Pathfinder;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.game.Seasons;
import net.minecraft.game.entity.EntityBlockEntity;
import net.minecraft.game.entity.EntityLightningBolt;
import net.minecraft.game.entity.PathfinderRelease;
import net.minecraft.game.entity.PlayerPositionComparator;
import net.minecraft.game.world.block.BlockFlower;
import net.minecraft.game.world.block.BlockState;

/**
 * The world: the shared client/server hub that owns the block grid, weather, time, entities and
 * tile entities, and exposes the block/entity queries, entity lifecycle, block tick scheduling,
 * sky-light dial and atmospheric colour logic through a set of injected collaborators
 * ({@link #entityQueryService}, {@link #entityManager}, {@link #blockTickScheduler},
 * {@link #skylightTracker}) plus the static {@link AtmosphereCalculator}. The collaborators were
 * split out of this class during the refactor; {@code World} keeps the public entry points as thin
 * delegates so external callers are unchanged.
 *
 * <p>Per-block lighting is handled by the two Starlight engines ({@link #blockLight} and
 * {@link #skyLight}); the world still owns the {@code skylightSubtracted} day/night dial (via
 * {@link SkylightTracker}) and the celestial/weather-derived sky, cloud and fog colours (via
 * {@link AtmosphereCalculator}).
 */
public class World implements IBlockAccess {
	private static final int blocksToTickPerFrame = 10;	// LCG random-tick probes per materialised subchunk (see updateBlocksAndPlayCaveSounds)	

	/** Chebyshev chunk radius around each player within which entities receive full updates (AI, movement, collisions). */
	public int entitySimulationRadiusChunks = 8;
	
	public boolean scheduledUpdatesAreImmediate;
	
	private final EntityManager entityManager;
	private final BlockTickScheduler blockTickScheduler;
	private final SkylightTracker skylightTracker;
	public List<TileEntity> loadedTileEntityList;
	private List<TileEntity> entityRemoval;
	public List<EntityPlayer> playerEntities;
	public List<Entity> weatherEffects;
	protected int updateLCG;
	protected final int DIST_HASH_MAGIC;
	
	public boolean editingBlocks;
	private long lockTimestamp;
	protected int autosavePeriod;
	public int difficultySetting;
	public Random rand;
	public boolean isNewWorld;
	public final WorldProvider worldProvider;
	public List<IWorldAccess> worldAccesses;
	public IChunkProvider chunkProvider;
	protected final ISaveHandler saveHandler;
	public WorldInfo worldInfo;
	public boolean findingSpawnPoint;
	private boolean allPlayersSleeping;
	public MapStorage mapStorage;
	private final EntityQueryService entityQueryService;
	private final TerrainHeightQueryCache heightQueryCache = new TerrainHeightQueryCache();
	private boolean scanningTileEntities;
	private boolean spawnHostileMobs;
	private boolean spawnPeacefulMobs;
	private Set<ChunkCoordIntPair> positionsToUpdate;
	private int soundCounter;
	public boolean isRemote;
	public boolean colouredAthmospherics;

	// Weather
		
	public float prevSnowingStrength;
	public float snowingStrength;
	
	public float prevRainingStrength;
	public float rainingStrength;
	
	public float prevThunderingStrength;
	public float thunderingStrength;
	public int lastLightningBolt;
	public int lightningFlash;
	public int lightningChance = 50000;
	
	// Blood moon
	
	public boolean badMoonDecide;
	public boolean badMoonText;
	public boolean nextMoonBad;
	
	// Handy
	
	private int snowTicker = 0;
	
	public final StarlightEngine blockLight = new StarlightEngine(false, this);
	public final StarlightEngine skyLight = new StarlightEngine(true, this);

	/** Returns the world chunk manager from the world provider. */
	public WorldChunkManager getWorldChunkManager() {
		return this.worldProvider.worldChunkMgr;
	}

	/**
	 * Primary constructor: creates a brand-new world with the given save handler, name, provider
	 * and settings. Used when no existing save data is available.
	 */
	public World(ISaveHandler saveHandler, String saveName, WorldProvider worldProvider, WorldSettings worldSettings) {
		this.scheduledUpdatesAreImmediate = false;
		this.entityManager = new EntityManager(this);
		this.blockTickScheduler = new BlockTickScheduler(this);
		this.loadedTileEntityList = new ArrayList<TileEntity>();
		this.entityRemoval = new ArrayList<TileEntity>();
		this.playerEntities = new ArrayList<EntityPlayer>();
		this.weatherEffects = new ArrayList<Entity>();
		this.skylightTracker = new SkylightTracker(this);
		this.updateLCG = (new Random()).nextInt();
		this.DIST_HASH_MAGIC = 1013904223;
		this.lastLightningBolt = 0;
		this.lightningFlash = 0;
		this.editingBlocks = false;
		this.lockTimestamp = System.currentTimeMillis();
		this.autosavePeriod = 40;
		this.rand = new Random();
		this.isNewWorld = false;
		this.worldAccesses = new ArrayList<IWorldAccess>();
		this.entityQueryService = new EntityQueryService(this);
		this.spawnHostileMobs = true;
		this.spawnPeacefulMobs = true;
		this.positionsToUpdate = new HashSet<ChunkCoordIntPair>();
		this.soundCounter = this.rand.nextInt(12000);
		this.isRemote = false;
		this.saveHandler = saveHandler;
		this.worldInfo = new WorldInfo(worldSettings, saveName);
		this.worldProvider = worldProvider;
		this.mapStorage = new MapStorage(saveHandler);
		worldProvider.registerWorld(this);
		this.chunkProvider = this.getChunkProvider();
		this.calculateInitialSkylight();
		this.calculateInitialWeather();
		
		WorldEdit.init();
	}

	/** Copy constructor: clones shared state from {@code sourceWorld} and assigns a new provider. */
	public World(World sourceWorld, WorldProvider worldProvider) {
		this.scheduledUpdatesAreImmediate = false;
		this.entityManager = new EntityManager(this);
		this.blockTickScheduler = new BlockTickScheduler(this);
		this.loadedTileEntityList = new ArrayList<TileEntity>();
		this.entityRemoval = new ArrayList<TileEntity>();
		this.playerEntities = new ArrayList<EntityPlayer>();
		this.weatherEffects = new ArrayList<Entity>();
		this.skylightTracker = new SkylightTracker(this);
		this.updateLCG = (new Random()).nextInt();
		this.DIST_HASH_MAGIC = 1013904223;
		this.lastLightningBolt = 0;
		this.lightningFlash = 0;
		this.editingBlocks = false;
		this.lockTimestamp = System.currentTimeMillis();
		this.autosavePeriod = 40;
		this.rand = new Random();
		this.isNewWorld = false;
		this.worldAccesses = new ArrayList<IWorldAccess>();
		this.entityQueryService = new EntityQueryService(this);
		this.spawnHostileMobs = true;
		this.spawnPeacefulMobs = true;
		this.positionsToUpdate = new HashSet<ChunkCoordIntPair>();
		this.soundCounter = this.rand.nextInt(12000);
		this.isRemote = false;
		this.lockTimestamp = sourceWorld.lockTimestamp;
		this.saveHandler = sourceWorld.saveHandler;
		this.worldInfo = new WorldInfo(sourceWorld.worldInfo);
		this.mapStorage = new MapStorage(this.saveHandler);
		this.worldProvider = worldProvider;
		worldProvider.registerWorld(this);
		this.chunkProvider = this.getChunkProvider();
		
		this.badMoonDecide = false;
		this.nextMoonBad = false;
		
		Seasons.dayOfTheYear = this.rand.nextInt(4 * Seasons.SEASON_DURATION);
		Seasons.updateSeasonCounters();

		this.calculateInitialSkylight();
		this.calculateInitialWeather();
		
		WorldEdit.init();
	}

	/** Convenience constructor that delegates to the full save-handler constructor with a null provider. */
	public World(ISaveHandler saveHandler, String saveName, WorldSettings worldSettings) {
		this(saveHandler, saveName, worldSettings, (WorldProvider)null);
	}

	/**
	 * Load constructor: reads existing world data (or creates fresh info), picks the dimension
	 * provider from saved data, and initialises the chunk provider and weather.
	 */
	public World(ISaveHandler saveHandler, String saveName, WorldSettings worldSettings, WorldProvider worldProvider) {
		this.scheduledUpdatesAreImmediate = false;
		this.entityManager = new EntityManager(this);
		this.blockTickScheduler = new BlockTickScheduler(this);
		this.loadedTileEntityList = new ArrayList<TileEntity>();
		this.entityRemoval = new ArrayList<TileEntity>();
		this.playerEntities = new ArrayList<EntityPlayer>();
		this.weatherEffects = new ArrayList<Entity>();
		this.skylightTracker = new SkylightTracker(this);
		this.updateLCG = (new Random()).nextInt();
		this.DIST_HASH_MAGIC = 1013904223;
		this.lastLightningBolt = 0;
		this.lightningFlash = 0;
		this.editingBlocks = false;
		this.lockTimestamp = System.currentTimeMillis();
		this.autosavePeriod = 40;
		this.rand = new Random();
		this.isNewWorld = false;
		this.worldAccesses = new ArrayList<IWorldAccess>();
		this.entityQueryService = new EntityQueryService(this);
		this.spawnHostileMobs = true;
		this.spawnPeacefulMobs = true;
		this.positionsToUpdate = new HashSet<ChunkCoordIntPair>();
		this.soundCounter = this.rand.nextInt(12000);
		this.isRemote = false;
		this.saveHandler = saveHandler;
		this.mapStorage = new MapStorage(saveHandler);
		
		Seasons.dayOfTheYear = -1;
		this.worldInfo = saveHandler.loadWorldInfo();
		
		this.badMoonDecide = false;
		this.nextMoonBad = false;

		boolean isFreshlyCreated = false;
		if(this.worldInfo == null) {
			this.worldInfo = new WorldInfo(worldSettings, saveName);
			isFreshlyCreated = true;
		} else {
			this.worldInfo.setWorldName(saveName);
		}
		
		this.isNewWorld = this.worldInfo == null;
		if(worldProvider != null) {
			this.worldProvider = worldProvider;
		} else if(this.worldInfo != null && this.worldInfo.getDimension() == -1) {
			this.worldProvider = WorldProvider.getProviderForDimension(-1);
		} else if(this.worldInfo != null && (this.worldInfo.getDimension() == 1 || this.worldInfo.getTerrainType() == WorldType.SKY)) {
			this.worldProvider = WorldProvider.getProviderForDimension(1);
		} else {
			this.worldProvider = WorldProvider.getProviderForDimension(0);
		}

		this.worldProvider.registerWorld(this);
		this.chunkProvider = this.getChunkProvider();
		if(isFreshlyCreated) {
			this.getInitialSpawnLocation();
			this.initializeWeather();
		}
		
		// Start in mid spring to mid summer
		if(Seasons.dayOfTheYear < 0) Seasons.dayOfTheYear = this.rand.nextInt(Seasons.SEASON_DURATION) + Seasons.SEASON_DURATION + (Seasons.SEASON_DURATION >> 1);
		Seasons.updateSeasonCounters();

		this.calculateInitialSkylight();
		this.calculateInitialWeather();
		
		WorldEdit.init();
	}

	/** Creates the chunk provider by loading the chunk loader from the save handler. */
	protected IChunkProvider getChunkProvider() {
		IChunkLoader chunkLoader = this.saveHandler.getChunkLoader(this.worldProvider);
		return new ChunkProvider(this, chunkLoader, this.worldProvider.getChunkProvider());
	}

	/**
	 * Returns the chunk loader this world's provider persists chunks through, or null when
	 * no save handler is attached (transient/test worlds). Useful for probing disk state
	 * without forcing generation.
	 */
	public IChunkLoader getChunkLoader() {
		if(this.saveHandler != null) {
			return this.saveHandler.getChunkLoader(this.worldProvider);
		}
		return null;
	}

	/** Finds a valid spawn position near the world origin, avoiding city-generated chunks. */
	protected void getInitialSpawnLocation() {
		this.findingSpawnPoint = true;
		byte y = 64;
		byte noCityRadius = 8;
		
		/*
		int x = 0;
		
		int z;
		for(z = 0; !this.worldProvider.canCoordinateBeSpawn(x, z); z += this.rand.nextInt(64) - this.rand.nextInt(64)) {
			x += this.rand.nextInt(64) - this.rand.nextInt(64);
		}
		*/
		
		/*
		int x = this.rand.nextInt(8) << 10;
		int z = this.rand.nextInt(8) << 10;
		
		do {
			x += this.rand.nextInt(64) - this.rand.nextInt(64);
			z += this.rand.nextInt(64) - this.rand.nextInt(64);
		} while (!this.worldProvider.canCoordinateBeSpawn(x, z) || this.isCityCoordinate(x, z));

		this.worldInfo.setSpawn(x, y, z);
		this.findingSpawnPoint = false;
		*/

		int x = 0;
		int z = 0;
		if (this.rand.nextBoolean()) {
			x = this.rand.nextInt(8) << 10;
			z = this.rand.nextInt(8) << 10;
		}

		while (this.findingSpawnPoint) {

			do {
				x += this.rand.nextInt(64) - this.rand.nextInt(64);
				z += this.rand.nextInt(64) - this.rand.nextInt(64);
			} while (!this.worldProvider.canCoordinateBeSpawn(x, z));

			this.findingSpawnPoint = false;
			
			// Avoid if too close to city
			if (this.worldInfo.getCityChance() >= 0.1F) {
				int chunkX = x >> 4;
				int chunkZ = z >> 4;
				
				findingCity:
				for(int cx = chunkX - noCityRadius; cx <= chunkX + noCityRadius; cx ++) {
					for(int cz = chunkX - noCityRadius; cz <= chunkZ + noCityRadius; cz ++) {
						if(this.isCityCoordinate(cx, cz)) {
							this.findingSpawnPoint = true;
							break findingCity;
						}
					}
				}
			}
			
		}

		this.worldInfo.setSpawn(x, y, z);
	}

	/** Returns true if the chunk at the given block coordinates contains a city building, road, or ruin. */
	public boolean isCityCoordinate(int x, int z) {
		Chunk chunk = getChunkFromBlockCoords(x, z);
		return chunk.hasBuilding || chunk.hasRoad || chunk.hasUnderwaterRuin;
	}
	
	/** Sets the world spawn to the first uncovered block near the current spawn coordinates. */
	public void setSpawnLocation() {
		if(this.worldInfo.getSpawnY() <= 0) {
			this.worldInfo.setSpawnY(64);
		}

		int spawnX = this.worldInfo.getSpawnX();

		int spawnZ;
		for(spawnZ = this.worldInfo.getSpawnZ(); this.getFirstUncoveredBlock(spawnX, spawnZ) == 0; spawnZ += this.rand.nextInt(8) - this.rand.nextInt(8)) {
			spawnX += this.rand.nextInt(8) - this.rand.nextInt(8);
		}

		this.worldInfo.setSpawnX(spawnX);
		this.worldInfo.setSpawnZ(spawnZ);
	}

	/** Returns the block ID of the first non-air block at (x, z), scanning up from y=63. */
	public int getFirstUncoveredBlock(int x, int z) {
		int y;
		for(y = 63; !this.isAirBlock(x, y + 1, z); ++y) {
		}

		return this.getBlockId(x, y, z);
	}

	/** No-op placeholder retained for compatibility with existing code. */
	public void emptyMethod1() {
	}

	/** Spawns the player into the world, restoring saved NBT data and centering the chunk loader. */
	public void spawnPlayerWithLoadedChunks(EntityPlayer entityPlayer) {
		try {
			NBTTagCompound nbtTag = this.worldInfo.getPlayerNBTTagCompound();
			if(nbtTag != null) {
				entityPlayer.readFromNBT(nbtTag);
				this.worldInfo.setPlayerNBTTagCompound((NBTTagCompound)null);
			}

			if(this.chunkProvider instanceof ChunkProviderLoadOrGenerate) {
				ChunkProviderLoadOrGenerate chunkProvider = (ChunkProviderLoadOrGenerate)this.chunkProvider;
				int chunkX = MathHelper.floor_float((float)((int)entityPlayer.posX)) >> 4;
				int chunkZ = MathHelper.floor_float((float)((int)entityPlayer.posZ)) >> 4;
				chunkProvider.setCurrentChunkOver(chunkX, chunkZ);
			}

			this.spawnEntityInWorld(entityPlayer);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	/** Saves the world level data and all chunks, optionally displaying progress. */
	public void saveWorld(boolean saveAllChunks, IProgressUpdate progressUpdate) {
		if(this.chunkProvider.canSave()) {
			if(progressUpdate != null) {
				progressUpdate.displaySavingString("Saving level");
			}

			this.saveLevel();
			if(progressUpdate != null) {
				progressUpdate.displayLoadingString("Saving chunks");
			}

			this.chunkProvider.saveChunks(saveAllChunks, progressUpdate);
		}
	}

	/** Persists world info, player data and map storage to disk. */
	private void saveLevel() {
		this.checkSessionLock();
		this.saveHandler.saveWorldInfoAndPlayer(this.worldInfo, this.playerEntities);
		this.mapStorage.saveAllData();
	}

	/**
	 * Quick-saves the world. Mode 0 saves level metadata; all modes save chunks.
	 * @return true if the provider cannot save (no-op) or chunks saved successfully
	 */
	public boolean quickSaveWorld(int mode) {
		if(!this.chunkProvider.canSave()) {
			return true;
		} else {
			if(mode == 0) {
				this.saveLevel();
			}

			return this.chunkProvider.saveChunks(false, (IProgressUpdate)null);
		}
	}

	/** Returns the block ID at the given world coordinates, or 0 if y is out of range. */
	public int getBlockId(int x, int y, int z) {
		if (y < 0 || y >= Chunk.SECTION_HEIGHT) return 0;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockID(x & 15, y, z & 15);
	}
	
	/** Returns the block ID at the given block position. */
	public int getBlockId(BlockPos blockPos) {
		return this.getBlockId(blockPos.x, blockPos.y, blockPos.z);
	}

	/** Returns true if the block at the given coordinates has ID 0 (air). */
	public boolean isAirBlock(int x, int y, int z) {
		return this.getBlockId(x, y, z) == 0;
	}
	
	/** Returns true if the block at the given position has ID 0 (air). */
	public boolean isAirBlock(BlockPos blockPos) {
		return this.isAirBlock(blockPos.x, blockPos.y, blockPos.z);
	}
	
	/** Returns true if the block at the given coordinates is an instance of BlockFluid. */
	public boolean isWaterBlock(int x, int y, int z) {
		Block b = Block.blocksList[this.getBlockId(x, y, z)];
		return (b != null && b instanceof BlockFluid);
	}

	/** Returns true if the block at the given position is an instance of BlockFluid. */
	public boolean isWaterBlock(BlockPos blockPos) {
		return this.isWaterBlock(blockPos.x, blockPos.y, blockPos.z);
	}
	
	/** Returns true if the block position is within the valid height range and its chunk is loaded. */
	public boolean blockExists(int x, int y, int z) {
		return y >= 0 && y < Chunk.SECTION_HEIGHT ? this.chunkExists(x >> 4, z >> 4) : false;
	}
	
	/** Returns true if the block at the given position is within valid height and its chunk is loaded. */
	public boolean blockExists(BlockPos blockPos) {
		return this.blockExists(blockPos.x, blockPos.y, blockPos.z);
	}

	/** Returns true if all chunks within {@code radius} blocks of (x, y, z) exist and are loaded. */
	public boolean doChunksNearChunkExist(int x, int y, int z, int radius) {
		return this.checkChunksExist(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
	}

	/**
	 * Returns true if every chunk intersecting the axis-aligned block region is loaded.
	 * The six coordinates are the lower and upper bounds on each axis.
	 */
	public boolean checkChunksExist(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if(maxY >= 0 && minY < Chunk.SECTION_HEIGHT) {
			minX >>= 4;
			minY >>= 4;
			minZ >>= 4;
			maxX >>= 4;
			maxY >>= 4;
			maxZ >>= 4;

			for(int chunkX = minX; chunkX <= maxX; ++chunkX) {
				for(int chunkZ = minZ; chunkZ <= maxZ; ++chunkZ) {
					if(!this.chunkExists(chunkX, chunkZ)) {
						return false;
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	/** Returns true if the chunk at the given chunk coordinates is loaded. */
	public boolean chunkExists(int chunkX, int chunkZ) {
		return this.chunkProvider.chunkExists(chunkX, chunkZ);
	}

	/** Returns the chunk containing the given block coordinates. */
	public Chunk getChunkFromBlockCoords(int blockX, int blockZ) {
		return this.getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
	}

	/** Returns the chunk with the given chunk coordinates, generating it if needed. */
	public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
		return this.chunkProvider.provideChunk(chunkX, chunkZ);
	}

	/** Sets the block ID and metadata at the given world coordinates, returning success. */
	public boolean setBlockAndMetadata(int x, int y, int z, int id, int metadata) {
		if(y < 0 || y >= Chunk.SECTION_HEIGHT) return false;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockIDWithMetadata(x & 15, y, z & 15, id, metadata);
	}

	/** Sets the block ID and metadata at the given block position. */
	public boolean setBlockAndMetadata(BlockPos blockPos, int id, int metadata) {
		return this.setBlockAndMetadata(blockPos.x, blockPos.y, blockPos.z, id, metadata);
	}
	
	/** Sets the block ID at the given world coordinates, returning success. */
	public boolean setBlock(int x, int y, int z, int id) {
		if(y < 0 || y >= Chunk.SECTION_HEIGHT) return false;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockID(x & 15, y, z & 15, id);
	}
	
	/** Sets the block ID at the given block position (metadata argument ignored). */
	public boolean setBlock(BlockPos blockPos, int id, int metadata) {
		return this.setBlock(blockPos.x, blockPos.y, blockPos.z, id);
	}
	
	/** Sets a column of block IDs starting at (x, y, z) from the given ID array. */
	public boolean setBlockAndMetadataColumn(int x, int y, int z, int[] id) {
		if(y < 0) return false;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockIDAndMetadataColumn(x & 15, y, z & 15, id);
	}
	
	/** Returns the material of the block at the given coordinates, using metadata-dependent material. */
	public Material getBlockMaterial(int x, int y, int z) {
		Block block = Block.blocksList[this.getBlockId(x, y, z)];
		int metadata = this.getBlockMetadata(x, y, z);
		if (block == null) return Material.air;
		Material material = block.getBlockMaterialBasedOnmetaData(metadata);
		
		return material;
	}
	
	/** Returns the material of the block at the given block position. */
	public Material getBlockMaterial(BlockPos blockPos) {
		return this.getBlockMaterial(blockPos.x, blockPos.y, blockPos.z);
	}

	/** Returns the metadata of the block at the given world coordinates, or 0 if out of range. */
	public int getBlockMetadata(int x, int y, int z) {
		if (y < 0 || y >= Chunk.SECTION_HEIGHT) return 0;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockMetadata(x & 15, y, z & 15);
	}

	/** Returns the metadata of the block at the given block position. */
	public int getBlockMetadata(BlockPos blockPos) {
		return this.getBlockMetadata(blockPos.x, blockPos.y, blockPos.z);
	}
	
	/** Sets metadata at the given coordinates and notifies the block or its neighbors of the change. */
	public void setBlockMetadataWithNotify(int x, int y, int z, int metadata) {
		if(this.setBlockMetadata(x, y, z, metadata)) {
			int blockId = this.getBlockId(x, y, z);
			if(Block.requiresSelfNotify[blockId & 255]) {
				this.notifyBlockChange(x, y, z, blockId);
			} else {
				this.notifyBlocksOfNeighborChange(x, y, z, blockId);
			}
		}

	}
	
	/** Sets metadata at the given block position and notifies the block or its neighbors. */
	public void setBlockMetadataWithNotify(BlockPos blockPos, int meta) {
		this.setBlockMetadataWithNotify(blockPos.x, blockPos.y, blockPos.z, meta);
	}

	/** Sets the metadata of the block at the given world coordinates, returning success. */
	public boolean setBlockMetadata(int x, int y, int z, int metadata) {
		if (y < 0 || y >= Chunk.SECTION_HEIGHT) return false;
		this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockMetadata(x & 15, y, z & 15, metadata);
		return true;
	}
	
	/** Sets the metadata of the block at the given block position. */
	public boolean setBlockMetadata(BlockPos blockPos, int metadata) {
		return this.setBlockMetadata(blockPos.x, blockPos.y, blockPos.z, metadata);
	}

	/** Sets the block ID at the given coordinates and notifies of the change. */
	public boolean setBlockWithNotify(int x, int y, int z, int id) {
		if(this.setBlock(x, y, z, id)) {
			this.notifyBlockChange(x, y, z, id);
			return true;
		} else {
			return false;
		}
	}
	
	/** Sets the block ID at the given block position and notifies of the change. */
	public boolean setBlockWithNotify(BlockPos blockPos, int id) {
		return this.setBlockWithNotify(blockPos.x, blockPos.y, blockPos.z, id);
	}

	/** Sets block ID and metadata at the given coordinates and notifies of the change. */
	public boolean setBlockAndMetadataWithNotify(int x, int y, int z, int id, int metadata) {
		if(this.setBlockAndMetadata(x, y, z, id, metadata)) {
			this.notifyBlockChange(x, y, z, id);
			return true;
		} else {
			return false;
		}
	}
	
	/** Sets block ID and metadata at the given block position and notifies of the change. */
	public boolean setBlockAndMetadataWithNotify(BlockPos blockPos, int id, int metadata) {
		return this.setBlockAndMetadataWithNotify(blockPos.x, blockPos.y, blockPos.z, id, metadata);
	}

	/** Tells every world access listener that the block at (x, y, z) needs a render update. */
	public void markBlockNeedsUpdate(int x, int y, int z) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).markBlockNeedsUpdate(x, y, z);
		}

	}

	/** Marks the block dirty and notifies its neighbors of the change in the given block ID. */
	protected void notifyBlockChange(int x, int y, int z, int blockId) {
		this.markBlockNeedsUpdate(x, y, z);
		this.notifyBlocksOfNeighborChange(x, y, z, blockId);
	}

	/**
	 * Marks the blocks between two Y values at the given X and Z for a render update,
	 * swapping the Y bounds so the lower one comes first.
	 */
	public void markBlocksDirtyVertical(int x, int z, int y1, int y2) {
		if(y1 > y2) {
			int temp = y2;
			y2 = y1;
			y1 = temp;
		}

		this.markBlocksDirty(x, y1, z, x, y2, z);
	}

	/** Tells every world access listener that a single block position needs a render update. */
	public void markBlockAsNeedsUpdate(int x, int y, int z) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).markBlockRangeNeedsUpdate(x, y, z, x, y, z);
		}

	}

	/** Tells every world access listener that the block region is dirty and needs a render update. */
	public void markBlocksDirty(int x1, int y1, int z1, int x2, int y2, int z2) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).markBlockRangeNeedsUpdate(x1, y1, z1, x2, y2, z2);
		}

	}

	/** Notifies each of the six neighboring blocks of the given block that a change occurred. */
	public void notifyBlocksOfNeighborChange(int x, int y, int z, int blockId) {
		this.notifyBlockOfNeighborChange(x - 1, y, z, blockId);
		this.notifyBlockOfNeighborChange(x + 1, y, z, blockId);
		this.notifyBlockOfNeighborChange(x, y - 1, z, blockId);
		this.notifyBlockOfNeighborChange(x, y + 1, z, blockId);
		this.notifyBlockOfNeighborChange(x, y, z - 1, blockId);
		this.notifyBlockOfNeighborChange(x, y, z + 1, blockId);
	}

	/** Notifies the single block at (x, y, z) that one of its neighbors changed. */
	public void notifyBlockOfNeighborChange(int x, int y, int z, int blockId) {
		if(!this.editingBlocks && !this.isRemote) {
			Block block = Block.blocksList[this.getBlockId(x, y, z)];
			if(block != null) {
				block.onNeighborBlockChange(this, x, y, z, blockId);
			}

		}
	}

	/** Returns true if the block at the given coordinates can see the sky above. */
	public boolean canBlockSeeTheSky(int x, int y, int z) {
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).canBlockSeeTheSky(x & 15, y, z & 15);
	}

	/** Returns the full block light value (sky light only), clamping y to the valid range. */
	public int getFullBlockLightValue(int x, int y, int z) {
		if(y < 0) {
			return 0;
		} else {
			if(y >= Chunk.SECTION_HEIGHT) {
				y = Chunk.SECTION_HEIGHT - 1;
			}

			return this.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockLightValue(x & 15, y, z & 15, 0);
		}
	}

	/** Returns the combined sky/block light value at the given coordinates. */
	public int getBlockLightValue(int x, int y, int z) {
		return this.getBlockLightValue_do(x, y, z, true);
	}

	/**
	 * Internal light lookup. When {@code includeSlabHeights}, thin blocks (slabs/tilled fields)
	 * sample the brightest of their six neighbours; otherwise the cached light value is returned.
	 */
	public int getBlockLightValue_do(int x, int y, int z, boolean includeSlabHeights) {
		if(includeSlabHeights) {
			int blockId = this.getBlockId(x, y, z);
			if(blockId == Block.stairSingle.blockID || blockId == Block.tilledField.blockID || blockId == Block.stairCompactCobblestone.blockID || blockId == Block.stairCompactPlanks.blockID) {
				int brightness = this.getBlockLightValue_do(x, y + 1, z, false);
				int east = this.getBlockLightValue_do(x + 1, y, z, false);
				int west = this.getBlockLightValue_do(x - 1, y, z, false);
				int north = this.getBlockLightValue_do(x, y, z + 1, false);
				int south = this.getBlockLightValue_do(x, y, z - 1, false);
				if(east > brightness) {
					brightness = east;
				}

				if(west > brightness) {
					brightness = west;
				}

				if(north > brightness) {
					brightness = north;
				}

				if(south > brightness) {
					brightness = south;
				}

				return brightness;
			}
		}

		if(y < 0) {
			return 0;
		} else {
			if(y >= Chunk.SECTION_HEIGHT) {
				y = Chunk.SECTION_HEIGHT - 1;
			}

			Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
			x &= 15;
			z &= 15;
			return chunk.getBlockLightValue(x, y, z, this.skylightTracker.getSkylightSubtracted());
		}
	}

	/** Returns true if the block can see the sky, handling out-of-range Y and unloaded chunks. */
	public boolean canExistingBlockSeeTheSky(int x, int y, int z) {
		if(y < 0) {
			return false;
		} else if(y >= Chunk.SECTION_HEIGHT) {
			return true;
		} else if(!this.chunkExists(x >> 4, z >> 4)) {
			return false;
		} else {
			Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
			x &= 15;
			z &= 15;
			return chunk.canBlockSeeTheSky(x, y, z);
		}
	}

	/** Returns the height of the highest non-air block at the given block coordinates, or 0 if unloaded. */
	public int getHeightValue(int blockX, int blockZ) {
		if(!this.chunkExists(blockX >> 4, blockZ >> 4)) {
			return 0;
		} else {
			Chunk chunk = this.getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
			return chunk.getHeightValue(blockX & 15, blockZ & 15);
		}
	}
	
	/** Returns the land-surface height at the given block coordinates, generating the chunk if needed. */
	public int getLandSurfaceHeightValue(int blockX, int blockZ) {
		int x = blockX >> 4;
		int z = blockZ >> 4;
		if(this.chunkExists(x, z)) { 
			return this.getChunkFromChunkCoords(x, z).getLandSurfaceHeightValue(blockX & 15, blockZ & 15);
		}
		return this.heightQueryCache.getOrCompute(x, z, this).landSurfaceHeightMap[((blockZ & 15) << 4) | (blockX & 15)] & 255;
	}
	
	/** Returns true if the chunk at the given chunk coordinates is an ocean chunk. */
	public boolean isOceanChunk(int chunkX, int chunkZ) {
		if(this.chunkExists(chunkX, chunkZ)) {
			return this.getChunkFromChunkCoords(chunkX, chunkZ).isOcean;
		}
		return this.heightQueryCache.getOrCompute(chunkX, chunkZ, this).isOcean;
	}
	
	/** Returns true if the chunk at the given chunk coordinates is urban. */
	public boolean isUrbanChunk(int chunkX, int chunkZ) {
		if(this.chunkExists(chunkX, chunkZ)) {
			return this.getChunkFromChunkCoords(chunkX, chunkZ).isUrbanChunk;
		}
		return this.heightQueryCache.getOrCompute(chunkX, chunkZ, this).isUrbanChunk;
	}
	
	/** Returns the chunk at the given chunk coordinates, generating it for height queries if needed. */
	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		if(this.chunkExists(chunkX, chunkZ)) {
			return this.getChunkFromChunkCoords(chunkX, chunkZ);
		}
		return this.heightQueryCache.getOrComputeChunk(chunkX, chunkZ, this);
	}
	
	/** Drops the cached height query result for the given chunk (chunk-unload tidy hook). */
	public void evictHeightQuery(int chunkX, int chunkZ) {
		this.heightQueryCache.remove(chunkX, chunkZ);
	}
		
	/** Returns the first Y below the surface that is not still water, scanning down from the height value. */
	public int getHeightValueUnderWater (int x, int z) {
		// Start here
		int y = getHeightValue (x, z);
		
		while (y > 8) {
			y --;
			if (getBlockId (x, y, z) != Block.waterStill.blockID) break;
		}
		
		return y;
	}

	/** Returns the brightness of the given sky-block type at the coordinates, sampling neighbors for see-through blocks. */
	public int getSkyBlockTypeBrightness(EnumSkyBlock skyBlockType, int x, int y, int z) {
		if(this.worldProvider.hasNoSky && skyBlockType == EnumSkyBlock.Sky) {
			return 0;
		} else {
			if(y < 0) {
				y = 0;
			}

			if(y >= Chunk.SECTION_HEIGHT) {
				return skyBlockType.defaultLightValue;
			} else {
				int chunkX = x >> 4;
				int chunkZ = z >> 4;
				if(!this.chunkExists(chunkX, chunkZ)) {
					return skyBlockType.defaultLightValue;
				} else if(Block.useNeighborBrightness[this.getBlockId(x, y, z)]) {
					int up = this.getSavedLightValue(skyBlockType, x, y + 1, z);
					int east = this.getSavedLightValue(skyBlockType, x + 1, y, z);
					int west = this.getSavedLightValue(skyBlockType, x - 1, y, z);
					int north = this.getSavedLightValue(skyBlockType, x, y, z + 1);
					int south = this.getSavedLightValue(skyBlockType, x, y, z - 1);
					if(east > up) {
						up = east;
					}

					if(west > up) {
						up = west;
					}

					if(north > up) {
						up = north;
					}

					if(south > up) {
						up = south;
					}

					return up;
				} else {
					Chunk chunk = this.getChunkFromChunkCoords(chunkX, chunkZ);
					return chunk.getSavedLightValue(skyBlockType, x & 15, y, z & 15);
				}
			} 
		}
	}

	/** Returns the cached light value for the given sky-block type, clamping Y to the valid range. */
	public int getSavedLightValue(EnumSkyBlock skyBlockType, int x, int y, int z) {
		if(y < 0) {
			y = 0;
		}

		if(y >= Chunk.SECTION_HEIGHT) {
			y = Chunk.SECTION_HEIGHT - 1;
		}

		if(y >= 0 && y < Chunk.SECTION_HEIGHT) {
			int chunkX = x >> 4;
			int chunkZ = z >> 4;
			if(!this.chunkExists(chunkX, chunkZ)) {
				return 0;
			} else {
				Chunk chunk = this.getChunkFromChunkCoords(chunkX, chunkZ);
				return chunk.getSavedLightValue(skyBlockType, x & 15, y, z & 15);
			}
		} else {
			return skyBlockType.defaultLightValue;
		}
	}

	/** Sets the light value of the given sky-block type, notifying listeners if it changed. */
	public void setLightValue(EnumSkyBlock skyBlockType, int x, int y, int z, int brightness) {
		if(y >= 0) {
			if(y < Chunk.SECTION_HEIGHT) {
				if(this.chunkExists(x >> 4, z >> 4)) {
					Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
					int previous = chunk.getSavedLightValue(skyBlockType, x & 15, y, z & 15);
					chunk.setLightValue(skyBlockType, x & 15, y, z & 15, brightness);

					if(previous != brightness) {
						for(int i = 0; i < this.worldAccesses.size(); ++i) {
							((IWorldAccess)this.worldAccesses.get(i)).markBlockNeedsUpdate(x, y, z);
						}
					}

				}
			}
		}
	}

	/** Packs sky and block brightness into the 24-bit format used by the lightmap, honoring the ambient floor. */
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minBrightness) {
		int skyBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
		int blockBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);
		if(blockBrightness < minBrightness) {
			blockBrightness = minBrightness;
		}

		return skyBrightness << 20 | blockBrightness << 4;
	}

	/** Returns the light brightness for the block, applying the given minimum brightness floor. */
	public float getBrightness(int x, int y, int z, int minBrightness) {
		int brightness = this.getBlockLightValue(x, y, z);
		if(brightness < minBrightness) {
			brightness = minBrightness;
		}

		return this.worldProvider.lightBrightnessTable[brightness];
	}

	/** Returns the light brightness (0.0-1.0) of the block at the given coordinates. */
	public float getLightBrightness(int x, int y, int z) {
		return this.worldProvider.lightBrightnessTable[this.getBlockLightValue(x, y, z)];
	}

	/** Returns true when the sky-light dial indicates daytime (skylight subtracted &lt; 4). */
	public boolean isDaytime() {
		return this.skylightTracker.getSkylightSubtracted() < 4;
	}

	/** Traces a ray from {@code from} to {@code to} and returns the first block hit, ignoring liquids and empty bounding boxes. */
	public MovingObjectPosition rayTraceBlocks(Vec3D from, Vec3D to) {
		return this.rayTraceBlocks(from, to, false, false);
	}

	/** Traces a ray with the option to stop on liquid blocks. */
	public MovingObjectPosition rayTraceBlocks(Vec3D from, Vec3D to, boolean stopOnLiquid) {
		return this.rayTraceBlocks(from, to, stopOnLiquid, false);
	}

	/**
	 * Traces a ray through the world, stepping one block at a time along the axis with the
	 * smallest fractional component. Returns the first {@link MovingObjectPosition} hit, or null.
	 *
	 * @param stopOnLiquid                    if true, liquids count as collision targets
	 * @param ignoreBlockWithoutBoundingBox    if true, blocks with no collision box are skipped
	 */
	public MovingObjectPosition rayTraceBlocks(Vec3D vFrom, Vec3D vTo, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
		if(!Double.isNaN(vFrom.xCoord) && !Double.isNaN(vFrom.yCoord) && !Double.isNaN(vFrom.zCoord)) {
			if(!Double.isNaN(vTo.xCoord) && !Double.isNaN(vTo.yCoord) && !Double.isNaN(vTo.zCoord)) {
				int toBlockX = MathHelper.floor_double(vTo.xCoord);
				int toBlockY = MathHelper.floor_double(vTo.yCoord);
				int toBlockZ = MathHelper.floor_double(vTo.zCoord);

				int currentX = MathHelper.floor_double(vFrom.xCoord);
				int currentY = MathHelper.floor_double(vFrom.yCoord);
				int currentZ = MathHelper.floor_double(vFrom.zCoord);

				int blockId = this.getBlockId(currentX, currentY, currentZ);
				int metadata = this.getBlockMetadata(currentX, currentY, currentZ);
				Block block = Block.blocksList[blockId];

				// Check the starting block for an immediate collision
				if(
					(
						!ignoreBlockWithoutBoundingBox || 
						block == null || 
						block.getCollisionBoundingBoxFromPool(this, currentX, currentY, currentZ) != null
					) && 
					blockId > 0 && 
					block.canCollideCheck(metadata, stopOnLiquid)
				) {
					MovingObjectPosition pos = block.collisionRayTrace(this, currentX, currentY, currentZ, vFrom, vTo);
					if(pos != null) {
						return pos;
					}
				}

				// Step along the ray one block at a time, choosing the axis with the smallest parametric step
				int maxSteps = 200;
				while(maxSteps-- >= 0) {
					if(
						Double.isNaN(vFrom.xCoord) || 
						Double.isNaN(vFrom.yCoord) || 
						Double.isNaN(vFrom.zCoord)
					) {
						return null;
					}

					if(currentX == toBlockX && currentY == toBlockY && currentZ == toBlockZ) {
						return null;
					}

					boolean canStepX = true;
					boolean canStepY = true;
					boolean canStepZ = true;
					double nextX = 999.0D;
					double nextY = 999.0D;
					double nextZ = 999.0D;

					if(toBlockX > currentX) {
						nextX = (double)currentX + 1.0D;
					} else if(toBlockX < currentX) {
						nextX = (double)currentX + 0.0D;
					} else {
						canStepX = false;
					}

					if(toBlockY > currentY) {
						nextY = (double)currentY + 1.0D;
					} else if(toBlockY < currentY) {
						nextY = (double)currentY + 0.0D;
					} else {
						canStepY = false;
					}

					if(toBlockZ > currentZ) {
						nextZ = (double)currentZ + 1.0D;
					} else if(toBlockZ < currentZ) {
						nextZ = (double)currentZ + 0.0D;
					} else {
						canStepZ = false;
					}

					double stepFractionX = 999.0D;
					double stepFractionY = 999.0D;
					double stepFractionZ = 999.0D;
					double deltaX = vTo.xCoord - vFrom.xCoord;
					double deltaY = vTo.yCoord - vFrom.yCoord;
					double deltaZ = vTo.zCoord - vFrom.zCoord;

					if(canStepX) {
						stepFractionX = (nextX - vFrom.xCoord) / deltaX;
					}

					if(canStepY) {
						stepFractionY = (nextY - vFrom.yCoord) / deltaY;
					}

					if(canStepZ) {
						stepFractionZ = (nextZ - vFrom.zCoord) / deltaZ;
					}

					// Advance to whichever axis boundary is crossed first
					byte face;
					if(stepFractionX < stepFractionY && stepFractionX < stepFractionZ) {
						if(toBlockX > currentX) {
							face = 4;
						} else {
							face = 5;
						}

						vFrom.xCoord = nextX;
						vFrom.yCoord += deltaY * stepFractionX;
						vFrom.zCoord += deltaZ * stepFractionX;
					} else if(stepFractionY < stepFractionZ) {
						if(toBlockY > currentY) {
							face = 0;
						} else {
							face = 1;
						}

						vFrom.xCoord += deltaX * stepFractionY;
						vFrom.yCoord = nextY;
						vFrom.zCoord += deltaZ * stepFractionY;
					} else {
						if(toBlockZ > currentZ) {
							face = 2;
						} else {
							face = 3;
						}

						vFrom.xCoord += deltaX * stepFractionZ;
						vFrom.yCoord += deltaY * stepFractionZ;
						vFrom.zCoord = nextZ;
					}

					Vec3D entryPoint = Vec3D.createVector(vFrom.xCoord, vFrom.yCoord, vFrom.zCoord);
					currentX = (int)(entryPoint.xCoord = (double)MathHelper.floor_double(vFrom.xCoord));
					if(face == 5) {
						--currentX;
						++entryPoint.xCoord;
					}

					currentY = (int)(entryPoint.yCoord = (double)MathHelper.floor_double(vFrom.yCoord));
					if(face == 1) {
						--currentY;
						++entryPoint.yCoord;
					}

					currentZ = (int)(entryPoint.zCoord = (double)MathHelper.floor_double(vFrom.zCoord));
					if(face == 3) {
						--currentZ;
						++entryPoint.zCoord;
					}

					blockId = this.getBlockId(currentX, currentY, currentZ);
					metadata = this.getBlockMetadata(currentX, currentY, currentZ);
					block = Block.blocksList[blockId];

					if(
						(
							!ignoreBlockWithoutBoundingBox || 
							block == null || 
							block.getCollisionBoundingBoxFromPool(this, currentX, currentY, currentZ) != null
						) && 
						blockId > 0 && 
						(block == null || block.canCollideCheck(metadata, stopOnLiquid))
					) {
						MovingObjectPosition pos = null;
						if(block != null) pos = block.collisionRayTrace(this, currentX, currentY, currentZ, vFrom, vTo);
						if(pos != null) {
							return pos;
						}
					}
				}

				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/** Plays a sound at the given entity's position for all world listeners. */
	public void playSoundAtEntity(Entity entity, String soundName, float volume, float pitch) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).playSound(soundName, entity.posX, entity.posY - (double)entity.yOffset, entity.posZ, volume, pitch);
		}

	}

	/** Plays a sound effect at the given world coordinates for all world listeners. */
	public void playSoundEffect(double x, double y, double z, String soundName, float volume, float pitch) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).playSound(soundName, x, y, z, volume, pitch);
		}

	}

	/** Plays a record (music disc) at the given block coordinates for all world listeners. */
	public void playRecord(String recordName, int x, int y, int z) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).playRecord(recordName, x, y, z);
		}

	}

	/** Spawns a named particle effect at the given position with velocity for all world listeners. */
	public void spawnParticle(String particleName, double x, double y, double z, double motionX, double motionY, double motionZ) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).spawnParticle(particleName, x, y, z, motionX, motionY, motionZ);
		}

	}

	/** Adds an entity to the weather-effects list (e.g. rain/snow particles, falling sand). */
	public boolean addWeatherEffect(Entity entity) {
		this.weatherEffects.add(entity);
		return true;
	}

	/** Delegates to {@link EntityManager} to add the entity to the world. */
	public boolean spawnEntityInWorld(Entity entity) {
		return this.entityManager.spawnEntityInWorld(entity);
	}
	
	/** Looks up an entity by its numeric ID across all loaded entity lists. */
	public Entity getEntityById(int id) {
		return this.entityManager.getEntityById(id);
	}

	/** Notifies all world accesses that an entity's skin (rendering data) should be obtained. */
	protected void obtainEntitySkin(Entity entity) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).obtainEntitySkin(entity);
		}

	}

	/** Notifies all world accesses that an entity's skin can be released. */
	protected void releaseEntitySkin(Entity entity) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).releaseEntitySkin(entity);
		}

	}

	/** Delegates to {@link EntityManager} to mark the entity as dead and remove it. */
	public void setEntityDead(Entity entity) {
		this.entityManager.setEntityDead(entity);
	}

	/** Delegates to {@link EntityManager} to remove a player entity from the world. */
	public void removePlayer(Entity entity) {
		this.entityManager.removePlayer(entity);
	}
	
	/** Registers a world access (rendering/lighting listener) to receive block/entity change notifications. */
	public void addWorldAccess(IWorldAccess worldAccess) {
		this.worldAccesses.add(worldAccess);
	}

	/** Unregisters a world access so it no longer receives change notifications. */
	public void removeWorldAccess(IWorldAccess worldAccess) {
		this.worldAccesses.remove(worldAccess);
	}
	
	/** Returns colliding bounding boxes in the world, excluding the given entity and water blocks. */
	public List<AxisAlignedBB> getCollidingBoundingBoxesExcludingWater(Entity entity, AxisAlignedBB aabb) {
		return this.entityQueryService.getCollidingBoundingBoxesExcludingWater(entity, aabb);
	}
	/** Returns all colliding bounding boxes in the world for the given entity's AABB. */
public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity, AxisAlignedBB aabb) {
		return this.entityQueryService.getCollidingBoundingBoxes(entity, aabb);
	}

	/** Returns the current skylight-subtracted value (0 at noon, higher at night). */
	public int getSkylightSubtracted() {
		return this.skylightTracker.getSkylightSubtracted();
	}

	/** Directly sets the skylight-subtracted value. */
	public void setSkylightSubtracted(int skylightSubtracted) {
		this.skylightTracker.setSkylightSubtracted(skylightSubtracted);
	}

	/** Computes the skylight-subtracted value for the given render partial tick. */
	public int calculateSkylightSubtracted(float renderPartialTick) {
		return this.skylightTracker.calculateSkylightSubtracted(renderPartialTick);
	}

	/** Returns the sun brightness (0.0-1.0) interpolated for the given partial tick. */
	public float getSunBrightness(float partialTick) {
		return AtmosphereCalculator.getSunBrightness(this, partialTick);
	}
	
	/** Returns the sky colour vector interpolated for the given partial tick. */
	public Vec3D getSkyColor(Entity entity, float renderPartialTick) {
		return AtmosphereCalculator.getSkyColor(this, renderPartialTick);
	}

	/** Returns the celestial angle (sun/moon rotation) interpolated for the given partial tick. */
	public float getCelestialAngle(float partialTick) {
		return this.worldProvider.calculateCelestialAngle(this.worldInfo.getWorldTime(), partialTick);
	}

	/** Returns the cloud colour vector interpolated for the given partial tick. */
	public Vec3D getCloudColor(float partialTick) {
		return AtmosphereCalculator.getCloudColor(this, partialTick);
	}

	/** Returns the fog colour vector interpolated for the given partial tick. */
	public Vec3D getFogColor(float partialTick) {
		return AtmosphereCalculator.getFogColor(this, partialTick);
	}

	/** Scans downward from Y=127 to find the top solid-or-liquid block, returning Y+1 or -1. */
	public int findTopSolidBlockUsingBlockMaterial(int x, int z) {
		Chunk chunk = this.getChunkFromBlockCoords(x, z);
		int y = 127;
		x &= 15;
		z &= 15;
		
		for(; y > 0; --y) {
			Block block = Block.blocksList[chunk.getBlockID(x, y, z)];
			if(block == null) continue;
			if(block.blockMaterial.getIsSolid() || block.blockMaterial.getIsLiquid()) {
				return y + 1;
			}
		}

		return -1;
	}

	/** Returns the star brightness (0.0-1.0) interpolated for the given partial tick. */
	public float getStarBrightness(float partialTick) {
		return AtmosphereCalculator.getStarBrightness(this, partialTick);
	}

	/** Scans downward from Y=127 to find the top solid block, returning Y+1 or -1. */
	public int findTopSolidBlock(int x, int z) {
		Chunk chunk = this.getChunkFromBlockCoords(x, z);
		int y = 127;
		x &= 15;

		for(z &= 15; y > 0; --y) {
			int blockId = chunk.getBlockID(x, y, z);
			if(blockId != 0 && Block.blocksList[blockId].blockMaterial.getIsSolid()) {
				return y + 1;
			}
		}

		return -1;
	}

	/** Schedules a block update at the given position after the specified tick delay. */
	public void scheduleBlockUpdate(int x, int y, int z, int blockID, int tickRate) {
		this.blockTickScheduler.scheduleBlockUpdate(x, y, z, blockID, tickRate);
	}

	/**
	 * Main per-tick entity update loop. Processes weather effects, updates all loaded entities
	 * (full update for those within simulation radius, tickExisted-only for distant ones),
	 * removes dead entities, and ticks all loaded tile entities.
	 */
	public void updateEntities() {
		int i;
		Entity entity;
		for(i = 0; i < this.weatherEffects.size(); ++i) {
			entity = (Entity)this.weatherEffects.get(i);
			entity.onUpdate();
			if(entity.isDead) {
				this.weatherEffects.remove(i--);
			}
		}

		this.entityManager.sweepUnloaded();

		int chunkX;
		int chunkZ;
		// Build the set of chunks within the simulation radius of every player.
		// Only entities inside these chunks receive full updates (AI, movement, collisions);
		// entities outside only have their ticksExisted advanced plus an isolated despawn pass.
		HashSet<Integer> activeChunks = new HashSet<Integer>();
		int simRadius = this.entitySimulationRadiusChunks;
		for(int p = 0; p < this.playerEntities.size(); ++p) {
			EntityPlayer player = (EntityPlayer)this.playerEntities.get(p);
			int playerChunkX = MathHelper.floor_double(player.posX / 16.0D);
			int playerChunkZ = MathHelper.floor_double(player.posZ / 16.0D);
			for(int dx = -simRadius; dx <= simRadius; ++dx) {
				for(int dz = -simRadius; dz <= simRadius; ++dz) {
					activeChunks.add(ChunkCoordIntPair.chunkXZ2Int(playerChunkX + dx, playerChunkZ + dz));
				}
			}
		}

		for(i = 0; i < this.getLoadedEntityList().size(); ++i) {
			entity = (Entity)this.getLoadedEntityList().get(i);
			if(entity.ridingEntity != null) {
				if(!entity.ridingEntity.isDead && entity.ridingEntity.riddenByEntity == entity) {
					continue;
				}

				entity.ridingEntity.riddenByEntity = null;
				entity.ridingEntity = null;
			}

			if(!entity.isDead) {
				boolean active = entity instanceof EntityPlayer
					|| activeChunks.contains(ChunkCoordIntPair.chunkXZ2Int(entity.chunkCoordX, entity.chunkCoordZ));

				if(active) {
					this.updateEntity(entity);
				} else {
					entity.ticksExisted++;
					this.tickDespawnOnly(entity);
				}
			}

			if(entity.isDead) {
				chunkX = entity.chunkCoordX;
				chunkZ = entity.chunkCoordZ;
				if(entity.addedToChunk && this.chunkExists(chunkX, chunkZ)) {
					this.getChunkFromChunkCoords(chunkX, chunkZ).removeEntity(entity);
				}

				this.entityManager.removeEntityFromWorldList(entity, i--);
			}
		}

		this.scanningTileEntities = true;
		Iterator<TileEntity> tileEntityIterator = this.loadedTileEntityList.iterator();

		while(tileEntityIterator.hasNext()) {
			TileEntity tileEntity = (TileEntity)tileEntityIterator.next();
			if(!tileEntity.isInvalid()) {
				tileEntity.updateEntity();
			}

			if(tileEntity.isInvalid()) {
				tileEntityIterator.remove();
				Chunk chunk = this.getChunkFromChunkCoords(tileEntity.xCoord >> 4, tileEntity.zCoord >> 4);
				if(chunk != null) {
					chunk.removeChunkBlockTileEntity(tileEntity.xCoord & 15, tileEntity.yCoord, tileEntity.zCoord & 15);
				}
			}
		}

		this.scanningTileEntities = false;
		if(!this.entityRemoval.isEmpty()) {
			Iterator<TileEntity> pendingIterator = this.entityRemoval.iterator();

			while(pendingIterator.hasNext()) {
				TileEntity pendingTileEntity = (TileEntity)pendingIterator.next();
				if(!pendingTileEntity.isInvalid()) {
					if(!this.loadedTileEntityList.contains(pendingTileEntity)) {
						this.loadedTileEntityList.add(pendingTileEntity);
					}

					Chunk chunk = this.getChunkFromChunkCoords(pendingTileEntity.xCoord >> 4, pendingTileEntity.zCoord >> 4);
					if(chunk != null) {
						chunk.setChunkBlockTileEntity(pendingTileEntity.xCoord & 15, pendingTileEntity.yCoord, pendingTileEntity.zCoord & 15, pendingTileEntity);
					}

					this.markBlockNeedsUpdate(pendingTileEntity.xCoord, pendingTileEntity.yCoord, pendingTileEntity.zCoord);
				}
			}

			this.entityRemoval.clear();
		}

	}

	/** Ticks the despawn logic for a single entity without a full update (used for distant entities). */
	private void tickDespawnOnly(Entity entity) {
		if(entity instanceof EntityLiving && !entity.isDead && !this.isRemote) {
			EntityLiving living = (EntityLiving)entity;
			++living.entityAge;
			living.performDespawn();
		}
	}

	/** Bulk-adds tile entities; defers to the pending list if the tile entity list is currently being scanned. */
	public void addTileEntity(Collection<TileEntity> tileEntities) {
		if(this.scanningTileEntities) {
			this.entityRemoval.addAll(tileEntities);
		} else {
			this.loadedTileEntityList.addAll(tileEntities);
		}

	}

	/** Full entity update: delegates to {@link #updateEntityWithOptionalForce} with force=true. */
	public void updateEntity(Entity entity) {
		this.updateEntityWithOptionalForce(entity, true);
	}

	/**
	 * Updates a single entity: saves previous position, runs its tick, validates position/rotation,
	 * moves it between chunks if it crossed a boundary, and recursively updates any ridden entity.
	 *
	 * @param entity  the entity to update
	 * @param doTick  if false, only the chunk bookkeeping is performed (no onUpdate call)
	 */
	public void updateEntityWithOptionalForce(Entity entity, boolean doTick) {
		int blockX = MathHelper.floor_double(entity.posX);
		int blockZ = MathHelper.floor_double(entity.posZ);
		byte viewRadius = 32;
		if(!doTick || this.checkChunksExist(blockX - viewRadius, 0, blockZ - viewRadius, blockX + viewRadius, Chunk.SECTION_HEIGHT, blockZ + viewRadius)) {
			entity.lastTickPosX = entity.posX;
			entity.lastTickPosY = entity.posY;
			entity.lastTickPosZ = entity.posZ;
			entity.prevRotationYaw = entity.rotationYaw;
			entity.prevRotationPitch = entity.rotationPitch;
			if(doTick && entity.addedToChunk) {
				if(entity.ridingEntity != null) {
					entity.updateRidden();
				} else {
					entity.onUpdate();
				}
			}

			// Reject NaN/Infinite positions by reverting to last tick's position
			if(Double.isNaN(entity.posX) || Double.isInfinite(entity.posX)) {
				entity.posX = entity.lastTickPosX;
			}

			if(Double.isNaN(entity.posY) || Double.isInfinite(entity.posY)) {
				entity.posY = entity.lastTickPosY;
			}

			if(Double.isNaN(entity.posZ) || Double.isInfinite(entity.posZ)) {
				entity.posZ = entity.lastTickPosZ;
			}

			if(Double.isNaN((double)entity.rotationPitch) || Double.isInfinite((double)entity.rotationPitch)) {
				entity.rotationPitch = entity.prevRotationPitch;
			}

			if(Double.isNaN((double)entity.rotationYaw) || Double.isInfinite((double)entity.rotationYaw)) {
				entity.rotationYaw = entity.prevRotationYaw;
			}

			// Move the entity between chunks if it crossed a 16-block boundary
			int chunkX = MathHelper.floor_double(entity.posX / 16.0D);
			int chunkY = MathHelper.floor_double(entity.posY / 16.0D);
			int chunkZ = MathHelper.floor_double(entity.posZ / 16.0D);
			if(!entity.addedToChunk || entity.chunkCoordX != chunkX || entity.chunkCoordY != chunkY || entity.chunkCoordZ != chunkZ) {
				if(entity.addedToChunk && this.chunkExists(entity.chunkCoordX, entity.chunkCoordZ)) {
					this.getChunkFromChunkCoords(entity.chunkCoordX, entity.chunkCoordZ).removeEntityAtIndex(entity, entity.chunkCoordY);
				}

				if(this.chunkExists(chunkX, chunkZ)) {
					entity.addedToChunk = true;
					this.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(entity);
				} else {
					entity.addedToChunk = false;
				}
			}

			// Recursively update any entity riding this one
			if(doTick && entity.addedToChunk && entity.riddenByEntity != null) {
				if(!entity.riddenByEntity.isDead && entity.riddenByEntity.ridingEntity == entity) {
					this.updateEntity(entity.riddenByEntity);
				} else {
					entity.riddenByEntity.ridingEntity = null;
					entity.riddenByEntity = null;
				}
			}

		}
	}

	/** Returns true if the AABB contains no non-air blocks. */
	public boolean checkIfAABBIsClear(AxisAlignedBB aabb) {
		return this.entityQueryService.checkIfAABBIsClear(aabb);
	}

	/** Returns true if the AABB overlaps any non-empty block. */
	public boolean getIsAnyNonEmptyBlock(AxisAlignedBB aabb) {
		return this.entityQueryService.getIsAnyNonEmptyBlock(aabb);
	}

	/** Returns true if the AABB overlaps any liquid block. */
	public boolean getIsAnyLiquid(AxisAlignedBB aabb) {
		return this.entityQueryService.getIsAnyLiquid(aabb);
	}
	
	/** Returns true if the AABB overlaps any block with the given ID. */
	public boolean getIsAnyBlockID(AxisAlignedBB aabb, int blockID) {
		return this.entityQueryService.getIsAnyBlockID(aabb, blockID);
	}

	/** Returns true if the AABB overlaps any burning block. */
	public boolean isBoundingBoxBurning(AxisAlignedBB aabb) {
		return this.entityQueryService.isBoundingBoxBurning(aabb);
	}

	/** Returns true if the given material accelerates (pushes) entities inside the AABB. */
	public boolean handleMaterialAcceleration(AxisAlignedBB aabb, Material material, Entity entity) {
		return this.entityQueryService.handleMaterialAcceleration(aabb, material, entity);
	}

	/** Returns true if the AABB contains any block made of the given material. */
	public boolean isMaterialInBB(AxisAlignedBB aabb, Material material) {
		return this.entityQueryService.isMaterialInBB(aabb, material);
	}

	/** Returns true if every block inside the AABB is made of the given material. */
	public boolean isAABBInMaterial(AxisAlignedBB aabb, Material material) {
		return this.entityQueryService.isAABBInMaterial(aabb, material);
	}

	/**
	 * Convenience wrapper around {@link #newExplosion} that always detonates a non-flaming
	 * explosion.
	 */
	public Explosion createExplosion(Entity exploder, double x, double y, double z, float radius) {
		return this.newExplosion(exploder, x, y, z, radius, false);
	}
	
	/**
	 * Convenience wrapper around {@link #newBlockExplosion} that always detonates a non-flaming
	 * block explosion.
	 */
	public Explosion createBlockExplosion(Entity exploder, double x, double y, double z, float radius, int blockID) {
		return this.newBlockExplosion(exploder, x, y, z, radius, blockID, false);
	}

	/**
	 * Creates an explosion at the given position and detonates it immediately, clearing blocks and
	 * damaging/launching nearby entities. The base implementation also plays the explosion sound
	 * and spawns the particle effects; the dedicated server overrides this method to send the
	 * destroyed block positions to every nearby player instead.
	 *
	 * @param exploder  the entity that caused the explosion, or null
	 * @param x         the explosion's X position
	 * @param y         the explosion's Y position
	 * @param z         the explosion's Z position
	 * @param radius    the explosion's size/radius
	 * @param flaming   whether to leave fires on destroyed surfaces
	 * @return the resolved explosion
	 */
	public Explosion newExplosion(Entity exploder, double x, double y, double z, float radius, boolean flaming) {
		Explosion explosion = new Explosion(this, exploder, x, y, z, radius);
		explosion.isFlaming = flaming;
		explosion.doExplosion();
		explosion.doEffects(true);
		return explosion;
	}
	
	/**
	 * Creates a block explosion at the given position and detonates it immediately. Unlike
	 * {@link #newExplosion}, every destroyed block position is replaced with the given {@code blockID}
	 * instead of dropping the block as an item. The dedicated server overrides this method to send
	 * the destroyed block positions to every nearby player instead.
	 *
	 * @param exploder  the entity that caused the explosion, or null
	 * @param x         the explosion's X position
	 * @param y         the explosion's Y position
	 * @param z         the explosion's Z position
	 * @param radius    the explosion's size/radius
	 * @param blockID   the block to place over every destroyed position
	 * @param flaming   whether to leave fires on destroyed surfaces
	 * @return the resolved explosion
	 */
	public Explosion newBlockExplosion(Entity exploder, double x, double y, double z, float radius, int blockID, boolean flaming) {
		Explosion explosion = new Explosion(this, exploder, x, y, z, radius, blockID);
		explosion.isFlaming = flaming;
		explosion.doExplosion();
		explosion.doEffects(true);
		return explosion;
	}

	/** Returns the density of solid blocks around the given point within the AABB (0.0-1.0). */
	public float getBlockDensity(Vec3D point, AxisAlignedBB aabb) {
		return this.entityQueryService.getBlockDensity(point, aabb);
	}

	/**
	 * Called when a player right-clicks (hits) a block. If the adjacent block is fire,
	 * extinguishes it and returns true; otherwise returns false.
	 */
	public boolean onBlockHit(EntityPlayer entityPlayer, int x, int y, int z, int sideHit) {
		if(sideHit == 0) {
			--y;
		}

		if(sideHit == 1) {
			++y;
		}

		if(sideHit == 2) {
			--z;
		}

		if(sideHit == 3) {
			++z;
		}

		if(sideHit == 4) {
			--x;
		}

		if(sideHit == 5) {
			++x;
		}

		if(this.getBlockId(x, y, z) == Block.fire.blockID) {
			this.playAuxSFXAtEntity(entityPlayer, 1004, x, y, z, 0);
			this.setBlockWithNotify(x, y, z, 0);
			return true;
		} else {
			return false;
		}
	}

	/** Returns the single entity of the given class in the world (unimplemented single-observer stub; returns null). */
	public Entity getEntityByClass(Class<?> entityClass) {
		return null;
	}

	/** Returns a debug string describing the loaded entity count. */
	public String getDebugLoadedEntities() {
		return "All: " + this.getLoadedEntityList().size();
	}

	/** Returns the name of the underlying chunk provider. */
	public String getProviderName() {
		return this.chunkProvider.makeString();
	}

	/** Returns the tile entity at the given coordinates, or null. */
	public TileEntity getBlockTileEntity(int x, int y, int z) {
		Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		return chunk != null ? chunk.getChunkBlockTileEntity(x & 15, y, z & 15) : null;
	}

	/** Returns the block entity at the given coordinates, or null. */
	public EntityBlockEntity getBlockEntity(int x, int y, int z) {
		Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		return chunk != null ? chunk.getChunkBlockEntity(x & 15, y, z & 15) : null;
	}

	/** Returns the block entity at the given coordinates without creating one, or null. */
	public EntityBlockEntity getBlockEntityIfExists(int x, int y, int z) {
		Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		return chunk != null ? chunk.getChunkBlockEntityIfExists(x & 15, y, z & 15) : null;
	}

	/** Associates the given tile entity with the block at the specified coordinates. */
	public void setBlockTileEntity(int x, int y, int z, TileEntity tileEntity) {
		if(!tileEntity.isInvalid()) {
			if(this.scanningTileEntities) {
				tileEntity.xCoord = x;
				tileEntity.yCoord = y;
				tileEntity.zCoord = z;
				this.entityRemoval.add(tileEntity);
			} else {
				this.loadedTileEntityList.add(tileEntity);
				Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
				if(chunk != null) {
					chunk.setChunkBlockTileEntity(x & 15, y, z & 15, tileEntity);
				}
			}
		}

	}
	
	/** Associates the given block entity with the block at the specified coordinates. */
	public void setBlockEntity(int x, int y, int z, EntityBlockEntity entity) {
		Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		if(chunk != null) {
			chunk.setChunkBlockEntity(x & 15, y, z & 15, entity);
		}
	}

	/** Removes the tile entity at the given coordinates, deferring invalidation if the lists are being scanned. */
	public void removeBlockTileEntity(int x, int y, int z) {
		TileEntity tileEntity = this.getBlockTileEntity(x, y, z);
		if(tileEntity != null && this.scanningTileEntities) {
			tileEntity.invalidate();
		} else {
			// TODO : THIS IS CHEESERY!
			//if(tileEntity != null) {
				this.loadedTileEntityList.remove(tileEntity);
			//}

			Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
			if(chunk != null) {
				chunk.removeChunkBlockTileEntity(x & 15, y, z & 15);
			}
		}

	}

	/** Removes the block entity at the given coordinates. */
	public void removeBlockEntity(int x, int y, int z) {
		Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		if(chunk != null) {
			chunk.removeChunkBlockEntity(x & 15, y, z & 15);
		}
	}
	
	/** Returns true if the block is an opaque (non-see-through) cube. */
	public boolean isBlockOpaqueCube(int x, int y, int z) {
		Block block = Block.blocksList[this.getBlockId(x, y, z)];
		return block == null ? false : block.isOpaqueCube();
	}

	/** Returns true if the block is a normal (translucent, renderable) cube. */
	public boolean isBlockNormalCube(int x, int y, int z) {
		Block block = Block.blocksList[this.getBlockId(x, y, z)];
		return block == null ? false : block.blockMaterial.getIsTranslucent() && block.renderAsNormalBlock();
	}

	/** Saves the world with an indirect save progress update. */
	public void saveWorldIndirectly(IProgressUpdate progressUpdate) {
		this.saveWorld(true, progressUpdate);
	}

	/** Recalculates the initial skylight value for world load. */
	public void calculateInitialSkylight() {
		this.skylightTracker.updateSkylightSubtracted(1.0F);
	}

	/** Enables or disables the spawning of hostile and peaceful mobs. */
	public void setAllowedMobSpawns(boolean spawnHostile, boolean spawnPeaceful) {
		this.spawnHostileMobs = spawnHostile;
		this.spawnPeacefulMobs = spawnPeaceful;
	}

	/**
	 * Main world tick. Advances weather, handles sleeping, spawns mobs (with a blood-moon bonus
	 * pass), unloads old chunks, increments the time-of-day, autosaves, and runs daily tasks.
	 */
	public void tick() {
		this.updateWeather();
		long worldTime;
		
		// No sleeping here so
		
		if(this.isAllPlayersFullyAsleep()) {
			boolean spawnedDuringSleep = false;
			if(this.spawnHostileMobs && this.difficultySetting >= 1) {
				spawnedDuringSleep = SpawnerAnimals.performSleepSpawning(this, this.playerEntities);
			}

			if(!spawnedDuringSleep) {
				worldTime = this.worldInfo.getWorldTime() + 24000L;
				this.worldInfo.setWorldTime(worldTime - worldTime % 24000L);
				this.wakeUpAllPlayers();
			}
		}
		
		SpawnerAnimals.performSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs);
		
		// During world moon we do this hack of a solution to increase spawning rate.
		if(this.worldInfo.isBloodMoon()) SpawnerAnimals.performSpawning(this, this.spawnHostileMobs, false);
			
		this.chunkProvider.unload100OldestChunks();
		
		this.skylightTracker.updateSkylightSubtracted(1.0F);

		worldTime = this.worldInfo.getWorldTime() + 1L;
		int hourOfTheDay = (int)(worldTime % 24000L);
		
		if(worldTime % (long)this.autosavePeriod == 0L) {
			this.saveWorld(false, (IProgressUpdate)null);
		}

		this.worldInfo.setWorldTime(worldTime);
		
		this.badMoonDecide(worldTime, hourOfTheDay);
		this.updateDailyTasks(worldTime, hourOfTheDay);
		this.TickUpdates(false);
		this.updateBlocksAndPlayCaveSounds();
	}
	
	/** Decides once per day (just before dusk) whether tonight is a blood moon. */
	protected void badMoonDecide(long worldTime, int hourOfTheDay) {
		if(hourOfTheDay == Seasons.dayLengthTicks - 500) {
			if (this.badMoonDecide == false) {
				this.worldInfo.setBloodMoon((rand.nextInt(10) == 0 || this.nextMoonBad) ? true : false);
				System.out.println ("Blood moon = " + this.worldInfo.isBloodMoon());
				this.badMoonDecide = true;
				this.nextMoonBad = false;
			}
		} else this.badMoonDecide = false;
	}
	
	/** Runs per-day tasks: blood-moon banner text, season counter advancement, and seasonal weather re-rolls. */
	protected void updateDailyTasks(long worldTime, int hourOfTheDay) {
		// Blood moon
		if(hourOfTheDay == Seasons.dayLengthTicks && this.worldInfo.isBloodMoon()) {
			if (this.badMoonText == false) {
				this.getWorldAccess(0).showString("Bad Moon Rising");
			}
			this.badMoonText = true;
		} else this.badMoonText = false;

		if(hourOfTheDay == 0) this.worldInfo.setBloodMoon(false);
		
		if(hourOfTheDay == 18000) {
			int oldCurrentSeason = Seasons.currentSeason;
			
			Seasons.dayOfTheYear ++;
			Seasons.updateSeasonCounters();
			
			// Leaves change colours so
			for(int i = 0; i < this.worldAccesses.size(); ++i) {
				((IWorldAccess)this.worldAccesses.get(i)).updateAllRenderers();
			}
			
			if(Seasons.currentSeason != oldCurrentSeason) {
				if(Seasons.currentSeason == Seasons.WINTER) {
					if(!this.worldInfo.getSnowing()) {
						int newSnowingTime = Weather.getTimeForNextSnow(this.rand);
						if(newSnowingTime < this.worldInfo.getSnowingTime()) {
							this.worldInfo.setSnowingTime(newSnowingTime);
						}
					}
					if(this.worldInfo.getRaining()) {
						int newRainingTime = 3000 + this.rand.nextInt(3000);
						if(this.worldInfo.getRainTime() > newRainingTime) this.worldInfo.setRainTime(newRainingTime);
					}
				}
				
				if(!this.worldInfo.getRaining() && (Seasons.currentSeason == Seasons.SPRING || Seasons.currentSeason == Seasons.AUTUMN)) {
					int newRainingTime = Weather.getTimeForNextRain(this.rand);
					if(newRainingTime < this.worldInfo.getRainTime()) {
						this.worldInfo.setRainTime(newRainingTime);
					}
				}
				
				this.getWorldAccess(0).showString(Seasons.seasonNames[Seasons.currentSeason]);
			}
		}
	}

	/** Initializes the rain, snow, and thunder timers to their first scheduled occurrences. */
	private void initializeWeather() {
		this.worldInfo.setRainTime(Weather.getTimeForNextRain(this.rand));
		this.worldInfo.setSnowingTime(Weather.getTimeForNextSnow(this.rand));
		this.worldInfo.setThunderTime(Weather.getTimeForNextThunder(this.rand));
	}
	
	/** Sets the rain/thunder/snow strengths to full if the world starts in a storm. */
	private void calculateInitialWeather() {
		if(this.worldInfo.getRaining()) {
			this.rainingStrength = 1.0F;	
		}
		
		if(this.worldInfo.getThundering()) {
			this.thunderingStrength = 1.0F;
		}

		if(this.worldInfo.getSnowing()) {
			this.snowingStrength = 1.0F;
		}
	}

	/** Advances the thunder, snow, and rain strengths toward their targets each tick, starting/ending storms on schedule. */
	protected void updateWeather() {
		if(!this.worldProvider.hasNoSky) {
			
			// Lightning bolts
			
			if(this.lastLightningBolt > 0) {
				--this.lastLightningBolt;
			}
			
			// Thunderstorm. In this version, it is independent of rainstorms.

			int thunderTime = this.worldInfo.getThunderTime();
			--thunderTime;
			this.worldInfo.setThunderTime(thunderTime);
			
			if(thunderTime <= 0) {
				if(this.worldInfo.getThundering()) {
					this.worldInfo.setThunderTime(Weather.getTimeForNextThunder(this.rand));
				} else {
					this.worldInfo.setThunderTime(Weather.getTimeForThunderingEnd(this.rand));
				}
				
				System.out.println ("Time for the next thundering time change " + this.worldInfo.getThunderTime());
				this.worldInfo.setThundering(!this.worldInfo.getThundering());
			}
			
			this.prevThunderingStrength = this.thunderingStrength;
			
			if(this.worldInfo.getThundering()) {
				this.thunderingStrength = (float)((double)this.thunderingStrength + 0.01D);
			} else {
				this.thunderingStrength = (float)((double)this.thunderingStrength - 0.01D);
			}

			if(this.thunderingStrength < 0.0F) {
				this.thunderingStrength = 0.0F;
			}

			if(this.thunderingStrength > 1.0F) {
				this.thunderingStrength = 1.0F;
			}

			// Snowstorm
			
			int snowingTime = this.worldInfo.getSnowingTime();
			--snowingTime;
			this.worldInfo.setSnowingTime(snowingTime);
			
			if(snowingTime <= 0) {
				if(this.worldInfo.getSnowing()) {
					this.worldInfo.setSnowingTime(Weather.getTimeForNextSnow(this.rand));
				} else {
					this.worldInfo.setSnowingTime(Weather.getTimeForSnowingEnd(this.rand));
				}
				
				System.out.println ("Time for the next snowing time change " + this.worldInfo.getSnowingTime());
				this.worldInfo.setSnowing(!this.worldInfo.getSnowing());
			} 

			this.prevSnowingStrength = this.snowingStrength;
			
			if(this.worldInfo.getSnowing()) {
				this.snowingStrength = (float)((double)this.snowingStrength + 0.01D);
			} else {
				this.snowingStrength = (float)((double)this.snowingStrength - 0.01D);
			}

			if(this.snowingStrength < 0.0F) {
				this.snowingStrength = 0.0F;
			}

			if(this.snowingStrength > 1.0F) {
				this.snowingStrength = 1.0F;
			}
			
			// Rains

			int rainingTime = this.worldInfo.getRainTime();
			--rainingTime;
			this.worldInfo.setRainTime(rainingTime);
			
			if(rainingTime <= 0) {
				if(this.worldInfo.getRaining()) {
					this.worldInfo.setRainTime(Weather.getTimeForNextRain(this.rand));
					this.lightningChance = 60000;
				} else {
					this.worldInfo.setRainTime(Weather.getTimeForRainingEnd(this.rand));
					this.lightningChance = 50000;
				}
				
				System.out.println ("Time for the next thundering time change " + this.worldInfo.getRainTime());
				this.worldInfo.setRaining(!this.worldInfo.getRaining());
			} 

			this.prevRainingStrength = this.rainingStrength;
			
			if(this.worldInfo.getRaining()) {
				this.rainingStrength = (float)((double)this.rainingStrength + 0.01D);
			} else {
				this.rainingStrength = (float)((double)this.rainingStrength - 0.01D);
			}

			if(this.rainingStrength < 0.0F) {
				this.rainingStrength = 0.0F;
			}

			if(this.rainingStrength > 1.0F) {
				this.rainingStrength = 1.0F;
			}

		}
	}

	/** Clears all active weather by resetting the timers and disabling rain and thunder. */
	private void clearWeather() {
		this.worldInfo.setRainTime(0);
		this.worldInfo.setRaining(false);
		this.worldInfo.setThunderTime(0);
		this.worldInfo.setThundering(false);
	}

	/**
	 * Selects the set of chunks around each player that should receive per-frame processing,
	 * then for each chunk: plays cave sounds, spawns lightning bolts, manages snow cover, and
	 * ticks a random selection of blocks.
	 */
	protected void updateBlocksAndPlayCaveSounds() {
		this.positionsToUpdate.clear();

		int originX;
		int originZ;
		int x;
		int y;
		int z;
		int tIndex;
		int blockId;

		// First make a list of chunks to update: a square centered in *each* player
		byte radius = 8; 	// Changed 9 to 8

		for(int i = 0; i < this.playerEntities.size(); ++i) {
			EntityPlayer entityPlayer = (EntityPlayer)this.playerEntities.get(i);
			originX = MathHelper.floor_double(entityPlayer.posX / 16.0D);
			originZ = MathHelper.floor_double(entityPlayer.posZ / 16.0D);

			for(x = -radius; x <= radius; ++x) {
				for(z = -radius; z <= radius; ++z) {
					if(this.chunkExists(x + originX, z + originZ)) {
						this.positionsToUpdate.add(new ChunkCoordIntPair(x + originX, z + originZ));
					}
				}
			}
		}

		if(this.soundCounter > 0) {
			--this.soundCounter;
		}

		// Update chunks
		Iterator<ChunkCoordIntPair> chunkIterator = this.positionsToUpdate.iterator();

		while(chunkIterator.hasNext()) {
			ChunkCoordIntPair chunkCoordIntPair = (ChunkCoordIntPair)chunkIterator.next();

			// Block coordinates at the beginning of this chunk
			originX = chunkCoordIntPair.chunkXPos * 16;
			originZ = chunkCoordIntPair.chunkZPos * 16;

			// Get this chunk
			Chunk chunk = this.getChunkFromChunkCoords(chunkCoordIntPair.chunkXPos, chunkCoordIntPair.chunkZPos);

			// Select a dark block and play an eerie sound on closest player

			if(this.soundCounter == 0) {
				this.updateLCG = this.updateLCG * 3 + DIST_HASH_MAGIC;
				tIndex = this.updateLCG >> 2;
				x = tIndex & 15;
				z = tIndex >> 8 & 15;
				y = tIndex >> 16 & 127;
				blockId = chunk.getBlockID(x, y, z);
				x += originX;
				z += originZ;
				if(blockId == 0 && this.getFullBlockLightValue(x, y, z) <= this.rand.nextInt(8) && this.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) <= 0) {
					EntityPlayer entityPlayer = this.getClosestPlayer((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, 8.0D);
					
					if(entityPlayer != null && entityPlayer.getDistanceSq((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D) > 4.0D) {
						this.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + this.rand.nextFloat() * 0.2F);
						this.soundCounter = this.rand.nextInt(12000) + 6000;
					}
				}
			}
			
			// Thunder hits
			if(this.worldInfo.getThundering() && this.rand.nextInt(this.lightningChance) == 0) {
				this.updateLCG = this.updateLCG * 3 + DIST_HASH_MAGIC;
				tIndex = this.updateLCG >> 2;
				x = originX + (tIndex & 15);
				z = originZ + (tIndex >> 8 & 15);
				y = this.findTopSolidBlockUsingBlockMaterial(x, z);
				
				// Let's find a lightning rod - that is, a close iron block which is higher than y
				byte rodRadius = 16;
				outterRodCheck:
				for(int xx = x - rodRadius; xx <= x + rodRadius; xx ++) {
					for(int zz = z - rodRadius; zz <= z + rodRadius; zz ++) {
						int yy = this.findTopSolidBlockUsingBlockMaterial(xx, zz);
						if(yy > y) {
							if(this.getBlockId(xx, yy, zz) == Block.blockSteel.blockID) {
								x = xx;
								y = yy;
								z = zz;
								
								// Cheesy
								//Minecraft.getMinecraft().thePlayer.triggerAchievement(AchievementList.lightningRod);
								
								break outterRodCheck;
							}
						}
					}
				}
				
				if(this.canBlockBeRainedOnForBolts(x, y, z)) {
					this.addWeatherEffect(new EntityLightningBolt(this, (double)x, (double)y, (double)z));
					this.lastLightningBolt = 2;
				}
			}

			// Select a top block and cover / uncover with snow
			if(GameSettingsValues.meltBuild) {
				this.snowTicker --;
				if(this.snowTicker <= 0) {
					this.snowTicker = 6;
					this.updateLCG = this.updateLCG * 3 + this.DIST_HASH_MAGIC;
					tIndex = this.updateLCG >> 2;
					x = tIndex & 15;
					z = tIndex >> 8 & 15;
					
					// Cover if particle decide happens to be "snow"
					BiomeGenBase biomegenbase = chunk.getBiomeGenAt(x, z);
					
					if(!biomegenbase.isPermaFrost()) {
						int particleType = Weather.particleDecide(biomegenbase, this);
						y = this.findTopSolidBlockUsingBlockMaterial(x + originX, z + originZ);
						
						if(y > 0) {
							int thisBlockID = chunk.getBlockID(x, y, z);
							Block thisBlock = Block.blocksList[thisBlockID];
						
							blockId = chunk.getBlockID(x, y - 1, z);
							
							if(particleType == Weather.SNOW) { 
								// Freeze / drop snow 
								
								if(thisBlockID == 0 || thisBlockID == Block.leafPile.blockID) {
									if (Block.snow.canPlaceBlockAt(this, x + originX, y, z + originZ)) {
										this.setBlockWithNotify(x + originX, y, z + originZ, Block.snow.blockID);
									}
								} else if(thisBlockID == Block.snow.blockID || (thisBlock != null && thisBlock.getRenderType() == 111)) {
									int meta = chunk.getBlockMetadata(x, y, z);
									if((meta & 15) < 15) chunk.setBlockMetadata(x, y, z, meta + 1);
								}
			
							} else if (rand.nextInt(4) == 0 && (Seasons.currentSeason != Seasons.WINTER || biomegenbase.weather != Weather.cold)) {
								// Unfreeze / remove snow 
								
								if (thisBlockID == Block.snow.blockID) {
									//this.setBlockWithNotify(x + originX, y, z + originZ, 0);
									chunk.setBlockID(x, y, z, 0);
								}
			
							}
						}
					}
				}
			}

			// Tick random blocks: iterate each materialised subchunk with a fixed per-subchunk
			// LCG budget (World.blocksToTickPerFrame probes). A fresh chunk therefore performs
			// the same 10 * 8 = 80 probes as the historical flat clean loop, while the per-cell
			// tick probability stays constant as more subchunks are built upward.
			for(int subchunkIndex = 0; subchunkIndex < chunk.getSubchunkCount(); ++subchunkIndex) {
				if(chunk.isSubchunkEmpty(subchunkIndex)) {
					continue;
				}

				for(int i = 0; i < World.blocksToTickPerFrame; ++i) {
					this.updateLCG = this.updateLCG * 3 + this.DIST_HASH_MAGIC;
					tIndex = this.updateLCG >> 2;
					x = tIndex & 15;
					z = tIndex >> 6 & 15;
					y = (subchunkIndex << 4) | (tIndex >> 10 & 15);
					blockId = chunk.getBlockID(x, y, z);
					if(Block.tickOnLoad[blockId]) {
						Block.blocksList[blockId].updateTick(this, x + originX, y, z + originZ, this.rand);
					}
				}
			}
		}

	}

	/** Processes pending block ticks, returning true if any ticks remained. */
	public boolean TickUpdates(boolean allPending) {
		return this.blockTickScheduler.tickUpdates(allPending);
	}

	/** Triggers random display ticks (particles, sounds, etc.) for blocks near the given position. */
	public void randomDisplayUpdates(int centerX, int centerY, int centerZ) {
		byte radius = 16;
		Random random = new Random();

		for(int i = 0; i < 1000; ++i) {
			int x = centerX + this.rand.nextInt(radius) - this.rand.nextInt(radius);
			int y = centerY + this.rand.nextInt(radius) - this.rand.nextInt(radius);
			int z = centerZ + this.rand.nextInt(radius) - this.rand.nextInt(radius);
			int blockId = this.getBlockId(x, y, z);
			Block block = Block.blocksList[blockId];
			if(block != null) {
				block.randomDisplayTick(this, x, y, z, random);
			}
		}

	}

	/** Returns entities within the given AABB, excluding the specified entity. */
	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB aabb) {
		return this.entityQueryService.getEntitiesWithinAABBExcludingEntity(entity, aabb);
	}

	/** Returns all entities of the given type within the AABB. */
	public List<Entity> getEntitiesWithinAABB(Class<?> entityClass, AxisAlignedBB aabb) {
		return this.entityQueryService.getEntitiesWithinAABB(entityClass, aabb);
	}

	/** Returns the nearest entity of the given type within the AABB, relative to the source entity. */
	public Entity findNearestEntityWithinAABB(Class<?> entityClass, AxisAlignedBB aabb, Entity source) {
		return this.entityQueryService.findNearestEntityWithinAABB(entityClass, aabb, source);
	}

	/** Returns the list of currently loaded entities. */
	public List<Entity> getLoadedEntityList() {
		return this.entityManager.getLoadedEntityList();
	}

	/** Notifies world accesses of a tile entity update without triggering block changes. */
	public void updateTileEntityChunkAndDoNothing(int x, int y, int z, TileEntity tileEntity) {
		if(this.blockExists(x, y, z)) {
			this.getChunkFromBlockCoords(x, z).setChunkModified();
		}

		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).doNothingWithTileEntity(x, y, z, tileEntity);
		}

	}

	/** Returns the count of entities matching the given class. */
	public int countEntities(Class<?> entityClass) {
		return this.entityManager.countEntities(entityClass);
	}

	/** Returns a cached count of entities matching the given class (fast-path for mob types). */
	public int getCachedEntityCount(Class<?> entityClass) {
		return this.entityManager.getCachedEntityCount(entityClass);
	}

	/** Bulk-adds entities to the loaded entity list. */
	public void addLoadedEntities(List<Entity> entities) {
		this.entityManager.addLoadedEntities(entities);
	}

	/** Schedules the given entities to be unloaded at the end of the tick. */
	public void unloadEntities(List<Entity> entities) {
		this.entityManager.unloadEntities(entities);
	}

	/** Repeatedly unloads the oldest chunks until none remain. */
	public void dropOldChunks() {
		while(this.chunkProvider.unload100OldestChunks()) {
		}

	}

	/** Returns true if the given block can be placed at the position, considering collisions and existing blocks. */
	public boolean canBlockBePlacedAt(int blockID, int x, int y, int z, boolean checkCollision, int side) {
		int existingId = this.getBlockId(x, y, z);
		Block existingBlock = Block.blocksList[existingId];
		Block newBlock = Block.blocksList[blockID];
		AxisAlignedBB collisionBox = newBlock.getCollisionBoundingBoxFromPool(this, x, y, z);
		if(checkCollision) {
			collisionBox = null;
		}

		if(collisionBox != null && !this.checkIfAABBIsClear(collisionBox)) {
			return false;
		} else {
			if (existingBlock != null) {
				if(existingBlock == Block.waterMoving || existingBlock == Block.waterStill || existingBlock == Block.lavaMoving || existingBlock == Block.lavaStill || existingBlock == Block.fire || existingBlock == Block.snow || existingBlock == Block.layeredSand || (existingBlock instanceof BlockFlower) || existingBlock.blockMaterial.getIsGroundCover()) {
					existingBlock = null;
				}
			}

			return blockID > 0 && existingBlock == null && newBlock.canPlaceBlockOnSide(this, x, y, z, side);
		}
	}

	/** Overload that also considers the item stack being placed (for items with special placement logic). */
	public boolean canBlockBePlacedAt(int blockID, int x, int y, int z, boolean checkCollision, int side, ItemStack itemStack) {
		int existingId = this.getBlockId(x, y, z);
		Block existingBlock = Block.blocksList[existingId];
		Block newBlock = Block.blocksList[blockID];
		AxisAlignedBB collisionBox = newBlock.getCollisionBoundingBoxFromPool(this, x, y, z);
		if(checkCollision) {
			collisionBox = null;
		}

		if(collisionBox != null && !this.checkIfAABBIsClear(collisionBox)) {
			return false;
		} else {
			
			if (existingBlock != null) {
				if(existingBlock == Block.waterMoving || existingBlock == Block.waterStill || existingBlock == Block.lavaMoving || existingBlock == Block.lavaStill || existingBlock == Block.fire || existingBlock == Block.snow || existingBlock == Block.layeredSand || (existingBlock instanceof BlockFlower) || existingBlock.blockMaterial.getIsGroundCover()) {
					existingBlock = null;
				}
			}

			return blockID > 0 && existingBlock == null && newBlock.canPlaceBlockOnSide(this, x, y, z, side, itemStack);
		}
	}
	
	/** Creates a path from the source entity to the target entity within the given range. */
	public PathEntity getPathToEntity(Entity source, Entity target, float range) {
		int srcX = MathHelper.floor_double(source.posX);
		int srcY = MathHelper.floor_double(source.posY);
		int srcZ = MathHelper.floor_double(source.posZ);
		int margin = (int)(range + 16.0F);
		int minX = srcX - margin;
		int minY = srcY - margin;
		int minZ = srcZ - margin;
		int maxX = srcX + margin;
		int maxY = srcY + margin;
		int maxZ = srcZ + margin;
		ChunkCache chunkCache = new ChunkCache(this, minX, minY, minZ, maxX, maxY, maxZ);
		return (new Pathfinder(chunkCache)).createEntityPathTo(source, target, range);
	}
	
	/** Creates a path with full navigation options (water/door avoidance, etc.). */
	public PathEntity getPathEntityToEntity(Entity entity, Entity target, float range, boolean avoidWater, boolean avoidBreakDoors, boolean avoidBlocks, boolean canSwim) {
		int srcX = MathHelper.floor_double(entity.posX);
		int srcY = MathHelper.floor_double(entity.posY + 1.0D);
		int srcZ = MathHelper.floor_double(entity.posZ);
		int margin = (int)(range + 16.0F);
		int minX = srcX - margin;
		int minY = srcY - margin;
		int minZ = srcZ - margin;
		int maxX = srcX + margin;
		int maxY = srcY + margin;
		int maxZ = srcZ + margin;
		ChunkCache chunkCache = new ChunkCache(this, minX, minY, minZ, maxX, maxY, maxZ);
		PathEntity path = (new PathfinderRelease(chunkCache, avoidWater, avoidBreakDoors, avoidBlocks, canSwim)).createEntityPathTo(entity, target, range);
		return path;
	}

	/** Creates a path from the entity to the given XYZ block coordinates within the range. */
	public PathEntity getEntityPathToXYZ(Entity entity, int targetX, int targetY, int targetZ, float range) {
		int srcX = MathHelper.floor_double(entity.posX);
		int srcY = MathHelper.floor_double(entity.posY);
		int srcZ = MathHelper.floor_double(entity.posZ);
		int margin = (int)(range + 8.0F);
		int minX = srcX - margin;
		int minY = srcY - margin;
		int minZ = srcZ - margin;
		int maxX = srcX + margin;
		int maxY = srcY + margin;
		int maxZ = srcZ + margin;
		ChunkCache chunkCache = new ChunkCache(this, minX, minY, minZ, maxX, maxY, maxZ);
		return (new Pathfinder(chunkCache)).createEntityPathTo(entity, targetX, targetY, targetZ, range);
	}
	
	/** Creates a path to XYZ with full navigation options (water/door avoidance, etc.). */
	public PathEntity getEntityPathToXYZ(Entity entity, int targetX, int targetY, int targetZ, float range, boolean avoidWater, boolean avoidBreakDoors, boolean avoidBlocks, boolean canSwim) {
		int srcX = MathHelper.floor_double(entity.posX);
		int srcY = MathHelper.floor_double(entity.posY);
		int srcZ = MathHelper.floor_double(entity.posZ);
		int margin = (int)(range + 8.0F);
		int minX = srcX - margin;
		int minY = srcY - margin;
		int minZ = srcZ - margin;
		int maxX = srcX + margin;
		int maxY = srcY + margin;
		int maxZ = srcZ + margin;
		ChunkCache chunkCache = new ChunkCache(this, minX, minY, minZ, maxX, maxY, maxZ);
		PathEntity path = (new PathfinderRelease(chunkCache, avoidWater, avoidBreakDoors, avoidBlocks, canSwim)).createEntityPathTo(entity, targetX, targetY, targetZ, range);
		return path;
	}

	/** Returns true if the block at (x,y,z) is directly powering the given side. */
	public boolean isBlockProvidingPowerTo(int x, int y, int z, int side) {
		int blockId = this.getBlockId(x, y, z);
		return blockId == 0 ? false : Block.blocksList[blockId].isIndirectlyPoweringTo(this, x, y, z, side);
	}

	/** Returns true if the block at (x,y,z) is receiving direct power from any adjacent block. */
	public boolean isBlockGettingPowered(int x, int y, int z) {
		return this.isBlockProvidingPowerTo(x, y - 1, z, 0) ? true :
			(this.isBlockProvidingPowerTo(x, y + 1, z, 1) ? true : 
				(this.isBlockProvidingPowerTo(x, y, z - 1, 2) ? true :
					(this.isBlockProvidingPowerTo(x, y, z + 1, 3) ? true : 
						(this.isBlockProvidingPowerTo(x - 1, y, z, 4) ? true : 
							this.isBlockProvidingPowerTo(x + 1, y, z, 5)))));
	}

	/** Returns true if the block at (x,y,z) is indirectly powering the given side.
	 *  A solid cube that is directly powered passes power through; otherwise checks the block's own powering state. */
	public boolean isBlockIndirectlyProvidingPowerTo(int x, int y, int z, int side) {
		
		// Block here is providing power if a) is a normal cube getting powered directly, or
		// Block here is powering.
		
		if(this.isBlockNormalCube(x, y, z)) {
			return this.isBlockGettingPowered(x, y, z);
		} else {
			int blockId = this.getBlockId(x, y, z);
			Block block = Block.blocksList[blockId];
			return block == null ? false : block.isPoweringTo(this, x, y, z, side);
		}
	}

	/** Returns true if the block at (x,y,z) is indirectly getting powered from any of the 6 surrounding blocks. */
	public boolean isBlockIndirectlyGettingPowered(int x, int y, int z) {
		// This block is indirectly getting powered if any of the surrounding 6 blocks is indirectly providing power
		return this.isBlockIndirectlyProvidingPowerTo(x, y - 1, z, 0) ? true : 
			(this.isBlockIndirectlyProvidingPowerTo(x, y + 1, z, 1) ? true : 
				(this.isBlockIndirectlyProvidingPowerTo(x, y, z - 1, 2) ? true : 
					(this.isBlockIndirectlyProvidingPowerTo(x, y, z + 1, 3) ? true : 
						(this.isBlockIndirectlyProvidingPowerTo(x - 1, y, z, 4) ? true :
							this.isBlockIndirectlyProvidingPowerTo(x + 1, y, z, 5)))));
	}

	/** Returns the closest player to the given entity within the max distance. */
	public EntityPlayer getClosestPlayerToEntity(Entity entity, double maxDistance) {
		return this.getClosestPlayer(entity.posX, entity.posY, entity.posZ, maxDistance);
	}

	/** Returns the closest player to the given coordinates within the max distance (or any player if maxDistance < 0). */
	public EntityPlayer getClosestPlayer(double x, double y, double z, double maxDistance) {
		double bestDistanceSq = -1.0D;
		EntityPlayer closestPlayer = null;

		for(int i = 0; i < this.playerEntities.size(); ++i) {
			EntityPlayer candidate = (EntityPlayer)this.playerEntities.get(i);
			double distanceSq = candidate.getDistanceSq(x, y, z);
			if((maxDistance < 0.0D || distanceSq < maxDistance * maxDistance) && (bestDistanceSq == -1.0D || distanceSq < bestDistanceSq)) {
				bestDistanceSq = distanceSq;
				closestPlayer = candidate;
			}
		}

		return closestPlayer;
	}
	
	/** Returns all players within the given range of the entity. */
	public List<EntityPlayer> getPlayersInRangeFromEntity(Entity entity, double range) {
		return this.getPlayersInRangeFrom(entity.posX, entity.posY, entity.posZ, range);
	}
	
	/** Returns all players within the given range of the coordinates. */
	public List<EntityPlayer> getPlayersInRangeFrom(double x, double y, double z, double range) {
		List<EntityPlayer> playersInRange = new ArrayList<EntityPlayer>();
		double rangeSq = range * range;
		
		Iterator<EntityPlayer> iterator = this.playerEntities.iterator();
		while(iterator.hasNext()) {
			EntityPlayer entityPlayer = iterator.next();
			double distanceSq = entityPlayer.getDistanceSq(x, y, z);
			if(distanceSq < rangeSq) {
				playersInRange.add(entityPlayer);
			}
		}
		
		return playersInRange;
	}
	
	/** Returns the closest player who is under a roof (cannot see the sky) within the range. */
	public EntityPlayer getClosestPlayerUnderRoof(double x, double y, double z, double range) {
		double rangeSq = range * range;
		double minDistanceSq = rangeSq + 1;
		EntityPlayer closestPlayer = null;
		
		Iterator<EntityPlayer> iterator = this.playerEntities.iterator();
		while(iterator.hasNext()) {
			EntityPlayer entityPlayer = iterator.next();
			double distanceSq = entityPlayer.getDistanceSq(x, y, z);
			if(distanceSq < rangeSq) {
				if(!this.canBlockSeeTheSky((int)entityPlayer.posX, (int)entityPlayer.posY, (int)entityPlayer.posZ) && minDistanceSq > distanceSq) {
					closestPlayer = entityPlayer;
					minDistanceSq = distanceSq;
				}
			}
		}
		
		return closestPlayer;
	}
	
	/** Returns the closest player horizontally (ignoring Y difference) to the given position. */
	public EntityPlayer getClosestPlayerHorizontal(double x, double z, double maxDistance) {
		double bestDistanceSq = -1.0D;
		EntityPlayer closestPlayer = null;

		for(int i = 0; i < this.playerEntities.size(); ++i) {
			EntityPlayer candidate = (EntityPlayer)this.playerEntities.get(i);
			double distanceSq = candidate.getDistanceSq(x, candidate.posY, z);
			if((maxDistance < 0.0D || distanceSq < maxDistance * maxDistance) && (bestDistanceSq == -1.0D || distanceSq < bestDistanceSq)) {
				bestDistanceSq = distanceSq;
				closestPlayer = candidate;
			}
		}

		return closestPlayer;
	}

	/** Returns the player with the given username, or null if not found. */
	public EntityPlayer getPlayerEntityByName(String username) {
		for(int i = 0; i < this.playerEntities.size(); ++i) {
			if(username.equals(((EntityPlayer)this.playerEntities.get(i)).username)) {
				return (EntityPlayer)this.playerEntities.get(i);
			}
		}

		return null;
	}
	
	/** Serializes block data for the region (x, y, z) with the given dimensions into a byte array. */
	public byte[] getChunkData(int x, int y, int z, int xSize, int ySize, int zSize) {
		// Build a flat byte array of 3 bytes per block (blockID | metadata nibbles | light nibbles)
		byte[] data = new byte[xSize * ySize * zSize * 3];
		int chunkXFrom = x >> 4;
		int chunkZFrom = z >> 4;
		int chunkXTo = x + xSize - 1 >> 4;
		int chunkZTo = z + zSize - 1 >> 4;
		int offset = 0;
		int yStart = y;
		int yEnd = y + ySize;
		if(y < 0) {
			yStart = 0;
		}

		if(yEnd > Chunk.SECTION_HEIGHT) {
			yEnd = Chunk.SECTION_HEIGHT;
		}

		for(int cx = chunkXFrom; cx <= chunkXTo; ++cx) {
			int localX1 = x - cx * 16;
			int localX2 = x + xSize - cx * 16;
			if(localX1 < 0) {
				localX1 = 0;
			}

			if(localX2 > 16) {
				localX2 = 16;
			}

			for(int cz = chunkZFrom; cz <= chunkZTo; ++cz) {
				int localZ1 = z - cz * 16;
				int localZ2 = z + zSize - cz * 16;
				if(localZ1 < 0) {
					localZ1 = 0;
				}

				if(localZ2 > 16) {
					localZ2 = 16;
				}

				offset = this.getChunkFromChunkCoords(cx, cz).getChunkData(data, localX1, yStart, localZ1, localX2, yEnd, localZ2, offset);
			}
		}

		return data;
	}	

	// Unpacks & copies all data (blockIDs, metas, lighting) in a chunk or a portion of it.
		public void setChunkData(int x0, int y0, int z0, int xSize, int ySize, int zSize, byte[] rawData) {
			int xChunkFrom = x0 >> 4;
			int zChunkFrom = z0 >> 4;

			int xChunkTo = x0 + xSize - 1 >> 4;
			int zChunkTo = z0 + zSize - 1 >> 4;

			int dataOffset = 0;
			
			int y1 = y0;
			int y2 = y0 + ySize;

			if(y0 < 0) {
				y1 = 0;
			}

			if(y2 > Chunk.SECTION_HEIGHT) {
				y2 = Chunk.SECTION_HEIGHT;
			}

			for(int xChunk = xChunkFrom; xChunk <= xChunkTo; ++xChunk) {
				int x1 = x0 - xChunk * 16;
				int x2 = x0 + xSize - xChunk * 16;

				if(x1 < 0) {
					x1 = 0;
				}

				if(x2 > 16) {
					x2 = 16;
				}

				for(int zChunk = zChunkFrom; zChunk <= zChunkTo; ++zChunk) {
					int z1 = z0 - zChunk * 16;
					int z2 = z0 + zSize - zChunk * 16;

					if(z1 < 0) {
						z1 = 0;
					}

					if(z2 > 16) {
						z2 = 16;
					}

					dataOffset = this.getChunkFromChunkCoords(xChunk, zChunk).setChunkData(rawData, x1, y1, z1, x2, y2, z2, dataOffset);
					this.markBlocksDirty(xChunk * 16 + x1, y1, zChunk * 16 + z1, xChunk * 16 + x2, y2, zChunk * 16 + z2);
				}
			}

		}

	/** Sends the packet to notify the server of a quit/disconnect. No-op in base World. */
	public void sendQuittingDisconnectingPacket() {
	}

	/** Verifies the session lock to prevent concurrent world modifications. */
	public void checkSessionLock() {
		this.saveHandler.checkSessionLock();
	}

	/** Sets the world time directly. */
	public void setWorldTime(long worldTime) {
		this.worldInfo.setWorldTime(worldTime);
	}

	/** Shifts every pending block tick by the world-clock delta, then applies the new world time. */
	public void shiftScheduledTimes(long newWorldTime) {
		this.blockTickScheduler.shiftScheduledTimes(newWorldTime);
	}

	/** Returns the world's random seed. */
	public long getRandomSeed() {
		return this.worldInfo.getRandomSeed();
	}

	/** Returns the current world time in ticks. */
	public long getWorldTime() {
		return this.worldInfo.getWorldTime();
	}

	/** Returns the world spawn point as a new ChunkCoordinates. */
	public ChunkCoordinates getSpawnPoint() {
		return new ChunkCoordinates(this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());
	}

	/** Sets the world spawn point. */
	public void setSpawnPoint(ChunkCoordinates spawnPoint) {
		this.worldInfo.setSpawn(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ);
	}

	/** Ensures surrounding chunks are loaded, then adds the entity to the world. */
	public void joinEntityInSurroundings(Entity entity) {
		int chunkX = MathHelper.floor_double(entity.posX / 16.0D);
		int chunkZ = MathHelper.floor_double(entity.posZ / 16.0D);
		byte radius = 2;

		for(int cx = chunkX - radius; cx <= chunkX + radius; ++cx) {
			for(int cz = chunkZ - radius; cz <= chunkZ + radius; ++cz) {
				this.getChunkFromChunkCoords(cx, cz);
			}
		}

		this.entityManager.addIfAbsent(entity);

	}

	/** Returns true if the player can mine the block at the given position. Always true in base World. */
	public boolean canMineBlock(EntityPlayer player, int x, int y, int z) {
		return true;
	}

	/** Sends the entity's state byte to all nearby players. No-op in base World (overridden in WorldClient). */
	public void setEntityState(Entity entity, byte state) {
	}

	/** Detaches ride links for stale mounts and removes dead entities from loaded lists. */
	public void updateEntityList() {
		this.entityManager.sweepUnloaded();

		int index;
		Entity entity;
		int chunkX;
		int chunkZ;
		for(index = 0; index < this.getLoadedEntityList().size(); ++index) {
			entity = (Entity)this.getLoadedEntityList().get(index);
			if(entity.ridingEntity != null) {
				if(!entity.ridingEntity.isDead && entity.ridingEntity.riddenByEntity == entity) {
					continue;
				}

				entity.ridingEntity.riddenByEntity = null;
				entity.ridingEntity = null;
			}

			if(entity.isDead) {
				chunkX = entity.chunkCoordX;
				chunkZ = entity.chunkCoordZ;
				if(entity.addedToChunk && this.chunkExists(chunkX, chunkZ)) {
					this.getChunkFromChunkCoords(chunkX, chunkZ).removeEntity(entity);
				}

				this.entityManager.removeEntityFromWorldList(entity, index--);
			}
		}

	}

	/** Returns the chunk provider for this world. */
	public IChunkProvider getIChunkProvider() {
		return this.chunkProvider;
	}

	/** Plays a note block sound at the given position with the specified instrument and note. */
	public void playNoteAt(int x, int y, int z, int instrument, int note) {
		int blockId = this.getBlockId(x, y, z);
		if(blockId > 0) {
			Block.blocksList[blockId].playBlock(this, x, y, z, instrument, note);
		}

	}

	/** Returns the save handler used for this world. */
	public ISaveHandler getWorldFile() {
		return this.saveHandler;
	}

	/** Returns the world info (time, seed, spawn, weather, etc.). */
	public WorldInfo getWorldInfo() {
		return this.worldInfo;
	}

	/** Updates the allPlayersSleeping flag: true only if every non-empty player list is sleeping. */
	public void updateAllPlayersSleepingFlag() {
		this.allPlayersSleeping = !this.playerEntities.isEmpty();
		Iterator<EntityPlayer> iterator = this.playerEntities.iterator();

		while(iterator.hasNext()) {
			EntityPlayer player = (EntityPlayer)iterator.next();
			if(!player.isPlayerSleeping()) {
				this.allPlayersSleeping = false;
				break;
			}
		}

	}

	/** Wakes up all sleeping players and clears weather. */
	protected void wakeUpAllPlayers() {
		this.allPlayersSleeping = false;
		Iterator<EntityPlayer> iterator = this.playerEntities.iterator();

		while(iterator.hasNext()) {
			EntityPlayer player = (EntityPlayer)iterator.next();
			if(player.isPlayerSleeping()) {
				player.wakeUpPlayer(false, false, true);
			}
		}

		this.clearWeather();
	}

	/** Returns true if every player is fully asleep and this is the server side. */
	public boolean isAllPlayersFullyAsleep() {
		if(this.allPlayersSleeping && !this.isRemote) {
			Iterator<EntityPlayer> iterator = this.playerEntities.iterator();

			EntityPlayer player;
			do {
				if(!iterator.hasNext()) {
					return true;
				}

				player = (EntityPlayer)iterator.next();
			} while(player.isPlayerFullyAsleep());

			return false;
		} else {
			return false;
		}
	}

	/** Returns the thunder strength interpolated for the given partial tick. */
	public float getWeightedThunderStrength(float tick) {
		// I want thunders without rain!
		return (this.prevThunderingStrength + (this.thunderingStrength - this.prevThunderingStrength) * tick) /* * this.getRainStrength(tick)*/ ;
	}

	/** Returns the rain strength interpolated for the given partial tick. */
	public float getRainStrength(float tick) {
		return this.prevRainingStrength + (this.rainingStrength - this.prevRainingStrength) * tick;
	}
	
	/** Returns the snow strength interpolated for the given partial tick. */
	public float getSnowStrength(float tick) {
		return this.prevSnowingStrength + (this.snowingStrength - this.prevSnowingStrength) * tick;
	}

	/** Sets the rain strength (used for transitions). */
	public void setRainStrength(float strength) {
		this.prevRainingStrength = strength;
		this.rainingStrength = strength;
	}
	
	/** Sets the snow strength (used for transitions). */
	public void setSnowingStrength(float strength) {
		this.prevSnowingStrength = strength;
		this.snowingStrength = strength;
	}
	
	/** Sets the thunder strength (used for transitions). */
	public void setThunderingStrength(float strength) {
		this.prevThunderingStrength = strength;
		this.thunderingStrength = strength;
	}

	/** Returns true if thunder is currently above the 90% intensity threshold. */
	public boolean thundering() {
		return (double)this.getWeightedThunderStrength(1.0F) > 0.9D;
	}

	/** Returns true if rain is currently above the 20% intensity threshold. */
	public boolean raining() {
		return (double)this.getRainStrength(1.0F) > 0.2D;
	}
	
	/** Returns true if snow is currently above the 20% intensity threshold. */
	public boolean snowing() {
		return (double)this.getSnowStrength(1.0F) > 0.2D;
	}


	/** Returns true if the block at (x,y,z) can be rained on: not snow biome, sees sky, not overshadowed. */
	public boolean canBlockBeRainedOn(int x, int y, int z) {
		if(!this.raining()) {
			return false;
		} else if(!this.canBlockSeeTheSky(x, y, z)) {
			return false;
		} else if(this.findTopSolidBlockUsingBlockMaterial(x, z) > y) {
			return false;
		} else {
			BiomeGenBase biome = this.getWorldChunkManager().getBiomeGenAt(x, z);
			return biome.getEnableSnow() ? false : biome.canSpawnLightningBolt();
		}
	}
	
	/** Like canBlockBeRainedOn but ignores rain state — only checks sky visibility and biome. */
	public boolean canBlockBeRainedOnForBolts(int x, int y, int z) {
		if(!this.canBlockSeeTheSky(x, y, z)) {
			return false;
		} else if(this.findTopSolidBlockUsingBlockMaterial(x, z) > y) {
			return false;
		} else {
			BiomeGenBase biome = this.getWorldChunkManager().getBiomeGenAt(x, z);
			return biome.getEnableSnow() ? false : biome.canSpawnLightningBolt();
		}
	}

	/** Stores map data under the given key for persistent world data. */
	public void setItemData(String key, MapDataBase mapData) {
		this.mapStorage.setData(key, mapData);
	}

	/** Loads persistent map data of the given class type under the specified key. */
	public MapDataBase loadItemData(Class<?> dataClass, String key) {
		return this.mapStorage.loadData(dataClass, key);
	}

	/** Returns a unique numeric data ID for the given key (used for map IDs, etc.). */
	public int getUniqueDataId(String key) {
		return this.mapStorage.getUniqueDataId(key);
	}

	/** Plays an auxiliary sound effect (particles, break/place sounds, etc.) at the given block. */
	public void playAuxSFX(int effectID, int x, int y, int z, int metadata) {
		this.playAuxSFXAtEntity((EntityPlayer)null, effectID, x, y, z, metadata);
	}

	/** Plays an auxiliary sound effect attributed to the given player. */
	public void playAuxSFXAtEntity(EntityPlayer player, int effectID, int x, int y, int z, int metadata) {
		for(int i = 0; i < this.worldAccesses.size(); ++i) {
			((IWorldAccess)this.worldAccesses.get(i)).playAuxSFX(player, effectID, x, y, z, metadata);
		}

	}

	/** Returns the biome at the given world coordinates. */
	public BiomeGenBase getBiomeGenAt(int x, int z) {
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).getBiomeGenAt(x & 15, z & 15);
	}

	/** Returns the temperature at the given world coordinates (chunk-cached where loaded). */
	public float getTemperatureAt(int x, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		return this.chunkExists(chunkX, chunkZ)
				? this.getChunkFromChunkCoords(chunkX, chunkZ).getTemperatureAt(x & 15, z & 15)
				: (float)this.getWorldChunkManager().getTemperatureAndHumidityAt(x, z)[0];
	}

	/** Returns the humidity at the given world coordinates (chunk-cached where loaded). */
	public float getHumidityAt(int x, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		return this.chunkExists(chunkX, chunkZ)
				? this.getChunkFromChunkCoords(chunkX, chunkZ).getHumidityAt(x & 15, z & 15)
				: (float)this.getWorldChunkManager().getTemperatureAndHumidityAt(x, z)[1];
	}
	
	/** Returns the world access at the given index. */
	public IWorldAccess getWorldAccess(int index) {
		return this.worldAccesses.get(index);
	}
	
	/** Commands the world to start snowing by resetting the snow timer to 0. */
	public void commandSetSnow() {
		this.worldInfo.setSnowingTime(0);
	}
	
	/** Commands the world to start raining by resetting the rain timer to 0. */
	public void commandSetRain() {
		this.worldInfo.setRainTime(0);
	}
	
	/** Commands the world to start thundering by resetting the thunder timer to 0. */
	public void commandSetThunder() {
		this.worldInfo.setThunderTime(0);
	}

	/** Returns true if the block at (x,y,z) is covered by leaves within 16 blocks above. */
	public boolean isUnderLeaves(int x, int y, int z) {
		for(int i = 0; i < 16 && y < Chunk.SECTION_HEIGHT; i ++) {
			if(this.getBlockId(x, y, z) == Block.leaves.blockID && this.getBlockMetadata(x, y, z) == 7) return true;
			y ++;
		}
		return false;
	}

	/** Returns the full block state (ID + metadata) at the given coordinates. */
	public BlockState getBlockStateAt(int x, int y, int z) {
		return new BlockState(this.getBlockId(x, y, z), this.getBlockMetadata(x, y, z), x, y, z);
	}

	/** Returns the Block instance at the given coordinates. */
	public Block getBlock(int x, int y, int z) {
		return Block.blocksList[this.getBlockId(x, y, z)];
	}

	/** Sets the block and metadata at the given coordinates from a BlockState. */
	public void setBlockAndMetadata(int x, int y, int z, BlockState blockState) {
		this.setBlockAndMetadata(x, y, z, blockState.getBlock().blockID, blockState.getMetadata());
	}
	
	// Future: override those in WorldServer with the right thing?
	/** Finds players matching the given name filter, optionally within a range of a spawn point. Sorted by distance if coords provided. */
	public List<EntityPlayer> findPlayers(ChunkCoordinates coords, int maxRange, String name) {
			if(this.playerEntities.isEmpty()) {
				return null;
			} else {
				ArrayList<EntityPlayer> result = new ArrayList<EntityPlayer>();
				int maxRangeSq = maxRange * maxRange;
				
				for(int i = 0; i < this.playerEntities.size(); ++i) {
					EntityPlayer curPlayer = (EntityPlayer)this.playerEntities.get(i);
					
					boolean negMatch;
					if(name != null) {
						negMatch = name.startsWith("!");
						if(negMatch) {
							name = name.substring(1);
						}

						if(negMatch == name.equalsIgnoreCase(curPlayer.username)) {
							continue;
						}
					}

					if(coords != null && maxRange > 0) {
						float distanceSq = coords.getDistanceSquaredToChunkCoordinates(curPlayer.getPlayerCoordinates());
						if(distanceSq > (float)maxRangeSq) {
							continue;
						}
					}

					result.add(curPlayer);
				}

				if(coords != null) {
					Collections.sort(result, new PlayerPositionComparator(coords));
				}

				return result;
			}
		}

	/** Returns the player with the exact username (case-sensitive), or null if not found. */
	public EntityPlayer getPlayerForUsername(String username) {
		if(username == null) return null;
		for(int i = 0; i < this.playerEntities.size(); i ++) {
			EntityPlayer player = playerEntities.get(i);
			if(username.equals(player.username)) return player;
		}
		return null;
	}

	/** Returns an array of all online player usernames. */
	public String[] getAllUsernames() {
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i < this.playerEntities.size(); i ++) {
			list.add(this.playerEntities.get(i).username);
		}
		return (String[]) list.toArray();
	}

	/** Returns the world height in blocks. */
	public int getWorldHeight() {
		return 128;
	}
}

