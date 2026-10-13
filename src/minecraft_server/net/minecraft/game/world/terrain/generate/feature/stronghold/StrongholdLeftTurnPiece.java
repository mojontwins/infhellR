package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** An L-corner that bends the stronghold to one side. */
public class StrongholdLeftTurnPiece extends StrongholdPiece {
	protected final StrongholdDoor doorType;

	public StrongholdLeftTurnPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
	}

	public static StrongholdLeftTurnPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -1, -1, 0, 5, 5, 5, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null
				? new StrongholdLeftTurnPiece(depth, rand, box, orientation) : null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		if(this.coordBaseMode != 2 && this.coordBaseMode != 3) {
			this.getNextSideZ(pieces, rand, 1, 1);
		} else {
			this.getNextSideX(pieces, rand, 1, 1);
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

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 4, 4, 4, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 1, 1, 0);
		if(this.coordBaseMode != 2 && this.coordBaseMode != 3) {
			this.fillWithBlocks(world, clip, 4, 1, 1, 4, 3, 3, 0, 0, false);
		} else {
			this.fillWithBlocks(world, clip, 0, 1, 1, 0, 3, 3, 0, 0, false);
		}
		return true;
	}
}
