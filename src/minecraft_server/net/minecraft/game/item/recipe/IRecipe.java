package net.minecraft.game.item.recipe;

import net.minecraft.game.container.InventoryCrafting;
import net.minecraft.game.item.ItemStack;

public interface IRecipe {
	public boolean matches(InventoryCrafting i1);

	public ItemStack getCraftingResult(InventoryCrafting i1);

	public int getRecipeSize();

	public ItemStack getRecipeOutput();
	
	public ItemStack[] getRecipeItems();
	
	public int getWidth();

	public int getHeight();
	
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
}
