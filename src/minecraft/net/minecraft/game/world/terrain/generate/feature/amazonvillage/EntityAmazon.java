package net.minecraft.game.world.terrain.generate.feature.amazonvillage;

import java.util.Iterator;
import java.util.List;

import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemFood;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.path.PathEntity;
import net.minecraft.game.world.World;

public class EntityAmazon extends EntityArmoredMob implements ISentient {
	private boolean looksWithInterest = false;
	private float headRoll;
	private float prevHeadRoll;
	protected String texturePrefix;
	
	public boolean isSwinging = false;
	public int swingProgressInt = 0;
	public int timeBeforeLosingInterest = 0;
	
	public EntityAmazon(World world1) {
		super(world1);
		this.texture = "/mob/amazon1.png";
		this.texturePrefix="amazon";
		this.moveSpeed = 1.1F;
		this.health = this.getFullHealth();
		this.inventory.setInventorySlotContents(0, rand.nextInt(32) == 0 ? new ItemStack(Item.maceGold) : new ItemStack(Item.swordGold));
	}
	
	public void setEntityToAttack(Entity entity) {
		if(this.entityToAttack != entity) {
			this.timeBeforeLosingInterest = 600;
		}	
		this.entityToAttack = entity;
	}
	
	public void swingItem() {
		this.swingProgressInt = -1;
		this.isSwinging = true;
	}
	
	protected int getMaxTextureVariations() {
		return 4;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
		this.dataWatcher.addObject(Datawatchers.DW_OWNER, "");
		this.dataWatcher.addObject(Datawatchers.DW_HEALTH, new Integer(this.health));
		
		// Datawatcher object 19 is texture variation
		this.dataWatcher.addObject(19, (byte)0);
		int variation = 4;
		if(rand.nextInt(9) != 0) {
			variation = 1 + rand.nextInt(3);
		}
		this.setTextureVariation((byte) (variation));		
	}
	
	public void setTextureVariation(byte variation) {
		this.dataWatcher.updateObject(19, variation);
	}
	
	public byte getTextureVariation() {
		return this.dataWatcher.getWatchableObjectByte(19);
	}

	@Override
	protected boolean canTriggerWalking() {
		return true;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setBoolean("Angry", this.isAmazonAngry());
		nBTTagCompound1.setBoolean("Sitting", this.getIsSitting());
		if(this.getOwner() == null) {
			nBTTagCompound1.setString("Owner", "");
		} else {
			nBTTagCompound1.setString("Owner", this.getOwner());
		}
		nBTTagCompound1.setByte("TextureVariation", this.getTextureVariation());
		
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.setAmazonAngry(nBTTagCompound1.getBoolean("Angry"));
		this.setIsSitting(nBTTagCompound1.getBoolean("Sitting"));
		String string2 = nBTTagCompound1.getString("Owner");
		if(string2.length() > 0) {
			this.setAmazonOwner(string2);
			this.setAmazonTamed(true);
		}
		if(nBTTagCompound1.hasKey("TextureVariation")) {
			this.setTextureVariation(nBTTagCompound1.getByte("TextureVariation"));
		}
	}

	@Override
	protected boolean canDespawn() {
		return false; //!this.isAmazonTamed();
	}

	@Override
	protected String getLivingSound() {
		return "";
	}

	protected String getHurtSoundByGender() {
		if(this.getTextureVariation() == 3) {
			return "random.hurt";
		} else {
			return "mob.amazon.hit";
		}
	}
	
	@Override
	protected String getHurtSound() {
		return this.getHurtSoundByGender();
	}

	@Override
	protected String getDeathSound() {
		return this.getHurtSoundByGender();
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected int getDropItemId() {
		return -1;
	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		if(!this.hasAttacked && !this.hasPath() && this.isAmazonTamed() && this.ridingEntity == null) {
			EntityPlayer entityPlayer3 = this.worldObj.getPlayerEntityByName(this.getOwner());
			if(entityPlayer3 != null) {
				float f2 = entityPlayer3.getDistanceToEntity(this);
				if(f2 > 5.0F) {
					this.getPathOrWalkableBlock(entityPlayer3, f2);
				}
			} else if(!this.isInWater()) {
				this.setIsSitting(true);
			}
		} else if(this.entityToAttack == null && !this.hasPath() && !this.isAmazonTamed() && this.worldObj.rand.nextInt(100) == 0) {
			AxisAlignedBB aabb = AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D);
			List<Entity> list1 = this.worldObj.getEntitiesWithinAABB(EntityZombie.class, aabb);
			list1.addAll(this.worldObj.getEntitiesWithinAABB(EntityZombie.class, aabb));
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
		
		if(this.entityToAttack != null) {
			if(this.entityToAttack.getDistanceToEntity(this) < 3.0F) {
				this.swingItem();
			}
			
			if(!this.entityToAttack.isEntityAlive()) {
				this.entityToAttack = null;
				this.setAmazonAngry(false);
			}
			
			if(this.timeBeforeLosingInterest > 0) {
				this.timeBeforeLosingInterest --;
				if(this.timeBeforeLosingInterest == 0) {
					this.entityToAttack = null;
					this.setAmazonAngry(false);
				}
			}
		}
		
		if(this.isSwinging) {
			++this.swingProgressInt;
			if(this.swingProgressInt >= 8) {
				this.swingProgressInt = 0;
				this.isSwinging = false;
			}
		} else {
			this.swingProgressInt = 0;
		}

		this.swingProgress = (float)this.swingProgressInt / 8.0F;
	}
	
	public boolean interestingItem(int itemID) {
		return (itemID == Item.ruby.shiftedIndex || itemID == Item.emerald.shiftedIndex || itemID == Item.diamond.shiftedIndex || itemID == Block.blockGold.blockID);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.looksWithInterest = false;
		if(this.hasCurrentTarget() && !this.hasPath() && !this.isAmazonAngry()) {
			Entity entity1 = this.getCurrentTarget();
			if(entity1 instanceof EntityPlayer) {
				EntityPlayer entityPlayer2 = (EntityPlayer)entity1;
				ItemStack itemStack3 = entityPlayer2.inventory.getCurrentItem();
				if(itemStack3 != null) {
					if(!this.isAmazonTamed() && this.interestingItem(itemStack3.itemID)) {
						this.looksWithInterest = true;
					} else if(this.isAmazonTamed() && Item.itemsList[itemStack3.itemID] instanceof ItemFood) {
						this.looksWithInterest = true;
					}
				}
			}
		}
		
		// Defend owner
		if(this.isAmazonTamed()) {
			EntityPlayer entityPlayer = this.worldObj.getPlayerEntityByName(this.getOwner());
			Entity entity = entityPlayer.lastAttackingEntity;
			if(entity != null && entity instanceof EntityLiving) {
				EntityLiving entityLiving = (EntityLiving)entity;
				if(this.isValidTarget(entityLiving)) {
					this.entityToAttack = entityLiving;
				}
			}
		}

	}
	
	public boolean isValidTarget(EntityLiving entityLiving) {
		if(entityLiving == null) return false;
		
		if(entityLiving == this) return false;
		
		if(!entityLiving.isEntityAlive()) return false;
		
		if(entityLiving.boundingBox.minY >= this.boundingBox.maxY || entityLiving.boundingBox.maxY <= this.boundingBox.minY) return false;
		
		return true;
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
	
	@Override
	public String getEntityTexture() {
		if(this.getMaxTextureVariations() > 1) {
			return "/mob/" + this.texturePrefix + this.dataWatcher.getWatchableObjectByte(19) + ".png";		
		} else return this.texture;
	}

	public float getInterestedAngle(float f1) {
		return (this.prevHeadRoll + (this.headRoll - this.prevHeadRoll) * f1) * 0.15F * (float)Math.PI;
	}

	@Override
	public float getEyeHeight() {
		return this.height * 0.8F;
	}

	@Override
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
		return this.getIsSitting();
	}

	@Override
	public boolean attackEntityFrom(Entity attackerEntity, int i2) {
		this.setIsSitting(false);
		if(attackerEntity != null && !(attackerEntity instanceof EntityPlayer) && !(attackerEntity instanceof EntityArrow)) {
			i2 = (i2 + 1) / 2;
		}
		
		// They can fight among them endlessly
		if(attackerEntity instanceof EntityAmazon) i2 = 0;

		boolean damaged = super.attackEntityFrom((Entity)attackerEntity, i2);

		if(attackerEntity instanceof EntityPlayer && ((EntityPlayer)attackerEntity).isCreative) return true;
			
		if(!this.isAmazonTamed() && !this.isAmazonAngry()) {
			if(attackerEntity instanceof EntityPlayer) {
				this.setAmazonAngry(true);
			}

			if(attackerEntity instanceof EntityArrow && ((EntityArrow)attackerEntity).shootingEntity != null) {
				attackerEntity = ((EntityArrow)attackerEntity).shootingEntity;
			}

			if(attackerEntity instanceof EntityLiving) {
				this.setEntityToAttack((Entity)attackerEntity);
				
				List<Entity> list3 = this.worldObj.getEntitiesWithinAABB(EntityAmazon.class, AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
				Iterator<Entity> iterator4 = list3.iterator();

				while(iterator4.hasNext()) {
					Entity entity5 = (Entity)iterator4.next();
					EntityAmazon entityAmazon6 = (EntityAmazon)entity5;
					if(!entityAmazon6.isAmazonTamed() && entityAmazon6.entityToAttack == null) {
						entityAmazon6.setEntityToAttack((Entity)attackerEntity);
						if(attackerEntity instanceof EntityPlayer) {
							entityAmazon6.setAmazonAngry(true);
						}
					}
				}
			}
		} else if(attackerEntity != this && attackerEntity != null) {
			if(this.isAmazonTamed() && attackerEntity instanceof EntityPlayer && ((EntityPlayer)attackerEntity).username.equalsIgnoreCase(this.getOwner())) {
				return true;
			}

			this.setEntityToAttack((Entity)attackerEntity);
		}

		return damaged;

	}

	@Override
	protected Entity findPlayerToAttack() {
		return this.isAmazonAngry() ? this.worldObj.getClosestPlayerToEntity(this, 16.0D) : null;
	}

	@Override
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
			
			byte b3 = 4;
			if(this.isAmazonTamed()) {
				b3 = 5;
			}

			entity1.attackEntityFrom(this, b3);
		}

	}

	@Override
	public boolean interact(EntityPlayer entityPlayer1) {
		ItemStack itemStack2 = entityPlayer1.inventory.getCurrentItem();
		if(!this.isAmazonTamed()) {
			if(itemStack2 != null && this.interestingItem(itemStack2.itemID) /* && !this.isAmazonAngry()*/ ) {
				if (!entityPlayer1.isCreative) --itemStack2.stackSize;
				if(itemStack2.stackSize <= 0) {
					entityPlayer1.inventory.setInventorySlotContents(entityPlayer1.inventory.currentItem, (ItemStack)null);
				}

				if(!this.worldObj.isRemote) {
					if(this.rand.nextInt(8) == 0) {
						this.setAmazonTamed(true);
						this.setPathToEntity((PathEntity)null);
						this.health = 20;
						this.setAmazonOwner(entityPlayer1.username);
						this.showHeartsOrSmokeFX(true);
						this.worldObj.setEntityState(this, (byte)7);
						entityPlayer1.triggerAchievement(AchievementList.currency);
					} else {
						this.showHeartsOrSmokeFX(false);
						this.worldObj.setEntityState(this, (byte)6);
					}
				}

				return true;
			}
		} else {
			if(itemStack2 != null && Item.itemsList[itemStack2.itemID] instanceof ItemFood) {
				if(this.dataWatcher.getWatchableObjectInt(Datawatchers.DW_HEALTH) < this.getFullHealth()) {
					if(!entityPlayer1.isCreative) --itemStack2.stackSize;
					if(itemStack2.stackSize <= 0) {
						entityPlayer1.inventory.setInventorySlotContents(entityPlayer1.inventory.currentItem, (ItemStack)null);
					}

					this.heal(((ItemFood)itemStack2.getItem()).getHealAmount());
					this.showHeartsOrSmokeFX(true);
					return true;
				}
			}

			if(entityPlayer1.username.equalsIgnoreCase(this.getOwner())) {
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
		} else {
			super.handleHealthUpdate(b1);
		}

	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 8;
	}

	public String getOwner() {
		return this.dataWatcher.getWatchableObjectString(Datawatchers.DW_OWNER);
	}

	public void setAmazonOwner(String string1) {
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

	public boolean isAmazonAngry() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 2) != 0;
	}

	public void setAmazonAngry(boolean z1) {
		byte b2 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(z1) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 | 2));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 & -3));
		}

	}

	public boolean isAmazonTamed() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 4) != 0;
	}

	public void setAmazonTamed(boolean z1) {
		byte b2 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(z1) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 | 4));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 & -5));
		}

	}
	
	@Override
	public int getFullHealth() {
		return 30;
	}
	
	@Override
	public void somebodyOpenedMyChest(EntityPlayer entityPlayer) {
		System.out.println("Opened a chest owned by Amazons. That makes them angry.");
		List<Entity> list3 = this.worldObj.getEntitiesWithinAABB(EntityAmazon.class, AxisAlignedBB.getBoundingBoxFromPool(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, entityPlayer.posX + 1.0D, entityPlayer.posY + 1.0D, entityPlayer.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
		Iterator<Entity> iterator4 = list3.iterator();

		while(iterator4.hasNext()) {
			Entity entity5 = (Entity)iterator4.next();
			EntityAmazon entityAmazon6 = (EntityAmazon)entity5;
			if(!entityAmazon6.isAmazonTamed() && entityAmazon6.entityToAttack == null) {
				System.out.println("Angering " + entityAmazon6 + " to " + entityPlayer);
				this.setEntityToAttack(entityPlayer);
				entityAmazon6.setAmazonAngry(true);
			}
		}
	}
}
