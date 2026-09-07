package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
 * Default renderer for regular full-cube blocks. Delegates world rendering to
 * {@link RenderBlocks#renderStandardBlock} (the standard 6-face box with AO and
 * texture handling) and inventory rendering to
 * {@link RenderBlockUtil#renderCubeOnInventory}.
 */
public final class RenderBlockNormal implements BlockRenderHandler {
	@Override
	// Renders the block as a standard full cube via renderStandardBlock.
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		return renderBlocks.renderStandardBlock(block, x, y, z);
	}

	@Override
	// Renders a plain cube for the inventory/drop view.
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderCubeOnInventory(renderBlocks, block, metadata, brightness);
	}
}