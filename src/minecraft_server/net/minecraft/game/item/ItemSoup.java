package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;

public class ItemSoup extends ItemFood {
	public ItemSoup(int i1, int i2) { 
		super(i1, i2, false, true);
	}

	/*
	public ItemStack onItemRightClick(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
		super.onItemRightClick(itemStack1, world2, entityPlayer3);
		return new ItemStack(Item.bowlEmpty);
	}
	*/
	
	public ItemStack onFoodEaten(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
		super.onFoodEaten(itemStack, world, entityPlayer);
		return new ItemStack(Item.bowlEmpty);
	}

}
