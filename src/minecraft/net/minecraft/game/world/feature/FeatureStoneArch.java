package net.minecraft.game.world.feature;

import java.util.Arrays;
import java.util.Random;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenRocky;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureStoneArch extends FeatureDynamicSchematic {
	int calculatedY0 = 64;
	
	public FeatureStoneArch(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public int getFeatureHeight() {
		return 64;
	}

	@Override
	public void generateSchematic(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		// Fill the whole schematic with -1s
		
		for(short[][] arr1: this.schematic) {
			for(short[] arr2: arr1) {
				Arrays.fill(arr2, (short)-1);
			}
		}
				
		float radius = 12F + rand.nextInt(6);
		float f = 0.13F + rand.nextFloat() * 0.04F;
		int maxHeight = (int) (radius * radius * f);		
		
		int width = (1 + this.getFeatureRadius() * 2) * 16;
		
		int x0 = width >> 1;
		int z0 = width >> 1;
		
		float angle = (float) (rand.nextFloat() * Math.PI);
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		
		for(float r = (int) -radius; r <= radius; r += 0.5F) {
			int x = (int) (r * cos);
			int z = (int) (r * sin);
			
			float height = r * r * f;
			float sphrR = 1 + 3F * Math.abs(r / radius) + rand.nextInt(3);

			this.sphere(x + x0, 6 + maxHeight - (int)height, z + z0, (int)sphrR, (short)Block.stone.blockID);
			
			if(rand.nextFloat() < 0.2F) {
				this.corneeze(rand, x + x0 + rand.nextInt(8) - 4, 6 + maxHeight - (int)height, z + z0 + rand.nextInt(8) - 4, 4 + rand.nextInt(2), (short)Block.stone.blockID, 3);
			}
			
		}
		
		// Sink
		int xx = (int) (radius * cos);
		int zz = (int) (radius * sin); 
		int h1 = world.getLandSurfaceHeightValue(this.centerX - xx, this.centerZ - zz);
		int h2 = world.getLandSurfaceHeightValue(this.centerX + xx, this.centerZ + zz);
		
		this.calculatedY0 = Math.min(128 - maxHeight, Math.min(h1, h2) - 3);
		
		System.out.println ("Arch @ " + this.centerX + " " + this.calculatedY0  + " " + this.centerZ);
	}

	public void corneeze(Random rand, int x0, int y0, int z0, int r, short s, int variance) {
		int rSq = r * r;
		for(int x = 0; x < r; x ++) {
			int xx = x * x;
			for(int z = 0; z < r; z ++) {
				int zz = z * z;
				for(int y = 0; y < r; y ++) {
					if(y * y + xx + zz <= (float)rSq - rand.nextInt(variance)) {

						this.schematic[x0 + x][z0 + z][y0 - y] = s;
						this.schematic[x0 - x][z0 + z][y0 - y] = s;
						this.schematic[x0 + x][z0 - z][y0 - y] = s;
						this.schematic[x0 - x][z0 - z][y0 - y] = s;
					}
				}
			}
		}
	}
	
	@Override
	public int getY0() {
		return this.calculatedY0;
	}

	@Override
	public int getFeatureRadius() {
		return 1;
	}

	@Override
	public int getSpawnChance() {
		return 500;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX,
			int chunkZ) {
		return biome instanceof BiomeGenRocky;
	}

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		this.drawPieceOnGeneration(chunkX, chunkZ, chunk);
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
	}
	
	@Override
	public int minimumSeparation() {
		return 1;
	}
}
