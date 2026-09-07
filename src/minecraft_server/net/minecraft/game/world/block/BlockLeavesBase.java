package net.minecraft.game.world.block;

import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.material.Material;

public class BlockLeavesBase extends Block {
	protected boolean graphicsLevel;

	protected BlockLeavesBase(int id, int blockIndex, Material material, boolean graphicsLevel) {
		super(id, blockIndex, material);
		this.graphicsLevel = graphicsLevel;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		int i6 = blockAccess.getBlockId(x, y, z);
		return !this.graphicsLevel && i6 == this.blockID ? false : super.shouldSideBeRendered(blockAccess, x, y, z, side);
	}
}
