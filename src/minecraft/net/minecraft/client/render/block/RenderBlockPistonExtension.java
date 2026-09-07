package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
 * Renderer for the extended portion of a piston (the head rod that slides out
 * of the base). Delegates to the shared piston-extension geometry routine in
 * {@link RenderBlocks}, always drawn in the extended/pushed state.
 */
public final class RenderBlockPistonExtension implements BlockRenderHandler {
	@Override
	/** Draws the piston extension/rod geometry in its extended pose. */
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		return renderBlocks.renderPistonExtension(block, x, y, z, true);
	}
}