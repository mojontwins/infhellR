package net.minecraft.game.world.block;

import net.minecraft.game.world.material.Material;

/**
 * Cobblestone and its mossy variant. Behaviourally identical to a plain rock
 * block; the class exists so that {@link #canGrowMoss()} can be overridden to
 * true (surface moss clings to cobblestone, e.g. on city walls).
 */
public class BlockCobblestone extends Block {
	protected BlockCobblestone(int blockID, int blockIndexInTexture) {
		super(blockID, blockIndexInTexture, Material.rock);
	}

	@Override
	public boolean canGrowMoss() {
		return true;
	}
}