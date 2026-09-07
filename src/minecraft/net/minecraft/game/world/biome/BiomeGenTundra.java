package net.minecraft.game.world.biome;

import java.util.Random;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityColdCow;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityIceSkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.world.terrain.generate.WorldGenIceSpike;
import net.minecraft.game.world.terrain.generate.WorldGenIgloos;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.terrain.generate.city.BuildingTaigaHut;
import net.minecraft.game.world.terrain.generate.tree.WorldGenShrub;





public class BiomeGenTundra extends BiomeGenPlains {
	public BiomeGenTundra() {
		super();
		this.pumpkinChance = 0;
		this.copperLumpAttempts = 2;
		
		this.weather = Weather.cold;
		
		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntityColdCow.class, 8));
		
		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntityIceSkeleton.class, 20));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.addCityCreatures();
		
		this.minHeight = -0.1F;
		this.maxHeight = 0.4F;
	}
	
	public boolean isPermaFrost() {
		return true;
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		return new WorldGenShrub();
	}
	
	public void populate (World world, Random rand, int chunkX, int chunkZ) {
		int x, y, z;
		
		// Generate IceSpike
		if (rand.nextInt(8) == 0) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			WorldGenIceSpike worldgenIceSpike = new WorldGenIceSpike();
			worldgenIceSpike.generate(world, rand, x, world.getHeightValue(x, z) + 1, z);
		}
	
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
	
	@Override
	public void generate(Random rand, int y0, Chunk chunk) {
		if(rand.nextInt(128) == 0) {
			(new BuildingTaigaHut ()).generate(y0, chunk);
		}
	}
}
