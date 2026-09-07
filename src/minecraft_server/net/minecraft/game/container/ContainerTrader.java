package net.minecraft.game.container;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.trading.InventoryTrading;

public class ContainerTrader extends Container {
	private ITrader entityTrader;
	private InventoryTrading inventoryTrader;
	private final World world;

	public ContainerTrader(InventoryPlayer par1InventoryPlayer, ITrader entityTrader2, World par3World) {
		this.entityTrader = entityTrader2;
		this.world = par3World;
		this.inventoryTrader = new InventoryTrading(par1InventoryPlayer.player, entityTrader2);
		this.addSlotToContainer(new Slot(this.inventoryTrader, 0, 36, 53));
		this.addSlotToContainer(new Slot(this.inventoryTrader, 1, 62, 53));
		this.addSlotToContainer(
				new SlotTradingResult(par1InventoryPlayer.player, entityTrader2, this.inventoryTrader, 2, 120, 53));
		int i;

		for (i = 0; i < 3; ++i) {
			for (int var5 = 0; var5 < 9; ++var5) {
				this.addSlotToContainer(
						new Slot(par1InventoryPlayer, var5 + i * 9 + 9, 8 + var5 * 18, 84 + i * 18));
			}
		}

		for (i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(par1InventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	public InventoryTrading getinventoryTrader() {
		return this.inventoryTrader;
	}

	/**
	 * Updates crafting matrix; called from onCraftMatrixChanged. Args: none
	 */
	public void updateCraftingResults() {
		super.updateCraftingResults();
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	public void onCraftMatrixChanged(IInventory par1IInventory) {
		this.inventoryTrader.resetRecipeAndSlots();
		super.onCraftMatrixChanged(par1IInventory);
	}

	public void setCurrentRecipeIndex(int par1) {
		this.inventoryTrader.setCurrentRecipeIndex(par1);
	}

	public void updateProgressBar(int par1, int par2) {
	}

	public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
		return this.entityTrader.getCustomer() == par1EntityPlayer;
	}

	/**
	 * Called to transfer a stack from one inventory to the other eg. when shift
	 * clicking.
	 */
	public ItemStack transferStackInSlot(int par1) {
		ItemStack var2 = null;
		Slot var3 = (Slot) this.inventorySlots.get(par1);

		if (var3 != null && var3.getHasStack()) {
			ItemStack var4 = var3.getStack();
			var2 = var4.copy();

			if (par1 == 2) {
				if (!this.mergeItemStack(var4, 3, 39, true)) {
					return null;
				}

				var3.onSlotChange(var4, var2);
			} else if (par1 != 0 && par1 != 1) {
				if (par1 >= 3 && par1 < 30) {
					if (!this.mergeItemStack(var4, 30, 39, false)) {
						return null;
					}
				} else if (par1 >= 30 && par1 < 39 && !this.mergeItemStack(var4, 3, 30, false)) {
					return null;
				}
			} else if (!this.mergeItemStack(var4, 3, 39, false)) {
				return null;
			}

			if (var4.stackSize == 0) {
				var3.putStack((ItemStack) null);
			} else {
				var3.onSlotChanged();
			}

			if (var4.stackSize == var2.stackSize) {
				return null;
			}

			var3.onPickupFromSlot(var4);
		}

		return var2;
	}

	/**
	 * Callback for when the crafting gui is closed.
	 */
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer) {
		super.onCraftGuiClosed(par1EntityPlayer);
		this.entityTrader.setCustomer((EntityPlayer) null);
		super.onCraftGuiClosed(par1EntityPlayer);

		if (!this.world.isRemote) {
			ItemStack var2 = this.inventoryTrader.getStackInSlotOnClosing(0);

			if (var2 != null) {
				par1EntityPlayer.dropPlayerItem(var2);
			}

			var2 = this.inventoryTrader.getStackInSlotOnClosing(1);

			if (var2 != null) {
				par1EntityPlayer.dropPlayerItem(var2);
			}
		}
	}
}
