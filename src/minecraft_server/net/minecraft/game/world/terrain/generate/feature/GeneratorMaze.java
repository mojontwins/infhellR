package net.minecraft.game.world.terrain.generate.feature;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;

/**
 * Grid maze used by the hollow hill. The maze is planned entirely in memory
 * (rooms, backtracker, torch flags) and then written per-chunk as raw
 * block-array data during terraform, so population never touches the World.
 */
public class GeneratorMaze {
	public int width;
	public int depth;
	int oddBias = 3;
	int evenBias = 1;
	public int tall = 3;
	public int worldX;
	public int worldY;
	public int worldZ;
	public int wallBlockID = Block.mazeStone.blockID;
	public int wallBlockMeta = 0;
	public int torchBlockID = Block.torchWood.blockID;
	public int torchBlockMeta = 0;
	protected int rawWidth;
	protected int rawDepth;
	protected int[] storage;
	// Per-raw-cell torch decision, precomputed by computeTorchFlags() so each
	// chunk can evaluate only its own offsets without touching the RNG again.
	protected boolean[] torchFlags;
	// RNG for maze layout: set by the owning feature to a world-seeded stream
	// (this used to default to system time, giving a different maze per run).
	public Random rand = new Random();

	public GeneratorMaze(int cellsWidth, int cellsDepth) {
		this.width = cellsWidth;
		this.depth = cellsDepth;
		this.rawWidth = this.width * 2 + 1;
		this.rawDepth = this.depth * 2 + 1;
		this.storage = new int[this.rawWidth * this.rawDepth];
	}

	/** World position of the maze box origin; must be set before copyToChunk
	 *  or hasWallBlockAt. */
	public void setWorldOrigin(int dx, int dy, int dz) {
		this.worldX = dx;
		this.worldY = dy;
		this.worldZ = dz;
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

	public int getRaw(int rawx, int rawz) {
		return rawx >= 0 && rawx < this.rawWidth && rawz >= 0 && rawz < this.rawDepth ? this.storage[rawz * this.rawWidth + rawx] : Integer.MIN_VALUE;
	}

	public boolean isEven(int n) {
		return n % 2 == 0;
	}

	public int getWorldX(int x) {
		return this.worldX + x * (this.evenBias + this.oddBias) + 1;
	}

	public int getWorldZ(int z) {
		return this.worldZ + z * (this.evenBias + this.oddBias) + 1;
	}

	/**
	 * Inverse of the copyToWorld map: world offset -> raw grid coordinate.
	 * Pillars/segments are written at worldX + (raw/2)*4 (+0..+3 for segments),
	 * so blocks on the 4-block period map back to raw = (d>>2)*2, and offsets
	 * with a non-zero remainder belong to the segment to the right of that raw
	 * cell. Out-of-box deltas land outside the grid and read as non-wall.
	 */
	private static int worldDeltaToRaw(int d) {
		return (d >> 2) * 2 + ((d & 3) == 0 ? 0 : 1);
	}

	/** True if a wall block occupies this world column at plan time. Used by
	 *  decoration air checks (walls are solid even where a torch flag will
	 *  replace the pillar middle). */
	public boolean hasWallBlockAt(int wx, int wz) {
		int rawx = worldDeltaToRaw(wx - this.worldX);
		int rawz = worldDeltaToRaw(wz - this.worldZ);
		if(this.getRaw(rawx, rawz) != 0) {
			return false;
		}
		// Carved cells are never even-even; defensively treat them as open.
		if(!this.isEven(rawx) && !this.isEven(rawz)) {
			return false;
		}
		return true;
	}

	/**
	 * Decide torch placement for every wall pillar up front, consuming the RNG
	 * at the same point the old placeTorches() did (after the backtracker,
	 * before decorations). copyToChunk then reads only this array.
	 */
	public void computeTorchFlags() {
		this.torchFlags = new boolean[this.rawWidth * this.rawDepth];

		for(int x = 0; x < this.rawWidth; ++x) {
			for(int z = 0; z < this.rawDepth; ++z) {
				// Only even-even positions become pillars, and only if they are walls.
				if(!this.isEven(x) || !this.isEven(z) || this.getRaw(x, z) != 0) {
					continue;
				}
				// Torch only when the pillar is not a straight run of wall in
				// either axis (i.e. at corners, junctions and wall ends).
				boolean straightX = this.getRaw(x + 1, z) == 0 && this.getRaw(x - 1, z) == 0;
				boolean straightZ = this.getRaw(x, z + 1) == 0 && this.getRaw(x, z - 1) == 0;
				boolean inBounds = x + 1 < this.rawWidth && x - 1 >= 0 && z + 1 < this.rawDepth && z - 1 >= 0;
				if(inBounds && !straightX && !straightZ) {
					this.torchFlags[z * this.rawWidth + x] = this.rand.nextInt(2) == 0;
				}
			}
		}
	}

	/**
	 * Write the maze walls and torches that fall inside the given chunk.
	 * Raw-array write: no World calls, no lighting, no neighbor updates - the
	 * chunk light pass runs after terraform.
	 */
	public void copyToChunk(Chunk chunk, int chunkX, int chunkZ) {
		if(this.torchFlags == null) {
			this.computeTorchFlags();
		}

		for(int x = 0; x < this.rawWidth; ++x) {
			for(int z = 0; z < this.rawDepth; ++z) {
				if(this.getRaw(x, z) != 0) {
					continue;
				}
				int mdx = this.worldX + x / 2 * (this.evenBias + this.oddBias);
				int mdz = this.worldZ + z / 2 * (this.evenBias + this.oddBias);
				if(this.isEven(x) && this.isEven(z)) {
					// Wall pillar; the middle block (worldY+1) becomes a torch
					// where computeTorchFlags decided so.
					boolean torch = this.torchFlags[z * this.rawWidth + x];
					for(int i = 0; i < this.tall; ++i) {
						int y = this.worldY + i;
						if(i == 1 && torch) {
							this.writeBlock(chunk, chunkX, chunkZ, mdx, y, mdz, this.torchBlockID, this.torchBlockMeta);
						} else {
							this.writeBlock(chunk, chunkX, chunkZ, mdx, y, mdz, this.wallBlockID, this.wallBlockMeta);
						}
					}
				}

				if(this.isEven(x) && !this.isEven(z)) {
					// Wall segment extending +z from the pillar slot.
					for(int i = 1; i <= this.oddBias; ++i) {
						for(int y = 0; y < this.tall; ++y) {
							this.writeBlock(chunk, chunkX, chunkZ, mdx, this.worldY + y, mdz + i, this.wallBlockID, this.wallBlockMeta);
						}
					}
				}

				if(!this.isEven(x) && this.isEven(z)) {
					// Wall segment extending +x from the pillar slot.
					for(int i = 1; i <= this.oddBias; ++i) {
						for(int y = 0; y < this.tall; ++y) {
							this.writeBlock(chunk, chunkX, chunkZ, mdx + i, this.worldY + y, mdz, this.wallBlockID, this.wallBlockMeta);
						}
					}
				}
			}
		}
	}

	private void writeBlock(Chunk chunk, int chunkX, int chunkZ, int wx, int wy, int wz, int blockID, int meta) {
		if((wx >> 4) != chunkX || (wz >> 4) != chunkZ) {
			return;
		}
		int lx = wx & 15;
		int lz = wz & 15;
		chunk.blocks[lx << 12 | lz << 8 | wy] = (byte) blockID;
		chunk.data[lx << 12 | lz << 8 | wy] = (byte) meta;
	}

	/**
	 * Recursive-backtracker carve starting at the given cell. Unvisited cells
	 * are 0; carved cells become 1 (rooms overwrite with 5 before this runs).
	 */
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

	/**
	 * Carve a 3x3-cell room around (cx, cz), punching exits back into the
	 * surrounding corridors. Run before the backtracker.
	 */
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
}
