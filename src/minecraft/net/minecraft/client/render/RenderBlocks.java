package net.minecraft.client.render;

import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.block.BlockRenderType;
import net.minecraft.client.render.block.RenderBlockUtil;
import net.minecraft.game.world.block.BlockPistonBase;
import net.minecraft.game.world.block.BlockPistonExtension;

/**
 * The central block-rendering engine. Renders a single block into the current
 * {@link Tessellator} using the block's {@link Block#getRenderType()} to dispatch
 * to a per-type handler via {@link BlockRenderType}. This class also exposes the
 * six low-level face emitters ({@link #renderBottomFace} ... {@link #renderSouthFace})
 * that the handlers call, and the shared AO/lighting state those emitters read.
 *
 * <p>The six face emitters are the workhorses: each emits a single quad for one side
 * of the block's current bounding box ({@link Block#minX} .. {@link Block#maxX}, etc.)
 * at the given world position, mapping the face onto the 16x16 tile selected by
 * <code>texId</code>. The emitters support per-face UV rotation ({@link #uvRotateBottom}
 * etc., used by pistons and oriented blocks) and ambient occlusion interpolation when
 * {@link #enableAO} is set.</p>
 *
 * <p>Render-type dispatch: {@link #renderBlockByRenderType} looks up the handler in
 * {@link BlockRenderType} and delegates; this keeps the engine small while handlers
 * keep their geometry. See the {@code net.minecraft.client.render.block} package for
 * the individual handlers.</p>
 */
public class RenderBlocks {
	/** World access used to read block metadata/lighting during rendering. */
	public IBlockAccess blockAccess;
	/** When &gt;= 0, an override texture index that replaces the default face texture. */
	public int overrideBlockTexture = -1;
	/** When true, mirrors the face's texture coordinates on the U axis. */
	public boolean flipTexture = false;
	/** When true, every handler renders all six faces (used for full-block item previews). */
	public boolean renderAllFaces = false;
	/** When true, grass uses the fancy (overlay-fill) rendering path. */
	public static boolean fancyGrass = false;
	/** When true, block-texture tinting is applied for inventory items. */
	public boolean useInventoryTint = true;
	/** UV rotation (0-3 quarter turns) applied to the East, West, South and North faces respectively. */
	public int uvRotateEast = 0;
	public int uvRotateWest = 0;
	public int uvRotateSouth = 0;
	public int uvRotateNorth = 0;
	/** UV rotation applied to the Top and Bottom faces respectively. */
	public int uvRotateTop = 0;
	public int uvRotateBottom = 0;
	/** When true, face emitters interpolate per-vertex color/brightness (ambient occlusion). */
	public boolean enableAO;
	/** Computed per-'own' light value for the block being rendered (see RenderBlockUtil). */
	/** Computed per-'own' light value for the block being rendered (see RenderBlockUtil). */
	public float lightValueOwn;
	/** Ambient-occlusion light values of the six neighbours (XNeg/YNeg/ZNeg/XPos/YPos/ZPos). */
	public float aoLightValueXNeg;
	public float aoLightValueYNeg;
	public float aoLightValueZNeg;
	public float aoLightValueXPos;
	public float aoLightValueYPos;
	public float aoLightValueZPos;
	/**
	 * Scratched AO sample values. The suffix encodes which of the three adjacent planes the
	 * sample belongs to (P = +1, N = -1 along that axis, or a 2-axis plane like "XY" when one
	 * axis is omitted). Consumers are RenderBlockUtil's ambient-occlusion pass.
	 */
	public float aoLightValueScratchXYZNNN;
	public float aoLightValueScratchXYNN;
	public float aoLightValueScratchXYZNNP;
	public float aoLightValueScratchYZNN;
	public float aoLightValueScratchYZNP;
	public float aoLightValueScratchXYZPNN;
	public float aoLightValueScratchXYPN;
	public float aoLightValueScratchXYZPNP;
	public float aoLightValueScratchXYZNPN;
	public float aoLightValueScratchXYNP;
	public float aoLightValueScratchXYZNPP;
	public float aoLightValueScratchYZPN;
	public float aoLightValueScratchXYZPPN;
	public float aoLightValueScratchXYPP;
	public float aoLightValueScratchYZPP;
	public float aoLightValueScratchXYZPPP;
	public float aoLightValueScratchXZNN;
	public float aoLightValueScratchXZPN;
	public float aoLightValueScratchXZNP;
	public float aoLightValueScratchXZPP;
	/** Packed (day|night) brightness values matching the {@code aoLightValueScratch*} samples. */
	public int aoBrightnessXYZNNN;
	public int aoBrightnessXYNN;
	public int aoBrightnessXYZNNP;
	public int aoBrightnessYZNN;
	public int aoBrightnessYZNP;
	public int aoBrightnessXYZPNN;
	public int aoBrightnessXYPN;
	public int aoBrightnessXYZPNP;
	public int aoBrightnessXYZNPN;
	public int aoBrightnessXYNP;
	public int aoBrightnessXYZNPP;
	public int aoBrightnessYZPN;
	public int aoBrightnessXYZPPN;
	public int aoBrightnessXYPP;
	public int aoBrightnessYZPP;
	public int aoBrightnessXYZPPP;
	public int aoBrightnessXZNN;
	public int aoBrightnessXZPN;
	public int aoBrightnessXZNP;
	public int aoBrightnessXZPP;
	/** AO mode: 0 = interpolated quads, 1 = classic corner-vertex pass (see RenderBlockUtil). */
	public int aoType = 1;
	/** Per-corner packed brightness for the face currently being emitted (AO path). */
	public int brightnessTopLeft;
	public int brightnessBottomLeft;
	public int brightnessBottomRight;
	public int brightnessTopRight;
	/** Per-corner RGB color for the face currently being emitted (AO path). */
	public float colorRedTopLeft;
	public float colorRedBottomLeft;
	public float colorRedBottomRight;
	public float colorRedTopRight;
	public float colorGreenTopLeft;
	public float colorGreenBottomLeft;
	public float colorGreenBottomRight;
	public float colorGreenTopRight;
	public float colorBlueTopLeft;
	public float colorBlueBottomLeft;
	public float colorBlueBottomRight;
	public float colorBlueTopRight;
	/** AO bitset for grass-fill corners (P/N = +1/-1, C = center plane). */
	public boolean aoGrassXYZCPN;
	public boolean aoGrassXYZPPC;
	public boolean aoGrassXYZNPC;
	public boolean aoGrassXYZCPP;
	public boolean aoGrassXYZNCN;
	public boolean aoGrassXYZPCP;
	public boolean aoGrassXYZNCP;
	public boolean aoGrassXYZPCN;
	public boolean aoGrassXYZCNN;
	public boolean aoGrassXYZPNC;
	public boolean aoGrassXYZNNC;
	public boolean aoGrassXYZCNP;

	private int activeRenderPass = 0;
	
	public static final float[] SHADE_PER_FACE = new float [] { 0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F };

	/**
	 * Per-signal-level RGB colors for redstone dust, indexed by power (0-15). Built in the
	 * {@code static} initializer below: R ramps up from 0.4, G/B fade in at higher levels.
	 */
	public static float[][] redstoneColors = new float[16][];

	/** Offset of the six neighbours in axis order (bottom, top, north, south, west, east). */
	public static final int[][] NEIGHBOR_OFFSETS = new int[][] { { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 },
			{ -1, 0, 0 }, { 1, 0, 0 } };
	/** Diffuse light multiplier for each face so the cube looks shaded (bottom, top, N, S, W, E). */
	public static final float[] SIDE_LIGHT = new float[] { 0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F };
	/** Unit normal for each face (same axis order as {@link #SIDE_LIGHT}). */
	public static final float[][] SIDE_NORMALS = new float[][] { { 0.0F, -1.0F, 0.0F }, { 0.0F, 1.0F, 0.0F },
			{ 0.0F, 0.0F, -1.0F }, { 0.0F, 0.0F, 1.0F }, { -1.0F, 0.0F, 0.0F }, { 1.0F, 0.0F, 0.0F } };

	/** Constructs a renderer bound to the given world access. */
	public RenderBlocks(IBlockAccess worldAccess) {
		this.blockAccess = worldAccess;
	}

	/** Constructs an unbound renderer (used for inventory/item previews). */
	public RenderBlocks() {
	}

	/** @return the currently active render pass (see stage 0 / GL linear-attention passes). */
	public int getActiveRenderPass() {
		return activeRenderPass;
	}

	/** Sets the active render pass, consumed by handlers that split geometry across passes. */
	public void setActiveRenderPass(int activeRenderPass) {
		this.activeRenderPass = activeRenderPass;
	}

	/** @return the world access this renderer is currently bound to. */
	public IBlockAccess getBlockAccess() {
		return this.blockAccess;
	}

	/**
	 * Renders the given block using a specific texture index on every face
	 * (temporarily overrides {@link #overrideBlockTexture}).
	 */
	public void renderBlockUsingTexture(Block block, int x, int y, int z, int textureId) {
		this.overrideBlockTexture = textureId;
		this.renderBlockByRenderType(block, x, y, z);
		this.overrideBlockTexture = -1;
	}

	/** Renders the given block with full occlusion for the duration (culled faces drawn too). */
	public void renderBlockAllFaces(Block block, int x, int y, int z) {
		this.renderAllFaces = true;
		this.renderBlockByRenderType(block, x, y, z);
		this.renderAllFaces = false;
	}

	/**
	 * Primary dispatch point: looks up the handler registered for
	 * {@link Block#getRenderType()} in {@link BlockRenderType} and hands the block to it.
	 *
	 * @return true if a handler was found (and thus geometry emitted)
	 */
	public boolean renderBlockByRenderType(Block block, int x, int y, int z) {
		BlockRenderType type = BlockRenderType.get(block.getRenderType());
		block.setBlockBoundsBasedOnState(this.blockAccess, x, y, z);
		return type == null ? false : type.handler().renderBlock(this, block, x, y, z);
	}

	/** Renders the piston body as if fully extended (all faces, head forced on). */
	public void renderPistonBaseAllFaces(Block block, int x, int y, int z) {
		this.renderAllFaces = true;
		this.renderPistonBase(block, x, y, z, true);
		this.renderAllFaces = false;
	}

	/**
	 * Renders the static piston body. Interprets the block metadata (bit 3 = extended) and the
	 * orientation to reshape the block bounds (the piston slides out along one of the six axes)
	 * and to rotate the face textures accordingly via the UV-rotation state before delegating to
	 * the standard cube rendering path.
	 *
	 * @param forceExtended render as fully extended regardless of metadata
	 */
	public boolean renderPistonBase(Block block, int x, int y, int z, boolean forceExtended) {
		int metadata = this.blockAccess.getBlockMetadata(x, y, z);
		boolean isExtended = forceExtended || (metadata & 8) != 0;
		int orientation = BlockPistonBase.getOrientation(metadata);
		if (isExtended) {
			switch (orientation) {
			case 0:
				this.uvRotateEast = 3;
				this.uvRotateWest = 3;
				this.uvRotateSouth = 3;
				this.uvRotateNorth = 3;
				block.setBlockBounds(0.0F, 0.25F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
			case 1:
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
				break;
			case 2:
				this.uvRotateSouth = 1;
				this.uvRotateNorth = 2;
				block.setBlockBounds(0.0F, 0.0F, 0.25F, 1.0F, 1.0F, 1.0F);
				break;
			case 3:
				this.uvRotateSouth = 2;
				this.uvRotateNorth = 1;
				this.uvRotateTop = 3;
				this.uvRotateBottom = 3;
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.75F);
				break;
			case 4:
				this.uvRotateEast = 1;
				this.uvRotateWest = 2;
				this.uvRotateTop = 2;
				this.uvRotateBottom = 1;
				block.setBlockBounds(0.25F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
			case 5:
				this.uvRotateEast = 2;
				this.uvRotateWest = 1;
				this.uvRotateTop = 1;
				this.uvRotateBottom = 2;
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 0.75F, 1.0F, 1.0F);
			}

			this.renderStandardBlock(block, x, y, z);
			this.uvRotateEast = 0;
			this.uvRotateWest = 0;
			this.uvRotateSouth = 0;
			this.uvRotateNorth = 0;
			this.uvRotateTop = 0;
			this.uvRotateBottom = 0;
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		} else {
			switch (orientation) {
			case 0:
				this.uvRotateEast = 3;
				this.uvRotateWest = 3;
				this.uvRotateSouth = 3;
				this.uvRotateNorth = 3;
			case 1:
			default:
				break;
			case 2:
				this.uvRotateSouth = 1;
				this.uvRotateNorth = 2;
				break;
			case 3:
				this.uvRotateSouth = 2;
				this.uvRotateNorth = 1;
				this.uvRotateTop = 3;
				this.uvRotateBottom = 3;
				break;
			case 4:
				this.uvRotateEast = 1;
				this.uvRotateWest = 2;
				this.uvRotateTop = 2;
				this.uvRotateBottom = 1;
				break;
			case 5:
				this.uvRotateEast = 2;
				this.uvRotateWest = 1;
				this.uvRotateTop = 1;
				this.uvRotateBottom = 2;
			}

			this.renderStandardBlock(block, x, y, z);
			this.uvRotateEast = 0;
			this.uvRotateWest = 0;
			this.uvRotateSouth = 0;
			this.uvRotateNorth = 0;
			this.uvRotateTop = 0;
			this.uvRotateBottom = 0;
		}

		return true;
	}

	/**
	 * Piston rod helper. Emits the four edges of the retracted/shifted piston
	 * shaft as thin quads, mapping them onto the piston-arm texture tile (texture 108).
	 * The rod spans (x1..x2, y1..y2, z1..z2); the caller passes the already-offset
	 * extents so the rod appears at its current extension. <code>shade</code> is the
	 * per-rod diffuse brightness.
	 *
	 * @param rodPixels length of the rod in texture pixels (16 when extended, 8 when retracted)
	 */
	private void renderPistonRodUD(double x1, double x2, double y1, double y2, double z1, double z2, float shade,
			double rodPixels) {
		int texId = 108;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		Tessellator tessellator = Tessellator.instance;
		double uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) (tileU + 0));
		double vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) (tileV + 0));
		double uHi = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + rodPixels - 0.01D);
		double vHi = TexelScale.vd(TextureAtlas.TERRAIN, (double) ((float) tileV + 4.0F) - 0.01D);
		tessellator.setColorOpaque_F(shade, shade, shade);
		tessellator.addVertexWithUV(x1, y2, z1, uHi, vLo);
		tessellator.addVertexWithUV(x1, y1, z1, uLo, vLo);
		tessellator.addVertexWithUV(x2, y1, z2, uLo, vHi);
		tessellator.addVertexWithUV(x2, y2, z2, uHi, vHi);
	}

	/** Piston rod helper for the north/south-facing shaft; see {@link #renderPistonRodUD}. */
	private void renderPistonRodSN(double x1, double x2, double y1, double y2, double z1, double z2, float shade,
			double rodPixels) {
		int texId = 108;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		Tessellator tessellator = Tessellator.instance;
		double uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) (tileU + 0));
		double vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) (tileV + 0));
		double uHi = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + rodPixels - 0.01D);
		double vHi = TexelScale.vd(TextureAtlas.TERRAIN, (double) ((float) tileV + 4.0F) - 0.01D);
		tessellator.setColorOpaque_F(shade, shade, shade);
		tessellator.addVertexWithUV(x1, y1, z2, uHi, vLo);
		tessellator.addVertexWithUV(x1, y1, z1, uLo, vLo);
		tessellator.addVertexWithUV(x2, y2, z1, uLo, vHi);
		tessellator.addVertexWithUV(x2, y2, z2, uHi, vHi);
	}

	/** Piston rod helper for the east/west-facing shaft; see {@link #renderPistonRodUD}. */
	private void renderPistonRodEW(double x1, double x2, double y1, double y2, double z1, double z2, float shade,
			double rodPixels) {
		int texId = 108;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		Tessellator tessellator = Tessellator.instance;
		double uLo = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) (tileU + 0));
		double vLo = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) (tileV + 0));
		double uHi = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + rodPixels - 0.01D);
		double vHi = TexelScale.vd(TextureAtlas.TERRAIN, (double) ((float) tileV + 4.0F) - 0.01D);
		tessellator.setColorOpaque_F(shade, shade, shade);
		tessellator.addVertexWithUV(x2, y1, z1, uHi, vLo);
		tessellator.addVertexWithUV(x1, y1, z1, uLo, vLo);
		tessellator.addVertexWithUV(x1, y2, z2, uLo, vHi);
		tessellator.addVertexWithUV(x2, y2, z2, uHi, vHi);
	}

	/** Renders the moving piston head as if fully extended. */
	public void renderPistonExtensionAllFaces(Block block, int x, int y, int z, boolean extended) {
		this.renderAllFaces = true;
		this.renderPistonExtension(block, x, y, z, extended);
		this.renderAllFaces = false;
	}

	/**
	 * Renders the moving piston <em>head</em> (the block that slides out). The metadata
	 * stores the push direction (0-5 = down, up, north, south, west, east); the body of the
	 * block is shrunk to a 1/4-tall/thick slab on that side and the shaft drawn by the three
	 * rod helpers, with texture rotated so the arm faces read correctly. When
	 * <code>extended</code> is false the shaft is half-length (8 px) so the block sits flush.
	 */
	public boolean renderPistonExtension(Block block, int x, int y, int z, boolean extended) {
		int metadata = this.blockAccess.getBlockMetadata(x, y, z);
		int direction = BlockPistonExtension.getDirectionMeta(metadata);
		float brightness = block.getBlockBrightness(this.blockAccess, x, y, z);
		float rodOffset = extended ? 1.0F : 0.5F;
		double rodPixels = extended ? (double) TextureAtlas.TILE : 8.0D;
		switch (direction) {
		case 0:
			this.uvRotateEast = 3;
			this.uvRotateWest = 3;
			this.uvRotateSouth = 3;
			this.uvRotateNorth = 3;
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
			this.renderStandardBlock(block, x, y, z);
			this.renderPistonRodUD((double) ((float) x + 0.375F), (double) ((float) x + 0.625F),
					(double) ((float) y + 0.25F), (double) ((float) y + 0.25F + rodOffset), (double) ((float) z + 0.625F),
					(double) ((float) z + 0.625F), brightness * 0.8F, rodPixels);
			this.renderPistonRodUD((double) ((float) x + 0.625F), (double) ((float) x + 0.375F),
					(double) ((float) y + 0.25F), (double) ((float) y + 0.25F + rodOffset), (double) ((float) z + 0.375F),
					(double) ((float) z + 0.375F), brightness * 0.8F, rodPixels);
			this.renderPistonRodUD((double) ((float) x + 0.375F), (double) ((float) x + 0.375F),
					(double) ((float) y + 0.25F), (double) ((float) y + 0.25F + rodOffset), (double) ((float) z + 0.375F),
					(double) ((float) z + 0.625F), brightness * 0.6F, rodPixels);
			this.renderPistonRodUD((double) ((float) x + 0.625F), (double) ((float) x + 0.625F),
					(double) ((float) y + 0.25F), (double) ((float) y + 0.25F + rodOffset), (double) ((float) z + 0.625F),
					(double) ((float) z + 0.375F), brightness * 0.6F, rodPixels);
			break;
		case 1:
			block.setBlockBounds(0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F);
			this.renderStandardBlock(block, x, y, z);
			this.renderPistonRodUD((double) ((float) x + 0.375F), (double) ((float) x + 0.625F),
					(double) ((float) y - 0.25F + 1.0F - rodOffset), (double) ((float) y - 0.25F + 1.0F),
					(double) ((float) z + 0.625F), (double) ((float) z + 0.625F), brightness * 0.8F, rodPixels);
			this.renderPistonRodUD((double) ((float) x + 0.625F), (double) ((float) x + 0.375F),
					(double) ((float) y - 0.25F + 1.0F - rodOffset), (double) ((float) y - 0.25F + 1.0F),
					(double) ((float) z + 0.375F), (double) ((float) z + 0.375F), brightness * 0.8F, rodPixels);
			this.renderPistonRodUD((double) ((float) x + 0.375F), (double) ((float) x + 0.375F),
					(double) ((float) y - 0.25F + 1.0F - rodOffset), (double) ((float) y - 0.25F + 1.0F),
					(double) ((float) z + 0.375F), (double) ((float) z + 0.625F), brightness * 0.6F, rodPixels);
			this.renderPistonRodUD((double) ((float) x + 0.625F), (double) ((float) x + 0.625F),
					(double) ((float) y - 0.25F + 1.0F - rodOffset), (double) ((float) y - 0.25F + 1.0F),
					(double) ((float) z + 0.625F), (double) ((float) z + 0.375F), brightness * 0.6F, rodPixels);
			break;
		case 2:
			this.uvRotateSouth = 1;
			this.uvRotateNorth = 2;
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.25F);
			this.renderStandardBlock(block, x, y, z);
			this.renderPistonRodSN((double) ((float) x + 0.375F), (double) ((float) x + 0.375F),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.375F), (double) ((float) z + 0.25F),
					(double) ((float) z + 0.25F + rodOffset), brightness * 0.6F, rodPixels);
			this.renderPistonRodSN((double) ((float) x + 0.625F), (double) ((float) x + 0.625F),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.625F), (double) ((float) z + 0.25F),
					(double) ((float) z + 0.25F + rodOffset), brightness * 0.6F, rodPixels);
			this.renderPistonRodSN((double) ((float) x + 0.375F), (double) ((float) x + 0.625F),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.375F), (double) ((float) z + 0.25F),
					(double) ((float) z + 0.25F + rodOffset), brightness * 0.5F, rodPixels);
			this.renderPistonRodSN((double) ((float) x + 0.625F), (double) ((float) x + 0.375F),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.625F), (double) ((float) z + 0.25F),
					(double) ((float) z + 0.25F + rodOffset), brightness, rodPixels);
			break;
		case 3:
			this.uvRotateSouth = 2;
			this.uvRotateNorth = 1;
			this.uvRotateTop = 3;
			this.uvRotateBottom = 3;
			block.setBlockBounds(0.0F, 0.0F, 0.75F, 1.0F, 1.0F, 1.0F);
			this.renderStandardBlock(block, x, y, z);
			this.renderPistonRodSN((double) ((float) x + 0.375F), (double) ((float) x + 0.375F),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.375F),
					(double) ((float) z - 0.25F + 1.0F - rodOffset), (double) ((float) z - 0.25F + 1.0F), brightness * 0.6F, rodPixels);
			this.renderPistonRodSN((double) ((float) x + 0.625F), (double) ((float) x + 0.625F),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.625F),
					(double) ((float) z - 0.25F + 1.0F - rodOffset), (double) ((float) z - 0.25F + 1.0F), brightness * 0.6F, rodPixels);
			this.renderPistonRodSN((double) ((float) x + 0.375F), (double) ((float) x + 0.625F),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.375F),
					(double) ((float) z - 0.25F + 1.0F - rodOffset), (double) ((float) z - 0.25F + 1.0F), brightness * 0.5F, rodPixels);
			this.renderPistonRodSN((double) ((float) x + 0.625F), (double) ((float) x + 0.375F),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.625F),
					(double) ((float) z - 0.25F + 1.0F - rodOffset), (double) ((float) z - 0.25F + 1.0F), brightness, rodPixels);
			break;
		case 4:
			this.uvRotateEast = 1;
			this.uvRotateWest = 2;
			this.uvRotateTop = 2;
			this.uvRotateBottom = 1;
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 0.25F, 1.0F, 1.0F);
			this.renderStandardBlock(block, x, y, z);
			this.renderPistonRodEW((double) ((float) x + 0.25F), (double) ((float) x + 0.25F + rodOffset),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.375F), (double) ((float) z + 0.625F),
					(double) ((float) z + 0.375F), brightness * 0.5F, rodPixels);
			this.renderPistonRodEW((double) ((float) x + 0.25F), (double) ((float) x + 0.25F + rodOffset),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.625F), (double) ((float) z + 0.375F),
					(double) ((float) z + 0.625F), brightness, rodPixels);
			this.renderPistonRodEW((double) ((float) x + 0.25F), (double) ((float) x + 0.25F + rodOffset),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.625F), (double) ((float) z + 0.375F),
					(double) ((float) z + 0.375F), brightness * 0.6F, rodPixels);
			this.renderPistonRodEW((double) ((float) x + 0.25F), (double) ((float) x + 0.25F + rodOffset),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.375F), (double) ((float) z + 0.625F),
					(double) ((float) z + 0.625F), brightness * 0.6F, rodPixels);
			break;
		case 5:
			this.uvRotateEast = 2;
			this.uvRotateWest = 1;
			this.uvRotateTop = 1;
			this.uvRotateBottom = 2;
			block.setBlockBounds(0.75F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			this.renderStandardBlock(block, x, y, z);
			this.renderPistonRodEW((double) ((float) x - 0.25F + 1.0F - rodOffset), (double) ((float) x - 0.25F + 1.0F),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.375F), (double) ((float) z + 0.625F),
					(double) ((float) z + 0.375F), brightness * 0.5F, rodPixels);
			this.renderPistonRodEW((double) ((float) x - 0.25F + 1.0F - rodOffset), (double) ((float) x - 0.25F + 1.0F),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.625F), (double) ((float) z + 0.375F),
					(double) ((float) z + 0.625F), brightness, rodPixels);
			this.renderPistonRodEW((double) ((float) x - 0.25F + 1.0F - rodOffset), (double) ((float) x - 0.25F + 1.0F),
					(double) ((float) y + 0.375F), (double) ((float) y + 0.625F), (double) ((float) z + 0.375F),
					(double) ((float) z + 0.375F), brightness * 0.6F, rodPixels);
			this.renderPistonRodEW((double) ((float) x - 0.25F + 1.0F - rodOffset), (double) ((float) x - 0.25F + 1.0F),
					(double) ((float) y + 0.625F), (double) ((float) y + 0.375F), (double) ((float) z + 0.625F),
					(double) ((float) z + 0.625F), brightness * 0.6F, rodPixels);
		}

		this.uvRotateEast = 0;
		this.uvRotateWest = 0;
		this.uvRotateSouth = 0;
		this.uvRotateNorth = 0;
		this.uvRotateTop = 0;
		this.uvRotateBottom = 0;
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		return true;
	}

	public boolean renderCrossedSquares(Block block, int x, int y, int z) {
		return RenderBlockUtil.renderCrossedSquares(this, block, x, y, z);
	}

	public void renderCrossedSquares(Block block, int metadata, double x, double y, double z) {
		RenderBlockUtil.renderCrossedSquares(this, block, metadata, x, y, z);
	}

	public void renderCrossedSquaresDoubleHeight(Block block, int metadata, double x, double y, double z) {
		RenderBlockUtil.renderCrossedSquaresDoubleHeight(this, block, metadata, x, y, z);
	}

	/**
	 * Renders falling sand / gravel for the render pass, centered on (-0.5, -0.5, -0.5) so the
	 * falling entity is drawn around its origin. Uses the classic cube shading:
	 * bottom 0.5, top 1.0, east/west 0.8, north/south 0.6.
	 */
	public void renderBlockFallingSand(Block block, World world, int x, int y, int z) {
		float shadeBottom = 0.5F;
		float shadeTop = 1.0F;
		float shadeEastWest = 0.8F;
		float shadeNorthSouth = 0.6F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

		tessellator.setColorOpaque_F(shadeBottom, shadeBottom, shadeBottom);
		this.renderBottomFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(0));

		tessellator.setColorOpaque_F(shadeTop, shadeTop, shadeTop);
		this.renderTopFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(1));

		tessellator.setColorOpaque_F(shadeEastWest, shadeEastWest, shadeEastWest);
		this.renderEastFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(2));

		tessellator.setColorOpaque_F(shadeEastWest, shadeEastWest, shadeEastWest);
		this.renderWestFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(3));

		tessellator.setColorOpaque_F(shadeNorthSouth, shadeNorthSouth, shadeNorthSouth);
		this.renderNorthFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(4));

		tessellator.setColorOpaque_F(shadeNorthSouth, shadeNorthSouth, shadeNorthSouth);
		this.renderSouthFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(5));
		tessellator.draw();
	}

	/** Standard cube renderer; see {@link RenderBlockUtil#renderStandardBlock}. */
	public boolean renderStandardBlock(Block block, int x, int y, int z) {
		return RenderBlockUtil.renderStandardBlock(this, block, x, y, z);
	}

	/**
	 * Standard cube renderer with per-vertex ambient occlusion; the packed corner
	 * AO/light values must already be in {@link #aoBrightnessTopLeft} etc.
	 */
	public boolean renderStandardBlockWithAmbientOcclusion(Block block, int x, int y, int z, float red, float green,
			float blue) {
		return RenderBlockUtil.renderStandardBlockWithAmbientOcclusion(this, block, x, y, z, red, green, blue);
	}

	/** Standard cube renderer with a uniform color multiplier applied to each face. */
	public boolean renderStandardBlockWithColorMultiplier(Block block, int x, int y, int z, float red, float green,
			float blue) {
		return RenderBlockUtil.renderStandardBlockWithColorMultiplier(this, block, x, y, z, red, green, blue);
	}

	/**
	 * Emits a single face of the block's bounding box at (x, y, z) with the given texture.
	 *
	 * @param side 0-5 = bottom, top, east, west, north, south (see face emitters below)
	 */
	public void renderFace(Block block, int side, double x, double y, double z, int texId) {
		switch (side) {
		case 0:
			this.renderBottomFace(block, x, y, z, texId);
			break;
		case 1:
			this.renderTopFace(block, x, y, z, texId);
			break;
		case 2:
			this.renderEastFace(block, x, y, z, texId);
			break;
		case 3:
			this.renderWestFace(block, x, y, z, texId);
			break;
		case 4:
			this.renderNorthFace(block, x, y, z, texId);
			break;
		case 5:
			this.renderSouthFace(block, x, y, z, texId);
			break;
		}
	}

	/**
	 * Emits the bottom (-Y) face of the block's bounds at height {@code y + block.minY}.
	 * One of six face emitters that all block renderers ultimately funnel into.
	 *
	 * <p>Corner-naming shared by every emitter:
	 * <code>xMin/xMax</code>, <code>yMin/yMax</code> (horizontal faces use
	 * <code>yFloor</code>/<code>yCeiling</code>) and <code>zMin/zMax</code> are the block's
	 * current bounds at the given origin.</p>
	 *
	 * <p>UV-naming shared by every emitter: the four corners carry (uN, vN) with N = 1..4,
	 * in the same order the vertices are emitted — here 1=(xMin,zMax), 2=(xMin,zMin),
	 * 3=(xMax,zMin), 4=(xMax,zMax). The pairs begin as u2/u4/v2/v4 computed from the block
	 * bounds mapped onto the selected 16x16 tile, then u1/u3/v1/v3 are copied so that the
	 * {@code uvRotate*} quarter-turns (1 = +90&deg;, 2 = 180&deg;, 3 = 270&deg;) can permute
	 * them consistently. The 0.01 texel offset on the "high" edge hides texture bleeding.</p>
	 *
	 * <p>With {@link #enableAO} the corner colors and packed brightness are taken from the
	 * {@code color*}/<code>brightness*</code> state fields set up by RenderBlockUtil.</p>
	 */
	public void renderBottomFace(Block block, double x, double y, double z, int texId) {
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minX * (double) TextureAtlas.TILE);
		double u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxX * (double) TextureAtlas.TILE - 0.01D);
		double v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minZ * (double) TextureAtlas.TILE);
		double v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxZ * (double) TextureAtlas.TILE - 0.01D);
		if (block.minX < 0.0D || block.maxX > 1.0D) {
			u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + 0.0F);
			u4 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + 0.0F);
			v4 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		double u3 = u4;
		double u1 = u2;
		double v3 = v2;
		double v1 = v4;
		if (this.uvRotateBottom == 2) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minZ * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxZ * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			v3 = v2;
			v1 = v4;
			u3 = u2;
			u1 = u4;
			v2 = v4;
			v4 = v3;
		} else if (this.uvRotateBottom == 1) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxX * (double) TextureAtlas.TILE);
			u3 = u4;
			u1 = u2;
			u2 = u4;
			u4 = u1;
			v3 = v4;
			v1 = v2;
		} else if (this.uvRotateBottom == 3) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE - 0.01D);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE - 0.01D);
			u3 = u4;
			u1 = u2;
			v3 = v2;
			v1 = v4;
		}

		double xMin = x + block.minX;
		double xMax = x + block.maxX;
		double yFloor = y + block.minY;
		double zMin = z + block.minZ;
		double zMax = z + block.maxZ;
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.setBrightness(this.brightnessTopLeft);
			tessellator.addVertexWithUV(xMin, yFloor, zMax, u1, v1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.setBrightness(this.brightnessBottomLeft);
			tessellator.addVertexWithUV(xMin, yFloor, zMin, u2, v2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight,
					this.colorBlueBottomRight);
			tessellator.setBrightness(this.brightnessBottomLeft);
			tessellator.addVertexWithUV(xMax, yFloor, zMin, u3, v3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.setBrightness(this.brightnessTopRight);
			tessellator.addVertexWithUV(xMax, yFloor, zMax, u4, v4);
		} else {
			tessellator.addVertexWithUV(xMin, yFloor, zMax, u1, v1);
			tessellator.addVertexWithUV(xMin, yFloor, zMin, u2, v2);
			tessellator.addVertexWithUV(xMax, yFloor, zMin, u3, v3);
			tessellator.addVertexWithUV(xMax, yFloor, zMax, u4, v4);
		}

	}

	/** Emits the top (+Y) face at height {@code y + block.maxY}; see {@link #renderBottomFace}. */
	public void renderTopFace(Block block, double x, double y, double z, int texId) {
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minX * (double) TextureAtlas.TILE);
		double u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxX * (double) TextureAtlas.TILE - 0.01D);
		double v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minZ * (double) TextureAtlas.TILE);
		double v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxZ * (double) TextureAtlas.TILE - 0.01D);
		if (block.minX < 0.0D || block.maxX > 1.0D) {
			u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + 0.0F);
			u4 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + 0.0F);
			v4 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		double u3 = u4;
		double u1 = u2;
		double v3 = v2;
		double v1 = v4;
		if (this.uvRotateTop == 1) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minZ * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxZ * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			v3 = v2;
			v1 = v4;
			u3 = u2;
			u1 = u4;
			v2 = v4;
			v4 = v3;
		} else if (this.uvRotateTop == 2) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxX * (double) TextureAtlas.TILE);
			u3 = u4;
			u1 = u2;
			u2 = u4;
			u4 = u1;
			v3 = v4;
			v1 = v2;
		} else if (this.uvRotateTop == 3) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE - 0.01D);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE - 0.01D);
			u3 = u4;
			u1 = u2;
			v3 = v2;
			v1 = v4;
		}

		double xMin = x + block.minX;
		double xMax = x + block.maxX;
		double yCeiling = y + block.maxY;
		double zMin = z + block.minZ;
		double zMax = z + block.maxZ;
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.setBrightness(this.brightnessTopLeft);
			tessellator.addVertexWithUV(xMax, yCeiling, zMax, u4, v4);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.setBrightness(this.brightnessBottomLeft);
			tessellator.addVertexWithUV(xMax, yCeiling, zMin, u3, v3);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight,
					this.colorBlueBottomRight);
			tessellator.setBrightness(this.brightnessBottomRight);
			tessellator.addVertexWithUV(xMin, yCeiling, zMin, u2, v2);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.setBrightness(this.brightnessTopRight);
			tessellator.addVertexWithUV(xMin, yCeiling, zMax, u1, v1);
		} else {
			tessellator.addVertexWithUV(xMax, yCeiling, zMax, u4, v4);
			tessellator.addVertexWithUV(xMax, yCeiling, zMin, u3, v3);
			tessellator.addVertexWithUV(xMin, yCeiling, zMin, u2, v2);
			tessellator.addVertexWithUV(xMin, yCeiling, zMax, u1, v1);
		}

	}

	/**
	 * Emits the face on the {@code z = z + block.minZ} plane (a vertical quad spanning the
	 * block's X/Y bounds); see {@link #renderBottomFace}. Note this face's name follows the
	 * legacy Beta convention where "east" readers expect X — the plane here is actually the
	 * Z-min side.
	 */
	public void renderEastFace(Block block, double x, double y, double z, int texId) {
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minX * (double) TextureAtlas.TILE);
		double u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxX * (double) TextureAtlas.TILE - 0.01D);
		double v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
		double v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE - 0.01D);
		double u3;
		if (this.flipTexture) {
			u3 = u2;
			u2 = u4;
			u4 = u3;
		}

		if (block.minX < 0.0D || block.maxX > 1.0D) {
			u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + 0.0F);
			u4 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		if (block.minY < 0.0D || block.maxY > 1.0D) {
			v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + 0.0F);
			v4 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		u3 = u4;
		double u1 = u2;
		double v3 = v2;
		double v1 = v4;
		if (this.uvRotateEast == 2) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE);
			v3 = v2;
			v1 = v4;
			u3 = u2;
			u1 = u4;
			v2 = v4;
			v4 = v3;
		} else if (this.uvRotateEast == 1) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minX * (double) TextureAtlas.TILE);
			u3 = u4;
			u1 = u2;
			u2 = u4;
			u4 = u1;
			v3 = v4;
			v1 = v2;
		} else if (this.uvRotateEast == 3) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE - 0.01D);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minY * (double) TextureAtlas.TILE - 0.01D);
			u3 = u4;
			u1 = u2;
			v3 = v2;
			v1 = v4;
		}

		double xMin = x + block.minX;
		double xMax = x + block.maxX;
		double yMin = y + block.minY;
		double yMax = y + block.maxY;
		double zMin = z + block.minZ;
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.setBrightness(this.brightnessTopLeft);
			tessellator.addVertexWithUV(xMin, yMax, zMin, u3, v3);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.setBrightness(this.brightnessBottomLeft);
			tessellator.addVertexWithUV(xMax, yMax, zMin, u2, v2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight,
					this.colorBlueBottomRight);
			tessellator.setBrightness(this.brightnessBottomRight);
			tessellator.addVertexWithUV(xMax, yMin, zMin, u1, v1);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.setBrightness(this.brightnessTopRight);
			tessellator.addVertexWithUV(xMin, yMin, zMin, u4, v4);
		} else {
			tessellator.addVertexWithUV(xMin, yMax, zMin, u3, v3);
			tessellator.addVertexWithUV(xMax, yMax, zMin, u2, v2);
			tessellator.addVertexWithUV(xMax, yMin, zMin, u1, v1);
			tessellator.addVertexWithUV(xMin, yMin, zMin, u4, v4);
		}

	}

	/** Emits the face on the {@code z = z + block.maxZ} plane (Z-max side); see {@link #renderBottomFace}. */
	public void renderWestFace(Block block, double x, double y, double z, int texId) {
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minX * (double) TextureAtlas.TILE);
		double u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxX * (double) TextureAtlas.TILE - 0.01D);
		double v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
		double v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE - 0.01D);
		double u3;
		if (this.flipTexture) {
			u3 = u2;
			u2 = u4;
			u4 = u3;
		}

		if (block.minX < 0.0D || block.maxX > 1.0D) {
			u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + 0.0F);
			u4 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		if (block.minY < 0.0D || block.maxY > 1.0D) {
			v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + 0.0F);
			v4 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		u3 = u4;
		double u1 = u2;
		double v3 = v2;
		double v1 = v4;
		if (this.uvRotateWest == 1) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE);
			v3 = v2;
			v1 = v4;
			u3 = u2;
			u1 = u4;
			v2 = v4;
			v4 = v3;
		} else if (this.uvRotateWest == 2) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxX * (double) TextureAtlas.TILE);
			u3 = u4;
			u1 = u2;
			u2 = u4;
			u4 = u1;
			v3 = v4;
			v1 = v2;
		} else if (this.uvRotateWest == 3) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minX * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxX * (double) TextureAtlas.TILE - 0.01D);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minY * (double) TextureAtlas.TILE - 0.01D);
			u3 = u4;
			u1 = u2;
			v3 = v2;
			v1 = v4;
		}

		double xMin = x + block.minX;
		double xMax = x + block.maxX;
		double yMin = y + block.minY;
		double yMax = y + block.maxY;
		double zMax = z + block.maxZ;
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.setBrightness(this.brightnessTopLeft);
			tessellator.addVertexWithUV(xMin, yMax, zMax, u2, v2);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.setBrightness(this.brightnessBottomLeft);
			tessellator.addVertexWithUV(xMin, yMin, zMax, u1, v1);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight,
					this.colorBlueBottomRight);
			tessellator.setBrightness(this.brightnessBottomRight);
			tessellator.addVertexWithUV(xMax, yMin, zMax, u4, v4);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.setBrightness(this.brightnessTopRight);
			tessellator.addVertexWithUV(xMax, yMax, zMax, u3, v3);
		} else {
			tessellator.addVertexWithUV(xMin, yMax, zMax, u2, v2);
			tessellator.addVertexWithUV(xMin, yMin, zMax, u1, v1);
			tessellator.addVertexWithUV(xMax, yMin, zMax, u4, v4);
			tessellator.addVertexWithUV(xMax, yMax, zMax, u3, v3);
		}

	}

	/** Emits the face on the {@code x = x + block.minX} plane (X-min side); see {@link #renderBottomFace}. */
	public void renderNorthFace(Block block, double x, double y, double z, int texId) {
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minZ * (double) TextureAtlas.TILE);
		double u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxZ * (double) TextureAtlas.TILE - 0.01D);
		double v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
		double v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE - 0.01D);
		double u3;
		if (this.flipTexture) {
			u3 = u2;
			u2 = u4;
			u4 = u3;
		}

		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + 0.0F);
			u4 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		if (block.minY < 0.0D || block.maxY > 1.0D) {
			v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + 0.0F);
			v4 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		u3 = u4;
		double u1 = u2;
		double v3 = v2;
		double v1 = v4;
		if (this.uvRotateNorth == 1) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			v3 = v2;
			v1 = v4;
			u3 = u2;
			u1 = u4;
			v2 = v4;
			v4 = v3;
		} else if (this.uvRotateNorth == 2) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minZ * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxZ * (double) TextureAtlas.TILE);
			u3 = u4;
			u1 = u2;
			u2 = u4;
			u4 = u1;
			v3 = v4;
			v1 = v2;
		} else if (this.uvRotateNorth == 3) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE - 0.01D);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minY * (double) TextureAtlas.TILE - 0.01D);
			u3 = u4;
			u1 = u2;
			v3 = v2;
			v1 = v4;
		}

		double xMin = x + block.minX;
		double yMin = y + block.minY;
		double yMax = y + block.maxY;
		double zMin = z + block.minZ;
		double zMax = z + block.maxZ;
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.setBrightness(this.brightnessTopLeft);
			tessellator.addVertexWithUV(xMin, yMax, zMax, u3, v3);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.setBrightness(this.brightnessBottomLeft);
			tessellator.addVertexWithUV(xMin, yMax, zMin, u2, v2);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight,
					this.colorBlueBottomRight);
			tessellator.setBrightness(this.brightnessBottomRight);
			tessellator.addVertexWithUV(xMin, yMin, zMin, u1, v1);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.setBrightness(this.brightnessTopRight);
			tessellator.addVertexWithUV(xMin, yMin, zMax, u4, v4);
		} else {
			tessellator.addVertexWithUV(xMin, yMax, zMax, u3, v3);
			tessellator.addVertexWithUV(xMin, yMax, zMin, u2, v2);
			tessellator.addVertexWithUV(xMin, yMin, zMin, u1, v1);
			tessellator.addVertexWithUV(xMin, yMin, zMax, u4, v4);
		}

	}

	/** Emits the face on the {@code x = x + block.maxX} plane (X-max side); see {@link #renderBottomFace}. */
	public void renderSouthFace(Block block, double x, double y, double z, int texId) {
		Tessellator tessellator = Tessellator.instance;
		if (this.overrideBlockTexture >= 0) {
			texId = this.overrideBlockTexture;
		}

		AtlasTexel.calc(texId, TextureAtlas.TERRAIN);
		int tileU = AtlasTexel.u;
		int tileV = AtlasTexel.v;
		double u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minZ * (double) TextureAtlas.TILE);
		double u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxZ * (double) TextureAtlas.TILE - 0.01D);
		double v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
		double v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE - 0.01D);
		double u3;
		if (this.flipTexture) {
			u3 = u2;
			u2 = u4;
			u4 = u3;
		}

		if (block.minZ < 0.0D || block.maxZ > 1.0D) {
			u2 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + 0.0F);
			u4 = (double) TexelScale.u(TextureAtlas.TERRAIN, (float) tileU + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		if (block.minY < 0.0D || block.maxY > 1.0D) {
			v2 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + 0.0F);
			v4 = (double) TexelScale.v(TextureAtlas.TERRAIN, (float) tileV + (float) TextureAtlas.TERRAIN.tileSpan);
		}

		u3 = u4;
		double u1 = u2;
		double v3 = v2;
		double v1 = v4;
		if (this.uvRotateSouth == 2) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.minY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) tileU + block.maxY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) (tileV + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE);
			v3 = v2;
			v1 = v4;
			u3 = u2;
			u1 = u4;
			v2 = v4;
			v4 = v3;
		} else if (this.uvRotateSouth == 1) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxY * (double) TextureAtlas.TILE);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxZ * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minZ * (double) TextureAtlas.TILE);
			u3 = u4;
			u1 = u2;
			u2 = u4;
			u4 = u1;
			v3 = v4;
			v1 = v2;
		} else if (this.uvRotateSouth == 3) {
			u2 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.minZ * (double) TextureAtlas.TILE);
			u4 = TexelScale.ud(TextureAtlas.TERRAIN, (double) (tileU + TextureAtlas.TILE) - block.maxZ * (double) TextureAtlas.TILE - 0.01D);
			v2 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.maxY * (double) TextureAtlas.TILE);
			v4 = TexelScale.vd(TextureAtlas.TERRAIN, (double) tileV + block.minY * (double) TextureAtlas.TILE - 0.01D);
			u3 = u4;
			u1 = u2;
			v3 = v2;
			v1 = v4;
		}

		double xMax = x + block.maxX;
		double yMin = y + block.minY;
		double yMax = y + block.maxY;
		double zMin = z + block.minZ;
		double zMax = z + block.maxZ;
		if (this.enableAO) {
			tessellator.setColorOpaque_F(this.colorRedTopLeft, this.colorGreenTopLeft, this.colorBlueTopLeft);
			tessellator.setBrightness(this.brightnessTopLeft);
			tessellator.addVertexWithUV(xMax, yMin, zMax, u1, v1);
			tessellator.setColorOpaque_F(this.colorRedBottomLeft, this.colorGreenBottomLeft, this.colorBlueBottomLeft);
			tessellator.setBrightness(this.brightnessBottomLeft);
			tessellator.addVertexWithUV(xMax, yMin, zMin, u4, v4);
			tessellator.setColorOpaque_F(this.colorRedBottomRight, this.colorGreenBottomRight,
					this.colorBlueBottomRight);
			tessellator.setBrightness(this.brightnessBottomRight);
			tessellator.addVertexWithUV(xMax, yMax, zMin, u3, v3);
			tessellator.setColorOpaque_F(this.colorRedTopRight, this.colorGreenTopRight, this.colorBlueTopRight);
			tessellator.setBrightness(this.brightnessTopRight);
			tessellator.addVertexWithUV(xMax, yMax, zMax, u2, v2);
		} else {
			tessellator.addVertexWithUV(xMax, yMin, zMax, u1, v1);
			tessellator.addVertexWithUV(xMax, yMin, zMin, u4, v4);
			tessellator.addVertexWithUV(xMax, yMax, zMin, u3, v3);
			tessellator.addVertexWithUV(xMax, yMax, zMax, u2, v2);
		}

	}

	/**
	 * Renders a block as an equirect showpiece (inventory/item), centered on the
	 * origin via a small GL translate. Standard (full cube) blocks get all six faces
	 * with the classic shading; note the shade values are applied with bottom=1.0 and
	 * top=0.5 here (a legacy quirk that makes items appear slightly inverted vs the
	 * world pass). Non-cube render types draw only the south face (the "flattened"
	 * item look).
	 */
	public void renderBlockAsItem(Block block, float brightness) {
		int renderType = block.getRenderType();
		Tessellator tessellator = Tessellator.instance;
		if (renderType == 0) {
			block.setBlockBoundsForItemRender();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			float shadeTop = 0.5F;
			float shadeBottom = 1.0F;
			float shadeEastWest = 0.8F;
			float shadeNorthSouth = 0.6F;
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(shadeBottom, shadeBottom, shadeBottom, brightness);
			this.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(0));
			tessellator.setColorRGBA_F(shadeTop, shadeTop, shadeTop, brightness);
			this.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(1));
			tessellator.setColorRGBA_F(shadeEastWest, shadeEastWest, shadeEastWest, brightness);
			this.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(2));
			this.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(3));
			tessellator.setColorRGBA_F(shadeNorthSouth, shadeNorthSouth, shadeNorthSouth, brightness);
			this.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(4));
			this.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(5));
			tessellator.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		} else {
			block.setBlockBoundsForItemRender();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, brightness);
			this.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSide(0));
			tessellator.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}

	}

	/**
	 * Inventory render path: applies the per-color tint (via
	 * {@link Block#getRenderColor}) scaled by brightness, then dispatches to the
	 * handler registered for the block's render type via <code>renderBlockOnInventory</code>.
	 */
	public void renderBlockOnInventory(Block block, int metadata, float brightness) {
		if (this.useInventoryTint) {
			int color = block.getRenderColor(metadata);
			float red = (float) (color >> 16 & 255) / 255.0F;
			float green = (float) (color >> 8 & 255) / 255.0F;
			float blue = (float) (color & 255) / 255.0F;
			GL11.glColor4f(red * brightness, green * brightness, blue * brightness, 1.0F);
		}

		int renderType = block.getRenderType();
		// Legacy alias: render type 255 renders as a plain cube in the inventory
		if (renderType == 255)
			renderType = 0;

		BlockRenderType type = BlockRenderType.get(renderType);
		if (type != null) {
			type.handler().renderBlockOnInventory(this, block, metadata, brightness);
		}
	}

	/**
	 * Whether the given render type is drawn as a true 3-D model in the inventory/held-item
	 * view (as opposed to a flattened 2-D "sprite").
	 *
	 * <p>Delegates to {@link BlockRenderType#handler()} so that each render type
	 * declares its own 3-D flag — adding a new block type requires only implementing
	 * {@link BlockRenderHandler#renderItemIn3d()} on its handler.</p>
	 */
	public static boolean renderItemIn3d(int renderType) {
		BlockRenderType type = BlockRenderType.get(renderType);
		return type != null && type.handler().renderItemIn3d();
	}

	/** Precomputes the redstone dust color table indexed by signal level 0-15. */
	static {
		for (int i = 0; i < redstoneColors.length; ++i) {
			float level = (float) i / 15.0F;
			float red = level * 0.6F + 0.4F;
			if (i == 0) {
				level = 0.0F;
			}

			float green = level * level * 0.7F - 0.5F;
			float blue = level * level * 0.6F - 0.7F;
			if (green < 0.0F) {
				green = 0.0F;
			}

			if (blue < 0.0F) {
				blue = 0.0F;
			}

			redstoneColors[i] = new float[] { red, green, blue };
		}

	}
}
