package net.minecraft.game.entity.misc;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.IHostileToPlayer;
import net.minecraft.game.entity.IWaterMob;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;

public class EntityTriton extends EntityArmoredMob implements IWaterMob, IHostileToPlayer {
	public EntityTriton(World world) {
		super(world);
		this.moveSpeed = 1.1F;
		this.health = this.getFullHealth();
		this.inventory.setInventorySlotContents(0, this.rand.nextInt(4) == 0 ? new ItemStack(Item.battleWood) : new ItemStack(Item.axeWood));
	}

	protected int getMaxTextureVariations() {
		return 3;
	}

	@Override
	protected String getVariantTextureStem() {
		return "/mob/triton";
	}

	protected int getDropItemId() {
		return this.rand.nextInt(10) == 0 ? Block.driedKelpBlock.blockID : Item.driedKelp.shiftedIndex;
	}
	
	protected void attackEntity(Entity target, float distance) {
		if(distance > 2.0F && distance < 6.0F && this.rand.nextInt(10) == 0) {
			if(this.onGround) {
				double deltaX = target.posX - this.posX;
				double deltaZ = target.posZ - this.posZ;
				float horizontalDistance = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
				this.motionX = deltaX / (double)horizontalDistance * 0.5D * (double)0.8F + this.motionX * (double)0.2F;
				this.motionZ = deltaZ / (double)horizontalDistance * 0.5D * (double)0.8F + this.motionZ * (double)0.2F;
				this.motionY = (double)0.4F;
			}
		} else if((double)distance < 1.5D && target.boundingBox.maxY > this.boundingBox.minY && target.boundingBox.minY < this.boundingBox.maxY) {
			this.attackTime = 20;
			target.attackEntityFrom(this, 6);
		}
	}
	
	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();

		// Swing whenever a target comes within melee reach.
		if(this.entityToAttack != null && this.entityToAttack.getDistanceToEntity(this) < 3.0F) {
			this.swingItem();
		}
		this.updateSwingAnimation();
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
