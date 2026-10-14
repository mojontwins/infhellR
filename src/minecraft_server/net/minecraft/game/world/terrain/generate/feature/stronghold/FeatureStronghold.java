package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenDarkForest;
import net.minecraft.game.world.biome.BiomeGenFlowerFields;
import net.minecraft.game.world.biome.BiomeGenGlacier;
import net.minecraft.game.world.biome.BiomeGenMangrove;
import net.minecraft.game.world.biome.BiomeGenMycelium;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.Feature;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * A stronghold, reimplemented as a {@link Feature}.
 *
 * <p>{@link #setup} plans the whole deterministic skeleton - the entrance stairwell plus
 * every corridor, room, crossing, library and prison attached to it - on a world-seeded
 * RNG, reading only the local land surface for the entrance height. The pieces are then
 * drawn during the population phase (like the vanilla stronghold, which overwrote the
 * caves it intersected), each piece gated on the terrain around it.
 *
 * <p>Every piece is assigned a seed derived from its own footprint, so a piece draws the
 * exact same blocks no matter which chunk - or how many times - it is rendered for.
 */
public class FeatureStronghold extends Feature {
	/** Vanilla {@code MapGenStronghold.chance}: one in 2048 chunks passes the probe. */
	private static final int SPAWN_CHANCE = 2048;

	private List<MineshaftComponent> components;
	private long planSeed;

	public FeatureStronghold(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public int getFeatureRadius() {
		return FeatureProvider.MAX_FEATURE_RADIUS;
	}

	/**
	 * The generic roll always passes; the real gate (a 1-in-2048 roll plus the biome
	 * exclusion list) lives in {@link #shouldSpawn} so it is evaluated against the chunk's
	 * biome, exactly like the vanilla {@code MapGenStronghold}.
	 */
	@Override
	public int getSpawnChance() {
		return Feature.MAXCHANCE;
	}

	/** Draw range only - a stronghold is happy to overlap other structures. */
	@Override
	public int minimumSeparation() {
		return 0;
	}

	/**
	 * Pure gate (runs on the shared probe): port of
	 * {@code MapGenStronghold.canSpawnStructureAtCoords}. The vanilla stateful 64-chunk
	 * keep-out between strongholds is intentionally dropped - it is not expressible through
	 * the Feature separation system, and dense strongholds are harmless.
	 */
	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		if(rand.nextInt(SPAWN_CHANCE) != 0) {
			return false;
		}
		return !(biome instanceof BiomeGenDarkForest
				|| biome instanceof BiomeGenFlowerFields
				|| biome instanceof BiomeGenGlacier
				|| biome instanceof BiomeGenMangrove
				|| biome instanceof BiomeGenMycelium);
	}

	@Override
	public boolean isGeneratedAfterCaves() {
		return true;
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		this.planSeed = world.getRandomSeed() + this.originChunkX * 25117L + this.originChunkZ * 151121L;
		Random planRand = new Random(this.planSeed);

		// Port of StructureStrongholdStart: build the entrance stairwell, then keep
		// expanding the queue of child pieces until it is exhausted.
		StrongholdComponentStairs2 start = new StrongholdComponentStairs2(this.world, 0, planRand, (this.originChunkX << 4) + 2, (this.originChunkZ << 4) + 2);
		start.setFactory(new StrongholdPieces(this.world));
		this.components = new ArrayList<MineshaftComponent>();
		this.components.add(start);
		start.buildComponent(start, this.components, planRand);

		while(!start.children.isEmpty()) {
			int index = planRand.nextInt(start.children.size());
			MineshaftComponent child = start.children.remove(index);
			child.buildComponent(start, this.components, planRand);
		}
	}

	/** The stronghold draws every piece in the world phase, so the chunk phase does nothing. */
	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
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
}
