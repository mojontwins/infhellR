package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.entity.animal.EntityGoat;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.monster.EntityPigZombieVolcanoes;
import net.minecraft.game.world.terrain.generate.WorldGenLakes;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.terrain.generate.WorldGenRockBoulder;



public class BiomeGenRocky extends BiomeGenBase {
	public BiomeGenRocky() {
		super();
		this.topBlock = this.fillerBlock = (byte)Block.stone.blockID;
		this.dirtLumpAttempts = 50;
		this.gravelLumpAttempts = 30;
		this.glowLumpAttempts = 25;
		this.copperLumpAttempts = 24;
		this.lavaAttempts = 100;
		this.treeBaseAttemptsModifier = 100;
		
		this.weather = Weather.hot;
		
		this.spawnableMonsterList.add(new SpawnListEntry(EntityPigZombieVolcanoes.class, 30));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityGoat.class, 6));
	}
	
	@Override
	public WorldGenerator getTreeGen(Random rand) {
		this.bo3Tree.setTreeName("Oak:Dead" + (1 + rand.nextInt(5)));
		this.bo3Tree.withLeavesMeta(2); // Yellowish
		return this.bo3Tree;
	}
	
	@Override
	public byte getTopBlock(Random rand) {
		return (byte)(rand.nextInt(8) != 0 ? Block.stone.blockID : Block.cobblestone.blockID);
	}
	
	@Override
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
	
	@Override
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
