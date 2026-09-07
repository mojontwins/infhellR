package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;

/**
 * Renders a vines block as thin wall-hugging quads. The four horizontal
 * directions are controlled by metadata bits (1 = south/z+ face, 2 = west/x-
 * face, 4 = north/z- face, 8 = east/x+ face), each drawn as a double-sided
 * quad inset 0.05 into the cell. A top covering quad is additionally drawn
 * when the block above is a solid cube. Vertex colour comes from the block's
 * foliage colour multiplier, and the block brightness is set from the mixed
 * brightness at the cell.
 */
public final class RenderBlockVine implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Emit the wall-hugging quads for each metadata bit, plus a top sheet when the block above is opaque.
		Tessellator tessellator = Tessellator.instance;
		int texId = block.getBlockTextureFromSide(0);
		if(renderBlocks.overrideBlockTexture >= 0) {
			texId = renderBlocks.overrideBlockTexture;
		}

		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		
		int colorValue = block.colorMultiplier(renderBlocks.blockAccess, x, y, z);
		float red = (float)(colorValue >> 16 & 255) / 255.0F;
		float green = (float)(colorValue >> 8 & 255) / 255.0F;
		float blue = (float)(colorValue & 255) / 255.0F;
		
		tessellator.setColorOpaque_F(red, green, blue);
		
		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
		double uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
		double vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);

		double wallOffset = 0.05D;
		
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		
		if ((metadata & 2) != 0) {
			tessellator.addVertexWithUV((double)x + wallOffset, y + 1, z + 1, uLo, vLo);
			tessellator.addVertexWithUV((double)x + wallOffset, y + 0, z + 1, uLo, vHi);
			tessellator.addVertexWithUV((double)x + wallOffset, y + 0, z + 0, uHi, vHi);
			tessellator.addVertexWithUV((double)x + wallOffset, y + 1, z + 0, uHi, vLo);
			tessellator.addVertexWithUV((double)x + wallOffset, y + 1, z + 0, uHi, vLo);
			tessellator.addVertexWithUV((double)x + wallOffset, y + 0, z + 0, uHi, vHi);
			tessellator.addVertexWithUV((double)x + wallOffset, y + 0, z + 1, uLo, vHi);
			tessellator.addVertexWithUV((double)x + wallOffset, y + 1, z + 1, uLo, vLo);
		}

		if ((metadata & 8) != 0) {
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 0, z + 1, uHi, vHi);
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 1, z + 1, uHi, vLo);
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 1, z + 0, uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 0, z + 0, uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 0, z + 0, uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 1, z + 0, uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 1, z + 1, uHi, vLo);
			tessellator.addVertexWithUV((double)(x + 1) - wallOffset, y + 0, z + 1, uHi, vHi);
		}

		if ((metadata & 4) != 0) {
			tessellator.addVertexWithUV(x + 1, y + 0, (double)z + wallOffset, uHi, vHi);
			tessellator.addVertexWithUV(x + 1, y + 1, (double)z + wallOffset, uHi, vLo);
			tessellator.addVertexWithUV(x + 0, y + 1, (double)z + wallOffset, uLo, vLo);
			tessellator.addVertexWithUV(x + 0, y + 0, (double)z + wallOffset, uLo, vHi);
			tessellator.addVertexWithUV(x + 0, y + 0, (double)z + wallOffset, uLo, vHi);
			tessellator.addVertexWithUV(x + 0, y + 1, (double)z + wallOffset, uLo, vLo);
			tessellator.addVertexWithUV(x + 1, y + 1, (double)z + wallOffset, uHi, vLo);
			tessellator.addVertexWithUV(x + 1, y + 0, (double)z + wallOffset, uHi, vHi);
		}

		if ((metadata & 1) != 0) {
			tessellator.addVertexWithUV(x + 1, y + 1, (double)(z + 1) - wallOffset, uLo, vLo);
			tessellator.addVertexWithUV(x + 1, y + 0, (double)(z + 1) - wallOffset, uLo, vHi);
			tessellator.addVertexWithUV(x + 0, y + 0, (double)(z + 1) - wallOffset, uHi, vHi);
			tessellator.addVertexWithUV(x + 0, y + 1, (double)(z + 1) - wallOffset, uHi, vLo);
			tessellator.addVertexWithUV(x + 0, y + 1, (double)(z + 1) - wallOffset, uHi, vLo);
			tessellator.addVertexWithUV(x + 0, y + 0, (double)(z + 1) - wallOffset, uHi, vHi);
			tessellator.addVertexWithUV(x + 1, y + 0, (double)(z + 1) - wallOffset, uLo, vHi);
			tessellator.addVertexWithUV(x + 1, y + 1, (double)(z + 1) - wallOffset, uLo, vLo);
		}
		
		if(renderBlocks.blockAccess.isBlockNormalCube(x, y + 1, z)) {
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 1) - wallOffset, (double)(z + 0), uLo, vLo);
			tessellator.addVertexWithUV((double)(x + 1), (double)(y + 1) - wallOffset, (double)(z + 1), uLo, vHi);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 1) - wallOffset, (double)(z + 1), uHi, vHi);
			tessellator.addVertexWithUV((double)(x + 0), (double)(y + 1) - wallOffset, (double)(z + 0), uHi, vLo);
		}
		
		return true;
	
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
