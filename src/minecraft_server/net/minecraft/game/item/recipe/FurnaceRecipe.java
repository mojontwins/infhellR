package net.minecraft.game.item.recipe;

import net.minecraft.game.item.ItemStack;

public class FurnaceRecipe {
	public final int inputId;
	public final ItemStack outputItemStack;
	
	public FurnaceRecipe(int inputId, ItemStack outputItemStack) {
		this.inputId = inputId;
		this.outputItemStack = outputItemStack;
	}

}
