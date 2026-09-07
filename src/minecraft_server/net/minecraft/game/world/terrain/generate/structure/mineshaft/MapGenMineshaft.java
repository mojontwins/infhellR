package net.minecraft.game.world.terrain.generate.structure.mineshaft;

import net.minecraft.game.world.terrain.generate.structure.MapGenStructure;
import net.minecraft.game.world.terrain.generate.structure.StructureStart;

import net.minecraft.game.world.World;

public class MapGenMineshaft extends MapGenStructure {
	public MapGenMineshaft(World world) {
		this.world = world;
	}
	
	protected boolean canSpawnStructureAtCoords(World world, int cX, int cZ) {
		return this.rand.nextInt(100) == 0 && this.rand.nextInt(80) < Math.max(Math.abs(cX), Math.abs(cZ));
	}

	protected StructureStart getStructureStart(int cX, int cZ) {
		return new StructureMineshaftStart(this.world, this.rand, cX, cZ);
	}
}
