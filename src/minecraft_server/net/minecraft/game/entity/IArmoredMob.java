package net.minecraft.game.entity;

import net.minecraft.game.item.ItemStack;

public interface IArmoredMob {
	
	public void setArmor(int type, ItemStack itemStack);
	
	public ItemStack getArmor(int type);
	
}
