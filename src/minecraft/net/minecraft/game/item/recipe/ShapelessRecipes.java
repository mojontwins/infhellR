package net.minecraft.game.item.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.game.container.InventoryCrafting;
import net.minecraft.game.item.ItemStack;

public class ShapelessRecipes implements IRecipe {
	private final ItemStack recipeOutput;
	private final List<ItemStack> recipeItems;
	private boolean enabled = true;

	public ShapelessRecipes(ItemStack itemStack1, List<ItemStack> list2) {
		this.recipeOutput = itemStack1;
		this.recipeItems = list2;
	}

	public ItemStack getRecipeOutput() {
		return this.recipeOutput;
	}

	public boolean matches(InventoryCrafting inventoryCrafting1) {
		if (!this.enabled) return false; 
		
		ArrayList<ItemStack> arrayList2 = new ArrayList<ItemStack>(this.recipeItems);

		for(int i3 = 0; i3 < 3; ++i3) {
			for(int i4 = 0; i4 < 3; ++i4) {
				ItemStack itemStack5 = inventoryCrafting1.getStackInRowAndColumn(i4, i3);
				if(itemStack5 != null) {
					boolean z6 = false;
					Iterator<ItemStack> iterator7 = arrayList2.iterator();

					while(iterator7.hasNext()) {
						ItemStack itemStack8 = (ItemStack)iterator7.next();
						if(itemStack5.itemID == itemStack8.itemID && (itemStack8.getItemDamage() == -1 || itemStack5.getItemDamage() == itemStack8.getItemDamage())) {
							z6 = true;
							arrayList2.remove(itemStack8);
							break;
						}
					}

					if(!z6) {
						return false;
					}
				}
			}
		}

		return arrayList2.isEmpty();
	}

	public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting1) {
		return this.recipeOutput.copy();
	}

	public int getRecipeSize() {
		return this.recipeItems.size();
	}
	
	public ItemStack[] getRecipeItems() {
		ItemStack[] recipeItemsAsArray = new ItemStack[9];
		for (int i = 0; i < 9; i ++) recipeItemsAsArray [i] = null;
		for (int i = 0; i < this.recipeItems.size(); i ++) recipeItemsAsArray[i] = this.recipeItems.get(i);
		return recipeItemsAsArray;
	}
	
	public int getWidth() {
		return 3;
	}

	public int getHeight() {
		return 3;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
