package net.minecraft.game.world.terrain.generate.twilightforest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;

public class TFTreasureTable {
	protected ArrayList<TFTreasureItem> list = new ArrayList<TFTreasureItem>();

	public void add(Item item, int quantity) {
		this.add((Item)item, quantity, 10);
	}

	public void add(Item item, int quantity, int rarity) {
		this.list.add(new TFTreasureItem(item, quantity, rarity));
	}

	public void add(Block block, int quantity) {
		this.add((Block)block, quantity, 10);
	}

	public void add(Block block, int quantity, int rarity) {
		this.list.add(new TFTreasureItem(block, quantity, rarity));
	}

	protected int total() {
		int value = 0;

		TFTreasureItem item;
		for(Iterator<TFTreasureItem> it = this.list.iterator(); it.hasNext(); value += item.getRarity()) {
			item = (TFTreasureItem)it.next();
		}

		return value;
	}

	public ItemStack getRandomItem(Random rand) {
		int value = rand.nextInt(this.total());

		TFTreasureItem item;
		for(Iterator<TFTreasureItem> it = this.list.iterator(); it.hasNext(); value -= item.getRarity()) {
			item = (TFTreasureItem)it.next();
			if(item.getRarity() > value) {
				return item.getItemStack(rand);
			}
		}

		return null;
	}
}
