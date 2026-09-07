package net.minecraft.game.entity.projectile;

import java.util.List;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public class EntityArrow extends Entity {
	protected int xTile = -1;
	protected int yTile = -1;
	protected int zTile = -1;
	protected int inTile = 0;
	protected int inMeta = 0;
	protected boolean inGround = false;
	public boolean doesArrowBelongToPlayer = false;
	public int arrowShake = 0;
	public EntityLiving shootingEntity;
	protected int ticksInGround;
	protected int ticksInAir = 0;
	public int arrowDamage = 4;
	
	// No effect rn
	protected boolean arrowCritical;

	public EntityArrow(World world) {
		super(world);
		this.setSize(0.5F, 0.5F);
	}

	protected void entityInit() {
	}
	
	public EntityArrow(World world, double posX, double posY, double posZ) {
		super(world);
		this.setSize(0.5F, 0.5F);
		this.setPosition(posX, posY, posZ);
		this.yOffset = 0.0F;
	}

	public EntityArrow(World world, EntityLiving entityLiving) {
		super(world);
		this.shootingEntity = entityLiving;
		this.doesArrowBelongToPlayer = entityLiving instanceof EntityPlayer;
		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(entityLiving.posX, entityLiving.posY + (double)entityLiving.getEyeHeight(), entityLiving.posZ, entityLiving.rotationYaw, entityLiving.rotationPitch);
		this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.posY -= (double)0.1F;
		this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI));
		this.setArrowHeading(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
		this.arrowDamage = this.shootingEntity instanceof EntityPlayer ? 4 : 3;
	}


	public void setArrowHeading(double motionX, double motionY, double motionZ, float velocity, float variance) {
		float modulus = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
		
		// Normalize vector
		motionX /= (double)modulus;
		motionY /= (double)modulus;
		motionZ /= (double)modulus;

		// Add random modifier
		motionX += this.rand.nextGaussian() * (double)0.0075F * (double)variance;
		motionY += this.rand.nextGaussian() * (double)0.0075F * (double)variance;
		motionZ += this.rand.nextGaussian() * (double)0.0075F * (double)variance;

		// Set velocity
		motionX *= (double)velocity;
		motionY *= (double)velocity;
		motionZ *= (double)velocity;

		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;

		float horizontalVelocity = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
		
		this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / (double)(float)Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(motionY, (double)horizontalVelocity) * 180.0D / (double)(float)Math.PI);
		this.ticksInGround = 0;
	}

	public void setVelocity(double motionX, double motionY, double motionZ) {
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		if(this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float horizontalVelocity = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / (double)(float)Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(motionY, (double)horizontalVelocity) * 180.0D / (double)(float)Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}

	}

	public void onUpdate() {
		super.onUpdate();
		if(this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / (double)(float)Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f1) * 180.0D / (double)(float)Math.PI);
		}

		int i15 = this.worldObj.getBlockId(this.xTile, this.yTile, this.zTile);
		if(i15 > 0) {
			Block.blocksList[i15].setBlockBoundsBasedOnState(this.worldObj, this.xTile, this.yTile, this.zTile);
			AxisAlignedBB axisAlignedBB2 = Block.blocksList[i15].getCollisionBoundingBoxFromPool(this.worldObj, this.xTile, this.yTile, this.zTile);
			if(axisAlignedBB2 != null && axisAlignedBB2.isVecInside(Vec3D.createVector(this.posX, this.posY, this.posZ))) {
				this.inGround = true;
			}
		}

		if(this.arrowShake > 0) {
			--this.arrowShake;
		}

		if(this.inGround) {
			i15 = this.worldObj.getBlockId(this.xTile, this.yTile, this.zTile);
			int i18 = this.worldObj.getBlockMetadata(this.xTile, this.yTile, this.zTile);
			if(i15 == this.inTile && i18 == this.inMeta) {
				++this.ticksInGround;
				if(this.ticksInGround == 1200) {
					this.setEntityDead();
				}

			} else {
				this.inGround = false;
				this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
		} else {
			++this.ticksInAir;
			Vec3D vec3D16 = Vec3D.createVector(this.posX, this.posY, this.posZ);
			Vec3D vec3D17 = Vec3D.createVector(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition movingObjectPosition3 = this.worldObj.rayTraceBlocks(vec3D16, vec3D17, false, true);
			vec3D16 = Vec3D.createVector(this.posX, this.posY, this.posZ);
			vec3D17 = Vec3D.createVector(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			if(movingObjectPosition3 != null) {
				vec3D17 = Vec3D.createVector(movingObjectPosition3.hitVec.xCoord, movingObjectPosition3.hitVec.yCoord, movingObjectPosition3.hitVec.zCoord);
			}

			Entity entity4 = null;
			List<Entity> list5 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d6 = 0.0D;

			float f10;
			for(int i8 = 0; i8 < list5.size(); ++i8) {
				Entity entity9 = (Entity)list5.get(i8);
				if(entity9.canBeCollidedWith() && (entity9 != this.shootingEntity || this.ticksInAir >= 5)) {
					f10 = 0.3F;
					AxisAlignedBB axisAlignedBB11 = entity9.boundingBox.expand((double)f10, (double)f10, (double)f10);
					MovingObjectPosition movingObjectPosition12 = axisAlignedBB11.raytrace(vec3D16, vec3D17);
					if(movingObjectPosition12 != null) {
						double d13 = vec3D16.distanceTo(movingObjectPosition12.hitVec);
						if(d13 < d6 || d6 == 0.0D) {
							entity4 = entity9;
							d6 = d13;
						}
					}
				}
			}

			if(entity4 != null) {
				movingObjectPosition3 = new MovingObjectPosition(entity4);
			}

			float f19;
			if(movingObjectPosition3 != null) {
				this.throwableHitEntity(movingObjectPosition3);
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			f19 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / (double)(float)Math.PI);

			for(this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f19) * 180.0D / (double)(float)Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
			}

			while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
				this.prevRotationPitch += 360.0F;
			}

			while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
				this.prevRotationYaw -= 360.0F;
			}

			while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			float f20 = 0.99F;
			f10 = 0.03F;
			if(this.isInWater()) {
				for(int i21 = 0; i21 < 4; ++i21) {
					float f22 = 0.25F;
					this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)f22, this.posY - this.motionY * (double)f22, this.posZ - this.motionZ * (double)f22, this.motionX, this.motionY, this.motionZ);
				}

				f20 = 0.8F;
			}

			this.motionX *= (double)f20;
			this.motionY *= (double)f20;
			this.motionZ *= (double)f20;
			this.motionY -= (double)f10;
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	public void throwableHitEntity(MovingObjectPosition movingObjectPosition) {
		if(movingObjectPosition.entityHit != null) {
			if(movingObjectPosition.entityHit.attackEntityFrom(this.shootingEntity, this.arrowDamage)) {
				this.worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
				this.setEntityDead();
			} else {
				this.motionX *= -0.10000000149011612D;
				this.motionY *= -0.10000000149011612D;
				this.motionZ *= -0.10000000149011612D;
				this.rotationYaw += 180.0F;
				this.prevRotationYaw += 180.0F;
				this.ticksInAir = 0;
			}
		} else {
			this.xTile = movingObjectPosition.blockX;
			this.yTile = movingObjectPosition.blockY;
			this.zTile = movingObjectPosition.blockZ;
			this.inTile = this.worldObj.getBlockId(this.xTile, this.yTile, this.zTile);
			this.inMeta = this.worldObj.getBlockMetadata(this.xTile, this.yTile, this.zTile);
			this.motionX = (double)((float)(movingObjectPosition.hitVec.xCoord - this.posX));
			this.motionY = (double)((float)(movingObjectPosition.hitVec.yCoord - this.posY));
			this.motionZ = (double)((float)(movingObjectPosition.hitVec.zCoord - this.posZ));
			
			float velocity = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
			this.posX -= this.motionX / (double)velocity * (double)0.05F;
			this.posY -= this.motionY / (double)velocity * (double)0.05F;
			this.posZ -= this.motionZ / (double)velocity * (double)0.05F;
			this.worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
			this.inGround = true;
			this.arrowShake = 7;
		}
	}
	
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		nBTTagCompound1.setShort("xTile", (short)this.xTile);
		nBTTagCompound1.setShort("yTile", (short)this.yTile);
		nBTTagCompound1.setShort("zTile", (short)this.zTile);
		nBTTagCompound1.setByte("inTile", (byte)this.inTile);
		nBTTagCompound1.setByte("inData", (byte)this.inMeta);
		nBTTagCompound1.setByte("shake", (byte)this.arrowShake);
		nBTTagCompound1.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		nBTTagCompound1.setBoolean("player", this.doesArrowBelongToPlayer);
	}

	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		this.xTile = nBTTagCompound1.getShort("xTile");
		this.yTile = nBTTagCompound1.getShort("yTile");
		this.zTile = nBTTagCompound1.getShort("zTile");
		this.inTile = nBTTagCompound1.getByte("inTile") & 255;
		this.inMeta = nBTTagCompound1.getByte("inData") & 255;
		this.arrowShake = nBTTagCompound1.getByte("shake") & 255;
		this.inGround = nBTTagCompound1.getByte("inGround") == 1;
		this.doesArrowBelongToPlayer = nBTTagCompound1.getBoolean("player");
	}

	public void onCollideWithPlayer(EntityPlayer entityPlayer) {
		if(!this.worldObj.isRemote) {
			if(this.inGround && this.doesArrowBelongToPlayer && this.arrowShake <= 0 && entityPlayer.inventory.addItemStackToInventory(new ItemStack(Item.arrow, 1))) {
				this.worldObj.playSoundAtEntity(this, "random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				entityPlayer.onItemPickup(this, 1);
				this.setEntityDead();
			}

		}
	}

	public float getShadowSize() {
		return 0.0F;
	}
}
