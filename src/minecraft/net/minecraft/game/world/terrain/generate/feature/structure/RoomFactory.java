package net.minecraft.game.world.terrain.generate.feature.structure;

import java.util.List;
import java.util.Random;

/**
 * One weighted entry in a {@link StructureRoomPool} - describes a kind of room and how
 * often it may appear. Renaming/adding room kinds is simply a matter of adding another
 * factory to a pool, which keeps the structure system easily expandable.
 */
public abstract class RoomFactory {
	public final Class<?> pieceClass;
	public final int weight;
	public final int instanceLimit;
	public final int minDepth;
	public final boolean onSurface;

	public int instancesPlaced;
	public boolean exhausted;

	public RoomFactory(Class<?> pieceClass, int weight, int instanceLimit, int minDepth, boolean onSurface) {
		this.pieceClass = pieceClass;
		this.weight = weight;
		this.instanceLimit = instanceLimit;
		this.minDepth = minDepth;
		this.onSurface = onSurface;
	}

	/** @param limit 0 means unlimited */
	public boolean canPlaceMore(int depth) {
		return !this.exhausted && depth >= this.minDepth && (this.instanceLimit <= 0 || this.instancesPlaced < this.instanceLimit);
	}

	public void onPlaced() {
		++this.instancesPlaced;
		if(this.instanceLimit > 0 && this.instancesPlaced >= this.instanceLimit) {
			this.exhausted = true;
		}
	}

	public abstract StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth);
}
