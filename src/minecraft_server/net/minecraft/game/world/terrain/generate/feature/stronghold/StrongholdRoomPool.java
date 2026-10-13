package net.minecraft.game.world.terrain.generate.feature.stronghold;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.RoomFactory;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureRoomPool;

/**
 * The stronghold room catalogue and its selection rule.
 *
 * <p>This reproduces the original weighted selection faithfully, including its quirks:
 * {@link #canAddStructurePieces()} only ever succeeds while a <em>limited</em> entry is
 * still unexhausted (so an all-unlimited catalogue would stop the stronghold dead), the
 * weight sum covers every remaining entry regardless of eligibility, a rolled entry is
 * rejected outright if it repeats the previous one, and pieces that are not surface-safe
 * are discarded while the attachment x is 64 or greater. If all five rolls fail, a bare
 * corridor is squeezed against whatever it first hits.
 *
 * <p>Per-structure mutable state (the forced room and the last-placed entry) lives on this
 * instance rather than a static, so strongholds can be built independently.
 */
public class StrongholdRoomPool extends StructureRoomPool {
	private static final List<Class<?>> SURFACE_ALLOWED = Arrays.<Class<?>>asList(
			StrongholdStraightPiece.class,
			StrongholdLeftTurnPiece.class,
			StrongholdRightTurnPiece.class,
			StrongholdCrossingPiece.class,
			StrongholdStairsStraightPiece.class);

	private Class<?> forcedRoom;
	private int pieceWeightTotal;

	public StrongholdRoomPool() {
		this.add(new StrongholdRoomFactory(StrongholdStraightPiece.class, 40, 0, 0));
		this.add(new StrongholdRoomFactory(StrongholdPrisonPiece.class, 5, 5, 0));
		this.add(new StrongholdRoomFactory(StrongholdLeftTurnPiece.class, 20, 0, 0));
		this.add(new StrongholdRoomFactory(StrongholdRightTurnPiece.class, 20, 0, 0));
		this.add(new StrongholdRoomFactory(StrongholdRoomCrossingPiece.class, 10, 20, 0));
		this.add(new StrongholdRoomFactory(StrongholdStairsStraightPiece.class, 5, 25, 0));
		this.add(new StrongholdRoomFactory(StrongholdStairsPiece.class, 10, 25, 0));
		this.add(new StrongholdRoomFactory(StrongholdCrossingPiece.class, 10, 20, 0));
		this.add(new StrongholdRoomFactory(StrongholdChestCorridorPiece.class, 5, 20, 0));
		this.add(new StrongholdRoomFactory(StrongholdLibraryPiece.class, 10, 10, 5));
	}

	@Override
	public void reset() {
		super.reset();
		this.forcedRoom = null;
	}

	/** Queues {@code roomClass} as the required next room (consumed by the next request). */
	public void setForcedRoom(Class<?> roomClass) {
		this.forcedRoom = roomClass;
	}

	@Override
	public StructurePiece createNextPiece(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		if(!this.canAddStructurePieces()) {
			return null;
		}

		if(this.forcedRoom != null) {
			Class<?> forced = this.forcedRoom;
			this.forcedRoom = null;
			StructurePiece piece = buildPiece(forced, pieces, rand, x, y, z, orientation, depth);
			if(piece != null) {
				return piece;
			}
		}

		for(int attempt = 0; attempt < 5; ++attempt) {
			int roll = rand.nextInt(this.pieceWeightTotal);
			for(int i = 0; i < this.factories.size(); ++i) {
				RoomFactory factory = this.factories.get(i);
				roll -= factory.weight;
				if(roll < 0) {
					if(!factory.canPlaceMore(depth) || factory == this.lastFactory) {
						break;
					}

					StructurePiece piece = buildPiece(factory.pieceClass, pieces, rand, x, y, z, orientation, depth);
					if(piece != null) {
						if(x >= 64 && !SURFACE_ALLOWED.contains(piece.getClass())) {
							continue;
						}

						factory.onPlaced();
						this.lastFactory = factory;
						if(factory.exhausted) {
							this.factories.remove(i);
						}
						return piece;
					}
				}
			}
		}

		FeatureAABB corridor = StrongholdCorridorPiece.findPlacement(pieces, rand, x, y, z, orientation);
		if(corridor != null && corridor.y1 > 1) {
			return new StrongholdCorridorPiece(depth, rand, corridor, orientation);
		}

		return null;
	}

	private boolean canAddStructurePieces() {
		if(this.factories.isEmpty()) {
			return false;
		}

		boolean anyLimited = false;
		this.pieceWeightTotal = 0;
		for(RoomFactory factory : this.factories) {
			this.pieceWeightTotal += factory.weight;
			if(factory.instanceLimit > 0 && !factory.exhausted) {
				anyLimited = true;
			}
		}

		return anyLimited;
	}

	private static StructurePiece buildPiece(Class<?> roomClass, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		if(roomClass == StrongholdStraightPiece.class) {
			return StrongholdStraightPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdPrisonPiece.class) {
			return StrongholdPrisonPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdLeftTurnPiece.class) {
			return StrongholdLeftTurnPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdRightTurnPiece.class) {
			return StrongholdRightTurnPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdRoomCrossingPiece.class) {
			return StrongholdRoomCrossingPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdStairsStraightPiece.class) {
			return StrongholdStairsStraightPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdStairsPiece.class) {
			return StrongholdStairsPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdCrossingPiece.class) {
			return StrongholdCrossingPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdChestCorridorPiece.class) {
			return StrongholdChestCorridorPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		if(roomClass == StrongholdLibraryPiece.class) {
			return StrongholdLibraryPiece.create(pieces, rand, x, y, z, orientation, depth);
		}
		return null;
	}

	/**
	 * A catalogue entry: only carries the class plus its weight/limit/depth rule. The pool
	 * drives placement itself, so {@link #tryPlace} simply defers to the shared builder.
	 */
	private static final class StrongholdRoomFactory extends RoomFactory {
		StrongholdRoomFactory(Class<?> pieceClass, int weight, int instanceLimit, int minDepth) {
			super(pieceClass, weight, instanceLimit, minDepth, false);
		}

		@Override
		public StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
			return buildPiece(this.pieceClass, pieces, rand, x, y, z, orientation, depth);
		}
	}
}
