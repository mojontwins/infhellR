package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.mineshaft.MineshaftComponent;

/**
 * The stronghold's start piece: the entrance stairwell plus the bookkeeping the vanilla
 * {@code StructureStrongholdPieces} kept on it - the last piece weight used (so the same
 * piece is not chosen twice in a row) and the queue of child pieces still to be expanded.
 */
public class StrongholdComponentStairs2 extends StrongholdComponentStairs {
	/** The last weight that produced a piece (never picked twice in a row). */
	public StrongholdPieceWeight currentPieceWeight;
	/** Pieces still to be expanded by the start. */
	public ArrayList<MineshaftComponent> children = new ArrayList<MineshaftComponent>();

	public StrongholdComponentStairs2(World world, int componentType, Random rand, int x, int z) {
		super(world, 0, rand, x, z);
	}
}
