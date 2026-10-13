package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.feature.structure.StructureBlockSelector;

/**
 * Stronghold wall material: hollow cells (the outer shell) become brick, with the classic
 * mossy/plain random substitutions; interior cells are carved to air.
 */
public class StrongholdBlockSelector extends StructureBlockSelector {
	@Override
	public void select(Random rand, int x, int y, int z, boolean onEdge) {
		if(!onEdge) {
			this.selectedBlockId = 0;
			this.selectedMetadata = 0;
			return;
		}

		float roll = rand.nextFloat();
		if(roll < 0.2F) {
			this.selectedBlockId = StrongholdPalette.MOSSY_BLOCK;
			this.selectedMetadata = StrongholdPalette.MOSSY_META;
		} else if(roll < 0.5F) {
			this.selectedBlockId = StrongholdPalette.PLAIN_BLOCK;
			this.selectedMetadata = StrongholdPalette.PLAIN_META;
		} else {
			this.selectedBlockId = StrongholdPalette.BRICK_BLOCK;
			this.selectedMetadata = StrongholdPalette.BRICK_META;
		}
	}
}
