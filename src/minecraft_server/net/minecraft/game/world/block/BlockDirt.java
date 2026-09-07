package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.material.Material;

public class BlockDirt extends Block {
	protected BlockDirt(int i1, int i2) {
		super(i1, i2, Material.ground);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	public boolean canGrowPlants() {
		return true;
	}	
}
