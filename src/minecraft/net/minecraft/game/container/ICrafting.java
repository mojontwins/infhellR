package net.minecraft.game.container;

import java.util.List;

import net.minecraft.game.item.ItemStack;

public interface ICrafting {
	void updateCraftingInventory(Container container1, List<ItemStack> list2);

	void updateCraftingInventorySlot(Container container1, int i2, ItemStack itemStack3);

	void updateCraftingInventoryInfo(Container container1, int i2, int i3);
}
