package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * The mesa mineshaft factory: identical to {@link MineshaftPieces} except the stair roll
 * starts at 60 (vanilla's {@code StructureMineshaftMesaPieces}) and corridors are the
 * environment-aware {@link MineshaftComponentMesaCorridor}.
 */
public class MineshaftPiecesMesa extends MineshaftPieces {
	public MineshaftPiecesMesa(World world) {
		super(world, 60);
	}

	@Override
	protected MineshaftComponent createCorridor(int componentType, Random rand, StructureBoundingBox box, int direction) {
		return new MineshaftComponentMesaCorridor(this.world, componentType, rand, box, direction);
	}

	@Override
	protected MineshaftComponent createCross(int componentType, Random rand, StructureBoundingBox box, int direction) {
		return new MineshaftComponentMesaCross(this.world, componentType, rand, box, direction);
	}

	@Override
	protected MineshaftComponent createStairs(int componentType, Random rand, StructureBoundingBox box, int direction) {
		return new MineshaftComponentMesaStairs(this.world, componentType, rand, box, direction);
	}
}
