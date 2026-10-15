package net.minecraft.game.entity.human.armored;

import java.util.List;
import java.util.Random;

import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.IHostileToPlayer;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemFood;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.path.PathEntity;
import net.minecraft.game.world.World;

/**
 * Amazon warrior. Wild versions can be tamed by offering a precious item; a
 * tamed amazon follows and defends her owner, and a whole tribe fights together
 * when one of them is attacked or a nearby chest is opened.
 */
public class EntityAmazon extends EntityArmoredMob implements ISentient {
	private boolean looksWithInterest = false;
	private float headRoll;
	private float prevHeadRoll;

	/** Ticks left before a provoked amazon gives up on her current target. */
	public int timeBeforeLosingInterest = 0;

	public EntityAmazon(World world) {
		super(world);
		this.moveSpeed = 1.1F;
		this.health = this.getFullHealth();
		
		// A rare amazon wields a golden mace instead of a sword.
		this.inventory.setInventorySlotContents(0, this.rand.nextInt(32) == 0 ? new ItemStack(Item.maceGold) : new ItemStack(Item.swordGold));
	}

	// Amazons are neutral until provoked; they do not implement the hostile marker.


	public void setEntityToAttack(Entity target) {
		if(this.entityToAttack != target) {
			this.timeBeforeLosingInterest = 600;
		}
		this.entityToAttack = target;
	}

	protected int getMaxTextureVariations() {
		return 4;
	}

	@Override
	protected String getVariantTextureStem() {
		return "/mob/amazon";
	}

	@Override
	public int getRandomTextureVariation(Random rand, int count) {
		// Most amazons use one of the three common skins; a rare one (1 in 9) uses the
		// alternate skin at the last index.
		return rand.nextInt(9) == 0 ? count - 1 : rand.nextInt(count - 1);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
		this.dataWatcher.addObject(Datawatchers.DW_OWNER, "");
		this.dataWatcher.addObject(Datawatchers.DW_HEALTH, this.health);
	}

	@Override
	protected boolean canTriggerWalking() {
		return true;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setBoolean("Angry", this.isAmazonAngry());
		tag.setBoolean("Sitting", this.getIsSitting());
		tag.setString("Owner", this.getOwner() == null ? "" : this.getOwner());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.setAmazonAngry(tag.getBoolean("Angry"));
		this.setIsSitting(tag.getBoolean("Sitting"));

		String owner = tag.getString("Owner");
		if(owner.length() > 0) {
			this.setAmazonOwner(owner);
			this.setAmazonTamed(true);
		}
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected String getLivingSound() {
		return "";
	}

	protected String getHurtSoundByGender() {
		return this.getVariant() == 2 ? "random.hurt" : "mob.amazon.hit";
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

		// A tamed amazon walks back to her owner; an idling wild one hunts monsters.
		if(!this.hasAttacked && !this.hasPath() && this.isAmazonTamed() && this.ridingEntity == null) {
			EntityPlayer owner = this.worldObj.getPlayerEntityByName(this.getOwner());
			if(owner != null) {
				float distance = owner.getDistanceToEntity(this);
				if(distance > 5.0F) {
					this.getPathOrWalkableBlock(owner, distance);
				}
			} else if(!this.isInWater()) {
				this.setIsSitting(true);
			}
		} else if(this.entityToAttack == null && !this.hasPath() && !this.isAmazonTamed() && this.worldObj.rand.nextInt(100) == 0) {
			// A wild amazon occasionally picks a nearby hostile mob to hunt.
			AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D);
			List<Entity> hostiles = this.worldObj.getEntitiesWithinAABB(IHostileToPlayer.class, searchBox);
			if(!hostiles.isEmpty()) {
				this.setEntityToAttack((Entity)hostiles.get(this.worldObj.rand.nextInt(hostiles.size())));
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
				--this.timeBeforeLosingInterest;
				if(this.timeBeforeLosingInterest == 0) {
					this.entityToAttack = null;
					this.setAmazonAngry(false);
				}
			}
		}

		this.updateSwingAnimation();
	}

	public boolean interestingItem(int itemID) {
		return itemID == Item.ruby.shiftedIndex || itemID == Item.emerald.shiftedIndex || itemID == Item.diamond.shiftedIndex || itemID == Block.blockGold.blockID;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.looksWithInterest = false;

		// Stare at a player holding something worth trading for (or food, if tamed).
		if(this.hasCurrentTarget() && !this.hasPath() && !this.isAmazonAngry()) {
			Entity currentTarget = this.getCurrentTarget();
			if(currentTarget instanceof EntityPlayer) {
				ItemStack heldStack = ((EntityPlayer)currentTarget).inventory.getCurrentItem();
				if(heldStack != null) {
					if(!this.isAmazonTamed() && this.interestingItem(heldStack.itemID)) {
						this.looksWithInterest = true;
					} else if(this.isAmazonTamed() && Item.itemsList[heldStack.itemID] instanceof ItemFood) {
						this.looksWithInterest = true;
					}
				}
			}
		}

		// Defend the owner by retaliating against whoever last hit them.
		if(this.isAmazonTamed()) {
			EntityPlayer owner = this.worldObj.getPlayerEntityByName(this.getOwner());
			Entity attacker = owner.lastAttackingEntity;
			if(attacker instanceof EntityLiving && this.isValidTarget((EntityLiving)attacker)) {
				this.entityToAttack = attacker;
			}
		}
	}

	public boolean isValidTarget(EntityLiving entityLiving) {
		if(entityLiving == null) {
			return false;
		}

		if(entityLiving == this) {
			return false;
		}

		if(!entityLiving.isEntityAlive()) {
			return false;
		}

		if(entityLiving.boundingBox.minY >= this.boundingBox.maxY || entityLiving.boundingBox.maxY <= this.boundingBox.minY) {
			return false;
		}

		return true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.prevHeadRoll = this.headRoll;

		// Ease the head roll toward the "interested" pose and back.
		this.headRoll += (this.looksWithInterest ? 1.0F - this.headRoll : 0.0F - this.headRoll) * 0.4F;

		if(this.looksWithInterest) {
			this.numTicksToChaseTarget = 10;
		}
	}

	public float getInterestedAngle(float partialTick) {
		return (this.prevHeadRoll + (this.headRoll - this.prevHeadRoll) * partialTick) * 0.15F * (float)Math.PI;
	}

	@Override
	public float getEyeHeight() {
		return this.height * 0.8F;
	}

	@Override
	public int getVerticalFaceSpeed() {
		return this.getIsSitting() ? 20 : super.getVerticalFaceSpeed();
	}

	private void getPathOrWalkableBlock(Entity target, float distance) {
		PathEntity path = this.worldObj.getPathToEntity(this, target, 16.0F);
		if(path == null && distance > 12.0F) {
			// Too far for a path: snap to a nearby walkable block near the target.
			int targetX = MathHelper.floor_double(target.posX) - 2;
			int targetZ = MathHelper.floor_double(target.posZ) - 2;
			int targetY = MathHelper.floor_double(target.boundingBox.minY);

			for(int offsetX = 0; offsetX <= 4; ++offsetX) {
				for(int offsetZ = 0; offsetZ <= 4; ++offsetZ) {
					if((offsetX < 1 || offsetZ < 1 || offsetX > 3 || offsetZ > 3)
							&& this.worldObj.isBlockNormalCube(targetX + offsetX, targetY - 1, targetZ + offsetZ)
							&& !this.worldObj.isBlockNormalCube(targetX + offsetX, targetY, targetZ + offsetZ)
							&& !this.worldObj.isBlockNormalCube(targetX + offsetX, targetY + 1, targetZ + offsetZ)) {
						this.setLocationAndAngles((double)((float)(targetX + offsetX) + 0.5F), (double)targetY, (double)((float)(targetZ + offsetZ) + 0.5F), this.rotationYaw, this.rotationPitch);
						return;
					}
				}
			}
		} else {
			this.setPathToEntity(path);
		}
	}

	@Override
	protected boolean isMovementCeased() {
		return this.getIsSitting();
	}

	@Override
	public boolean attackEntityFrom(Entity source, int damage) {
		this.setIsSitting(false);

		// Non-player, non-arrow sources deal reduced damage.
		if(source != null && !(source instanceof EntityPlayer) && !(source instanceof EntityArrow)) {
			damage = (damage + 1) / 2;
		}

		// Amazons may brawl among themselves endlessly.
		if(source instanceof EntityAmazon) {
			damage = 0;
		}

		boolean damaged = super.attackEntityFrom(source, damage);

		if(source instanceof EntityPlayer && ((EntityPlayer)source).isCreative) {
			return true;
		}

		if(!this.isAmazonTamed() && !this.isAmazonAngry()) {
			if(source instanceof EntityPlayer) {
				this.setAmazonAngry(true);
			}

			if(source instanceof EntityArrow && ((EntityArrow)source).shootingEntity != null) {
				source = ((EntityArrow)source).shootingEntity;
			}

			if(source instanceof EntityLiving) {
				this.setEntityToAttack(source);

				// Rally every nearby untamed amazon onto the same attacker.
				List<Entity> nearbyAmazons = this.worldObj.getEntitiesWithinAABB(EntityAmazon.class, AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
				for(Entity entity : nearbyAmazons) {
					EntityAmazon otherAmazon = (EntityAmazon)entity;
					if(!otherAmazon.isAmazonTamed() && otherAmazon.entityToAttack == null) {
						otherAmazon.setEntityToAttack(source);
						if(source instanceof EntityPlayer) {
							otherAmazon.setAmazonAngry(true);
						}
					}
				}
			}
		} else if(source != this && source != null) {
			if(this.isAmazonTamed() && source instanceof EntityPlayer && ((EntityPlayer)source).username.equalsIgnoreCase(this.getOwner())) {
				return true;
			}

			this.setEntityToAttack(source);
		}

		return damaged;
	}

	@Override
	protected Entity findPlayerToAttack() {
		return this.isAmazonAngry() ? this.worldObj.getClosestPlayerToEntity(this, 16.0D) : null;
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if(distance > 2.0F && distance < 6.0F && this.rand.nextInt(10) == 0) {
			// Leap toward a target that is at mid range.
			if(this.onGround) {
				double deltaX = target.posX - this.posX;
				double deltaZ = target.posZ - this.posZ;
				float horizontalDistance = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
				this.motionX = deltaX / (double)horizontalDistance * 0.5D * (double)0.8F + this.motionX * (double)0.2F;
				this.motionZ = deltaZ / (double)horizontalDistance * 0.5D * (double)0.8F + this.motionZ * (double)0.2F;
				this.motionY = (double)0.4F;
			}
		} else if((double)distance < 1.5D && target.boundingBox.maxY > this.boundingBox.minY && target.boundingBox.minY < this.boundingBox.maxY) {
			// Adjacent: melee. Tamed amazons hit slightly harder.
			this.attackTime = 20;
			byte damage = 4;
			if(this.isAmazonTamed()) {
				damage = 5;
			}

			target.attackEntityFrom(this, damage);
		}
	}

	@Override
	public boolean interact(EntityPlayer player) {
		ItemStack heldStack = player.inventory.getCurrentItem();

		if(!this.isAmazonTamed()) {
			// Offer a precious item for a chance to tame her.
			if(heldStack != null && this.interestingItem(heldStack.itemID)) {
				if(!player.isCreative) {
					--heldStack.stackSize;
				}
				if(heldStack.stackSize <= 0) {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				if(!this.worldObj.isRemote) {
					if(this.rand.nextInt(8) == 0) {
						this.setAmazonTamed(true);
						this.setPathToEntity(null);
						this.health = 20;
						this.setAmazonOwner(player.username);
						this.showHeartsOrSmokeFX(true);
						this.worldObj.setEntityState(this, (byte)7);
						player.triggerAchievement(AchievementList.hireAmazon);
					} else {
						this.showHeartsOrSmokeFX(false);
						this.worldObj.setEntityState(this, (byte)6);
					}
				}

				return true;
			}
		} else {
			// Feed a wounded tamed amazon, or toggle sitting when the owner clicks.
			if(heldStack != null && Item.itemsList[heldStack.itemID] instanceof ItemFood) {
				if(this.dataWatcher.getWatchableObjectInt(Datawatchers.DW_HEALTH) < this.getFullHealth()) {
					if(!player.isCreative) {
						--heldStack.stackSize;
					}
					if(heldStack.stackSize <= 0) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}

					this.heal(((ItemFood)heldStack.getItem()).getHealAmount());
					this.showHeartsOrSmokeFX(true);
					return true;
				}
			}

			if(player.username.equalsIgnoreCase(this.getOwner())) {
				if(!this.worldObj.isRemote) {
					this.setIsSitting(!this.getIsSitting());
					this.isJumping = false;
					this.setPathToEntity(null);
				}

				return true;
			}
		}

		return false;
	}

	void showHeartsOrSmokeFX(boolean hearts) {
		String particle = hearts ? "heart" : "smoke";
		for(int i = 0; i < 7; ++i) {
			double motionX = this.rand.nextGaussian() * 0.02D;
			double motionY = this.rand.nextGaussian() * 0.02D;
			double motionZ = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle(particle, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, motionX, motionY, motionZ);
		}
	}

	@Override
	public void handleHealthUpdate(byte status) {
		if(status == 7) {
			this.showHeartsOrSmokeFX(true);
		} else if(status == 6) {
			this.showHeartsOrSmokeFX(false);
		} else if(status == 8) {
			// No-op.
		} else {
			super.handleHealthUpdate(status);
		}
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 8;
	}

	public String getOwner() {
		return this.dataWatcher.getWatchableObjectString(Datawatchers.DW_OWNER);
	}

	public void setAmazonOwner(String owner) {
		this.dataWatcher.updateObject(Datawatchers.DW_OWNER, owner);
	}

	public boolean getIsSitting() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 1) != 0;
	}

	public void setIsSitting(boolean sitting) {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(sitting) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(status | 1));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(status & -2));
		}
	}

	public boolean isAmazonAngry() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 2) != 0;
	}

	public void setAmazonAngry(boolean angry) {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(angry) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(status | 2));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(status & -3));
		}
	}

	public boolean isAmazonTamed() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 4) != 0;
	}

	public void setAmazonTamed(boolean tamed) {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(tamed) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(status | 4));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(status & -5));
		}
	}

	@Override
	public int getFullHealth() {
		return 30;
	}

	@Override
	public void somebodyOpenedMyChest(EntityPlayer player) {
		// Opening a chest in amazon territory angers every nearby untamed amazon.
		List<Entity> nearbyAmazons = this.worldObj.getEntitiesWithinAABB(EntityAmazon.class, AxisAlignedBB.getBoundingBoxFromPool(player.posX, player.posY, player.posZ, player.posX + 1.0D, player.posY + 1.0D, player.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
		for(Entity entity : nearbyAmazons) {
			EntityAmazon amazon = (EntityAmazon)entity;
			if(!amazon.isAmazonTamed() && amazon.entityToAttack == null) {
				// Point each nearby amazon at the thief, not at the throwaway
				// instance this hook was invoked on.
				amazon.setEntityToAttack(player);
				amazon.setAmazonAngry(true);
			}
		}
	}
}
