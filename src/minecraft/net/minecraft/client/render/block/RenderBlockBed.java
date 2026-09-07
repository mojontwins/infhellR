package net.minecraft.client.render.block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BedUtils;
import net.minecraft.game.world.block.BlockBed;
/**
 * Renders the bed block as a two-part model: a lower mattress slab (foot half raised
 * slightly via offset Y) and a taller pillow bump on the head half. The top face UVs
 * are rotated per direction so the pillow texture always faces the head of the bed.
 * Side faces are drawn individually with culling suppressed for the invisible
 * (interior) face determined by {@link BedUtils}.
 */
public final class RenderBlockBed implements BlockRenderHandler {
	@Override
	// Draws the lower mattress slab surface, the top pillow quad (UVs rotated by
	// direction), then each side face that is not the hidden interior face.
	public boolean renderBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		Tessellator tessellator = Tessellator.instance;
		int metadata = renderBlocks.blockAccess.getBlockMetadata(x, y, z);
		int direction = BlockBed.getDirectionFromMetadata(metadata);
		boolean isFoot = BlockBed.isBlockFootOfBed(metadata);
		float shadeBottom = 0.5F;
		float shadeTop = 1.0F;
		float shadeXZ = 0.8F;
		float shadeNS = 0.6F;
		
		int brightness = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z);
		tessellator.setBrightness(brightness);
		tessellator.setColorOpaque_F(shadeBottom, shadeBottom, shadeBottom);
		
		// Bed side (mattress edge) texture
		int bedSideTexture = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 0);
		AtlasTexel.calc(bedSideTexture, TextureAtlas.TERRAIN);
		int sideTileU = AtlasTexel.u;
		int sideTileV = AtlasTexel.v;
		double sideU0 = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)sideTileU);
		double sideU1 = TexelScale.ud(TextureAtlas.TERRAIN, (double)(sideTileU + TextureAtlas.TILE) - 0.01D);
		double sideV0 = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)sideTileV);
		double sideV1 = TexelScale.ud(TextureAtlas.TERRAIN, (double)(sideTileV + TextureAtlas.TILE) - 0.01D);
		double xLow = (double)x + block.minX;
		double xHigh = (double)x + block.maxX;
		double footY = (double)y + block.minY + 0.1875D;
		double zLow = (double)z + block.minZ;
		double zHigh = (double)z + block.maxZ;
		tessellator.addVertexWithUV(xLow, footY, zHigh, sideU0, sideV1);
		tessellator.addVertexWithUV(xLow, footY, zLow, sideU0, sideV0);
		tessellator.addVertexWithUV(xHigh, footY, zLow, sideU1, sideV0);
		tessellator.addVertexWithUV(xHigh, footY, zHigh, sideU1, sideV1);
		// Top (pillow/mattress surface) texture
		float topBrightness = block.getBlockBrightness(renderBlocks.blockAccess, x, y + 1, z);
		tessellator.setColorOpaque_F(shadeTop * topBrightness, shadeTop * topBrightness, shadeTop * topBrightness);
		int topTexture = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 1);
		AtlasTexel.calc(topTexture, TextureAtlas.TERRAIN);
		sideTileU = AtlasTexel.u;
		int topTileV = AtlasTexel.v;
		double topU0 = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)sideTileU);
		double topU1 = TexelScale.ud(TextureAtlas.TERRAIN, (double)(sideTileU + TextureAtlas.TILE) - 0.01D);
		double topV0 = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)topTileV);
		double topV1 = TexelScale.ud(TextureAtlas.TERRAIN, (double)(topTileV + TextureAtlas.TILE) - 0.01D);
		// Four corner UVs composing the top (pillow) quad; rotated per bed direction
		// so the pillow texture always points toward the head of the bed.
		double uCorner0 = topU0;
		double u1 = topU1;
		double u2 = topV0;
		double uCorner3 = topV0;
		double vCorner0 = topU0;
		double v1 = topU1;
		double v2 = topV1;
		double vCorner3 = topV1;
		if(direction == 0) {
			u1 = topU0;
			u2 = topV1;
			vCorner0 = topU1;
			vCorner3 = topV0;
		} else if(direction == 2) {
			uCorner0 = topU1;
			uCorner3 = topV1;
			v1 = topU0;
			v2 = topV0;
		} else if(direction == 3) {
			uCorner0 = topU1;
			uCorner3 = topV1;
			v1 = topU0;
			v2 = topV0;
			u1 = topU0;
			u2 = topV1;
			vCorner0 = topU1;
			vCorner3 = topV0;
		}

		double xHeadLow = (double)x + block.minX;
		double xHeadHigh = (double)x + block.maxX;
		double headY = (double)y + block.maxY;
		double zHeadLow = (double)z + block.minZ;
		double zHeadHigh = (double)z + block.maxZ;
		tessellator.addVertexWithUV(xHeadHigh, headY, zHeadHigh, vCorner0, v2);
		tessellator.addVertexWithUV(xHeadHigh, headY, zHeadLow, uCorner0, u2);
		tessellator.addVertexWithUV(xHeadLow, headY, zHeadLow, u1, uCorner3);
		tessellator.addVertexWithUV(xHeadLow, headY, zHeadHigh, v1, vCorner3);
		// Invisible (interior) face index of the bed that should not be drawn
		int invisibleFace = BedUtils.headInvisibleFace[direction];
		if(isFoot) {
			invisibleFace = BedUtils.headInvisibleFace[BedUtils.footInvisibleFaceRemap[direction]];
		}

		byte hiddenFace = 4;
		switch(direction) {
		case 0:
			hiddenFace = 5;
			break;
		case 1:
			hiddenFace = 3;
		case 2:
		default:
			break;
		case 3:
			hiddenFace = 2;
		}

		if(invisibleFace != 2 && (renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z - 1, 2))) {
			tessellator.setBrightness(block.minZ > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1));
			tessellator.setColorOpaque_F(shadeXZ, shadeXZ, shadeXZ);
			renderBlocks.flipTexture = hiddenFace == 2;
			renderBlocks.renderEastFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 2));
		}

		if(invisibleFace != 3 && (renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z + 1, 3))) {
			tessellator.setBrightness(block.maxZ < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1));
			tessellator.setColorOpaque_F(shadeXZ, shadeXZ, shadeXZ);
			renderBlocks.flipTexture = hiddenFace == 3;
			renderBlocks.renderWestFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 3));
		}

		if(invisibleFace != 4 && (renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x - 1, y, z, 4))) {
			tessellator.setBrightness(block.minX > 0.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z));
			tessellator.setColorOpaque_F(shadeNS, shadeNS, shadeNS);
			renderBlocks.flipTexture = hiddenFace == 4;
			renderBlocks.renderNorthFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 4));
		}

		if(invisibleFace != 5 && (renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x + 1, y, z, 5))) {
			tessellator.setBrightness(block.maxX < 1.0D ? brightness : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z));
			tessellator.setColorOpaque_F(shadeNS, shadeNS, shadeNS);
			renderBlocks.flipTexture = hiddenFace == 5;
			renderBlocks.renderSouthFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 5));
		}

		renderBlocks.flipTexture = false;
		return true;
	}

	@Override
	public boolean renderItemIn3d() {
		return false;
	}
}
