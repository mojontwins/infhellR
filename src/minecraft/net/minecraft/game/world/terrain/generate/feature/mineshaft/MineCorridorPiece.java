package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A straight mineshaft corridor, optionally lined with rails or infested with spiders. */
public class MineCorridorPiece extends StructurePiece {
	private final boolean hasRails;
	private final boolean hasSpiders;
	private boolean spawnerPlaced;
	private final int sectionCount;

	public MineCorridorPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.box = box;
		this.hasRails = rand.nextInt(3) == 0;
		this.hasSpiders = !this.hasRails && rand.nextInt(15) == 0;
		if(this.coordBaseMode != 2 && this.coordBaseMode != 0) {
			this.sectionCount = box.getXSize() / 5;
		} else {
			this.sectionCount = box.getZSize() / 5;
		}
	}

	public static FeatureAABB findValidPlacement(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation) {
		FeatureAABB box = new FeatureAABB(x, y, z, x, y + 2, z);

		int sections;
		for(sections = rand.nextInt(3) + 2; sections > 0; --sections) {
			int length = sections * 5;
			switch(orientation) {
			case 0:
				box.x2 = x + 2;
				box.z2 = z + (length - 1);
				break;
			case 1:
				box.x1 = x - (length - 1);
				box.z2 = z + 2;
				break;
			case 2:
				box.x2 = x + 2;
				box.z1 = z - (length - 1);
				break;
			case 3:
				box.x2 = x + (length - 1);
				box.z2 = z + 2;
			}

			if(StructurePiece.findIntersecting(pieces, box) == null) {
				break;
			}
		}

		return sections > 0 ? box : null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		int direction = rand.nextInt(4);
		switch(this.coordBaseMode) {
		case 0:
			if(direction <= 1) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1, this.box.y1 - 1 + rand.nextInt(3), this.box.z2 + 1, this.coordBaseMode, this.getPieceType());
			} else if(direction == 2) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 - 1 + rand.nextInt(3), this.box.z2 - 3, 1, this.getPieceType());
			} else {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 - 1 + rand.nextInt(3), this.box.z2 - 3, 3, this.getPieceType());
			}
			break;
		case 1:
			if(direction <= 1) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 - 1 + rand.nextInt(3), this.box.z1, this.coordBaseMode, this.getPieceType());
			} else if(direction == 2) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1, this.box.y1 - 1 + rand.nextInt(3), this.box.z1 - 1, 2, this.getPieceType());
			} else {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1, this.box.y1 - 1 + rand.nextInt(3), this.box.z2 + 1, 0, this.getPieceType());
			}
			break;
		case 2:
			if(direction <= 1) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1, this.box.y1 - 1 + rand.nextInt(3), this.box.z1 - 1, this.coordBaseMode, this.getPieceType());
			} else if(direction == 2) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 - 1 + rand.nextInt(3), this.box.z1, 1, this.getPieceType());
			} else {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 - 1 + rand.nextInt(3), this.box.z1, 3, this.getPieceType());
			}
			break;
		case 3:
			if(direction <= 1) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 - 1 + rand.nextInt(3), this.box.z1, this.coordBaseMode, this.getPieceType());
			} else if(direction == 2) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x2 - 3, this.box.y1 - 1 + rand.nextInt(3), this.box.z1 - 1, 2, this.getPieceType());
			} else {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x2 - 3, this.box.y1 - 1 + rand.nextInt(3), this.box.z2 + 1, 0, this.getPieceType());
			}
		}

		if(this.getPieceType() < 8) {
			int x;
			int roll;
			if(this.coordBaseMode != 2 && this.coordBaseMode != 0) {
				for(x = this.box.x1 + 3; x + 3 <= this.box.x2; x += 5) {
					roll = rand.nextInt(5);
					if(roll == 0) {
						this.owner.requestNextPiece(this, pieces, rand, x, this.box.y1, this.box.z1 - 1, 2, this.getPieceType() + 1);
					} else if(roll == 1) {
						this.owner.requestNextPiece(this, pieces, rand, x, this.box.y1, this.box.z2 + 1, 0, this.getPieceType() + 1);
					}
				}
			} else {
				for(x = this.box.z1 + 3; x + 3 <= this.box.z2; x += 5) {
					roll = rand.nextInt(5);
					if(roll == 0) {
						this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1, x, 1, this.getPieceType() + 1);
					} else if(roll == 1) {
						this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1, x, 3, this.getPieceType() + 1);
					}
				}
			}
		}
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(mostlySolid && !this.isMostlySolid(world, clip)) {
			return false;
		}

		int lastSection = this.sectionCount * 5 - 1;
		this.fillWithBlocks(world, clip, 0, 0, 0, 2, 1, lastSection, 0, 0, false);
		this.fillWithBlockVariation(world, clip, 0, -1, 0, 2, 0, lastSection, Block.stone.blockID, Block.cobblestone.blockID, rand, 4, true);
		this.randomlyFillWithBlocks(world, clip, rand, 0.9F, 0, 2, 0, 2, 2, lastSection, 0, 0, false);
		if(this.hasSpiders) {
			this.randomlyFillWithBlocks(world, clip, rand, 0.6F, 0, 0, 0, 2, 1, lastSection, Block.web.blockID, 0, false);
		}

		int section;
		int along;
		for(section = 0; section < this.sectionCount; ++section) {
			along = 2 + section * 5;

			if(this.getBlockIdNoVertClip(world, 1, 3, along, clip) != 0) {
				if(this.getBlockIdNoVertClip(world, 0, -1, along, clip) != 0 && this.getBlockIdNoVertClip(world, 2, -1, along, clip) != 0) {
					this.fillWithBlocks(world, clip, 0, 0, along, 0, 1, along, Block.fence.blockID, 0, false);
					this.fillWithBlocks(world, clip, 2, 0, along, 2, 1, along, Block.fence.blockID, 0, false);
					this.placeBlock(world, Block.planks.blockID, 0, 0, 2, along, clip);
					this.placeBlock(world, Block.stairSingle.blockID, 2 | 8, 1, 2, along, clip);
					this.placeBlock(world, Block.planks.blockID, 0, 2, 2, along, clip);
				}
			} else {
				if(this.getBlockIdNoVertClip(world, 0, -1, along, clip) != 0 && !this.canBlockSeeTheSky(world, 0, 1, along, clip)) {
					this.placeBlock(world, Block.fence.blockID, 0, 0, 0, along, clip);
					int y = 1;
					while(this.getBlockIdNoVertClip(world, 0, y, along, clip) == 0) {
						this.placeBlockNoVertClip(world, Block.chain.blockID, 0, 0, y, along, clip);
						++y;
					}
				}

				if(this.getBlockIdNoVertClip(world, 2, -1, along, clip) != 0 && !this.canBlockSeeTheSky(world, 2, 1, along, clip)) {
					this.placeBlock(world, Block.fence.blockID, 0, 2, 0, along, clip);
					int y = 1;
					while(this.getBlockIdNoVertClip(world, 2, y, along, clip) == 0) {
						this.placeBlockNoVertClip(world, Block.chain.blockID, 0, 2, y, along, clip);
						++y;
					}
				}
			}

			this.randomlyPlaceBlock(world, clip, rand, 0.1F, 0, 2, along - 1, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.1F, 2, 2, along - 1, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.1F, 0, 2, along + 1, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.1F, 2, 2, along + 1, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.05F, 0, 2, along - 2, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.05F, 2, 2, along - 2, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.05F, 0, 2, along + 2, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.05F, 2, 2, along + 2, Block.web.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.05F, 1, 2, along - 1, Block.torchWood.blockID, 0);
			this.randomlyPlaceBlock(world, clip, rand, 0.05F, 1, 2, along + 1, Block.torchWood.blockID, 0);

			if(rand.nextInt(100) == 0) {
				this.placeLootChest(world, clip, rand, 2, 0, along - 1, MineshaftLoot.ENTRIES, 3 + rand.nextInt(4));
			}
			if(rand.nextInt(100) == 0) {
				this.placeLootChest(world, clip, rand, 0, 0, along + 1, MineshaftLoot.ENTRIES, 3 + rand.nextInt(4));
			}

			if(this.hasSpiders && !this.spawnerPlaced) {
				int spawnerY = this.getYWithOffset(0);
				int spawnerIndex = along - 1 + rand.nextInt(3);
				int spawnerX = this.getXWithOffset(1, spawnerIndex);
				int spawnerZ = this.getZWithOffset(1, spawnerIndex);
				if(clip.isVecInside(spawnerX, spawnerY, spawnerZ)) {
					this.spawnerPlaced = true;
					world.setBlockWithNotify(spawnerX, spawnerY, spawnerZ, Block.mobSpawner.blockID);
					TileEntityMobSpawner spawner = (TileEntityMobSpawner)world.getBlockTileEntity(spawnerX, spawnerY, spawnerZ);
					if(spawner != null) {
						spawner.setMobID("Spider");
					}

					for(int dy = -1; dy <= 1; ++dy) {
						for(int dx = -4; dx <= 4; ++dx) {
							for(int dz = -4; dz <= 4; ++dz) {
								int wx = spawnerX + dx;
								int wy = spawnerY + dy;
								int wz = spawnerZ + dz;
								if(world.getBlockId(wx, wy, wz) == 0) {
									world.setBlockWithNotify(wx, wy, wz, Block.web.blockID);
								}
							}
						}
					}
				}
			}
		}

		for(section = 0; section <= 2; ++section) {
			for(along = 0; along <= lastSection; ++along) {
				if(this.getBlockId(world, section, -1, along, clip) == 0) {
					this.placeBlock(world, Block.planks.blockID, 0, section, -1, along, clip);
				}
			}
		}

		if(this.hasRails) {
			for(section = 0; section <= lastSection; ++section) {
				int floorId = this.getBlockId(world, 1, -1, section, clip);
				if(floorId > 0 && Block.opaqueCubeLookup[floorId]) {
					this.randomlyPlaceBlock(world, clip, rand, 0.7F, 1, 0, section, Block.rail.blockID, this.getMetadataWithOffset(Block.rail.blockID, 0));
				}
			}
		}

		return true;
	}
}
