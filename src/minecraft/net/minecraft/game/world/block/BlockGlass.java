package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockGlass extends BlockBreakable implements IBlockWithSubtypes {
	private int[] glassTextures = { 49, 9 * 16 + 15, 9 * 16 + 14, 49 };
	private String[] glassNames = {"glass", "wooden", "metal"};

	public BlockGlass(int i1, int i2, Material material3, boolean z4) {
		super(i1, i2, material3, z4);
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		return this.glassTextures [meta & 3];
	}
	
	@Override
	public int quantityDropped(Random random1) {
		return 1;
	}
	
	@Override
	public int idDropped(int meta, Random random2) {
		return meta == 0 ? 0 : this.blockID;
	}

	@Override
	public int damageDropped(int meta) {
		return meta & 3;
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		world.setBlockMetadata(x, y, z, world.getBlockMetadata(x, y, z) & 3);
	}
	
	@Override
	public int getRenderBlockPass() {
		return 0;
	}
	
	public boolean seeThrough() {
		return true; 
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for (int i = 0; i < this.glassNames.length; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

	@Override
	public String getNameFromMeta(int meta) {
		int i = meta & 3;
		if(i >= this.glassNames.length) i = 0; // meta 3 = plain glass (same texture as meta 0)
		return "glass." + this.glassNames[i];
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return this.getBlockTextureFromSideAndMetadata(2, meta);
	}
}
