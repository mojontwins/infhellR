package net.minecraft.game.trading;

import net.minecraft.game.entity.player.EntityPlayer;
public interface ITrader {
	public void setCustomer(EntityPlayer entityPlayer);
	
	public EntityPlayer getCustomer();
	
	public TradingRecipeList getRecipes(EntityPlayer entityPlayer);
	
	public void setRecipes(TradingRecipeList tradingRecipeList);
	
	public void useRecipe(TradingRecipe par1MerchantRecipe);

	public String getTraderName();

	public Currency getCurrency();
}
