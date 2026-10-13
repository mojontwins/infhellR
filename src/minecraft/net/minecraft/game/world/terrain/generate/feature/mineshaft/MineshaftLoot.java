package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import net.minecraft.game.item.Item;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.structure.LootEntry;

public final class MineshaftLoot {
	private MineshaftLoot() {
	}

	public static final LootEntry[] ENTRIES = new LootEntry[] {
		new LootEntry(Item.ingotIron.shiftedIndex, 0, 1, 5, 10),
		new LootEntry(Item.ingotGold.shiftedIndex, 0, 1, 3, 5),
		new LootEntry(Item.redstone.shiftedIndex, 0, 4, 9, 5),
		new LootEntry(Item.dyePowder.shiftedIndex, 4, 4, 9, 5),
		new LootEntry(Item.diamond.shiftedIndex, 0, 1, 2, 3),
		new LootEntry(Item.nametagSimple.shiftedIndex, 0, 1, 2, 3),
		new LootEntry(Item.coal.shiftedIndex, 0, 3, 8, 10),
		new LootEntry(Item.bread.shiftedIndex, 0, 1, 3, 15),
		new LootEntry(Item.pickaxeSteel.shiftedIndex, 0, 1, 1, 1),
		new LootEntry(Block.rail.blockID, 0, 4, 8, 1),
		new LootEntry(Item.seeds.shiftedIndex, 0, 2, 4, 10),
	};
}
