package net.minecraft.game.world.feature;

import java.util.Random;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureSinkHole extends Feature {
	public static final int maxpoints = 24;
	
	public int[][] shape;
	FeatureSinkHole.SimplePoint [] points = new FeatureSinkHole.SimplePoint [maxpoints];
	
	public FeatureSinkHole(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getFeatureRadius() {
		return 3;
	}

	@Override
	public int getSpawnChance() {
		return 1;
	}
	
	@Override
	public int minimumSeparation() {
		return 16;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX,
			int chunkZ) {
		return world.isOceanChunk(chunkX, chunkZ);
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		int dOffset = 16;
		int dOffsetSq = dOffset * dOffset;
		
		// Create a blank big array and generate a shape
		int size = (1 + 2 * this.getFeatureRadius()) << 4;
		this.shape = new int[size][size];
		
		// Some points at random
		long seed = this.world.getRandomSeed() + originChunkX * 25117 + originChunkZ * 151121;
		Random randPoints = new Random(seed);
		
		int dispersion = this.getFeatureRadius() * 12;
		int center = size >> 1;
		
		for(int i = 0; i < maxpoints; i ++) {
			this.points[i] = new FeatureSinkHole.SimplePoint(
					center + randPoints.nextInt(2 * dispersion) - dispersion,
					center + randPoints.nextInt(2 * dispersion) - dispersion
				);
		}
		
		for(int x = 0; x < size; x ++) {
			for(int z = 0; z < size; z ++) {
				for(FeatureSinkHole.SimplePoint point : this.points) {
					if(point.distanceSq(x, z) < dOffsetSq) {
						this.shape[x][z] = 1;
					}
				}
			}
		}
		
		System.out.println ("Sink Hole @ " + this.centerX + " " + this.centerZ);
	}
	
	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		// Erode terrain based upon this.shape.
		int x1 = (chunkX - this.originChunkX + this.getFeatureRadius()) << 4;
		int z1 = (chunkZ - this.originChunkZ + this.getFeatureRadius()) << 4;
						
		int index = 0;
		for(int x = 0; x < 16; x ++) {
			for(int z = 0; z < 16; z ++) {
				
				// This assumes marked cells don't reach the borders of the array!!
				
				if(this.shape[x1 + x][z1 + z] == 1) {
					int heightMapIndex = z << 4 | x;
					
					int total = 0;
					int filled = 0;
					int r = 5;
					for(int i = -r; i <= r; i ++) {
						for(int j = -r; j <= r; j ++) {
							total ++;
							filled += this.shape[x1 + x + i][z1 + z + j];
						}
					}
					
					float strength = (float)filled / (float)total;
					
					// Land surface height map ignores water so this may be well under 64!
					int height = chunk.landSurfaceHeightMap[heightMapIndex];
					
					// Sink!
					int newHeight = (int) (height / (2.0F * strength * strength * strength));
					
					// newHeight < height so fill with air:
					for(int y = newHeight; y < 128; y ++) {
						byte b = (y == 63 && strength < 1.0F) ? (byte)Block.waterMoving.blockID : 0;
						chunk.blocks[index + y] = b;
					}
					
					chunk.landSurfaceHeightMap[heightMapIndex] = (byte) newHeight;
				}
				
				index += 128;
			}
		}
		
		// Pretty
		chunk.hasUnderwaterRuin = false;
		chunk.isOcean = false;
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
	}

	public class SimplePoint {
		public final int x;
		public final int y;
		
		public SimplePoint(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int distanceSq(int x, int y) {
			int dx = Math.abs(this.x - x);
			int dy = Math.abs(this.y - y);
			return dx * dx + dy * dy;
		}

		public int taxyDistanceFrom(int x, int y) {
			return Math.abs(this.x - x) + Math.abs(this.y - y);
		}
	}
}
