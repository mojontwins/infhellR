package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
	 * Renders an oriented log/wood block. It reads the wood's axis from metadata bits 2-3 and rotates the
	 * cube's UVs around the correct axis: orientation 12 (bitmask value) treats the block as vertical
	 * (bark wraps around the long vertical run), 4 as running east/west and 0/8 as running north/south.
	 * It sets the matching {@code uvRotate*} flags on the renderer, renders the standard block, then resets
	 * every rotation flag so the shared renderer state is restored for the next block.
	 */
	public final class RenderBlockWoodOriented implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// metadata bits 2-3 carry the axis orientation.
		int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		int orientation = meta & 12;
		if(orientation == 12) {
			renderBlocks.uvRotateEast = 1;
			renderBlocks.uvRotateWest = 1;
			renderBlocks.uvRotateTop = 1;
			renderBlocks.uvRotateBottom = 1;
		} else if(orientation == 4) {
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
