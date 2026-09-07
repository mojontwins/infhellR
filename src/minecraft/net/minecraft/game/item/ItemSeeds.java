package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class ItemSeeds extends Item {
	private int blockType;

	public ItemSeeds(int i1, int i2) {
		super(i1);
		this.blockType = i2;

		this.displayOnCreativeTab = CreativeTabs.tabMaterials;		
	}

	public boolean onItemUse(ItemStack itemStack1, EntityPlayer entityPlayer2, World world3, int i4, int i5, int i6, int i7) {
		if(i7 != 1) {
			return false;
		} else {
			int i8 = world3.getBlockId(i4, i5, i6);
			if(i8 == Block.tilledField.blockID && world3.isAirBlock(i4, i5 + 1, i6)) {
				world3.setBlockWithNotify(i4, i5 + 1, i6, this.blockType);
				if(!entityPlayer2.isCreative) --itemStack1.stackSize;
				return true;
			} else {
				return false;
			}
		}
	}
}
