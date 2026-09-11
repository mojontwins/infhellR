package net.minecraft.game.world.terrain.generate.oceanruins;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.game.world.feature.AvailableBuildingWithProbability;
import net.minecraft.game.world.feature.FeatureAABB;
import net.minecraft.game.world.feature.FeatureBuilding;
import net.minecraft.game.world.feature.FeatureProvider;
import net.minecraft.game.world.feature.FeatureVillage;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenGlacier;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.World;

public class FeatureOceanRuins extends FeatureVillage {
	
	/*
	 * Generates a set of underwater ruins formed by several kinds of buildings sreading from a central piece.
	 * This is a size 3 feature, meaning it takes 7x7 chunks.
	 */

	public FeatureOceanRuins(World world, int originChunkX, int originChunkZ, FeatureProvider featureProvider) {
		super(world, originChunkX, originChunkZ, featureProvider);
	}

	@Override
	public AvailableBuildingWithProbability[] getAvailableBuildings() {
		return new AvailableBuildingWithProbability[] {
				new AvailableBuildingWithProbability(BuildingBuild1.class, 3),
				new AvailableBuildingWithProbability(BuildingBuild2.class, 3),
				new AvailableBuildingWithProbability(BuildingDerelict1.class, 2),
				new AvailableBuildingWithProbability(BuildingDerelict2.class, 2),
				new AvailableBuildingWithProbability(BuildingDome1.class, 3),
				new AvailableBuildingWithProbability(BuildingHut1.class, 5),
				new AvailableBuildingWithProbability(BuildingHut2.class, 5),
				new AvailableBuildingWithProbability(BuildingHut3.class, 5),
				new AvailableBuildingWithProbability(BuildingWalk.class, 3),
				new AvailableBuildingWithProbability(BuildingDigme.class, 1)
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
		BuildingOceanRuin centerPiece = new BuildingDigme(this.world, this.randPieces.nextBoolean());
		
		// Calculate height based on the land surface height value
		int yAtCenter = centerPiece.getLandSurfaceAtCenter(x, z);
		
		// This method sets where it is, and current chunk X/Z (for later clipping)
		centerPiece.placeBuilding(x, yAtCenter, z);
		
		this.generateBuildingListRecursive(centerPiece);
		System.out.println ("Placing Ocean Ruins @ " + this.centerX + " " +this.centerZ + " - Total buildings " + this.buildingList.size());

	}
	
	public void generateBuildingListRecursive(BuildingOceanRuin piece) {
		// Add current piece
		this.buildingList.add(piece);
		
		// Spawn (possibly) new pieces to each side of current piece
		int spawns = 4; 
		while(spawns -- > 0) {
			
			// Select new piece
			boolean rotated = this.randPieces.nextBoolean();
			BuildingOceanRuin newPiece = null;
			try {
				newPiece = (BuildingOceanRuin) this.getRandomBuildingClass().getConstructor(new Class[] {World.class, Boolean.TYPE}).newInstance(new Object[] {world, rotated});
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
					offsetX = this.randPieces.nextInt(2) + 2;
					offsetZ = this.randPieces.nextInt(8) - 4;

					x = piece.xAbsolute - offsetX - newPiece.getWidth();
					z = piece.zAbsolute + offsetZ;
					break;
					
				case 1:
					// East (x)
					offsetX = this.randPieces.nextInt(2) + 2;
					offsetZ = this.randPieces.nextInt(8) - 4;

					x = piece.xAbsolute + piece.getWidth() + offsetX;
					z = piece.zAbsolute + offsetZ;
					break;
					
				case 2:
					// North (-z)
					offsetX = this.randPieces.nextInt(8) - 4;
					offsetZ = this.randPieces.nextInt(2) + 2;
					
					x = piece.xAbsolute + offsetX;
					z = piece.zAbsolute - newPiece.getLength() - offsetZ;
					break;
					
				case 3:
					// South (z)
					offsetX = this.randPieces.nextInt(8) - 4;
					offsetZ = this.randPieces.nextInt(2) + 2;
					
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
			if(!badChunk && !collided && this.randPieces.nextBoolean ()) {
				this.generateBuildingListRecursive(newPiece);
			}
		}
	}
	
	public boolean badChunkInCorner(World world, int x, int z) {
		// NOTE: this is called with CHUNK coordinates (the caller shifts the AABB corners by >> 4),
		// so world coords are chunkCoord << 4. We query the biome manager directly rather than
		// world.getBiomeGenAt() because the latter loads/generates chunks via getChunkFromChunkCoords;
		// doing so from inside a feature's setup() would recurse (that generated chunk runs
		// getNearestFeatures, which sets up more features, which call this again on other unloaded
		// chunks) until the stack overflows. WorldChunkManager.getBiomeGenAt() is pure noise-on-seed,
		// so it never touches the chunk pipeline.
		return world.getWorldChunkManager().getBiomeGenAt(x << 4, z << 4) instanceof BiomeGenGlacier || !world.isOceanChunk(x, z);
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
		return 3;
	}

	@Override
	public int getSpawnChance() {
		return 5;
	}

	@Override
	public boolean shouldSpawn(IChunkProvider chunkProvider, World world, Random rand, BiomeGenBase biome, int chunkX, int chunkZ) {
		return !(biome instanceof BiomeGenGlacier) && world.isOceanChunk(chunkX, chunkZ);
	}
	
	@Override
	public int minimumSeparation() {
		return 16;
	}
}
