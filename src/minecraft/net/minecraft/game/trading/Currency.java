package net.minecraft.game.trading;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;

public class Currency {
	public static final Currency currencyGeneral = new Currency().setName("wildcard");
	public static final Currency currencyRuby = new Currency().setItem(Item.ruby).setName("Ruby");
	public static final Currency currencyEmerald = new Currency().setItem(Item.emerald).setName("Emerald");
	
	public static final Currency validCurrencies[] = new Currency[] { currencyRuby, currencyEmerald };
	
	private String name;
	private Item item = null;
	
	public Currency() {
	}

	public Currency setName(String name) {
		this.name = name;
		return this;
	}
	
	public static Currency getOtherCurrency(Currency currency) {
		if(currency == currencyRuby) return currencyEmerald;
		if(currency == currencyEmerald) return currencyRuby;
		return currency;
	}
	
	public static Currency getCurrencyFromItem(Item item) {
		for(int i = 0; i < validCurrencies.length; i ++) {
			if(item == validCurrencies[i].getItem()) return validCurrencies [i];
		}
		return null;
	}
	
	public static ItemStack getItemStack(Currency currency) {
		return new ItemStack(currency.getItem());
	}
	
	public String getName() {
		return this.name;
	}
	
	public Item getItem() {
		return this.item;
	}
	
	public Currency setItem(Item item) {
		this.item = item;
		return this;
	}
}
