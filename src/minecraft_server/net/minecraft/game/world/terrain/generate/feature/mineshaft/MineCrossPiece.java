package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A crossing where several corridors meet; may span two levels. */
public class MineCrossPiece extends StructurePiece {
	private final int corridorDirection;
	private final boolean isMultipleFloors;

	public MineCrossPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.corridorDirection = orientation;
		this.box = box;
		this.isMultipleFloors = box.getYSize() > 3;
	}

	public static FeatureAABB findValidPlacement(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation) {
		FeatureAABB box = new FeatureAABB(x, y, z, x, y + 3, z);
		if(rand.nextInt(4) == 0) {
			box.y2 += 4;
		}

		switch(orientation) {
		case 0:
			box.x1 = x - 1;
			box.x2 = x + 3;
			box.z2 = z + 4;
			break;
		case 1:
			box.x1 = x - 4;
			box.z1 = z - 1;
			box.z2 = z + 3;
			break;
		case 2:
			box.x1 = x - 1;
			box.x2 = x + 3;
			box.z1 = z - 4;
			break;
		case 3:
			box.x2 = x + 4;
			box.z1 = z - 1;
			box.z2 = z + 3;
		}

		return StructurePiece.findIntersecting(pieces, box) != null ? null : box;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		switch(this.corridorDirection) {
		case 0:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1, this.box.z2 + 1, 0, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1, this.box.z1 + 1, 1, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1, this.box.z1 + 1, 3, this.getPieceType());
			break;
		case 1:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1, this.box.z1 - 1, 2, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1, this.box.z2 + 1, 0, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1, this.box.z1 + 1, 1, this.getPieceType());
			break;
		case 2:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1, this.box.z1 - 1, 2, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1, this.box.z1 + 1, 1, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1, this.box.z1 + 1, 3, this.getPieceType());
			break;
		case 3:
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1, this.box.z1 - 1, 2, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1, this.box.z2 + 1, 0, this.getPieceType());
			this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1, this.box.z1 + 1, 3, this.getPieceType());
		}

		if(this.isMultipleFloors) {
			if(rand.nextBoolean()) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1 + 4, this.box.z1 - 1, 2, this.getPieceType());
			}
			if(rand.nextBoolean()) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 + 4, this.box.z1 + 1, 1, this.getPieceType());
			}
			if(rand.nextBoolean()) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 + 4, this.box.z1 + 1, 3, this.getPieceType());
			}
			if(rand.nextBoolean()) {
				this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + 1, this.box.y1 + 4, this.box.z2 + 1, 0, this.getPieceType());
			}
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

		if(this.isMultipleFloors) {
			this.fillWithBlocks(world, clip, this.box.x1 + 1, this.box.y1, this.box.z1, this.box.x2 - 1, this.box.y1 + 2, this.box.z2, 0, 0, false);
			this.fillWithBlocks(world, clip, this.box.x1, this.box.y1, this.box.z1 + 1, this.box.x2, this.box.y1 + 2, this.box.z2 - 1, 0, 0, false);
			this.fillWithBlocks(world, clip, this.box.x1 + 1, this.box.y2 - 2, this.box.z1, this.box.x2 - 1, this.box.y2, this.box.z2, 0, 0, false);
			this.fillWithBlocks(world, clip, this.box.x1, this.box.y2 - 2, this.box.z1 + 1, this.box.x2, this.box.y2, this.box.z2 - 1, 0, 0, false);
			this.fillWithBlocks(world, clip, this.box.x1 + 1, this.box.y1 + 3, this.box.z1 + 1, this.box.x2 - 1, this.box.y1 + 3, this.box.z2 - 1, 0, 0, false);
		} else {
			this.fillWithBlocks(world, clip, this.box.x1 + 1, this.box.y1, this.box.z1, this.box.x2 - 1, this.box.y2, this.box.z2, 0, 0, false);
			this.fillWithBlocks(world, clip, this.box.x1, this.box.y1, this.box.z1 + 1, this.box.x2, this.box.y2, this.box.z2 - 1, 0, 0, false);
		}

		this.fillWithBlockVariation(world, clip, this.box.x1 + 1, this.box.y1, this.box.z1 + 1, this.box.x1 + 1, this.box.y2 - 1, this.box.z1 + 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);
		this.fillWithBlockVariation(world, clip, this.box.x1 + 1, this.box.y1, this.box.z2 - 1, this.box.x1 + 1, this.box.y2 - 1, this.box.z2 - 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);
		this.fillWithBlockVariation(world, clip, this.box.x2 - 1, this.box.y1, this.box.z1 + 1, this.box.x2 - 1, this.box.y2 - 1, this.box.z1 + 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);
		this.fillWithBlockVariation(world, clip, this.box.x2 - 1, this.box.y1, this.box.z2 - 1, this.box.x2 - 1, this.box.y2 - 1, this.box.z2 - 1, Block.wood.blockID, Block.chippedWood.blockID, rand, 4, false);

		for(int x = this.box.x1; x <= this.box.x2; ++x) {
			for(int z = this.box.z1; z <= this.box.z2; ++z) {
				this.placeBlock(world, Block.planks.blockID, 0, x, this.box.y2, z, clip);
				if(this.getBlockId(world, x, this.box.y1 - 1, z, clip) == 0) {
					this.placeBlock(world, Block.stairSingle.blockID, 8 | 2, x, this.box.y1 - 1, z, clip);
				}

				this.throwChainUp(world, this.box.x1, this.box.z1, clip);
				this.throwChainUp(world, this.box.x1, this.box.z2, clip);
				this.throwChainUp(world, this.box.x2, this.box.z1, clip);
				this.throwChainUp(world, this.box.x2, this.box.z2, clip);
			}
		}

		return true;
	}

	private void throwChainUp(World world, int x, int z, FeatureAABB clip) {
		if(this.getBlockIdNoVertClip(world, x, this.box.y2 + 1, z, clip) == 0 && !this.canBlockSeeTheSky(world, x, this.box.y2 + 1, z, clip)) {
			int y = this.box.y2 + 1;
			while(this.getBlockIdNoVertClip(world, x, y, z, clip) == 0) {
				this.placeBlockNoVertClip(world, Block.chain.blockID, 0, x, y, z, clip);
				++y;
			}
		}
	}
}
