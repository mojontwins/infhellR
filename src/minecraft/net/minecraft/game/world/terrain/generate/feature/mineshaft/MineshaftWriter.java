package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * Block access layer shared by the mineshaft pieces.
 *
 * <p>The two generation phases touch the world in very different ways, so this class
 * hides the difference behind one small API:
 *
 * <ul>
 *   <li><b>Generation phase</b> ({@code population == false}): the chunk has just been
 *       carved and still owns its flat 256-high generation buffers. Blocks are written
 *       straight into {@code chunk.blocks}/{@code chunk.data} at the flat index
 *       {@code x << 12 | z << 8 | y} - the same layout every other generator uses - so
 *       they survive the later {@code loadFlatBlocks} slice. Reads go through
 *       {@link Chunk#getBlockID}, which falls back to the flat buffer.</li>
 *   <li><b>Population phase</b> ({@code population == true}): the chunk is sliced into
 *       subchunks and the flat buffers are gone. Writes go through
 *       {@link Chunk#setBlockIDWithMetadata} and reads through the world.</li>
 * </ul>
 *
 * <p>Every write/read is clipped to the chunk the piece is currently being drawn into:
 * out-of-chunk cells are simply dropped (writes) or reported as {@code 0}/{@code -1}
 * (reads), mirroring the chunk bounding box the vanilla generator clipped against.
 */
public class MineshaftWriter {
	/** The world; only non-null in the population phase. */
	private final World world;
	/** The chunk currently being drawn into. */
	private final Chunk chunk;
	/** Absolute chunk bounds (inclusive). */
	private final int chunkMinX;
	private final int chunkMinZ;
	private final int chunkMaxX;
	private final int chunkMaxZ;
	/** True in the population phase, false during generation. */
	private final boolean population;
	/** Cached full-height bounding box of the current chunk (vanilla's chunk bounding box). */
	private final StructureBoundingBox chunkBox;

	public MineshaftWriter(Chunk chunk, boolean population) {
		this.world = population ? chunk.worldObj : null;
		this.chunk = chunk;
		this.population = population;
		this.chunkMinX = chunk.xPosition << 4;
		this.chunkMinZ = chunk.zPosition << 4;
		this.chunkMaxX = this.chunkMinX + 15;
		this.chunkMaxZ = this.chunkMinZ + 15;
		this.chunkBox = new StructureBoundingBox(this.chunkMinX, 0, this.chunkMinZ, this.chunkMaxX, 255, this.chunkMaxZ);
	}

	/**
	 * Full-height bounding box of the chunk currently being drawn. Mirrors the
	 * {@code StructureBoundingBox} the vanilla generator clipped every piece against.
	 */
	public StructureBoundingBox getChunkBox() {
		return this.chunkBox;
	}

	public boolean isPopulation() {
		return this.population;
	}

	/** Whether an absolute (x, z) column lies inside the chunk currently being drawn. */
	public boolean isInChunk(int x, int z) {
		return x >= this.chunkMinX && x <= this.chunkMaxX && z >= this.chunkMinZ && z <= this.chunkMaxZ;
	}

	public World getWorld() {
		return this.world;
	}

	public Chunk getChunk() {
		return this.chunk;
	}

	/**
	 * Writes a single block, clipped to this chunk. Returns true when the write landed
	 * inside the chunk (out-of-chunk writes are silently ignored).
	 */
	public boolean setBlock(int x, int y, int z, int blockID, int metadata) {
		if(y < 0 || y > 255) {
			return false;
		}
		if(x < this.chunkMinX || x > this.chunkMaxX || z < this.chunkMinZ || z > this.chunkMaxZ) {
			return false;
		}

		if(this.population) {
			this.chunk.setBlockIDWithMetadata(x & 15, y, z & 15, blockID, metadata);
		} else {
			int index = ((x & 15) << 12) | ((z & 15) << 8) | y;
			this.chunk.blocks[index] = (byte) blockID;
			this.chunk.data[index] = (byte) metadata;
		}
		return true;
	}

	/**
	 * Reads a block id. Cells outside this chunk (or outside the world height) read as
	 * {@code 0}, matching the vanilla {@code getBlockIdAtCurrentPosition} clipping.
	 */
	public int getBlockID(int x, int y, int z) {
		if(y < 0 || y > 255) {
			return 0;
		}
		if(x < this.chunkMinX || x > this.chunkMaxX || z < this.chunkMinZ || z > this.chunkMaxZ) {
			return 0;
		}
		return this.chunk.getBlockID(x & 15, y, z & 15);
	}

	/**
	 * Reads a block id, reporting {@code -1} for cells outside this chunk. Used by the
	 * decorative code that mimics vanilla's "no vertical clip" read (a sentinel that is
	 * distinct from air and from any real block id).
	 */
	public int getBlockIDNoClip(int x, int y, int z) {
		if(y < 0 || y > 255) {
			return -1;
		}
		if(x < this.chunkMinX || x > this.chunkMaxX || z < this.chunkMinZ || z > this.chunkMaxZ) {
			return -1;
		}
		return this.chunk.getBlockID(x & 15, y, z & 15);
	}

	/**
	 * Whether the sky is visible from the given (in-chunk) cell. Always false during
	 * generation because no world is available then.
	 */
	public boolean canBlockSeeTheSky(int x, int y, int z) {
		return this.world != null && this.world.canBlockSeeTheSky(x, y, z);
	}
}
