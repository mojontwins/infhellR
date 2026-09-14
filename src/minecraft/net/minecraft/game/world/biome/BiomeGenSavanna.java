package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.entity.animal.EntityBetaOcelot;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.WorldGenLilypad;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBaobab;
import net.minecraft.game.world.terrain.generate.tree.WorldGenPalmTree;
import net.minecraft.game.world.terrain.generate.WorldGenSeaweed;
import net.minecraft.game.world.terrain.generate.WorldGenVines;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenSavanna extends BiomeGenBase {
	public BiomeGenSavanna() {
		super();
		this.bigTreesEach10Trees = 5;
		this.treeBaseAttemptsModifier = -2;
		this.tallGrassAttempts = 32;
		this.pumpkinChance = 0;
		this.deadBushAttempts = 4;

		this.weather = Weather.hot;
		
		this.spawnableCreatureList.add(new SpawnListEntry(EntityBetaOcelot.class, 10, true));
	}

	public WorldGenerator getTreeGen(Random rand) {
		if(rand.nextInt(3) == 0) {
			this.bo3Tree.setTreeName("Acacia:AcaciaSavanna" + (1 + rand.nextInt(18)));
			return this.bo3Tree;
		} else return new WorldGenBaobab(2+rand.nextInt(3));
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		return new WorldGenBaobab(6+rand.nextInt(7));
	}
	
	public byte getTopBlock(Random rand) {
		return (byte)(rand.nextInt(8) != 0 ? Block.grass.blockID : Block.dirtPath.blockID);
	}
	
	public void prePopulate(World world, Random rand, int x0, int z0) {
		int x, y, z;
		
		// Generate lakes
		if(rand.nextInt(4) == 0) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenLakes(Block.waterMoving.blockID)).generate(world, rand, x, y, z);
		}
		
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
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		int x, y, z;
		
		// Generate vines
		
		for (int i = 0; i < 50; i++) {
			x = chunkX + rand.nextInt(16) + 8;
			y = 64;
			z = chunkZ + rand.nextInt(16) + 8;
			
			(new WorldGenVines()).generate(world, rand, x, y, z);
		}
		
		// Generate Lilypads
		
		for (int i = 0; i < 8; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			
			for(y = rand.nextInt(128); y > 0 && world.getBlockId(x, y - 1, z) == 0; y --) {}
			
			(new WorldGenLilypad()).generate(world, rand, x, y, z);
		}
		
		// Generate algae
		int algae = rand.nextInt (32);
		 
		for (int i = 0; i < algae; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getHeightValueUnderWater (x, z);
			(new WorldGenSeaweed()).generate(world,  rand, x, y, z);
		}
		
		// Generate coral
		for (int i = 0; i < 32; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getHeightValueUnderWater (x, z) + 1;
			if(Block.coral.canBlockStay(world, x, y, z)) {
				world.setBlockAndMetadataWithNotify(x, y, z, Block.coral.blockID, 8 | rand.nextInt(3));
			}
		}
		
		// Palms
		if(rand.nextInt(3) == 0) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getLandSurfaceHeightValue(x, z);
			
			WorldGenerator treeGen = new WorldGenPalmTree(false);
			treeGen.generate(world, rand, x, y, z);
		}
	}
}
