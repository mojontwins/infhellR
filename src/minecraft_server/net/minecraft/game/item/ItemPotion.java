package net.minecraft.game.item;

import net.minecraft.game.entity.status.Status;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.entity.projectile.EntityThrowablePotion;

public class ItemPotion extends Item {
	private int potionColorMultiplier;
	private int potionType;
	
	public static final int EMPTY = 0;
	public static final int POISON = Status.statusPoisoned.id;
	public static final int SLOWNESS = Status.statusSlowness.id;
	public static final int AUTOHEALING = Status.statusAutoHealing.id;
	public static final int INSTANTDAMAGE = Status.statusInstantDamage.id;
	public static final int DIZZY = Status.statusDizzy.id;
	
	protected ItemPotion(int i1, int potionColorMultiplier, int potionType) {
		super(i1);
		this.potionColorMultiplier = potionColorMultiplier;
		this.potionType = potionType;
		this.iconIndex = this.potionType > 0 ? 10*16 + 11 : 11*16 + 11;
		
		this.statusAmplifier = 1;
		this.statusTime = 100;

		this.displayOnCreativeTab = CreativeTabs.tabBrewing;
	}

	public int getColorFromDamage(int i1) {
		return this.potionColorMultiplier;
	}
	
	public int getPotionType() {
		return this.potionType;
	}
	
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
		if(entityPlayer.isCreative || entityPlayer.inventory.consumeInventoryItem(this.shiftedIndex)) {
			world.playSoundAtEntity(entityPlayer, "mob.witch.throw", 1.0F, 1.0F / (Item.rand.nextFloat() * 0.4F + 0.8F));
			if(!world.isRemote) {
				world.spawnEntityInWorld(new EntityThrowablePotion(world, entityPlayer, this));
			}
		}

		return itemStack;
	}
}
