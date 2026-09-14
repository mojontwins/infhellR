package net.minecraft.game.world.terrain;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.BetterDungeons;
import net.minecraft.game.world.terrain.generate.structure.mineshaft.MapGenMineshaft;
import net.minecraft.game.world.terrain.generate.structure.mineshaftmesa.MapGenMineshaftMesa;
import net.minecraft.game.world.terrain.generate.structure.stronghold.MapGenStronghold;
import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldChunkManager;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.terrain.generate.MapGenBase;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorOctaves;
import net.minecraft.game.world.block.BlockSand;
import net.minecraft.game.world.block.BlockTerracotta;
import net.minecraft.game.world.terrain.generate.city.BuildingHighway;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.MapGenCaves;
import net.minecraft.game.world.terrain.generate.MapGenCity;
import net.minecraft.game.world.terrain.generate.MapGenOceanRavine;
import net.minecraft.game.world.terrain.generate.MapGenRavine;
import net.minecraft.game.world.terrain.generate.MapGenUnderwater;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBigTree;
import net.minecraft.game.world.terrain.generate.WorldGenCactus;
import net.minecraft.game.world.terrain.generate.WorldGenClay;
import net.minecraft.game.world.terrain.generate.WorldGenDungeons;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenLiquids;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;
import net.minecraft.game.world.terrain.generate.WorldGenPumpkin;
import net.minecraft.game.world.terrain.generate.WorldGenReed;
import net.minecraft.game.world.terrain.generate.WorldGenSurfaceMoss;
import net.minecraft.game.world.terrain.generate.WorldGenTallGrass;



/**
 * Chunk provider that generates terrain using noise functions.
 * Handles biome placement, terrain shape, ore distribution, and decoration.
 */
public class ChunkProviderGenerate implements IChunkProvider {
	protected Random rand;
	protected NoiseGeneratorOctaves minLimitNoise;
	protected NoiseGeneratorOctaves maxLimitNoise;
	protected NoiseGeneratorOctaves mainNoise;
	protected NoiseGeneratorOctaves noiseGenSandOrGravel;
	protected NoiseGeneratorOctaves noiseStone;
	public NoiseGeneratorOctaves scaleNoise;
	public NoiseGeneratorOctaves depthNoise;
	public NoiseGeneratorOctaves caveNoise;
	public NoiseGeneratorOctaves mobSpawnerNoise;
	protected World worldObj;
	public final boolean mapFeaturesEnabled;
	public final boolean generateCities;
	protected double[] terrainNoise;
	protected double[] sandNoise = new double[256];
	protected double[] gravelNoise = new double[256];
	protected double[] stoneNoise = new double[256];
	protected MapGenBase caveGenerator = new MapGenCaves();
	protected MapGenBase cityGenerator = new MapGenCity();
	protected MapGenBase underwaterGenerator = new MapGenUnderwater();
	protected MapGenBase ravineGenerator = new MapGenRavine();
	protected MapGenBase oceanRavineGenerator = new MapGenOceanRavine();
	protected MapGenMineshaft mineshaftGenerator;
	protected MapGenMineshaftMesa mineshaftMesaGenerator;
	protected MapGenStronghold strongholdGenerator;

	// Reusable world generators: each holds only immutable constructor state,
	// so a single instance is shared across every per-chunk population attempt.
	// (Saves allocating a fresh object on each loop iteration.)
	protected WorldGenMinable coalGen, glowGen, ironGen, copperGen, goldGen, redstoneGen, diamondGen;
	protected WorldGenMinable dirtLumpGen, gravelLumpGen;
	protected WorldGenFlowers yellowFlowerGen, redFlowerGen, brownMushroomGen, redMushroomGen;
	protected WorldGenReed reedGen;
	protected WorldGenPumpkin pumpkinGen;
	protected WorldGenCactus cactusGen;
	protected WorldGenTallGrass tallGrassGen;
	protected WorldGenLiquids waterSpringGen, lavaSpringGen;
	protected WorldGenClay clayGen;
	protected WorldGenSurfaceMoss surfaceMossGen;

	protected BiomeGenBase[] biomesForGeneration;
	public double[] mainArray;
	public double[] minLimitArray;
	public double[] maxLimitArray;
	public double[] scaleArray;
	public double[] depthArray;
		double[] caveArray;
	public double[] generatedTemperatures;
	float[] distanceArray;
	
	public boolean isOcean;

	// Multi-chunk features	
	public FeatureProvider featureProvider;
	
	// Better Dungeons
	public BetterDungeons betterDungeons;
	
	public ChunkProviderGenerate(World world1, long j2) {
		this(world1, j2, true, true);
	}

	public ChunkProviderGenerate(World world1, long j2, boolean z4, boolean z5) {
		this.worldObj = world1;
		this.rand = new Random(j2);
		this.minLimitNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.maxLimitNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.mainNoise = new NoiseGeneratorOctaves(this.rand, 8);
		this.noiseGenSandOrGravel = new NoiseGeneratorOctaves(this.rand, 4);
		this.noiseStone = new NoiseGeneratorOctaves(this.rand, 4);
		this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
		this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.caveNoise = new NoiseGeneratorOctaves(this.rand, 4);
		this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
		
		this.betterDungeons = new BetterDungeons();
		this.featureProvider = new FeatureProvider(worldObj, this, this.betterDungeons);
		
		this.mapFeaturesEnabled = z4;
		this.generateCities = z5;
		
		this.mineshaftGenerator = new MapGenMineshaft(world1);
		this.mineshaftMesaGenerator = new MapGenMineshaftMesa(world1);
		this.strongholdGenerator = new MapGenStronghold(world1);

		// Reusable decoration/ore generators (immutable constructor state).
		this.coalGen      = new WorldGenMinable(Block.oreCoal.blockID, 16);
		this.glowGen      = new WorldGenMinable(Block.oreGlow.blockID, 6);
		this.ironGen      = new WorldGenMinable(Block.oreIron.blockID, 8);
		this.copperGen    = new WorldGenMinable(Block.oreCopper.blockID, 8);
		this.goldGen      = new WorldGenMinable(Block.oreGold.blockID, 8);
		this.redstoneGen  = new WorldGenMinable(Block.oreRedstone.blockID, 7);
		this.diamondGen   = new WorldGenMinable(Block.oreDiamond.blockID, 7);
		this.dirtLumpGen  = new WorldGenMinable(Block.dirt.blockID, 32);
		this.gravelLumpGen = new WorldGenMinable(Block.gravel.blockID, 32);
		this.clayGen      = new WorldGenClay(32);
		this.yellowFlowerGen  = new WorldGenFlowers(Block.plantYellow.blockID);
		this.redFlowerGen     = new WorldGenFlowers(Block.plantRed.blockID);
		this.brownMushroomGen = new WorldGenFlowers(Block.mushroomBrown.blockID);
		this.redMushroomGen   = new WorldGenFlowers(Block.mushroomRed.blockID);
		this.reedGen          = new WorldGenReed();
		this.pumpkinGen       = new WorldGenPumpkin();
		this.cactusGen        = new WorldGenCactus();
		this.tallGrassGen     = new WorldGenTallGrass(Block.tallGrass.blockID, -1);
		this.waterSpringGen   = new WorldGenLiquids(Block.waterMoving.blockID);
		this.lavaSpringGen    = new WorldGenLiquids(Block.lavaMoving.blockID);
		this.surfaceMossGen   = new WorldGenSurfaceMoss();
	}

	/**
	 * Generates the raw terrain heightmap and block IDs (stone/water/air) for a chunk.
	 * Uses layered noise to produce terrain features.
	 * 
	 * @param chunkX  Chunk X coordinate
	 * @param chunkZ  Chunk Z coordinate
	 * @param blocks  Array to fill with block IDs (stone=1, water=8, air=0)
	 */
	public void generateTerrain(int chunkX, int chunkZ, byte[] blocks) {
		final double noiseScale = 0.125D;
		final double scalingFactor = 0.25D;
		final double densityVariationSpeed = 0.25D;
		
		final byte quadrantSize = 4;
		final byte seaLevel = 64;
		final int xSize = quadrantSize + 1;
		final byte ySize = 17;
		final int zSize = quadrantSize + 1;
		final short chunkHeight = 128;

		this.terrainNoise = this.initializeNoiseField(this.terrainNoise, chunkX * quadrantSize, 0, chunkZ * quadrantSize, xSize, ySize, zSize, chunkX, chunkZ);
		this.isOcean = true;
		
		// Split in 4x16x4 sections
		for(int xSection = 0; xSection < quadrantSize; ++xSection) {
			for(int zSection = 0; zSection < quadrantSize; ++zSection) {
				
				//boolean[][] seaBedFound = new boolean [4][4];
				
				for(int ySection = 0; ySection < 16; ++ySection) {
					
					double densityMinXMinYMinZ = this.terrainNoise[((xSection + 0) * zSize + zSection + 0) * ySize + ySection + 0];
					double densityMinXMinYMaxZ = this.terrainNoise[((xSection + 0) * zSize + zSection + 1) * ySize + ySection + 0];
					double densityMaxXMinYMinZ = this.terrainNoise[((xSection + 1) * zSize + zSection + 0) * ySize + ySection + 0];
					double densityMaxXMinYMaxZ = this.terrainNoise[((xSection + 1) * zSize + zSection + 1) * ySize + ySection + 0];
					double yLerpAmountMinXMinZ = (this.terrainNoise[((xSection + 0) * zSize + zSection + 0) * ySize + ySection + 1] - densityMinXMinYMinZ) * noiseScale;
					double yLerpAmountMinXMaxZ = (this.terrainNoise[((xSection + 0) * zSize + zSection + 1) * ySize + ySection + 1] - densityMinXMinYMaxZ) * noiseScale;
					double yLerpAmountMaxXMinZ = (this.terrainNoise[((xSection + 1) * zSize + zSection + 0) * ySize + ySection + 1] - densityMaxXMinYMinZ) * noiseScale;
					double yLerpAmountMaxXMaxZ = (this.terrainNoise[((xSection + 1) * zSize + zSection + 1) * ySize + ySection + 1] - densityMaxXMinYMaxZ) * noiseScale;
					
					for(int y = 0; y < 8; ++y) {
						double curDensityMinXMinYMinZ = densityMinXMinYMinZ;
						double curDensityMinXMinYMaxZ = densityMinXMinYMaxZ;
						double xLerpAmountMinZ = (densityMaxXMinYMinZ - densityMinXMinYMinZ) * scalingFactor;
						double xLerpAmountMaxZ = (densityMaxXMinYMaxZ - densityMinXMinYMaxZ) * scalingFactor;

						int yy = ySection * 8 + y;

						for(int x = 0; x < 4; ++x) {
							int indexInBlockArray = (x + (xSection << 2)) << 11 | (0 + (zSection << 2)) << 7 | (ySection << 3) + y;
					
							double density = curDensityMinXMinYMinZ;
							double densityIncrement = (curDensityMinXMinYMaxZ - curDensityMinXMinYMinZ) * densityVariationSpeed;

							for(int z = 0; z < 4; ++z) {
								int blockID;

								// World density positive: fill with block. Otherwise, fill with water or air.
								if(density > 0.0D) {
									blockID = Block.stone.blockID;
									/*
									if (yy < seaLevel) {
										seaBedFound[x][z] = true;
									}
									*/
								} else if(yy < seaLevel /*&& !seaBedFound[x][z]*/) {
									blockID = Block.waterStill.blockID;
								} else {
									blockID = 0;
								}

								blocks[indexInBlockArray] = (byte)blockID;
								
								// Next Z
								indexInBlockArray += chunkHeight;
								density += densityIncrement;
								
								// Ocean detector
								if(yy == seaLevel - 1) this.isOcean &= (blockID != Block.stone.blockID);
							}

							curDensityMinXMinYMinZ += xLerpAmountMinZ;
							curDensityMinXMinYMaxZ += xLerpAmountMaxZ;
						}

						densityMinXMinYMinZ += yLerpAmountMinXMinZ;
						densityMinXMinYMaxZ += yLerpAmountMinXMaxZ;
						densityMaxXMinYMinZ += yLerpAmountMaxXMinZ;
						densityMaxXMinYMaxZ += yLerpAmountMaxXMaxZ;
					}
				}
			}
		}

	}

	/**
	 * Applies biome surface rules: replaces terrain blocks with biome-specific
	 * surfaces (sand, gravel, stone) using the pre-generated noise arrays.
	 *
	 * @param chunkX   Chunk X coordinate
	 * @param chunkZ   Chunk Z coordinate
	 * @param blocks   Block ID array for the chunk
	 * @param metadata Metadata array for the chunk
	 * @param biomes   Biome grid (16×16)
	 */
	public void replaceBlocksForBiome(int chunkX, int chunkZ, byte[] blocks, byte[] metadata, BiomeGenBase[] biomes) {
		// Replace blocks for each cell using the biome's surface rules.
		// sand/gravel/stone noise drives which surfaces get replaced.
		final int seaLevel = 64;
		double surfaceNoiseScale = 8.0D / 256D;
		this.sandNoise = this.noiseGenSandOrGravel.generateNoiseOctaves(this.sandNoise, (double)(chunkX * 16), (double)(chunkZ * 16), 0.0D, 16, 16, 1, surfaceNoiseScale, surfaceNoiseScale, 1.0D);
		this.gravelNoise = this.noiseGenSandOrGravel.generateNoiseOctaves(this.gravelNoise, (double)(chunkX * 16), 109.0134D, (double)(chunkZ * 16), 16, 1, 16, surfaceNoiseScale, 1.0D, surfaceNoiseScale);
		this.stoneNoise = this.noiseStone.generateNoiseOctaves(this.stoneNoise, (double)(chunkX * 16), (double)(chunkZ * 16), 0.0D, 16, 16, 1, surfaceNoiseScale * 2.0D, surfaceNoiseScale * 2.0D, surfaceNoiseScale * 2.0D);

		BiomeGenBase biomeGen;

		for(int z = 0; z < 16; ++z) {
			for(int x = 0; x < 16; ++x) {		
				biomeGen = biomes[z | (x << 4)];

				int noiseIndex = z | (x << 4);
				biomeGen.replaceBlocksForBiome(this, this.worldObj, this.rand, chunkX, chunkZ, x, z, blocks, metadata, seaLevel, this.sandNoise[noiseIndex], this.gravelNoise[noiseIndex], this.stoneNoise[noiseIndex]);
			}
		}

	}

	/**
	 * Shortcut that delegates to provideChunk; IChunkProvider interface entry point.
	 */
	public Chunk prepareChunk(int i1, int i2) {
		return this.provideChunk(i1, i2);
	}
	
	/**
	 * Biome-specific terrain shaping: mycelium ramps, dark forest flattens,
	 * mangrove squishes below 90, flower fields raise; fills reshaped columns
	 * with water or air. Currently disabled (call site commented out).
	 */
	public void specialCarving(int chunkX, int chunkZ, byte[] blockArray, Chunk chunk) {
		int index;
		boolean dry;
		
		index = 0;
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				dry = false;
				
				int hmIndex = z << 4 | x;
				int height = chunk.landSurfaceHeightMap[hmIndex];
				int newHeight = height;
				
				// Micellium ramps up 0-80, and attenuates 80-128	
				if(this.biomesForGeneration[index] == BiomeGenBase.biomeMycelium) {					
					newHeight = (height < 80) ? height * 64 / 80 : 64 + ((height - 80) * 64) / 48;				
				}
								
				// Dark forest halves bumpiness, centered 4 blocks higher
				// NewHeight = 68 + (height - 64) / 2
				if(this.biomesForGeneration[index] == BiomeGenBase.biomeDarkForest) {
					newHeight = 68 + (height - 64) / 2;
				}
				
				// Mangrove uses sea level squished terrain
				if(this.biomesForGeneration[index] == BiomeGenBase.biomeMangrove && height < 90) {
					newHeight = 64 + (height - 64) / 8;
					dry = true;
				}
				
				// Flower fields uses slightly squished terrain, over 64
				if(this.biomesForGeneration[index] == BiomeGenBase.biomeFlowerFields) {
					newHeight = height < 63 ? 63 : 63 + (height - 63) / 4;
				}
				
				// Carve & fill with water.
				if(height != newHeight) for(int y = newHeight; y <= height; y ++) {							
					blockArray[x << 11 | z << 7 | y] = (y < 64 && !dry) ? (byte)Block.waterStill.blockID : 0;							
				}
			
				chunk.landSurfaceHeightMap[hmIndex] = (byte) newHeight;
				
				index ++;
			}
		}
	}
	
	/**
	 * Creates a new chunk filled with terrain blocks according to biome rules.
	 *
	 * @param chunkX  Chunk X coordinate
	 * @param chunkZ  Chunk Z coordinate
	 * @param blocks  Array to fill with block IDs (stone=1, water=8, air=0)
	 */
	public Chunk provideChunk(int chunkX, int chunkZ) {
		this.rand.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);
		
		// Empty block array & new Chunk
		byte[] blockArray = new byte[32768];
		byte[] metadata = new byte[32768];
		Chunk chunk = new Chunk(this.worldObj, blockArray, metadata, chunkX, chunkZ);

		// Calculate biomes & temperatures for this chunk
		this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
		
		// Cache biomes in chunk
		chunk.biomeGenCache = this.biomesForGeneration.clone();

		// Harvest the temperature/humidity ramp computed by loadBlockGeneratorData
		// so the chunk's per-column climate caches need no extra noise pass.
		WorldChunkManager worldChunkManager = this.worldObj.getWorldChunkManager();
		if(worldChunkManager.temperature != null) {
			chunk.setClimateCache(worldChunkManager.temperature, worldChunkManager.humidity);
		}

		// New approach to preselect chunks for cities
		boolean isUrbanChunk = this.worldObj.getWorldChunkManager().isUrbanChunk(chunkX, chunkZ);
		chunk.isUrbanChunk = isUrbanChunk;
		
		// Generate terrain for this chunk
		this.generateTerrain(chunkX, chunkZ, blockArray);

		// Calculate height maps for this chunk
		chunk.generateLandSurfaceHeightMap();
		
		// If there is only water at sea level in this chunk, this.isOcean == true.
		chunk.isOcean = this.isOcean;

		if(chunk.isOcean && this.mapFeaturesEnabled) { 
			((MapGenUnderwater)this.underwaterGenerator).setChunk(chunk);
			this.underwaterGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
		}
		
		// Erosion pass (disabled)
		//this.specialCarving(chunkX, chunkZ, blockArray, chunk);
		
		// Features system
		boolean featureChunk = false;
		if (this.mapFeaturesEnabled) {
			featureChunk = this.featureProvider.getNearestFeatures(chunkX, chunkZ, chunk);
		} 
		
		// Ocean ravines:
		if(chunk.isOcean) {
			this.oceanRavineGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
		}
		
		// Replace blocks
		this.replaceBlocksForBiome(chunkX, chunkZ, blockArray, metadata, this.biomesForGeneration);

		// Mapgen stuff:
		
		// Biome based generation
		if(!chunk.isUrbanChunk) {
			BiomeGenBase biome = this.biomesForGeneration[8 + (8 << 4)];
			biome.generate(this.rand, 64, chunk);
		}
		
		// Caves
		this.caveGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
		
		// Mineshafts
		if (this.mapFeaturesEnabled) {
			this.mineshaftMesaGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
			this.mineshaftGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
			this.strongholdGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
		}
		
		// Cities & highways
		if (this.generateCities) { 
			if(BuildingHighway.generate(chunkX, chunkZ, this.rand, blockArray, this instanceof ChunkProviderSky)) {
				chunk.hasBuilding = true;
			} else {
				if(chunk.isUrbanChunk && !featureChunk) this.buildOnChunk(chunkX, chunkZ, chunk);
			}
		}
		
		// Ravines
		if(!chunk.isOcean) {
			this.ravineGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
		}

		// Terrain generation is done: slice the flat 128-high generation buffers into runtime
		// subchunks now that every gen-stage writer (city, features, MapGens) has finished.
		// Note: generateSkylightMap below recomputes the identical height map itself, so there
		// is no need for a separate generateHeightMap pass before the slice.
		chunk.loadFlatBlocks(blockArray, metadata);
		chunk.generateSkylightMap();

		// Done
		return chunk;
	}

	/**
	 * Generates the land-surface height map for a chunk without allocating or filling block
	 * storage — Solution 2-A of the height-query caching plan (docs/height_query_caching.md).
	 *
	 * <p>The density field is computed exactly as in {@link #generateTerrain} (same noise lattice,
	 * same interpolation order, so bit-identical values), but instead of writing 32 768 block
	 * bytes and re-scanning them, the "surface" is read straight off the density sign: a column's
	 * height is its topmost y in 0-127 whose interpolated density is positive, and the chunk is an
	 * ocean chunk unless some column has positive density at y 63. This reproduces, bit for bit,
	 * the observable output of today's {@code generateTerrain} + {@code Chunk.generateLandSurfaceHeightMap}
	 * (the flat height-only chunk's {@code getBlockID} resolves everything above y 127 to air, so
	 * the height map scan is exactly "first positive density from the top").
	 *
	 * <p>Returned chunk has no block storage; only {@code landSurfaceHeightMap} and
	 * {@code isOcean} are populated (plus {@code isUrbanChunk}), matching what the height-only
	 * callers consume. The {@code rand} seed is still re-seeded for parity with the pre-2-A path.
	 *
	 * @param chunkX  Chunk X coordinate
	 * @param chunkZ  Chunk Z coordinate
	 * @return  Chunk with terrain heightmap but no blocks placed
	 */
	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		this.rand.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);

		Chunk chunk = new Chunk(this.worldObj, chunkX, chunkZ);
		chunk.isUrbanChunk = this.worldObj.getWorldChunkManager().isUrbanChunk(chunkX, chunkZ);

		final double noiseScale = 0.125D;
		final double scalingFactor = 0.25D;
		final double densityVariationSpeed = 0.25D;

		final byte quadrantSize = 4;
		final byte seaLevel = 64;
		final int xSize = quadrantSize + 1;
		final byte ySize = 17;
		final int zSize = quadrantSize + 1;

		this.terrainNoise = this.initializeNoiseField(this.terrainNoise, chunkX * quadrantSize, 0, chunkZ * quadrantSize, xSize, ySize, zSize, chunkX, chunkZ);

		byte[] topY = new byte[256];
		this.isOcean = true;

		for(int xSection = 0; xSection < quadrantSize; ++xSection) {
			for(int zSection = 0; zSection < quadrantSize; ++zSection) {
				for(int ySection = 0; ySection < 16; ++ySection) {
					double densityMinXMinYMinZ = this.terrainNoise[((xSection + 0) * zSize + zSection + 0) * ySize + ySection + 0];
					double densityMinXMinYMaxZ = this.terrainNoise[((xSection + 0) * zSize + zSection + 1) * ySize + ySection + 0];
					double densityMaxXMinYMinZ = this.terrainNoise[((xSection + 1) * zSize + zSection + 0) * ySize + ySection + 0];
					double densityMaxXMinYMaxZ = this.terrainNoise[((xSection + 1) * zSize + zSection + 1) * ySize + ySection + 0];
					double yLerpAmountMinXMinZ = (this.terrainNoise[((xSection + 0) * zSize + zSection + 0) * ySize + ySection + 1] - densityMinXMinYMinZ) * noiseScale;
					double yLerpAmountMinXMaxZ = (this.terrainNoise[((xSection + 0) * zSize + zSection + 1) * ySize + ySection + 1] - densityMinXMinYMaxZ) * noiseScale;
					double yLerpAmountMaxXMinZ = (this.terrainNoise[((xSection + 1) * zSize + zSection + 0) * ySize + ySection + 1] - densityMaxXMinYMinZ) * noiseScale;
					double yLerpAmountMaxXMaxZ = (this.terrainNoise[((xSection + 1) * zSize + zSection + 1) * ySize + ySection + 1] - densityMaxXMinYMaxZ) * noiseScale;

					for(int y = 0; y < 8; ++y) {
						double curDensityMinXMinYMinZ = densityMinXMinYMinZ;
						double curDensityMinXMinYMaxZ = densityMinXMinYMaxZ;
						double xLerpAmountMinZ = (densityMaxXMinYMinZ - densityMinXMinYMinZ) * scalingFactor;
						double xLerpAmountMaxZ = (densityMaxXMinYMaxZ - densityMinXMinYMaxZ) * scalingFactor;

						int yy = ySection * 8 + y;

						for(int x = 0; x < 4; ++x) {
							double density = curDensityMinXMinYMinZ;
							double densityIncrement = (curDensityMinXMinYMaxZ - curDensityMinXMinYMinZ) * densityVariationSpeed;

							for(int z = 0; z < 4; ++z) {
								// Byte-for-byte the generateTerrain block decision (density > 0 → stone),
								// replacing the 32 KB block write with a column top + ocean fold.
								if(density > 0.0D) {
									topY[(((zSection << 2) | z) << 4) | ((xSection << 2) | x)] = (byte)yy;
								}

								// Ocean detector: same cell (yy == seaLevel - 1) and same stone test.
								if(yy == seaLevel - 1) this.isOcean &= (density <= 0.0D);

								density += densityIncrement;
							}

							curDensityMinXMinYMinZ += xLerpAmountMinZ;
							curDensityMinXMinYMaxZ += xLerpAmountMaxZ;
						}

						densityMinXMinYMinZ += yLerpAmountMinXMinZ;
						densityMinXMinYMaxZ += yLerpAmountMinXMaxZ;
						densityMaxXMinYMinZ += yLerpAmountMaxXMinZ;
						densityMaxXMinYMaxZ += yLerpAmountMaxXMaxZ;
					}
				}
			}
		}

		System.arraycopy(topY, 0, chunk.landSurfaceHeightMap, 0, topY.length);
		chunk.isOcean = this.isOcean;
		return chunk;
	}
	
		/**
	 * Populates the chunk with a generated city if conditions are met.
	 *
	 * @param chunkX  Chunk X coordinate
	 * @param chunkZ  Chunk Z coordinate
	 * @param chunk   Chunk to populate with city structures
	 */
	public void buildOnChunk(int chunkX, int chunkZ, Chunk chunk) {	
		((MapGenCity)this.cityGenerator).setChunk(chunk);
		((MapGenCity)this.cityGenerator).generate(this, this.worldObj, chunkX, chunkZ, chunk.blocks, chunk.data);
		chunk.hasBuilding = ((MapGenCity) this.cityGenerator).hasBuilding;
		chunk.hasRoad = ((MapGenCity) this.cityGenerator).hasRoad;
		chunk.baseHeight = ((MapGenCity) this.cityGenerator).baseHeight;
		chunk.roadVariation = ((MapGenCity) this.cityGenerator).roadVariation;
	}

	/**
	 * Initializes the noise field arrays for terrain generation.
	 * Generates noise for Alpha and Beta terrain types using various noise functions.
	 * 
	 * @param densityMapArray  Array to store the noise values
	 * @param x                 X coordinate of the noise field
	 * @param y                 Y coordinate of the noise field
	 * @param z                 Z coordinate of the noise field
	 * @param xSize             Size of the noise field in X dimension
	 * @param ySize             Size of the noise field in Y dimension
	 * @param zSize             Size of the noise field in Z dimension
	 * @param chunkX            Chunk X coordinate (for Beta noise generation)
	 * @param chunkZ            Chunk Z coordinate (for Beta noise generation)
	 * @return                  Array containing the generated noise values
	 */
	private double[] initializeNoiseField(double[] densityMapArray, int x, int y, int z, int xSize, int ySize, int zSize, int chunkX, int chunkZ) {
		
		// The noise field makes Alpha and Beta terrain generate differently.
		
		if(this.distanceArray == null) {
			this.distanceArray = new float[25];

			for(int dx = -2; dx <= 2; ++dx) {
				for(int dz = -2; dz <= 2; ++dz) {
					this.distanceArray[dx + 2 + (dz + 2) * 5] = 10.0F / MathHelper.sqrt_float((float)(dx * dx + dz * dz) + 0.2F);
				}
			}
		}
		
		// Alpha noise:
		
		if(densityMapArray == null) {
			densityMapArray = new double[xSize * ySize * zSize];
		}

		double noiseScaleXZ = 684.412D;
		double noiseScaleY = 684.412D;

		this.scaleArray = this.scaleNoise.generateNoiseOctaves(this.scaleArray, (double)x, (double)y, (double)z, xSize, 1, zSize, 1.0D, 0.0D, 1.0D);
		this.depthArray = this.depthNoise.generateNoiseOctaves(this.depthArray, (double)x, (double)y, (double)z, xSize, 1, zSize, 100.0D, 0.0D, 100.0D);
		this.mainArray = this.mainNoise.generateNoiseOctaves(this.mainArray, (double)x, (double)y, (double)z, xSize, ySize, zSize, noiseScaleXZ / 80.0D, noiseScaleY / 160.0D, noiseScaleXZ / 80.0D);
		this.minLimitArray = this.minLimitNoise.generateNoiseOctaves(this.minLimitArray, (double)x, (double)y, (double)z, xSize, ySize, zSize, noiseScaleXZ, noiseScaleY, noiseScaleXZ);
		this.maxLimitArray = this.maxLimitNoise.generateNoiseOctaves(this.maxLimitArray, (double)x, (double)y, (double)z, xSize, ySize, zSize, noiseScaleXZ, noiseScaleY, noiseScaleXZ);
		//this.caveArray = this.caveNoise.generateNoiseOctaves(this.caveArray, (double)x, (double)y, (double)z, xSize, ySize, zSize, 0.5, 1.5, 0.5);
		
		int mainIndex = 0;
		int depthScaleIndex = 0;

		int x0 = chunkX << 4;
		int z0 = chunkZ << 4;
		
		// Solution 2-C: the original code re-derived the biome grid 625 times per chunk (25 lattice
		// nodes × 25 neighbourhood samples, every sample a full 1×1 loadBlockGeneratorData → four
		// octave evaluations) plus 625 isUrbanChunk calls. The sampling window spans exactly
		// [x0, x0+20] in both axes (node centres at (dx*4+2, dz*4+2) with a ±2 radius), so a single
		// 21×21 grid decoded once is bit-identical input for every lookup; the noise octaves and the
		// city-noise field are each evaluated once instead of per sample.
		WorldChunkManager worldChunkManager = this.worldObj.getWorldChunkManager();
		final int gridSize = 21;
		BiomeGenBase[] biomeGrid = worldChunkManager.loadBlockGeneratorData(null, x0, z0, gridSize, gridSize);

		// Each 1×1 biome call the old path made left invalidateCity true iff exactly that sample's
		// cell had been special-overridden, and the following isUrbanChunk read that transient flag —
		// so a sample on an overridden cell was non-urban while every other sample kept its plain
		// city-noise value. Recompute the four plain chunk-coordinate values with the flag pinned off.
		worldChunkManager.invalidateCity = false;
		boolean[] urbanBase = new boolean[4];
		for(int cityDx = 0; cityDx <= 1; ++cityDx) {
			for(int cityDz = 0; cityDz <= 1; ++cityDz) {
				urbanBase[cityDx | (cityDz << 1)] = worldChunkManager.isUrbanChunk(chunkX + cityDx, chunkZ + cityDz);
			}
		}

		// xSize, zSize = 5
		for(int dx = 0; dx < xSize; ++dx) {
			for(int dz = 0; dz < zSize; ++dz) {
				
				// Calculate average surrounding biome min/max heights:
				float maxHeightScaled = 0.0F;
				float minHeightScaled = 0.0F;
				float totalDistance = 0.0F;
				
				// Limit max h for urban chunks
				
				BiomeGenBase thisBiome = biomeGrid[((dx << 2) + 2) * gridSize + ((dz << 2) + 2)];
				
				for(int avgDx = -2; avgDx <= 2; ++avgDx) {
					for(int avgDz = -2; avgDz <= 2; ++avgDz) {
						int gx = (dx << 2) + 2 + avgDx;
						int gz = (dz << 2) + 2 + avgDz;
						int gridIndex = gx * gridSize + gz;
						
						BiomeGenBase otherBiome = biomeGrid[gridIndex];
						// Special-overridden cells (mangrove / mycelium / dark-forest / flower-fields)
						// flipped invalidateCity in the 1×1 calls, masking only that sample's own
						// isUrbanChunk. Detect them exactly: such a cell's grid biome differs from what
						// the raw temperature/humidity lookup yields (same decode the 1×1 calls used).
						int blockX = x0 + gx;
						int blockZ = z0 + gz;
						boolean wasSpecialOverride = otherBiome != BiomeGenBase.getBiomeFromLookup(worldChunkManager.temperature[gridIndex], worldChunkManager.humidity[gridIndex], 0.0);
						boolean isOtherUrban = !wasSpecialOverride && urbanBase[(blockX >> 4 == chunkX ? 0 : 1) | (blockZ >> 4 == chunkZ ? 0 : 2)];
						
						float distance = this.distanceArray[avgDx + 2 + (avgDz + 2) * 5] / (otherBiome.minHeight + 2.0F);
						if(otherBiome.minHeight > thisBiome.minHeight) {
							distance /= 2.0F;
						}
						
						double otherMaxHeight = isOtherUrban && otherBiome.maxHeight > 0.15 ? 0.15 : otherBiome.maxHeight;
						
						maxHeightScaled += otherMaxHeight * distance;
						minHeightScaled += otherBiome.minHeight * distance;
						totalDistance += distance;
					}
				}

				float avgMaxHeight = maxHeightScaled / totalDistance;
				float avgMinHeight = minHeightScaled / totalDistance;
				
				//avgMaxHeight = avgMaxHeight * 0.9F + 0.1F;
				//avgMinHeight = (avgMinHeight * 4.0F - 1.0F) / 8.0F; 
				
				double scale = (this.scaleArray[depthScaleIndex] + 256.0D) / 512.0D;
				if(scale > 1.0D) {
					scale = 1.0D;
				}

				double depth = this.depthArray[depthScaleIndex] / 8000.0D;
				if(depth < 0.0D) {
					depth = -depth;
				}

				depth = depth * 3.0D - 3.0D;
				if(depth < 0.0D) {
					depth /= 2.0D;
					if(depth < -1.0D) {
						depth = -1.0D;
					}

					depth /= 1.4D;
					depth /= 2.0D;
					scale = 0.0D;
				} else {
					if(depth > 1.0D) {
						depth = 1.0D;
					}

					depth /= 6.0D;
				}

				scale += 0.5D;
				depth = depth * (double)ySize / 16.0D;
				//double offsetY = (double)ySize / 2.0D + depth * 4.0D; // Moved down
				++depthScaleIndex;

				for(int dy = 0; dy < ySize; ++dy) {
					double density = 0.0D;

					double minHeight = (double)avgMinHeight;
					double maxHeight = (double)avgMaxHeight;
					minHeight = minHeight * (double)ySize / 16.0D;
					
					// Find a way to combine min / max height with depth / scale!
					
					// Here this seems to affect things in the right way
					double offsetY = (double)ySize / 2.0D + (depth + minHeight) * 4.0D;
					double densityOffset = ((double)dy - offsetY) * 12.0D / (scale * maxHeight); 
					
					if(densityOffset < 0.0D) {
						densityOffset *= 4.0D;
					}

					double minDensity = this.minLimitArray[mainIndex] / 512.0D;
					double maxDensity = this.maxLimitArray[mainIndex] / 512.0D;
					double mainDensity = (this.mainArray[mainIndex] / 10.0D + 1.0D) / 2.0D;

					if(mainDensity < 0.0D) {
						density = minDensity;
					} else if(mainDensity > 1.0D) {
						density = maxDensity;
					} else {
						density = minDensity + (maxDensity - minDensity) * mainDensity;
					}

					density -= densityOffset;

					double cliffFactor;
					
					if(dy > ySize - 4) {
						cliffFactor = (double)((float)(dy - (ySize - 4)) / 3.0F);
						density = density * (1.0D - cliffFactor) + -10.0D * cliffFactor;
					}

					// This will never happen!
					if((double)dy < 0) {
						cliffFactor = (- (double)dy) / 4.0D;
						if(cliffFactor < 0.0D) {
							cliffFactor = 0.0D;
						}

						if(cliffFactor > 1.0D) {
							cliffFactor = 1.0D;
						}

						density = density * (1.0D - cliffFactor) + -10.0D * cliffFactor;
					}

					// Dig huge caves
					/*
					double caveDensity = this.caveArray[mainIndex] / 2;
					if(caveDensity > 0) { 
						// Y runs from 0 to 17, that's 1/8 precision, so
						// Need to atenuate caveDensity if dy <= 1 and if dy > 6
						// I mean it has to disappear completely for dy < 1 and dy >= 6
						if (dy < 2) {
							caveDensity *= (double)dy / 2.0D;
						} else if(dy > 6) {
							caveDensity = 0.0D;
						} else if (dy > 4) {
							caveDensity *= (double)(6 - dy) / 2.0D;
						}
											
						
						if(density > 0 && caveDensity > 0) {
							density *= -caveDensity;
						}
					}
					*/
					
					densityMapArray[mainIndex] = density;
					++mainIndex;
				}
			}
		}

		// The old per-sample path left invalidateCity = whether the very last 1×1 sample cell — the
		// (20,20) corner of the window — had been special-overridden. Replicate that tail state so
		// any later isUrbanChunk reads see the same value they did before.
		worldChunkManager.invalidateCity = biomeGrid[gridSize * gridSize - 1] != BiomeGenBase.getBiomeFromLookup(worldChunkManager.temperature[gridSize * gridSize - 1], worldChunkManager.humidity[gridSize * gridSize - 1], 0.0);

		return densityMapArray;
		
		// Beta noise: 
		/*
		if(d1 == null) {
			d1 = new double[i5 * i6 * i7];
		}

		double d8 = 684.412D;
		double d10 = 684.412D;
		double[] d12 = this.worldObj.getWorldChunkManager().temperature;
		double[] d13 = this.worldObj.getWorldChunkManager().humidity;
		this.scaleArray = this.scaleNoise.generateNoiseOctaves(this.scaleArray, i2, i4, i5, i7, 1.121D, 1.121D, 0.5D);
		this.depthArray = this.depthNoise.generateNoiseOctaves(this.depthArray, i2, i4, i5, i7, 200.0D, 200.0D, 0.5D);
		this.mainArray = this.mainNoise.generateNoiseOctaves(this.mainArray, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8 / 80.0D, d10 / 160.0D, d8 / 80.0D);
		this.minLimitArray = this.minLimitNoise.generateNoiseOctaves(this.minLimitArray, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8, d10, d8);
		this.maxLimitArray = this.maxLimitNoise.generateNoiseOctaves(this.maxLimitArray, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8, d10, d8);
		int i14 = 0;
		int i15 = 0;
		int i16 = 16 / i5;

		for(int i17 = 0; i17 < i5; ++i17) {
			int i18 = i17 * i16 + i16 / 2;

			for(int i19 = 0; i19 < i7; ++i19) {
				int i20 = i19 * i16 + i16 / 2;
				double d21 = d12[i18 * 16 + i20];
				double d23 = d13[i18 * 16 + i20] * d21;
				double d25 = 1.0D - d23;
				d25 *= d25;
				d25 *= d25;
				d25 = 1.0D - d25;
				double d27 = (this.scaleArray[i15] + 256.0D) / 512.0D;
				d27 *= d25;
				if(d27 > 1.0D) {
					d27 = 1.0D;
				}

				double d29 = this.depthArray[i15] / 8000.0D;
				if(d29 < 0.0D) {
					d29 = -d29 * 0.3D;
				}

				d29 = d29 * 3.0D - 2.0D;
				if(d29 < 0.0D) {
					d29 /= 2.0D;
					if(d29 < -1.0D) {
						d29 = -1.0D;
					}

					d29 /= 1.4D;
					d29 /= 2.0D;
					d27 = 0.0D;
				} else {
					if(d29 > 1.0D) {
						d29 = 1.0D;
					}

					d29 /= 8.0D;
				}

				if(d27 < 0.0D) {
					d27 = 0.0D;
				}

				d27 += 0.5D;
				d29 = d29 * (double)i6 / 16.0D;
				double d31 = (double)i6 / 2.0D + d29 * 4.0D;
				++i15;

				for(int i33 = 0; i33 < i6; ++i33) {
					double d34 = 0.0D;
					double d36 = ((double)i33 - d31) * 12.0D / d27;
					if(d36 < 0.0D) {
						d36 *= 4.0D;
					}

					double d38 = this.minLimitArray[i14] / 512.0D;
					double d40 = this.maxLimitArray[i14] / 512.0D;
					double d42 = (this.mainArray[i14] / 10.0D + 1.0D) / 2.0D;
					if(d42 < 0.0D) {
						d34 = d38;
					} else if(d42 > 1.0D) {
						d34 = d40;
					} else {
						d34 = d38 + (d40 - d38) * d42;
					}

					d34 -= d36;
					if(i33 > i6 - 4) {
						double d44 = (double)((float)(i33 - (i6 - 4)) / 3.0F);
						d34 = d34 * (1.0D - d44) + -10.0D * d44;
					}

					d1[i14] = d34;
					++i14;
				}
			}
		}

		return d1;
		*/
	}

	/**
	 * Checks if the chunk at the given coordinates exists.
	 *
	 * @param x  Chunk X coordinate
	 * @param z  Chunk Z coordinate
	 * @return   Always true (chunks are generated on demand)
	 */
	public boolean chunkExists(int i1, int i2) {
		return true;
	}

	/**
	 * Populates the chunk with ores based on the biome generator.
	 *
	 * @param x0       Starting X coordinate
	 * @param z0       Starting Z coordinate
	 * @param biomeGen Biome generator providing ore distribution settings
	 */
	public void populateOres(int x0, int z0, BiomeGenBase biomeGen) {
		int i, x, y, z;
		
		for(i = 0; i < biomeGen.coalLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			this.coalGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.glowLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(48);
			z = z0 + this.rand.nextInt(16);
			this.glowGen.generate(this.worldObj, this.rand, x, y, z);
		}
				
		for(i = 0; i < biomeGen.ironLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(72);
			z = z0 + this.rand.nextInt(16);
			this.ironGen.generate(this.worldObj, this.rand, x, y, z);
		}
		
		for(i = 0; i < biomeGen.copperLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(48);
			z = z0 + this.rand.nextInt(16);
			this.copperGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.goldLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(biomeGen.goldLumpMaxHeight);
			z = z0 + this.rand.nextInt(16);
			this.goldGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.redstoneLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(biomeGen.redstoneLumpMaxHeight);
			z = z0 + this.rand.nextInt(16);
			this.redstoneGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.diamondLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(biomeGen.diamondLumpMaxHeight);
			z = z0 + this.rand.nextInt(16);
			this.diamondGen.generate(this.worldObj, this.rand, x, y, z);
		}
	}
	
	/**
	 * Generates mineshafts and strongholds in the chunk if map features are enabled.
	 *
	 * @param chunkX  Chunk X coordinate
	 * @param chunkZ  Chunk Z coordinate
	 */
	public void generateMapFeatures(int chunkX, int chunkZ) {
		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ, false);
			this.mineshaftMesaGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ, true);
			this.strongholdGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ, true);
		}
	}
	
	/**
	 * Populates the chunk with features such as ores, flora, trees, and structures.
	 *
	 * @param chunkProvider  Chunk provider to get the base chunk
	 * @param chunkX         Chunk X coordinate
	 * @param chunkZ         Chunk Z coordinate
	 */
	public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
		BlockSand.fallInstantly = true;
		int x0 = chunkX * 16;
		int z0 = chunkZ * 16;
		
		// We need the chunk
		Chunk thisChunk = chunkProvider.provideChunk(chunkX, chunkZ);
		if(thisChunk.beingDecorated) {
			System.out.println ("Being decorated " + chunkX + " " + chunkZ);
			return;
		}
		thisChunk.beingDecorated = true;
		
		// Find which biome
		BiomeGenBase biomeGen = thisChunk.getBiomeGenAt(8, 8);
				
		this.rand.setSeed(this.worldObj.getRandomSeed());
		long seed1 = this.rand.nextLong() / 2L * 2L + 1L;
		long seed2 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed((long)chunkX * seed1 + (long)chunkZ * seed2 ^ this.worldObj.getRandomSeed());

		biomeGen.prePopulate(this.worldObj, this.rand, x0, z0);
		
		int i, x, y, z;

		// Features
		if(this.mapFeaturesEnabled) {
			// Vanilla map features (mineshafts)
			this.generateMapFeatures(chunkX, chunkZ);
			
			// Custom features
			this.featureProvider.populateFeatures(worldObj, rand, chunkX, chunkZ);
		}

		// Random very big tree
		if(this.rand.nextInt(512) == 0) {
			x = x0 + this.rand.nextInt(16) + 8;
			z = z0 + this.rand.nextInt(16) + 8;
			y = this.worldObj.getHeightValue(x, z);
			WorldGenerator hugeTreeGen = new WorldGenBigTree(false);
			hugeTreeGen.setScale(2.0F, 2.0F, 2.0F);
			if(hugeTreeGen.generate(this.worldObj, this.rand, x, y, z)) {
				System.out.println("Big tree @ " + x + " " + y + " " + z);
			}
		}
		
		int maxDungeonHeight = 128;
		if(thisChunk.hasBuilding) maxDungeonHeight = 70;

		// The dungeon generator stores the biome it was built for; since that
		// changes per chunk (and its instance fields are re-seeded each generate),
		// create one per chunk rather than per attempt.
		WorldGenDungeons dungeonGen = new WorldGenDungeons(biomeGen);

		for(i = 0; i < biomeGen.dungeonAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(maxDungeonHeight);
			z = z0 + this.rand.nextInt(16) + 8;
			dungeonGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.clayAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			this.clayGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.dirtLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			this.dirtLumpGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.gravelLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			this.gravelLumpGen.generate(this.worldObj, this.rand, x, y, z);
		}

		this.populateOres(x0, z0, biomeGen);

		double treeNoiseScale = 0.5D;
		int treeBaseAttempts = (int)((this.mobSpawnerNoise.generateNoiseOctaves((double)x0 * treeNoiseScale, (double)z0 * treeNoiseScale) / 8.0D + this.rand.nextDouble() * 4.0D + 4.0D) / 3.0D);
		if(treeBaseAttempts < 0) {
			treeBaseAttempts = 0;
		}

		if(this.rand.nextInt(10) == 0) {
			++treeBaseAttempts;
		}
		
		treeBaseAttempts += biomeGen.treeBaseAttemptsModifier;

		for(i = 0; i < treeBaseAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			z = z0 + this.rand.nextInt(16) + 8;
			y = this.worldObj.getHeightValue(x, z);
			
			WorldGenerator treeGen = biomeGen.genTreeTryFirst(this.rand);
			
			if(treeGen == null || treeGen.generate(this.worldObj, this.rand, x, y, z) == false) {	
				if (this.rand.nextInt(10) < biomeGen.bigTreesEach10Trees) {
					treeGen = biomeGen.getBigTreeGen(this.rand);
				} else {
					treeGen = biomeGen.getTreeGen(this.rand);
				}
				
				if(treeGen != null) treeGen.generate(this.worldObj, this.rand, x, y, z);
			}
		}

		for(i = 0; i < biomeGen.yellowFlowersAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16) + 8;
			this.yellowFlowerGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.redFlowersAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16) + 8;
			this.redFlowerGen.generate(this.worldObj, this.rand, x, y, z);
		}

		if(this.rand.nextInt(biomeGen.mushroomBrownChance) == 0) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16) + 8;
			this.brownMushroomGen.generate(this.worldObj, this.rand, x, y, z);
		}

		if(this.rand.nextInt(biomeGen.mushroomRedChance) == 0) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16) + 8;
			this.redMushroomGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.reedAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16) + 8;
			this.reedGen.generate(this.worldObj, this.rand, x, y, z);
		}
		
		if(biomeGen.pumpkinChance > 0 && this.rand.nextInt(biomeGen.pumpkinChance) == 0) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16) + 8;
			this.pumpkinGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.cactusAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16) + 8;
			this.cactusGen.generate(this.worldObj, this.rand, x, y, z);
		}
		
		// Generate tall grass
		for(i = 0; i < biomeGen.tallGrassAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(64) + 64;
			z = z0 + this.rand.nextInt(16) + 8;
			this.tallGrassGen.generate(this.worldObj, this.rand, x, y, z);
		}
		
		// Generate dead bushes
		for(i = 0; i < biomeGen.deadBushAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			z = z0 + this.rand.nextInt(16) + 8;
			y = this.worldObj.getHeightValue(x, z);
			
			Block block = Block.blocksList[this.worldObj.getBlockId(x, y - 1, z)];
			if (block == Block.sand || block == Block.grass || block == Block.dirt || (block instanceof BlockTerracotta)) 
				this.worldObj.setBlock(x, y, z, Block.deadBush.blockID);
		}

		for(i = 0; i < biomeGen.waterFallAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(this.rand.nextInt(biomeGen.waterFallMaxHeight - biomeGen.waterFallMinHeight) + biomeGen.waterFallMinHeight);
			z = z0 + this.rand.nextInt(16) + 8;
			this.waterSpringGen.generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.lavaAttempts; ++i) {
			x = x0 + this.rand.nextInt(16) + 8;
			y = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(112) + 8) + 8);
			z = z0 + this.rand.nextInt(16) + 8;
			this.lavaSpringGen.generate(this.worldObj, this.rand, x, y, z);
		}

		// Custom mapgenerator population 

		if(this.generateCities) { 
			((MapGenCity)this.cityGenerator).populate(this.worldObj, this.rand, chunkX, chunkZ, thisChunk);
			if(thisChunk.building != null) thisChunk.building.populate(this.worldObj, this.rand, chunkX, chunkZ, thisChunk);
		}
		
		if(this.mapFeaturesEnabled) {
			((MapGenUnderwater)this.underwaterGenerator).populate(this.worldObj, this.rand, chunkX, chunkZ, thisChunk);
		}
		
		// Layered sand
		// 1st step = build float "blurred" height map
		float[][] floatHeightMap = new float[18][18];
		for (x = -1; x < 17; x ++) {
			for (z = -1; z < 17; z ++) {
				floatHeightMap[x + 1][z + 1] = (float)this.worldObj.getHeightValue(x0 + x, z0 + z);
			}
		}
		
		float[][] blurredHeightMap = new float[16][16];
		for (x = 0; x < 16; x ++) {
			for (z = 0; z < 16; z ++) {
				int nearX = x + 1; 
				int nearZ = z + 1;
				float neighborHeightSum = 0.0F;
				for (int xx = nearX - 1; xx <= nearX + 1; xx ++) {
					for (int zz = nearZ - 1; zz <= nearZ + 1; zz ++) {
						neighborHeightSum += floatHeightMap[xx][zz];
					}
				}
				blurredHeightMap[x][z] = neighborHeightSum / 9.0F;
			}
		}

		// Now fill "decimal" part of height value over sand blocks with layered sand
		for (x = 0; x < 16; x ++) {
			for (z = 0; z < 16; z ++) {
				float f = blurredHeightMap[x][z];
				y = (int)f - 1;
				if(y >= 63 && this.worldObj.getBlockId(x0 + x, y, z0 + z) == Block.sand.blockID) {
					int yy = (int)f;
					int meta = (int)(16.0F * (f - Math.floor(f))) - 1 + (this.rand.nextInt(2) << 1) - 1;
					if(meta < 0) meta = 0;
					if(meta > 15) meta = 15;
					if (this.worldObj.getBlockId(x0 + x, yy + 1, z0 + z) == 0 && meta > 0) {
						this.worldObj.setBlockAndMetadata(x0 + x, yy , z0 + z, Block.layeredSand.blockID, meta);
					}
				}
			}
		}		
		
		// Surface moss: only attempt on humid, temperate land (after snow cover).
		float mossTemperature = this.worldObj.getTemperatureAt(x0 + 8, z0 + 8);
		float mossHumidity = this.worldObj.getHumidityAt(x0 + 8, z0 + 8);
		if(mossHumidity > 0.5F && mossTemperature > 0.4F && mossTemperature < 0.6F) {
			int mossAttempts = 1 + (int)((mossHumidity - 0.5F) * 8.0F);
			for(int moss = 0; moss < mossAttempts; ++moss) {
				int mossX = x0 + this.rand.nextInt(16) + 8;
				int mossZ = z0 + this.rand.nextInt(16) + 8;
				int mossY = this.worldObj.getLandSurfaceHeightValue(mossX, mossZ);
				this.surfaceMossGen.generate(this.worldObj, this.rand, mossX, mossY, mossZ);
			}
		}

		// Scatter pebbled grass: 4 random spots per chunk, terrain level like trees.
		for(i = 0; i < 4; ++i) {
			x = x0 + this.rand.nextInt(16);
			z = z0 + this.rand.nextInt(16);
			y = this.worldObj.getHeightValue(x, z);
			if(this.worldObj.getBlockId(x, y - 1, z) == Block.grass.blockID) {
				this.worldObj.setBlockAndMetadata(x, y - 1, z, Block.grassWithPebbles.blockID, this.worldObj.getBlockMetadata(x, y - 1, z));
			}
		}
		
		// Biome based population
		biomeGen.populate(this.worldObj, this.rand, x0, z0);
		
		// Cover with snow:
		for(x = x0; x < x0 + 16; x ++) {
			for(z = z0; z < z0 + 16; z ++) {
				if(this.worldObj.getBiomeGenAt(x, z).weather != Weather.cold) continue;
				
				y = this.worldObj.findTopSolidBlockUsingBlockMaterial(x, z);
				
				Block blockBeneath = Block.blocksList[this.worldObj.getBlockId(x, y - 1, z)];
				
				if(
					y > 0 && y < 128 && 
					blockBeneath.blockMaterial.getIsSolid() && 
					blockBeneath.blockMaterial != Material.ice
				) {
					if(blockBeneath.isOpaqueCube()) {
						Block block = Block.blocksList[this.worldObj.getBlockId(x, y, z)];
						if(block != null) {
							if (block.getRenderType() == 111) {
								this.worldObj.setBlockMetadata(x, y, z, (this.worldObj.getBlockMetadata(x, y, z) & 0xf0) | rand.nextInt(5));
							}
						} else {
							this.worldObj.setBlockAndMetadata(x, y, z, Block.snow.blockID, rand.nextInt(5) + 1);
						}
					} else if(blockBeneath.blockID == Block.layeredSand.blockID) {
						this.worldObj.setBlockAndMetadata(x, y - 1, z, Block.layeredSand.blockID, this.worldObj.getBlockMetadata(x, y - 1, z));
					}
				}
			}
		}

		BlockSand.fallInstantly = false;
		thisChunk.beingDecorated = false;
	}

	/**
	 * Saves chunks to disk.
	 *
	 * @param saveChunksHere  Whether to save chunks that are currently in use
	 * @param progressCallback  Callback for saving progress updates
	 * @return  True if chunks were saved successfully
	 */
	public boolean saveChunks(boolean saveChunksHere, IProgressUpdate progressCallback) {
		return true;
	}

	/**
	 * Unloads the 100 oldest chunks from memory.
	 *
	 * @return  False (not implemented)
	 */
	public boolean unload100OldestChunks() {
		return false;
	}

	public boolean canSave() {
		return true;
	}

	public String makeString() {
		return "RandomLevelSource";
	}
}
