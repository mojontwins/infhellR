package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.Seasons;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityBetaOcelot;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.game.entity.monster.EntityElementalCreeper;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.WorldGenBigPine;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBigTree;
import net.minecraft.game.world.terrain.generate.WorldGenCaveVines;
import net.minecraft.game.world.terrain.generate.WorldGenCypress;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenHollowLogs;
import net.minecraft.game.world.terrain.generate.WorldGenLeafPile;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.WorldGenVines;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenDarkForest extends BiomeGenBase {
	public BiomeGenDarkForest() {
		super();
		this.bigTreesEach10Trees = 10;
		this.treeBaseAttemptsModifier = 32;
		this.tallGrassAttempts = 128;
		this.redFlowersAttempts = 16;
		this.yellowFlowersAttempts = 24;
		this.pumpkinChance = 16;
		this.copperLumpAttempts = 8;
		
		this.spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 2));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityElementalCreeper.class, 1));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityBetaOcelot.class, 5, true));
		
		this.minHeight = 0.1F;
		this.maxHeight = 0.5F;
	}

	public int getNetherVinesPerChunk() {
		return 32;
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		return new WorldGenTrees();		
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		if(rand.nextInt(100) == 0) {
			this.bo3Tree.setTreeName("DarkOak:DarkOakEnderGiant" + (1 + rand.nextInt(7)));
			return this.bo3Tree; 
		} else switch (rand.nextInt(6)) {
			case 0:	
				this.bo3Tree.setTreeName("Spruce:SpruceFirSmall" + (1 + rand.nextInt(15)));
				return this.bo3Tree;
				//return  new WorldGenFir(5+rand.nextInt(5), true);
			case 1: return new WorldGenCypress(5+rand.nextInt(5));
			case 2: 
			case 3: 
			case 4: return new WorldGenBigPine();
			default: return new WorldGenBigTree(7, false);
		}
	}
	
	public void populate(World world, Random rand, int chunkX, int chunkZ) {	
		super.populate(world, rand, chunkX, chunkZ);
		int x, y, z;
		
		// Generate vines
		for(int i = 0; i < 100; i++) {
			x = chunkX + rand.nextInt(16) + 8;
			y = 64;
			z = chunkZ + rand.nextInt(16) + 8;
			
			(new WorldGenVines()).generate(world, rand, x, y, z);
		}
		
		// Dead logs
		for(int i = 0; i < 3 + rand.nextInt(4); i ++) {
			x = chunkX + rand.nextInt(16);
			z = chunkZ + rand.nextInt(16);
			y = world.getHeightValue(x, z);
			(new WorldGenHollowLogs ()).generate(world, rand, x, y, z);
		}
		
		// Paeonias
		for(int i = 0; i < 12; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.paeonia.blockID)).generate(world, rand, x, y, z);
		}
		
		// Blue Flowers
		for(int i = 0; i < 10; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.blueFlower.blockID)).generate(world, rand, x, y, z);
		}
		
		// Extra trees
		int treeBaseAttempts = 2 + rand.nextInt(4);
		for(int i = 0; i < treeBaseAttempts; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getHeightValue(x, z);
			(new WorldGenTrees()).generate(world, rand, x, y, z);			
		}
		
		// Cave vines
		for (int i = 0; i < this.getNetherVinesPerChunk(); ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(96);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenCaveVines()).generate(world, rand, x, y, z);
		}
		
		// Generate leaves on autumn
		if(this.weather != Weather.cold && Seasons.currentSeason == Seasons.AUTUMN) {
			for(int i = 0; i < 64; i ++) {
				x = chunkX + rand.nextInt(16) + 8;
				z = chunkZ + rand.nextInt(16) + 8;
				y = world.getLandSurfaceHeightValue(x, z);
				(new WorldGenLeafPile()).generate(world, rand, x, y, z);
			}
		}
	}
	
	@Override
	public boolean isHumid() {
		return true;
	}
}
