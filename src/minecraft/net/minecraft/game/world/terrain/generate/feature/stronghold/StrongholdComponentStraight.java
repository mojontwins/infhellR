package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftWriter;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/** A straight corridor, optionally opening onto a side branch on the X and/or Z axis. */
public class StrongholdComponentStraight extends StrongholdComponent {
	private final StrongholdDoor doorType;
	private final boolean expandsX;
	private final boolean expandsZ;

	public StrongholdComponentStraight(World world, int componentType, Random rand, StructureBoundingBox box, int coordBaseMode) {
		super(world, componentType);
		this.coordBaseMode = coordBaseMode;
		this.doorType = this.getRandomDoor(rand);
		this.boundingBox = box;
		this.expandsX = rand.nextInt(2) == 0;
		this.expandsZ = rand.nextInt(2) == 0;
	}

	public static StrongholdComponentStraight findValidPlacement(World world, List<MineshaftComponent> components, Random rand, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, coordBaseMode);
		return canStrongholdGoDeeper(box) && MineshaftComponent.findIntersecting(components, box) == null
				? new StrongholdComponentStraight(world, componentType, rand, box, coordBaseMode) : null;
	}

	@Override
	public void buildComponent(MineshaftComponent startRoom, List<MineshaftComponent> components, Random rand) {
		StrongholdComponentStairs2 start = (StrongholdComponentStairs2) startRoom;
		this.getNextComponentNormal(start, components, rand, 1, 1);
		if(this.expandsX) {
			this.getNextComponentX(start, components, rand, 1, 2);
		}
		if(this.expandsZ) {
			this.getNextComponentZ(start, components, rand, 1, 2);
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

		this.fillWithRandomizedBlocks(writer, chunkBox, 0, 0, 0, 4, 4, 6, true, rand, StrongholdPieces.getStrongholdStones());
		this.placeDoor(writer, rand, this.doorType, 1, 1, 0);
		this.placeDoor(writer, rand, StrongholdDoor.OPENING, 1, 1, 6);
		this.randomBlock(writer, rand, 0.1F, 1, 2, 1, Block.torchWood.blockID, 0);
		this.randomBlock(writer, rand, 0.1F, 3, 2, 1, Block.torchWood.blockID, 0);
		this.randomBlock(writer, rand, 0.1F, 1, 2, 5, Block.torchWood.blockID, 0);
		this.randomBlock(writer, rand, 0.1F, 3, 2, 5, Block.torchWood.blockID, 0);
		if(this.expandsX) {
			this.fill(writer, 0, 1, 2, 0, 3, 4, 0, 0, false);
		}
		if(this.expandsZ) {
			this.fill(writer, 4, 1, 2, 4, 3, 4, 0, 0, false);
		}
		return true;
	}
}
