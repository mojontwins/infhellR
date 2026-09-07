package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.material.Material;

public class BlockStone extends Block {
	public static final int[] stoneColor = new int [] {
		0xFFFFFF,
		0xCCCCCC
	};
	
	public BlockStone(int id, int blockIndex) {
		super(id, blockIndex, Material.rock);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int idDropped(int metadata, Random rand) {
		return Block.cobblestone.blockID;
	}
	
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return this.getRenderColor(blockAccess.getBlockMetadata(x, y, z));
	}

	@Override
	public int getRenderColor(int meta) {
		return stoneColor[(meta >> 4) & 7];
	}
	
    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < 2; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}
}
