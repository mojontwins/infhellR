package net.minecraft.game.item;

public class ItemPiston extends ItemBlock {
	public ItemPiston(int i1) {
		super(i1);
	}

	public int getPlacedBlockMetadata(int i1) {
		return 7;
	}
}
