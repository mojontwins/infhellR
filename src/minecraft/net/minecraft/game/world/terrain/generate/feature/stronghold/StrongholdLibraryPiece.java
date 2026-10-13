package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** The two-storey library: a small variant and a tall one chosen by available height. */
public class StrongholdLibraryPiece extends StrongholdPiece {
	protected final StrongholdDoor doorType;
	private final boolean isLargeRoom;

	public StrongholdLibraryPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
		this.isLargeRoom = box.getYSize() > 6;
	}

	public static StrongholdLibraryPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -4, -1, 0, 14, 11, 15, orientation);
		if(!canGoDeeper(box) || StructurePiece.findIntersecting(pieces, box) != null) {
			box = StructureGeometry.boxFrom(x, y, z, -4, -1, 0, 14, 6, 15, orientation);
			if(!canGoDeeper(box) || StructurePiece.findIntersecting(pieces, box) != null) {
				return null;
			}
		}

		return new StrongholdLibraryPiece(depth, rand, box, orientation);
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(this.getAirRatioInWalls(world, clip) > 0.2F) {
			return false;
		}

		int height = this.isLargeRoom ? 11 : 6;
		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 13, height - 1, 14, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 4, 1, 0);
		this.randomlyFillWithBlocks(world, clip, rand, 0.07F, 2, 1, 1, 11, 4, 13, Block.web.blockID, Block.web.blockID, false);

		for(int z = 1; z <= 13; ++z) {
			if((z - 1) % 4 == 0) {
				this.fillWithBlocks(world, clip, 1, 1, z, 1, 4, z, Block.planks.blockID, Block.planks.blockID, false);
				this.fillWithBlocks(world, clip, 12, 1, z, 12, 4, z, Block.planks.blockID, Block.planks.blockID, false);
				this.placeBlock(world, Block.torchWood.blockID, 0, 2, 3, z, clip);
				this.placeBlock(world, Block.torchWood.blockID, 0, 11, 3, z, clip);
				if(this.isLargeRoom) {
					this.fillWithBlocks(world, clip, 1, 6, z, 1, 9, z, Block.planks.blockID, Block.planks.blockID, false);
					this.fillWithBlocks(world, clip, 12, 6, z, 12, 9, z, Block.planks.blockID, Block.planks.blockID, false);
				}
			} else {
				this.fillWithBlocks(world, clip, 1, 1, z, 1, 4, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
				this.fillWithBlocks(world, clip, 12, 1, z, 12, 4, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
				if(this.isLargeRoom) {
					this.fillWithBlocks(world, clip, 1, 6, z, 1, 9, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
					this.fillWithBlocks(world, clip, 12, 6, z, 12, 9, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
				}
			}
		}

		for(int z = 3; z < 12; z += 2) {
			this.fillWithBlocks(world, clip, 3, 1, z, 4, 3, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
			this.fillWithBlocks(world, clip, 6, 1, z, 7, 3, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
			this.fillWithBlocks(world, clip, 9, 1, z, 10, 3, z, Block.bookShelf.blockID, Block.bookShelf.blockID, false);
		}

		if(this.isLargeRoom) {
			this.fillWithBlocks(world, clip, 1, 5, 1, 3, 5, 13, Block.planks.blockID, Block.planks.blockID, false);
			this.fillWithBlocks(world, clip, 10, 5, 1, 12, 5, 13, Block.planks.blockID, Block.planks.blockID, false);
			this.fillWithBlocks(world, clip, 4, 5, 1, 9, 5, 2, Block.planks.blockID, Block.planks.blockID, false);
			this.fillWithBlocks(world, clip, 4, 5, 12, 9, 5, 13, Block.planks.blockID, Block.planks.blockID, false);
			this.placeBlock(world, Block.planks.blockID, 0, 9, 5, 11, clip);
			this.placeBlock(world, Block.planks.blockID, 0, 8, 5, 11, clip);
			this.placeBlock(world, Block.planks.blockID, 0, 9, 5, 10, clip);
			this.fillWithBlocks(world, clip, 3, 6, 2, 3, 6, 12, Block.fence.blockID, Block.fence.blockID, false);
			this.fillWithBlocks(world, clip, 10, 6, 2, 10, 6, 10, Block.fence.blockID, Block.fence.blockID, false);
			this.fillWithBlocks(world, clip, 4, 6, 2, 9, 6, 2, Block.fence.blockID, Block.fence.blockID, false);
			this.fillWithBlocks(world, clip, 4, 6, 12, 8, 6, 12, Block.fence.blockID, Block.fence.blockID, false);
			this.placeBlock(world, Block.fence.blockID, 0, 9, 6, 11, clip);
			this.placeBlock(world, Block.fence.blockID, 0, 8, 6, 11, clip);
			this.placeBlock(world, Block.fence.blockID, 0, 9, 6, 10, clip);
			int ladderMeta = this.getMetadataWithOffset(Block.ladder.blockID, 3);
			this.placeBlock(world, Block.ladder.blockID, ladderMeta, 10, 1, 13, clip);
			this.placeBlock(world, Block.ladder.blockID, ladderMeta, 10, 2, 13, clip);
			this.placeBlock(world, Block.ladder.blockID, ladderMeta, 10, 3, 13, clip);
			this.placeBlock(world, Block.ladder.blockID, ladderMeta, 10, 4, 13, clip);
			this.placeBlock(world, Block.ladder.blockID, ladderMeta, 10, 5, 13, clip);
			this.placeBlock(world, Block.ladder.blockID, ladderMeta, 10, 6, 13, clip);
			this.placeBlock(world, Block.ladder.blockID, ladderMeta, 10, 7, 13, clip);

			int railX = 7;
			int railZ = 7;
			this.placeBlock(world, Block.fence.blockID, 0, railX - 1, 9, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX, 9, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX - 1, 8, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX, 8, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX - 1, 7, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX, 7, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX - 2, 7, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX + 1, 7, railZ, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX - 1, 7, railZ - 1, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX - 1, 7, railZ + 1, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX, 7, railZ - 1, clip);
			this.placeBlock(world, Block.fence.blockID, 0, railX, 7, railZ + 1, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, railX - 2, 8, railZ, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, railX + 1, 8, railZ, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, railX - 1, 8, railZ - 1, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, railX - 1, 8, railZ + 1, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, railX, 8, railZ - 1, clip);
			this.placeBlock(world, Block.torchWood.blockID, 0, railX, 8, railZ + 1, clip);
		}

		this.placeLootChest(world, clip, rand, 3, 3, 5, StrongholdLoot.LIBRARY, 1 + rand.nextInt(4));
		if(this.isLargeRoom) {
			this.placeBlock(world, 0, 0, 12, 9, 1, clip);
			this.placeLootChest(world, clip, rand, 12, 8, 1, StrongholdLoot.LIBRARY, 1 + rand.nextInt(4));
		}

		return true;
	}
}
