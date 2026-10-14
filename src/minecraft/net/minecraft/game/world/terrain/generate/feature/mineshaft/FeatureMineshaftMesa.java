package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenMesa;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.Feature;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/**
 * The mesa mineshaft, reimplemented as a {@link Feature}.
 *
 * <p>It shares all of its skeleton with the regular {@link FeatureMineshaft} (the rooms,
 * corridors, crosses and stairs only differ by the mesa roll thresholds and the
 * environment-aware mesa corridor), but its placement differs:
 *
 * <ul>
 *   <li>it only spawns in a {@link BiomeGenMesa} (1 in 50 chunks);</li>
 *   <li>it sits above ground (base 66 + 0..31) and is never pushed underground, so the
 *       {@code markAvailableHeight} step of the regular shaft is omitted;</li>
 *   <li>every piece is drawn during population, gated on the surrounding terrain, so the
 *       whole structure is built in the world phase.</li>
 * </ul>
 */
public class FeatureMineshaftMesa extends Feature {
	private List<MineshaftComponent> components;
	private long planSeed;

	public FeatureMineshaftMesa(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public int getFeatureRadius() {
		return FeatureProvider.MAX_FEATURE_RADIUS;
	}

	/**
	 * The generic roll always passes; the real gate (mesa biome, 1 in 50) lives in
	 * {@link #shouldSpawn} so it is evaluated against the chunk's biome.
	 */
	@Override
	public int getSpawnChance() {
		return Feature.MAXCHANCE;
	}

	/** Draw range only, like the vanilla mesa shaft - it is happy to overlap structures. */
	@Override
	public int minimumSeparation() {
		return 0;
	}

	/** Pure gate (runs on the shared probe): vanilla {@code MapGenMineshaftMesa.canSpawnStructureAtCoords}. */
	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return biome instanceof BiomeGenMesa && rand.nextInt(50) == 0;
	}

	@Override
	public boolean isGeneratedAfterCaves() {
		return true;
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		this.planSeed = world.getRandomSeed() + this.originChunkX * 25117L + this.originChunkZ * 151121L;
		Random planRand = new Random(this.planSeed);

		// Mesa room: above ground, no markAvailableHeight - the structure is meant to sit
		// on the surface. Everything else mirrors the regular FeatureMineshaft.setup.
		MineshaftComponent room = new MineshaftComponentMesaRoom(this.world, 0, planRand, (this.originChunkX << 4) + 2, (this.originChunkZ << 4) + 2);
		room.setPieces(new MineshaftPiecesMesa(this.world));
		this.components = new ArrayList<MineshaftComponent>();
		this.components.add(room);
		room.buildComponent(room, this.components, planRand);

		StructureBoundingBox box = room.getBoundingBox();
		System.out.println("[MesaMS] @ " + box.getCenterX() + " " + box.getCenterY() + " " + box.getCenterZ());
	}

	/** The mesa draws every piece in the world phase, so the chunk phase does nothing. */
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
