package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;

public class TFTreasureItem {
	int itemID;
	int itemDamage;
	int quantity;
	int rarity;

	public TFTreasureItem(Item item) {
		this((Item)item, 1, 10);
	}

	public TFTreasureItem(Item item, int quantity) {
		this((Item)item, quantity, 10);
	}

	public TFTreasureItem(Item item, int quantity, int rarity) {
		this.itemID = item.shiftedIndex;
		this.itemDamage = 0;
		this.quantity = quantity;
		this.rarity = rarity;
	}

	public TFTreasureItem(Block block, int quantity, int rarity) {
		this.itemID = block.blockID;
		this.itemDamage = 0;
		this.quantity = quantity;
		this.rarity = rarity;
	}

	public TFTreasureItem(ItemStack itemStack, int rarity) {
		this.itemID = itemStack.itemID;
		this.itemDamage = itemStack.getItemDamage();
		this.quantity = itemStack.stackSize;
		this.rarity = rarity;
	}

	public ItemStack getItemStack(Random rand) {
		return new ItemStack(this.itemID, rand.nextInt(this.quantity) + 1, this.itemDamage);
	}

	public int getRarity() {
		return this.rarity;
	}
}
