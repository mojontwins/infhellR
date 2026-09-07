package net.minecraft.game.world.terrain.generate.betterdungeons.schematics;

import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.BetterDungeons;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockContainer;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.World;

public abstract class Schematics implements ISchematic {
	public BetterDungeons betterDungeons = null;	
	public abstract short[][][][] getStructureList();

	public Schematics(BetterDungeons betterDungeons) {
		this.betterDungeons = betterDungeons;
	}
	
	public short[][][] pickupRandomStructure(Random rand) {
		short[][][][] structureList = this.getStructureList();
		return structureList[rand.nextInt(structureList.length)];
	}
	
	public short[][][] pickStructure(int index) {
		return this.getStructureList() [index];
	}
	
	public int pickupRandomIndex(Random rand) {
		return rand.nextInt(this.getStructureList().length);
	}
	
	public int getStructureCount() {
		return this.getStructureList().length;
	}
	
	public int getStructureWidth(short[][][] structure) {
		return structure.length;
	}
	
	public int getStructureLength(short[][][] structure) {
		return structure[0].length;
	}
	
	/*
	 * Unpacks a rle'd schematic into a bigger, unpacked map.
	 * Remember that everything is column-ordered [x][z][y]!!
	 */
	public void unpackStructureToArrayTransparentAt(short[][][] structure, short[][][] map, int x0, int y0, int z0) {
		short element;
		int count;
		
		for(int x = 0; x < structure.length; x ++) {
			for(int z = 0; z < structure[x].length; z ++) {
				int y = 0;
				int i = 0; while (i < structure[x][z].length) {
					element = structure[x][z][i ++];
					if(element == -255) break;
					
					if(element < -1) {
						count = -element;
						element = structure[x][z][i ++];
					} else {
						count = 1;
					}
					
					while(count -- > 0) {
						if(element != -1) {
							map[x0 + x][z0 + z][y0 + y] = element;
							y ++;
						}
					}
				}
			}
		}
	}

	/*
	 * Renders a rle'd schematic to the world
	 * Remember that everything is column-ordered [x][z][y]!!
	 */
	public void unpackStructureToWorldTransparentAt(short[][][] structure, World world, Random rand, int x0, int y0, int z0, int structureId) {
		this.betterDungeons.getSpecialBlocks().clear();
		
		short element;
		int count;
		
		for(int x = 0; x < structure.length; x ++) {
			for(int z = 0; z < structure[x].length; z ++) {
				
				int absX = x0 + x; 
				int absZ = z0 + z;
				Chunk chunk = world.getChunkFromBlockCoords(absX, absZ);
				
				int cx = absX & 0xf;
				int cz = absZ & 0xf;
				
				int y = 0;
				int i = 0; while (i < structure[x][z].length) {
					element = structure[x][z][i ++];
					if(element == -255) break;
					
					if(element < -1) {
						count = -element;
						element = structure[x][z][i ++];
					} else {
						count = 1;
					}
					
					while(count -- > 0) {
						if(element != -1) {
							int absY = y + y0; 
							if(absY >= 0) {
								Block existingBlock = Block.blocksList[chunk.getBlockID(cx, absY, cz)];
								if(existingBlock == null || !(existingBlock instanceof BlockContainer)) {
									int blockID = element & 0xff;
									int metadata = (element >> 8) & 0xf;
									
									// Special blocks
									if(
											blockID == 50 || blockID == 64 || blockID == 65 || blockID == 69 || 
											blockID == 71 || blockID == 75 || blockID == 76 || blockID == 77
									) {
										this.betterDungeons.addSpecialBlock(absX, absY, absZ, blockID, metadata);
									} else if(blockID == 52) {
										this.betterDungeons.addSpawner(rand, world, absX, absY, absZ);
									} else if(blockID == 54) {
										this.betterDungeons.addChest(rand, world, absX, absY, absZ);
									} else if (blockID < 254) {
										if(blockID == Block.stoneBricks.blockID) {
											metadata = rand.nextInt(7); 
											if(metadata > 2) {
												metadata = 0;
											}
										}
										chunk.setBlockIDWithMetadata(cx, absY, cz, blockID, metadata);
									} else {
										this.betterDungeons.addCustomBlock(absX, absY, absZ, blockID, metadata, world, structureId);
									}
								}
							}
						}
						
						y ++;
					}
				}
			}
		}
		
		this.betterDungeons.copySpecialBlocks(world);
	}	
	
	/*
	 * Renders a rle'd schematic to the world
	 * Remember that everything is column-ordered [x][z][y]!!
	 * Chunk-alligned version
	 */
	public void unpackStructureToWorldTransparentAtChunk(short[][][] structure, World world, Random rand, int x0, int y0, int z0, int structureId) {
		this.betterDungeons.getSpecialBlocks().clear();
		
		short element;
		int count;
		
		x0 &= 0xfffffff0;
		z0 &= 0xfffffff0;
		
		Chunk chunk = world.getChunkFromChunkCoords(x0 >> 4, z0 >> 4);
		
		for(int x = 0; x < structure.length; x ++) {
			for(int z = 0; z < structure[x].length; z ++) {
				
				int absX = x0 + x; 
				int absZ = z0 + z;
				
				int y = 0;
				int i = 0; while (i < structure[x][z].length) {
					element = structure[x][z][i ++];
					if(element == -255) break;
					
					if(element < -1) {
						count = -element;
						element = structure[x][z][i ++];
					} else {
						count = 1;
					}
					
					while(count -- > 0) {
						if(element != -1) {
							int absY = y + y0;
							Block existingBlock = Block.blocksList[chunk.getBlockID(x, absY, z)];
							if(existingBlock == null || !(existingBlock instanceof BlockContainer)) {
								int blockID = element & 0xff;
								int metadata = (element >> 8) & 0xf;
								
								// Special blocks
								if(
										blockID == 50 || blockID == 64 || blockID == 65 || blockID == 69 || 
										blockID == 71 || blockID == 75 || blockID == 76 || blockID == 77
								) {
									this.betterDungeons.addSpecialBlock(absX, absY, absZ, blockID, metadata);
								} else if(blockID == 52) {
									this.betterDungeons.addSpawner(rand, world, absX, absY, absZ);
								} else if(blockID == 54) {
									this.betterDungeons.addChest(rand, world, absX, absY, absZ);
								} else if (blockID < 254) {
									if(blockID == Block.stoneBricks.blockID) {
										metadata = rand.nextInt(7); 
										if(metadata > 2) {
											metadata = 0;
										}
									}
									chunk.setBlockIDWithMetadataNoLights(x, absY, z, blockID, metadata);
								} else {
									this.betterDungeons.addCustomBlock(absX, absY, absZ, blockID, metadata, world, structureId);
								}
							}
						}
						
						y ++;
					}
				}
			}
		}
		
		this.betterDungeons.copySpecialBlocks(world);
	}
}
