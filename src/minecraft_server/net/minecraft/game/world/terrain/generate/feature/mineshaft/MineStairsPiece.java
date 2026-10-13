package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A descending staircase that links the mineshaft to a lower level. */
public class MineStairsPiece extends StructurePiece {
	public MineStairsPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.box = box;
	}

	public static FeatureAABB findValidPlacement(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation) {
		FeatureAABB box = new FeatureAABB(x, y - 5, z, x, y + 2, z);
		switch(orientation) {
		case 0:
			box.x2 = x + 2;
			box.z2 = z + 8;
			break;
		case 1:
			box.x1 = x - 8;
			box.z2 = z + 2;
			break;
		case 2:
			box.x2 = x + 2;
			box.z1 = z - 8;
			break;
		case 3:
			box.x2 = x + 8;
			box.z2 = z + 2;
		}

		return StructurePiece.findIntersecting(pieces, box) != null ? null : box;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		switch(this.coordBaseMode) {
		case 0:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1, this.box.y1, this.box.z2 + 1, 0, this.getPieceType());
			break;
		case 1:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1, this.box.z1, 1, this.getPieceType());
			break;
		case 2:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1, this.box.y1, this.box.z1 - 1, 2, this.getPieceType());
			break;
		case 3:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1, this.box.z1, 3, this.getPieceType());
		}
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(mostlySolid && !this.isMostlySolid(world, clip)) {
			return false;
		}

		this.fillWithBlocks(world, clip, 0, 5, 0, 2, 7, 1, 0, 0, false);
		this.fillWithBlocks(world, clip, 0, 0, 7, 2, 2, 8, 0, 0, false);

		for(int step = 0; step < 5; ++step) {
			this.fillWithBlocks(world, clip, 0, 5 - step - (step < 4 ? 1 : 0), 2 + step, 2, 7 - step, 2 + step, 0, 0, false);
		}

		return true;
	}
}
