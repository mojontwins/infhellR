package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.world.block.Block;
import org.lwjgl.opengl.GL11;

/**
 * Renderer for stair blocks. Builds stairs from two overlapping standard
 * quads (one lower half-step, one upper/offset step) whose bounds depend on
 * the 2-bit orientation (metadata 0-3) and the 8-bit "upside-down" flag. The
 * full-cube bounds are restored afterwards so neighbouring geometry is
 * correct.
 */
public final class RenderBlockStairs implements BlockRenderHandler {
	@Override
	/** Draws a stair as two stacked/offset standard blocks for the current orientation. */
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {

		boolean rendered = false;
		// Lower 3 bits = facing/orientation; top bit = upside-down stair.
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		boolean upsideDown = (metadata & 8) != 0;
		metadata &= 7;
		
		if(upsideDown) {
			if(metadata == 0) {
				block.setBlockBounds(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			} else if(metadata == 1) {
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			} else if(metadata == 2) {
				block.setBlockBounds(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			} else if(metadata == 3) {
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			}
		} else {
			if(metadata == 0) {
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			} else if(metadata == 1) {
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			} else if(metadata == 2) {
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			} else if(metadata == 3) {
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				block.setBlockBounds(0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
				rendered = true;
			}
		}
		
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		return rendered;
	}

	@Override
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		for(int j = 0; j < 2; ++j) {
			if(j == 0) {
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
			} else {
				block.setBlockBounds(0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F);
			}
			
			renderBlocks.renderAllFaces = true;
			renderBlocks.overrideBlockTexture = -1;
			for(int side = 0; side < 6; ++side) {
				tessellator.startDrawingQuads();
				float[] normal = RenderBlocks.SIDE_NORMALS[side];
				tessellator.setNormal(normal[0], normal[1], normal[2]);
				float shade = side == 0 ? 0.5F
				             : side == 1 ? 1.0F
				             : side == 2 || side == 3 ? 0.8F
				             : 0.6F;
				RenderBlockUtil.applyInventoryColor(renderBlocks, block, metadata, brightness * shade);
				renderBlocks.renderFace(block, side, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(side, metadata));
				tessellator.draw();
			}
		}
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
