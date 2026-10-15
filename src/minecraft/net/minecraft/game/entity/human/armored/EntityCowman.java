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
 * Cowman trader. Passive unless provoked: hitting one (or a nearby cowman with a
 * player/zombie) makes the whole local group hostile.
 */
public class EntityCowman extends EntityTrader {
	private int randomSoundDelay = 0;

	public EntityCowman(World world) {
		super(world);
		this.texture = "/mob/cowman.png";
		this.moveSpeed = 0.5F;
		this.attackStrength = 5;
	}

	@Override
	public void onUpdate() {
		// Chase faster than he wanders, and moo occasionally while angry.
		this.moveSpeed = this.entityToAttack != null ? 0.95F : 0.5F;
		if(this.randomSoundDelay > 0 && --this.randomSoundDelay == 0) {
			this.worldObj.playSoundAtEntity(this, "mob.cow", this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
		}

		super.onUpdate();
	}

	@Override
	protected Entity findPlayerToAttack() {
		return this.angerLevel == 0 || !this.angryAtPlayer ? null : super.findPlayerToAttack();
	}

	@Override
	public boolean attackEntityFrom(Entity source, int damage) {
		// Cowmen never hurt each other.
		if(source instanceof EntityCowman) {
			return false;
		}

		// Being hit by a player or zombie angers the whole nearby group.
		if(source instanceof EntityPlayer || source instanceof EntityZombie) {
			List<Entity> nearbyEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 32.0D, 32.0D));

			for(Entity other : nearbyEntities) {
				if(other instanceof EntityCowman) {
					((EntityCowman)other).becomeAngryAt(source);
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
		return "mob.cow";
	}

	@Override
	protected String getHurtSound() {
		return "mob.cowhurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.cowhurt";
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
		return Currency.currencyEmerald;
	}

	@Override
	public int getFullHealth() {
		return 22;
	}
}
