package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockStep;

public class ItemSlab extends ItemBlock {
	public ItemSlab(int i1) {
		super(i1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	public int getIconFromDamage(int i1) {
		return Block.stairSingle.getBlockTextureFromSideAndMetadata(2, i1);
	}

	public int getPlacedBlockMetadata(int i1) {
		return i1;
	}

	public String getItemNameIS(ItemStack itemStack1) {
		return super.getItemName() + "." + BlockStep.blockStepTypes[itemStack1.getItemDamage()];
	}
	
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int face, float xWithinFace, float yWithinFace, float zWithinFace)  {
		if(itemStack.stackSize == 0) {
			return false;
		} else {
			int blockID = world.getBlockId(x, y, z);
			int meta = world.getBlockMetadata(x, y, z);
			int type = meta & 7;
			boolean isUpper = (meta & 8) != 0;
			
			if(
				((face == 1 && !isUpper) || (face == 0 && isUpper)) && 
				blockID == Block.stairSingle.blockID && 
				type == (itemStack.getItemDamage() & 7)
			) {
				if (world.checkIfAABBIsClear(Block.stairDouble.getCollisionBoundingBoxFromPool(world, x, y, z)) && 
					world.setBlockAndMetadataWithNotify(x, y, z, Block.stairDouble.blockID, type)
				) {
					world.playSoundEffect(
						(double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), 
						Block.stairDouble.stepSound.getStepSound(), 
						(Block.stairDouble.stepSound.getVolume() + 1.0F) / 2.0F, 
						Block.stairDouble.stepSound.getPitch() * 0.8F
					);
					if(!entityPlayer.isCreative) --itemStack.stackSize;
				}

				return true;
			} else if (onItemUseAux(itemStack, entityPlayer, world, x, y, z, face, xWithinFace, yWithinFace, zWithinFace)) {
				return true;
			} else {
				return super.onItemUse(itemStack, entityPlayer, world, x, y, z, face, xWithinFace, yWithinFace, zWithinFace);
			}
		}
	}

	private static boolean onItemUseAux (ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float xWithinFace, float yWithinFace, float zWithinFace) {
		switch (side) {
			case 0:	y --; break;
			case 1: y ++; break;
			case 2: z --; break;
			case 3: z ++; break;
			case 4: x --; break;
			case 5: x ++; break;
		}

		int blockID = world.getBlockId(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		int type = meta & 7;

		if (blockID == Block.stairSingle.blockID && type == itemStack.getItemDamage ()) {
			if (world.checkIfAABBIsClear(Block.stairDouble.getCollisionBoundingBoxFromPool(world, x, y, z)) &&
				world.setBlockAndMetadataWithNotify(x, y, z, Block.stairDouble.blockID, type)
			) {
				world.playSoundEffect(
					(double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), 
					Block.stairDouble.stepSound.getStepSound(), 
					(Block.stairDouble.stepSound.getVolume() + 1.0F) / 2.0F, 
					Block.stairDouble.stepSound.getPitch() * 0.8F
				);
				if(!entityPlayer.isCreative) --itemStack.stackSize;
			}

			return true;
		} else {
			return false;
		}
	}
}
