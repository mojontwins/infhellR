package net.minecraft.game.entity.human;

import java.util.List;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.trading.Currency;

public class EntityCowman extends EntityTrader {
	private int randomSoundDelay = 0;
	
	public EntityCowman(World world1) {
		super(world1);
		this.texture = "/mob/cowman.png";
		this.moveSpeed = 0.5F;
		this.attackStrength = 5;
	}

	@Override
	public void onUpdate() {
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
	public void onLivingUpdate() {
		super.onLivingUpdate();
	}

	// Pigmen may be attacked by zombies and will fight back in groups
	@Override
	public boolean attackEntityFrom(Entity entity, int i2) {
		if(entity instanceof EntityCowman) return false;
		
		if(entity instanceof EntityPlayer || entity instanceof EntityZombie) {
			List<Entity> list4 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 32.0D, 32.0D));

			for(int i5 = 0; i5 < list4.size(); ++i5) {
				Entity entity6 = (Entity)list4.get(i5);
				if(entity6 instanceof EntityCowman) {
					EntityCowman entityCowMan7 = (EntityCowman)entity6;
					entityCowMan7.becomeAngryAt(entity);
				}
			}

			this.becomeAngryAt(entity);
		}

		return super.attackEntityFrom(entity, i2);
	}
	
	private void becomeAngryAt(Entity entity1) {
		this.angryAtPlayer = (entity1 instanceof EntityPlayer);
		this.entityToAttack = entity1;
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
		int i3 = this.rand.nextInt(2);

		int i4;
		for(i4 = 0; i4 < i3; ++i4) {
			this.dropItem(Item.porkRaw.shiftedIndex, 1);
		}

		i3 = this.rand.nextInt(2);

		for(i4 = 0; i4 < i3; ++i4) {
			this.dropItem(Item.ingotGold.shiftedIndex, 1);
		}
	}

	@Override
	protected int getDropItemId() {
		return Item.porkRaw.shiftedIndex;
	}

	@Override
	public ItemStack getHeldItem() {
		return defaultHeldItem;
	}
	
	@Override
	public boolean burnsOnDaylight() {
		return false;
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
