package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.misc.EntityMinecart;
import net.minecraft.game.world.block.BlockRail;

public class ItemMinecart extends Item {
	public int minecartType;

	public ItemMinecart(int i1, int i2) {
		super(i1);
		this.maxStackSize = 1;
		this.minecartType = i2;
		
		this.displayOnCreativeTab = CreativeTabs.tabTransport;
	}

	public boolean onItemUse(ItemStack itemStack1, EntityPlayer entityPlayer2, World world3, int i4, int i5, int i6, int i7) {
		int i8 = world3.getBlockId(i4, i5, i6);
		if(BlockRail.isRailBlock(i8)) {
			if(!world3.isRemote) {
				world3.spawnEntityInWorld(new EntityMinecart(world3, (double)((float)i4 + 0.5F), (double)((float)i5 + 0.5F), (double)((float)i6 + 0.5F), this.minecartType));
			}

			if(!entityPlayer2.isCreative) --itemStack1.stackSize;
			return true;
		} else {
			return false;
		}
	}
}
