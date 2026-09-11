package net.minecraft.game.world.block;

import net.minecraft.game.world.material.Material;

public class BlockPlanks extends Block {

	protected BlockPlanks(int id, int blockIndex, Material material) {
		super(id, blockIndex, material);
	}

	@Override
	public int getEncouragementToFire() {
		return 5;
	}

	@Override
	public int getAbilityToCatchFire() {
		return 20;
	}
}