package net.minecraft.game.entity;

import net.minecraft.game.container.IInventory;
import net.minecraft.game.container.InventoryMob;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

public class EntityArmoredMob extends EntityMob implements IArmoredMob {
	// Added a proper inventory to manage actual armor and stuff
	public InventoryMob inventory;
	protected int carryoverDamage = 0;
		
	public EntityArmoredMob(World var1) {
		super(var1);
		this.inventory = new InventoryMob(this, this.getSecondaryInventorySize()); 
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		NBTTagCompound var2 = var1.getCompoundTag("Inventory");
		this.inventory.readFromNBT(var2);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
		var1.setTag("Inventory", this.inventory.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public IInventory getIInventory() {
		return this.inventory;
	}
	
	public void setInventory(IInventory inventory) {
		this.inventory = (InventoryMob) inventory;
	}
	
	@Override
	protected int getTotalArmorValue() {
		return this.inventory.getTotalArmorValue();
	}
	
	public int getSecondaryInventorySize() {
		return 9;
	}

	public void setArmor(int type, ItemStack itemStack) {
		this.inventory.setArmorItemInSlot(3 - type, itemStack);
	}
	
	public ItemStack getArmor(int type) {
		return this.inventory.getArmorItemInSlot(3 - type);
	}
	
	@Override
	public ItemStack getHeldItem() {
		return this.inventory.getHeldItem();
	}
	
	@Override
	public boolean setHeldItem(ItemStack itemStack) {
		this.inventory.setHeldItem(itemStack);
		return true;
	}

}
