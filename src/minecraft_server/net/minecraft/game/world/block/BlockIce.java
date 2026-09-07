package net.minecraft.game.world.block;

import java.util.List;
import java.util.Random;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.EnumSkyBlock;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.Seasons;
import net.minecraft.game.container.creativetab.CreativeTabs;

public class BlockIce extends BlockBreakable implements IBlockWithSubtypes {
	private int rainbowCounter = 0;
	
	String[] names = new String[] { "ice", "ice.orange" };
	
	public static int[] rainbowColors = new int[] {
			0xFF6666, 0xFF66A3, 0xFF66EB, 0xE666FF, 0xA866FF, 0x7066FF, 0x668AFF, 0x66D2FF, 
			0x66FFFA, 0x66FFC4, 0x66FF85, 0x70FF66, 0xADFF66, 0xEEFF66, 0xFFE566, 0xFF9E66
	};
	
	public BlockIce(int blockID, int blockIndex) {
		super(blockID, blockIndex, Material.ice, false);
		this.slipperiness = 0.98F;
		this.setTickOnLoad(true);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return super.shouldSideBeRendered(blockAccess, x, y, z, 1 - side);
	}

	@Override
	public void harvestBlock(World world1, EntityPlayer entityPlayer2, int i3, int i4, int i5, int i6) {
		super.harvestBlock(world1, entityPlayer2, i3, i4, i5, i6);
		if(world1.getBlockMetadata(i3, i4, i5) > 0) return;
		Material material7 = world1.getBlockMaterial(i3, i4 - 1, i5);
		if(material7.getIsSolid() || material7.getIsLiquid()) {
			world1.setBlockWithNotify(i3, i4, i5, Block.waterMoving.blockID);
		}

	}

	@Override
	public int quantityDropped(Random rand) {
		return 0;
	}

	@Override
	public void updateTick(World world1, int i2, int i3, int i4, Random rand) {
		if(world1.getBlockMetadata(i2, i3, i4) > 0 || world1.getBiomeGenAt(i2, i3).weather == Weather.cold || Seasons.currentSeason == Seasons.WINTER || (Seasons.currentSeason == Seasons.AUTUMN && rand.nextBoolean())) return;
		if(world1.getSavedLightValue(EnumSkyBlock.Block, i2, i3, i4) > 11 - Block.lightOpacity[this.blockID]) {
			this.dropBlockAsItem(world1, i2, i3, i4, world1.getBlockMetadata(i2, i3, i4));
			world1.setBlockWithNotify(i2, i3, i4, Block.waterStill.blockID);
		}

	}

	@Override
	public int getMobilityFlag() {
		return 0;
	}
	
	public boolean seeThrough() {
		return true; 
	}

	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		int meta = blockAccess.getBlockMetadata(x, y, z);
		
		return meta == 0 ? 
				0xFFFFFF
			:
				rainbowColors[(x + y + z) & 0xF];
	}

	@Override
	public int getRenderColor(int meta) {
		rainbowCounter = (rainbowCounter + 1) & 0xf;
		return meta == 0 ? 0xFFFFFF : rainbowColors[0];
	}
	
	@Override
	public int getRenderBlockPass() {
		return 1;
	}
	
    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < 2; i ++) {
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

	@Override
	public String getNameFromMeta(int meta) {
		return this.names[meta];
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return this.blockIndexInTexture;
	}
}
