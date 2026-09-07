package net.minecraft.game.world.terrain.generate.betterdungeons.schematics;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.game.world.terrain.generate.betterdungeons.BetterDungeons;
import net.minecraft.game.world.terrain.generate.betterdungeons.StructureBlockData;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockContainer;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.World;

public abstract class SchematicsBigShip extends Schematics implements ISchematic {

	public SchematicsBigShip(BetterDungeons betterDungeons) {
		super(betterDungeons);
	}
	
	public abstract short[][][][] getStructureList();
	
	public abstract int getStructurePiecesX();
	
	public abstract int getStructurePiecesZ();

	public void drawShip(World world, Random rand, int x0, int y0, int z0, int structureId) {

		int index = 0;
		
		// Avoid possible shitters - make sure this is chunk aligned i.e. we use chunk coordinates
		int cx = x0 >> 4;
		for(int xx = 0; xx < this.getStructurePiecesX(); xx ++) {
			
			int cz = z0 >> 4;
			for(int zz = 0; zz < this.getStructurePiecesZ(); zz ++) {
				short[][][] structure = this.pickStructure(index ++);
			
				this.drawPiece(world, rand, cx, y0, cz, structureId, structure);
				
				cz ++;
			}
			
			cx ++;
		}
	}
	
	public void drawShipPiece(World world, Random rand, int x0, int y0, int z0, int structureId, int pieceIndex) {
		this.drawPiece(world, rand, x0, y0, z0, structureId, this.pickStructure(pieceIndex));
	}
	
	public void drawPiece(World world, Random rand, int cx, int y0, int cz, int structureId, short[][][] structure) {
		Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
		short element;
		int count;
		
		int x1 = cx << 4;
		int z1 = cz << 4;
		
		ArrayList<StructureBlockData> specialBlocks = new ArrayList<StructureBlockData> ();
		
		for(int x = 0; x < structure.length; x ++) {
			int absX = x1 | x;
			
			for(int z = 0; z < structure[x].length; z ++) {
				int absZ = z1 | z;
				
				/*
				// clear at least 10 blocks above the castle. most castles are 50
				// blocks high.
				for(int y = 49; y < 60; y ++ ) {
					chunk.setBlockIDWithMetadataNoLights(x, y + y0, z, 0, 0);
				}
				*/
				
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
									specialBlocks.add(new StructureBlockData(absX, absY, absZ, blockID, metadata));
								} else if(blockID == 52) {
									this.betterDungeons.addSpawner(rand, world, absX, absY, absZ);
								} else if(blockID == 54) {
									this.betterDungeons.addChest(rand, world, absX, absY, absZ);
								} else if (blockID < 254) { 
									if(blockID == Block.thinGlass.blockID) blockID = Block.glass.blockID;
									else if(blockID == Block.blockBed.blockID) { blockID = Block.stairSingle.blockID; metadata = 0; }
									
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
		
		this.betterDungeons.copySpecialBlocksCustom(world, specialBlocks);
		chunk.clearAllLights();
		chunk.generateHeightMap();
		chunk.generateSkylightMap();
		chunk.initLightingForRealNotJustHeightmap();
	}
}
