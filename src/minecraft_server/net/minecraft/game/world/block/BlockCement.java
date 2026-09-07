package net.minecraft.game.world.block;

import java.util.List;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.material.Material;

public class BlockCement extends Block implements IBlockWithSubtypes {
	private int[] cementTextures = { 11*16, 10*16+15, 10*16+14 };
	private String[] cementNames = {"normal", "wooden", "metal"};

	public BlockCement(int i1, Material material2) {
		super(i1, material2);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		return this.cementTextures [meta];
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for (int i = 0; i < this.cementTextures.length; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

	@Override
	public String getNameFromMeta(int meta) {
		return "cement." + this.cementNames[meta];
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return this.getBlockTextureFromSideAndMetadata(2, meta);
	}
}
