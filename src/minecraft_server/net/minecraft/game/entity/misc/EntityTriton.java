package net.minecraft.game.entity.misc;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.IWaterMob;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntityTriton extends EntityArmoredMob implements IWaterMob {
	public boolean isSwinging = false;
	public int swingProgressInt = 0;
	protected String texturePrefix;
	
	public EntityTriton(World var1) {
		super(var1);
		this.texture = "/mob/triton1.png";
		this.texturePrefix="triton";
		this.moveSpeed = 1.1F;
		this.health = this.getFullHealth();
		this.inventory.setInventorySlotContents(0, rand.nextInt(4) == 0 ? new ItemStack(Item.battleWood) : new ItemStack(Item.axeWood));

	}
	
	public void swingItem() {
		this.swingProgressInt = -1;
		this.isSwinging = true;
	}
	
	protected int getMaxTextureVariations() {
		return 3;
	}
	
	protected void entityInit() {
		super.entityInit();
		// Datawatcher object 19 is texture variation
		this.dataWatcher.addObject(Datawatchers.DW_TYPE, (byte)0);
		this.setTextureVariation((byte) (1 + rand.nextInt(this.getMaxTextureVariations())));		
	}
	
	public void setTextureVariation(byte variation) {
		this.dataWatcher.updateObject(Datawatchers.DW_TYPE, variation);
	}
	
	public byte getTextureVariation() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_TYPE);
	}
	
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);

		nBTTagCompound1.setByte("TextureVariation", this.getTextureVariation());
		
	}

	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);

		if(nBTTagCompound1.hasKey("TextureVariation")) {
			this.setTextureVariation(nBTTagCompound1.getByte("TextureVariation"));
		}
	}
	
	protected int getDropItemId() {
		return this.rand.nextInt(10) == 0 ? Block.driedKelpBlock.blockID : Item.driedKelp.shiftedIndex;
	}
	
	public void onUpdate() {
		super.onUpdate();
		if(this.getMaxTextureVariations() > 0) {
			this.texture = "/mob/" + this.texturePrefix + this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_TYPE) + ".png";		
		}

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
			
			entity1.attackEntityFrom(this, 6);
		}

	}
	
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		if(this.entityToAttack != null) {
			if(this.entityToAttack.getDistanceToEntity(this) < 3.0F) {
				this.swingItem();
			}
		}
	}
	
	public int getMaxSpawnedInChunk() {
		return 8;
	}

	public int getFullHealth() {
		return 30;
	}
	
	public boolean getCanSpawnHere() {
		return this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0;
	}

	public boolean canBreatheUnderwater() {
		return true;
	}
	
	public boolean triesToFloat() {
		return false; 
	}
	
	protected String getLivingSound() {
		return "mob.triton.idle";
	}

	protected String getHurtSound() {
		return "mob.triton.hurt";
	}

	protected String getDeathSound() {
		return "mob.triton.death";
	}
}
