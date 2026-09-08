package net.minecraft.game.world.terrain.generate;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockSurfaceMoss;

/**
 * Seeds an initial patch of {@link BlockSurfaceMoss} on a suitable land
 * surface, then creeps a short chain of extra cells from it.
 *
 * <p>The anchor cell (given by the caller, on the land surface) must sit in a
 * strictly humid, temperate climate and over a {@link Block#canGrowMoss()}
 * ground block. Chain cells follow the anchor's original height (within one
 * block) and only need air above plus suitable ground — light and season are
 * not checked at generation time (runtime spreading is governed by
 * {@link BlockSurfaceMoss#updateTick}).</p>
 */
public class WorldGenSurfaceMoss extends WorldGenerator {
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if(y < 0 || y >= 127) return true;
		if(!world.isAirBlock(x, y + 1, z)) return true;

		Block groundBlock = Block.blocksList[world.getBlockId(x, y, z)];
		if(groundBlock == null || !groundBlock.canGrowMoss()) return true;

		// The anchor must sit in a strictly humid, temperate climate.
		float temperature = world.getTemperatureAt(x, z);
		float humidity = world.getHumidityAt(x, z);
		if(humidity <= 0.6F || temperature <= 0.4F || temperature >= 0.6F) return true;

		world.setBlock(x, y + 1, z, Block.surfaceMoss.blockID);

		// Creep a short chain of extra cells from the anchor.
		int anchorY = y;
		int currentX = x;
		int currentZ = z;
		int chainCells = 2 + rand.nextInt(4);

		for(int cell = 0; cell < chainCells; ++cell) {
			int nextX = currentX + rand.nextInt(3) - 1;
			int nextZ = currentZ + rand.nextInt(3) - 1;
			int nextY = world.getLandSurfaceHeightValue(nextX, nextZ);

			if(nextY < 0 || nextY >= 127) continue;
			if(Math.abs(nextY - anchorY) > 1) continue;
			if(!world.isAirBlock(nextX, nextY + 1, nextZ)) continue;

			Block nextGround = Block.blocksList[world.getBlockId(nextX, nextY, nextZ)];
			if(nextGround == null || !nextGround.canGrowMoss()) continue;

			world.setBlock(nextX, nextY + 1, nextZ, Block.surfaceMoss.blockID);
			currentX = nextX;
			currentZ = nextZ;
		}

		return true;
	}
}