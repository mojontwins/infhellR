package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/** A straight flight of cobblestone stairs climbing up and out of the stronghold. */
public class StrongholdComponentStairsStraight extends StrongholdComponent {
	private final StrongholdDoor doorType;

	public StrongholdComponentStairsStraight(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
	}

	public static StrongholdComponentStairsStraight findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -7, 0, 5, 11, 8, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null
				? new StrongholdComponentStairsStraight(world, componentType, rand, box, coordBaseMode) : null;
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
		if(this.getAirRatioBelow(writer, chunkBox) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 4, 10, 7, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 1, 7, 0);
		this.placeDoor(writer, rand, StrongholdDoor.OPENING, 1, 1, 7);
		int stairMeta = this.getMetadataWithOffset(Block.stairCompactCobblestone.blockID, 2);

		for(int step = 0; step < 6; ++step) {
			if(this.getBlockID(writer, 1, 6 - step, step) != 0) {
				this.placeBlock(writer, Block.stairCompactCobblestone.blockID, stairMeta, 1, 6 - step, 1 + step);
			}
			if(this.getBlockID(writer, 2, 6 - step, step) != 0) {
				this.placeBlock(writer, Block.stairCompactCobblestone.blockID, stairMeta, 2, 6 - step, 1 + step);
			}
			if(this.getBlockID(writer, 3, 6 - step, step) != 0) {
				this.placeBlock(writer, Block.stairCompactCobblestone.blockID, stairMeta, 3, 6 - step, 1 + step);
			}
			if(step < 5) {
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 5 - step, 1 + step);
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 2, 5 - step, 1 + step);
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3, 5 - step, 1 + step);
			}
		}
		return true;
	}
}
