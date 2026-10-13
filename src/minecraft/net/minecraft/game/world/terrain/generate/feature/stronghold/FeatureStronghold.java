package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenDarkForest;
import net.minecraft.game.world.biome.BiomeGenFlowerFields;
import net.minecraft.game.world.biome.BiomeGenGlacier;
import net.minecraft.game.world.biome.BiomeGenMangrove;
import net.minecraft.game.world.biome.BiomeGenMycelium;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.feature.structure.FeatureStructure;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureRoomPool;

/**
 * Underground stronghold. The gate is the vanilla {@code nextInt(2048) == 0} roll (with the
 * same five biomes blacklisted); the whole room graph is grown breadth-first from a surface
 * stairwell root.
 */
public class FeatureStronghold extends FeatureStructure {
	public FeatureStronghold(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
		this.mostlySolid = true;
		this.queueExpansion = true;
		this.maxBuildDepth = 50;
	}

	@Override
	public StructureRoomPool createRoomPool() {
		return new StrongholdRoomPool();
	}

	@Override
	public StructurePiece createRoot(Random rand, BiomeGenBase biome) {
		return new StrongholdStairsPiece(this.world, rand, (this.originChunkX << 4) + 2, (this.originChunkZ << 4) + 2);
	}

	@Override
	public int minimumSeparation() {
		return 8;
	}

	@Override
	public int getSpawnChance() {
		// The real roll lives in shouldSpawn (it depends on the biome); this keeps the
		// provider's generic chance roll neutral.
		return MAXCHANCE;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return rand.nextInt(2048) == 0 && !isBlacklisted(biome);
	}

	private static boolean isBlacklisted(BiomeGenBase biome) {
		return biome instanceof BiomeGenDarkForest
				|| biome instanceof BiomeGenFlowerFields
				|| biome instanceof BiomeGenGlacier
				|| biome instanceof BiomeGenMangrove
				|| biome instanceof BiomeGenMycelium;
	}
}
