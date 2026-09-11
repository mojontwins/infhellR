package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;

import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.terrain.generate.WorldGenerator;

public class WorldGenHugeTrees extends WorldGenerator
{
	// Code adapted from release 1.2.5
	// so it works in a1.2.6 with vanilla blocks!
	// https://github.com/SMEZ1234/EnhancedBiomes/
	
	private static final int leavesId = Block.leaves.blockID;
	private static final int woodId = Block.wood.blockID;
	private static final int leavesMeta = EnumTreeType.JUNGLE.getLeafMetadata();
	
	private int height;

	public WorldGenHugeTrees(int height) {
		this.height = height;
	}

	public boolean generate(World world, Random rand, int x, int y, int z) {
		int i = rand.nextInt(3) + height;
		boolean flag = true;

		if (y < 1 || y + i + 1 > Chunk.SECTION_HEIGHT) {
			return false;
		}

		for (int j = y; j <= y + 1 + i; j++) {
			byte radius = 2;

			if (j == y) {
				radius = 1;
			}

			if (j >= (y + 1 + i) - 2) {
				radius = 2;
			}

			for (int xx = x - radius; xx <= x + radius && flag; xx++) {
				for (int zz = z - radius; zz <= z + radius && flag; zz++) {
					if (j >= 0 && j < Chunk.SECTION_HEIGHT) {
						int k2 = world.getBlockId(xx, j, zz);

						if (k2 != 0 && k2 != Block.leaves.blockID && k2 != Block.grass.blockID && k2 != Block.dirt.blockID && k2 != woodId && k2 != Block.sapling.blockID) {
							flag = false;
						}
					} else {
						flag = false;
					}
				}
			}
		}

		if (!flag) {
			return false;
		}
		
		// These trees can grow even in water, but only up to 4 blocks deep
		int diveFor = 4;
			
		while (y > 32) {		
			if(
				world.getBlockId(x, y - 1, z) != Block.waterMoving.blockID &&
				world.getBlockId(x + 1, y - 1, z + 1) != Block.waterMoving.blockID &&
				world.getBlockId(x + 1, y - 1, z) != Block.waterMoving.blockID &&
				world.getBlockId(x, y - 1, z + 1) != Block.waterMoving.blockID
			) break;
			y --;
			diveFor --;
			if(diveFor == 0) return false;
		}

		int blockID = world.getBlockId(x, y - 1, z);
		if (blockID != Block.grass.blockID && blockID != Block.dirt.blockID || y >= 256 - i - 1) {
			return false;
		}

		world.setBlock(x, y - 1, z, Block.dirt.blockID);
		world.setBlock(x + 1, y - 1, z, Block.dirt.blockID);
		world.setBlock(x, y - 1, z + 1, Block.dirt.blockID);
		world.setBlock(x + 1, y - 1, z + 1, Block.dirt.blockID);
		this.draw_leaves(world, x, z, y + i, 2, rand);

		for (int l = (y + i) - 2 - rand.nextInt(4); l > y + i / 2; l -= 2 + rand.nextInt(4)) {
			float f = rand.nextFloat() * (float)Math.PI * 2.0F;
			int l1 = x + (int)(0.5F + MathHelper.cos(f) * 4F);
			int l2 = z + (int)(0.5F + MathHelper.sin(f) * 4F);
			this.draw_leaves(world, l1, l2, l, 0, rand);

			for (int j3 = 0; j3 < 5; j3++) {
				int i2 = x + (int)(1.5F + MathHelper.cos(f) * (float)j3);
				int i3 = z + (int)(1.5F + MathHelper.sin(f) * (float)j3);
				world.setBlock(i2, (l - 3) + j3 / 2, i3, woodId);
			}
		}

		for (int j1 = 0; j1 < i; j1++) {
			blockID = world.getBlockId(x, y + j1, z);

			if (blockID == 0 || blockID == leavesId) {
				world.setBlock(x, y + j1, z, woodId);

				if (j1 > 0) {
					if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x - 1, y + j1, z)) {
						world.setBlockAndMetadata(x - 1, y + j1, z, Block.vine.blockID, 8);
					}

					if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x, y + j1, z - 1)) {
						world.setBlockAndMetadata(x, y + j1, z - 1, Block.vine.blockID, 1);
					}
				}
			}

			if (j1 >= i - 1) {
				continue;
			}

			blockID = world.getBlockId(x + 1, y + j1, z);

			if (blockID == 0 || blockID == leavesId) {
				world.setBlock(x + 1, y + j1, z, woodId);

				if (j1 > 0) {
					if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x + 2, y + j1, z)) {
						world.setBlockAndMetadata(x + 2, y + j1, z, Block.vine.blockID, 2);
					}

					if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x + 1, y + j1, z - 1)) {
						world.setBlockAndMetadata(x + 1, y + j1, z - 1, Block.vine.blockID, 1);
					}
				}
			}

			blockID = world.getBlockId(x + 1, y + j1, z + 1);

			if (blockID == 0 || blockID == leavesId) {
				world.setBlock(x + 1, y + j1, z + 1, woodId);

				if (j1 > 0) {
					if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x + 2, y + j1, z + 1)) {
						world.setBlockAndMetadata(x + 2, y + j1, z + 1, Block.vine.blockID, 2);
					}

					if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x + 1, y + j1, z + 2)) {
						world.setBlockAndMetadata(x + 1, y + j1, z + 2, Block.vine.blockID, 4);
					}
				}
			}

			blockID = world.getBlockId(x, y + j1, z + 1);

			if (blockID != 0 && blockID != leavesId) {
				continue;
			}

			world.setBlock(x, y + j1, z + 1, woodId);

			if (j1 <= 0) {
				continue;
			}

			if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x - 1, y + j1, z + 1)) {
				world.setBlockAndMetadata(x - 1, y + j1, z + 1, Block.vine.blockID, 8);
			}

			if (rand.nextInt(3) > 0 && 0 == world.getBlockId(x, y + j1, z + 2))
			{
				world.setBlockAndMetadata(x, y + j1, z + 2, Block.vine.blockID, 4);
			}
		}

		return true;
	}

	private void draw_leaves(World world, int x, int z, int y, int frnd, Random par6Random) {
		byte radius = 2;

		for (int i = y - radius; i <= y; i++) {
			int j = i - y;
			int k = (frnd + 1) - j;

			for (int l = x - k; l <= x + k + 1; l++) {
				int i1 = l - x;

				for (int j1 = z - k; j1 <= z + k + 1; j1++) {
					int k1 = j1 - z;

					int i1sq = i1 * i1; int k1sq = k1 * k1;

					if ((i1 >= 0 || k1 >= 0 || i1sq + k1sq <= k * k) && (i1 <= 0 && k1 <= 0 || i1sq + k1sq <= (k + 1) * (k + 1)) && (par6Random.nextInt(4) != 0 || i1sq + k1sq <= (k - 1) * (k - 1)) && !Block.opaqueCubeLookup[world.getBlockId(l, i, j1)]) {
						world.setBlockAndMetadata(l, i, j1, leavesId, leavesMeta);
					}
				}
			}
		}
	}
}
