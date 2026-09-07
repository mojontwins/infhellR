package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.entity.projectile.EntityEgg;

public class ItemEgg extends Item {
	public ItemEgg(int i1) {
		super(i1);
		this.maxStackSize = 16;
		
		this.displayOnCreativeTab = CreativeTabs.tabMaterials;
	}

	public ItemStack onItemRightClick(ItemStack itemStack1, World world2, EntityPlayer entityPlayer3) {
		if(!entityPlayer3.isCreative) --itemStack1.stackSize;
		world2.playSoundAtEntity(entityPlayer3, "random.bow", 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
		if(!world2.isRemote) {
			world2.spawnEntityInWorld(new EntityEgg(world2, entityPlayer3));
		}

		return itemStack1;
	}
}
