package net.minecraft.game.item.recipe;

import java.util.Comparator;

public class FurnaceRecipeSorterForGUI implements Comparator<Object> {

	@Override
	public int compare(Object o1, Object o2) {
		return this.compareRecipesForGUI((FurnaceRecipe)o1, (FurnaceRecipe)o2);
	}

	private int compareRecipesForGUI(FurnaceRecipe furnaceRecipe1, FurnaceRecipe furnaceRecipe2) {
		return Integer.signum(furnaceRecipe1.outputItemStack.itemID - furnaceRecipe2.outputItemStack.itemID);
	}

}
