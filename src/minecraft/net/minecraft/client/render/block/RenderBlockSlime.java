package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
	 * Renders a slime block as a nested cube using the two render passes. On the first pass
	 * ({@code getActiveRenderPass() == 0}) it draws a smaller inner box (from 0.125 to 0.875); on the
	 * second pass it draws the full outer box over it, producing the classic bouncy slime look where the
	 * inner blob shows through the translucent outer shell. Both passes use the standard block renderer.
	 */
	public final class RenderBlockSlime implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// This renderer sends different quads depending on the active renderPass
		
		if(renderBlocks.getActiveRenderPass() == 0) {
			// Set dimensions of inner block
			block.setBlockBounds(0.125F, 0.125F, 0.125F, 0.875F, 0.875F, 0.875F);
			
			// Render inner box
			boolean wasRendered = RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
			
			// Set dimensions of outer block
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		
			return wasRendered;
		} else {
			// Render outer box
			return RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		}
	}


	@Override
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderCubeOnInventory(renderBlocks, block, metadata, brightness);
	}
}
