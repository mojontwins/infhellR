package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;

/**
 * Renders a cobblestone wall block as thin, full-bright face plates using the
 * side-0 texture (honouring the override texture). Metadata picks the axis:
 * 4/5 draw two plates at x+0.49/x+0.51, 2/3 draw two plates at z+0.49 (the
 * second plate wound in reverse to stay double-sided). The renderer runs on
 * the mixed-brightness of the block with a fixed white colour.
 */
public final class RenderBlockWall implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Draw thin wall plates on the horizontal faces selected by metadata (4/5 = x-axis, 2/3 = z-axis).
		Tessellator tessellator = Tessellator.instance;
		int texId = block.getBlockTextureFromSide(0);
		if(renderBlocks.overrideBlockTexture >= 0) {
			texId = renderBlocks.overrideBlockTexture;
		}

		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		
		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
		double uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
		double vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
		int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		
		float offset1 = 0.49F; 
		float offset2 = 0.51F;
		
		
		if(meta == 4 || meta == 5) {
			tessellator.addVertexWithUV((double)((float)x + offset1), (double)(y + 1), (double)(z + 1), uLo, vLo);
			tessellator.addVertexWithUV((double)((float)x + offset1), (double)(y + 0), (double)(z + 1), uLo, vHi);
			tessellator.addVertexWithUV((double)((float)x + offset1), (double)(y + 0), (double)(z + 0), uHi, vHi);
			tessellator.addVertexWithUV((double)((float)x + offset1), (double)(y + 1), (double)(z + 0), uHi, vLo);
			
			tessellator.addVertexWithUV((double)((float)x + offset2), (double)(y + 1), (double)(z + 0), uHi, vLo);
			tessellator.addVertexWithUV((double)((float)x + offset2), (double)(y + 0), (double)(z + 0), uHi, vHi);
			tessellator.addVertexWithUV((double)((float)x + offset2), (double)(y + 0), (double)(z + 1), uLo, vHi);
			tessellator.addVertexWithUV((double)((float)x + offset2), (double)(y + 1), (double)(z + 1), uLo, vLo);
		}

		if(meta == 2 || meta == 3) {
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)((float)z + offset1), uHi, vHi);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 1), (double)((float)z + offset1), uHi, vLo);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 1), (double)((float)z + offset1), uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)((float)z + offset1), uLo, vHi);
			
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)((float)z + offset1), uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 1), (double)((float)z + offset1), uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 1), (double)((float)z + offset1), uHi, vLo);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)((float)z + offset1), uHi, vHi);
		}

		return true;
	}

}