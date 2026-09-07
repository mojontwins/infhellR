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
}
