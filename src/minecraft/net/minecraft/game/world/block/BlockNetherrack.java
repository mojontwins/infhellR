package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.material.Material;

public class BlockNetherrack extends Block {
	public BlockNetherrack(int i1, int i2) {
		super(i1, i2, Material.rock);
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
}
