package net.minecraft.game.world.terrain.generate.city;

import net.minecraft.game.world.schematic.Schematic;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;


public abstract class BuildingSchematic extends Building {
	public abstract String getSchematic();
	
	Schematic schematic;
	
	public BuildingSchematic() {
		// Load schematic
		schematic = new Schematic(this.getSchematic());
	}
	
	public void generate(int y0, Chunk chunk) {
		int idx = 0;
		int height = schematic.getHeight();
		int length = schematic.getLength();
		int width = schematic.getWidth();
		byte b;
		
		int chunkidx;
		for(int y = 0; y < height; y ++) {
			for(int z = 0; z < length; z ++) {
				chunkidx = (y0 + y) | (z << 7);
				for(int x = 0; x < width; x ++) {
					b = this.schematic.getBlocks()[idx];
					if(b != (byte)Block.structureVoid.blockID) {	
						chunk.blocks[chunkidx] = b;
						chunk.data[chunkidx] = this.schematic.getData()[idx];
					}
					
					chunkidx += 2048;
					idx ++;
				}
			}
		}
	}
}
