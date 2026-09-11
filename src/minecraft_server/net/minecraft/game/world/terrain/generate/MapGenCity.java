package net.minecraft.game.world.terrain.generate;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenDesert;
import net.minecraft.game.world.biome.BiomeGenDesertOutskirts;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityMobSpawner;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.generate.city.Building;
import net.minecraft.game.world.terrain.generate.city.BuildingBurger;
import net.minecraft.game.world.terrain.generate.city.BuildingGas;
import net.minecraft.game.world.terrain.generate.city.BuildingGraveyard;
import net.minecraft.game.world.terrain.generate.city.BuildingShoppe1;
import net.minecraft.game.world.terrain.generate.city.BuildingWitchMansion;
import net.minecraft.game.world.terrain.generate.city.CityBlockData;
import net.minecraft.game.world.terrain.generate.city.FurniturePieces;
import net.minecraft.game.world.terrain.generate.city.CityBitmaps;
import net.minecraft.game.world.terrain.generate.tree.WorldGenStreetLight;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTreesDead;





public class MapGenCity extends MapGenBase {
	/* 
	 * Generates chunks with buildings or roads or whatever.
	 * Must be called from provideChunk.
	 */
	
	// TODO :: Massive cleanup. Remove dead / test code or something. Put the 2D bitmaps somewhere else?

	public boolean hasBuilding = false;
	public boolean hasRoad = false;
	public int baseHeight;
	public int averageHeight;
	public int roadVariation;
	byte [] heightMap;
	
	private byte floorID;
	private byte floorMeta;
	private byte wallID;
	private byte wallMeta;
	private byte glassID;
	private byte glassMeta;
	private byte columnsID;
	private byte columnsMeta;

	private int floorIDarray[] = new int[] { 
			Block.cobblestone.blockID, 
			Block.stone.blockID, 
			Block.wood.blockID, 
			Block.cobblestoneMossy.blockID,
			Block.encode(Block.stainedTerracotta, 8),
			Block.encode(Block.stainedTerracotta, 7),
		};
	
	private int wallIDarray[] = new int[] { 
			Block.brick.blockID, 
			Block.stone.blockID, 
			Block.sand.blockID, 
			Block.planks.blockID, 
			Block.cement.blockID, 
			Block.blockClay.blockID, 
			Block.stoneBricks.blockID, 
			Block.sandStone.blockID, 
			Block.brick.blockID, 
			Block.stone.blockID, 
			Block.planks.blockID, 
			Block.cement.blockID, 
			Block.stoneBricks.blockID, 
			Block.encode(Block.stainedTerracotta, 0),  
			Block.encode(Block.stainedTerracotta, 1),
			Block.encode(Block.stainedTerracotta, 3),
			Block.encode(Block.stainedTerracotta, 4),
			Block.encode(Block.stainedTerracotta, 8),
			Block.encode(Block.stainedTerracotta, 7),
			Block.encode(Block.stoneBricks, 1)
		};
	
	private int glassIDarray[] = new int[] { 
			Block.glass.blockID, 
			0, 
			Block.fence.blockID, 
			Block.glass.blockID,
			Block.glass.blockID, 
			Block.fence.blockID, 
			Block.glass.blockID,
			Block.encode(Block.glass, 1),
			Block.encode(Block.glass, 2)
		};
	
	private int columnsIDarray[] = new int[] { 
			Block.stairDouble.blockID, 
			Block.wood.blockID, 
			Block.chippedWood.blockID, 
			Block.stairDouble.blockID, 
			Block.encode(Block.stainedTerracotta, 15)  
		};
	
	private int windowType;
	private int bottomWallType;
	private int columnsType;
	private int b2floorType;
	
	public static final int doorWallType = 5;
	public static final int fancyWallTypeFirst = 6;
	
	public static HashMap<ChunkCoordinates,CityChunkDescriptor> cityChunks = new HashMap<ChunkCoordinates,CityChunkDescriptor>();
	public Chunk thisChunk;
	
	private boolean desertChunk = false;
	
	private static final int stairOffset = 2;
	
	public static final int EMPTY = 0;
	public static final int ROAD_TYPE_1 = 1;
	public static final int ROAD_TYPE_2 = 2;
	
	// Buildings
	
	public Building buildingShoppe1 = new BuildingShoppe1();
	public Building buildingGas = new BuildingGas();
	public Building buildingBurger = new BuildingBurger();
	public Building buildingWitchMansion = new BuildingWitchMansion();
	public Building buildingGraveyard = new BuildingGraveyard();
	
	
	
	

	
	public void setChunk(Chunk chunk) {
		this.thisChunk = chunk;
		this.heightMap = chunk.landSurfaceHeightMap;
	}
	
	public void setIsDesertChunk(BiomeGenBase biomeGen) {
		this.desertChunk = (biomeGen instanceof BiomeGenDesert) && !(biomeGen instanceof BiomeGenDesertOutskirts);
	}
	
	public void generate(IChunkProvider chunkProviderGenerate, World world, int xChunk, int zChunk, byte[] data, byte[] meta) {
		int chunkMinHeight = 127;
		int chunkMaxHeight = 0;

		this.thisChunk.chestY = -1;
		this.thisChunk.specialY = -1;
		
		Random rand = new Random();
		rand.setSeed((long)xChunk * 341873128712L + (long)zChunk * 132897987541L);
		
		this.hasBuilding = false;
		this.hasRoad = false;
			
		// First of all, get average height
		int sumOfHeights = 0;
		for(int z = 0; z < 16; z ++) {
			for(int x = 0; x < 16; x ++) {
				int h = this.heightMap[z << 4 | x];
				if(h < chunkMinHeight) chunkMinHeight = h;
				if(h > chunkMaxHeight) chunkMaxHeight = h;
				if(h < 65) return;
				sumOfHeights += h;
			}
		}
		this.averageHeight = sumOfHeights >> 8;
		this.baseHeight = this.averageHeight;

		// Set values
		int encodedIdMeta =	this.floorIDarray[rand.nextInt(this.floorIDarray.length)];
		this.floorID = (byte) Block.decodeID (encodedIdMeta);
		this.floorMeta = (byte) Block.decodeMeta (encodedIdMeta);
				
		encodedIdMeta = this.wallIDarray[rand.nextInt(this.wallIDarray.length)];
		this.wallID = (byte) Block.decodeID (encodedIdMeta);
		this.wallMeta = (byte) Block.decodeMeta (encodedIdMeta);
		
		encodedIdMeta = this.glassIDarray[rand.nextInt(this.glassIDarray.length)];
		this.glassID = (byte) Block.decodeID (encodedIdMeta);
		this.glassMeta = (byte) Block.decodeMeta (encodedIdMeta);
		
		encodedIdMeta = this.columnsIDarray[rand.nextInt(this.columnsIDarray.length)];
		this.columnsID = (byte) Block.decodeID (encodedIdMeta);
		this.columnsMeta = (byte) Block.decodeMeta (encodedIdMeta);

		if(rand.nextInt(8) == 0) {
			this.windowType = MapGenCity.fancyWallTypeFirst + rand.nextInt(CityBitmaps.fancyWindow.length);
		} else {
			this.windowType = rand.nextInt(3);
		}
		
		this.columnsType = rand.nextInt(3);
		this.bottomWallType = rand.nextInt(3) + 2;
		
		// Just moved this here so the average calculation with adjacent chunks is done with the adjusted height
		
		this.baseHeight = 4 + (this.baseHeight & 0xFFFFFFF8);	// Test: multiple of 8 + 4
		
		// This chunk is eligible for city. We'll find any city chunks adjacent to this
		// To carve or raise to average height.
		int heightSum = this.baseHeight;
		int adjacentCityChunks = 1;
		
		for (int x = xChunk - 1 ; x <= xChunk + 1; x ++) {
			for (int z = zChunk - 1; z <= zChunk + 1; z ++) {
				if ((x != xChunk || z != zChunk) && world.chunkExists(x, z)) {
					Chunk chunk = world.getChunkFromChunkCoords(x, z);
					if (chunk.hasBuilding || chunk.hasRoad) {
						adjacentCityChunks ++;
						heightSum += chunk.baseHeight;
					}
				}
			}
		}
		
		if (adjacentCityChunks > 0) {
			this.baseHeight = heightSum / adjacentCityChunks;
		}
		
		this.baseHeight = 4 + (this.baseHeight & 0xFFFFFFF8);	// Test: multiple of 8 + 4

		// Now height is adjusted...
		if(this.baseHeight < 64 || this.baseHeight > 90) return;
		
		// Chunk main biome for cool desert cities
		BiomeGenBase biomeGen = this.thisChunk.getBiomeGenAt(8, 8);
		this.setIsDesertChunk(biomeGen); 
		
		
		// This is the new condition which I hope works better
		if(chunkMinHeight > this.baseHeight - 9 && (this.desertChunk || chunkMaxHeight < this.baseHeight + 10)) {
			
			// Terraform
			this.raiseTerrain(this.baseHeight, data, meta);
			if(!this.desertChunk) {
				this.flattenTerrain(this.baseHeight, data, meta);
			}
			
			// If a CityChunkDescriptor exists for this chunk, build what we are told
			ChunkCoordinates chunkCoordinates = new ChunkCoordinates(xChunk, zChunk);
			CityChunkDescriptor cityChunkDescriptor = cityChunks.get(chunkCoordinates);
			if (cityChunkDescriptor != null) {
				this.buildPiece (cityChunkDescriptor.forceBuild, data, meta, rand);
				cityChunks.remove(chunkCoordinates);
				return;
			}
			
			if (
				((xChunk & 1) == 1 && (zChunk & 1) == 1) || 
				(
					rand.nextInt(6) == 0 && 
					((xChunk & 1) == 1 || (zChunk & 1) == 1)
				)
			) {
				// First of all: a misaligned building should cause adjacent roads to change
				// If chunks with such roads have been already generated, they are altered and chunks marked for re-populating
				// If not, they are stored for the future. Which is PRETTY CLEVER to work around the out of order chunk generation
				
				// (xChunk & 0) is always 0, so z-chunk roads are always marked
				this.markOrProcessChunk(world, xChunk, zChunk - 1, ROAD_TYPE_1, rand);
				this.markOrProcessChunk(world, xChunk, zChunk + 1, ROAD_TYPE_1, rand);
				
				// New contents being generated ahead, so
				rand = new Random();
				rand.setSeed((long)xChunk * 341873128712L + (long)zChunk * 132897987541L);
								
				int y = this.baseHeight;
				Building building = null;
				
				switch (rand.nextInt(20)) {
					case 0:
						// Big fountain
						this.generateBigFountain(0, y, 0, data, meta, rand);
						break;
					case 1:
						// Urban garden
						this.generateUrbanGarden(0, y, 0, data, meta, rand);
						break;
					case 2:
						// Burger
						building = this.buildingBurger;
						break;
					case 3:
						// Shops
						building = this.buildingShoppe1;
						break;
					case 4:
						// Gas station
						building = this.buildingGas;
						break;
					case 5:
						// Witches palace
						if(rand.nextInt(4) == 0) {
							building = this.buildingWitchMansion;
						}
						// Wonky code! Like the C code I write for 8 bit machines!
						// Note how the "break" is placed inside the IF so it generates the next kind of building if it fails!
					case 6:
						// Graveyard
						if(building == null && rand.nextInt(3) == 0) {
							building = this.buildingGraveyard;
						}
						// Wonky code! Like the C code I write for 8 bit machines!
						// Note how the "break" is placed inside the IF so it generates the next kind of building if it fails!
					default:
						// procedural building
						if (building == null)  {
							switch (rand.nextInt(2)) {
								case 0:
									this.generateSimpleBuilding1(0, y, 0, data, meta, rand);
									break;
								case 1:
									this.generateSimpleBuilding2(0, y, 0, data, meta, rand);
									break;
								// TODO: Add more procedural buildings
									
								// TODO: .schematic pieces based buildings.
							}
						}
				}
				
				if(building != null) {
					building.generate(y, this.thisChunk);
					this.thisChunk.building = building;
					this.thisChunk.buildingY0 = y;
				}
				
				this.hasBuilding = true;
			} else {
				int variation;
				
				if ((xChunk & 1) == 0 && (zChunk & 1) == 0) {
					variation = 0;
				} else if ((zChunk & 1) == 0) {
					variation = 1;
				} else {
					variation = 2;
				}
				
				this.generateStreetFloorSimple(0, this.baseHeight, 0, data, meta, rand, variation);
				
				this.hasRoad = true;
				this.roadVariation = variation;
			}
			
		}		
	}
	
	public void raiseTerrain(int y0, byte[] data, byte[] meta) {
		//byte b = (byte)Block.cobblestone.blockID;
		byte w = (byte)Block.stone.blockID;
		
		for (int y = y0 - 9; y < y0; y ++) {
			// This was hollow, but I've decided against
			/*
			for (int i = 0; i < 16; i ++) {
				data [i << 11 | y] = b;
				data [i << 7 | y] = b;
				data [15 << 11 | i << 7 | y] = b;
				data [i << 11 | 15 << 7 | y ] = b;
			}
			*/
			int idx = y;
			for(int i = 0; i < 256; i ++) {
				if(data[idx] != w) data[idx] = w;
				idx += 128;
			}
		}
		
	}
	
	public void flattenTerrain(int y0, byte[] data, byte meta[]) {
		for (int y = y0 + 1; y < y0 + 10; y ++) {
			int idx = y;
			for(int i = 0; i < 256; i ++) {
				data[idx] = 0; idx += 128;
			}
		}
	}
	
	public void generateSimpleBuilding1(int x0, int y0, int z0, byte[] data, byte[] meta, Random rand) {
		// Get max number of floors & random number of floors
		int maxFloors = (120 - y0 - 5) / 5;					
		int floors = 2 + rand.nextInt(maxFloors - 2);

		int offsetX = x0; // rand.nextInt(2);
		int offsetZ = z0; // rand.nextInt(2);
		
		int y = y0;
		
		boolean orientation = rand.nextBoolean();
		
		// Base floor
		this.drawBaseFloor(offsetX, y, offsetZ, data, meta, rand);
		y += 5;
		
		int yFloors = y;
		
		// Common floors
		for (int i = 0; i < floors; i ++) {
			this.drawFloorType1(offsetX, y, offsetZ, data, meta, rand, orientation);
			y += 5;
		}
		
		// Roof
		this.drawRoofType1(offsetX, y, offsetZ, data, meta, rand, orientation);
		
		// Furniture
		y = yFloors;
		for(int i = 0; i < floors; i ++) {
			int numPieces = rand.nextInt(3);
			for(int j = 0; j < numPieces; j ++) this.drawFurniturePiece(rand.nextInt(FurniturePieces.NUM_FURNITURE_PIECES), x0 + 2, y + 1, z0 + 2, data, meta, rand);
			y += 5;
		}
	}
	
	public void generateSimpleBuilding2(int x0, int y0, int z0, byte[]data, byte[] meta, Random rand) {
		int maxFloors = (120 - y0 - 5) / 4;
		int floors = 2 + rand.nextInt(maxFloors - 2);

		int offsetX = x0 + 1; // rand.nextInt(2);
		int offsetZ = z0 + 1; // rand.nextInt(2);
		
		int y = y0;
		
		this.b2floorType = rand.nextInt(2);
		
		// Base floor
		this.drawBaseFloor(offsetX - 1, y, offsetZ - 1, data, meta, rand);
		int x = offsetX + 2; int z = offsetZ + 2;
		data[(x + 1) << 11 | z << 7 | (y + 1)] = (byte)Block.stairSingle.blockID;
		data[(x + 1) << 11 | (z + 1) << 7 | (y + 1)] = this.wallID;
		meta[(x + 1) << 11 | (z + 1) << 7 | (y + 1)] = this.wallMeta;
		data[(x + 2) << 11 | z << 7 | (y + 1)] = (byte)Block.stairDouble.blockID;
		y += 5;	
				
		int yFloors = y;
		
		// Common floors
		for (int i = 0; i < floors; i ++) {
			this.drawFloorType2(offsetX, y, offsetZ, data, meta, rand);
			y += 4;
		}
		
		// Roof
		this.drawRoofType2(offsetX, y, offsetZ, data, meta, rand);
		
		// Furniture
		y = yFloors;
		for(int i = 0; i < floors; i ++) {
			int numPieces = rand.nextInt(3);
			for(int j = 0; j < numPieces; j ++) this.drawFurniturePiece(rand.nextInt(FurniturePieces.NUM_FURNITURE_PIECES), x0 + 2, y + 1, z0 + 2, data, meta, rand);
			y += 4;
		}
	}
	
	public void drawBaseFloor(int x0, int y0, int z0, byte[] data, byte[] meta, Random rand) {
		int index; 
		
		// Floor
		for (int x = 0; x < 16; x ++)
			for (int z = 0; z < 16; z ++) {
				byte b, m = 0;
				if (x >= 1 && x <= 14 && z >= 1 && z <= 14) {
					b = this.floorID;
					m = this.floorMeta;
				} else {
					b = (byte)Block.cobblestone.blockID;
				}
				index = (x0 + x) << 11 | (z0 + z) << 7 | y0;
				data[index] = b;
				meta[index] = m;
			}
		
		// Columns
		int x = x0 + 1;
		int z;
		for (int i = 0; i < 4; i ++) {
			z = z0 + 1;
			for (int j = 0; j < 4; j ++) {
				if (i == 0 || j == 0 || i == 3 || j == 3) {
					for (int y = y0 + 1; y <= y0 + 5; y ++) {
						data[x << 11 | z << 7 | y] = this.columnsID; 
						meta[x << 11 | z << 7 | y] = this.columnsMeta; 
						
						if (i == 0) {
							data[(x - 1) << 11 | z << 7 | y] = this.columnsID;
							meta[(x - 1) << 11 | z << 7 | y] = this.columnsMeta;
						}
						if (i == 3) {
							data[(x + 1) << 11 | z << 7 | y] = this.columnsID;
							meta[(x + 1) << 11 | z << 7 | y] = this.columnsMeta;
							
						}
						if (j == 0) {
							data[x << 11 | (z - 1) << 7 | y] = this.columnsID;
							meta[x << 11 | (z - 1) << 7 | y] = this.columnsMeta;
						}
						if (j == 3) {
							data[x << 11 | (z + 1) << 7 | y] = this.columnsID;
							meta[x << 11 | (z + 1) << 7 | y] = this.columnsMeta;
						}

						if (i == 0 && j == 0) {
							data[(x - 1) << 11 | (z - 1) << 7 | y] = this.columnsID;
							meta[(x - 1) << 11 | (z - 1) << 7 | y] = this.columnsMeta;
						}
						if (i == 3 && j == 0) {
							data[(x + 1) << 11 | (z - 1) << 7 | y] = this.columnsID;
							meta[(x + 1) << 11 | (z - 1) << 7 | y] = this.columnsMeta;
						}
						if (i == 0 && j == 3) {
							data[(x - 1) << 11 | (z + 1) << 7 | y] = this.columnsID;
							meta[(x - 1) << 11 | (z + 1) << 7 | y] = this.columnsMeta;
						}
						if (i == 3 && j == 3) {
							data[(x + 1) << 11 | (z + 1) << 7 | y] = this.columnsID;
							meta[(x + 1) << 11 | (z + 1) << 7 | y] = this.columnsMeta;
						}
					}
					
				}
				z += 4;
			}
			x += 4;
		}
		
		// Walls
		int whichIterationForDoor = rand.nextInt(3);
		for (int i = 0; i < 3; i ++) {
			int selectedWall = rand.nextInt(4);
			this.drawWallType1(x0 + 2 + i * 4, y0 + 1, z0 + 1, data, meta, true, selectedWall == 0 && whichIterationForDoor == i ? MapGenCity.doorWallType : this.bottomWallType);
			this.drawWallType1(x0 + 2 + i * 4, y0 + 1, z0 + 13, data, meta, true, selectedWall == 1 && whichIterationForDoor == i ? MapGenCity.doorWallType : this.bottomWallType);
			
			this.drawWallType1(x0 + 1, y0 + 1, z0 + 2 + i * 4, data, meta, false, selectedWall == 2 && whichIterationForDoor == i ? MapGenCity.doorWallType : this.bottomWallType);
			this.drawWallType1(x0 + 13, y0 + 1, z0 + 2 + i * 4, data, meta, false, selectedWall == 3 && whichIterationForDoor == i ? MapGenCity.doorWallType : this.bottomWallType);
		}
		
		// Hollow
		for(x = 2; x < 13; x ++) {
			for(z = 2; z < 13; z ++) {
				int idx = (x << 11) | (z << 7) | (y0 + 1);
				for(int y = 1; y <= 5; y ++) {
					data[idx ++] = 0;
				}
			}
		}
	}
	
	/*
	 * Carves a hole and builds a 4-step staircase down through the floor
	 * (or roof). stairsOrientation=true means stairs run along z, false means
	 * along x. Shared by drawFloorType1 and drawRoofType1.
	 */
	private void drawRoofStairs(int x0, int y0, int z0, byte[] data, boolean stairsOrientation) {
		if (stairsOrientation) {
			for (int z = 3; z < 10; z++) {
				data[(x0 + stairOffset) << 11 | (z0 + z) << 7 | y0] = 0;
			}
			int x = x0 + stairOffset, y = y0, z = z0 + 3;
			for (int i = 0; i < 4; i++) {
				data[x << 11 | z << 7 | y] = (byte) Block.stairSingle.blockID;
				data[x << 11 | z << 7 | (y - 1)] = (byte) Block.cobblestone.blockID;
				data[x << 11 | (z + 1) << 7 | (y - 1)] = (byte) Block.cobblestone.blockID;
				z += 2;
				y--;
			}
			data[x << 11 | z << 7 | y] = (byte) Block.stairSingle.blockID;
		} else {
			for (int x = 3; x < 10; x++) {
				data[(x0 + x) << 11 | (z0 + stairOffset) << 7 | y0] = 0;
			}
			int x = x0 + 3, y = y0, z = z0 + stairOffset;
			for (int i = 0; i < 4; i++) {
				data[x << 11 | z << 7 | y] = (byte) Block.stairSingle.blockID;
				data[x << 11 | z << 7 | (y - 1)] = (byte) Block.cobblestone.blockID;
				data[(x + 1) << 11 | z << 7 | (y - 1)] = (byte) Block.cobblestone.blockID;
				x += 2;
				y--;
			}
			data[x << 11 | z << 7 | y] = (byte) Block.stairSingle.blockID;
		}
	}

	public void drawFloorType1(int x0, int y0, int z0, byte[] data, byte[] meta, Random rand, boolean stairsOrientation) {
		// Floor
		for (int x = 1; x < 14; x ++)
			for (int z = 1; z < 14; z ++) {
				data[(x0 + x) << 11 | (z0 + z) << 7 | y0] = this.floorID;
				meta[(x0 + x) << 11 | (z0 + z) << 7 | y0] = this.floorMeta;
			}

		drawRoofStairs(x0, y0, z0, data, stairsOrientation);
		
		// Columns
		int x = x0 + 1;
		for (int i = 0; i < 4; i ++) {
			int z = z0 + 1;
			for (int j = 0; j < 4; j ++) {
				if (i == 0 || j == 0 || i == 3 || j == 3) {
					for (int y = y0; y <= y0 + 5; y ++) {
						data[x << 11 | z << 7 | y] = this.columnsID;
						meta[x << 11 | z << 7 | y] = this.columnsMeta;
						
						if (this.columnsType > 0) {
							if (i == 0) {
								data[(x - 1) << 11 | z << 7 | y] = this.columnsID;
								meta[(x - 1) << 11 | z << 7 | y] = this.columnsMeta;
							}
							if (i == 3) {
								data[(x + 1) << 11 | z << 7 | y] = this.columnsID;
								meta[(x + 1) << 11 | z << 7 | y] = this.columnsMeta;
							}
							if (j == 0) {
								data[x << 11 | (z - 1) << 7 | y] = this.columnsID;
								meta[x << 11 | (z - 1) << 7 | y] = this.columnsMeta;
							}
							if (j == 3) {
								data[x << 11 | (z + 1) << 7 | y] = this.columnsID;
								meta[x << 11 | (z + 1) << 7 | y] = this.columnsMeta;
							}
						}
						if (this.columnsType == 2) {
							if (i == 0 && j == 0) {
								data[(x - 1) << 11 | (z - 1) << 7 | y] = this.columnsID;
								meta[(x - 1) << 11 | (z - 1) << 7 | y] = this.columnsMeta;
							}
							if (i == 3 && j == 0) {
								data[(x + 1) << 11 | (z - 1) << 7 | y] = this.columnsID;
								meta[(x + 1) << 11 | (z - 1) << 7 | y] = this.columnsMeta;
							}
							if (i == 0 && j == 3) {
								data[(x - 1) << 11 | (z + 1) << 7 | y] = this.columnsID;
								meta[(x - 1) << 11 | (z + 1) << 7 | y] = this.columnsMeta;
							}
							if (i == 3 && j == 3) {
								data[(x + 1) << 11 | (z + 1) << 7 | y] = this.columnsID;
								meta[(x + 1) << 11 | (z + 1) << 7 | y] = this.columnsMeta;
							}
						}
					}
					
				}
				z += 4;
			}
			x += 4;
		}
		
		// Walls
		for (int i = 0; i < 3; i ++) {
			this.drawWallType1(x0 + 2 + i * 4, y0 + 1, z0 + 1, data, meta, true, this.windowType);
			this.drawWallType1(x0 + 2 + i * 4, y0 + 1, z0 + 13, data, meta, true, this.windowType);
			
			this.drawWallType1(x0 + 1, y0 + 1, z0 + 2 + i * 4, data, meta, false, this.windowType);
			this.drawWallType1(x0 + 13, y0 + 1, z0 + 2 + i * 4, data, meta, false, this.windowType);
		}
	}
	
	public void drawFloorType2(int x0, int y0, int z0, byte [] data, byte[] meta, Random rand) {
		// Corners, pillars & windows
		for (int y = y0; y < y0 + 5; y ++) {
			
			// Corners
			data[x0 << 11 | z0 << 7 | y] = this.columnsID;
			data[(x0 + 1) << 11 | z0 << 7 | y] = this.columnsID;
			data[x0 << 11 | (z0 + 1) << 7 | y] = this.columnsID;
			data[(x0 + 1) << 11 | (z0 + 1) << 7 | y] = this.columnsID;
			
			data[(x0 + 11) << 11 | z0 << 7 | y] = this.columnsID;
			data[(x0 + 12) << 11 | z0 << 7 | y] = this.columnsID;
			data[(x0 + 11) << 11 | (z0 + 1) << 7 | y] = this.columnsID;
			data[(x0 + 12) << 11 | (z0 + 1) << 7 | y] = this.columnsID;
			
			data[x0 << 11 | (z0 + 11) << 7 | y] = this.columnsID;
			data[(x0 + 1) << 11 | (z0 + 11) << 7 | y] = this.columnsID;
			data[x0 << 11 | (z0 + 12) << 7 | y] = this.columnsID;
			data[(x0 + 1) << 11 | (z0 + 12) << 7 | y] = this.columnsID;
			
			data[(x0 + 11) << 11 | (z0 + 11) << 7 | y] = this.columnsID;
			data[(x0 + 12) << 11 | (z0 + 11) << 7 | y] = this.columnsID;
			data[(x0 + 11) << 11 | (z0 + 12) << 7 | y] = this.columnsID;
			data[(x0 + 12) << 11 | (z0 + 12) << 7 | y] = this.columnsID;
			
			meta[x0 << 11 | z0 << 7 | y] = this.columnsMeta;
			meta[(x0 + 1) << 11 | z0 << 7 | y] = this.columnsMeta;
			meta[x0 << 11 | (z0 + 1) << 7 | y] = this.columnsMeta;
			meta[(x0 + 1) << 11 | (z0 + 1) << 7 | y] = this.columnsMeta;
			
			meta[(x0 + 11) << 11 | z0 << 7 | y] = this.columnsMeta;
			meta[(x0 + 12) << 11 | z0 << 7 | y] = this.columnsMeta;
			meta[(x0 + 11) << 11 | (z0 + 1) << 7 | y] = this.columnsMeta;
			meta[(x0 + 12) << 11 | (z0 + 1) << 7 | y] = this.columnsMeta;
			
			meta[x0 << 11 | (z0 + 11) << 7 | y] = this.columnsMeta;
			meta[(x0 + 1) << 11 | (z0 + 11) << 7 | y] = this.columnsMeta;
			meta[x0 << 11 | (z0 + 12) << 7 | y] = this.columnsMeta;
			meta[(x0 + 1) << 11 | (z0 + 12) << 7 | y] = this.columnsMeta;
			
			meta[(x0 + 11) << 11 | (z0 + 11) << 7 | y] = this.columnsMeta;
			meta[(x0 + 12) << 11 | (z0 + 11) << 7 | y] = this.columnsMeta;
			meta[(x0 + 11) << 11 | (z0 + 12) << 7 | y] = this.columnsMeta;
			meta[(x0 + 12) << 11 | (z0 + 12) << 7 | y] = this.columnsMeta;
			
			// Pillars
			for (int i = 0; i < 7; i += 2) {
				data[(x0 + 3 + i) << 11 | z0 << 7 | y] = this.columnsID;
				data[(x0 + 3 + i) << 11 | (z0 + 1) << 7 | y] = this.columnsID;
				
				data[(x0 + 3 + i) << 11 | (z0 + 11) << 7 | y] = this.columnsID;
				data[(x0 + 3 + i) << 11 | (z0 + 12) << 7 | y] = this.columnsID;
				
				data[x0 << 11 | (z0 + 3 + i) << 7 | y] = this.columnsID;
				data[(x0 + 1) << 11 | (z0 + 3 + i) << 7 | y] = this.columnsID;
				data[(x0 + 11) << 11 | (z0 + 3 + i) << 7 | y] = this.columnsID;
				data[(x0 + 12) << 11 | (z0 + 3 + i) << 7 | y] = this.columnsID;
				
				meta[(x0 + 3 + i) << 11 | z0 << 7 | y] = this.columnsMeta;
				meta[(x0 + 3 + i) << 11 | (z0 + 1) << 7 | y] = this.columnsMeta;
				
				meta[(x0 + 3 + i) << 11 | (z0 + 11) << 7 | y] = this.columnsMeta;
				meta[(x0 + 3 + i) << 11 | (z0 + 12) << 7 | y] = this.columnsMeta;
				
				meta[x0 << 11 | (z0 + 3 + i) << 7 | y] = this.columnsMeta;
				meta[(x0 + 1) << 11 | (z0 + 3 + i) << 7 | y] = this.columnsMeta;
				meta[(x0 + 11) << 11 | (z0 + 3 + i) << 7 | y] = this.columnsMeta;
				meta[(x0 + 12) << 11 | (z0 + 3 + i) << 7 | y] = this.columnsMeta;
			}
			
			// Windows
			for (int i = 0; i < 9; i += 2) {
				data[(x0 + 2 + i) << 11 | (z0 + 1) << 7 | y] = this.glassID;
				data[(x0 + 2 + i) << 11 | (z0 + 11) << 7 | y] = this.glassID;
				
				data[(x0 + 1) << 11 | (z0 + 2 + i) << 7 | y] = this.glassID;
				data[(x0 + 11) << 11 | (z0 + 2 + i) << 7 | y] = this.glassID;
			}
		}
				
		// Floor
		int extend = this.b2floorType << 1;
		for (int x = x0 + 2 - extend; x < x0 + 11 + extend; x ++) {
			for (int z = z0 + 2 - extend; z < z0 + 11 + extend; z ++) {
				data[x << 11 | z << 7 | y0] = this.floorID;
			}
		}
		
		this.drawStairsType2(x0, y0, z0, data, meta);	
	}
	
	public void drawFurniturePiece(int n, int x0, int y0, int z0, byte[] data, byte[] meta, Random rand) {
		// get dimensions
		byte size[] = new byte[2];
		FurniturePieces.getFeatureSize(n, size);
		
		int dimX = size[0]; 
		int dimZ = size[1];
		
		x0 += rand.nextInt(11 - dimX);
		z0 += rand.nextInt(11 - dimZ);
		
		// check if clear
		for(int x = 0; x < dimX; x ++) {
			for(int z = 0; z < dimZ; z ++) {
				int idx = (x0 + x) << 11 | (z0 + z) << 7 | y0;
				if(data[idx] != 0 || data [idx + 1] != 0) return;
			}
		}

		// Draw selected piece
		FurniturePieces.drawFeature(n, x0, y0, z0, this.thisChunk);
	}
	
	public void drawStairsType2(int x0, int y0, int z0, byte[] data, byte[] meta) {
		// Stairs down (in a corner)
		// Layer 1 with hole
		int x = x0 + 2, y = y0, z = z0 + 2;
		data[x << 11 | z << 7 | y] = 0;
		data[(x + 1) << 11 | z << 7 | y] = (byte)Block.stairSingle.blockID;
		data[(x + 2) << 11 | z << 7 | y] = (byte)Block.stairDouble.blockID;
		data[x << 11 | (z + 1) << 7 | y] = 0;
		data[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallID;
		meta[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallMeta;
		data[(x + 2) << 11 | (z + 1) << 7 | y] = 0;
		data[x << 11 | (z + 2) << 7 | y] = 0;
		data[(x + 1) << 11 | (z + 2) << 7 | y] = 0;
		data[(x + 2) << 11 | (z + 2) << 7 | y] = 0;
		
		// Layer 2
		y --;
		data[x << 11 | z << 7 | y] = (byte)Block.stairDouble.blockID;
		data[x << 11 | (z + 1) << 7 | y] = (byte)Block.stairSingle.blockID;
		data[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallID;
		meta[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallMeta;
		
		// Layer 3
		y --;
		data[x << 11 | (z + 2) << 7 | y] = (byte)Block.stairDouble.blockID;
		data[(x + 1) << 11 | (z + 2) << 7 | y] = (byte)Block.stairSingle.blockID;
		data[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallID;
		meta[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallMeta;
		
		// Layer 4
		y --;
		data[(x + 2) << 11 | (z + 1) << 7 | y] = (byte)Block.stairSingle.blockID;
		data[(x + 2) << 11 | (z + 2) << 7 | y] = (byte)Block.stairDouble.blockID;
		data[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallID;
		meta[(x + 1) << 11 | (z + 1) << 7 | y] = this.wallMeta;
	}
	
	public void drawRoofType1(int x0, int y0, int z0, byte[] data, byte[] meta, Random rand, boolean stairsOrientation) {
		// Floor
		for (int x = 1; x < 14; x ++)
			for (int z = 1; z < 14; z ++) {
				data[(x0 + x) << 11 | (z0 + z) << 7 | y0] = this.floorID;
				meta[(x0 + x) << 11 | (z0 + z) << 7 | y0] = this.floorMeta;
			}
		
		// Possible chest
		if(rand.nextInt(8) == 0) {
			this.thisChunk.chestY = y0 + 1;
			this.thisChunk.chestX = 4 + rand.nextInt(8);
			this.thisChunk.chestZ = 4 + rand.nextInt(8);
		}
		
		// Possible spawner
		if(rand.nextInt(4) == 0) {
			this.thisChunk.specialY = y0 - 2;
			this.thisChunk.specialX = 4 + rand.nextInt(8);
			this.thisChunk.specialZ = 4 + rand.nextInt(8);
			data[(thisChunk.specialX) << 11 | (thisChunk.specialZ) << 7 | (y0 - 3)] = (byte)Block.cobblestoneMossy.blockID;
			data[(thisChunk.specialX) << 11 | (thisChunk.specialZ) << 7 | (y0 - 4)] = (byte)Block.cobblestoneMossy.blockID;

		}
		
		drawRoofStairs(x0, y0, z0, data, stairsOrientation);
		
		// Railing
		int y = y0 + 1;
		for (int i = 1; i < 14; i ++) {
			data[(x0 + i) << 11 | (z0 + 1) << 7 | y] = (byte) this.columnsID;
			data[(x0 + i) << 11 | (z0 + 13) << 7 | y] = (byte) this.columnsID;
			data[(x0 + 1) << 11 | (z0 + i) << 7 | y] = (byte) this.columnsID;
			data[(x0 + 13) << 11 | (z0 + i) << 7 | y] = (byte) this.columnsID;
			
			meta[(x0 + i) << 11 | (z0 + 1) << 7 | y] = (byte) this.columnsMeta;
			meta[(x0 + i) << 11 | (z0 + 13) << 7 | y] = (byte) this.columnsMeta;
			meta[(x0 + 1) << 11 | (z0 + i) << 7 | y] = (byte) this.columnsMeta;
			meta[(x0 + 13) << 11 | (z0 + i) << 7 | y] = (byte) this.columnsMeta;
		}
		
		// Dirt?
		if (rand.nextInt(6) == 0) {
			for (int x = 2; x < 13; x ++)
				for (int z = 2; z < 13; z ++) {
					data[(x0 + x) << 11 | (z0 + z) << 7 | y] = (byte)Block.grass.blockID;
				}		
		}
	}
	
	public void drawRoofType2(int x0, int y0, int z0, byte[] data, byte[] meta, Random rand) {
		for (int x = x0; x < x0 + 13; x ++) {
			for (int z = z0; z < z0 + 13; z ++) {
				if (x != x0 && z != z0 && x != x0 + 12 && z != z0 + 12) {
					data[x << 11 | z << 7 | y0] = this.floorID;
					meta[x << 11 | z << 7 | y0] = this.floorMeta;
				} else {
					data[x << 11 | z << 7 | y0] = this.wallID;
					meta[x << 11 | z << 7 | y0] = this.wallMeta;
					data[x << 11 | z << 7 | (y0 + 1)] = this.wallID;
					meta[x << 11 | z << 7 | (y0 + 1)] = this.wallMeta;
				}
			}
		}
		
		// Possible chest
		if(rand.nextInt(8) == 0) {
			this.thisChunk.chestY = y0 + 1;
			this.thisChunk.chestX = 4 + rand.nextInt(8);
			this.thisChunk.chestZ = 4 + rand.nextInt(8);
		}
		
		// Possible spawner
		if(rand.nextInt(4) == 0) {
			this.thisChunk.specialY = y0 - 2;
			this.thisChunk.specialX = 4 + rand.nextInt(8);
			this.thisChunk.specialZ = 4 + rand.nextInt(8);
		}

		this.drawStairsType2(x0, y0, z0, data, meta);
	}
	
	public void drawWallType1(int x0, int y0, int z0, byte[] data, byte meta[], boolean orientation, int windowType) {
		if (orientation) {
			drawWallAxis(x0, y0, z0, data, meta, windowType, true);
		} else {
			drawWallAxis(x0, y0, z0, data, meta, windowType, false);
		}
	}

	/*
	 * Draws a 3x4 wall segment. If alongX is true, the wall extends along
	 * the x axis (looping x, fixed z); otherwise it extends along z.
	 */
	private void drawWallAxis(int x0, int y0, int z0, byte[] data, byte[] meta, int windowType, boolean alongX) {
		int fixedCoord = alongX ? z0 : x0;

		if (windowType >= MapGenCity.fancyWallTypeFirst) {
			int fancyType = windowType - MapGenCity.fancyWallTypeFirst;
			for (int moving = 0; moving < 3; moving++) {
				for (int y = y0; y < y0 + 4; y++) {
					int coord = alongX ? (x0 + moving) : (z0 + moving);
					int encoded = CityBitmaps.fancyWindow[fancyType][y - y0][moving];
					if (encoded == -1) {
						CityBlockData.setBlock(data, meta, alongX ? coord : fixedCoord, y, alongX ? fixedCoord : coord, this.wallID, this.wallMeta);
					} else if (encoded == -2) {
						CityBlockData.setBlock(data, meta, alongX ? coord : fixedCoord, y, alongX ? fixedCoord : coord, this.glassID, this.glassMeta);
					} else {
						if (alongX) {
							CityBlockData.setEncoded(data, meta, coord, y, fixedCoord, encoded);
						} else {
							CityBlockData.setEncoded(data, meta, fixedCoord, y, coord, encoded);
						}
					}
				}
			}
		} else if (windowType == MapGenCity.doorWallType) {
			for (int moving = 0; moving < 3; moving++) {
				for (int y = y0; y < y0 + 4; y++) {
					int coord = alongX ? (x0 + moving) : (z0 + moving);
					if (y > y0 + 1 || moving != 1) {
						if (alongX) {
							CityBlockData.setBlock(data, coord, y, fixedCoord, (byte) Block.stone.blockID);
						} else {
							CityBlockData.setBlock(data, fixedCoord, y, coord, (byte) Block.stone.blockID);
						}
					}
				}
			}
		} else {
			for (int moving = 0; moving < 3; moving++) {
				int coord = alongX ? (x0 + moving) : (z0 + moving);
				int y = y0;
				boolean isCenter = alongX ? (coord == x0 + 1) : (coord == z0 + 1);

				if (alongX) {
					CityBlockData.setBlock(data, meta, coord, y, fixedCoord, this.wallID, this.wallMeta);
				} else {
					CityBlockData.setBlock(data, meta, fixedCoord, y, coord, this.wallID, this.wallMeta);
				}

				if (windowType < 2) {
					if (alongX) {
						CityBlockData.setBlock(data, meta, coord, y + 3, fixedCoord, this.wallID, this.wallMeta);
					} else {
						CityBlockData.setBlock(data, meta, fixedCoord, y + 3, coord, this.wallID, this.wallMeta);
					}
				}

				if (windowType != 1 && windowType < 4) {
					int yyStart = y + (windowType == 3 ? 0 : 1);
					int yyEnd = y + (windowType == 0 ? 2 : 3);
					for (int yy = yyStart; yy <= yyEnd; yy++) {
						if (alongX) {
							CityBlockData.setBlock(data, meta, coord, yy, fixedCoord, this.glassID, this.glassMeta);
						} else {
							CityBlockData.setBlock(data, meta, fixedCoord, yy, coord, this.glassID, this.glassMeta);
						}
					}
				} else {
					byte b = isCenter ? this.glassID : this.wallID;
					byte m = isCenter ? this.glassMeta : this.wallMeta;
					if (alongX) {
						CityBlockData.setBlock(data, meta, coord, y + 1, fixedCoord, b, m);
						CityBlockData.setBlock(data, meta, coord, y + 2, fixedCoord, b, m);
						CityBlockData.setBlock(data, meta, coord, y + 3, fixedCoord, this.wallID, this.wallMeta);
					} else {
						CityBlockData.setBlock(data, meta, fixedCoord, y + 1, coord, b, m);
						CityBlockData.setBlock(data, meta, fixedCoord, y + 2, coord, b, m);
						CityBlockData.setBlock(data, meta, fixedCoord, y + 3, coord, this.wallID, this.wallMeta);
					}
				}
			}
		}
	}
	
	public void fillWholeLayer(int y0, byte[] data, byte[] meta, byte blockID) {
		for (int x = 0; x < 16; x ++)
			for (int z = 0; z < 16; z ++) 
				data[x << 11 | z << 7 | y0] = blockID;
	}
	
	public void drawBitmap(int y0, byte[] data, byte[] meta, byte[] bitmap) {
		int idx = 0; byte b; 
		for (int x = 0; x < 16; x ++)
			for (int z = 0; z < 16; z ++) {
				byte bb = bitmap[idx];
				if (bb >= 0) {
					b = (byte)CityBitmaps.blockIDMappings[bb][0]; 
					data[x << 11 | z << 7 | y0] = b;
					b = (byte)CityBitmaps.blockIDMappings[bb][1];
					meta[x << 11 | z << 7 | y0] = b;
				}
				idx ++;
			}
		
		// Fix slabs
		for (int x = 0; x < 16; x ++)
			for (int z = 0; z < 16; z ++) {
				idx = x << 11 | z << 7 | y0;
				if(data[idx] == Block.stairSingle.blockID && data[idx + 1] != 0) {
					data[idx] = (byte)Block.dirt.blockID;
				}
			}
	}
	
	public void drawBitmapRoadHollow(int y0, byte[] data, byte[] meta, byte[] bitmap) {
		int idx = 0;
		for (int x = 0; x < 16; x ++)
			for (int z = 0; z < 16; z ++) {
				byte b = bitmap[idx ++];
				if(b != 2 && b != 3 && b != 0) {
					data[x << 11 | z << 7 | y0] = (byte)CityBitmaps.blockIDMappings[b][0];
					meta[x << 11 | z << 7 | y0] = (byte)CityBitmaps.blockIDMappings[b][1];
				}
				else if(!this.desertChunk) data[x << 11 | z << 7 | y0] = 0;
			}	
	}
	
	public void generateStreetFloorSimple(int x0, int y0, int z0, byte[] data, byte[] meta, Random rand, int variation) {
		for (int i = 0; i < 2; i ++)
			this.drawBitmap(y0 - 1 + i, data, meta, CityBitmaps.streetBlockBitmap[variation][i]);
	}
	
	public void generateBigFountain(int x0, int y0, int z0, byte []data, byte[] meta, Random rand) {
		for (int i = 0; i < 3; i ++) 
			this.drawBitmap(y0 + i, data, meta, CityBitmaps.fountainBigBlockBitmap[i]);
		
		int y = y0 + 3;
		data[7 << 11 | 7 << 7 | y] = (byte)Block.cobblestone.blockID;
		data[8 << 11 | 7 << 7 | y] = (byte)Block.cobblestone.blockID;
		data[7 << 11 | 8 << 7 | y] = (byte)Block.cobblestoneMossy.blockID;
		data[8 << 11 | 8 << 7 | y] = (byte)Block.cobblestone.blockID;
		y++;
		data[7 << 11 | 7 << 7 | y] = (byte)Block.fence.blockID;
		data[8 << 11 | 7 << 7 | y] = (byte)Block.fence.blockID;
		data[7 << 11 | 8 << 7 | y] = (byte)Block.fence.blockID;
		data[8 << 11 | 8 << 7 | y] = (byte)Block.fence.blockID;
		y++;
		data[7 << 11 | 7 << 7 | y] = (byte)Block.stairSingle.blockID;
		data[8 << 11 | 7 << 7 | y] = (byte)Block.stairSingle.blockID;
		data[7 << 11 | 8 << 7 | y] = (byte)Block.stairSingle.blockID;
		data[8 << 11 | 8 << 7 | y] = (byte)Block.stairSingle.blockID;
	}
	
	public void generateUrbanGarden(int x0, int y0, int z0, byte[] data, byte[] meta, Random rand) {
		for (int i = 0; i < 3; i ++) 
			this.drawBitmap(y0 + i, data, meta, CityBitmaps.urbanGardenBlockBitmap[i]);		
	}
	
	public void markOrProcessChunk(World world, int xChunk, int zChunk, int cityPiece, Random rand) {
		if (world.chunkExists(xChunk, zChunk)) {
			// Modify if it contains a cross road
			Chunk chunk = world.getChunkFromChunkCoords(xChunk, zChunk);
			if(chunk.hasRoad && chunk.roadVariation == 0) {
				// This chunk is already in the world, so it was sliced into subchunks when it
				// was generated and its flat generation buffers have been dropped. Stage the
				// terrain edit into a local flat 128-high pair and apply it back through the
				// subchunk storage (raiseTerrain / flattenTerrain / generateStreetFloorSimple are
				// flat-buffer based, and the historical behaviour wrote straight into the chunk).
				byte[] data = chunk.exportFlatBlocks128();
				byte[] meta = chunk.exportFlatData128();
				this.raiseTerrain(chunk.baseHeight, data, meta);
				if(!this.desertChunk) {
					this.flattenTerrain(chunk.baseHeight, data, meta);
				}
				this.generateStreetFloorSimple(0, chunk.baseHeight, 0, data, meta, rand, cityPiece);
				chunk.importFlatBlocks128(data, meta);
				chunk.roadVariation = cityPiece;
				chunk.isTerrainPopulated = false;
			}
		} else {
			// Mark for the future
			CityChunkDescriptor cityChunkDescriptor = new CityChunkDescriptor ();
			cityChunkDescriptor.forceBuild = cityPiece;
			cityChunks.put(new ChunkCoordinates(xChunk, zChunk), cityChunkDescriptor);
		}
	}
	
	public void buildPiece(int pieceID, byte[] data, byte[] meta, Random rand) {
		//this.raiseTerrain(this.baseHeight, data);
		//this.flattenTerrain(this.baseHeight, data);
		
		switch (pieceID) {
			case ROAD_TYPE_1:
			case ROAD_TYPE_2:
				this.generateStreetFloorSimple(0, this.baseHeight, 0, data, meta, rand, pieceID);
				this.hasRoad = true;
				this.roadVariation = pieceID;
		}
	}
	
	public void populate(World world, Random rand, int chunkX, int chunkZ, Chunk chunk) {
		int x0 = chunkX * 16;
		int z0 = chunkZ * 16;
		
		BiomeGenBase biomeGen = chunk.getBiomeGenAt(8, 8);
		this.desertChunk = biomeGen instanceof BiomeGenDesert;
		
		int i, x, y, z;
		
		// City related decorations
		if(chunk.hasRoad) {
			y = chunk.baseHeight + 1;
			WorldGenTrees worldGenTrees = this.desertChunk ? new WorldGenTreesDead() : new WorldGenTrees();
			boolean roadAlongX = (chunk.roadVariation == 1);

			int[] treeX = roadAlongX ? new int[]{3, 11, 3, 11} : new int[]{1, 13, 1, 13};
			int[] treeZ = roadAlongX ? new int[]{1, 1, 13, 13} : new int[]{3, 3, 11, 11};
			for (int t = 0; t < 4; t++) {
				worldGenTrees.generate(world, rand, x0 + treeX[t], y, z0 + treeZ[t]);
			}

			int[] lightX = roadAlongX ? new int[]{7, 15, 7, 15} : new int[]{2, 2, 12, 12};
			int[] lightZ = roadAlongX ? new int[]{2, 2, 12, 12} : new int[]{7, 15, 7, 15};
			// case 1: light on when rand != 0  (2/3 of the time)
			// case 2: light on when rand == 0  (1/3 of the time)
			boolean lightOnWhenZero = !roadAlongX;
			for (int t = 0; t < 4; t++) {
				boolean lit = lightOnWhenZero ? (rand.nextInt(3) == 0) : (rand.nextInt(3) != 0);
				(new WorldGenStreetLight(lit)).generate(world, rand, x0 + lightX[t], y, z0 + lightZ[t]);
			}
			
			// Railings & stairs against adjacent chunks.
			// Direction 0 = west (-x), 1 = east (+x), 2 = north (-z), 3 = south (+z)
			processEdgeChunk(world, rand, chunk, chunkX, chunkZ, x0, y, z0, 0);
			processEdgeChunk(world, rand, chunk, chunkX, chunkZ, x0, y, z0, 1);
			processEdgeChunk(world, rand, chunk, chunkX, chunkZ, x0, y, z0, 2);
			processEdgeChunk(world, rand, chunk, chunkX, chunkZ, x0, y, z0, 3);

			if(rand.nextBoolean()) for (i = 0; i < 8; i ++) {
				x = rand.nextInt(16);
				z = rand.nextInt(16);
				if (world.getBlockId(x0 + x, y - 1, z0 + z) == 0 && world.getBlockId(x0 + x, y - 2, z0 + z) != 0) {
					world.setBlock(x0 + x, y - 1, z0 + z, Block.woodenSpikes.blockID);
				}
			}
		}
		
		// Chest 
		if(chunk.hasBuilding) {
			if(chunk.chestY != -1) {
				world.setBlockWithNotify(x0 + chunk.chestX, chunk.chestY, z0 + chunk.chestZ, Block.chest.blockID);
				TileEntityChest tec = (TileEntityChest)world.getBlockTileEntity(x0 + chunk.chestX, chunk.chestY, z0 + chunk.chestZ);
		
				if(tec != null && tec.getSizeInventory() > 0) {
					int ni = rand.nextInt(4) + 1;
					int level = rand.nextInt(10);
					
					for(i = 0; i < ni; ++i) {
						ItemStack itemStack = this.getTreasure(level, rand);
						tec.setInventorySlotContents(i, itemStack);
					}
				}
			}
			
			// Spawner
			if(chunk.specialY != -1) {
				world.setBlockWithNotify(x0 + chunk.specialX, chunk.specialY, z0 + chunk.specialZ, Block.mobSpawner.blockID);
				TileEntityMobSpawner ms = (TileEntityMobSpawner)world.getBlockTileEntity(x0 + chunk.specialX, chunk.specialY, z0 + chunk.specialZ);
				if(ms != null) {
					ms.mobID = this.getMobID(rand);
				}
			}
		}
	}
	

	/*
	 * Adds railing or stairs on one side of a road chunk based on the height
	 * of the adjacent chunk in that direction.
	 *
	 * direction: 0 = -x edge, 1 = +x edge, 2 = -z edge, 3 = +z edge.
	 */
	private void processEdgeChunk(World world, Random rand, Chunk chunk, int chunkX, int chunkZ,
	                              int x0, int y, int z0, int direction) {
		int otherX = chunkX + (direction == 0 ? -1 : direction == 1 ? 1 : 0);
		int otherZ = chunkZ + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
		if (!world.chunkExists(otherX, otherZ)) {
			return;
		}
		Chunk otherChunk = world.getChunkFromChunkCoords(otherX, otherZ);
		if (!(otherChunk.hasRoad || otherChunk.hasBuilding)) {
			return;
		}

		// For -x: edge at x0, looping z. For +x: edge at x0+15, looping z. Etc.
		int edgeX = (direction == 0) ? x0 : (direction == 1) ? x0 + 15 : -1;
		int edgeZ = (direction == 2) ? z0 : (direction == 3) ? z0 + 15 : -1;
		boolean edgeAlongX = (direction >= 2); // dir 2/3: edge is along x, fixed z

		if (otherChunk.baseHeight < chunk.baseHeight) {
			// Railing along the edge
			for (int i = 0; i < 16; i++) {
				int rx = edgeAlongX ? x0 + i : edgeX;
				int rz = edgeAlongX ? edgeZ : z0 + i;
				world.setBlock(rx, y, rz, Block.streetLanternFence.blockID);
				world.setBlock(rx, y - 1, rz, Block.stairDouble.blockID);
			}
			// Hole in railing (the gap where stairs from the neighbor would land)
			if (otherChunk.hasRoad) {
				int holeStart = (direction == 0 || direction == 3) ? 5 : 9;
				for (int j = 0; j < 2; j++) {
					int rx = edgeAlongX ? x0 + holeStart + j : edgeX;
					int rz = edgeAlongX ? edgeZ : z0 + holeStart + j;
					world.setBlock(rx, y, rz, 0);
				}
			}
		} else if (otherChunk.hasRoad && otherChunk.baseHeight > chunk.baseHeight) {
			// Stairs down toward the higher neighbor
			int stairType = direction;
			int sx, sz;
			if (direction == 0) { sx = x0;     sz = z0 + 5;  }
			else if (direction == 1) { sx = x0 + 13; sz = z0 + 5;  }
			else if (direction == 2) { sx = x0 + 5;  sz = z0;     }
			else                   { sx = x0 + 5;  sz = z0 + 13; }
			(new WorldGenStairs(stairType)).generate(world, rand, sx, y - 2, sz);
		}
	}

	@FunctionalInterface
	private interface ItemStackFactory {
		ItemStack make(Random rand);
	}

	private static ItemStackFactory[] array(ItemStackFactory... items) {
		return items;
	}

	private static final ItemStackFactory[] TREASURE_NORMAL = array(
		r -> new ItemStack(Item.ingotIron,  r.nextInt(4) + 1),
		r -> new ItemStack(Item.bucketEmpty),
		r -> new ItemStack(Item.bread),
		r -> new ItemStack(Block.torchWood, r.nextInt(16) + 1),
		r -> new ItemStack(Item.wheat,      r.nextInt(3) + 1),
		r -> new ItemStack(Block.torchWood, r.nextInt(16) + 1)
	);

	@FunctionalInterface
	private interface NestedTreasure extends ItemStackFactory {}

	private final ItemStackFactory[] TREASURE_MIDDLE = array(
		r -> this.getTreasure(1, new java.util.Random(r.nextLong())),
		r -> this.getTreasure(1, new java.util.Random(r.nextLong())),
		r -> this.getTreasure(1, new java.util.Random(r.nextLong())),
		r -> new ItemStack(Item.bowlSoup),
		r -> new ItemStack(Item.ingotGold,   1 + r.nextInt(6)),
		r -> new ItemStack(Item.saddle),
		r -> new ItemStack(Item.dyePowder,  1 + r.nextInt(10), r.nextInt(16)),
		r -> new ItemStack(Item.bowlSoup)
	);

	private final ItemStackFactory[] TREASURE_GOOD = array(
		r -> this.getTreasure(2, new java.util.Random(r.nextLong())),
		r -> this.getTreasure(2, new java.util.Random(r.nextLong())),
		r -> this.getTreasure(2, new java.util.Random(r.nextLong())),
		r -> new ItemStack(Item.diamond),
		r -> new ItemStack(Item.appleGold),
		r -> new ItemStack(Block.sponge),
		r -> new ItemStack(Item.saddle),
		r -> new ItemStack(Item.diamond)
	);

	private static final String[] MOBS = {
		"SwarmSpider", "SwarmSpider", "SwarmSpider",
		"Spider",      "Spider",
		"Zombie",      "Zombie",
		"Skeleton",    "Skeleton",    "Skeleton",
		"ElementalCreeper",
	};

	protected ItemStack getTreasure(int level, Random rand) {
		ItemStackFactory[] table;
		if (level < 7) {
			table = TREASURE_NORMAL;
		} else if (level < 9) {
			table = TREASURE_MIDDLE;
		} else {
			table = TREASURE_GOOD;
		}
		return table[rand.nextInt(table.length)].make(rand);
	}

	public String getMobID(Random rand) {
		return MOBS[rand.nextInt(MOBS.length)];
	}
}
