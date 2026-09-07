package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.misc.EntityTNTPrimed;

public class BlockTNT extends Block {
	public BlockTNT(int id, int blockIndex) {
		super(id, blockIndex, Material.tnt);
		this.displayOnCreativeTab = CreativeTabs.tabRedstone;
	}

	public int getBlockTextureFromSide(int side) {
		return side == 0 ? this.blockIndexInTexture + 2 : (side == 1 ? this.blockIndexInTexture + 1 : this.blockIndexInTexture);
	}

	public void onBlockAdded(World world1, int i2, int i3, int i4) {
		super.onBlockAdded(world1, i2, i3, i4);
		if(world1.isBlockIndirectlyGettingPowered(i2, i3, i4)) {
			this.onBlockDestroyedByPlayer(world1, i2, i3, i4, 1);
			world1.setBlockWithNotify(i2, i3, i4, 0);
		}

	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		if(id > 0 && Block.blocksList[id].canProvidePower() && world.isBlockIndirectlyGettingPowered(x, y, z)) {
			this.onBlockDestroyedByPlayer(world, x, y, z, 0);
			world.setBlockWithNotify(x, y, z, 0);
		}

	}

	public int quantityDropped(Random rand) {
		return 0;
	}

	public void onBlockDestroyedByExplosion(World world, int x, int y, int z) {
		EntityTNTPrimed entityTNTPrimed5 = new EntityTNTPrimed(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
		entityTNTPrimed5.fuse = world.rand.nextInt(entityTNTPrimed5.fuse / 4) + entityTNTPrimed5.fuse / 8;
		world.spawnEntityInWorld(entityTNTPrimed5);
	}

	public void onBlockDestroyedByPlayer(World world1, int i2, int i3, int i4, int i5) {
		if(!world1.isRemote) {
			if((i5 & 1) == 0) {
				this.dropBlockAsItem_do(world1, i2, i3, i4, new ItemStack(Block.tnt.blockID, 1, 0));
			} else {
				EntityTNTPrimed entityTNTPrimed6 = new EntityTNTPrimed(world1, (double)((float)i2 + 0.5F), (double)((float)i3 + 0.5F), (double)((float)i4 + 0.5F));
				world1.spawnEntityInWorld(entityTNTPrimed6);
				world1.playSoundAtEntity(entityTNTPrimed6, "random.fuse", 1.0F, 1.0F);
			}

		}
	}

	public void onBlockClicked(World world1, int i2, int i3, int i4, EntityPlayer entityPlayer5) {
		if(entityPlayer5.getCurrentEquippedItem() != null && entityPlayer5.getCurrentEquippedItem().itemID == Item.flintAndSteel.shiftedIndex) {
			world1.setBlockMetadata(i2, i3, i4, 1);
		}

		super.onBlockClicked(world1, i2, i3, i4, entityPlayer5);
	}

	public boolean blockActivated(World world1, int i2, int i3, int i4, EntityPlayer entityPlayer5) {
		return super.blockActivated(world1, i2, i3, i4, entityPlayer5);
	}
}
