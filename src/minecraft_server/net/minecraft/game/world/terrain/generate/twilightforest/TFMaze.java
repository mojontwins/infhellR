package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.World;

public class TFMaze {
	int width;
	int depth;
	int oddBias = 3;
	int evenBias = 1;
	int tall = 3;
	int roots = 0;
	int worldX;
	int worldY;
	int worldZ;
	int type;
	int wallBlockID = Block.mazeStone.blockID;
	int wallBlockMeta = 0;
	int rootBlockID = Block.mazeStone.blockID;
	int rootBlockMeta = 1;
	int torchBlockID = Block.torchWood.blockID;
	int torchBlockMeta = 0;
	int torchRarity = 2;
	protected int rawWidth;
	protected int rawDepth;
	protected int[] storage;
	static final int OUT_OF_BOUNDS = Integer.MIN_VALUE;
	static final int OOB = Integer.MIN_VALUE;
	static final int ROOM = 5;
	Random rand = new Random();

	public TFMaze(int cellsWidth, int cellsDepth) {
		this.width = cellsWidth;
		this.depth = cellsDepth;
		this.rawWidth = this.width * 2 + 1;
		this.rawDepth = this.depth * 2 + 1;
		this.storage = new int[this.rawWidth * this.rawDepth];
	}

	public int getCell(int x, int z) {
		return this.getRaw(x * 2 + 1, z * 2 + 1);
	}

	public void putCell(int x, int z, int value) {
		this.putRaw(x * 2 + 1, z * 2 + 1, value);
	}

	public boolean cellEquals(int x, int z, int value) {
		return this.getCell(x, z) == value;
	}

	public int getWall(int sx, int sz, int dx, int dz) {
		if(dx == sx + 1 && dz == sz) {
			return this.getRaw(sx * 2 + 2, sz * 2 + 1);
		} else if(dx == sx - 1 && dz == sz) {
			return this.getRaw(sx * 2 + 0, sz * 2 + 1);
		} else if(dx == sx && dz == sz + 1) {
			return this.getRaw(sx * 2 + 1, sz * 2 + 2);
		} else if(dx == sx && dz == sz - 1) {
			return this.getRaw(sx * 2 + 1, sz * 2 + 0);
		} else {
			System.out.println("Wall check out of bounds; s = " + sx + ", " + sz + "; d = " + dx + ", " + dz);
			return Integer.MIN_VALUE;
		}
	}

	public void putWall(int sx, int sz, int dx, int dz, int value) {
		if(dx == sx + 1 && dz == sz) {
			this.putRaw(sx * 2 + 2, sz * 2 + 1, value);
		}

		if(dx == sx - 1 && dz == sz) {
			this.putRaw(sx * 2 + 0, sz * 2 + 1, value);
		}

		if(dx == sx && dz == sz + 1) {
			this.putRaw(sx * 2 + 1, sz * 2 + 2, value);
		}

		if(dx == sx && dz == sz - 1) {
			this.putRaw(sx * 2 + 1, sz * 2 + 0, value);
		}

	}

	public boolean isWall(int sx, int sz, int dx, int dz) {
		return this.getWall(sx, sz, dx, dz) == 0;
	}

	protected void putRaw(int rawx, int rawz, int value) {
		if(rawx >= 0 && rawx < this.rawWidth && rawz >= 0 && rawz < this.rawDepth) {
			this.storage[rawz * this.rawWidth + rawx] = value;
		}

	}

	protected int getRaw(int rawx, int rawz) {
		return rawx >= 0 && rawx < this.rawWidth && rawz >= 0 && rawz < this.rawDepth ? this.storage[rawz * this.rawWidth + rawx] : Integer.MIN_VALUE;
	}

	public void copyToWorld(World world, int dx, int dy, int dz) {
		this.worldX = dx;
		this.worldY = dy;
		this.worldZ = dz;

		for(int x = 0; x < this.rawWidth; ++x) {
			for(int z = 0; z < this.rawDepth; ++z) {
				if(this.getRaw(x, z) == 0) {
					int mdx = dx + x / 2 * (this.evenBias + this.oddBias);
					int mdz = dz + z / 2 * (this.evenBias + this.oddBias);
					int i;
					if(this.isEven(x) && this.isEven(z)) {
						/*
						if(this.type == 4 && this.shouldTree(x, z)) {
							(new TFGenCanopyTree()).generate(world, this.rand, mdx, dy, mdz);
						} else 
						*/{
							for(i = 0; i < this.tall; ++i) {
								this.putWallBlock(world, mdx, dy + i, mdz);
							}

							for(i = 1; i <= this.roots; ++i) {
								this.putRootBlock(world, mdx, dy - i, mdz);
							}
						}
					}

					int y;
					if(this.isEven(x) && !this.isEven(z)) {
						for(i = 1; i <= this.oddBias; ++i) {
							for(y = 0; y < this.tall; ++y) {
								this.putWallBlock(world, mdx, dy + y, mdz + i);
							}

							for(y = 1; y <= this.roots; ++y) {
								this.putRootBlock(world, mdx, dy - y, mdz + i);
							}
						}
					}

					if(!this.isEven(x) && this.isEven(z)) {
						for(i = 1; i <= this.oddBias; ++i) {
							for(y = 0; y < this.tall; ++y) {
								this.putWallBlock(world, mdx + i, dy + y, mdz);
							}

							for(y = 1; y <= this.roots; ++y) {
								this.putRootBlock(world, mdx + i, dy - y, mdz);
							}
						}
					}
				}
			}
		}

		this.placeTorches(world);
	}

	public void carveToWorld(World world, int dx, int dy, int dz) {
		this.worldX = dx;
		this.worldY = dy;
		this.worldZ = dz;

		for(int x = 0; x < this.rawWidth; ++x) {
			for(int z = 0; z < this.rawDepth; ++z) {
				if(this.getRaw(x, z) != 0) {
					int mdx = dx + x / 2 * (this.evenBias + this.oddBias);
					int mdz = dz + z / 2 * (this.evenBias + this.oddBias);
					int mx;
					if(this.isEven(x) && this.isEven(z)) {
						for(mx = 0; mx < this.tall; ++mx) {
							this.carveBlock(world, mdx, dy + mx, mdz);
						}
					} else {
						int mz;
						if(this.isEven(x) && !this.isEven(z)) {
							for(mx = 1; mx <= this.oddBias; ++mx) {
								for(mz = 0; mz < this.tall; ++mz) {
									this.carveBlock(world, mdx, dy + mz, mdz + mx);
								}
							}
						} else if(!this.isEven(x) && this.isEven(z)) {
							for(mx = 1; mx <= this.oddBias; ++mx) {
								for(mz = 0; mz < this.tall; ++mz) {
									this.carveBlock(world, mdx + mx, dy + mz, mdz);
								}
							}
						} else if(!this.isEven(x) && !this.isEven(z)) {
							for(mx = 1; mx <= this.oddBias; ++mx) {
								for(mz = 1; mz <= this.oddBias; ++mz) {
									for(int y = 0; y < this.tall; ++y) {
										this.carveBlock(world, mdx + mx, dy + y, mdz + mz);
									}
								}
							}
						}
					}
				}
			}
		}

		this.placeTorches(world);
	}

	protected void putWallBlock(World world, int x, int y, int z) {
		world.setBlockAndMetadataWithNotify(x, y, z, this.wallBlockID, this.wallBlockMeta);
	}

	protected void carveBlock(World world, int x, int y, int z) {
		world.setBlockAndMetadataWithNotify(x, y, z, 0, 0);
	}

	protected void putRootBlock(World world, int x, int y, int z) {
		world.setBlockAndMetadataWithNotify(x, y, z, this.rootBlockID, this.rootBlockMeta);
	}

	public boolean isEven(int n) {
		return n % 2 == 0;
	}

	public void placeTorches(World world) {
		byte torchHeight = 1;

		for(int x = 0; x < this.rawWidth; ++x) {
			for(int z = 0; z < this.rawDepth; ++z) {
				if(this.getRaw(x, z) == 0) {
					int mdx = this.worldX + x / 2 * (this.evenBias + this.oddBias);
					int mdy = this.worldY + torchHeight;
					int mdz = this.worldZ + z / 2 * (this.evenBias + this.oddBias);
					if(this.isEven(x) && this.isEven(z) && this.shouldTorch(x, z) && world.getBlockId(mdx, mdy, mdz) == this.wallBlockID) {
						world.setBlockAndMetadataWithNotify(mdx, mdy, mdz, this.torchBlockID, this.torchBlockMeta);
					}
				}
			}
		}

	}

	public boolean shouldTorch(int rx, int rz) {
		return this.getRaw(rx + 1, rz) != Integer.MIN_VALUE && this.getRaw(rx - 1, rz) != Integer.MIN_VALUE && this.getRaw(rx, rz + 1) != Integer.MIN_VALUE && this.getRaw(rx, rz - 1) != Integer.MIN_VALUE ? (this.getRaw(rx + 1, rz) == 0 && this.getRaw(rx - 1, rz) == 0 || this.getRaw(rx, rz + 1) == 0 && this.getRaw(rx, rz - 1) == 0 ? false : this.rand.nextInt(2) == 0) : false;
	}

	public boolean shouldTree(int rx, int rz) {
		return rx != 0 && rx != this.rawWidth - 1 || this.getRaw(rx, rz + 1) == 0 && this.getRaw(rx, rz - 1) == 0 ? ((rz == 0 || rz == this.rawDepth - 1) && (this.getRaw(rx + 1, rz) != 0 || this.getRaw(rx - 1, rz) != 0) ? true : this.rand.nextInt(50) == 0) : true;
	}

	int getWorldX(int x) {
		return this.worldX + x * (this.evenBias + this.oddBias) + 1;
	}

	int getWorldZ(int z) {
		return this.worldZ + z * (this.evenBias + this.oddBias) + 1;
	}

	public void carveRoom0(int cx, int cz) {
		this.putCell(cx, cz, 5);
		this.putCell(cx + 1, cz, 5);
		this.putWall(cx, cz, cx + 1, cz, 5);
		this.putCell(cx - 1, cz, 5);
		this.putWall(cx, cz, cx - 1, cz, 5);
		this.putCell(cx, cz + 1, 5);
		this.putWall(cx, cz, cx, cz + 1, 5);
		this.putCell(cx, cz - 1, 5);
		this.putWall(cx, cz, cx, cz - 1, 5);
	}

	public void carveRoom1(int cx, int cz) {
		int rx = cx * 2 + 1;
		int rz = cz * 2 + 1;

		for(int i = -2; i <= 2; ++i) {
			for(int j = -2; j <= 2; ++j) {
				this.putRaw(rx + i, rz + j, 5);
			}
		}

		this.putCell(rx, rz + 1, 0);
		this.putCell(rx, rz - 1, 0);
		this.putCell(rx + 1, rz, 0);
		this.putCell(rx - 1, rz, 0);
		if(this.getRaw(rx, rz + 4) != Integer.MIN_VALUE) {
			this.putRaw(rx, rz + 3, 5);
		}

		if(this.getRaw(rx, rz - 4) != Integer.MIN_VALUE) {
			this.putRaw(rx, rz - 3, 5);
		}

		if(this.getRaw(rx + 4, rz) != Integer.MIN_VALUE) {
			this.putRaw(rx + 3, rz, 5);
		}

		if(this.getRaw(rx - 4, rz) != Integer.MIN_VALUE) {
			this.putRaw(rx - 3, rz, 5);
		}

	}

	public void add4Exits() {
		int hx = this.rawWidth / 2 + 1;
		int hz = this.rawDepth / 2 + 1;
		this.putRaw(hx, 0, 5);
		this.putRaw(hx, this.rawDepth - 1, 5);
		this.putRaw(0, hz, 5);
		this.putRaw(this.rawWidth - 1, hz, 5);
	}

	public void generateRecursiveBacktracker(int sx, int sz) {
		this.rbGen(sx, sz);
	}

	public void rbGen(int sx, int sz) {
		this.putCell(sx, sz, 1);
		int unvisited = 0;
		if(this.cellEquals(sx + 1, sz, 0)) {
			++unvisited;
		}

		if(this.cellEquals(sx - 1, sz, 0)) {
			++unvisited;
		}

		if(this.cellEquals(sx, sz + 1, 0)) {
			++unvisited;
		}

		if(this.cellEquals(sx, sz - 1, 0)) {
			++unvisited;
		}

		if(unvisited != 0) {
			int rn = this.rand.nextInt(unvisited);
			int dz = 0;
			int dx = 0;
			if(this.cellEquals(sx + 1, sz, 0)) {
				if(rn == 0) {
					dx = sx + 1;
					dz = sz;
				}

				--rn;
			}

			if(this.cellEquals(sx - 1, sz, 0)) {
				if(rn == 0) {
					dx = sx - 1;
					dz = sz;
				}

				--rn;
			}

			if(this.cellEquals(sx, sz + 1, 0)) {
				if(rn == 0) {
					dx = sx;
					dz = sz + 1;
				}

				--rn;
			}

			if(this.cellEquals(sx, sz - 1, 0) && rn == 0) {
				dx = sx;
				dz = sz - 1;
			}

			this.putWall(sx, sz, dx, dz, 2);
			this.rbGen(dx, dz);
			this.rbGen(sx, sz);
			this.rbGen(sx, sz);
		}
	}
}
