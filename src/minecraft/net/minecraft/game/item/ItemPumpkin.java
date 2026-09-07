package net.minecraft.game.item;

public class ItemPumpkin extends ItemBlock {
	public ItemPumpkin (int var1) {
		super(var1);
	}

	public boolean canUseAsHelmet() {
		return true;
	}
	
	public int getPlacedBlockMetadata(int var1) {
		return var1;
	}	
}
