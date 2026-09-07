package net.minecraft.game.world.terrain.generate;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.World;

public class WorldGenGiantFlower1 extends WorldGenBop {
	private final int minTreeHeight;
	private final boolean vinesGrow;
	private static final int bigFlowerId = Block.bigFlower.blockID;
	
	public WorldGenGiantFlower1() {
		this(2, false);
	}

	public WorldGenGiantFlower1(int var2, boolean var5)  {
		this.minTreeHeight = var2;
		this.vinesGrow = var5;
	}

	public boolean generate(World world, Random rand, int x, int y, int z) {
		int var6 = rand.nextInt(18) + this.minTreeHeight;
		boolean var7 = true;

		if (y >= 1 && y + var6 + 1 <= 256) {
			int var8;
			byte var9;
			int var10;
			int var11;

			for (var8 = y; var8 <= y + 1 + var6; ++var8) {
				var9 = 1;

				if (var8 == y) {
					var9 = 0;
				}

				if (var8 >= y + 1 + var6 - 2) {
					var9 = 2;
				}

				for (int var12 = x - var9; var12 <= x + var9 && var7; ++var12) {
					for (var10 = z - var9; var10 <= z + var9 && var7; ++var10) {
						if (var8 >= 0 && var8 < 256) {
							var11 = world.getBlockId(var12, var8, var10);

							if (var11 != 0 && var11 != Block.leaves.blockID && var11 != Block.grass.blockID && var11 != Block.dirt.blockID && var11 != Block.wood.blockID) {
								var7 = false;
							}
						} else {
							var7 = false;
						}
					}
				}
			}

			if (!var7) {
				return false;
			} else {
				var8 = world.getBlockId(x, y - 1, z);

				if ((var8 == Block.grass.blockID || var8 == Block.dirt.blockID) && y < 256 - var6 - 1) {
					world.setBlock(x, y - 1, z, Block.dirt.blockID);
					var9 = 3;
					byte var18 = 0;
					int var13;
					int var14;
					int var15;

					for (var10 = y - var9 + var6; var10 <= y + var6; ++var10) {
						var11 = var10 - (y + var6);
						var13 = var18 + 1 - var11 / 2;

						for (var14 = x - var13; var14 <= x + var13; ++var14) {
							var15 = var14 - x;

							for (int var16 = z - var13; var16 <= z + var13; ++var16) {
								int var17 = var16 - z;

								if ((Math.abs(var15) != var13 || Math.abs(var17) != var13 || rand.nextInt(2) != 0 && var11 != 0) && !Block.opaqueCubeLookup[world.getBlockId(var14, var10, var16)]) {
									;
								}
							}
						}
					}

					for (var10 = 0; var10 < var6; ++var10) {
						var11 = world.getBlockId(x, y + var10, z);

						if (var11 == 0 || var11 == Block.leaves.blockID) {
							world.setBlockAndMetadata(x, y + var10, z, bigFlowerId, 2);
							world.setBlockAndMetadata(x - 1, y + (var6 - 1), z, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 1, y + (var6 - 1), z, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + (var6 - 1), z - 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + (var6 - 1), z + 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x - 1, y + var6, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 1, y + var6, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6, z - 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6, z + 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x - 2, y + var6, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 2, y + var6, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6, z - 2, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6, z + 2, bigFlowerId, 0);
							world.setBlockAndMetadata(x - 1, y + var6, z - 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x - 1, y + var6, z + 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 1, y + var6, z - 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 1, y + var6, z + 1, bigFlowerId, 0);
							world.setBlockAndMetadata(x - 2, y + var6, z - 2, bigFlowerId, 0);
							world.setBlockAndMetadata(x - 2, y + var6, z + 2, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 2, y + var6, z - 2, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 2, y + var6, z + 2, bigFlowerId, 0);
							
							// Mine: crown
							world.setBlockAndMetadata(x - 2, y + var6 + 1, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 2, y + var6 + 1, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6 + 1, z - 2, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6 + 1, z + 2, bigFlowerId, 0);
							
							world.setBlockAndMetadata(x - 3, y + var6 + 2, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x + 3, y + var6 + 2, z, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6 + 2, z - 3, bigFlowerId, 0);
							world.setBlockAndMetadata(x, y + var6 + 2, z + 3, bigFlowerId, 0);

							if (this.vinesGrow && var10 > 0) {
								if (rand.nextInt(3) > 0 && world.isAirBlock(x - 1, y + var10, z)) {
									world.setBlockAndMetadata(x - 1, y + var10, z, Block.vine.blockID, 8);
								}

								if (rand.nextInt(3) > 0 && world.isAirBlock(x + 1, y + var10, z)) {
									world.setBlockAndMetadata(x + 1, y + var10, z, Block.vine.blockID, 2);
								}

								if (rand.nextInt(3) > 0 && world.isAirBlock(x, y + var10, z - 1)) {
									world.setBlockAndMetadata(x, y + var10, z - 1, Block.vine.blockID, 1);
								}

								if (rand.nextInt(3) > 0 && world.isAirBlock(x, y + var10, z + 1)) {
									world.setBlockAndMetadata(x, y + var10, z + 1, Block.vine.blockID, 4);
								}
							}
						}
					}

					if (this.vinesGrow) {
						for (var10 = y - 3 + var6; var10 <= y + var6; ++var10) {
							var11 = var10 - (y + var6);
							var13 = 2 - var11 / 2;

							for (var14 = x - var13; var14 <= x + var13; ++var14) {
								for (var15 = z - var13; var15 <= z + var13; ++var15) {
									if (world.getBlockId(var14, var10, var15) == Block.leaves.blockID) {
										if (rand.nextInt(4) == 0 && world.getBlockId(var14 - 1, var10, var15) == 0) {
											this.growVines(world, var14 - 1, var10, var15, 8);
										}

										if (rand.nextInt(4) == 0 && world.getBlockId(var14 + 1, var10, var15) == 0) {
											this.growVines(world, var14 + 1, var10, var15, 2);
										}

										if (rand.nextInt(4) == 0 && world.getBlockId(var14, var10, var15 - 1) == 0) {
											this.growVines(world, var14, var10, var15 - 1, 1);
										}

										if (rand.nextInt(4) == 0 && world.getBlockId(var14, var10, var15 + 1) == 0) {
											this.growVines(world, var14, var10, var15 + 1, 4);
										}
									}
								}
							}
						}
					}

					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}
}
