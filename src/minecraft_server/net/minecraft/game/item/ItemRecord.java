package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockJukeBox;

public class ItemRecord extends Item {
	public final String recordName;

	protected ItemRecord(int i1, String string2) {
		super(i1);
		this.recordName = string2;
		this.maxStackSize = 1;
		
		this.displayOnCreativeTab = CreativeTabs.tabMisc;
	}

	public boolean onItemUse(ItemStack itemStack1, EntityPlayer entityPlayer2, World world3, int i4, int i5, int i6, int i7) {
		if(world3.getBlockId(i4, i5, i6) == Block.jukebox.blockID && world3.getBlockMetadata(i4, i5, i6) == 0) {
			if(world3.isRemote) {
				return true;
			} else {
				((BlockJukeBox)Block.jukebox).ejectRecord(world3, i4, i5, i6, this.shiftedIndex);
				world3.playAuxSFXAtEntity((EntityPlayer)null, 1005, i4, i5, i6, this.shiftedIndex);
				if(!entityPlayer2.isCreative) --itemStack1.stackSize;
				return true;
			}
		} else {
			return false;
		}
	}
}
