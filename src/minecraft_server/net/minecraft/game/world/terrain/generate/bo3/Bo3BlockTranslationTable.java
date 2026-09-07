package net.minecraft.game.world.terrain.generate.bo3;

import java.util.HashMap;

import net.minecraft.game.world.block.Block;

public class Bo3BlockTranslationTable {
	private static final HashMap<String, Integer> translationTable = new HashMap<String, Integer> () {
		private static final long serialVersionUID = 987654679L;
	
		{
			this.put("AIR", 0);
			this.put("STONE", Block.stone.blockID);
			this.put("DIRT", Block.dirt.blockID);
			this.put("SAND", Block.sand.blockID);
			this.put("GRASS", Block.grass.blockID);
			this.put("STATIONARY_WATER", Block.waterStill.blockID);
			this.put("WATER", Block.waterMoving.blockID);
			this.put("WATER_LILY", Block.lilyPad.blockID);
			this.put("HARD_CLAY", Block.terracotta.blockID);
			this.put("STAINED_CLAY", Block.stainedTerracotta.blockID);
			this.put("CLAY", Block.blockClay.blockID);
			this.put("SANDSTONE", Block.sandStone.blockID);
			this.put("GRAVEL", Block.gravel.blockID);
			this.put("LONG_GRASS", Block.tallGrass.blockID);
			this.put("DEAD_BUSH", Block.deadBush.blockID);
			this.put("YELLOW_FLOWER", Block.plantYellow.blockID);
			this.put("RED_ROSE",Block.plantRed.blockID);
			this.put("BROWN_MUSHROOM", Block.mushroomBrown.blockID);
			this.put("RED_MUSHROOM", Block.mushroomRed.blockID);
			this.put("LOG", Block.wood.blockID);
			this.put("LOG_2", Block.wood.blockID);
			this.put("HUGE_MUSHROOM_1", Block.mushroomCapBrown.blockID);
			this.put("HUGE_MUSHROOM_2", Block.mushroomCapRed.blockID);
			this.put("WEB", Block.web.blockID);
			this.put("DOUBLE_PLANT", 0);
			this.put("LEAVES", Block.leaves.blockID);
			this.put("LEAVES_2", Block.leaves.blockID);
			this.put("VINE", Block.vine.blockID);
			this.put("ICE", Block.ice.blockID);
			this.put("JACK_O_LANTERN", Block.pumpkin.blockID);
			this.put("SNOW_BLOCK", Block.blockSnow.blockID);
			this.put("COCOA", 0);
			
			// Add more, as fit
		}
	};	

	public static int getId(String representation) {
		Object o = translationTable.get(representation);
		if(o == null) {
			System.out.println ("Warning! can't translate -> " + representation);
			return 0;
		}
		
		try {
			return ((Integer) o).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
