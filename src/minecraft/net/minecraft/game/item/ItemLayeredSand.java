package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class ItemLayeredSand extends ItemBlock {
	public ItemLayeredSand(int i1) {
		super(i1);
	}

	// Support for stacking sand using metadata
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side) {

		if(world.getBlockId(x, y, z) == Block.snow.blockID) {
			side = 0;
		} else {
			if(side == 0) {
				--y;
			}

			if(side == 1) {
				++y;
			}

			if(side == 2) {
				--z;
			}

			if(side == 3) {
				++z;
			}

			if(side == 4) {
				--x;
			}

			if(side == 5) {
				++x;
			}
		}

		if(itemStack.stackSize == 0) {
			return false;
		} else {
			if(world.getBlockId(x, y - 1, z) == Block.layeredSand.blockID) {
				--y;
				int meta = world.getBlockMetadata(x, y, z); 
				if(meta < 14) {
					world.setBlockAndMetadataWithNotify(x, y, z, this.blockID, meta + 1);
				} else {
					world.setBlockAndMetadataWithNotify(x, y, z, Block.sand.blockID, 0);
				}
				
				return true;
			} else {
				Block block8 = Block.blocksList[this.blockID];
				if(!block8.canPlaceBlockAt(world, x, y, z)) return false;
												
				if(world.setBlockAndMetadataWithNotify(x, y, z, this.blockID, this.getPlacedBlockMetadata(itemStack.getItemDamage()))) {
					if(world.getBlockId(x, y, z) == blockID) {
						Block.blocksList[this.blockID].onBlockPlaced(world, x, y, z, side);
						Block.blocksList[this.blockID].onBlockPlacedBy(world, x, y, z, entityPlayer);
					}
					world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), block8.stepSound.getStepSound(), (block8.stepSound.getVolume() + 1.0F) / 2.0F, block8.stepSound.getPitch() * 0.8F);
					if(!entityPlayer.isCreative) --itemStack.stackSize;
				}
				
				return true;
			}
		}
	}
}
