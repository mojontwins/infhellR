package net.minecraft.game.world.terrain.generate;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.World;

public class WorldGenPopsicleshroom extends WorldGenMojon {

	public WorldGenPopsicleshroom(boolean withNotify) {
		super(withNotify);
	}

	@Override
	public boolean generate(World world, Random rand, int x0, int y0, int z0) {
		int radius = 2 + rand.nextInt(3);
		int height = 3 + rand.nextInt(3);
		
		// on solid?
		if(!world.isBlockOpaqueCube(x0, y0 - 1, z0)) return false;
		if(world.getBlockId(x0, y0 - 1, z0) == Block.glass.blockID) return false;
		
		// Fits?
		int r = 0;
		for(int yy = 0; yy < radius * 2 + 1 + height; yy ++) {
			if (yy > height) r = radius;
			int y = yy + y0;
			
			for(int x = x0 - r; x <= x0 + r; x ++) {
				for(int z = z0 - r; z <= z0 + r; z ++) {
					if(world.isBlockOpaqueCube(x, y, z)) return false;
				}
			}
		}
		
		// trunk
		for(int y = y0; y < y0 + radius * 2 + height; y ++ ) {
			this.setBlockWithMetadata(world, x0, y, z0, Block.mushroomCapBrown.blockID, 10);
		}
		
		// Blob
		this.roundedShape(world, x0, y0 + height + radius, z0, radius, Block.glass.blockID, 0, true);
		
		return false;
	}

}
