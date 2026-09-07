package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.Tessellator;

/**
	 * Renders a hanging chain as two crossing diagonal slats (an "X" when viewed from above): Square 1 is
	 * drawn diagonally from corner (z1,z1) to (z2,z2) and Square 2 is drawn on the other diagonal, each
	 * slightly inset from a full tile (0.45F from centre) so the chain reads as a thin criss-cross rod.
	 * The full chain texture tile is wrapped over each slat; brightness is set from the block and the
	 * override texture (if any) is honoured.
	 */
	public final class RenderBlockChain implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		Tessellator tessellator = Tessellator.instance;
		
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1, 1, 1);
		
		int texId = block.getBlockTextureFromSideAndMetadata(0, renderBlocks.blockAccess.getBlockMetadata(x, y, z));
		if(renderBlocks.overrideBlockTexture >= 0) {
			texId = renderBlocks.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double u1 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
		double u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
		double v1 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
		double v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
		double x1 = (double)x + 0.5D - (double)0.45F;
		double x2 = (double)x + 0.5D + (double)0.45F;
		double z1 = (double)z + 0.5D - (double)0.45F;
		double z2 = (double)z + 0.5D + (double)0.45F;
		double yy = (double)y;
		
		// Square 1
		
		tessellator.addVertexWithUV(x1, yy + 1.0D, z1,  u1, v2);
		tessellator.addVertexWithUV(x1, yy + 0.0D, z1,  u1, v1);
		tessellator.addVertexWithUV(x2, yy + 0.0D, z2,  u2, v1);
		tessellator.addVertexWithUV(x2, yy + 1.0D, z2,  u2, v2)
		;
		tessellator.addVertexWithUV(x2, yy + 1.0D, z2,  u1, v2);
		tessellator.addVertexWithUV(x2, yy + 0.0D, z2,  u1, v1);
		tessellator.addVertexWithUV(x1, yy + 0.0D, z1,  u2, v1);
		tessellator.addVertexWithUV(x1, yy + 1.0D, z1,  u2, v2);
		
		// Square 2 [upside down]
		
		tessellator.addVertexWithUV(x1, yy + 1.0D, z2,  u1, v1);
		tessellator.addVertexWithUV(x1, yy + 0.0D, z2,  u1, v2);
		tessellator.addVertexWithUV(x2, yy + 0.0D, z1,  u2, v2);
		tessellator.addVertexWithUV(x2, yy + 1.0D, z1,  u2, v1);
		
		tessellator.addVertexWithUV(x2, yy + 1.0D, z1,  u1, v1);
		tessellator.addVertexWithUV(x2, yy + 0.0D, z1,  u1, v2);
		tessellator.addVertexWithUV(x1, yy + 0.0D, z2,  u2, v2);
		tessellator.addVertexWithUV(x1, yy + 1.0D, z2,  u2, v1);
	
		return true;
	}

}
