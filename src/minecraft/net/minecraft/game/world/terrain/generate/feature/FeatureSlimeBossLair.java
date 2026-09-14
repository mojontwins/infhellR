package net.minecraft.game.world.terrain.generate.feature;

import java.util.Random;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockArrayUtils;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawnerOneshot;
import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.WorldGenBigMushroom;

public class FeatureSlimeBossLair extends Feature {	
	public int chunkXb1;
	public int chunkXb2;
	public int chunkZb1;
	public int chunkZb2;
	
	public Random randSpheres;
	public Random randGeneral;
	
	public static final int NUMSPHERES = 40;

	public FeatureSlimeBossLair(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
		
		long seed = this.world.getRandomSeed() + originChunkX * 25117 + originChunkZ * 151121;
		this.randSpheres = new Random(seed);
		this.randGeneral = new Random(seed);
	}
	
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		System.out.println ("Slime Boss Lair @ " + this.centerX + " " + this.centerZ);
	}
	
	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		Chunk chunk = world.justGenerateForHeight(chunkX, chunkZ);
		return 
				!(chunkProvider instanceof ChunkProviderSky) && 
				!chunk.isOcean && 
				!chunk.isUrbanChunk;
	}

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		this.chunkXb1 = chunkX << 4;
		this.chunkXb2 = this.chunkXb1 + 15;
		this.chunkZb1 = chunkZ << 4;
		this.chunkZb2 = this.chunkZb1 + 15;
		
		BlockArrayUtils blockArrayUtils = new BlockArrayUtils(chunk.blocks, chunkX, chunkZ);

		// Generate spheres using a gaussian distribution centered around centerX, centerZ;
		// Radius is random, smaller as we dig further to the center.
		// Y position is always 32 with a small variance.
		
		boolean chunkWasChanged = false;
		
		for(int i = 0; i < NUMSPHERES; i ++) {
			int cX = (int)(this.randSpheres.nextGaussian() * 40);
			if(this.randSpheres.nextBoolean()) cX = -cX;
			cX += this.centerX;
			
			int cZ = (int)(this.randSpheres.nextGaussian() * 40);
			if(this.randSpheres.nextBoolean()) cZ = -cZ;
			cZ += this.centerZ;			
			
			int cY = this.randSpheres.nextInt(4) + 30;
			
			int dX = Math.abs(cX - this.centerX);
			int dZ = Math.abs(cZ - this.centerZ);
			int d = Math.max(dX, dZ);
			
			int radius = 20 - (d >> 1);
			
			chunkWasChanged |= this.carveSphere(world, cX, cY, cZ, radius, 0, chunk.blocks, blockArrayUtils);
		}
		
		if(chunkWasChanged) this.replaceBlocks(world, chunk.blocks);
		
		// Build the monument on surface
		if(chunkX == this.originChunkX && chunkZ == this.originChunkZ - 2) {
			
			// Use height map to calculate min height in this chunk central area
			int minHeight = 129;
			for(int x = 4; x < 12; x ++) {
				for(int z = 4; z < 12; z ++) {
					int h = chunk.landSurfaceHeightMap[z << 4 | x] & 0xff;
					if(h < minHeight) minHeight = h;
				}
			}
			
			// Draw base
			int y = minHeight;
			
			// System.out.println("Built monument @ around " + (16 * chunkX + 8) + ", " + y + ", " + (16 * chunkZ + 8));
			
			for(int i = 0; i < 2; i ++) {
				for(int x = 4 + i; x < 12 - i; x ++) {
					for(int z = 4 + i; z < 12 - i; z ++) {
						chunk.blocks[x << 11 | z << 7 | y] = (byte)(this.randGeneral.nextInt(4) == 0 ? Block.cobblestoneMossy.blockID : Block.cobblestone.blockID);
					}
				}
				++ y;
			}
			
			for(int i = 0; i < 4; i ++) {
				for(int x = 6; x < 10; x ++) {
					for(int z = 6; z < 10; z ++) {
						chunk.blocks[x << 11 | z << 7 | y] = (byte)Block.stoneBricks.blockID;
					}
				}
				++ y;
			}
			
			y -= 2; 
			chunk.blocks[6 << 11 | 9 << 7 | y] = (byte)Block.blockCoal.blockID;
			chunk.blocks[9 << 11 | 9 << 7 | y] = (byte)Block.blockCoal.blockID;
		}
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		/*
		if(chunkX == this.originChunkX && chunkZ == this.originChunkZ) {
			// Central lamp thingy
			int y = 32;	while(y ++ < 64 && world.getBlockId(this.centerX, y, this.centerZ) == 0);
			
			for(int x = -1; x <= 1; x ++) {
				for(int z = -1; z <= 1; z ++) {
					this.glowySpikey(world, rand, x + this.centerX, y - 1, z + this.centerZ, (x == 0 && z == 0));
				}
			}
		}
		*/

		if(chunkX == this.originChunkX && chunkZ == this.originChunkZ) {
			// Set up teh boss spawner
			int y = 32; while(y > 1 && world.getBlockId(this.centerX, -- y, this.centerZ) == 0);
			
			for(int i = 0; i < 3; i ++) {
				for(int x = this.centerX - 6 + i; x <= this.centerX + 6 - i; x ++) {
					for(int z = this.centerZ - 6 + i; z <= this.centerZ + 6 - i; z ++) {
						world.setBlock(x, y, z, Block.cobblestoneMossy.blockID);
					}
				}
				y ++;
				world.setBlock(this.centerX - 6 + i, y, this.centerZ - 6 + i, Block.torchWood.blockID);
				world.setBlock(this.centerX - 6 + i, y, this.centerZ + 6 - i, Block.torchWood.blockID);
				world.setBlock(this.centerX + 6 - i, y, this.centerZ - 6 + i, Block.torchWood.blockID);
				world.setBlock(this.centerX + 6 - i, y, this.centerZ + 6 - i, Block.torchWood.blockID);
			}
			
			world.setBlockAndMetadata(this.centerX, y, this.centerZ, Block.mobSpawnerOneshot.blockID, 8);
			TileEntityMobSpawnerOneshot tileEntityMobSpawner = (TileEntityMobSpawnerOneshot)world.getBlockTileEntity(this.centerX, y, this.centerZ);
			
			if (tileEntityMobSpawner != null) {
				tileEntityMobSpawner.setMobID("SlimeBoss");
				// System.out.println("Slime boss spawner set");
			}
		}
		
		int x0 = chunkX << 4;
		int z0 = chunkZ << 4;
		int amount = 4 + rand.nextInt(8);
		for(int i = 0; i < amount; i ++) {
			int x = x0 + rand.nextInt(16);
			int z = z0 + rand.nextInt(16);
			int y = 32 - rand.nextInt(22);
			(new WorldGenBigMushroom(2)).generate(world, rand, x, y, z);
		}
		
	}
	
	public void replaceBlocks(World world, byte[] data) {
		int index;
		for(int x = 0; x < 16; x ++) {
			for(int z = 0; z < 16; z ++) {
				if(this.randGeneral.nextInt(16) == 0) {		
					index = x << 11 | z << 7 | 54;
					
					for(int y = 0; y < 20; y ++) {
						if(data[index] == 0 && data[index + 1] != 0) {
							data[index + 1] = (byte)Block.glowStone.blockID;
							break;
						}
						index --;
					}
				}
				
				
				index = x << 11 | z << 7 | 10;
				
				for(int y = 0; y < 20; y ++) {
					if(data[index] == 0 && data[index - 1] != 0) {
						data[index - 1] = (byte)Block.grass.blockID;
						if(this.randGeneral.nextInt(8) == 0) {
							data[index] = (byte)Block.glowshroom.blockID;
						}
						break;
					}
					index ++;
				}
			
			}
		}
	}
	
	public void glowySpikey(World world, Random rand, int x, int y0, int z, boolean center) {
		int length = (center ? 12 : 8) + rand.nextInt(6);
		for(int y = y0; y > y0 - length; y --) {
			world.setBlockWithNotify(x, y, z, rand.nextInt(3) == 0 ? Block.glass.blockID : Block.shinyGlass.blockID);
		}
	}
	
	public boolean carveSphere(World world, int x0, int y0, int z0, int radius, int id, byte[] data, BlockArrayUtils blockArrayUtils) {

		// First: return if sphere is completely outside of this chunk.
		int testX = x0;
		int testZ = z0;
		if(x0 <= this.chunkXb1) testX = this.chunkXb1;
		else if(x0 >= this.chunkXb2) testX = this.chunkXb2;
		
		if(z0 <= this.chunkZb1) testZ = this.chunkZb1;
		else if(z0 >= this.chunkZb2) testZ = this.chunkZb2;
		
		int dx = x0 - testX;
		int dz = z0 - testZ;
		int distanceSquared = dx * dx + dz * dz;
		
		if(distanceSquared > radius * radius) return false;
		
		// Draw sphere
		blockArrayUtils.drawSphereAbsolute(x0, y0, z0, radius, id);
		return true;
	}

	@Override
	public int getFeatureRadius() {
		return 3;
	}

	@Override
	public int getSpawnChance() {
		return 1;
	}
	
	public int minimumSeparation() {
		return 16;
	}
}
