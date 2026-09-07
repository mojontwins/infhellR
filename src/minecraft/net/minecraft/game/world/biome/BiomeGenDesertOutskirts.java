package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.entity.animal.EntityGoat;
import net.minecraft.game.entity.animal.EntityBetaOcelot;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.world.terrain.generate.WorldGenDesertFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.tree.WorldGenPalmTree;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenDesertOutskirts extends BiomeGenDesert {
	public BiomeGenDesertOutskirts() {
		super();
		this.bigTreesEach10Trees = 5;
		this.fillerBlock = (byte)Block.dirt.blockID;
		this.treeBaseAttemptsModifier = 0;
		this.cactusAttempts = 10;
		this.pumpkinChance = 128;
		this.deadBushAttempts = 4;
		
		this.foliageColorizer = 1;
		
		this.weather = Weather.hot;
		
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityBetaOcelot.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityGoat.class, 6));
		
		this.addCityCreatures();
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
		
		// Generate tall grass
		for(int i = 0; i < 16; ++i) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(64) + 64;
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenDesertFlowers(Block.tallGrass.blockID, 0x10, 0x90)).generate(world, rand, x, y, z);
		}
		
		// Palms
		if(rand.nextInt(16) == 0) {
			x = x0 + rand.nextInt(16) + 8;
			z = z0 + rand.nextInt(16) + 8;
			y = world.getLandSurfaceHeightValue(x, z);
			
			WorldGenerator treeGen = null;
			boolean b1 = rand.nextBoolean();
			if (b1) {
				bo3Tree.setTreeName(rand.nextBoolean() ? "Beach:ShrubPalm1" : "Beach:ShrubPalm2");
				bo3Tree.withLeavesMeta(2);
				treeGen = bo3Tree;
			} else {
				treeGen = new WorldGenPalmTree(false);
			}
			treeGen.generate(world, rand, x, y, z);
		}
	}
	
	public byte getTopBlock(Random rand) {
		return (byte)(rand.nextBoolean() ? Block.grass.blockID : Block.sand.blockID);
	}
}
