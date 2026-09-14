package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBigTree;
import net.minecraft.game.world.terrain.generate.tree.WorldGenEucalyptus1;
import net.minecraft.game.world.terrain.generate.tree.WorldGenEucalyptusBig;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenLilypad;
import net.minecraft.game.world.terrain.generate.tree.WorldGenPalmTree;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.tree.WorldGenWillow;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
import net.minecraft.game.world.terrain.generate.tree.WorldGenShrub;




public class BiomeGenHotForest extends BiomeGenForest {
	public BiomeGenHotForest() {
		super();
		this.bigTreesEach10Trees = 3;
		this.treeBaseAttemptsModifier = 5;
		this.tallGrassAttempts = 32;
		this.yellowFlowersAttempts = 6;
		this.redFlowersAttempts = 3;
		this.copperLumpAttempts = 8;
		
		this.weather = Weather.hot;
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		// same chance: acacia, vanilla, small eucalyptus, palm shrub 
		// Occasionally: willow
		
		if(rand.nextInt(64) == 0) {
			return new WorldGenWillow(5 + rand.nextInt(5));
		} else {
			switch (rand.nextInt(8)) {
			case 0:
			case 4:
				return new WorldGenTrees();
			case 1:
			case 5:
			case 6:
				return new WorldGenEucalyptus1();
			case 2:
				this.bo3Tree.setTreeName("Acacia:AcaciaSavanna" + (1 + rand.nextInt(18)));
				bo3Tree.withLeavesMeta(2);
				return this.bo3Tree;
			case 3:
				bo3Tree.setTreeName(rand.nextBoolean() ? "Beach:ShrubPalm1" : "Beach:ShrubPalm2");
				bo3Tree.withLeavesMeta(2);
				return this.bo3Tree;
			case 7:
			default:
				return new WorldGenShrub();
			}
		}
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		if(rand.nextInt(64) == 0) {
			return new WorldGenBigTree();
		} else {
			switch (rand.nextInt(3)) {
			case 0:
				return new WorldGenEucalyptus1(12 + rand.nextInt(8), 7, 12);
			case 1:
				return new WorldGenEucalyptusBig();
			case 2:
			default:
				return new WorldGenPalmTree(false);
			}
		}
		// TODO: same chance: big eucalyptus, XXL eucalyptus, palm
		// Occasionally: big vanilla
	}
	
	public int getAlgaeAmount() {
		return 48;
	}
	
	public int getCoralAmount() {
		return 64;
	}
	
	public int getNetherVinesPerChunk() {
		return 48;
	}
	
	public void prePopulate(World world, Random rand, int x0, int z0) {
		super.prePopulate(world, rand, x0, z0);
	}
	
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		super.populate(world, rand, chunkX, chunkZ);
		int x, y, z;
		
		// Generate Lilypads
		for(int i = 0; i < 8; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			
			for(y = rand.nextInt(128); y > 0 && world.getBlockId(x, y - 1, z) == 0; y --) {}
			
			(new WorldGenLilypad()).generate(world, rand, x, y, z);
		}
		
		// Paeonias
		for(int i = 0; i < 3; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.paeonia.blockID)).generate(world, rand, x, y, z);
		}
		
		// Blue Flowers
		for(int i = 0; i < 5; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.blueFlower.blockID)).generate(world, rand, x, y, z);
		}
		
		// Palms
		for(int i = 0; i < 4; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			y = world.getLandSurfaceHeightValue(x, z);
			
			WorldGenerator treeGen = null;
			
			if (rand.nextInt(8) == 0) {
				bo3Tree.setTreeName(rand.nextBoolean() ? "Beach:ShrubPalm1" : "Beach:ShrubPalm2");
				treeGen = bo3Tree;
			} else {
				treeGen = new WorldGenPalmTree(false);
			}
			treeGen.generate(world, rand, x, y, z);
		}
	}
}
