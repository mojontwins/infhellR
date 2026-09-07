package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;

public class ItemSpade extends ItemTool {
	private static Block[] blocksEffectiveAgainst = new Block[]{
		Block.grass, 
		Block.dirt, 
		Block.sand, 
		Block.gravel, 
		Block.snow, 
		Block.blockSnow, 
		Block.blockClay, 
		Block.tilledField,
		Block.dirtPath,
		Block.cementPowder
	};

	public ItemSpade(int id, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(id, 1, enumToolMaterial2, blocksEffectiveAgainst, silkTouch);
	}

	public boolean canHarvestBlock(Block block1) {
		if(block1 == Block.snow || block1 == Block.blockSnow) return true;
		
		if(block1.getRenderType() == 111) return true;
		
		return false;
	}
	
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side) {
		int blockID = world.getBlockId(x, y, z);
		Material material = world.getBlockMaterial(x, y + 1, z);
		if(material.isSolid() || blockID != Block.grass.blockID) {
			return false;
		} else {
			Block blockDirtPath = Block.dirtPath;
			world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), blockDirtPath.stepSound.getStepSound(), (blockDirtPath.stepSound.getVolume() + 1.0F) / 2.0F, blockDirtPath.stepSound.getPitch() * 0.8F);
			world.setBlockWithNotify(x, y, z, blockDirtPath.blockID);
			return true;
		}
	}
}
