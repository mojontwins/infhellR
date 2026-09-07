package net.minecraft.client.render.atlas;

/**
 * The texture atlases the client stitches tiles from. Resizing an atlas is a
 * two-number change: edit the {@link #width} and {@link #height} of the matching
 * constant below — every UV calculation in the renderers reads its scale and tile
 * span from here, so nothing else needs to touch the atlas.
 *
 * <p>The tile-index layout assumed by {@link AtlasTexel} packs one tile per row
 * (16 texels per tile); wider atlases can't be expressed by this layout without
 * re-packing tiles, but taller ones (the common case, e.g. a 256x512 terrain)
 * work without any other change.</p>
 *
 * <p>Keep the {@code width}/{@code height} in step with the actual atlas image
 * dimensions and with {@link net.minecraft.client.render.RenderEngine}'s atlas
 * allocation ((256) in {@code allocateImageData}), or textures will sample
 * incorrectly.</p>
 *
 * <p>Performance optimizations: scale factors are precomputed as lookup tables
 * for fast integer-pixel UV calculation. Use {@link #u(int)} and {@link #v(int)}
 * for integer texel offsets (0-15). For fractional offsets, use {@link TexelScale}.
 */
public enum TextureAtlas {
	/** The block/terrain atlas ({@code terrain.png}). */
	TERRAIN(256, 256),
	/** The item icon atlas ({@code gui/items.png}). */
	ITEMS(256, 256);

	/** Edge length of a single tile, in texels. */
	public static final int TILE = 16;
	/** Inset per tile edge so neighbouring tiles never bleed into each other. */
	public static final float TILE_INSET = 0.01F;

	public final int width;
	public final int height;
	public final int widthInTiles;
	public final int heightInTiles;
	/** Effective tile span in texels ({@link #TILE} - {@link #TILE_INSET}). */
	public final float tileSpan;
	/** Precomputed UV scale for one texel row/column (i.e. 1 / width). */
	public final float pixelU;
	/** Precomputed UV scale for one texel row/column (i.e. 1 / height). */
	public final float pixelV;
	/** Precomputed tile span in UV space ({@link #tileSpan} / width). */
	public final float uSpan;
	/** Precomputed tile span in UV space ({@link #tileSpan} / height). */
	public final float vSpan;
	/** Half-pixel inset (0.5px) for UV bleeding prevention. */
	public final float halfPixelU;
	/** Half-pixel inset (0.5px) for UV bleeding prevention. */
	public final float halfPixelV;
	/** Lookup table of U scales for texels 0..15, plus 16 (i.e. 0..TILE inclusive). */
	public final float[] uPixels;
	/** Lookup table of V scales for texels 0..15, plus 16. */
	public final float[] vPixels;
	/** Lookup table of UVs for every tile index (0..widthInTiles*heightInTiles-1). */
	public final float[] uvLut;

	private TextureAtlas(int width, int height) {
		this.width = width;
		this.height = height;
		this.widthInTiles = width / TILE;
		this.heightInTiles = height / TILE;
		this.tileSpan = TILE - TILE_INSET;
		this.pixelU = 1.0F / (float) width;
		this.pixelV = 1.0F / (float) height;
		this.uSpan = this.tileSpan * this.pixelU;
		this.vSpan = this.tileSpan * this.pixelV;
		this.halfPixelU = 0.5F * this.pixelU;
		this.halfPixelV = 0.5F * this.pixelV;
		this.uPixels = new float[TILE + 1];
		this.vPixels = new float[TILE + 1];
		for (int i = 0; i <= TILE; i++) {
			this.uPixels[i] = (float) i * this.pixelU;
			this.vPixels[i] = (float) i * this.pixelV;
		}
		int nTiles = this.widthInTiles * this.heightInTiles;
		this.uvLut = new float[nTiles];
		for (int i = 0; i < nTiles; i++) {
			this.uvLut[i] = (float) (i % this.widthInTiles) * (float) TILE * this.pixelU;
		}
	}

	/** UV scale for an integer texel count (0..15). */
	public float u(int texels) {
		return texels >= 0 && texels <= TILE ? this.uPixels[texels] : (float) texels * this.pixelU;
	}

	/** UV scale for an integer texel count (0..15). */
	public float v(int texels) {
		return texels >= 0 && texels <= TILE ? this.vPixels[texels] : (float) texels * this.pixelV;
	}

	/** Precomputed UV for every tile index (0..widthInTiles*heightInTiles-1). */
	public float uvLut(int tileIndex) {
		return this.uvLut[tileIndex];
	}
}