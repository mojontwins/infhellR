package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.entity.animal.EntityGoat;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityColdCow;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityIceSkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.world.terrain.generate.WorldGenIgloos;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;



public class BiomeGenColdForest extends BiomeGenThickForest {
	public BiomeGenColdForest() {
		super();
		this.weather = Weather.cold;
		this.copperLumpAttempts = 2;
		
		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityColdCow.class, 8));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityGoat.class, 6));
		
		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 5));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombieAlex.class, 5));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityIceSkeleton.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.addCityCreatures();
	}
	
	public int getAlgaeAmount() {
		return 1;
	}
	
	public int getCoralAmount() {
		return 8;
	}
	
	public void populate (World world, Random rand, int chunkX, int chunkZ) {
		super.populate(world, rand, chunkX, chunkZ);
		
		int x, y, z;
	
		// Generate igloos
		if (rand.nextInt(64) == 0) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			WorldGenIgloos worldgenIgloos = new WorldGenIgloos();
			worldgenIgloos.generate(world, rand, x, world.getHeightValue(x, z), z);
		}
		
		// Generate snow pools
		for(int i = 0; i < 20; ++i) {
			x = chunkX + rand.nextInt(16);
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16);
			(new WorldGenMinable(Block.blockSnow.blockID, 32)).generate(world, rand, x, y, z);
		}
	}
}

