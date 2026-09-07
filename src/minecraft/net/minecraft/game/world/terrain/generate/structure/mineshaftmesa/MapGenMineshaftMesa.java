package net.minecraft.game.world.terrain.generate.structure.mineshaftmesa;

import net.minecraft.game.world.terrain.generate.structure.MapGenStructure;
import net.minecraft.game.world.terrain.generate.structure.StructureStart;

import net.minecraft.game.world.biome.BiomeGenMesa;
import net.minecraft.game.world.World;

public class MapGenMineshaftMesa extends MapGenStructure {
	public MapGenMineshaftMesa(World world) {
		this.world = world;
	}
	
	@Override
	protected boolean canSpawnStructureAtCoords(World world, int cX, int cZ) {
		return world.getWorldChunkManager().getBiomeGenAt((cX << 4) + 8, (cZ << 4) + 8) instanceof BiomeGenMesa && this.rand.nextInt(50) == 0;
	}

	@Override
	protected StructureStart getStructureStart(int cX, int cZ) {
		return new StructureMineshaftMesaStart(this.world, this.rand, cX, cZ);
	}

}
