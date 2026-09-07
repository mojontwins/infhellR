package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.Tessellator;

/**
	 * Renders the barbed-wire block as two diagonal X-shaped crossed strands (half 1 and half 2)
	 * meeting at the block centre. Each strand spans from one corner of the block to the opposing
	 * vertical edge, and the face orientation depends on the block metadata (2/3 = Z axis, 4/5 = X axis).
	 * The wire texture tile is sampled to only 8 pixels wide (the <code>uh</code> mid-UV) so the strand
	 * appears narrower than a full tile. Quirks: both the inner (toward centre) and outer faces of each
	 * strand are drawn so the wire reads from either side, and a commented-out diffuse-shading block is
	 * left in place (brightness is applied via {@link Tessellator#setBrightness} instead).
	 */
	public final class RenderBlockBarbedWire implements BlockRenderHandler {
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		Tessellator tessellator = Tessellator.instance;		
		
		/*
		float blockBrightness = block.getBlockBrightness(renderBlocks.blockAccess, x, y, z);
		tessellator.setColorOpaque_F(blockBrightness, blockBrightness, blockBrightness);
		*/
		
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		
		// Resolve the wire texture tile (overridable), then expand it into a 0..1 UV space.
		int textureIndex = block.getBlockTextureFromSide(2);
		if(renderBlocks.overrideBlockTexture >= 0) {
			textureIndex = renderBlocks.overrideBlockTexture;
		}
		
		AtlasTexel.calc(textureIndex, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		
		double u1 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
		double uh = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + (float)TextureAtlas.TILE * 0.5F);
		double u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
		
		double v1 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
		double v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);		
		
		int meta = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		
		// metadata 2/3: strands run across the Z axis (drawn in the X plane); 4/5: strands run across the X axis (Z plane).
		if (meta == 2 || meta == 3) {
			// half 1, side 1, add vertexes CCW
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 0.0F, (double)z + 1.0F, uh, v2);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 1.0F, (double)z + 0.5F, u1, v1);
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 0.0F, (double)z + 1.0F, u1, v2);
		
			// half 1, side 2, add vertexes CW
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 0.0F, (double)z + 1.0F, u1, v2);
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 1.0F, (double)z + 0.5F, u1, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 0.0F, (double)z + 1.0F, uh, v2);			

			// half 2, side 1, add vertexes CCW
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 0.0F, (double)z + 0.0F, u2, v2);
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 1.0F, (double)z + 0.5F, u2, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 0.0F, (double)z + 0.0F, uh, v2);
					
			// half 2, side 2, add vertexes CW
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 0.0F, (double)z + 0.0F, uh, v2);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 1.0F, (double)z + 0.5F, u2, v1);
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 0.0F, (double)z + 0.0F, u2, v2);
		} else if (meta == 4 || meta == 5) {
			
			// half 1, side 1, add vertexes CCW
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 0.0F, (double)z + 0.5F, uh, v2);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.0F, u1, v1);
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 0.0F, (double)z + 0.0F, u1, v2);
			
			// half 1, side 2, add vertexes CW
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 0.0F, (double)z + 0.0F, u1, v2);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.0F, u1, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 1.0F, (double)y + 0.0F, (double)z + 0.5F, uh, v2);		
			
			// half 2, side 1, add vertexes CCW
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 0.0F, (double)z + 1.0F, u2, v2);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 1.0F, u2, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 0.0F, (double)z + 0.5F, uh, v2);
			
			// half 2, side 2, add vertexes CW
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 0.0F, (double)z + 0.5F, uh, v2);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 0.5F, uh, v1);
			tessellator.addVertexWithUV((double)x + 0.5F, (double)y + 1.0F, (double)z + 1.0F, u2, v1);
			tessellator.addVertexWithUV((double)x + 0.0F, (double)y + 0.0F, (double)z + 1.0F, u2, v2);		
			
		}
		
		return true;
	}

}
