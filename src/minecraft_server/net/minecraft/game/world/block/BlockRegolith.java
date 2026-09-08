package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.material.Material;

public class BlockRegolith extends Block {
	public BlockRegolith(int id, int blockIndex) {
		super(id, blockIndex, Material.grass);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	public int quantityDropped(Random rand) {
		return 1 + rand.nextInt(4);
	}

	public int idDropped(int metadata, Random rand) {
		return Item.pebble.shiftedIndex;
	}
	
	public boolean canGrowPlants() {
		return true;
	}
	
	public boolean canGrowMoss() {
		return true;
	}
}
