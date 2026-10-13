package net.minecraft.game.world.terrain.generate.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;

public class TreasureTable {
	protected ArrayList<TreasureItem> list = new ArrayList<TreasureItem>();

	public void add(Item item, int quantity) {
		this.add((Item)item, quantity, 10);
	}

	public void add(Item item, int quantity, int rarity) {
		this.list.add(new TreasureItem(item, quantity, rarity));
	}

	public void add(Block block, int quantity) {
		this.add((Block)block, quantity, 10);
	}

	public void add(Block block, int quantity, int rarity) {
		this.list.add(new TreasureItem(block, quantity, rarity));
	}

	protected int total() {
		int value = 0;

		TreasureItem item;
		for(Iterator<TreasureItem> it = this.list.iterator(); it.hasNext(); value += item.getRarity()) {
			item = (TreasureItem)it.next();
		}

		return value;
	}

	public ItemStack getRandomItem(Random rand) {
		int value = rand.nextInt(this.total());

		TreasureItem item;
		for(Iterator<TreasureItem> it = this.list.iterator(); it.hasNext(); value -= item.getRarity()) {
			item = (TreasureItem)it.next();
			if(item.getRarity() > value) {
				return item.getItemStack(rand);
			}
		}

		return null;
	}
}
