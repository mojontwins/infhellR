package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
/**
 * Renders a Beta 1.7.3 ladder block: a single thin slat quad (0.05F thick)
 * protruding from one of the four horizontal walls selected by block metadata
 * (5 = west/x- face, 4 = east/x+ face, 3 = north/z- face, 2 = south/z+ face).
 * Drawn full-bright (colour 1.0F) with the side-0 texture, honouring the
 * renderer's override texture.
 */
public final class RenderBlockLadder implements BlockRenderHandler {
	@Override
public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Draw the slat quad for whichever horizontal face the metadata selects.
		Tessellator tessellator = Tessellator.instance;
		int texId = block.getBlockTextureFromSide(0);
		if(renderBlocks.overrideBlockTexture >= 0) {
			texId = renderBlocks.overrideBlockTexture;
		}

		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		float brightnessFactor = 1.0F;
		tessellator.setColorOpaque_F(brightnessFactor, brightnessFactor, brightnessFactor);
		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		double uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileX);
		double uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileX + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileY);
		double vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileY + TextureAtlas.TERRAIN.tileSpan);
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		float faceOffset = 0.0F;
		float slat = 0.05F;
		if(metadata == 5) {
			tessellator.addVertexWithUV((double)((float)x + slat), (double)((float)(y + 1) + faceOffset), (double)((float)(z + 1) + faceOffset), uLo, vLo);
			tessellator.addVertexWithUV((double)((float)x + slat), (double)((float)(y + 0) - faceOffset), (double)((float)(z + 1) + faceOffset), uLo, vHi);
			tessellator.addVertexWithUV((double)((float)x + slat), (double)((float)(y + 0) - faceOffset), (double)((float)(z + 0) - faceOffset), uHi, vHi);
			tessellator.addVertexWithUV((double)((float)x + slat), (double)((float)(y + 1) + faceOffset), (double)((float)(z + 0) - faceOffset), uHi, vLo);
		}

		if(metadata == 4) {
			tessellator.addVertexWithUV((double)((float)(x + 1) - slat), (double)((float)(y + 0) - faceOffset), (double)((float)(z + 1) + faceOffset), uHi, vHi);
			tessellator.addVertexWithUV((double)((float)(x + 1) - slat), (double)((float)(y + 1) + faceOffset), (double)((float)(z + 1) + faceOffset), uHi, vLo);
			tessellator.addVertexWithUV((double)((float)(x + 1) - slat), (double)((float)(y + 1) + faceOffset), (double)((float)(z + 0) - faceOffset), uLo, vLo);
			tessellator.addVertexWithUV((double)((float)(x + 1) - slat), (double)((float)(y + 0) - faceOffset), (double)((float)(z + 0) - faceOffset), uLo, vHi);
		}

		if(metadata == 3) {
			tessellator.addVertexWithUV((double)((float)(x + 1) + faceOffset), (double)((float)(y + 0) - faceOffset), (double)((float)z + slat), uHi, vHi);
			tessellator.addVertexWithUV((double)((float)(x + 1) + faceOffset), (double)((float)(y + 1) + faceOffset), (double)((float)z + slat), uHi, vLo);
			tessellator.addVertexWithUV((double)((float)(x + 0) - faceOffset), (double)((float)(y + 1) + faceOffset), (double)((float)z + slat), uLo, vLo);
			tessellator.addVertexWithUV((double)((float)(x + 0) - faceOffset), (double)((float)(y + 0) - faceOffset), (double)((float)z + slat), uLo, vHi);
		}

		if(metadata == 2) {
			tessellator.addVertexWithUV((double)((float)(x + 1) + faceOffset), (double)((float)(y + 1) + faceOffset), (double)((float)(z + 1) - slat), uLo, vLo);
			tessellator.addVertexWithUV((double)((float)(x + 1) + faceOffset), (double)((float)(y + 0) - faceOffset), (double)((float)(z + 1) - slat), uLo, vHi);
			tessellator.addVertexWithUV((double)((float)(x + 0) - faceOffset), (double)((float)(y + 0) - faceOffset), (double)((float)(z + 1) - slat), uHi, vHi);
			tessellator.addVertexWithUV((double)((float)(x + 0) - faceOffset), (double)((float)(y + 1) + faceOffset), (double)((float)(z + 1) - slat), uHi, vLo);
		}

		return true;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
