package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockRail;

/**
 * Renders a rail track as a single thin quad (0.0625 thick) laid on the
 * supporting block face, emitted twice with reversed winding so both sides of
 * the track are visible under face culling. The quad's four corner coordinates
 * are remapped by metadata: states 0, 4, 5, 6 keep the base square, the corner
 * states 8 and 9 rotate the corners 90 degrees, states 1, 2, 3, 7 (rotated
 * straights / sloped) use a different corner remap, and the slope states 2/4
 * and 3/5 raise one edge a full block to form an incline. Powered rails first
 * mask off the powered bit (metadata &= 7).
 */
public final class RenderBlockRail implements BlockRenderHandler {
	/**
	 * Emits the double-sided rail quad for the cell at (x, y, z), honouring
	 * powered-rail metadata and per-state corner/slope geometry; returns true.
	 */
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		BlockRail rail = (BlockRail) block;
		Tessellator tessellator = Tessellator.instance;
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		int texId = rail.getBlockTextureFromSideAndMetadata(0, metadata);
		if(renderBlocks.overrideBlockTexture >= 0) {
			texId = renderBlocks.overrideBlockTexture;
		}

		if(rail.getIsPowered()) {
			metadata &= 7;
		}

		tessellator.setBrightness(rail.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		
		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		double uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileX);
		double uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileX + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileY);
		double vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileY + TextureAtlas.TERRAIN.tileSpan);
		float plateY = 0.0625F;
		float xMin = (float)(x + 1);
		float x1 = (float)(x + 1);
		float x2 = (float)(x + 0);
		float xMax = (float)(x + 0);
		float zMin = (float)(z + 0);
		float z1 = (float)(z + 1);
		float z2 = (float)(z + 1);
		float zMax = (float)(z + 0);
		float yMin = (float)y + plateY;
		float y1 = (float)y + plateY;
		float y2 = (float)y + plateY;
		float yMax = (float)y + plateY;
		if(metadata != 1 && metadata != 2 && metadata != 3 && metadata != 7) {
			if(metadata == 8) {
				xMin = x1 = (float)(x + 0);
				x2 = xMax = (float)(x + 1);
				zMin = zMax = (float)(z + 1);
				z1 = z2 = (float)(z + 0);
			} else if(metadata == 9) {
				xMin = xMax = (float)(x + 0);
				x1 = x2 = (float)(x + 1);
				zMin = z1 = (float)(z + 0);
				z2 = zMax = (float)(z + 1);
			}
		} else {
			xMin = xMax = (float)(x + 1);
			x1 = x2 = (float)(x + 0);
			zMin = z1 = (float)(z + 1);
			z2 = zMax = (float)(z + 0);
		}

		if(metadata != 2 && metadata != 4) {
			if(metadata == 3 || metadata == 5) {
				++y1;
				++y2;
			}
		} else {
			++yMin;
			++yMax;
		}

		tessellator.addVertexWithUV((double)xMin, (double)yMin, (double)zMin, uHi, vLo);
		tessellator.addVertexWithUV((double)x1, (double)y1, (double)z1, uHi, vHi);
		tessellator.addVertexWithUV((double)x2, (double)y2, (double)z2, uLo, vHi);
		tessellator.addVertexWithUV((double)xMax, (double)yMax, (double)zMax, uLo, vLo);
		tessellator.addVertexWithUV((double)xMax, (double)yMax, (double)zMax, uLo, vLo);
		tessellator.addVertexWithUV((double)x2, (double)y2, (double)z2, uLo, vHi);
		tessellator.addVertexWithUV((double)x1, (double)y1, (double)z1, uHi, vHi);
		tessellator.addVertexWithUV((double)xMin, (double)yMin, (double)zMin, uHi, vLo);
		return true;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
