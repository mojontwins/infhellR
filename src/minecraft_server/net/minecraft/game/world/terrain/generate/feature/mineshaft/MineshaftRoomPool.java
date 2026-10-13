package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.List;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.RoomFactory;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;
import net.minecraft.game.world.terrain.generate.feature.structure.StructureRoomPool;

/**
 * Room catalogue for a mineshaft. Uses the classic 80/10/10 split (crossing / stairs /
 * corridor); {@link #selectFactory(Random)} is the single extension point for adding
 * brand-new mineshaft rooms.
 */
public class MineshaftRoomPool extends StructureRoomPool {
	private final RoomFactory corridorFactory;
	private final RoomFactory stairsFactory;
	private final RoomFactory crossFactory;

	public MineshaftRoomPool() {
		this.corridorFactory = new RoomFactory(MineCorridorPiece.class, 70, 0, 0, false) {
			@Override
			public StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
				FeatureAABB box = MineCorridorPiece.findValidPlacement(pieces, rand, x, y, z, orientation);
				return box == null ? null : new MineCorridorPiece(depth, rand, box, orientation);
			}
		};
		this.stairsFactory = new RoomFactory(MineStairsPiece.class, 30, 0, 0, false) {
			@Override
			public StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
				FeatureAABB box = MineStairsPiece.findValidPlacement(pieces, rand, x, y, z, orientation);
				return box == null ? null : new MineStairsPiece(depth, rand, box, orientation);
			}
		};
		this.crossFactory = new RoomFactory(MineCrossPiece.class, 20, 0, 0, false) {
			@Override
			public StructurePiece tryPlace(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
				FeatureAABB box = MineCrossPiece.findValidPlacement(pieces, rand, x, y, z, orientation);
				return box == null ? null : new MineCrossPiece(depth, rand, box, orientation);
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
		if(roll >= 70) {
			return this.stairsFactory;
		}
		return this.corridorFactory;
	}

	@Override
	public StructurePiece createNextPiece(StructurePiece parent, List<StructurePiece> pieces, Random rand, int x, int y, int z, int orientation, int depth) {
		return this.place(this.selectFactory(rand), parent, pieces, rand, x, y, z, orientation, depth);
	}
}
