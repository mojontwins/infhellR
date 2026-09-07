package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.block.Block;

public class ItemBattleAxe extends ItemAxe {
	private static Block[] blocksEffectiveAgainst = new Block[]{
		Block.ladder,
		Block.pressurePlatePlanks,
		Block.signPost,
		Block.signWall,
		Block.fence,
		Block.woodenSpikes,
		Block.pumpkin,
		Block.pumpkinLantern,
		Block.chippedWood,
		Block.thinPlanks
	};

	public ItemBattleAxe(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, 4, enumToolMaterial2, blocksEffectiveAgainst, silkTouch);
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
