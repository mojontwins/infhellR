package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/**
 * A mineshaft corridor: a 3-wide tunnel that may carry rails, spider webs and a spider
 * spawner. The structural shell (floor, cleared interior, ceiling, webs) is laid during
 * generation; the world-dependent decorations (arches vs. hanging chains, rails, torches,
 * chests and the spawner) are applied during population.
 */
public class MineshaftComponentCorridor extends MineshaftComponent {
	protected final boolean hasRails;
	protected final boolean hasSpiders;
	protected boolean spawnerPlaced;
	protected final int sectionCount;

	public MineshaftComponentCorridor(World world, int componentType, Random rand, StructureBoundingBox boundingBox, int direction) {
		super(world, componentType);
		this.coordBaseMode = direction;
		this.boundingBox = boundingBox;
		this.hasRails = rand.nextInt(3) == 0;
		this.hasSpiders = !this.hasRails && rand.nextInt(15) == 0;
		if(this.coordBaseMode != 2 && this.coordBaseMode != 0) {
			this.sectionCount = boundingBox.getXSize() / 5;
		} else {
			this.sectionCount = boundingBox.getZSize() / 5;
		}
	}

	public static StructureBoundingBox findValidPlacement(List<MineshaftComponent> components, Random rand, int x, int y, int z, int direction) {
		StructureBoundingBox box = new StructureBoundingBox(x, y, z, x, y + 2, z);

		int length;
		for(length = rand.nextInt(3) + 2; length > 0; --length) {
			int span = length * 5;
			switch(direction) {
			case 0:
				box.maxX = x + 2;
				box.maxZ = z + (span - 1);
				break;
			case 1:
				box.minX = x - (span - 1);
				box.maxZ = z + 2;
				break;
			case 2:
				box.maxX = x + 2;
				box.minZ = z - (span - 1);
				break;
			case 3:
				box.maxX = x + (span - 1);
				box.maxZ = z + 2;
			}

			if(findIntersecting(components, box) == null) {
				break;
			}
		}

		return length > 0 ? box : null;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		int componentType = this.getComponentType();
		int roll = rand.nextInt(4);
		switch(this.coordBaseMode) {
		case 0:
			if(roll <= 1) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, this.coordBaseMode, componentType);
			} else if(roll == 2) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ - 3, 1, componentType);
			} else {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ - 3, 3, componentType);
			}
			break;
		case 1:
			if(roll <= 1) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, this.coordBaseMode, componentType);
			} else if(roll == 2) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, 2, componentType);
			} else {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, 0, componentType);
			}
			break;
		case 2:
			if(roll <= 1) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, this.coordBaseMode, componentType);
			} else if(roll == 2) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, 1, componentType);
			} else {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, 3, componentType);
			}
			break;
		case 3:
			if(roll <= 1) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, this.coordBaseMode, componentType);
			} else if(roll == 2) {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, 2, componentType);
			} else {
				this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, 0, componentType);
			}
		}

		if(componentType < 8) {
			int offset;
			int roll2;
			if(this.coordBaseMode != 2 && this.coordBaseMode != 0) {
				for(offset = this.boundingBox.minX + 3; offset + 3 <= this.boundingBox.maxX; offset += 5) {
					roll2 = rand.nextInt(5);
					if(roll2 == 0) {
						this.pieces.getNextComponent(startRoom, components, rand, offset, this.boundingBox.minY, this.boundingBox.minZ - 1, 2, componentType + 1);
					} else if(roll2 == 1) {
						this.pieces.getNextComponent(startRoom, components, rand, offset, this.boundingBox.minY, this.boundingBox.maxZ + 1, 0, componentType + 1);
					}
				}
			} else {
				for(offset = this.boundingBox.minZ + 3; offset + 3 <= this.boundingBox.maxZ; offset += 5) {
					roll2 = rand.nextInt(5);
					if(roll2 == 0) {
						this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.minX - 1, this.boundingBox.minY, offset, 1, componentType + 1);
					} else if(roll2 == 1) {
						this.pieces.getNextComponent(startRoom, components, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, offset, 3, componentType + 1);
					}
				}
			}
		}
	}

	@Override
	public void generate(MineshaftWriter writer, Random rand) {
		int length = this.sectionCount * 5 - 1;
		this.fill(writer, 0, 0, 0, 2, 1, length, 0, 0, false);
		this.fillVariation(writer, 0, -1, 0, 2, 0, length, Block.stone.blockID, Block.cobblestone.blockID, rand, 4, true);
		this.randomFill(writer, rand, 0.9F, 0, 2, 0, 2, 2, length, 0, 0, false);
		if(this.hasSpiders) {
			this.randomFill(writer, rand, 0.6F, 0, 0, 0, 2, 1, length, Block.web.blockID, 0, false);
		}
	}

	@Override
	public void populate(MineshaftWriter writer, Random rand) {
		int length = this.sectionCount * 5 - 1;

		for(int section = 0; section < this.sectionCount; ++section) {
			int midZ = 2 + section * 5;

			// Only build a support/decoration where the corridor has a ceiling above it.
			if(this.getBlockIDNoClip(writer, 1, 3, midZ) != 0) {
				// Arch: two fence posts carrying a plank/slab lintel.
				if(this.getBlockIDNoClip(writer, 0, -1, midZ) != 0 && this.getBlockIDNoClip(writer, 2, -1, midZ) != 0) {
					this.fill(writer, 0, 0, midZ, 0, 1, midZ, Block.fence.blockID, 0, false);
					this.fill(writer, 2, 0, midZ, 2, 1, midZ, Block.fence.blockID, 0, false);
					this.placeBlock(writer, Block.planks.blockID, 0, 0, 2, midZ);
					this.placeBlock(writer, Block.stairSingle.blockID, 2 | 8, 1, 2, midZ);
					this.placeBlock(writer, Block.planks.blockID, 0, 2, 2, midZ);
				}
			} else {
				// Open skyward: hang a chain down from an intact ceiling block.
				if(this.getBlockIDNoClip(writer, 0, -1, midZ) != 0 && !this.canBlockSeeTheSky(writer, 0, 1, midZ)) {
					this.placeBlock(writer, Block.fence.blockID, 0, 0, 0, midZ);
					int y = 1;
					while(this.getBlockIDNoClip(writer, 0, y, midZ) == 0) {
						this.placeBlockNoVertClip(writer, Block.chain.blockID, 0, 0, y, midZ);
						++y;
					}
				}

				if(this.getBlockIDNoClip(writer, 2, -1, midZ) != 0 && !this.canBlockSeeTheSky(writer, 2, 1, midZ)) {
					this.placeBlock(writer, Block.fence.blockID, 0, 2, 0, midZ);
					int y = 1;
					while(this.getBlockIDNoClip(writer, 2, y, midZ) == 0) {
						this.placeBlockNoVertClip(writer, Block.chain.blockID, 0, 2, y, midZ);
						++y;
					}
				}
			}

			this.randomBlock(writer, rand, 0.1F, 0, 2, midZ - 1, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.1F, 2, 2, midZ - 1, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.1F, 0, 2, midZ + 1, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.1F, 2, 2, midZ + 1, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.05F, 0, 2, midZ - 2, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.05F, 2, 2, midZ - 2, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.05F, 0, 2, midZ + 2, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.05F, 2, 2, midZ + 2, Block.web.blockID, 0);
			this.randomBlock(writer, rand, 0.05F, 1, 2, midZ - 1, Block.torchWood.blockID, 0);
			this.randomBlock(writer, rand, 0.05F, 1, 2, midZ + 1, Block.torchWood.blockID, 0);

			if(rand.nextInt(100) == 0) {
				this.createTreasureChest(writer, rand, 2, 0, midZ - 1, this.pieces.getLoot(), 3 + rand.nextInt(4));
			}
			if(rand.nextInt(100) == 0) {
				this.createTreasureChest(writer, rand, 0, 0, midZ + 1, this.pieces.getLoot(), 3 + rand.nextInt(4));
			}

			if(this.hasSpiders && !this.spawnerPlaced) {
				int yAbs = this.getYWithOffset(0);
				int randZ = midZ - 1 + rand.nextInt(3);
				int xAbs = this.getXWithOffset(1, randZ);
				int zAbs = this.getZWithOffset(1, randZ);
				if(writer.isInChunk(xAbs, zAbs)) {
					this.spawnerPlaced = true;
					this.placeSpawner(writer, 1, 0, randZ, "Spider");

					World world = writer.getWorld();
					for(int dy = -1; dy <= 1; ++dy) {
						for(int dx = -4; dx <= 4; ++dx) {
							for(int dz = -4; dz <= 4; ++dz) {
								int xx = xAbs + dx;
								int yy = yAbs + dy;
								int zz = zAbs + dz;
								if(world.getBlockId(xx, yy, zz) == 0) {
									world.setBlockWithNotify(xx, yy, zz, Block.web.blockID);
								}
							}
						}
					}
				}
			}
		}

		// Lay a plank floor wherever the natural floor is missing.
		for(int x = 0; x <= 2; ++x) {
			for(int z = 0; z <= length; ++z) {
				if(this.getBlockID(writer, x, -1, z) == 0) {
					this.placeBlock(writer, Block.planks.blockID, 0, x, -1, z);
				}
			}
		}

		if(this.hasRails) {
			for(int z = 0; z <= length; ++z) {
				int below = this.getBlockID(writer, 1, -1, z);
				if(below > 0 && Block.opaqueCubeLookup[below]) {
					this.randomBlock(writer, rand, 0.7F, 1, 0, z, Block.rail.blockID, this.getMetadataWithOffset(Block.rail.blockID, 0));
				}
			}
		}
	}
}
