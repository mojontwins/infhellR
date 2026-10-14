package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/** A four-way crossing room, each side optionally opening onto a branch. */
public class StrongholdComponentCrossing extends StrongholdComponent {
	protected final StrongholdDoor doorType;
	private final boolean connectsXLow;
	private final boolean connectsXHigh;
	private final boolean connectsZLow;
	private final boolean connectsZHigh;

	public StrongholdComponentCrossing(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
		this.connectsXLow = rand.nextBoolean();
		this.connectsXHigh = rand.nextBoolean();
		this.connectsZLow = rand.nextBoolean();
		this.connectsZHigh = rand.nextInt(3) > 0;
	}

	public static StrongholdComponentCrossing findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -4, -3, 0, 10, 9, 11, coordBaseMode);
		StrongholdComponentCrossing component = new StrongholdComponentCrossing(world, componentType, rand, box, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null ? component : null;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		StrongholdComponentStairs2 start = (StrongholdComponentStairs2) startRoom;
		int low = 3;
		int high = 5;
		if(this.coordBaseMode == 1 || this.coordBaseMode == 2) {
			low = 8 - low;
			high = 8 - high;
		}

		this.getNextComponentNormal(start, components, rand, 5, 1);
		if(this.connectsXLow) {
			this.getNextComponentX(start, components, rand, low, 1);
		}
		if(this.connectsXHigh) {
			this.getNextComponentX(start, components, rand, high, 7);
		}
		if(this.connectsZLow) {
			this.getNextComponentZ(start, components, rand, low, 1);
		}
		if(this.connectsZHigh) {
			this.getNextComponentZ(start, components, rand, high, 7);
		}
	}

	@Override
	protected boolean addComponentParts(MineshaftWriter writer, Random rand, StructureBoundingBox chunkBox) {
		if(this.isLiquidInChunk(writer, chunkBox)) {
			return false;
		}
		if(this.getAirRatioBelow(writer, chunkBox) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 9, 8, 10, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 4, 3, 0);
		if(this.connectsXLow) {
			this.fill(writer, 0, 3, 1, 0, 5, 3, 0, 0, false);
		}
		if(this.connectsZLow) {
			this.fill(writer, 9, 3, 1, 9, 5, 3, 0, 0, false);
		}
		if(this.connectsXHigh) {
			this.fill(writer, 0, 5, 7, 0, 7, 9, 0, 0, false);
		}
		if(this.connectsZHigh) {
			this.fill(writer, 9, 5, 7, 9, 7, 9, 0, 0, false);
		}

		this.fill(writer, 5, 1, 10, 7, 3, 10, 0, 0, false);
		this.fillWithRandomizedBlocks(writer, chunkBox, 1, 2, 1, 8, 2, 6, false, rand, StrongholdPieces.getStrongholdStones());
		this.fillWithRandomizedBlocks(writer, chunkBox, 4, 1, 5, 4, 4, 9, false, rand, StrongholdPieces.getStrongholdStones());
		this.fillWithRandomizedBlocks(writer, chunkBox, 8, 1, 5, 8, 4, 9, false, rand, StrongholdPieces.getStrongholdStones());
		this.fillWithRandomizedBlocks(writer, chunkBox, 1, 4, 7, 3, 4, 9, false, rand, StrongholdPieces.getStrongholdStones());
		this.fillWithRandomizedBlocks(writer, chunkBox, 1, 3, 5, 3, 3, 6, false, rand, StrongholdPieces.getStrongholdStones());
		this.fill(writer, 1, 3, 4, 3, 3, 4, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fill(writer, 1, 4, 6, 3, 4, 6, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fillWithRandomizedBlocks(writer, chunkBox, 5, 1, 7, 7, 1, 8, false, rand, StrongholdPieces.getStrongholdStones());
		this.fill(writer, 5, 1, 9, 7, 1, 9, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fill(writer, 5, 2, 7, 7, 2, 7, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fill(writer, 4, 5, 7, 4, 5, 9, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fill(writer, 8, 5, 7, 8, 5, 9, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fill(writer, 5, 5, 7, 7, 5, 9, Block.stairDouble.blockID, Block.stairDouble.blockID, false);
		this.placeBlock(writer, Block.torchWood.blockID, 0, 6, 5, 6);
		return true;
	}
}
