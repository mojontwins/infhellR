package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.WorldGenerator;

public class WorldGenStreetLight extends WorldGenerator {
	private boolean broken;
	
	public WorldGenStreetLight(boolean broken) {
		this.broken = broken;
	}
	
	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if(block != null && block.isOpaqueCube()) return false;
		
		y--;
		world.setBlock(x, y ++, z, Block.stone.blockID);
		world.setBlock(x, y ++, z, Block.streetLanternFence.blockID);
		world.setBlock(x, y ++, z, Block.streetLanternFence.blockID);
		world.setBlock(x, y ++, z, Block.streetLanternFence.blockID);
		world.setBlock(x, y, z, this.broken ? Block.streetLanternBroken.blockID : Block.streetLantern.blockID);
		return true;
	}

}
