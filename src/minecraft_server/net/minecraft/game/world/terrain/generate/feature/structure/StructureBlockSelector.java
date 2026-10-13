package net.minecraft.game.world.terrain.generate.feature.structure;

import java.util.Random;

/** Picks the block id + metadata to place at each cell of a randomized fill. */
public abstract class StructureBlockSelector {
	protected int selectedBlockId;
	protected int selectedMetadata;

	public abstract void select(Random rand, int x, int y, int z, boolean onEdge);

	public int getSelectedBlockId() {
		return this.selectedBlockId;
	}

	public int getSelectedMetadata() {
		return this.selectedMetadata;
	}
}
