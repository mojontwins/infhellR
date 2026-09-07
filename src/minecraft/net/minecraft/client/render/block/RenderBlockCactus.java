package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.EntityRenderer;

/**
 * Renders the cactus block. Each of the six faces is drawn with the standard shade
 * multiplier (bottom 0.5, top 1.0, X/Z sides 0.8, N/S sides 0.6) applied to the
 * block's fetched color, and the X/Z and N/S side faces are inset by one pixel
 * (0.0625) so the flat geometry appears as an eight-spiked cactus.
 */
public final class RenderBlockCactus implements BlockRenderHandler {
	@Override
	// Resolves the block color (with optional anaglyph grayscale conversion) and
	// delegates to the shared implementation.
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		int color = block.colorMultiplier(renderBlocks.blockAccess, x, y, z);
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;
		if(EntityRenderer.anaglyphEnable) {
			float redGray = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
			float greenGray = (red * 30.0F + green * 70.0F) / 100.0F;
			float blueGray = (red * 30.0F + blue * 70.0F) / 100.0F;
			red = redGray;
			green = greenGray;
			blue = blueGray;
		}

		return renderBlockCactusImpl(renderBlocks, block, x, y, z, red, green, blue);
	}

	// Static alternate entry point mirroring renderBlock() for callers that bypass
	// the BlockRenderHandler interface; resolves color and delegates to the impl.
	public static boolean renderBlockCactus(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		int color = block.colorMultiplier(renderBlocks.blockAccess, x, y, z);
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;
		if(EntityRenderer.anaglyphEnable) {
			float redGray = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
			float greenGray = (red * 30.0F + green * 70.0F) / 100.0F;
			float blueGray = (red * 30.0F + blue * 70.0F) / 100.0F;
			red = redGray;
			green = greenGray;
			blue = blueGray;
		}

		return renderBlockCactusImpl(renderBlocks, block, x, y, z, red, green, blue);
	}

	// Shared implementation: draws bottom/top faces at full bounds and the four side
	// faces inset by one pixel, shading each face with its standard brightness tier.
	public static boolean renderBlockCactusImpl(RenderBlocks renderBlocks, Block block, int x, int y, int z, float red, float green, float blue) {
		Tessellator tessellator = Tessellator.instance;
		boolean hasRendered = false;
		float shadeBottom = 0.5F;
		float shadeTop = 1.0F;
		float shadeXZ = 0.8F;
		float shadeNS = 0.6F;
		float redBottom = shadeBottom * red;
		float redTop = shadeTop * red;
		float redXZ = shadeXZ * red;
		float redNS = shadeNS * red;
		float greenBottom = shadeBottom * green;
		float greenTop = shadeTop * green;
		float greenXZ = shadeXZ * green;
		float greenNS = shadeNS * green;
		float blueBottom = shadeBottom * blue;
		float blueTop = shadeTop * blue;
		float blueXZ = shadeXZ * blue;
		float blueNS = shadeNS * blue;
		float inset = 0.0625F;
		int brightness = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z);
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y - 1, z, 0)) {
			tessellator.setBrightness(block.minY > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z));
			tessellator.setColorOpaque_F(redBottom, greenBottom, blueBottom);
			renderBlocks.renderBottomFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 0));
			hasRendered = true;
		}

		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y + 1, z, 1)) {
			tessellator.setBrightness(block.maxY < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z));
			tessellator.setColorOpaque_F(redTop, greenTop, blueTop);
			renderBlocks.renderTopFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 1));
			hasRendered = true;
		}

		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z - 1, 2)) {
			tessellator.setBrightness(block.minZ > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1));
			tessellator.setColorOpaque_F(redXZ, greenXZ, blueXZ);
			tessellator.addTranslation(0.0F, 0.0F, inset);
			renderBlocks.renderEastFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 2));
			tessellator.addTranslation(0.0F, 0.0F, -inset);
			hasRendered = true;
		}

		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z + 1, 3)) {
			tessellator.setBrightness(block.maxZ < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1));
			tessellator.setColorOpaque_F(redXZ, greenXZ, blueXZ);
			tessellator.addTranslation(0.0F, 0.0F, -inset);
			renderBlocks.renderWestFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 3));
			tessellator.addTranslation(0.0F, 0.0F, inset);
			hasRendered = true;
		}

		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x - 1, y, z, 4)) {
			tessellator.setBrightness(block.minX > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z));
			tessellator.setColorOpaque_F(redNS, greenNS, blueNS);
			tessellator.addTranslation(inset, 0.0F, 0.0F);
			renderBlocks.renderNorthFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 4));
			tessellator.addTranslation(-inset, 0.0F, 0.0F);
			hasRendered = true;
		}

		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x + 1, y, z, 5)) {
			tessellator.setBrightness(block.maxX < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z));
			tessellator.setColorOpaque_F(redNS, greenNS, blueNS);
			tessellator.addTranslation(-inset, 0.0F, 0.0F);
			renderBlocks.renderSouthFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 5));
			tessellator.addTranslation(inset, 0.0F, 0.0F);
			hasRendered = true;
}

		return hasRendered;
	}
}
