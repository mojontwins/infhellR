package net.minecraft.game.container;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;

public interface IInventory {
	int getSizeInventory();

	ItemStack getStackInSlot(int i1);

	ItemStack decrStackSize(int i1, int i2);

	void setInventorySlotContents(int i1, ItemStack itemStack2);

	String getInvName();

	int getInventoryStackLimit();

	void onInventoryChanged();

	boolean canInteractWith(EntityPlayer entityPlayer1);
}
