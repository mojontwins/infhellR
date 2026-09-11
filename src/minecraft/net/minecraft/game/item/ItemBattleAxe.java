package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.block.Block;

public class ItemBattleAxe extends ItemAxe {
	// Effective against these block:metadata pairs (damage -1 = any metadata)
	private static ItemStack[] stacksEffectiveAgainst = new ItemStack[]{
		new ItemStack(Block.ladder, 1, -1),
		new ItemStack(Block.pressurePlatePlanks, 1, -1),
		new ItemStack(Block.signPost, 1, -1),
		new ItemStack(Block.signWall, 1, -1),
		new ItemStack(Block.fence, 1, -1),
		new ItemStack(Block.woodenSpikes, 1, -1),
		new ItemStack(Block.pumpkin, 1, -1),
		new ItemStack(Block.pumpkinLantern, 1, -1),
		new ItemStack(Block.chippedWood, 1, -1),
		new ItemStack(Block.thinPlanks, 1, -1)
	};

	public ItemBattleAxe(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, 4, enumToolMaterial2, stacksEffectiveAgainst, silkTouch);
		this.displayOnCreativeTab = CreativeTabs.tabCombat;
	}
	
	/*
	 * Add knock back when hitting an entity.
	 */
	public float getExtraKnockbackVsEntity(Entity entity) {
		return 0.7F;
	}

	/*
	 * Default swinging speed = 6, less is faster. 
	 */
	public int getSwingSpeed() {
		return 10;
	}
}
