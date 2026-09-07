package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockPane;

/**
 * Renders glass/plank panes (BlockPane). Based on which neighbours the pane
 * connects to, it draws the connecting bars and the thin centre strut as
 * double-sided quads on the block faces, mixing a wide face texture and a
 * narrow side texture. Frozen thin-planks panes
 * (blockID == Block.thinPlanks) use a simpler path that reuses
 * RenderBlockUtil.renderStandardBlock with per-direction bounding boxes
 * instead of hand-rolled quads.
 */
public final class RenderBlockPane implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Neighbour connectivity (connectN/S/W/E plus above/below) selects which pane bars and struts to emit.
		BlockPane pane = (BlockPane) block;
 
		int maxHeight = 128;
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(pane.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		
		int colorMultiplier = pane.colorMultiplier(renderBlocks.blockAccess, x, y, z);
		float r = (float)(colorMultiplier >> 16 & 255) / 255.0F;
		float g = (float)(colorMultiplier >> 8 & 255) / 255.0F;
		float b = (float)(colorMultiplier & 255) / 255.0F;
		
		tessellator.setColorOpaque_F(r, g, b);

		int textureId;
		int sideTextureId;

		if(renderBlocks.overrideBlockTexture >= 0) {
			textureId = renderBlocks.overrideBlockTexture;
			sideTextureId = renderBlocks.overrideBlockTexture;
		} else {
			int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
			textureId = pane.getBlockTextureFromSideAndMetadata(0, meta);
			sideTextureId = pane.getSideTextureIndex();
		}

		AtlasTexel.calc(textureId, TextureAtlas.TERRAIN);
		int u = AtlasTexel.u;
		int v = AtlasTexel.v;
		double faceU1 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) u);
		double faceU2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) u + 7.99F);
		double faceU3 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) u + TextureAtlas.TERRAIN.tileSpan);
		double faceV1 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) v);
		double faceV2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) v + TextureAtlas.TERRAIN.tileSpan);

		AtlasTexel.calc(sideTextureId, TextureAtlas.TERRAIN);
		u = AtlasTexel.u;
		v = AtlasTexel.v;
		double sideU1 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)(u + 7));
		double sideU2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) u + 8.99F);
		double sideV3 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) v);
		double sideV1 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)(v + 8));
		double sideV2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) v + TextureAtlas.TERRAIN.tileSpan);
		
		double xMin = (double)x;
		double xc = (double)x + 0.5D;
		double x1 = (double)(x + 1);
		double zMin = (double)z;
		double zc = (double)z + 0.5D;
		double z1 = (double)(z + 1);
		double xc0 = (double)x + 0.5D - 0.0625D;
		double xc1 = (double)x + 0.5D + 0.0625D;
		double zc0 = (double)z + 0.5D - 0.0625D;
		double zc1 = (double)z + 0.5D + 0.0625D;
		
		boolean connectN = pane.canThisPaneConnectToThisBlockID(renderBlocks.blockAccess.getBlockId(x, y, z - 1));
		boolean connectS = pane.canThisPaneConnectToThisBlockID(renderBlocks.blockAccess.getBlockId(x, y, z + 1));
		boolean connectW = pane.canThisPaneConnectToThisBlockID(renderBlocks.blockAccess.getBlockId(x - 1, y, z));
		boolean connectE = pane.canThisPaneConnectToThisBlockID(renderBlocks.blockAccess.getBlockId(x + 1, y, z));
		boolean connectUP = pane.shouldSideBeRendered(renderBlocks.blockAccess, x, y + 1, z, 1);
		boolean connectDW = pane.shouldSideBeRendered(renderBlocks.blockAccess, x, y - 1, z, 0);
		
		if (pane.blockID == Block.thinPlanks.blockID) {
		
			// New, simple renderer
			if (!connectN && !connectS && !connectW && !connectE) {
				pane.setBlockBounds(.5F-0.0625F, 0.0F, .5F-0.0625F, .5F+0.0625F, 1.0F, .5F+0.0625F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, pane, x, y, z);
			} else if (connectN && connectS && !connectW && !connectE) {
				pane.setBlockBounds(.5F-0.0625F, 0.0F, 0.0F, .5F+0.0625F, 1.0F, 1.0F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, pane, x, y, z);
			} else if (!connectN && !connectS && connectW && connectE) {
				pane.setBlockBounds(0.0F, 0.0F, .5F-0.0625F, 1.0F, 1.0F, .5F+0.0625F);
				RenderBlockUtil.renderStandardBlock(renderBlocks, pane, x, y, z);
			} else {
				if (connectS) {
					pane.setBlockBounds(.5F-0.0625F, 0.0F, .5F, .5F+0.0625F, 1.0F, 1F);
					RenderBlockUtil.renderStandardBlock(renderBlocks, pane, x, y, z);
				}
				
				if (connectN) {
					pane.setBlockBounds(.5F-0.0625F, 0.0F, 0F, .5F+0.0625F, 1.0F, .5F);
					RenderBlockUtil.renderStandardBlock(renderBlocks, pane, x, y, z);
				}
				
				if (connectE) {
					pane.setBlockBounds(0.5F, 0.0F, .5F-0.0625F, 1.0F, 1.0F, .5F+0.0625F);
					RenderBlockUtil.renderStandardBlock(renderBlocks, pane, x, y, z);
				}
				
				if (connectW) {
					pane.setBlockBounds(0F, 0.0F, .5F-0.0625F, 0.5F, 1.0F, .5F+0.0625F);
					RenderBlockUtil.renderStandardBlock(renderBlocks, pane, x, y, z);
				}
			}
			
		} else {
			
			if((!connectW || !connectE) && (connectW || connectE || connectN || connectS)) {
				if(connectW && !connectE) {
					tessellator.addVertexWithUV(xMin, (double)(y + 1), zc, faceU1, faceV1);
					tessellator.addVertexWithUV(xMin, (double)(y + 0), zc, faceU1, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU2, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU2, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU1, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU1, faceV2);
					tessellator.addVertexWithUV(xMin, (double)(y + 0), zc, faceU2, faceV2);
					tessellator.addVertexWithUV(xMin, (double)(y + 1), zc, faceU2, faceV1);
					if(!connectS && !connectN) {
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc1, sideU1, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc1, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc0, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc0, sideU2, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc0, sideU1, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc1, sideU2, sideV3);
					}
	
					if(connectUP || y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x - 1, y + 1, z)) {
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
					}
	
					if(connectDW || y > 1 && renderBlocks.blockAccess.isAirBlock(x - 1, y - 1, z)) {
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV1);
					}
				} else if(!connectW && connectE) {
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU2, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU2, faceV2);
					tessellator.addVertexWithUV(x1, (double)(y + 0), zc, faceU3, faceV2);
					tessellator.addVertexWithUV(x1, (double)(y + 1), zc, faceU3, faceV1);
					tessellator.addVertexWithUV(x1, (double)(y + 1), zc, faceU2, faceV1);
					tessellator.addVertexWithUV(x1, (double)(y + 0), zc, faceU2, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU3, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU3, faceV1);
					if(!connectS && !connectN) {
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc0, sideU1, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc1, sideU1, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc1, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 0), zc0, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1), zc0, sideU2, sideV3);
					}
	
					if(connectUP || y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x + 1, y + 1, z)) {
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV3);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc0, sideU1, sideV3);
					}
	
					if(connectDW || y > 1 && renderBlocks.blockAccess.isAirBlock(x + 1, y - 1, z)) {
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV3);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc0, sideU1, sideV3);
					}
				}
			} else {
				tessellator.addVertexWithUV(xMin, (double)(y + 1), zc, faceU1, faceV1);
				tessellator.addVertexWithUV(xMin, (double)(y + 0), zc, faceU1, faceV2);
				tessellator.addVertexWithUV(x1, (double)(y + 0), zc, faceU3, faceV2);
				tessellator.addVertexWithUV(x1, (double)(y + 1), zc, faceU3, faceV1);
				tessellator.addVertexWithUV(x1, (double)(y + 1), zc, faceU1, faceV1);
				tessellator.addVertexWithUV(x1, (double)(y + 0), zc, faceU1, faceV2);
				tessellator.addVertexWithUV(xMin, (double)(y + 0), zc, faceU3, faceV2);
				tessellator.addVertexWithUV(xMin, (double)(y + 1), zc, faceU3, faceV1);
				if(connectUP) {
					tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc1, sideU2, sideV2);
					tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc1, sideU2, sideV3);
					tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc0, sideU1, sideV3);
					tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc0, sideU1, sideV2);
					tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc1, sideU2, sideV2);
					tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc1, sideU2, sideV3);
					tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc0, sideU1, sideV3);
					tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc0, sideU1, sideV2);
				} else {
					if(y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x - 1, y + 1, z)) {
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xMin, (double)(y + 1) + 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
					}
	
					if(y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x + 1, y + 1, z)) {
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV3);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)(y + 1) + 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(x1, (double)(y + 1) + 0.01D, zc0, sideU1, sideV3);
					}
				}
	
				if(connectDW) {
					tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc1, sideU2, sideV2);
					tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc1, sideU2, sideV3);
					tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc0, sideU1, sideV3);
					tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc0, sideU1, sideV2);
					tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc1, sideU2, sideV2);
					tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc1, sideU2, sideV3);
					tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc0, sideU1, sideV3);
					tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc0, sideU1, sideV2);
				} else {
					if(y > 1 && renderBlocks.blockAccess.isAirBlock(x - 1, y - 1, z)) {
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc1, sideU2, sideV2);
						tessellator.addVertexWithUV(xMin, (double)y - 0.01D, zc0, sideU1, sideV2);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV1);
					}
	
					if(y > 1 && renderBlocks.blockAccess.isAirBlock(x + 1, y - 1, z)) {
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV3);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc1, sideU2, sideV3);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc1, sideU2, sideV1);
						tessellator.addVertexWithUV(xc, (double)y - 0.01D, zc0, sideU1, sideV1);
						tessellator.addVertexWithUV(x1, (double)y - 0.01D, zc0, sideU1, sideV3);
					}
				}
			}
	
			if((!connectN || !connectS) && (connectW || connectE || connectN || connectS)) {
				if(connectN && !connectS) {
					tessellator.addVertexWithUV(xc, (double)(y + 1), zMin, faceU1, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zMin, faceU1, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU2, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU2, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU1, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU1, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zMin, faceU2, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 1), zMin, faceU2, faceV1);
					if(!connectE && !connectW) {
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU1, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 0), zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 0), zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU2, sideV3);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU1, sideV3);
						tessellator.addVertexWithUV(xc1, (double)(y + 0), zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc0, (double)(y + 0), zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU2, sideV3);
					}
	
					if(connectUP || y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x, y + 1, z - 1)) {
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zMin, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zMin, sideU1, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zMin, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zMin, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU1, sideV3);
					}
	
					if(connectDW || y > 1 && renderBlocks.blockAccess.isAirBlock(x, y - 1, z - 1)) {
						tessellator.addVertexWithUV(xc0, (double)y, zMin, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zMin, sideU1, sideV3);
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)y, zMin, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zMin, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU1, sideV3);
					}
				} else if(!connectN && connectS) {
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU2, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU2, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 0), z1, faceU3, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 1), z1, faceU3, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 1), z1, faceU2, faceV1);
					tessellator.addVertexWithUV(xc, (double)(y + 0), z1, faceU2, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 0), zc, faceU3, faceV2);
					tessellator.addVertexWithUV(xc, (double)(y + 1), zc, faceU3, faceV1);
					if(!connectE && !connectW) {
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU1, sideV3);
						tessellator.addVertexWithUV(xc1, (double)(y + 0), zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc0, (double)(y + 0), zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU1, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 0), zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 0), zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU2, sideV3);
					}
	
					if(connectUP || y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x, y + 1, z + 1)) {
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), z1, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), z1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), z1, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), z1, sideU2, sideV1);
					}
	
					if(connectDW || y > 1 && renderBlocks.blockAccess.isAirBlock(x, y - 1, z + 1)) {
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)y, z1, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, z1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc0, (double)y, z1, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, z1, sideU2, sideV1);
					}
				}
			} else {
				tessellator.addVertexWithUV(xc, (double)(y + 1), z1, faceU1, faceV1);
				tessellator.addVertexWithUV(xc, (double)(y + 0), z1, faceU1, faceV2);
				tessellator.addVertexWithUV(xc, (double)(y + 0), zMin, faceU3, faceV2);
				tessellator.addVertexWithUV(xc, (double)(y + 1), zMin, faceU3, faceV1);
				tessellator.addVertexWithUV(xc, (double)(y + 1), zMin, faceU1, faceV1);
				tessellator.addVertexWithUV(xc, (double)(y + 0), zMin, faceU1, faceV2);
				tessellator.addVertexWithUV(xc, (double)(y + 0), z1, faceU3, faceV2);
				tessellator.addVertexWithUV(xc, (double)(y + 1), z1, faceU3, faceV1);
				if(connectUP) {
					tessellator.addVertexWithUV(xc1, (double)(y + 1), z1, sideU2, sideV2);
					tessellator.addVertexWithUV(xc1, (double)(y + 1), zMin, sideU2, sideV3);
					tessellator.addVertexWithUV(xc0, (double)(y + 1), zMin, sideU1, sideV3);
					tessellator.addVertexWithUV(xc0, (double)(y + 1), z1, sideU1, sideV2);
					tessellator.addVertexWithUV(xc1, (double)(y + 1), zMin, sideU2, sideV2);
					tessellator.addVertexWithUV(xc1, (double)(y + 1), z1, sideU2, sideV3);
					tessellator.addVertexWithUV(xc0, (double)(y + 1), z1, sideU1, sideV3);
					tessellator.addVertexWithUV(xc0, (double)(y + 1), zMin, sideU1, sideV2);
				} else {
					if(y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x, y + 1, z - 1)) {
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zMin, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zMin, sideU1, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zMin, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zMin, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU1, sideV3);
					}
	
					if(y < maxHeight - 1 && renderBlocks.blockAccess.isAirBlock(x, y + 1, z + 1)) {
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), z1, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), z1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), z1, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)(y + 1), zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)(y + 1), z1, sideU2, sideV1);
					}
				}
	
				if(connectDW) {
					tessellator.addVertexWithUV(xc1, (double)y, z1, sideU2, sideV2);
					tessellator.addVertexWithUV(xc1, (double)y, zMin, sideU2, sideV3);
					tessellator.addVertexWithUV(xc0, (double)y, zMin, sideU1, sideV3);
					tessellator.addVertexWithUV(xc0, (double)y, z1, sideU1, sideV2);
					tessellator.addVertexWithUV(xc1, (double)y, zMin, sideU2, sideV2);
					tessellator.addVertexWithUV(xc1, (double)y, z1, sideU2, sideV3);
					tessellator.addVertexWithUV(xc0, (double)y, z1, sideU1, sideV3);
					tessellator.addVertexWithUV(xc0, (double)y, zMin, sideU1, sideV2);
				} else {
					if(y > 1 && renderBlocks.blockAccess.isAirBlock(x, y - 1, z - 1)) {
						tessellator.addVertexWithUV(xc0, (double)y, zMin, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zMin, sideU1, sideV3);
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU2, sideV3);
						tessellator.addVertexWithUV(xc0, (double)y, zMin, sideU2, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zMin, sideU1, sideV1);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU1, sideV3);
					}
	
					if(y > 1 && renderBlocks.blockAccess.isAirBlock(x, y - 1, z + 1)) {
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)y, z1, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, z1, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU2, sideV1);
						tessellator.addVertexWithUV(xc0, (double)y, z1, sideU1, sideV1);
						tessellator.addVertexWithUV(xc0, (double)y, zc, sideU1, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, zc, sideU2, sideV2);
						tessellator.addVertexWithUV(xc1, (double)y, z1, sideU2, sideV1);
					}
				}
			}
		}
		
		return true;
	
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
