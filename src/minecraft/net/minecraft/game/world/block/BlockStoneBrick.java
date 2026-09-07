package net.minecraft.game.world.block;

import java.util.List;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.material.Material;

public class BlockStoneBrick extends Block implements IBlockWithSubtypes {

	String names[] = new String[] {
			"normal", "small"
	};
	int[] brickTextures = { 14*16 + 5, 9*16+13 };

	public BlockStoneBrick(int i1, Material material2) {
		super(i1, material2);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		if(meta >= this.brickTextures.length) meta = 0;
		return this.brickTextures [meta];
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for (int i = 0; i < this.brickTextures.length; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

	@Override
	public String getNameFromMeta(int meta) {
		return "stoneBricks." + this.names[meta];
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return this.getBlockTextureFromSideAndMetadata(2, meta);
	}

}
