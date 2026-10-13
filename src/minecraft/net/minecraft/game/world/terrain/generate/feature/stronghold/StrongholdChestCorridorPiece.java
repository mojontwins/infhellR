package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A corridor with a single treasure chest set into an alcove. */
public class StrongholdChestCorridorPiece extends StrongholdPiece {
	private final StrongholdDoor doorType;
	private boolean hasMadeChest;

	public StrongholdChestCorridorPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
	}

	public static StrongholdChestCorridorPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null
				? new StrongholdChestCorridorPiece(depth, rand, box, orientation) : null;
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

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 4, 4, 6, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 1, 1, 0);
		this.placeDoor(world, rand, clip, StrongholdDoor.OPENING, 1, 1, 6);
		this.fillWithBlocks(world, clip, 3, 1, 2, 3, 1, 4, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_BLOCK, false);
		this.placeBlock(world, Block.stairSingle.blockID, 5, 3, 1, 1, clip);
		this.placeBlock(world, Block.stairSingle.blockID, 5, 3, 1, 5, clip);
		this.placeBlock(world, Block.stairSingle.blockID, 5, 3, 2, 2, clip);
		this.placeBlock(world, Block.stairSingle.blockID, 5, 3, 2, 4, clip);

		for(int i = 2; i <= 4; ++i) {
			this.placeBlock(world, Block.stairSingle.blockID, 5, 2, 1, i, clip);
		}

		if(!this.hasMadeChest) {
			int chestY = this.getYWithOffset(2);
			int chestX = this.getXWithOffset(3, 3);
			int chestZ = this.getZWithOffset(3, 3);
			if(clip.isVecInside(chestX, chestY, chestZ)) {
				this.hasMadeChest = true;
				this.placeLootChest(world, clip, rand, 3, 2, 3, StrongholdLoot.CHEST_CORRIDOR, 2 + rand.nextInt(2));
			}
		}

		return true;
	}
}
