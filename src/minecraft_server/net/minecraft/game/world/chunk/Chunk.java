package net.minecraft.game.world.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.ChunkPosition;
import net.minecraft.game.world.EnumSkyBlock;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldChunkManager;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockContainer;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.terrain.generate.city.Building;
import net.minecraft.game.entity.EntityBlockEntity;
import net.minecraft.game.world.block.BlockEntity;


/**
 * A 16x16 column of the world, 256 blocks tall, stored as a stack of lazily allocated
 * 16x16x16 subchunks (sections).
 *
 * <p><b>Vertical layout.</b> The world height is {@value #SECTION_HEIGHT} blocks split into
 * {@value #SUBCHUNK_COUNT} subchunks of {@value #SECTION_SIZE} blocks each. Section 0 covers
 * y 0-15, section 15 covers y 240-255. Each section has its own block-id array, metadata array,
 * sky-light nibble array and block-light nibble array (all {@code null} until the section is
 * materialized by {@link #ensureSubchunk}).</p>
 *
 * <p><b>Null sections.</b> An unmaterialized section is a pure-air cell: block ids/metadata
 * read as 0, block light reads as 0 and sky light reads as 15 (full brightness). The Starlight
 * engine relies on these implicit defaults and never allocates a section purely for lighting.</p>
 *
 * <p><b>Flat generation buffers.</b> The {@code blocks}/{@code data} arrays remain the flat
 * 128-high (16x16x128, index {@code x << 11 | z << 7 | y}) buffers that the terrain generators
 * write to through direct array access. Once generation finishes, {@link #loadFlatBlocks} slices
 * those buffers into sections 0-7 and nulls them; from then on the section arrays are the only
 * storage. ({@code getBlockID}/{@code getBlockMetadata} keep a read-only fallback to the flat
 * buffers for the transient "just generate for height" chunks that are never sliced.)</p>
 */
public class Chunk {
	/** Edge size of one subchunk in blocks (16). */
	public static final int SECTION_SIZE = 16;
	/** Total world height in blocks (256). */
	public static final int SECTION_HEIGHT = 256;
	/** Number of stacked subchunks (SECTION_HEIGHT / SECTION_SIZE = 16). */
	public static final int SUBCHUNK_COUNT = SECTION_HEIGHT >>> 4;
	/** Number of flat 128-high generation buffer slices fed into subchunks (128 / 16). */
	private static final int FLAT_SECTION_COUNT = 128 >>> 4;

	public static boolean isLit;
	
	/**
	 * Flat 128-high block-id buffer used ONLY during terrain generation (Option A: generators
	 * write into this array directly). Null after {@link #loadFlatBlocks} slices it.
	 */
	public byte[] blocks;
	/** Flat 128-high metadata buffer used ONLY during terrain generation. Null after slicing. */
	public byte[] data;

	/** Per-subchunk block-id arrays; a null entry means the whole section is air. */
	public byte[][] sectionBlocks;
	/** Per-subchunk metadata arrays; entries are allocated together with {@link #sectionBlocks}. */
	public byte[][] sectionData;
	/** Per-subchunk sky-light nibbles; a null section is implicitly full bright (15). */
	public NibbleArray[] skyLightMap;
	/** Per-subchunk block-light nibbles; a null section is implicitly dark (0). */
	public NibbleArray[] blockLightMap;
	/** Per-subchunk "contains no blocks?" flag, used by the renderer to skip empty sections. */
	public boolean[] isEmpty;
	/** Highest materialized subchunk index plus one; 0 while the chunk has no sections yet. */
	public int subchunkCount;

	public boolean isChunkLoaded;
	public World worldObj;
	public byte[] heightMap;
	public byte[] landSurfaceHeightMap;
	public int heightMapMinimum;
	public final int xPosition;
	public final int zPosition;
	public Map<ChunkPosition,TileEntity> chunkTileEntityMap;
	public Map<ChunkPosition, EntityBlockEntity> chunkSpecialEntityMap;
	public List<Entity>[] entities;
	public boolean isTerrainPopulated;
	public boolean isModified;
	public boolean neverSave;
	public boolean hasEntities;
	public long lastSaveTime;
	public BiomeGenBase [] biomeGenCache = null;
	/** Per-column temperature for this chunk's 16x16 (index x << 4 | z), null until seeded/populated. */
	public float[] temperatureCache = null;
	/** Per-column humidity for this chunk's 16x16 (index x << 4 | z), null until seeded/populated. */
	public float[] humidityCache = null;
	public boolean hasBuilding = false;
	public boolean hasRoad = false;
	public boolean isOcean = false;
	public boolean hasUnderwaterRuin = false;
	public boolean hasFeature = false;
	public boolean isUrbanChunk = false;
	public int baseHeight = 0;
	public int roadVariation = 0;
	public boolean gotBlocks = false;
	
	// For custom features during "populate"
	public int chestX;
	public int chestY = -1;
	public int chestZ;
	
	public int specialX;
	public int specialY = -1;
	public int specialZ;
	
	public Building building = null;
	
	public boolean beingDecorated = false;

	public int buildingY0 = 64;

	/**
	 * Creates an empty chunk: every subchunk starts unmaterialized and the entity buckets are
	 * one per subchunk (so entities at y up to 255 find the right bucket).
	 */
	@SuppressWarnings("unchecked")
	public Chunk(World world, int chunkX, int chunkZ) {
		this.chunkTileEntityMap = new HashMap<ChunkPosition, TileEntity>();
		this.chunkSpecialEntityMap = new HashMap<ChunkPosition, EntityBlockEntity>();
		this.entities = (List<Entity> []) new List[SUBCHUNK_COUNT];
		this.sectionBlocks = new byte[SUBCHUNK_COUNT][];
		this.sectionData = new byte[SUBCHUNK_COUNT][];
		this.skyLightMap = new NibbleArray[SUBCHUNK_COUNT];
		this.blockLightMap = new NibbleArray[SUBCHUNK_COUNT];
		this.isEmpty = new boolean[SUBCHUNK_COUNT];
		Arrays.fill(this.isEmpty, true);
		this.subchunkCount = 0;
		this.isTerrainPopulated = false;
		this.isModified = false;
		this.hasEntities = false;
		this.lastSaveTime = 0L;
		this.worldObj = world;
		this.xPosition = chunkX;
		this.zPosition = chunkZ;
		this.heightMap = new byte[256];
		this.landSurfaceHeightMap = new byte[256];
		
		for(int i4 = 0; i4 < this.entities.length; ++i4) {
			this.entities[i4] = new ArrayList<Entity>();
		}

	}

	/**
	 * Creates a chunk wrapping the flat 128-high generation buffers (block ids + metadata).
	 * The buffers stay consultable via the flat fallback and are finalized by
	 * {@link #loadFlatBlocks} once terrain generation has finished.
	 */
	public Chunk(World world, byte[] blocks, byte[] metadata, int chunkX, int chunkZ) {
		this(world, chunkX, chunkZ);
		this.blocks = blocks;
		this.data = metadata;
	}

	/**
	 * Allocates the four storage planes (block ids, metadata, sky light, block light) for the
	 * given subchunk the first time anything writes into it. Both light planes start at zero,
	 * matching vanilla {@code ExtendedBlockStorage}: the real values are written afterwards by
	 * the lighting pipeline (the vanilla top-down gradient in {@link #generateSkylightMap()}
	 * followed by the increase-only Starlight init in {@link #initLightingForRealNotJustHeightmap()}).
	 *
	 * <p>The "fully lit open sky" state is represented by a <b>null</b> subchunk — {@link
	 * #getSavedLightValue} reports sky 15 for it — never by pre-filling a materialized plane.
	 * Pre-filling would break the increase-only engine: it can only ever raise a stored nibble,
	 * so a pre-filled 15 under any opaque block (roof, terrain, water) could never be lowered
	 * and every interior cavity would stay scanner-bright forever.</p>
	 *
	 * @param section subchunk index (0-15); section s covers world Y {@code s*16 .. (s*16)+15}
	 */
	public void ensureSubchunk(int section) {
		if(section >= 0 && section < SUBCHUNK_COUNT && this.sectionBlocks[section] == null) {
			int cellCount = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;
			this.sectionBlocks[section] = new byte[cellCount];
			this.sectionData[section] = new byte[cellCount];
			this.skyLightMap[section] = new NibbleArray(cellCount);
			this.blockLightMap[section] = new NibbleArray(cellCount);
			this.isEmpty[section] = true;
			if(section + 1 > this.subchunkCount) {
				this.subchunkCount = section + 1;
			}
		}
	}

	/**
	 * Slices the (still flat) 128-high generation buffers into the runtime subchunks. Each flat
	 * column (address {@code x << 11 | z << 7 | y}) is split into its eight 16-tall segments and
	 * copied into subchunk-local layout ({@code x << 8 | z << 4 | yLocal}). The flat buffers are
	 * then dropped ({@code null}) because the sections are the only storage from this point on.
	 *
	 * @param blockArray flat 128-high block ids
	 * @param metadata   flat 128-high metadata
	 */
	public void loadFlatBlocks(byte[] blockArray, byte[] metadata) {
		if(blockArray == null) {
			return;
		}

		// Materialize all eight lower sections that still exist in the flat buffers.
		for(int section = 0; section < FLAT_SECTION_COUNT; ++section) {
			this.ensureSubchunk(section);
		}

		// Re-slice each flat column into its subchunk segments. Sections that were already
		// materialized by a generator (City column writes etc.) are re-copied from the flat
		// buffer, which mirrors their content, so no information is lost.
		for(int x = 0; x < 16; ++x) {
			for(int z = 0; z < 16; ++z) {
				int flatColumnBase = (x << 4 | z) << 7;      // (x*16 + z) * 128
				int sectionColumnBase = (x << 4 | z) << 4;   // (x*16 + z) * 16
				for(int section = 0; section < FLAT_SECTION_COUNT; ++section) {
					System.arraycopy(blockArray, flatColumnBase + (section << 4), this.sectionBlocks[section], sectionColumnBase, SECTION_SIZE);
					System.arraycopy(metadata, flatColumnBase + (section << 4), this.sectionData[section], sectionColumnBase, SECTION_SIZE);
				}
			}
		}

		// Recompute the per-subchunk empty flags (sections that came up all air render nothing).
		this.recomputeEmptyFlags();

		// The flat generation buffers have served their purpose; drop them.
		this.blocks = null;
		this.data = null;
	}

	/**
	 * Exports the block ids of the lower 128-high region into a flat generation-style buffer
	 * ({@code x << 11 | z << 7 | y}). City generation still edits terrain through such a buffer,
	 * but in-world chunks have already been sliced into subchunks and dropped their flat storage,
	 * so the edit is staged locally and written back with {@link #importFlatBlocks128}.
	 *
	 * @return a 32768-element flat buffer; unmaterialized sections read as air
	 */
	public byte[] exportFlatBlocks128() {
		byte[] flat = new byte[(SECTION_HEIGHT >> 1) * 256];
		for(int x = 0; x < 16; ++x) {
			for(int z = 0; z < 16; ++z) {
				int flatColumnBase = (x << 4 | z) << 7;      // (x*16 + z) * 128
				int subchunkColumnBase = (x << 4 | z) << 4;  // (x*16 + z) * 16
				for(int section = 0; section < FLAT_SECTION_COUNT; ++section) {
					byte[] sectionBlockData = this.sectionBlocks[section];
					if(sectionBlockData != null) {
						System.arraycopy(sectionBlockData, subchunkColumnBase, flat, flatColumnBase + (section << 4), SECTION_SIZE);
					}
				}
			}
		}
		return flat;
	}

	/** Exports the metadata of the lower 128-high region; see {@link #exportFlatBlocks128}. */
	public byte[] exportFlatData128() {
		byte[] flat = new byte[(SECTION_HEIGHT >> 1) * 256];
		for(int x = 0; x < 16; ++x) {
			for(int z = 0; z < 16; ++z) {
				int flatColumnBase = (x << 4 | z) << 7;      // (x*16 + z) * 128
				int subchunkColumnBase = (x << 4 | z) << 4;  // (x*16 + z) * 16
				for(int section = 0; section < FLAT_SECTION_COUNT; ++section) {
					byte[] sectionMetaData = this.sectionData[section];
					if(sectionMetaData != null) {
						System.arraycopy(sectionMetaData, subchunkColumnBase, flat, flatColumnBase + (section << 4), SECTION_SIZE);
					}
				}
			}
		}
		return flat;
	}

	/**
	 * Applies a flat 128-high block/metadata pair back into subchunk storage, the reverse of
	 * {@link #exportFlatBlocks128}/{@link #exportFlatData128}. Sections that would become pure
	 * air are not materialized; a section gains storage the first time it carries a block.
	 * Mirrors {@link #loadFlatBlocks} (empty flags and the materialized count are refreshed),
	 * but lighting is deliberately left untouched, matching the historical direct-array writes.
	 *
	 * @param blocks the flat block ids to apply (generation layout)
	 * @param data   the flat metadata to apply (generation layout)
	 */
	public void importFlatBlocks128(byte[] blocks, byte[] data) {
		if(blocks == null || data == null) {
			return;
		}
		for(int x = 0; x < 16; ++x) {
			for(int z = 0; z < 16; ++z) {
				int flatColumnBase = (x << 4 | z) << 7;      // (x*16 + z) * 128
				int subchunkColumnBase = (x << 4 | z) << 4;  // (x*16 + z) * 16
				for(int section = 0; section < FLAT_SECTION_COUNT; ++section) {
					boolean hasContent = false;
					for(int k = 0; k < SECTION_SIZE && !hasContent; ++k) {
						if((blocks[flatColumnBase + (section << 4) + k] & 255) != 0) {
							hasContent = true;
						}
					}
					if(!hasContent) {
						continue;
					}
					this.ensureSubchunk(section);
					System.arraycopy(blocks, flatColumnBase + (section << 4), this.sectionBlocks[section], subchunkColumnBase, SECTION_SIZE);
					System.arraycopy(data, flatColumnBase + (section << 4), this.sectionData[section], subchunkColumnBase, SECTION_SIZE);
				}
			}
		}
		this.recomputeEmptyFlags();
	}

	/**
	 * Rebuilds the per-subchunk empty flags and the materialized subchunk count from scratch.
	 * Used after bulk writes ({@link #loadFlatBlocks}, {@link #setChunkData}) where keeping
	 * per-cell airness up to date is impractical.
	 */
	public void recomputeEmptyFlags() {
		int highestMaterialized = -1;
		for(int section = 0; section < SUBCHUNK_COUNT; ++section) {
			byte[] sectionBlockData = this.sectionBlocks[section];
			if(sectionBlockData == null) {
				this.isEmpty[section] = true;
				continue;
			}
			boolean empty = true;
			for(int i = 0; i < sectionBlockData.length; ++i) {
				if((sectionBlockData[i] & 255) != 0) {
					empty = false;
					break;
				}
			}
			this.isEmpty[section] = empty;
			highestMaterialized = section;
		}
		this.subchunkCount = highestMaterialized + 1;
	}

	/**
	 * Re-checks whether a single materialized section still contains any non-air block and
	 * updates its empty flag accordingly. Called after a block is removed through the single-cell
	 * setters, where doing a whole-section scan is cheap (4096 cells).
	 *
	 * @param section subchunk index to re-examine
	 */
	private void recomputeEmptyFlag(int section) {
		byte[] sectionBlockData = this.sectionBlocks[section];
		if(sectionBlockData == null) {
			this.isEmpty[section] = true;
			return;
		}
		for(int i = 0; i < sectionBlockData.length; ++i) {
			if((sectionBlockData[i] & 255) != 0) {
				this.isEmpty[section] = false;
				return;
			}
		}
		this.isEmpty[section] = true;
	}

	/** @return the number of materialized subchunks (highest allocated section index + 1). */
	public int getSubchunkCount() {
		return this.subchunkCount;
	}

	/**
	 * @param section subchunk index
	 * @return whether the given section has no non-air blocks (renderer scan-skip)
	 */
	public boolean isSubchunkEmpty(int section) {
		return section >= 0 && section < SUBCHUNK_COUNT && this.isEmpty[section];
	}

	public boolean isAtLocation(int i1, int i2) {
		return i1 == this.xPosition && i2 == this.zPosition;
	}

	public int getHeightValue(int i1, int i2) {
		return this.heightMap[i2 << 4 | i1] & 255;
	}
	
	public int getLandSurfaceHeightValue(int i1, int i2) {
		return this.landSurfaceHeightMap[i2 << 4 | i1] & 255;
	}

	public void generateHeightMap() {
		this.heightMapMinimum = SECTION_HEIGHT - 1;

		for(int x = 0; x < 16; ++x) {
			for(int z = 0; z < 16; ++z) {
				int height = SECTION_HEIGHT - 1;

				for(; height > 0 && Block.lightOpacity[this.getBlockID(x, height - 1, z)] == 0; --height) {
				}

				this.heightMap[z << 4 | x] = (byte)height;
				if(height < this.heightMapMinimum) {
					this.heightMapMinimum = height;
				}
			}
		}

		this.isModified = true;
	}
	
	public void generateLandSurfaceHeightMap() {
		int index = 0;
		for(int z = 0; z < 16; z ++) {
			for(int x = 0; x < 16; x ++) {
				int y = SECTION_HEIGHT - 1;

				// Changed: not just air but also any non opaque block.
				while(Block.lightOpacity[this.getBlockID(x, y, z)] < 255 && y > 0) { y --; }

				this.landSurfaceHeightMap[index ++] = (byte)y;
			}
		}
	}

	public void generateSkylightMap() {
		this.heightMapMinimum = SECTION_HEIGHT - 1;

		int x;
		int z;
		for(x = 0; x < 16; ++x) {
			for(z = 0; z < 16; ++z) {
				int height = SECTION_HEIGHT - 1;

				for(; height > 0 && Block.lightOpacity[this.getBlockID(x, height - 1, z)] == 0; --height) {
				}

				this.heightMap[z << 4 | x] = (byte)height;
				if(height < this.heightMapMinimum) {
					this.heightMapMinimum = height;
				}

				if(!this.worldObj.worldProvider.hasNoSky) {
					int lightLevel = 15;

					// Walk down from the top of the world, one subchunk at a time, writing the
					// vanilla sky gradient into every materialized section. Null sections are
					// implicitly full bright (15), so the running level simply resets above them.
					for(int section = SUBCHUNK_COUNT - 1; section >= 0; --section) {
						NibbleArray sectionSky = this.skyLightMap[section];
						if(sectionSky == null) {
							lightLevel = 15;
							continue;
						}

						int yLocal = SECTION_SIZE - 1;
						do {
							lightLevel -= Block.lightOpacity[this.getBlockID(x, section << 4 | yLocal, z)];
							if(lightLevel > 0) {
								sectionSky.setNibble(x, yLocal, z, lightLevel);
							}

							--yLocal;
						} while(yLocal > 0 && lightLevel > 0);
					}
				}
			}
		}

		this.isModified = true;
	}

	public void doNothing() {
	}

	/*
	 * This method relights a column from the sky after setting block at x, y, z.
	 */
	private void relightBlock(int x, int y, int z) {
		int columnHeight = this.heightMap[z << 4 | x] & 255;

		// If we just set a block higher than the cur. block height...
		int newHeight = Math.max(y, columnHeight);

		// But, if blocks beneath the new height are not opaque, lower the value until an opaque block is found
		for(; newHeight > 0 && Block.lightOpacity[this.getBlockID(x, newHeight - 1, z)] == 0; --newHeight) {
		}

		// newHeight is now at the topmost opaque block.
		// If the stored height and the new height are different...
		if(newHeight != columnHeight) {
			// Store new height
			this.heightMap[z << 4 | x] = (byte)newHeight;
			
			if(newHeight < this.heightMapMinimum) {
				this.heightMapMinimum = newHeight;
			} 
			this.isModified = true;
		}
	}

	/**
	 * Reads a block id, consulting the subchunk array first and falling back to the flat 128-high
	 * generation buffer for sections that have not been materialized/finalized yet.
	 *
	 * @return the block id at (x, y, z); 0 if the cell lies outside the world or is implicit air
	 */
	public int getBlockID(int x, int y, int z) {
		if(y >= 0 && y < SECTION_HEIGHT) {
			int section = y >> 4;
			byte[] sectionBlockData = this.sectionBlocks[section];
			if(sectionBlockData != null) {
				return sectionBlockData[x << 8 | z << 4 | (y & 15)] & 255;
			}
			// Flat fallback: transient "just generate for height" chunks never get sliced, so their
			// height-only callers keep reading the raw generation buffer.
			if(this.blocks != null && y < (SECTION_HEIGHT >> 1)) {
				return this.blocks[x << 11 | z << 7 | y] & 255;
			}
		}
		return 0;
	}

	/**
	 * Sets a single block (id + metadata), materializing its subchunk on first use. Height map and
	 * light are only touched when the block's light behavior actually changes (same fast-path as
	 * before, now routed through {@link #relightBlock}/{@link #updateLight}).
	 *
	 * @return true if the cell changed
	 */
	public boolean setBlockIDWithMetadata(int x, int y, int z, int id, int metadata) {
		if(y < 0 || y >= SECTION_HEIGHT) {
			return false;
		}

		int height = this.heightMap[z << 4 | x] & 255;
		int section = y >> 4;
		this.ensureSubchunk(section);
		byte[] sectionBlockData = this.sectionBlocks[section];
		byte[] sectionMetaData = this.sectionData[section];
		int index = x << 8 | z << 4 | (y & 15);
		int existingId = sectionBlockData[index] & 255;
		if(existingId == id && sectionMetaData[index] == metadata) {
			return false;
		} else {
			int absX = (this.xPosition << 4) | x;
			int absZ = (this.zPosition << 4) | z;

			Block block = Block.blocksList[existingId];

			// Write new block ID
			sectionBlockData[index] = (byte)id;

			// Call `onRemoval` from removed block, if applies.
			if(block != null && !this.worldObj.isRemote) {
				block.onBlockRemoval(this.worldObj, absX, y, absZ);
			}

			// Write new metadata
			sectionMetaData[index] = (byte)metadata;

			// Keep the subchunk empty flag in sync (renderer scan-skip).
			if(id == 0) {
				this.recomputeEmptyFlag(section);
			} else {
				this.isEmpty[section] = false;
			}

			// If there's a sky, skylight may need to be recalculated.			
			if(!this.worldObj.worldProvider.hasNoSky) {

				// Skip light recompute if both blocks have identical light properties.
				// Swapping one opaque block for another with the same opacity/value
				// cannot change light levels.
				boolean sameLight = (Block.lightOpacity[existingId] == Block.lightOpacity[id])
				                 && (Block.lightValue[existingId]     == Block.lightValue[id]);

				if(!sameLight) {
					// If block is not 100% transparent
					if(Block.lightOpacity[id] != 0) {

						// And set above current topmost block
						if(y >= height) {
							// Relight from just above this new block
							this.relightBlock(x, y + 1, z);
						}
					} else {
						// Set a 100% transparent block

						// If it is replacing the topmost block, relight from here.
						if(y == height - 1) {
							this.relightBlock(x, y, z);
						}
					}
					this.updateLight(x, y, z);
				}
			}
			
			block = Block.blocksList[id];
			if(block != null) {
				block.onBlockAdded(this.worldObj, absX, y, absZ);
			}

			this.isModified = true;
			return true;
		}
	}
	
	/**
	 * Writes a column of blocks (bottom-to-top encoded, with run-length markers) starting at
	 * world Y {@code y}. Writes go into subchunk storage; when the flat generation buffer is still
	 * alive the same cells are mirrored there so a later {@link #loadFlatBlocks} sees them.
	 *
	 * @return true if light properties changed anywhere in the column (drives relight)
	 */
	public boolean setBlockIDAndMetadataColumn(int x, int y, int z, int[] id) {
		// Column is bottom to top ordered
		// Metadata is encoded as a most significant byte

		int absX = (this.xPosition << 4) | x;
		int absZ = (this.zPosition << 4) | z;

		int height = this.heightMap[z << 4 | x] & 255;

		// Running flat buffer offset (same addressing the generators use); also the source of the
		// subchunk-local offset so flat and section writes always stay in sync.
		int colBase = x << 11 | z << 7;
		int index = colBase | y;

		boolean lightChanged = false;

		// Write blocks
		for(int i = 0; i < id.length; i ++) {
			int b = id[i];
			if(b >= 0) {
				int newId = b & 255;
				int existingId = this.getBlockID(x, y, z);
				if(!lightChanged
					&& (Block.lightOpacity[existingId] != Block.lightOpacity[newId]
						|| Block.lightValue[existingId]     != Block.lightValue[newId])) {
					lightChanged = true;
				}

				// Subchunk write (materializes the section on first use).
				int colOff = index - colBase;
				int section = colOff >> 4;
				this.ensureSubchunk(section);
				this.sectionBlocks[section][(x << 8 | z << 4) | (colOff & 15)] = (byte)newId;
				this.sectionData[section][(x << 8 | z << 4) | (colOff & 15)] = (byte)((b >> 8) & 0xff);

				// Mirror into the flat generation buffer while it is still present.
				if(this.blocks != null && colOff < (SECTION_HEIGHT >> 1)) {
					this.blocks[index] = (byte)newId;
					this.data[index] = (byte)((b >> 8) & 0xff);
				}

				// Call `onRemoval` from removed block, if applies.
				Block block = Block.blocksList[existingId];
				if(block != null && !this.worldObj.isRemote) {
					block.onBlockRemoval(this.worldObj, absX, y, absZ);
				}

				if(newId == 0) {
					this.recomputeEmptyFlag(section);
				} else {
					this.isEmpty[section] = false;
				}

				index ++;
				y ++;
			} else if(b < -1) {
				// A negative value is the count for a run
				int c = -b;
				i ++;
				b = id[i];
				if(b == -1) {
					// A skip run: advance the flat offset only (historic behavior).
					index += c;
				} else {
					byte m = (byte) ((b >> 8) & 255);
					byte b0 = (byte) (b & 255);
					while (c -- > 0) {
						int existingId = this.getBlockID(x, y, z);
						if(!lightChanged
							&& (Block.lightOpacity[existingId] != Block.lightOpacity[b0 & 255]
								|| Block.lightValue[existingId]     != Block.lightValue[b0 & 255])) {
							lightChanged = true;
						}

						int colOff = index - colBase;
						int section = colOff >> 4;
						this.ensureSubchunk(section);
						this.sectionBlocks[section][(x << 8 | z << 4) | (colOff & 15)] = b0;
						this.sectionData[section][(x << 8 | z << 4) | (colOff & 15)] = m;

						if(this.blocks != null && colOff < (SECTION_HEIGHT >> 1)) {
							this.blocks[index] = b0;
							this.data[index] = m;
						}

						// Call `onRemoval` from removed block, if applies.
						Block block = Block.blocksList[existingId];
						if(block != null && !this.worldObj.isRemote) {
							block.onBlockRemoval(this.worldObj, absX, y, absZ);
						}

						if((b0 & 255) == 0) {
							this.recomputeEmptyFlag(section);
						} else {
							this.isEmpty[section] = false;
						}

						index ++;
						y ++;
					}
				}
			} else {
				y ++;
			};
			if(y >= SECTION_HEIGHT) break;
		}

		// The topmost block
		y --;

		// Skip relight if no block in the column changed light properties.
		if(lightChanged) {
			if (y >= height) this.relightBlock(x, y + 1, z);
			this.updateLight(x, y, z);
		}

		this.isModified = true;

		return true;
	}

	public boolean setBlockID(int x, int y, int z, int id) {
		return this.setBlockIDWithMetadata(x, y, z, id, 0);
	}

	/**
	 * Reads a block's metadata, consulting the subchunk storage (see {@link #getBlockID} for the
	 * flat fallback rule).
	 */
	public int getBlockMetadata(int x, int y, int z) {
		if(y >= 0 && y < SECTION_HEIGHT) {
			int section = y >> 4;
			byte[] sectionMetaData = this.sectionData[section];
			if(sectionMetaData != null) {
				return sectionMetaData[x << 8 | z << 4 | (y & 15)] & 255;
			}
			if(this.data != null && y < (SECTION_HEIGHT >> 1)) {
				return this.data[x << 11 | z << 7 | y] & 255;
			}
		}
		return 0;
	}

	public void setBlockMetadata(int x, int y, int z, int meta) {
		this.isModified = true;
		if(y >= 0 && y < SECTION_HEIGHT) {
			int section = y >> 4;
			this.ensureSubchunk(section);
			this.sectionData[section][x << 8 | z << 4 | (y & 15)] = (byte)meta;
		}
	}

	/**
	 * Reads a saved light nibble. Above the world (or in any unmaterialized section) the answer is
	 * implicit: sky light is full bright, block light is zero.
	 */
	public int getSavedLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z) {
		if(y < 0) return 0;
		if(y >= SECTION_HEIGHT) return enumSkyBlock == EnumSkyBlock.Sky ? 15 : 0;

		boolean isSky = enumSkyBlock == EnumSkyBlock.Sky;
		NibbleArray nibbles = isSky ? this.skyLightMap[y >> 4] : this.blockLightMap[y >> 4];
		if(nibbles == null) return isSky ? 15 : 0;

		return nibbles.getNibble(x, y & 15, z);
	}

	/**
	 * Writes a light nibble. Unmaterialized sections are left alone: as far as the light engine is
	 * concerned they are implicitly sky-15/block-0, and nothing outside the section allocates one.
	 */
	public void setLightValue(EnumSkyBlock enumSkyBlock, int x, int y, int z, int l) {
		if(y < 0 || y >= SECTION_HEIGHT) return;
		this.isModified = true;
		if(enumSkyBlock == EnumSkyBlock.Sky) {
			NibbleArray sky = this.skyLightMap[y >> 4];
			if(sky != null) {
				sky.setNibble(x, y & 15, z, l);
			}
		} else if(enumSkyBlock == EnumSkyBlock.Block) {
			NibbleArray block = this.blockLightMap[y >> 4];
			if(block != null) {
				block.setNibble(x, y & 15, z, l);
			}
		}

	}

	public int getBlockLightValue(int x, int y, int z, int skylightSubtracted) {
		int skylight = this.getSavedLightValue(EnumSkyBlock.Sky, x, y, z);

		if(skylight > 0) {
			isLit = true;
		}

		skylight -= skylightSubtracted;

		int blockLight = this.getSavedLightValue(EnumSkyBlock.Block, x, y, z);

		if(blockLight > skylight) {
			skylight = blockLight;
		}

		return skylight;
	}

	public void addEntity(Entity entity1) {
		this.hasEntities = true;
		int i2 = MathHelper.floor_double(entity1.posX / 16.0D);
		int i3 = MathHelper.floor_double(entity1.posZ / 16.0D);
		if(i2 != this.xPosition || i3 != this.zPosition) {
			System.out.println("Wrong location! " + entity1);
			Thread.dumpStack();
		}

		int i4 = MathHelper.floor_double(entity1.posY / 16.0D);
		if(i4 < 0) {
			i4 = 0;
		}

		if(i4 >= this.entities.length) {
			i4 = this.entities.length - 1;
		}

		entity1.addedToChunk = true;
		entity1.chunkCoordX = this.xPosition;
		entity1.chunkCoordY = i4;
		entity1.chunkCoordZ = this.zPosition;
		this.entities[i4].add(entity1);
	}

	public void removeEntity(Entity entity1) {
		this.removeEntityAtIndex(entity1, entity1.chunkCoordY);
	}

	public void removeEntityAtIndex(Entity entity1, int i2) {
		if(i2 < 0) {
			i2 = 0;
		}

		if(i2 >= this.entities.length) {
			i2 = this.entities.length - 1;
		}

		this.entities[i2].remove(entity1);
	}

	public boolean canBlockSeeTheSky(int i1, int i2, int i3) {
		return i2 >= (this.heightMap[i3 << 4 | i1] & 255);
	}

	public TileEntity getChunkBlockTileEntity(int i1, int i2, int i3) {
		ChunkPosition chunkPosition4 = new ChunkPosition(i1, i2, i3);
		TileEntity tileEntity5 = (TileEntity)this.chunkTileEntityMap.get(chunkPosition4);
		if(tileEntity5 == null) {
			int i6 = this.getBlockID(i1, i2, i3);
			if(!Block.isBlockContainer[i6]) {
				return null;
			}

			BlockContainer blockContainer7 = (BlockContainer)Block.blocksList[i6];
			blockContainer7.onBlockAdded(this.worldObj, this.xPosition << 4 | i1, i2, this.zPosition << 4 | i3);
			tileEntity5 = (TileEntity)this.chunkTileEntityMap.get(chunkPosition4);
		}

		if(tileEntity5 != null && tileEntity5.isInvalid()) {
			this.chunkTileEntityMap.remove(chunkPosition4);
			return null;
		} else {
			return tileEntity5;
		}
	}

	public EntityBlockEntity getChunkBlockEntity(int x, int y, int z) {
		ChunkPosition chunkPosition = new ChunkPosition(x, y, z);
		EntityBlockEntity entity = this.chunkSpecialEntityMap.get(chunkPosition);
		if(entity == null) {
			Block block = Block.blocksList[this.getBlockID(x, y, z)];
			if(block == null || !(block instanceof BlockEntity)) {
				return null;
			}
			
			BlockEntity blockEntity = (BlockEntity)block;
			blockEntity.onBlockAdded(this.worldObj, this.xPosition << 4 | x, y, this.zPosition << 4 | z);
			entity = this.chunkSpecialEntityMap.get(chunkPosition);
		}
		
		return entity;
	}
		
	public EntityBlockEntity getChunkBlockEntityIfExists(int x, int y, int z) {
		ChunkPosition chunkPosition = new ChunkPosition(x, y, z);
		EntityBlockEntity entity = this.chunkSpecialEntityMap.get(chunkPosition);
		
		return entity;
	}

	public void addTileEntity(TileEntity tileEntity1) {
		int i2 = tileEntity1.xCoord - (this.xPosition << 4);
		int i3 = tileEntity1.yCoord;
		int i4 = tileEntity1.zCoord - (this.zPosition << 4);
		this.setChunkBlockTileEntity(i2, i3, i4, tileEntity1);
		if(this.isChunkLoaded) {
			this.worldObj.loadedTileEntityList.add(tileEntity1);
		}

	}
	
	public void addSpecialEntity(EntityBlockEntity entity) {
		int x = entity.xTile - (this.xPosition << 4);
		int y = entity.yTile;
		int z = entity.zTile - (this.zPosition << 4);
		this.setChunkBlockEntity(x, y, z, entity);
		
		this.hasEntities = true;
		int i2 = MathHelper.floor_double(entity.posX / 16.0D);
		int i3 = MathHelper.floor_double(entity.posZ / 16.0D);
		if(i2 != this.xPosition || i3 != this.zPosition) {
			System.out.println("Wrong location! " + entity);
		}

		int i4 = MathHelper.floor_double(entity.posY / 16.0D);
		if(i4 < 0) {
			i4 = 0;
		}

		if(i4 >= this.entities.length) {
			i4 = this.entities.length - 1;
		}

		entity.addedToChunk = true;
		entity.chunkCoordX = this.xPosition;
		entity.chunkCoordY = i4;
		entity.chunkCoordZ = this.zPosition;
		this.entities[i4].add(entity);
	}

	public void setChunkBlockTileEntity(int i1, int i2, int i3, TileEntity tileEntity4) {
		ChunkPosition chunkPosition5 = new ChunkPosition(i1, i2, i3);
		tileEntity4.worldObj = this.worldObj;
		tileEntity4.xCoord = this.xPosition << 4 | i1;
		tileEntity4.yCoord = i2;
		tileEntity4.zCoord = this.zPosition << 4 | i3;
		if(this.getBlockID(i1, i2, i3) != 0 && Block.blocksList[this.getBlockID(i1, i2, i3)] instanceof BlockContainer) {
			tileEntity4.validate();
			this.chunkTileEntityMap.put(chunkPosition5, tileEntity4);
		} else {
			System.out.println("Attempted to place a tile entity where there was no entity tile!");
		}
	}

	public void setChunkBlockEntity(int x, int y, int z, EntityBlockEntity entity) {
		ChunkPosition chunkPosition5 = new ChunkPosition(x, y, z); 
		entity.worldObj = this.worldObj;
		Block block = Block.blocksList[this.getBlockID(x, y, z)];
		if(block != null && block instanceof BlockEntity) {
			/*
			if(this.isChunkLoaded) {
				if(this.chunkSpecialEntityMap.get(chunkPosition5) != null) {
					this.world.loadedEntityList.remove(this.chunkSpecialEntityMap.get(chunkPosition5));
				}

				this.world.loadedEntityList.add(entity);
			}
			*/

			this.chunkSpecialEntityMap.put(chunkPosition5, entity);
		} else {
			System.out.println("Attempted to place a special entity where there was no entity tile! " + x + " " + y + " " + z + "   " + entity.getClass() + " block was " + this.getBlockID(x, y, z));
		}	
	}

	public void removeChunkBlockTileEntity(int i1, int i2, int i3) {
		ChunkPosition chunkPosition4 = new ChunkPosition(i1, i2, i3);
		if(this.isChunkLoaded) {
			TileEntity tileEntity5 = (TileEntity)this.chunkTileEntityMap.remove(chunkPosition4);
			if(tileEntity5 != null) {
				tileEntity5.invalidate();
			}
		}

	}

	public void removeChunkBlockEntity(int x, int y, int z) {
		ChunkPosition chunkPosition4 = new ChunkPosition(x, y, z);
		if(this.isChunkLoaded) {
			Entity entity = this.chunkSpecialEntityMap.remove(chunkPosition4);
			if(entity != null) {
				// System.out.println("Removing " + entity + this.worldObj.loadedEntityList.remove(entity));
				entity.setEntityDead();
			}
		}

	}
	
	public void onChunkLoad() {
		this.isChunkLoaded = true;
		this.worldObj.addTileEntity(this.chunkTileEntityMap.values());

		for(int i1 = 0; i1 < this.entities.length; ++i1) {
			this.worldObj.addLoadedEntities(this.entities[i1]);
		}

	}

	public void onChunkUnload() {
		this.isChunkLoaded = false;
		Iterator<TileEntity> iterator1 = this.chunkTileEntityMap.values().iterator();

		while(iterator1.hasNext()) {
			TileEntity tileEntity2 = (TileEntity)iterator1.next();
			tileEntity2.invalidate();
		}

		for(int i3 = 0; i3 < this.entities.length; ++i3) {
			this.worldObj.unloadEntities(this.entities[i3]);
		}

	}

	public void setChunkModified() {
		this.isModified = true;
	}

	public void getEntitiesWithinAABBForEntity(Entity entity1, AxisAlignedBB axisAlignedBB2, List<Entity> list3) {
		int i4 = MathHelper.floor_double((axisAlignedBB2.minY - 2.0D) / 16.0D);
		int i5 = MathHelper.floor_double((axisAlignedBB2.maxY + 2.0D) / 16.0D);
		if(i4 < 0) {
			i4 = 0;
		}

		if(i5 >= this.entities.length) {
			i5 = this.entities.length - 1;
		}

		for(int i6 = i4; i6 <= i5; ++i6) {
			List<Entity> list7 = this.entities[i6];

			for(int i8 = 0; i8 < list7.size(); ++i8) {
				Entity entity9 = (Entity)list7.get(i8);
				if(entity9 != entity1 && entity9.boundingBox.intersectsWith(axisAlignedBB2)) {
					list3.add(entity9);
				}
			}
		}

	}

	public void getEntitiesOfTypeWithinAAAB(Class<?> class1, AxisAlignedBB axisAlignedBB2, List<Entity> list3) {
		int i4 = MathHelper.floor_double((axisAlignedBB2.minY - 2.0D) / 16.0D);
		int i5 = MathHelper.floor_double((axisAlignedBB2.maxY + 2.0D) / 16.0D);
		if(i4 < 0) {
			i4 = 0;
		}

		if(i5 >= this.entities.length) {
			i5 = this.entities.length - 1;
		}

		for(int i6 = i4; i6 <= i5; ++i6) {
			List<Entity> list7 = this.entities[i6];

			for(int i8 = 0; i8 < list7.size(); ++i8) {
				Entity entity9 = (Entity)list7.get(i8);
				if(class1.isAssignableFrom(entity9.getClass()) && entity9.boundingBox.intersectsWith(axisAlignedBB2)) {
					list3.add(entity9);
				}
			}
		}

	}

	public boolean needsSaving(boolean z1) {
		if(this.neverSave) {
			return false;
		} else {
			if(z1) {
				if(this.hasEntities && this.worldObj.getWorldTime() != this.lastSaveTime) {
					return true;
				}
			} else if(this.hasEntities && this.worldObj.getWorldTime() >= this.lastSaveTime + 600L) {
				return true;
			}

			return this.isModified;
		}
	}

	/**
	 * Applies a wire chunk-data region (blocks plane, metadata plane, block-light plane, sky-light
	 * plane, all column-major over the (x, z) range with contiguous Y) into subchunk storage.
	 * A section is only materialized when the incoming block plane proves it contains a non-air
	 * block; pure-air sections stay unmaterialized (implicit air + full sky light + zero block
	 * light), which keeps client-side storage faithful to the server's materialization.
	 *
	 * @return the offset just past everything consumed from {@code rawData}
	 */
	public int setChunkData(byte[] rawData, int x1, int y1, int z1, int x2, int y2, int z2, int dataOffset) {
		int xSize = x2 - x1;
		int ySize = y2 - y1;
		int zSize = z2 - z1;

		boolean[] materialize = new boolean[SUBCHUNK_COUNT];

		// Pass 1: scan the block plane to find which sections carry at least one non-air block.
		int scanOffset = dataOffset;
		for(int x = x1; x < x2; ++x) {
			for(int z = z1; z < z2; ++z) {
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					if(!materialize[section]) {
						for(int k = yLocalFrom; k < yLocalTo && !materialize[section]; ++k) {
							if(rawData[scanOffset + (k - yLocalFrom)] != 0) {
								materialize[section] = true;
							}
						}
					}
					scanOffset += yLocalTo - yLocalFrom;
				}
			}
		}

		int x;
		int z;

		// Block plane.
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = dataOffset;
				dataOffset += ySize;
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					if(!materialize[section]) continue;
					this.ensureSubchunk(section);
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					System.arraycopy(rawData, columnBase + (yOff - y1) + yLocalFrom, this.sectionBlocks[section], x << 8 | z << 4 | yLocalFrom, count);
				}
			}
		}

		// Metadata plane.
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = dataOffset;
				dataOffset += ySize;
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					if(!materialize[section]) continue;
					this.ensureSubchunk(section);
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					System.arraycopy(rawData, columnBase + (yOff - y1) + yLocalFrom, this.sectionData[section], x << 8 | z << 4 | yLocalFrom, count);
				}
			}
		}

		// Block light nibble plane (two cells per wire byte).
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = dataOffset;
				dataOffset += ySize / 2;
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					if(!materialize[section]) continue;
					this.ensureSubchunk(section);
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					int srcIndex = columnBase + (yOff - y1 + yLocalFrom) / 2;
					int destIndex = (x << 8 | z << 4 | yLocalFrom) >> 1;
					System.arraycopy(rawData, srcIndex, this.blockLightMap[section].data, destIndex, count / 2);
				}
			}
		}

		// Sky light nibble plane.
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = dataOffset;
				dataOffset += ySize / 2;
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					if(!materialize[section]) continue;
					this.ensureSubchunk(section);
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					int srcIndex = columnBase + (yOff - y1 + yLocalFrom) / 2;
					int destIndex = (x << 8 | z << 4 | yLocalFrom) >> 1;
					System.arraycopy(rawData, srcIndex, this.skyLightMap[section].data, destIndex, count / 2);
				}
			}
		}

		this.generateHeightMap();
		this.generateLandSurfaceHeightMap();
		this.recomputeEmptyFlags();

		return dataOffset;
	}

	/**
	 * Serializes a chunk-data region into the wire layout (blocks plane, metadata plane, block-light
	 * plane, sky-light plane, all column-major over the (x, z) range with contiguous Y). Sections
	 * that are not materialized serialize as zeros for blocks/metadata (implicit air) and as
	 * full-bright 0xF nibbles for sky light.
	 *
	 * @return the offset just past everything written into {@code rawData}
	 */
	public int getChunkData(byte[] rawData, int x1, int y1, int z1, int x2, int y2, int z2, int arrayOffset) {
		int xSize = x2 - x1;
		int ySize = y2 - y1;
		int zSize = z2 - z1;

		int x;
		int z;

		// Block plane: one byte per cell, column-major, Y contiguous.
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = arrayOffset;
				arrayOffset += ySize;   // advance the plane regardless of materialized sections
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					byte[] blockData = this.sectionBlocks[section];
					if(blockData == null) continue;
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					System.arraycopy(blockData, x << 8 | z << 4 | yLocalFrom, rawData, columnBase + (yOff - y1) + yLocalFrom, count);
				}
			}
		}

		// Metadata plane.
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = arrayOffset;
				arrayOffset += ySize;
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					byte[] metaData = this.sectionData[section];
					if(metaData == null) continue;
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					System.arraycopy(metaData, x << 8 | z << 4 | yLocalFrom, rawData, columnBase + (yOff - y1) + yLocalFrom, count);
				}
			}
		}

		// Block light nibble plane.
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = arrayOffset;
				arrayOffset += ySize / 2;
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					NibbleArray nibbles = this.blockLightMap[section];
					if(nibbles == null) {
						// Implicit zero block light; the fresh wire buffer is already zeroed.
						continue;
					}
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					int srcIndex = (x << 8 | z << 4 | yLocalFrom) >> 1;
					int destIndex = columnBase + (yOff - y1 + yLocalFrom) / 2;
					System.arraycopy(nibbles.data, srcIndex, rawData, destIndex, count / 2);
				}
			}
		}

		// Sky light nibble plane: null sections serialize as full bright (0xF) since that is what
		// the light engine will read from them.
		for(x = x1; x < x2; ++x) {
			for(z = z1; z < z2; ++z) {
				int columnBase = arrayOffset;
				arrayOffset += ySize / 2;
				for(int section = y1 >> 4, sectionMax = (y2 - 1) >> 4; section <= sectionMax; ++section) {
					NibbleArray nibbles = this.skyLightMap[section];
					int yOff = section << 4;
					int yLocalFrom = Math.max(y1 - yOff, 0);
					int yLocalTo = Math.min(y2 - yOff, 16);
					int count = yLocalTo - yLocalFrom;
					int destIndex = columnBase + (yOff - y1 + yLocalFrom) / 2;
					if(nibbles == null) {
						Arrays.fill(rawData, destIndex, destIndex + count / 2, (byte)-1);
						continue;
					}
					int srcIndex = (x << 8 | z << 4 | yLocalFrom) >> 1;
					System.arraycopy(nibbles.data, srcIndex, rawData, destIndex, count / 2);
				}
			}
		}

		return arrayOffset;
	}

	public Random getRandomWithSeed(long j1) {
		return new Random(this.worldObj.getRandomSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ j1);
	}

	public boolean getIsChunkRendered() {
		return false;
	}

	/** Translates every unknown/removed block id, both in the flat buffer (if present) and in each materialized subchunk. */
	public void removeUnknownBlocks() {
		if(this.blocks != null) {
			ChunkBlockMap.translateBlocks(this.blocks);
		}
		for(int section = 0; section < SUBCHUNK_COUNT; ++section) {
			if(this.sectionBlocks[section] != null) {
				ChunkBlockMap.translateBlocks(this.sectionBlocks[section]);
			}
		}
	}
	
	public void refreshCaches() {
		WorldChunkManager manager = this.worldObj.getWorldChunkManager();
		BiomeGenBase biomeGen [] = null;
		biomeGen = manager.loadBlockGeneratorData(biomeGen, this.xPosition << 4, this.zPosition << 4, 16, 16);
		this.biomeGenCache = biomeGen.clone();
		if(manager.temperature != null) {
			this.setClimateCache(manager.temperature, manager.humidity);
		}
	}
	
	public BiomeGenBase getBiomeGenAt (int x, int z) {
		if (this.biomeGenCache == null) {
			// System.out.println ("Biomes for chunk " + this.xPosition + ", " + this.zPosition + " were not cached - caching now!");
			this.refreshCaches();
		}
		return this.biomeGenCache [x << 4 | z];
	}

	public float getTemperatureAt (int x, int z) {
		if(this.temperatureCache == null) {
			this.refreshCaches();
		}
		return this.temperatureCache [x << 4 | z];
	}

	public float getHumidityAt (int x, int z) {
		if(this.humidityCache == null) {
			this.refreshCaches();
		}
		return this.humidityCache [x << 4 | z];
	}

	/** Seeds the per-column climate caches from the given temperature/humidity grids (index x << 4 | z). */
	public void setClimateCache(float[] temperature, float[] humidity) {
		if(temperature == null || humidity == null) {
			return;
		}
		if(this.temperatureCache == null) {
			this.temperatureCache = new float[256];
		}
		if(this.humidityCache == null) {
			this.humidityCache = new float[256];
		}
		int length = Math.min(temperature.length, Math.min(humidity.length, 256));
		for(int i = 0; i < length; ++i) {
			this.temperatureCache[i] = temperature[i];
			this.humidityCache[i] = humidity[i];
		}
	}

	/** Seeds the per-column climate caches from the manager's double grids (index x << 4 | z). */
	public void setClimateCache(double[] temperature, double[] humidity) {
		if(temperature == null || humidity == null) {
			return;
		}
		if(this.temperatureCache == null) {
			this.temperatureCache = new float[256];
		}
		if(this.humidityCache == null) {
			this.humidityCache = new float[256];
		}
		int length = Math.min(temperature.length, Math.min(humidity.length, 256));
		for(int i = 0; i < length; ++i) {
			this.temperatureCache[i] = (float)temperature[i];
			this.humidityCache[i] = (float)humidity[i];
		}
	}

	/**
	 * Sets a block (id + metadata) without touching the light maps, materializing its subchunk on
	 * first use. Used by schematic placers that re-light the whole chunk in one pass afterwards.
	 *
	 * @return true if the cell changed
	 */
	public boolean setBlockIDWithMetadataNoLights(int x, int y, int z, int id, int metadata) {
		if(y < 0 || y >= SECTION_HEIGHT) {
			return false;
		}

		int section = y >> 4;
		this.ensureSubchunk(section);
		byte[] sectionBlockData = this.sectionBlocks[section];
		byte[] sectionMetaData = this.sectionData[section];
		int index = x << 8 | z << 4 | (y & 15);
		int existingId = sectionBlockData[index] & 255;
		if(existingId == id && sectionMetaData[index] == metadata) {
			return false;
		} else {
			int absX = (this.xPosition << 4) | x;
			int absZ = (this.zPosition << 4) | z;

			Block block = Block.blocksList[existingId];

			// Write new block ID
			sectionBlockData[index] = (byte)id;

			// Call `onRemoval` from removed block, if applies.
			if(block != null && !this.worldObj.isRemote) {
				block.onBlockRemoval(this.worldObj, absX, y, absZ);
			}

			// Write new metadata
			sectionMetaData[index] = (byte)metadata;

			if(id == 0) {
				this.recomputeEmptyFlag(section);
			} else {
				this.isEmpty[section] = false;
			}

			block = Block.blocksList[id];
			if(block != null) {
				block.onBlockAdded(this.worldObj, absX, y, absZ);
			}

			this.isModified = true;
			return true;
		}
	}

	/** Resets both light planes of every materialized subchunk to zero ahead of a full relight pass. */
	public void clearAllLights() {
		for(int section = 0; section < SUBCHUNK_COUNT; ++section) {
			if(this.skyLightMap[section] != null) {
				this.skyLightMap[section].setAll(0);
			}
			if(this.blockLightMap[section] != null) {
				this.blockLightMap[section].setAll(0);
			}
		}
	}

	public void cacheBiomes(BiomeGenBase[] biomesForGeneration) {
		for(int x = 0; x < 16; x ++) {
			for(int z = 0; z < 16; z ++) {
				this.biomeGenCache[z | x << 4] = biomesForGeneration[z + x * 21];
			}
		}
		
	}

	public void setMetadata(byte[] metadata) {
		// TODO Auto-generated method stub
		
	}
	
	public void initLightingForRealNotJustHeightmap() {
		// Always rebuild from a zeroed slate: both Starlight init passes are pure increases, so any
		// stored light (e.g. the all-bright planes saved by the sky pre-fill bug, or planes carried
		// over from a legacy slice) would otherwise pin every occluded cell at its stored value
		// forever. Clearing first makes this a complete, self-contained relight for every caller.
		this.clearAllLights();

		this.worldObj.blockLight.initBlockLight(this.xPosition, this.zPosition);

		if (!this.worldObj.worldProvider.hasNoSky) {
			this.worldObj.skyLight.initSkylight(this.xPosition, this.zPosition);
		}
	}

	public void updateLight(int localX, int worldY, int localZ) {
		int worldX = localX | (this.xPosition << 4);
		int worldZ = localZ | (this.zPosition << 4);

		this.worldObj.blockLight.checkBlockEmittance(worldX, worldY, worldZ);
		if (!this.worldObj.worldProvider.hasNoSky) {
			this.worldObj.skyLight.checkSkyEmittance(worldX, worldY, worldZ);
		}
	}
}