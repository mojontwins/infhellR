package net.minecraft.game.entity.projectile;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

public class EntityArrowWithEffect extends EntityArrow {
	private StatusEffect statusEffect;
	
	public EntityArrowWithEffect(World world, double posX, double posY, double posZ) {
		super(world, posX, posY, posZ);
	}

	public EntityArrowWithEffect(World world, EntityLiving entityLiving) {
		super(world, entityLiving);
	}

	public EntityArrowWithEffect(World world) {
		super(world);
	}
	
	public EntityArrowWithEffect withStatusEffect(StatusEffect statusEffect) {
		this.setStatusEffect(statusEffect);
		return this;
	}
	
	@Override
	public void throwableHitEntity(MovingObjectPosition movingObjectPosition) {
		if(
				movingObjectPosition.entityHit instanceof EntityLiving && 
				movingObjectPosition.entityHit != null &&
				this.statusEffect != null
		) {
			EntityLiving entityHit = (EntityLiving)movingObjectPosition.entityHit;
			entityHit.addStatusEffect(this.statusEffect);
			
			if(entityHit instanceof EntityPlayer) {
				EntityPlayer entityPlayer1 = (EntityPlayer)entityHit;
				entityPlayer1.triggerAchievement(AchievementList.getStatus);
			}
		}
		
		super.throwableHitEntity(movingObjectPosition);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound) {
		super.writeEntityToNBT(nBTTagCompound);
		nBTTagCompound.setInteger("statusId", this.statusEffect.statusID);
		nBTTagCompound.setInteger("statusTime", this.statusEffect.duration);
		nBTTagCompound.setInteger("statusAmplifier", this.statusEffect.amplifier);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound) {
		super.readEntityFromNBT(nBTTagCompound);
		int statusID = nBTTagCompound.getInteger("statusId");
		int statusTime = nBTTagCompound.getInteger("statusTime");
		int statusAmplifier = nBTTagCompound.getInteger("statusAmplifier");
		this.statusEffect = new StatusEffect(statusID, statusTime, statusAmplifier);
	}

	public StatusEffect getStatusEffect() {
		return statusEffect;
	}

	public void setStatusEffect(StatusEffect statusEffect) {
		this.statusEffect = statusEffect;
	}
}
