package net.minecraft.game.world.feature;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.schematics.SchematicsBigShip;
import net.minecraft.game.world.terrain.generate.betterdungeons.schematics.SchematicsBigShipCog;
import net.minecraft.game.world.terrain.generate.betterdungeons.schematics.SchematicsBigShipFishing;
import net.minecraft.game.world.terrain.generate.betterdungeons.schematics.SchematicsBigShipGaleon;
import net.minecraft.game.world.terrain.generate.betterdungeons.schematics.SchematicsBigShipIsland;
import net.minecraft.game.world.terrain.generate.betterdungeons.schematics.SchematicsBigShipShark;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenGlacier;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureBigShip extends FeatureBetterDungeons {
	public SchematicsBigShip schematics;
	public int shipID;
	
	public FeatureBigShip(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {	
		super(world, originChunkX, originChunkZ, featureProvider);
	}
	
	@Override
	public void setup(World world, Random rand, BiomeGenBase biomeGenBase, int chunkX, int chunkZ) {
		super.setup(world, rand, biomeGenBase, chunkX, chunkZ);
		
		this.shipID = this.rand.nextInt(5);
		
		switch(this.shipID) {
		case 0: schematics = new SchematicsBigShipGaleon(this.betterDungeons); break;
		case 1: schematics = new SchematicsBigShipShark(this.betterDungeons); break;
		case 2: schematics = new SchematicsBigShipCog(this.betterDungeons); break;
		case 3: schematics = new SchematicsBigShipFishing(this.betterDungeons); break;
		default: schematics = new SchematicsBigShipIsland(this.betterDungeons); break;
		}
		
		System.out.println ("Big ship @ " + this.centerX + " " + this.centerZ);
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
		// Draw schematic for this chunk
		
		/// Which piece? Castles are size 2 features, meaning 5x5 chunks. The center piece is (2, 2), so:
		int pieceX = chunkX - this.originChunkX + this.schematics.getStructurePiecesX() / 2;
		int pieceZ = chunkZ - this.originChunkZ + this.schematics.getStructurePiecesZ() / 2;
		int pieceIndex = pieceX * this.schematics.getStructurePiecesZ() + pieceZ;

		if(
			pieceX >= 0 && pieceX < this.schematics.getStructurePiecesX() &&
			pieceZ >= 0 && pieceZ < this.schematics.getStructurePiecesZ()
		) {
			this.schematics.drawShipPiece(world, rand, chunkX, world.getWorldInfo().getTerrainType().getSeaLevel(world) - 4 + 1, chunkZ, 3, pieceIndex);
		}
	}

	@Override
	public int getFeatureRadius() {
		return 2;
	}

	@Override
	public int getSpawnChance() {
		return 2;
	}

	@Override
	public int minimumSeparation() {
		return 16;
	}
}
