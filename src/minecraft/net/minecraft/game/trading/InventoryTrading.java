package net.minecraft.game.trading;

import net.minecraft.game.container.IInventory;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;

public class InventoryTrading implements IInventory {
	private final ITrader theMerchant;
	private final EntityPlayer thePlayer;
	private ItemStack[] theInventory = new ItemStack[3];
	
	public TradingManager tradingManager;
	private TradingRecipe currentRecipe;
	private int currentRecipeIndex;

	public InventoryTrading(EntityPlayer entityPlayer, ITrader entityTrader) {
		this.thePlayer = entityPlayer;
		this.theMerchant = entityTrader;
		this.tradingManager = new TradingManager(entityTrader.getRecipes(entityPlayer), entityTrader.getCurrency());
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	public int getSizeInventory() {
		return this.theInventory.length;
	}

	/**
	 * Returns the stack in slot i
	 */
	public ItemStack getStackInSlot(int par1) {
		return this.theInventory[par1];
	}

	/**
	 * Removes from an inventory slot (first arg) up to a specified number (second
	 * arg) of items and returns them in a new stack.
	 */
	public ItemStack decrStackSize(int slotId, int amount) {
		if (this.theInventory[slotId] != null) {
			ItemStack itemStack;

			if (slotId == 2) {
				// Taken from result
				
				itemStack = this.theInventory[slotId];
				this.theInventory[slotId] = null;
				return itemStack;
			} else if (this.theInventory[slotId].stackSize <= amount) {
				itemStack = this.theInventory[slotId];
				this.theInventory[slotId] = null;

				if (this.inventoryResetNeededOnSlotChange(slotId)) {
					this.resetRecipeAndSlots();
				}

				return itemStack;
			} else {
				itemStack = this.theInventory[slotId].splitStack(amount);

				if (this.theInventory[slotId].stackSize == 0) {
					this.theInventory[slotId] = null;
				}

				if (this.inventoryResetNeededOnSlotChange(slotId)) {
					this.resetRecipeAndSlots();
				}

				return itemStack;
			}
		} else {
			return null;
		}
	}

	/**
	 * if par1 slot has changed, does resetRecipeAndSlots need to be called?
	 */
	private boolean inventoryResetNeededOnSlotChange(int par1) {
		return par1 == 0 || par1 == 1;
	}

	/**
	 * When some containers are closed they call this on each slot, then drop
	 * whatever it returns as an EntityItem - like when you close a workbench GUI.
	 */
	public ItemStack getStackInSlotOnClosing(int par1) {
		if (this.theInventory[par1] != null) {
			ItemStack var2 = this.theInventory[par1];
			this.theInventory[par1] = null;
			return var2;
		} else {
			return null;
		}
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	public void setInventorySlotContents(int slotId, ItemStack itemStack) {
		this.theInventory[slotId] = itemStack;

		if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
			itemStack.stackSize = this.getInventoryStackLimit();
		}

		if (this.inventoryResetNeededOnSlotChange(slotId)) {
			this.resetRecipeAndSlots();
		}
	}

	/**
	 * Returns the name of the inventory.
	 */
	public String getInvName() {
		return "mob.villager";
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64,
	 * possibly will be extended. *Isn't this more of a set than a get?*
	 */
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes with
	 * Container
	 */
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer) {
		return this.theMerchant.getCustomer() == par1EntityPlayer;
	}

	public void openChest() {
	}

	public void closeChest() {
	}

	/**
	 * Called when an the contents of an Inventory change, usually
	 */
	public void onInventoryChanged() {
		this.resetRecipeAndSlots();
	}

	public void resetRecipeAndSlots() {
		this.currentRecipe = null;
		ItemStack offers1 = this.theInventory[0];
		ItemStack offers2 = this.theInventory[1];
	
		if (offers1 == null) {
			offers1 = offers2;
			offers2 = null;
		}

		if (offers1 == null) {
			this.setInventorySlotContents(2, (ItemStack) null);
		} else {
			TradingRecipeList tradingRecipeList = this.theMerchant.getRecipes(this.thePlayer);

			if (tradingRecipeList != null) {
				ItemStack recipeResult = this.tradingManager.findMatchingRecipe(offers1, offers2, this.currentRecipeIndex);
				this.setInventorySlotContents(2, recipeResult);
				this.currentRecipe = this.tradingManager.getRecipe(this.currentRecipeIndex);

			}
		}
	}

	public TradingRecipe getCurrentRecipe() {
		return this.currentRecipe;
	}

	public void setCurrentRecipeIndex(int par1) {
		this.currentRecipeIndex = par1;
		this.resetRecipeAndSlots();
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer1) {
		return true;
	}
}
