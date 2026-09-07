package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockRedstoneWire;

/**
 * Renders redstone dust as a thin flat quad elevated 0.015625 above the
 * supporting block face. The quad is coloured per signal level (metadata 0-15):
 * red = level*0.6+0.4 (fixed 0.3 at level 0) with quadratically scaled
 * green/blue components clamped at zero. Connections are probed in all four
 * cardinal directions (adjacent cell, wire one level below under a non-cube,
 * and wire on top of an adjacent cube). A purely east-west or north-south run
 * draws a one-block-wide straight line using the +16 tile column; otherwise a
 * cross is formed by notching the quad 0.3125 toward each unconnected side
 * (the raised centre dot is supplied by the dust's cross tile, not model
 * geometry). When the cell above is open, a vertical quad is emitted onto the
 * side of any neighbouring normal cube that carries wire on top of it so wires
 * can climb (also uses the +16 tile column).
 */
public final class RenderBlockRedstoneWire implements BlockRenderHandler {
	/**
	 * Emits the redstone dust quads (flat cross/line plus optional climbing
	 * side quads) for the cell at (x, y, z) and returns true to indicate the
	 * block was fully handled.
	 */
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {

		Tessellator tessellator = Tessellator.instance;
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		int texId = block.getBlockTextureFromSideAndMetadata(1, metadata);
		if(renderBlocks.overrideBlockTexture >= 0) {
			texId = renderBlocks.overrideBlockTexture;
		}

		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		float white = 1.0F;
		float level = (float)metadata / 15.0F;
		float redColor = level * 0.6F + 0.4F;
		if(metadata == 0) {
			redColor = 0.3F;
		}

		float greenColor = level * level * 0.7F - 0.5F;
		float blueColor = level * level * 0.6F - 0.7F;
		if(greenColor < 0.0F) {
			greenColor = 0.0F;
		}

		if(blueColor < 0.0F) {
			blueColor = 0.0F;
		}

		tessellator.setColorOpaque_F(redColor, greenColor, blueColor);
		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
		double uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileU + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
		double vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
		boolean hasWest = BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x - 1, y, z, 1) || !renderBlocks.blockAccess.isBlockNormalCube(x - 1, y, z) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x - 1, y - 1, z, -1);
		boolean hasEast = BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x + 1, y, z, 3) || !renderBlocks.blockAccess.isBlockNormalCube(x + 1, y, z) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x + 1, y - 1, z, -1);
		boolean hasNorth = BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x, y, z - 1, 2) || !renderBlocks.blockAccess.isBlockNormalCube(x, y, z - 1) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x, y - 1, z - 1, -1);
		boolean hasSouth = BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x, y, z + 1, 0) || !renderBlocks.blockAccess.isBlockNormalCube(x, y, z + 1) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x, y - 1, z + 1, -1);
		if(!renderBlocks.blockAccess.isBlockNormalCube(x, y + 1, z)) {
			if(renderBlocks.blockAccess.isBlockNormalCube(x - 1, y, z) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x - 1, y + 1, z, -1)) {
				hasWest = true;
			}

			if(renderBlocks.blockAccess.isBlockNormalCube(x + 1, y, z) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x + 1, y + 1, z, -1)) {
				hasEast = true;
			}

			if(renderBlocks.blockAccess.isBlockNormalCube(x, y, z - 1) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x, y + 1, z - 1, -1)) {
				hasNorth = true;
			}

			if(renderBlocks.blockAccess.isBlockNormalCube(x, y, z + 1) && BlockRedstoneWire.isPowerProviderOrWire(renderBlocks.blockAccess, x, y + 1, z + 1, -1)) {
				hasSouth = true;
			}
		}

		float xMin = (float)(x + 0);
		float x1 = (float)(x + 1);
		float zMin = (float)(z + 0);
		float z1 = (float)(z + 1);
		byte axis = 0;
		if((hasWest || hasEast) && !hasNorth && !hasSouth) {
			axis = 1;
		}

		if((hasNorth || hasSouth) && !hasEast && !hasWest) {
			axis = 2;
		}

		if(axis != 0) {
			uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)(tileU + TextureAtlas.TILE));
			uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)(tileU + TextureAtlas.TILE) + TextureAtlas.TERRAIN.tileSpan);
			vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
			vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
		}

		if(axis == 0) {
			if(hasEast || hasNorth || hasSouth || hasWest) {
				if(!hasWest) {
					xMin += 0.3125F;
				}

				if(!hasWest) {
					uLo += 0.01953125D;
				}

				if(!hasEast) {
					x1 -= 0.3125F;
				}

				if(!hasEast) {
					uHi -= 0.01953125D;
				}

				if(!hasNorth) {
					zMin += 0.3125F;
				}

				if(!hasNorth) {
					vLo += 0.01953125D;
				}

				if(!hasSouth) {
					z1 -= 0.3125F;
				}

				if(!hasSouth) {
					vHi -= 0.01953125D;
				}
			}

			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)z1, uHi, vHi);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)zMin, uHi, vLo);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)zMin, uLo, vLo);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)z1, uLo, vHi);
			/*tessellator.setColorOpaque_F(white, white, white);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)z1, uHi, vHi + 0.0625D);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)zMin, uHi, vLo + 0.0625D);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)zMin, uLo, vLo + 0.0625D);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)z1, uLo, vHi + 0.0625D);*/
		} else if(axis == 1) {
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)z1, uHi, vHi);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)zMin, uHi, vLo);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)zMin, uLo, vLo);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)z1, uLo, vHi);
			/*tessellator.setColorOpaque_F(white, white, white);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)z1, uHi, vHi + 0.0625D);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)zMin, uHi, vLo + 0.0625D);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)zMin, uLo, vLo + 0.0625D);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)z1, uLo, vHi + 0.0625D);*/
		} else if(axis == 2) {
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)z1, uHi, vHi);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)zMin, uLo, vHi);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)zMin, uLo, vLo);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)z1, uHi, vLo);
			/*tessellator.setColorOpaque_F(white, white, white);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)z1, uHi, vHi + 0.0625D);
			tessellator.addVertexWithUV((double)x1, (double)y + 0.015625D, (double)zMin, uLo, vHi + 0.0625D);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)zMin, uLo, vLo + 0.0625D);
			tessellator.addVertexWithUV((double)xMin, (double)y + 0.015625D, (double)z1, uHi, vLo + 0.0625D);*/
		}

		if(!renderBlocks.blockAccess.isBlockNormalCube(x, y + 1, z)) {
			uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)(tileU + TextureAtlas.TILE));
			uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)(tileU + TextureAtlas.TILE) + TextureAtlas.TERRAIN.tileSpan);
			vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV);
			vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + TextureAtlas.TERRAIN.tileSpan);
			if(renderBlocks.blockAccess.isBlockNormalCube(x - 1, y, z) && renderBlocks.blockAccess.getBlockId(x - 1, y + 1, z) == Block.redstoneWire.blockID) {
				tessellator.setColorOpaque_F(white * redColor, white * greenColor, white * blueColor);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 1), uHi, vLo);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)(y + 0), (double)(z + 1), uLo, vLo);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)(y + 0), (double)(z + 0), uLo, vHi);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 0), uHi, vHi);
				/*tessellator.setColorOpaque_F(white, white, white);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 1), uHi, vLo + 0.0625D);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)(y + 0), (double)(z + 1), uLo, vLo + 0.0625D);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)(y + 0), (double)(z + 0), uLo, vHi + 0.0625D);
				tessellator.addVertexWithUV((double)((float)x + 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 0), uHi, vHi + 0.0625D);*/
			}

			if(renderBlocks.blockAccess.isBlockNormalCube(x + 1, y, z) && renderBlocks.blockAccess.getBlockId(x + 1, y + 1, z) == Block.redstoneWire.blockID) {
				tessellator.setColorOpaque_F(white * redColor, white * greenColor, white * blueColor);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)(y + 0), (double)(z + 1), uLo, vHi);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 1), uHi, vHi);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 0), uHi, vLo);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)(y + 0), (double)(z + 0), uLo, vLo);
				/*tessellator.setColorOpaque_F(white, white, white);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)(y + 0), (double)(z + 1), uLo, vHi + 0.0625D);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 1), uHi, vHi + 0.0625D);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)((float)(y + 1) + 0.021875F), (double)(z + 0), uHi, vLo + 0.0625D);
				tessellator.addVertexWithUV((double)((float)(x + 1) - 0.015625F), (double)(y + 0), (double)(z + 0), uLo, vLo + 0.0625D);*/
			}

			if(renderBlocks.blockAccess.isBlockNormalCube(x, y, z - 1) && renderBlocks.blockAccess.getBlockId(x, y + 1, z - 1) == Block.redstoneWire.blockID) {
				tessellator.setColorOpaque_F(white * redColor, white * greenColor, white * blueColor);
				tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)((float)z + 0.015625F), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 1) + 0.021875F), (double)((float)z + 0.015625F), uHi, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 1) + 0.021875F), (double)((float)z + 0.015625F), uHi, vLo);
				tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)((float)z + 0.015625F), uLo, vLo);
				/*tessellator.setColorOpaque_F(white, white, white);
				tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)((float)z + 0.015625F), uLo, vHi + 0.0625D);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 1) + 0.021875F), (double)((float)z + 0.015625F), uHi, vHi + 0.0625D);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 1) + 0.021875F), (double)((float)z + 0.015625F), uHi, vLo + 0.0625D);
				tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)((float)z + 0.015625F), uLo, vLo + 0.0625D);*/
			}

			if(renderBlocks.blockAccess.isBlockNormalCube(x, y, z + 1) && renderBlocks.blockAccess.getBlockId(x, y + 1, z + 1) == Block.redstoneWire.blockID) {
				tessellator.setColorOpaque_F(white * redColor, white * greenColor, white * blueColor);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 1) + 0.021875F), (double)((float)(z + 1) - 0.015625F), uHi, vLo);
				tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)((float)(z + 1) - 0.015625F), uLo, vLo);
				tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)((float)(z + 1) - 0.015625F), uLo, vHi);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 1) + 0.021875F), (double)((float)(z + 1) - 0.015625F), uHi, vHi);
				/*tessellator.setColorOpaque_F(white, white, white);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)(y + 1) + 0.021875F), (double)((float)(z + 1) - 0.015625F), uHi, vLo + 0.0625D);
				tessellator.addVertexWithUV((double)(x + 1), (double)(y + 0), (double)((float)(z + 1) - 0.015625F), uLo, vLo + 0.0625D);
				tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)((float)(z + 1) - 0.015625F), uLo, vHi + 0.0625D);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)(y + 1) + 0.021875F), (double)((float)(z + 1) - 0.015625F), uHi, vHi + 0.0625D);*/
			}
		}

		return true;
	
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
