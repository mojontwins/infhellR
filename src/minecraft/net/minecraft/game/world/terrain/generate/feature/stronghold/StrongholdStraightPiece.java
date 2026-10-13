package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A short corridor that may open a side passage on either side. */
public class StrongholdStraightPiece extends StrongholdPiece {
	private final StrongholdDoor doorType;
	private final boolean expandsX;
	private final boolean expandsZ;

	public StrongholdStraightPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
		this.expandsX = rand.nextInt(2) == 0;
		this.expandsZ = rand.nextInt(2) == 0;
	}

	public static StrongholdStraightPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null
				? new StrongholdStraightPiece(depth, rand, box, orientation) : null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		this.getNextNormal(pieces, rand, 1, 1);
		if(this.expandsX) {
			this.getNextSideX(pieces, rand, 1, 2);
		}
		if(this.expandsZ) {
			this.getNextSideZ(pieces, rand, 1, 2);
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

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 4, 4, 6, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 1, 1, 0);
		this.placeDoor(world, rand, clip, StrongholdDoor.OPENING, 1, 1, 6);
		this.randomlyPlaceBlock(world, clip, rand, 0.1F, 1, 2, 1, Block.torchWood.blockID, 0);
		this.randomlyPlaceBlock(world, clip, rand, 0.1F, 3, 2, 1, Block.torchWood.blockID, 0);
		this.randomlyPlaceBlock(world, clip, rand, 0.1F, 1, 2, 5, Block.torchWood.blockID, 0);
		this.randomlyPlaceBlock(world, clip, rand, 0.1F, 3, 2, 5, Block.torchWood.blockID, 0);
		if(this.expandsX) {
			this.fillWithBlocks(world, clip, 0, 1, 2, 0, 3, 4, 0, 0, false);
		}
		if(this.expandsZ) {
			this.fillWithBlocks(world, clip, 4, 1, 2, 4, 3, 4, 0, 0, false);
		}
		return true;
	}
}
