package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenCactus extends WorldGenerator {
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		for(int attempts = 0; attempts < 16; ++attempts) {
			int x = x0 + rand.nextInt(8) - rand.nextInt(8);
			int y = y0 + rand.nextInt(4) - rand.nextInt(4);
			int z = z0 + rand.nextInt(8) - rand.nextInt(8);
			
			/*
			if(world.getBlockId(x, y, z) == 0) {
				int i10 = 1 + rand.nextInt(rand.nextInt(3) + 1);

				for(int i11 = 0; i11 < i10; ++i11) {
					int yy = y + i11;
					
					if(world.getBlockId(x, yy, z) == Block.layeredSand.blockID || Block.cactus.canBlockStay(world, x, yy, z)) {
						world.setBlock(x, yy, z, Block.cactus.blockID);
					}
				}
			}
			*/
			
			if(world.getBlockId(x, y, z) == Block.layeredSand.blockID) {
				world.setBlockAndMetadata(x, y, z, 0, 0);
					}
			if(world.getBlockId(x, y, z) == 0 && world.getBlockId(x, y - 1, z) == Block.sand.blockID) {
				int length = 1 + rand.nextInt(5);
				for(int i = y; i < length + y; i ++) {
					if(Block.cactus.canBlockStay(world, x, i, z)) world.setBlock(x, i, z, Block.cactus.blockID);
				}
			}
		}

		return true;
	}
}
