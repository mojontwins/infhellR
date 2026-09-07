package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.Tessellator;

/**
	 * Renders a street lantern. On the first render pass, and only for the intact lantern block, it draws a
	 * torch at the centre of the block (the visible light source); broken lanterns render nothing on this
	 * pass. On the second pass it draws the lantern casing via the cactus renderer, giving the hollow
	 * post-and-pane frame. The inventory view renders an inset cube.
	 */
	public final class RenderBlockStreetLantern implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Add a torch for non broken street lanterns
		if(renderBlocks.getActiveRenderPass() == 0) {
			if(block.blockID == Block.streetLantern.blockID) {
				Tessellator tessellator = Tessellator.instance;
				tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);		
				RenderBlockTorch.renderTorchAtAngle(renderBlocks, Block.torchWood, (double)x, (double)y, (double)z, 0.0D, 0.0D);
				return true;
			} else {
				return false;
			}
		} else {
			return RenderBlockCactus.renderBlockCactus(renderBlocks, block, x, y, z);
		}
	}


	@Override
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderInsetCubeOnInventory(renderBlocks, block, brightness);
	}
}
