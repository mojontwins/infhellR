package net.minecraft.game.entity.animal;

import java.util.Iterator;
import java.util.List;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.path.PathEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.item.ItemFood;

public class EntityWolf extends EntityAnimal {
	private boolean looksWithInterest = false;
	private float headRoll;
	private float prevHeadRoll;
	private boolean isWolfShaking;
	private boolean isCuqui;
	private float timeWolfIsShaking;
	private float prevTimeWolfIsShaking;
	
	public EntityWolf(World world1) {
		super(world1);
		this.texture = "/mob/wolf.png";
		this.setSize(0.8F, 0.8F);
		this.moveSpeed = 1.1F;
		this.health = 8;
	}

	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
		this.dataWatcher.addObject(Datawatchers.DW_OWNER, "");
		this.dataWatcher.addObject(Datawatchers.DW_HEALTH, new Integer(this.health));
	}

	protected boolean canTriggerWalking() {
		return false;
	}

	public String getEntityTexture() {
		return this.isTamed() ? "/mob/wolf_tame.png" : (this.isWolfAngry() ? "/mob/wolf_angry.png" : super.getEntityTexture());
	}

	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setBoolean("Angry", this.isWolfAngry());
		nBTTagCompound1.setBoolean("Sitting", this.getIsSitting());
		if(this.getOwner() == null) {
			nBTTagCompound1.setString("Owner", "");
		} else {
			nBTTagCompound1.setString("Owner", this.getOwner());
		}

	}

	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.setWolfAngry(nBTTagCompound1.getBoolean("Angry"));
		this.setIsSitting(nBTTagCompound1.getBoolean("Sitting"));
		String string2 = nBTTagCompound1.getString("Owner");
		if(string2.length() > 0) {
			this.setWolfOwner(string2);
			this.setWolfTamed(true);
		}

	}

	protected boolean canDespawn() {
		return !this.isTamed();
	}

	protected String getLivingSound() {
		return this.isWolfAngry() ? "mob.wolf.growl" : (this.rand.nextInt(3) == 0 ? (this.isTamed() && this.dataWatcher.getWatchableObjectInt(Datawatchers.DW_HEALTH) < 10 ? "mob.wolf.whine" : "mob.wolf.panting") : "mob.wolf.bark");
	}

	protected String getHurtSound() {
		return "mob.wolf.hurt";
	}

	protected String getDeathSound() {
		return "mob.wolf.death";
	}

	protected float getSoundVolume() {
		return 0.4F;
	}

	protected int getDropItemId() {
		return -1;
	}

	protected void updateEntityActionState() {
		super.updateEntityActionState();
		if(!this.hasAttacked && !this.hasPath() && this.isTamed() && this.ridingEntity == null) {
			EntityPlayer entityPlayer3 = this.worldObj.getPlayerEntityByName(this.getOwner());
			if(entityPlayer3 != null) {
				float f2 = entityPlayer3.getDistanceToEntity(this);
				if(f2 > 5.0F) {
					this.getPathOrWalkableBlock(entityPlayer3, f2);
				}
			} else if(!this.isInWater()) {
				this.setIsSitting(true);
			}
		} else if(this.entityToAttack == null && !this.hasPath() && !this.isTamed() && this.worldObj.rand.nextInt(100) == 0) {
			List<Entity> list1 = this.worldObj.getEntitiesWithinAABB(EntitySheep.class, AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
			if(!list1.isEmpty()) {
				this.setTarget((Entity)list1.get(this.worldObj.rand.nextInt(list1.size())));
			}
		}

		if(this.isInWater()) {
			this.setIsSitting(false);
		}

		if(!this.worldObj.isRemote) {
			this.dataWatcher.updateObject(Datawatchers.DW_HEALTH, this.health);
		}

	}

	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.looksWithInterest = false;
		if(this.hasCurrentTarget() && !this.hasPath() && !this.isWolfAngry()) {
			Entity entity1 = this.getCurrentTarget();
			if(entity1 instanceof EntityPlayer) {
				EntityPlayer entityPlayer2 = (EntityPlayer)entity1;
				ItemStack itemStack3 = entityPlayer2.inventory.getCurrentItem();
				if(itemStack3 != null) {
					if(!this.isTamed() && itemStack3.itemID == Item.bone.shiftedIndex) {
						this.looksWithInterest = true;
					} else if(this.isTamed() && Item.itemsList[itemStack3.itemID] instanceof ItemFood) {
						this.looksWithInterest = ((ItemFood)Item.itemsList[itemStack3.itemID]).getIsWolfsFavoriteMeat();
					}
				}
			}
		}

		if(!this.isMultiplayerEntity && this.isWolfShaking && !this.isCuqui && !this.hasPath() && this.onGround) {
			this.isCuqui = true;
			this.timeWolfIsShaking = 0.0F;
			this.prevTimeWolfIsShaking = 0.0F;
			this.worldObj.setEntityState(this, (byte)8);
		}

	}

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

		if(this.isWet()) {
			this.isWolfShaking = true;
			this.isCuqui = false;
			this.timeWolfIsShaking = 0.0F;
			this.prevTimeWolfIsShaking = 0.0F;
		} else if((this.isWolfShaking || this.isCuqui) && this.isCuqui) {
			if(this.timeWolfIsShaking == 0.0F) {
				this.worldObj.playSoundAtEntity(this, "mob.wolf.shake", this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
			}

			this.prevTimeWolfIsShaking = this.timeWolfIsShaking;
			this.timeWolfIsShaking += 0.05F;
			if(this.prevTimeWolfIsShaking >= 2.0F) {
				this.isWolfShaking = false;
				this.isCuqui = false;
				this.prevTimeWolfIsShaking = 0.0F;
				this.timeWolfIsShaking = 0.0F;
			}

			if(this.timeWolfIsShaking > 0.4F) {
				float f1 = (float)this.boundingBox.minY;
				int i2 = (int)(MathHelper.sin((this.timeWolfIsShaking - 0.4F) * (float)Math.PI) * 7.0F);

				for(int i3 = 0; i3 < i2; ++i3) {
					float f4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
					float f5 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
					this.worldObj.spawnParticle("splash", this.posX + (double)f4, (double)(f1 + 0.8F), this.posZ + (double)f5, this.motionX, this.motionY, this.motionZ);
				}
			}
		}

	}

	public boolean getWolfShaking() {
		return this.isWolfShaking;
	}

	public float getShadingWhileShaking(float f1) {
		return 0.75F + (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * f1) / 2.0F * 0.25F;
	}

	public float getShakeAngle(float f1, float f2) {
		float f3 = (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * f1 + f2) / 1.8F;
		if(f3 < 0.0F) {
			f3 = 0.0F;
		} else if(f3 > 1.0F) {
			f3 = 1.0F;
		}

		return MathHelper.sin(f3 * (float)Math.PI) * MathHelper.sin(f3 * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
	}

	public float getInterestedAngle(float f1) {
		return (this.prevHeadRoll + (this.headRoll - this.prevHeadRoll) * f1) * 0.15F * (float)Math.PI;
	}

	public float getEyeHeight() {
		return this.height * 0.8F;
	}

	public int getVerticalFaceSpeed() {
		return this.getIsSitting() ? 20 : super.getVerticalFaceSpeed();
	}

	private void getPathOrWalkableBlock(Entity entity1, float f2) {
		PathEntity pathEntity3 = this.worldObj.getPathToEntity(this, entity1, 16.0F);
		if(pathEntity3 == null && f2 > 12.0F) {
			int i4 = MathHelper.floor_double(entity1.posX) - 2;
			int i5 = MathHelper.floor_double(entity1.posZ) - 2;
			int i6 = MathHelper.floor_double(entity1.boundingBox.minY);

			for(int i7 = 0; i7 <= 4; ++i7) {
				for(int i8 = 0; i8 <= 4; ++i8) {
					if((i7 < 1 || i8 < 1 || i7 > 3 || i8 > 3) && this.worldObj.isBlockNormalCube(i4 + i7, i6 - 1, i5 + i8) && !this.worldObj.isBlockNormalCube(i4 + i7, i6, i5 + i8) && !this.worldObj.isBlockNormalCube(i4 + i7, i6 + 1, i5 + i8)) {
						this.setLocationAndAngles((double)((float)(i4 + i7) + 0.5F), (double)i6, (double)((float)(i5 + i8) + 0.5F), this.rotationYaw, this.rotationPitch);
						return;
					}
				}
			}
		} else {
			this.setPathToEntity(pathEntity3);
		}

	}

	@Override
	protected boolean isMovementCeased() {
		return this.getIsSitting() || this.isCuqui;
	}

	public boolean attackEntityFrom(Entity entity1, int i2) {
		this.setIsSitting(false);
		if(entity1 != null && !(entity1 instanceof EntityPlayer) && !(entity1 instanceof EntityArrow)) {
			i2 = (i2 + 1) / 2;
		}

		if(!super.attackEntityFrom((Entity)entity1, i2)) {
			return false;
		} else {
			if(entity1 instanceof EntityPlayer && ((EntityPlayer)entity1).isCreative) return true;
			
			if(!this.isTamed() && !this.isWolfAngry()) {
				if(entity1 instanceof EntityPlayer) {
					this.setWolfAngry(true);
					this.entityToAttack = (Entity)entity1;
				}

				if(entity1 instanceof EntityArrow && ((EntityArrow)entity1).shootingEntity != null) {
					entity1 = ((EntityArrow)entity1).shootingEntity;
				}

				if(entity1 instanceof EntityLiving) {
					List<Entity> list3 = this.worldObj.getEntitiesWithinAABB(EntityWolf.class, AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
					Iterator<Entity> iterator4 = list3.iterator();

					while(iterator4.hasNext()) {
						Entity entity5 = (Entity)iterator4.next();
						EntityWolf entityWolf6 = (EntityWolf)entity5;
						if(!entityWolf6.isTamed() && entityWolf6.entityToAttack == null) {
							entityWolf6.entityToAttack = (Entity)entity1;
							if(entity1 instanceof EntityPlayer) {
								entityWolf6.setWolfAngry(true);
							}
						}
					}
				}
			} else if(entity1 != this && entity1 != null) {
				if(this.isTamed() && entity1 instanceof EntityPlayer && ((EntityPlayer)entity1).username.equalsIgnoreCase(this.getOwner())) {
					return true;
				}

				this.entityToAttack = (Entity)entity1;
			}

			return true;
		}
	}

	protected Entity findPlayerToAttack() {
		return this.isWolfAngry() ? this.worldObj.getClosestPlayerToEntity(this, 16.0D) : null;
	}

	protected void attackEntity(Entity entity1, float f2) {
		if(f2 > 2.0F && f2 < 6.0F && this.rand.nextInt(10) == 0) {
			if(this.onGround) {
				double d8 = entity1.posX - this.posX;
				double d5 = entity1.posZ - this.posZ;
				float f7 = MathHelper.sqrt_double(d8 * d8 + d5 * d5);
				this.motionX = d8 / (double)f7 * 0.5D * (double)0.8F + this.motionX * (double)0.2F;
				this.motionZ = d5 / (double)f7 * 0.5D * (double)0.8F + this.motionZ * (double)0.2F;
				this.motionY = (double)0.4F;
			}
		} else if((double)f2 < 1.5D && entity1.boundingBox.maxY > this.boundingBox.minY && entity1.boundingBox.minY < this.boundingBox.maxY) {
			this.attackTime = 20;
			byte b3 = 2;
			if(this.isTamed()) {
				b3 = 4;
			}

			entity1.attackEntityFrom(this, b3);
		}

	}

	public boolean interact(EntityPlayer entityPlayer) {
		if (super.interact(entityPlayer)) {
			return false;
		}
		
		ItemStack itemStack2 = entityPlayer.inventory.getCurrentItem();
		if(!this.isTamed()) {
			if(itemStack2 != null && itemStack2.itemID == Item.bone.shiftedIndex && !this.isWolfAngry()) {
				--itemStack2.stackSize;
				if(itemStack2.stackSize <= 0) {
					entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, (ItemStack)null);
				}

				if(!this.worldObj.isRemote) {
					if(this.rand.nextInt(3) == 0) {
						this.setWolfTamed(true);
						this.setPathToEntity((PathEntity)null);
						this.setIsSitting(true);
						this.health = 20;
						this.setWolfOwner(entityPlayer.username);
						this.showHeartsOrSmokeFX(true);
						this.worldObj.setEntityState(this, (byte)7);
					} else {
						this.showHeartsOrSmokeFX(false);
						this.worldObj.setEntityState(this, (byte)6);
					}
				}

				return true;
			}
		} else {
			if(itemStack2 != null && Item.itemsList[itemStack2.itemID] instanceof ItemFood) {
				ItemFood itemFood3 = (ItemFood)Item.itemsList[itemStack2.itemID];
				if(itemFood3.getIsWolfsFavoriteMeat() && this.dataWatcher.getWatchableObjectInt(Datawatchers.DW_HEALTH) < 20) {
					--itemStack2.stackSize;
					if(itemStack2.stackSize <= 0) {
						entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, (ItemStack)null);
					}

					this.heal(((ItemFood)Item.porkRaw).getHealAmount());
					return true;
				}
			}

			if(entityPlayer.username.equalsIgnoreCase(this.getOwner())) {
				if(!this.worldObj.isRemote) {
					this.setIsSitting(!this.getIsSitting());
					this.isJumping = false;
					this.setPathToEntity((PathEntity)null);
				}

				return true;
			}
		}

		return false;
	}

	void showHeartsOrSmokeFX(boolean z1) {
		String string2 = "heart";
		if(!z1) {
			string2 = "smoke";
		}

		for(int i3 = 0; i3 < 7; ++i3) {
			double d4 = this.rand.nextGaussian() * 0.02D;
			double d6 = this.rand.nextGaussian() * 0.02D;
			double d8 = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle(string2, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d4, d6, d8);
		}

	}

	public void handleHealthUpdate(byte b1) {
		if(b1 == 7) {
			this.showHeartsOrSmokeFX(true);
		} else if(b1 == 6) {
			this.showHeartsOrSmokeFX(false);
		} else if(b1 == 8) {
			this.isCuqui = true;
			this.timeWolfIsShaking = 0.0F;
			this.prevTimeWolfIsShaking = 0.0F;
		} else {
			super.handleHealthUpdate(b1);
		}

	}

	public float setTailRotation() {
		return this.isWolfAngry() ? 1.5393804F : (this.isTamed() ? (0.55F - (float)(20 - this.dataWatcher.getWatchableObjectInt(Datawatchers.DW_HEALTH)) * 0.02F) * (float)Math.PI : 0.62831855F);
	}

	public int getMaxSpawnedInChunk() {
		return 8;
	}

	public String getOwner() {
		return this.dataWatcher.getWatchableObjectString(Datawatchers.DW_OWNER);
	}

	public void setWolfOwner(String string1) {
		this.dataWatcher.updateObject(Datawatchers.DW_OWNER, string1);
	}

	public boolean getIsSitting() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 1) != 0;
	}

	public void setIsSitting(boolean z1) {
		byte b2 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(z1) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 | 1));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 & -2));
		}

	}

	public boolean isWolfAngry() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 2) != 0;
	}

	public void setWolfAngry(boolean z1) {
		byte b2 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(z1) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 | 2));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 & -3));
		}

	}

	public boolean isTamed() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 4) != 0;
	}

	public void setWolfTamed(boolean z1) {
		byte b2 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(z1) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 | 4));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 & -5));
		}

	}
	
	public int getFullHealth() {
		return 8;
	}
}
