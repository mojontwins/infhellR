package net.minecraft.game.entity.animal;

import net.minecraft.game.entity.monster.EntityHauntedCow;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.EntityLightningBolt;
import net.minecraft.game.item.ItemBow;
import net.minecraft.game.item.ItemSword;

public class EntityCow extends EntityAnimal {
	protected boolean looksWithInterest;
	private float prevHeadRoll;
	private float headRoll;

	public EntityCow(World world1) {
		super(world1);
		this.texture = "/mob/cow.png";
		this.setSize(0.9F, 1.3F);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
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
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected int getDropItemId() {
		return Item.leather.shiftedIndex;
	}

	@Override
	public boolean interact(EntityPlayer entityPlayer) {
		if (super.interact(entityPlayer)) {
			return false;
		}
		
		ItemStack itemStack2 = entityPlayer.inventory.getCurrentItem();
		if(itemStack2 != null && itemStack2.itemID == Item.bucketEmpty.shiftedIndex) {
			entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, new ItemStack(Item.bucketMilk));
			entityPlayer.triggerAchievement(AchievementList.bucketMilkCow);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean attackEntityFrom(Entity entity1, int i2) {
		if(super.attackEntityFrom(entity1, i2) == false) {
			return false;
		}
		
		if(!this.worldObj.isRemote) {
			
			boolean hauntMe = false;
			
			if(entity1 instanceof EntityPlayer) {
				EntityPlayer entityPlayer = (EntityPlayer)entity1;
				ItemStack heldItemStack = entityPlayer.getCurrentEquippedItem();
				
				// Turn into haunted cow?
				if(heldItemStack == null || (heldItemStack.getItem() instanceof ItemSword) || (heldItemStack.getItem() instanceof ItemBow)) {
					return true;
				}
			
				if(this.rand.nextInt(32) != 0) {
					return true;
				}
				
				// Knock player back a bit
				entityPlayer.addVelocity(
						(double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) *  0.5F), 
						0.1D, 
						(double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) *  0.5F)
					);
				this.motionX *= 0.6D;
				this.motionZ *= 0.6D;
				
				hauntMe = true;
			}
			
			if(entity1 instanceof EntityHauntedCow) {
				hauntMe = true;
			}
			
			if(hauntMe) {
				// Add lighting
				this.worldObj.addWeatherEffect(new EntityLightningBolt(this.worldObj, this.posX, this.posY, this.posZ));
				
				// Create haunted cow!
				this.setEntityDead();
				EntityHauntedCow entityHauntedCow = new EntityHauntedCow(this.worldObj);
				entityHauntedCow.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
				this.worldObj.spawnEntityInWorld(entityHauntedCow);
				
				// Night time!
				long worldTime = this.worldObj.getWorldTime();
				long dayTime = worldTime % 24000;
				if(dayTime < 18000) {
					this.worldObj.setWorldTime((worldTime / 24000) * 24000 + 18000);
				}
				
				if(entity1 instanceof EntityPlayer) ((EntityPlayer)entity1).triggerAchievement(AchievementList.cursedCow);
			}
		}
		
		return true;
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
