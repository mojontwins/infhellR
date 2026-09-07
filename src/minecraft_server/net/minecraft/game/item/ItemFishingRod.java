package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.EntityFish;

public class ItemFishingRod extends Item {
	public ItemFishingRod(int i1) {
		super(i1);
		this.setMaxDamage(64);
		this.setMaxStackSize(1);
		
		this.displayOnCreativeTab = CreativeTabs.tabTools;
	}

	public boolean isFull3D() {
		return true;
	}

	public boolean shouldRotateAroundWhenRendering() {
		return true;
	}

	public ItemStack onItemRightClick(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
		if(entityPlayer3.fishEntity != null) {
			int i4 = entityPlayer3.fishEntity.catchFish();
			itemStack1.damageItem(i4, entityPlayer3);
			entityPlayer3.swingItem();
		} else {
			world2.playSoundAtEntity(entityPlayer3, "random.bow", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
			if(!world2.isRemote) {
				world2.spawnEntityInWorld(new EntityFish(world2, entityPlayer3));
			}

			entityPlayer3.swingItem();
		}

		return itemStack1;
	}
}
