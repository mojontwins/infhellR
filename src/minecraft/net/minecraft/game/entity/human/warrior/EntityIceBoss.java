package net.minecraft.game.entity.human.warrior;

import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.human.base.EntityHumanWarrior;
import net.minecraft.game.entity.monster.EntityIceBall;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

/**
 * Ranged ice boss. It teleports at random, throws ice balls at the nearest
 * player, and is immune to everything except its own projectiles.
 */
public class EntityIceBoss extends EntityHumanWarrior {
	private Entity targetedEntity = null;
	private int aggroCooldown = 0;
	public int prevAttackCounter = 0;
	public int attackCounter = 0;

	public EntityIceBoss(World world) {
		super(world);
		this.texture = "/mob/ice_boss.png";
		this.setLevel(4);
		this.health = this.getFullHealth();
		this.setHeldItem(new ItemStack(Block.ice));
	}

	@Override
	public void entityInit() {
		super.entityInit();

		// State bits (currently just "attacking") synced through the data watcher.
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
	}

	@Override
	public void configureAttributesBasedOnLevel() {
		this.armorTexture = "/armor/ice_diamond";
	}

	public void setAttacking(boolean attacking) {
		byte flags = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)((flags & 0xfe) | (attacking ? 1 : 0)));
	}

	public boolean getAttacking() {
		byte flags = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		return (flags & 1) == 1;
	}

	/** Teleports to a random nearby spot that has solid ground. */
	public boolean teleport() {
		double startX = this.posX;
		double startY = this.posY;
		double startZ = this.posZ;

		// Pick a random destination up to 5 blocks away on each axis.
		double offsetX = (this.rand.nextDouble() - 0.5D) * 10.0D;
		double offsetZ = (this.rand.nextDouble() - 0.5D) * 10.0D;
		double destX = this.posX + offsetX;
		double destZ = this.posZ + offsetZ;

		int blockX = (int)Math.floor(destX);
		int blockZ = (int)Math.floor(destZ);
		int blockY = (int)Math.floor(this.posY);

		// Require solid ground below the destination.
		Block below = Block.blocksList[this.worldObj.getBlockId(blockX, blockY - 1, blockZ)];
		if(below == null || !below.blockMaterial.getIsSolid()) {
			return false;
		}

		this.setLocationAndAngles(destX, this.posY, destZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
		if(this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox)) {
			double particleSpread = 2.0F;

			for(int i = 0; i < 10; ++i) {
				this.worldObj.spawnParticle("largesmoke", startX + (double)this.rand.nextFloat() - 0.5D, startY, startZ + (double)this.rand.nextFloat() - 0.5D, (destX - startX) / particleSpread, (this.posY - startY) / particleSpread, (destZ - startZ) / particleSpread);
			}

			this.worldObj.playSoundEffect(startX, startY, startZ, "random.breath", 1.0F, 1.0F);
			this.worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.0F);
			return true;
		}

		// Failed: teleport back to where we started.
		this.setLocationAndAngles(startX, startY, startZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
		return false;
	}

	@Override
	public boolean attackEntityFrom(Entity source, int damage) {
		// Only its own ice balls can hurt the boss.
		if(source instanceof EntityIceBall) {
			return super.attackEntityFrom(source, 5);
		} else {
			return false;
		}
	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();

		// Back away from a target standing right on top of us.
		if(this.entityToAttack != null && this.entityToAttack.getDistanceSqToEntity(this) < 49.0D) {
			this.moveForward = -this.moveSpeed;
		}

		if(this.targetedEntity != null && this.targetedEntity.isDead) {
			this.targetedEntity = null;
		}

		if(this.targetedEntity == null || this.aggroCooldown-- <= 0) {
			this.targetedEntity = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
			if(this.targetedEntity != null) {
				this.aggroCooldown = 20;
			}
		}

		if(this.targetedEntity instanceof EntityPlayer && ((EntityPlayer)this.targetedEntity).isCreative) {
			this.targetedEntity = null;
		}

		double attackRange = 16.0D;
		if(this.targetedEntity != null && this.targetedEntity.getDistanceSqToEntity(this) < attackRange * attackRange) {
			double deltaX = this.targetedEntity.posX - this.posX;
			double deltaY = this.targetedEntity.boundingBox.minY + (double)(this.targetedEntity.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
			double deltaZ = this.targetedEntity.posZ - this.posZ;
			this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(deltaX, deltaZ)) * 180.0F / (float)Math.PI;

			if(this.canEntityBeSeen(this.targetedEntity)) {
				if(this.attackCounter == 10) {
					this.worldObj.playSoundAtEntity(this, "mob.ice.idle", this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
				}

				++this.attackCounter;
				if(this.attackCounter == 20) {
					this.worldObj.playSoundAtEntity(this, "mob.ice.hurt", this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
					EntityIceBall iceBall = new EntityIceBall(this.worldObj, this, deltaX, deltaY, deltaZ);
					iceBall.posX = this.posX;
					iceBall.posY = this.posY + 1.0D;
					iceBall.posZ = this.posZ;
					this.worldObj.spawnEntityInWorld(iceBall);
					this.attackCounter = -60;
				}
			} else if(this.attackCounter > 0) {
				--this.attackCounter;
			}
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		if(!this.worldObj.isRemote && this.rand.nextInt(25) == 0) {
			this.teleport();
		}
	}

	@Override
	public int getFullHealth() {
		return 100;
	}

	@Override
	public void updateArmor() {
		// Shed one armour layer per fifth of the health pool lost.
		int maxHealth = this.getFullHealth();
		if(this.health < maxHealth / 5) {
			this.armor = 0;
		} else if(this.health < maxHealth / 5 * 2) {
			this.armor = 1;
		} else if(this.health < maxHealth / 5 * 3) {
			this.armor = 2;
		} else if(this.health < maxHealth / 5 * 4) {
			this.armor = 3;
		} else {
			this.armor = 4;
		}
	}

	@Override
	protected String getHurtSound() {
		return "mob.ice.hurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.ice.hurt";
	}

	@Override
	protected String getLivingSound() {
		return "mob.ice.idle";
	}

	@Override
	public void onDeath(Entity source) {
		if(source instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)source;
			player.triggerAchievement(AchievementList.iceBoss);

			if(!this.worldObj.isRemote) {
				this.dropItem(Item.pirateSigil.shiftedIndex, 1);
			}
		}

		super.onDeath(source);
	}
}
