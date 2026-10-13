package net.minecraft.game.world.terrain.generate.feature.stronghold;

import net.minecraft.game.world.block.Block;

/**
 * Block palette used by every stronghold piece. Centralised so a resource-pack-style
 * re-skin is a single edit rather than a hunt through a dozen files.
 */
public final class StrongholdPalette {
	public static final int BRICK_BLOCK = Block.stoneBricks.blockID;
	public static final int BRICK_META = 0;

	/** Randomly substituted variant (mossy cobblestone). */
	public static final int MOSSY_BLOCK = Block.cobblestoneMossy.blockID;
	public static final int MOSSY_META = 0;

	/** Second random variant (plain stone). */
	public static final int PLAIN_BLOCK = Block.stone.blockID;
	public static final int PLAIN_META = 0;

	public static final int IRON_FENCE_BLOCK = Block.fenceIron.blockID;
	public static final int IRON_FENCE_META = 0;

	private StrongholdPalette() {
	}
}
