package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityDrowned;
import net.minecraft.game.entity.monster.EntityElementalCreeper;
import net.minecraft.game.entity.monster.EntityPigZombieVolcanoes;
import net.minecraft.game.entity.monster.EntitySkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenHollowLogs;
import net.minecraft.game.world.terrain.generate.tree.EnumTreeType;
import net.minecraft.game.world.terrain.generate.tree.WorldGenHugeTrees;
import net.minecraft.game.world.terrain.generate.tree.WorldGenMangrove;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.WorldGenLilypad;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.WorldGenVines;
import net.minecraft.game.world.terrain.generate.WorldGenWitchHut;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenMangrove extends BiomeGenBase {
	public BiomeGenMangrove() {
		super();
		
		this.bigTreesEach10Trees = 9;
		this.treeBaseAttemptsModifier = 5;
		this.tallGrassAttempts = 128;
		this.redFlowersAttempts = 0;
		this.yellowFlowersAttempts = 0;
		this.deadBushAttempts = 4;
		this.genBeaches = false;
		this.copperLumpAttempts = 8;
		
		this.weather = Weather.hot;
		
		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.spawnableMonsterList.add(new SpawnListEntry(EntityDrowned.class, 30));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityElementalCreeper.class, 1));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityPigZombieVolcanoes.class, 30));
		
		this.addCityCreatures();
		
		this.minHeight = -0.1F;
		this.maxHeight = 0.125F;
	}
	
	public WorldGenerator genTreeTryFirst(Random rand) {
		return new WorldGenMangrove(false);
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		return new WorldGenTrees();
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		if(rand.nextInt(64) == 0) return new WorldGenHugeTrees(16 + rand.nextInt(16));
		return new WorldGenMangrove(false);
	}
	
	public void prePopulate(World world, Random rand, int x0, int z0) {
		int x, y, z;
		
		// Mangrove is very wet. After squishing terrain I must add tons of water.
		
		// Generate lakes
		for(int i = 0; i < 6; i++) {
			x = x0 + rand.nextInt(16) + 8;			
			z = z0 + rand.nextInt(16) + 8;
			y = world.getHeightValue(x, z);
			(new WorldGenLakes(Block.waterMoving.blockID)).generate(world, rand, x, y, z);
		}
		
		// Witch hut
		if(rand.nextInt(256) == 0) {
			x = x0 + /*rand.nextInt(16)*/ + 8;
			z = z0 + /*rand.nextInt(16)*/ + 8;
			y = 64 + rand.nextInt(4);
			(new WorldGenWitchHut()).generate(world, rand, x, y, z);
		}
		
		// Add special tree
		x = x0 + rand.nextInt(16) + 8;
		z = z0 + rand.nextInt(16) + 8;
		y = world.findTopSolidBlockUsingBlockMaterial(x, z) - 2;
		
		switch (rand.nextInt(4)) {
		case 0:	this.bo3Tree.setTreeName("Oak:SpookySwamp" + (1 + rand.nextInt(2))); break;
		case 1: this.bo3Tree.setTreeName("Oak:OakSwamp" + (1 + rand.nextInt(6))); break;
		case 2: this.bo3Tree.setTreeName("Oak:OakMangroveTree" + (1 + rand.nextInt(8))); break;
		case 3: this.bo3Tree.setTreeName("Oak:OakWetlandWillow" + (1 + rand.nextInt(4))); break;
		}
		this.bo3Tree.withLeavesMeta(EnumTreeType.MANGROVE.getLeafMetadata() | 2);
		this.bo3Tree.setExtendBottom(true);
		this.bo3Tree.generate(world, rand, x, y, z);
		this.bo3Tree.setExtendBottom(false);
		
	}
	
	public void populate (World world, Random rand, int x0, int z0) {
		int x, y, z, i;
		
		// Hollow logs
		for(i = 0; i < rand.nextInt(4); i ++) {
			x = x0 + rand.nextInt(16);
			z = z0 + rand.nextInt(16);
			y = world.getHeightValue(x, z);
			(new WorldGenHollowLogs ()).generate(world, rand, x, y, z);
		}
		
		// Generate vines
		for(i = 0; i < 150; i++) {
			x = x0 + rand.nextInt(16) + 8;
			y = 32;
			z = z0 + rand.nextInt(16) + 8;
			
			(new WorldGenVines()).generate(world, rand, x, y, z);
		}
		
		// Generate Lilypads
		for(i = 0; i < 8; i ++) {
			x = x0 + rand.nextInt(16) + 8;
			z = z0 + rand.nextInt(16) + 8;
			
			for(y = rand.nextInt(128); y > 0 && world.getBlockId(x, y - 1, z) == 0; y --) {}
			
			(new WorldGenLilypad()).generate(world, rand, x, y, z);
		}
		
		// Blue Flowers
		for(i = 0; i < 10; ++i) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.blueFlower.blockID)).generate(world, rand, x, y, z);
		}
		
		// Glowshrooms
		for(i = 0; i < 8; i ++) {
			x = x0 + rand.nextInt(16) + 8;
			y = 64 + rand.nextInt(64);
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.glowshroom.blockID)).generate(world, rand, x, y, z);
		}
		
		// Surface schrooms
		for(i = 0; i < 8; i ++) {
			x = x0 + rand.nextInt(16) + 8;
			z = z0 + rand.nextInt(16) + 8;
			y = world.getHeightValue(x, z);
			(new WorldGenFlowers(Block.mushroomBrown.blockID)).generate(world, rand, x, y, z);
		}
		
		for(i = 0; i < 4; i ++) {
			x = x0 + rand.nextInt(16) + 8;
			z = z0 + rand.nextInt(16) + 8;
			y = world.getHeightValue(x, z);
			(new WorldGenFlowers(Block.mushroomRed.blockID)).generate(world, rand, x, y, z);
		}
	}	
	
	@Override
	public boolean isHumid() {
		return true;
	}
}
