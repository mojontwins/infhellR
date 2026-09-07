package net.minecraft.game.world.biome;

import java.util.Random;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.WorldGenWillow;
import net.minecraft.game.world.terrain.generate.WorldGenerator;





public class BiomeGenWillowForest extends BiomeGenForest {

	public BiomeGenWillowForest() {
		super();
	}

	public WorldGenerator getTreeGen(Random rand) {
		return rand.nextInt(4) == 0 ? new WorldGenTrees() : new WorldGenWillow(4 + rand.nextInt(4));
	}
}
