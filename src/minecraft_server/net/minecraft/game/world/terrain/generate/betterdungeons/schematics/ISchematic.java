package net.minecraft.game.world.terrain.generate.betterdungeons.schematics;

import java.util.Random;

public interface ISchematic {
	public short[][][][] getStructureList();
	
	public short[][][] pickupRandomStructure(Random rand);
}
