package net.minecraft.game.world.terrain.generate.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.EnumArt;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenForest;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorOctaves;

/**
 * Hollow hill rework: the constructor plans the whole feature (interior
 * content, maze layout, decorations, specials) on a world-seeded RNG without
 * reading a single world block; terraform writes normal blocks directly into
 * the chunk arrays; populate only flushes specials chunk-locally.
 *
 * This replaces WorldGenHollowHillInterior + WorldGenHillMaze and removes all
 * recursive chunk generation from hill population (no world reads/writes
 * toward -x/-z, no decoration outside the populated chunk).
 */
public class FeatureHollowHill extends Feature {
	// Interior content counts per feature size (from WorldGenHollowHillInterior).
	private static final int[] SPIKE_COUNT = new int[]{0, 128, 256, 512};
	private static final int[] MOB_SPAWNER_COUNT = new int[]{0, 2, 5, 10};
	private static final int[] TREASURE_CHEST_COUNT = new int[]{0, 2, 6, 12};

	public int featureRadius;
	public double diameter;
	public double hillHeight;

	/**
	 * Max bump modulation of the cosine arc, in [0, 1]:
	 * raise = arc * (1 + BUMP_AMPLITUDE * noise/3), so the raise swings
	 * (1 - A)..(1 + A) times the plain cosine value (the noise is scaled by
	 * 1/3 like vanilla's stone-noise consumption, then clamped). Tune
	 * manually if the hills should be calmer or craggier: 0 = smooth dome,
	 * higher = craggier (values above ~0.5 start hitting the clamp often).
	 */
	public static final double BUMP_AMPLITUDE = 0.30D;

	// Bump layer: perlin modulation of the cosine arc (hill_bump_noise.md).
	// Parameters copied from ChunkProviderGenerate's block-replacer stone
	// noise (noiseGen5): 4 octaves at 1/16 per world block, i.e. octave
	// wavelengths 16/8/4/2 blocks. World-global field (world-seeded), so
	// neighbouring hills share one continuous bump landscape instead of a
	// seam per feature.
	private NoiseGeneratorOctaves bumpNoise;

	// Ring radius of the interior content area (WorldGenHollowHillInterior.radius).
	private int interiorRadius;

	// Planning state. Everything below is computed in setup(), which
	// FeatureProvider runs on the real feature (the shared gate probe only
	// calls shouldSpawn), so no world block read happens here.
	private Random planRand;
	private GeneratorMaze maze;
	private int mazeBoxX;
	private int mazeBoxZ;
	private int mazeBoxSize;
	private final List<SpecialBlockInStructure> specialBlocks = new ArrayList<SpecialBlockInStructure>();
	private final List<BlockWrite> decorationBlocks = new ArrayList<BlockWrite>();
	private final Set<Long> claimedPositions = new HashSet<Long>();
	private final List<SpikePlan> spikes = new ArrayList<SpikePlan>();

	public FeatureHollowHill(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		// Feature radius 1-3; the size roll consumes `rand` exactly like the old
		// WorldGenHollowHill ctor did, so hill sizes stay put for a given seed.
		this.featureRadius = 3;
		switch(rand.nextInt(6)) {
		case 0:
		case 1:
		case 2:
			this.featureRadius --;
		case 3:
		case 4:
			this.featureRadius --;
		}

		this.diameter = (2 * this.featureRadius + 1) * 16.0D;
		this.hillHeight = this.diameter / 3.0D;
		this.interiorRadius = (this.featureRadius * 2 + 1) * 8 - 6;

		// World-seeded RNG using the FeatureProvider seed formula. This replaces
		// the old system-time MazeGenerator RNG: same seed -> same maze.
		long seed = world.getRandomSeed();
		this.planRand = new Random(seed + this.originChunkX * 25117L + this.originChunkZ * 151121L);

		// Bump field on its own world-seeded stream: never consumes planRand,
		// so maze/loot/spike rolls for a given seed stay put (WorldChunkManager
		// uses the same world.getRandomSeed() * K style for its climate noise).
		this.bumpNoise = new NoiseGeneratorOctaves(new Random(seed * 65891L), 4);

		this.planInterior();
		this.planMaze();

		System.out.println("Hollow hill @ " + this.centerX + " " + this.centerZ);
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return
				!(chunkProvider instanceof ChunkProviderSky) &&
				biome instanceof BiomeGenForest &&
				Math.abs(chunkX - (world.getWorldInfo().getSpawnX() >> 4)) > 8 &&
				Math.abs(chunkZ - (world.getWorldInfo().getSpawnZ() >> 4)) > 8;
	}

	// ---------------------------------------------------------------------------
	// Raise model: cosine arc + perlin bump layer. Shared by terraform and the
	// plan-time consumers (isSurfaceRaisedAt, planSpike) so both sides always
	// agree on the surface height.
	// ---------------------------------------------------------------------------

	/**
	 * Raise in blocks at distance d from the hill centre, with the bump layer
	 * applied after the cosine calculation: floor(arc * (1 + A * noise)) where
	 * arc = hillHeight * cos(d * PI / diameter).
	 *
	 * Multiplicative rather than additive so the rim stays flush: arc -> 0 at
	 * d == radius means the noise contribution vanishes at the feature edge
	 * (additive noise would leave a discontinuous cliff there).
	 */
	private int raiseAt(double d, double bump) {
		double arc = this.hillHeight * Math.cos(d * Math.PI / this.diameter);
		// Vanilla divides the stone noise by 3 in replaceSurfaceBlocks
		// (stoneNoise / 3 + 3): the raw 4-octave field swings to about +/-5,
		// so scale before clamping to keep the bumps graded instead of
		// saturating into flat +/- A plateaus.
		double n = bump / 3.0D;
		n = n < -1.0D ? -1.0D : (n > 1.0D ? 1.0D : n);
		return (int) Math.floor(arc * (1.0D + BUMP_AMPLITUDE * n));
	}

	/**
	 * Single-column bump sample for plan-time callers. Bit-identical to the
	 * bulk per-chunk terraform call: same y0 = 0, yscale = 1 and
	 * xscale = zscale = 1/16 (the stone-noise scale), and the perlin field is
	 * a pure function of the world coordinates.
	 */
	private double bumpNoiseAt(int worldX, int worldZ) {
		double[] one = this.bumpNoise.generateNoiseOctaves(null,
				(double) worldX, 0.0D, (double) worldZ, 1, 1, 1,
				1.0D / 16.0D, 1.0D, 1.0D / 16.0D);
		return one[0];
	}

	// ---------------------------------------------------------------------------
	// terraform: raw chunk-array writes only, zero World calls.
	// ---------------------------------------------------------------------------

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		// Running chunk-array index: x << 11 | z << 7.
		int index = 0;
		double radius = this.diameter / 2;

		// Bump layer for this chunk, one bulk call. populateNoiseArray loops
		// x (outer) -> z -> y (inner, size 1), so index = x * 16 + z; same
		// coordinates the scalar bumpNoiseAt would read.
		double[] bump = this.bumpNoise.generateNoiseOctaves(null,
				(double) (chunkX * 16), 0.0D, (double) (chunkZ * 16),
				16, 1, 16, 1.0D / 16.0D, 1.0D, 1.0D / 16.0D);

		// Per-column pocket model captured while raising: the air pocket runs
		// from colPocketFloorY to colNewHeight - 5 (stone cap above). -1 marks
		// columns outside the raise radius.
		int[] colPocketFloorY = new int[256];
		int[] colNewHeight = new int[256];

		int rx = (chunkX << 4) - this.centerX;
		for(int x = 0; x < 16; x++) {
			int rz = (chunkZ << 4) - this.centerZ;
			for(int z = 0; z < 16; z++) {
				double d = Math.sqrt(rx * rx + rz * rz);
				int heightMapIndex = z << 4 | x;
				colPocketFloorY[heightMapIndex] = -1;
				colNewHeight[heightMapIndex] = -1;

				if(d <= radius) {

					// Raise terrain using a scaled cosine function, modulated by
					// the perlin bump layer (raiseAt applies it after the cos):
					// cos goes from 1 to 0 for angles 0 to PI/2 radians, scaled
					// over 0..radius, then scaled by (1 + A * noise).

					int height = chunk.landSurfaceHeightMap[heightMapIndex];
					int newHeight = height + this.raiseAt(d, bump[x * 16 + z]);
					if(newHeight >= 120) newHeight = 120;

					// Pocket floor: terrain below y64 is compressed to a quarter
					// of its height; the pocket air pocket starts here.
					int pocketFloorY = 64 + (height - 64) / 4;
					colPocketFloorY[heightMapIndex] = pocketFloorY;
					colNewHeight[heightMapIndex] = newHeight;

					// Raise to height: air pocket below, 4-block stone cap on top

					for(int y = pocketFloorY; y < newHeight; y ++) {
						chunk.blocks[index + y] = y < newHeight - 4 ? 0 : (byte)Block.stone.blockID;
					}

					// Remove water
					int y = 64;
					while(y > 8) {
						y --;
						Block b = Block.blocksList[chunk.blocks[index + y] & 255];
						if(b != null && b.blockMaterial == Material.water) {
							chunk.blocks[index + y] = (byte)Block.sand.blockID;
						} else break;
					}

					chunk.landSurfaceHeightMap[heightMapIndex] = (byte)newHeight;
				}
				rz ++;
				index += 256;

			}
			rx ++;
		}

		// Spikes first: the maze box overwrites any spike that overlaps it,
		// matching the old order (interior generated before the maze).
		this.placeSpikes(chunk, chunkX, chunkZ, colPocketFloorY, colNewHeight);

		// Maze box: floor (y47), air interior (y48-50), ceiling (y51).
		// Port of the three WorldGenTF.fill calls (offsets 0..boxSize-1 exclusive).
		this.fillMazeBox(chunk, chunkX, chunkZ, 47, 1, Block.mazeStone.blockID, 1);
		this.fillMazeBox(chunk, chunkX, chunkZ, 48, 3, 0, 0);
		this.fillMazeBox(chunk, chunkX, chunkZ, 51, 1, Block.mazeStone.blockID, 2);

		// Maze walls and torch pillars, then the planned decoration blocks.
		this.maze.copyToChunk(chunk, chunkX, chunkZ);
		this.placeDecorationBlocks(chunk, chunkX, chunkZ);
	}

	/**
	 * Materialize the planned spikes that fall inside this chunk. Pocket
	 * bounds come from the raise model captured in terraform; the column
	 * length factor comes from hash01(spikeSeed, dx, dz) so chunks compute
	 * identical geometry with no RNG stream-alignment problem.
	 */
	private void placeSpikes(Chunk chunk, int chunkX, int chunkZ, int[] colPocketFloorY, int[] colNewHeight) {
		for(int i = 0; i < this.spikes.size(); i++) {
			SpikePlan spike = this.spikes.get(i);

			int halfWidth = (int)(spike.length / 4.5D);
			// Stone spikes stay within a 6-block reach so the footprint remains
			// inside the featureRadius rings.
			if(spike.blockID == Block.stone.blockID && halfWidth > 6) {
				halfWidth = 6;
			}

			for(int dx = -halfWidth; dx <= halfWidth; dx++) {
				for(int dz = -halfWidth; dz <= halfWidth; dz++) {
					int wx = spike.x + dx;
					int wz = spike.z + dz;
					if((wx >> 4) != chunkX || (wz >> 4) != chunkZ) continue;

					// Same distance metric as the original makeSpike.
					int ax = Math.abs(dx);
					int az = Math.abs(dz);
					int dist = (int)((double)Math.max(ax, az) + (double)Math.min(ax, az) * 0.5D);
					int dl = dist == 0
							? spike.length
							: (int)(hash01(spike.seed, dx, dz) * (double)Math.max(1, (int)(spike.length / ((double)dist + 0.25D))));
					if(dl < 1) continue;

					int column = (wz & 15) << 4 | (wx & 15);
					int ceilingY = colNewHeight[column];
					if(ceilingY < 0) continue; // column outside the raise radius
					int floorY = colPocketFloorY[column];
					int pocketTopY = ceilingY - 5;
					if(pocketTopY < floorY) continue; // empty pocket

					int blockIndex = (wx & 15) << 12 | (wz & 15) << 8;
					for(int k = 0; k < dl; k++) {
						// Anchor cells (ceiling cap / solid below the pocket)
						// start one block outside the pocket; the original
						// putBlock(priority=false) skipped them as non-air too.
						int y = spike.hang ? pocketTopY + 1 - k : floorY - 1 + k;
						if(y < floorY || y > pocketTopY) continue;
						if(chunk.blocks[blockIndex | y] != 0) continue;
						chunk.blocks[blockIndex | y] = (byte)spike.blockID;
						chunk.data[(wx & 15) << 12 | (wz & 15) << 8 | y] = 0;
					}
				}
			}
		}
	}

	/** Port of WorldGenTF.fill: exclusive loops, raw chunk-array writes. */
	private void fillMazeBox(Chunk chunk, int chunkX, int chunkZ, int y, int height, int blockID, int meta) {
		for(int cx = 0; cx < this.mazeBoxSize; cx++) {
			for(int cy = 0; cy < height; cy++) {
				for(int cz = 0; cz < this.mazeBoxSize; cz++) {
					this.writeChunkBlock(chunk, chunkX, chunkZ, this.mazeBoxX + cx, y + cy, this.mazeBoxZ + cz, blockID, meta);
				}
			}
		}
	}

	private void writeChunkBlock(Chunk chunk, int chunkX, int chunkZ, int wx, int wy, int wz, int blockID, int meta) {
		if((wx >> 4) != chunkX || (wz >> 4) != chunkZ) return;
		if(wy < 0 || wy > 255) return;
		chunk.blocks[(wx & 15) << 12 | (wz & 15) << 8 | wy] = (byte)blockID;
		chunk.data[(wx & 15) << 12 | (wz & 15) << 8 | wy] = (byte)meta;
	}

	private void placeDecorationBlocks(Chunk chunk, int chunkX, int chunkZ) {
		for(int i = 0; i < this.decorationBlocks.size(); i++) {
			BlockWrite block = this.decorationBlocks.get(i);
			this.writeChunkBlock(chunk, chunkX, chunkZ, block.x, block.y, block.z, block.blockID, block.metadata);
		}
	}

	// ---------------------------------------------------------------------------
	// populate: chunk-local special flush only.
	// ---------------------------------------------------------------------------

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		// Per-chunk flush replaces the old origin-chunk gate, so specials now
		// appear in every chunk of the feature (corner chunks included). This
		// relies on the Step 0 feature-list refresh in ChunkProviderGenerate.
		int x0 = chunkX << 4;
		int z0 = chunkZ << 4;
		int x1 = x0 + 15;
		int z1 = z0 + 15;
		// This chunk exists (populate runs on it), so this call never generates.
		Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);

		for(int i = 0; i < this.specialBlocks.size(); i++) {
			SpecialBlockInStructure special = this.specialBlocks.get(i);
			// Inclusive on both axes: a special on the chunk's east/south edge
			// belongs to this chunk.
			if(special.x < x0 || special.x > x1 || special.z < z0 || special.z > z1) continue;

			if(special instanceof SpecialBlockInStructurePainting) {
				this.spawnPainting(world, (SpecialBlockInStructurePainting)special);
			} else {
				this.placeSpecialBlock(world, chunk, special);

				if(special instanceof SpecialBlockInStructureChest) {
					this.fillChest(world, (SpecialBlockInStructureChest)special);
				} else if(special instanceof SpecialBlockInStructureSpawner) {
					TileEntityMobSpawner spawner = (TileEntityMobSpawner)world.getBlockTileEntity(special.x, special.y, special.z);
					if(spawner != null) {
						spawner.mobID = ((SpecialBlockInStructureSpawner)special).mobID;
					}
				}
			}
		}
	}

	/**
	 * Placement guard for populate. World reads/writes toward -x/-z could
	 * trigger recursive chunk generation (west/north neighbors may not exist
	 * while this chunk populates), so every path is checked to be generation-free:
	 *
	 * - All 4 horizontal neighbors exist (blockExists, generation-free):
	 *   setBlockAndMetadataWithNotify keeps the full original neighbor-update
	 *   semantics (only existing chunks are touched).
	 * - Otherwise: plain setBlockAndMetadata + markBlockNeedsUpdate. Safe
	 *   because chest/spawner (BlockContainer.onBlockAdded reads only its own
	 *   chunk), door (no onBlockAdded override) and water (BlockFluid.checkForHarden
	 *   reads no neighbors unless the block itself is lava; BlockFlowing.onBlockAdded
	 *   only schedules itself) touch no neighbors.
	 * - Lava at such a position: checkForHarden would read 4 horizontal + y+1
	 *   neighbors, so write the raw chunk array instead and schedule the fluid
	 *   tick manually. Documented caveat: possibly static lava / stale light at
	 *   the west and north chunk edges (see diary.md).
	 */
	private void placeSpecialBlock(World world, Chunk chunk, SpecialBlockInStructure special) {
		boolean neighborsExist =
				world.blockExists(special.x - 1, special.y, special.z) &&
				world.blockExists(special.x + 1, special.y, special.z) &&
				world.blockExists(special.x, special.y, special.z - 1) &&
				world.blockExists(special.x, special.y, special.z + 1);

		if(neighborsExist) {
			world.setBlockAndMetadataWithNotify(special.x, special.y, special.z, special.blockID, special.metadata);
			world.markBlockNeedsUpdate(special.x, special.y, special.z);
		} else if(special.blockID == Block.lavaMoving.blockID) {
			int lx = special.x & 15;
			int lz = special.z & 15;
			chunk.setBlockIDWithMetadata(lx, special.y, lz, special.blockID, special.metadata);
			world.scheduleBlockUpdate(special.x, special.y, special.z, special.blockID, Block.blocksList[special.blockID].tickRate());
			world.markBlockNeedsUpdate(special.x, special.y, special.z);
		} else {
			world.setBlockAndMetadata(special.x, special.y, special.z, special.blockID, special.metadata);
			world.markBlockNeedsUpdate(special.x, special.y, special.z);
		}
	}

	private void fillChest(World world, SpecialBlockInStructureChest chest) {
		TileEntityChest tileEntity = (TileEntityChest)world.getBlockTileEntity(chest.x, chest.y, chest.z);
		if(tileEntity != null && tileEntity.getSizeInventory() > 0) {
			// Loot was rolled in the constructor; filled sequentially instead of
			// at random chest slots (cosmetic difference only).
			for(int i = 0; i < chest.contents.length && i < tileEntity.getSizeInventory(); i++) {
				tileEntity.setInventorySlotContents(i, chest.contents[i]);
			}
		}

		if(chest.cobblePillar) {
			// Port of WorldGenTF.fillWithBlocksDownwads: own column only (all y
			// values share this chunk), non-notify, stops above y1.
			int y = chest.y - 1;
			while((world.getBlockId(chest.x, y, chest.z) == 0 || world.getBlockMaterial(chest.x, y, chest.z) == Material.water) && y > 1) {
				world.setBlockAndMetadata(chest.x, y--, chest.z, Block.cobblestone.blockID, 0);
			}
		}
	}

	private void spawnPainting(World world, SpecialBlockInStructurePainting special) {
		// The title constructor reads no world state; the footprint was already
		// validated at plan time (world reads toward -x/-z are forbidden here).
		EntityPainting painting = new EntityPainting(world, special.x, special.y, special.z, special.direction, special.artTitle);

		// Port of EntityPainting.onValidSurface's entity-overlap clause. The
		// entity scan is chunkExists-guarded (World.getEntitiesWithinAABBExcludingEntity),
		// so it never generates a chunk.
		List<Entity> nearby = world.getEntitiesWithinAABBExcludingEntity(painting, painting.boundingBox);
		boolean overlap = false;
		for(int i = 0; i < nearby.size(); i++) {
			if(nearby.get(i) instanceof EntityPainting) {
				overlap = true;
				break;
			}
		}

		if(!overlap) {
			if(!world.isRemote) {
				world.spawnEntityInWorld(painting);
			}
		} else {
			System.out.println("Painting fail!! " + painting.art.title + " at " + painting.xPosition + " , " + painting.yPosition + ", " + painting.zPosition + " : " + painting.direction);
		}
	}

	// ---------------------------------------------------------------------------
	// Interior plan (port of WorldGenHollowHillInterior, plan-time only).
	// ---------------------------------------------------------------------------

	private void planInterior() {
		int baseY = 64;

		// Mob spawners: position, then y roll, then mob roll (RNG order kept).
		for(int i = 0; i < MOB_SPAWNER_COUNT[this.featureRadius]; i++) {
			int[] pos = this.getCoordsInHill2D();
			int y = baseY + this.planRand.nextInt(4);
			this.planSpawner(pos[0], y, pos[1], this.getInteriorMobID(this.featureRadius));
		}

		// Treasure chests with cobble support pillar below (no air check, as original).
		for(int i = 0; i < TREASURE_CHEST_COUNT[this.featureRadius]; i++) {
			int[] pos = this.getCoordsInHill2D();
			int y = baseY + this.planRand.nextInt(4);
			this.planChest(pos[0], y, pos[1], this.rollInteriorLoot(), true);
		}

		// Ore stalactites.
		for(int i = 0; i < SPIKE_COUNT[this.featureRadius]; i++) {
			int[] pos = this.getCoordsInHill2D();
			GeneratorCaveStalactite spike = GeneratorCaveStalactite.makeRandomOreStalactite(this.planRand, this.featureRadius);
			this.planSpike(pos[0], pos[1], spike);
		}

		// Stone stalactites hanging from the pocket ceiling.
		for(int i = 0; i < SPIKE_COUNT[this.featureRadius]; i++) {
			int[] pos = this.getCoordsInHill2D();
			this.planSpike(pos[0], pos[1], new GeneratorCaveStalactite(Block.stone.blockID, this.planRand.nextDouble(), true));
		}

		// Stone stalagmites rising from the pocket floor.
		for(int i = 0; i < SPIKE_COUNT[this.featureRadius]; i++) {
			int[] pos = this.getCoordsInHill2D();
			this.planSpike(pos[0], pos[1], new GeneratorCaveStalactite(Block.stone.blockID, this.planRand.nextDouble() * 0.7D, false));
		}

		// Two wraith spawners on the hilltop (largest hills only). The original
		// world air check at y74 is replaced by the raise model: with base
		// terrain y64, a raise >= 15 puts the surface at y79+, so y74 sits
		// inside the pocket air.
		if(this.featureRadius == 3) {
			for(int i = 0; i < 2; i++) {
				int[] pos = this.getCoordsInHillWithAirModel(20);
				this.planSpawner(pos[0], baseY + 10, pos[1], "TwilightWraith");
			}
		}
	}

	/** Port of WorldGenHollowHillInterior.getCoordsInHill2D. */
	private int[] getCoordsInHill2D() {
		int rx;
		int rz;
		do {
			rx = this.centerX + this.planRand.nextInt(2 * this.interiorRadius) - this.interiorRadius;
			rz = this.centerZ + this.planRand.nextInt(2 * this.interiorRadius) - this.interiorRadius;
		} while(!this.isInHill(rx, rz));

		return new int[]{rx, rz};
	}

	/**
	 * Port of WorldGenHollowHillInterior.getEmptyCoordsInHill with the world
	 * block read replaced by the raise model (see planInterior for the y74
	 * derivation).
	 */
	private int[] getCoordsInHillWithAirModel(int rad) {
		int rx;
		int rz;
		do {
			rx = this.centerX + this.planRand.nextInt(2 * rad) - rad;
			rz = this.centerZ + this.planRand.nextInt(2 * rad) - rad;
		} while(!this.isInHill(rx, rz) || !this.isSurfaceRaisedAt(rx, rz));

		return new int[]{rx, rz};
	}

	/** Port of WorldGenHollowHillInterior.isInHill. */
	private boolean isInHill(int x, int z) {
		int dx = this.centerX - x;
		int dz = this.centerZ - z;
		int dist = (int)Math.sqrt((double)(dx * dx + dz * dz));
		return dist < this.interiorRadius;
	}

	/** Raise model (cosine + bump layer): does the surface reach at least y79 over (x, z)? */
	private boolean isSurfaceRaisedAt(int x, int z) {
		double dx = x - this.centerX;
		double dz = z - this.centerZ;
		double d = Math.sqrt(dx * dx + dz * dz);
		return this.raiseAt(d, this.bumpNoiseAt(x, z)) >= 15;
	}

	/** Port of WorldGenHollowHillInterior.getMobID. */
	private String getInteriorMobID(int level) {
		return level == 1 ? this.getLevel1Mob() : (level == 2 ? this.getLevel2Mob() : (level == 3 ? this.getLevel3Mob() : "Spider"));
	}

	private String getLevel1Mob() {
		switch(this.planRand.nextInt(10)) {
		case 0:
		case 1:
		case 2:
			return "SwarmSpider";
		case 3:
		case 4:
		case 5:
			return "Spider";
		case 6:
		case 7:
			return "Zombie";
		case 8:
			//return "Silverfish";
		case 9:
			return "Redcap";
		default:
			return "Spider";
		}
	}

	private String getLevel2Mob() {
		switch(this.planRand.nextInt(10)) {
		case 0:
		case 1:
		case 2:
			return "Redcap";
		case 3:
		case 4:
		case 5:
			return "Zombie";
		case 6:
		case 7:
			return "Skeleton";
		case 8:
			return "Spider";
		case 9:
			return "SwarmSpider";
		default:
			return "Redcap";
		}
	}

	private String getLevel3Mob() {
		switch(this.planRand.nextInt(11)) {
		case 0:
		case 1:
		case 2:
			return "SwarmSpider";
		case 3:
		case 4:
		case 5:
			return "Skeleton";
		case 6:
		case 7:
			return "Spider";
		case 8:
			return "Creeper";
		case 9:
		case 10:
			return "TwilightWraith";
		default:
			return "Husk";
		}
	}

	/** Rolls the interior chest loot (rand(4) + rand(4) + 2 items), as the old
	 *  WorldGenHollowHillInterior.placeTreasureChest did. */
	private ItemStack[] rollInteriorLoot() {
		int count = this.planRand.nextInt(4) + this.planRand.nextInt(4) + 2;
		ItemStack[] loot = new ItemStack[count];
		for(int i = 0; i < count; i++) {
			loot[i] = this.getInteriorLootItem(this.featureRadius);
		}
		return loot;
	}

	/** Port of WorldGenHollowHillInterior.getTreasure. */
	private ItemStack getInteriorLootItem(int level) {
		if(level == 1) {
			switch(this.planRand.nextInt(6)) {
			case 0:
				return new ItemStack(Item.ingotIron, this.planRand.nextInt(4) + 1);
			case 1:
				return new ItemStack(Item.bucketEmpty);
			case 2:
				return new ItemStack(Item.bread);
			case 3:
				return new ItemStack(Item.ruby);
			case 5:
				return new ItemStack(Item.emerald);
			case 4:
				return new ItemStack(Item.wheat, this.planRand.nextInt(3) + 1);
			default:
				return new ItemStack(Block.torchWood, this.planRand.nextInt(16) + 1);
			}
		} else if(level == 2) {
			switch(this.planRand.nextInt(8)) {
			case 0:
			case 1:
			case 2:
				return this.getInteriorLootItem(1);
			case 3:
				return new ItemStack(Item.ruby);
			case 7:
				return new ItemStack(Item.emerald);
			case 4:
				return new ItemStack(Item.ingotGold, this.planRand.nextInt(6) + 1);
			case 5:
				return new ItemStack(Item.saddle);
			case 6:
				return new ItemStack(Item.dyePowder, this.planRand.nextInt(10) + 1, this.planRand.nextInt(16));
			default:
				return new ItemStack(Item.bowlSoup);
			}
		} else if(level == 3) {
			switch(this.planRand.nextInt(8)) {
			case 0:
			case 1:
			case 2:
				return this.getInteriorLootItem(2);
			case 3:
				return new ItemStack(Item.emerald);
			case 7:
				return new ItemStack(Item.ruby);
			case 4:
				return new ItemStack(Item.appleGold);
			case 5:
				return new ItemStack(Block.sponge);
			case 6:
				return new ItemStack(Item.saddle);
			default:
				return new ItemStack(Item.diamond);
			}
		} else {
			return new ItemStack(Item.coal, this.planRand.nextInt(16) + 1);
		}
	}

	/**
	 * Plan a spike. Port of WorldGenCaveStalactite.generate as an analytic
	 * model: the old code scanned the real world for the pocket floor and
	 * ceiling at this column (native cave pockets are not detected anymore -
	 * documented behavior change). At base terrain y64 the raise model gives
	 * ceiling = 60 + raise and floor = 63, so length = (raise - 3) * size,
	 * identical to the old (ceiling - floor) * size for that base height.
	 * The raise comes from the shared raiseAt (cosine + bump layer), so plan
	 * and terraform use the same bumpy surface height.
	 */
	private void planSpike(int x, int z, GeneratorCaveStalactite spike) {
		double dx = x - this.centerX;
		double dz = z - this.centerZ;
		double d = Math.sqrt(dx * dx + dz * dz);
		int raise = this.raiseAt(d, this.bumpNoiseAt(x, z));
		int length = (int)((raise - 3) * spike.size);

		// Degenerate pockets write nothing in the original either (length 0);
		// the guard also keeps the hash math well-defined for shallow hills.
		if(length < 1) {
			return;
		}

		// Length caps ported verbatim from WorldGenCaveStalactite.generate
		// (coal and stone are intentionally uncapped).
		if(spike.bType == Block.oreDiamond.blockID && length > 4) {
			length = 4;
		}
		if(spike.bType == Block.oreGold.blockID && length > 6) {
			length = 6;
		}
		if(length > 8 && (spike.bType == Block.oreRuby.blockID || spike.bType == Block.oreEmerald.blockID
				|| spike.bType == Block.oreRedstone.blockID || spike.bType == Block.oreIron.blockID
				|| spike.bType == Block.glowStone.blockID)) {
			length = 8;
		}

		// Per-spike seed replaces the shared random stream: terraform chunks can
		// be visited in any order and still write identical geometry.
		this.spikes.add(new SpikePlan(x, z, spike.bType, spike.hang, length, this.planRand.nextLong()));
	}

	// ---------------------------------------------------------------------------
	// Maze plan (port of WorldGenHillMaze, plan-time only).
	// ---------------------------------------------------------------------------

	private void planMaze() {
		int msize = this.featureRadius == 1 ? 11 : (this.featureRadius == 2 ? 19 : 27);
		this.mazeBoxX = this.centerX - 7 - this.featureRadius * 16;
		this.mazeBoxZ = this.centerZ - 7 - this.featureRadius * 16;
		this.mazeBoxSize = msize * 4;

		this.maze = new GeneratorMaze(msize, msize);
		// World-seeded stream: replaces MazeGenerator's system-time default.
		this.maze.rand = this.planRand;
		this.maze.setWorldOrigin(this.mazeBoxX, 48, this.mazeBoxZ);

		// RNG order kept from WorldGenHillMaze: rooms -> backtracker ->
		// torch flags (old placeTorches position) -> decorations.
		int nrooms = msize / 3;
		int[] roomCoords = new int[nrooms * 2];
		for(int i = 0; i < nrooms; i++) {
			int rx;
			int rz;
			do {
				rx = this.planRand.nextInt(msize - 2) + 1;
				rz = this.planRand.nextInt(msize - 2) + 1;
			} while(this.isNearRoom(rx, rz, roomCoords));

			this.maze.carveRoom1(rx, rz);
			roomCoords[i * 2] = rx;
			roomCoords[i * 2 + 1] = rz;
		}

		this.maze.generateRecursiveBacktracker(0, 0);
		this.maze.computeTorchFlags();

		this.planDeadEnds();
		this.plan3x3Rooms(roomCoords);
	}

	/** Port of WorldGenHillMaze.isNearRoom (zero-filled slots read as (0,0),
	 *  and carved rooms are never at 0 thanks to nextInt(msize-2)+1). */
	private boolean isNearRoom(int dx, int dz, int[] roomCoords) {
		for(int i = 0; i < roomCoords.length / 2; i++) {
			int rx = roomCoords[i * 2];
			int rz = roomCoords[i * 2 + 1];
			if((rx != 0 || rz != 0) && Math.abs(dx - rx) < 3 && Math.abs(dz - rz) < 3) {
				return true;
			}
		}

		return false;
	}

	/** Port of WorldGenHillMaze.decorateDeadEnds (facing flag mapping kept). */
	private void planDeadEnds() {
		for(int x = 0; x < this.maze.width; x++) {
			for(int z = 0; z < this.maze.depth; z++) {
				if(!this.maze.isWall(x, z, x - 1, z) && this.maze.isWall(x, z, x + 1, z) && this.maze.isWall(x, z, x, z - 1) && this.maze.isWall(x, z, x, z + 1)) {
					this.planDeadEnd(x, z, 3);
				}

				if(this.maze.isWall(x, z, x - 1, z) && !this.maze.isWall(x, z, x + 1, z) && this.maze.isWall(x, z, x, z - 1) && this.maze.isWall(x, z, x, z + 1)) {
					this.planDeadEnd(x, z, 1);
				}

				if(this.maze.isWall(x, z, x - 1, z) && this.maze.isWall(x, z, x + 1, z) && !this.maze.isWall(x, z, x, z - 1) && this.maze.isWall(x, z, x, z + 1)) {
					this.planDeadEnd(x, z, 0);
				}

				if(this.maze.isWall(x, z, x - 1, z) && this.maze.isWall(x, z, x + 1, z) && this.maze.isWall(x, z, x, z - 1) && !this.maze.isWall(x, z, x, z + 1)) {
					this.planDeadEnd(x, z, 2);
				}
			}
		}
	}

	/** Port of WorldGenHillMaze.decorateDeadEnd (17-wide roll, 15/16 no-op). */
	private void planDeadEnd(int x, int z, int f) {
		int dec = this.planRand.nextInt(17);
		switch(dec) {
		case 0:
			this.planDeadEndSpiderSpawner(x, z, f);
			break;
		case 1:
			this.planDeadEndWebs(x, z, f);
			break;
		case 2:
			this.planDeadEndTreasure(x, z, f);
			break;
		case 3:
			this.planDeadEndSpawner(x, z, f);
			break;
		case 4:
			this.planDeadEndPainting(x, z, f);
			break;
		case 5:
			this.planDeadEndTrap(x, z, f);
			break;
		case 6:
			this.planDeadEndTrappedChest(x, z, f);
			break;
		case 7:
			this.planDeadEndTorch(x, z, f);
			break;
		case 8:
			this.planDeadEndTorchRedstone(x, z, f);
			break;
		case 9:
			this.planDeadEndFountain(x, z, f);
			break;
		case 10:
			this.planDeadEndLavaFountain(x, z, f);
			break;
		case 11:
			this.planDeadEndDoorway(x, z, f);
			break;
		case 12:
			this.planDeadEndDoor(x, z, f);
			break;
		case 13:
			this.planDeadEndDoorSteel(x, z, f);
			break;
		case 14:
			this.planDeadEndDoorTreasure(x, z, f);
		}

	}

	/** The one decoration cell of a dead end, by facing. */
	private static int[] deadEndSpot(int dx, int dy, int dz, int f) {
		if(f == 0) {
			return new int[]{dx + 1, dy, dz + 2};
		} else if(f == 1) {
			return new int[]{dx, dy, dz + 1};
		} else if(f == 2) {
			return new int[]{dx + 1, dy, dz};
		} else {
			return new int[]{dx + 2, dy, dz + 1};
		}
	}

	private void planDeadEndSpiderSpawner(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.planDeadEndWebs(x, z, f);
		String spiderType = this.planRand.nextBoolean() ? "Spider" : "SwarmSpider";
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planSpawner(spot[0], spot[1], spot[2], spiderType);
	}

	private void planDeadEndWebs(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			this.planBlock(dx + 1, dy + 0, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 0, dy + 1, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 2, dy + 1, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 2, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 0, dy + 0, dz + 2, Block.web.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 2, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 1, dz + 2, Block.web.blockID, 0);
		} else if(f == 1) {
			this.planBlock(dx + 1, dy + 0, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 1, dz + 0, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 1, dz + 2, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 2, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 0, dy + 0, dz + 0, Block.web.blockID, 0);
			this.planBlock(dx + 0, dy + 0, dz + 2, Block.web.blockID, 0);
			this.planBlock(dx + 0, dy + 1, dz + 1, Block.web.blockID, 0);
		} else if(f == 2) {
			this.planBlock(dx + 1, dy + 0, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 0, dy + 1, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 2, dy + 1, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 2, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 0, dy + 0, dz + 0, Block.web.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 0, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 1, dz + 0, Block.web.blockID, 0);
		} else if(f == 3) {
			this.planBlock(dx + 1, dy + 0, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 1, dz + 0, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 1, dz + 2, Block.web.blockID, 0);
			this.planBlock(dx + 1, dy + 2, dz + 1, Block.web.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 0, Block.web.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 2, Block.web.blockID, 0);
			this.planBlock(dx + 2, dy + 1, dz + 1, Block.web.blockID, 0);
		}

	}

	private void planDeadEndTreasure(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planChest(spot[0], spot[1], spot[2], Treasure.underhill_deadend.rollLoot(this.planRand), false);
	}

	private void planDeadEndSpawner(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		String mobID = this.planRand.nextInt(3) == 0 ? "Skeleton" : "Redcap";
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planSpawner(spot[0], spot[1], spot[2], mobID);
	}

	private void planDeadEndPainting(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int anum = this.planRand.nextInt(7);
		EnumArt art = EnumArt.values()[anum];
		int px;
		int py = dy + 1;
		int pz;
		int direction;
		if(f == 0) {
			this.planBlock(dx + 0, dy + 1, dz + 2, Block.torchWood.blockID, 0);
			px = dx + 1;
			pz = dz + 3;
			direction = 0;
			this.planBlock(dx + 2, dy + 1, dz + 2, Block.torchWood.blockID, 0);
		} else if(f == 1) {
			this.planBlock(dx + 0, dy + 1, dz + 0, Block.torchWood.blockID, 0);
			px = dx - 1;
			pz = dz + 1;
			direction = 3;
			this.planBlock(dx + 0, dy + 1, dz + 2, Block.torchWood.blockID, 0);
		} else if(f == 2) {
			this.planBlock(dx + 0, dy + 1, dz + 0, Block.torchWood.blockID, 0);
			px = dx + 1;
			pz = dz - 1;
			direction = 2;
			this.planBlock(dx + 2, dy + 1, dz + 0, Block.torchWood.blockID, 0);
		} else {
			this.planBlock(dx + 2, dy + 1, dz + 0, Block.torchWood.blockID, 0);
			px = dx + 3;
			pz = dz + 1;
			direction = 1;
			this.planBlock(dx + 2, dy + 1, dz + 2, Block.torchWood.blockID, 0);
		}

		// Footprint validated against the plan (EntityPainting.onValidSurface's
		// world reads replaced by plan-air checks; torch claims read as solid).
		// A failed painting prints the original message right here at plan time.
		if(this.isFootprintSolid(px, py, pz, direction, art)) {
			this.specialBlocks.add(new SpecialBlockInStructurePainting(px, py, pz, direction, art.title));
		} else {
			System.out.println("Painting fail!! " + art.title + " at " + px + " , " + py + ", " + pz + " : " + direction);
		}
	}

	private void planDeadEndTrap(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.planBlock(dx + 1, dy + 0, dz + 1, Block.pressurePlateStone.blockID, 0);
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planBlock(spot[0], dy - 1, spot[2], Block.tnt.blockID, 0);
	}

	private void planDeadEndTrappedChest(int x, int z, int f) {
		this.planDeadEndTrap(x, z, f);
		this.planDeadEndTreasure(x, z, f);
	}

	private void planDeadEndTorch(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planBlock(spot[0], dy + 1, spot[2], Block.torchWood.blockID, 0);
	}

	private void planDeadEndTorchRedstone(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planBlock(spot[0], dy + 1, spot[2], Block.torchRedstoneActive.blockID, 0);
	}

	private void planDeadEndNook(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			this.planBlock(dx + 0, dy + 0, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 1, dy + 0, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 1, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 1, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 2, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 1, dy + 2, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 2, dz + 2, Block.stone.blockID, 0);
		} else if(f == 1) {
			this.planBlock(dx + 0, dy + 0, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 0, dz + 1, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 0, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 1, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 1, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 2, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 2, dz + 1, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 2, dz + 2, Block.stone.blockID, 0);
		} else if(f == 2) {
			this.planBlock(dx + 0, dy + 0, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 1, dy + 0, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 1, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 1, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 0, dy + 2, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 1, dy + 2, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 2, dz + 0, Block.stone.blockID, 0);
		} else if(f == 3) {
			this.planBlock(dx + 2, dy + 0, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 1, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 0, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 1, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 1, dz + 2, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 2, dz + 0, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 2, dz + 1, Block.stone.blockID, 0);
			this.planBlock(dx + 2, dy + 2, dz + 2, Block.stone.blockID, 0);
		}

	}

	private void planDeadEndFountain(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.planDeadEndNook(x, z, f);
		// Hole in the maze floor so the liquid feeds down.
		this.planBlock(dx + 1, dy - 1, dz + 1, 0, 0);
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planSimpleBlock(spot[0], dy + 1, spot[2], Block.waterMoving.blockID, 0);
	}

	private void planDeadEndLavaFountain(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.planDeadEndNook(x, z, f);
		this.planBlock(dx + 1, dy - 1, dz + 1, 0, 0);
		int[] spot = deadEndSpot(dx, dy, dz, f);
		this.planSimpleBlock(spot[0], dy + 1, spot[2], Block.lavaMoving.blockID, 0);
	}

	private void planDeadEndDoorway(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int wallID = this.maze.wallBlockID;
		int wallMeta = this.maze.wallBlockMeta;
		if(f == 0) {
			this.planBlock(dx + 0, dy + 0, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 0, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 1, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 1, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 2, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 1, dy + 2, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 2, dz + 0, wallID, wallMeta);
		} else if(f == 1) {
			this.planBlock(dx + 2, dy + 0, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 0, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 1, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 1, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 2, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 2, dz + 1, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 2, dz + 2, wallID, wallMeta);
		} else if(f == 2) {
			this.planBlock(dx + 0, dy + 0, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 0, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 1, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 1, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 2, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 1, dy + 2, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 2, dy + 2, dz + 2, wallID, wallMeta);
		} else if(f == 3) {
			this.planBlock(dx + 0, dy + 0, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 0, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 1, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 1, dz + 2, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 2, dz + 0, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 2, dz + 1, wallID, wallMeta);
			this.planBlock(dx + 0, dy + 2, dz + 2, wallID, wallMeta);
		}

	}

	/** Door lower half is planned before the upper half (flush order). */
	private void planDeadEndDoor(int x, int z, int f) {
		this.planDeadEndDoor(x, z, f, Block.doorWood.blockID);
	}

	private void planDeadEndDoorSteel(int x, int z, int f) {
		this.planDeadEndDoor(x, z, f, Block.doorSteel.blockID);
	}

	private void planDeadEndDoor(int x, int z, int f, int doorBlockID) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.planDeadEndDoorway(x, z, f);
		if(f == 0) {
			this.planSimpleBlock(dx + 1, dy + 0, dz + 0, doorBlockID, 1);
			this.planSimpleBlock(dx + 1, dy + 1, dz + 0, doorBlockID, 9);
		} else if(f == 1) {
			this.planSimpleBlock(dx + 2, dy + 0, dz + 1, doorBlockID, 2);
			this.planSimpleBlock(dx + 2, dy + 1, dz + 1, doorBlockID, 10);
		} else if(f == 2) {
			this.planSimpleBlock(dx + 1, dy + 0, dz + 2, doorBlockID, 3);
			this.planSimpleBlock(dx + 1, dy + 1, dz + 2, doorBlockID, 11);
		} else if(f == 3) {
			this.planSimpleBlock(dx + 0, dy + 0, dz + 1, doorBlockID, 0);
			this.planSimpleBlock(dx + 0, dy + 1, dz + 1, doorBlockID, 8);
		}

	}

	private void planDeadEndDoorTreasure(int x, int z, int f) {
		this.planDeadEndDoor(x, z, f);
		this.planDeadEndTreasure(x, z, f);
	}

	/** Port of WorldGenHillMaze.decorate3x3Rooms. */
	private void plan3x3Rooms(int[] roomCoords) {
		for(int i = 0; i < roomCoords.length / 2; i++) {
			this.plan3x3Room(roomCoords[i * 2], roomCoords[i * 2 + 1]);
		}
	}

	private void plan3x3Room(int x, int z) {
		int dx = this.maze.getWorldX(x) + 1;
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z) + 1;
		this.planRoomSpawner(dx, dy, dz, 11);
		if(!this.planRoomTreasure(dx, dy, dz, 11) || this.planRand.nextInt(2) == 0) {
			this.planRoomTreasure(dx, dy, dz, 11);
		}

		this.planRoomSpiderwebs(dx, dy, dz, 11);
	}

	private void planRoomSpawner(int dx, int dy, int dz, int diameter) {
		// No air check, as the original: spawners may sit inside walls.
		int rx = this.planRand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.planRand.nextInt(diameter) + dz - diameter / 2;
		this.planSpawner(rx, dy, rz, "Skeleton");
	}

	private boolean planRoomTreasure(int dx, int dy, int dz, int diameter) {
		int rx = this.planRand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.planRand.nextInt(diameter) + dz - diameter / 2;
		if(!this.isPlanAir(rx, dy, rz)) {
			return false;
		}
		this.planChest(rx, dy, rz, Treasure.underhill_room.rollLoot(this.planRand), false);
		return true;
	}

	private void planRoomSpiderwebs(int dx, int dy, int dz, int diameter) {
		int rx = dx;
		int rz = dz;
		int corner = this.planRand.nextInt(4);
		if(corner == 0) {
			rx = dx - 5;
			rz = dz - 5;
		} else if(corner == 1) {
			rx = dx + 5;
			rz = dz - 5;
		} else if(corner == 2) {
			rx = dx - 5;
			rz = dz + 5;
		} else if(corner == 3) {
			rx = dx + 5;
			rz = dz + 5;
		}

		// The original accumulated three return flags and never used them.
		this.planRoomSpiderweb(rx, dy, rz, 3);
		this.planRoomSpiderweb(rx, dy, rz, 3);
		this.planRoomSpiderweb(rx, dy, rz, 3);
	}

	private boolean planRoomSpiderweb(int dx, int dy, int dz, int diameter) {
		// RNG is consumed before the air check, as in the original.
		int rx = this.planRand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.planRand.nextInt(diameter) + dz - diameter / 2;
		if(!this.isPlanAir(rx, dy + 2, rz)) {
			return false;
		}
		this.planBlock(rx, dy + 2, rz, Block.web.blockID, 0);
		return true;
	}

	// ---------------------------------------------------------------------------
	// Plan helpers.
	// ---------------------------------------------------------------------------

	/** Raw-block plan entry written during terraform (non-special). */
	private void planBlock(int x, int y, int z, int blockID, int meta) {
		this.decorationBlocks.add(new BlockWrite(x, y, z, blockID, meta));
		if(blockID != 0) {
			this.claim(x, y, z);
		}
	}

	/** Non-block specials (doors, liquids) that flush during populate. */
	private void planSimpleBlock(int x, int y, int z, int blockID, int meta) {
		this.specialBlocks.add(new SpecialBlockInStructure(x, y, z, blockID, meta));
		if(blockID != 0) {
			this.claim(x, y, z);
		}
	}

	private void planChest(int x, int y, int z, ItemStack[] contents, boolean cobblePillar) {
		this.specialBlocks.add(new SpecialBlockInStructureChest(x, y, z, Block.chest.blockID, 0, contents, cobblePillar));
		this.claim(x, y, z);
	}

	private void planSpawner(int x, int y, int z, String mobID) {
		this.specialBlocks.add(new SpecialBlockInStructureSpawner(x, y, z, Block.mobSpawner.blockID, 0, mobID));
		this.claim(x, y, z);
	}

	private void claim(int x, int y, int z) {
		this.claimedPositions.add(claimKey(x, y, z));
	}

	private static long claimKey(int x, int y, int z) {
		return ((long)(x & 0x3FFFFFF) << 38) | ((long)(y & 0xFFF) << 26) | (long)(z & 0x3FFFFFF);
	}

	/**
	 * Plan-time air check used by the decoration placements: inside the maze
	 * box, on maze floor level, not a wall column, not already claimed by an
	 * earlier plan entry. Anything outside the plan reads as solid.
	 */
	private boolean isPlanAir(int x, int y, int z) {
		if(y < 48 || y > 50) return false;
		if(x < this.mazeBoxX || x >= this.mazeBoxX + this.mazeBoxSize) return false;
		if(z < this.mazeBoxZ || z >= this.mazeBoxZ + this.mazeBoxSize) return false;
		if(this.maze.hasWallBlockAt(x, z)) return false;
		return !this.claimedPositions.contains(claimKey(x, y, z));
	}

	/**
	 * Port of EntityPainting.setDirection/onValidSurface footprint math with
	 * the world reads replaced by plan-air checks (no world access).
	 */
	private boolean isFootprintSolid(int x, int y, int z, int direction, EnumArt art) {
		// Replicates setDirection's anchor position (pure math).
		float f5 = x + 0.5F;
		float f6 = y + 0.5F;
		float f7 = z + 0.5F;
		if(direction == 0) f7 -= 0.5625F;
		if(direction == 1) f5 -= 0.5625F;
		if(direction == 2) f7 += 0.5625F;
		if(direction == 3) f5 += 0.5625F;
		if(direction == 0) f5 -= artSize(art.sizeX);
		if(direction == 1) f7 += artSize(art.sizeX);
		if(direction == 2) f5 += artSize(art.sizeX);
		if(direction == 3) f7 -= artSize(art.sizeX);
		f6 += artSize(art.sizeY);

		int baseY = MathHelper.floor_double(f6 - art.sizeY / 32.0D);
		int baseX = x;
		int baseZ = z;
		if(direction == 0 || direction == 2) baseX = MathHelper.floor_double(f5 - art.sizeX / 32.0D);
		if(direction == 1 || direction == 3) baseZ = MathHelper.floor_double(f7 - art.sizeX / 32.0D);

		int width = art.sizeX / 16;
		int height = art.sizeY / 16;
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				int cellX = (direction != 0 && direction != 2) ? x : baseX + i;
				int cellZ = (direction != 0 && direction != 2) ? baseZ + i : z;
				if(this.isPlanAir(cellX, baseY + j, cellZ)) {
					return false;
				}
			}
		}
		return true;
	}

	/** Port of EntityPainting.getArtSize. */
	private static float artSize(int pixelSize) {
		return pixelSize == 32 ? 0.5F : (pixelSize == 64 ? 0.5F : 0.0F);
	}

	/**
	 * SplitMix64-style hash folded to [0, 1). Per-spike deterministic
	 * replacement for the original shared RNG column rolls.
	 */
	private static double hash01(long seed, int dx, int dz) {
		long h = seed ^ (0x9E3779B97F4A7C15L * (dx + 0x100000001L * dz));
		h ^= h >>> 30;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 27;
		h *= 0x9E3779B97F4A7C15L;
		h ^= h >>> 31;
		return (h >>> 11) * (1.0D / (double)(1L << 53));
	}

	@Override
	public int minimumSeparation() {
		return 1;
	}

	@Override
	public int getFeatureRadius() {
		return this.featureRadius;
	}

	@Override
	public int getSpawnChance() {
		return 6;
	}

	/** Raw block entry for terraform-time decoration writes. */
	private static class BlockWrite {
		final int x;
		final int y;
		final int z;
		final int blockID;
		final int metadata;

		BlockWrite(int x, int y, int z, int blockID, int metadata) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.blockID = blockID;
			this.metadata = metadata;
		}
	}

	/** Planned spike geometry, materialized per chunk during terraform. */
	private static class SpikePlan {
		final int x;
		final int z;
		final int blockID;
		final boolean hang;
		final int length;
		final long seed;

		SpikePlan(int x, int z, int blockID, boolean hang, int length, long seed) {
			this.x = x;
			this.z = z;
			this.blockID = blockID;
			this.hang = hang;
			this.length = length;
			this.seed = seed;
		}
	}
}
