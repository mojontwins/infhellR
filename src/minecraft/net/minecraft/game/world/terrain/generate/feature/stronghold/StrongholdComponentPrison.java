package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/** A prison block: iron-barred cells along one wall. */
public class StrongholdComponentPrison extends StrongholdComponent {
	/** Cell floor columns clear of the central divider (x = 4). */
	private static final int[] CELL_FLOOR_X = new int[] {1, 2, 3, 5, 6, 7};

	protected final StrongholdDoor doorType;

	public StrongholdComponentPrison(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
	}

	public static StrongholdComponentPrison findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 9, 5, 11, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null
				? new StrongholdComponentPrison(world, componentType, rand, box, coordBaseMode) : null;
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
		if(this.getAirRatioInWalls(writer, chunkBox) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 8, 4, 10, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 1, 1, 0);
		this.fill(writer, 1, 1, 10, 3, 3, 10, 0, 0, false);
		this.fillWithRandomizedBlocks(writer, chunkBox, 4, 1, 1, 4, 3, 1, false, rand, StrongholdPieces.getStrongholdStones());
		this.fillWithRandomizedBlocks(writer, chunkBox, 4, 1, 3, 4, 3, 3, false, rand, StrongholdPieces.getStrongholdStones());
		this.fillWithRandomizedBlocks(writer, chunkBox, 4, 1, 7, 4, 3, 7, false, rand, StrongholdPieces.getStrongholdStones());
		this.fillWithRandomizedBlocks(writer, chunkBox, 4, 1, 9, 4, 3, 9, false, rand, StrongholdPieces.getStrongholdStones());
		this.fill(writer, 4, 1, 4, 4, 3, 6, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_ID, false);
		this.fill(writer, 5, 1, 5, 7, 3, 5, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_ID, false);
		this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, 4, 3, 2);
		this.placeBlock(writer, StrongholdPieces.IRON_FENCE_ID, StrongholdPieces.IRON_FENCE_META, 4, 3, 8);
		this.placeBlock(writer, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3), 4, 1, 2);
		this.placeBlock(writer, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3) + 8, 4, 2, 2);
		this.placeBlock(writer, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3), 4, 1, 8);
		this.placeBlock(writer, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3) + 8, 4, 2, 8);

		Random spawnerRand = this.specialRandom();
		int spawnerX = CELL_FLOOR_X[spawnerRand.nextInt(CELL_FLOOR_X.length)];
		int spawnerZ = 1 + spawnerRand.nextInt(9);
		this.placeRandomMobSpawner(writer, spawnerRand, spawnerX, 1, spawnerZ);
		return true;
	}
}
