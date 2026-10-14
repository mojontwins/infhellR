package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.Feature;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * A mineshaft, reimplemented as a {@link Feature}.
 *
 * <p>{@link #setup} plans the whole deterministic skeleton (rooms, corridors, crosses and
 * stairs) on a world-seeded RNG without reading a single world block. The normal/shell
 * blocks are then laid during the after-caves generation pass, writing straight into the
 * chunk's flat generation buffers; the world-dependent and special blocks (arches vs.
 * chains, rails, torches, cobwebs, chests and spawners) are applied during population.
 *
 * <p>Every piece is assigned a seed derived from its own footprint, so a piece draws the
 * exact same blocks no matter which chunk - or how many times - it is rendered for.
 */
public class FeatureMineshaft extends Feature {
	/** Roughly one mineshaft per 100 candidate chunks (the vanilla 1-in-100 roll). */
	private static final int SPAWN_CHANCE = Feature.MAXCHANCE / 100;

	private List<MineshaftComponent> components;
	private long planSeed;

	public FeatureMineshaft(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public int getFeatureRadius() {
		return FeatureProvider.MAX_FEATURE_RADIUS;
	}

	@Override
	public int getSpawnChance() {
		return SPAWN_CHANCE;
	}

	/**
	 * The mineshaft draws across a wide radius (corridors reach ~8 chunks), but that is a
	 * DRAW range, not a keep-out range. Like the vanilla shaft it is happy to overlap
	 * other structures, so give it no separation from them. Inheriting the default
	 * ({@link #getFeatureRadius()} = 8) would effectively veto the shaft almost everywhere,
	 * because some other feature is nearly always within 8 chunks.
	 */
	@Override
	public int minimumSeparation() {
		return 0;
	}

	/**
	 * Pure gate (runs on the shared probe): the classic vanilla mineshaft rule - denser
	 * the further the chunk is from the origin, so shafts cluster away from spawn.
	 */
	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
	}

	@Override
	public boolean isGeneratedAfterCaves() {
		return true;
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		this.planSeed = world.getRandomSeed() + this.originChunkX * 25117L + this.originChunkZ * 151121L;
		Random planRand = new Random(this.planSeed);

		MineshaftComponent room = new MineshaftComponentRoom(this.world, 0, planRand, (this.originChunkX << 4) + 2, (this.originChunkZ << 4) + 2);
		room.setPieces(new MineshaftPieces(this.world, 70));
		this.components = new ArrayList<MineshaftComponent>();
		this.components.add(room);
		room.buildComponent(room, this.components, planRand);

		// Port of StructureStart.updateBoundingBox + markAvailableHeight(world, rand, 10):
		// union every piece footprint, then shift the whole shaft vertically so it stays
		// buried under the surface (the draw keeps consuming planRand exactly like the
		// vanilla generator did).
		StructureBoundingBox union = StructureBoundingBox.getNewBoundingBox();
		for(int i = 0; i < this.components.size(); ++i) {
			union.expandTo(this.components.get(i).getBoundingBox());
		}

		int maxY = 63 - 10;
		int structureHeight = union.getYSize() + 1;
		if(structureHeight < maxY) {
			structureHeight += planRand.nextInt(maxY - structureHeight);
		}
		int verticalOffset = structureHeight - union.maxY;
		for(int i = 0; i < this.components.size(); ++i) {
			this.components.get(i).getBoundingBox().offset(0, verticalOffset, 0);
		}
	}

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		if(this.components == null) {
			return;
		}

		StructureBoundingBox chunkBox = new StructureBoundingBox(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 255, (chunkZ << 4) + 15);
		MineshaftWriter writer = new MineshaftWriter(chunk, false);
		for(int i = 0; i < this.components.size(); ++i) {
			MineshaftComponent component = this.components.get(i);
			if(component.getBoundingBox().intersectsWith(chunkBox)) {
				component.generate(writer, new Random(this.pieceSeed(component)));
			}
		}
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		if(this.components == null) {
			return;
		}

		Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		StructureBoundingBox chunkBox = new StructureBoundingBox(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 255, (chunkZ << 4) + 15);
		MineshaftWriter writer = new MineshaftWriter(chunk, true);
		for(int i = 0; i < this.components.size(); ++i) {
			MineshaftComponent component = this.components.get(i);
			if(component.getBoundingBox().intersectsWith(chunkBox)) {
				component.populate(writer, new Random(this.pieceSeed(component)));
			}
		}
	}

	/** Deterministic per-piece seed derived from the piece footprint and type. */
	private long pieceSeed(MineshaftComponent component) {
		StructureBoundingBox box = component.getBoundingBox();
		long seed = this.planSeed;
		seed = seed * 31L + box.minX;
		seed = seed * 31L + box.minY;
		seed = seed * 31L + box.minZ;
		seed = seed * 31L + box.maxX;
		seed = seed * 31L + box.maxY;
		seed = seed * 31L + box.maxZ;
		seed = seed * 31L + component.getComponentType();
		return seed;
	}
	
	@Override
	public boolean exclusive() {
		return false;
	}
}
