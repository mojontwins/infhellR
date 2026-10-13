package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/**
 * A bare brick corridor used as the fallback when none of the weighted rooms could be
 * placed. It is squeezed against an existing piece so it always connects.
 */
public class StrongholdCorridorPiece extends StrongholdPiece {
	private final int length;

	public StrongholdCorridorPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.box = box;
		this.length = orientation != 2 && orientation != 0 ? box.getXSize() : box.getZSize();
	}

	public static FeatureAABB findPlacement(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -1, -1, 0, 5, 5, 4, orientation);
		StructurePiece other = StructurePiece.findIntersecting(pieces, box);
		if(other == null) {
			return null;
		}

		if(other.getBox().y1 == box.y1) {
			for(int size = 3; size >= 1; --size) {
				box = StructureGeometry.boxFrom(x, y, z, -1, -1, 0, 5, 5, size - 1, orientation);
				if(!other.getBox().collidesWith(box)) {
					return StructureGeometry.boxFrom(x, y, z, -1, -1, 0, 5, 5, size, orientation);
				}
			}
		}

		return null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(this.getAirRatioBelow(world, clip) > 0.2F) {
			return false;
		}

		for(int i = 0; i < this.length; ++i) {
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 0, 0, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 0, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 2, 0, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3, 0, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 4, 0, i, clip);

			for(int y = 1; y <= 3; ++y) {
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 0, y, i, clip);
				this.placeBlock(world, 0, 0, 1, y, i, clip);
				this.placeBlock(world, 0, 0, 2, y, i, clip);
				this.placeBlock(world, 0, 0, 3, y, i, clip);
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 4, y, i, clip);
			}

			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 0, 4, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 1, 4, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 2, 4, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3, 4, i, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 4, 4, i, clip);
		}

		return true;
	}
}
