package net.minecraft.client.gui;

import java.util.Comparator;

import net.minecraft.game.item.recipe.IRecipe;

public class RecipeSorterForGUI implements Comparator<Object> {
	public int compareRecipesForGUI(IRecipe recipe1, IRecipe recipe2) {
		if(recipe2.getRecipeOutput().itemID > recipe1.getRecipeOutput().itemID) return -1;
		if(recipe2.getRecipeOutput().itemID < recipe1.getRecipeOutput().itemID) return 1;
		if(recipe2.getRecipeOutput().itemDamage > recipe1.getRecipeOutput().itemDamage) return -1;
		if(recipe2.getRecipeOutput().itemDamage < recipe1.getRecipeOutput().itemDamage) return 1;
		return 0;
	}
	
	public int compare(Object o1, Object o2) {
		return this.compareRecipesForGUI((IRecipe)o1, (IRecipe)o2);
	}
}
