package net.minecraft.game.trading;

import net.minecraft.game.item.ItemStack;

public class TradingManager {
	private TradingRecipeList tradingRecipeList;
	private Currency currency;
	
	public TradingManager(TradingRecipeList tradingRecipeList, Currency currency) {
		this.tradingRecipeList = tradingRecipeList;
		this.currency = currency;
	}
	
	public TradingRecipe getRecipe(int recipeIndex) {
		return this.tradingRecipeList.get(recipeIndex);
	}
	
	public ItemStack findMatchingRecipe(ItemStack offers1, ItemStack offers2, int currentRecipeIndex) {
		TradingRecipe tradingRecipe = this.tradingRecipeList.get(currentRecipeIndex);
		return this.findMatchingRecipe(offers1, offers2, tradingRecipe);
	}
	
	public ItemStack performCurrencyChange(ItemStack offers, TradingRecipe tradingRecipe) {
		int resultAmount = (int)Math.floor((double)(offers.stackSize) * tradingRecipe.getChangeFactor());
		return resultAmount <= 0 ? null : new ItemStack(Currency.getOtherCurrency(this.currency).getItem(), resultAmount);
	}
		
	public ItemStack findMatchingRecipe(ItemStack offers1, ItemStack offers2, TradingRecipe tradingRecipe) {
		if(tradingRecipe.isCurrencyChange() && offers1 != null) {
			if(offers1.getItem().shiftedIndex == this.currency.getItem().shiftedIndex) {
				return this.performCurrencyChange(offers1, tradingRecipe);
			}
		} else {	
			if(
				(
					ItemStack.areItemStacksCompatibleForTrading(tradingRecipe.getItemStack1(), offers1) && 
					ItemStack.areItemStacksCompatibleForTrading(tradingRecipe.getItemStack2(), offers2)
				) ||
				(
					ItemStack.areItemStacksCompatibleForTrading(tradingRecipe.getItemStack2(), offers1) &&
					ItemStack.areItemStacksCompatibleForTrading(tradingRecipe.getItemStack1(), offers2)
				)
			) {
				return tradingRecipe.getItemStackResult().copy();
			}
		}
		
		return null;
	}
	
	public Currency getCurrency(ItemStack itemStack) {
		if(itemStack == null) return null;
		return Currency.getCurrencyFromItem(itemStack.getItem());
	}
	
	public ItemStack makeCurrencyChange(InventoryTrading inventoryTrading, double factor) {
		int amountIn = inventoryTrading.getStackInSlot(0).stackSize + inventoryTrading.getStackInSlot(1).stackSize;
		
		Currency currencyIn = this.getCurrency(inventoryTrading.getStackInSlot(0));
		if(this.getCurrency(inventoryTrading.getStackInSlot(1)) != currencyIn) amountIn = 0;
		if(currencyIn == null) amountIn = 0;
		
		Currency currencyOut = Currency.getOtherCurrency(currencyIn);
		
		if(currencyOut != null) {
			return new ItemStack(currencyOut.getItem(), (int)(Math.floor((double)amountIn * factor)));
		} else {
			return null;
		}
	}
}
