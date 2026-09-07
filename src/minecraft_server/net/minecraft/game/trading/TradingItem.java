package net.minecraft.game.trading;

import net.minecraft.game.item.ItemStack;

public class TradingItem {
	public ItemStack itemStack = null;
	public int amount; 
	
	public TradingItem(Currency currency, int amount) {
		this.amount = amount;
	}
	
	public TradingItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public boolean isCurrency() {
		return this.itemStack == null;
	}
	
	public ItemStack getItemStack(Currency currency) {
		if(this.isCurrency()) {
			return new ItemStack(currency.getItem(), this.amount);
		} else return this.itemStack;
	}
	
	public String toString() {
		return "TradingItem " + this.itemStack + " [" + this.amount + "]";
	}
}
