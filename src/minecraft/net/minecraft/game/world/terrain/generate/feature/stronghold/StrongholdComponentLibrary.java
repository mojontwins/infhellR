package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;
import net.minecraft.game.world.terrain.generate.feature.StructurePieceTreasure;

/** The stronghold library: two rows of bookcases, a reading platform and treasure. */
public class StrongholdComponentLibrary extends StrongholdComponent {
	private static final StructurePieceTreasure[] treasureList = new StructurePieceTreasure[] {
		new StructurePieceTreasure(Item.book.shiftedIndex, 0, 1, 3, 20),
		new StructurePieceTreasure(Item.paper.shiftedIndex, 0, 2, 7, 20),
		new StructurePieceTreasure(Block.cryingObsidian.blockID, 0, 1, 1, 1),
		new StructurePieceTreasure(Item.compass.shiftedIndex, 0, 1, 1, 1)
	};
	protected final StrongholdDoor doorType;
	private final boolean isLargeRoom;

	public StrongholdComponentLibrary(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
		this.isLargeRoom = box.getYSize() > 6;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
	}

	public static StrongholdComponentLibrary findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 14, 11, 15, coordBaseMode);
		if(!canStrongholdGoDeeper(box) || MineshaftComponent.findIntersecting(components, box) != null) {
			box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 14, 6, 15, coordBaseMode);
			if(!canStrongholdGoDeeper(box) || MineshaftComponent.findIntersecting(components, box) != null) {
				return null;
			}
		}
		return new StrongholdComponentLibrary(world, componentType, rand, box, coordBaseMode);
	}

	@Override
	protected boolean addComponentParts(MineshaftWriter writer, Random rand, StructureBoundingBox chunkBox) {
		if(this.isLiquidInChunk(writer, chunkBox)) {
			return false;
		}
		if(this.getAirRatioInWalls(writer, chunkBox) > 0.2F) {
			return false;
		}

		int height = this.isLargeRoom ? 11 : 6;
		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 13, height - 1, 14, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 4, 1, 0);
		this.randomFill(writer, rand, 0.07F, 2, 1, 1, 11, 4, 13, Block.web.blockID, Block.web.blockID, false);

		int z;
		for(z = 1; z <= 13; ++z) {
			if((z - 1) % 4 == 0) {
				this.fill(writer, 1, 1, z, 1, 4, z, Block.planks.blockID, Block.planks.blockID, false);
				this.fill(writer, 12, 1, z, 12, 4, z, Block.planks.blockID, Block.planks.blockID, false);
				this.placeBlock(writer, Block.torchWood.blockID, 0, 2, 3, z);
				this.placeBlock(writer, Block.torchWood.blockID, 0, 11, 3, z);
				if(this.isLargeRoom) {
					this.fill(writer, 1, 6, z, 1, 9, z, Block.planks.blockID, Block.planks.blockID, false);
					this.fill(writer, 12, 6, z, 12, 9, z, Block.planks.blockID, Block.planks.blockID, false);
				}
			} else {
				this.fill(writer, 1, 1, z, 1, 4, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
				this.fill(writer, 12, 1, z, 12, 4, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
				if(this.isLargeRoom) {
					this.fill(writer, 1, 6, z, 1, 9, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
					this.fill(writer, 12, 6, z, 12, 9, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
				}
			}
		}

		for(z = 3; z < 12; z += 2) {
			this.fill(writer, 3, 1, z, 4, 3, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
			this.fill(writer, 6, 1, z, 7, 3, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
			this.fill(writer, 9, 1, z, 10, 3, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
		}

		if(this.isLargeRoom) {
			this.fill(writer, 1, 5, 1, 3, 5, 13, Block.planks.blockID, Block.planks.blockID, false);
			this.fill(writer, 10, 5, 1, 12, 5, 13, Block.planks.blockID, Block.planks.blockID, false);
			this.fill(writer, 4, 5, 1, 9, 5, 2, Block.planks.blockID, Block.planks.blockID, false);
			this.fill(writer, 4, 5, 12, 9, 5, 13, Block.planks.blockID, Block.planks.blockID, false);
			this.placeBlock(writer, Block.planks.blockID, 0, 9, 5, 11);
			this.placeBlock(writer, Block.planks.blockID, 0, 8, 5, 11);
			this.placeBlock(writer, Block.planks.blockID, 0, 9, 5, 10);
			this.fill(writer, 3, 6, 2, 3, 6, 12, Block.fence.blockID, Block.fence.blockID, false);
			this.fill(writer, 10, 6, 2, 10, 6, 10, Block.fence.blockID, Block.fence.blockID, false);
			this.fill(writer, 4, 6, 2, 9, 6, 2, Block.fence.blockID, Block.fence.blockID, false);
			this.fill(writer, 4, 6, 12, 8, 6, 12, Block.fence.blockID, Block.fence.blockID, false);
			this.placeBlock(writer, Block.fence.blockID, 0, 9, 6, 11);
			this.placeBlock(writer, Block.fence.blockID, 0, 8, 6, 11);
			this.placeBlock(writer, Block.fence.blockID, 0, 9, 6, 10);
			int ladderMeta = this.getMetadataWithOffset(Block.ladder.blockID, 3);
			this.placeBlock(writer, Block.ladder.blockID, ladderMeta, 10, 1, 13);
			this.placeBlock(writer, Block.ladder.blockID, ladderMeta, 10, 2, 13);
			this.placeBlock(writer, Block.ladder.blockID, ladderMeta, 10, 3, 13);
			this.placeBlock(writer, Block.ladder.blockID, ladderMeta, 10, 4, 13);
			this.placeBlock(writer, Block.ladder.blockID, ladderMeta, 10, 5, 13);
			this.placeBlock(writer, Block.ladder.blockID, ladderMeta, 10, 6, 13);
			this.placeBlock(writer, Block.ladder.blockID, ladderMeta, 10, 7, 13);
			int px = 7;
			int pz = 7;
			this.placeBlock(writer, Block.fence.blockID, 0, px - 1, 9, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px, 9, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px - 1, 8, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px, 8, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px - 1, 7, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px, 7, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px - 2, 7, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px + 1, 7, pz);
			this.placeBlock(writer, Block.fence.blockID, 0, px - 1, 7, pz - 1);
			this.placeBlock(writer, Block.fence.blockID, 0, px - 1, 7, pz + 1);
			this.placeBlock(writer, Block.fence.blockID, 0, px, 7, pz - 1);
			this.placeBlock(writer, Block.fence.blockID, 0, px, 7, pz + 1);
			this.placeBlock(writer, Block.torchWood.blockID, 0, px - 2, 8, pz);
			this.placeBlock(writer, Block.torchWood.blockID, 0, px + 1, 8, pz);
			this.placeBlock(writer, Block.torchWood.blockID, 0, px - 1, 8, pz - 1);
			this.placeBlock(writer, Block.torchWood.blockID, 0, px - 1, 8, pz + 1);
			this.placeBlock(writer, Block.torchWood.blockID, 0, px, 8, pz - 1);
			this.placeBlock(writer, Block.torchWood.blockID, 0, px, 8, pz + 1);
		}

		this.createTreasureChest(writer, rand, 3, 3, 5, treasureList, 1 + rand.nextInt(4));
		if(this.isLargeRoom) {
			this.placeBlock(writer, 0, 0, 12, 9, 1);
			this.createTreasureChest(writer, rand, 12, 8, 1, treasureList, 1 + rand.nextInt(4));
		}

		Random spawnerRand = this.specialRandom();
		List<int[]> topShelves = new ArrayList<int[]>();
		int wallTop = this.isLargeRoom ? 9 : 4;
		for(int sz = 1; sz <= 13; ++sz) {
			if((sz - 1) % 4 != 0) {
				topShelves.add(new int[] {1, wallTop, sz});
				topShelves.add(new int[] {12, wallTop, sz});
			}
		}
		for(int sz = 3; sz < 12; sz += 2) {
			topShelves.add(new int[] {3, 3, sz});
			topShelves.add(new int[] {4, 3, sz});
			topShelves.add(new int[] {6, 3, sz});
			topShelves.add(new int[] {7, 3, sz});
			topShelves.add(new int[] {9, 3, sz});
			topShelves.add(new int[] {10, 3, sz});
		}
		for(int roll = 0; roll < 3 && !topShelves.isEmpty(); ++roll) {
			if(spawnerRand.nextFloat() < 0.8F) {
				int[] spot = topShelves.remove(spawnerRand.nextInt(topShelves.size()));
				this.placeRandomMobSpawner(writer, spawnerRand, spot[0], spot[1], spot[2]);
			}
		}
		return true;
	}
}
