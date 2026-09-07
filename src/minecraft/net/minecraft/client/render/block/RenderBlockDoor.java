package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.world.block.Block;
/**
 * Renderer for door blocks. Draws the door as a set of six faces (bottom,
 * top, east, west, north, south), each shaded with the fixed Beta door
 * brightness levels. Multi-texture doors select their side texture via the
 * negative-texture flip convention: when the tile texture id is negative, the
 * face is drawn after toggling {@link RenderBlocks#flipTexture}.
 */
public final class RenderBlockDoor implements BlockRenderHandler {
	@Override
	/** Draws all six faces of the door block with per-face shading and texture flipping. */
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		Tessellator tessellator = Tessellator.instance;
		boolean hasRendered = false;
		float shadeBottom = 0.5F;
		float shadeTop = 1.0F;
		float shadeXZ = 0.8F;
		float shadeNS = 0.6F;
		int brightness = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z);
		tessellator.setBrightness(block.minY > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z));
		tessellator.setColorOpaque_F(shadeBottom, shadeBottom, shadeBottom);
		renderBlocks.renderBottomFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 0));
		hasRendered = true;
		tessellator.setBrightness(block.maxY < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z));
		tessellator.setColorOpaque_F(shadeTop, shadeTop, shadeTop);
		renderBlocks.renderTopFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 1));
		hasRendered = true;
		tessellator.setBrightness(block.minZ > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1));
		tessellator.setColorOpaque_F(shadeXZ, shadeXZ, shadeXZ);
		int texId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 2);
		if(texId < 0) {
			renderBlocks.flipTexture = true;
			texId = -texId;
		}

		renderBlocks.renderEastFace(block, (double)x, (double)y, (double)z, texId);
		hasRendered = true;
		renderBlocks.flipTexture = false;
		tessellator.setBrightness(block.maxZ < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1));
		tessellator.setColorOpaque_F(shadeXZ, shadeXZ, shadeXZ);
		texId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 3);
		if(texId < 0) {
			renderBlocks.flipTexture = true;
			texId = -texId;
		}

		renderBlocks.renderWestFace(block, (double)x, (double)y, (double)z, texId);
		hasRendered = true;
		renderBlocks.flipTexture = false;
		tessellator.setBrightness(block.minX > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z));
		tessellator.setColorOpaque_F(shadeNS, shadeNS, shadeNS);
		texId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 4);
		if(texId < 0) {
			renderBlocks.flipTexture = true;
			texId = -texId;
		}

		renderBlocks.renderNorthFace(block, (double)x, (double)y, (double)z, texId);
		hasRendered = true;
		renderBlocks.flipTexture = false;
		tessellator.setBrightness(block.maxX < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z));
		tessellator.setColorOpaque_F(shadeNS, shadeNS, shadeNS);
		texId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 5);
		if(texId < 0) {
			renderBlocks.flipTexture = true;
			texId = -texId;
		}

		renderBlocks.renderSouthFace(block, (double)x, (double)y, (double)z, texId);
		hasRendered = true;
		renderBlocks.flipTexture = false;
return hasRendered;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
