package net.minecraft.game.world.terrain.generate.feature.mineshaftmesa;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.RoomFactory;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureRoomPool;

/**
 * Room catalogue for a mesa mineshaft. Uses the surface variant split (80% crossing /
 * 20% stairs / 20% corridor rolled against 80 and 60); {@link #selectFactory(Random)} is
 * the single extension point for adding brand-new mesa rooms.
 */
public class MineshaftMesaRoomPool extends StructureRoomPool {
	private final RoomFactory corridorFactory;
	private final RoomFactory stairsFactory;
	private final RoomFactory crossFactory;

	public MineshaftMesaRoomPool() {
		this.corridorFactory = new RoomFactory(MesaCorridorPiece.class, 60, 0, 0, false) {
			@Override
			public StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
				FeatureAABB box = MesaCorridorPiece.findValidPlacement(pieces, rand, x, y, z, orientation);
				return box == null ? null : new MesaCorridorPiece(depth, rand, box, orientation);
			}
		};
		this.stairsFactory = new RoomFactory(MesaStairsPiece.class, 20, 0, 0, false) {
			@Override
			public StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
				FeatureAABB box = MesaStairsPiece.findValidPlacement(pieces, rand, x, y, z, orientation);
				return box == null ? null : new MesaStairsPiece(depth, rand, box, orientation);
			}
		};
		this.crossFactory = new RoomFactory(MesaCrossPiece.class, 20, 0, 0, false) {
			@Override
			public StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
				FeatureAABB box = MesaCrossPiece.findValidPlacement(pieces, rand, x, y, z, orientation);
				return box == null ? null : new MesaCrossPiece(depth, rand, box, orientation);
			}
		};

		this.add(this.corridorFactory);
		this.add(this.stairsFactory);
		this.add(this.crossFactory);
	}

	protected RoomFactory selectFactory(Random rand) {
		int roll = rand.nextInt(100);
		if(roll >= 80) {
			return this.crossFactory;
		}
		if(roll >= 60) {
			return this.stairsFactory;
		}
		return this.corridorFactory;
	}

	@Override
	public StructurePiece createNextPiece(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		return this.place(this.selectFactory(rand), parent, pieces, rand, x, y, z, orientation, depth);
	}
}
