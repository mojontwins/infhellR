package net.minecraft.game.entity.projectile;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.world.World;
import net.minecraft.game.item.ItemPotion;

public class EntityThrowablePotion extends EntitySnowball {
	public ItemPotion itemPotion;
	private static Item potionsMap[] = new Item[] {
		null, 
		Item.potionPoison,
		Item.potionSlowness,
		Item.potionInstantDamage,
		Item.potionAutoHealing
	};
	
	public EntityThrowablePotion(World world1) {
		super(world1);
		this.setSize(0.25F, 0.25F);
	}
	
	public EntityThrowablePotion(World world1, EntityLiving entityLiving2, ItemPotion itemPotion) {
		super(world1, entityLiving2);
		this.itemPotion = itemPotion;
	}

	public EntityThrowablePotion(World world1, double d2, double d4, double d6, ItemPotion itemPotion) {
		super(world1, d2, d4, d6);
		this.itemPotion = itemPotion;
	}
	
	public EntityThrowablePotion(World world1, double d2, double d4, double d6, int potionAsIndex) {
		super(world1, d2, d4, d6);
		this.itemPotion = (ItemPotion) potionsMap[potionAsIndex];
	}
	
	public int getPotionAsIndex() {
		for(int i = 0; i < EntityThrowablePotion.potionsMap.length; i ++) {
			if(EntityThrowablePotion.potionsMap[i] == this.itemPotion) {
				return i;
			}
		}
		
		return 0;
	}
	
	public void setVelocity(double d1, double d3, double d5) {
		this.motionX = d1;
		this.motionY = d3;
		this.motionZ = d5;
		if(this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f7 = MathHelper.sqrt_double(d1 * d1 + d5 * d5);
			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(d1, d5) * 180.0D / (double)(float)Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(d3, (double)f7) * 180.0D / (double)(float)Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}

	}
	
	public void throwableHitEntity(MovingObjectPosition movingObjectPosition3) {
		if(
			movingObjectPosition3.entityHit instanceof EntityLiving && 
			movingObjectPosition3.entityHit != null &&
			this.itemPotion != null
		) {
			EntityLiving entityHit = (EntityLiving)movingObjectPosition3.entityHit;
			entityHit.addStatusEffect(new StatusEffect(itemPotion.getPotionType(), itemPotion.statusTime, itemPotion.statusAmplifier));
			if(entityHit instanceof EntityPlayer) {
				EntityPlayer entityPlayer1 = (EntityPlayer)entityHit;
				entityPlayer1.triggerAchievement(AchievementList.getStatus);
			}
		}

		for(int i8 = 0; i8 < 8; ++i8) {
			this.worldObj.spawnParticle("crackedpotion", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}

		this.setEntityDead();
	}
}
