package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.world.block.Block;

public class ItemSword extends Item {
	protected int weaponDamage;
	
	public static Block[] canHarvest = new Block[] {
		Block.leaves,
		Block.web
	};

	public ItemSword(int i1, EnumToolMaterial enumToolMaterial2, boolean silkTouch) {
		super(i1);
		this.maxStackSize = 1;
		this.setMaxDamage(enumToolMaterial2.getMaxUses());
		this.weaponDamage =  4 + enumToolMaterial2.getDamageVsEntity(); //4 + enumToolMaterial2.getDamageVsEntity() * 2;
		this.silkTouch = silkTouch;

		this.displayOnCreativeTab = CreativeTabs.tabCombat;
	}

	public float getStrVsBlock(ItemStack itemStack1, Block block2) {
		return block2.blockID == Block.web.blockID ? 15.0F : 1.5F;
	}

	public boolean hitEntity(ItemStack itemStack1, EntityLiving entityLiving2, EntityLiving entityLiving3) {
		itemStack1.damageItem(1, entityLiving3);
		return true;
	}

	public boolean onBlockDestroyed(ItemStack itemStack1, int i2, int i3, int i4, int i5, EntityLiving entityLiving6) {
		itemStack1.damageItem(2, entityLiving6);
		return true;
	}

	public int getDamageVsEntity(Entity entity1) {
		return this.weaponDamage;
	}

	public boolean isFull3D() {
		return true;
	}

	public boolean canHarvestBlock(Block block) {
		for(int i = 0; i < canHarvest.length; i ++) {
			if(block == canHarvest[i]) return true;
		}
		return false;
	}
}
