package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * A mesa mineshaft stairwell. Identical to the regular stairwell
 * ({@link MineshaftComponentStairs}); the mesa version only gates drawing on the chunk
 * region being solid ground and draws in the world phase.
 */
public class MineshaftComponentMesaStairs extends MineshaftComponentStairs {
	public MineshaftComponentMesaStairs(World world, int componentType, Random rand, StructureBoundingBox boundingBox, int direction) {
		super(world, componentType, rand, boundingBox, direction);
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
		if(!this.isMostlySolid(writer, chunkBox)) {
			return;
		}
		super.generate(writer, rand);
	}
}
