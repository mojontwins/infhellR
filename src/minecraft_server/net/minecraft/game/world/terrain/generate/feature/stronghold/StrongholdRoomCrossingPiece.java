package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A wide room with several decorative layouts chosen at random. */
public class StrongholdRoomCrossingPiece extends StrongholdPiece {
	protected final StrongholdDoor doorType;
	protected final int roomType;

	public StrongholdRoomCrossingPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
		this.roomType = rand.nextInt(5);
	}

	public static StrongholdRoomCrossingPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -4, -1, 0, 11, 7, 11, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null
				? new StrongholdRoomCrossingPiece(depth, rand, box, orientation) : null;
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		this.getNextNormal(pieces, rand, 4, 1);
		this.getNextSideX(pieces, rand, 1, 4);
		this.getNextSideZ(pieces, rand, 1, 4);
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(this.getAirRatioInWalls(world, clip) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 10, 6, 10, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 4, 1, 0);
		this.fillWithBlocks(world, clip, 4, 1, 10, 6, 3, 10, 0, 0, false);
		this.fillWithBlocks(world, clip, 0, 1, 4, 0, 3, 6, 0, 0, false);
		this.fillWithBlocks(world, clip, 10, 1, 4, 10, 3, 6, 0, 0, false);

		switch(this.roomType) {
		case 0:
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 5, 1, 5, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 5, 2, 5, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 5, 3, 5, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, 4, 3, 5, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, 6, 3, 5, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, 5, 3, 4, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, 5, 3, 6, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 4, 1, 4, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 4, 1, 5, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 4, 1, 6, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 6, 1, 4, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 6, 1, 5, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 6, 1, 6, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 5, 1, 4, clip);
			this.placeBlock(world, Block.stairSingle.blockID, 0, 5, 1, 6, clip);
			break;
		case 1:
			for(int i = 0; i < 5; ++i) {
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3, 1, 3 + i, clip);
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 7, 1, 3 + i, clip);
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3 + i, 1, 3, clip);
				this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 3 + i, 1, 7, clip);
			}
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 5, 1, 5, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 5, 2, 5, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, 5, 3, 5, clip);
			this.placeBlock(world, Block.waterMoving.blockID, 0, 5, 4, 5, clip);
			break;
		case 2:
			for(int i = 1; i <= 9; ++i) {
				this.placeBlock(world, Block.cobblestone.blockID, 0, 1, 3, i, clip);
				this.placeBlock(world, Block.cobblestone.blockID, 0, 9, 3, i, clip);
			}
			for(int i = 1; i <= 9; ++i) {
				this.placeBlock(world, Block.cobblestone.blockID, 0, i, 3, 1, clip);
				this.placeBlock(world, Block.cobblestone.blockID, 0, i, 3, 9, clip);
			}
			this.placeBlock(world, Block.cobblestone.blockID, 0, 5, 1, 4, clip);
			this.placeBlock(world, Block.cobblestone.blockID, 0, 5, 1, 6, clip);
			this.placeBlock(world, Block.cobblestone.blockID, 0, 5, 3, 4, clip);
			this.placeBlock(world, Block.cobblestone.blockID, 0, 5, 3, 6, clip);
			this.placeBlock(world, Block.cobblestone.blockID, 0, 4, 1, 5, clip);
			this.placeBlock(world, Block.cobblestone.blockID, 0, 6, 1, 5, clip);
			this.placeBlock(world, Block.cobblestone.blockID, 0, 4, 3, 5, clip);
			this.placeBlock(world, Block.cobblestone.blockID, 0, 6, 3, 5, clip);

			for(int i = 1; i <= 3; ++i) {
				this.placeBlock(world, Block.cobblestone.blockID, 0, 4, i, 4, clip);
				this.placeBlock(world, Block.cobblestone.blockID, 0, 6, i, 4, clip);
				this.placeBlock(world, Block.cobblestone.blockID, 0, 4, i, 6, clip);
				this.placeBlock(world, Block.cobblestone.blockID, 0, 6, i, 6, clip);
			}

			this.placeBlock(world, Block.torchWood.blockID, 0, 5, 3, 5, clip);

			for(int i = 2; i <= 8; ++i) {
				this.placeBlock(world, Block.planks.blockID, 0, 2, 3, i, clip);
				this.placeBlock(world, Block.planks.blockID, 0, 3, 3, i, clip);
				if(i <= 3 || i >= 7) {
					this.placeBlock(world, Block.planks.blockID, 0, 4, 3, i, clip);
					this.placeBlock(world, Block.planks.blockID, 0, 5, 3, i, clip);
					this.placeBlock(world, Block.planks.blockID, 0, 6, 3, i, clip);
				}
				this.placeBlock(world, Block.planks.blockID, 0, 7, 3, i, clip);
				this.placeBlock(world, Block.planks.blockID, 0, 8, 3, i, clip);
			}

			this.placeBlock(world, Block.ladder.blockID, this.getMetadataWithOffset(Block.ladder.blockID, 4), 9, 1, 3, clip);
			this.placeBlock(world, Block.ladder.blockID, this.getMetadataWithOffset(Block.ladder.blockID, 4), 9, 2, 3, clip);
			this.placeBlock(world, Block.ladder.blockID, this.getMetadataWithOffset(Block.ladder.blockID, 4), 9, 3, 3, clip);
			this.placeLootChest(world, clip, rand, 3, 4, 8, StrongholdLoot.ROOM_CROSSING, 1 + rand.nextInt(4));
		}

		return true;
	}
}
