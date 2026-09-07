package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenLilypad extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		for (int i = 0; i < 10; i ++) {
			int x = (x0 + rand.nextInt(8)) - rand.nextInt(8);
			int y = (y0 + rand.nextInt(4)) - rand.nextInt(4);
			int z = (z0 + rand.nextInt(8)) - rand.nextInt(8);
			
			if(world.getBlockId(x, y, z) == 0 && Block.lilyPad.canPlaceBlockAt(world, x, y, z)) {
				world.setBlock(x, y, z, Block.lilyPad.blockID);
			}
		}

		return false;
	}

}
