package net.minecraft.game.world.terrain.generate.feature.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Holds the catalogue of {@link RoomFactory}s for one structure kind and drives piece
 * selection. Subclasses implement {@link #createNextPiece} to express their own selection
 * rule (plain weighted, forced-first, near-surface biased, ...).
 *
 * <p>A pool instance is owned by a single {@link FeatureStructure}, so its mutable state
 * ({@link RoomFactory#instancesPlaced}, {@link #lastFactory}, ...) is per-instance rather
 * than static - structures can therefore be built concurrently and deterministically.
 */
public abstract class StructureRoomPool {
	protected final List<RoomFactory> factories = new ArrayList<RoomFactory>();
	protected RoomFactory lastFactory;

	public void add(RoomFactory factory) {
		this.factories.add(factory);
	}

	/** Clears per-build counters before a structure is generated. */
	public void reset() {
		for(RoomFactory factory : this.factories) {
			factory.instancesPlaced = 0;
			factory.exhausted = false;
		}
		this.lastFactory = null;
	}

	public RoomFactory getLastFactory() {
		return this.lastFactory;
	}

	public abstract StructurePiece createNextPiece(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth);

	protected int totalWeight(int depth) {
		int total = 0;
		for(RoomFactory factory : this.factories) {
			if(factory.canPlaceMore(depth)) {
				total += factory.weight;
			}
		}
		return total;
	}

	/** Picks a factory proportional to weight among those still eligible at {@code depth}. */
	protected RoomFactory pickWeighted(Random rand, int depth) {
		int total = this.totalWeight(depth);
		if(total <= 0) {
			return null;
		}

		int roll = rand.nextInt(total);
		for(RoomFactory factory : this.factories) {
			if(factory.canPlaceMore(depth)) {
				roll -= factory.weight;
				if(roll < 0) {
					return factory;
				}
			}
		}

		return null;
	}

	/** Selects a factory, builds its piece, records the placement and returns it (or null). */
	protected StructurePiece place(RoomFactory factory, StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		if(factory == null) {
			return null;
		}

		StructurePiece piece = factory.tryPlace(parent, pieces, rand, x, y, z, orientation, depth);
		if(piece != null) {
			factory.onPlaced();
			this.lastFactory = factory;
		}
		return piece;
	}
}
