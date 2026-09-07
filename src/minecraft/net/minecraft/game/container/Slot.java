package net.minecraft.game.container;

import net.minecraft.game.item.ItemStack;

public class Slot {
	public final int slotIndex;
	public final IInventory inventory;
	public int id;
	public int xDisplayPosition;
	public int yDisplayPosition;

	public Slot(IInventory iInventory1, int i2, int i3, int i4) {
		this.inventory = iInventory1;
		this.slotIndex = i2;
		this.xDisplayPosition = i3;
		this.yDisplayPosition = i4;
	}

	public void onSlotChange(ItemStack par1ItemStack, ItemStack par2ItemStack) {
		if (par1ItemStack != null && par2ItemStack != null) {
			if (par1ItemStack.itemID == par2ItemStack.itemID) {
				int var3 = par2ItemStack.stackSize - par1ItemStack.stackSize;

				if (var3 > 0) {
					this.onCrafting(par1ItemStack, var3);
				}
			}
		}
	}

	protected void onCrafting(ItemStack par1ItemStack, int par2) {
	}

	protected void onCrafting(ItemStack par1ItemStack) {
	}

	public void onPickupFromSlot(ItemStack itemStack1) {
		this.onSlotChanged();
	}

	public boolean isItemValid(ItemStack itemStack1) {
		return true;
	}

	public ItemStack getStack() {
		return this.getInventory().getStackInSlot(this.slotIndex);
	}

	public boolean getHasStack() {
		return this.getStack() != null;
	}

	public void putStack(ItemStack itemStack1) {
		this.getInventory().setInventorySlotContents(this.slotIndex, itemStack1);
		this.onSlotChanged();
	}

	public void onSlotChanged() {
		this.getInventory().onInventoryChanged();
	}

	public int getSlotStackLimit() {
		return this.getInventory().getInventoryStackLimit();
	}

	public int getBackgroundIconIndex() {
		return -1;
	}

	public ItemStack decrStackSize(int i1) {
		return this.getInventory().decrStackSize(this.slotIndex, i1);
	}

	public boolean isSlotInInventory(IInventory iInventory1, int i2) {
		return iInventory1 == this.getInventory() && i2 == this.slotIndex;
	}

	public IInventory getInventory() {
		return inventory;
	}

}
