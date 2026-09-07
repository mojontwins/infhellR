package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.monster.EntityElementalCreeper;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBigTree;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenGiantFlower1;
import net.minecraft.game.world.terrain.generate.WorldGenGiantFlower2;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenFlowerFields extends BiomeGenBase {
	public BiomeGenFlowerFields() {
		super();
		this.bigTreesEach10Trees = 1;
		this.treeBaseAttemptsModifier = 2;
		this.tallGrassAttempts = 32;
		this.redFlowersAttempts = 16;
		this.yellowFlowersAttempts = 24;
		this.pumpkinChance = 8;
		this.copperLumpAttempts = 30;
		
		this.spawnableMonsterList.add(new SpawnListEntry(EntityElementalCreeper.class, 1));
		
		this.minHeight = 0.1F;
		this.maxHeight = 0.2F;
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		if(rand.nextBoolean()) {
			return new WorldGenGiantFlower1();
		} else {
			return new WorldGenGiantFlower2();
		}
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		if(rand.nextBoolean()) {
			return new WorldGenTrees();
		} else {
			return new WorldGenBigTree();
		}
	}
	
	public void populate(World world, Random rand, int chunkX, int chunkZ) {	
		super.populate(world, rand, chunkX, chunkZ);
		int x, y, z;
	
		// Paeonias
		for(int i = 0; i < 16; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.paeonia.blockID)).generate(world, rand, x, y, z);
		}
		
		// Blue Flowers
		for(int i = 0; i < 16; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.blueFlower.blockID)).generate(world, rand, x, y, z);
		}
	}
	
	@Override
	public boolean isHumid() {
		return true;
	}
}
