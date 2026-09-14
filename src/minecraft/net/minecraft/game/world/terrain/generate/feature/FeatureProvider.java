package net.minecraft.game.world.terrain.generate.feature;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.BetterDungeons;
import net.minecraft.game.world.terrain.generate.feature.amazonvillage.FeatureAmazonVillage;
import net.minecraft.game.world.terrain.generate.feature.fossils.FeatureFossil;
import net.minecraft.game.world.terrain.generate.feature.icepalace.FeatureIcePalace;
import net.minecraft.game.world.terrain.generate.feature.oceanruins.FeatureOceanRuins;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.chunk.loader.IChunkLoader;
import net.minecraft.game.world.World;

/**
 * Multi-chunk feature coordinator.
 *
 * <p>Each chunk that @{link net.minecraft.game.world.terrain.ChunkProviderGenerate}
 * generates asks this class "which features live in or near me?" (and later "populate
 * the features that overlap me"). The provider remembers every feature created in the
 * current session so that a chunk that gets regenerated draws the exact same structure
 * instead of rolling a new one.
 *
 * <p>Chunk coordinate treatment: almost every long key in this class is produced by
 * {@link ChunkCoordIntPair#chunkXZ2Long(int, int)}, which packs the two chunk coordinates
 * into one {@code long} (bijectively), so the maps here double as coord-keyed lookup tables.
 */
public class FeatureProvider {
	public World world;
	public IChunkProvider chunkProvider; 
	public BetterDungeons betterDungeons;

	/**
	 * Registered feature classes, in registration order.
	 *
	 * <p>Order matters: {@link #getFeatureForChunkCoords} walks this list and hands the
	 * chunk to the FIRST feature whose spawn gate passes. Volcanos are registered first
	 * on purpose, so a rocky/volcanic biome surface picks the volcano over anything else.
	 */
	private static final List<Class<?>> registeredFeatures;
	
	/**
	 * Cached {@code (World, int, int, FeatureProvider)} constructor for every registered
	 * feature class.
	 *
	 * <p>Looked up ONCE at registration time (reflection is only paid per feature type)
	 * instead of per chunk x feature type, which is what the original code did and was a
	 * measurable hot spot while exploring (the gate loop could reflect 11 constructors
	 * for every one of the 49 cells a chunk checks).
	 */
	private static final Map<Class<?>, Constructor<? extends Feature>> featureConstructors;
	
	/*
	 * Features generated during this session, keyed by their owner (origin) chunk.
	 * Deliberately NOT size-bounded: if a chunk is regenerated the session's feature
	 * set must still be able to hand back the same feature object, otherwise newly
	 * generated terrain would randomly differ from what was visible before.
	 *
	 * A dynamic-schematic feature removes itself from this list (via removeFeature)
	 * as soon as every chunk it spans has been populated; its cell then behaves like a
	 * "no feature" cell because the spawn gate short-circuits on the fully built
	 * structure. Only features that are still drawing - schematics still live - are
	 * ever present here.
	 */
	private HashMap<Long, Feature> featureList;
	
	/*
	 * Bounded (LRU) set of chunks that already went through the population stage.
	 *
	 * The original code used an unbounded HashSet purely to stop the population stage
	 * from re-running for a chunk. The cap is high enough (16k chunks) that single
	 * player sessions never hit it, while memory stays bounded no matter how far the
	 * player explores.
	 */
	private final Map<Long, Boolean> populatedChunks;
	
	/*
	 * Negative memoization: chunk coords whose spawn gate returned "no feature".
	 *
	 * The spawn decision is a pure function of (seed, coords, world biome state), so a
	 * cell that gets "no" today will get "no" for the rest of this session. Caching that
	 * answer lets every OTHER chunk that overlaps the cell in its scan (each cell is
	 * checked by up to ~49 neighbouring chunk generations) skip the whole gate instead of
	 * re-running up to 11 Random draws + biome/height queries.
	 *
	 * LRU-bounded so an unrestricted exploration direction can't grow it without limit.
	 */
	private final Map<Long, Boolean> noFeatureChunks;
	
	/*
	 * One premade instance per registered feature type, used ONLY as a gate probe.
	 *
	 * {@code shouldSpawn} (and the default {@code shouldFeatureSpawn}) are required to be
	 * pure functions of their parameters - no instance-field assignment, no world
	 * mutation. That is what makes sharing a single probe per class safe and identical to
	 * constructing a fresh instance per evaluation (which the original code did via
	 * reflection). The probe is never the feature that ends up owning a chunk; when the
	 * probe's gate passes we construct a real feature for the actual coords.
	 */
	private final Map<Class<?>, Feature> probeFeatures;

	/** How many chunks stay worth of population-guard memory we keep (practically never evicts). */
	private static final int POPULATED_CHUNK_CACHE_CAPACITY = 16384;
	
	/** How many "no feature here" cells we remember. */
	private static final int NO_FEATURE_CHUNK_CACHE_CAPACITY = 4096;

	public FeatureProvider(World world, IChunkProvider chunkProvider, BetterDungeons betterDungeons) {
		this.world = world;
		this.chunkProvider = chunkProvider;
		this.betterDungeons = betterDungeons;
		this.featureList = new HashMap<Long, Feature> ();
		
		// accessOrder=true turns the LinkedHashMaps into LRU caches; removeEldestEntry
		// drops the least-recently-touched entry once the cap is exceeded.
		this.populatedChunks = new LinkedHashMap<Long, Boolean>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
				return this.size() > POPULATED_CHUNK_CACHE_CAPACITY;
			}
		};
		this.noFeatureChunks = new LinkedHashMap<Long, Boolean>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
				return this.size() > NO_FEATURE_CHUNK_CACHE_CAPACITY;
			}
		};
		
		// Build one probe per registered type. Probes are cheap (the constructor only
		// stores the world/provider and computes the AABB from getFeatureRadius()) and
		// the actual winner is instantiated separately when its probe passes.
		this.probeFeatures = new HashMap<Class<?>, Feature> ();
		for(Class<?> featureClass: registeredFeatures) {
			if(featureClass == null) continue;
			try {
				this.probeFeatures.put(featureClass,
						(Feature) featureConstructors.get(featureClass)
						.newInstance(new Object[] { world, Integer.valueOf(0), Integer.valueOf(0), this }));
			} catch (Exception e) {
				// A concrete-but-abstract subclass (shouldn't happen for registered ones)
				// or a missing constructor: log and skip - the gate for that class can never pass.
				System.err.println("[FeatureProvider] could not build probe for " + featureClass.getName() + ": " + e);
			}
		}
	}
	
	/**
	 * Adds a feature type to the spawn pool and caches its constructor so chunk
	 * selection never has to reflect on it again.
	 *
	 * @param featureClass a concrete {@code Feature} subclass with the standard
	 *        {@code (World, int, int, FeatureProvider)} constructor
	 */
	public static void registerFeature(Class<?> featureClass) {
		try {
			// The registered classes are Feature subclasses by contract; the cast is only
			// needed to satisfy the generic type system (featureClass is Class<?> here).
			@SuppressWarnings("unchecked")
			Constructor<? extends Feature> constructor = ((Class<? extends Feature>) featureClass).getConstructor(new Class[] {
					World.class,
					Integer.TYPE,
					Integer.TYPE,
					FeatureProvider.class});
			featureConstructors.put(featureClass, constructor);
		} catch (Exception e) {
			// Missing/private constructor: the feature can never be spawned, but keep the
			// pool consistent by still registering it (its constructor call will fail loudly later).
			System.err.println("[FeatureProvider] could not cache constructor for " + featureClass.getName() + ": " + e);
		}
		registeredFeatures.add(featureClass);
	}

	/**
	 * Selects a feature at random for chunkX, chunkZ. If successful the feature is
	 * initialized (but not yet setup) and returned.
	 *
	 * <p>Determinism: the whole selection is a function of the world seed, the chunk
	 * coordinates and the biomes known so far - the gate Random is reseeded identically
	 * on every call so the same chunk always selects the same feature.
	 */
	public Feature getFeatureForChunkCoords(int chunkX, int chunkZ) { 
		// Provide a feature.
		Feature feature = null;
		
		// Seed must be consistent. Note the multiplications are INT arithmetic and may
		// overflow - that overflow is part of the (intentionally) stable seed, so keep
		// the expression shape unchanged.
		long seed = this.world.getRandomSeed() + chunkX * 25117 + chunkZ * 151121;
		
		// Get the biome
		BiomeGenBase biome = this.world.getWorldChunkManager().getBiomeGenAt((chunkX << 4) + 8, (chunkZ << 4) + 8);
		
		// Select a feature. First feature from the list which can be spawned is selected!
		Iterator<Class<?>> featureClassIterator = registeredFeatures.iterator();
		while(featureClassIterator.hasNext()) {					
			Class<?> featureClass = featureClassIterator.next();
			if(featureClass == null) continue;
			
			// Evaluate the gate on the shared probe for this class. Because shouldSpawn
			// is a pure function of (world, seed, biome, coords) the probe's verdict is
			// byte-identical to building a fresh instance - without the allocation.
			Feature probe = this.probeFeatures.get(featureClass);
			if(probe != null && probe.shouldFeatureSpawn(this.chunkProvider, this.world, new Random(seed), biome, chunkX, chunkZ)) {
				// The probe passed: construct the REAL feature for this chunk now.
				try {
					feature = (Feature) featureConstructors.get(featureClass)
							.newInstance(new Object[] { world, Integer.valueOf(chunkX), Integer.valueOf(chunkZ), this });
					break;
				} catch (Exception e) {
					// Mirror the original behaviour: treat the failed construction as "not
					// a feature here" and keep trying the remaining classes.
					e.printStackTrace();
					feature = null;
				}
			}
		}
		
		if(feature == null) return null;
		
		// Skip re-creating a structure that is already fully built into the world.
		//
		// Every world entry constructs a fresh provider with an empty featureList, so a
		// chunk generated next to a feature whose chunks were persisted in an earlier
		// session re-runs this gate, passes (the seed is deterministic), and would
		// create a brand-new feature object - rebuilding its (potentially several-hundred
		// kilobyte) schematic from scratch and re-printing its setup, even though every
		// block the structure would draw is already in the world or on disk. Skip exactly
		// like a failed gate: the caller remembers the cell as "no feature" for this
		// session. A feature whose box is only PARTLY built (e.g. the frontier of a saved
		// world) is NOT skipped: the outstanding box chunks legitimately need the feature
		// object so the deterministic schematic rebuild completes the structure there.
		if(this.isFeatureFullyBuilt(feature)) {
			return null;
		}
		
		// Check if it's not too close to other features.
		// The separation is expressed in CHUNKS (minimumSeparation returns a chunk count),
		// so the block-space centers are shifted back to chunk coords before comparing.
		int separation = feature.minimumSeparation();
		Iterator<Feature> iterator = this.featureList.values().iterator();
		while(iterator.hasNext()) {
			Feature otherFeature = iterator.next();
			if(
					/*otherFeature.getClass() == feature.getClass() &&*/ (
						Math.abs((feature.centerX >> 4) - (otherFeature.centerX >> 4)) <= separation || 
						Math.abs((feature.centerZ >> 4) - (otherFeature.centerZ >> 4)) <= separation
					)
			) {
				return null;
			}
		}
		
		// Set this feature up (for instance creating a list of pieces or creating a logic representation of it)
		feature.setup(world, new Random(seed), biome, chunkX, chunkZ);
		
		// Return so it can be processed
		return feature;
	}
	
	/**
	 * Drops a feature from the session's feature list.
	 *
	 * <p>Called by {@link FeatureDynamicSchematic} when every chunk it spans has been
	 * populated and its schematic released. From then on the cell behaves like a "no
	 * feature" cell for the rest of the session: a later chunk generation that scans it
	 * re-runs the spawn gate, which sees the fully built structure (every box chunk
	 * final) and short-circuits - see {@link #isFeatureFullyBuilt(Feature)}.
	 *
	 * @param feature the feature to remove; ignored if it is no longer the tracked
	 *        feature for its origin chunk
	 */
	public void removeFeature(Feature feature) {
		long chunkHash = ChunkCoordIntPair.chunkXZ2Long(feature.originChunkX, feature.originChunkZ);
		if(this.featureList.get(chunkHash) == feature) {
			this.featureList.remove(chunkHash);
		}
	}
	
	/**
	 * Scans the 7x7 chunk square centered on (chunkX, chunkZ) for features that are
	 * near or overlapping this chunk and runs their generation stage.
	 *
	 * <p>The scan visits every cell exactly ONCE (ordered by ring distance, 0..3), which
	 * preserves the exact feature-creation order of the original quadruple-nested sweep:
	 * a cell appears in {@code ring = max(|dx|, |dz|)}, so skipping cells that an earlier
	 * ring already covered yields the identical first-visit sequence - and therefore the
	 * identical feature set in {@link #featureList}. Features whose origins produced "no"
	 * are remembered in {@link #noFeatureChunks} so later, overlapping chunk requests
	 * short-circuit instead of re-running the gate.
	 *
	 * @return true if any feature drew blocks into this chunk
	 */
	public boolean getNearestFeatures(int chunkX, int chunkZ, Chunk chunk) {
		boolean featureInChunk = false;
		
		HashSet<Long> featureHashes = new HashSet<Long>();
		
		// Single sweep, rings 0..3. Cell order is the same as the old loop would have
		// VISITED each cell for the FIRST time, which is all that matters for creation
		// order (re-visits could never create anything new).
		for(int i = 0; i <= 3; i ++) {
			for(int x = chunkX - i; x <= chunkX + i; x ++) {
				for(int z = chunkZ - i; z <= chunkZ + i; z ++) {
					// Skip cells that appear on an inner ring - they were handled already.
					int ring = Math.max(Math.abs(x - chunkX), Math.abs(z - chunkZ));
					if(ring < i) continue;
					
					Feature feature = null;
					
					// First check already calculated features
					long chunkHash = ChunkCoordIntPair.chunkXZ2Long(x, z);
					feature = this.featureList.get(chunkHash); 
					
					// Skip cells we already proved have no feature - the verdict is stable
					// for the whole session, so this is a pure cache hit.
					if(feature == null && this.noFeatureChunks.containsKey(chunkHash)) {
						continue;
					}
					
					// Not found, get a new (possibly)
					if(feature == null) {
						feature = this.getFeatureForChunkCoords(x, z);
						
						if(feature != null) {
							// If we got a feature for this chunk, store
							this.featureList.put(chunkHash, feature);	
							
						} else {
							// Remember the negative answer so other chunks don't re-ask.
							this.noFeatureChunks.put(chunkHash, Boolean.TRUE);
						}
					}
					
					if(feature != null && feature.getFeatureRadius() >= i) {
						featureHashes.add(chunkHash);
					}
				}
			}
		}
		
		// Generation phase. Iteration order over the HashSet is deterministic for a given
		// set of entries + insertion sequence, both of which we preserved above, so the
		// generation order is unchanged from the original two-phase scan.
		Iterator<Long> iterator = featureHashes.iterator();
		while(iterator.hasNext()) {
			Feature feature = this.featureList.get(iterator.next());
			
			feature.generate(chunkX, chunkZ, chunk);
			featureInChunk = true;
		}
		
		return featureInChunk;
	}

	/* 
	 * Populates structures for this chunk. Called once per chunk (guarded by the LRU
	 * populatedChunks set). Each overlapping feature is told how to draw its stretch of
	 * this chunk, and afterwards it is notified that one more of its chunks is done so
	 * it can free its (potentially several-hundred-KB) schematic - and remove itself
	 * from this provider's feature list - once every chunk it spans has been drawn.
	 */
	public void populateFeatures(World world, Random rand, int chunkX, int chunkZ) {
		
		// Skip any chunk that already went through the population stage this session.
		long thisChunkHash = ChunkCoordIntPair.chunkXZ2Long(chunkX, chunkZ);
		if(this.populatedChunks.containsKey(thisChunkHash)) { 
			return;
		}
		this.populatedChunks.put(thisChunkHash, Boolean.TRUE);
				
		// Look which nearby features would affect this chunk
		int i = 3; {
			for(int x = chunkX - i; x <= chunkX + i; x ++) {
				for(int z = chunkZ - i; z <= chunkZ + i; z ++) {
					long chunkHash = ChunkCoordIntPair.chunkXZ2Long(x, z);
					
					Feature feature = this.featureList.get(chunkHash);
					if(feature != null &&
							Math.abs(feature.originChunkX - x) <= feature.getFeatureRadius() &&
							Math.abs(feature.originChunkZ - z) <= feature.getFeatureRadius()
					) {
							
						// Scattering passes (lilypads, seaweed, coral, spawners...) run for
						// every chunk around the origin; each feature's populate() guards its
						// own piece window, so all chunk within the scan are visited.
						feature.populate(world, rand, chunkX, chunkZ);
						
						// But the schematic-release counter must only tick for the feature's
						// OWN box chunks: those are the only draws the schematic is needed
						// for. A chunk inside the +/-3 scan but outside the box holds no part
						// of the structure, so it must not count towards release - otherwise
						// the counter drains early (via unrelated neighbors) and the schematic
						// is freed before its true last draw.
						if(
							Math.abs(chunkX - feature.originChunkX) <= feature.getFeatureRadius() &&
							Math.abs(chunkZ - feature.originChunkZ) <= feature.getFeatureRadius()
						) {
							feature.onChunkPopulated(chunkX, chunkZ);
						}
					}					
				}
			}
		}

	}
	
	/**
	 * Calculates the effective per-chunk spawn chance for a feature, considering
	 * the base roll chance and the biome condition.
	 *
	 * <p>The base chance is rollChance = getSpawnChance() / MAXCHANCE, where
	 * MAXCHANCE = 4096. The biome condition is evaluated at the chunk's center.
	 *
	 * @param featureClass the feature class to calculate the chance for
	 * @param chunkX the chunk X coordinate
	 * @param chunkZ the chunk Z coordinate
	 * @return the effective per-chunk spawn chance as a percentage (0-100)
	 */
	public double getEffectiveSpawnChance(Class<?> featureClass, int chunkX, int chunkZ) {
		// Get the biome at the chunk center
		BiomeGenBase biome = this.world.getWorldChunkManager().getBiomeGenAt((chunkX << 4) + 8, (chunkZ << 4) + 8);
		
		// Compute the base chance
		double baseChance = 0.0;
		try {
			Feature feature = (Feature) featureConstructors.get(featureClass)
					.newInstance(new Object[] {world, Integer.valueOf(chunkX), Integer.valueOf(chunkZ), this});
			baseChance = (double) feature.getSpawnChance() / Feature.MAXCHANCE;
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
		
		// Compute the biome condition
		boolean biomePasses = false;
		try {
			Feature feature = (Feature) featureConstructors.get(featureClass)
					.newInstance(new Object[] {world, Integer.valueOf(chunkX), Integer.valueOf(chunkZ), this});
			biomePasses = feature.shouldSpawn(this.chunkProvider, this.world, new Random(), biome, chunkX, chunkZ);
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
		
		// The effective chance is the base chance times the biome condition
		return baseChance * (biomePasses ? 1.0 : 0.0) * 100.0;
	}
	
	/**
	 * Whether the given feature's entire footprint - its origin chunk plus every chunk
	 * in its {@code getFeatureRadius()} box - is already final for this world. A chunk
	 * is final when it is either resident with its terrain populated (its blocks are in
	 * memory and it will never be generated again this session) or already persisted by
	 * the chunk loader from an earlier session (a saved chunk is only written after its
	 * populate stage, so its blocks - including the structure - are final forever).
	 *
	 * <p>Uses only cache and disk probes ({@link World#chunkExists(int,int)} never forces
	 * generation), so it is side-effect free. On a brand new world no chunk is final and
	 * this always returns false, preserving the original creation behaviour exactly.
	 */
	private boolean isFeatureFullyBuilt(Feature feature) {
		int radius = feature.getFeatureRadius();
		for(int chunkX = feature.originChunkX - radius; chunkX <= feature.originChunkX + radius; chunkX ++) {
			for(int chunkZ = feature.originChunkZ - radius; chunkZ <= feature.originChunkZ + radius; chunkZ ++) {
				if(!this.isChunkFinal(chunkX, chunkZ)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isChunkFinal(int chunkX, int chunkZ) {
		if(this.world.chunkExists(chunkX, chunkZ)) {
			return this.world.getChunkFromChunkCoords(chunkX, chunkZ).isTerrainPopulated;
		}
		IChunkLoader chunkLoader = this.world.getChunkLoader();
		return chunkLoader != null && chunkLoader.chunkExists(this.world, chunkX, chunkZ);
	}
	
	static {
		/*
		 * Register features here. Notice that order is important. 
		 */
		registeredFeatures = new ArrayList<Class<?>> ();
		featureConstructors = new HashMap<Class<?>, Constructor<? extends Feature>> ();
		
		registerFeature(FeatureVolcano.class);
		registerFeature(FeatureAmazonVillage.class);
		registerFeature(FeatureOceanRuins.class);
		registerFeature(FeatureHollowHill.class);
		registerFeature(FeatureWreck.class);
		registerFeature(FeatureShip.class);
		registerFeature(FeatureBigShip.class);
		registerFeature(FeatureSlimeBossLair.class);
		registerFeature(FeatureIcePalace.class);
		registerFeature(FeatureFossil.class);
		registerFeature(FeatureStoneArch.class);
		//registerFeature(FeatureSinkHole.class);
	}
}