package net.minecraft.game.world.block;

import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.material.Material;

public class BlockBreakable extends Block {
	private boolean localFlag;

	protected BlockBreakable(int id, int blockIndex, Material material, boolean localFlag) {
		super(id, blockIndex, material);
		this.localFlag = localFlag;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		int i6 = blockAccess.getBlockId(x, y, z);
		return !this.localFlag && i6 == this.blockID ? false : super.shouldSideBeRendered(blockAccess, x, y, z, side);
	}
}
