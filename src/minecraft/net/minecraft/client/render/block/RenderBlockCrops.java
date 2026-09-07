package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
/**
 * Renders crops (wheat etc.) as two thin crossed X quads, one aligned on the
 * X axis (0.25 thick) and one on the Z axis (0.25 thick), spanning the block's
 * full height. Quirk: the world render sinks the geometry 1/16 below the block
 * surface, while the inventory render draws it centered at (-0.5, -0.5, -0.5).
 */
public final class RenderBlockCrops implements BlockRenderHandler {
	@Override
	// Draws the crossed X quads in-world, shifted down 1/16 of a block so the crop
	// appears planted slightly below the surface.
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		renderBlockCropsImpl(renderBlocks, block, renderBlocks.blockAccess.getBlockMetadata(x, y, z), (double)x, (double)((float)y - 0.0625F), (double)z);
		return true;
	}

	@Override
	// Emits the crossed X quads as a single quad batch centered at the origin for
	// the inventory/drop rendering.
	public void renderBlockOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		RenderBlockUtil.applyInventoryColor(renderBlocks, block, metadata, brightness);
		renderBlockCropsImpl(renderBlocks, block, metadata, -0.5D, -0.5D, -0.5D);
		tessellator.draw();
	}

	// Builds two perpendicular 0.25-thick X quads spanning the block height,
	// each drawn as two strips of four vertices using the full crop texture tile.
	public static void renderBlockCropsImpl(RenderBlocks renderBlocks, Block block, int metadata, double x, double y, double z) {
		Tessellator tessellator = Tessellator.instance;
		int textureId = block.getBlockTextureFromSideAndMetadata(0, metadata);
		if(renderBlocks.overrideBlockTexture >= 0) {
			textureId = renderBlocks.overrideBlockTexture;
		}

		AtlasTexel.calc(textureId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
		double uHi = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
		double vHi = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
		double xMin = x + 0.5D - 0.25D;
		double x1 = x + 0.5D + 0.25D;
		double zMin = z + 0.5D - 0.5D;
		double z1 = z + 0.5D + 0.5D;
		tessellator.addVertexWithUV(xMin, y + 1.0D, zMin, uLo, vLo);
		tessellator.addVertexWithUV(xMin, y + 0.0D, zMin, uLo, vHi);
		tessellator.addVertexWithUV(xMin, y + 0.0D, z1, uHi, vHi);
		tessellator.addVertexWithUV(xMin, y + 1.0D, z1, uHi, vLo);
		tessellator.addVertexWithUV(xMin, y + 1.0D, z1, uLo, vLo);
		tessellator.addVertexWithUV(xMin, y + 0.0D, z1, uLo, vHi);
		tessellator.addVertexWithUV(xMin, y + 0.0D, zMin, uHi, vHi);
		tessellator.addVertexWithUV(xMin, y + 1.0D, zMin, uHi, vLo);
		tessellator.addVertexWithUV(x1, y + 1.0D, z1, uLo, vLo);
		tessellator.addVertexWithUV(x1, y + 0.0D, z1, uLo, vHi);
		tessellator.addVertexWithUV(x1, y + 0.0D, zMin, uHi, vHi);
		tessellator.addVertexWithUV(x1, y + 1.0D, zMin, uHi, vLo);
		tessellator.addVertexWithUV(x1, y + 1.0D, zMin, uLo, vLo);
		tessellator.addVertexWithUV(x1, y + 0.0D, zMin, uLo, vHi);
		tessellator.addVertexWithUV(x1, y + 0.0D, z1, uHi, vHi);
		tessellator.addVertexWithUV(x1, y + 1.0D, z1, uHi, vLo);
		xMin = x + 0.5D - 0.5D;
		x1 = x + 0.5D + 0.5D;
		zMin = z + 0.5D - 0.25D;
		z1 = z + 0.5D + 0.25D;
		tessellator.addVertexWithUV(xMin, y + 1.0D, zMin, uLo, vLo);
		tessellator.addVertexWithUV(xMin, y + 0.0D, zMin, uLo, vHi);
		tessellator.addVertexWithUV(x1, y + 0.0D, zMin, uHi, vHi);
		tessellator.addVertexWithUV(x1, y + 1.0D, zMin, uHi, vLo);
		tessellator.addVertexWithUV(x1, y + 1.0D, zMin, uLo, vLo);
		tessellator.addVertexWithUV(x1, y + 0.0D, zMin, uLo, vHi);
		tessellator.addVertexWithUV(xMin, y + 0.0D, zMin, uHi, vHi);
		tessellator.addVertexWithUV(xMin, y + 1.0D, zMin, uHi, vLo);
		tessellator.addVertexWithUV(x1, y + 1.0D, z1, uLo, vLo);
		tessellator.addVertexWithUV(x1, y + 0.0D, z1, uLo, vHi);
		tessellator.addVertexWithUV(xMin, y + 0.0D, z1, uHi, vHi);
		tessellator.addVertexWithUV(xMin, y + 1.0D, z1, uHi, vLo);
		tessellator.addVertexWithUV(xMin, y + 1.0D, z1, uLo, vLo);
		tessellator.addVertexWithUV(xMin, y + 0.0D, z1, uLo, vHi);
		tessellator.addVertexWithUV(x1, y + 0.0D, z1, uHi, vHi);
		tessellator.addVertexWithUV(x1, y + 1.0D, z1, uHi, vLo);
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
