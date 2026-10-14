package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.feature.StructureBoundingBox;

/**
 * The starting room of a mesa mineshaft. The room itself is drawn exactly like the
 * regular one ({@link MineshaftComponentRoom}); only its placement differs - it sits
 * above ground (base 66 + 0..31) and, like the whole mesa structure, is drawn during
 * population so it can be gated on the surrounding terrain actually being solid.
 */
public class MineshaftComponentMesaRoom extends MineshaftComponentRoom {
	public MineshaftComponentMesaRoom(World world, int componentType, Random rand, int x, int z) {
		super(world, componentType, rand, x, z, world.getLandSurfaceHeightValue(x, z) - 5);
	}

	/** No structural pass: the mesa draws everything in the world phase (see {@link #populate}). */
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
