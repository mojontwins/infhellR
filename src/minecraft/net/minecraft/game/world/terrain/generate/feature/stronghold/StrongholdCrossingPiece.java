package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** The large four-way junction room near the top of a stronghold. */
public class StrongholdCrossingPiece extends StrongholdPiece {
	protected final StrongholdDoor doorType;
	private final boolean openWestNear;
	private final boolean openWestFar;
	private final boolean openEastNear;
	private final boolean openEastFar;

	public StrongholdCrossingPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
		this.openWestNear = rand.nextBoolean();
		this.openWestFar = rand.nextBoolean();
		this.openEastNear = rand.nextBoolean();
		this.openEastFar = rand.nextInt(3) > 0;
	}

	public static StrongholdCrossingPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -4, -3, 0, 10, 9, 11, orientation);
		StrongholdCrossingPiece piece = new StrongholdCrossingPiece(depth, rand, box, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null ? piece : null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		int nearOffset = 3;
		int farOffset = 5;
		if(this.coordBaseMode == 1 || this.coordBaseMode == 2) {
			nearOffset = 8 - nearOffset;
			farOffset = 8 - farOffset;
		}

		this.getNextNormal(pieces, rand, 5, 1);
		if(this.openWestNear) {
			this.getNextSideX(pieces, rand, nearOffset, 1);
		}
		if(this.openWestFar) {
			this.getNextSideX(pieces, rand, farOffset, 7);
		}
		if(this.openEastNear) {
			this.getNextSideZ(pieces, rand, nearOffset, 1);
		}
		if(this.openEastFar) {
			this.getNextSideZ(pieces, rand, farOffset, 7);
		}
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(this.getAirRatioBelow(world, clip) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 9, 8, 10, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 4, 3, 0);
		if(this.openWestNear) {
			this.fillWithBlocks(world, clip, 0, 3, 1, 0, 5, 3, 0, 0, false);
		}
		if(this.openEastNear) {
			this.fillWithBlocks(world, clip, 9, 3, 1, 9, 5, 3, 0, 0, false);
		}
		if(this.openWestFar) {
			this.fillWithBlocks(world, clip, 0, 5, 7, 0, 7, 9, 0, 0, false);
		}
		if(this.openEastFar) {
			this.fillWithBlocks(world, clip, 9, 5, 7, 9, 7, 9, 0, 0, false);
		}

		this.fillWithBlocks(world, clip, 5, 1, 10, 7, 3, 10, 0, 0, false);
		this.fillWithRandomizedBlocks(world, clip, 1, 2, 1, 8, 2, 6, false, rand, this.stoneSelector);
		this.fillWithRandomizedBlocks(world, clip, 4, 1, 5, 4, 4, 9, false, rand, this.stoneSelector);
		this.fillWithRandomizedBlocks(world, clip, 8, 1, 5, 8, 4, 9, false, rand, this.stoneSelector);
		this.fillWithRandomizedBlocks(world, clip, 1, 4, 7, 3, 4, 9, false, rand, this.stoneSelector);
		this.fillWithRandomizedBlocks(world, clip, 1, 3, 5, 3, 3, 6, false, rand, this.stoneSelector);
		this.fillWithBlocks(world, clip, 1, 3, 4, 3, 3, 4, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fillWithBlocks(world, clip, 1, 4, 6, 3, 4, 6, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fillWithRandomizedBlocks(world, clip, 5, 1, 7, 7, 1, 8, false, rand, this.stoneSelector);
		this.fillWithBlocks(world, clip, 5, 1, 9, 7, 1, 9, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fillWithBlocks(world, clip, 5, 2, 7, 7, 2, 7, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fillWithBlocks(world, clip, 4, 5, 7, 4, 5, 9, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fillWithBlocks(world, clip, 8, 5, 7, 8, 5, 9, Block.stairSingle.blockID, Block.stairSingle.blockID, false);
		this.fillWithBlocks(world, clip, 5, 5, 7, 7, 5, 9, Block.stairDouble.blockID, Block.stairDouble.blockID, false);
		this.placeBlock(world, Block.torchWood.blockID, 0, 6, 5, 6, clip);
		return true;
	}
}
