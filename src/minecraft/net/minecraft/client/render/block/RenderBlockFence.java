package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;

/**
 * Renderer for fence blocks. Draws the four central vertical posts, then the
 * horizontal rails (top and mid) only along the axes where a neighbouring
 * fence exists. Connectivity is decided by checking the four horizontal
 * neighbours for the same block id, and an isolated post defaults to
 * east-west so it never renders as an unconnected dot.
 */
public final class RenderBlockFence implements BlockRenderHandler {
	@Override
	/** Draws fence posts plus axis rails based on neighbouring fence connectivity. */
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {

		
		float min = 0.375F;
		float max = 0.625F;
		block.setBlockBounds(min, 0.0F, min, max, 1.0F, max);
		RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		
		// Detect fence neighbours in each horizontal direction.
		boolean connectEorW = false;
		boolean connectNorS = false;
		if(renderBlocks.blockAccess.getBlockId(x - 1, y, z) == block.blockID || renderBlocks.blockAccess.getBlockId(x + 1, y, z) == block.blockID) {
			connectEorW = true;
		}

		if(renderBlocks.blockAccess.getBlockId(x, y, z - 1) == block.blockID || renderBlocks.blockAccess.getBlockId(x, y, z + 1) == block.blockID) {
			connectNorS = true;
		}

		boolean connectE = renderBlocks.blockAccess.getBlockId(x - 1, y, z) == block.blockID;
		boolean connectW = renderBlocks.blockAccess.getBlockId(x + 1, y, z) == block.blockID;
		boolean connectN = renderBlocks.blockAccess.getBlockId(x, y, z - 1) == block.blockID;
		boolean connectS = renderBlocks.blockAccess.getBlockId(x, y, z + 1) == block.blockID;
		// An isolated fence post still renders as an east-west rail.
		if(!connectEorW && !connectNorS) {
			connectEorW = true;
		}

		min = 0.4375F;
		max = 0.5625F;
		// Rail bounds stretch end-to-end on connected axes, else shrink to the post.
		float minY = 0.75F;
		float maxY = 0.9375F;
		float minX = connectE ? 0.0F : min;
		float maxX = connectW ? 1.0F : max;
		float minZ = connectN ? 0.0F : min;
		float maxZ = connectS ? 1.0F : max;
		
		// Top rail.
		if(connectEorW) {
			block.setBlockBounds(minX, minY, min, maxX, maxY, max);
			RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		}

		if(connectNorS) {
			block.setBlockBounds(min, minY, minZ, max, maxY, maxZ);
			RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		}

		// Mid rail.
		minY = 0.375F;
		maxY = 0.5625F;
		if(connectEorW) {
			block.setBlockBounds(minX, minY, min, maxX, maxY, max);
			RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		}

		if(connectNorS) {
			block.setBlockBounds(min, minY, minZ, max, maxY, maxZ);
			RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		}

		//block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		block.setBlockBoundsBasedOnState(renderBlocks.blockAccess, x, y, z);
		
		return true;
	
	}
}
