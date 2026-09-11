package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class ItemAxe extends ItemTool {
	// Effective against these block:metadata pairs (damage -1 = any metadata)
	private static ItemStack[] stacksEffectiveAgainst = new ItemStack[]{
		new ItemStack(Block.planks, 1, -1), 
		new ItemStack(Block.bookShelf, 1, -1), 
		new ItemStack(Block.wood, 1, -1), 
		new ItemStack(Block.chest, 1, -1),
		new ItemStack(Block.ladder, 1, -1),
		new ItemStack(Block.pressurePlatePlanks, 1, -1),
		new ItemStack(Block.signPost, 1, -1),
		new ItemStack(Block.signWall, 1, -1),
		new ItemStack(Block.stairCompactPlanks, 1, -1),
		new ItemStack(Block.workbench, 1, -1),
		new ItemStack(Block.doorWood, 1, -1),
		new ItemStack(Block.fence, 1, -1),
		new ItemStack(Block.woodenSpikes, 1, -1),
		new ItemStack(Block.jukebox, 1, -1),
		new ItemStack(Block.pumpkin, 1, -1),
		new ItemStack(Block.pumpkinLantern, 1, -1),
		new ItemStack(Block.chippedWood, 1, -1),
		new ItemStack(Block.thinPlanks, 1, -1)
	};

	protected ItemAxe(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, 3, enumToolMaterial2, stacksEffectiveAgainst, silkTouch);
	}
	
	protected ItemAxe(int i1, int damageModifier, EnumToolMaterial enumToolMaterial, ItemStack[] stacksEffectiveAgainst, boolean silkTouch) {
		super(i1, damageModifier, enumToolMaterial, stacksEffectiveAgainst, silkTouch);
	}
	
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side) {
		int blockID = world.getBlockId(x, y, z);
		
		if(blockID == Block.wood.blockID) {
			Block blockChippedLog = Block.chippedWood;
			world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), blockChippedLog.stepSound.getStepSound(), (blockChippedLog.stepSound.getVolume() + 1.0F) / 2.0F, blockChippedLog.stepSound.getPitch() * 0.8F);
			world.setBlockAndMetadataWithNotify(x, y, z, blockChippedLog.blockID, world.getBlockMetadata(x, y, z));
			Block.hollowLog.dropBlockAsItem(world, x, y, z, Block.hollowLog.blockID);
			return true;
		} 
		
		return false;
	}
}
