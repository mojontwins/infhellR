package net.minecraft.game.world.terrain.generate.tree;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.terrain.generate.WorldGenerator;

public class WorldGenBog1 extends WorldGenerator {
	public static int leavesBlockID =  Block.leaves.blockID;
	public static int leavesMeta = 0;
	public static int woodBlockID = Block.wood.blockID;
	public static int woodMeta = 0;

	public static int EWmeta = 4;
	public static int NSmeta = 8;
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int height;

		for (
				height = rand.nextInt(5) + 7; 
				world.getBlockMaterial(x, y - 1,z) == Material.water; 
				--y
		) {
			;
		}

		boolean validTree = true;

		if (y >= 1 && y + height + 1 <= 128) {

			for (int yy = y; yy <= y + 1 + height; ++yy) {
				byte radius = 1;

				if (yy == y) {
					radius = 0;
				}

				if (yy >= y + 1 + height - 2) {
					radius = 3;
				}

				for (int xx = x - radius; xx <= x + radius && validTree; ++xx) {
					for (int zz = z - radius; zz <= z + radius && validTree; ++zz) {
						if (yy >= 0 && yy < 128) {
							int blockID = world.getBlockId(xx, yy, zz);

							if (blockID != 0 && blockID != WorldGenBog1.leavesBlockID) {
								if (blockID != Block.waterStill.blockID && blockID != Block.waterMoving.blockID) {
									validTree = false;
								} else if (yy > y) {
									validTree = false;
								}
							}
						} else {
							validTree = false;
						}
					}
				}
			}

			if (!validTree) {
				return false;
			} else {
				int blockID = world.getBlockId(x, y - 1, z);

				if ((blockID == Block.grass.blockID || blockID == Block.dirt.blockID) && y < 128 - height - 1) {
					world.setBlock(x, y - 1, z, Block.dirt.blockID);
					
					for (int yy = y - 3 + height; yy <= y + height; ++yy) {
						int canopyHeight = yy - (y + height);
						int canopyRadius = 3 - canopyHeight;

						for (int xx = x - canopyRadius; xx <= x + canopyRadius; ++xx) {
							int dx = xx - x;

							for (int zz = z - canopyRadius; zz <= z + canopyRadius; ++zz) {
								int dz = zz - z;

								if ((Math.abs(dx) != canopyRadius || Math.abs(dz) != canopyRadius
										|| rand.nextInt(2) != 0 && canopyHeight != 0)
										&& !Block.opaqueCubeLookup[world.getBlockId(xx, yy, zz)]) {
									world.setBlock(xx, yy, zz, WorldGenBog1.leavesBlockID);
								}
							}
						}
					}

					for (int yy = 0; yy < height; ++yy) {
						blockID = world.getBlockId(x, y + yy, z);

						if (blockID == 0 || blockID == WorldGenBog1.leavesBlockID || blockID == Block.waterMoving.blockID
								|| blockID == Block.waterStill.blockID) {
							world.setBlockAndMetadata(x, y + yy, z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 1, y + yy, z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 1, y + yy, z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x, y + yy, z - 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x, y + yy, z + 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 1, y, z - 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 1, y, z + 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 1, y, z + 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 1, y, z - 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 1, y + 1, z - 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 1, y + 1, z + 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 1, y + 1, z + 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 1, y + 1, z - 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 2, y, z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.EWmeta);
							world.setBlockAndMetadata(x + 2, y, z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.EWmeta);
							world.setBlockAndMetadata(x, y, z - 2, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.NSmeta);
							world.setBlockAndMetadata(x, y, z + 2, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.NSmeta);
							world.setBlockAndMetadata(x - 1, y + (height - 4), z - 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 1, y + (height - 4), z + 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 1, y + (height - 4), z + 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 1, y + (height - 4), z - 1, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x - 2, y + (height - 4), z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.EWmeta);
							world.setBlockAndMetadata(x + 2, y + (height - 4), z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.EWmeta);
							world.setBlockAndMetadata(x, y + (height - 4), z - 2, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.NSmeta);
							world.setBlockAndMetadata(x, y + (height - 4), z + 2, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta | WorldGenBog1.NSmeta);
							world.setBlockAndMetadata(x - 3, y + (height - 3), z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x + 3, y + (height - 3), z, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x, y + (height - 3), z - 3, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
							world.setBlockAndMetadata(x, y + (height - 3), z + 3, WorldGenBog1.woodBlockID, WorldGenBog1.woodMeta);
						}
					}

					for (int yy = y - 3 + height; yy <= y + height; ++yy) {
						int canopyHeight = yy - (y + height);
						int canopyRadius = 3 - canopyHeight;

						for (int xx = x - canopyRadius; xx <= x + canopyRadius; ++xx) {
							for (int zz = z - canopyRadius; zz <= z + canopyRadius; ++zz) {
								if (world.getBlockId(xx, yy, zz) == WorldGenBog1.leavesBlockID) {
									if (rand.nextInt(4) == 0
											&& world.getBlockId(xx - 1, yy, zz) == 0) {
										this.generateVines(world, xx - 1, yy, zz, 8);
									}

									if (rand.nextInt(4) == 0
											&& world.getBlockId(xx + 1, yy, zz) == 0) {
										this.generateVines(world, xx + 1, yy, zz, 2);
									}

									if (rand.nextInt(4) == 0
											&& world.getBlockId(xx, yy, zz - 1) == 0) {
										this.generateVines(world, xx, yy, zz - 1, 1);
									}

									if (rand.nextInt(4) == 0
											&& world.getBlockId(xx, yy, zz + 1) == 0) {
										this.generateVines(world, xx, yy, zz + 1, 4);
									}
								}
							}
						}
					}

					return true;
				} else
					return false;
			}
		} else
			return false;
	}

	private void generateVines(World world, int x, int y, int z, int vineMeta) {
		world.setBlockAndMetadata(x, y, z, Block.vine.blockID, vineMeta);
		int height = 4;

		while (true) {
			--y;

			if (world.getBlockId(x, y, z) != 0 || height <= 0)
				return;

			world.setBlockAndMetadata(x, y, z, Block.vine.blockID, vineMeta);
			--height;
		}
	}
}
