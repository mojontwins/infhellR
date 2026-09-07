package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenIgloos extends WorldGenerator {
	private static final int blockSnow = Block.blockSnow.blockID;
	private static final int blockWood = Block.wood.blockID;
	private static final int blockCloth = Block.cloth.blockID;
	private static final int blockWater = Block.waterStill.blockID;
	
	public WorldGenIgloos () {
	}
	
	private boolean canBePlaced (World world, int x, int y, int z) {
		int zeros = 0;
		for (int xx = x - 3; xx <= x + 3; xx ++) {
			for (int zz = z - 3; zz <= z + 3; zz ++) {
				if (world.getBlockId(xx, y - 1, zz) == 0) zeros ++;
				
				if ((xx == x - 3 || xx == x + 3) && (zz <= z - 2 || zz >= z + 2) ||
					(zz == x - 3 || zz == x + 3) && (xx <= z - 2 || zz >= z + 2)) continue;
				
				if (world.getBlockId(xx, y, zz) != 0) return false;
				if (world.getBlockId(xx, y - 1, zz) == blockWater) return false;	
			}
		}
		
		if(zeros > 20) return false;
		
		return true;
	}
	
	private void pillarDown (World world, int x, int y, int z) {
		while (y > 0) {
			y --;
			if (world.getBlockId(x, y, z) != 0) return;
			world.setBlock(x, y, z, blockSnow);
		}
	}
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (!canBePlaced (world, x, y, z)) return false;
		
		for (int xx = x - 3; xx <= x + 3; xx ++) {
			for (int zz = z - 3; zz <= z + 3; zz ++) {
				if ((xx == x - 3 || xx == x + 3) && (zz <= z - 2 || zz >= z + 2) ||
					(zz == x - 3 || zz == x + 3) && (xx <= z - 2 || zz >= z + 2)) continue;
				pillarDown(world, xx, y, zz);
			}
		}
		
		for (int i = 0; i < 3; i ++) {
			int yy = y + i;
					
			// Pillars / borders
			world.setBlock(x - 2, yy, z - 2, blockSnow);
			world.setBlock(x + 2, yy, z - 2, blockSnow);
			world.setBlock(x - 2, yy, z + 2, blockSnow);
			world.setBlock(x + 2, yy, z + 2, blockSnow);
			
			world.setBlock(x - 1 + i, y + 3, z - 2, blockSnow);
			world.setBlock(x - 1 + i, y + 3, z + 2, blockSnow);
			world.setBlock(x - 2, y + 3, z - 1 + i, blockSnow);
			world.setBlock(x + 2, y + 3, z - 1 + i, blockSnow);

			// Walls & ceiling
			for (int j = 0; j < 3; j ++) {
				world.setBlock(x - 1 + j, yy, z - 3, blockSnow);
				world.setBlock(x - 1 + j, yy, z + 3, blockSnow);
				world.setBlock(x - 3, yy, z - 1 + j, blockSnow);
				world.setBlock(x + 3, yy, z - 1 + j, blockSnow);
				world.setBlock(x - 1 + i, y + 4, z - 1 + j, blockSnow);
			}
		}
		
		// Door
		world.setBlock(x + 3, y, z, 0);
		world.setBlock(x + 3, y + 1, z, 0);
		
		// Random wood & cloth
		for (int i = 0; i < 3; i ++) {
			int h = 1 + rand.nextInt(3);
			for (int j = 0; j < h; j ++) {
				world.setBlock (x - 2, y + j, z - 1 + i, blockWood);
				world.setBlockAndMetadata(x - 1 + i, y + j, z - 2, blockCloth, 1+rand.nextInt(14));
			}
		}
		
		return true;
	}

}
