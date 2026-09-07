package net.minecraft.game.world.schematic;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockState;

public class VanillaConverter {
	
	// Converts a vanilla schematic and remaps some stuff for Infhell
	
	public static void convert(Schematic schematic) {
		int size = schematic.getWidth() * schematic.getHeight() * schematic.getLength();
		BlockState blockState; 
		
		for(int idx = 0; idx < size; idx ++) {
			blockState = convert((int)schematic.getBlocks()[idx] & 0xff, (int)schematic.getData()[idx] & 0xff);
			
			schematic.getBlocks()[idx] = (byte) blockState.getBlockID();
			schematic.getData()[idx] = (byte) blockState.getMetadata();
		}
	}
	
	public static BlockState convert(int blockID, int metadata) {
		
		if(blockID == Block.wood.blockID) {
			if(metadata == 4) metadata = 12;
			else if(metadata == 8) metadata = 4;
		} else {
			// Out of bounds / changed / non existing
			switch(blockID) {
			
			// Flowers
			case 38:
				if(metadata == 2) {
					blockID = Block.paeonia.blockID;
					metadata = 0;
					break;
				} else if(metadata == 1) {
					blockID = Block.blueFlower.blockID;
					metadata = 0;
					break;
				}
				break;
				
			case 175:
				if(metadata == 5) {
					blockID = Block.paeonia.blockID;
					metadata = 0;
					break;
				}
				break;
			
			// Wooden slab
			case 126:
				blockID = Block.stairSingle.blockID; 
				metadata = (metadata & 0xF8) | 2; 
				break;
					
			// Hardened clay
			case 159:
				blockID = Block.stainedTerracotta.blockID; 
				break;
				
			case 172:
				blockID = Block.terracotta.blockID;
				break;
			}
			
		}
		
		// TODO :: More
		
		return new BlockState(blockID, metadata);
	}
}
