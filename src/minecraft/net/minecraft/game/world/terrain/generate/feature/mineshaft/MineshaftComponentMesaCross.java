package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.structure.StructureBoundingBox;

/**
 * A mesa mineshaft crossing. The structure is identical to the regular cross
 * ({@link MineshaftComponentCross}); the mesa version only gates drawing on the chunk
 * region being neither liquid-filled nor mostly exposed to the sky, and draws in the
 * world phase.
 */
public class MineshaftComponentMesaCross extends MineshaftComponentCross {
	public MineshaftComponentMesaCross(World world, int componentType, Random rand, StructureBoundingBox boundingBox, int corridorDirection) {
		super(world, componentType, rand, boundingBox, corridorDirection);
	}

	@Override
	public void generate(MineshaftWriter writer, Random rand) {
	}

	@Override
	public void populate(MineshaftWriter writer, Random rand) {
		StructureBoundingBox chunkBox = writer.getChunkBox();
		if(this.isLiquidInChunk(writer, chunkBox)) {
			return;
		}
		if(this.getFractionSolid(writer, chunkBox) < 0.4F || this.getFractionExposed(writer, chunkBox) > 0.5F) {
			return;
		}
		super.generate(writer, rand);
		super.populate(writer, rand);
	}
}
