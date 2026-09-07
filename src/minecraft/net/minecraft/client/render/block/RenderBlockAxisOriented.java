package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
	 * Renders a generic axis-oriented block whose facing comes directly from raw metadata: 4/5 treats the
	 * block as running east/west and 2/3 as running north/south (other values leave it unrotated). It sets
	 * the matching {@code uvRotate*} flags, renders the standard cube, and resets every flag afterwards so
	 * the shared renderer state is returned to defaults.
	 */
	public final class RenderBlockAxisOriented implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Choose the rotation axis from metadata: 4/5 = east/west, 2/3 = north/south.
		int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		
		if(meta == 4 || meta == 5) {
			renderBlocks.uvRotateEast = 1;
			renderBlocks.uvRotateWest = 1;
			renderBlocks.uvRotateTop = 1;
			renderBlocks.uvRotateBottom = 1;
		} else if(meta == 2 || meta == 3) {
			renderBlocks.uvRotateSouth = 1;
			renderBlocks.uvRotateNorth = 1;
		}

		boolean res = RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		renderBlocks.uvRotateSouth = 0;
		renderBlocks.uvRotateEast = 0;
		renderBlocks.uvRotateWest = 0;
		renderBlocks.uvRotateNorth = 0;
		renderBlocks.uvRotateTop = 0;
		renderBlocks.uvRotateBottom = 0;
		return res;
	}


	@Override
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderCubeOnInventory(renderBlocks, block, metadata, brightness);
	}
}
