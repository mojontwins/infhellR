package net.minecraft.game.trading;

public class TradingRecipePreset {
	private TradingItem wants1;
	private TradingItem wants2;
	private TradingItem offers;
	private int minStock;
	private int maxStock;
	private float chance;
	
	public TradingRecipePreset(TradingItem wants1, TradingItem wants2, TradingItem offers, int minStock, int maxStock, float chance) {		
		this.wants1 = wants1;
		this.wants2 = wants2;
		this.offers = offers;
		this.minStock = minStock;
		this.maxStock = maxStock;
		this.chance = chance;
	}
	
	public String toString() {
		return "TradingRecipePreset W1:" + this.wants1 + " W2:" + this.wants2 + " O:" + this.offers + " Stock:" + this.minStock + "-" + this.maxStock + " chance: " + this.chance;
	}

	public int getMinStock() {
		return minStock;
	}

	public void setMinStock(int minStock) {
		this.minStock = minStock;
	}

	public int getMaxStock() {
		return maxStock;
	}

	public void setMaxStock(int maxStock) {
		this.maxStock = maxStock;
	}

	public float getChance() {
		return chance;
	}

	public void setChance(float chance) {
		this.chance = chance;
	}

	public TradingItem getWants1() {
		return wants1;
	}

	public void setWants1(TradingItem wants1) {
		this.wants1 = wants1;
	}

	public TradingItem getWants2() {
		return wants2;
	}

	public void setWants2(TradingItem wants2) {
		this.wants2 = wants2;
	}

	public TradingItem getOffers() {
		return offers;
	}

	public void setOffers(TradingItem offers) {
		this.offers = offers;
	}
}
