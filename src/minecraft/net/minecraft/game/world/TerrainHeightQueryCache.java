package net.minecraft.game.world;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;

/**
 * Bounded LRU cache for the height-only chunk queries ({@link World#getLandSurfaceHeightValue},
 * {@link World#isOceanChunk}, {@link World#isUrbanChunk}, {@link World#justGenerateForHeight}).
 *
 * <p>The world's height queries regenerate an entire chunk's terrain (density lattice + block
 * writes) on every miss, and the hot callers (biome populators, feature placement, moss growth)
 * repeatedly query the <em>same</em> not-yet-generated neighbour chunk. This cache memoises the
 * lightweight result — the 256-byte land-surface height map plus the {@code isOcean} /
 * {@code isUrbanChunk} flags — instead of the full 64 KB block buffers, so repeated queries cost
 * ~350 bytes worth of state and one hash lookup instead of a full terrain generation.
 *
 * <p><b>Consistency.</b> No invalidation is ever required:
 * <ul>
 *   <li>the height-only result is a pure function of the (seed, chunk coords) generation, so a
 *       stored value always equals a fresh miss's value;</li>
 *   <li>{@code World}'s four facades short-circuit to the live chunk the moment it is generated,
 *       so a stale entry is never served;</li>
 *   <li>if a chunk later unloads, the entry still answers the base pre-population height these
 *       queries are defined to answer — identical to what a fresh regeneration would compute.</li>
 * </ul>
 * The entry is dropped voluntarily in the chunk-unload path (tidy) and by LRU eviction otherwise.
 *
 * <p>Bounded by {@link #DEFAULT_CAPACITY} entries ({@code < 100 KB}); keys are the packed
 * {@link ChunkCoordIntPair#chunkXZ2Int} integers already used throughout the codebase.
 */
public final class TerrainHeightQueryCache {

	/** Recommended default capacity: plenty for one populate pass's neighbour queries, ~90 KB worst case. */
	public static final int DEFAULT_CAPACITY = 256;

	/** The maximum number of entries kept before the least-recently-used one is evicted. */
	private final int capacity;

	/** LRU map from {@link ChunkCoordIntPair#chunkXZ2Int} packed key to cached height result. */
	private final LinkedHashMap<Integer, TerrainHeightEntry> cache;

	/** Number of queries answered from the cache (diagnostics). */
	private long cacheHits;

	/** Number of queries that missed the cache and had to compute a fresh height result (diagnostics). */
	private long cacheMisses;

	/**
	 * Creates a cache with the default capacity.
	 */
	public TerrainHeightQueryCache() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Creates a cache with the given maximum number of entries.
	 *
	 * @param capacity the maximum number of chunk results to retain before LRU eviction kicks in
	 */
	public TerrainHeightQueryCache(int capacity) {
		this.capacity = capacity;
		this.cache = new LinkedHashMap<Integer, TerrainHeightEntry>(16, 0.75F, true) {
			private static final long serialVersionUID = 6326162487140364900L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<Integer, TerrainHeightEntry> eldest) {
				return this.size() > TerrainHeightQueryCache.this.capacity;
			}
		};
	}

	/**
	 * Returns the cached height result for a chunk, computing it via
	 * {@code world.chunkProvider.justGenerateForHeight} and storing it on a miss.
	 *
	 * @param chunkX chunk X coordinate
	 * @param chunkZ chunk Z coordinate
	 * @param world  the owning world (used only on a cache miss, to compute the height result)
	 * @return the height result for the chunk
	 */
	public TerrainHeightEntry getOrCompute(int chunkX, int chunkZ, World world) {
		int key = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
		TerrainHeightEntry entry = this.cache.get(key);
		if(entry != null) {
			++this.cacheHits;
			return entry;
		}

		++this.cacheMisses;
		Chunk chunk = world.chunkProvider.justGenerateForHeight(chunkX, chunkZ);
		entry = new TerrainHeightEntry(chunk);
		this.cache.put(key, entry);
		return entry;
	}

	/**
	 * Returns a chunk exposing the cached height result, computing it on a miss.
	 *
	 * <p>On a miss this returns the freshly computed height-only chunk unchanged. On a hit it
	 * synthesises a lightweight empty chunk carrying a copy of the cached payload — the same
	 * public members the height-only path exposes today ({@code landSurfaceHeightMap},
	 * {@code isOcean}, {@code isUrbanChunk}, {@code hasBuilding} = false), with no block data.
	 *
	 * @param chunkX chunk X coordinate
	 * @param chunkZ chunk Z coordinate
	 * @param world  the owning world
	 * @return a chunk answering the height query for the given coordinates
	 */
	public Chunk getOrComputeChunk(int chunkX, int chunkZ, World world) {
		int key = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
		TerrainHeightEntry entry = this.cache.get(key);
		if(entry != null) {
			++this.cacheHits;
			Chunk chunk = new Chunk(world, chunkX, chunkZ);
			chunk.landSurfaceHeightMap = Arrays.copyOf(entry.landSurfaceHeightMap, entry.landSurfaceHeightMap.length);
			chunk.isOcean = entry.isOcean;
			chunk.isUrbanChunk = entry.isUrbanChunk;
			return chunk;
		}

		++this.cacheMisses;
		Chunk chunk = world.chunkProvider.justGenerateForHeight(chunkX, chunkZ);
		this.cache.put(key, new TerrainHeightEntry(chunk));
		return chunk;
	}

	/**
	 * Drops the cached result for a chunk (used when the real chunk unloads, so the cache does not
	 * hold entries for long-gone chunks).
	 *
	 * @param chunkX chunk X coordinate
	 * @param chunkZ chunk Z coordinate
	 */
	public void remove(int chunkX, int chunkZ) {
		this.cache.remove(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
	}

	/**
	 * Drops every cached result.
	 */
	public void clear() {
		this.cache.clear();
	}

	/**
	 * @return the number of chunks currently held in the cache
	 */
	public int size() {
		return this.cache.size();
	}

	/**
	 * @return how many queries were answered from the cache (diagnostics)
	 */
	public long getCacheHits() {
		return this.cacheHits;
	}

	/**
	 * @return how many queries missed the cache and computed a fresh result (diagnostics)
	 */
	public long getCacheMisses() {
		return this.cacheMisses;
	}

	/**
	 * Immutable lightweight payload stored per cached chunk: the 256-byte land-surface height map
	 * (index {@code z << 4 | x}) plus the {@code isOcean} / {@code isUrbanChunk} flags.
	 */
	public static final class TerrainHeightEntry {

		/** The 256-byte land-surface height map, indexed {@code z << 4 | x}. */
		public final byte[] landSurfaceHeightMap;

		/** Whether the chunk's sea-level rows are entirely water (no stone column reaches y 63). */
		public final boolean isOcean;

		/** Whether the chunk is selected for city generation. */
		public final boolean isUrbanChunk;

		/**
		 * Copies the height query payload out of a freshly computed height-only chunk.
		 *
		 * @param chunk the chunk whose payload should be cached (its arrays are not aliased)
		 */
		TerrainHeightEntry(Chunk chunk) {
			this.landSurfaceHeightMap = Arrays.copyOf(chunk.landSurfaceHeightMap, chunk.landSurfaceHeightMap.length);
			this.isOcean = chunk.isOcean;
			this.isUrbanChunk = chunk.isUrbanChunk;
		}
	}
}