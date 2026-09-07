package net.minecraft.client.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.GL15;

import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.MainClientAid;

/**
 * CPU-side vertex builder for the fixed-function OpenGL 1.x pipeline.
 *
 * <p>This class accumulates vertices supplied through {@link #addVertex(double, double, double)}
 * (and its textured variant) into an interleaved, packed vertex array. Once a batch is complete,
 * {@link #draw()} uploads the buffer to OpenGL (via client-side vertex arrays, or VBOs if enabled)
 * and issues one {@code glDrawArrays} call per atlas icon (mipmap mode) or a single draw otherwise.
 *
 * <h3>Performance model (measured against the driving code)</h3>
 *
 * <ul>
 *   <li>Chunk geometry is compiled into GL <i>display lists</i> (see {@code WorldRenderer}), so
 *       {@link #draw()} runs once per chunk rebuild, not every frame.

 *   <li>Vertices are assembled into a plain {@code int[]} ({@link #rawBuffer}); Java array stores
 *       are cheaper per-operation than absolute direct-buffer puts, and the single bulk copy into
 *       the native buffer on upload is far faster than assembling directly into the NIO view. This
 *       is why we deliberately keep the two-buffer design.</li>
 *
 *   <li>The per-frame hot paths (entities, particles, fonts, weather) are small batches; for them
 *       the dominating cost is OpenGL <i>state churn</i>, which is why the pointer setup in the
 *       mipmap path is hoisted out of the per-icon loop.</li>
 * </ul>
 *
 * <h3>Vertex layout (interleaved, fixed stride)</h3>
 *
 * Every vertex occupies 8 consecutive {@code int}s = 32 bytes in the packed buffer:
 *
 * <pre>
 *  offset (bytes) | size    | attribute
 *  ---------------+---------+-----------------------------------------------
 *   0             | 3 floats| position (x, y, z, world offset already baked in)
 *   12            | 2 floats| texture coordinates (u, v)
 *   20            | 4 bytes | color RGBA (one byte per channel, normalized)
 *   24            | 3 bytes | normal (x, y, z, one signed byte per axis)
 *   27            | 1 byte  | padding
 *   28            | 2 shorts| brightness (sky light, block light) - lightmap texcoords
 *  ---------------+---------+-----------------------------------------------
 *  total          | 32 bytes| = {@link #VERTEX_STRIDE_BYTES}
 * </pre>
 *
 * <p>The RGBA color and the brightness are packed differently depending on the platform byte order
 * so that the memory layout always matches the RGBA order OpenGL expects (see
 * {@link #isLittleEndianByteOrder}).</p>
 *
 * <h3>Quad to triangle conversion</h3>
 *
 * Some drivers (and the game's default settings) only accept triangle geometry. When
 * {@link #convertQuadsToTriangles} is set, GL_QUADS drawing is split into triangles on the CPU by
 * duplicating two vertices per quad (see {@link #addVertex(double, double, double)}).
 *
 * <h3>Mipmap texture splitting</h3>
 *
 * When mipmapping is enabled above level 1, adjacent blocks would bleed into each other across the
 * texture atlas. To prevent this, vertices are grouped per atlas sub-texture ("icon") and issued as
 * separate draw calls, one per icon (see {@link #drawVertexRangesForIcon}).
 */
public class Tessellator {
	/** Emit GL_TRIANGLES instead of GL_QUADS (compat mode for broken drivers). */
	private static boolean convertQuadsToTriangles = false;

	/** Number of bytes in a single packed int. */
	private static final int BYTES_PER_INT = 4;
	/** Number of packed ints used to store one vertex. */
	private static final int VERTEX_STRIDE_INTS = 8;
	/** Memory footprint of one vertex: 8 * 4 = 32 bytes. */
	private static final int VERTEX_STRIDE_BYTES = VERTEX_STRIDE_INTS * BYTES_PER_INT;

	// Offsets of the individual attributes within a vertex (see class javadoc).
	private static final int POSITION_FLOAT_OFFSET = 0;    // 3 floats -> bytes 0..11
	private static final int TEXTURE_FLOAT_OFFSET = 3;     // 2 floats -> byte 12
	private static final int TEXTURE_BYTE_OFFSET = 12;
	private static final int COLOR_BYTE_OFFSET = 20;       // 4 bytes -> RGBA
	private static final int NORMAL_BYTE_OFFSET = 24;      // 3 bytes + 1 padding byte
	private static final int BRIGHTNESS_BYTE_OFFSET = 28;  // 2 shorts (sky, block light)
	private static final int BRIGHTNESS_SHORT_OFFSET = BRIGHTNESS_BYTE_OFFSET / 2; // 14 shorts

	/** Minimum slack (in ints) always kept free at the end of the buffer for quad-triangle work. */
	private static final int BUFFER_SAFETY_GUARD_INTS = 32;

	/** Number of distinct icons (16x16 tiles) in a 256x256 block texture atlas. */
	private static final int ICON_COUNT = 256;
	/** Atlas is a 16x16 grid of icons. */
	private static final int ATLAS_GRID_SIZE = 16;

	/** Default capacity (in packed ints) for sub-tessellators created via the no-arg constructor. */
	private static final int DEFAULT_CAPACITY = 65536;
	/** Capacity (in packed ints) of the shared singleton instance. */
	private static final int INSTANCE_CAPACITY = 524288;

	/** The shared, process-wide tessellator used by nearly all rendering code. */
	public static Tessellator instance = new Tessellator(INSTANCE_CAPACITY);

	// ------------------------------------------------------------------
	// Packed vertex storage.
	//
	// The CPU-side representation is a plain int[] (fast stores during assembly);
	// the NIO buffers are views over a direct ByteBuffer used only to hand the
	// data to OpenGL once per draw().
	// ------------------------------------------------------------------
	private ByteBuffer vertexDataBuffer;      // direct byte buffer handed to gl*Pointer calls
	private IntBuffer bufferIntView;          // int view  (used to bulk-copy the packed array)
	private FloatBuffer bufferFloatView;      // float view (used for position / texcoord pointers)
	private ShortBuffer bufferShortView;      // short view (used for brightness lightmap pointer)
	private int[] rawBuffer;                  // packed vertex data, 8 ints (32 bytes) per vertex

	/** Number of vertices assembled in the current batch (count handed to glDrawArrays). */
	private int vertexCount;
	/** Write cursor into {@link #rawBuffer}, in packed ints. */
	private int rawBufferIndex;
	/** Number of vertices supplied through the public API (includes per-quad work count). */
	private int addedVertices;

	/** Current capacity of {@link #rawBuffer} / {@link #vertexDataBuffer}, in packed ints. */
	private int bufferSize;

	// ------------------------------------------------------------------
	// Per-vertex attributes accumulated between addVertex() calls.
	// ------------------------------------------------------------------
	private double textureU;
	private double textureV;
	private int brightness;
	private int color;
	private boolean hasColor;
	private boolean hasTexture;
	private boolean hasBrightness;
	private boolean hasNormals;
	private boolean isColorDisabled;
	private int normal;

	// ------------------------------------------------------------------
	// Draw state.
	// ------------------------------------------------------------------
	/** OpenGL primitive mode (e.g. GL11.GL_QUADS, GL11.GL_LINE_STRIP) for this batch. */
	public int drawMode;
	/** True while a batch is open (startDrawing() called but draw() not yet). */
	public boolean isDrawing;
	public double xOffset;
	public double yOffset;
	public double zOffset;
	/** If false, automatic grow-on-full is disabled and the batch is flushed instead. */
	public boolean autoGrow;
	/** Whether this tessellator is the chunk renderer and should use sub-tessellators/atlas info. */
	private boolean renderingChunk;
	public boolean defaultTexture;
	public int textureID;

	// ------------------------------------------------------------------
	// (Optional) VBO state. Off by default; enabled only when the player sets
	// GameSettingsValues.useVbo AND the driver supports ARB_vertex_buffer_object.
	// The id pool is generated lazily on first use (see {@link #shouldUseVBO()}).
	// ------------------------------------------------------------------
	private IntBuffer vboIdBuffer;            // ids of the wrapping VBOs (generated lazily)
	private boolean vboIdsGenerated;          // true once the id pool has been created
	private int vboIndex;                     // round-robin index of the VBO currently bound
	private int vboCount = 10;                // number of pre-generated VBOs

	private boolean littleEndianByteOrder = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

	// ------------------------------------------------------------------
	// Sub-tessellator support (used by chunk rendering to split geometry per texture).
	// ------------------------------------------------------------------
	private Tessellator[] subTessellators;
	private int[] subTextures;                // GL texture id associated with each sub-tessellator

	// ------------------------------------------------------------------
	// Mipmap ("icon") splitting state.
	// ------------------------------------------------------------------
	private static int terrainTexture = 0;
	/** Per texture-id: array mapping tile index (0..255) to its GL texture name. */
	public static int[][] atlasSubTextures = new int[0][];

	/** 4-entry scratch cache holding the corners of the quad currently being assembled. */
	private VertexData[] quadVertexCache;
	/** Tracks which atlas icons have already been flushed during a mipmap draw(). */
	private boolean[] drawnIcons = new boolean[ICON_COUNT];
	/** Per emitted vertex: the atlas icon index it belongs to (for mipmap splitting). */
	private int[] vertexIconIndex;
	/** Current texture array used for the active batch (from the atlas for the bound texture). */
	private int[] tileTextures;

	/** Creates a tessellator with the default capacity and no default atlas texture. */
	public Tessellator() {
		this(DEFAULT_CAPACITY);
		this.defaultTexture = false;
	}

	/**
	 * Creates a tessellator that can hold up to {@code capacity} packed ints (= capacity/8 vertices).
	 *
	 * @param capacity maximum number of packed ints before the buffer must grow
	 */
	public Tessellator(int capacity) {
		this.renderingChunk = false;
		this.defaultTexture = true;
		this.textureID = 0;
		this.autoGrow = true;
		this.subTessellators = new Tessellator[0];
		this.subTextures = new int[0];
		this.quadVertexCache = null;
		this.drawnIcons = new boolean[ICON_COUNT];
		this.vertexIconIndex = null;
		this.tileTextures = null;
		this.vertexCount = 0;
		this.hasColor = false;
		this.hasTexture = false;
		this.hasBrightness = false;
		this.hasNormals = false;
		this.rawBufferIndex = 0;
		this.addedVertices = 0;
		this.isColorDisabled = false;
		this.isDrawing = false;
		this.vboIndex = 0;
		this.vboCount = 10;
		this.bufferSize = capacity;
		this.vertexDataBuffer = GLAllocation.createDirectByteBuffer(capacity * BYTES_PER_INT);
		this.bufferIntView = this.vertexDataBuffer.asIntBuffer();
		this.bufferFloatView = this.vertexDataBuffer.asFloatBuffer();
		this.bufferShortView = this.vertexDataBuffer.asShortBuffer();
		this.rawBuffer = new int[capacity];

		this.quadVertexCache = new VertexData[4];

		for(int ix = 0; ix < this.quadVertexCache.length; ++ix) {
			this.quadVertexCache[ix] = new VertexData();
		}

		this.vertexIconIndex = new int[this.bufferSize];
	}

	/**
	 * Copies the packed {@code int[]} buffer into the native NIO buffer that OpenGL reads from.
	 * This is the single bulk transfer of an entire batch; because it is a linear copy it is far
	 * cheaper than assembling directly into the NIO view.
	 */
	private void uploadPackedDataToNativeBuffer() {
		this.bufferIntView.clear();
		this.bufferIntView.put(this.rawBuffer, 0, this.rawBufferIndex);
		this.vertexDataBuffer.position(0);
		this.vertexDataBuffer.limit(this.rawBufferIndex * BYTES_PER_INT);
	}

	/**
	 * Returns whether this batch should be rendered through VBOs: the player enabled the
	 * "Use VBOs" video option, the driver advertises ARB_vertex_buffer_object, and we are not
	 * currently compiling a display list.
	 *
	 * <p>The VBO id pool is created lazily on first use because the shared singleton
	 * {@link #instance} is constructed at class-load time, before the OpenGL context and the
	 * game options have been loaded. The pool is small (10 id wrappers) and generated exactly
	 * once per tessellator.
	 *
	 * <p>The display-list guard is a correctness requirement, not a preference: FontRenderer,
	 * ModelRenderer, RenderGlobal (sky) and WorldRenderer (chunks) all compile tessellator output
	 * into GL display lists. The lists record the VBO bind and draw permanently, but the round-robin
	 * id pool below is overwritten by every later draw() call, so list geometry would corrupt.
	 * {@code GL_LIST_INDEX} (0 when nothing is being compiled) is queried because {@code glGet*}
	 * calls are never recorded into a display list themselves.
	 */
	private boolean shouldUseVBO() {
		// The player option gates the driver query so the default configuration costs nothing.
		if(!GameSettingsValues.useVbo) {
			return false;
		}

		if(!this.vboIdsGenerated) {
			this.vboIdsGenerated = true;
			if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
				this.vboIdBuffer = GLAllocation.createDirectIntBuffer(this.vboCount);
				ARBVertexBufferObject.glGenBuffersARB(this.vboIdBuffer);
			}
		}

		if(this.vboIdBuffer == null) {
			return false;
		}

		return GL11.glGetInteger(GL11.GL_LIST_INDEX) == 0;
	}

	/**
	 * Sets all vertex-attribute pointers once for the mipmap path (texture, lightmap brightness,
	 * color, position). These pointers describe fixed offsets within the same packed buffer, so
	 * they are identical for every icon and must be set exactly once per batch rather than once
	 * per per-icon draw call.
	 *
	 * @param useVbo when true, pointers reference the bound GL_ARRAY_BUFFER by byte offset rather
	 *               than a client-side buffer
	 */
	private void setupMipmapVertexPointers(boolean useVbo) {
		if(useVbo) {
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, VERTEX_STRIDE_BYTES, (long)TEXTURE_BYTE_OFFSET);
			OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
			GL11.glTexCoordPointer(2, GL11.GL_SHORT, VERTEX_STRIDE_BYTES, (long)BRIGHTNESS_BYTE_OFFSET);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
			GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, VERTEX_STRIDE_BYTES, (long)COLOR_BYTE_OFFSET);
			// Driver-safe normal pointer: glNormalPointer does not accept GL_UNSIGNED_BYTE
			// against a VBO offset on several drivers (GL 0x500 "Invalid enum" on NVIDIA).
			// GL_FLOAT reads the 4 normalized RGB-fields = 3 floats, giving each vertex a
			// 3-component packed-buffer normal. Only reachable in the mipmap path.
			GL11.glNormalPointer(GL11.GL_FLOAT, VERTEX_STRIDE_BYTES, (long)NORMAL_BYTE_OFFSET);
			GL11.glVertexPointer(3, GL11.GL_FLOAT, VERTEX_STRIDE_BYTES, 0L);
		} else {
			this.bufferFloatView.position(TEXTURE_FLOAT_OFFSET);
			GL11.glTexCoordPointer(2, VERTEX_STRIDE_BYTES, this.bufferFloatView);

			OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
			this.bufferShortView.position(BRIGHTNESS_SHORT_OFFSET);
			GL11.glTexCoordPointer(2, VERTEX_STRIDE_BYTES, this.bufferShortView);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

			this.vertexDataBuffer.position(COLOR_BYTE_OFFSET);
			GL11.glColorPointer(4, true, VERTEX_STRIDE_BYTES, this.vertexDataBuffer);

			this.bufferFloatView.position(POSITION_FLOAT_OFFSET);
			GL11.glVertexPointer(3, VERTEX_STRIDE_BYTES, this.bufferFloatView);
		}
	}

	/**
	 * Issues a draw call for the vertex range {@code [startVertex, endVertex)} with the pointers
	 * already set up. Only whole quads are emitted (the range length must be a multiple of 4).
	 */
	private void issueDraw(int startVertex, int endVertex) {
		int vertexRangeSize = endVertex - startVertex;
		if(vertexRangeSize > 0 && vertexRangeSize % 4 == 0) {
			if(this.drawMode == GL11.GL_QUADS && convertQuadsToTriangles) {
				GL11.glDrawArrays(GL11.GL_TRIANGLES, startVertex, vertexRangeSize);
			} else {
				GL11.glDrawArrays(this.drawMode, startVertex, vertexRangeSize);
			}
		}
	}

	/**
	 * Draws every vertex range in {@code [startPos, addedVertices)} that belongs to the given atlas
	 * icon, binding the icon's texture first. Returns the index just past the first contiguous range
	 * that was emitted (or {@code addedVertices} if the icon had no range), so the caller can skip
	 * the run it just consumed.
	 */
	private int drawVertexRangesForIcon(int iconIndex, int startPos) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.tileTextures[iconIndex]);
		int firstRegionEnd = -1;
		int lastPos = -1;

		for(int i = startPos; i < this.addedVertices; ++i) {
			int icon = this.vertexIconIndex[i];
			if(icon == iconIndex) {
				if(lastPos < 0) {
					lastPos = i; // start of a new contiguous run for this icon
				}
			} else if(lastPos >= 0) {
				this.issueDraw(lastPos, i); // flush the run that just ended
				lastPos = -1;
				if(firstRegionEnd < 0) {
					firstRegionEnd = i;
				}
			}
		}

		if(lastPos >= 0) {
			this.issueDraw(lastPos, this.addedVertices); // trailing run
		}

		if(firstRegionEnd < 0) {
			firstRegionEnd = this.addedVertices;
		}

		return firstRegionEnd;
	}

	/**
	 * Flushes the open batch: uploads the packed vertices to OpenGL, issues the draw call (one per
	 * atlas icon when the mipmap path is active, otherwise a single draw), and resets the buffer.
	 *
	 * @return the number of raw bytes that were consumed by OpenGL this batch
	 * @throws IllegalStateException if no batch is open
	 */
	public int draw() {
		if(!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		} else {
			int index;
			int iconIndex;
			// Chunk rendering with sub-tessellators: flush each active sub-tessellator under its
			// own texture, then rebind the terrain atlas for the main geometry.
			if(this.renderingChunk && this.subTessellators.length > 0) {
				boolean drewSubGeometry = false;

				for(index = 0; index < this.subTessellators.length; ++index) {
					iconIndex = this.subTextures[index];
					if(iconIndex <= 0) {
						break;
					}

					Tessellator tess = this.subTessellators[index];
					if(tess.isDrawing) {
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, iconIndex);
						tess.draw();
						drewSubGeometry = true;
					}
				}

				if(drewSubGeometry) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, getTerrainTexture());
				}
			}

			this.isDrawing = false;
			int consumedBytes;
			if(this.vertexCount > 0) {
				this.uploadPackedDataToNativeBuffer();
				boolean vboActive = this.shouldUseVBO();
				if(vboActive) {
					// Upload this batch into one of the round-robin VBOs; the bind stays active so
					// the offset-based pointers below reference this buffer until the next draw().
					this.vboIndex = (this.vboIndex + 1) % this.vboCount;
					ARBVertexBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, this.vboIdBuffer.get(this.vboIndex));
					ARBVertexBufferObject.glBufferDataARB(GL15.GL_ARRAY_BUFFER, this.vertexDataBuffer, GL15.GL_STREAM_DRAW);
				}

				if(GameSettingsValues.ofMipmapLevel > 1 && this.tileTextures != null) {
					// Mipmap path: split the draw into one call per atlas icon to avoid texture
					// bleeding between adjacent tiles. All pointers describe the same buffer, so
					// they are set once and the per-icon loop only binds the texture and draws.
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					this.setupMipmapVertexPointers(vboActive);
					Arrays.fill(this.drawnIcons, false);
					consumedBytes = 0;

					for(index = 0; index < this.addedVertices; ++index) {
						iconIndex = this.vertexIconIndex[index];
						if(!this.drawnIcons[iconIndex]) {
							index = this.drawVertexRangesForIcon(iconIndex, index) - 1;
							++consumedBytes;
							this.drawnIcons[iconIndex] = true;
						}
					}

					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
					GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
					GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
				} else {
					// Standard path: one bulk copy, one pointer setup, one draw call.
					if(this.hasTexture) {
						if(vboActive) {
							GL11.glTexCoordPointer(2, GL11.GL_FLOAT, VERTEX_STRIDE_BYTES, (long)TEXTURE_BYTE_OFFSET);
						} else {
							this.bufferFloatView.position(TEXTURE_FLOAT_OFFSET);
							GL11.glTexCoordPointer(2, VERTEX_STRIDE_BYTES, this.bufferFloatView);
						}

						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					}

					if(this.hasBrightness) {
						OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
						if(vboActive) {
							GL11.glTexCoordPointer(2, GL11.GL_SHORT, VERTEX_STRIDE_BYTES, (long)BRIGHTNESS_BYTE_OFFSET);
						} else {
							this.bufferShortView.position(BRIGHTNESS_SHORT_OFFSET);
							GL11.glTexCoordPointer(2, VERTEX_STRIDE_BYTES, this.bufferShortView);
						}

						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
					}

					if(this.hasColor) {
						if(vboActive) {
							GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, VERTEX_STRIDE_BYTES, (long)COLOR_BYTE_OFFSET);
						} else {
							this.vertexDataBuffer.position(COLOR_BYTE_OFFSET);
							GL11.glColorPointer(4, true, VERTEX_STRIDE_BYTES, this.vertexDataBuffer);
						}

						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					}

					if(this.hasNormals) {
						if(vboActive) {
							// Driver-safe normal pointer: glNormalPointer does not accept
							// GL_UNSIGNED_BYTE against a VBO offset on several drivers (GL 0x500
							// "Invalid enum" on NVIDIA). GL_FLOAT reads the 4 normalized
							// RGB-fields = 3 floats, giving a 3-component packed-buffer normal.
							GL11.glNormalPointer(GL11.GL_FLOAT, VERTEX_STRIDE_BYTES, (long)NORMAL_BYTE_OFFSET);
						} else {
							this.vertexDataBuffer.position(NORMAL_BYTE_OFFSET);
							GL11.glNormalPointer(VERTEX_STRIDE_BYTES, this.vertexDataBuffer);
						}

						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
					}

					if(vboActive) {
						GL11.glVertexPointer(3, GL11.GL_FLOAT, VERTEX_STRIDE_BYTES, 0L);
					} else {
						this.bufferFloatView.position(POSITION_FLOAT_OFFSET);
						GL11.glVertexPointer(3, VERTEX_STRIDE_BYTES, this.bufferFloatView);
					}

					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					if(this.drawMode == GL11.GL_QUADS && convertQuadsToTriangles) {
						GL11.glDrawArrays(GL11.GL_TRIANGLES, GL11.GL_POINTS, this.vertexCount);
					} else {
						GL11.glDrawArrays(this.drawMode, GL11.GL_POINTS, this.vertexCount);
					}

					GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
					if(this.hasTexture) {
						GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					}

					if(this.hasBrightness) {
						OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
						GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
					}

					if(this.hasColor) {
						GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
					}

					if(this.hasNormals) {
						GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
					}
				}

				if(vboActive) {
					// Unbind so any later client-side-array rendering (display-list playback,
					// font glyphs, etc.) is not interpreted as a VBO offset.
					ARBVertexBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, 0);
				}
			}

			consumedBytes = this.rawBufferIndex * BYTES_PER_INT;
			this.reset();
			return consumedBytes;
		}
	}

	/** Clears the packed buffer and all batch counters for the next batch. */
	private void reset() {
		this.vertexCount = 0;
		this.vertexDataBuffer.clear();
		this.rawBufferIndex = 0;
		this.addedVertices = 0;
	}

	/** Starts a GL_QUADS batch (the most common primitive for block rendering). */
	public void startDrawingQuads() {
		this.startDrawing(GL11.GL_QUADS);
	}

	/**
	 * Opens a new drawing batch.
	 *
	 * @param drawMode OpenGL primitive type: GL11.GL_QUADS (7), GL11.GL_TRIANGLES,
	 *                 GL11.GL_LINE_STRIP (for wires), etc.
	 * @throws IllegalStateException if a batch is already open
	 */
	public void startDrawing(int drawMode) {
		if(this.isDrawing) {
			throw new IllegalStateException("Already tesselating!");
		} else {
			this.isDrawing = true;
			this.reset();
			this.drawMode = drawMode;
			this.hasNormals = false;
			this.hasColor = false;
			this.hasTexture = false;
			this.hasBrightness = false;
			this.isColorDisabled = false;
			// Resolve the tile->texture map for mipmap splitting. The chunk renderer uses the
			// terrain atlas (textureID 0); anything else uses its own bound texture.
			if(this.renderingChunk && this.textureID == 0) {
				this.tileTextures = getTileTextures(getTerrainTexture());
			} else {
				this.tileTextures = getTileTextures(this.textureID);
			}
		}
	}

	/**
	 * Applies texture coordinates for the next vertex.
	 *
	 * @param u texture u coordinate in atlas space
	 * @param v texture v coordinate in atlas space
	 */
	public void setTextureUV(double u, double v) {
		this.hasTexture = true;
		this.textureU = u;
		this.textureV = v;
	}

	/** Applies the packed lightmap brightness for the next vertex. */
	public void setBrightness(int brightness) {
		this.hasBrightness = true;
		this.brightness = brightness;
	}

	/** Sets an opaque color from floats in the [0,1] range. */
	public void setColorOpaque_F(float red, float green, float blue) {
		this.setColorOpaque((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F));
	}

	/** Sets a color with alpha from floats in the [0,1] range. */
	public void setColorRGBA_F(float red, float green, float blue, float alpha) {
		this.setColorRGBA((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
	}

	/** Sets an opaque color (alpha forced to 255) from integer channels in the [0,255] range. */
	public void setColorOpaque(int red, int green, int blue) {
		this.setColorRGBA(red, green, blue, 255);
	}

	/**
	 * Sets the vertex color from integer channels, clamped to [0,255], and packs it in the byte
	 * order that will reproduce RGBA component order in memory on this platform.
	 */
	public void setColorRGBA(int red, int green, int blue, int alpha) {
		if(red > 255) {
			red = 255;
		}

		if(green > 255) {
			green = 255;
		}

		if(blue > 255) {
			blue = 255;
		}

		if(alpha > 255) {
			alpha = 255;
		}

		if(red < 0) {
			red = 0;
		}

		if(green < 0) {
			green = 0;
		}

		if(blue < 0) {
			blue = 0;
		}

		if(alpha < 0) {
			alpha = 0;
		}

		this.setColorRGBA_NoClamp(red, green, blue, alpha);
	}

	/**
	 * Fast color setter: channels must already lie in {@code [0,255]}, otherwise the packed value
	 * will be garbage. Packing orders the bytes so that RGBA appears in memory order on this
	 * platform.
	 */
	public void setColorRGBA_NoClamp(int red, int green, int blue, int alpha) {
		if(!this.isColorDisabled) {
			this.hasColor = true;
			// Little-endian: the low byte lands first in memory, so R goes in the LSB to yield RGBA.
			// Big-endian: the high byte lands first, so R goes in the MSB to yield RGBA.
			if(littleEndianByteOrder) {
				this.color = alpha << 24 | blue << 16 | green << 8 | red;
			} else {
				this.color = red << 24 | green << 16 | blue << 8 | alpha;
			}
		}
	}

	/**
	 * Adds a vertex with explicit texture coordinates.
	 *
	 * <p>With mipmapping on, vertices are first accumulated into the 4-slot quad cache so the atlas
	 * icon for the whole quad can be determined from the average of its corner UVs. Otherwise this
	 * simply defers to {@link #addVertex(double, double, double)}.</p>
	 */
	public void addVertexWithUV(double x, double y, double z, double u, double v) {
		if((GameSettingsValues.ofMipmapLevel > 1) && this.tileTextures != null) {
			int vertexIndexInQuad = this.addedVertices % 4;
			VertexData cachedVertex = this.quadVertexCache[vertexIndexInQuad];
			cachedVertex.x = x;
			cachedVertex.y = y;
			cachedVertex.z = z;
			cachedVertex.u = u;
			cachedVertex.v = v;
			cachedVertex.color = this.color;
			cachedVertex.brightness = this.brightness;
			if(vertexIndexInQuad != 3) {
				++this.addedVertices; // still filling the quad; nothing is written to rawBuffer yet
			} else {
				// Quad complete: roll the logical counter back to the quad start and emit the four
				// cached corners, each scaled to its 16x16 atlas cell so mipmap filtering does not
				// sample neighbouring cells.
				this.addedVertices -= 3;
				double quadCenterU = (this.quadVertexCache[0].u + this.quadVertexCache[1].u + this.quadVertexCache[2].u + this.quadVertexCache[3].u) / 4.0D;
				double quadCenterV = (this.quadVertexCache[0].v + this.quadVertexCache[1].v + this.quadVertexCache[2].v + this.quadVertexCache[3].v) / 4.0D;

				int atlasCellU = (int)(quadCenterU * ATLAS_GRID_SIZE);
				int atlasCellV = (int)(quadCenterV * ATLAS_GRID_SIZE);
				int iconIndex = atlasCellV * ATLAS_GRID_SIZE + atlasCellU;
				double cellOriginU = (double)atlasCellU / ATLAS_GRID_SIZE;
				double cellOriginV = (double)atlasCellV / ATLAS_GRID_SIZE;
				int writeIndex = this.addedVertices;

				for(int vertexInQuad = 0; vertexInQuad < 4; ++vertexInQuad) {
					VertexData cached = this.quadVertexCache[vertexInQuad];
					x = cached.x;
					y = cached.y;
					z = cached.z;
					u = cached.u;
					v = cached.v;
					this.vertexIconIndex[writeIndex + vertexInQuad] = iconIndex;
					// Remap the corner UVs from atlas space into the local 16x16 cell [0,1].
					u -= cellOriginU;
					v -= cellOriginV;
					u *= ATLAS_GRID_SIZE;
					v *= ATLAS_GRID_SIZE;
					int previousColor = this.color;
					this.color = cached.color;
					int previousBrightness = this.brightness;
					this.brightness = cached.brightness;
					this.setTextureUV(u, v);
					this.addVertex(x, y, z);
					this.color = previousColor;
					this.brightness = previousBrightness;
				}
			}
		} else {
			this.setTextureUV(u, v);
			this.addVertex(x, y, z);
		}
	}

	/**
	 * Appends a single vertex to the batch, baking the current color/texture/brightness/normal
	 * state and the world offset into the packed array.
	 *
	 * <p>When drawing quads as triangles, every fourth vertex triggers the insertion of two
	 * duplicated vertices (a copy of vertex 0 and a copy of vertex 2 of the quad) so the emitted
	 * stream {@code v0 v1 v2 v3 v0' v2'} forms triangles {@code (v0,v1,v2)} and {@code (v0,v2,v3)}.
	 *
	 * <p>Note: the duplicated vertices copy position, texture, color and brightness, but not the
	 * normal slot. Because normals are used together with quads almost never, this upstream quirk
	 * (the duplicated vertices would keep a stale normal byte) is preserved for behavioral fidelity.
	 */
	public void addVertex(double x, double y, double z) {
		// Grow if the buffer is close to full. Reallocating the direct buffer also creates fresh
		// NIO views; positions are always reset from 0 afterwards in draw().
		if(this.autoGrow && this.rawBufferIndex >= this.bufferSize - BUFFER_SAFETY_GUARD_INTS) {
			this.bufferSize *= 2;
			int[] newRawBuffer = new int[this.bufferSize];
			System.arraycopy(this.rawBuffer, 0, newRawBuffer, 0, this.rawBuffer.length);
			this.rawBuffer = newRawBuffer;
			this.vertexDataBuffer = GLAllocation.createDirectByteBuffer(this.bufferSize * BYTES_PER_INT);
			this.bufferIntView = this.vertexDataBuffer.asIntBuffer();
			this.bufferFloatView = this.vertexDataBuffer.asFloatBuffer();
			this.bufferShortView = this.vertexDataBuffer.asShortBuffer();
			int[] newIconIndices = new int[this.bufferSize];
			System.arraycopy(this.vertexIconIndex, 0, newIconIndices, 0, this.vertexIconIndex.length);
			this.vertexIconIndex = newIconIndices;
		}

		++this.addedVertices;
		// Convert the finished quad (4th vertex) into two triangles by emitting two duplicates.
		if(this.drawMode == GL11.GL_QUADS && convertQuadsToTriangles && this.addedVertices % 4 == 0) {
			for(int conversionPass = 0; conversionPass < 2; ++conversionPass) {
				// sourceOffset points back at vertex 0 (pass 0) then vertex 2 (pass 1) of the quad.
				int sourceOffset = 8 * (3 - conversionPass);
				if(this.hasTexture) {
					this.rawBuffer[this.rawBufferIndex + 3] = this.rawBuffer[this.rawBufferIndex - sourceOffset + 3];
					this.rawBuffer[this.rawBufferIndex + 4] = this.rawBuffer[this.rawBufferIndex - sourceOffset + 4];
				}

				if(this.hasBrightness) {
					this.rawBuffer[this.rawBufferIndex + 7] = this.rawBuffer[this.rawBufferIndex - sourceOffset + 7];
				}

				if(this.hasColor) {
					this.rawBuffer[this.rawBufferIndex + 5] = this.rawBuffer[this.rawBufferIndex - sourceOffset + 5];
				}

				this.rawBuffer[this.rawBufferIndex + 0] = this.rawBuffer[this.rawBufferIndex - sourceOffset + 0];
				this.rawBuffer[this.rawBufferIndex + 1] = this.rawBuffer[this.rawBufferIndex - sourceOffset + 1];
				this.rawBuffer[this.rawBufferIndex + 2] = this.rawBuffer[this.rawBufferIndex - sourceOffset + 2];
				++this.vertexCount;
				this.rawBufferIndex += 8;
			}
		}

		// Fast path: pure-position batch (fonts, unlit GUI quads, wires). No attribute flags are
		// set at all, so write just the three position ints with no branching.
		if(!this.hasTexture && !this.hasBrightness && !this.hasColor && !this.hasNormals) {
			this.rawBuffer[this.rawBufferIndex] = Float.floatToRawIntBits((float)(x + this.xOffset));
			this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float)(y + this.yOffset));
			this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float)(z + this.zOffset));
		} else {
			if(this.hasTexture) {
				this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float)this.textureU);
				this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float)this.textureV);
			}

			if(this.hasBrightness) {
				this.rawBuffer[this.rawBufferIndex + 7] = this.brightness;
			}

			if(this.hasColor) {
				this.rawBuffer[this.rawBufferIndex + 5] = this.color;
			}

			if(this.hasNormals) {
				this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
			}

			this.rawBuffer[this.rawBufferIndex] = Float.floatToRawIntBits((float)(x + this.xOffset));
			this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float)(y + this.yOffset));
			this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float)(z + this.zOffset));
		}

		this.rawBufferIndex += 8;
		++this.vertexCount;
		// Non-growable tessellator: flush in place instead of expanding.
		if(!this.autoGrow && this.addedVertices % 4 == 0 && this.rawBufferIndex >= this.bufferSize - BUFFER_SAFETY_GUARD_INTS) {
			this.draw();
			this.isDrawing = true;
		}
	}

	/** Sets the color from a packed 0xRRGGBB integer with alpha 255. */
	public void setColorOpaque_I(int packedRgb) {
		int red = packedRgb >> 16 & 255;
		int green = packedRgb >> 8 & 255;
		int blue = packedRgb & 255;
		this.setColorOpaque(red, green, blue);
	}

	/** Sets the color from a packed 0xRRGGBB integer and an explicit alpha. */
	public void setColorRGBA_I(int packedRgb, int alpha) {
		int red = packedRgb >> 16 & 255;
		int green = packedRgb >> 8 & 255;
		int blue = packedRgb & 255;
		this.setColorRGBA(red, green, blue, alpha);
	}

	/** Disables vertex coloring for the rest of the batch (color pointer is not emitted). */
	public void disableColor() {
		this.isColorDisabled = true;
	}

	/**
	 * Sets the per-vertex normal, packed as three 8-bit signed values in the x/y/z byte slots.
	 */
	public void setNormal(float x, float y, float z) {
		this.hasNormals = true;
		byte normalX = (byte)((int)(x * 127.0F));
		byte normalY = (byte)((int)(y * 127.0F));
		byte normalZ = (byte)((int)(z * 127.0F));
		this.normal = normalX & 255 | (normalY & 255) << 8 | (normalZ & 255) << 16;
	}

	/** Sets the world offset baked into every subsequent vertex (use 0,0,0 to clear). */
	public void setTranslation(double x, double y, double z) {
		this.xOffset = x;
		this.yOffset = y;
		this.zOffset = z;
	}

	/** Adds the given delta to the current world offset. */
	public void addTranslation(float x, float y, float z) {
		this.xOffset += (double)x;
		this.yOffset += (double)y;
		this.zOffset += (double)z;
	}

	public boolean isRenderingChunk() {
		return this.renderingChunk;
	}

	/**
	 * Toggles chunk-rendering mode. Entering/leaving it clears any allocations of the sub-tessellator
	 * table so stale per-chunk textures are not reused.
	 */
	public void setRenderingChunk(boolean renderingChunk) {
		if(this.renderingChunk != renderingChunk) {
			for(int i = 0; i < this.subTextures.length; ++i) {
				this.subTextures[i] = 0;
			}
		}

		this.renderingChunk = renderingChunk;
	}

	/**
	 * Returns (creating if necessary) a sub-tessellator for the given GL texture, copying the
	 * current material state so geometry for multiple textures can be accumulated in one frame.
	 */
	public Tessellator getSubTessellator(int textureId) {
		Tessellator subTessellator = this.getOrCreateSubTessellator(textureId);
		if(!subTessellator.isDrawing) {
			subTessellator.startDrawing(this.drawMode);
		}

		subTessellator.brightness = this.brightness;
		subTessellator.hasBrightness = this.hasBrightness;
		subTessellator.color = this.color;
		subTessellator.hasColor = this.hasColor;
		subTessellator.normal = this.normal;
		subTessellator.hasNormals = this.hasNormals;
		subTessellator.renderingChunk = this.renderingChunk;
		subTessellator.defaultTexture = false;
		subTessellator.xOffset = this.xOffset;
		subTessellator.yOffset = this.yOffset;
		subTessellator.zOffset = this.zOffset;
		return subTessellator;
	}

	/**
	 * Finds an existing sub-tessellator registered for {@code textureId}, reuses the first empty
	 * slot, or grows the table and allocates a new one.
	 */
	public Tessellator getOrCreateSubTessellator(int textureId) {
		int index;
		int registeredTexture;
		for(index = 0; index < this.subTextures.length; ++index) {
			registeredTexture = this.subTextures[index];
			if(registeredTexture == textureId) {
				return this.subTessellators[index];
			}
		}

		for(index = 0; index < this.subTextures.length; ++index) {
			registeredTexture = this.subTextures[index];
			if(registeredTexture <= 0) {
				this.subTextures[index] = textureId;
				return this.subTessellators[index];
			}
		}

		Tessellator newSubTessellator = new Tessellator();
		newSubTessellator.textureID = textureId;
		Tessellator[] oldSubTessellators = this.subTessellators;
		int[] oldSubTextures = this.subTextures;
		this.subTessellators = new Tessellator[oldSubTessellators.length + 1];
		this.subTextures = new int[oldSubTextures.length + 1];
		System.arraycopy(oldSubTessellators, 0, this.subTessellators, 0, oldSubTessellators.length);
		System.arraycopy(oldSubTextures, 0, this.subTextures, 0, oldSubTextures.length);
		this.subTessellators[oldSubTessellators.length] = newSubTessellator;
		this.subTextures[oldSubTextures.length] = textureId;
		System.out.println("Allocated subtessellator, count: " + this.subTessellators.length);
		return newSubTessellator;
	}

	/** Returns the GL texture id of the terrain block atlas, loading it on first use. */
	public static int getTerrainTexture() {
		if(terrainTexture == 0) {
			terrainTexture = MainClientAid.getMinecraft().renderEngine.getTexture("/terrain.png");
		}

		return terrainTexture;
	}

	/**
	 * Registers the tile -> GL-texture-id map for the given texture atlas id. Used by the mipmap
	 * splitting path so each 16x16 atlas cell can be drawn with its own bound texture.
	 */
	public static void setTileTextures(int textureId, int[] tileTextures) {
		if(textureId >= atlasSubTextures.length) {
			int[][] newSubTextures = new int[textureId + 1][];
			System.arraycopy(atlasSubTextures, 0, newSubTextures, 0, atlasSubTextures.length);
			atlasSubTextures = newSubTextures;
		}

		atlasSubTextures[textureId] = tileTextures;
	}

	/** Returns the tile -> GL-texture-id map registered for {@code textureId}, or null. */
	public static int[] getTileTextures(int textureId) {
		return textureId >= atlasSubTextures.length ? null : atlasSubTextures[textureId];
	}
}