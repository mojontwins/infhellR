package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.achievements.StatList;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.EnumSkyBlock;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.entity.misc.EntityItem;

public class BlockSnow extends Block {
	protected BlockSnow(int id, int blockIndex) {
		super(id, blockIndex, Material.snow);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
		this.setTickOnLoad(true);
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}

	@Override
	public void setBlockBoundsForItemRender() {
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		int meta = blockAccess.getBlockMetadata(x, y, z) & 15;
		float height = (float)(meta + 1) / 16.0F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F);
	}
	
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		Block block = Block.blocksList[world.getBlockId(x, y - 1, z)];
		if(block == null) return false;
		if(!block.isOpaqueCube()) return false;
		//if(block == Block.sand ) return false;
		return world.getBlockMaterial(x, y - 1, z).getIsSolid();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		this.canSnowStay(world, x, y, z);
	}

	private boolean canSnowStay(World world, int x, int y, int z) {
		if(!this.canPlaceBlockAt(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
			world.setBlockWithNotify(x, y, z, 0);
			return false;
		} else {
			return true;
		}
	}

	@Override	
	public void harvestBlock(World world1, EntityPlayer entityPlayer2, int i3, int i4, int i5, int i6) {
		int i7 = Item.snowball.shiftedIndex;
		float f8 = 0.7F;
		double d9 = (double)(world1.rand.nextFloat() * f8) + (double)(1.0F - f8) * 0.5D;
		double d11 = (double)(world1.rand.nextFloat() * f8) + (double)(1.0F - f8) * 0.5D;
		double d13 = (double)(world1.rand.nextFloat() * f8) + (double)(1.0F - f8) * 0.5D;
		EntityItem entityItem15 = new EntityItem(world1, (double)i3 + d9, (double)i4 + d11, (double)i5 + d13, new ItemStack(i7, 1, 0));
		entityItem15.delayBeforeCanPickup = 10;
		world1.spawnEntityInWorld(entityItem15);
		world1.setBlockWithNotify(i3, i4, i5, 0);
		entityPlayer2.addStat(StatList.mineBlockStatArray[this.blockID], 1);
	}

	@Override
	public int idDropped(int metadata, Random rand) {
		return Item.snowball.shiftedIndex;
	}

	@Override
	public int quantityDropped(Random rand) {
		return 0;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(world.getSavedLightValue(EnumSkyBlock.Block, x, y, z) > 11) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
			world.setBlockWithNotify(x, y, z, 0);
		}

	}
	
	@Override
	public void onEntityCollidedWithBlock(World var1, int var2, int var3, int var4, Entity var5) {
		var5.setInSnow();
	}
}
