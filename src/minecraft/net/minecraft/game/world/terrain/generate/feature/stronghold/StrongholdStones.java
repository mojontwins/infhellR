package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.feature.StructurePieceBlockSelector;

/**
 * Chooses the stone variety for a stronghold block: solid stone bricks on the piece
 * boundary, and a random mix of stone bricks / mossy cobblestone / stone in the interior.
 * Port of the vanilla {@code StructureStrongholdStones}.
 */
public class StrongholdStones extends StructurePieceBlockSelector {
	@Override
	public void selectBlocks(Random rand, int x, int y, int z, boolean boundary) {
		if(!boundary) {
			this.selectedBlockId = 0;
			this.selectedBlockMetaData = 0;
			return;
		}

		this.selectedBlockId = StrongholdPieces.BRICKS_ID;
		this.selectedBlockMetaData = StrongholdPieces.BRICKS_META;
		float roll = rand.nextFloat();
		if(roll < 0.2F) {
			this.selectedBlockId = StrongholdPieces.BRICKS_ALT1_ID;
			this.selectedBlockMetaData = StrongholdPieces.BRICKS_ALT1_META;
		} else if(roll < 0.5F) {
			this.selectedBlockId = StrongholdPieces.BRICKS_ALT2_ID;
			this.selectedBlockMetaData = StrongholdPieces.BRICKS_ALT2_META;
		}
	}
}
