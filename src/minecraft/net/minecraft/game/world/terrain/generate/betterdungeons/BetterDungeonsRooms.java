package net.minecraft.game.world.terrain.generate.betterdungeons;

import java.util.Random;

public class BetterDungeonsRooms {
	int[][][] mapa;
	public int[] pos = new int[3];
	public int sizeX;
	public int sizeZ;
	boolean created = true;
	final int maxSize;
	public int roomHeight;

	public BetterDungeonsRooms(int[][][] mapa, int x, int y, int z, Random random, int maxSize, int roomHeight) {
		this.roomHeight = roomHeight;		
		this.maxSize = maxSize;
		
		if(x < maxSize / 2) {
			x = maxSize / 2;
		}

		if(x > mapa.length - maxSize / 2) {
			x = mapa.length - maxSize / 2;
		}

		if(z < maxSize / 2) {
			z = maxSize / 2;
		}

		if(z > mapa.length - maxSize / 2) {
			z = mapa.length - maxSize / 2;
		}

		this.mapa = mapa;
		this.pos[0] = x;
		this.pos[1] = y;
		this.pos[2] = z;
		boolean created = false;
		int tx = random.nextInt(maxSize - 5) + 5;
		int tz = random.nextInt(maxSize - 5) + 5;
		int maxCount = tx;
		if(tx < tz) {
			maxCount = tz;
		}

		if(!this.createRoom(mapa, random, tx, tz)) {
			for(int c = 0; c < maxCount - 5; ++c) {
				if(!created) {
					if(tx > 5) {
						--tx;
					}

					if(tz > 5) {
						--tz;
					}

					if(this.createRoom(mapa, random, tx, tz)) {
						created = true;
					}
				}
			}

			if(!created) {
				this.created = false;
			}
		}

	}

	public boolean createRoom(int[][][] mapa, Random random, int sx, int sz) {
		if(sx < 5) {
			this.sizeX = random.nextInt(this.maxSize - 5) + 5;
		} else {
			this.sizeX = sx;
		}

		if(sz < 5) {
			this.sizeZ = random.nextInt(this.maxSize - 5) + 5;
		} else {
			this.sizeZ = sz;
		}

		int posX = this.pos[0] - this.sizeX / 2;
		int posZ = this.pos[2] - this.sizeZ / 2;

		for(int x = 0; x < this.sizeX; ++x) {
			for(int z = 0; z < this.sizeZ; ++z) {
				if(mapa[posX + x][2][posZ + z] > 0) {
					return false;
				}
			}
		}

		this.addFloor(posX, posZ, mapa);
		this.addWalls(posX, posZ, mapa);
		mapa[this.pos[0]][2][this.pos[2]] = 2;
		return true;
	}

	public void addFloor(int posX, int posZ, int[][][] mapa) {
		for(int x = 0; x < this.sizeX; ++x) {
			for(int z = 0; z < this.sizeZ; ++z) {
				mapa[posX + x][2][posZ + z] = 1;
				mapa[posX + x][this.roomHeight - 1][posZ + z] = 1;
			}
		}

	}

	public void addWalls(int posX, int posZ, int[][][] mapa) {
		for(int y = 3; y < this.roomHeight - 1; ++y) {
			int x;
			int z;
			for(x = 0; x < this.sizeX; ++x) {
				for(z = 0; z < this.sizeZ; ++z) {
					mapa[posX + x][y][posZ + z] = 1;
				}
			}

			for(x = 1; x < this.sizeX - 1; ++x) {
				for(z = 1; z < this.sizeZ - 1; ++z) {
					mapa[posX + x][y][posZ + z] = 0;
				}
			}
		}

	}
}
