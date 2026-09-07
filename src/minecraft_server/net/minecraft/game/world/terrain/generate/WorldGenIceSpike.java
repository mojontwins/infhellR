package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;

public class WorldGenIceSpike {

	public int randomIce(Random rand) {
		switch(rand.nextInt(10)) {
		case 0: return Block.blockSnow.blockID;
		case 1:
		case 2:
		case 3: return Block.ice.blockID;
		default: return Block.packedIce.blockID;
		}
	}
	
	public boolean generate(World world, Random rand, int x, int y, int z) {
		while ((world.isAirBlock(x, y, z) || world.getBlockId(x, y, z) == Block.snow.blockID) && y > 2) {
			--y;
		}

		if (world.getBlockId(x, y, z) != Block.blockSnow.blockID && world.getBlockId(x, y, z) != Block.grass.blockID && world.getBlockId(x, y, z) != Block.dirt.blockID) {
			return false;
		} else {
			y += rand.nextInt(6);
			int height = rand.nextInt(6) + 7;
			int var7 = height / 4 + rand.nextInt(2);

			if (var7 > 1 && rand.nextInt(60) == 0) {
				y += 10 + rand.nextInt(30);
			}

			int dY;
			int radius;
			int dX;

			for (dY = 0; dY < height; ++dY) {
				float var9 = (1.0F - (float) dY / (float) height) * (float) var7;
				radius = MathHelper.ceiling_float_int(var9);

				for (dX = -radius; dX <= radius; ++dX) {
					float var12 = (float) MathHelper.abs_int(dX) - 0.25F;

					for (int dZ = -radius; dZ <= radius; ++dZ) {
						float var14 = (float) MathHelper.abs_int(dZ) - 0.25F;

						if ((dX == 0 && dZ == 0 || var12 * var12 + var14 * var14 <= var9 * var9)
								&& (dX != -radius && dX != radius && dZ != -radius && dZ != radius
										|| rand.nextFloat() <= 0.75F)) {
							
							int blockID = world.getBlockId(x + dX, y + dY, z + dZ);
							Block block = Block.blocksList[blockID];							

							if (block == null || block.blockMaterial == Material.air || blockID == Block.dirt.blockID || blockID == Block.blockSnow.blockID
									|| blockID == Block.ice.blockID) {
								world.setBlock(x + dX, y + dY, z + dZ, this.randomIce(rand));
							}

							if (dY != 0 && radius > 1) {
								blockID = world.getBlockId(x + dX, y - dY, z + dZ);
								block = Block.blocksList[blockID];

								if (block == null || block.blockMaterial == Material.air || blockID == Block.dirt.blockID || blockID == Block.blockSnow.blockID
										|| blockID == Block.ice.blockID) {
									world.setBlock(x + dX, y - dY, z + dZ, this.randomIce(rand));
								}
							}
						}
					}
				}
			}

			radius = var7 - 1;

			if (radius < 0) {
				radius = 0;
			} else if (radius > 1) {
				radius = 1;
			}

			for (int xx = -radius; xx <= radius; ++xx) {
				int zz = -radius;

				while (zz <= radius) {
					int yy = y - 1;
					int var17 = 50;

					if (Math.abs(xx) == 1 && Math.abs(zz) == 1) {
						var17 = rand.nextInt(5);
					}

					while (true) {
						if (yy > 50) {
							int blockID = world.getBlockId(x + xx, yy, z + zz);
							Block block = Block.blocksList[blockID];

							if (
									block == null || block.blockMaterial == Material.air || 
									blockID == Block.dirt.blockID || blockID == Block.blockSnow.blockID || 
									blockID == Block.ice.blockID || blockID == Block.packedIce.blockID
							) {
								world.setBlock(x + xx, yy, z + zz, this.randomIce(rand));
								--yy;
								--var17;

								if (var17 <= 0) {
									yy -= rand.nextInt(5) + 1;
									var17 = rand.nextInt(5);
								}

								continue;
							}
						}

						++zz;
						break;
					}
				}
			}

			return true;
		}
	}

}
