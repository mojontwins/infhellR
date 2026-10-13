package net.minecraft.game.world.terrain.generate.feature.mineshaftmesa;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenMesa;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.feature.structure.FeatureStructure;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureRoomPool;

/**
 * Surface mineshaft that only spawns in mesa biomes ({@code nextInt(50) == 0}). Because it
 * sits above ground its rooms demand solid surrounding terrain and it pins itself to the
 * mesa's own height range instead of burrowing underground.
 */
public class FeatureMineshaftMesa extends FeatureStructure {
	public FeatureMineshaftMesa(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
		this.mostlySolid = true;
	}

	@Override
	public StructureRoomPool createRoomPool() {
		return new MineshaftMesaRoomPool();
	}

	@Override
	public StructurePiece createRoot(Random rand, BiomeGenBase biome) {
		return new MesaRoomPiece(rand, (this.originChunkX << 4) + 2, (this.originChunkZ << 4) + 2);
	}

	@Override
	public int getSpawnChance() {
		// The real roll lives in shouldSpawn (it depends on the biome and coordinates).
		return MAXCHANCE;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return biome instanceof BiomeGenMesa && rand.nextInt(50) == 0;
	}
}
