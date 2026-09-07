package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.AtlasUV;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;

/**
 * Renderer for torch blocks. The torch is a thin rod with a flame cap,
 * drawn by the cross-handler static {@link #renderTorchAtAngle}. The block's
 * metadata selects the wall-facing lean (1-4 lean into a side wall) or a
 * floor-mounted straight torch (0/other), with wall torches shifted and
 * lifted off the block floor accordingly.
 */
public final class RenderBlockTorch implements BlockRenderHandler {
	@Override
	/** Positions the torch based on wall-facing metadata and delegates to renderTorchAtAngle. */
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		// Lean magnitude and the resulting centre offset for wall torches.
		double lean = (double)0.4F;
		double half = 0.5D - lean;
		double liftY = (double)0.2F;
		if(metadata == 1) {
			RenderBlockTorch.renderTorchAtAngle(renderBlocks, block, (double)x - half, (double)y + liftY, (double)z, -lean, 0.0D);
		} else if(metadata == 2) {
			RenderBlockTorch.renderTorchAtAngle(renderBlocks, block, (double)x + half, (double)y + liftY, (double)z, lean, 0.0D);
		} else if(metadata == 3) {
			RenderBlockTorch.renderTorchAtAngle(renderBlocks, block, (double)x, (double)y + liftY, (double)z - half, 0.0D, -lean);
		} else if(metadata == 4) {
			RenderBlockTorch.renderTorchAtAngle(renderBlocks, block, (double)x, (double)y + liftY, (double)z + half, 0.0D, lean);
		} else {
			RenderBlockTorch.renderTorchAtAngle(renderBlocks, block, (double)x, (double)y, (double)z, 0.0D, 0.0D);
		}

		return true;
	}

	@Override
	/** Renders a standalone upright torch centered on an inventory slot. */
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		RenderBlockUtil.applyInventoryColor(renderBlocks, block, metadata, brightness);
		renderTorchAtAngle(renderBlocks, block, -0.5D, -0.5D, -0.5D, 0.0D, 0.0D);
		tessellator.draw();
	}

	/** Builds the torch quad geometry: a square flame cap plus four leaning side panels. */
	public static void renderTorchAtAngle(RenderBlocks renderBlocks, Block block, double x, double y, double z, double leanX, double leanZ) {
		Tessellator tessellator = Tessellator.instance;
		int textureId = block.getBlockTextureFromSide(0);
		if(renderBlocks.overrideBlockTexture >= 0) {
			textureId = renderBlocks.overrideBlockTexture;
		}

		// Decode the block texture into tile coordinates, then into UVs.
		AtlasTexel.calc(textureId, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		float uLo0 = TexelScale.u(TextureAtlas.TERRAIN, (float)tileX);
		float uHi0 = TexelScale.u(TextureAtlas.TERRAIN, (float)tileX + TextureAtlas.TERRAIN.tileSpan);
		float vLo0 = TexelScale.v(TextureAtlas.TERRAIN, (float)tileY);
		float vHi0 = TexelScale.v(TextureAtlas.TERRAIN, (float)tileY + TextureAtlas.TERRAIN.tileSpan);
		// Inner UVs carve out the small flame-cap sprite from the tile.
		AtlasUV.calcPixels(tileX + 7, tileY + 6, 2.0F, 2.0F, TextureAtlas.TERRAIN);
		double uInnerLo = AtlasUV.u1;
		double vInnerLo = AtlasUV.v1;
		double uInnerHi = AtlasUV.u2;
		double vInnerHi = AtlasUV.v2;
		x += 0.5D;
		z += 0.5D;
		double xLeft = x - 0.5D;
		double xRight = x + 0.5D;
		double zLeft = z - 0.5D;
		double zRight = z + 0.5D;
		double halfW = 0.0625D;
		double length = 0.625D;
		// Flame cap quad (2px square) at the top of the rod.
		tessellator.addVertexWithUV(x + leanX * (1.0D - length) - halfW, y + length, z + leanZ * (1.0D - length) - halfW, uInnerLo, vInnerLo);
		tessellator.addVertexWithUV(x + leanX * (1.0D - length) - halfW, y + length, z + leanZ * (1.0D - length) + halfW, uInnerLo, vInnerHi);
		tessellator.addVertexWithUV(x + leanX * (1.0D - length) + halfW, y + length, z + leanZ * (1.0D - length) + halfW, uInnerHi, vInnerHi);
		tessellator.addVertexWithUV(x + leanX * (1.0D - length) + halfW, y + length, z + leanZ * (1.0D - length) - halfW, uInnerHi, vInnerLo);
		tessellator.addVertexWithUV(x - halfW, y + 1.0D, zLeft, (double)uLo0, (double)vLo0);
		tessellator.addVertexWithUV(x - halfW + leanX, y + 0.0D, zLeft + leanZ, (double)uLo0, (double)vHi0);
		tessellator.addVertexWithUV(x - halfW + leanX, y + 0.0D, zRight + leanZ, (double)uHi0, (double)vHi0);
		tessellator.addVertexWithUV(x - halfW, y + 1.0D, zRight, (double)uHi0, (double)vLo0);
		tessellator.addVertexWithUV(x + halfW, y + 1.0D, zRight, (double)uLo0, (double)vLo0);
		tessellator.addVertexWithUV(x + leanX + halfW, y + 0.0D, zRight + leanZ, (double)uLo0, (double)vHi0);
		tessellator.addVertexWithUV(x + leanX + halfW, y + 0.0D, zLeft + leanZ, (double)uHi0, (double)vHi0);
		tessellator.addVertexWithUV(x + halfW, y + 1.0D, zLeft, (double)uHi0, (double)vLo0);
		tessellator.addVertexWithUV(xLeft, y + 1.0D, z + halfW, (double)uLo0, (double)vLo0);
		tessellator.addVertexWithUV(xLeft + leanX, y + 0.0D, z + halfW + leanZ, (double)uLo0, (double)vHi0);
		tessellator.addVertexWithUV(xRight + leanX, y + 0.0D, z + halfW + leanZ, (double)uHi0, (double)vHi0);
		tessellator.addVertexWithUV(xRight, y + 1.0D, z + halfW, (double)uHi0, (double)vLo0);
		tessellator.addVertexWithUV(xRight, y + 1.0D, z - halfW, (double)uLo0, (double)vLo0);
		tessellator.addVertexWithUV(xRight + leanX, y + 0.0D, z - halfW + leanZ, (double)uLo0, (double)vHi0);
		tessellator.addVertexWithUV(xLeft + leanX, y + 0.0D, z - halfW + leanZ, (double)uHi0, (double)vHi0);
		tessellator.addVertexWithUV(xLeft, y + 1.0D, z - halfW, (double)uHi0, (double)vLo0);
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
