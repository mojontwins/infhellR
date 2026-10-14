package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;
import net.minecraft.game.world.terrain.generate.feature.StructurePieceTreasure;

/** A dead-end corridor with a chest at the far wall. */
public class StrongholdComponentChestCorridor extends StrongholdComponent {
	private static final StructurePieceTreasure[] chestLoot = new StructurePieceTreasure[] {
		new StructurePieceTreasure(Block.cryingObsidian.blockID, 0, 1, 1, 10),
		new StructurePieceTreasure(Item.diamond.shiftedIndex, 0, 1, 3, 3),
		new StructurePieceTreasure(Item.nametagSimple.shiftedIndex, 0, 1, 1, 3),
		new StructurePieceTreasure(Item.ingotIron.shiftedIndex, 0, 1, 5, 10),
		new StructurePieceTreasure(Item.ingotGold.shiftedIndex, 0, 1, 3, 5),
		new StructurePieceTreasure(Item.redstone.shiftedIndex, 0, 4, 9, 5),
		new StructurePieceTreasure(Item.bread.shiftedIndex, 0, 1, 3, 15),
		new StructurePieceTreasure(Item.appleRed.shiftedIndex, 0, 1, 3, 15),
		new StructurePieceTreasure(Item.pickaxeSteel.shiftedIndex, 0, 1, 1, 5),
		new StructurePieceTreasure(Item.swordSteel.shiftedIndex, 0, 1, 1, 5),
		new StructurePieceTreasure(Item.plateSteel.shiftedIndex, 0, 1, 1, 5),
		new StructurePieceTreasure(Item.helmetSteel.shiftedIndex, 0, 1, 1, 5),
		new StructurePieceTreasure(Item.legsSteel.shiftedIndex, 0, 1, 1, 5),
		new StructurePieceTreasure(Item.bootsSteel.shiftedIndex, 0, 1, 1, 5),
		new StructurePieceTreasure(Item.appleGold.shiftedIndex, 0, 1, 1, 1)
	};
	private final StrongholdDoor doorType;
	private boolean hasMadeChest;

	public StrongholdComponentChestCorridor(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
	}

	public static StrongholdComponentChestCorridor findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null
				? new StrongholdComponentChestCorridor(world, componentType, rand, box, coordBaseMode) : null;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		this.getNextComponentNormal((StrongholdComponentStairs2) startRoom, components, rand, 1, 1);
	}

	@Override
	protected boolean addComponentParts(MineshaftWriter writer, Random rand, StructureBoundingBox chunkBox) {
		if(this.isLiquidInChunk(writer, chunkBox)) {
			return false;
		}

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 4, 4, 6, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 1, 1, 0);
		this.placeDoor(writer, rand, StrongholdDoor.OPENING, 1, 1, 6);
		this.fill(writer, 3, 1, 2, 3, 1, 4, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_ID, false);
		this.placeBlock(writer, Block.stairSingle.blockID, 5, 3, 1, 1);
		this.placeBlock(writer, Block.stairSingle.blockID, 5, 3, 1, 5);
		this.placeBlock(writer, Block.stairSingle.blockID, 5, 3, 2, 2);
		this.placeBlock(writer, Block.stairSingle.blockID, 5, 3, 2, 4);

		int i4;
		for(i4 = 2; i4 <= 4; ++i4) {
			this.placeBlock(writer, Block.stairSingle.blockID, 5, 2, 1, i4);
		}

		if(!this.hasMadeChest) {
			i4 = this.getYWithOffset(2);
			int x = this.getXWithOffset(3, 3);
			int z = this.getZWithOffset(3, 3);
			if(chunkBox.isVecInside(x, i4, z)) {
				this.hasMadeChest = true;
				this.createTreasureChest(writer, rand, 3, 2, 3, chestLoot, 2 + rand.nextInt(2));
			}
		}
		return true;
	}
}
