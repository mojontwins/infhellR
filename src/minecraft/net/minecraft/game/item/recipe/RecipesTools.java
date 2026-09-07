package net.minecraft.game.item.recipe;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;

public class RecipesTools {
	private String[][] recipePatterns = new String[][]{
		{"XXX", " # ", " # "}, 
		{"X", "#", "#"}, 
		{"XX", "X#", " #"}, 
		{"XX", " #", " #"},
		{"X", "#"},
		{" XX", " #X", "#  "},
		{"XXX", "X#X", " # "},
		{" XX", " XX", "#  "}
	};
	private Object[][] recipeItems = new Object[][]{
		{Block.planks, Block.cobblestone, Item.ingotIron, Item.diamond, Item.ingotGold}, 
		{Item.pickaxeWood, Item.pickaxeStone, Item.pickaxeSteel, Item.pickaxeDiamond, Item.pickaxeGold}, 
		{Item.shovelWood, Item.shovelStone, Item.shovelSteel, Item.shovelDiamond, Item.shovelGold}, 
		{Item.axeWood, Item.axeStone, Item.axeSteel, Item.axeDiamond, Item.axeGold}, 
		{Item.hoeWood, Item.hoeStone, Item.hoeSteel, Item.hoeDiamond, Item.hoeGold},
		{Item.knifeWood, Item.knifeStone, Item.knifeSteel, Item.knifeDiamond, Item.knifeGold},
		{Item.battleWood, Item.battleStone, Item.battleSteel, Item.battleDiamond, Item.battleGold},
		{null, null, Item.hammerSteel, Item.hammerDiamond, Item.hammerGold},
		{null, null, Item.maceSteel, Item.maceDiamond, Item.maceGold}	
	};
	
	public void addRecipes(CraftingManager craftingManager1) {
		for(int i2 = 0; i2 < this.recipeItems[0].length; ++i2) {
			Object object3 = this.recipeItems[0][i2];

			for(int i4 = 0; i4 < this.recipeItems.length - 1; ++i4) {
				Item item5 = (Item)this.recipeItems[i4 + 1][i2];
				if(item5 != null) craftingManager1.addRecipe(new ItemStack(item5), new Object[]{this.recipePatterns[i4], '#', Item.stick, 'X', object3});
			}
		}

		// Softlocked for b1.6.6
		craftingManager1.addRecipe(new ItemStack(Item.shears), new Object[]{" #", "# ", '#', Item.ingotIron});
	}
}
