package net.minecraft.game.world.biome;

import java.util.Random;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityColdCow;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityIceSkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTaigaTree1;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTaigaTree2;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.terrain.generate.city.BuildingTaigaHut;
import net.minecraft.game.world.terrain.generate.tree.WorldGenShrub;





public class BiomeGenTaigaBorder extends BiomeGenTaiga {

	public BiomeGenTaigaBorder() {
		super();
		this.bigTreesEach10Trees = 3;
		this.treeBaseAttemptsModifier = 0;
		this.tallGrassAttempts = 32;
		this.pumpkinChance = 32;
		this.copperLumpAttempts = 4;
		
		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntityColdCow.class, 8));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 2));
		
		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombieAlex.class, 5));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityIceSkeleton.class, 20));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.addCityCreatures();
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		return new WorldGenShrub();
	}

	public WorldGenerator getBigTreeGen(Random rand) {
		return rand.nextBoolean() ? new WorldGenTaigaTree1() : new WorldGenTaigaTree2();
	}
	
	@Override
	public void generate(Random rand, int y0, Chunk chunk) {
		if(rand.nextInt(96) == 0) {
			(new BuildingTaigaHut ()).generate(y0, chunk);
		}
	}
}
