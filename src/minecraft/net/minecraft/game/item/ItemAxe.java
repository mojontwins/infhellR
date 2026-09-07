package net.minecraft.game.item;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class ItemAxe extends ItemTool {
	private static Block[] blocksEffectiveAgainst = new Block[]{
		Block.planks, 
		Block.bookShelf, 
		Block.wood, 
		Block.chest,
		Block.ladder,
		Block.pressurePlatePlanks,
		Block.signPost,
		Block.signWall,
		Block.stairCompactPlanks,
		Block.workbench,
		Block.doorWood,
		Block.fence,
		Block.woodenSpikes,
		Block.jukebox,
		Block.pumpkin,
		Block.pumpkinLantern,
		Block.chippedWood,
		Block.thinPlanks
	};

	protected ItemAxe(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, 3, enumToolMaterial2, blocksEffectiveAgainst, silkTouch);
	}
	
	protected ItemAxe(int i1, int damageModifier, EnumToolMaterial enumToolMaterial, Block[] blocksEffectiveAgainst, boolean silkTouch) {
		super(i1, damageModifier, enumToolMaterial, blocksEffectiveAgainst, silkTouch);
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
