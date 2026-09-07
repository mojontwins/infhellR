package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class WorldGenIcebergs extends WorldGenerator {

	public WorldGenIcebergs () {
	}
	
	private void raiseColumn (World world, Random rand, int x, int y, int z, int l) {
		for (; l > 0 && y < 127; l--) {
			world.setBlock(x, y ++, z, rand.nextInt(4) == 0 ? Block.blockSnow.blockID : Block.ice.blockID);
		}
	}
	
	private void spawnColumn (World world, Random rand, int x, int y, int z, int chance) {
		/*int blockId = world.getBlockId(x, y - 1, z);
		while (blockId == 0 && y > 1) {
			y --; blockId = world.getBlockId(x, y - 1, z);
		}
		*/
		while(!this.blockIsCube(world, x, y - 1, z) && y > 1) {
			y --;
		}
		if(y <= 1) return;
				
		// Raise this column
		raiseColumn (world, rand, x, y, z, 4 + rand.nextInt (6));
		
		// Spawn new columns
		/*
		if (rand.nextInt(chance) == 0) {
			chance ++;
			if (!this.blockIsCube(world, x - 1, y, z)) spawnColumn (world, rand, x - 1, y, z, chance);
			if (!this.blockIsCube(world, x + 1, y, z)) spawnColumn (world, rand, x + 1, y, z, chance);
			if (!this.blockIsCube(world, x, y, z - 1)) spawnColumn (world, rand, x, y, z - 1, chance);
			if (!this.blockIsCube(world, x, y, z + 1)) spawnColumn (world, rand, x, y, z + 1, chance);
		}
		*/
		if (rand.nextInt(chance) == 0) {
			if (!this.blockIsCube(world, x - 1, y, z)) spawnColumn (world, rand, x - 1, y, z, chance + 1);
		}
		if (rand.nextInt(chance) == 0) {
			if (!this.blockIsCube(world, x + 1, y, z)) spawnColumn (world, rand, x + 1, y, z, chance + 1);
		}
		if (rand.nextInt(chance) == 0) {
			if (!this.blockIsCube(world, x, y, z - 1)) spawnColumn (world, rand, x, y, z - 1, chance + 1);
		}
		if (rand.nextInt(chance) == 0) {
			if (!this.blockIsCube(world, x, y, z + 1)) spawnColumn (world, rand, x, y, z + 1, chance + 1);
		}
		
	}
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.spawnColumn (world, rand, x, y, z, 1 + rand.nextInt(4));
		return true;
	}

}
