package net.minecraft.game.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.game.entity.helpers.EntityJumpHelper;
import net.minecraft.game.entity.helpers.EntityLookHelper;
import net.minecraft.game.entity.helpers.EntityMoveHelper;
import net.minecraft.game.entity.helpers.EntitySenses;
import net.minecraft.game.entity.helpers.PathNavigate;
import net.minecraft.game.entity.ai.EntityAITasks;
import net.minecraft.game.entity.status.Status;
import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.StepSound;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityGhast;

public abstract class EntityLiving extends Entity {
	public int maxHurtResistantTime = 20;
	public float unusedRotationPitch2;
	public float unusedRotationPitch;
	public float renderYawOffset = 0.0F;
	public float prevRenderYawOffset = 0.0F;
	public float rotationYawHead = 0.0F;
	public float prevRotationYawHead = 0.0F;
	protected float ridingRotUnused;
	protected float prevRidingRotUnused;
	protected float rotationUnused;
	protected float prevRotationUnused;
	protected boolean unusedBool1 = true;
	protected String texture = "/mob/char.png";
	protected boolean unusedBool2 = true;
	protected float unusedRotation = 0.0F;
	protected String entityType = null;
	protected float unusedFloat1 = 1.0F;
	protected int scoreValue = 0;
	protected float unusedFloat2 = 0.0F;
	public boolean isMultiplayerEntity = false;
	public float prevSwingProgress;
	public float swingProgress;
	public int health = 10;
	public int prevHealth;
	private int livingSoundTime;
	public int hurtTime;
	public int maxHurtTime;
	public float attackedAtYaw = 0.0F;
	public int deathTime = 0;
	public int attackTime = 0;
	public float cameraPitch;
	public float field_9328_R;
	protected boolean unused_flag = false;
	public int unusedInt = -1;
	public float unusedFloat4 = (float)(Math.random() * (double)0.9F + (double)0.1F);
	public float prevLimbYaw;
	public float limbYaw;
	public float limbSwing;
	protected int newPosRotationIncrements;
	protected double newPosX;
	protected double newPosY;
	protected double newPosZ;
	protected double newRotationYaw;
	protected double newRotationPitch;
	float unusedFloat3 = 0.0F;
	protected int lastDamage = 0;
	public int entityAge = 0;
	public float moveStrafing;
	public float moveForward;
	protected float randomYawVelocity;
	public boolean isJumping = false;
	protected float defaultPitch = 0.0F;
	public float moveSpeed = 0.7F;
	public float speedModifier = 1.0F;
	protected Entity currentTarget;
	protected int numTicksToChaseTarget = 0;
	public boolean isStopped = false;
	private ChunkCoordinates homePosition = new ChunkCoordinates(0, 0, 0);
	private float maximumHomeDistance = -1.0F;
	
	// New AI
	protected float AImoveSpeed;
	protected EntityLookHelper lookHelper;
	protected EntityMoveHelper moveHelper;
	protected EntityJumpHelper jumpHelper;
	protected PathNavigate navigator;
	protected EntitySenses entitySenses;
	protected EntityLiving attackTarget;
	protected EntityLiving entityLivingToAttack = null;
	public EntityLiving lastAttackingEntity = null;
	public boolean isBlinded = false;
	
	protected EntityAITasks tasks = new EntityAITasks();
	protected EntityAITasks targetTasks = new EntityAITasks();
	
	protected int recentlyHit = 0;
	protected EntityPlayer attackingPlayer = null;
	
	// Status effects
	protected HashMap<Integer,StatusEffect> activeStatusEffectsMap;
	private int revengeTimer;
	private int carryoverDamage;
	
	// New
	public float landMovementFactor = 0.1F;
	public float airMovementFactor = 0.02F;
	
	public EntityLiving(World world1) {
		super(world1);
		this.preventEntitySpawning = true;
		this.unusedRotationPitch = (float)(Math.random() + 1.0D) * 0.01F;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.unusedRotationPitch2 = (float)Math.random() * 12398.0F;
		this.rotationYaw = (float)(Math.random() * (double)(float)Math.PI * 2.0D);
		this.rotationYawHead = this.rotationYaw;
		this.stepHeight = 0.5F;
		
		this.lookHelper = new EntityLookHelper(this);
		this.moveHelper = new EntityMoveHelper(this);
		this.jumpHelper = new EntityJumpHelper(this);
		this.navigator = new PathNavigate(this, world1, 16.0F);
		this.entitySenses = new EntitySenses(this);
		
		this.activeStatusEffectsMap = new HashMap<Integer,StatusEffect>();
	}

	protected void entityInit() { 
	}

	public boolean canEntityBeSeen(Entity entity1) {
		return this.worldObj.rayTraceBlocks(Vec3D.createVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), Vec3D.createVector(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ)) == null;
	}

	public String getEntityTexture() {
		return this.texture;
	}

	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	public boolean canBePushed() {
		return !this.isDead;
	}

	public float getEyeHeight() {
		return this.height * 0.85F;
	}

	public int getTalkInterval() {
		return 80;
	}

	public void playLivingSound() {
		String string1 = this.getLivingSound();
		if(string1 != null) {
			this.worldObj.playSoundAtEntity(this, string1, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
		}

	}

	public void onEntityUpdate() {
		this.prevSwingProgress = this.swingProgress;
		super.onEntityUpdate();
		if(this.rand.nextInt(1000) < this.livingSoundTime++) {
			this.livingSoundTime = -this.getTalkInterval();
			this.playLivingSound();
		}

		if(this.isEntityAlive() && this.isEntityInsideOpaqueBlock()) {
			this.attackEntityFrom((Entity)null, 1);
		}

		if(this.isImmuneToFire || this.worldObj.isRemote) {
			this.fire = 0;
		}

		int i1;
		if(
			this.isEntityAlive() && 
			(
				(this.isInsideOfMaterial(Material.water) && !this.canBreatheUnderwater()) || 
				this.posY < 0)
			) {
			this.setAir(this.decreaseAirSupply(this.getAir()));
			if(this.getAir() == -20) {
				this.setAir(0);

				for(i1 = 0; i1 < 8; ++i1) {
					float f2 = this.rand.nextFloat() - this.rand.nextFloat();
					float f3 = this.rand.nextFloat() - this.rand.nextFloat();
					float f4 = this.rand.nextFloat() - this.rand.nextFloat();
					this.worldObj.spawnParticle("bubble", this.posX + (double)f2, this.posY + (double)f3, this.posZ + (double)f4, this.motionX, this.motionY, this.motionZ);
				}

				this.attackEntityFrom((Entity)null, 2);
			}

			this.fire = 0;
		} else {
			this.setAir(this.maxAir);
		}

		this.cameraPitch = this.field_9328_R;
		if(this.attackTime > 0) {
			--this.attackTime;
		}

		if(this.hurtTime > 0) {
			--this.hurtTime;
		}

		if(this.hurtResistantTime > 0) {
			--this.hurtResistantTime;
		}

		if(this.health <= 0) {
			++this.deathTime;
			if(this.deathTime > 20) {
				this.onEntityDeath();
				this.setEntityDead();

				for(i1 = 0; i1 < 20; ++i1) {
					double d8 = this.rand.nextGaussian() * 0.02D;
					double d9 = this.rand.nextGaussian() * 0.02D;
					double d6 = this.rand.nextGaussian() * 0.02D;
					this.worldObj.spawnParticle("explode", this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d8, d9, d6);
				}
			}
		}

		if(this.recentlyHit > 0) {
			--this.recentlyHit;
		} else {
			this.attackingPlayer = null;
		}

		if (this.lastAttackingEntity != null && !this.lastAttackingEntity.isEntityAlive()) {
			this.lastAttackingEntity = null;
		}

		if (this.entityLivingToAttack != null) {
			if (!this.entityLivingToAttack.isEntityAlive()) {
				this.setRevengeTarget((EntityLiving) null);
			} else if (this.revengeTimer > 0) {
				--this.revengeTimer;
			} else {
				this.setRevengeTarget((EntityLiving) null);
			}
		}
        
		this.prevRotationUnused = this.rotationUnused;
		this.prevRenderYawOffset = this.renderYawOffset;
		this.prevRotationYawHead = this.rotationYawHead;
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		
		this.updateStatusEffects ();
	}

	protected int decreaseAirSupply(int i1) {
		return i1 - 1;
	}
	
	public void spawnExplosionParticle() {
		for(int i1 = 0; i1 < 20; ++i1) {
			double d2 = this.rand.nextGaussian() * 0.02D;
			double d4 = this.rand.nextGaussian() * 0.02D;
			double d6 = this.rand.nextGaussian() * 0.02D;
			double d8 = 10.0D;
			this.worldObj.spawnParticle("explode", this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - d2 * d8, this.posY + (double)(this.rand.nextFloat() * this.height) - d4 * d8, this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - d6 * d8, d2, d4, d6);
		}

	}

	public void updateRidden() {
		super.updateRidden();
		this.ridingRotUnused = this.prevRidingRotUnused;
		this.prevRidingRotUnused = 0.0F;
	}

	public void setPositionAndRotation2(double d1, double d3, double d5, float f7, float f8, int i9) {
		this.yOffset = 0.0F;
		this.newPosX = d1;
		this.newPosY = d3;
		this.newPosZ = d5;
		this.newRotationYaw = (double)f7;
		this.newRotationPitch = (double)f8;
		this.newPosRotationIncrements = i9;
	}

	public void onUpdate() {
		super.onUpdate();
		this.onLivingUpdate();
		double d1 = this.posX - this.prevPosX;
		double d3 = this.posZ - this.prevPosZ;
		float f5 = MathHelper.sqrt_double(d1 * d1 + d3 * d3);
		float f6 = this.renderYawOffset;
		float f7 = 0.0F;
		this.ridingRotUnused = this.prevRidingRotUnused;
		float f8 = 0.0F;
		if(f5 > 0.05F) {
			f8 = 1.0F;
			f7 = f5 * 3.0F;
			f6 = (float)Math.atan2(d3, d1) * 180.0F / (float)Math.PI - 90.0F;
		}

		if(this.swingProgress > 0.0F) {
			f6 = this.rotationYaw;
		}

		if(!this.onGround) {
			f8 = 0.0F;
		}

		this.prevRidingRotUnused += (f8 - this.prevRidingRotUnused) * 0.3F;

		float f9;
		for(f9 = f6 - this.renderYawOffset; f9 < -180.0F; f9 += 360.0F) {
		}

		while(f9 >= 180.0F) {
			f9 -= 360.0F;
		}

		this.renderYawOffset += f9 * 0.3F;

		float f10;
		for(f10 = this.rotationYaw - this.renderYawOffset; f10 < -180.0F; f10 += 360.0F) {
		}

		while(f10 >= 180.0F) {
			f10 -= 360.0F;
		}

		boolean z11 = f10 < -90.0F || f10 >= 90.0F;
		if(f10 < -75.0F) {
			f10 = -75.0F;
		}

		if(f10 >= 75.0F) {
			f10 = 75.0F;
		}

		this.renderYawOffset = this.rotationYaw - f10;
		if(f10 * f10 > 2500.0F) {
			this.renderYawOffset += f10 * 0.2F;
		}

		if(z11) {
			f7 *= -1.0F;
		}

		while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
			this.prevRotationYaw -= 360.0F;
		}

		while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			this.prevRotationYaw += 360.0F;
		}

		while(this.renderYawOffset - this.prevRenderYawOffset < -180.0F) {
			this.prevRenderYawOffset -= 360.0F;
		}

		while(this.renderYawOffset - this.prevRenderYawOffset >= 180.0F) {
			this.prevRenderYawOffset += 360.0F;
		}

		while(this.rotationPitch - this.prevRotationPitch < -180.0F) {
			this.prevRotationPitch -= 360.0F;
		}

		while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			this.prevRotationPitch += 360.0F;
		}

		while(this.rotationYawHead - this.prevRotationYawHead < -180.0F) {
			this.prevRotationYawHead -= 360.0F;
		}

		while(this.rotationYawHead - this.prevRotationYawHead >= 180.0F) {
			this.prevRotationYawHead += 360.0F;
		}
		this.rotationUnused += f7;
	}

	protected void setSize(float f1, float f2) {
		super.setSize(f1, f2);
	}

	public void heal(int i1) {
		if(this.health > 0) {
			this.health += i1;
			if(this.health > 20) {
				this.health = 20;
			}

			this.hurtResistantTime = this.maxHurtResistantTime / 2;
		}
	}

	public boolean attackEntityFrom(Entity attacker, int damage) {
		if(this.worldObj.isRemote) {
			return false;
		} else {
			this.entityAge = 0;
			if(this.health <= 0) {
				return false;
			} else {
				this.limbYaw = 1.5F;
				boolean z3 = true;
				if((float)this.hurtResistantTime > (float)this.maxHurtResistantTime / 2.0F) {
					if(damage <= this.lastDamage) {
						return false;
					}

					this.damageEntity(damage - this.lastDamage);
					this.lastDamage = damage;
					z3 = false;
				} else {
					this.lastDamage = damage;
					this.prevHealth = this.health;
					this.hurtResistantTime = this.maxHurtResistantTime;
					this.damageEntity(damage);
					this.hurtTime = this.maxHurtTime = 10;
				}

				this.attackedAtYaw = 0.0F;
				
				if(attacker != null) {
					if(attacker instanceof EntityLiving) {
						this.setRevengeTarget((EntityLiving)attacker);
					} 
					
					if(attacker instanceof EntityPlayer) {
						this.recentlyHit = 60;
						this.attackingPlayer = (EntityPlayer)attacker;
					} else if(attacker instanceof EntityWolf) {
						EntityWolf entityWolf5 = (EntityWolf)attacker;
						if(entityWolf5.isTamed()) {
							this.recentlyHit = 60;
							this.attackingPlayer = null;
						}
					}
				}
				
				if(z3) {
					this.worldObj.setEntityState(this, (byte)2);
					this.setBeenAttacked();
					if(attacker != null) {
						double d4 = attacker.posX - this.posX;

						double d6;
						for(d6 = attacker.posZ - this.posZ; d4 * d4 + d6 * d6 < 1.0E-4D; d6 = (Math.random() - Math.random()) * 0.01D) {
							d4 = (Math.random() - Math.random()) * 0.01D;
						}

						this.attackedAtYaw = (float)(Math.atan2(d6, d4) * 180.0D / (double)(float)Math.PI) - this.rotationYaw;
						this.knockBack(attacker, damage, d4, d6);
					} else {
						this.attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
					}
				}

				if(this.health <= 0) {
					if(z3) {
						this.worldObj.playSoundAtEntity(this, this.getDeathSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
					}

					this.onDeath(attacker);
				} else if(z3) {
					this.worldObj.playSoundAtEntity(this, this.getHurtSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
				}
				
				if(attacker instanceof EntityLiving) {
					this.lastAttackingEntity = (EntityLiving)attacker;
				}

				return true;
			}
		}
	}

	public void performHurtAnimation() {
		this.hurtTime = this.maxHurtTime = 10;
		this.attackedAtYaw = 0.0F;
	}

	protected void damageEntity(int damage) {
		//System.out.print("Damaging entity " + this.getClass() + " -> " + damage);
		
		damage = this.applyArmorCalculations(damage);
		damage = this.applyStatusEffectsCalculations(damage);
		this.health -= damage;
		
		//System.out.println(" .. " + damage);
	}

	protected void damageEntityNoArmor(int damage) {
		this.health -= damage;
	}
	
	protected int applyArmorCalculations(int damage) {
		int hitStrength = 25 - this.getTotalArmorValue();
		int damageRaw = damage * hitStrength + this.carryoverDamage;
		this.damageArmor(damage);
		damage = damageRaw / 25;
		this.carryoverDamage = damageRaw % 25;
		
		return damage;
	}
	
	protected int applyStatusEffectsCalculations(int damage) {
		return damage;
	}

	protected void damageArmor(int damage) {
	}

	protected int getTotalArmorValue() {
		return 0;
	}

	protected float getSoundVolume() {
		return 1.0F;
	}

	protected String getLivingSound() {
		return null;
	}

	protected String getHurtSound() {
		return "random.hurt";
	}

	protected String getDeathSound() {
		return "random.hurt";
	}

	public void knockBack(Entity entity1, int i2, double d3, double d5) { 
		float f7 = MathHelper.sqrt_double(d3 * d3 + d5 * d5);
		float f8 = 0.4F;
		this.motionX /= 2.0D;
		this.motionY /= 2.0D;
		this.motionZ /= 2.0D;
		this.motionX -= d3 / (double)f7 * (double)f8;
		this.motionY += (double)0.4F;
		this.motionZ -= d5 / (double)f7 * (double)f8;
		if(this.motionY > (double)0.4F) {
			this.motionY = (double)0.4F;
		}

	}

	public void onDeath(Entity entity1) {
		if(this.scoreValue >= 0 && entity1 != null) {
			entity1.addToPlayerScore(this, this.scoreValue);
		}

		if(entity1 != null) {
			entity1.onKillEntity(this);
		}

		this.unused_flag = true;
		if(!this.worldObj.isRemote) {
			if(
					!(entity1 instanceof EntityMob) || // Wasn't killed by a mob
					entity1 instanceof EntityPlayer || // Or Was killed by the player
					rand.nextInt(16) == 0              // Or random chance...
			) {
				this.dropFewItems();
			}
		}

		this.worldObj.setEntityState(this, (byte)3);
	}

	protected void dropFewItems() {
		int i1 = this.getDropItemId();
		if(i1 > 0) {
			int i2 = this.rand.nextInt(3);

			for(int i3 = 0; i3 < i2; ++i3) {
				this.dropItem(i1, 1);
			}
		}

	}

	protected int getDropItemId() {
		return 0;
	}

	protected boolean fall(float distance) {
		boolean rebound = super.fall(distance);
		
		int i2 = (int)Math.ceil((double)(distance - 3.0F));
		
		int x = MathHelper.floor_double(this.posX);
		int y = MathHelper.floor_double(this.posY - (double)0.2F - (double)this.yOffset);
		int z = MathHelper.floor_double(this.posZ);
		
		int i3 = this.worldObj.getBlockId(x, y, z);
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		
		if(i3 == Block.slimeBlock.blockID) {
			this.motionY = -(this.motionY * 0.9D);
			rebound = true;
		} else if(i3 == Block.snow.blockID && meta >= 8 || i3 == Block.blockSnow.blockID) {
			this.motionY = 0;
			rebound = true;
		} else if(i2 > 0) {
			this.attackEntityFrom((Entity)null, i2);
		}
		
		if(i3 > 2 || (i3 > 0 && rebound)) {
			Block block = Block.blocksList[i3];
			if (block != null) {
				StepSound stepSound4 = block.stepSound;
				this.worldObj.playSoundAtEntity(this, stepSound4.getStepSound(), stepSound4.getVolume() * 0.5F, stepSound4.getPitch() * 0.75F);
			}
		}
		
		if(this.isFlying()) this.distanceWalkedModified = 0;

		return rebound;
	}

	public void moveEntityWithHeading(float strafing, float forward) {
		double d3;
		if(this.isInWater() && (!(this instanceof EntityPlayer) || !((EntityPlayer)this).divingHelmetOn())) {
			d3 = this.posY;
			this.moveFlying(strafing, forward, 0.02F);
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= (double)0.8F;
			this.motionY *= (double)0.8F;
			this.motionZ *= (double)0.8F;
			this.motionY -= 0.02D;
			if(this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + (double)0.6F - this.posY + d3, this.motionZ)) {
				this.motionY = (double)0.3F;
			}
		} else if(this.handleLavaMovement()) {
			d3 = this.posY;
			this.moveFlying(strafing, forward, 0.02F);
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
			this.motionY -= 0.02D;
			if(this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + (double)0.6F - this.posY + d3, this.motionZ)) {
				this.motionY = (double)0.3F;
			}
		} else {
			float friction = 0.91F;
			if(this.onGround) {
				friction = 0.54600006F;
				int idAtFeet = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
				if(idAtFeet > 0) {
					friction = Block.blocksList[idAtFeet].slipperiness * 0.91F;
				}
			}

			// Calculate cartesian motionX / motionZ based upon forward/strafe and current yaw.
			float f9 = this.speedModifier * 0.16277136F / (friction * friction * friction);
			float factor = 0.2F;
			
			// This is a creative mode cheap hack to fly faster when pressing CTRL.
			if(this.isSprinting()) factor *= 2; 
			this.moveFlying(strafing, forward, 
					this.onGround ? 
							this.landMovementFactor * f9 					// Actual speed when on ground. Used constant 0.1F in Vanilla
						: 
							this.isFlying() ? 
									factor * f9 
								: 
									this.airMovementFactor 					// Actual speed when on air. Used constant 0.02F in Vanilla
			);

			// No need to calculate this again!
			/*
			friction = 0.91F;
			if(this.onGround) {
				friction = 0.54600006F;
				int i5 = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
				if(i5 > 0) {
					friction = Block.blocksList[i5].slipperiness * 0.91F;
				}
			}
			*/

			// Motion is heavily capped when on a ladder
			if(this.isOnLadder()) { 
				float strafing0 = 0.15F;
				if(this.motionX < (double)(-strafing0)) {
					this.motionX = (double)(-strafing0);
				}

				if(this.motionX > (double)strafing0) {
					this.motionX = (double)strafing0;
				}

				if(this.motionZ < (double)(-strafing0)) {
					this.motionZ = (double)(-strafing0);
				}

				if(this.motionZ > (double)strafing0) {
					this.motionZ = (double)strafing0;
				}

				this.fallDistance = 0.0F;
				if(this.motionY < -0.15D) {
					this.motionY = -0.15D;
				}

				if(this.isSneaking() && this.motionY < 0.0D) {
					this.motionY = 0.0D;
				}
			}

			// Actually move this entity
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			if(this.isCollidedHorizontally && this.isOnLadder()) {
				this.motionY = 0.2D;
			}

			this.motionY -= 0.08D;
			this.motionY *= (double)0.98F;
			this.motionX *= (double)friction;
			this.motionZ *= (double)friction;
		}

		this.prevLimbYaw = this.limbYaw;
		d3 = this.posX - this.prevPosX;
		double d11 = this.posZ - this.prevPosZ;
		float f7 = MathHelper.sqrt_double(d3 * d3 + d11 * d11) * 4.0F;
		if(f7 > 1.0F) {
			f7 = 1.0F;
		}

		this.limbYaw += (f7 - this.limbYaw) * 0.4F;
		this.limbSwing += this.limbYaw;
	}

	public boolean isOnLadder() {
		int i1 = MathHelper.floor_double(this.posX);
		int i2 = MathHelper.floor_double(this.boundingBox.minY);
		int i3 = MathHelper.floor_double(this.posZ);
		/*
		return this.worldObj.getBlockId(i1, i2, i3) == Block.ladder.blockID;
		*/
		Block block1 = Block.blocksList[this.worldObj.getBlockId(i1, i2, i3)];
		Block block2 = Block.blocksList[this.worldObj.getBlockId(i1, i2 + 1, i3)];
		return (block1 != null && block1.isClimbable()) || (block2 != null && block2.isClimbable());
	}

	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		nBTTagCompound1.setShort("Health", (short)this.health);
		nBTTagCompound1.setShort("HurtTime", (short)this.hurtTime);
		nBTTagCompound1.setShort("DeathTime", (short)this.deathTime);
		nBTTagCompound1.setShort("AttackTime", (short)this.attackTime);
		
				
		NBTTagCompound statusEffectsCompound = new NBTTagCompound();
		statusEffectsCompound.setInteger("Size", this.activeStatusEffectsMap.size());
		Iterator<Integer> it = activeStatusEffectsMap.keySet().iterator();
		int i = 0;
		while(it.hasNext()) {
			Integer id = it.next(); // System.out.println("Getting status " + i + " with ID " + id);
			StatusEffect statusEffect = this.activeStatusEffectsMap.get(id);
			NBTTagCompound statusEffectCompound = new NBTTagCompound();
			statusEffect.writeStatusEffectToNBT(statusEffectCompound);
			statusEffectsCompound.setCompoundTag("StatusEffect_" + i, statusEffectCompound);
			i ++;
		}
		
		nBTTagCompound1.setCompoundTag("ActiveStatusEffects", statusEffectsCompound);
		
		// Held item
		if(this.getHeldItem() != null) {
			nBTTagCompound1.setInteger("HeldItemId", this.getHeldItem().itemID);
		}
	}

	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		this.health = nBTTagCompound1.getShort("Health");
		if(!nBTTagCompound1.hasKey("Health")) {
			this.health = 10;
		}

		this.hurtTime = nBTTagCompound1.getShort("HurtTime");
		this.deathTime = nBTTagCompound1.getShort("DeathTime");
		this.attackTime = nBTTagCompound1.getShort("AttackTime");

		NBTTagCompound statusEffectsCompound = nBTTagCompound1.getCompoundTag("ActiveStatusEffects");
		int size = statusEffectsCompound.getInteger("Size");
		
		this.activeStatusEffectsMap.clear();
		for(int i = 0; i < size; i ++) {
			NBTTagCompound statusEffectCompound = statusEffectsCompound.getCompoundTag("StatusEffect_" + i);
			StatusEffect statusEffect = new StatusEffect(statusEffectCompound);
			this.activeStatusEffectsMap.put(statusEffect.statusID, statusEffect);
		}
		
		// Held item
		if(!nBTTagCompound1.hasKey("HeldItemId")) {
			this.setHeldItem(new ItemStack(nBTTagCompound1.getInteger("HeldItemId")));
		}
	}

	public boolean isEntityAlive() {
		return !this.isDead && this.health > 0;
	}

	public boolean canBreatheUnderwater() {
		return false;
	}

	public void onLivingUpdate() {
		if(this.newPosRotationIncrements > 0) {
			double d1 = this.posX + (this.newPosX - this.posX) / (double)this.newPosRotationIncrements;
			double d3 = this.posY + (this.newPosY - this.posY) / (double)this.newPosRotationIncrements;
			double d5 = this.posZ + (this.newPosZ - this.posZ) / (double)this.newPosRotationIncrements;

			double d7;
			for(d7 = this.newRotationYaw - (double)this.rotationYaw; d7 < -180.0D; d7 += 360.0D) {
			}

			while(d7 >= 180.0D) {
				d7 -= 360.0D;
			}

			this.rotationYaw = (float)((double)this.rotationYaw + d7 / (double)this.newPosRotationIncrements);
			this.rotationPitch = (float)((double)this.rotationPitch + (this.newRotationPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
			--this.newPosRotationIncrements;
			this.setPosition(d1, d3, d5);
			this.setRotation(this.rotationYaw, this.rotationPitch);
			
			List<AxisAlignedBB> list9 = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.getInsetBoundingBox(8.0D / 256D, 0.0D, 8.0D / 256D));
			if(list9.size() > 0) {
				double d10 = 0.0D;

				for(int i12 = 0; i12 < list9.size(); ++i12) {
					AxisAlignedBB axisAlignedBB13 = (AxisAlignedBB)list9.get(i12);
					if(axisAlignedBB13.maxY > d10) {
						d10 = axisAlignedBB13.maxY;
					}
				}

				d3 += d10 - this.boundingBox.minY;
				this.setPosition(d1, d3, d5);
			}
		}

		if(this.isMovementBlocked()) {
			this.isJumping = false;
			this.moveStrafing = 0.0F;
			this.moveForward = 0.0F;
			this.randomYawVelocity = 0.0F;
		} else if(!this.isMultiplayerEntity) {
			if(this.isAIEnabled()) {
				this.updateAITasks();
			} else {
				this.updateEntityActionState();
				this.rotationYawHead = this.rotationYaw;
			}
		}

		boolean z14;
		
		if(this instanceof EntityPlayer) {
			z14 = this.isInWater() && !((EntityPlayer)this).divingHelmetOn();
		} else {
			z14 = this.isInWater();
		}
		
		boolean z2 = this.handleLavaMovement();
		if(this.isJumping) {
			if(z14) {
				this.motionY += (double)0.04F;
			} else if(z2) {
				this.motionY += (double)0.04F;
			} else if(this.onGround) {
				this.jump();
			}
		}

		this.moveStrafing *= 0.98F;
		this.moveForward *= 0.98F;
		this.randomYawVelocity *= 0.9F;
		if (!this.isStopped) this.moveEntityWithHeading(this.moveStrafing, this.moveForward);
		List<Entity> list15 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand((double)0.2F, 0.0D, (double)0.2F));
		if(list15 != null && list15.size() > 0) {
			for(int i4 = 0; i4 < list15.size(); ++i4) {
				Entity entity16 = (Entity)list15.get(i4);
				if(entity16.canBePushed()) {
					entity16.applyEntityCollision(this);
				}
			}
		}

	}

	private void updateAITasks() {
		this.despawnEntity();
		this.entitySenses.clearSensingCache();
		this.targetTasks.onUpdateTasks();
		this.tasks.onUpdateTasks();
		this.navigator.onUpdateNavigation();
		this.updateAITick();
		this.moveHelper.onUpdateMoveHelper();
		this.lookHelper.onUpdateLook();
		this.jumpHelper.doJump();
	}

	protected void updateAITick() {
	}
	
	protected boolean isAIEnabled() {
		return false;
	}

	protected boolean isMovementBlocked() {
		return this.health <= 0;
	}

	protected void jump() {
		this.motionY = (double)0.42F;
		
		if(this.isSprinting()) {
			float f1 = this.rotationYaw * 0.017453292F;
			this.motionX -= (double)(MathHelper.sin(f1) * 0.2F);
			this.motionZ += (double)(MathHelper.cos(f1) * 0.2F);
		}

	}

	protected boolean canDespawn() {
		return true;
	}

	protected void despawnEntity() {
		EntityPlayer entityPlayer1 = this.worldObj.getClosestPlayerToEntity(this, -1.0D);
		if(this.canDespawn() && entityPlayer1 != null) {
			double d2 = entityPlayer1.posX - this.posX;
			double d4 = entityPlayer1.posY - this.posY;
			double d6 = entityPlayer1.posZ - this.posZ;
			double d8 = d2 * d2 + d4 * d4 + d6 * d6;
			if(d8 > 16384.0D) {
				this.setEntityDead();
			}

			if(this.entityAge > 600 && this.rand.nextInt(800) == 0) {
				if(d8 < 1024.0D) {
					this.entityAge = 0;
				} else {
					this.setEntityDead();
				}
			}
		}

	}

	protected void updateEntityActionState() {
		++this.entityAge;
		EntityPlayer entityPlayer1 = this.worldObj.getClosestPlayerToEntity(this, -1.0D);
		this.despawnEntity();
		this.moveStrafing = 0.0F;
		this.moveForward = 0.0F;
		float f2 = 8.0F;
		if(this.rand.nextFloat() < 0.02F) {
			entityPlayer1 = this.worldObj.getClosestPlayerToEntity(this, (double)f2);
			if(entityPlayer1 != null) {
				this.currentTarget = entityPlayer1;
				this.numTicksToChaseTarget = 10 + this.rand.nextInt(20);
			} else {
				this.randomYawVelocity = (this.rand.nextFloat() - 0.5F) * 20.0F;
			}
		}

		if(this.currentTarget != null) {
			this.faceEntity(this.currentTarget, 10.0F, (float)this.getVerticalFaceSpeed());
			if(this.numTicksToChaseTarget-- <= 0 || this.currentTarget.isDead || this.currentTarget.getDistanceSqToEntity(this) > (double)(f2 * f2)) {
				this.currentTarget = null;
			}
		} else {
			if(this.rand.nextFloat() < 0.05F) {
				this.randomYawVelocity = (this.rand.nextFloat() - 0.5F) * 20.0F;
			}

			this.rotationYaw += this.randomYawVelocity;
			this.rotationPitch = this.defaultPitch;
		}

		if(this.triesToFloat()) {
			boolean z3 = this.isInWater();
			boolean z4 = this.handleLavaMovement();
			if(z3 || z4) {
				this.isJumping = this.rand.nextFloat() < 0.8F;
			}
		}
	}
	
	public boolean triesToFloat() {
		return true; 
	}

	public int getVerticalFaceSpeed() {
		return 40;
	}

	public void faceEntity(Entity entity1, float f2, float f3) {
		double d4 = entity1.posX - this.posX;
		double d8 = entity1.posZ - this.posZ;
		double d6;
		if(entity1 instanceof EntityLiving) {
			EntityLiving entityLiving10 = (EntityLiving)entity1;
			d6 = this.posY + (double)this.getEyeHeight() - (entityLiving10.posY + (double)entityLiving10.getEyeHeight());
		} else {
			d6 = (entity1.boundingBox.minY + entity1.boundingBox.maxY) / 2.0D - (this.posY + (double)this.getEyeHeight());
		}

		double d14 = (double)MathHelper.sqrt_double(d4 * d4 + d8 * d8);
		float f12 = (float)(Math.atan2(d8, d4) * 180.0D / (double)(float)Math.PI) - 90.0F;
		float f13 = (float)(-(Math.atan2(d6, d14) * 180.0D / (double)(float)Math.PI));
		this.rotationPitch = -this.updateRotation(this.rotationPitch, f13, f3);
		this.rotationYaw = this.updateRotation(this.rotationYaw, f12, f2);
	}

	public boolean hasCurrentTarget() {
		return this.currentTarget != null;
	}

	public Entity getCurrentTarget() {
		return this.currentTarget;
	}

	private float updateRotation(float f1, float f2, float f3) {
		float f4;
		for(f4 = f2 - f1; f4 < -180.0F; f4 += 360.0F) {
		}

		while(f4 >= 180.0F) {
			f4 -= 360.0F;
		}

		if(f4 > f3) {
			f4 = f3;
		}

		if(f4 < -f3) {
			f4 = -f3;
		}

		return f1 + f4;
	}

	public void onEntityDeath() {
	}

	public boolean getCanSpawnHere() {
		return this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}

	protected void kill() {
		this.attackEntityFrom((Entity)null, 4);
	}

	public float getSwingProgress(float f1) {
		float f2 = this.swingProgress - this.prevSwingProgress;
		if(f2 < 0.0F) {
			++f2;
		}

		return this.prevSwingProgress + f2 * f1;
	}

	public Vec3D getPosition(float f1) {
		if(f1 == 1.0F) {
			return Vec3D.createVector(this.posX, this.posY, this.posZ);
		} else {
			double d2 = this.prevPosX + (this.posX - this.prevPosX) * (double)f1;
			double d4 = this.prevPosY + (this.posY - this.prevPosY) * (double)f1;
			double d6 = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f1;
			return Vec3D.createVector(d2, d4, d6);
		}
	}

	public Vec3D getLookVec() {
		return this.getLook(1.0F);
	}

	public Vec3D getLook(float f1) {
		float f2;
		float f3;
		float f4;
		float f5;
		if(f1 == 1.0F) {
			f2 = MathHelper.cos(-this.rotationYaw * 0.017453292F - (float)Math.PI);
			f3 = MathHelper.sin(-this.rotationYaw * 0.017453292F - (float)Math.PI);
			f4 = -MathHelper.cos(-this.rotationPitch * 0.017453292F);
			f5 = MathHelper.sin(-this.rotationPitch * 0.017453292F);
			return Vec3D.createVector((double)(f3 * f4), (double)f5, (double)(f2 * f4));
		} else {
			f2 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * f1;
			f3 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * f1;
			f4 = MathHelper.cos(-f3 * 0.017453292F - (float)Math.PI);
			f5 = MathHelper.sin(-f3 * 0.017453292F - (float)Math.PI);
			float f6 = -MathHelper.cos(-f2 * 0.017453292F);
			float f7 = MathHelper.sin(-f2 * 0.017453292F);
			return Vec3D.createVector((double)(f5 * f6), (double)f7, (double)(f4 * f6));
		}
	}

	public MovingObjectPosition rayTrace(double d1, float f3) {
		Vec3D vec3D4 = this.getPosition(f3);
		Vec3D vec3D5 = this.getLook(f3);
		Vec3D vec3D6 = vec3D4.addVector(vec3D5.xCoord * d1, vec3D5.yCoord * d1, vec3D5.zCoord * d1);
		return this.worldObj.rayTraceBlocks(vec3D4, vec3D6);
	}

	public int getMaxSpawnedInChunk() {
		return 4;
	}

	public ItemStack getHeldItem() {
		return null;
	}
	
	public boolean setHeldItem(ItemStack itemStack) {
		return false;
	}

	public void handleHealthUpdate(byte b1) {
		if(b1 == 2) {
			this.limbYaw = 1.5F;
			this.hurtResistantTime = this.maxHurtResistantTime;
			this.hurtTime = this.maxHurtTime = 10;
			this.attackedAtYaw = 0.0F;
			this.worldObj.playSoundAtEntity(this, this.getHurtSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
			this.attackEntityFrom((Entity)null, 0);
		} else if(b1 == 3) {
			this.worldObj.playSoundAtEntity(this, this.getDeathSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
			this.health = 0;
			this.onDeath((Entity)null);
		} else {
			super.handleHealthUpdate(b1);
		}

	}

	public boolean isPlayerSleeping() {
		return false;
	}

	public int getItemIcon(ItemStack itemStack1) {
		return itemStack1.getIconIndex();
	}
	
	// Status effects
	
	public void updateStatusEffects () {
		if(activeStatusEffectsMap.size() == 0) return; 
		
		Iterator<Integer> it = activeStatusEffectsMap.keySet().iterator();
		
		// This will help me select one effect at random:
		int randomEffectCounter = 0;
		int randomEffectSelected = this.rand.nextInt(activeStatusEffectsMap.size());    	
		
		while (it.hasNext()) {
			Integer statusID = it.next();
			StatusEffect statusEffect = (StatusEffect) activeStatusEffectsMap.get(statusID);
			
			// Create a particle? Not perfect but greedy solution which mostly works
			if (randomEffectCounter == randomEffectSelected) {
				Status status = Status.statusTypes[statusEffect.statusID];
				if (status.showParticles) {
					int particleColour = status.particleColor;

					this.worldObj.spawnParticle(
							"status_effect",
							posX + (this.rand.nextFloat() - 0.5F) * (float) width, 
							(posY + this.rand.nextFloat() * (float) height) - (float) yOffset, 
							posZ + (this.rand.nextFloat() - 0.5F) * (float) width,
							(float)(particleColour >> 16 & 0xff) / 255F,
							(float)(particleColour >> 8 & 0xff) / 255F,
							(float)(particleColour & 0xff) / 255F
						   );
				}
			}
			randomEffectCounter ++;
			
			// Status effect will return false when duration has run out
			if (!statusEffect.onUpdate(this) && !this.worldObj.isRemote) {
				it.remove ();
				this.onFinishedStatusEffect(statusEffect);
			}
		}
	}
	
	public void onNewStatusEffect(StatusEffect statusEffect) {
		
	}
	
	public void onChangedStatusEffect(StatusEffect statusEffect) {
		
	}
	
	public void onFinishedStatusEffect(StatusEffect statusEffect) {
		
	}

	public void removeStatusEffect (int id) {
		if (activeStatusEffectsMap.containsKey(id)) {
			activeStatusEffectsMap.remove(id);
		}
	}
	
	public void clearActiveStatusEffects() {
		Iterator<Integer> it = activeStatusEffectsMap.keySet().iterator ();
		
		while (it.hasNext()) {
			int idx = it.next();
			StatusEffect statusEffect = this.activeStatusEffectsMap.get(idx);
			if(!this.worldObj.isRemote) {
			it.remove ();
				this.onFinishedStatusEffect(statusEffect);
			}
		}
	}
	
	public Collection<StatusEffect> getActiveStatusEffects() {
		return activeStatusEffectsMap.values();
	}    
	
	public boolean isStatusActive(Status status) {
		return activeStatusEffectsMap.containsKey(Integer.valueOf(status.id));
	}  
	
	public StatusEffect getActiveStatusEffect(Status status) {
		return (StatusEffect)activeStatusEffectsMap.get(Integer.valueOf(status.id));
	}    
	
	public void addStatusEffect (StatusEffect statusEffect) {
		if(statusEffect.statusID == Status.statusDizzy.id) {
			if(this instanceof EntityPlayer) {
				((EntityPlayer)this).triggerAchievement(AchievementList.gotDizzy);
			}
		}
		
		if (Status.statusTypes[statusEffect.statusID].isApplicableTo(this)) {
			if (activeStatusEffectsMap.containsKey(Integer.valueOf(statusEffect.statusID))) {
				((StatusEffect)activeStatusEffectsMap.get(Integer.valueOf(statusEffect.statusID))).combine(statusEffect);
				this.onChangedStatusEffect(this.activeStatusEffectsMap.get(statusEffect.statusID));
			} else {
				activeStatusEffectsMap.put(Integer.valueOf(statusEffect.statusID), statusEffect);
				this.onNewStatusEffect(statusEffect);
			}
		}
	}

	public int getFullHealth() {
		return 10;
	}
	
	public float getAIMoveSpeed() {
		return this.AImoveSpeed;
	}

	public void setAIMoveSpeed(float par1) {
		this.AImoveSpeed = par1;
		this.setMoveForward(par1);
	}
	
	public void setMoveForward(float par1) {
		this.moveForward = par1;
	}
	
	public void setJumping(boolean par1) {
		this.isJumping = par1;
	}

	public EntityLookHelper getLookHelper() {
		return this.lookHelper;
	}

	public EntityMoveHelper getMoveHelper() {
		return this.moveHelper;
	}

	public EntityJumpHelper getJumpHelper() {
		return this.jumpHelper;
	}

	public PathNavigate getNavigator() {
		return this.navigator;
	}

	public EntityLiving getAttackTarget() {
		return attackTarget;
	}

	public void setAttackTarget(EntityLiving attackTarget) {
		this.attackTarget = attackTarget;
	}

	public EntitySenses getEntitySenses() {
		return entitySenses;
	}

	public void setEntitySenses(EntitySenses entitySenses) {
		this.entitySenses = entitySenses;
	}
	
	/*
	 * Override this if you want your mobs to get angry or do something about this!
	 */
	public void somebodyOpenedMyChest(EntityPlayer entityPlayer) {
		
	}

	public boolean isChild() {
		return false;
	}
	
	// ??? 

	public boolean func_48100_a(Class<?> class1) {
		return EntityCreeper.class != class1 && EntityGhast.class != class1;
	}
	
	public boolean isWithinHomeDistanceCurrentPosition() {
		return this.isWithinHomeDistance(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
	}

	public boolean isWithinHomeDistance(int i1, int i2, int i3) {
		return this.maximumHomeDistance == -1.0F ? true : this.homePosition.getSqDistanceTo(i1, i2, i3) < this.maximumHomeDistance * this.maximumHomeDistance;
	}

	public void setHomeArea(int i1, int i2, int i3, int i4) {
		this.homePosition.set(i1, i2, i3);
		this.maximumHomeDistance = (float)i4;
	}

	public float getMaximumHomeDistance() {
		return this.maximumHomeDistance;
	}

	public void detachHome() {
		this.maximumHomeDistance = -1.0F;
	}

	public boolean hasHome() {
		return this.maximumHomeDistance != -1.0F;
	}
	
	public void setRevengeTarget(EntityLiving par1EntityLiving) {
		this.entityLivingToAttack = par1EntityLiving;
		this.revengeTimer = this.entityLivingToAttack != null ? 60 : 0;
	}	
	
	public Random getRNG() {
		return this.rand;
	}

	public EntityLiving getAITarget() {
		return this.entityLivingToAttack;
	}

	public EntityLiving getLastAttackingEntity() {
		return this.lastAttackingEntity;
	}

	public void setLastAttackingEntity(Entity entity1) {
		if(entity1 instanceof EntityLiving) {
			this.lastAttackingEntity = (EntityLiving)entity1;
		}

	}
	
	
}
