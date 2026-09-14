package net.minecraft.game.entity.animal;

import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.EntityLightningBolt;
import net.minecraft.game.entity.monster.EntityPigZombie;

public class EntityPig extends EntityAnimal {
	private boolean looksWithInterest;
	private float headRoll;
	private float prevHeadRoll;
	/** True while a player is steering this pig so the 2x speed boost is undone exactly once on dismount. */
	private boolean speedBoosted;
	/** Maximum pig yaw rotation per tick while steered (degrees). */
	private static final float TURN_RATE = 30.0F;

	public EntityPig(World world1) {
		super(world1);
		this.texture = "/mob/pig.png";
		this.setSize(0.9F, 0.9F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setBoolean("Saddle", this.getSaddled());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.setSaddled(nBTTagCompound1.getBoolean("Saddle"));
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
	public boolean interact(EntityPlayer entityPlayer) {
		if (super.interact(entityPlayer)) {
			return false;
		}
		
		if(!this.getSaddled() || this.worldObj.isRemote || this.riddenByEntity != null && this.riddenByEntity != entityPlayer) {
			return false;
		} else {
			entityPlayer.mountEntity(this);
			return true;
		}
	}

	@Override
	protected int getDropItemId() {
		return this.fire > 0 ? Item.porkCooked.shiftedIndex : Item.porkRaw.shiftedIndex;
	}

	public boolean getSaddled() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 1) != 0;
	}

	public void setSaddled(boolean z1) {
		if(z1) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)1);
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)0);
		}

	}

	@Override
	public void onStruckByLightning(EntityLightningBolt entityLightningBolt1) {
		if(!this.worldObj.isRemote) {
			EntityPigZombie entityPigZombie2 = new EntityPigZombie(this.worldObj);
			entityPigZombie2.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.worldObj.spawnEntityInWorld(entityPigZombie2);
			this.setEntityDead();
		}
	}

	@Override
	protected boolean fall(float f1) {
		boolean rebound = super.fall(f1);
		if(f1 > 5.0F && this.riddenByEntity instanceof EntityPlayer) {
			((EntityPlayer)this.riddenByEntity).triggerAchievement(AchievementList.flyPig);
		}
		return rebound;
	}
	
	@Override
	protected void updateEntityActionState() {
		if (this.riddenByEntity instanceof EntityPlayer && !this.worldObj.isRemote) {
			// A player is steering this saddled pig directly: skip the AI and
			// apply the rider's movement input instead. Multiplayer client pigs
			// skip this whole method (they are interpolated from server position
			// packets), so only the logical server / singleplayer steers.
			EntityPlayer rider = (EntityPlayer)this.riddenByEntity;

			// Clamp the rider's input to [-1,1] so a hacked client cannot send
			// huge strafe/forward values and exceed the intended 2x speed.
			this.moveStrafing = clampMoveInput(rider.moveStrafing);
			this.moveForward = clampMoveInput(rider.moveForward);
			this.isJumping = rider.isJumping;

			// Controlled speed: 2x a walking player. Pig and player share the same
			// landMovementFactor, so a 2.0 speedModifier doubles it exactly.
			if (!this.speedBoosted) {
				this.speedModifier = 2.0F;
				this.speedBoosted = true;
			}

			// While any direction input is held, turn the pig toward where the
			// rider is looking, capped at TURN_RATE degrees per tick.
			if (this.moveForward != 0.0F || this.moveStrafing != 0.0F) {
				float delta = rider.rotationYaw - this.rotationYaw;
				while (delta < -180.0F) {
					delta += 360.0F;
				}
				while (delta >= 180.0F) {
					delta -= 360.0F;
				}
				if (delta > TURN_RATE) {
					delta = TURN_RATE;
				} else if (delta < -TURN_RATE) {
					delta = -TURN_RATE;
				}

				this.rotationYaw += delta;
			}
		} else {
			super.updateEntityActionState();
			
			Entity target = null;
			
			EntityPlayer closestPlayer = this.worldObj.getClosestPlayerToEntity(this, 8.0D); 
			if (closestPlayer != null) {
				ItemStack heldItem = closestPlayer.inventory.getCurrentItem();
				
				// Start following player?
				if (heldItem != null && heldItem.itemID == Item.wheat.shiftedIndex) {
					target = closestPlayer;
				}
			}
			
			this.setTarget(target);

			// Dismounted: restore the pig's normal speed exactly once.
			if (this.speedBoosted) {
				this.speedModifier = 1.0F;
				this.speedBoosted = false;
			}
		}
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		this.looksWithInterest = false;
		
		if (this.hasCurrentTarget() && !this.hasPath()) {
			Entity currentTargetEntity = this.getCurrentTarget();
			if (currentTargetEntity instanceof EntityPlayer) {
				EntityPlayer entityPlayer = (EntityPlayer) currentTargetEntity;
				ItemStack heldItem = entityPlayer.inventory.getCurrentItem();
				if (heldItem != null && heldItem.itemID == Item.wheat.shiftedIndex) {
					this.looksWithInterest = true;
				}
			}
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		this.prevHeadRoll = this.headRoll;
		
		if(this.looksWithInterest) {
			this.headRoll += (1.0F - this.headRoll) * 0.4F;
		} else {
			this.headRoll += (0.0F - this.headRoll) * 0.4F;
		}
		
		if(this.looksWithInterest) {
			this.numTicksToChaseTarget = 10;
		}
	}
	
	public float getInterestedAngle(float f1) {
		return (this.prevHeadRoll + (this.headRoll - this.prevHeadRoll) * f1) * 0.15F * (float)Math.PI;
	}

	/** Clamps the rider's movement input to [-1,1] before applying it to the pig. */
	private static float clampMoveInput(float f1) {
		if (f1 > 1.0F) {
			return 1.0F;
		} else if (f1 < -1.0F) {
			return -1.0F;
		} else {
			return f1;
		}
	}
}
