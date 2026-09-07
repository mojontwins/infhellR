package net.minecraft.game.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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

public class World implements IBlockAccess {
	private static final int blocksToTickPerFrame = 80;	

	/** Chebyshev chunk radius around each player within which entities receive full updates (AI, movement, collisions). */
	public int entitySimulationRadiusChunks = 8;
	
	public boolean scheduledUpdatesAreImmediate;
	
	public List<Entity> loadedEntityList;
	private List<Entity> unloadedEntityList;
	private TreeSet<NextTickListEntry> scheduledTickTreeSet;
	private Set<NextTickListEntry> scheduledTickSet;
	public List<TileEntity> loadedTileEntityList;
	private List<TileEntity> entityRemoval;
	public List<EntityPlayer> playerEntities;
	public List<Entity> weatherEffects;
	private long cloudColour;
	public int skylightSubtracted;
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
	private ArrayList<AxisAlignedBB> collidingBoundingBoxes;
	private boolean scanningTileEntities;
	private boolean spawnHostileMobs;
	private boolean spawnPeacefulMobs;
	private Set<ChunkCoordIntPair> positionsToUpdate;
	private int soundCounter;
	private List<Entity> entitiesWithinAABBExcludingEntity;
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

	public WorldChunkManager getWorldChunkManager() {
		return this.worldProvider.worldChunkMgr;
	}

	public World(ISaveHandler iSaveHandler1, String string2, WorldProvider worldProvider3, WorldSettings par4WorldSettings) {
		this.scheduledUpdatesAreImmediate = false;
		this.loadedEntityList = new ArrayList<Entity>();
		this.unloadedEntityList = new ArrayList<Entity>();
		this.scheduledTickTreeSet = new TreeSet<NextTickListEntry>();
		this.scheduledTickSet = new HashSet<NextTickListEntry>();
		this.loadedTileEntityList = new ArrayList<TileEntity>();
		this.entityRemoval = new ArrayList<TileEntity>();
		this.playerEntities = new ArrayList<EntityPlayer>();
		this.weatherEffects = new ArrayList<Entity>();
		this.cloudColour = 16777215L;
		this.skylightSubtracted = 0;
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
		this.collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
		this.spawnHostileMobs = true;
		this.spawnPeacefulMobs = true;
		this.positionsToUpdate = new HashSet<ChunkCoordIntPair>();
		this.soundCounter = this.rand.nextInt(12000);
		this.entitiesWithinAABBExcludingEntity = new ArrayList<Entity>();
		this.isRemote = false;
		this.saveHandler = iSaveHandler1;
		this.worldInfo = new WorldInfo(par4WorldSettings, string2);
		this.worldProvider = worldProvider3;
		this.mapStorage = new MapStorage(iSaveHandler1);
		worldProvider3.registerWorld(this);
		this.chunkProvider = this.getChunkProvider();
		this.calculateInitialSkylight();
		this.calculateInitialWeather();
		
		WorldEdit.init();
	}

	public World(World world1, WorldProvider worldProvider2) {
		this.scheduledUpdatesAreImmediate = false;
		this.loadedEntityList = new ArrayList<Entity>();
		this.unloadedEntityList = new ArrayList<Entity>();
		this.scheduledTickTreeSet = new TreeSet<NextTickListEntry>();
		this.scheduledTickSet = new HashSet<NextTickListEntry>();
		this.loadedTileEntityList = new ArrayList<TileEntity>();
		this.entityRemoval = new ArrayList<TileEntity>();
		this.playerEntities = new ArrayList<EntityPlayer>();
		this.weatherEffects = new ArrayList<Entity>();
		this.cloudColour = 16777215L;
		this.skylightSubtracted = 0;
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
		this.collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
		this.spawnHostileMobs = true;
		this.spawnPeacefulMobs = true;
		this.positionsToUpdate = new HashSet<ChunkCoordIntPair>();
		this.soundCounter = this.rand.nextInt(12000);
		this.entitiesWithinAABBExcludingEntity = new ArrayList<Entity>();
		this.isRemote = false;
		this.lockTimestamp = world1.lockTimestamp;
		this.saveHandler = world1.saveHandler;
		this.worldInfo = new WorldInfo(world1.worldInfo);
		this.mapStorage = new MapStorage(this.saveHandler);
		this.worldProvider = worldProvider2;
		worldProvider2.registerWorld(this);
		this.chunkProvider = this.getChunkProvider();
		
		this.badMoonDecide = false;
		this.nextMoonBad = false;
		
		Seasons.dayOfTheYear = this.rand.nextInt(4 * Seasons.SEASON_DURATION);
		Seasons.updateSeasonCounters();

		this.calculateInitialSkylight();
		this.calculateInitialWeather();
		
		WorldEdit.init();
	}

	public World(ISaveHandler iSaveHandler1, String string2, WorldSettings par3WorldSettings) {
		this(iSaveHandler1, string2, par3WorldSettings, (WorldProvider)null);
	}

	public World(ISaveHandler iSaveHandler1, String string2, WorldSettings par3WorldSettings, WorldProvider worldProvider5) {
		this.scheduledUpdatesAreImmediate = false;
		this.loadedEntityList = new ArrayList<Entity>();
		this.unloadedEntityList = new ArrayList<Entity>();
		this.scheduledTickTreeSet = new TreeSet<NextTickListEntry>();
		this.scheduledTickSet = new HashSet<NextTickListEntry>();
		this.loadedTileEntityList = new ArrayList<TileEntity>();
		this.entityRemoval = new ArrayList<TileEntity>();
		this.playerEntities = new ArrayList<EntityPlayer>();
		this.weatherEffects = new ArrayList<Entity>();
		this.cloudColour = 16777215L;
		this.skylightSubtracted = 0;
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
		this.collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
		this.spawnHostileMobs = true;
		this.spawnPeacefulMobs = true;
		this.positionsToUpdate = new HashSet<ChunkCoordIntPair>();
		this.soundCounter = this.rand.nextInt(12000);
		this.entitiesWithinAABBExcludingEntity = new ArrayList<Entity>();
		this.isRemote = false;
		this.saveHandler = iSaveHandler1;
		this.mapStorage = new MapStorage(iSaveHandler1);
		
		Seasons.dayOfTheYear = -1;
		this.worldInfo = iSaveHandler1.loadWorldInfo();
		
		this.badMoonDecide = false;
		this.nextMoonBad = false;

		boolean z6 = false;
		if(this.worldInfo == null) {
			this.worldInfo = new WorldInfo(par3WorldSettings, string2);
			z6 = true;
		} else {
			this.worldInfo.setWorldName(string2);
		}
		
		this.isNewWorld = this.worldInfo == null;
		if(worldProvider5 != null) {
			this.worldProvider = worldProvider5;
		} else if(this.worldInfo != null && this.worldInfo.getDimension() == -1) {
			this.worldProvider = WorldProvider.getProviderForDimension(-1);
		} else if(this.worldInfo != null && (this.worldInfo.getDimension() == 1 || this.worldInfo.getTerrainType() == WorldType.SKY)) {
			this.worldProvider = WorldProvider.getProviderForDimension(1);
		} else {
			this.worldProvider = WorldProvider.getProviderForDimension(0);
		}

		this.worldProvider.registerWorld(this);
		this.chunkProvider = this.getChunkProvider();
		if(z6) {
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

	protected IChunkProvider getChunkProvider() {
		IChunkLoader iChunkLoader1 = this.saveHandler.getChunkLoader(this.worldProvider);
		return new ChunkProvider(this, iChunkLoader1, this.worldProvider.getChunkProvider());
	}

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
			if (this.worldInfo.getCityChance() <= 0.1F) {
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

	public boolean isCityCoordinate(int x, int z) {
		Chunk chunk = getChunkFromBlockCoords(x, z);
		return chunk.hasBuilding || chunk.hasRoad || chunk.hasUnderwaterRuin;
	}
	
	public void setSpawnLocation() {
		if(this.worldInfo.getSpawnY() <= 0) {
			this.worldInfo.setSpawnY(64);
		}

		int i1 = this.worldInfo.getSpawnX();

		int i2;
		for(i2 = this.worldInfo.getSpawnZ(); this.getFirstUncoveredBlock(i1, i2) == 0; i2 += this.rand.nextInt(8) - this.rand.nextInt(8)) {
			i1 += this.rand.nextInt(8) - this.rand.nextInt(8);
		}

		this.worldInfo.setSpawnX(i1);
		this.worldInfo.setSpawnZ(i2);
	}

	public int getFirstUncoveredBlock(int i1, int i2) {
		int i3;
		for(i3 = 63; !this.isAirBlock(i1, i3 + 1, i2); ++i3) {
		}

		return this.getBlockId(i1, i3, i2);
	}

	public void emptyMethod1() {
	}

	public void spawnPlayerWithLoadedChunks(EntityPlayer entityPlayer1) {
		try {
			NBTTagCompound nBTTagCompound2 = this.worldInfo.getPlayerNBTTagCompound();
			if(nBTTagCompound2 != null) {
				entityPlayer1.readFromNBT(nBTTagCompound2);
				this.worldInfo.setPlayerNBTTagCompound((NBTTagCompound)null);
			}

			if(this.chunkProvider instanceof ChunkProviderLoadOrGenerate) {
				ChunkProviderLoadOrGenerate chunkProviderLoadOrGenerate3 = (ChunkProviderLoadOrGenerate)this.chunkProvider;
				int i4 = MathHelper.floor_float((float)((int)entityPlayer1.posX)) >> 4;
				int i5 = MathHelper.floor_float((float)((int)entityPlayer1.posZ)) >> 4;
				chunkProviderLoadOrGenerate3.setCurrentChunkOver(i4, i5);
			}

			this.spawnEntityInWorld(entityPlayer1);
		} catch (Exception exception6) {
			exception6.printStackTrace();
		}

	}

	public void saveWorld(boolean z1, IProgressUpdate iProgressUpdate2) {
		if(this.chunkProvider.canSave()) {
			if(iProgressUpdate2 != null) {
				iProgressUpdate2.displaySavingString("Saving level");
			}

			this.saveLevel();
			if(iProgressUpdate2 != null) {
				iProgressUpdate2.displayLoadingString("Saving chunks");
			}

			this.chunkProvider.saveChunks(z1, iProgressUpdate2);
		}
	}

	private void saveLevel() {
		this.checkSessionLock();
		this.saveHandler.saveWorldInfoAndPlayer(this.worldInfo, this.playerEntities);
		this.mapStorage.saveAllData();
	}

	public boolean quickSaveWorld(int i1) {
		if(!this.chunkProvider.canSave()) {
			return true;
		} else {
			if(i1 == 0) {
				this.saveLevel();
			}

			return this.chunkProvider.saveChunks(false, (IProgressUpdate)null);
		}
	}

	public int getBlockId(int x, int y, int z) {
		if (y < 0 || y >= 128) return 0;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockID(x & 15, y, z & 15);
	}
	
	public int getBlockId(BlockPos blockPos) {
		return this.getBlockId(blockPos.x, blockPos.y, blockPos.z);
	}

	public boolean isAirBlock(int i1, int i2, int i3) {
		return this.getBlockId(i1, i2, i3) == 0;
	}
	
	public boolean isAirBlock(BlockPos blockPos) {
		return this.isAirBlock(blockPos.x, blockPos.y, blockPos.z);
	}
	
	public boolean isWaterBlock(int i1, int i2, int i3) {
		Block b = Block.blocksList[this.getBlockId(i1, i2, i3)];
		return (b != null && b instanceof BlockFluid);
	}

	public boolean isWaterBlock(BlockPos blockPos) {
		return this.isWaterBlock(blockPos.x, blockPos.y, blockPos.z);
	}
	
	public boolean blockExists(int i1, int i2, int i3) {
		return i2 >= 0 && i2 < 128 ? this.chunkExists(i1 >> 4, i3 >> 4) : false;
	}
	
	public boolean blockExists(BlockPos blockPos) {
		return this.blockExists(blockPos.x, blockPos.y, blockPos.z);
	}

	public boolean doChunksNearChunkExist(int i1, int i2, int i3, int i4) {
		return this.checkChunksExist(i1 - i4, i2 - i4, i3 - i4, i1 + i4, i2 + i4, i3 + i4);
	}

	public boolean checkChunksExist(int i1, int i2, int i3, int i4, int i5, int i6) {
		if(i5 >= 0 && i2 < 128) {
			i1 >>= 4;
			i2 >>= 4;
			i3 >>= 4;
			i4 >>= 4;
			i5 >>= 4;
			i6 >>= 4;

			for(int i7 = i1; i7 <= i4; ++i7) {
				for(int i8 = i3; i8 <= i6; ++i8) {
					if(!this.chunkExists(i7, i8)) {
						return false;
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	public boolean chunkExists(int i1, int i2) {
		return this.chunkProvider.chunkExists(i1, i2);
	}

	public Chunk getChunkFromBlockCoords(int i1, int i2) {
		return this.getChunkFromChunkCoords(i1 >> 4, i2 >> 4);
	}

	public Chunk getChunkFromChunkCoords(int i1, int i2) {
		return this.chunkProvider.provideChunk(i1, i2);
	}

	public boolean setBlockAndMetadata(int x, int y, int z, int id, int metadata) {
		if(y < 0 || y >= 128) return false;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockIDWithMetadata(x & 15, y, z & 15, id, metadata);
	}

	public boolean setBlockAndMetadata(BlockPos blockPos, int id, int metadata) {
		return this.setBlockAndMetadata(blockPos.x, blockPos.y, blockPos.z, id, metadata);
	}
	
	public boolean setBlock(int x, int y, int z, int id) {
		if(y < 0 || y >= 128) return false;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockID(x & 15, y, z & 15, id);
	}
	
	public boolean setBlock(BlockPos blockPos, int id, int metadata) {
		return this.setBlock(blockPos.x, blockPos.y, blockPos.z, id);
	}
	
	public boolean setBlockAndMetadataColumn(int x, int y, int z, int[] id) {
		if(y < 0) return false;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockIDAndMetadataColumn(x & 15, y, z & 15, id);
	}
	
	public Material getBlockMaterial(int x, int y, int z) {
		Block block = Block.blocksList[this.getBlockId(x, y, z)];
		int metadata = this.getBlockMetadata(x, y, z);
		if (block == null) return Material.air;
		Material material = block.getBlockMaterialBasedOnmetaData(metadata);
		
		return material;
	}
	
	public Material getBlockMaterial(BlockPos blockPos) {
		return this.getBlockMaterial(blockPos.x, blockPos.y, blockPos.z);
	}

	public int getBlockMetadata(int x, int y, int z) {
		if (y < 0 || y >= 128) return 0;
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).getBlockMetadata(x & 15, y, z & 15);
	}

	public int getBlockMetadata(BlockPos blockPos) {
		return this.getBlockMetadata(blockPos.x, blockPos.y, blockPos.z);
	}
	
	public void setBlockMetadataWithNotify(int i1, int i2, int i3, int i4) {
		if(this.setBlockMetadata(i1, i2, i3, i4)) {
			int i5 = this.getBlockId(i1, i2, i3);
			if(Block.requiresSelfNotify[i5 & 255]) {
				this.notifyBlockChange(i1, i2, i3, i5);
			} else {
				this.notifyBlocksOfNeighborChange(i1, i2, i3, i5);
			}
		}

	}
	
	public void setBlockMetadataWithNotify(BlockPos blockPos, int meta) {
		this.setBlockMetadataWithNotify(blockPos.x, blockPos.y, blockPos.z, meta);
	}

	public boolean setBlockMetadata(int x, int y, int z, int metadata) {
		if (y < 0 || y >= 128) return false;
		this.getChunkFromChunkCoords(x >> 4, z >> 4).setBlockMetadata(x & 15, y, z & 15, metadata);
		return true;
	}
	
	public boolean setBlockMetadata(BlockPos blockPos, int metadata) {
		return this.setBlockMetadata(blockPos.x, blockPos.y, blockPos.z, metadata);
	}

	public boolean setBlockWithNotify(int x, int y, int z, int id) {
		if(this.setBlock(x, y, z, id)) {
			this.notifyBlockChange(x, y, z, id);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setBlockWithNotify(BlockPos blockPos, int i) {
		return this.setBlockWithNotify(blockPos.x, blockPos.y, blockPos.z, i);
	}

	public boolean setBlockAndMetadataWithNotify(int x, int y, int z, int id, int metadata) {
		if(this.setBlockAndMetadata(x, y, z, id, metadata)) {
			this.notifyBlockChange(x, y, z, id);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean setBlockAndMetadataWithNotify(BlockPos blockPos, int id, int metadata) {
		return this.setBlockAndMetadataWithNotify(blockPos.x, blockPos.y, blockPos.z, id, metadata);
	}

	public void markBlockNeedsUpdate(int i1, int i2, int i3) {
		for(int i4 = 0; i4 < this.worldAccesses.size(); ++i4) {
			((IWorldAccess)this.worldAccesses.get(i4)).markBlockNeedsUpdate(i1, i2, i3);
		}

	}

	protected void notifyBlockChange(int i1, int i2, int i3, int i4) {
		this.markBlockNeedsUpdate(i1, i2, i3);
		this.notifyBlocksOfNeighborChange(i1, i2, i3, i4);
	}

	public void markBlocksDirtyVertical(int i1, int i2, int i3, int i4) {
		if(i3 > i4) {
			int i5 = i4;
			i4 = i3;
			i3 = i5;
		}

		this.markBlocksDirty(i1, i3, i2, i1, i4, i2);
	}

	public void markBlockAsNeedsUpdate(int i1, int i2, int i3) {
		for(int i4 = 0; i4 < this.worldAccesses.size(); ++i4) {
			((IWorldAccess)this.worldAccesses.get(i4)).markBlockRangeNeedsUpdate(i1, i2, i3, i1, i2, i3);
		}

	}

	public void markBlocksDirty(int i1, int i2, int i3, int i4, int i5, int i6) {
		for(int i7 = 0; i7 < this.worldAccesses.size(); ++i7) {
			((IWorldAccess)this.worldAccesses.get(i7)).markBlockRangeNeedsUpdate(i1, i2, i3, i4, i5, i6);
		}

	}

	public void notifyBlocksOfNeighborChange(int i1, int i2, int i3, int i4) {
		this.notifyBlockOfNeighborChange(i1 - 1, i2, i3, i4);
		this.notifyBlockOfNeighborChange(i1 + 1, i2, i3, i4);
		this.notifyBlockOfNeighborChange(i1, i2 - 1, i3, i4);
		this.notifyBlockOfNeighborChange(i1, i2 + 1, i3, i4);
		this.notifyBlockOfNeighborChange(i1, i2, i3 - 1, i4);
		this.notifyBlockOfNeighborChange(i1, i2, i3 + 1, i4);
	}

	public void notifyBlockOfNeighborChange(int i1, int i2, int i3, int i4) {
		if(!this.editingBlocks && !this.isRemote) {
			Block block5 = Block.blocksList[this.getBlockId(i1, i2, i3)];
			if(block5 != null) {
				block5.onNeighborBlockChange(this, i1, i2, i3, i4);
			}

		}
	}

	public boolean canBlockSeeTheSky(int i1, int i2, int i3) {
		return this.getChunkFromChunkCoords(i1 >> 4, i3 >> 4).canBlockSeeTheSky(i1 & 15, i2, i3 & 15);
	}

	public int getFullBlockLightValue(int i1, int i2, int i3) {
		if(i2 < 0) {
			return 0;
		} else {
			if(i2 >= 128) {
				i2 = 127;
			}

			return this.getChunkFromChunkCoords(i1 >> 4, i3 >> 4).getBlockLightValue(i1 & 15, i2, i3 & 15, 0);
		}
	}

	public int getBlockLightValue(int i1, int i2, int i3) {
		return this.getBlockLightValue_do(i1, i2, i3, true);
	}

	public int getBlockLightValue_do(int i1, int i2, int i3, boolean z4) {
		if(z4) {
			int i5 = this.getBlockId(i1, i2, i3);
			if(i5 == Block.stairSingle.blockID || i5 == Block.tilledField.blockID || i5 == Block.stairCompactCobblestone.blockID || i5 == Block.stairCompactPlanks.blockID) {
				int i6 = this.getBlockLightValue_do(i1, i2 + 1, i3, false);
				int i7 = this.getBlockLightValue_do(i1 + 1, i2, i3, false);
				int i8 = this.getBlockLightValue_do(i1 - 1, i2, i3, false);
				int i9 = this.getBlockLightValue_do(i1, i2, i3 + 1, false);
				int i10 = this.getBlockLightValue_do(i1, i2, i3 - 1, false);
				if(i7 > i6) {
					i6 = i7;
				}

				if(i8 > i6) {
					i6 = i8;
				}

				if(i9 > i6) {
					i6 = i9;
				}

				if(i10 > i6) {
					i6 = i10;
				}

				return i6;
			}
		}

		if(i2 < 0) {
			return 0;
		} else {
			if(i2 >= 128) {
				i2 = 127;
			}

			Chunk chunk11 = this.getChunkFromChunkCoords(i1 >> 4, i3 >> 4);
			i1 &= 15;
			i3 &= 15;
			return chunk11.getBlockLightValue(i1, i2, i3, this.skylightSubtracted);
		}
	}

	public boolean canExistingBlockSeeTheSky(int x, int y, int z) {
		if(y < 0) {
			return false;
		} else if(y >= 128) {
			return true;
		} else if(!this.chunkExists(x >> 4, z >> 4)) {
			return false;
		} else {
			Chunk chunk4 = this.getChunkFromChunkCoords(x >> 4, z >> 4);
			x &= 15;
			z &= 15;
			return chunk4.canBlockSeeTheSky(x, y, z);
		}
	}

	public int getHeightValue(int blockX, int blockZ) {
		if(!this.chunkExists(blockX >> 4, blockZ >> 4)) {
			return 0;
		} else {
			Chunk chunk3 = this.getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
			return chunk3.getHeightValue(blockX & 15, blockZ & 15);
		}
	}
	
	public int getLandSurfaceHeightValue(int blockX, int blockZ) {
		Chunk chunk3 = null;
		if(this.chunkExists(blockX >> 4, blockZ >> 4)) { 
			chunk3 = this.getChunkFromChunkCoords(blockX >> 4, blockZ >> 4);
		} else { 
			chunk3 = this.chunkProvider.justGenerateForHeight(blockX >> 4, blockZ >> 4);
		}
		return chunk3.getLandSurfaceHeightValue(blockX & 15, blockZ & 15);
	}
	
	public boolean isOceanChunk(int chunkX, int chunkZ) {
		Chunk chunk = null;
		if(this.chunkExists(chunkX, chunkZ)) {
			chunk = this.getChunkFromChunkCoords(chunkX, chunkZ);
		} else {
			chunk = this.chunkProvider.justGenerateForHeight(chunkX, chunkZ);
		}
		return chunk.isOcean;
	}
	
	public boolean isUrbanChunk(int chunkX, int chunkZ) {
		Chunk chunk = null;
		if(this.chunkExists(chunkX, chunkZ)) {
			chunk = this.getChunkFromChunkCoords(chunkX, chunkZ);
		} else {
			chunk = this.chunkProvider.justGenerateForHeight(chunkX, chunkZ);
		}
		return chunk.isUrbanChunk;
	}
	
	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		if(this.chunkExists(chunkX, chunkZ)) {
			return this.getChunkFromChunkCoords(chunkX, chunkZ);
		} else {
			return this.chunkProvider.justGenerateForHeight(chunkX, chunkZ);
		}
	}
		
	public int getHeightValueUnderWater (int x, int z) {
		// Start here
		int y = getHeightValue (x, z);
		
		while (y > 8) {
			y --;
			if (getBlockId (x, y, z) != Block.waterStill.blockID) break;
		}
		
		return y;
	}

	public int getSkyBlockTypeBrightness(EnumSkyBlock enumSkyBlock1, int i2, int i3, int i4) {
		if(this.worldProvider.hasNoSky && enumSkyBlock1 == EnumSkyBlock.Sky) {
			return 0;
		} else {
			if(i3 < 0) {
				i3 = 0;
			}

			if(i3 >= 128) {
				return enumSkyBlock1.defaultLightValue;
			} else {
				int i5 = i2 >> 4;
				int i6 = i4 >> 4;
				if(!this.chunkExists(i5, i6)) {
					return enumSkyBlock1.defaultLightValue;
				} else if(Block.useNeighborBrightness[this.getBlockId(i2, i3, i4)]) {
					int i12 = this.getSavedLightValue(enumSkyBlock1, i2, i3 + 1, i4);
					int i8 = this.getSavedLightValue(enumSkyBlock1, i2 + 1, i3, i4);
					int i9 = this.getSavedLightValue(enumSkyBlock1, i2 - 1, i3, i4);
					int i10 = this.getSavedLightValue(enumSkyBlock1, i2, i3, i4 + 1);
					int i11 = this.getSavedLightValue(enumSkyBlock1, i2, i3, i4 - 1);
					if(i8 > i12) {
						i12 = i8;
					}

					if(i9 > i12) {
						i12 = i9;
					}

					if(i10 > i12) {
						i12 = i10;
					}

					if(i11 > i12) {
						i12 = i11;
					}

					return i12;
				} else {
					Chunk chunk7 = this.getChunkFromChunkCoords(i5, i6);
					return chunk7.getSavedLightValue(enumSkyBlock1, i2 & 15, i3, i4 & 15);
				}
			} 
		}
	}

	public int getSavedLightValue(EnumSkyBlock enumSkyBlock1, int i2, int i3, int i4) {
		if(i3 < 0) {
			i3 = 0;
		}

		if(i3 >= 128) {
			i3 = 127;
		}

		if(i3 >= 0 && i3 < 128) {
			int i5 = i2 >> 4;
			int i6 = i4 >> 4;
			if(!this.chunkExists(i5, i6)) {
				return 0;
			} else {
				Chunk chunk7 = this.getChunkFromChunkCoords(i5, i6);
				return chunk7.getSavedLightValue(enumSkyBlock1, i2 & 15, i3, i4 & 15);
			}
		} else {
			return enumSkyBlock1.defaultLightValue;
		}
	}

	public void setLightValue(EnumSkyBlock enumSkyBlock1, int i2, int i3, int i4, int i5) {
		if(i3 >= 0) {
			if(i3 < 128) {
				if(this.chunkExists(i2 >> 4, i4 >> 4)) {
					Chunk chunk6 = this.getChunkFromChunkCoords(i2 >> 4, i4 >> 4);
					int previous = chunk6.getSavedLightValue(enumSkyBlock1, i2 & 15, i3, i4 & 15);
					chunk6.setLightValue(enumSkyBlock1, i2 & 15, i3, i4 & 15, i5);

					if(previous != i5) {
						for(int i7 = 0; i7 < this.worldAccesses.size(); ++i7) {
							((IWorldAccess)this.worldAccesses.get(i7)).markBlockNeedsUpdate(i2, i3, i4);
						}
					}

				}
			}
		}
	}

	public int getLightBrightnessForSkyBlocks(int i1, int i2, int i3, int i4) {
		int i5 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, i1, i2, i3);
		int i6 = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, i1, i2, i3);
		if(i6 < i4) {
			i6 = i4;
		}

		return i5 << 20 | i6 << 4;
	}

	public float getBrightness(int i1, int i2, int i3, int i4) {
		int i5 = this.getBlockLightValue(i1, i2, i3);
		if(i5 < i4) {
			i5 = i4;
		}

		return this.worldProvider.lightBrightnessTable[i5];
	}

	public float getLightBrightness(int i1, int i2, int i3) {
		return this.worldProvider.lightBrightnessTable[this.getBlockLightValue(i1, i2, i3)];
	}

	public boolean isDaytime() {
		return this.skylightSubtracted < 4;
	}

	public MovingObjectPosition rayTraceBlocks(Vec3D vec3D1, Vec3D vec3D2) {
		return this.rayTraceBlocks(vec3D1, vec3D2, false, false);
	}

	public MovingObjectPosition rayTraceBlocks(Vec3D vec3D1, Vec3D vec3D2, boolean z3) {
		return this.rayTraceBlocks(vec3D1, vec3D2, z3, false);
	}

	public MovingObjectPosition rayTraceBlocks(Vec3D vFrom, Vec3D vTo, boolean z3, boolean z4) {
		if(!Double.isNaN(vFrom.xCoord) && !Double.isNaN(vFrom.yCoord) && !Double.isNaN(vFrom.zCoord)) {
			if(!Double.isNaN(vTo.xCoord) && !Double.isNaN(vTo.yCoord) && !Double.isNaN(vTo.zCoord)) {
				int x2 = MathHelper.floor_double(vTo.xCoord);
				int y2 = MathHelper.floor_double(vTo.yCoord);
				int z2 = MathHelper.floor_double(vTo.zCoord);

				int x1 = MathHelper.floor_double(vFrom.xCoord);
				int y1 = MathHelper.floor_double(vFrom.yCoord);
				int z1 = MathHelper.floor_double(vFrom.zCoord);

				int blockID = this.getBlockId(x1, y1, z1);
				int metadata = this.getBlockMetadata(x1, y1, z1);
				Block block = Block.blocksList[blockID];

				if(
					(
						!z4 || 
						block == null || 
						block.getCollisionBoundingBoxFromPool(this, x1, y1, z1) != null
					) && 
					blockID > 0 && 
					block.canCollideCheck(metadata, z3)
				) {
					MovingObjectPosition pos = block.collisionRayTrace(this, x1, y1, z1, vFrom, vTo);
					if(pos != null) {
						return pos;
					}
				}

				int it = 200;
				while(it-- >= 0) {
					if(
						Double.isNaN(vFrom.xCoord) || 
						Double.isNaN(vFrom.yCoord) || 
						Double.isNaN(vFrom.zCoord)
					) {
						return null;
					}

					if(x1 == x2 && y1 == y2 && z1 == z2) {
						return null;
					}

					boolean z39 = true;
					boolean z40 = true;
					boolean z41 = true;
					double d15 = 999.0D;
					double d17 = 999.0D;
					double d19 = 999.0D;

					if(x2 > x1) {
						d15 = (double)x1 + 1.0D;
					} else if(x2 < x1) {
						d15 = (double)x1 + 0.0D;
					} else {
						z39 = false;
					}

					if(y2 > y1) {
						d17 = (double)y1 + 1.0D;
					} else if(y2 < y1) {
						d17 = (double)y1 + 0.0D;
					} else {
						z40 = false;
					}

					if(z2 > z1) {
						d19 = (double)z1 + 1.0D;
					} else if(z2 < z1) {
						d19 = (double)z1 + 0.0D;
					} else {
						z41 = false;
					}

					double d21 = 999.0D;
					double d23 = 999.0D;
					double d25 = 999.0D;
					double d27 = vTo.xCoord - vFrom.xCoord;
					double d29 = vTo.yCoord - vFrom.yCoord;
					double d31 = vTo.zCoord - vFrom.zCoord;

					if(z39) {
						d21 = (d15 - vFrom.xCoord) / d27;
					}

					if(z40) {
						d23 = (d17 - vFrom.yCoord) / d29;
					}

					if(z41) {
						d25 = (d19 - vFrom.zCoord) / d31;
					}

					byte face;
					if(d21 < d23 && d21 < d25) {
						if(x2 > x1) {
							face = 4;
						} else {
							face = 5;
						}

						vFrom.xCoord = d15;
						vFrom.yCoord += d29 * d21;
						vFrom.zCoord += d31 * d21;
					} else if(d23 < d25) {
						if(y2 > y1) {
							face = 0;
						} else {
							face = 1;
						}

						vFrom.xCoord += d27 * d23;
						vFrom.yCoord = d17;
						vFrom.zCoord += d31 * d23;
					} else {
						if(z2 > z1) {
							face = 2;
						} else {
							face = 3;
						}

						vFrom.xCoord += d27 * d25;
						vFrom.yCoord += d29 * d25;
						vFrom.zCoord = d19;
					}

					Vec3D vec3D34 = Vec3D.createVector(vFrom.xCoord, vFrom.yCoord, vFrom.zCoord);
					x1 = (int)(vec3D34.xCoord = (double)MathHelper.floor_double(vFrom.xCoord));
					if(face == 5) {
						--x1;
						++vec3D34.xCoord;
					}

					y1 = (int)(vec3D34.yCoord = (double)MathHelper.floor_double(vFrom.yCoord));
					if(face == 1) {
						--y1;
						++vec3D34.yCoord;
					}

					z1 = (int)(vec3D34.zCoord = (double)MathHelper.floor_double(vFrom.zCoord));
					if(face == 3) {
						--z1;
						++vec3D34.zCoord;
					}

					blockID = this.getBlockId(x1, y1, z1);
					metadata = this.getBlockMetadata(x1, y1, z1);
					block = Block.blocksList[blockID];

					if(
						(
							!z4 || 
							block == null || 
							block.getCollisionBoundingBoxFromPool(this, x1, y1, z1) != null
						) && 
						blockID > 0 && 
						(block == null || block.canCollideCheck(metadata, z3))
					) {
						MovingObjectPosition pos = null;
						if(block != null) pos = block.collisionRayTrace(this, x1, y1, z1, vFrom, vTo);
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

	public void playSoundAtEntity(Entity entity1, String string2, float f3, float f4) {
		for(int i5 = 0; i5 < this.worldAccesses.size(); ++i5) {
			((IWorldAccess)this.worldAccesses.get(i5)).playSound(string2, entity1.posX, entity1.posY - (double)entity1.yOffset, entity1.posZ, f3, f4);
		}

	}

	public void playSoundEffect(double d1, double d3, double d5, String string7, float f8, float f9) {
		for(int i10 = 0; i10 < this.worldAccesses.size(); ++i10) {
			((IWorldAccess)this.worldAccesses.get(i10)).playSound(string7, d1, d3, d5, f8, f9);
		}

	}

	public void playRecord(String string1, int i2, int i3, int i4) {
		for(int i5 = 0; i5 < this.worldAccesses.size(); ++i5) {
			((IWorldAccess)this.worldAccesses.get(i5)).playRecord(string1, i2, i3, i4);
		}

	}

	public void spawnParticle(String string1, double d2, double d4, double d6, double d8, double d10, double d12) {
		for(int i14 = 0; i14 < this.worldAccesses.size(); ++i14) {
			((IWorldAccess)this.worldAccesses.get(i14)).spawnParticle(string1, d2, d4, d6, d8, d10, d12);
		}

	}

	public boolean addWeatherEffect(Entity entity1) {
		this.weatherEffects.add(entity1);
		return true;
	}

	public boolean spawnEntityInWorld(Entity entity1) {
		int i2 = MathHelper.floor_double(entity1.posX / 16.0D);
		int i3 = MathHelper.floor_double(entity1.posZ / 16.0D);
		boolean z4 = false;
		if(entity1 instanceof EntityPlayer) {
			z4 = true;
		}

		if(!z4 && !this.chunkExists(i2, i3)) {
			return false;
		} else {
			if(entity1 instanceof EntityPlayer) {
				EntityPlayer entityPlayer5 = (EntityPlayer)entity1;
				this.playerEntities.add(entityPlayer5);
				this.updateAllPlayersSleepingFlag();
			}

			this.getChunkFromChunkCoords(i2, i3).addEntity(entity1);
			this.loadedEntityList.add(entity1);
			this.obtainEntitySkin(entity1);
			return true;
		}
	}
	
	public Entity getEntityById(int id) {
		Iterator<Entity>it = this.loadedEntityList.iterator();
		while(it.hasNext()) {
			Entity e = it.next();
			if(e.entityId == id) return e;
		}
		return null;
	}

	protected void obtainEntitySkin(Entity entity1) {
		for(int i2 = 0; i2 < this.worldAccesses.size(); ++i2) {
			((IWorldAccess)this.worldAccesses.get(i2)).obtainEntitySkin(entity1);
		}

	}

	protected void releaseEntitySkin(Entity entity1) {
		for(int i2 = 0; i2 < this.worldAccesses.size(); ++i2) {
			((IWorldAccess)this.worldAccesses.get(i2)).releaseEntitySkin(entity1);
		}

	}

	public void setEntityDead(Entity entity1) {
		if(entity1.riddenByEntity != null) {
			entity1.riddenByEntity.mountEntity((Entity)null);
		}

		if(entity1.ridingEntity != null) {
			entity1.mountEntity((Entity)null);
		}

		entity1.setEntityDead();
		if(entity1 instanceof EntityPlayer) {
			this.playerEntities.remove((EntityPlayer)entity1);
			this.updateAllPlayersSleepingFlag();
		}

	}

	public void removePlayer(Entity entity1) {
		entity1.setEntityDead();
		if(entity1 instanceof EntityPlayer) {
			this.playerEntities.remove((EntityPlayer)entity1);
			this.updateAllPlayersSleepingFlag();
		}

		int i2 = entity1.chunkCoordX;
		int i3 = entity1.chunkCoordZ;
		if(entity1.addedToChunk && this.chunkExists(i2, i3)) {
			this.getChunkFromChunkCoords(i2, i3).removeEntity(entity1);
		}

		this.loadedEntityList.remove(entity1);
		this.releaseEntitySkin(entity1);
	}
	
	public void addWorldAccess(IWorldAccess iWorldAccess1) {
		this.worldAccesses.add(iWorldAccess1);
	}

	public void removeWorldAccess(IWorldAccess iWorldAccess1) {
		this.worldAccesses.remove(iWorldAccess1);
	}
	
	public List<AxisAlignedBB> getCollidingBoundingBoxesExcludingWater(Entity entity1, AxisAlignedBB axisAlignedBB2) {
		this.collidingBoundingBoxes.clear();
		int i3 = MathHelper.floor_double(axisAlignedBB2.minX);
		int i4 = MathHelper.floor_double(axisAlignedBB2.maxX + 1.0D);
		int i5 = MathHelper.floor_double(axisAlignedBB2.minY);
		int i6 = MathHelper.floor_double(axisAlignedBB2.maxY + 1.0D);
		int i7 = MathHelper.floor_double(axisAlignedBB2.minZ);
		int i8 = MathHelper.floor_double(axisAlignedBB2.maxZ + 1.0D);

		for(int i9 = i3; i9 < i4; ++i9) {
			for(int i10 = i7; i10 < i8; ++i10) {
				if(this.blockExists(i9, 64, i10)) {
					for(int i11 = i5 - 1; i11 < i6; ++i11) {
						Block block12 = Block.blocksList[this.getBlockId(i9, i11, i10)];
						if(block12 != null && !(block12 instanceof BlockFluid)) {
							block12.getCollidingBoundingBoxes(this, i9, i11, i10, axisAlignedBB2, this.collidingBoundingBoxes, entity1);
						}
					}
				}
			}
		}
		
		return this.collidingBoundingBoxes;
	}

	public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity1, AxisAlignedBB axisAlignedBB2) {
		this.collidingBoundingBoxes.clear();
		int i3 = MathHelper.floor_double(axisAlignedBB2.minX);
		int i4 = MathHelper.floor_double(axisAlignedBB2.maxX + 1.0D);
		int i5 = MathHelper.floor_double(axisAlignedBB2.minY);
		int i6 = MathHelper.floor_double(axisAlignedBB2.maxY + 1.0D);
		int i7 = MathHelper.floor_double(axisAlignedBB2.minZ);
		int i8 = MathHelper.floor_double(axisAlignedBB2.maxZ + 1.0D);

		for(int i9 = i3; i9 < i4; ++i9) {
			for(int i10 = i7; i10 < i8; ++i10) {
				if(this.blockExists(i9, 64, i10)) {
					for(int i11 = i5 - 1; i11 < i6; ++i11) {
						Block block12 = Block.blocksList[this.getBlockId(i9, i11, i10)];
						if(block12 != null) {
							block12.getCollidingBoundingBoxes(this, i9, i11, i10, axisAlignedBB2, this.collidingBoundingBoxes, entity1);
						}
					}
				}
			}
		}

		double d14 = 0.25D;
		List<Entity> list15 = this.getEntitiesWithinAABBExcludingEntity(entity1, axisAlignedBB2.expand(d14, d14, d14));

		for(int i16 = 0; i16 < list15.size(); ++i16) {
			AxisAlignedBB axisAlignedBB13 = ((Entity)list15.get(i16)).getBoundingBox();
			if(axisAlignedBB13 != null && axisAlignedBB13.intersectsWith(axisAlignedBB2)) {
				this.collidingBoundingBoxes.add(axisAlignedBB13);
			}

			axisAlignedBB13 = entity1.getCollisionBox((Entity)list15.get(i16));
			if(axisAlignedBB13 != null && axisAlignedBB13.intersectsWith(axisAlignedBB2)) {
				this.collidingBoundingBoxes.add(axisAlignedBB13);
			}
		}

		return this.collidingBoundingBoxes;
	}

	public int calculateSkylightSubtracted(float renderPartialTick) {
		float f2 = this.getCelestialAngle(renderPartialTick);
		float f3 = 1.0F - (MathHelper.cos(f2 * (float)Math.PI * 2.0F) * 2.0F + 0.5F);
		if(f3 < 0.0F) {
			f3 = 0.0F;
		}

		if(f3 > 1.0F) {
			f3 = 1.0F;
		}

		f3 = 1.0F - f3;
		/*
		f3 = (float)((double)f3 * (1.0D - (double)(this.getRainStrength(f1) * 5.0F) / 16.0D));
		f3 = (float)((double)f3 * (1.0D - (double)(this.getWeightedThunderStrength(f1) * 5.0F) / 16.0D));
		*/
		float rainStrength = this.getRainStrength(renderPartialTick) - this.getSnowStrength(renderPartialTick);
		if(rainStrength < 0.0F) rainStrength = 0.0F;
		f3 = (float)((double)f3 * (1.0D - (double)(rainStrength * 5F) / 16D));
		float factor = 6F - 3 * rainStrength;
		f3 = (float)((double)f3 * (1.0D - (double)(getWeightedThunderStrength(renderPartialTick) * factor) / 16D));
		
		f3 = 1.0F - f3;

		return (int)(f3 * 11.0F);
	}

	public float getSunBrightness(float f1) {
		//float f2 = this.getCelestialAngle(f1);
		float f2 = this.getCelestialAngle(f1);
		float f3 = 1.0F - (MathHelper.cos(f2 * (float)Math.PI * 2.0F) * 2.0F + 0.2F);
		if(f3 < 0.0F) {
			f3 = 0.0F;
		}

		if(f3 > 1.0F) {
			f3 = 1.0F;
		}

		f3 = 1.0F - f3;
		f3 = (float)((double)f3 * (1.0D - (double)(this.getRainStrength(f1) * 5.0F) / 16.0D));
		f3 = (float)((double)f3 * (1.0D - (double)(this.getWeightedThunderStrength(f1) * 5.0F) / 16.0D));
		return f3 * 0.8F + 0.2F;
	}
	
	public Vec3D getSkyColor(Entity entity1, float renderPartialTick) {
		float celestialAngle = this.getCelestialAngle(renderPartialTick);
		float celestialLight = MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(celestialLight < 0.0F) {
			celestialLight = 0.0F;
		}

		if(celestialLight > 1.0F) {
			celestialLight = 1.0F;
		}

		int skyColor;
		if(this.colouredAthmospherics) {
			skyColor = Seasons.getSkyColorForToday();
		} else {
			skyColor = 0x88BBFF;
		}
		
		float r = (float)(skyColor >> 16 & 255L) / 255.0F;
		float g = (float)(skyColor >> 8 & 255L) / 255.0F;
		float b = (float)(skyColor & 255L) / 255.0F;
		r *= celestialLight;
		g *= celestialLight;
		b *= celestialLight;

		/*
		float skyColorComponent;
		float skyColorAtenuation;
		
		float rainAtenuation = this.getRainStrength(renderPartialTick);
		
		if(rainAtenuation > 0.0F) {
			skyColorComponent = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
			skyColorAtenuation = 1.0F - rainAtenuation * 0.75F;
			r = r * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			g = g * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			b = b * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
		}

		float thunderingAtenuation = this.getWeightedThunderStrength(renderPartialTick);
		if(thunderingAtenuation > 0.0F) {
			skyColorComponent = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
			skyColorAtenuation = 1.0F - thunderingAtenuation * 0.75F;
			r = r * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			g = g * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			b = b * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
		}
		*/
		
		float atenuationStrength = this.getRainStrength(renderPartialTick) + this.getWeightedThunderStrength(renderPartialTick) - this.getSnowStrength(renderPartialTick);
		if(atenuationStrength >= 0.0F) {
			if(atenuationStrength >= 1.0F) atenuationStrength = 1.0F;
			float skyColorComponent = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
			float skyColorAtenuation = 1.0F - atenuationStrength * 0.75F;
			r = r * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			g = g * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			b = b * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
		}		

		if(this.lightningFlash > 0) {
			float lightning = (float)this.lightningFlash - renderPartialTick;
			if(lightning > 1.0F) {
				lightning = 1.0F;
			}

			lightning *= 0.45F;
			r = r * (1.0F - lightning) + 0.8F * lightning;
			g = g * (1.0F - lightning) + 0.8F * lightning;
			b = b * (1.0F - lightning) + 1.0F * lightning;
		}

		return Vec3D.createVector((double)r, (double)g, (double)b);
	}

	public float getCelestialAngle(float f1) {
		return this.worldProvider.calculateCelestialAngle(this.worldInfo.getWorldTime(), f1);
	}

	public Vec3D getCloudColor(float f1) {
		float f2 = this.getCelestialAngle(f1);
		float f3 = MathHelper.cos(f2 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(f3 < 0.0F) {
			f3 = 0.0F;
		}

		if(f3 > 1.0F) {
			f3 = 1.0F;
		}

		float f4 = (float)(this.cloudColour >> 16 & 255L) / 255.0F;
		float f5 = (float)(this.cloudColour >> 8 & 255L) / 255.0F;
		float f6 = (float)(this.cloudColour & 255L) / 255.0F;
		float f7 = this.getRainStrength(f1);
		float f8;
		float f9;
		if(f7 > 0.0F) {
			f8 = (f4 * 0.3F + f5 * 0.59F + f6 * 0.11F) * 0.6F;
			f9 = 1.0F - f7 * 0.95F;
			f4 = f4 * f9 + f8 * (1.0F - f9);
			f5 = f5 * f9 + f8 * (1.0F - f9);
			f6 = f6 * f9 + f8 * (1.0F - f9);
		}

		f4 *= f3 * 0.9F + 0.1F;
		f5 *= f3 * 0.9F + 0.1F;
		f6 *= f3 * 0.85F + 0.15F;
		f8 = this.getWeightedThunderStrength(f1);
		if(f8 > 0.0F) {
			f9 = (f4 * 0.3F + f5 * 0.59F + f6 * 0.11F) * 0.2F;
			float f10 = 1.0F - f8 * 0.95F;
			f4 = f4 * f10 + f9 * (1.0F - f10);
			f5 = f5 * f10 + f9 * (1.0F - f10);
			f6 = f6 * f10 + f9 * (1.0F - f10);
		}

		return Vec3D.createVector((double)f4, (double)f5, (double)f6);
	}

	public Vec3D getFogColor(float f1) {
		float f2 = this.getCelestialAngle(f1);
		return this.worldProvider.getFogColor(f2, f1, this.worldInfo.isBloodMoon(), this.colouredAthmospherics);
	}

	public int findTopSolidBlockUsingBlockMaterial(int x, int z) {
		Chunk chunk3 = this.getChunkFromBlockCoords(x, z);
		int y = 127;
		x &= 15;
		z &= 15;
		
		for(; y > 0; --y) {
			Block block = Block.blocksList[chunk3.getBlockID(x, y, z)];
			if(block == null) continue;
			if(block.blockMaterial.getIsSolid() || block.blockMaterial.getIsLiquid()) {
				return y + 1;
			}
		}

		return -1;
	}

	public float getStarBrightness(float f1) {
		float f2 = this.getCelestialAngle(f1);
		float f3 = 1.0F - (MathHelper.cos(f2 * (float)Math.PI * 2.0F) * 2.0F + 0.75F);
		if(f3 < 0.0F) {
			f3 = 0.0F;
		}

		if(f3 > 1.0F) {
			f3 = 1.0F;
		}

		return f3 * f3 * 0.5F;
	}

	public int findTopSolidBlock(int i1, int i2) {
		Chunk chunk3 = this.getChunkFromBlockCoords(i1, i2);
		int i4 = 127;
		i1 &= 15;

		for(i2 &= 15; i4 > 0; --i4) {
			int i5 = chunk3.getBlockID(i1, i4, i2);
			if(i5 != 0 && Block.blocksList[i5].blockMaterial.getIsSolid()) {
				return i4 + 1;
			}
		}

		return -1;
	}

	public void scheduleBlockUpdate(int x, int y, int z, int blockID, int tickRate) {
		NextTickListEntry nextTickListEntry6 = new NextTickListEntry(x, y, z, blockID);
		byte b7 = 8;
		if(this.scheduledUpdatesAreImmediate) {
			if(this.checkChunksExist(nextTickListEntry6.xCoord - b7, nextTickListEntry6.yCoord - b7, nextTickListEntry6.zCoord - b7, nextTickListEntry6.xCoord + b7, nextTickListEntry6.yCoord + b7, nextTickListEntry6.zCoord + b7)) {
				int i8 = this.getBlockId(nextTickListEntry6.xCoord, nextTickListEntry6.yCoord, nextTickListEntry6.zCoord);
				if(i8 == nextTickListEntry6.blockID && i8 > 0) {
					Block.blocksList[i8].updateTick(this, nextTickListEntry6.xCoord, nextTickListEntry6.yCoord, nextTickListEntry6.zCoord, this.rand);
				}
			}

		} else {
			if(this.checkChunksExist(x - b7, y - b7, z - b7, x + b7, y + b7, z + b7)) {
				if(blockID > 0) {
					nextTickListEntry6.setScheduledTime((long)tickRate + this.worldInfo.getWorldTime());
				}

				if(!this.scheduledTickSet.contains(nextTickListEntry6)) {
					this.scheduledTickSet.add(nextTickListEntry6);
					this.scheduledTickTreeSet.add(nextTickListEntry6);
				}
			}

		}
	}

	public void updateEntities() {
		int i1;
		Entity entity2;
		for(i1 = 0; i1 < this.weatherEffects.size(); ++i1) {
			entity2 = (Entity)this.weatherEffects.get(i1);
			entity2.onUpdate();
			if(entity2.isDead) {
				this.weatherEffects.remove(i1--);
			}
		}

		this.loadedEntityList.removeAll(this.unloadedEntityList);

		int i3;
		int i4;
		for(i1 = 0; i1 < this.unloadedEntityList.size(); ++i1) {
			entity2 = (Entity)this.unloadedEntityList.get(i1);
			i3 = entity2.chunkCoordX;
			i4 = entity2.chunkCoordZ;
			if(entity2.addedToChunk && this.chunkExists(i3, i4)) {
				this.getChunkFromChunkCoords(i3, i4).removeEntity(entity2);
			}
		}

		for(i1 = 0; i1 < this.unloadedEntityList.size(); ++i1) {
			this.releaseEntitySkin((Entity)this.unloadedEntityList.get(i1));
		}

		this.unloadedEntityList.clear();

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

		for(i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
			entity2 = (Entity)this.loadedEntityList.get(i1);
			if(entity2.ridingEntity != null) {
				if(!entity2.ridingEntity.isDead && entity2.ridingEntity.riddenByEntity == entity2) {
					continue;
				}

				entity2.ridingEntity.riddenByEntity = null;
				entity2.ridingEntity = null;
			}

			if(!entity2.isDead) {
				boolean active = entity2 instanceof EntityPlayer
					|| activeChunks.contains(ChunkCoordIntPair.chunkXZ2Int(entity2.chunkCoordX, entity2.chunkCoordZ));

				if(active) {
					this.updateEntity(entity2);
				} else {
					entity2.ticksExisted++;
					this.tickDespawnOnly(entity2);
				}
			}

			if(entity2.isDead) {
				i3 = entity2.chunkCoordX;
				i4 = entity2.chunkCoordZ;
				if(entity2.addedToChunk && this.chunkExists(i3, i4)) {
					this.getChunkFromChunkCoords(i3, i4).removeEntity(entity2);
				}

				this.loadedEntityList.remove(i1--);
				this.releaseEntitySkin(entity2);
			}
		}

		this.scanningTileEntities = true;
		Iterator<TileEntity> iterator10 = this.loadedTileEntityList.iterator();

		while(iterator10.hasNext()) {
			TileEntity tileEntity5 = (TileEntity)iterator10.next();
			if(!tileEntity5.isInvalid()) {
				tileEntity5.updateEntity();
			}

			if(tileEntity5.isInvalid()) {
				iterator10.remove();
				Chunk chunk7 = this.getChunkFromChunkCoords(tileEntity5.xCoord >> 4, tileEntity5.zCoord >> 4);
				if(chunk7 != null) {
					chunk7.removeChunkBlockTileEntity(tileEntity5.xCoord & 15, tileEntity5.yCoord, tileEntity5.zCoord & 15);
				}
			}
		}

		this.scanningTileEntities = false;
		if(!this.entityRemoval.isEmpty()) {
			Iterator<TileEntity> iterator6 = this.entityRemoval.iterator();

			while(iterator6.hasNext()) {
				TileEntity tileEntity8 = (TileEntity)iterator6.next();
				if(!tileEntity8.isInvalid()) {
					if(!this.loadedTileEntityList.contains(tileEntity8)) {
						this.loadedTileEntityList.add(tileEntity8);
					}

					Chunk chunk9 = this.getChunkFromChunkCoords(tileEntity8.xCoord >> 4, tileEntity8.zCoord >> 4);
					if(chunk9 != null) {
						chunk9.setChunkBlockTileEntity(tileEntity8.xCoord & 15, tileEntity8.yCoord, tileEntity8.zCoord & 15, tileEntity8);
					}

					this.markBlockNeedsUpdate(tileEntity8.xCoord, tileEntity8.yCoord, tileEntity8.zCoord);
				}
			}

			this.entityRemoval.clear();
		}

	}

	private void tickDespawnOnly(Entity entity) {
		if(entity instanceof EntityLiving && !entity.isDead && !this.isRemote) {
			EntityLiving living = (EntityLiving)entity;
			++living.entityAge;
			living.performDespawn();
		}
	}

	public void addTileEntity(Collection<TileEntity> collection1) {
		if(this.scanningTileEntities) {
			this.entityRemoval.addAll(collection1);
		} else {
			this.loadedTileEntityList.addAll(collection1);
		}

	}

	public void updateEntity(Entity entity1) {
		this.updateEntityWithOptionalForce(entity1, true);
	}

	public void updateEntityWithOptionalForce(Entity entity1, boolean z2) {
		int i3 = MathHelper.floor_double(entity1.posX);
		int i4 = MathHelper.floor_double(entity1.posZ);
		byte b5 = 32;
		if(!z2 || this.checkChunksExist(i3 - b5, 0, i4 - b5, i3 + b5, 128, i4 + b5)) {
			entity1.lastTickPosX = entity1.posX;
			entity1.lastTickPosY = entity1.posY;
			entity1.lastTickPosZ = entity1.posZ;
			entity1.prevRotationYaw = entity1.rotationYaw;
			entity1.prevRotationPitch = entity1.rotationPitch;
			if(z2 && entity1.addedToChunk) {
				if(entity1.ridingEntity != null) {
					entity1.updateRidden();
				} else {
					entity1.onUpdate();
				}
			}

			if(Double.isNaN(entity1.posX) || Double.isInfinite(entity1.posX)) {
				entity1.posX = entity1.lastTickPosX;
			}

			if(Double.isNaN(entity1.posY) || Double.isInfinite(entity1.posY)) {
				entity1.posY = entity1.lastTickPosY;
			}

			if(Double.isNaN(entity1.posZ) || Double.isInfinite(entity1.posZ)) {
				entity1.posZ = entity1.lastTickPosZ;
			}

			if(Double.isNaN((double)entity1.rotationPitch) || Double.isInfinite((double)entity1.rotationPitch)) {
				entity1.rotationPitch = entity1.prevRotationPitch;
			}

			if(Double.isNaN((double)entity1.rotationYaw) || Double.isInfinite((double)entity1.rotationYaw)) {
				entity1.rotationYaw = entity1.prevRotationYaw;
			}

			int i6 = MathHelper.floor_double(entity1.posX / 16.0D);
			int i7 = MathHelper.floor_double(entity1.posY / 16.0D);
			int i8 = MathHelper.floor_double(entity1.posZ / 16.0D);
			if(!entity1.addedToChunk || entity1.chunkCoordX != i6 || entity1.chunkCoordY != i7 || entity1.chunkCoordZ != i8) {
				if(entity1.addedToChunk && this.chunkExists(entity1.chunkCoordX, entity1.chunkCoordZ)) {
					this.getChunkFromChunkCoords(entity1.chunkCoordX, entity1.chunkCoordZ).removeEntityAtIndex(entity1, entity1.chunkCoordY);
				}

				if(this.chunkExists(i6, i8)) {
					entity1.addedToChunk = true;
					this.getChunkFromChunkCoords(i6, i8).addEntity(entity1);
				} else {
					entity1.addedToChunk = false;
				}
			}

			if(z2 && entity1.addedToChunk && entity1.riddenByEntity != null) {
				if(!entity1.riddenByEntity.isDead && entity1.riddenByEntity.ridingEntity == entity1) {
					this.updateEntity(entity1.riddenByEntity);
				} else {
					entity1.riddenByEntity.ridingEntity = null;
					entity1.riddenByEntity = null;
				}
			}

		}
	}

	public boolean checkIfAABBIsClear(AxisAlignedBB axisAlignedBB1) {
		List<Entity> list2 = this.getEntitiesWithinAABBExcludingEntity((Entity)null, axisAlignedBB1);

		for(int i3 = 0; i3 < list2.size(); ++i3) {
			Entity entity4 = (Entity)list2.get(i3);
			if(!entity4.isDead && entity4.preventEntitySpawning) {
				return false;
			}
		}

		return true;
	}

	public boolean getIsAnyNonEmptyBlock(AxisAlignedBB axisAlignedBB1) {
		int i2 = MathHelper.floor_double(axisAlignedBB1.minX);
		int i3 = MathHelper.floor_double(axisAlignedBB1.maxX + 1.0D);
		int i4 = MathHelper.floor_double(axisAlignedBB1.minY);
		int i5 = MathHelper.floor_double(axisAlignedBB1.maxY + 1.0D);
		int i6 = MathHelper.floor_double(axisAlignedBB1.minZ);
		int i7 = MathHelper.floor_double(axisAlignedBB1.maxZ + 1.0D);
		if(axisAlignedBB1.minX < 0.0D) {
			--i2;
		}

		if(axisAlignedBB1.minY < 0.0D) {
			--i4;
		}

		if(axisAlignedBB1.minZ < 0.0D) {
			--i6;
		}

		for(int i8 = i2; i8 < i3; ++i8) {
			for(int i9 = i4; i9 < i5; ++i9) {
				for(int i10 = i6; i10 < i7; ++i10) {
					Block block11 = Block.blocksList[this.getBlockId(i8, i9, i10)];
					if(block11 != null) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean getIsAnyLiquid(AxisAlignedBB axisAlignedBB1) {
		int i2 = MathHelper.floor_double(axisAlignedBB1.minX);
		int i3 = MathHelper.floor_double(axisAlignedBB1.maxX + 1.0D);
		int i4 = MathHelper.floor_double(axisAlignedBB1.minY);
		int i5 = MathHelper.floor_double(axisAlignedBB1.maxY + 1.0D);
		int i6 = MathHelper.floor_double(axisAlignedBB1.minZ);
		int i7 = MathHelper.floor_double(axisAlignedBB1.maxZ + 1.0D);
		if(axisAlignedBB1.minX < 0.0D) {
			--i2;
		}

		if(axisAlignedBB1.minY < 0.0D) {
			--i4;
		}

		if(axisAlignedBB1.minZ < 0.0D) {
			--i6;
		}

		for(int i8 = i2; i8 < i3; ++i8) {
			for(int i9 = i4; i9 < i5; ++i9) {
				for(int i10 = i6; i10 < i7; ++i10) {
					Block block11 = Block.blocksList[this.getBlockId(i8, i9, i10)];
					if(block11 != null && block11.blockMaterial.getIsLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public boolean getIsAnyBlockID(AxisAlignedBB aabb, int blockID) {
		int i2 = MathHelper.floor_double(aabb.minX);
		int i3 = MathHelper.floor_double(aabb.maxX + 1.0D);
		int i4 = MathHelper.floor_double(aabb.minY);
		int i5 = MathHelper.floor_double(aabb.maxY + 1.0D);
		int i6 = MathHelper.floor_double(aabb.minZ);
		int i7 = MathHelper.floor_double(aabb.maxZ + 1.0D);
		if(aabb.minX < 0.0D) {
			--i2;
		}

		if(aabb.minY < 0.0D) {
			--i4;
		}

		if(aabb.minZ < 0.0D) {
			--i6;
		}

		for(int i8 = i2; i8 < i3; ++i8) {
			for(int i9 = i4; i9 < i5; ++i9) {
				for(int i10 = i6; i10 < i7; ++i10) {
					if(blockID == this.getBlockId(i8, i9, i10)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean isBoundingBoxBurning(AxisAlignedBB axisAlignedBB1) {
		int i2 = MathHelper.floor_double(axisAlignedBB1.minX);
		int i3 = MathHelper.floor_double(axisAlignedBB1.maxX + 1.0D);
		int i4 = MathHelper.floor_double(axisAlignedBB1.minY);
		int i5 = MathHelper.floor_double(axisAlignedBB1.maxY + 1.0D);
		int i6 = MathHelper.floor_double(axisAlignedBB1.minZ);
		int i7 = MathHelper.floor_double(axisAlignedBB1.maxZ + 1.0D);
		if(this.checkChunksExist(i2, i4, i6, i3, i5, i7)) {
			for(int i8 = i2; i8 < i3; ++i8) {
				for(int i9 = i4; i9 < i5; ++i9) {
					for(int i10 = i6; i10 < i7; ++i10) {
						int i11 = this.getBlockId(i8, i9, i10);
						if(i11 == Block.fire.blockID || i11 == Block.lavaMoving.blockID || i11 == Block.lavaStill.blockID) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean handleMaterialAcceleration(AxisAlignedBB axisAlignedBB1, Material material2, Entity entity3) {
		int i4 = MathHelper.floor_double(axisAlignedBB1.minX);
		int i5 = MathHelper.floor_double(axisAlignedBB1.maxX + 1.0D);
		int i6 = MathHelper.floor_double(axisAlignedBB1.minY);
		int i7 = MathHelper.floor_double(axisAlignedBB1.maxY + 1.0D);
		int i8 = MathHelper.floor_double(axisAlignedBB1.minZ);
		int i9 = MathHelper.floor_double(axisAlignedBB1.maxZ + 1.0D);
		if(!this.checkChunksExist(i4, i6, i8, i5, i7, i9)) {
			return false;
		} else {
			boolean z10 = false;
			Vec3D vec3D11 = Vec3D.createVector(0.0D, 0.0D, 0.0D);

			for(int i12 = i4; i12 < i5; ++i12) {
				for(int i13 = i6; i13 < i7; ++i13) {
					for(int i14 = i8; i14 < i9; ++i14) {
						Block block15 = Block.blocksList[this.getBlockId(i12, i13, i14)];
						if(block15 != null && block15.blockMaterial == material2) {
							double d16 = (double)((float)(i13 + 1) - BlockFluid.getPercentAir(this.getBlockMetadata(i12, i13, i14)));
							if((double)i7 >= d16) {
								z10 = true;
								block15.velocityToAddToEntity(this, i12, i13, i14, entity3, vec3D11);
							}
						}
					}
				}
			}

			if(vec3D11.lengthVector() > 0.0D) {
				vec3D11 = vec3D11.normalize();
				double d18 = 0.014D;
				entity3.motionX += vec3D11.xCoord * d18;
				entity3.motionY += vec3D11.yCoord * d18;
				entity3.motionZ += vec3D11.zCoord * d18;
			}

			return z10;
		}
	}

	public boolean isMaterialInBB(AxisAlignedBB axisAlignedBB1, Material material2) {
		int i3 = MathHelper.floor_double(axisAlignedBB1.minX);
		int i4 = MathHelper.floor_double(axisAlignedBB1.maxX + 1.0D);
		int i5 = MathHelper.floor_double(axisAlignedBB1.minY);
		int i6 = MathHelper.floor_double(axisAlignedBB1.maxY + 1.0D);
		int i7 = MathHelper.floor_double(axisAlignedBB1.minZ);
		int i8 = MathHelper.floor_double(axisAlignedBB1.maxZ + 1.0D);

		for(int i9 = i3; i9 < i4; ++i9) {
			for(int i10 = i5; i10 < i6; ++i10) {
				for(int i11 = i7; i11 < i8; ++i11) {
					Block block12 = Block.blocksList[this.getBlockId(i9, i10, i11)];
					if(block12 != null && block12.blockMaterial == material2) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean isAABBInMaterial(AxisAlignedBB axisAlignedBB1, Material material2) {
		int i3 = MathHelper.floor_double(axisAlignedBB1.minX);
		int i4 = MathHelper.floor_double(axisAlignedBB1.maxX + 1.0D);
		int i5 = MathHelper.floor_double(axisAlignedBB1.minY);
		int i6 = MathHelper.floor_double(axisAlignedBB1.maxY + 1.0D);
		int i7 = MathHelper.floor_double(axisAlignedBB1.minZ);
		int i8 = MathHelper.floor_double(axisAlignedBB1.maxZ + 1.0D);

		for(int i9 = i3; i9 < i4; ++i9) {
			for(int i10 = i5; i10 < i6; ++i10) {
				for(int i11 = i7; i11 < i8; ++i11) {
					Block block12 = Block.blocksList[this.getBlockId(i9, i10, i11)];
					if(block12 != null && block12.blockMaterial == material2) {
						int i13 = this.getBlockMetadata(i9, i10, i11);
						double d14 = (double)(i10 + 1);
						if(i13 < 8) {
							d14 = (double)(i10 + 1) - (double)i13 / 8.0D;
						}

						if(d14 >= axisAlignedBB1.minY) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public Explosion createExplosion(Entity entity1, double d2, double d4, double d6, float f8) {
		return this.newExplosion(entity1, d2, d4, d6, f8, false);
	}
	
	public Explosion createBlockExplosion(Entity entity1, double d2, double d4, double d6, float f8, int blockID) {
		return this.newBlockExplosion(entity1, d2, d4, d6, f8, blockID, false);
	}

	public Explosion newExplosion(Entity entity1, double d2, double d4, double d6, float f8, boolean z9) {
		Explosion explosion10 = new Explosion(this, entity1, d2, d4, d6, f8);
		explosion10.isFlaming = z9;
		explosion10.doExplosion();
		explosion10.doEffects(true);
		return explosion10;
	}
	
	public Explosion newBlockExplosion(Entity entity1, double d2, double d4, double d6, float f8, int blockID, boolean z9) {
		Explosion explosion10 = new Explosion(this, entity1, d2, d4, d6, f8, blockID);
		explosion10.isFlaming = z9;
		explosion10.doExplosion();
		explosion10.doEffects(true);
		return explosion10;
	}

	public float getBlockDensity(Vec3D vec3D1, AxisAlignedBB axisAlignedBB2) {
		double d3 = 1.0D / ((axisAlignedBB2.maxX - axisAlignedBB2.minX) * 2.0D + 1.0D);
		double d5 = 1.0D / ((axisAlignedBB2.maxY - axisAlignedBB2.minY) * 2.0D + 1.0D);
		double d7 = 1.0D / ((axisAlignedBB2.maxZ - axisAlignedBB2.minZ) * 2.0D + 1.0D);
		int i9 = 0;
		int i10 = 0;

		for(float f11 = 0.0F; f11 <= 1.0F; f11 = (float)((double)f11 + d3)) {
			for(float f12 = 0.0F; f12 <= 1.0F; f12 = (float)((double)f12 + d5)) {
				for(float f13 = 0.0F; f13 <= 1.0F; f13 = (float)((double)f13 + d7)) {
					double d14 = axisAlignedBB2.minX + (axisAlignedBB2.maxX - axisAlignedBB2.minX) * (double)f11;
					double d16 = axisAlignedBB2.minY + (axisAlignedBB2.maxY - axisAlignedBB2.minY) * (double)f12;
					double d18 = axisAlignedBB2.minZ + (axisAlignedBB2.maxZ - axisAlignedBB2.minZ) * (double)f13;
					if(this.rayTraceBlocks(Vec3D.createVector(d14, d16, d18), vec3D1) == null) {
						++i9;
					}

					++i10;
				}
			}
		}

		return (float)i9 / (float)i10;
	}

	public boolean onBlockHit(EntityPlayer entityPlayer1, int i2, int i3, int i4, int i5) {
		if(i5 == 0) {
			--i3;
		}

		if(i5 == 1) {
			++i3;
		}

		if(i5 == 2) {
			--i4;
		}

		if(i5 == 3) {
			++i4;
		}

		if(i5 == 4) {
			--i2;
		}

		if(i5 == 5) {
			++i2;
		}

		if(this.getBlockId(i2, i3, i4) == Block.fire.blockID) {
			this.playAuxSFXAtEntity(entityPlayer1, 1004, i2, i3, i4, 0);
			this.setBlockWithNotify(i2, i3, i4, 0);
			return true;
		} else {
			return false;
		}
	}

	public Entity func_4085_a(Class<?> class1) {
		return null;
	}

	public String getDebugLoadedEntities() {
		return "All: " + this.loadedEntityList.size();
	}

	public String getProviderName() {
		return this.chunkProvider.makeString();
	}

	public TileEntity getBlockTileEntity(int i1, int i2, int i3) {
		Chunk chunk4 = this.getChunkFromChunkCoords(i1 >> 4, i3 >> 4);
		return chunk4 != null ? chunk4.getChunkBlockTileEntity(i1 & 15, i2, i3 & 15) : null;
	}

	public EntityBlockEntity getBlockEntity(int x, int y, int z) {
		Chunk chunk4 = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		return chunk4 != null ? chunk4.getChunkBlockEntity(x & 15, y, z & 15) : null;
	}

	public EntityBlockEntity getBlockEntityIfExists(int x, int y, int z) {
		Chunk chunk4 = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		return chunk4 != null ? chunk4.getChunkBlockEntityIfExists(x & 15, y, z & 15) : null;
	}

	public void setBlockTileEntity(int i1, int i2, int i3, TileEntity tileEntity4) {
		if(!tileEntity4.isInvalid()) {
			if(this.scanningTileEntities) {
				tileEntity4.xCoord = i1;
				tileEntity4.yCoord = i2;
				tileEntity4.zCoord = i3;
				this.entityRemoval.add(tileEntity4);
			} else {
				this.loadedTileEntityList.add(tileEntity4);
				Chunk chunk5 = this.getChunkFromChunkCoords(i1 >> 4, i3 >> 4);
				if(chunk5 != null) {
					chunk5.setChunkBlockTileEntity(i1 & 15, i2, i3 & 15, tileEntity4);
				}
			}
		}

	}
	
	public void setBlockEntity(int x, int y, int z, EntityBlockEntity entity) {
		Chunk chunk5 = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		if(chunk5 != null) {
			chunk5.setChunkBlockEntity(x & 15, y, z & 15, entity);
		}
	}

	public void removeBlockTileEntity(int i1, int i2, int i3) {
		TileEntity tileEntity4 = this.getBlockTileEntity(i1, i2, i3);
		if(tileEntity4 != null && this.scanningTileEntities) {
			tileEntity4.invalidate();
		} else {
			// TODO : THIS IS CHEESERY!
			//if(tileEntity4 != null) {
				this.loadedTileEntityList.remove(tileEntity4);
			//}

			Chunk chunk5 = this.getChunkFromChunkCoords(i1 >> 4, i3 >> 4);
			if(chunk5 != null) {
				chunk5.removeChunkBlockTileEntity(i1 & 15, i2, i3 & 15);
			}
		}

	}

	public void removeBlockEntity(int x, int y, int z) {
		Chunk chunk4 = this.getChunkFromChunkCoords(x >> 4, z >> 4);
		if(chunk4 != null) {
			chunk4.removeChunkBlockEntity(x & 15, y, z & 15);
		}
	}
	
	public boolean isBlockOpaqueCube(int i1, int i2, int i3) {
		Block block4 = Block.blocksList[this.getBlockId(i1, i2, i3)];
		return block4 == null ? false : block4.isOpaqueCube();
	}

	public boolean isBlockNormalCube(int i1, int i2, int i3) {
		Block block4 = Block.blocksList[this.getBlockId(i1, i2, i3)];
		return block4 == null ? false : block4.blockMaterial.getIsTranslucent() && block4.renderAsNormalBlock();
	}

	public void saveWorldIndirectly(IProgressUpdate iProgressUpdate1) {
		this.saveWorld(true, iProgressUpdate1);
	}

	public void calculateInitialSkylight() {
		int i1 = this.calculateSkylightSubtracted(1.0F);
		if(i1 != this.skylightSubtracted) {
			this.skylightSubtracted = i1;
		}

	}

	public void setAllowedMobSpawns(boolean z1, boolean z2) {
		this.spawnHostileMobs = z1;
		this.spawnPeacefulMobs = z2;
	}

	public void tick() {
		this.updateWeather();
		long worldTime;
		
		// No sleeping here so
		
		if(this.isAllPlayersFullyAsleep()) {
			boolean z1 = false;
			if(this.spawnHostileMobs && this.difficultySetting >= 1) {
				z1 = SpawnerAnimals.performSleepSpawning(this, this.playerEntities);
			}

			if(!z1) {
				worldTime = this.worldInfo.getWorldTime() + 24000L;
				this.worldInfo.setWorldTime(worldTime - worldTime % 24000L);
				this.wakeUpAllPlayers();
			}
		}
		
		SpawnerAnimals.performSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs);
		
		// During world moon we do this hack of a solution to increase spawning rate.
		if(this.worldInfo.isBloodMoon()) SpawnerAnimals.performSpawning(this, this.spawnHostileMobs, false);
			
		this.chunkProvider.unload100OldestChunks();
		
		int i4 = this.calculateSkylightSubtracted(1.0F);
		if(i4 != this.skylightSubtracted) {
			this.skylightSubtracted = i4;
			/*
			for(int i5 = 0; i5 < this.worldAccesses.size(); ++i5) {
				((IWorldAccess)this.worldAccesses.get(i5)).updateAllRenderers();
			}
			*/
		}

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
			for(int i5 = 0; i5 < this.worldAccesses.size(); ++i5) {
				((IWorldAccess)this.worldAccesses.get(i5)).updateAllRenderers();
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

	private void initializeWeather() {
		this.worldInfo.setRainTime(Weather.getTimeForNextRain(this.rand));
		this.worldInfo.setSnowingTime(Weather.getTimeForNextSnow(this.rand));
		this.worldInfo.setThunderTime(Weather.getTimeForNextThunder(this.rand));
	}
	
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

	protected void updateWeather() {
		if(!this.worldProvider.hasNoSky) {
			
			// Lightning bolts
			
			if(this.lastLightningBolt > 0) {
				--this.lastLightningBolt;
			}
			
			// Thunderstorm. In this version, it is independent of rainstorms.

			int i1 = this.worldInfo.getThunderTime();
			--i1;
			this.worldInfo.setThunderTime(i1);
			
			if(i1 <= 0) {
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
			
			int i3 = this.worldInfo.getSnowingTime();
			--i3;
			this.worldInfo.setSnowingTime(i3);
			
			if(i3 <= 0) {
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

			int i2 = this.worldInfo.getRainTime();
			--i2;
			this.worldInfo.setRainTime(i2);
			
			if(i2 <= 0) {
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

	private void clearWeather() {
		this.worldInfo.setRainTime(0);
		this.worldInfo.setRaining(false);
		this.worldInfo.setThunderTime(0);
		this.worldInfo.setThundering(false);
	}

	protected void updateBlocksAndPlayCaveSounds() {
		this.positionsToUpdate.clear();

		int x0;
		int z0;
		int x;
		int y;
		int z;
		int tIndex;
		int blockID;

		// First make a list of chunks to update: a square centered in *each* player
		byte radius = 8; 	// Changed 9 to 8

		for(int i1 = 0; i1 < this.playerEntities.size(); ++i1) {
			EntityPlayer entityPlayer = (EntityPlayer)this.playerEntities.get(i1);
			x0 = MathHelper.floor_double(entityPlayer.posX / 16.0D);
			z0 = MathHelper.floor_double(entityPlayer.posZ / 16.0D);

			for(x = -radius; x <= radius; ++x) {
				for(z = -radius; z <= radius; ++z) {
					if(this.chunkExists(x + x0, z + z0)) {
						this.positionsToUpdate.add(new ChunkCoordIntPair(x + x0, z + z0));
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
			x0 = chunkCoordIntPair.chunkXPos * 16;
			z0 = chunkCoordIntPair.chunkZPos * 16;

			// Get this chunk
			Chunk chunk = this.getChunkFromChunkCoords(chunkCoordIntPair.chunkXPos, chunkCoordIntPair.chunkZPos);

			// Select a dark block and play an eerie sound on closest player

			if(this.soundCounter == 0) {
				this.updateLCG = this.updateLCG * 3 + DIST_HASH_MAGIC;
				tIndex = this.updateLCG >> 2;
				x = tIndex & 15;
				z = tIndex >> 8 & 15;
				y = tIndex >> 16 & 127;
				blockID = chunk.getBlockID(x, y, z);
				x += x0;
				z += z0;
				if(blockID == 0 && this.getFullBlockLightValue(x, y, z) <= this.rand.nextInt(8) && this.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) <= 0) {
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
				x = x0 + (tIndex & 15);
				z = z0 + (tIndex >> 8 & 15);
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
						y = this.findTopSolidBlockUsingBlockMaterial(x + x0, z + z0);
						
						if(y > 0) {
							int thisBlockID = chunk.getBlockID(x, y, z);
							Block thisBlock = Block.blocksList[thisBlockID];
						
							blockID = chunk.getBlockID(x, y - 1, z);
							
							if(particleType == Weather.SNOW) { 
								// Freeze / drop snow 
								
								if(thisBlockID == 0 || thisBlockID == Block.leafPile.blockID) {
									if (Block.snow.canPlaceBlockAt(this, x + x0, y, z + z0)) {
										this.setBlockWithNotify(x + x0, y, z + z0, Block.snow.blockID);
									}
								} else if(thisBlockID == Block.snow.blockID || (thisBlock != null && thisBlock.getRenderType() == 111)) {
									int meta = chunk.getBlockMetadata(x, y, z);
									if((meta & 15) < 15) chunk.setBlockMetadata(x, y, z, meta + 1);
								}
			
							} else if (rand.nextInt(4) == 0 && (Seasons.currentSeason != Seasons.WINTER || biomegenbase.weather != Weather.cold)) {
								// Unfreeze / remove snow 
								
								if (thisBlockID == Block.snow.blockID) {
									//this.setBlockWithNotify(x + x0, y, z + z0, 0);
									chunk.setBlockID(x, y, z, 0);
								}
			
							}
						}
					}
				}
			}

			// Tick X random blocks
			for(int i = 0; i < World.blocksToTickPerFrame; ++i) {
				this.updateLCG = this.updateLCG * 3 + this.DIST_HASH_MAGIC;
				tIndex = this.updateLCG >> 2;
				x = tIndex & 15;
				z = tIndex >> 8 & 15;
				y = tIndex >> 16 & 127;
				blockID = (int) chunk.blocks[x << 11 | z << 7 | y] & 0xff; 
				if(Block.tickOnLoad[blockID]) {
					Block.blocksList[blockID].updateTick(this, x + x0, y, z + z0, this.rand);
				}
			}
		}

	}

	public boolean TickUpdates(boolean z1) {
		int i2 = this.scheduledTickTreeSet.size();
		if(i2 != this.scheduledTickSet.size()) {
			throw new IllegalStateException("TickNextTick list out of synch");
		} else {
			if(i2 > 1000) {
				i2 = 1000;
			}

			for(int i3 = 0; i3 < i2; ++i3) {
				NextTickListEntry nextTickListEntry4 = (NextTickListEntry)this.scheduledTickTreeSet.first();
				if(!z1 && nextTickListEntry4.scheduledTime > this.worldInfo.getWorldTime()) {
					break;
				}

				this.scheduledTickTreeSet.remove(nextTickListEntry4);
				this.scheduledTickSet.remove(nextTickListEntry4);
				byte b5 = 8;
				if(this.checkChunksExist(nextTickListEntry4.xCoord - b5, nextTickListEntry4.yCoord - b5, nextTickListEntry4.zCoord - b5, nextTickListEntry4.xCoord + b5, nextTickListEntry4.yCoord + b5, nextTickListEntry4.zCoord + b5)) {
					int i6 = this.getBlockId(nextTickListEntry4.xCoord, nextTickListEntry4.yCoord, nextTickListEntry4.zCoord);
					if(i6 == nextTickListEntry4.blockID && i6 > 0) {
						Block.blocksList[i6].updateTick(this, nextTickListEntry4.xCoord, nextTickListEntry4.yCoord, nextTickListEntry4.zCoord, this.rand);
					}
				}
			}

			return this.scheduledTickTreeSet.size() != 0;
		}
	}

	public void randomDisplayUpdates(int i1, int i2, int i3) {
		byte b4 = 16;
		Random random5 = new Random();

		for(int i6 = 0; i6 < 1000; ++i6) {
			int i7 = i1 + this.rand.nextInt(b4) - this.rand.nextInt(b4);
			int i8 = i2 + this.rand.nextInt(b4) - this.rand.nextInt(b4);
			int i9 = i3 + this.rand.nextInt(b4) - this.rand.nextInt(b4);
			int i10 = this.getBlockId(i7, i8, i9);
			Block block = Block.blocksList[i10];
			if(block != null) {
				block.randomDisplayTick(this, i7, i8, i9, random5);
			}
		}

	}

	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entity1, AxisAlignedBB axisAlignedBB2) {
		this.entitiesWithinAABBExcludingEntity.clear();
		int i3 = MathHelper.floor_double((axisAlignedBB2.minX - 2.0D) / 16.0D);
		int i4 = MathHelper.floor_double((axisAlignedBB2.maxX + 2.0D) / 16.0D);
		int i5 = MathHelper.floor_double((axisAlignedBB2.minZ - 2.0D) / 16.0D);
		int i6 = MathHelper.floor_double((axisAlignedBB2.maxZ + 2.0D) / 16.0D);

		for(int i7 = i3; i7 <= i4; ++i7) {
			for(int i8 = i5; i8 <= i6; ++i8) {
				if(this.chunkExists(i7, i8)) {
					this.getChunkFromChunkCoords(i7, i8).getEntitiesWithinAABBForEntity(entity1, axisAlignedBB2, this.entitiesWithinAABBExcludingEntity);
				}
			}
		}

		return this.entitiesWithinAABBExcludingEntity;
	}

	public List<Entity> getEntitiesWithinAABB(Class<?> class1, AxisAlignedBB axisAlignedBB2) {
		int i3 = MathHelper.floor_double((axisAlignedBB2.minX - 2.0D) / 16.0D);
		int i4 = MathHelper.floor_double((axisAlignedBB2.maxX + 2.0D) / 16.0D);
		int i5 = MathHelper.floor_double((axisAlignedBB2.minZ - 2.0D) / 16.0D);
		int i6 = MathHelper.floor_double((axisAlignedBB2.maxZ + 2.0D) / 16.0D);
		ArrayList<Entity> arrayList7 = new ArrayList<Entity>();

		for(int i8 = i3; i8 <= i4; ++i8) {
			for(int i9 = i5; i9 <= i6; ++i9) {
				if(this.chunkExists(i8, i9)) {
					this.getChunkFromChunkCoords(i8, i9).getEntitiesOfTypeWithinAAAB(class1, axisAlignedBB2, arrayList7);
				}
			}
		}

		return arrayList7;
	}

	public Entity findNearestEntityWithinAABB(Class<?> class1, AxisAlignedBB axisAlignedBB2, Entity entity3) {
		List<Entity> list4 = this.getEntitiesWithinAABB(class1, axisAlignedBB2);
		Entity entity5 = null;
		double d6 = Double.MAX_VALUE;
		Iterator<Entity> iterator8 = list4.iterator();

		while(iterator8.hasNext()) {
			Entity entity9 = (Entity)iterator8.next();
			if(entity9 != entity3) {
				double d10 = entity3.getDistanceSqToEntity(entity9);
				if(d10 <= d6) {
					entity5 = entity9;
					d6 = d10;
				}
			}
		}

		return entity5;
	}

	public List<Entity> getLoadedEntityList() {
		return this.loadedEntityList;
	}

	public void updateTileEntityChunkAndDoNothing(int i1, int i2, int i3, TileEntity tileEntity4) {
		if(this.blockExists(i1, i2, i3)) {
			this.getChunkFromBlockCoords(i1, i3).setChunkModified();
		}

		for(int i5 = 0; i5 < this.worldAccesses.size(); ++i5) {
			((IWorldAccess)this.worldAccesses.get(i5)).doNothingWithTileEntity(i1, i2, i3, tileEntity4);
		}

	}

	public int countEntities(Class<?> class1) {
		int i2 = 0;

		for(int i3 = 0; i3 < this.loadedEntityList.size(); ++i3) {
			Entity entity4 = (Entity)this.loadedEntityList.get(i3);
			if(class1.isAssignableFrom(entity4.getClass())) {
				++i2;
			}
		}

		return i2;
	}

	public void addLoadedEntities(List<Entity> list1) {
		this.loadedEntityList.addAll(list1);

		for(int i2 = 0; i2 < list1.size(); ++i2) {
			this.obtainEntitySkin((Entity)list1.get(i2));
		}

	}

	public void unloadEntities(List<Entity> list1) {
		this.unloadedEntityList.addAll(list1);
	}

	public void dropOldChunks() {
		while(this.chunkProvider.unload100OldestChunks()) {
		}

	}

	public boolean canBlockBePlacedAt(int blockID, int x, int y, int z, boolean z5, int side) {
		int i7 = this.getBlockId(x, y, z);
		Block existingBlock = Block.blocksList[i7];
		Block block9 = Block.blocksList[blockID];
		AxisAlignedBB axisAlignedBB10 = block9.getCollisionBoundingBoxFromPool(this, x, y, z);
		if(z5) {
			axisAlignedBB10 = null;
		}

		if(axisAlignedBB10 != null && !this.checkIfAABBIsClear(axisAlignedBB10)) {
			return false;
		} else {
			if (existingBlock != null) {
				if(existingBlock == Block.waterMoving || existingBlock == Block.waterStill || existingBlock == Block.lavaMoving || existingBlock == Block.lavaStill || existingBlock == Block.fire || existingBlock == Block.snow || existingBlock == Block.layeredSand || (existingBlock instanceof BlockFlower) || existingBlock.blockMaterial.getIsGroundCover()) {
					existingBlock = null;
				}
			}

			return blockID > 0 && existingBlock == null && block9.canPlaceBlockOnSide(this, x, y, z, side);
		}
	}

	public boolean canBlockBePlacedAt(int blockID, int x, int y, int z, boolean z5, int side, ItemStack itemStack) {
		int i7 = this.getBlockId(x, y, z);
		Block existingBlock = Block.blocksList[i7];
		Block block9 = Block.blocksList[blockID];
		AxisAlignedBB axisAlignedBB10 = block9.getCollisionBoundingBoxFromPool(this, x, y, z);
		if(z5) {
			axisAlignedBB10 = null;
		}

		if(axisAlignedBB10 != null && !this.checkIfAABBIsClear(axisAlignedBB10)) {
			return false;
		} else {
			
			if (existingBlock != null) {
				if(existingBlock == Block.waterMoving || existingBlock == Block.waterStill || existingBlock == Block.lavaMoving || existingBlock == Block.lavaStill || existingBlock == Block.fire || existingBlock == Block.snow || existingBlock == Block.layeredSand || (existingBlock instanceof BlockFlower) || existingBlock.blockMaterial.getIsGroundCover()) {
					existingBlock = null;
				}
			}

			return blockID > 0 && existingBlock == null && block9.canPlaceBlockOnSide(this, x, y, z, side, itemStack);
		}
	}
	
	public PathEntity getPathToEntity(Entity entity1, Entity entity2, float f3) {
		int i4 = MathHelper.floor_double(entity1.posX);
		int i5 = MathHelper.floor_double(entity1.posY);
		int i6 = MathHelper.floor_double(entity1.posZ);
		int i7 = (int)(f3 + 16.0F);
		int i8 = i4 - i7;
		int i9 = i5 - i7;
		int i10 = i6 - i7;
		int i11 = i4 + i7;
		int i12 = i5 + i7;
		int i13 = i6 + i7;
		ChunkCache chunkCache14 = new ChunkCache(this, i8, i9, i10, i11, i12, i13);
		return (new Pathfinder(chunkCache14)).createEntityPathTo(entity1, entity2, f3);
	}
	
	public PathEntity getPathEntityToEntity(Entity par1Entity, Entity par2Entity, float par3, boolean par4, boolean par5, boolean par6, boolean par7) {
		int var8 = MathHelper.floor_double(par1Entity.posX);
		int var9 = MathHelper.floor_double(par1Entity.posY + 1.0D);
		int var10 = MathHelper.floor_double(par1Entity.posZ);
		int var11 = (int)(par3 + 16.0F);
		int var12 = var8 - var11;
		int var13 = var9 - var11;
		int var14 = var10 - var11;
		int var15 = var8 + var11;
		int var16 = var9 + var11;
		int var17 = var10 + var11;
		ChunkCache var18 = new ChunkCache(this, var12, var13, var14, var15, var16, var17);
		PathEntity var19 = (new PathfinderRelease(var18, par4, par5, par6, par7)).createEntityPathTo(par1Entity, par2Entity, par3);
		return var19;
	}

	public PathEntity getEntityPathToXYZ(Entity entity1, int i2, int i3, int i4, float f5) {
		int i6 = MathHelper.floor_double(entity1.posX);
		int i7 = MathHelper.floor_double(entity1.posY);
		int i8 = MathHelper.floor_double(entity1.posZ);
		int i9 = (int)(f5 + 8.0F);
		int i10 = i6 - i9;
		int i11 = i7 - i9;
		int i12 = i8 - i9;
		int i13 = i6 + i9;
		int i14 = i7 + i9;
		int i15 = i8 + i9;
		ChunkCache chunkCache16 = new ChunkCache(this, i10, i11, i12, i13, i14, i15);
		return (new Pathfinder(chunkCache16)).createEntityPathTo(entity1, i2, i3, i4, f5);
	}
	
	public PathEntity getEntityPathToXYZ(Entity par1Entity, int par2, int par3, int par4, float par5, boolean par6, boolean par7, boolean par8, boolean par9) {
		int var10 = MathHelper.floor_double(par1Entity.posX);
		int var11 = MathHelper.floor_double(par1Entity.posY);
		int var12 = MathHelper.floor_double(par1Entity.posZ);
		int var13 = (int)(par5 + 8.0F);
		int var14 = var10 - var13;
		int var15 = var11 - var13;
		int var16 = var12 - var13;
		int var17 = var10 + var13;
		int var18 = var11 + var13;
		int var19 = var12 + var13;
		ChunkCache var20 = new ChunkCache(this, var14, var15, var16, var17, var18, var19);
		PathEntity var21 = (new PathfinderRelease(var20, par6, par7, par8, par9)).createEntityPathTo(par1Entity, par2, par3, par4, par5);
		return var21;
	}

	public boolean isBlockProvidingPowerTo(int i1, int i2, int i3, int i4) {
		int i5 = this.getBlockId(i1, i2, i3);
		return i5 == 0 ? false : Block.blocksList[i5].isIndirectlyPoweringTo(this, i1, i2, i3, i4);
	}

	public boolean isBlockGettingPowered(int i1, int i2, int i3) {
		return this.isBlockProvidingPowerTo(i1, i2 - 1, i3, 0) ? true :
			(this.isBlockProvidingPowerTo(i1, i2 + 1, i3, 1) ? true : 
				(this.isBlockProvidingPowerTo(i1, i2, i3 - 1, 2) ? true :
					(this.isBlockProvidingPowerTo(i1, i2, i3 + 1, 3) ? true : 
						(this.isBlockProvidingPowerTo(i1 - 1, i2, i3, 4) ? true : 
							this.isBlockProvidingPowerTo(i1 + 1, i2, i3, 5)))));
	}

	public boolean isBlockIndirectlyProvidingPowerTo(int i1, int i2, int i3, int i4) {
		
		// Block here is providing power if a) is a normal cube getting powered directly, or
		// Block here is powering.
		
		if(this.isBlockNormalCube(i1, i2, i3)) {
			return this.isBlockGettingPowered(i1, i2, i3);
		} else {
			int i5 = this.getBlockId(i1, i2, i3);
			Block block = Block.blocksList[i5];
			return block == null ? false : block.isPoweringTo(this, i1, i2, i3, i4);
		}
	}

	public boolean isBlockIndirectlyGettingPowered(int i1, int i2, int i3) {
		// This block is indirectly getting powered if any of the surrounding 6 blocks is indirectly providing power
		return this.isBlockIndirectlyProvidingPowerTo(i1, i2 - 1, i3, 0) ? true : 
			(this.isBlockIndirectlyProvidingPowerTo(i1, i2 + 1, i3, 1) ? true : 
				(this.isBlockIndirectlyProvidingPowerTo(i1, i2, i3 - 1, 2) ? true : 
					(this.isBlockIndirectlyProvidingPowerTo(i1, i2, i3 + 1, 3) ? true : 
						(this.isBlockIndirectlyProvidingPowerTo(i1 - 1, i2, i3, 4) ? true :
							this.isBlockIndirectlyProvidingPowerTo(i1 + 1, i2, i3, 5)))));
	}

	public EntityPlayer getClosestPlayerToEntity(Entity entity1, double d2) {
		return this.getClosestPlayer(entity1.posX, entity1.posY, entity1.posZ, d2);
	}

	public EntityPlayer getClosestPlayer(double d1, double d3, double d5, double d7) {
		double d9 = -1.0D;
		EntityPlayer entityPlayer11 = null;

		for(int i12 = 0; i12 < this.playerEntities.size(); ++i12) {
			EntityPlayer entityPlayer13 = (EntityPlayer)this.playerEntities.get(i12);
			double d14 = entityPlayer13.getDistanceSq(d1, d3, d5);
			if((d7 < 0.0D || d14 < d7 * d7) && (d9 == -1.0D || d14 < d9)) {
				d9 = d14;
				entityPlayer11 = entityPlayer13;
			}
		}

		return entityPlayer11;
	}
	
	public List<EntityPlayer> getPlayersInRangeFromEntity(Entity entity, double range) {
		return this.getPlayersInRangeFrom(entity.posX, entity.posY, entity.posZ, range);
	}
	
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
	
	public EntityPlayer getClosestPlayerHorizontal(double d1, double d3, double d5) {
		double d7 = -1.0D;
		EntityPlayer entityPlayer9 = null;

		for(int i10 = 0; i10 < this.playerEntities.size(); ++i10) {
			EntityPlayer entityPlayer11 = (EntityPlayer)this.playerEntities.get(i10);
			double d12 = entityPlayer11.getDistanceSq(d1, entityPlayer11.posY, d3);
			if((d5 < 0.0D || d12 < d5 * d5) && (d7 == -1.0D || d12 < d7)) {
				d7 = d12;
				entityPlayer9 = entityPlayer11;
			}
		}

		return entityPlayer9;
	}

	public EntityPlayer getPlayerEntityByName(String string1) {
		for(int i2 = 0; i2 < this.playerEntities.size(); ++i2) {
			if(string1.equals(((EntityPlayer)this.playerEntities.get(i2)).username)) {
				return (EntityPlayer)this.playerEntities.get(i2);
			}
		}

		return null;
	}
	
	public byte[] getChunkData(int i1, int i2, int i3, int i4, int i5, int i6) {
		byte[] b7 = new byte[i4 * i5 * i6 * 3];
		int i8 = i1 >> 4;
		int i9 = i3 >> 4;
		int i10 = i1 + i4 - 1 >> 4;
		int i11 = i3 + i6 - 1 >> 4;
		int i12 = 0;
		int i13 = i2;
		int i14 = i2 + i5;
		if(i2 < 0) {
			i13 = 0;
		}

		if(i14 > 128) {
			i14 = 128;
		}

		for(int i15 = i8; i15 <= i10; ++i15) {
			int i16 = i1 - i15 * 16;
			int i17 = i1 + i4 - i15 * 16;
			if(i16 < 0) {
				i16 = 0;
			}

			if(i17 > 16) {
				i17 = 16;
			}

			for(int i18 = i9; i18 <= i11; ++i18) {
				int i19 = i3 - i18 * 16;
				int i20 = i3 + i6 - i18 * 16;
				if(i19 < 0) {
					i19 = 0;
				}

				if(i20 > 16) {
					i20 = 16;
				}

				i12 = this.getChunkFromChunkCoords(i15, i18).getChunkData(b7, i16, i13, i19, i17, i14, i20, i12);
			}
		}

		return b7;
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

			if(y2 > 128) {
				y2 = 128;
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

	public void sendQuittingDisconnectingPacket() {
	}

	public void checkSessionLock() {
		this.saveHandler.checkSessionLock();
	}

	public void setWorldTime(long j1) {
		this.worldInfo.setWorldTime(j1);
	}

	public void s_func_32005_b(long j1) {
		long j3 = j1 - this.worldInfo.getWorldTime();

		NextTickListEntry nextTickListEntry6;
		for(Iterator<NextTickListEntry> iterator5 = this.scheduledTickSet.iterator(); iterator5.hasNext(); nextTickListEntry6.scheduledTime += j3) {
			nextTickListEntry6 = (NextTickListEntry)iterator5.next();
		}

		this.setWorldTime(j1);
	}

	public long getRandomSeed() {
		return this.worldInfo.getRandomSeed();
	}

	public long getWorldTime() {
		return this.worldInfo.getWorldTime();
	}

	public ChunkCoordinates getSpawnPoint() {
		return new ChunkCoordinates(this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());
	}

	public void setSpawnPoint(ChunkCoordinates chunkCoordinates1) {
		this.worldInfo.setSpawn(chunkCoordinates1.posX, chunkCoordinates1.posY, chunkCoordinates1.posZ);
	}

	public void joinEntityInSurroundings(Entity entity1) {
		int i2 = MathHelper.floor_double(entity1.posX / 16.0D);
		int i3 = MathHelper.floor_double(entity1.posZ / 16.0D);
		byte b4 = 2;

		for(int i5 = i2 - b4; i5 <= i2 + b4; ++i5) {
			for(int i6 = i3 - b4; i6 <= i3 + b4; ++i6) {
				this.getChunkFromChunkCoords(i5, i6);
			}
		}

		if(!this.loadedEntityList.contains(entity1)) {
			this.loadedEntityList.add(entity1);
		}

	}

	public boolean canMineBlock(EntityPlayer entityPlayer1, int i2, int i3, int i4) {
		return true;
	}

	public void setEntityState(Entity entity1, byte b2) {
	}

	public void updateEntityList() {
		this.loadedEntityList.removeAll(this.unloadedEntityList);

		int i1;
		Entity entity2;
		int i3;
		int i4;
		for(i1 = 0; i1 < this.unloadedEntityList.size(); ++i1) {
			entity2 = (Entity)this.unloadedEntityList.get(i1);
			i3 = entity2.chunkCoordX;
			i4 = entity2.chunkCoordZ;
			if(entity2.addedToChunk && this.chunkExists(i3, i4)) {
				this.getChunkFromChunkCoords(i3, i4).removeEntity(entity2);
			}
		}

		for(i1 = 0; i1 < this.unloadedEntityList.size(); ++i1) {
			this.releaseEntitySkin((Entity)this.unloadedEntityList.get(i1));
		}

		this.unloadedEntityList.clear();

		for(i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
			entity2 = (Entity)this.loadedEntityList.get(i1);
			if(entity2.ridingEntity != null) {
				if(!entity2.ridingEntity.isDead && entity2.ridingEntity.riddenByEntity == entity2) {
					continue;
				}

				entity2.ridingEntity.riddenByEntity = null;
				entity2.ridingEntity = null;
			}

			if(entity2.isDead) {
				i3 = entity2.chunkCoordX;
				i4 = entity2.chunkCoordZ;
				if(entity2.addedToChunk && this.chunkExists(i3, i4)) {
					this.getChunkFromChunkCoords(i3, i4).removeEntity(entity2);
				}

				this.loadedEntityList.remove(i1--);
				this.releaseEntitySkin(entity2);
			}
		}

	}

	public IChunkProvider getIChunkProvider() {
		return this.chunkProvider;
	}

	public void playNoteAt(int i1, int i2, int i3, int i4, int i5) {
		int i6 = this.getBlockId(i1, i2, i3);
		if(i6 > 0) {
			Block.blocksList[i6].playBlock(this, i1, i2, i3, i4, i5);
		}

	}

	public ISaveHandler getWorldFile() {
		return this.saveHandler;
	}

	public WorldInfo getWorldInfo() {
		return this.worldInfo;
	}

	public void updateAllPlayersSleepingFlag() {
		this.allPlayersSleeping = !this.playerEntities.isEmpty();
		Iterator<EntityPlayer> iterator1 = this.playerEntities.iterator();

		while(iterator1.hasNext()) {
			EntityPlayer entityPlayer2 = (EntityPlayer)iterator1.next();
			if(!entityPlayer2.isPlayerSleeping()) {
				this.allPlayersSleeping = false;
				break;
			}
		}

	}

	protected void wakeUpAllPlayers() {
		this.allPlayersSleeping = false;
		Iterator<EntityPlayer> iterator1 = this.playerEntities.iterator();

		while(iterator1.hasNext()) {
			EntityPlayer entityPlayer2 = (EntityPlayer)iterator1.next();
			if(entityPlayer2.isPlayerSleeping()) {
				entityPlayer2.wakeUpPlayer(false, false, true);
			}
		}

		this.clearWeather();
	}

	public boolean isAllPlayersFullyAsleep() {
		if(this.allPlayersSleeping && !this.isRemote) {
			Iterator<EntityPlayer> iterator1 = this.playerEntities.iterator();

			EntityPlayer entityPlayer2;
			do {
				if(!iterator1.hasNext()) {
					return true;
				}

				entityPlayer2 = (EntityPlayer)iterator1.next();
			} while(entityPlayer2.isPlayerFullyAsleep());

			return false;
		} else {
			return false;
		}
	}

	public float getWeightedThunderStrength(float par1) {
		// I want thunders without rain!
		return (this.prevThunderingStrength + (this.thunderingStrength - this.prevThunderingStrength) * par1) /* * this.getRainStrength(par1)*/ ;
	}

	public float getRainStrength(float f1) {
		return this.prevRainingStrength + (this.rainingStrength - this.prevRainingStrength) * f1;
	}
	
	public float getSnowStrength(float par1) {
		return this.prevSnowingStrength + (this.snowingStrength - this.prevSnowingStrength) * par1;
	}

	public void setRainStrength(float f1) {
		this.prevRainingStrength = f1;
		this.rainingStrength = f1;
	}
	
	public void setSnowingStrength(float f1) {
		this.prevSnowingStrength = f1;
		this.snowingStrength = f1;
	}
	
	public void setThunderingStrength(float f1) {
		this.prevThunderingStrength = f1;
		this.thunderingStrength = f1;
	}

	public boolean thundering() {
		return (double)this.getWeightedThunderStrength(1.0F) > 0.9D;
	}

	public boolean raining() {
		return (double)this.getRainStrength(1.0F) > 0.2D;
	}
	
	public boolean snowing() {
		return (double)this.getSnowStrength(1.0F) > 0.2D;
	}


	public boolean canBlockBeRainedOn(int i1, int i2, int i3) {
		if(!this.raining()) {
			return false;
		} else if(!this.canBlockSeeTheSky(i1, i2, i3)) {
			return false;
		} else if(this.findTopSolidBlockUsingBlockMaterial(i1, i3) > i2) {
			return false;
		} else {
			BiomeGenBase biomeGenBase4 = this.getWorldChunkManager().getBiomeGenAt(i1, i3);
			return biomeGenBase4.getEnableSnow() ? false : biomeGenBase4.canSpawnLightningBolt();
		}
	}
	
	public boolean canBlockBeRainedOnForBolts(int i1, int i2, int i3) {
		if(!this.canBlockSeeTheSky(i1, i2, i3)) {
			return false;
		} else if(this.findTopSolidBlockUsingBlockMaterial(i1, i3) > i2) {
			return false;
		} else {
			BiomeGenBase biomeGenBase4 = this.getWorldChunkManager().getBiomeGenAt(i1, i3);
			return biomeGenBase4.getEnableSnow() ? false : biomeGenBase4.canSpawnLightningBolt();
		}
	}

	public void setItemData(String string1, MapDataBase mapDataBase2) {
		this.mapStorage.setData(string1, mapDataBase2);
	}

	public MapDataBase loadItemData(Class<?> class1, String string2) {
		return this.mapStorage.loadData(class1, string2);
	}

	public int getUniqueDataId(String string1) {
		return this.mapStorage.getUniqueDataId(string1);
	}

	public void playAuxSFX(int i1, int i2, int i3, int i4, int i5) {
		this.playAuxSFXAtEntity((EntityPlayer)null, i1, i2, i3, i4, i5);
	}

	public void playAuxSFXAtEntity(EntityPlayer entityPlayer1, int i2, int i3, int i4, int i5, int i6) {
		for(int i7 = 0; i7 < this.worldAccesses.size(); ++i7) {
			((IWorldAccess)this.worldAccesses.get(i7)).playAuxSFX(entityPlayer1, i2, i3, i4, i5, i6);
		}

	}

	public BiomeGenBase getBiomeGenAt(int x, int z) {
		return this.getChunkFromChunkCoords(x >> 4, z >> 4).getBiomeGenAt(x & 15, z & 15);
	}
	
	public IWorldAccess getWorldAccess(int i) {
		return this.worldAccesses.get(i);
	}
	
	public void commandSetSnow() {
		this.worldInfo.setSnowingTime(0);
	}
	
	public void commandSetRain() {
		this.worldInfo.setRainTime(0);
	}
	
	public void commandSetThunder() {
		this.worldInfo.setThunderTime(0);
	}

	public boolean isUnderLeaves(int x, int y, int z) {
		for(int i = 0; i < 16 && y < 128; i ++) {
			if(this.getBlockId(x, y, z) == Block.leaves.blockID && this.getBlockMetadata(x, y, z) == 7) return true;
			y ++;
		}
		return false;
	}

	public BlockState getBlockStateAt(int x0, int y0, int z0) {
		return new BlockState(this.getBlockId(x0, y0, z0), this.getBlockMetadata(x0, y0, z0), x0, y0, z0);
	}

	public Block getBlock(int x, int y, int z) {
		return Block.blocksList[this.getBlockId(x, y, z)];
	}

	public void setBlockAndMetadata(int x, int y, int z, BlockState blockState) {
		this.setBlockAndMetadata(x, y, z, blockState.getBlock().blockID, blockState.getMetadata());
	}
	
	// Future: override those in WorldServer with the right thing?
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

	public EntityPlayer getPlayerForUsername(String var1) {
		if(var1 == null) return null;
		for(int i = 0; i < this.playerEntities.size(); i ++) {
			EntityPlayer player = playerEntities.get(i);
			if(var1.equals(player.username)) return player;
		}
		return null;
	}

	public String[] getAllUsernames() {
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i < this.playerEntities.size(); i ++) {
			list.add(this.playerEntities.get(i).username);
		}
		return (String[]) list.toArray();
	}

	public int getWorldHeight() {
		return 128;
	}
}

