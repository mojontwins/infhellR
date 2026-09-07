package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.entity.animal.EntityGoat;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityChickenBlack;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityHusk;
import net.minecraft.game.entity.monster.EntitySkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;
import net.minecraft.game.world.terrain.generate.WorldGenRockBoulder;



public class BiomeGenRockyDesert extends BiomeGenRocky {
	public BiomeGenRockyDesert() {
		super();
		
		this.dungeonAttempts = 10;
		this.reedAttempts = 0;
		this.cactusAttempts = 20;
		this.waterFallAttempts = 20;
		this.lavaAttempts = 40;
		this.pumpkinChance = 2;
		this.deadBushAttempts = 8;
		
		this.foliageColorizer = 1;
		
		this.weather = Weather.desert;
		
		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntityChickenBlack.class, 10));
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityGoat.class, 12));
		
		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntityHusk.class, 25));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.addCityCreatures();
	}
	
	public byte getTopBlock(Random rand) {
		return (byte)(rand.nextInt(4) == 0 ? Block.sandStone.blockID : Block.sand.blockID);
	}
	
	public void prePopulate(World world, Random rand, int x0, int z0) {
		int x, y, z;
		
		// Generate lakes
		for(int i = 0; i < 2; i ++) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenLakes(Block.waterMoving.blockID)).generate(world, rand, x, y, z);
		}
	}
	
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		int x, y, z, i;
		
		Chunk thisChunk = world.getChunkFromBlockCoords(chunkX, chunkZ);
		if (!thisChunk.hasBuilding && !thisChunk.hasRoad && rand.nextInt(8) == 0) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			WorldGenRockBoulder worldGenRockBoulder = new WorldGenRockBoulder();
			worldGenRockBoulder.generate(world, rand, x, world.getHeightValue(x, z) + 1, z);
		}
		
		for(i = 0; i < 10; ++i) {
			x = chunkX + rand.nextInt(16);
			y = rand.nextInt(64) + 48;
			z = chunkZ + rand.nextInt(16);
			(new WorldGenMinable(Block.regolith.blockID, 32)).generate(world, rand, x, y, z);
		}
		
		for(i = 0; i < 2; ++i) {
			x = chunkX + rand.nextInt(16);
			y = 12 + rand.nextInt(48);
			z = chunkZ + rand.nextInt(16);
			(new WorldGenMinable(Block.oreRuby.blockID, 7)).generate(world, rand, x, y, z);
		}
		
		for(i = 0; i < 2; ++i) {
			x = chunkX + rand.nextInt(16);
			y = 12 + rand.nextInt(48);
			z = chunkZ + rand.nextInt(16);
			(new WorldGenMinable(Block.oreEmerald.blockID, 7)).generate(world, rand, x, y, z);
		}
	}
}
