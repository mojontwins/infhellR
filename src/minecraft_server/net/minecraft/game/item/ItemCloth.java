package net.minecraft.game.item;
import net.minecraft.game.world.block.BlockCloth;

import net.minecraft.game.world.block.Block;

public class ItemCloth extends ItemBlock {
	public ItemCloth(int i1) {
		super(i1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	public int getIconFromDamage(int i1) {
		return Block.cloth.blockIndexInTexture;
	}

	public int getPlacedBlockMetadata(int i1) {
		return i1;
	}

	public String getItemNameIS(ItemStack itemStack1) {
		return super.getItemName() + "." + ItemDye.dyeColorNames[BlockCloth.getBlockFromDye(itemStack1.getItemDamage())];
	}
}

