package net.minecraft.game.entity.projectile;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.world.World;

public class EntityPebble extends EntitySnowball {
	public static final float pebbleSpeed = 0.7F;
	public static final int pebbleDamage = 2;
	
	public EntityPebble(World world) {
		super(world);
	}
	
	public EntityPebble(World world1, EntityLiving entityLiving2) {
		super(world1);
		this.thrower = entityLiving2;
		this.setSize(0.25F, 0.25F);
		this.setLocationAndAngles(entityLiving2.posX, entityLiving2.posY + (double)entityLiving2.getEyeHeight(), entityLiving2.posZ, entityLiving2.rotationYaw, entityLiving2.rotationPitch);	
		this.posY -= (double)0.1F;
		this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		
		this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * pebbleSpeed);
		this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * pebbleSpeed);
		this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI) * pebbleSpeed);
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
	}

	public EntityPebble(World world1, double d2, double d4, double d6) {
		super(world1);
		this.ticksInGround = 0;
		this.setSize(0.25F, 0.25F);
		this.setPosition(d2, d4, d6);
		this.yOffset = 0.0F;
	}
		
	public void throwableHitEntity(MovingObjectPosition movingObjectPosition3) {
		if(movingObjectPosition3.entityHit != null && movingObjectPosition3.entityHit.attackEntityFrom(this.thrower, pebbleDamage)) {
			;
		}

		for(int i8 = 0; i8 < 8; ++i8) {
			this.worldObj.spawnParticle("crackedpebble", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}

		this.setEntityDead();
	}
}
