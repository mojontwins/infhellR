package net.minecraft.game.world.terrain.generate.feature;

import java.util.Random;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureTest extends Feature {
	public static final int radiusSquared = 24 * 24; 
	public static final int innerRadiusSquared = 8 * 8;		
	
	public FeatureTest(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider)  {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		int rx = (chunkX << 4) - this.centerX;
		int index = 0;
		
		for(int x = 0; x < 16; x ++) {
			int rz = (chunkZ << 4) - this.centerZ;
			for(int z = 0; z < 16; z ++) {
				int d = rx*rx + rz*rz;
				if(d > innerRadiusSquared && d < radiusSquared) {
					// Make a hole!
					int height = chunk.heightMap[z << 4 | x];
					for(int y = height / 3; y <= height; y ++) {
						chunk.blocks[index + y] = 0;
					}
				}
				rz ++;
				index += 128;
			}
			rx ++;			
		}
	}
	
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		
	}

	@Override
	public int getFeatureRadius() {
		return 2;
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
