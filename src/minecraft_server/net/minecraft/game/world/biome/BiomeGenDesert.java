package net.minecraft.game.world.biome;

import java.util.Random;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityChickenBlack;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityHusk;
import net.minecraft.game.entity.monster.EntitySkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBigTreeDead;
import net.minecraft.game.world.terrain.generate.WorldGenDesertFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenDesert extends BiomeGenBase {
	public BiomeGenDesert() {
		super();
		this.dungeonAttempts = 10;
		this.clayAttempts = 20;
		this.dirtLumpAttempts = 10;
		this.diamondLumpAttempts = 4;
		this.diamondLumpMaxHeight = 24;
		this.treeBaseAttemptsModifier = -5;
		this.bigTreesEach10Trees = 10;
		this.yellowFlowersAttempts = 0;
		this.redFlowersAttempts = 0;
		this.mushroomBrownChance = 2;
		this.mushroomRedChance = 2;
		this.reedAttempts = 0;
		this.cactusAttempts = 20;
		this.waterFallAttempts = 20;
		this.lavaAttempts = 40;
		this.topBlock = this.fillerBlock = (byte)Block.sand.blockID;
		this.pumpkinChance = 0;
		this.deadBushAttempts = 6;
		this.glowLumpAttempts = 25;
		
		this.foliageColorizer = 1;
		
		this.weather = Weather.desert;
		
		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntityChickenBlack.class, 10));
		
		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntityHusk.class, 25));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.addCityCreatures();
	}
	
	public String getPreferedSpawner() {
		return "Husk";
	}
	
	public int getPreferedSpawnerChance() {
		return 4;
	}
	
	public int getPreferedSpawnerChanceOffset() {
		return 3;
	}
	
	public WorldGenerator getBigTreeGen() {
		return new WorldGenBigTreeDead();
	}
	
	public void prePopulate(World world, Random rand, int x0, int z0) {
		int x, y, z;
		
		// Generate lava lakes
		if(rand.nextInt(8) == 0) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(rand.nextInt(120) + 8);
			z = z0 + rand.nextInt(16) + 8;
			if(y < 64 || rand.nextInt(10) == 0) {
				(new WorldGenLakes(Block.lavaMoving.blockID)).generate(world, rand, x, y, z);
			}
		}
	}
	
	public void populate(World world, Random rand, int x0, int z0) {
		int x, y, z, i;
		
		for(i = 0; i < 2; ++i) {
			x = x0 + rand.nextInt(16);
			y = 12 + rand.nextInt(48);
			z = z0 + rand.nextInt(16);
			(new WorldGenMinable(Block.oreRuby.blockID, 7)).generate(world, rand, x, y, z);
		}
		
		for(i = 0; i < 2; ++i) {
			x = x0 + rand.nextInt(16);
			y = 12 + rand.nextInt(48);
			z = z0 + rand.nextInt(16);
			(new WorldGenMinable(Block.oreEmerald.blockID, 7)).generate(world, rand, x, y, z);
		}
		
		// Generate tall grass on high ground
		for(i = 0; i < 16; ++i) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(32) + 96;
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenDesertFlowers(Block.tallGrass.blockID, 0x10, 0x90)).generate(world, rand, x, y, z);
		}
	}
}
