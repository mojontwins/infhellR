package net.minecraft.game.world.terrain.generate.feature;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.feature.amazonvillage.FeatureAmazonVillage;
import net.minecraft.game.world.terrain.generate.feature.fossils.FeatureFossil;
import net.minecraft.game.world.terrain.generate.feature.icepalace.FeatureIcePalace;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.FeatureMineshaft;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.FeatureMineshaftMesa;
import net.minecraft.game.world.terrain.generate.feature.oceanruins.FeatureOceanRuins;
import net.minecraft.game.world.terrain.generate.feature.stronghold.FeatureStronghold;
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
	/**
	 * Largest feature radius the provider scans for, in chunks.
	 *
	 * <p>The per-chunk feature sweep walks a square of this half-size (rings
	 * {@code 0..MAX_FEATURE_RADIUS}) around the chunk being generated/populated, and the
	 * population scan re-visits the same square. A feature whose {@link Feature#getFeatureRadius()}
	 * exceeds this value would have some of its outer chunks never drawn, so registered
	 * features must keep their radius at or below this bound. Eight is enough for the
	 * mineshaft (its corridors can reach ~7 chunks from the origin room).
	 */
	public static final int MAX_FEATURE_RADIUS = 8;

	public World world;
	public IChunkProvider chunkProvider; 

	/**
	 * Registered feature classes, in registration order.
	 *
	 * <p>Order matters: {@link #getFeatureForChunkCoords} walks this list in order and
	 * accepts every feature whose gate passes. Because exclusive features are
	 * first-come-first-served, a class registered earlier wins the single exclusive slot
	 * for its chunk. Volcanos are registered first on purpose, so a rocky/volcanic biome
	 * surface picks the volcano over any other exclusive feature.
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
	 *
	 * Each cell maps to the LIST of features owned by that origin chunk. Today the
	 * selection stage yields at most one feature per cell, so every list holds a single
	 * element, but the list form is kept so the provider can grow to multiple features
	 * per chunk without another type change.
	 */
	private HashMap<Long, List<Feature>> featureList;
	
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

	public FeatureProvider(World world, IChunkProvider chunkProvider) {
		this.world = world;
		this.chunkProvider = chunkProvider;
		this.featureList = new HashMap<Long, List<Feature>> ();
		
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
	 * Looks up a registered feature class by its simple or fully-qualified name,
	 * case-insensitively (e.g. "FeatureIcePalace").
	 *
	 * @param featureClassName the class name to look up
	 * @return the matching registered class, or {@code null} if no registered
	 *         feature matches
	 */
	public static Class<?> getRegisteredFeatureClass(String featureClassName) {
		if(featureClassName == null) return null;
		for(Class<?> featureClass : registeredFeatures) {
			if(featureClass == null) continue;
			if(featureClass.getSimpleName().equalsIgnoreCase(featureClassName)
					|| featureClass.getName().equalsIgnoreCase(featureClassName)) {
				return featureClass;
			}
		}
		return null;
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
	 * initialized (but not yet setup) and returned inside a list.
	 *
	 * <p>Returns the list of features that spawn here, or {@code null} when none does.
	 * A chunk can host several features: only exclusive features refuse to share their
	 * origin chunk with another exclusive feature (see {@link Feature#exclusive()}), so
	 * the result may hold one exclusive feature plus any number of non-exclusive ones.
	 *
	 * <p>Determinism: the whole selection is a function of the world seed, the chunk
	 * coordinates and the biomes known so far - the gate Random is reseeded identically
	 * on every call so the same chunk always selects the same feature.
	 */
	public List<Feature> getFeatureForChunkCoords(int chunkX, int chunkZ) { 
		// Features accepted for this origin chunk. A chunk can hold more than one.
		List<Feature> features = new ArrayList<Feature>();
		
		// Seed must be consistent. Note the multiplications are INT arithmetic and may
		// overflow - that overflow is part of the (intentionally) stable seed, so keep
		// the expression shape unchanged.
		long seed = this.world.getRandomSeed() + chunkX * 25117 + chunkZ * 151121;
		
		// Get the biome
		BiomeGenBase biome = this.world.getWorldChunkManager().getBiomeGenAt((chunkX << 4) + 8, (chunkZ << 4) + 8);
		
		// Walk EVERY registered class (not just the first match): non-exclusive features
		// (like the mineshafts) may share the origin chunk with anything, so several can
		// be accepted here. An exclusive feature claims the chunk on a first-come,
		// first-served basis: at most one exclusive feature may live here, but any number
		// of non-exclusive features are welcome alongside it.
		boolean hasExclusive = false;
		Iterator<Class<?>> featureClassIterator = registeredFeatures.iterator();
		while(featureClassIterator.hasNext()) {					
			Class<?> featureClass = featureClassIterator.next();
			if(featureClass == null) continue;
			
			// Evaluate the gate on the shared probe for this class. Because shouldSpawn
			// is a pure function of (world, seed, biome, coords) the probe's verdict is
			// byte-identical to building a fresh instance - without the allocation.
			Feature probe = this.probeFeatures.get(featureClass);
			if(probe == null || !probe.shouldFeatureSpawn(this.chunkProvider, this.world, new Random(seed), biome, chunkX, chunkZ)) {
				continue;
			}
			
			// Exclusivity gate: an exclusive class refuses to share this chunk with another
			// exclusive class, but never with a non-exclusive one. `exclusive()` is pure
			// constant state, so asking the probe is equivalent to asking the real feature.
			boolean exclusive = probe.exclusive();
			if(exclusive && hasExclusive) {
				continue;
			}
			
			// The probe passed: construct the REAL feature for this chunk now.
			Feature feature;
			try {
				feature = (Feature) featureConstructors.get(featureClass)
						.newInstance(new Object[] { world, Integer.valueOf(chunkX), Integer.valueOf(chunkZ), this });
			} catch (Exception e) {
				// Mirror the original behaviour: treat the failed construction as "not
				// a feature here" and keep trying the remaining classes.
				e.printStackTrace();
				continue;
			}
			
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
				continue;
			}
			
			// Check if it's not too close to other features. Only features from OTHER origin
			// chunks are compared: features sharing this chunk are meant to overlap, so the
			// ones accepted earlier in this same call must not veto this one.
			// The separation is expressed in CHUNKS (minimumSeparation returns a chunk count),
			// so the block-space centers are shifted back to chunk coords before comparing.
			int separation = feature.minimumSeparation();
			boolean tooClose = false;
			Iterator<List<Feature>> iterator = this.featureList.values().iterator();
			while(iterator.hasNext()) {
				List<Feature> otherFeatures = iterator.next();
				for(Feature otherFeature : otherFeatures) {
					if(
							/*otherFeature.getClass() == feature.getClass() &&*/ (
								Math.abs((feature.centerX >> 4) - (otherFeature.centerX >> 4)) <= separation || 
								Math.abs((feature.centerZ >> 4) - (otherFeature.centerZ >> 4)) <= separation
							)
					) {
						tooClose = true;
						break;
					}
				}
				if(tooClose) {
					break;
				}
			}
			if(tooClose) {
				continue;
			}
			
			// Set this feature up (for instance creating a list of pieces or creating a logic representation of it)
			feature.setup(world, new Random(seed), biome, chunkX, chunkZ);
			System.out.println ("Feature " + feature.getClass().getName() + " @ " + feature.centerX + " " + feature.centerZ);
			features.add(feature);
			
			// An accepted exclusive feature now blocks any later exclusive class.
			if(exclusive) {
				hasExclusive = true;
			}
		}
		
		if(features.isEmpty()) return null;
		return features;
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
		List<Feature> features = this.featureList.get(chunkHash);
		if(features != null) {
			features.remove(feature);
			if(features.isEmpty()) {
				this.featureList.remove(chunkHash);
			}
		}
	}
	
	/**
	 * Scans the 7x7 chunk square centered on (chunkX, chunkZ) for features that are
	 * near or overlapping this chunk and returns them in the order their generation
	 * stage must run.
	 *
	 * <p>The scan visits every cell exactly ONCE (ordered by ring distance, 0..3), which
	 * preserves the exact feature-creation order of the original quadruple-nested sweep:
	 * a cell appears in {@code ring = max(|dx|, |dz|)}, so skipping cells that an earlier
	 * ring already covered yields the identical first-visit sequence - and therefore the
	 * identical feature set in {@link #featureList}. Features whose origins produced "no"
	 * are remembered in {@link #noFeatureChunks} so later, overlapping chunk requests
	 * short-circuit instead of re-running the gate.
	 *
	 * <p>This method only <em>collects</em>. The actual drawing is split across two
	 * generation stages by {@link #generateFeatures(List, int, int, Chunk, boolean)}:
	 * one pass before cave/ravine carving and one after it.
	 *
	 * @return the overlapping features, in the deterministic order their stage must run
	 */
	public List<Feature> collectNearestFeatures(int chunkX, int chunkZ) {
		List<Feature> features = new ArrayList<Feature>();
		
		// Single sweep, rings 0..3. Cell order is the same as the old loop would have
		// VISITED each cell for the FIRST time, which is all that matters for creation
		// order (re-visits could never create anything new).
		for(int i = 0; i <= MAX_FEATURE_RADIUS; i ++) {
			for(int x = chunkX - i; x <= chunkX + i; x ++) {
				for(int z = chunkZ - i; z <= chunkZ + i; z ++) {
					// Skip cells that appear on an inner ring - they were handled already.
					int ring = Math.max(Math.abs(x - chunkX), Math.abs(z - chunkZ));
					if(ring < i) continue;
					
					// First check already calculated features for this cell.
					long chunkHash = ChunkCoordIntPair.chunkXZ2Long(x, z);
					List<Feature> chunkFeatures = this.featureList.get(chunkHash); 
					
					// Skip cells we already proved have no feature - the verdict is stable
					// for the whole session, so this is a pure cache hit.
					if(chunkFeatures == null && this.noFeatureChunks.containsKey(chunkHash)) {
						continue;
					}
					
					// Not found, get a new one (possibly)
					if(chunkFeatures == null) {
						chunkFeatures = this.getFeatureForChunkCoords(x, z);
						
						if(chunkFeatures != null) {
							// If we got features for this chunk, store them
							this.featureList.put(chunkHash, chunkFeatures);	
							
						} else {
							// Remember the negative answer so other chunks don't re-ask.
							this.noFeatureChunks.put(chunkHash, Boolean.TRUE);
						}
					}
					
					if(chunkFeatures != null) {
						for(Feature chunkFeature : chunkFeatures) {
							// Filter PER feature: features sharing a cell can have different
							// radii, and each one's radius must actually reach this chunk or
							// it has no business drawing here.
							if(chunkFeature.getFeatureRadius() >= i) {
								features.add(chunkFeature);
							}
						}
					}
				}
			}
		}
		
		// The list is already in cell-visit order (rings 0..MAX, then within-cell
		// registration order), which keeps the generation order of same-stage features
		// stable across runs.
		return features;
	}
	
	/**
	 * Runs the generation stage of every collected feature whose carving stage matches.
	 *
	 * <p>Features are drawn in the order {@link #collectNearestFeatures(int, int)}
	 * returned them, so within a stage the draw order is identical to the historical
	 * single pass. A feature opts into the after-caves stage by overriding
	 * {@link Feature#isGeneratedAfterCaves()}.
	 *
	 * @param features   the list from {@link #collectNearestFeatures(int, int)}
	 * @param afterCaves {@code false} for the pass before carving, {@code true} for the
	 *        pass that runs once caves and ravines have been carved
	 * @return true if any feature of this stage drew into the chunk
	 */
	public boolean generateFeatures(List<Feature> features, int chunkX, int chunkZ, Chunk chunk, boolean afterCaves) {
		boolean featureInChunk = false;
		
		for(int i = 0; i < features.size(); i ++) {
			Feature feature = features.get(i);
			if(feature.isGeneratedAfterCaves() == afterCaves) {
				feature.generate(chunkX, chunkZ, chunk);
				featureInChunk = true;
			}
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
		int i = MAX_FEATURE_RADIUS; {
			for(int x = chunkX - i; x <= chunkX + i; x ++) {
				for(int z = chunkZ - i; z <= chunkZ + i; z ++) {
					long chunkHash = ChunkCoordIntPair.chunkXZ2Long(x, z);
					
					List<Feature> chunkFeatures = this.featureList.get(chunkHash);
					if(chunkFeatures != null) {
						// Iterate a SNAPSHOT: a feature may remove itself from the provider (and
						// therefore from this very list) inside onChunkPopulated once its last
						// chunk is drawn. With several features sharing a cell the iteration
						// would otherwise keep going and throw a ConcurrentModificationException.
						for(Feature feature : new ArrayList<Feature>(chunkFeatures)) {
							if(
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
		// The stronghold is rare (1 in 2048 chunks) but must be picked before the much
		// more common mineshafts, or a shaft gate that also passes would preempt it.
		registerFeature(FeatureStronghold.class);
		// Mesa shafts must be picked before the regular ones so a mesa chunk rolls the
		// (rarer, above-ground) mesa variant first; non-mesa chunks fall through because
		// FeatureMineshaftMesa.shouldSpawn requires a mesa biome.
		registerFeature(FeatureMineshaftMesa.class);
		registerFeature(FeatureMineshaft.class);
		//registerFeature(FeatureSinkHole.class);
	}
}