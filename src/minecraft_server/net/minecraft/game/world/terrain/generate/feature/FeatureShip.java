package net.minecraft.game.world.terrain.generate.feature;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.schematics.SchematicsShips;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenGlacier;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureShip extends FeatureBetterDungeons {
	public SchematicsShips schematics;
	public int shipID;
	
	public FeatureShip(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public void setup(World world, Random rand, BiomeGenBase biomeGenBase, int chunkX, int chunkZ) {	
		super.setup(world, rand, biomeGenBase, chunkX, chunkZ);;
		
		this.schematics = new SchematicsShips(this.betterDungeons);
		this.shipID = this.rand.nextInt(schematics.getStructureCount());
		
		System.out.println ("Ship @ " + this.centerX + " " + this.centerZ);
	}
	
	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return !(biome instanceof BiomeGenGlacier) && world.isOceanChunk(chunkX, chunkZ);
	}
	
	@Override
	public void generate(int chunkX, int chunkZ, Chunk chunk) {
	}

	@Override
	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		if(chunkX == this.originChunkX && chunkZ == this.originChunkZ) {
			this.schematics.unpackStructureToWorldTransparentAtChunk(
					this.schematics.pickStructure(this.shipID),
					world, rand, chunkX << 4, world.getWorldInfo().getTerrainType().getSeaLevel(world) - 4 + 1, chunkZ << 4, 3
			);
		}
	}

	@Override
	public int getFeatureRadius() {
		return 0;
	}

	@Override
	public int getSpawnChance() {
		return 20;
	}

	@Override
	public int minimumSeparation() {
		return 4;
	}
}
