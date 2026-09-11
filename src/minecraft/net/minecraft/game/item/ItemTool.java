package net.minecraft.game.item;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.world.block.Block;

public class ItemTool extends Item {
	// Each entry stores a block:metadata pair the tool is effective against.
	// itemID = block.blockID; damage = metadata (or -1 for any metadata).
	private ItemStack[] stacksEffectiveAgainst;
	protected float efficiencyOnProperMaterial = 4.0F;
	private int damageVsEntity;
	protected EnumToolMaterial toolMaterial;

	protected ItemTool(int id, int attackDmg, EnumToolMaterial enumToolMaterial3, ItemStack[] stacks, boolean silkTouch) {
		super(id);
		this.toolMaterial = enumToolMaterial3;
		this.stacksEffectiveAgainst = stacks;
		this.maxStackSize = 1;
		this.setMaxDamage(enumToolMaterial3.getMaxUses());
		this.efficiencyOnProperMaterial = enumToolMaterial3.getEfficiencyOnProperMaterial();
		this.damageVsEntity = attackDmg + enumToolMaterial3.getDamageVsEntity();
		this.silkTouch = silkTouch;
		
		this.displayOnCreativeTab = CreativeTabs.tabTools;
	}

	public float getStrVsBlock(ItemStack itemStack1, Block block2) {
		for(int i3 = 0; i3 < this.stacksEffectiveAgainst.length; ++i3) {
			ItemStack stack = this.stacksEffectiveAgainst[i3];
			if(stack.itemID == block2.blockID && (stack.itemDamage == -1 || stack.itemDamage == 0)) {
				return this.efficiencyOnProperMaterial;
			}
		}

		return 1.0F;
	}

	public boolean hitEntity(ItemStack itemStack1, EntityLiving entityLiving2, EntityLiving entityLiving3) {
		itemStack1.damageItem(2, entityLiving3);
		return true;
	}

	public boolean onBlockDestroyed(ItemStack itemStack1, int i2, int i3, int i4, int i5, EntityLiving entityLiving6) {
		itemStack1.damageItem(1, entityLiving6);
		return true;
	}

	public int getDamageVsEntity(Entity entity1) {
		return this.damageVsEntity;
	}

	public boolean isFull3D() {
		return true;
	}
}
