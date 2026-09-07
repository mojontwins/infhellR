package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenLiquids extends WorldGenerator {
	private int liquidBlockId;

	public WorldGenLiquids(int liquidBlockID) {
		this.liquidBlockId = liquidBlockID;
	}

	public boolean generate(World world, Random rand, int x, int y, int z) {
		if(world.getBlockId(x, y + 1, z) != Block.stone.blockID) {
			return false;
		} else if(world.getBlockId(x, y - 1, z) != Block.stone.blockID) {
			return false;
		} else if(world.getBlockId(x, y, z) != 0 && world.getBlockId(x, y, z) != Block.stone.blockID) {
			return false;
		} else {
			int i6 = 0;
			if(world.getBlockId(x - 1, y, z) == Block.stone.blockID) {
				++i6;
			}

			if(world.getBlockId(x + 1, y, z) == Block.stone.blockID) {
				++i6;
			}

			if(world.getBlockId(x, y, z - 1) == Block.stone.blockID) {
				++i6;
			}

			if(world.getBlockId(x, y, z + 1) == Block.stone.blockID) {
				++i6;
			}

			int i7 = 0;
			if(world.isAirBlock(x - 1, y, z)) {
				++i7;
			}

			if(world.isAirBlock(x + 1, y, z)) {
				++i7;
			}

			if(world.isAirBlock(x, y, z - 1)) {
				++i7;
			}
			
			if(world.isAirBlock(x, y, z + 1)) {
				++i7;
			}

			if(i6 == 3 && i7 == 1) {
				world.setBlockWithNotify(x, y, z, this.liquidBlockId);
				world.scheduledUpdatesAreImmediate = true;
				Block.blocksList[this.liquidBlockId].updateTick(world, x, y, z, rand);
				world.scheduledUpdatesAreImmediate = false;
			}

			return true;
		}
	}
}
