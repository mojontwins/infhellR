package net.minecraft.game.world.biome;

import java.util.Random;
import net.minecraft.game.world.block.Block;


public class BiomeGenMesaSurroundings extends BiomeGenMesa {

	public BiomeGenMesaSurroundings(boolean forested) {
		super(forested);
		
		this.treeBaseAttemptsModifier = 0;
		this.minHeight = 0;
		this.maxHeight = 0.1F;
		this.gravelLumpAttempts = 8;
	}

	@Override
	public byte getTopBlock(Random rand) {
		/*
		switch(rand.nextInt(8)) {
		case 0: 
		case 1:
		case 2: return (byte)Block.sand.blockID;
		case 3: return (byte)Block.cobblestone.blockID;
		default: return (byte)Block.stainedTerracotta.blockID;
		}
		*/
		return rand.nextBoolean() ? (byte)Block.terracotta.blockID : (byte)Block.dirtPath.blockID;
	}
}
