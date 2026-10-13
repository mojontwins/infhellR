package net.minecraft.game.world.terrain.generate.feature.stronghold;

import net.minecraft.game.item.Item;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.structure.LootEntry;

/** Loot tables for the three kinds of stronghold chest. */
public final class StrongholdLoot {
	public static final LootEntry[] CHEST_CORRIDOR = new LootEntry[] {
		new LootEntry(Block.cryingObsidian.blockID, 0, 1, 1, 10),
		new LootEntry(Item.diamond.shiftedIndex, 0, 1, 3, 3),
		new LootEntry(Item.nametagSimple.shiftedIndex, 0, 1, 1, 3),
		new LootEntry(Item.ingotIron.shiftedIndex, 0, 1, 5, 10),
		new LootEntry(Item.ingotGold.shiftedIndex, 0, 1, 3, 5),
		new LootEntry(Item.redstone.shiftedIndex, 0, 4, 9, 5),
		new LootEntry(Item.bread.shiftedIndex, 0, 1, 3, 15),
		new LootEntry(Item.appleRed.shiftedIndex, 0, 1, 3, 15),
		new LootEntry(Item.pickaxeSteel.shiftedIndex, 0, 1, 1, 5),
		new LootEntry(Item.swordSteel.shiftedIndex, 0, 1, 1, 5),
		new LootEntry(Item.plateSteel.shiftedIndex, 0, 1, 1, 5),
		new LootEntry(Item.helmetSteel.shiftedIndex, 0, 1, 1, 5),
		new LootEntry(Item.legsSteel.shiftedIndex, 0, 1, 1, 5),
		new LootEntry(Item.bootsSteel.shiftedIndex, 0, 1, 1, 5),
		new LootEntry(Item.appleGold.shiftedIndex, 0, 1, 1, 1)
	};

	public static final LootEntry[] ROOM_CROSSING = new LootEntry[] {
		new LootEntry(Item.ingotIron.shiftedIndex, 0, 1, 5, 10),
		new LootEntry(Item.ingotGold.shiftedIndex, 0, 1, 3, 5),
		new LootEntry(Item.redstone.shiftedIndex, 0, 4, 9, 5),
		new LootEntry(Item.coal.shiftedIndex, 0, 3, 8, 10),
		new LootEntry(Item.bread.shiftedIndex, 0, 1, 3, 15),
		new LootEntry(Item.appleRed.shiftedIndex, 0, 1, 3, 15),
		new LootEntry(Item.pickaxeSteel.shiftedIndex, 0, 1, 1, 1)
	};

	public static final LootEntry[] LIBRARY = new LootEntry[] {
		new LootEntry(Item.book.shiftedIndex, 0, 1, 3, 20),
		new LootEntry(Item.paper.shiftedIndex, 0, 2, 7, 20),
		new LootEntry(Block.cryingObsidian.blockID, 0, 1, 1, 1),
		new LootEntry(Item.compass.shiftedIndex, 0, 1, 1, 1)
	};

	private StrongholdLoot() {
	}
}
