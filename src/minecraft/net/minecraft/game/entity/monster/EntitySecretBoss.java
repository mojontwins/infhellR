package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntitySecretBoss extends EntitySlime {
	int lvl = 0;
	public float b;
	public float c;
	public int fullHealth = 20;
	
	public EntitySecretBoss(World world) {
		/*
		super(world);
		this.yOffset = 0.0F;
		if(this.lvl == 0) {
			this.slimeJumpDelay = this.rand.nextInt(20) + 10;
			this.setSlimeSize(1);
			this.size = 1.0F;
		} else {
			this.slimeJumpDelay = this.rand.nextInt(20) + 10;
			this.size = (float)(1 + this.lvl);
			this.health = (1 + this.lvl + world.difficultySetting) * 2;
			this.setSize(this.size * 0.6F, this.size * 0.6F);
		}
		*/
		this(world, 8);
	}

	public EntitySecretBoss(World world, int lvl) {
		super(world);
		this.yOffset = 0.0F;
		this.slimeJumpDelay = this.rand.nextInt(20) + 10;
		this.lvl = lvl;
		this.setSlimeSize(1 + this.lvl);
		this.moveSpeed = 0.5F + (float)(lvl / 10);
		//this.health = (1 + lvl + world.difficultySetting) * 2;
	}

	public void initBoss() {
		this.slimeJumpDelay = 0;
		this.texture = "/mob/slime.png";
		this.yOffset = 0.0F;
		this.slimeJumpDelay = this.rand.nextInt(20) + 10;
		this.setSlimeSize(1 + this.lvl);
		this.moveSpeed = 0.5F + (float)(this.lvl / 10);
		this.health = (1 + this.lvl + this.worldObj.difficultySetting) * 2;
	}

	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setInteger("Size", this.lvl);
	}

	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		this.lvl = nbttagcompound.getInteger("Size");
		this.initBoss();
	}

	public void onUpdate() {
		this.c = this.b;
		boolean flag = this.onGround;
		super.onUpdate();
		if(this.onGround && !flag) {
			int i = this.getSlimeSize();

			for(int j = 0; j < i * 8; ++j) {
				float f = this.rand.nextFloat() * 3.141593F * 2.0F;
				float f1 = this.rand.nextFloat() * 0.5F + 0.5F;
				float f2 = MathHelper.sin(f) * (float)i * 0.5F * f1;
				float f3 = MathHelper.cos(f) * (float)i * 0.5F * f1;
				this.worldObj.spawnParticle("slime", this.posX + (double)f2, this.boundingBox.minY, this.posZ + (double)f3, 0.0D, 0.0D, 0.0D);
			}

			if(i > 2) {
				this.worldObj.playSoundAtEntity(this, "mob.slime", this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			}

			this.b = -0.5F;
		}

		this.b *= 0.6F;
	}

	protected void updateEntityActionState() {
		EntityPlayer entityplayer = this.worldObj.getClosestPlayerToEntity(this, 30.0D);
		if(entityplayer != null) {
			this.faceEntity(entityplayer, 10.0F, 20.0F);
		}

		if(this.onGround && this.slimeJumpDelay-- <= 0) {
			this.slimeJumpDelay = 10;
			if(entityplayer != null) {
				this.slimeJumpDelay /= 3;
			}

			this.isJumping = true;
			if(this.getSlimeSize() > 1) {
				this.worldObj.playSoundAtEntity(this, "mob.slime", this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
			}

			this.b = 1.0F;
			this.moveStrafing = 1.0F - this.rand.nextFloat() * 2.0F;
			this.moveForward = (float)(1 * this.getSlimeSize());
		} else {
			this.isJumping = false;
			if(this.onGround) {
				this.moveStrafing = this.moveForward = 0.0F;
			}
		}

	}

	public void setEntityDead() {
		int j;
		float f1;
		int size = this.getSlimeSize();
		if(size == 1) {
			if(!this.worldObj.isRemote && size > 1 && this.health <= 0) {
				for(int f = 0; f < 4; ++f) {
					f1 = ((float)(f % 2) - 0.5F) * (float)size / 4.0F;
					float entityslime = ((float)(f / 2) - 0.5F) * (float)size / 4.0F;
					EntitySlime entityslime1 = new EntitySlime(this.worldObj);
					entityslime1.setSlimeSize(size / 2);
					entityslime1.setLocationAndAngles(this.posX + (double)f1, this.posY + 0.5D, this.posZ + (double)entityslime, this.rand.nextFloat() * 360.0F, 0.0F);
					this.worldObj.spawnEntityInWorld(entityslime1);
				}
			}
		} else {
			if(!this.worldObj.isRemote && this.lvl > 1 && this.health <= 0) {
				for(j = 0; j < 2; ++j) {
					float f6 = ((float)(j % 2) - 0.5F) * (float)this.lvl / 10.0F / 4.0F;
					f1 = ((float)(j / 2) - 0.5F) * (float)this.lvl / 10.0F / 4.0F;
					EntitySecretBoss EntitySecretBoss7 = new EntitySecretBoss(this.worldObj, this.lvl - 1);
					EntitySecretBoss7.setLocationAndAngles(this.posX + (double)f6, this.posY + 0.5D, this.posZ + (double)f1, this.rand.nextFloat() * 360.0F, 0.0F);
					this.worldObj.spawnEntityInWorld(EntitySecretBoss7);
				}
			}
		}

		this.isDead = true;
	}

	public void onCollideWithPlayer(EntityPlayer entityplayer) {
		int i = this.getSlimeSize();
		if(i > 1 && this.canEntityBeSeen(entityplayer) && (double)this.getDistanceToEntity(entityplayer) < 0.6D * (double)i && entityplayer.attackEntityFrom(this, i)) {
			this.worldObj.playSoundAtEntity(this, "mob.slimeattack", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
		}

	}

	protected String getHurtSound() {
		return "mob.slime";
	}

	protected String getDeathSound() {
		return "mob.slime";
	}

	protected int getDropItemId() {
		if(this.lvl > 1) {
			int r = this.rand.nextInt(20);
			if(r < 10) return Item.slimeBall.shiftedIndex;
			if(r < 12) return Item.ingotGold.shiftedIndex;
			if(r < 15) return Item.ingotIron.shiftedIndex;
			if(r < 17) return Item.ruby.shiftedIndex;
			if(r < 19) return Item.emerald.shiftedIndex;		
			return Item.diamond.shiftedIndex;
		} else {
			return 0;
		}
	}

	public boolean getCanSpawnHere() {
		return false;
	}

	protected float getSoundVolume() {
		return 0.6F;
	}
}
