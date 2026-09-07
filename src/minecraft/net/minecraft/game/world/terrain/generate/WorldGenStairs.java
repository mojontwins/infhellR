package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;

public class WorldGenStairs extends WorldGenerator {
	private int rotation;
	
	public WorldGenStairs(int rotation) {
		this.rotation = rotation;
	}
	
	@Override
	public boolean generate(World world, Random random, int x0, int y0, int z0) {
		switch (this.rotation) {
			case 0:
				// Rotation 1 (x, z)
				world.setBlock(x0 + 2, y0 + 1, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 3, y0 + 1, z0 + 0, 53, 1);
				world.setBlock(x0 + 0, y0 + 1, z0 + 1, 85);
				world.setBlock(x0 + 4, y0 + 1, z0 + 1, 85);
				world.setBlock(x0 + 1, y0 + 2, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 2, z0 + 0, 53, 1);
				world.setBlock(x0 + 0, y0 + 2, z0 + 1, 85);
				world.setBlock(x0 + 4, y0 + 2, z0 + 1, 85);
				world.setBlock(x0 + 0, y0 + 3, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 1, y0 + 3, z0 + 0, 53, 1);
				world.setBlock(x0 + 0, y0 + 3, z0 + 1, 45);
				world.setBlock(x0 + 1, y0 + 3, z0 + 1, 45);
				world.setBlock(x0 + 4, y0 + 3, z0 + 1, 85);
				world.setBlock(x0 + 1, y0 + 4, z0 + 1, 53);
				world.setBlock(x0 + 2, y0 + 4, z0 + 1, 45);
				world.setBlock(x0 + 4, y0 + 4, z0 + 1, 85);
				world.setBlock(x0 + 2, y0 + 5, z0 + 1, 53);
				world.setBlock(x0 + 3, y0 + 5, z0 + 1, 45);
				world.setBlock(x0 + 4, y0 + 5, z0 + 1, 85);
				world.setBlock(x0 + 3, y0 + 6, z0 + 0, 45);
				world.setBlock(x0 + 4, y0 + 6, z0 + 0, 45);
				world.setBlock(x0 + 3, y0 + 6, z0 + 1, 53);
				world.setBlock(x0 + 4, y0 + 6, z0 + 1, 45);
				world.setBlock(x0 + 2, y0 + 7, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 3, y0 + 7, z0 + 0, 53, 1);
				world.setBlock(x0 + 1, y0 + 8, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 8, z0 + 0, 53, 1);
				world.setBlock(x0 + 0, y0 + 9, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 1, y0 + 9, z0 + 0, 53, 1);
				
				world.setBlock(x0 + 0, y0 + 10, z0 - 1, 0);
				world.setBlock(x0 + 1, y0 + 10, z0 - 1, 0);
				break;
			case 1:
				// Rotation 2 (-z, x)
				world.setBlock(x0 + 0, y0 + 1, z0 + 3, 45);
				world.setBlockAndMetadata(x0 + 0, y0 + 1, z0 + 2, 53, 2);
				world.setBlock(x0 + 1, y0 + 1, z0 + 5, 85);
				world.setBlock(x0 + 1, y0 + 1, z0 + 1, 85);
				world.setBlock(x0 + 0, y0 + 2, z0 + 4, 45);
				world.setBlockAndMetadata(x0 + 0, y0 + 2, z0 + 3, 53, 2);
				world.setBlock(x0 + 1, y0 + 2, z0 + 5, 85);
				world.setBlock(x0 + 1, y0 + 2, z0 + 1, 85);
				world.setBlock(x0 + 0, y0 + 3, z0 + 5, 45);
				world.setBlockAndMetadata(x0 + 0, y0 + 3, z0 + 4, 53, 2);
				world.setBlock(x0 + 1, y0 + 3, z0 + 5, 45);
				world.setBlock(x0 + 1, y0 + 3, z0 + 4, 45);
				world.setBlock(x0 + 1, y0 + 3, z0 + 1, 85);
				world.setBlockAndMetadata(x0 + 1, y0 + 4, z0 + 4, 53, 3);
				world.setBlock(x0 + 1, y0 + 4, z0 + 3, 45);
				world.setBlock(x0 + 1, y0 + 4, z0 + 1, 85);
				world.setBlockAndMetadata(x0 + 1, y0 + 5, z0 + 3, 53, 3);
				world.setBlock(x0 + 1, y0 + 5, z0 + 2, 45);
				world.setBlock(x0 + 1, y0 + 5, z0 + 1, 85);
				world.setBlock(x0 + 0, y0 + 6, z0 + 2, 45);
				world.setBlock(x0 + 0, y0 + 6, z0 + 1, 45);
				world.setBlockAndMetadata(x0 + 1, y0 + 6, z0 + 2, 53, 3);
				world.setBlock(x0 + 1, y0 + 6, z0 + 1, 45);
				world.setBlock(x0 + 0, y0 + 7, z0 + 3, 45);
				world.setBlockAndMetadata(x0 + 0, y0 + 7, z0 + 2, 53, 2);
				world.setBlock(x0 + 0, y0 + 8, z0 + 4, 45);
				world.setBlockAndMetadata(x0 + 0, y0 + 8, z0 + 3, 53, 2);
				world.setBlock(x0 + 0, y0 + 9, z0 + 5, 45);
				world.setBlockAndMetadata(x0 + 0, y0 + 9, z0 + 4, 53, 2);
				
				world.setBlock(x0 - 1, y0 + 10, z0 + 5, 0);
				world.setBlock(x0 - 1, y0 + 10, z0 + 4, 0);
				break;
			case 2:
				// Rotation 3 (-x, -z)
				world.setBlock(x0 + 3, y0 + 1, z0 + 2, 45);
				world.setBlock(x0 + 2, y0 + 1, z0 + 2, 53);
				world.setBlock(x0 + 5, y0 + 1, z0 + 1, 85);
				world.setBlock(x0 + 1, y0 + 1, z0 + 1, 85);
				world.setBlock(x0 + 4, y0 + 2, z0 + 2, 45);
				world.setBlock(x0 + 3, y0 + 2, z0 + 2, 53);
				world.setBlock(x0 + 5, y0 + 2, z0 + 1, 85);
				world.setBlock(x0 + 1, y0 + 2, z0 + 1, 85);
				world.setBlock(x0 + 5, y0 + 3, z0 + 2, 45);
				world.setBlock(x0 + 4, y0 + 3, z0 + 2, 53);
				world.setBlock(x0 + 5, y0 + 3, z0 + 1, 45);
				world.setBlock(x0 + 4, y0 + 3, z0 + 1, 45);
				world.setBlock(x0 + 1, y0 + 3, z0 + 1, 85);
				world.setBlockAndMetadata(x0 + 4, y0 + 4, z0 + 1, 53, 1);
				world.setBlock(x0 + 3, y0 + 4, z0 + 1, 45);
				world.setBlock(x0 + 1, y0 + 4, z0 + 1, 85);
				world.setBlockAndMetadata(x0 + 3, y0 + 5, z0 + 1, 53, 1);
				world.setBlock(x0 + 2, y0 + 5, z0 + 1, 45);
				world.setBlock(x0 + 1, y0 + 5, z0 + 1, 85);
				world.setBlock(x0 + 2, y0 + 6, z0 + 2, 45);
				world.setBlock(x0 + 1, y0 + 6, z0 + 2, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 6, z0 + 1, 53, 1);
				world.setBlock(x0 + 1, y0 + 6, z0 + 1, 45);
				world.setBlock(x0 + 3, y0 + 7, z0 + 2, 45);
				world.setBlock(x0 + 2, y0 + 7, z0 + 2, 53);
				world.setBlock(x0 + 4, y0 + 8, z0 + 2, 45);
				world.setBlock(x0 + 3, y0 + 8, z0 + 2, 53);
				world.setBlock(x0 + 5, y0 + 9, z0 + 2, 45);
				world.setBlock(x0 + 4, y0 + 9, z0 + 2, 53);
				
				world.setBlock(x0 + 4, y0 + 10, z0 + 3, 0);
				world.setBlock(x0 + 5, y0 + 10, z0 + 3, 0);
				break;
			case 3:
				// Rotation 4 (-z, x)
				world.setBlock(x0 + 2, y0 + 1, z0 + 2, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 1, z0 + 3, 53, 3);
				world.setBlock(x0 + 1, y0 + 1, z0 + 0, 85);
				world.setBlock(x0 + 1, y0 + 1, z0 + 4, 85);
				world.setBlock(x0 + 2, y0 + 2, z0 + 1, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 2, z0 + 2, 53, 3);
				world.setBlock(x0 + 1, y0 + 2, z0 + 0, 85);
				world.setBlock(x0 + 1, y0 + 2, z0 + 4, 85);
				world.setBlock(x0 + 2, y0 + 3, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 3, z0 + 1, 53, 3);
				world.setBlock(x0 + 1, y0 + 3, z0 + 0, 45);
				world.setBlock(x0 + 1, y0 + 3, z0 + 1, 45);
				world.setBlock(x0 + 1, y0 + 3, z0 + 4, 85);
				world.setBlockAndMetadata(x0 + 1, y0 + 4, z0 + 1, 53, 2);
				world.setBlock(x0 + 1, y0 + 4, z0 + 2, 45);
				world.setBlock(x0 + 1, y0 + 4, z0 + 4, 85);
				world.setBlockAndMetadata(x0 + 1, y0 + 5, z0 + 2, 53, 2);
				world.setBlock(x0 + 1, y0 + 5, z0 + 3, 45);
				world.setBlock(x0 + 1, y0 + 5, z0 + 4, 85);
				world.setBlock(x0 + 2, y0 + 6, z0 + 3, 45);
				world.setBlock(x0 + 2, y0 + 6, z0 + 4, 45);
				world.setBlockAndMetadata(x0 + 1, y0 + 6, z0 + 3, 53, 2);
				world.setBlock(x0 + 1, y0 + 6, z0 + 4, 45);
				world.setBlock(x0 + 2, y0 + 7, z0 + 2, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 7, z0 + 3, 53, 3);
				world.setBlock(x0 + 2, y0 + 8, z0 + 1, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 8, z0 + 2, 53, 3);
				world.setBlock(x0 + 2, y0 + 9, z0 + 0, 45);
				world.setBlockAndMetadata(x0 + 2, y0 + 9, z0 + 1, 53, 3);
				
				world.setBlock(x0 + 3, y0 + 10, z0 + 0, 0);
				world.setBlock(x0 + 3, y0 + 10, z0 + 1, 0);
				break;
			}
		return true;
	}

}
