package net.minecraft.game.trading;

import net.minecraft.game.entity.player.EntityPlayer;
public class NpcTrader implements ITrader {
	private EntityPlayer customer;
	private TradingRecipeList recipes;
	private String traderName;
	private Currency currency;
	
	public NpcTrader(String traderName, Currency currency) {
		this.traderName = traderName;
		this.currency = currency;
	}

	@Override
	public void setCustomer(EntityPlayer entityPlayer) {
		this.customer = entityPlayer;
	}

	@Override
	public EntityPlayer getCustomer() {
		return this.customer;
	}

	@Override
	public TradingRecipeList getRecipes(EntityPlayer entityPlayer) {
		return this.recipes;
	}

	@Override
	public void setRecipes(TradingRecipeList tradingRecipeList) {
		this.recipes = tradingRecipeList;
	}

	@Override
	public void useRecipe(TradingRecipe par1MerchantRecipe) {	
	}

	@Override
	public String getTraderName() {
		return this.traderName;
	}

	@Override
	public Currency getCurrency() {
		return this.currency;
	}

}
