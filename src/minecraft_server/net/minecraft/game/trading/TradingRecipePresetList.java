package net.minecraft.game.trading;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;

public class TradingRecipePresetList {
	// TODO :: Replace this by enum and do it properly!
	public static final int TRADER_TYPES = 3;
	
	public static final int TRADER_FARMER = 0;
	public static final int TRADER_BUTCHER = 1;
	public static final int TRADER_BLACKSMITH = 2;
	
	public static final String[] traderName = new String [] { "Farmer", "Butcher", "Blacksmith" };
	
	@SuppressWarnings("unchecked")
	private static ArrayList<TradingRecipePreset>[] tradingRecipeList = new ArrayList[3];
	
	public static TradingRecipeList getNewTradingRecipeList(Random rand, boolean specialTrader, Currency currency, int traderType) {
		TradingRecipeList tradingRecipeList = new TradingRecipeList();
		
		int tradingItems = 2 + rand.nextInt(3);
		for(int i = 0; i < tradingItems; i ++) {
			TradingRecipe tradingRecipe = TradingRecipePresetList.pickUpTradingRecipe(rand, currency, traderType);
			
			boolean unique = true;
			for(int j = 0; j < tradingRecipeList.size(); j ++) {
				if(tradingRecipeList.get(j).isTradingRecipeEquals(tradingRecipe)) {
					unique = false; break;
				}
			}
			
			if(unique) tradingRecipeList.add(tradingRecipe);
		}
		
		tradingRecipeList.add(new TradingRecipe(0.5F + 0.5F * rand.nextFloat()));
		return tradingRecipeList;
	}
	
	public static TradingRecipe pickUpTradingRecipe(Random rand, Currency currency, int traderType) {
		TradingRecipe tradingRecipe = null;
		while(tradingRecipe == null) {
			TradingRecipePreset tradingRecipePreset = tradingRecipeList[traderType].get(rand.nextInt(tradingRecipeList[traderType].size()));
			if(tradingRecipePreset.getChance() >= rand.nextFloat()) {
				ItemStack itemStack1 = getItemStackFromTradingItem(tradingRecipePreset.getWants1(), currency);
				ItemStack itemStack2 = getItemStackFromTradingItem(tradingRecipePreset.getWants2(), currency);
				ItemStack itemStackResult = getItemStackFromTradingItem(tradingRecipePreset.getOffers(), currency);
				
				tradingRecipe = new TradingRecipe(itemStack1, itemStack2, itemStackResult, tradingRecipePreset.getMinStock() + rand.nextInt(tradingRecipePreset.getMaxStock() - tradingRecipePreset.getMinStock() + 1));
			}
		}
		
		return tradingRecipe;
	}
	
	public static ItemStack getItemStackFromTradingItem(TradingItem tradingItem, Currency currency) {
		if(tradingItem == null) return null;
		return tradingItem.getItemStack(currency);
	}
	
	static {
		for(int i = 0; i < TRADER_TYPES; i ++) {
			tradingRecipeList[i] = new ArrayList<TradingRecipePreset>();
		}
		
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 2), null, new TradingItem(new ItemStack(Item.porkCooked)), 5, 10, 0.7F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 2), null, new TradingItem(new ItemStack(Item.helmetLeather)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 4), null, new TradingItem(new ItemStack(Item.plateLeather)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 3), null, new TradingItem(new ItemStack(Item.legsLeather)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 2), null, new TradingItem(new ItemStack(Item.bootsLeather)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Item.porkRaw)), null, new TradingItem(Currency.currencyGeneral, 1), 5, 10, 1.0F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Item.coal, 15)), null, new TradingItem(Currency.currencyGeneral, 1), 5, 10, 1.0F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Item.ingotGold, 8)), null, new TradingItem(Currency.currencyGeneral, 1), 5, 10, 1.0F));
		tradingRecipeList[TRADER_BUTCHER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Block.cryingObsidian)), null, new TradingItem(Currency.currencyGeneral, 20), 5, 10, 0.3F));
		
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 1), null, new TradingItem(new ItemStack(Item.bread)), 5, 10, 1.0F));
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 1), null, new TradingItem(new ItemStack(Item.appleRed)), 5, 10, 0.7F));
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 1), null, new TradingItem(new ItemStack(Item.arrow, 10)), 5, 10, 1.0F));
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 3), null, new TradingItem(new ItemStack(Item.flintAndSteel)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 1), new TradingItem(new ItemStack(Block.gravel, 10)), new TradingItem(new ItemStack(Item.flint)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Item.wheat, 20)), null, new TradingItem(Currency.currencyGeneral, 1), 5, 10, 1.0F));
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 1), null, new TradingItem(new ItemStack(Item.pumpkinPie, 4)), 5, 10, 0.5F));
		for(int i = 1; i < 16; i ++) {
			tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Block.cloth, 2, i)), null, new TradingItem(Currency.currencyGeneral, 1), 1, 3, 1.0F));
		}
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Block.cryingObsidian)), null, new TradingItem(Currency.currencyGeneral, 20), 5, 10, 0.3F));
		tradingRecipeList[TRADER_FARMER].add(new TradingRecipePreset(new TradingItem(new ItemStack(Block.cryingObsidian)), null, new TradingItem(Currency.currencyGeneral, 20), 5, 10, 0.3F));
		
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(new ItemStack(Item.diamond)), null, new TradingItem(Currency.currencyGeneral, 5), 5, 10, 0.5F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(new ItemStack(Item.ingotIron, 5)), null, new TradingItem(Currency.currencyGeneral, 1), 5, 10, 0.5F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 7), null, new TradingItem(new ItemStack(Item.helmetDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 16), null, new TradingItem(new ItemStack(Item.plateDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 13), null, new TradingItem(new ItemStack(Item.legsDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 7), null, new TradingItem(new ItemStack(Item.bootsDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 12), null, new TradingItem(new ItemStack(Item.swordDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 15), null, new TradingItem(new ItemStack(Item.pickaxeDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 15), null, new TradingItem(new ItemStack(Item.axeDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 10), null, new TradingItem(new ItemStack(Item.shovelDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 10), null, new TradingItem(new ItemStack(Item.hoeDiamond)), 1, 3, 0.3F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 12), null, new TradingItem(new ItemStack(Item.swordSteel)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 15), null, new TradingItem(new ItemStack(Item.pickaxeSteel)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 15), null, new TradingItem(new ItemStack(Item.axeSteel)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 10), null, new TradingItem(new ItemStack(Item.shovelSteel)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(Currency.currencyGeneral, 10), null, new TradingItem(new ItemStack(Item.hoeSteel)), 1, 3, 0.5F));
		tradingRecipeList[TRADER_BLACKSMITH].add(new TradingRecipePreset(new TradingItem(new ItemStack(Block.cryingObsidian)), null, new TradingItem(Currency.currencyGeneral, 20), 5, 10, 0.5F));

	}
}
