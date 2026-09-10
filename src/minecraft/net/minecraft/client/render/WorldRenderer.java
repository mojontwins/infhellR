package net.minecraft.client.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.ChunkCache;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.entity.RenderItem;
import net.minecraft.client.render.tileentity.TileEntityRenderer;

/**
 * Renders one 16x16x16 chunk section of the world into OpenGL display lists.
 *
 * <p>Each WorldRenderer is bound to a single chunk section (located at
 * {@link #posX}, {@link #posY}, {@link #posZ} in world block coordinates) and
 * owns three display lists:
 * <ul>
 *   <li>glRenderList + 0 - the translucent block pass</li>
 *   <li>glRenderList + 1 - the opaque block pass</li>
 *   <li>glRenderList + 2 - a bounding box used for occlusion queries</li>
 * </ul>
 *
 * <p>Rendering is driven by {@link RenderGlobal}: when terrain changes,
 * {@link #markDirty()} flags the section and the lists are recompiled on the
 * next frame.</p>
 *
 * <p>Note: this class is really the "chunk renderer" - it could be renamed
 * ChunkRenderer to make its purpose immediately clear.</p>
 */
public class WorldRenderer {

	/** The world this renderer draws terrain from. */
	public World worldObj;
	/** Base OpenGL display list id (see the pass layout in the class comment). */
	protected int glRenderList = -1;
	/** Global counter of chunk sections recompiled this frame (metrics). */
	public static volatile int chunksUpdated = 0;
	/** World block coordinates of the lower corner of this chunk section. */
	public int posX;
	public int posY;
	public int posZ;
	/**
	 * Section origin snapped to a 1024-block grid (pos minus its 1024 modulus).
	 * Used by RenderGlobal to sort renderers by camera distance for occlusion.
	 */
	public int posXMinus;
	public int posYMinus;
	public int posZMinus;
	/** Section position modulo 1024, used for the occlusion bounding-box list. */
	public int posXClip;
	public int posYClip;
	public int posZClip;
	/** True when the section passes the frustum culling test this frame. */
	public boolean isInFrustum = false;
	/**
	 * skipRenderPass[0] = true when the opaque pass produced no geometry,
	 * skipRenderPass[1] = true when the translucent pass produced no geometry.
	 * Passes that are marked skipped are not drawn at all.
	 */
	public boolean[] skipRenderPass = new boolean[2];
	/** Section centre (corner + 8), used for distance checks. */
	public int posXPlus;
	public int posYPlus;
	public int posZPlus;
	/** True when the terrain in this section changed and the lists must be rebuilt. */
	public volatile boolean needsUpdate;
	/** World-space bounding box of this section, for frustum/occlusion tests. */
	public AxisAlignedBB rendererBoundingBox;
	/** Index of this renderer in RenderGlobal's worldRenderers array. */
	public int chunkIndex;
	public boolean isVisible = true;
	public boolean isWaitingOnOcclusionQuery;
	public int glOcclusionQuery;
	/** True if any block with a light source was drawn in this section. */
	public boolean isChunkLit;
	protected boolean isInitialized = false;
	/** Tile entities discovered inside this section on the last rebuild. */
	public List<TileEntity> tileEntityRenderers = new ArrayList<TileEntity>();
	/** The shared global list of all tile entities (owned by RenderGlobal). */
	protected List<TileEntity> tileEntities;
	/** Bytes/vertices drawn during the last rebuild (metrics). */
	protected int bytesDrawn;
	public boolean isVisibleFromPosition = false;
	public double visibleFromX;
	public double visibleFromY;
	public double visibleFromZ;
	public boolean isInFrustrumFully = false;
	/** True when the occlusion-query bounding box list needs regenerating. */
	protected boolean needsBoxUpdate = false;
	/** True while this renderer is rebuilding its lists (threading safety). */
	public volatile boolean isUpdating = false;
	/** Shared world-space origin offset for all chunk renderers (multiplayer sync). */
	public static int globalChunkOffsetX = 0;
	public static int globalChunkOffsetZ = 0;

	/**
	 * @param world            the world to render
	 * @param tileEntityList   the shared list of all tile entities in the world
	 * @param chunkX           section corner X in block coordinates (section index * 16)
	 * @param chunkY           section corner Y in block coordinates
	 * @param chunkZ           section corner Z in block coordinates
	 * @param glListId         base OpenGL display list id for this renderer
	 */
	public WorldRenderer(World world, List<TileEntity> tileEntityList, int chunkX, int chunkY, int chunkZ, int glListId) {
		this.worldObj = world;
		this.tileEntities = tileEntityList;
		this.glRenderList = glListId;
		this.posX = -999;
		this.setPosition(chunkX, chunkY, chunkZ);
		this.needsUpdate = false;
	}

	/**
	 * Moves this renderer to a new chunk section. All cached state is dropped
	 * and the renderer is queued for a rebuild.
	 */
	public void setPosition(int chunkX, int chunkY, int chunkZ) {
		if(chunkX != this.posX || chunkY != this.posY || chunkZ != this.posZ) {
			this.setDontDraw();
			this.posX = chunkX;
			this.posY = chunkY;
			this.posZ = chunkZ;
			this.posXPlus = chunkX + 8;
			this.posYPlus = chunkY + 8;
			this.posZPlus = chunkZ + 8;
			// 1024-grid alignment: the clip values keep a small repeating range so the
			// occlusion bounding-box list stays in a tight coordinate space, while the
			// ...Minus values are the section origin snapped onto that 1024 boundary.
			this.posXClip = chunkX & 1023;
			this.posYClip = chunkY;
			this.posZClip = chunkZ & 1023;
			this.posXMinus = chunkX - this.posXClip;
			this.posYMinus = chunkY - this.posYClip;
			this.posZMinus = chunkZ - this.posZClip;
			float boundaryFudge = 0.0F;
			this.rendererBoundingBox = AxisAlignedBB.getBoundingBox((double)((float)chunkX - boundaryFudge), (double)((float)chunkY - boundaryFudge), (double)((float)chunkZ - boundaryFudge), (double)((float)(chunkX + 16) + boundaryFudge), (double)((float)(chunkY + 16) + boundaryFudge), (double)((float)(chunkZ + 16) + boundaryFudge));
			this.needsBoxUpdate = true;
			this.markDirty();
			this.isVisibleFromPosition = false;
		}
	}

	/**
	 * Rebuilds the display lists for this chunk section when the terrain changed
	 * ({@link #needsUpdate}). Called once per frame by RenderGlobal.
	 *
	 * <p>The section is drawn in up to two passes:
	 * <ul>
	 *   <li>pass 0 - opaque blocks (solid cubes, leaves, ...)</li>
	 *   <li>pass 1 - translucent blocks (glass, water, ...)</li>
	 * </ul>
	 * A block's pass is chosen by {@link Block#getRenderBlockPass()} (0 = opaque,
	 * 1 = translucent, 2 = renders in both). Each pass gets its own display list;
	 * passes that emitted no geometry are flagged in {@link #skipRenderPass} so
	 * they are skipped when drawing.</p>
	 */
	public void updateRenderer() {
		if(this.worldObj != null) {
			if(this.needsUpdate) {
				// Regenerate the occlusion-query bounding box list if the section moved.
				if(this.needsBoxUpdate) {
					float boxFudge = 0.0F;
					GL11.glNewList(this.glRenderList + 2, GL11.GL_COMPILE);
					RenderItem.renderAABB(AxisAlignedBB.getBoundingBoxFromPool((double)((float)this.posXClip - boxFudge), (double)((float)this.posYClip - boxFudge), (double)((float)this.posZClip - boxFudge), (double)((float)(this.posXClip + 16) + boxFudge), (double)((float)(this.posYClip + 16) + boxFudge), (double)((float)(this.posZClip + 16) + boxFudge)));
					GL11.glEndList();
					this.needsBoxUpdate = false;
				}

				this.isVisible = true;
				this.isVisibleFromPosition = false;
				this.needsUpdate = false;

				// Section bounds in world block coordinates. The block loop itself
				// only covers the 16x16x16 range, but the cache below includes a
				// 1-block margin so neighbour borders are available for culling.
				int xMin = this.posX;
				int yMin = this.posY;
				int zMin = this.posZ;
				int xMax = this.posX + 16;
				int yMax = this.posY + 16;
				int zMax = this.posZ + 16;
				int margin = 1;

				// Optimistically mark both passes as empty; they are cleared below
				// whenever a block actually emits geometry.
				for(int pass = 0; pass < 2; ++pass) {
					this.skipRenderPass[pass] = true;
				}

				// Chunk.isLit is set by blocks with a brightness probe while rendering
				// (used for the fog/light tweaks). Also snapshot the current tile
				// entities so we can reconcile additions/removals afterwards.
				Chunk.isLit = false;
				HashSet<TileEntity> existingTileEntities = new HashSet<TileEntity>();
				existingTileEntities.addAll(this.tileEntityRenderers);
				this.tileEntityRenderers.clear();

				// A small block cache covering this section plus the margin; block and
				// light lookups during rendering then avoid repeated world access.
				ChunkCache chunkCache = new ChunkCache(this.worldObj, xMin - margin, yMin - margin, zMin - margin, xMax + margin, yMax + margin, zMax + margin);

				// Fast-path access to the raw block ids of this 16-tall section (subchunk).
				// EmptyChunk (outside the loaded world) and all-air / not-yet-materialized
				// subchunks report null here and every pass is skipped via the null check
				// inside the pass loop (avoids the 4096-cell scan for implicit-air sections).
				Chunk renderChunk = this.worldObj.getChunkFromChunkCoords(this.posX >> 4, this.posZ >> 4);
				byte[] renderChunkBlocks = null;
				if(renderChunk != null) {
					int subchunkIndex = this.posY >> 4;
					if(subchunkIndex >= 0 && subchunkIndex < Chunk.SUBCHUNK_COUNT && !renderChunk.isSubchunkEmpty(subchunkIndex)) {
						renderChunkBlocks = renderChunk.sectionBlocks[subchunkIndex];
					}
				}

				++chunksUpdated;
				RenderBlocks renderBlocks = new RenderBlocks(chunkCache);
				this.bytesDrawn = 0;
				Tessellator tessellator = Tessellator.instance;

				for(int renderPass = 0; renderPass < 2; ++renderPass) {
					// EmptyChunk shortcut: no block data, nothing to render.
					if(renderChunkBlocks == null) {
						break;
					}

					boolean renderNextPass = false;    // a block needs a later pass
					boolean hasRenderedBlocks = false; // a block actually emitted geometry
					boolean hasGlList = false;         // a display list was opened

					// Tell RenderBlocks which pass we are compiling so it can cull faces
					// against the neighbouring pass (prevents flicker at opaque/glass seams).
					renderBlocks.setActiveRenderPass(renderPass);

					for(int y = yMin; y < yMax; ++y) {
						for(int z = zMin; z < zMax; ++z) {
							for(int x = xMin; x < xMax; ++x) {
								// Direct block id lookup from the subchunk-local index:
								// ((x&15)<<8) | ((z&15)<<4) | (y&15), converted to unsigned.
								int blockId = renderChunkBlocks[(x & 15) << 8 | (z & 15) << 4 | (y & 15)] & 255;
								if(blockId > 0) {
									// Open the pass display list the first time a block is found.
									if(!hasGlList) {
										hasGlList = true;
										GL11.glNewList(this.glRenderList + renderPass, GL11.GL_COMPILE);
										tessellator.setRenderingChunk(true);
										tessellator.startDrawingQuads();
										// All sections are compiled relative to the shared world
										// offset so adjacent renderers line up exactly.
										tessellator.setTranslation((double)(-globalChunkOffsetX), 0.0D, (double)(-globalChunkOffsetZ));
									}

									Block block = Block.blocksList[blockId];
									if(block == null) {
										System.out.println("Warning! block id = " + blockId + " in WorldRenderer!");
										continue;
									}

									// Tile entities (chests, signs, spawners, ...) are collected
									// during the opaque pass and drawn separately afterwards.
									if(renderPass == 0 && Block.isBlockContainer[blockId]) {
										TileEntity tileEntity = chunkCache.getBlockTileEntity(x, y, z);
										if(TileEntityRenderer.instance.hasSpecialRenderer(tileEntity)) {
											this.tileEntityRenderers.add(tileEntity);
										}
									}

									// Which pass does this block belong to? 0 = opaque,
									// 1 = translucent, 2 = renders in both passes.
									int blockRenderPass = block.getRenderBlockPass();

									boolean canRender = true;
									if(blockRenderPass == 2) {
										renderNextPass = true;                    // drawn in both passes -> keep going
									} else if(blockRenderPass != renderPass) {
										renderNextPass = true;                    // belongs to another pass -> defer
										canRender = false;
									}

									if(canRender || blockRenderPass == 2) {
										hasRenderedBlocks |= renderBlocks.renderBlockByRenderType(block, x, y, z);
									}
								}
							}
						}
					}

					if(hasGlList) {
						this.bytesDrawn += tessellator.draw();
						GL11.glEndList();
						tessellator.setRenderingChunk(false);
						tessellator.setTranslation(0.0D, 0.0D, 0.0D);
					} else {
						hasRenderedBlocks = false;
					}

					// Remember whether this pass produced anything drawable.
					if(hasRenderedBlocks) {
						this.skipRenderPass[renderPass] = false;
					}

					// Stop after the last pass that still needs geometry.
					if(!renderNextPass) {
						break;
					}
				}

				// Reconcile the tile entity list against the shared RenderGlobal list:
				// entities that disappeared must be removed, newly found ones added.
				HashSet<TileEntity> newTileEntities = new HashSet<TileEntity>();
				newTileEntities.addAll(this.tileEntityRenderers);
				newTileEntities.removeAll(existingTileEntities);
				this.tileEntities.addAll(newTileEntities);
				existingTileEntities.removeAll(this.tileEntityRenderers);
				this.tileEntities.removeAll(existingTileEntities);
				this.isChunkLit = Chunk.isLit;
				this.isInitialized = true;
			}
		}
	}

	/**
	 * Squared distance from the section centre (posXPlus/YPlus/ZPlus) to an entity.
	 * Used for distance sorting before drawing.
	 */
	public float distanceToEntitySquared(Entity entity) {
		float deltaX = (float)(entity.posX - (double)this.posXPlus);
		float deltaY = (float)(entity.posY - (double)this.posYPlus);
		float deltaZ = (float)(entity.posZ - (double)this.posZPlus);
		return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
	}

	/** Marks both render passes as empty and resets frustum/init state. */
	public void setDontDraw() {
		for(int pass = 0; pass < 2; ++pass) {
			this.skipRenderPass[pass] = true;
		}

		this.isInFrustum = false;
		this.isInitialized = false;
	}

	/** Permanently stops this renderer (the section was unloaded). */
	public void stopRendering() {
		this.setDontDraw();
		this.worldObj = null;
	}

	/**
	 * Returns the OpenGL display list to draw for the given render pass,
	 * or -1 if the section is outside the frustum or that pass is empty.
	 */
	public int getGLCallListForPass(int renderPass) {
		return !this.isInFrustum ? -1 : (!this.skipRenderPass[renderPass] ? this.glRenderList + renderPass : -1);
	}

	/** Re-evaluates whether this section is inside the view frustum. */
	public void updateInFrustum(ICamera camera) {
		this.isInFrustum = camera.isBoundingBoxInFrustum(this.rendererBoundingBox);
		this.isInFrustrumFully = false;
	}

	/** Executes the occlusion-query bounding box display list. */
	public void callOcclusionQueryList() {
		GL11.glCallList(this.glRenderList + 2);
	}

	/** True when both render passes are empty, so the section may be skipped entirely. */
	public boolean skipAllRenderPasses() {
		return !this.isInitialized ? false : this.skipRenderPass[0] && this.skipRenderPass[1];
	}

	/** Queues this renderer for a list rebuild on the next frame. */
	public void markDirty() {
		this.needsUpdate = true;
	}
}