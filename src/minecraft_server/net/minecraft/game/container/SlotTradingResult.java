package net.minecraft.game.container;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.trading.TradingRecipe;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.trading.InventoryTrading;

public class SlotTradingResult extends Slot {
	private final InventoryTrading InventoryTrading;
	private EntityPlayer thePlayer;
	private final ITrader theTrader;

	public SlotTradingResult(EntityPlayer par1EntityPlayer, ITrader entityTrader2,
			InventoryTrading par3InventoryMerchant, int par4, int par5, int par6) {
		super(par3InventoryMerchant, par4, par5, par6);
		this.thePlayer = par1EntityPlayer;
		this.theTrader = entityTrader2;
		this.InventoryTrading = par3InventoryMerchant;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the
	 * armor slots.
	 */
	public boolean isItemValid(ItemStack par1ItemStack) {
		return false;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the
	 * second int arg. Returns the new stack.
	 */
	public ItemStack decrStackSize(int par1) {
		return super.decrStackSize(par1);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
	 * ore and wood. Typically increases an internal count then calls
	 * onCrafting(item).
	 */
	protected void onCrafting(ItemStack par1ItemStack, int par2) {
		this.onCrafting(par1ItemStack);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
	 * ore and wood.
	 */
	protected void onCrafting(ItemStack par1ItemStack) {
		par1ItemStack.onCrafting(this.thePlayer.worldObj, this.thePlayer);
	}

	/**
	 * Called when the player picks up an item from an inventory slot
	 */
	public void onPickupFromSlot(ItemStack par1ItemStack) {
		this.onCrafting(par1ItemStack);
		TradingRecipe var2 = this.InventoryTrading.getCurrentRecipe();

		if (var2 != null) {
			ItemStack var3 = this.InventoryTrading.getStackInSlot(0);
			ItemStack var4 = this.InventoryTrading.getStackInSlot(1);

			if (this.matches(var2, var3, var4) || this.matches(var2, var4, var3)) {
				if (var3 != null && var3.stackSize <= 0) {
					var3 = null;
				}

				if (var4 != null && var4.stackSize <= 0) {
					var4 = null;
				}

				this.InventoryTrading.setInventorySlotContents(0, var3);
				this.InventoryTrading.setInventorySlotContents(1, var4);
				this.theTrader.useRecipe(var2);
			}
		}
	}

	private boolean matches(TradingRecipe recipe, ItemStack offers1, ItemStack offers2) {
		/*
		if(recipe.isCurrencyChange()) {
			
		} else {
			ItemStack item1 = recipe.getItemStack1();
			ItemStack item2 = recipe.getItemStack2();
	
			if (offers1 != null && offers1.itemID == item1.itemID) {
				if (item2 != null && offers2 != null && item2.itemID == offers2.itemID) {
					offers1.stackSize -= item1.stackSize;
					offers2.stackSize -= item2.stackSize;
					return true;
				}
	
				if (item2 == null && offers2 == null) {
					offers1.stackSize -= item1.stackSize;
					return true;
				}
			}
		}
			
		return false;
		*/
		ItemStack item1;
		ItemStack item2;
		
		// Just 1 offer, wrong slot
		if(offers1 == null && offers2 != null) {
			offers1 = offers2;
			offers2 = null;
		}
		
		if(recipe.isCurrencyChange()) {
			offers1.stackSize = 0;
		} else {
			item1 = recipe.getItemStack1();
			item2 = recipe.getItemStack2();

			// 2 offers for 2 items, wrong order
			if(
				offers1 != null && offers2 != null &&
				item1 != null && item2 != null &&
				offers1.itemID == item2.itemID &&
				offers2.itemID == item1.itemID 
			) {
				ItemStack aux = offers1;
				offers1 = offers2;
				offers2 = aux;
			}
			if(offers1 != null && item1 != null && offers1.itemID == item1.itemID) offers1.stackSize -= item1.stackSize;
			if(offers2 != null && item2 != null && offers2.itemID == item2.itemID) offers2.stackSize -= item2.stackSize;
		}
		
		return true;
	}
}
