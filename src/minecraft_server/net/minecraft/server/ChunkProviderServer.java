package net.minecraft.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.EmptyChunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.chunk.loader.IChunkLoader;

/**
 * Chunk provider for the dedicated server. Implements IChunkProvider and wraps
 * a Minecraft world with chunk loading/generation/dropping logic.
 *
 * This provider uses a HashMap of chunk coordinates to Chunk objects, plus
 * a dummy chunk for missing regions. It delegates actual generation to
 * the injected IChunkProvider supplied by the WorldServer.
 */
public class ChunkProviderServer implements IChunkProvider {

	/** Set of chunk coords that have been dropped/unloaded. */
	private Set<Integer> droppedChunksSet = new HashSet<Integer>();

	/** A dummy chunk used when no generated chunk is available. */
	private Chunk dummyChunk;

	/** The actual IChunkProvider that generates chunks. */
	private IChunkProvider serverChunkGenerator;

	/** Loader responsible for chunk save/load from disk. */
	private IChunkLoader chunkLoader;

	/** Flag that allows server-side chunk loading to override normal logic. */
	public boolean chunkLoadOverride = false;

	/** Map from chunk XZ int to Chunk object. */
	private Map<Integer, Chunk> id2ChunkMap = new HashMap<Integer, Chunk>();

	/** List of currently loaded chunks (for iteration and save/unsave). */
	private List<Chunk> loadedChunksServer = new ArrayList<Chunk>();

	/** The server-side World this provider belongs to. */
	private WorldServer world;

	/**
	 * Initializes the chunk provider, creates a dummy chunk, and stores references
	 * to the chunk loader and generator.
	 *
	 * @param worldServer1    the WorldServer that owns this provider
	 * @param iChunkLoader2   the chunk save/load handler
	 * @param iChunkProvider3 the generator to delegate to
	 */
	public ChunkProviderServer(WorldServer worldServer1, IChunkLoader iChunkLoader2, IChunkProvider iChunkProvider3) {
		this.dummyChunk = new EmptyChunk(worldServer1, 0, 0);
		this.world = worldServer1;
		this.chunkLoader = iChunkLoader2;
		this.serverChunkGenerator = iChunkProvider3;
	}

	/** Returns true if the given chunk coordinates have been generated. */
	public boolean chunkExists(int i1, int i2) {
		return this.id2ChunkMap.containsKey(ChunkCoordIntPair.chunkXZ2Int(i1, i2));
	}

	/** Removes a chunk from the world and marks it for dropping. */
	public void dropChunk(int i1, int i2) {
		if (this.world.worldProvider.canRespawnHere()) {
			ChunkCoordinates spawn = this.world.getSpawnPoint();
			int dx = i1 * 16 + 8 - spawn.posX;
			int dz = i2 * 16 + 8 - spawn.posZ;
			int maxDist = 128;
			if (dx < -maxDist || dx > maxDist || dz < -maxDist || dz > maxDist) {
				this.droppedChunksSet.add(ChunkCoordIntPair.chunkXZ2Int(i1, i2));
			}
		} else {
			this.droppedChunksSet.add(ChunkCoordIntPair.chunkXZ2Int(i1, i2));
		}
	}

	/** Unloads and saves all currently loaded chunks. */
	public void unloadAllChunks() {
		java.util.Iterator<Chunk> iterator1 = this.loadedChunksServer.iterator();
		while (iterator1.hasNext()) {
			Chunk chunk2 = iterator1.next();
			this.dropChunk(chunk2.xPosition, chunk2.zPosition);
		}
	}

	/** Prepares a chunk by loading it from file or generating it. If the generator
	 * is null, a dummy chunk is returned. After loading, lighting is initialized
	 * and adjacent chunks are populated if needed. */
	public Chunk prepareChunk(int chunkX, int chunkZ) {
		int hash = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
		this.droppedChunksSet.remove(hash);
		Chunk chunk = (Chunk) this.id2ChunkMap.get(hash);
		if (chunk == null) {
			chunk = this.loadChunkFromFile(chunkX, chunkZ);
			if (chunk == null) {
				if (this.serverChunkGenerator == null) {
					chunk = this.dummyChunk;
				} else {
					chunk = this.serverChunkGenerator.provideChunk(chunkX, chunkZ);
				}
			}
			this.id2ChunkMap.put(hash, chunk);
			this.loadedChunksServer.add(chunk);
			if (chunk != null) {
				chunk.onChunkLoad();
				chunk.initLightingForRealNotJustHeightmap();
			}

			// Populate adjacent chunks if terrain isn't fully populated yet.
			if (!chunk.isTerrainPopulated && this.chunkExists(chunkX + 1, chunkZ + 1) && this.chunkExists(chunkX, chunkZ + 1)
					&& this.chunkExists(chunkX + 1, chunkZ)) {
				this.populate(this, chunkX, chunkZ);
			}
			if (this.chunkExists(chunkX - 1, chunkZ) && !this.provideChunk(chunkX - 1, chunkZ).isTerrainPopulated
					&& this.chunkExists(chunkX - 1, chunkZ + 1) && this.chunkExists(chunkX, chunkZ + 1)
					&& this.chunkExists(chunkX - 1, chunkZ)) {
				this.populate(this, chunkX - 1, chunkZ);
			}
			if (this.chunkExists(chunkX, chunkZ - 1) && !this.provideChunk(chunkX, chunkZ - 1).isTerrainPopulated
					&& this.chunkExists(chunkX + 1, chunkZ - 1) && this.chunkExists(chunkX, chunkZ - 1)
					&& this.chunkExists(chunkX + 1, chunkZ)) {
				this.populate(this, chunkX, chunkZ - 1);
			}
			if (this.chunkExists(chunkX - 1, chunkZ - 1) && !this.provideChunk(chunkX - 1, chunkZ - 1).isTerrainPopulated
					&& this.chunkExists(chunkX, chunkZ - 1) && this.chunkExists(chunkX - 1, chunkZ - 1)
					&& this.chunkExists(chunkX, chunkZ - 1)) {
				this.populate(this, chunkX - 1, chunkZ - 1);
			}
		}
		return chunk;
	}

	/** Generates a chunk by delegating to the injected IChunkProvider. */
	public Chunk provideChunk(int chunkX, int chunkZ) {
		Chunk result = (Chunk) this.id2ChunkMap.get(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
		return result == null ? (!this.world.findingSpawnPoint && !this.chunkLoadOverride ? this.dummyChunk : this.prepareChunk(chunkX, chunkZ)) : result;
	}

	/** Loads a chunk from disk via the chunk loader. */
	private Chunk loadChunkFromFile(int chunkX, int chunkZ) {
		if (this.chunkLoader == null) {
			return null;
		} else {
			try {
				Chunk result3 = this.chunkLoader.loadChunk(this.world, chunkX, chunkZ);
				if (result3 != null) {
					result3.lastSaveTime = this.world.getWorldTime();
				}
				return result3;
			} catch (Exception exception4) {
				exception4.printStackTrace();
				return null;
			}
		}
	}

	/** Saves extra chunk data (biomes, etc.) via the chunk loader. */
	private void saveChunkExtraData(Chunk chunk1) {
		if (this.chunkLoader != null) {
			try {
				this.chunkLoader.saveExtraChunkData(this.world, chunk1);
			} catch (Exception exception3) {
				exception3.printStackTrace();
			}
		}
	}

	/** Saves a chunk's block data to disk via the chunk loader. */
	private void saveChunkData(Chunk chunk1) {
		if (this.chunkLoader != null) {
			try {
				chunk1.lastSaveTime = this.world.getWorldTime();
				this.chunkLoader.saveChunk(this.world, chunk1);
			} catch (IOException iOException3) {
				iOException3.printStackTrace();
			}
		}
	}

	/** Runs the generator's populate step for a given chunk. */
	public void populate(IChunkProvider iChunkProvider1, int chunkX, int chunkZ) {
		Chunk chunk4 = this.provideChunk(chunkX, chunkZ);
		if (!chunk4.isTerrainPopulated) {
			chunk4.isTerrainPopulated = true;
			if (this.serverChunkGenerator != null) {
				this.serverChunkGenerator.populate(iChunkProvider1, chunkX, chunkZ);
				chunk4.setChunkModified();
			}
		}
	}

	/** Saves all chunks. Returns false if the save was aborted early. */
	public boolean saveChunks(boolean saveAll1, IProgressUpdate progress2) {
		int savedCount = 0;
		for (int i4 = 0; i4 < this.loadedChunksServer.size(); ++i4) {
			Chunk chunk5 = (Chunk) this.loadedChunksServer.get(i4);
			if (saveAll1 && !chunk5.neverSave) {
				this.saveChunkExtraData(chunk5);
			}
			if (chunk5.needsSaving(saveAll1)) {
				this.saveChunkData(chunk5);
				chunk5.isModified = false;
				++savedCount;
				if (savedCount == 24 && !saveAll1) {
					return false;
				}
			}
		}
		if (saveAll1) {
			if (this.chunkLoader == null) {
				return true;
			}
			this.chunkLoader.saveExtraData();
		}
		return true;
	}

	/** Unloads the 100 oldest chunks, saving them if needed. */
	public boolean unload100OldestChunks() {
		if (!this.world.levelSaving) {
			for (int i1 = 0; i1 < 100; ++i1) {
				if (!this.droppedChunksSet.isEmpty()) {
					Integer hash2 = (Integer) this.droppedChunksSet.iterator().next();
					Chunk chunk3 = (Chunk) this.id2ChunkMap.get(hash2);
					chunk3.onChunkUnload();
					this.saveChunkData(chunk3);
					this.saveChunkExtraData(chunk3);
					this.droppedChunksSet.remove(hash2);
					this.id2ChunkMap.remove(hash2);
					this.world.evictHeightQuery(chunk3.xPosition, chunk3.zPosition);
					this.loadedChunksServer.remove(chunk3);
				}
			}
			if (this.chunkLoader != null) {
				this.chunkLoader.chunkTick();
			}
		}
		return this.serverChunkGenerator.unload100OldestChunks();
	}

	/** Returns true if the world is not currently saving. */
	public boolean canSave() {
		return !this.world.levelSaving;
	}

	/** Debug string showing chunk counts. */
	public String makeString() {
		return "ServerChunkCache: " + this.id2ChunkMap.size() + " Drop: " + this.droppedChunksSet.size();
	}

	/** Delegates height-map generation to the underlying generator. */
	public Chunk justGenerateForHeight(int x, int z) {
		return this.serverChunkGenerator.justGenerateForHeight(x, z);
	}
}