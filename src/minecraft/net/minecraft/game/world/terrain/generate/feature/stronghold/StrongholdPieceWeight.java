package net.minecraft.game.world.terrain.generate.feature.stronghold;

/**
 * One entry of the stronghold piece-selection table: a piece class, its selection weight
 * and how many times it may still be spawned (0 = unlimited). Port of the vanilla
 * {@code StructureStrongholdPieceWeight} / {@code StructureStrongholdPieceWeight2}; the
 * latter's "only when deep enough" rule is folded into {@link #onlyDeep}.
 */
public class StrongholdPieceWeight {
	public final Class<?> pieceClass;
	public final int pieceWeight;
	public final int instancesLimit;
	/** True for pieces that may only be chosen once the corridor depth counter exceeds 4. */
	public final boolean onlyDeep;
	public int instancesSpawned;

	public StrongholdPieceWeight(Class<?> pieceClass, int pieceWeight, int instancesLimit, boolean onlyDeep) {
		this.pieceClass = pieceClass;
		this.pieceWeight = pieceWeight;
		this.instancesLimit = instancesLimit;
		this.onlyDeep = onlyDeep;
	}

	public boolean canSpawnMoreStructuresOfType(int depth) {
		if(this.onlyDeep && depth <= 4) {
			return false;
		}
		return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
	}

	public boolean canSpawnMoreStructures() {
		return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
	}
}
