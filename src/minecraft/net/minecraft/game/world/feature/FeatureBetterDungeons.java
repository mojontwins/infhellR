package net.minecraft.game.world.feature;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.BetterDungeons;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.World;

public abstract class FeatureBetterDungeons extends Feature {
	protected BetterDungeons betterDungeons;
	protected Random rand;
	
	public FeatureBetterDungeons(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider)  {
		super(world, originChunkX, originChunkZ, featureProvider);
		this.betterDungeons = featureProvider.betterDungeons;
	}
	
	public void setup(World world, Random rand, BiomeGenBase biomeGenBase, int chunkX, int chunkZ) {	
		long seed = this.world.getRandomSeed() + originChunkX * 25117 + originChunkZ * 151121;
		this.rand = new Random(seed);
	}
}
