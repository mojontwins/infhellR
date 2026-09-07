package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
	 * Renders a dirt-path block as a standard block squashed to only 15/16 of a block tall (a thin slab,
	 * 0.0625F above the floor) so it reads as a worn earthen track rather than a full cube. The remaining
	 * 1/16 gap below is not filled, which lets neighbouring path blocks look contiguous. Rendering is
	 * delegated to the standard block renderer and the inventory cube renderer.
	 */
	public final class RenderBlockDirtPath implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 15.0F * (1 / 16.0F), 1.0F);
		return RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
	}


	@Override
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderCubeOnInventory(renderBlocks, block, metadata, brightness);
	}
}
