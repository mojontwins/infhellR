package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
public class WorldGenPalmTree1 extends WorldGenerator {
	private final int leavesBlock = Block.leaves.blockID;
	private final int leavesMeta = 0;
	private final int woodBlock = Block.wood.blockID;
	private final int woodMeta = 0;
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		while (world.isAirBlock(x, y, z) && y > 2) {
			--y;
		}

		int blockID = world.getBlockId(x, y, z);

		if (blockID != Block.sand.blockID ) {
			return false;
		} else {
			for(int yy = 8; yy < 10; yy ++) {
				for (int var7 = -2; var7 <= 2; ++var7) {
					for (int var8 = -2; var8 <= 2; ++var8) {
						if(!world.isAirBlock(x + var7, y + yy, z + var8)) return false;
					}
				}
			}

			// settings========
			double strength = rand.nextInt(35) / 100D;
			// ================

			double xoff = 0;
			double yoff = 0;
			int r = rand.nextInt(4);
			if (r == 0) {
				xoff = strength;
			} else if (r == 1) {
				xoff = -strength;
			} else if (r == 2) {
				yoff = strength;
			} else {
				yoff = -strength;
			}

			int h = 1;
			buildBlock(world, x, y, z, Block.dirt.blockID, 0);
			for (int b = 0; b < 10; b++) {
				buildBlock(world, x + ((int) Math.floor(xoff)), y + h, z + ((int) Math.floor(yoff)),
						this.woodBlock, this.woodMeta);
				if (b == 9) {
					generateTop(world, x + ((int) Math.floor(xoff)), y + h, z + ((int) Math.floor(yoff)));
				} else {
					h++;
					xoff *= 1.3D;
					yoff *= 1.3D;
				}
			}

			return true;
		}
	}

	public void generateTop(World world, int x, int y, int z) {
		// x
		// 
		// xx
		// 
		// x
		buildBlock(world, x + 2, y - 1, z, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x - 2, y - 1, z, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x, y - 1, z + 2, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x, y - 1, z - 2, this.leavesBlock, this.leavesMeta);

		// xx
		// x
		// xx
		// x
		// xx
		buildBlock(world, x + 1, y, z, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x - 1, y, z, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x, y, z + 1, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x, y, z - 1, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x + 2, y, z + 2, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x - 2, y, z - 2, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x + 2, y, z - 2, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x - 2, y, z + 2, this.leavesBlock, this.leavesMeta);

		// 
		// xx
		// x
		// xx
		// 
		buildBlock(world, x + 1, y + 1, z - 1, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x - 1, y + 1, z + 1, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x + 1, y + 1, z + 1, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x - 1, y + 1, z - 1, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x, y + 1, z, this.leavesBlock, this.leavesMeta);

		// x
		// 
		// xx
		// 
		// x
		buildBlock(world, x + 2, y + 2, z, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x - 2, y + 2, z, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x, y + 2, z + 2, this.leavesBlock, this.leavesMeta);
		buildBlock(world, x, y + 2, z - 2, this.leavesBlock, this.leavesMeta);
	}

	public void buildBlock(World world, int x, int y, int z, int id, int meta) {
		Material m = world.getBlockMaterial(x, y, z);
		if (m == Material.air || m == Material.leaves || m == Material.vine || m == Material.plants) {
			world.setBlockAndMetadata(x, y, z, id, meta);
		}
	}
}
