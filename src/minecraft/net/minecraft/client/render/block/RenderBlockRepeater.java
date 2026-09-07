package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockRedstoneRepeater;
/**
 * Renders a redstone repeater in two parts: first the solid body/base via
 * RenderBlockUtil.renderStandardBlock, then the two redstone-torch caps on top
 * via RenderBlockTorch.renderTorchAtAngle. One torch is fixed at the far end
 * (+-0.3125 per direction) and the other sits on the end whose offset slides
 * along the delay axis via BlockRedstoneRepeater.repeaterTorchOffset to
 * reflect the delay state (metadata bits 2-3). A single top-face quad is drawn
 * at y+0.125 and its four corner coordinates are remapped per direction
 * (metadata bits 0-1) to rotate the tile so the texture follows the repeater's
 * facing.
 */
public final class RenderBlockRepeater implements BlockRenderHandler {
	/**
	 * Emits the repeater body (standard block), both torch caps and the
	 * direction-rotated top-face quad for the cell at (x, y, z); returns true.
	 */
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		int direction = metadata & 3;
		int delaySteps = (metadata & 12) >> 2;
		RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		double torchY = -0.1875D;
		double offX1 = 0.0D;
		double offZ1 = 0.0D;
		double offX2 = 0.0D;
		double offZ2 = 0.0D;
		switch(direction) {
		case 0:
			offZ2 = -0.3125D;
			offZ1 = BlockRedstoneRepeater.repeaterTorchOffset[delaySteps];
			break;
		case 1:
			offX2 = 0.3125D;
			offX1 = -BlockRedstoneRepeater.repeaterTorchOffset[delaySteps];
			break;
		case 2:
			offZ2 = 0.3125D;
			offZ1 = -BlockRedstoneRepeater.repeaterTorchOffset[delaySteps];
			break;
		case 3:
			offX2 = -0.3125D;
			offX1 = BlockRedstoneRepeater.repeaterTorchOffset[delaySteps];
		}

		RenderBlockTorch.renderTorchAtAngle(renderBlocks, block, (double)x + offX1, (double)y + torchY, (double)z + offZ1, 0.0D, 0.0D);
		RenderBlockTorch.renderTorchAtAngle(renderBlocks, block, (double)x + offX2, (double)y + torchY, (double)z + offZ2, 0.0D, 0.0D);
		int textureId = block.getBlockTextureFromSide(1);
		AtlasTexel.calc(textureId, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		double uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileX);
		double uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileX + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileY);
		double vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileY + TextureAtlas.TERRAIN.tileSpan);
		float heightStep = 0.125F;
		float xMin = (float)(x + 1);
		float x1 = (float)(x + 1);
		float x2 = (float)(x + 0);
		float xMax = (float)(x + 0);
		float zMin = (float)(z + 0);
		float z1 = (float)(z + 1);
		float z2 = (float)(z + 1);
		float zMax = (float)(z + 0);
		float quadY = (float)y + heightStep;
		if(direction == 2) {
			xMin = x1 = (float)(x + 0);
			x2 = xMax = (float)(x + 1);
			zMin = zMax = (float)(z + 1);
			z1 = z2 = (float)(z + 0);
		} else if(direction == 3) {
			xMin = xMax = (float)(x + 0);
			x1 = x2 = (float)(x + 1);
			zMin = z1 = (float)(z + 0);
			z2 = zMax = (float)(z + 1);
		} else if(direction == 1) {
			xMin = xMax = (float)(x + 1);
			x1 = x2 = (float)(x + 0);
			zMin = z1 = (float)(z + 1);
			z2 = zMax = (float)(z + 0);
		}

		tessellator.addVertexWithUV((double)xMax, (double)quadY, (double)zMax, uLo, vLo);
		tessellator.addVertexWithUV((double)x2, (double)quadY, (double)z2, uLo, vHi);
		tessellator.addVertexWithUV((double)x1, (double)quadY, (double)z1, uHi, vHi);
		tessellator.addVertexWithUV((double)xMin, (double)quadY, (double)zMin, uHi, vLo);
		return true;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
