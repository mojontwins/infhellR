package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * The fallback connector: a plain brick tube used when no weighted piece can be placed.
 * It is shortened to fit against the piece it runs into.
 */
public class StrongholdComponentCorridor extends StrongholdComponent {
	private final int corridorLength;

	public StrongholdComponentCorridor(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.boundingBox = box;
		this.corridorLength = coordBaseMode != 2 && coordBaseMode != 0 ? box.getXSize() : box.getZSize();
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
	}

	/**
	 * Finds the corridor footprint: a 5x5x4 tube that is shortened until it clears the
	 * piece it collides with (mirrors the vanilla {@code func_35051_a}).
	 */
	public static StructureBoundingBox findCorridorPlacement(List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 4, coordBaseMode);
		MineshaftComponent colliding = MineshaftComponent.findIntersecting(components, box);
		if(colliding == null) {
			return null;
		}
		if(colliding.getBoundingBox().minY == box.minY) {
			for(int length = 3; length >= 1; --length) {
				box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, length - 1, coordBaseMode);
				if(!colliding.getBoundingBox().intersectsWith(box)) {
					return StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, length, coordBaseMode);
				}
			}
		}
		return null;
	}

	@Override
	protected boolean addComponentParts(MineshaftWriter writer, Random rand, StructureBoundingBox chunkBox) {
		if(this.isLiquidInChunk(writer, chunkBox)) {
			return false;
		}
		if(this.getAirRatioBelow(writer, chunkBox) > 0.2F) {
			return false;
		}

		for(int i = 0; i < this.corridorLength; ++i) {
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 0, 0, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 0, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 2, 0, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3, 0, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 4, 0, i);

			for(int y = 1; y <= 3; ++y) {
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 0, y, i);
				this.placeBlock(writer, 0, 0, 1, y, i);
				this.placeBlock(writer, 0, 0, 2, y, i);
				this.placeBlock(writer, 0, 0, 3, y, i);
				this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 4, y, i);
			}

			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 0, 4, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 1, 4, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 2, 4, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 3, 4, i);
			this.placeBlock(writer, StrongholdPieces.BRICKS_ID, StrongholdPieces.BRICKS_META, 4, 4, i);
		}
		return true;
	}
}
