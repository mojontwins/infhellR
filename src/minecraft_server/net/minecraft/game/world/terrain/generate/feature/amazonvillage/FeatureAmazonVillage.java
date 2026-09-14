package net.minecraft.game.world.terrain.generate.feature.amazonvillage;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenForest;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.feature.AvailableBuildingWithProbability;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.FeatureBuilding;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;
import net.minecraft.game.world.terrain.generate.feature.FeatureVillage;

public class FeatureAmazonVillage extends FeatureVillage {
	
	/*
	 * Generates an amazon village formed by several kinds of buildings spreading from a center Atalaya.
	 * This is a size 2 feature, meaning it takes 5x5 chunks.
	 * Wells happens in the central chunk.
	 */
	
	public FeatureAmazonVillage(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {	
		super(world, originChunkX, originChunkZ, featureProvider);
	}
	
	@Override
	public AvailableBuildingWithProbability[] getAvailableBuildings() {
		return new AvailableBuildingWithProbability[] {
				new AvailableBuildingWithProbability(BuildingAmazonHouse1.class, 5),
				new AvailableBuildingWithProbability(BuildingAmazonHouse2.class, 5),
				new AvailableBuildingWithProbability(BuildingAmazonHut1.class, 5),
				new AvailableBuildingWithProbability(BuildingAmazonHut2.class, 5),
				new AvailableBuildingWithProbability(BuildingAmazonHayDeposit.class, 3),
				new AvailableBuildingWithProbability(BuildingAmazonCrops.class, 3),
				new AvailableBuildingWithProbability(BuildingAmazonWell.class, 2)
		};
	}
	
	@Override
	public void generateBuildingList() {
		/*
		 * The algorithm goes as follows: for current piece,
		 * - Iterate 1 to 3 times.
		 * - Select side, and offset.
		 * - Select new building.
		 * - Does it fit? place & recur
		 * 
		 * - Start from a well center piece.
		 */
		
		// Add the original piece, a well in the center of the originChunk
		int x = this.centerX - 2;
		int z = this.centerZ - 2;
		
		// Calling the constructor gets the object and sets its dimensions
		BuildingAmazon centerPiece = new BuildingAmazonAtalaya(this.world, this.randPieces.nextBoolean());
		
		// Calculate height based on the land surface height value
		int yAtCenter = Math.max(64, centerPiece.getLandSurfaceAtCenter(x, z));
		
		// This method sets where it is, and current chunk X/Z (for later clipping)
		centerPiece.placeBuilding(x, yAtCenter, z);
		
		this.generateBuildingListRecursive(centerPiece);
		System.out.println ("Placing BuildingAmazonAtalaya @ " + x + " " + yAtCenter + " " + z + " - Total buildings " + this.buildingList.size());
	}
	
	public void generateBuildingListRecursive(BuildingAmazon piece) {
		// Add current piece
		this.buildingList.add(piece);
		
		// Spawn (possibly) new pieces to each side of current piece
		int spawns = 4; 
		while(spawns -- > 0) {
			
			// Select new piece
			boolean rotated = this.randPieces.nextBoolean();
			BuildingAmazon newPiece = null;
			try {
				newPiece = (BuildingAmazon) this.getRandomBuildingClass().getConstructor(new Class[] {World.class, Boolean.TYPE}).newInstance(new Object[] {world, rotated});
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			int x = 0;
			int z = 0;
			int offsetX, offsetZ;
			
			// Select placement
			switch(this.randPieces.nextInt(4)) {
				case 0:
					// West (-x)
					offsetX = this.randPieces.nextInt(3) + 4;
					offsetZ = this.randPieces.nextInt(8) - 4;

					x = piece.xAbsolute - offsetX - newPiece.getWidth();
					z = piece.zAbsolute + offsetZ;
					break;
					
				case 1:
					// East (x)
					offsetX = this.randPieces.nextInt(3) + 4;
					offsetZ = this.randPieces.nextInt(8) - 4;

					x = piece.xAbsolute + piece.getWidth() + offsetX;
					z = piece.zAbsolute + offsetZ;
					break;
					
				case 2:
					// North (-z)
					offsetX = this.randPieces.nextInt(8) - 4;
					offsetZ = this.randPieces.nextInt(3) + 4;
					
					x = piece.xAbsolute + offsetX;
					z = piece.zAbsolute - newPiece.getLength() - offsetZ;
					break;
					
				case 3:
					// South (z)
					offsetX = this.randPieces.nextInt(8) - 4;
					offsetZ = this.randPieces.nextInt(3) + 4;
					
					x = piece.xAbsolute + offsetX;
					z = piece.zAbsolute + piece.getLength() + offsetZ;
			}
			
			// Temporally place
			int yAtCenter = newPiece.getLandSurfaceAtCenter(x, z);
			newPiece.placeBuilding(x, yAtCenter, z);
			
			// Boundary check
			if(!this.featureAABB.containsFully(newPiece.aabb)) break;
			
			// Does it fit?
			boolean collided = false;
			Iterator<FeatureBuilding> iterator = this.buildingList.iterator();
			while(iterator.hasNext() && !collided) {
				collided = newPiece.aabb.collidesWith(iterator.next().aabb);
			}
			
			// Does it *not* overlap a non suitable chunk?
			boolean badChunk = this.badChunksOnCorners(this.world, newPiece.aabb);
			
			// Does it not overlap any? so place & recurse!
			if(!badChunk && !collided && this.randPieces.nextBoolean () && yAtCenter >= 64) {
				this.generateBuildingListRecursive(newPiece);
			}
		}
	}
	
	public boolean badChunkInCorner(World world, int x, int z) {
		//if(!world.chunkExists(x, z)) return false; 
		return world.justGenerateForHeight(x, z).hasBuilding;
	}

	public boolean badChunksOnCorners(World world, FeatureAABB aabb) {
		return 
				this.badChunkInCorner(world, aabb.x1 >> 4, aabb.z1 >> 4) || 
				this.badChunkInCorner(world, aabb.x1 >> 4, aabb.z2 >> 4) || 
				this.badChunkInCorner(world, aabb.x2 >> 4, aabb.z1 >> 4) || 
				this.badChunkInCorner(world, aabb.x2 >> 4, aabb.z2 >> 4);		
	}
	
	@Override
	public int getFeatureRadius() {
		return 2;
	}

	@Override
	public int getSpawnChance() {
		return 16;
	}
	
	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return biome instanceof BiomeGenForest && !world.isOceanChunk(chunkX, chunkZ) && !world.isUrbanChunk(chunkX, chunkZ);
	}
	
	@Override
	public int minimumSeparation() {
		return 16;
	}
}
