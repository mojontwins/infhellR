package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/**
 * The descending stairwell. The {@code mainStairs} flavour is the stronghold's entrance
 * (it fixes the whole structure's surface height and forces a crossing as its first
 * branch); the other flavour is a normal stairwell placed from a bounding box.
 */
public class StrongholdComponentStairs extends StrongholdComponent {
	private final boolean mainStairs;
	private final StrongholdDoor doorType;

	/** Entrance stairwell: height comes from the local land surface. */
	public StrongholdComponentStairs(World world, int componentType, Random rand, int x, int z) {
		super(world, componentType);
		int base = world.getLandSurfaceHeightValue(x, z);
		this.mainStairs = true;
		this.coordBaseMode = rand.nextInt(4);
		this.doorType = StrongholdDoor.OPENING;
		this.boundingBox = new StructureBoundingBox(x, base, z, x + 5 - 1, base + 10, z + 5 - 1);
	}

	/** Ordinary stairwell placed from a precomputed box. */
	public StrongholdComponentStairs(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.mainStairs = false;
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
	}

	public static StrongholdComponentStairs findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -7, 0, 5, 11, 5, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null
				? new StrongholdComponentStairs(world, componentType, rand, box, coordBaseMode) : null;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		if(this.mainStairs) {
			this.factory.setComponentType(StrongholdComponentCrossing.class);
		}
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

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 4, 10, 4, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 1, 7, 0);
		this.placeDoor(writer, rand, StrongholdDoor.OPENING, 1, 1, 4);
		if(!this.mainStairs) {
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 2, 6, 1);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 5, 1);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 1, 6, 1);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 5, 2);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 4, 3);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 1, 5, 3);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 2, 4, 3);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3, 3, 3);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 3, 4, 3);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3, 3, 2);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3, 2, 1);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 3, 3, 1);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 2, 2, 1);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 1, 1);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 1, 2, 1);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 1, 2);
			this.placeBlock(writer, Block.stairSingle.blockID, 0, 1, 1, 3);
		}
		return true;
	}
}
