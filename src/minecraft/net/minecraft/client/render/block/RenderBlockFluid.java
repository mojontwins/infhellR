package net.minecraft.client.render.block;

import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.block.BlockFluid;
import net.minecraft.game.world.material.Material;

/**
 * Block render handler for fluid blocks (water/lava, BlockRenderType FLUID).
 *
 * <p>Emits the fluid's visible faces into the active tessellator:
 * <ul>
 *   <li><b>Top surface</b> - a single quad across the cell at the blended height
 *       of its four corners (hNW/hSW/hSE/hNE, from getFluidHeight). The atlas
 *       tile is rotated about its centre by the flow angle {@code theta}
 *       (BlockFluid.func_293_a) so a current looks directional; when no current
 *       is present the plain stationary tile is used, otherwise the offset tile
 *       (tileU+16, tileV+16) is drawn.</li>
 *   <li><b>Bottom face</b> - delegated to RenderBlocks.renderBottomFace, always
 *       drawn at full cell height using the block's front texture and shaded
 *       with a flat grey (shadeBottom).</li>
 *   <li><b>Side faces</b> - for each visible side one quad runs from each
 *       corner's fluid height down to the block base. The top-edge V coordinate
 *       is shifted by {@code (1.0 - height) * 16.0} pixels so a shallower
 *       surface shows a larger part of the water tile.</li>
 * </ul>
 *
 * <p>Quirks:
 * <ul>
 *   <li>A face is only emitted when the neighbouring block in that direction is
 *       non-transparent (shouldSideBeRendered) unless renderAllFaces is set;
 *       when no face at all is visible the method returns false so the
 *       dispatcher can skip the cell.</li>
 *   <li>Corner heights are sampled per-neighbour by getFluidHeight, which
 *       returns 1.0 immediately if a same-material block sits above a corner.</li>
 *   <li>Colour comes from block.colorMultiplier(): the top face is tinted
 *       red/green/blue and the side faces are tinted further by a fixed face
 *       shade (0.8 for the north/south face pair, 0.6 for east/west); the
 *       bottom face is drawn uncoloured.</li>
 *   <li>The block's minY/maxY bounds are restored to the full cell (0..1) on
 *       the way out.</li>
 * </ul>
 */
public final class RenderBlockFluid implements BlockRenderHandler {
	/**
	 * Renders a fluid cell (top/bottom/side faces) into the active tessellator.
	 * The caller (RenderBlocks) owns the tessellator start/draw lifecycle.
	 *
	 * @param renderBlocks the active block renderer (blockAccess, renderAllFaces)
	 * @param block the water/lava block being rendered
	 * @param x the block's x coordinate
	 * @param y the block's y coordinate
	 * @param z the block's z coordinate
	 * @return true if at least one face was emitted, false if the cell is
	 *         completely hidden by opaque neighbours
	 */
	@Override
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		Tessellator tessellator = Tessellator.instance;
		int colorValue = block.colorMultiplier(renderBlocks.blockAccess, x, y, z);
		float red = (float)(colorValue >> 16 & 255) / 255.0F;
		float green = (float)(colorValue >> 8 & 255) / 255.0F;
		float blue = (float)(colorValue & 255) / 255.0F;
		boolean renderTop = block.shouldSideBeRendered(renderBlocks.blockAccess, x, y + 1, z, 1);
		boolean renderBottom = block.shouldSideBeRendered(renderBlocks.blockAccess, x, y - 1, z, 0);
		boolean[] renderSides = new boolean[]{block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z - 1, 2), block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z + 1, 3), block.shouldSideBeRendered(renderBlocks.blockAccess, x - 1, y, z, 4), block.shouldSideBeRendered(renderBlocks.blockAccess, x + 1, y, z, 5)};
		if(!renderTop && !renderBottom && !renderSides[0] && !renderSides[1] && !renderSides[2] && !renderSides[3]) {
			return false;
		} else {
			boolean rendered = false;
			float shadeBottom = 0.5F;
			float shadeTop = 1.0F;
			float shadeSideA = 0.8F;
			float shadeSideB = 0.6F;
			double minY = 0.0D;
			double maxY = 1.0D;
			Material material = block.blockMaterial;
			int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
			float hNW = getFluidHeight(renderBlocks, x, y, z, material);
			float hSW = getFluidHeight(renderBlocks, x, y, z + 1, material);
			float hSE = getFluidHeight(renderBlocks, x + 1, y, z + 1, material);
			float hNE = getFluidHeight(renderBlocks, x + 1, y, z, material);
			if(renderBlocks.renderAllFaces || renderTop) {
				rendered = true;
				int topTextureId = block.getBlockTextureFromSideAndMetadata(1, metadata);
				float theta = (float)BlockFluid.func_293_a(renderBlocks.blockAccess, x, y, z, material);
				if(theta > -999.0F) {
					topTextureId = block.getBlockTextureFromSideAndMetadata(2, metadata);
				}

				AtlasTexel.calc(topTextureId, TextureAtlas.TERRAIN);
				int tileU2 = AtlasTexel.u;
				int tileV2 = AtlasTexel.v;
				double uCenter = TexelScale.ud(TextureAtlas.TERRAIN, (double)tileU2 + (double)TextureAtlas.TILE * 0.5D);
				double vCenter = TexelScale.vd(TextureAtlas.TERRAIN, (double)tileV2 + (double)TextureAtlas.TILE * 0.5D);
				if(theta < -999.0F) {
					theta = 0.0F;
				} else {
					uCenter = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)(tileU2 + TextureAtlas.TILE));
					vCenter = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)(tileV2 + TextureAtlas.TILE));
				}

				float sinOffset = TexelScale.v(TextureAtlas.TERRAIN, MathHelper.sin(theta) * (float)TextureAtlas.TILE * 0.5F);
				float cosOffset = TexelScale.v(TextureAtlas.TERRAIN, MathHelper.cos(theta) * (float)TextureAtlas.TILE * 0.5F);
				tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
				tessellator.setColorOpaque_F(shadeTop * red, shadeTop * green, shadeTop * blue);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + hNW), (double)(z + 0), uCenter - (double)cosOffset - (double)sinOffset, vCenter - (double)cosOffset + (double)sinOffset);
				tessellator.addVertexWithUV((double)(x + 0), (double)((float)y + hSW), (double)(z + 1), uCenter - (double)cosOffset + (double)sinOffset, vCenter + (double)cosOffset + (double)sinOffset);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + hSE), (double)(z + 1), uCenter + (double)cosOffset + (double)sinOffset, vCenter + (double)cosOffset - (double)sinOffset);
				tessellator.addVertexWithUV((double)(x + 1), (double)((float)y + hNE), (double)(z + 0), uCenter + (double)cosOffset - (double)sinOffset, vCenter - (double)cosOffset - (double)sinOffset);
			}

			if(renderBlocks.renderAllFaces || renderBottom) {
				tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z));
				tessellator.setColorOpaque_F(shadeBottom, shadeBottom, shadeBottom);
				renderBlocks.renderBottomFace(block, (double)x, (double)y, (double)z, block.getBlockTextureFromSide(0));
				rendered = true;
			}

			for(int side = 0; side < 4; ++side) {
				int adjacentX = x;
				int adjacentZ = z;
				if(side == 0) {
					adjacentZ = z - 1;
				}

				if(side == 1) {
					++adjacentZ;
				}

				if(side == 2) {
					adjacentX = x - 1;
				}

				if(side == 3) {
					++adjacentX;
				}

				int sideTextureId = block.getBlockTextureFromSideAndMetadata(side + 2, metadata);
				AtlasTexel.calc(sideTextureId, TextureAtlas.TERRAIN);
				int tileU = AtlasTexel.u;
				int tileV = AtlasTexel.v;
				if(renderBlocks.renderAllFaces || renderSides[side]) {
					float height1;
					float height2;
					float corner1X;
					float corner2X;
					float corner1Z;
					float corner2Z;
					if(side == 0) {
						height1 = hNW;
						height2 = hNE;
						corner1X = (float)x;
						corner2X = (float)(x + 1);
						corner1Z = (float)z;
						corner2Z = (float)z;
					} else if(side == 1) {
						height1 = hSE;
						height2 = hSW;
						corner1X = (float)(x + 1);
						corner2X = (float)x;
						corner1Z = (float)(z + 1);
						corner2Z = (float)(z + 1);
					} else if(side == 2) {
						height1 = hSW;
						height2 = hNW;
						corner1X = (float)x;
						corner2X = (float)x;
						corner1Z = (float)(z + 1);
						corner2Z = (float)z;
					} else {
						height1 = hNE;
						height2 = hSE;
						corner1X = (float)(x + 1);
						corner2X = (float)(x + 1);
						corner1Z = (float)z;
						corner2Z = (float)(z + 1);
					}

					rendered = true;
					double uLow = (double) TexelScale.u(TextureAtlas.TERRAIN, (float)tileU);
					double uHigh = TexelScale.ud(TextureAtlas.TERRAIN, (double)(tileU + TextureAtlas.TILE) - 0.01D);
					double vLowFirst = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + (1.0F - height1) * (float)TextureAtlas.TILE);
					double vLowSecond = (double) TexelScale.v(TextureAtlas.TERRAIN, (float)tileV + (1.0F - height2) * (float)TextureAtlas.TILE);
					double vHigh = TexelScale.vd(TextureAtlas.TERRAIN, (double)(tileV + TextureAtlas.TILE) - 0.01D);
					tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, adjacentX, y, adjacentZ));
					float faceShade;
					if(side < 2) {
						faceShade = shadeSideA;
					} else {
						faceShade = shadeSideB;
					}

					tessellator.setColorOpaque_F(shadeTop * faceShade * red, shadeTop * faceShade * green, shadeTop * faceShade * blue);
					tessellator.addVertexWithUV((double)corner1X, (double)((float)y + height1), (double)corner1Z, uLow, vLowFirst);
					tessellator.addVertexWithUV((double)corner2X, (double)((float)y + height2), (double)corner2Z, uHigh, vLowSecond);
					tessellator.addVertexWithUV((double)corner2X, (double)(y + 0), (double)corner2Z, uHigh, vHigh);
					tessellator.addVertexWithUV((double)corner1X, (double)(y + 0), (double)corner1Z, uLow, vHigh);
				}
			}

			block.minY = minY;
			block.maxY = maxY;
			return rendered;
		}
	}

	/**
	 * Computes the fluid surface height at one corner of a cell by averaging the
	 * four neighbour samples around that corner. Returns 1.0 immediately if a
	 * same-material block is directly above a sample; non-solid neighbours pull
	 * the surface up toward 1.0.
	 */
	private static float getFluidHeight(RenderBlocks renderBlocks, int x, int y, int z, Material material) {
		int count = 0;
		float height = 0.0F;

		for(int corner = 0; corner < 4; ++corner) {
			int adjacentX = x - (corner & 1);
			int adjacentZ = z - (corner >> 1 & 1);
			if(renderBlocks.blockAccess.getBlockMaterial(adjacentX, y + 1, adjacentZ) == material) {
				return 1.0F;
			}

			Material adjacentMaterial = renderBlocks.blockAccess.getBlockMaterial(adjacentX, y, adjacentZ);
			if(adjacentMaterial != material) {
				if(!adjacentMaterial.isSolid()) {
					++height;
					++count;
				}
			} else {
				int adjacentMetadata = renderBlocks.blockAccess.getBlockMetadata(adjacentX, y, adjacentZ);
				if(adjacentMetadata >= 8 || adjacentMetadata == 0) {
					height += BlockFluid.getPercentAir(adjacentMetadata) * 10.0F;
					count += 10;
				}

				height += BlockFluid.getPercentAir(adjacentMetadata);
				++count;
			}
		}

		return 1.0F - height / (float)count;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}