package net.minecraft.game.world.terrain.generate.feature.schematics;

import java.util.Random;

public interface ISchematic {
	public short[][][][] getStructureList();
	
	public short[][][] pickupRandomStructure(Random rand);
}
