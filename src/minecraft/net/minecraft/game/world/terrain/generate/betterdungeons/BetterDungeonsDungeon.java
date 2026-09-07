package net.minecraft.game.world.terrain.generate.betterdungeons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class BetterDungeonsDungeon {
	int[][][] map;
	final int totalSize;
	final int maxRooms;
	final int roomHeight = 7;
	public ArrayList<BetterDungeonsRooms> rooms;
	final int roomSize;

	public BetterDungeonsDungeon(Random random, int totalSize, int maxRooms, int roomSize) {
		this.roomSize = roomSize;
		this.totalSize = totalSize;
		this.maxRooms = maxRooms;
		Random rnd = random;
		this.map = new int[totalSize][this.roomHeight][totalSize];

		for(int x = 0; x < totalSize; ++x) {
			for(int y = 0; y < this.roomHeight; ++y) {
				for(int z = 0; z < totalSize; ++z) {
					this.map[x][y][z] = -1;
				}
			}
		}

		int roomsCount = random.nextInt(maxRooms) + 10;
		this.rooms = new ArrayList<BetterDungeonsRooms>();
		BetterDungeonsRooms room = new BetterDungeonsRooms(this.map, random.nextInt(totalSize), 0, random.nextInt(totalSize), random, roomSize, this.roomHeight);
		this.rooms.add(room);
		
		int lastx = room.pos[0];
		int lastz = room.pos[2];
		for(int i = 0; i < roomsCount; ++i) {
			int tx = rnd.nextInt(room.maxSize * 2);
			int tz = rnd.nextInt(room.maxSize * 2);
			if(rnd.nextInt(2) == 0) {
				tx = -tx;
			}

			if(rnd.nextInt(2) == 0) {
				tz = -tz;
			}

			room = new BetterDungeonsRooms(this.map, lastx + tx, 0, lastz + tz, rnd, roomSize, this.roomHeight);
			if(room.created) {
				this.rooms.add(room);
				lastx = room.pos[0];
				lastz = room.pos[2];
			}
		}

		Iterator<BetterDungeonsRooms> iterator = this.rooms.iterator();
		iterator.next();

		for(int cont = 0; iterator.hasNext(); ++cont) {
			BetterDungeonsRooms r1 = (BetterDungeonsRooms)this.rooms.get(cont);
			BetterDungeonsRooms r2 = (BetterDungeonsRooms)iterator.next();
			this.addWalkway(r1, r2, this.map, rnd);
		}

		this.rem();
	}

	public void addWalkway(BetterDungeonsRooms room1, BetterDungeonsRooms room2, int[][][] map, Random rnd) {
		int x1 = room1.pos[0];
		int z1 = room1.pos[2];
		int x2 = room2.pos[0];
		int z2 = room2.pos[2];
		int dx = x1 - x2;
		int dz = z1 - z2;
		int i;
		int c;
		byte b16;
		
		if(Math.abs(dx * 2) != room1.sizeX) {
			if(dx < 0) {
				b16 = 1;
			} else {
				b16 = -1;
			}

			for(i = 0; i < Math.abs(dx); x1 += b16) {
				for(c = 0; c < this.roomHeight; ++c) {
					if(c < 3) {
						map[x1][c][z1] = 1;
					} else {
						map[x1][c][z1] = 0;
					}
				}

				++i;
			}

			if(dz < 0) {
				b16 = 1;
			} else {
				b16 = -1;
			}

			for(i = 0; i < Math.abs(dz); z1 += b16) {
				for(c = 0; c < this.roomHeight; ++c) {
					if(c < 3) {
						map[x1][c][z1] = 1;
					} else {
						map[x1][c][z1] = 0;
					}
				}

				++i;
			}
		} else {
			if(dz < 0) {
				b16 = 1;
			} else {
				b16 = -1;
			}

			for(i = 0; i < Math.abs(dz); z1 += b16) {
				for(c = 0; c < this.roomHeight; ++c) {
					if(c < 3) {
						map[x1][c][z1] = 1;
					} else {
						map[x1][c][z1] = 0;
					}
				}

				++i;
			}

			if(dx < 0) {
				b16 = 1;
			} else {
				b16 = -1;
			}

			for(i = 0; i < Math.abs(dx); x1 += b16) {
				for(c = 0; c < this.roomHeight; ++c) {
					if(c < 3) {
						map[x1][c][z1] = 1;
					} else {
						map[x1][c][z1] = 0;
					}
				}

				++i;
			}
		}

	}

	public void rem() {
		int x;
		int y;
		int z;
		for(x = 0; x < this.totalSize; ++x) {
			for(y = 3; y < this.roomHeight; ++y) {
				for(z = 0; z < this.totalSize; ++z) {
					if(this.map[x][y][z] == -1) {
						this.map[x][y][z] = 1;
					}
				}
			}
		}

		for(x = 1; x < this.totalSize - 1; ++x) {
			for(y = 3; y < this.roomHeight; ++y) {
				for(z = 1; z < this.totalSize - 1; ++z) {
					if(
						(this.map[x - 1][y][z + 1] == 1 || this.map[x - 1][y][z + 1] == -1) && 
						(this.map[x][y][z + 1] == 1 || this.map[x][y][z + 1] == -1) && 
						(this.map[x + 1][y][z + 1] == 1 || this.map[x + 1][y][z + 1] == -1) && 
						(this.map[x + 1][y][z] == 1 || this.map[x + 1][y][z] == -1) && 
						(this.map[x + 1][y][z - 1] == 1 || this.map[x][y][z - 1] == -1) && 
						(this.map[x][y][z - 1] == 1 || this.map[x][y][z - 1] == -1) && 
						(this.map[x - 1][y][z - 1] == 1 || this.map[x - 1][y][z - 1] == -1)
					) {
						this.map[x][y][z] = -1;
					}
				}
			}
		}

	}
}
