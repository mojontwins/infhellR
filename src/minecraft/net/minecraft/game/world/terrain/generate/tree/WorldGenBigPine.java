package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.WorldGenerator;

public class WorldGenBigPine extends WorldGenerator {
	private static final int leavesId = Block.leaves.blockID;
	private static final int trunkId = Block.wood.blockID;
	
	public static final int leavesMeta = 7; 	// Seasonal colorizer
	
	private int height;

	public void drawLeavesPlatform(World world, Random rand, int x0, int y, int z0, int size) {
		int dz = rand.nextInt(3) - 1;
		for(int z = 0; z < size + dz; z ++) {
			int dx = rand.nextInt(3) - 1;
			for(int x = 0; x < size - z + dx; x ++) {
				this.setBlockIfEmpty(world, x0 + x, y, z0 + z, leavesId, WorldGenBigPine.leavesMeta);
				this.setBlockIfEmpty(world, x0 + x, y, z0 - 1 - z, leavesId, WorldGenBigPine.leavesMeta);
				this.setBlockIfEmpty(world, x0 - 1 - x, y, z0 + z, leavesId, WorldGenBigPine.leavesMeta);
				this.setBlockIfEmpty(world, x0 - 1 - x, y, z0 - 1 - z, leavesId, WorldGenBigPine.leavesMeta);
			}
		}
		
		for(int z = -(size + 3) + 1; z < size + 3; z ++) {
			for (int x = -(size + 3) + 1; x < size + 3; x ++) {
				if(rand.nextInt(64) == 0 && world.getBlockId(x + x0, y, z + z0) == leavesId) {
					world.setBlock(x + x0, y, z + z0, Block.wood.blockID);
				}
			}
		}
	}
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.height = 8 + rand.nextInt(8);
		if (y + this.height > 126) return false;
		
		// Limit height:	
		/*
		int blockID;
		limitHeight: {
			for(int yy = 0; yy < this.height; yy++) {
				for(int xx = 0; xx < 2; xx++) {
					for(int zz = 0; zz < 2; zz++) {
						blockID = world.getBlockId(x + zz, y + yy, z + xx);
						if(blockID != 0 && blockID != leavesId) {
							this.height = yy - 1;
							break limitHeight;
						}
					}
				}
			}
		}
		*/

		// Check soil 
		
		Block soilBlock = Block.blocksList[world.getBlockId(x, y - 1, z)];
		if (!(soilBlock != null && soilBlock.canGrowPlants())) return false;

		soilBlock = Block.blocksList[world.getBlockId(x, y - 1, z + 1)];
		if (!(soilBlock != null && soilBlock.canGrowPlants())) return false;

		soilBlock = Block.blocksList[world.getBlockId(x + 1, y - 1, z)];
		if (!(soilBlock != null && soilBlock.canGrowPlants())) return false;
		
		soilBlock = Block.blocksList[world.getBlockId(x + 1, y - 1, z + 1)];
		if (!(soilBlock != null && soilBlock.canGrowPlants())) return false;

		// Canopy half vertical size
		int canopyHalfSize = 3 + rand.nextInt(3);
		
		int x0 = x + 1; 
		int z0 = z + 1;
		int size = 2 + rand.nextInt(2);
		int yy = y + this.height;
		
		// Top half, canopy increases randomly, center offset moves around.
		for(int i = 0; i < canopyHalfSize; i ++) {
			this.drawLeavesPlatform(world, rand, x0, yy --, z0, size);
			size += 1 + rand.nextInt(3);
			x0 += (rand.nextInt(3) - 1);
			z0 += (rand.nextInt(3) - 1);
		}
		
		int branchesY = yy; 
		int branchesSize = size - 2;
		
		// Bottom half, canopy decreases randomly
		for(int i = 0; i < canopyHalfSize; i ++) {
			this.drawLeavesPlatform(world, rand, x0, yy --, z0, size);
			size -= 1 + rand.nextInt(3); if (size < 3) size = 3;
			x0 += (rand.nextInt(3) - 1);
			z0 += (rand.nextInt(3) - 1);
		}
		
		for(int i = 1; i < branchesSize; i ++) {
			if(world.getBlockId(x - i, branchesY, z + 1 + i) == leavesId) world.setBlock(x - i, branchesY, z + 1 + i, trunkId); 
			if(world.getBlockId(x + 1 + i, branchesY, z + 1 + i) == leavesId) world.setBlock(x + 1 + i, branchesY, z + 1 + i, trunkId); 
			if(world.getBlockId(x - i, branchesY, z - i) == leavesId) world.setBlock(x - i, branchesY, z - i, trunkId); 
			if(world.getBlockId(x + 1 + i, branchesY, z - i) == leavesId) world.setBlock(x + 1 + i, branchesY, z - i, trunkId); 
		}
		

		//Trunk
		for(int i = 0; i < this.height; i++) {
			world.setBlock(x, y + i, z, trunkId);
			world.setBlock(x + 1, y + i, z, trunkId);
			world.setBlock(x, y + i, z + 1, trunkId);
			world.setBlock(x + 1, y + i, z + 1, trunkId);
		}
		
		// Roots
		for(int xx = 0; xx < 4; xx ++) {
			for(int zz = 0; zz < 4; zz ++) {
				if (xx < 1 || xx > 2 || zz < 1 || zz > 2) {
					int rh = 1 + rand.nextInt(4);
					for (yy = 0; yy < rh; yy ++) {
						world.setBlock(x - 1 + xx, y + yy, z - 1 + zz, trunkId);
					}
				}
			}
		}

		return true;
	
	}

}
