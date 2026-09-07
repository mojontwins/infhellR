package net.minecraft.game.world.biome;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.world.terrain.generate.WorldGenDesertFlowers;



public class BiomeGenDesertWithGrass extends BiomeGenDesert {

	public BiomeGenDesertWithGrass() {
		super();
		
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 4));
	}

	public void populate(World world, Random rand, int x0, int z0) {
		super.populate(world, rand, x0, z0);
		
		int x, y, z, i;
		
		// Generate tall grass
		for(i = 0; i < 16; ++i) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(64) + 64;
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenDesertFlowers(Block.tallGrass.blockID, 0x10, 0x90)).generate(world, rand, x, y, z);
		}
	}
}
