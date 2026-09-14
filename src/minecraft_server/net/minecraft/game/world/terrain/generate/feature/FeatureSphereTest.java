package net.minecraft.game.world.terrain.generate.feature;

import java.util.Random;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockArrayUtils;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureSphereTest extends Feature {

	public FeatureSphereTest(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider)  {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		BlockArrayUtils blockArrayUtils = new BlockArrayUtils(chunk.blocks, chunkX, chunkZ);
		blockArrayUtils.drawSphereAbsolute(this.centerX, 80, this.centerZ, 24, Block.glass.blockID);
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFeatureRadius() {
		return 1;
	}

	@Override
	public int getSpawnChance() {
		return 0;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome,
			int chunkX, int chunkZ) {
		return false;
	}

}
