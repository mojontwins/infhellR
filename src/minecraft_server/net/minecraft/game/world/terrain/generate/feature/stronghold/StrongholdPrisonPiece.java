package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureGeometry;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** A barred cell block with iron-fence partitions and steel doors. */
public class StrongholdPrisonPiece extends StrongholdPiece {
	private final StrongholdDoor doorType;

	public StrongholdPrisonPiece(int pieceType, Random rand, FeatureAABB box, int orientation) {
		super(pieceType);
		this.coordBaseMode = orientation;
		this.doorType = this.getRandomDoor(rand);
		this.box = box;
	}

	public static StrongholdPrisonPiece create(List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		FeatureAABB box = StructureGeometry.boxFrom(x, y, z, -1, -1, 0, 9, 5, 11, orientation);
		return canGoDeeper(box) && StructurePiece.findIntersecting(pieces, box) == null
				? new StrongholdPrisonPiece(depth, rand, box, orientation) : null;
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
		if(this.getAirRatioInWalls(world, clip) > 0.2F) {
			return false;
		}

		this.fillWithRandomizedBlocks(world, clip, 0, 0, 0, 8, 4, 10, true, rand, this.stoneSelector);
		this.placeDoor(world, rand, clip, this.doorType, 1, 1, 0);
		this.fillWithBlocks(world, clip, 1, 1, 10, 3, 3, 10, 0, 0, false);
		this.fillWithRandomizedBlocks(world, clip, 4, 1, 1, 4, 3, 1, false, rand, this.stoneSelector);
		this.fillWithRandomizedBlocks(world, clip, 4, 1, 3, 4, 3, 3, false, rand, this.stoneSelector);
		this.fillWithRandomizedBlocks(world, clip, 4, 1, 7, 4, 3, 7, false, rand, this.stoneSelector);
		this.fillWithRandomizedBlocks(world, clip, 4, 1, 9, 4, 3, 9, false, rand, this.stoneSelector);
		this.fillWithBlocks(world, clip, 4, 1, 4, 4, 3, 6, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_BLOCK, false);
		this.fillWithBlocks(world, clip, 5, 1, 5, 7, 3, 5, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_BLOCK, false);
		this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, 4, 3, 2, clip);
		this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, 4, 3, 8, clip);
		this.placeBlock(world, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3), 4, 1, 2, clip);
		this.placeBlock(world, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3) + 8, 4, 2, 2, clip);
		this.placeBlock(world, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3), 4, 1, 8, clip);
		this.placeBlock(world, Block.doorSteel.blockID, this.getMetadataWithOffset(Block.doorSteel.blockID, 3) + 8, 4, 2, 8, clip);
		return true;
	}
}
