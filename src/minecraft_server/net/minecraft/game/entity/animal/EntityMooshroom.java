package net.minecraft.game.entity.animal;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.item.ItemSword;

public class EntityMooshroom extends EntityCow {
	public EntityMooshroom(World par1World) {
		super(par1World);
		this.texture = "/mob/redcow.png";
		this.setSize(0.9F, 1.3F);
	}

	public boolean interact(EntityPlayer par1EntityPlayer) {
		ItemStack itemstack = par1EntityPlayer.inventory.getCurrentItem();

		if (itemstack != null && itemstack.itemID == Item.bowlEmpty.shiftedIndex) {
			if (itemstack.stackSize == 1) {
				par1EntityPlayer.inventory.setInventorySlotContents(par1EntityPlayer.inventory.currentItem, new ItemStack(Item.bowlSoup));
				return true;
			}

			if (par1EntityPlayer.inventory.addItemStackToInventory(new ItemStack(Item.bowlSoup)) && !par1EntityPlayer.isCreative) {
				par1EntityPlayer.inventory.decrStackSize(par1EntityPlayer.inventory.currentItem, 1);
				return true;
			}
		}

		if (itemstack != null && itemstack.getItem() instanceof ItemSword) {
			this.setEntityDead();
			this.worldObj.spawnParticle("largeexplode", this.posX, this.posY + (double)(this.height / 2.0F), this.posZ, 0.0D, 0.0D, 0.0D);

			if (!this.worldObj.isRemote) {
				EntityCow entitycow = new EntityCow(this.worldObj);
				entitycow.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
				entitycow.health = this.health;
				entitycow.renderYawOffset = this.renderYawOffset;
				this.worldObj.spawnEntityInWorld(entitycow);

				for (int i = 0; i < 5; i++) {
					this.worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, this.posX, this.posY + (double)this.height, this.posZ, new ItemStack(Block.mushroomRed)));
				}
			}

			return true;
		} else {
			return super.interact(par1EntityPlayer);
		}
	}
}
