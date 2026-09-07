package net.minecraft.game.world.biome;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityMooshroom;
import net.minecraft.game.world.terrain.generate.WorldGenBigMushroom;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;



public class BiomeGenMycelium extends BiomeGenBase {
	public BiomeGenMycelium() {		
		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntityMooshroom.class, 10));
		
		// Change stuff
		this.topBlock = (byte)Block.mycelium.blockID;
		this.fillerBlock = (byte)Block.dirt.blockID;
		this.genBeaches = false;
		this.copperLumpAttempts = 30;
		
		this.minHeight = -0.2F;
		this.maxHeight = 1.5F;
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
	}
	
	public void populate (World world, Random rand, int chunkX, int chunkZ) {
		int x, y, z, i;
		
		int amount = 4 + rand.nextInt(8);
		for(i = 0; i < amount; i ++) {
			x = chunkX + rand.nextInt(16);
			z = chunkZ + rand.nextInt(16);
			y = world.getHeightValue(x, z);
			(new WorldGenBigMushroom(rand.nextInt(3) == 0 ? 0 : 1)).generate(world, rand, x, y, z);
		}
		
		for(i = 0; i < 8; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(64);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.glowshroom.blockID)).generate(world, rand, x, y, z);
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
		
		// Generate coral
		for (i = 0; i < 64; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getHeightValueUnderWater (x, z) + 1;
			if(Block.coral.canBlockStay(world, x, y, z)) {
				world.setBlockAndMetadataWithNotify(x, y, z, Block.coral.blockID, 8 | rand.nextInt(3));
			}
		}
		
		// Surface schrooms
		for(i = 0; i < 8; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getHeightValue(x, z);
			(new WorldGenFlowers(Block.mushroomBrown.blockID)).generate(world, rand, x, y, z);
		}
		
		for(i = 0; i < 4; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getHeightValue(x, z);
			(new WorldGenFlowers(Block.mushroomRed.blockID)).generate(world, rand, x, y, z);
		}
	}
}
