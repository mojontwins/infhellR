package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/**
 * A vertical stairwell. Two flavours share this class: the "main" stairs (the stronghold
 * root, anchored to the surface) and ordinary stair pieces attached further in.
 */
public class StrongholdStairsPiece extends StrongholdPiece {
	private final boolean mainStairs;
	private final StrongholdDoor doorType;

	/** Root constructor: anchors to the surface height at the stronghold origin. */
	public StrongholdStairsPiece(World world, Random rand, int absX, int absZ) {
		super(0);
		int base = world.getLandSurfaceHeightValue(absX, absZ);
		this.mainStairs = true;
		this.coordBaseMode = rand.nextInt(4);
		this.doorType = StrongholdDoor.OPENING;
		this.box = new FeatureAABB(absX, base, absZ, absX + 5 - 1, base + 10, absZ + 5 - 1);
	}

	public StrongholdStairsPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.mainStairs = false;
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
	}

	public static StrongholdStairsPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -1, -7, 0, 5, 11, 5, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null
				? new StrongholdStairsPiece(depth, rand, box, orientation) : null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		if(this.mainStairs) {
			this.forceNextRoom(StrongholdCrossingPiece.class);
		}
		this.getNextNormal(pieces, rand, 1, 1);
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(this.getAirRatioBelow(world, clip) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 4, 10, 4, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 1, 7, 0);
		this.placeDoor(world, rand, clip, StrongholdDoor.OPENING, 1, 1, 4);
		if(!this.mainStairs) {
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 2, 6, 1, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 5, 1, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 1, 6, 1, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 5, 2, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 4, 3, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 1, 5, 3, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 2, 4, 3, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3, 3, 3, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 3, 4, 3, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3, 3, 2, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3, 2, 1, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 3, 3, 1, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 2, 2, 1, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 1, 1, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 1, 2, 1, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 1, 2, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 1, 1, 3, clip);
		}
		return true;
	}
}
