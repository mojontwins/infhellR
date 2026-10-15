package net.minecraft.game.entity.human.armored;

import java.util.List;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.trading.Currency;

/**
 * Pigman trader. Passive unless provoked: hitting one (or a nearby pigman with a
 * player/zombie) makes the whole local group hostile.
 */
public class EntityPigman extends EntityTrader {
	private int randomSoundDelay = 0;

	public EntityPigman(World world) {
		super(world);
		this.texture = "/mob/pigman.png";
		this.moveSpeed = 0.5F;
		this.attackStrength = 5;
	}

	@Override
	public void onUpdate() {
		// Chase faster than he wanders, and oink occasionally while angry.
		this.moveSpeed = this.entityToAttack != null ? 0.95F : 0.5F;
		if(this.randomSoundDelay > 0 && --this.randomSoundDelay == 0) {
			this.worldObj.playSoundAtEntity(this, "mob.pig", this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
		}

		super.onUpdate();
	}

	@Override
	protected Entity findPlayerToAttack() {
		return this.angerLevel == 0 || !this.angryAtPlayer ? null : super.findPlayerToAttack();
	}

	@Override
	public boolean attackEntityFrom(Entity source, int damage) {
		// Pigmen never hurt each other.
		if(source instanceof EntityPigman) {
			return false;
		}

		// Being hit by a player or zombie angers the whole nearby group.
		if(source instanceof EntityPlayer || source instanceof EntityZombie) {
			List<Entity> nearbyEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 32.0D, 32.0D));

			for(Entity other : nearbyEntities) {
				if(other instanceof EntityPigman) {
					((EntityPigman)other).becomeAngryAt(source);
				}
			}

			this.becomeAngryAt(source);
		}

		return super.attackEntityFrom(source, damage);
	}

	private void becomeAngryAt(Entity source) {
		this.angryAtPlayer = source instanceof EntityPlayer;
		this.entityToAttack = source;
		this.angerLevel = 400 + this.rand.nextInt(400);
		this.randomSoundDelay = this.rand.nextInt(40);
	}

	@Override
	protected String getLivingSound() {
		return "mob.pig";
	}

	@Override
	protected String getHurtSound() {
		return "mob.pig";
	}

	@Override
	protected String getDeathSound() {
		return "mob.pigdeath";
	}

	@Override
	public void dropFewItems() {
		int count = this.rand.nextInt(2);
		for(int i = 0; i < count; ++i) {
			this.dropItem(Item.porkRaw.shiftedIndex, 1);
		}

		count = this.rand.nextInt(2);
		for(int i = 0; i < count; ++i) {
			this.dropItem(Item.ingotGold.shiftedIndex, 1);
		}
	}

	@Override
	protected int getDropItemId() {
		return Item.porkRaw.shiftedIndex;
	}

	@Override
	public ItemStack getHeldItem() {
		return this.defaultHeldItem;
	}

	@Override
	public Currency getCurrency() {
		return Currency.currencyRuby;
	}

	@Override
	public int getFullHealth() {
		return 22;
	}
}
