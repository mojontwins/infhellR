package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/**
 * Shared behaviour for every stronghold room: the doorway placement routine, the
 * door-roll, and the three neighbour-attachment helpers that map a local offset to the
 * absolute coordinates the structure provider expects.
 */
public abstract class StrongholdPiece extends StructurePiece {
	protected final StrongholdBlockSelector stoneSelector = new StrongholdBlockSelector();

	protected StrongholdPiece(int pieceType) {
		super(pieceType);
	}

	/** True while there is still rock under the piece, so it must not be carved open. */
	protected static boolean canGoDeeper(FeatureAABB box) {
		return box != null && box.y1 > 10;
	}

	/** Queues {@code className} as the required next room (used once, then cleared). */
	protected void forceNextRoom(Class<?> roomClass) {
		((StrongholdRoomPool)this.owner.getRoomPool()).setForcedRoom(roomClass);
	}

	protected StrongholdDoor getRandomDoor(Random rand) {
		return StrongholdDoor.random(rand);
	}

	/**
	 * Places one doorway of {@code door} at local (x,y,z). Mirrors the original: only
	 * builds when there is solid ground beneath and at least one roof block above the
	 * three-block span.
	 */
	protected void placeDoor(World world, Random rand, FeatureAABB clip, StrongholdDoor door, int x, int y, int z) {
		if(this.getBlockId(world, x, y - 1, z, clip) == 0
				|| this.getBlockId(world, x + 1, y - 1, z, clip) == 0
				|| this.getBlockId(world, x + 2, y - 1, z, clip) == 0) {
			return;
		}

		if(this.getBlockId(world, x, y + 3, z, clip) == 0
				&& this.getBlockId(world, x + 1, y + 3, z, clip) == 0
				&& this.getBlockId(world, x + 2, y + 3, z, clip) == 0) {
			return;
		}

		switch(door.ordinal()) {
		case 0:
		default:
			this.fillWithBlocks(world, clip, x, y, z, x + 2, y + 2, z, 0, 0, false);
			break;
		case 1:
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x, y, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x, y + 1, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 1, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 2, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 2, y + 1, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 2, y, z, clip);
			this.placeBlock(world, Block.doorWood.blockID, 0, x + 1, y, z, clip);
			this.placeBlock(world, Block.doorWood.blockID, 8, x + 1, y + 1, z, clip);
			break;
		case 2:
			this.placeBlock(world, 0, 0, x + 1, y, z, clip);
			this.placeBlock(world, 0, 0, x + 1, y + 1, z, clip);
			this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, x, y, z, clip);
			this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, x, y + 1, z, clip);
			this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, x, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, x + 1, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, x + 2, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, x + 2, y + 1, z, clip);
			this.placeBlock(world, StrongholdPalette.IRON_FENCE_BLOCK, StrongholdPalette.IRON_FENCE_META, x + 2, y, z, clip);
			break;
		case 3:
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x, y, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x, y + 1, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 1, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 2, y + 2, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 2, y + 1, z, clip);
			this.placeBlock(world, StrongholdPalette.BRICK_BLOCK, StrongholdPalette.BRICK_META, x + 2, y, z, clip);
			this.placeBlock(world, Block.doorSteel.blockID, 0, x + 1, y, z, clip);
			this.placeBlock(world, Block.doorSteel.blockID, 8, x + 1, y + 1, z, clip);
			this.placeBlock(world, Block.button.blockID, this.getMetadataWithOffset(Block.button.blockID, 4), x + 2, y + 1, z + 1, clip);
			this.placeBlock(world, Block.button.blockID, this.getMetadataWithOffset(Block.button.blockID, 3), x + 2, y + 1, z - 1, clip);
		}
	}

	/** Neighbour straight ahead of this piece's facing. */
	protected StructurePiece getNextNormal(List<StructurePiece> pieces, Random rand, int dx, int dy) {
		switch(this.coordBaseMode) {
		case 0:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + dx, this.box.y1 + dy, this.box.z2 + 1, this.coordBaseMode, this.getPieceType());
		case 1:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 + dy, this.box.z1 + dx, this.coordBaseMode, this.getPieceType());
		case 2:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + dx, this.box.y1 + dy, this.box.z1 - 1, this.coordBaseMode, this.getPieceType());
		case 3:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 + dy, this.box.z1 + dx, this.coordBaseMode, this.getPieceType());
		default:
			return null;
		}
	}

	/** Neighbour on the piece's left (relative to its facing). */
	protected StructurePiece getNextSideX(List<StructurePiece> pieces, Random rand, int dy, int dz) {
		switch(this.coordBaseMode) {
		case 0:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 + dy, this.box.z1 + dz, 1, this.getPieceType());
		case 1:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + dz, this.box.y1 + dy, this.box.z1 - 1, 2, this.getPieceType());
		case 2:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 + dy, this.box.z1 + dz, 1, this.getPieceType());
		case 3:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + dz, this.box.y1 + dy, this.box.z1 - 1, 2, this.getPieceType());
		default:
			return null;
		}
	}

	/** Neighbour on the piece's right (relative to its facing). */
	protected StructurePiece getNextSideZ(List<StructurePiece> pieces, Random rand, int dy, int dz) {
		switch(this.coordBaseMode) {
		case 0:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 + dy, this.box.z1 + dz, 3, this.getPieceType());
		case 1:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + dz, this.box.y1 + dy, this.box.z2 + 1, 0, this.getPieceType());
		case 2:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 + dy, this.box.z1 + dz, 3, this.getPieceType());
		case 3:
			return this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + dz, this.box.y1 + dy, this.box.z2 + 1, 0, this.getPieceType());
		default:
			return null;
		}
	}
}
