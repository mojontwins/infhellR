package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.Tessellator;

/**
	 * Renders a "chipped wood" block. When the low metadata bit is clear it draws a full standard cube.
	 * Otherwise it draws a half-height block whose six faces are emitted manually (so each face can get its
	 * own directional light value), with the two end faces sampled from the "ends" texture (side 1) and the
	 * four long faces from the "sides" texture (side 0). Metadata selects the long axis: bit 3 set = runs
	 * North/South, otherwise it runs East/West.
	 */
	public final class RenderBlockChippedWood implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Shared buffer for face bounds and per-face UV corners (reassigned before each face).
		float x1, y1, z1, x2, y2, z2;
		float u1, v1, u2, v2;
		
		Tessellator tessellator = Tessellator.instance;
		
		int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		
		if((meta & 4) == 0) {
			block.setBlockBounds((1 / 16.0F), 0.0F, (1 / 16.0F), 15 * (1 / 16.0F), 1.0F, 15 * (1 / 16.0F));
			RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			return true;
		} else if((meta & 8) != 0) { 
			// facing North/South: the long dimension is along Z, the half-block cross-section is in X/Y.
			
			// Texture #1 (ends)
			int ti_1 = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 1); 	// CUSTOM : side = 1 means "ends"
			if(renderBlocks.overrideBlockTexture >= 0) ti_1 = renderBlocks.overrideBlockTexture;
			
			AtlasTexel.calc(ti_1, TextureAtlas.TERRAIN);
			float t1_u = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u);
			float t1_v = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v);

			// Texture #2 (sides)
			int ti_2 = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 0); 	// CUSTOM : side = 1 means "sides"
			if(renderBlocks.overrideBlockTexture >= 0) ti_2 = renderBlocks.overrideBlockTexture;
			
			AtlasTexel.calc(ti_2, TextureAtlas.TERRAIN);
			float t2_u = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u);
			float t2_v = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v);
			
			x1 = x + 0.0000F;
			y1 = y + 0.0625F;
			z1 = z + 0.0625F;
			x2 = x + 1.0000F;
			y2 = y + 0.9375F;
			z2 = z + 0.9375F;

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y + 1, z, 1.0F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
			tessellator.addVertexWithUV(x2, y2, z1, u2, v1);
			tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
			tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y - 1, z, 0.5F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x1, y1, z2, u1, v2);
			tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
			tessellator.addVertexWithUV(x2, y1, z1, u2, v1);
			tessellator.addVertexWithUV(x2, y1, z2, u1, v1);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y, z - 1, 0.8F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x1, y2, z1, u1, v2);
			tessellator.addVertexWithUV(x2, y2, z1, u1, v1);
			tessellator.addVertexWithUV(x2, y1, z1, u2, v1);
			tessellator.addVertexWithUV(x1, y1, z1, u2, v2);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y, z + 1, 0.8F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
			tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
			tessellator.addVertexWithUV(x2, y1, z2, u2, v2);
			tessellator.addVertexWithUV(x2, y2, z2, u1, v2);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x + 1, y, z, 0.6F);
			u1 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 1.0F);
			v1 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 1.0F);
			u2 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 15.0F);
			v2 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 15.0F);
			tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
			tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
			tessellator.addVertexWithUV(x2, y1, z2, u2, v1);
			tessellator.addVertexWithUV(x2, y1, z1, u2, v2);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x - 1, y, z, 0.6F);
			u1 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 1.0F);
			v1 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 1.0F);
			u2 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 15.0F);
			v2 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 15.0F);
			tessellator.addVertexWithUV(x1, y2, z1, u1, v1);
			tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
			tessellator.addVertexWithUV(x1, y1, z2, u2, v2);
			tessellator.addVertexWithUV(x1, y2, z2, u1, v2);
			
			return true;
		} else {
			// facing East/West: the long dimension is along X, the half-block cross-section is in Y/Z.
			
			// Texture #1 (ends)
			int ti_1 = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 1); 	// CUSTOM : side = 1 means "ends"
			if(renderBlocks.overrideBlockTexture >= 0) ti_1 = renderBlocks.overrideBlockTexture;
			AtlasTexel.calc(ti_1, TextureAtlas.TERRAIN);
			float t1_u = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u);
			float t1_v = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v);

			// Texture #2 (sides)
			int ti_2 = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 0); 	// CUSTOM : side = 1 means "sides"
			if(renderBlocks.overrideBlockTexture >= 0) ti_2 = renderBlocks.overrideBlockTexture;
			AtlasTexel.calc(ti_2, TextureAtlas.TERRAIN);
			float t2_u = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u);
			float t2_v = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v);
			
			x1 = x + 0.0625F;
			y1 = y + 0.0625F;
			z1 = z + 0.0000F;
			x2 = x + 0.9375F;
			y2 = y + 0.9375F;
			z2 = z + 1.0000F;

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y + 1, z, 1.0F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
			tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
			tessellator.addVertexWithUV(x1, y2, z1, u2, v2);
			tessellator.addVertexWithUV(x1, y2, z2, u2, v1);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y - 1, z, 0.5F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x1, y1, z2, u2, v1);
			tessellator.addVertexWithUV(x1, y1, z1, u2, v2);
			tessellator.addVertexWithUV(x2, y1, z1, u1, v2);
			tessellator.addVertexWithUV(x2, y1, z2, u1, v1);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x + 1, y, z, 0.6F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x1, y2, z1, u1, v1);
			tessellator.addVertexWithUV(x1, y1, z1, u2, v1);
			tessellator.addVertexWithUV(x1, y1, z2, u2, v2);
			tessellator.addVertexWithUV(x1, y2, z2, u1, v2);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x - 1, y, z, 0.6F);
			u1 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, 0.0F);
			v1 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, 0.0F);
			u2 = t2_u + TexelScale.u(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			v2 = t2_v + TexelScale.v(TextureAtlas.TERRAIN, (float)TextureAtlas.TILE);
			tessellator.addVertexWithUV(x2, y2, z1, u1, v2);
			tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
			tessellator.addVertexWithUV(x2, y1, z2, u2, v1);
			tessellator.addVertexWithUV(x2, y1, z1, u2, v2);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y, z - 1, 0.8F);
			u1 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 1.0F);
			v1 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 1.0F);
			u2 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 15.0F);
			v2 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 15.0F);
			tessellator.addVertexWithUV(x1, y2, z1, u2, v1);
			tessellator.addVertexWithUV(x2, y2, z1, u1, v1);
			tessellator.addVertexWithUV(x2, y1, z1, u1, v2);
			tessellator.addVertexWithUV(x1, y1, z1, u2, v2);

			RenderBlockUtil.setLightValue(tessellator, renderBlocks.blockAccess, block, x, y, z + 1, 0.8F);
			u1 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 1.0F);
			v1 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 1.0F);
			u2 = t1_u + TexelScale.u(TextureAtlas.TERRAIN, 15.0F);
			v2 = t1_v + TexelScale.v(TextureAtlas.TERRAIN, 15.0F);
			tessellator.addVertexWithUV(x1, y2, z2, u1, v1);
			tessellator.addVertexWithUV(x1, y1, z2, u1, v2);
			tessellator.addVertexWithUV(x2, y1, z2, u2, v2);
			tessellator.addVertexWithUV(x2, y2, z2, u2, v1);
			
			return true;
		}

	}


	@Override
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		RenderBlockUtil.renderCubeOnInventory(renderBlocks, block, metadata, brightness);
	}
}
