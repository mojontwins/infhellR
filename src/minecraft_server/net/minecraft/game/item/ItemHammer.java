package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;

public class ItemHammer extends ItemPickaxe {

	public ItemHammer(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, 8, enumToolMaterial2, silkTouch);
		this.displayOnCreativeTab = CreativeTabs.tabCombat;
	}
	
	/*
	 * Add knock back when hitting an entity.
	 */
	public float getExtraKnockbackVsEntity(Entity entity) {
		return 1.0F;
	}

	/*
	 * Default swinging speed = 6, less is faster. 
	 */
	public int getSwingSpeed() {
		return 16;
	}
	
	public float getStrVsBlock(ItemStack itemStack1, Block block2, int metadata) {
		return 1.5F * ( block2 == null || block2.blockMaterial != Material.iron && block2.blockMaterial != Material.rock ? super.getStrVsBlock(itemStack1, block2, metadata) : this.efficiencyOnProperMaterial );
	}
}
