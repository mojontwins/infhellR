package net.minecraft.game.world.block;

import java.util.List;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.material.Material;

public class BlockTerracotta extends Block {
	private boolean stained;
	
	public static final int terracottaColors[] = {
		0x261811, 0x913E30, 0x4F562D, 0x503626, 0x4D3F5D, 0x7A4A59, 0x595E5D, 0x8B6E64,
		0x3C2D25, 0xA85252, 0x6F7A38, 0xC08926, 0x766F8C, 0x9B5C70, 0xA75829, 0xD5B7A4
	};
	
	public static final int nonDiedColor = 0x955439;
	
	public BlockTerracotta(int id, int textureIndex, Material material, boolean stained) {
		super(id, textureIndex, material);
		this.stained = stained;
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	protected int damageDropped(int i1) {
		return i1;
	}

	public static int getBlockFromDye(int i0) {
		return ~i0 & 15;
	}

	public static int getDyeFromBlock(int i0) {
		return ~i0 & 15;
	}
	
	// Use this and save 15 textures in the texture atlas. Note how metadata-color is reversed
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		if(this.stained) {
			int meta = blockAccess.getBlockMetadata(x, y, z);
			return terracottaColors[15 - meta];
		} else return 0x955439;
	}
	
	public int getRenderColor(int meta) {
		return this.stained ? terracottaColors[15 - meta] : nonDiedColor;
	}
	
    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		if(this.stained) {
	    	for(int i = 0; i < 16; i ++) {
				par3List.add(new ItemStack(par1, 1, i));
			}
		} else {
			par3List.add(new ItemStack(par1, 1, 0));
		}
	}
}
