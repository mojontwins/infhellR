package net.minecraft.game.item;

import java.util.List;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockFlower;
import net.minecraft.game.world.chunk.Chunk;

public class ItemBlock extends Item {
	protected int blockID;

	public ItemBlock(int i1) {
		super(i1);
		this.blockID = i1 + 256;
		this.setIconIndex(Block.blocksList[i1 + 256].getBlockTextureFromSide(2));
	}

	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float xWithinFace, float yWithinFace, float zWithinFace) {

		int blockID = world.getBlockId(x, y, z);
		Block block = Block.blocksList[blockID];
		boolean isPlant = false;
		boolean isGroundCover = false;
		if (block != null) {
			isPlant = block instanceof BlockFlower;
			isGroundCover = block.blockMaterial.getIsGroundCover();
		}
		
		if(blockID == Block.snow.blockID) {
			side = 1;
		} else if (
			blockID != Block.vine.blockID && 
			blockID != Block.tallGrass.blockID && 
			blockID != Block.deadBush.blockID &&
			!isPlant &&
			!isGroundCover
		) {
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
		} else if(y >= Chunk.SECTION_HEIGHT - 1 && Block.blocksList[this.blockID].blockMaterial.isSolid()) {
			// Keep the topmost world layer unbuildable (the 256-height analog of the vanilla
			// "y == 127" cap). Before this fix the hardcoded 127 stopped pillaring at the old
			// 128-block ceiling even though the world is now 256 tall.
			return false;
		} else if(world.canBlockBePlacedAt(this.blockID, x, y, z, false, side, itemStack)) {
			block = Block.blocksList[this.blockID];
			if(world.setBlockAndMetadataWithNotify(x, y, z, this.blockID, this.getPlacedBlockMetadata(itemStack.getItemDamage()))) {
				Block.blocksList[this.blockID].onBlockPlaced(world, x, y, z, side, xWithinFace, yWithinFace, zWithinFace);
				Block.blocksList[this.blockID].onBlockPlacedBy(world, x, y, z, entityPlayer);
			}
			
				world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), block.stepSound.getStepSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
				if(!entityPlayer.isCreative) --itemStack.stackSize;

			return true;
		} else {
			return false;
		}
	}

	public String getItemNameIS(ItemStack itemStack1) {
		return Block.blocksList[this.blockID].getBlockName();
	}

	public String getItemName() {
		return Block.blocksList[this.blockID].getBlockName();
	}
	
	public CreativeTabs getCreativeTab() {
		return Block.blocksList[this.blockID].getCreativeTab();
	}

	public void getSubItems(int var1, CreativeTabs var2, List<ItemStack> var3) {
		Block.blocksList[this.blockID].getSubBlocks(var1, var2, var3);
	}
}
