package net.minecraft.client.render.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.EntityRenderer;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.block.Block;
import org.lwjgl.opengl.GL11;

/**
 * Geometry shared by several render handlers: the standard shaded cube with the
 * full ambient-occlusion machinery (see {@link #renderStandardBlockWithAmbientOcclusion}),
 * the flat per-face color path ({@link #renderStandardBlockWithColorMultiplier}), the
 * crossed-squares planes of plants ({@link #renderCrossedSquares}), direct writes to the
 * shared block bounds, and the inventory cube helpers. All public methods take the
 * {@link RenderBlocks} engine as state so they stay stateless/sharable.
 */
public final class RenderBlockUtil {
	private RenderBlockUtil() {
	}

	/**
	 * Renders a standard cube. Reads the block's biome tint color multiplier, maps it
	 * through the anaglyph transform when active, then routes to the ambient-occlusion
	 * or flat-color path based on the global AO setting (blocks that emit light skip AO).
	 */
	public static boolean renderStandardBlock(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		int colorPacked = block.colorMultiplier(renderBlocks.blockAccess, x, y, z);
		float red = (float)(colorPacked >> 16 & 255) / 255.0F;
		float green = (float)(colorPacked >> 8 & 255) / 255.0F;
		float blue = (float)(colorPacked & 255) / 255.0F;
		if(EntityRenderer.anaglyphEnable) {
			float anagRed = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
			float anagGreen = (red * 30.0F + green * 70.0F) / 100.0F;
			float anagBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
			red = anagRed;
			green = anagGreen;
			blue = anagBlue;
		}
	
		return Minecraft.isAmbientOcclusionEnabled() && Block.lightValue[block.blockID] == 0 ? 
				renderStandardBlockWithAmbientOcclusion(renderBlocks, block, x, y, z, red, green, blue) 
			: 
				renderStandardBlockWithColorMultiplier(renderBlocks, block, x, y, z, red, green, blue);
	}

	/**
	 * Renders the cube with per-vertex ambient occlusion. For each of the six faces
	 * (sampled at the block's neighbour on that side when the block touches the plane)
	 * it gathers the AO light values and packed day/night brightness of the surrounding
	 * cells into the {@code aoLightValueScratch*} scratch fields, averages the three
	 * cells at each corner (blending them with the face light) into the
	 * {@code cornerTopLeft/...}/{@code brightnessTopLeft/...} state, and finally hands
	 * the face to the engine's emitters, which interpolate the colors across the quad.
	 *
	 * <p>The {@code tint*} booleans gate the red/green/blue color multiplier per face:
	 * grass (texture 3) and override textures render untinted. The per-face diffuse
	 * factors (bottom 0.5, top 1.0, z-planes 0.8, x-planes 0.6) match
	 * {@link net.minecraft.client.render.RenderBlocks#SIDE_LIGHT}.</p>
	 */
	public static boolean renderStandardBlockWithAmbientOcclusion(RenderBlocks renderBlocks, Block block, int x, int y, int z, float red, float green, float blue) {
		renderBlocks.enableAO = true;
		boolean anyFace = false;
		float lightTopLeft = renderBlocks.lightValueOwn;
		float lightBottomLeft = renderBlocks.lightValueOwn;
		float lightBottomRight = renderBlocks.lightValueOwn;
		float lightTopRight = renderBlocks.lightValueOwn;
		boolean tintBottom = true;
		boolean tintTop = true;
		boolean tintZNeg = true;
		boolean tintZPos = true;
		boolean tintXNeg = true;
		boolean tintXPos = true;
		renderBlocks.lightValueOwn = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z);
		renderBlocks.aoLightValueXNeg = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z);
		renderBlocks.aoLightValueYNeg = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z);
		renderBlocks.aoLightValueZNeg = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z - 1);
		renderBlocks.aoLightValueXPos = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z);
		renderBlocks.aoLightValueYPos = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z);
		renderBlocks.aoLightValueZPos = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z + 1);
		int brightnessSelf = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z);
		int brightnessXNeg = brightnessSelf;
		int brightnessYNeg = brightnessSelf;
		int brightnessZNeg = brightnessSelf;
		int brightnessXPos = brightnessSelf;
		int brightnessYPos = brightnessSelf;
		int brightnessZPos = brightnessSelf;
		if(block.minY <= 0.0D) {
			brightnessYNeg = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z);
		}
	
		if(block.maxY >= 1.0D) {
			brightnessYPos = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z);
		}
	
		if(block.minX <= 0.0D) {
			brightnessXNeg = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z);
		}
	
		if(block.maxX >= 1.0D) {
			brightnessXPos = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z);
		}
	
		if(block.minZ <= 0.0D) {
			brightnessZNeg = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1);
		}
	
		if(block.maxZ >= 1.0D) {
			brightnessZPos = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1);
		}
	
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(983055);
		renderBlocks.aoGrassXYZPPC = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x + 1, y + 1, z)];
		renderBlocks.aoGrassXYZPNC = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x + 1, y - 1, z)];
		renderBlocks.aoGrassXYZPCP = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x + 1, y, z + 1)];
		renderBlocks.aoGrassXYZPCN = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x + 1, y, z - 1)];
		renderBlocks.aoGrassXYZNPC = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x - 1, y + 1, z)];
		renderBlocks.aoGrassXYZNNC = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x - 1, y - 1, z)];
		renderBlocks.aoGrassXYZNCN = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x - 1, y, z - 1)];
		renderBlocks.aoGrassXYZNCP = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x - 1, y, z + 1)];
		renderBlocks.aoGrassXYZCPP = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x, y + 1, z + 1)];
		renderBlocks.aoGrassXYZCPN = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x, y + 1, z - 1)];
		renderBlocks.aoGrassXYZCNP = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x, y - 1, z + 1)];
		renderBlocks.aoGrassXYZCNN = Block.canBlockGrass[renderBlocks.blockAccess.getBlockId(x, y - 1, z - 1)];
		if(block.blockIndexInTexture == 3) {
			tintXPos = false;
			tintXNeg = false;
			tintZPos = false;
			tintZNeg = false;
			tintBottom = false;
		}
	
		if(renderBlocks.overrideBlockTexture >= 0) {
			tintXPos = false;
			tintXNeg = false;
			tintZPos = false;
			tintZNeg = false;
			tintBottom = false;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y - 1, z, 0)) {
			if(renderBlocks.aoType > 0) {
				if(block.minY <= 0.0D) {
				--y;
				}
	
				renderBlocks.aoBrightnessXYNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoBrightnessYZNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoBrightnessYZNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1);
				renderBlocks.aoBrightnessXYPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z);
				renderBlocks.aoLightValueScratchXYNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoLightValueScratchYZNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoLightValueScratchYZNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z + 1);
				renderBlocks.aoLightValueScratchXYPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z);
				if(!renderBlocks.aoGrassXYZCNN && !renderBlocks.aoGrassXYZNNC) {
					renderBlocks.aoLightValueScratchXYZNNN = renderBlocks.aoLightValueScratchXYNN;
					renderBlocks.aoBrightnessXYZNNN = renderBlocks.aoBrightnessXYNN;
				} else {
					renderBlocks.aoLightValueScratchXYZNNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z - 1);
					renderBlocks.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZCNP && !renderBlocks.aoGrassXYZNNC) {
					renderBlocks.aoLightValueScratchXYZNNP = renderBlocks.aoLightValueScratchXYNN;
					renderBlocks.aoBrightnessXYZNNP = renderBlocks.aoBrightnessXYNN;
				} else {
					renderBlocks.aoLightValueScratchXYZNNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z + 1);
					renderBlocks.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z + 1);
				}
	
				if(!renderBlocks.aoGrassXYZCNN && !renderBlocks.aoGrassXYZPNC) {
					renderBlocks.aoLightValueScratchXYZPNN = renderBlocks.aoLightValueScratchXYPN;
					renderBlocks.aoBrightnessXYZPNN = renderBlocks.aoBrightnessXYPN;
				} else {
					renderBlocks.aoLightValueScratchXYZPNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z - 1);
					renderBlocks.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZCNP && !renderBlocks.aoGrassXYZPNC) {
					renderBlocks.aoLightValueScratchXYZPNP = renderBlocks.aoLightValueScratchXYPN;
					renderBlocks.aoBrightnessXYZPNP = renderBlocks.aoBrightnessXYPN;
				} else {
					renderBlocks.aoLightValueScratchXYZPNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z + 1);
					renderBlocks.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z + 1);
				}
	
				if(block.minY <= 0.0D) {
					++y;
				}
	
				lightTopLeft = (renderBlocks.aoLightValueScratchXYZNNP + renderBlocks.aoLightValueScratchXYNN + renderBlocks.aoLightValueScratchYZNP + renderBlocks.aoLightValueYNeg) / 4.0F;
				lightTopRight = (renderBlocks.aoLightValueScratchYZNP + renderBlocks.aoLightValueYNeg + renderBlocks.aoLightValueScratchXYZPNP + renderBlocks.aoLightValueScratchXYPN) / 4.0F;
				lightBottomRight = (renderBlocks.aoLightValueYNeg + renderBlocks.aoLightValueScratchYZNN + renderBlocks.aoLightValueScratchXYPN + renderBlocks.aoLightValueScratchXYZPNN) / 4.0F;
				lightBottomLeft = (renderBlocks.aoLightValueScratchXYNN + renderBlocks.aoLightValueScratchXYZNNN + renderBlocks.aoLightValueYNeg + renderBlocks.aoLightValueScratchYZNN) / 4.0F;
				renderBlocks.brightnessTopLeft = getAoBrightness(renderBlocks.aoBrightnessXYZNNP, renderBlocks.aoBrightnessXYNN, renderBlocks.aoBrightnessYZNP, brightnessYNeg);
				renderBlocks.brightnessTopRight = getAoBrightness(renderBlocks.aoBrightnessYZNP, renderBlocks.aoBrightnessXYZPNP, renderBlocks.aoBrightnessXYPN, brightnessYNeg);
				renderBlocks.brightnessBottomRight = getAoBrightness(renderBlocks.aoBrightnessYZNN, renderBlocks.aoBrightnessXYPN, renderBlocks.aoBrightnessXYZPNN, brightnessYNeg);
				renderBlocks.brightnessBottomLeft = getAoBrightness(renderBlocks.aoBrightnessXYNN, renderBlocks.aoBrightnessXYZNNN, renderBlocks.aoBrightnessYZNN, brightnessYNeg);
			} else {
				lightTopRight = renderBlocks.aoLightValueYNeg;
				lightBottomRight = renderBlocks.aoLightValueYNeg;
				lightBottomLeft = renderBlocks.aoLightValueYNeg;
				lightTopLeft = renderBlocks.aoLightValueYNeg;
				renderBlocks.brightnessTopLeft = renderBlocks.brightnessBottomLeft = renderBlocks.brightnessBottomRight = renderBlocks.brightnessTopRight = renderBlocks.aoBrightnessXYNN;
			}
	
			renderBlocks.colorRedTopLeft = renderBlocks.colorRedBottomLeft = renderBlocks.colorRedBottomRight = renderBlocks.colorRedTopRight = (tintBottom ? red : 1.0F) * 0.5F;
			renderBlocks.colorGreenTopLeft = renderBlocks.colorGreenBottomLeft = renderBlocks.colorGreenBottomRight = renderBlocks.colorGreenTopRight = (tintBottom ? green : 1.0F) * 0.5F;
			renderBlocks.colorBlueTopLeft = renderBlocks.colorBlueBottomLeft = renderBlocks.colorBlueBottomRight = renderBlocks.colorBlueTopRight = (tintBottom ? blue : 1.0F) * 0.5F;
			renderBlocks.colorRedTopLeft *= lightTopLeft;
			renderBlocks.colorGreenTopLeft *= lightTopLeft;
			renderBlocks.colorBlueTopLeft *= lightTopLeft;
			renderBlocks.colorRedBottomLeft *= lightBottomLeft;
			renderBlocks.colorGreenBottomLeft *= lightBottomLeft;
			renderBlocks.colorBlueBottomLeft *= lightBottomLeft;
			renderBlocks.colorRedBottomRight *= lightBottomRight;
			renderBlocks.colorGreenBottomRight *= lightBottomRight;
			renderBlocks.colorBlueBottomRight *= lightBottomRight;
			renderBlocks.colorRedTopRight *= lightTopRight;
			renderBlocks.colorGreenTopRight *= lightTopRight;
			renderBlocks.colorBlueTopRight *= lightTopRight;
			renderBlocks.renderBottomFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 0));
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y + 1, z, 1)) {
			if(renderBlocks.aoType > 0) {
				if(block.maxY >= 1.0D) {
				++y;
				}
	
				renderBlocks.aoBrightnessXYNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoBrightnessXYPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z);
				renderBlocks.aoBrightnessYZPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoBrightnessYZPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1);
				renderBlocks.aoLightValueScratchXYNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoLightValueScratchXYPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z);
				renderBlocks.aoLightValueScratchYZPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoLightValueScratchYZPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z + 1);
				if(!renderBlocks.aoGrassXYZCPN && !renderBlocks.aoGrassXYZNPC) {
					renderBlocks.aoLightValueScratchXYZNPN = renderBlocks.aoLightValueScratchXYNP;
					renderBlocks.aoBrightnessXYZNPN = renderBlocks.aoBrightnessXYNP;
				} else {
					renderBlocks.aoLightValueScratchXYZNPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z - 1);
					renderBlocks.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZCPN && !renderBlocks.aoGrassXYZPPC) {
					renderBlocks.aoLightValueScratchXYZPPN = renderBlocks.aoLightValueScratchXYPP;
					renderBlocks.aoBrightnessXYZPPN = renderBlocks.aoBrightnessXYPP;
				} else {
					renderBlocks.aoLightValueScratchXYZPPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z - 1);
					renderBlocks.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZCPP && !renderBlocks.aoGrassXYZNPC) {
					renderBlocks.aoLightValueScratchXYZNPP = renderBlocks.aoLightValueScratchXYNP;
					renderBlocks.aoBrightnessXYZNPP = renderBlocks.aoBrightnessXYNP;
				} else {
					renderBlocks.aoLightValueScratchXYZNPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z + 1);
					renderBlocks.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z + 1);
				}
	
				if(!renderBlocks.aoGrassXYZCPP && !renderBlocks.aoGrassXYZPPC) {
					renderBlocks.aoLightValueScratchXYZPPP = renderBlocks.aoLightValueScratchXYPP;
					renderBlocks.aoBrightnessXYZPPP = renderBlocks.aoBrightnessXYPP;
				} else {
					renderBlocks.aoLightValueScratchXYZPPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z + 1);
					renderBlocks.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z + 1);
				}
	
				if(block.maxY >= 1.0D) {
					--y;
				}
	
				lightTopRight = (renderBlocks.aoLightValueScratchXYZNPP + renderBlocks.aoLightValueScratchXYNP + renderBlocks.aoLightValueScratchYZPP + renderBlocks.aoLightValueYPos) / 4.0F;
				lightTopLeft = (renderBlocks.aoLightValueScratchYZPP + renderBlocks.aoLightValueYPos + renderBlocks.aoLightValueScratchXYZPPP + renderBlocks.aoLightValueScratchXYPP) / 4.0F;
				lightBottomLeft = (renderBlocks.aoLightValueYPos + renderBlocks.aoLightValueScratchYZPN + renderBlocks.aoLightValueScratchXYPP + renderBlocks.aoLightValueScratchXYZPPN) / 4.0F;
				lightBottomRight = (renderBlocks.aoLightValueScratchXYNP + renderBlocks.aoLightValueScratchXYZNPN + renderBlocks.aoLightValueYPos + renderBlocks.aoLightValueScratchYZPN) / 4.0F;
				renderBlocks.brightnessTopRight = getAoBrightness(renderBlocks.aoBrightnessXYZNPP, renderBlocks.aoBrightnessXYNP, renderBlocks.aoBrightnessYZPP, brightnessYPos);
				renderBlocks.brightnessTopLeft = getAoBrightness(renderBlocks.aoBrightnessYZPP, renderBlocks.aoBrightnessXYZPPP, renderBlocks.aoBrightnessXYPP, brightnessYPos);
				renderBlocks.brightnessBottomLeft = getAoBrightness(renderBlocks.aoBrightnessYZPN, renderBlocks.aoBrightnessXYPP, renderBlocks.aoBrightnessXYZPPN, brightnessYPos);
				renderBlocks.brightnessBottomRight = getAoBrightness(renderBlocks.aoBrightnessXYNP, renderBlocks.aoBrightnessXYZNPN, renderBlocks.aoBrightnessYZPN, brightnessYPos);
			} else {
				lightTopRight = renderBlocks.aoLightValueYPos;
				lightBottomRight = renderBlocks.aoLightValueYPos;
				lightBottomLeft = renderBlocks.aoLightValueYPos;
				lightTopLeft = renderBlocks.aoLightValueYPos;
				renderBlocks.brightnessTopLeft = renderBlocks.brightnessBottomLeft = renderBlocks.brightnessBottomRight = renderBlocks.brightnessTopRight = brightnessYPos;
			}
	
			renderBlocks.colorRedTopLeft = renderBlocks.colorRedBottomLeft = renderBlocks.colorRedBottomRight = renderBlocks.colorRedTopRight = tintTop ? red : 1.0F;
			renderBlocks.colorGreenTopLeft = renderBlocks.colorGreenBottomLeft = renderBlocks.colorGreenBottomRight = renderBlocks.colorGreenTopRight = tintTop ? green : 1.0F;
			renderBlocks.colorBlueTopLeft = renderBlocks.colorBlueBottomLeft = renderBlocks.colorBlueBottomRight = renderBlocks.colorBlueTopRight = tintTop ? blue : 1.0F;
			renderBlocks.colorRedTopLeft *= lightTopLeft;
			renderBlocks.colorGreenTopLeft *= lightTopLeft;
			renderBlocks.colorBlueTopLeft *= lightTopLeft;
			renderBlocks.colorRedBottomLeft *= lightBottomLeft;
			renderBlocks.colorGreenBottomLeft *= lightBottomLeft;
			renderBlocks.colorBlueBottomLeft *= lightBottomLeft;
			renderBlocks.colorRedBottomRight *= lightBottomRight;
			renderBlocks.colorGreenBottomRight *= lightBottomRight;
			renderBlocks.colorBlueBottomRight *= lightBottomRight;
			renderBlocks.colorRedTopRight *= lightTopRight;
			renderBlocks.colorGreenTopRight *= lightTopRight;
			renderBlocks.colorBlueTopRight *= lightTopRight;
			renderBlocks.renderTopFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 1));
			anyFace = true;
		}
	
		int sideTexture;
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z - 1, 2)) {
			if(renderBlocks.aoType > 0) {
				if(block.minZ <= 0.0D) {
				--z;
				}
	
				renderBlocks.aoLightValueScratchXZNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoLightValueScratchYZNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoLightValueScratchYZPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z);
				renderBlocks.aoLightValueScratchXZPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z);
				renderBlocks.aoBrightnessXZNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoBrightnessYZNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoBrightnessYZPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z);
				renderBlocks.aoBrightnessXZPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z);
				if(!renderBlocks.aoGrassXYZNCN && !renderBlocks.aoGrassXYZCNN) {
					renderBlocks.aoLightValueScratchXYZNNN = renderBlocks.aoLightValueScratchXZNN;
					renderBlocks.aoBrightnessXYZNNN = renderBlocks.aoBrightnessXZNN;
				} else {
					renderBlocks.aoLightValueScratchXYZNNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y - 1, z);
					renderBlocks.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y - 1, z);
				}
	
				if(!renderBlocks.aoGrassXYZNCN && !renderBlocks.aoGrassXYZCPN) {
					renderBlocks.aoLightValueScratchXYZNPN = renderBlocks.aoLightValueScratchXZNN;
					renderBlocks.aoBrightnessXYZNPN = renderBlocks.aoBrightnessXZNN;
				} else {
					renderBlocks.aoLightValueScratchXYZNPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y + 1, z);
					renderBlocks.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y + 1, z);
				}
	
				if(!renderBlocks.aoGrassXYZPCN && !renderBlocks.aoGrassXYZCNN) {
					renderBlocks.aoLightValueScratchXYZPNN = renderBlocks.aoLightValueScratchXZPN;
					renderBlocks.aoBrightnessXYZPNN = renderBlocks.aoBrightnessXZPN;
				} else {
					renderBlocks.aoLightValueScratchXYZPNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y - 1, z);
					renderBlocks.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y - 1, z);
				}
	
				if(!renderBlocks.aoGrassXYZPCN && !renderBlocks.aoGrassXYZCPN) {
					renderBlocks.aoLightValueScratchXYZPPN = renderBlocks.aoLightValueScratchXZPN;
					renderBlocks.aoBrightnessXYZPPN = renderBlocks.aoBrightnessXZPN;
				} else {
					renderBlocks.aoLightValueScratchXYZPPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y + 1, z);
					renderBlocks.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y + 1, z);
				}
	
				if(block.minZ <= 0.0D) {
					++z;
				}
	
				lightTopLeft = (renderBlocks.aoLightValueScratchXZNN + renderBlocks.aoLightValueScratchXYZNPN + renderBlocks.aoLightValueZNeg + renderBlocks.aoLightValueScratchYZPN) / 4.0F;
				lightBottomLeft = (renderBlocks.aoLightValueZNeg + renderBlocks.aoLightValueScratchYZPN + renderBlocks.aoLightValueScratchXZPN + renderBlocks.aoLightValueScratchXYZPPN) / 4.0F;
				lightBottomRight = (renderBlocks.aoLightValueScratchYZNN + renderBlocks.aoLightValueZNeg + renderBlocks.aoLightValueScratchXYZPNN + renderBlocks.aoLightValueScratchXZPN) / 4.0F;
				lightTopRight = (renderBlocks.aoLightValueScratchXYZNNN + renderBlocks.aoLightValueScratchXZNN + renderBlocks.aoLightValueScratchYZNN + renderBlocks.aoLightValueZNeg) / 4.0F;
				renderBlocks.brightnessTopLeft = getAoBrightness(renderBlocks.aoBrightnessXZNN, renderBlocks.aoBrightnessXYZNPN, renderBlocks.aoBrightnessYZPN, brightnessZNeg);
				renderBlocks.brightnessBottomLeft = getAoBrightness(renderBlocks.aoBrightnessYZPN, renderBlocks.aoBrightnessXZPN, renderBlocks.aoBrightnessXYZPPN, brightnessZNeg);
				renderBlocks.brightnessBottomRight = getAoBrightness(renderBlocks.aoBrightnessYZNN, renderBlocks.aoBrightnessXYZPNN, renderBlocks.aoBrightnessXZPN, brightnessZNeg);
				renderBlocks.brightnessTopRight = getAoBrightness(renderBlocks.aoBrightnessXYZNNN, renderBlocks.aoBrightnessXZNN, renderBlocks.aoBrightnessYZNN, brightnessZNeg);
			} else {
				lightTopRight = renderBlocks.aoLightValueZNeg;
				lightBottomRight = renderBlocks.aoLightValueZNeg;
				lightBottomLeft = renderBlocks.aoLightValueZNeg;
				lightTopLeft = renderBlocks.aoLightValueZNeg;
				renderBlocks.brightnessTopLeft = renderBlocks.brightnessBottomLeft = renderBlocks.brightnessBottomRight = renderBlocks.brightnessTopRight = brightnessZNeg;
			}
	
			renderBlocks.colorRedTopLeft = renderBlocks.colorRedBottomLeft = renderBlocks.colorRedBottomRight = renderBlocks.colorRedTopRight = (tintZNeg ? red : 1.0F) * 0.8F;
			renderBlocks.colorGreenTopLeft = renderBlocks.colorGreenBottomLeft = renderBlocks.colorGreenBottomRight = renderBlocks.colorGreenTopRight = (tintZNeg ? green : 1.0F) * 0.8F;
			renderBlocks.colorBlueTopLeft = renderBlocks.colorBlueBottomLeft = renderBlocks.colorBlueBottomRight = renderBlocks.colorBlueTopRight = (tintZNeg ? blue : 1.0F) * 0.8F;
			renderBlocks.colorRedTopLeft *= lightTopLeft;
			renderBlocks.colorGreenTopLeft *= lightTopLeft;
			renderBlocks.colorBlueTopLeft *= lightTopLeft;
			renderBlocks.colorRedBottomLeft *= lightBottomLeft;
			renderBlocks.colorGreenBottomLeft *= lightBottomLeft;
			renderBlocks.colorBlueBottomLeft *= lightBottomLeft;
			renderBlocks.colorRedBottomRight *= lightBottomRight;
			renderBlocks.colorGreenBottomRight *= lightBottomRight;
			renderBlocks.colorBlueBottomRight *= lightBottomRight;
			renderBlocks.colorRedTopRight *= lightTopRight;
			renderBlocks.colorGreenTopRight *= lightTopRight;
			renderBlocks.colorBlueTopRight *= lightTopRight;
			sideTexture = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 2);
			renderBlocks.renderEastFace(block, (double)x, (double)y, (double)z, sideTexture);
			if(RenderBlocks.fancyGrass && sideTexture == 3 && renderBlocks.overrideBlockTexture < 0) {
				renderBlocks.colorRedTopLeft *= red;
				renderBlocks.colorRedBottomLeft *= red;
				renderBlocks.colorRedBottomRight *= red;
				renderBlocks.colorRedTopRight *= red;
				renderBlocks.colorGreenTopLeft *= green;
				renderBlocks.colorGreenBottomLeft *= green;
				renderBlocks.colorGreenBottomRight *= green;
				renderBlocks.colorGreenTopRight *= green;
				renderBlocks.colorBlueTopLeft *= blue;
				renderBlocks.colorBlueBottomLeft *= blue;
				renderBlocks.colorBlueBottomRight *= blue;
				renderBlocks.colorBlueTopRight *= blue;
				renderBlocks.renderEastFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z + 1, 3)) {
			if(renderBlocks.aoType > 0) {
				if(block.maxZ >= 1.0D) {
				++z;
				}
	
				renderBlocks.aoLightValueScratchXZNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoLightValueScratchXZPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y, z);
				renderBlocks.aoLightValueScratchYZNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoLightValueScratchYZPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z);
				renderBlocks.aoBrightnessXZNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z);
				renderBlocks.aoBrightnessXZPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z);
				renderBlocks.aoBrightnessYZNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoBrightnessYZPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z);
				if(!renderBlocks.aoGrassXYZNCP && !renderBlocks.aoGrassXYZCNP) {
					renderBlocks.aoLightValueScratchXYZNNP = renderBlocks.aoLightValueScratchXZNP;
					renderBlocks.aoBrightnessXYZNNP = renderBlocks.aoBrightnessXZNP;
				} else {
					renderBlocks.aoLightValueScratchXYZNNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y - 1, z);
					renderBlocks.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y - 1, z);
				}
	
				if(!renderBlocks.aoGrassXYZNCP && !renderBlocks.aoGrassXYZCPP) {
					renderBlocks.aoLightValueScratchXYZNPP = renderBlocks.aoLightValueScratchXZNP;
					renderBlocks.aoBrightnessXYZNPP = renderBlocks.aoBrightnessXZNP;
				} else {
					renderBlocks.aoLightValueScratchXYZNPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x - 1, y + 1, z);
					renderBlocks.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y + 1, z);
				}
	
				if(!renderBlocks.aoGrassXYZPCP && !renderBlocks.aoGrassXYZCNP) {
					renderBlocks.aoLightValueScratchXYZPNP = renderBlocks.aoLightValueScratchXZPP;
					renderBlocks.aoBrightnessXYZPNP = renderBlocks.aoBrightnessXZPP;
				} else {
					renderBlocks.aoLightValueScratchXYZPNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y - 1, z);
					renderBlocks.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y - 1, z);
				}
	
				if(!renderBlocks.aoGrassXYZPCP && !renderBlocks.aoGrassXYZCPP) {
					renderBlocks.aoLightValueScratchXYZPPP = renderBlocks.aoLightValueScratchXZPP;
					renderBlocks.aoBrightnessXYZPPP = renderBlocks.aoBrightnessXZPP;
				} else {
					renderBlocks.aoLightValueScratchXYZPPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x + 1, y + 1, z);
					renderBlocks.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y + 1, z);
				}
	
				if(block.maxZ >= 1.0D) {
					--z;
				}
	
				lightTopLeft = (renderBlocks.aoLightValueScratchXZNP + renderBlocks.aoLightValueScratchXYZNPP + renderBlocks.aoLightValueZPos + renderBlocks.aoLightValueScratchYZPP) / 4.0F;
				lightTopRight = (renderBlocks.aoLightValueZPos + renderBlocks.aoLightValueScratchYZPP + renderBlocks.aoLightValueScratchXZPP + renderBlocks.aoLightValueScratchXYZPPP) / 4.0F;
				lightBottomRight = (renderBlocks.aoLightValueScratchYZNP + renderBlocks.aoLightValueZPos + renderBlocks.aoLightValueScratchXYZPNP + renderBlocks.aoLightValueScratchXZPP) / 4.0F;
				lightBottomLeft = (renderBlocks.aoLightValueScratchXYZNNP + renderBlocks.aoLightValueScratchXZNP + renderBlocks.aoLightValueScratchYZNP + renderBlocks.aoLightValueZPos) / 4.0F;
				renderBlocks.brightnessTopLeft = getAoBrightness(renderBlocks.aoBrightnessXZNP, renderBlocks.aoBrightnessXYZNPP, renderBlocks.aoBrightnessYZPP, brightnessZPos);
				renderBlocks.brightnessTopRight = getAoBrightness(renderBlocks.aoBrightnessYZPP, renderBlocks.aoBrightnessXZPP, renderBlocks.aoBrightnessXYZPPP, brightnessZPos);
				renderBlocks.brightnessBottomRight = getAoBrightness(renderBlocks.aoBrightnessYZNP, renderBlocks.aoBrightnessXYZPNP, renderBlocks.aoBrightnessXZPP, brightnessZPos);
				renderBlocks.brightnessBottomLeft = getAoBrightness(renderBlocks.aoBrightnessXYZNNP, renderBlocks.aoBrightnessXZNP, renderBlocks.aoBrightnessYZNP, brightnessZPos);
			} else {
				lightTopRight = renderBlocks.aoLightValueZPos;
				lightBottomRight = renderBlocks.aoLightValueZPos;
				lightBottomLeft = renderBlocks.aoLightValueZPos;
				lightTopLeft = renderBlocks.aoLightValueZPos;
				renderBlocks.brightnessTopLeft = renderBlocks.brightnessBottomLeft = renderBlocks.brightnessBottomRight = renderBlocks.brightnessTopRight = brightnessZPos;
			}
	
			renderBlocks.colorRedTopLeft = renderBlocks.colorRedBottomLeft = renderBlocks.colorRedBottomRight = renderBlocks.colorRedTopRight = (tintZPos ? red : 1.0F) * 0.8F;
			renderBlocks.colorGreenTopLeft = renderBlocks.colorGreenBottomLeft = renderBlocks.colorGreenBottomRight = renderBlocks.colorGreenTopRight = (tintZPos ? green : 1.0F) * 0.8F;
			renderBlocks.colorBlueTopLeft = renderBlocks.colorBlueBottomLeft = renderBlocks.colorBlueBottomRight = renderBlocks.colorBlueTopRight = (tintZPos ? blue : 1.0F) * 0.8F;
			renderBlocks.colorRedTopLeft *= lightTopLeft;
			renderBlocks.colorGreenTopLeft *= lightTopLeft;
			renderBlocks.colorBlueTopLeft *= lightTopLeft;
			renderBlocks.colorRedBottomLeft *= lightBottomLeft;
			renderBlocks.colorGreenBottomLeft *= lightBottomLeft;
			renderBlocks.colorBlueBottomLeft *= lightBottomLeft;
			renderBlocks.colorRedBottomRight *= lightBottomRight;
			renderBlocks.colorGreenBottomRight *= lightBottomRight;
			renderBlocks.colorBlueBottomRight *= lightBottomRight;
			renderBlocks.colorRedTopRight *= lightTopRight;
			renderBlocks.colorGreenTopRight *= lightTopRight;
			renderBlocks.colorBlueTopRight *= lightTopRight;
			sideTexture = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 3);
			renderBlocks.renderWestFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 3));
			if(RenderBlocks.fancyGrass && sideTexture == 3 && renderBlocks.overrideBlockTexture < 0) {
				renderBlocks.colorRedTopLeft *= red;
				renderBlocks.colorRedBottomLeft *= red;
				renderBlocks.colorRedBottomRight *= red;
				renderBlocks.colorRedTopRight *= red;
				renderBlocks.colorGreenTopLeft *= green;
				renderBlocks.colorGreenBottomLeft *= green;
				renderBlocks.colorGreenBottomRight *= green;
				renderBlocks.colorGreenTopRight *= green;
				renderBlocks.colorBlueTopLeft *= blue;
				renderBlocks.colorBlueBottomLeft *= blue;
				renderBlocks.colorBlueBottomRight *= blue;
				renderBlocks.colorBlueTopRight *= blue;
				renderBlocks.renderWestFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x - 1, y, z, 4)) {
			if(renderBlocks.aoType > 0) {
				if(block.minX <= 0.0D) {
				--x;
				}
	
				renderBlocks.aoLightValueScratchXYNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoLightValueScratchXZNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoLightValueScratchXZNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z + 1);
				renderBlocks.aoLightValueScratchXYNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z);
				renderBlocks.aoBrightnessXYNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoBrightnessXZNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoBrightnessXZNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1);
				renderBlocks.aoBrightnessXYNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z);
				if(!renderBlocks.aoGrassXYZNCN && !renderBlocks.aoGrassXYZNNC) {
					renderBlocks.aoLightValueScratchXYZNNN = renderBlocks.aoLightValueScratchXZNN;
					renderBlocks.aoBrightnessXYZNNN = renderBlocks.aoBrightnessXZNN;
				} else {
					renderBlocks.aoLightValueScratchXYZNNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z - 1);
					renderBlocks.aoBrightnessXYZNNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZNCP && !renderBlocks.aoGrassXYZNNC) {
					renderBlocks.aoLightValueScratchXYZNNP = renderBlocks.aoLightValueScratchXZNP;
					renderBlocks.aoBrightnessXYZNNP = renderBlocks.aoBrightnessXZNP;
				} else {
					renderBlocks.aoLightValueScratchXYZNNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z + 1);
					renderBlocks.aoBrightnessXYZNNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z + 1);
				}
	
				if(!renderBlocks.aoGrassXYZNCN && !renderBlocks.aoGrassXYZNPC) {
					renderBlocks.aoLightValueScratchXYZNPN = renderBlocks.aoLightValueScratchXZNN;
					renderBlocks.aoBrightnessXYZNPN = renderBlocks.aoBrightnessXZNN;
				} else {
					renderBlocks.aoLightValueScratchXYZNPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z - 1);
					renderBlocks.aoBrightnessXYZNPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZNCP && !renderBlocks.aoGrassXYZNPC) {
					renderBlocks.aoLightValueScratchXYZNPP = renderBlocks.aoLightValueScratchXZNP;
					renderBlocks.aoBrightnessXYZNPP = renderBlocks.aoBrightnessXZNP;
				} else {
					renderBlocks.aoLightValueScratchXYZNPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z + 1);
					renderBlocks.aoBrightnessXYZNPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z + 1);
				}
	
				if(block.minX <= 0.0D) {
					++x;
				}
	
				lightTopRight = (renderBlocks.aoLightValueScratchXYNN + renderBlocks.aoLightValueScratchXYZNNP + renderBlocks.aoLightValueXNeg + renderBlocks.aoLightValueScratchXZNP) / 4.0F;
				lightTopLeft = (renderBlocks.aoLightValueXNeg + renderBlocks.aoLightValueScratchXZNP + renderBlocks.aoLightValueScratchXYNP + renderBlocks.aoLightValueScratchXYZNPP) / 4.0F;
				lightBottomLeft = (renderBlocks.aoLightValueScratchXZNN + renderBlocks.aoLightValueXNeg + renderBlocks.aoLightValueScratchXYZNPN + renderBlocks.aoLightValueScratchXYNP) / 4.0F;
				lightBottomRight = (renderBlocks.aoLightValueScratchXYZNNN + renderBlocks.aoLightValueScratchXYNN + renderBlocks.aoLightValueScratchXZNN + renderBlocks.aoLightValueXNeg) / 4.0F;
				renderBlocks.brightnessTopRight = getAoBrightness(renderBlocks.aoBrightnessXYNN, renderBlocks.aoBrightnessXYZNNP, renderBlocks.aoBrightnessXZNP, brightnessXNeg);
				renderBlocks.brightnessTopLeft = getAoBrightness(renderBlocks.aoBrightnessXZNP, renderBlocks.aoBrightnessXYNP, renderBlocks.aoBrightnessXYZNPP, brightnessXNeg);
				renderBlocks.brightnessBottomLeft = getAoBrightness(renderBlocks.aoBrightnessXZNN, renderBlocks.aoBrightnessXYZNPN, renderBlocks.aoBrightnessXYNP, brightnessXNeg);
				renderBlocks.brightnessBottomRight = getAoBrightness(renderBlocks.aoBrightnessXYZNNN, renderBlocks.aoBrightnessXYNN, renderBlocks.aoBrightnessXZNN, brightnessXNeg);
			} else {
				lightTopRight = renderBlocks.aoLightValueXNeg;
				lightBottomRight = renderBlocks.aoLightValueXNeg;
				lightBottomLeft = renderBlocks.aoLightValueXNeg;
				lightTopLeft = renderBlocks.aoLightValueXNeg;
				renderBlocks.brightnessTopLeft = renderBlocks.brightnessBottomLeft = renderBlocks.brightnessBottomRight = renderBlocks.brightnessTopRight = brightnessXNeg;
			}
	
			renderBlocks.colorRedTopLeft = renderBlocks.colorRedBottomLeft = renderBlocks.colorRedBottomRight = renderBlocks.colorRedTopRight = (tintXNeg ? red : 1.0F) * 0.6F;
			renderBlocks.colorGreenTopLeft = renderBlocks.colorGreenBottomLeft = renderBlocks.colorGreenBottomRight = renderBlocks.colorGreenTopRight = (tintXNeg ? green : 1.0F) * 0.6F;
			renderBlocks.colorBlueTopLeft = renderBlocks.colorBlueBottomLeft = renderBlocks.colorBlueBottomRight = renderBlocks.colorBlueTopRight = (tintXNeg ? blue : 1.0F) * 0.6F;
			renderBlocks.colorRedTopLeft *= lightTopLeft;
			renderBlocks.colorGreenTopLeft *= lightTopLeft;
			renderBlocks.colorBlueTopLeft *= lightTopLeft;
			renderBlocks.colorRedBottomLeft *= lightBottomLeft;
			renderBlocks.colorGreenBottomLeft *= lightBottomLeft;
			renderBlocks.colorBlueBottomLeft *= lightBottomLeft;
			renderBlocks.colorRedBottomRight *= lightBottomRight;
			renderBlocks.colorGreenBottomRight *= lightBottomRight;
			renderBlocks.colorBlueBottomRight *= lightBottomRight;
			renderBlocks.colorRedTopRight *= lightTopRight;
			renderBlocks.colorGreenTopRight *= lightTopRight;
			renderBlocks.colorBlueTopRight *= lightTopRight;
			sideTexture = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 4);
			renderBlocks.renderNorthFace(block, (double)x, (double)y, (double)z, sideTexture);
			if(RenderBlocks.fancyGrass && sideTexture == 3 && renderBlocks.overrideBlockTexture < 0) {
				renderBlocks.colorRedTopLeft *= red;
				renderBlocks.colorRedBottomLeft *= red;
				renderBlocks.colorRedBottomRight *= red;
				renderBlocks.colorRedTopRight *= red;
				renderBlocks.colorGreenTopLeft *= green;
				renderBlocks.colorGreenBottomLeft *= green;
				renderBlocks.colorGreenBottomRight *= green;
				renderBlocks.colorGreenTopRight *= green;
				renderBlocks.colorBlueTopLeft *= blue;
				renderBlocks.colorBlueBottomLeft *= blue;
				renderBlocks.colorBlueBottomRight *= blue;
				renderBlocks.colorBlueTopRight *= blue;
				renderBlocks.renderNorthFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x + 1, y, z, 5)) {
			if(renderBlocks.aoType > 0) {
				if(block.maxX >= 1.0D) {
				++x;
				}
	
				renderBlocks.aoLightValueScratchXYPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoLightValueScratchXZPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoLightValueScratchXZPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y, z + 1);
				renderBlocks.aoLightValueScratchXYPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z);
				renderBlocks.aoBrightnessXYPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z);
				renderBlocks.aoBrightnessXZPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1);
				renderBlocks.aoBrightnessXZPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1);
				renderBlocks.aoBrightnessXYPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z);
				if(!renderBlocks.aoGrassXYZPNC && !renderBlocks.aoGrassXYZPCN) {
					renderBlocks.aoLightValueScratchXYZPNN = renderBlocks.aoLightValueScratchXZPN;
					renderBlocks.aoBrightnessXYZPNN = renderBlocks.aoBrightnessXZPN;
				} else {
					renderBlocks.aoLightValueScratchXYZPNN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z - 1);
					renderBlocks.aoBrightnessXYZPNN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZPNC && !renderBlocks.aoGrassXYZPCP) {
					renderBlocks.aoLightValueScratchXYZPNP = renderBlocks.aoLightValueScratchXZPP;
					renderBlocks.aoBrightnessXYZPNP = renderBlocks.aoBrightnessXZPP;
				} else {
					renderBlocks.aoLightValueScratchXYZPNP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y - 1, z + 1);
					renderBlocks.aoBrightnessXYZPNP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z + 1);
				}
	
				if(!renderBlocks.aoGrassXYZPPC && !renderBlocks.aoGrassXYZPCN) {
					renderBlocks.aoLightValueScratchXYZPPN = renderBlocks.aoLightValueScratchXZPN;
					renderBlocks.aoBrightnessXYZPPN = renderBlocks.aoBrightnessXZPN;
				} else {
					renderBlocks.aoLightValueScratchXYZPPN = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z - 1);
					renderBlocks.aoBrightnessXYZPPN = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z - 1);
				}
	
				if(!renderBlocks.aoGrassXYZPPC && !renderBlocks.aoGrassXYZPCP) {
					renderBlocks.aoLightValueScratchXYZPPP = renderBlocks.aoLightValueScratchXZPP;
					renderBlocks.aoBrightnessXYZPPP = renderBlocks.aoBrightnessXZPP;
				} else {
					renderBlocks.aoLightValueScratchXYZPPP = block.getAmbientOcclusionLightValue(renderBlocks.blockAccess, x, y + 1, z + 1);
					renderBlocks.aoBrightnessXYZPPP = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z + 1);
				}
	
				if(block.maxX >= 1.0D) {
					--x;
				}
	
				lightTopLeft = (renderBlocks.aoLightValueScratchXYPN + renderBlocks.aoLightValueScratchXYZPNP + renderBlocks.aoLightValueXPos + renderBlocks.aoLightValueScratchXZPP) / 4.0F;
				lightTopRight = (renderBlocks.aoLightValueXPos + renderBlocks.aoLightValueScratchXZPP + renderBlocks.aoLightValueScratchXYPP + renderBlocks.aoLightValueScratchXYZPPP) / 4.0F;
				lightBottomRight = (renderBlocks.aoLightValueScratchXZPN + renderBlocks.aoLightValueXPos + renderBlocks.aoLightValueScratchXYZPPN + renderBlocks.aoLightValueScratchXYPP) / 4.0F;
				lightBottomLeft = (renderBlocks.aoLightValueScratchXYZPNN + renderBlocks.aoLightValueScratchXYPN + renderBlocks.aoLightValueScratchXZPN + renderBlocks.aoLightValueXPos) / 4.0F;
				renderBlocks.brightnessTopLeft = getAoBrightness(renderBlocks.aoBrightnessXYPN, renderBlocks.aoBrightnessXYZPNP, renderBlocks.aoBrightnessXZPP, brightnessXPos);
				renderBlocks.brightnessTopRight = getAoBrightness(renderBlocks.aoBrightnessXZPP, renderBlocks.aoBrightnessXYPP, renderBlocks.aoBrightnessXYZPPP, brightnessXPos);
				renderBlocks.brightnessBottomRight = getAoBrightness(renderBlocks.aoBrightnessXZPN, renderBlocks.aoBrightnessXYZPPN, renderBlocks.aoBrightnessXYPP, brightnessXPos);
				renderBlocks.brightnessBottomLeft = getAoBrightness(renderBlocks.aoBrightnessXYZPNN, renderBlocks.aoBrightnessXYPN, renderBlocks.aoBrightnessXZPN, brightnessXPos);
			} else {
				lightTopRight = renderBlocks.aoLightValueXPos;
				lightBottomRight = renderBlocks.aoLightValueXPos;
				lightBottomLeft = renderBlocks.aoLightValueXPos;
				lightTopLeft = renderBlocks.aoLightValueXPos;
				renderBlocks.brightnessTopLeft = renderBlocks.brightnessBottomLeft = renderBlocks.brightnessBottomRight = renderBlocks.brightnessTopRight = brightnessXPos;
			}
	
			renderBlocks.colorRedTopLeft = renderBlocks.colorRedBottomLeft = renderBlocks.colorRedBottomRight = renderBlocks.colorRedTopRight = (tintXPos ? red : 1.0F) * 0.6F;
			renderBlocks.colorGreenTopLeft = renderBlocks.colorGreenBottomLeft = renderBlocks.colorGreenBottomRight = renderBlocks.colorGreenTopRight = (tintXPos ? green : 1.0F) * 0.6F;
			renderBlocks.colorBlueTopLeft = renderBlocks.colorBlueBottomLeft = renderBlocks.colorBlueBottomRight = renderBlocks.colorBlueTopRight = (tintXPos ? blue : 1.0F) * 0.6F;
			renderBlocks.colorRedTopLeft *= lightTopLeft;
			renderBlocks.colorGreenTopLeft *= lightTopLeft;
			renderBlocks.colorBlueTopLeft *= lightTopLeft;
			renderBlocks.colorRedBottomLeft *= lightBottomLeft;
			renderBlocks.colorGreenBottomLeft *= lightBottomLeft;
			renderBlocks.colorBlueBottomLeft *= lightBottomLeft;
			renderBlocks.colorRedBottomRight *= lightBottomRight;
			renderBlocks.colorGreenBottomRight *= lightBottomRight;
			renderBlocks.colorBlueBottomRight *= lightBottomRight;
			renderBlocks.colorRedTopRight *= lightTopRight;
			renderBlocks.colorGreenTopRight *= lightTopRight;
			renderBlocks.colorBlueTopRight *= lightTopRight;
			sideTexture = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 5);
			renderBlocks.renderSouthFace(block, (double)x, (double)y, (double)z, sideTexture);
			if(RenderBlocks.fancyGrass && sideTexture == 3 && renderBlocks.overrideBlockTexture < 0) {
				renderBlocks.colorRedTopLeft *= red;
				renderBlocks.colorRedBottomLeft *= red;
				renderBlocks.colorRedBottomRight *= red;
				renderBlocks.colorRedTopRight *= red;
				renderBlocks.colorGreenTopLeft *= green;
				renderBlocks.colorGreenBottomLeft *= green;
				renderBlocks.colorGreenBottomRight *= green;
				renderBlocks.colorGreenTopRight *= green;
				renderBlocks.colorBlueTopLeft *= blue;
				renderBlocks.colorBlueBottomLeft *= blue;
				renderBlocks.colorBlueBottomRight *= blue;
				renderBlocks.colorBlueTopRight *= blue;
				renderBlocks.renderSouthFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		renderBlocks.enableAO = false;
		return anyFace;
	}

	/**
	 * Blends three packed brightness samples with a fallback (the face's own light) and
	 * averages their four components. Sample values of 0 (opaque unlit cells the AO pass
	 * did not fill in) are replaced by the fallback first. The mask 16711935 keeps only
	 * the green/alpha bytes (0x00FF00FF).
	 */
	static int getAoBrightness(int a, int b, int c, int fallback) {
		if(a == 0) {
			a = fallback;
		}
	
		if(b == 0) {
			b = fallback;
		}
	
		if(c == 0) {
			c = fallback;
		}
	
		return a + b + c + fallback >> 2 & 16711935;
	}

	/**
	 * Renders the cube with a uniform per-face color (no corner interpolation). The
	 * per-face color is the diffuse shade times the tint (top face carries the full
	 * red/green/blue tint; the other faces are darkened). For grass the sides add the
	 * fancy overlay pass (a second overlaid quad with grass-edge texture 38).
	 */
	public static boolean renderStandardBlockWithColorMultiplier(RenderBlocks renderBlocks, Block block, int x, int y, int z, float red, float green, float blue) {
		renderBlocks.enableAO = false;
		Tessellator tessellator = Tessellator.instance;
		boolean anyFace = false;
		float shadeBottom = 0.5F;
		float shadeTop = 1.0F;
		float shadeEastWest = 0.8F;
		float shadeNorthSouth = 0.6F;
		float topRed = shadeTop * red;
		float topGreen = shadeTop * green;
		float topBlue = shadeTop * blue;
		float bottomRed = shadeBottom;
		float eastWestRed = shadeEastWest;
		float northSouthRed = shadeNorthSouth;
		float bottomGreen = shadeBottom;
		float eastWestGreen = shadeEastWest;
		float northSouthGreen = shadeNorthSouth;
		float bottomBlue = shadeBottom;
		float eastWestBlue = shadeEastWest;
		float northSouthBlue = shadeNorthSouth;
		if(block != Block.grass) {
			bottomRed = shadeBottom * red;
			eastWestRed = shadeEastWest * red;
			northSouthRed = shadeNorthSouth * red;
			bottomGreen = shadeBottom * green;
			eastWestGreen = shadeEastWest * green;
			northSouthGreen = shadeNorthSouth * green;
			bottomBlue = shadeBottom * blue;
			eastWestBlue = shadeEastWest * blue;
			northSouthBlue = shadeNorthSouth * blue;
		}
	
		int brightnessSelf = block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z);
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y - 1, z, 0)) {
			tessellator.setBrightness(block.minY > 0.0D ? brightnessSelf : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y - 1, z));
			tessellator.setColorOpaque_F(bottomRed, bottomGreen, bottomBlue);
			renderBlocks.renderBottomFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 0));
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y + 1, z, 1)) {
			tessellator.setBrightness(block.maxY < 1.0D ? brightnessSelf : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y + 1, z));
			tessellator.setColorOpaque_F(topRed, topGreen, topBlue);
			renderBlocks.renderTopFace(block, (double)x, (double)y, (double)z, block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 1));
			anyFace = true;
		}
	
		int textureId;
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z - 1, 2)) {
			tessellator.setBrightness(block.minZ > 0.0D ? brightnessSelf : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z - 1));
			tessellator.setColorOpaque_F(eastWestRed, eastWestGreen, eastWestBlue);
			textureId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 2);
			renderBlocks.renderEastFace(block, (double)x, (double)y, (double)z, textureId);
			if(RenderBlocks.fancyGrass && textureId == 3 && renderBlocks.overrideBlockTexture < 0) {
				tessellator.setColorOpaque_F(eastWestRed * red, eastWestGreen * green, eastWestBlue * blue);
				renderBlocks.renderEastFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x, y, z + 1, 3)) {
			tessellator.setBrightness(block.maxZ < 1.0D ? brightnessSelf : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z + 1));
			tessellator.setColorOpaque_F(eastWestRed, eastWestGreen, eastWestBlue);
			textureId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 3);
			renderBlocks.renderWestFace(block, (double)x, (double)y, (double)z, textureId);
			if(RenderBlocks.fancyGrass && textureId == 3 && renderBlocks.overrideBlockTexture < 0) {
				tessellator.setColorOpaque_F(eastWestRed * red, eastWestGreen * green, eastWestBlue * blue);
				renderBlocks.renderWestFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x - 1, y, z, 4)) {
			tessellator.setBrightness(block.minX > 0.0D ? brightnessSelf : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x - 1, y, z));
			tessellator.setColorOpaque_F(northSouthRed, northSouthGreen, northSouthBlue);
			textureId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 4);
			renderBlocks.renderNorthFace(block, (double)x, (double)y, (double)z, textureId);
			if(RenderBlocks.fancyGrass && textureId == 3 && renderBlocks.overrideBlockTexture < 0) {
				tessellator.setColorOpaque_F(northSouthRed * red, northSouthGreen * green, northSouthBlue * blue);
				renderBlocks.renderNorthFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		if(renderBlocks.renderAllFaces || block.shouldSideBeRendered(renderBlocks.blockAccess, x + 1, y, z, 5)) {
			tessellator.setBrightness(block.maxX < 1.0D ? brightnessSelf : block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x + 1, y, z));
			tessellator.setColorOpaque_F(northSouthRed, northSouthGreen, northSouthBlue);
			textureId = block.getBlockTexture(renderBlocks.blockAccess, x, y, z, 5);
			renderBlocks.renderSouthFace(block, (double)x, (double)y, (double)z, textureId);
			if(RenderBlocks.fancyGrass && textureId == 3 && renderBlocks.overrideBlockTexture < 0) {
				tessellator.setColorOpaque_F(northSouthRed * red, northSouthGreen * green, northSouthBlue * blue);
				renderBlocks.renderSouthFace(block, (double)x, (double)y, (double)z, 38);
			}
	
			anyFace = true;
		}
	
		return anyFace;
	}

	/**
	 * Renders a crossed-squares X-shaped plane (the classic "plant" look) at the given
	 * block, applying the biome tint (with anaglyph remapping). Tall grass gets a
	 * deterministic per-cell scatter offset derived from a simple hash of its position.
	 */
	public static boolean renderCrossedSquares(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(block.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
		int color = block.colorMultiplier(renderBlocks.blockAccess, x, y, z);
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;
		if(EntityRenderer.anaglyphEnable) {
			float anagRed = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
			float anagGreen = (red * 30.0F + green * 70.0F) / 100.0F;
			float anagBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
			red = anagRed;
			green = anagGreen;
			blue = anagBlue;
		}
	
		tessellator.setColorOpaque_F(red, green, blue);
		double xOffset = (double)x;
		double yOffset = (double)y;
		double zOffset = (double)z;
		if(block == Block.tallGrass) {
			long hashSeed = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
			hashSeed = hashSeed * hashSeed * 42317861L + hashSeed * 11L;
			xOffset += ((double)((float)(hashSeed >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
			yOffset += ((double)((float)(hashSeed >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
			zOffset += ((double)((float)(hashSeed >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
		}
	
		renderCrossedSquares(renderBlocks, block, renderBlocks.blockAccess.getBlockMetadata(x, y, z), xOffset, yOffset, zOffset);
		return true;
	}

	/**
	 * Emits the two crossed quads for a plant, centered on (x+0.5, y..y+1, z+0.5) with a
	 * slight inset (0.45 corners) so neighboring plants don't z-fight. The tile is the
	 * full 16x16 texture cell of the block's metadata. No colors are set here; the caller
	 * is expected to have applied the tint.
	 */
	public static void renderCrossedSquares(RenderBlocks renderBlocks, Block block, int metadata, double x, double y, double z) {
		Tessellator tessellator = Tessellator.instance;
		int textureId = block.getBlockTextureFromSideAndMetadata(0, metadata);
		if(renderBlocks.overrideBlockTexture >= 0) {
			textureId = renderBlocks.overrideBlockTexture;
		}
	
		AtlasTexel.calc(textureId, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		double uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileX);
		double uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileX + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileY);
		double vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileY + TextureAtlas.TERRAIN.tileSpan);
		double xLow = x + 0.5D - (double)0.45F;
		double xHigh = x + 0.5D + (double)0.45F;
		double zLow = z + 0.5D - (double)0.45F;
		double zHigh = z + 0.5D + (double)0.45F;
		tessellator.addVertexWithUV(xLow, y + 1.0D, zLow, uLo, vLo);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zLow, uLo, vHi);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zHigh, uHi, vHi);
		tessellator.addVertexWithUV(xHigh, y + 1.0D, zHigh, uHi, vLo);
		tessellator.addVertexWithUV(xHigh, y + 1.0D, zHigh, uLo, vLo);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zHigh, uLo, vHi);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zLow, uHi, vHi);
		tessellator.addVertexWithUV(xLow, y + 1.0D, zLow, uHi, vLo);
		tessellator.addVertexWithUV(xLow, y + 1.0D, zHigh, uLo, vLo);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zHigh, uLo, vHi);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zLow, uHi, vHi);
		tessellator.addVertexWithUV(xHigh, y + 1.0D, zLow, uHi, vLo);
		tessellator.addVertexWithUV(xHigh, y + 1.0D, zLow, uLo, vLo);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zLow, uLo, vHi);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zHigh, uHi, vHi);
		tessellator.addVertexWithUV(xLow, y + 1.0D, zHigh, uHi, vLo);
	}

	/**
	 * Like {@link #renderCrossedSquares(RenderBlocks, Block, int, double, double, double)}
	 * but the quads span two blocks in height (for double-tall plants).
	 */
	public static void renderCrossedSquaresDoubleHeight(RenderBlocks renderBlocks, Block block, int metadata, double x, double y, double z) {
		Tessellator tessellator = Tessellator.instance;
		int textureId = block.getBlockTextureFromSideAndMetadata(0, metadata);
		if(renderBlocks.overrideBlockTexture >= 0) {
			textureId = renderBlocks.overrideBlockTexture;
		}
	
		AtlasTexel.calc(textureId, TextureAtlas.TERRAIN);
		int tileX = AtlasTexel.u;
		int tileY = AtlasTexel.v;
		double uLo = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileX);
		double uHi = (double)TexelScale.u(TextureAtlas.TERRAIN, (float)tileX + TextureAtlas.TERRAIN.tileSpan);
		double vLo = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileY);
		double vHi = (double)TexelScale.v(TextureAtlas.TERRAIN, (float)tileY + TextureAtlas.TERRAIN.tileSpan);
		double xLow = x + 0.5D - (double)0.45F;
		double xHigh = x + 0.5D + (double)0.45F;
		double zLow = z + 0.5D - (double)0.45F;
		double zHigh = z + 0.5D + (double)0.45F;
		tessellator.addVertexWithUV(xLow, y + 2.0D, zLow, uLo, vLo);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zLow, uLo, vHi);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zHigh, uHi, vHi);
		tessellator.addVertexWithUV(xHigh, y + 2.0D, zHigh, uHi, vLo);
		tessellator.addVertexWithUV(xHigh, y + 2.0D, zHigh, uLo, vLo);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zHigh, uLo, vHi);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zLow, uHi, vHi);
		tessellator.addVertexWithUV(xLow, y + 2.0D, zLow, uHi, vLo);
		tessellator.addVertexWithUV(xLow, y + 2.0D, zHigh, uLo, vLo);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zHigh, uLo, vHi);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zLow, uHi, vHi);
		tessellator.addVertexWithUV(xHigh, y + 2.0D, zLow, uHi, vLo);
		tessellator.addVertexWithUV(xHigh, y + 2.0D, zLow, uLo, vLo);
		tessellator.addVertexWithUV(xHigh, y + 0.0D, zLow, uLo, vHi);
		tessellator.addVertexWithUV(xLow, y + 0.0D, zHigh, uHi, vHi);
		tessellator.addVertexWithUV(xLow, y + 2.0D, zHigh, uHi, vLo);
	}

	/**
	 * Sets the per-face diffuse shade and brightness for a block (used by direction-aware
	 * handlers); legacy wrapper retained for the modded renderers.
	 */
	public static void setLightValue(Tessellator tessellator, IBlockAccess blockAccess, Block block, float x, float y, float z, float factor) {
		/*
		float f;
		if (blockAccess == null) f = factor; else f = block.getBlockBrightness(blockAccess, (int) x, (int) y, (int) z) * factor;
		if (Block.lightValue[block.blockID] > 0) f = factor;
		tessellator.setColorOpaque_F(f, f, f);
		*/
		
		tessellator.setBrightness(block.getMixedBrightnessForBlock(blockAccess, (int)x, (int)y, (int)z));
		tessellator.setColorOpaque_F(factor, factor, factor);
	}


	/** Directly writes the six bounds of a block (used to convince the face emitters to
	 *  draw sub-block geometry without a full bounds-setup pass). */
	static void setBounds(Block block, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		block.minX = minX;
		block.minY = minY;
		block.minZ = minZ;
		block.maxX = maxX;
		block.maxY = maxY;
		block.maxZ = maxZ;
	}

	/** Restores a block to the full unit cube. */
	static void resetBounds(Block block) {
		setBounds(block, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	/** Draws the six faces of the block's item cube, each tinted uniformly by the block's render
	 *  color (grass green, etc.) and scaled by {@code brightness} — matching the original
	 *  inventory renderer, which applied {@code renderColor * brightness} across every face
	 *  rather than per-face directional shading. */
	static void renderCubeOnInventory(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		Tessellator tessellator = Tessellator.instance;
		block.setBlockBoundsForItemRender();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		for(int side = 0; side < 6; ++side) {
			tessellator.startDrawingQuads();
			float[] normal = RenderBlocks.SIDE_NORMALS[side];
			tessellator.setNormal(normal[0], normal[1], normal[2]);
			float shade = side == 0 ? 0.5F
			             : side == 1 ? 1.0F
			             : side == 2 || side == 3 ? 0.8F
			             : 0.6F;
			applyInventoryColor(renderBlocks, block, metadata, brightness * shade);
			renderBlocks.renderFace(block, side, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(side, metadata));
			tessellator.draw();
		}
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	/** Sets the tessellator's current color to the block's render color (tint) multiplied by
	 * {@code brightness}. This mirrors the original inventory renderer's
	 * {@code GL11.glColor4f(renderColor * brightness)} which the vertex-array tessellator
	 * ignores; the color must instead be pushed through {@link Tessellator#setColorOpaque_F}. */
	public static void applyInventoryColor(RenderBlocks renderBlocks, Block block, int metadata, float brightness) {
		float red = 1.0F;
		float green = 1.0F;
		float blue = 1.0F;
		if(renderBlocks.useInventoryTint) {
			int tint = block.getRenderColor(metadata);
			red = (float) (tint >> 16 & 255) / 255.0F;
			green = (float) (tint >> 8 & 255) / 255.0F;
			blue = (float) (tint & 255) / 255.0F;
		}
		Tessellator.instance.setColorOpaque_F(red * brightness, green * brightness, blue * brightness);
	}

	/** Draws the six faces of the block's item cube, each with white color scaled by
	 * {@code brightness}. The four vertical faces are rendered slightly inset (by 0.0625
	 * world units) so the item reads as a solid cube rather than a hollow shell. Used by
	 * blocks like furnaces or chests that render as block-type items in the inventory.
	 *
	 * @param brightness 1.0 for full brightness; lower values darken the item
	 */
	static void renderInsetCubeOnInventory(RenderBlocks renderBlocks, Block block, float brightness) {
		Tessellator tessellator = Tessellator.instance;
		float amount = 0.0625F;
		block.setBlockBoundsForItemRender();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_F(brightness, brightness, brightness);
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderBlocks.renderFace(block, 0, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(0));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_F(brightness, brightness, brightness);
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderBlocks.renderFace(block, 1, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(1));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_F(brightness, brightness, brightness);
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addTranslation(0.0F, 0.0F, amount);
		renderBlocks.renderFace(block, 2, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(2));
		tessellator.addTranslation(0.0F, 0.0F, -amount);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_F(brightness, brightness, brightness);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addTranslation(0.0F, 0.0F, -amount);
		renderBlocks.renderFace(block, 3, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(3));
		tessellator.addTranslation(0.0F, 0.0F, amount);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_F(brightness, brightness, brightness);
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addTranslation(amount, 0.0F, 0.0F);
		renderBlocks.renderFace(block, 4, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(4));
		tessellator.addTranslation(-amount, 0.0F, 0.0F);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_F(brightness, brightness, brightness);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		tessellator.addTranslation(-amount, 0.0F, 0.0F);
		renderBlocks.renderFace(block, 5, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(5));
		tessellator.addTranslation(amount, 0.0F, 0.0F);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
