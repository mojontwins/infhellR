package net.minecraft.game.container.creativetab;

import net.minecraft.game.item.Item;

final class CreativeTabBrewing extends CreativeTabs {
	CreativeTabBrewing(int par1, String par2Str) {
		super(par1, par2Str);
	}

	/**
	 * the itemID for the item to be displayed on the tab
	 */
	public int getTabIconItemIndex() {
		return Item.potionEmpty.shiftedIndex;
	}
}
