package net.minecraft.game.world.terrain.generate.feature.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.terrain.generate.feature.Feature;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;

/**
 * Base for multi-chunk structures (mineshafts, strongholds, ...).
 *
 * <p>Lifecycle: {@link #setup} builds the whole piece graph once, {@link #populate} then
 * draws only the pieces overlapping the chunk being populated, and {@link #generate} does
 * nothing. Structures are drawn "after caves" so carving never cuts them.
 *
 * <p>Two expansion models are supported by {@link #queueExpansion}:
 * <ul>
 *   <li><b>depth-first</b> (default): each piece recursively builds its own children;</li>
 *   <li><b>breadth-first</b>: pieces are queued in {@link #pendingExpansion} and drained by
 *       the subclass after the root is built (strongholds use this).</li>
 * </ul>
 */
public abstract class FeatureStructure extends Feature {
	protected List<StructurePiece> pieces = new ArrayList<StructurePiece>();
	protected List<StructurePiece> pendingExpansion = new ArrayList<StructurePiece>();
	protected FeatureAABB structureBox;
	/** Box of the root piece, used as the fixed reference for the extent limit. */
	protected FeatureAABB originBox;
	protected boolean mostlySolid;
	protected boolean queueExpansion;
	protected int maxHorizontalExtent;
	protected int maxBuildDepth;
	protected StructureRoomPool roomPool;

	public FeatureStructure(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
		this.mostlySolid = false;
		this.queueExpansion = false;
		this.maxHorizontalExtent = this.getFeatureRadius() * 16;
		this.maxBuildDepth = 8;
	}

	/** Creates the room catalogue for this structure kind. */
	public abstract StructureRoomPool createRoomPool();

	/** Creates the root piece at the structure's chosen origin (y included). */
	public abstract StructurePiece createRoot(Random rand, BiomeGenBase biome);

	public StructureRoomPool getRoomPool() {
		return this.roomPool;
	}

	public FeatureAABB getStructureBox() {
		return this.structureBox;
	}

	public boolean isMostlySolidStructure() {
		return this.mostlySolid;
	}

	@Override
	public int getFeatureRadius() {
		return FeatureProvider.MAX_FEATURE_RADIUS;
	}

	@Override
	public boolean isGeneratedAfterCaves() {
		// Structures must survive cave/ravine carving, so they draw in the after-caves pass.
		return true;
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		this.pieces.clear();
		this.pendingExpansion.clear();
		this.roomPool = this.createRoomPool();
		this.roomPool.reset();

		this.buildStructure(world, rand, biome);

		this.updateStructureBox();
		this.placeVertically(world, rand);
	}

	protected void buildStructure(World world, Random rand, BiomeGenBase biome) {
		StructurePiece root = this.createRoot(rand, biome);
		if(root == null) {
			return;
		}

		root.setOwner(this);
		this.pieces.add(root);
		this.originBox = root.getBox();
		root.buildConnections(null, this.pieces, rand);

		// Breadth-first expansion: pieces that asked to be visited later are drained here in
		// random order (strongholds use this; each drained piece may enqueue more).
		while(this.queueExpansion && !this.pendingExpansion.isEmpty()) {
			int index = rand.nextInt(this.pendingExpansion.size());
			StructurePiece piece = this.pendingExpansion.remove(index);
			piece.buildConnections(root, this.pieces, rand);
		}
	}

	protected void updateStructureBox() {
		if(this.pieces.isEmpty()) {
			this.structureBox = null;
			return;
		}

		FeatureAABB combined = FeatureAABB.empty();
		for(StructurePiece piece : this.pieces) {
			combined.expandTo(piece.getBox());
		}
		this.structureBox = combined;
	}

	/** Hook for subclasses that pin the structure to a vertical level (default: none). */
	protected void placeVertically(World world, Random rand) {
	}

	/**
	 * Entry point every {@link StructurePiece} uses to ask for its next neighbour.
	 *
	 * @param depth the vanilla "attachment depth" of the caller (0 for the root). A new
	 *              piece is refused once this exceeds {@link #maxBuildDepth}; the child is
	 *              assigned depth {@code depth + 1}, matching the original piece counter.
	 */
	public StructurePiece requestNextPiece(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		if(this.roomPool == null || depth > this.maxBuildDepth) {
			return null;
		}
		if(!this.withinExtent(x, z)) {
			return null;
		}

		StructurePiece piece = this.roomPool.createNextPiece(parent, pieces, rand, x, y, z, orientation, depth + 1);
		if(piece != null) {
			piece.setOwner(this);
			pieces.add(piece);
			if(this.queueExpansion) {
				this.pendingExpansion.add(piece);
			}
		}
		return piece;
	}

	/**
	 * Keeps the structure inside the provider's scan window: relative to the root piece
	 * (as the vanilla 80-block rule) and relative to the origin centre (so the whole
	 * structure stays within {@link FeatureProvider#MAX_FEATURE_RADIUS} chunks).
	 */
	protected boolean withinExtent(int x, int z) {
		int refX = this.originBox != null ? this.originBox.x1 : this.centerX;
		int refZ = this.originBox != null ? this.originBox.z1 : this.centerZ;
		if(Math.abs(x - refX) > this.maxHorizontalExtent || Math.abs(z - refZ) > this.maxHorizontalExtent) {
			return false;
		}
		return Math.abs(x - this.centerX) <= this.maxHorizontalExtent && Math.abs(z - this.centerZ) <= this.maxHorizontalExtent;
	}

	/* ------------------------------------------------------------------ *
	 *  Vertical placement helpers (mirror StructureStart)
	 * ------------------------------------------------------------------ */

	protected void markAvailableHeight(Random rand, int blocksUnderground) {
		if(this.structureBox == null) {
			return;
		}

		int maxY = 63 - blocksUnderground;
		int structureHeight = this.structureBox.getYSize() + 1;
		if(structureHeight < maxY) {
			structureHeight += rand.nextInt(maxY - structureHeight);
		}

		this.offsetStructure(0, structureHeight - this.structureBox.y2, 0);
	}

	protected void setRandomHeight(Random rand, int minY, int maxY) {
		if(this.structureBox == null) {
			return;
		}

		int range = maxY - minY + 1 - this.structureBox.getYSize();
		int target = range > 1 ? minY + rand.nextInt(range) : minY;
		this.offsetStructure(0, target - this.structureBox.y1, 0);
	}

	protected void offsetStructure(int dx, int dy, int dz) {
		if(this.structureBox != null) {
			this.structureBox.offset(dx, dy, dz);
		}
		for(StructurePiece piece : this.pieces) {
			if(piece.getBox() != null) {
				piece.getBox().offset(dx, dy, dz);
			}
		}
	}

	/* ------------------------------------------------------------------ *
	 *  Feature draw hooks
	 * ------------------------------------------------------------------ */

	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
		// Structures do all their drawing in populate().
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		if(this.pieces.isEmpty()) {
			return;
		}

		FeatureAABB chunkBox = new FeatureAABB(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, 255, (chunkZ << 4) + 15);
		Iterator<StructurePiece> iterator = this.pieces.iterator();
		while(iterator.hasNext()) {
			StructurePiece piece = iterator.next();
			if(piece.getBox().collidesWith(chunkBox) && !piece.generate(world, rand, chunkBox, this.mostlySolid)) {
				iterator.remove();
			}
		}
	}
}
