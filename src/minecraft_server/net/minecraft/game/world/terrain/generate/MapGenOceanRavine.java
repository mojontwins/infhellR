package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class MapGenOceanRavine extends MapGenBase {
	// Those are in fact very specialized forms of caves

	private float ravineW[];

	public MapGenOceanRavine() {
		ravineW = new float[1024];
	}

	protected void generateRavine(World world, long seed, int chunkX, int chunkZ, byte data[], double x, double y,
			double z, float par12, float horzAngle, float vertAngle, int par15, int par16, double par17) {
		Random random = new Random(seed);
		double x0 = chunkX * 16 + 8;
		double z0 = chunkZ * 16 + 8;
		float f = 0.0F;
		float f1 = 0.0F;

		if (par16 <= 0) {
			int i = range * 16 - 16;
			par16 = i - random.nextInt(i / 4);
		}

		boolean flag = false;

		if (par15 == -1) {
			par15 = par16 / 2;
			flag = true;
		}

		float f2 = 1.0F;

		for (int j = 0; j < 128; j++) {
			if (j == 0 || random.nextInt(3) == 0) {
				f2 = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
			}

			ravineW[j] = f2 * f2;
		}

		for (; par15 < par16; par15++) {
			double d2 = 1.5D
					+ (double) (MathHelper.sin(((float) par15 * (float) Math.PI) / (float) par16) * par12 * 1.0F);
			double d3 = d2 * par17;
			d2 *= (double) random.nextFloat() * 0.25D + 0.75D;
			d3 *= (double) random.nextFloat() * 0.25D + 0.75D;
			float f3 = MathHelper.cos(vertAngle);
			float f4 = MathHelper.sin(vertAngle);
			x += MathHelper.cos(horzAngle) * f3;
			y += f4;
			z += MathHelper.sin(horzAngle) * f3;
			vertAngle *= 0.7F;
			vertAngle += f1 * 0.05F;
			horzAngle += f * 0.05F;
			f1 *= 0.8F;
			f *= 0.5F;
			f1 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			f += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4F;

			if (!flag && random.nextInt(4) == 0) {
				continue;
			}

			double dx = x - x0;
			double dz = z - z0;
			double dy = par16 - par15;
			double d7 = par12 + 2.0F + 16F;

			if ((dx * dx + dz * dz) - dy * dy > d7 * d7) {
				return;
			}

			if (x < x0 - 16D - d2 * 2D || z < z0 - 16D - d2 * 2D || x > x0 + 16D + d2 * 2D || z > z0 + 16D + d2 * 2D) {
				continue;
			}

			int x1 = MathHelper.floor_double(x - d2) - chunkX * 16 - 1;
			int x2 = (MathHelper.floor_double(x + d2) - chunkX * 16) + 1;
			int y1 = MathHelper.floor_double(y - d3) - 1;
			int y2 = MathHelper.floor_double(y + d3) + 1;
			int z1 = MathHelper.floor_double(z - d2) - chunkZ * 16 - 1;
			int z2 = (MathHelper.floor_double(z + d2) - chunkZ * 16) + 1;

			if (x1 < 0) {
				x1 = 0;
			}

			if (x2 > 16) {
				x2 = 16;
			}

			if (y1 < 1) {
				y1 = 1;
			}

			if (y2 > 64) {
				y2 = 64;
			}

			if (z1 < 0) {
				z1 = 0;
			}

			if (z2 > 16) {
				z2 = 16;
			}

			boolean flag1 = false;

			if (flag1) {
				continue;
			}

			for (int xx = (int) x1; xx < x2; xx++) {
				dx = (((double) (xx + chunkX * 16) + 0.5D) - x) / d2;
				label0:

				for (int zz = (int) z1; zz < z2; zz++) {
					dz = (((double) (zz + chunkZ * 16) + 0.5D) - z) / d2;
					int index = (xx * 16 + zz) * 128 + y2;
				
					if (dx * dx + dz * dz >= 1.0D) {
						continue;
					}

					int yy = y2;

					do {
						if (yy < y1) {
							continue label0;
						}

						dy = (((double) yy + 0.5D) - y) / d3;

						if ((dx * dx + dz * dz) * (double) ravineW[yy] + (dy * dy) / 6D < 1.0D) {
							
							if(yy >= 64) {
								data [index] = 0;
							} else {
								if (yy < 10) {
									data[index] = (byte) Block.lavaMoving.blockID;
								} else {
									data[index] = (byte) Block.waterStill.blockID;

								}
							}
						}

						index--;
						yy--;
					} while (true);
				}
			}

			if (flag) {
				break;
			}
		}
	}

	/**
	 * Recursively called by generate() (generate) and optionally by itself.
	 */
	protected void recursiveGenerate(World world, int curChunkX, int curChunkZ, int chunkX, int chunkZ, byte data[]) {
		if (rand.nextInt(50) != 0) {
			return;
		}

		double x0 = curChunkX * 16 + rand.nextInt(16);
		double y0 = rand.nextInt(30) + 32;
		double z0 = curChunkZ * 16 + rand.nextInt(16);
		int i = 1;
		
		for (int j = 0; j < i; j++) {
			float f = rand.nextFloat() * (float) Math.PI * 2.0F;
			float f1 = ((rand.nextFloat() - 0.5F) * 2.0F) / 8F;
			float f2 = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;
			generateRavine(world, rand.nextLong(), chunkX, chunkZ, data, x0, y0, z0, f2, f, f1, 0, 0, 3D);
		}
	}
}
