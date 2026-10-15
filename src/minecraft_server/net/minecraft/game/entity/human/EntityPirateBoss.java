package net.minecraft.game.entity.human;

import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.entity.IHostileToPlayer;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Pirate captain. When wounded he turns invisible and teleports behind his
 * target, switching from a diamond sword to a dagger while hidden.
 */
public class EntityPirateBoss extends EntityMob implements IHostileToPlayer {
	private int invisibilityCooldown = 10;
	public boolean angryAtPlayer = false;

	public EntityPirateBoss(World world) {
		super(world);
		this.texture = "/mob/pirateBoss.png";
		this.attackStrength = 10;
		this.health = this.getFullHealth();
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		// "Invisible" flag, synced through the data watcher so SMP agrees.
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
	}

	public void setInvisible(boolean invisible) {
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(invisible ? 1 : 0));
	}

	public boolean isInvisible() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) == 1;
	}

	@Override
	public String getEntityTexture() {
		return this.isInvisible() ? "/mob/invisible.png" : "/mob/pirateBoss.png";
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		super.attackEntity(target, distance);

		if(this.attackTime <= 0 && distance < 2.0F && target.boundingBox.maxY > this.boundingBox.minY && target.boundingBox.minY < this.boundingBox.maxY) {
			this.attackTime = 20;
			this.attackEntityAsMob(target);
		}

		if(this.health < 200 && !this.isInvisible() && this.invisibilityCooldown < 0) {
			this.setInvisible(true);

			for(int i = 0; i < 3; ++i) {
				this.worldObj.spawnParticle("spell", this.posX + (double)this.rand.nextFloat() - 0.5D, this.posY + 1.0D, this.posZ + (double)this.rand.nextFloat() - 0.5D, 1.0D, 1.0D, 1.0D);
			}

			this.castTeleport(target);
		}

		--this.invisibilityCooldown;
	}

	public int getFullHealth() {
		return 100;
	}

	@Override
	public boolean attackEntityFrom(Entity source, int damage) {
		// Any hit breaks the invisibility and starts the cooldown.
		if(this.isInvisible()) {
			this.setInvisible(false);
			this.invisibilityCooldown = 30;
		}

		return super.attackEntityFrom(source, damage);
	}

	/** Teleports the boss to a solid spot behind the given entity. */
	protected boolean castTeleport(Entity target) {
		// Pick an offset at least 4 blocks away on at least one axis.
		double offsetX;
		double offsetZ;
		do {
			offsetX = (this.rand.nextDouble() - 0.5D) * 16.0D;
			offsetZ = (this.rand.nextDouble() - 0.5D) * 16.0D;
		} while((offsetX >= -4.0D && offsetX <= 4.0D) || (offsetZ >= -4.0D && offsetZ <= 4.0D));

		double destX = target.posX + offsetX;
		double destZ = target.posZ + offsetZ;
		double destY = target.posY;

		double startX = this.posX;
		double startY = this.posY;
		double startZ = this.posZ;

		boolean hasTeleported = false;

		int blockX = MathHelper.floor_double(destX);
		int blockY = MathHelper.floor_double(destY);
		int blockZ = MathHelper.floor_double(destZ);

		if(this.worldObj.blockExists(blockX, blockY, blockZ)) {
			// Sink until solid ground is found.
			boolean foundSolid = false;
			while(!foundSolid && blockY > 0) {
				Block below = Block.blocksList[this.worldObj.getBlockId(blockX, blockY - 1, blockZ)];
				if(below != null && below.blockMaterial.getIsSolid()) {
					foundSolid = true;
				} else {
					--this.posY;
					--blockY;
				}
			}

			if(foundSolid) {
				this.setLocationAndAngles(destX, destY, destZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);

				if(this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox)) {
					double particleSpread = 2.0F;
					for(int i = 0; i < 10; ++i) {
						this.worldObj.spawnParticle("largesmoke", startX + (double)this.rand.nextFloat() - 0.5D, startY, startZ + (double)this.rand.nextFloat() - 0.5D, (destX - startX) / particleSpread, (destY - startY) / particleSpread, (destZ - startZ) / particleSpread);
					}

					hasTeleported = true;
				}
			}
		}

		if(!hasTeleported) {
			// Failed: return to where we started.
			this.setLocationAndAngles(startX, startY, startZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
			return false;
		} else {
			this.worldObj.playSoundEffect(startX, startY, startZ, "random.breath", 1.0F, 1.0F);
			this.worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.0F);
			return true;
		}
	}

	@Override
	public void dropFewItems() {
		super.dropFewItems();

		if(this.rand.nextInt(5) == 0 || this.rand.nextInt(2) > 0) {
			this.dropItem(Item.diamond.shiftedIndex, 2);
		}
	}

	@Override
	public ItemStack getHeldItem() {
		// While invisible he holds a dagger, otherwise his sword.
		return this.isInvisible() ? new ItemStack(Item.knifeDiamond, 1) : new ItemStack(Item.swordDiamond, 1);
	}

	@Override
	protected void despawnEntity() {
		if(this.isDead) {
			super.despawnEntity();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		tag.setBoolean("Invisible", this.isInvisible());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.angryAtPlayer = tag.getBoolean("AngryAtPlayer");
		this.setInvisible(tag.getBoolean("Invisible"));
	}

	@Override
	protected Entity findPlayerToAttack() {
		EntityPlayer player = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
		if(player != null && !player.isCreative && this.canEntityBeSeen(player) && (!player.dressedAsAPirate() || this.angryAtPlayer)) {
			return player;
		}

		return null;
	}

	@Override
	public void onDeath(Entity source) {
		if(source instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)source;
			player.triggerAchievement(AchievementList.pirateKing);

			if(!this.worldObj.isRemote) {
				this.dropItem(Item.pirateSigil.shiftedIndex, 1);
			}
		}

		super.onDeath(source);
	}
}
