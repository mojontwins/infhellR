package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.entity.projectile.EntityArrow;

public class ItemBow extends Item {
	public ItemBow(int i1) {
		super(i1);
		this.maxStackSize = 1;
		this.setMaxDamage(384);
		
		this.displayOnCreativeTab = CreativeTabs.tabCombat;
	}

	public ItemStack onItemRightClick(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
		if(entityPlayer3.inventory.consumeInventoryItem(Item.arrow.shiftedIndex)) {
			world2.playSoundAtEntity(entityPlayer3, "random.bow", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F));
			if(!world2.isRemote) {
				world2.spawnEntityInWorld(new EntityArrow(world2, entityPlayer3));
			}
		}

		return itemStack1;
	}
}
