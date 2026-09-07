package net.minecraft.game.entity.monster;

import java.util.List;

import net.minecraft.game.entity.animal.EntityBetaOcelot;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.EntityLightningBolt;
import net.minecraft.game.entity.EntityMob;

public class EntityCreeper extends EntityMob {
	int timeSinceIgnited;
	int lastActiveTime;
	protected static final int fuseDuration = 30;
	
	public EntityCreeper(World world1) {
		super(world1);
		this.texture = "/mob/creeper.png";
	}

	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)-1);
		this.dataWatcher.addObject(Datawatchers.DW_CHARGING, (byte)0);
	}

	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		if(this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_CHARGING) == 1) {
			nBTTagCompound1.setBoolean("powered", true);
		}

	}

	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.dataWatcher.updateObject(Datawatchers.DW_CHARGING, (byte)(nBTTagCompound1.getBoolean("powered") ? 1 : 0));
	}

	protected void attackBlockedEntity(Entity entity1, float f2) {
		if(!this.worldObj.isRemote) {
			if(this.timeSinceIgnited > 0) {
				this.setCreeperState(-1);
				--this.timeSinceIgnited;
				if(this.timeSinceIgnited < 0) {
					this.timeSinceIgnited = 0;
				}
			}

		}
	}

	public void onUpdate() {
		this.lastActiveTime = this.timeSinceIgnited;
		if(this.worldObj.isRemote) {
			int i1 = this.getCreeperState();
			if(i1 > 0 && this.timeSinceIgnited == 0) {
				this.worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
			}

			this.timeSinceIgnited += i1;
			if(this.timeSinceIgnited < 0) {
				this.timeSinceIgnited = 0;
			}

			if(this.timeSinceIgnited >= fuseDuration) {
				this.timeSinceIgnited = fuseDuration;
			}
		}

		super.onUpdate();
		if(this.entityToAttack == null && this.timeSinceIgnited > 0) {
			this.setCreeperState(-1);
			--this.timeSinceIgnited;
			if(this.timeSinceIgnited < 0) {
				this.timeSinceIgnited = 0;
			}
		}

	}
	
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		
		List<Entity> list3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(5.0D, 3.0D, 5.0D));
		for(int i4 = 0; i4 < list3.size(); ++i4) {
			Entity entity5 = (Entity)list3.get(i4);
			if(entity5 instanceof EntityBetaOcelot) {
				this.moveForward = -this.moveSpeed;
				this.entityToAttack = null;
				
				break;
			}
		}
		
	}
	
	protected String getHurtSound() {
		return "mob.creeper";
	}

	protected String getDeathSound() {
		return "mob.creeperdeath";
	}

	public void onDeath(Entity entity1) {
		super.onDeath(entity1);
		if(entity1 instanceof EntitySkeleton) {
			this.dropItem(Item.record13.shiftedIndex + this.rand.nextInt(2), 1);
		}

	}

	protected void attackEntity(Entity entity1, float f2) {
		if(!this.worldObj.isRemote) {
			int i3 = this.getCreeperState();
			if(i3 <= 0 && f2 < 3.0F || i3 > 0 && f2 < 7.0F) {
				if(this.timeSinceIgnited == 0) {
					this.worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
				}

				this.setCreeperState(1);
				++this.timeSinceIgnited;
				if(this.timeSinceIgnited >= fuseDuration) {
					if(this.getPowered()) {
						this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 6.0F);
					} else {
						this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 3.0F);
					}

					this.setEntityDead();
				}

				this.hasAttacked = true;
			} else {
				this.setCreeperState(-1);
				--this.timeSinceIgnited;
				if(this.timeSinceIgnited < 0) {
					this.timeSinceIgnited = 0;
				}
			}

		}
	}

	public boolean getPowered() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_CHARGING) == 1;
	}

	public float setCreeperFlashTime(float f1) {
		return ((float)this.lastActiveTime + (float)(this.timeSinceIgnited - this.lastActiveTime) * f1) / 28.0F;
	}

	protected int getDropItemId() {
		return Item.gunpowder.shiftedIndex;
	}

	protected int getCreeperState() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
	}

	protected void setCreeperState(int i1) {
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)i1);
	}

	public void onStruckByLightning(EntityLightningBolt entityLightningBolt1) {
		super.onStruckByLightning(entityLightningBolt1);
		this.dataWatcher.updateObject(Datawatchers.DW_CHARGING, (byte)1);
	}
}
