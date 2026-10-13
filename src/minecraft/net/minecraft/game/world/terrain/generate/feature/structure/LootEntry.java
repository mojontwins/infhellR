package net.minecraft.game.world.terrain.generate.feature.structure;

import java.util.Random;

import net.minecraft.game.WeightedRandom;
import net.minecraft.game.WeightedRandomChoice;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.tileentity.TileEntityChest;

/** A weighted "treasure table" entry used to seed structure loot chests. */
public class LootEntry extends WeightedRandomChoice {
	public int itemId;
	public int metadata;
	public int minCount;
	public int maxCount;

	public LootEntry(int itemId, int metadata, int minCount, int maxCount, int weight) {
		super(weight);
		this.itemId = itemId;
		this.metadata = metadata;
		this.minCount = minCount;
		this.maxCount = maxCount;
	}

	/** Rolls {@code stacks} entries from {@code entries} and drops them into random chest slots. */
	public static void fillChest(Random rand, LootEntry[] entries, TileEntityChest chest, int stacks) {
		for(int i = 0; i < stacks; ++i) {
			LootEntry entry = (LootEntry)WeightedRandom.getRandomItem(rand, (WeightedRandomChoice[])entries);
			int count = entry.minCount + rand.nextInt(entry.maxCount - entry.minCount + 1);
			if(Item.itemsList[entry.itemId].getItemStackLimit() >= count) {
				chest.setInventorySlotContents(rand.nextInt(chest.getSizeInventory()), new ItemStack(entry.itemId, count, entry.metadata));
			} else {
				for(int j = 0; j < count; ++j) {
					chest.setInventorySlotContents(rand.nextInt(chest.getSizeInventory()), new ItemStack(entry.itemId, 1, entry.metadata));
				}
			}
		}
	}
}
