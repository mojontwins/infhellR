package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
/**
 * Renders a lily pad as a flat, thin quad at water level (inset 0.015625
 * above the block bottom). The quad corners are perturbed per-cell using a
 * small hash of the block coordinates so neighbouring pads vary slightly in
 * shape and rotation. Pads whose hash sets bit 2 additionally render a small
 * flower on top by overriding the texture and reusing renderCrossedSquares.
 */
public final class RenderBlockLilyPad implements BlockRenderHandler {
	@Override
public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Emit the per-cell-perturbed lily-pad quad; roughly half the hashes also draw the flower cross on top.
		Tessellator tessellator = Tessellator.instance;
		
		int i = block.blockIndexInTexture;

		AtlasTexel.calc(i, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		
		float inset = 0.015625F;
		
		float uLo = TexelScale.u(TextureAtlas.TERRAIN, (float) tileX);
		float uHi = TexelScale.u(TextureAtlas.TERRAIN, (float) tileX + TextureAtlas.TERRAIN.tileSpan);
		float vLo = TexelScale.v(TextureAtlas.TERRAIN, (float) tileY);
		float vHi = TexelScale.v(TextureAtlas.TERRAIN, (float) tileY + TextureAtlas.TERRAIN.tileSpan);
		
		long hashSeed = (long)(x * 0x2fc20f) ^ (long)z * 0x6ebfff5L ^ (long)y;
		hashSeed = hashSeed * hashSeed * 0x285b825L + hashSeed * 11L;
		int flowerNo = (int)(hashSeed >> 16 & 7L);
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));

		
		float midX = (float)x + 0.5F;
		float midZ = (float)z + 0.5F;
		float offA = (float)(flowerNo & 1) * 0.5F * (float)(1 - (flowerNo & 2));
		float offB = (float)(flowerNo + 1 & 1) * 0.5F * (float)(1 - ((flowerNo + 1) & 2));
		
		tessellator.addVertexWithUV((midX + offA) - offB, (float)y + inset, midZ + offA + offB, uLo, vLo);
		tessellator.addVertexWithUV(midX + offA + offB, (float)y + inset, (midZ - offA) + offB, uHi, vLo);
		tessellator.addVertexWithUV((midX - offA) + offB, (float)y + inset, midZ - offA - offB, uHi, vHi);
		tessellator.addVertexWithUV(midX - offA - offB, (float)y + inset, (midZ + offA) - offB, uLo, vHi);
		tessellator.addVertexWithUV(midX - offA - offB, (float)y + inset, (midZ + offA) - offB, uLo, vHi);
		tessellator.addVertexWithUV((midX - offA) + offB, (float)y + inset, midZ - offA - offB, uHi, vHi);
		tessellator.addVertexWithUV(midX + offA + offB, (float)y + inset, (midZ - offA) + offB, uHi, vLo);
		tessellator.addVertexWithUV((midX + offA) - offB, (float)y + inset, midZ + offA + offB, uLo, vLo);
		
		// Render a lily flower on top
		if((flowerNo & 4) != 0) {
			renderBlocks.overrideBlockTexture = 12 * 16 + 2;
			RenderBlockUtil.renderCrossedSquares(renderBlocks, block, x, y, z);
			renderBlocks.overrideBlockTexture = -1;
		}
		
		return true;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
