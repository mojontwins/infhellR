package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/** The mirror of {@link StrongholdComponentLeftTurn}. */
public class StrongholdComponentRightTurn extends StrongholdComponentLeftTurn {
	public StrongholdComponentRightTurn(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType, rand, box, coordBaseMode);
	}

	public static StrongholdComponentRightTurn findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 5, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null
				? new StrongholdComponentRightTurn(world, componentType, rand, box, coordBaseMode) : null;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		StrongholdComponentStairs2 start = (StrongholdComponentStairs2) startRoom;
		if(this.coordBaseMode != 2 && this.coordBaseMode != 3) {
			this.getNextComponentX(start, components, rand, 1, 1);
		} else {
			this.getNextComponentZ(start, components, rand, 1, 1);
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

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 4, 4, 4, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 1, 1, 0);
		if(this.coordBaseMode != 2 && this.coordBaseMode != 3) {
			this.fill(writer, 0, 1, 1, 0, 3, 3, 0, 0, false);
		} else {
			this.fill(writer, 4, 1, 1, 4, 3, 3, 0, 0, false);
		}
		return true;
	}
}
