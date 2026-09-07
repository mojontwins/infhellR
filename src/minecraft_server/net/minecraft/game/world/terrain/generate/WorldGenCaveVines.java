package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenCaveVines extends WorldGenerator {
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if(world.getBlockId(x, y, z) != Block.stone.blockID) return false;
		
		while(!world.isAirBlock(x, y, z) && y > 2) {
			y --;
		}
		
		if(y <= 2) return false;
		
		if(rand.nextBoolean()) this.throwVine(world, rand, x - 1, y, z);
		if(rand.nextBoolean()) this.throwVine(world, rand, x + 1, y, z);
		if(rand.nextBoolean()) this.throwVine(world, rand, x, y, z - 1);
		if(rand.nextBoolean()) this.throwVine(world, rand, x, y, z + 1);
		
		return true;
	}
	
	public void throwVine(World world, Random rand, int x, int y, int z) {
		int vineLength = 8 + rand.nextInt(8);
		for(int dy = 0; dy < vineLength; dy ++) {
			if(world.isAirBlock(x, y - dy, z)) {
				world.setBlock(x,  y - dy, z, Block.caveVine.blockID);
			}
		}
	}
}
