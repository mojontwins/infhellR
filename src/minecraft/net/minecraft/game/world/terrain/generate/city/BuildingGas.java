package net.minecraft.game.world.terrain.generate.city;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.Chunk;


public class BuildingGas extends BuildingSchematic {

	@Override
	public String getSchematic() {
		return "/resources/schematics/building/gas.schematic";
	}

	@Override
	public void populate(World world, Random rand, int x0, int y0, Chunk chunk) {
	}
}
