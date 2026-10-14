package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;
import net.minecraft.game.world.terrain.generate.structure.StructurePieceTreasure;

/** A large room off a crossing, with one of five decorative layouts. */
public class StrongholdComponentRoomCrossing extends StrongholdComponent {
	private static final StructurePieceTreasure[] chestLoot = new StructurePieceTreasure[] {
		new StructurePieceTreasure(Item.ingotIron.shiftedIndex, 0, 1, 5, 10),
		new StructurePieceTreasure(Item.ingotGold.shiftedIndex, 0, 1, 3, 5),
		new StructurePieceTreasure(Item.redstone.shiftedIndex, 0, 4, 9, 5),
		new StructurePieceTreasure(Item.coal.shiftedIndex, 0, 3, 8, 10),
		new StructurePieceTreasure(Item.bread.shiftedIndex, 0, 1, 3, 15),
		new StructurePieceTreasure(Item.appleRed.shiftedIndex, 0, 1, 3, 15),
		new StructurePieceTreasure(Item.pickaxeSteel.shiftedIndex, 0, 1, 1, 1)
	};
	protected final StrongholdDoor doorType;
	protected final int roomType;

	public StrongholdComponentRoomCrossing(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
		this.roomType = rand.nextInt(5);
	}

	public static StrongholdComponentRoomCrossing findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -1, 0, 11, 7, 11, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null
				? new StrongholdComponentRoomCrossing(world, componentType, rand, box, coordBaseMode) : null;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		StrongholdComponentStairs2 start = (StrongholdComponentStairs2) startRoom;
		this.getNextComponentNormal(start, components, rand, 4, 1);
		this.getNextComponentX(start, components, rand, 1, 4);
		this.getNextComponentZ(start, components, rand, 1, 4);
	}

	@Override
	protected boolean addComponentParts(MineshaftWriter writer, Random rand, StructureBoundingBox chunkBox) {
		if(this.isLiquidInChunk(writer, chunkBox)) {
			return false;
		}
		if(this.getAirRatioInWalls(writer, chunkBox) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 10, 6, 10, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 4, 1, 0);
		this.fill(writer, 4, 1, 10, 6, 3, 10, 0, 0, false);
		this.fill(writer, 0, 1, 4, 0, 3, 6, 0, 0, false);
		this.fill(writer, 10, 1, 4, 10, 3, 6, 0, 0, false);

		int i4;
		switch(this.roomType) {
		case 0:
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 5, 1, 5);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 5, 2, 5);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 5, 3, 5);
			this.placeBlock(writer, Block.torchWood.blockID, 0, 4, 3, 5);
			this.placeBlock(writer, Block.torchWood.blockID, 0, 6, 3, 5);
			this.placeBlock(writer, Block.torchWood.blockID, 0, 5, 3, 4);
			this.placeBlock(writer, Block.torchWood.blockID, 0, 5, 3, 6);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 4, 1, 4);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 4, 1, 5);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 4, 1, 6);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 6, 1, 4);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 6, 1, 5);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 6, 1, 6);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 5, 1, 4);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 5, 1, 6);
			break;
		case 1:
			for(i4 = 0; i4 < 5; ++i4) {
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3, 1, 3 + i4);
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 7, 1, 3 + i4);
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3 + i4, 1, 3);
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3 + i4, 1, 7);
			}
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 5, 1, 5);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 5, 2, 5);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 5, 3, 5);
			this.placeBlock(writer, Block.waterMoving.blockID, 0, 5, 4, 5);
			break;
		case 2:
			for(i4 = 1; i4 <= 9; ++i4) {
				this.placeBlock(writer, Block.cobblestone.blockID, 0, 1, 3, i4);
				this.placeBlock(writer, Block.cobblestone.blockID, 0, 9, 3, i4);
			}
			for(i4 = 1; i4 <= 9; ++i4) {
				this.placeBlock(writer, Block.cobblestone.blockID, 0, i4, 3, 1);
				this.placeBlock(writer, Block.cobblestone.blockID, 0, i4, 3, 9);
			}
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 5, 1, 4);
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 5, 1, 6);
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 5, 3, 4);
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 5, 3, 6);
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 4, 1, 5);
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 6, 1, 5);
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 4, 3, 5);
			this.placeBlock(writer, Block.cobblestone.blockID, 0, 6, 3, 5);
			for(i4 = 1; i4 <= 3; ++i4) {
				this.placeBlock(writer, Block.cobblestone.blockID, 0, 4, i4, 4);
				this.placeBlock(writer, Block.cobblestone.blockID, 0, 6, i4, 4);
				this.placeBlock(writer, Block.cobblestone.blockID, 0, 4, i4, 6);
				this.placeBlock(writer, Block.cobblestone.blockID, 0, 6, i4, 6);
			}
			this.placeBlock(writer, Block.torchWood.blockID, 0, 5, 3, 5);
			for(i4 = 2; i4 <= 8; ++i4) {
				this.placeBlock(writer, Block.planks.blockID, 0, 2, 3, i4);
				this.placeBlock(writer, Block.planks.blockID, 0, 3, 3, i4);
				if(i4 <= 3 || i4 >= 7) {
					this.placeBlock(writer, Block.planks.blockID, 0, 4, 3, i4);
					this.placeBlock(writer, Block.planks.blockID, 0, 5, 3, i4);
					this.placeBlock(writer, Block.planks.blockID, 0, 6, 3, i4);
				}
				this.placeBlock(writer, Block.planks.blockID, 0, 7, 3, i4);
				this.placeBlock(writer, Block.planks.blockID, 0, 8, 3, i4);
			}
			this.placeBlock(writer, Block.ladder.blockID, this.getMetadataWithOffset(Block.ladder.blockID, 4), 9, 1, 3);
			this.placeBlock(writer, Block.ladder.blockID, this.getMetadataWithOffset(Block.ladder.blockID, 4), 9, 2, 3);
			this.placeBlock(writer, Block.ladder.blockID, this.getMetadataWithOffset(Block.ladder.blockID, 4), 9, 3, 3);
			this.createTreasureChest(writer, rand, 3, 4, 8, chestLoot, 1 + rand.nextInt(4));
			break;
		default:
			break;
		}

		Random spawnerRand = this.specialRandom();
		if(spawnerRand.nextFloat() < 0.8F) {
			int spawnerX = 1 + spawnerRand.nextInt(9);
			int spawnerZ = 1 + spawnerRand.nextInt(9);
			if(spawnerX == 5 && spawnerZ == 5) {
				spawnerZ = 4;
			}
			this.placeRandomMobSpawner(writer, spawnerRand, spawnerX, 1, spawnerZ);
		}
		return true;
	}
}
