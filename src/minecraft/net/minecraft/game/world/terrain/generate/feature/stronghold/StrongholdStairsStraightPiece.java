package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A long staircase descending seven blocks over seven tiles. */
public class StrongholdStairsStraightPiece extends StrongholdPiece {
	private final StrongholdDoor doorType;

	public StrongholdStairsStraightPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
	}

	public static StrongholdStairsStraightPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -1, -7, 0, 5, 11, 8, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null
				? new StrongholdStairsStraightPiece(depth, rand, box, orientation) : null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
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

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 4, 10, 7, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 1, 7, 0);
		this.placeDoor(world, rand, clip, StrongholdDoor.OPENING, 1, 1, 7);
		int stairMeta = this.getMetadataWithOffset(Block.stairCompactCobblestone.blockID, 2);

		for(int step = 0; step < 6; ++step) {
			if(this.getBlockId(world, 1, 6 - step, step, clip) != 0) {
				this.placeBlock(world, Block.stairCompactCobblestone.blockID, stairMeta, 1, 6 - step, 1 + step, clip);
			}
			if(this.getBlockId(world, 2, 6 - step, step, clip) != 0) {
				this.placeBlock(world, Block.stairCompactCobblestone.blockID, stairMeta, 2, 6 - step, 1 + step, clip);
			}
			if(this.getBlockId(world, 3, 6 - step, step, clip) != 0) {
				this.placeBlock(world, Block.stairCompactCobblestone.blockID, stairMeta, 3, 6 - step, 1 + step, clip);
			}
			if(step < 5) {
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 5 - step, 1 + step, clip);
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 2, 5 - step, 1 + step, clip);
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3, 5 - step, 1 + step, clip);
			}
		}
		return true;
	}
}
