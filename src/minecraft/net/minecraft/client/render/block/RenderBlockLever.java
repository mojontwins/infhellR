package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.physics.Vec3D;
/**
 * Renders a lever block in two passes: first the cobblestone body/base is
 * drawn with RenderBlockUtil.renderStandardBlock while temporarily overriding
 * the block texture to cobblestone; then the handle bar is drawn as an
 * 8-corner box rotated on its pivot to match the facing direction (metadata
 * 0-5) and its flipped/toggled state (bit 3). Vertices are translated to the
 * block centre and each of the six faces is emitted with the lever tile's UV
 * range.
 */
public final class RenderBlockLever implements BlockRenderHandler {
	@Override
public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		// Pass 1: cobblestone base via renderStandardBlock (texture override); pass 2: rotated handle bar.
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		int direction = metadata & 7;
		boolean isToggled = (metadata & 8) > 0;
		Tessellator tessellator = Tessellator.instance;
		boolean hadOverride = renderBlocks.overrideBlockTexture >= 0;
		if(!hadOverride) {
			renderBlocks.overrideBlockTexture = Block.cobblestone.blockIndexInTexture;
		}

		float plateHalf = 0.25F;
		float handleHalf = 0.1875F;
		float baseHalf = 0.1875F;
		if(direction == 5) {
			block.setBlockBounds(0.5F - handleHalf, 0.0F, 0.5F - plateHalf, 0.5F + handleHalf, baseHalf, 0.5F + plateHalf);
		} else if(direction == 6) {
			block.setBlockBounds(0.5F - plateHalf, 0.0F, 0.5F - handleHalf, 0.5F + plateHalf, baseHalf, 0.5F + handleHalf);
		} else if(direction == 4) {
			block.setBlockBounds(0.5F - handleHalf, 0.5F - plateHalf, 1.0F - baseHalf, 0.5F + handleHalf, 0.5F + plateHalf, 1.0F);
		} else if(direction == 3) {
			block.setBlockBounds(0.5F - handleHalf, 0.5F - plateHalf, 0.0F, 0.5F + handleHalf, 0.5F + plateHalf, baseHalf);
		} else if(direction == 2) {
			block.setBlockBounds(1.0F - baseHalf, 0.5F - plateHalf, 0.5F - handleHalf, 1.0F, 0.5F + plateHalf, 0.5F + handleHalf);
		} else if(direction == 1) {
			block.setBlockBounds(0.0F, 0.5F - plateHalf, 0.5F - handleHalf, baseHalf, 0.5F + plateHalf, 0.5F + handleHalf);
		}

		RenderBlockUtil.renderStandardBlock(renderBlocks, block, x, y, z);
		if(!hadOverride) {
			renderBlocks.overrideBlockTexture = -1;
		}

		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		float white = 1.0F;
		tessellator.setColorOpaque_F(white, white, white);
		int textureId = block.getBlockTextureFromSide(0);
		if(renderBlocks.overrideBlockTexture >= 0) {
			textureId = renderBlocks.overrideBlockTexture;
		}

		AtlasTexel.calc(textureId, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		float uLo = TexelScale.u(TextureAtlas.TERRAIN, (float) tileX);
		float uHi = TexelScale.u(TextureAtlas.TERRAIN, (float) tileX + TextureAtlas.TERRAIN.tileSpan);
		float vLo = TexelScale.v(TextureAtlas.TERRAIN, (float) tileY);
		float vHi = TexelScale.v(TextureAtlas.TERRAIN, (float) tileY + TextureAtlas.TERRAIN.tileSpan);
		Vec3D[] pts = new Vec3D[8];
		float barHalfX = 0.0625F;
		float barHalfZ = 0.0625F;
		float barLength = 0.625F;
		pts[0] = Vec3D.createVector((double)(-barHalfX), 0.0D, (double)(-barHalfZ));
		pts[1] = Vec3D.createVector((double)barHalfX, 0.0D, (double)(-barHalfZ));
		pts[2] = Vec3D.createVector((double)barHalfX, 0.0D, (double)barHalfZ);
		pts[3] = Vec3D.createVector((double)(-barHalfX), 0.0D, (double)barHalfZ);
		pts[4] = Vec3D.createVector((double)(-barHalfX), (double)barLength, (double)(-barHalfZ));
		pts[5] = Vec3D.createVector((double)barHalfX, (double)barLength, (double)(-barHalfZ));
		pts[6] = Vec3D.createVector((double)barHalfX, (double)barLength, (double)barHalfZ);
		pts[7] = Vec3D.createVector((double)(-barHalfX), (double)barLength, (double)barHalfZ);

		for(int pi = 0; pi < 8; ++pi) {
			if(isToggled) {
				pts[pi].zCoord -= 0.0625D;
				pts[pi].rotateAroundX((float)Math.PI / 4.5F);
			} else {
				pts[pi].zCoord += 0.0625D;
				pts[pi].rotateAroundX(-0.69813174F);
			}

			if(direction == 6) {
				pts[pi].rotateAroundY((float)Math.PI / 2F);
			}

			if(direction < 5) {
				pts[pi].yCoord -= 0.375D;
				pts[pi].rotateAroundX((float)Math.PI / 2F);
				if(direction == 4) {
					pts[pi].rotateAroundY(0.0F);
				}

				if(direction == 3) {
					pts[pi].rotateAroundY((float)Math.PI);
				}

				if(direction == 2) {
					pts[pi].rotateAroundY((float)Math.PI / 2F);
				}

				if(direction == 1) {
					pts[pi].rotateAroundY(-1.5707964F);
				}

				pts[pi].xCoord += (double)x + 0.5D;
				pts[pi].yCoord += (double)((float)y + 0.5F);
				pts[pi].zCoord += (double)z + 0.5D;
			} else {
				pts[pi].xCoord += (double)x + 0.5D;
				pts[pi].yCoord += (double)((float)y + 0.125F);
				pts[pi].zCoord += (double)z + 0.5D;
			}
		}

		Vec3D corner0 = null;
		Vec3D corner1 = null;
		Vec3D corner2 = null;
		Vec3D corner3 = null;

		for(int side = 0; side < 6; ++side) {
			if(side == 0) {
				uLo = TexelScale.u(TextureAtlas.TERRAIN, (float)(tileX + 7));
				uHi = TexelScale.u(TextureAtlas.TERRAIN, (float)(tileX + 9) - 0.01F);
				vLo = TexelScale.v(TextureAtlas.TERRAIN, (float)(tileY + 6));
				vHi = TexelScale.v(TextureAtlas.TERRAIN, (float)(tileY + 8) - 0.01F);
			} else if(side == 2) {
				uLo = TexelScale.u(TextureAtlas.TERRAIN, (float)(tileX + 7));
				uHi = TexelScale.u(TextureAtlas.TERRAIN, (float)(tileX + 9) - 0.01F);
				vLo = TexelScale.v(TextureAtlas.TERRAIN, (float)(tileY + 6));
				vHi = TexelScale.v(TextureAtlas.TERRAIN, (float)(tileY + TextureAtlas.TILE) - 0.01F);
			}

			if(side == 0) {
				corner0 = pts[0];
				corner1 = pts[1];
				corner2 = pts[2];
				corner3 = pts[3];
			} else if(side == 1) {
				corner0 = pts[7];
				corner1 = pts[6];
				corner2 = pts[5];
				corner3 = pts[4];
			} else if(side == 2) {
				corner0 = pts[1];
				corner1 = pts[0];
				corner2 = pts[4];
				corner3 = pts[5];
			} else if(side == 3) {
				corner0 = pts[2];
				corner1 = pts[1];
				corner2 = pts[5];
				corner3 = pts[6];
			} else if(side == 4) {
				corner0 = pts[3];
				corner1 = pts[2];
				corner2 = pts[6];
				corner3 = pts[7];
			} else if(side == 5) {
				corner0 = pts[0];
				corner1 = pts[3];
				corner2 = pts[7];
				corner3 = pts[4];
			}

			tessellator.addVertexWithUV(corner0.xCoord, corner0.yCoord, corner0.zCoord, (double)uLo, (double)vHi);
			tessellator.addVertexWithUV(corner1.xCoord, corner1.yCoord, corner1.zCoord, (double)uHi, (double)vHi);
			tessellator.addVertexWithUV(corner2.xCoord, corner2.yCoord, corner2.zCoord, (double)uHi, (double)vLo);
			tessellator.addVertexWithUV(corner3.xCoord, corner3.yCoord, corner3.zCoord, (double)uLo, (double)vLo);
		}

		return true;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
