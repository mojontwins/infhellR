package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.achievements.StatList;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.entity.misc.EntityItem;

public class BlockFlower extends Block {
	protected BlockFlower(int i1, int i2) {
		super(i1, Material.plants);
		this.blockIndexInTexture = i2;
		this.setTickOnLoad(true);
		this.setMyBlockBounds();
		this.setLightValue(0);
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}

	public void setMyBlockBounds() {
		float var3 = 0.4F;
		this.setBlockBounds(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 0.8F, 0.5F + var3);
	}
	
	public boolean canPlaceBlockAt(World world1, int i2, int i3, int i4) {
		return super.canPlaceBlockAt(world1, i2, i3, i4) && this.canThisPlantGrowOnThisBlockID(world1.getBlockId(i2, i3 - 1, i4));
	}

	protected boolean canThisPlantGrowOnThisBlockID(int i1) {
		Block block = Block.blocksList[i1];
		return block != null && block.canGrowPlants();
	}

	public void onNeighborBlockChange(World world1, int i2, int i3, int i4, int i5) {
		super.onNeighborBlockChange(world1, i2, i3, i4, i5);
		this.checkFlowerChange(world1, i2, i3, i4);
	}

	public void updateTick(World world1, int i2, int i3, int i4, Random random5) {
		this.checkFlowerChange(world1, i2, i3, i4);
	}

    protected void checkFlowerChange(World world, int x, int y, int z) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
            world.setBlockWithNotify(x, y, z, 0);
        }

    }
	
	public boolean canBlockStay(World world1, int i2, int i3, int i4) {
		return (world1.getFullBlockLightValue(i2, i3, i4) >= 8 || world1.canBlockSeeTheSky(i2, i3, i4)) && this.canThisPlantGrowOnThisBlockID(world1.getBlockId(i2, i3 - 1, i4));
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world1, int i2, int i3, int i4) {
		return null;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return 111;
	}
	
	public boolean seeThrough() {
		return true; 
	}
	
	@Override	
	public void harvestBlock(World world, EntityPlayer entityPlayer, int x, int y, int z, int metadata) {
		if(this.getRenderType() == 111 && (metadata & 15) > 0) {
			int itemID = Item.snowball.shiftedIndex;
			float f8 = 0.7F;
			double d9 = (double)(world.rand.nextFloat() * f8) + (double)(1.0F - f8) * 0.5D;
			double d11 = (double)(world.rand.nextFloat() * f8) + (double)(1.0F - f8) * 0.5D;
			double d13 = (double)(world.rand.nextFloat() * f8) + (double)(1.0F - f8) * 0.5D;
			EntityItem entityItem15 = new EntityItem(world, (double)x + d9, (double)y + d11, (double)z + d13, new ItemStack(itemID, 1, 0));
			entityItem15.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(entityItem15);
			world.setBlockWithNotify(x, y, z, 0);
			entityPlayer.addStat(StatList.mineBlockStatArray[this.blockID], 1);
		} 
		super.harvestBlock(world, entityPlayer, x, y, z, metadata);
	}
}
