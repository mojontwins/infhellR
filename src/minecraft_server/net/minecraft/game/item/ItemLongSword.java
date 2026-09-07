package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;

public class ItemLongSword extends ItemSword {

	public ItemLongSword(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1, enumToolMaterial2, silkTouch);
		this.weaponDamage = 4 + enumToolMaterial2.getDamageVsEntity() + 3;
		this.displayOnCreativeTab = CreativeTabs.tabCombat;
	}

	/*
	 * Add knock back when hitting an entity.
	 */
	public float getExtraKnockbackVsEntity(Entity entity) {
		return 0.4F;
	}

	/*
	 * Default swinging speed = 6, less is faster. 
	 */
	public int getSwingSpeed() {
		return 12;
	}
}
