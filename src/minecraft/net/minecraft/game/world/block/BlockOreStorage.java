package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.material.Material;

public class BlockOreStorage extends Block {
	public BlockOreStorage(int i1, int i2) {
		super(i1, Material.iron);
		this.blockIndexInTexture = i2;
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int getBlockTextureFromSide(int i1) {
		return this.blockIndexInTexture;
	}
}
