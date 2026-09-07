package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityColdCow;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityIceSkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.world.terrain.generate.WorldGenFir;
import net.minecraft.game.world.terrain.generate.WorldGenIgloos;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;
import net.minecraft.game.world.terrain.generate.tree.WorldGenPineTree;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTaigaTree1;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTaigaTree2;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.terrain.generate.city.BuildingTaigaHut;





public class BiomeGenTaiga extends BiomeGenForest {
	public BiomeGenTaiga() {
		super();
		this.bigTreesEach10Trees = 5;
		this.treeBaseAttemptsModifier = 7;
		this.tallGrassAttempts = 128;
		this.copperLumpAttempts = 8;
		
		this.weather = Weather.cold;
		
		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityColdCow.class, 8));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 2));
		
		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 5));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombieAlex.class, 5));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityIceSkeleton.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.addCityCreatures();
	}
	
	public boolean isPermaFrost() {
		return true;
	}
	
	public int getAlgaeAmount() {
		return 1;
	}
	
	public int getCoralAmount() {
		return 8;
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		if(rand.nextInt(10) != 0) {
			return rand.nextBoolean() ? new WorldGenTaigaTree1() : new WorldGenTaigaTree2();
		}
		
		this.bo3Tree.setTreeName("Spruce:SpruceFir" + (1 + rand.nextInt(21)));
		return this.bo3Tree;		
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		if(rand.nextInt(60) != 0) {
			if(rand.nextInt(100) == 0) return new WorldGenPineTree(6 + rand.nextInt(8), false); //new WorldGenFir(4 + rand.nextInt(7), true);
			if(rand.nextInt(10) == 0) return new WorldGenFir(4 + rand.nextInt(4), false);
			
			return this.getTreeGen(rand);
		} 
		
		this.bo3Tree.setTreeName("Spruce:SpruceFirSmall" + (1 + rand.nextInt(15)));
		return this.bo3Tree;
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
	
	@Override
	public boolean isHumid() {
		return true;
	}
	
	@Override
	public void generate(Random rand, int y0, Chunk chunk) {
		if(rand.nextInt(64) == 0) {
			(new BuildingTaigaHut ()).generate(y0, chunk);
		}
	}

}
