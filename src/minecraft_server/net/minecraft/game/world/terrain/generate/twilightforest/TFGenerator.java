package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
public abstract class TFGenerator extends WorldGenerator {
	protected World worldObj;

	public abstract boolean generate(World world1, Random random2, int i3, int i4, int i5);

	protected boolean putBlock(int dx, int dy, int dz, int blockValue, boolean priority) {
		return this.putBlockAndMetadata(dx, dy, dz, blockValue, 0, priority);
	}

	protected boolean putBlockAndMetadata(int dx, int dy, int dz, int blockValue, int metaValue, boolean priority) {
		if(priority) {
			this.worldObj.setBlockAndMetadata(dx, dy, dz, blockValue, metaValue);
		} else {
			int whatsThere = this.worldObj.getBlockId(dx, dy, dz);
			if(whatsThere != 0) {
				return false;
			}

			this.worldObj.setBlockAndMetadata(dx, dy, dz, blockValue, metaValue);
		}

		return true;
	}

	protected void putBlockAndMetadata(int[] pixel, int blockValue, int metaValue, boolean priority) {
		this.putBlockAndMetadata(pixel[0], pixel[1], pixel[2], blockValue, metaValue, priority);
	}

	protected void putBlock(int[] pixel, int blockValue, boolean priority) {
		this.putBlockAndMetadata(pixel[0], pixel[1], pixel[2], blockValue, 0, priority);
	}

	protected int[] translate(int sx, int sy, int sz, double distance, double angle, double tilt) {
		int[] dest = new int[]{sx, sy, sz};
		double rangle = angle * 2.0D * Math.PI;
		double rtilt = tilt * Math.PI;
		dest[0] = (int)((long)dest[0] + Math.round(Math.sin(rangle) * Math.sin(rtilt) * distance));
		dest[1] = (int)((long)dest[1] + Math.round(Math.cos(rtilt) * distance));
		dest[2] = (int)((long)dest[2] + Math.round(Math.cos(rangle) * Math.sin(rtilt) * distance));
		return dest;
	}

	protected void bresenham(int x1, int y1, int z1, int x2, int y2, int z2, byte blockValue, boolean priority) {
		this.bresenham(x1, y1, z1, x2, y2, z2, blockValue, (byte)0, priority);
	}

	protected void bresenham(int x1, int y1, int z1, int x2, int y2, int z2, byte blockValue, byte metaValue, boolean priority) {
		int[] pixel = new int[]{x1, y1, z1};
		int dx = x2 - x1;
		int dy = y2 - y1;
		int dz = z2 - z1;
		int x_inc = dx < 0 ? -1 : 1;
		int l = Math.abs(dx);
		int y_inc = dy < 0 ? -1 : 1;
		int m = Math.abs(dy);
		int z_inc = dz < 0 ? -1 : 1;
		int n = Math.abs(dz);
		int dx2 = l << 1;
		int dy2 = m << 1;
		int dz2 = n << 1;
		int i;
		int err_1;
		int err_2;
		if(l >= m && l >= n) {
			err_1 = dy2 - l;
			err_2 = dz2 - l;

			for(i = 0; i < l; ++i) {
				this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
				if(err_1 > 0) {
					pixel[1] += y_inc;
					err_1 -= dx2;
				}

				if(err_2 > 0) {
					pixel[2] += z_inc;
					err_2 -= dx2;
				}

				err_1 += dy2;
				err_2 += dz2;
				pixel[0] += x_inc;
			}
		} else if(m >= l && m >= n) {
			err_1 = dx2 - m;
			err_2 = dz2 - m;

			for(i = 0; i < m; ++i) {
				this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
				if(err_1 > 0) {
					pixel[0] += x_inc;
					err_1 -= dy2;
				}

				if(err_2 > 0) {
					pixel[2] += z_inc;
					err_2 -= dy2;
				}

				err_1 += dx2;
				err_2 += dz2;
				pixel[1] += y_inc;
			}
		} else {
			err_1 = dy2 - n;
			err_2 = dx2 - n;

			for(i = 0; i < n; ++i) {
				this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
				if(err_1 > 0) {
					pixel[1] += y_inc;
					err_1 -= dz2;
				}

				if(err_2 > 0) {
					pixel[0] += x_inc;
					err_2 -= dz2;
				}

				err_1 += dy2;
				err_2 += dx2;
				pixel[2] += z_inc;
			}
		}

		this.putBlockAndMetadata(pixel, blockValue, metaValue, priority);
	}

	public void circle(int sx, int sy, int sz, byte rad, byte blockValue, int metaValue, boolean priority) {
		for(byte dx = 0; dx <= rad; ++dx) {
			for(byte dz = 0; dz <= rad; ++dz) {
				int dist = (int)((double)Math.max(dx, dz) + (double)Math.min(dx, dz) * 0.5D);
				if(dx == 3 && dz == 3) {
					dist = 6;
				}

				if(dist <= rad) {
					this.putBlockAndMetadata(sx + dx, sy, sz + dz, blockValue, metaValue, priority);
					this.putBlockAndMetadata(sx + dx, sy, sz - dz, blockValue, metaValue, priority);
					this.putBlockAndMetadata(sx - dx, sy, sz + dz, blockValue, metaValue, priority);
					this.putBlockAndMetadata(sx - dx, sy, sz - dz, blockValue, metaValue, priority);
				}
			}
		}

	}

	public void drawDiameterCircle(int sx, int sy, int sz, int diam, int block, int meta, boolean priority) {
		byte rad = (byte)((diam - 1) / 2);
		if(diam % 2 == 1) {
			this.circle(sx, sy, sz, rad, (byte)block, meta, priority);
		} else {
			this.circle(sx, sy, sz, rad, (byte)block, meta, priority);
			this.circle(sx + 1, sy, sz, rad, (byte)block, meta, priority);
			this.circle(sx, sy, sz + 1, rad, (byte)block, meta, priority);
			this.circle(sx + 1, sy, sz + 1, rad, (byte)block, meta, priority);
		}

	}

	protected byte randStone(Random rand, int howMuch) {
		return rand.nextInt(howMuch) >= 1 ? (byte)Block.cobblestone.blockID : (byte)Block.cobblestoneMossy.blockID;
	}

	protected boolean isAreaClear(World world, Random rand, int x, int y, int z, int dx, int dy, int dz) {
		boolean flag = true;

		for(int cx = 0; cx < dx; ++cx) {
			for(int cz = 0; cz < dy; ++cz) {
				Material m = world.getBlockMaterial(x + cx, y - 1, z + cz);
				if(m != Material.grass && m != Material.rock) {
					flag = false;
				}

				for(int cy = 0; cy < dz; ++cy) {
					if(!world.isAirBlock(x + cx, y + cy, z + cz)) {
						flag = false;
					}
				}
			}
		}

		return flag;
	}
	
	protected void fill(int dx, int dy, int dz, int width, int height, int depth, int blockID, int meta) {
		for(int cx = 0; cx < width; ++cx) {
			for(int cy = 0; cy < height; ++cy) {
				for(int cz = 0; cz < depth; ++cz) {
					this.worldObj.setBlockAndMetadata(dx + cx, dy + cy, dz + cz, blockID, meta);
				}
			}
		}

	}
	
	protected void fillWithBlocksDownwads(int dx, int dy, int dz, int blockID, int meta) {
		while((this.worldObj.getBlockId(dx, dy, dz) == 0 || this.worldObj.getBlockMaterial(dx, dy, dz) == Material.water) && dy > 1) {
			this.worldObj.setBlockAndMetadata(dx, dy --, dz, blockID, meta);
		}
	}
}
