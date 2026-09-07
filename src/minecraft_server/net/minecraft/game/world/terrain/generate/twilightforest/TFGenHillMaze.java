package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.EnumArt;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.World;

public class TFGenHillMaze extends TFGenerator {
	int hsize;
	TFMaze maze;
	Random rand;

	public TFGenHillMaze(int size) {
		this.hsize = size;
	}

	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;
		this.rand = rand;
		int sx = x - 7 - this.hsize * 16;
		int sz = z - 7 - this.hsize * 16;
		byte msize = 11;
		if(this.hsize == 2) {
			msize = 19;
		} else if(this.hsize == 3) {
			msize = 27;
		}

		this.fill(sx, y - 1, sz, msize * 4, 1, msize * 4, Block.mazeStone.blockID, 1);
		this.fill(sx, y, sz, msize * 4, 3, msize * 4, 0, 0);
		this.fill(sx, y + 3, sz, msize * 4, 1, msize * 4, Block.mazeStone.blockID, 2);
		this.maze = new TFMaze(msize, msize);
		int nrooms = msize / 3;
		int[] rcoords = new int[nrooms * 2];

		for(int i = 0; i < nrooms; ++i) {
			int rx;
			int rz;
			do {
				rx = rand.nextInt(msize - 2) + 1;
				rz = rand.nextInt(msize - 2) + 1;
			} while(this.isNearRoom(rx, rz, rcoords));

			this.maze.carveRoom1(rx, rz);
			rcoords[i * 2] = rx;
			rcoords[i * 2 + 1] = rz;
		}

		this.maze.generateRecursiveBacktracker(0, 0);
		this.maze.copyToWorld(this.worldObj, sx, y, sz);
		this.decorateDeadEnds();
		this.decorate3x3Rooms(rcoords);
		return true;
	}

	protected boolean isNearRoom(int dx, int dz, int[] rcoords) {
		for(int i = 0; i < rcoords.length / 2; ++i) {
			int rx = rcoords[i * 2];
			int rz = rcoords[i * 2 + 1];
			if((rx != 0 || rz != 0) && Math.abs(dx - rx) < 3 && Math.abs(dz - rz) < 3) {
				return true;
			}
		}

		return false;
	}

	public void decorateDeadEnds() {
		for(int x = 0; x < this.maze.width; ++x) {
			for(int z = 0; z < this.maze.depth; ++z) {
				if(!this.maze.isWall(x, z, x - 1, z) && this.maze.isWall(x, z, x + 1, z) && this.maze.isWall(x, z, x, z - 1) && this.maze.isWall(x, z, x, z + 1)) {
					this.decorateDeadEnd(x, z, 3);
				}

				if(this.maze.isWall(x, z, x - 1, z) && !this.maze.isWall(x, z, x + 1, z) && this.maze.isWall(x, z, x, z - 1) && this.maze.isWall(x, z, x, z + 1)) {
					this.decorateDeadEnd(x, z, 1);
				}

				if(this.maze.isWall(x, z, x - 1, z) && this.maze.isWall(x, z, x + 1, z) && !this.maze.isWall(x, z, x, z - 1) && this.maze.isWall(x, z, x, z + 1)) {
					this.decorateDeadEnd(x, z, 0);
				}

				if(this.maze.isWall(x, z, x - 1, z) && this.maze.isWall(x, z, x + 1, z) && this.maze.isWall(x, z, x, z - 1) && !this.maze.isWall(x, z, x, z + 1)) {
					this.decorateDeadEnd(x, z, 2);
				}
			}
		}

	}

	public void decorateDeadEnd(int x, int z, int f) {
		int dec = this.rand.nextInt(17);
		switch(dec) {
		case 0:
			this.deadEndSpiderSpawner(x, z, f);
			break;
		case 1:
			this.deadEndWebs(x, z, f);
			break;
		case 2:
			this.deadEndTreasure(x, z, f);
			break;
		case 3:
			this.deadEndSpawner(x, z, f);
			break;
		case 4:
			this.deadEndPainting(x, z, f);
			break;
		case 5:
			this.deadEndTrap(x, z, f);
			break;
		case 6:
			this.deadEndTrappedChest(x, z, f);
			break;
		case 7:
			this.deadEndTorch(x, z, f);
			break;
		case 8:
			this.deadEndTorchRedstone(x, z, f);
			break;
		case 9:
			this.deadEndFountain(x, z, f);
			break;
		case 10:
			this.deadEndLavaFountain(x, z, f);
			break;
		case 11:
			this.deadEndDoorway(x, z, f);
			break;
		case 12:
			this.deadEndDoor(x, z, f);
			break;
		case 13:
			this.deadEndDoorSteel(x, z, f);
			break;
		case 14:
			this.deadEndDoorTreasure(x, z, f);
		}

	}

	void deadEndSpiderSpawner(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.deadEndWebs(x, z, f);
		String spiderType = this.rand.nextBoolean() ? "Spider" : "SwarmSpider";
		if(f == 0) {
			this.placeMobSpawner(dx + 1, dy + 0, dz + 2, spiderType);
		} else if(f == 1) {
			this.placeMobSpawner(dx + 0, dy + 0, dz + 1, spiderType);
		} else if(f == 2) {
			this.placeMobSpawner(dx + 1, dy + 0, dz + 0, spiderType);
		} else if(f == 3) {
			this.placeMobSpawner(dx + 2, dy + 0, dz + 1, spiderType);
		}

	}

	void deadEndWebs(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 0, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 2, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 2, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 2, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 2, Block.web.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 0, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 0, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 2, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 2, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 0, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 2, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 1, Block.web.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 0, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 2, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 0, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 0, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 0, Block.web.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 0, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 0, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 2, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 2, dz + 1, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 0, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 2, Block.web.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 1, Block.web.blockID);
		}

	}

	void deadEndTreasure(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			TFTreasure.underhill_deadend.generate(this.worldObj, this.rand, dx + 1, dy + 0, dz + 2);
		} else if(f == 1) {
			TFTreasure.underhill_deadend.generate(this.worldObj, this.rand, dx + 0, dy + 0, dz + 1);
		} else if(f == 2) {
			TFTreasure.underhill_deadend.generate(this.worldObj, this.rand, dx + 1, dy + 0, dz + 0);
		} else if(f == 3) {
			TFTreasure.underhill_deadend.generate(this.worldObj, this.rand, dx + 2, dy + 0, dz + 1);
		}

	}

	void deadEndSpawner(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		String mobID = this.rand.nextInt(3) == 0 ? "Skeleton" : "Redcap";
		if(f == 0) {
			this.placeMobSpawner(dx + 1, dy + 0, dz + 2, mobID);
		} else if(f == 1) {
			this.placeMobSpawner(dx + 0, dy + 0, dz + 1, mobID);
		} else if(f == 2) {
			this.placeMobSpawner(dx + 1, dy + 0, dz + 0, mobID);
		} else if(f == 3) {
			this.placeMobSpawner(dx + 2, dy + 0, dz + 1, mobID);
		}

	}

	void deadEndPainting(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int anum = this.rand.nextInt(7);
		EnumArt[] aenumart = EnumArt.values();
		String artID = aenumart[anum].title;
		EntityPainting painting = null;
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 2, Block.torchWood.blockID);
			painting = new EntityPainting(this.worldObj, dx + 1, dy + 1, dz + 3, 0, artID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 2, Block.torchWood.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 0, Block.torchWood.blockID);
			painting = new EntityPainting(this.worldObj, dx - 1, dy + 1, dz + 1, 3, artID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 2, Block.torchWood.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 0, Block.torchWood.blockID);
			painting = new EntityPainting(this.worldObj, dx + 1, dy + 1, dz - 1, 2, artID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 0, Block.torchWood.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 0, Block.torchWood.blockID);
			painting = new EntityPainting(this.worldObj, dx + 3, dy + 1, dz + 1, 1, artID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 2, Block.torchWood.blockID);
		}

		if(painting != null && painting.onValidSurface()) {
			if(!this.worldObj.isRemote) {
				this.worldObj.spawnEntityInWorld(painting);
			}
		} else {
			System.out.println("Painting fail!! " + painting.art.title + " at " + painting.xPosition + " , " + painting.yPosition + ", " + painting.zPosition + " : " + painting.direction);
		}

	}

	void deadEndTrap(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.worldObj.setBlockWithNotify(dx + 1, dy + 0, dz + 1, Block.pressurePlateStone.blockID);
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 1, dy - 1, dz + 2, Block.tnt.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 0, dy - 1, dz + 1, Block.tnt.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 1, dy - 1, dz + 0, Block.tnt.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 2, dy - 1, dz + 1, Block.tnt.blockID);
		}

	}

	void deadEndTrappedChest(int x, int z, int f) {
		this.deadEndTrap(x, z, f);
		this.deadEndTreasure(x, z, f);
	}

	void deadEndTorch(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 2, Block.torchWood.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 1, Block.torchWood.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 0, Block.torchWood.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 1, Block.torchWood.blockID);
		}

	}

	void deadEndTorchRedstone(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 2, Block.torchRedstoneActive.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 1, Block.torchRedstoneActive.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 0, Block.torchRedstoneActive.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 1, Block.torchRedstoneActive.blockID);
		}

	}

	void deadEndNook(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 0, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 2, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 2, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 2, dz + 2, Block.stone.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 1, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 2, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 2, dz + 1, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 2, dz + 2, Block.stone.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 0, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 0, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 0, dy + 2, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 1, dy + 2, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 2, dz + 0, Block.stone.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 1, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 0, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 2, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 2, dz + 0, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 2, dz + 1, Block.stone.blockID);
			this.worldObj.setBlockWithNotify(dx + 2, dy + 2, dz + 2, Block.stone.blockID);
		}

	}

	void deadEndFountain(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.deadEndNook(x, z, f);
		this.worldObj.setBlockWithNotify(dx + 1, dy - 1, dz + 1, 0);
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 2, Block.waterMoving.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 1, Block.waterMoving.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 0, Block.waterMoving.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 1, Block.waterMoving.blockID);
		}

	}

	void deadEndLavaFountain(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.deadEndNook(x, z, f);
		this.worldObj.setBlockWithNotify(dx + 1, dy - 1, dz + 1, 0);
		if(f == 0) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 2, Block.lavaMoving.blockID);
		} else if(f == 1) {
			this.worldObj.setBlockWithNotify(dx + 0, dy + 1, dz + 1, Block.lavaMoving.blockID);
		} else if(f == 2) {
			this.worldObj.setBlockWithNotify(dx + 1, dy + 1, dz + 0, Block.lavaMoving.blockID);
		} else if(f == 3) {
			this.worldObj.setBlockWithNotify(dx + 2, dy + 1, dz + 1, Block.lavaMoving.blockID);
		}

	}

	void deadEndDoorway(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		if(f == 0) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 0, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 0, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 1, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 1, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 2, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 2, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 2, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
		} else if(f == 1) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 0, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 0, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 1, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 1, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 2, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 2, dz + 1, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 2, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
		} else if(f == 2) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 0, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 0, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 1, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 1, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 2, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 2, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 2, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
		} else if(f == 3) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 0, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 0, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 1, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 1, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 2, dz + 0, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 2, dz + 1, this.maze.wallBlockID, this.maze.wallBlockMeta);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 2, dz + 2, this.maze.wallBlockID, this.maze.wallBlockMeta);
		}

	}

	void deadEndDoor(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.deadEndDoorway(x, z, f);
		if(f == 0) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 0, dz + 0, Block.doorWood.blockID, 1);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 1, dz + 0, Block.doorWood.blockID, 9);
		} else if(f == 1) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 0, dz + 1, Block.doorWood.blockID, 2);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 1, dz + 1, Block.doorWood.blockID, 10);
		} else if(f == 2) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 0, dz + 2, Block.doorWood.blockID, 3);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 1, dz + 2, Block.doorWood.blockID, 11);
		} else if(f == 3) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 0, dz + 1, Block.doorWood.blockID, 0);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 1, dz + 1, Block.doorWood.blockID, 8);
		}

	}

	void deadEndDoorSteel(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		this.deadEndDoorway(x, z, f);
		if(f == 0) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 0, dz + 0, Block.doorSteel.blockID, 1);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 1, dz + 0, Block.doorSteel.blockID, 9);
		} else if(f == 1) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 0, dz + 1, Block.doorSteel.blockID, 2);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 2, dy + 1, dz + 1, Block.doorSteel.blockID, 10);
		} else if(f == 2) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 0, dz + 2, Block.doorSteel.blockID, 3);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 1, dy + 1, dz + 2, Block.doorSteel.blockID, 11);
		} else if(f == 3) {
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 0, dz + 1, Block.doorSteel.blockID, 0);
			this.worldObj.setBlockAndMetadataWithNotify(dx + 0, dy + 1, dz + 1, Block.doorSteel.blockID, 8);
		}

	}

	void deadEndDoorTreasure(int x, int z, int f) {
		this.deadEndDoor(x, z, f);
		this.deadEndTreasure(x, z, f);
	}

	protected boolean placeMobSpawner(int dx, int dy, int dz, String mobID) {
		this.worldObj.setBlockWithNotify(dx, dy, dz, Block.mobSpawner.blockID);
		TileEntityMobSpawner ms = (TileEntityMobSpawner)this.worldObj.getBlockTileEntity(dx, dy, dz);
		if(ms != null) {
			ms.setMobID(mobID);
			return true;
		} else {
			return false;
		}
	}

	void decorate3x3Rooms(int[] rcoords) {
		for(int i = 0; i < rcoords.length / 2; ++i) {
			int dx = rcoords[i * 2];
			int dz = rcoords[i * 2 + 1];
			this.decorate3x3Room(dx, dz);
		}

	}

	void decorate3x3Room(int x, int z) {
		int dx = this.maze.getWorldX(x) + 1;
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z) + 1;
		this.roomSpawner(dx, dy, dz, 11);
		if(!this.roomTreasure(dx, dy, dz, 11) || this.rand.nextInt(2) == 0) {
			this.roomTreasure(dx, dy, dz, 11);
		}

		this.roomSpiderwebs(dx, dy, dz, 11);
	}

	private boolean roomSpawner(int dx, int dy, int dz, int diameter) {
		int rx = this.rand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.rand.nextInt(diameter) + dz - diameter / 2;
		return this.placeMobSpawner(rx, dy, rz, "Skeleton");
	}

	private boolean roomTreasure(int dx, int dy, int dz, int diameter) {
		int rx = this.rand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.rand.nextInt(diameter) + dz - diameter / 2;
		return this.worldObj.getBlockId(rx, dy, rz) != 0 ? false : TFTreasure.underhill_room.generate(this.worldObj, this.rand, rx, dy, rz);
	}

	private boolean roomSpiderwebs(int dx, int dy, int dz, int diameter) {
		int rx = dx;
		int rz = dz;
		int corner = this.rand.nextInt(4);
		if(corner == 0) {
			rx = dx - 5;
			rz = dz - 5;
		} else if(corner == 1) {
			rx = dx + 5;
			rz = dz - 5;
		} else if(corner == 2) {
			rx = dx - 5;
			rz = dz + 5;
		} else if(corner == 3) {
			rx = dx + 5;
			rz = dz + 5;
		}

		boolean flag = false;
		flag |= this.roomSpiderweb(rx, dy, rz, 3);
		flag |= this.roomSpiderweb(rx, dy, rz, 3);
		flag |= this.roomSpiderweb(rx, dy, rz, 3);
		return flag;
	}

	private boolean roomSpiderweb(int dx, int dy, int dz, int diameter) {
		int rx = this.rand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.rand.nextInt(diameter) + dz - diameter / 2;
		if(this.worldObj.getBlockId(rx, dy + 2, rz) != 0) {
			return false;
		} else {
			this.worldObj.setBlockWithNotify(rx, dy + 2, rz, Block.web.blockID);
			return true;
		}
	}
}

