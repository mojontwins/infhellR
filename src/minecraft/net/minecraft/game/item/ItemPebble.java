package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class ItemPebble extends Item {
	public ItemPebble(int id) {
		super(id);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side) {
		if(world.getBlockId(x, y, z) == Block.grass.blockID) {
			if(!world.isRemote) {
				world.setBlockAndMetadataWithNotify(x, y, z, Block.grassWithPebbles.blockID, world.getBlockMetadata(x, y, z));
				if(!entityPlayer.isCreative) --itemStack.stackSize;
			}
			return true;
		}

		return false;
	}
}