package net.minecraft.game.entity.human;

import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.entity.IHostileToPlayer;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.projectile.EntityArrowWithEffect;
import net.minecraft.game.entity.status.Status;
import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Pirate archer. Fires blinding arrows at players, and like the pirate
 * swordsmen it only attacks players who are not disguised (unless provoked).
 */
public class EntityPirateArcher extends EntityMob implements ISentient, IHostileToPlayer {
	private ItemStack heldItem;
	public boolean angryAtPlayer = false;

	public EntityPirateArcher(World world) {
		super(world);
		this.attackStrength = this.worldObj == null ? 7 : 5 + this.worldObj.difficultySetting;
		this.texture = "/mob/pirate1.png";
		this.setHeldItem(new ItemStack(Item.bow));
	}

	protected int getMaxTextureVariations() {
		return 3;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_TYPE, (byte)0);
		this.setTextureVariation((byte)(1 + this.rand.nextInt(this.getMaxTextureVariations())));
	}

	public void setTextureVariation(byte variation) {
		this.dataWatcher.updateObject(Datawatchers.DW_TYPE, variation);
	}

	public byte getTextureVariation() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_TYPE);
	}

	@Override
	public String getEntityTexture() {
		if(this.getMaxTextureVariations() > 1) {
			return "/mob/pirate" + this.getTextureVariation() + ".png";
		} else {
			return this.texture;
		}
	}

	@Override
	public boolean setHeldItem(ItemStack itemStack) {
		this.heldItem = itemStack;
		return true;
	}

	@Override
	public ItemStack getHeldItem() {
		return this.heldItem;
	}

	public int getFullHealth() {
		return this.worldObj == null ? 27 : 25 + this.worldObj.difficultySetting;
	}

	@Override
	protected String getLivingSound() {
		return "mob.zombie";
	}

	@Override
	protected String getHurtSound() {
		return "mob.zombiehurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.zombiedeath";
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if(distance < 10.0F) {
			double deltaX = target.posX - this.posX;
			double deltaZ = target.posZ - this.posZ;

			if(this.attackTime == 0) {
				EntityArrow arrow = this.selectArrow();
				++arrow.posY;

				double deltaY = target.posY + (double)target.getEyeHeight() - (double)0.2F - arrow.posY;
				float arc = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ) * 0.2F;
				this.worldObj.spawnEntityInWorld(arrow);
				arrow.setArrowHeading(deltaX, deltaY + (double)arc, deltaZ, 0.6F, 12.0F);

				this.worldObj.playSoundAtEntity(this, "random.bow", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
				this.attackTime = 100;
			}

			this.rotationYaw = (float)(Math.atan2(deltaZ, deltaX) * 180.0D / (double)(float)Math.PI) - 90.0F;
			this.hasAttacked = true;
		}
	}

	public EntityArrow selectArrow() {
		return new EntityArrowWithEffect(this.worldObj, this)
				.withStatusEffect(new StatusEffect(Status.statusBlind.id, 100, 1));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		tag.setByte("TextureVariation", this.getTextureVariation());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.angryAtPlayer = tag.getBoolean("AngryAtPlayer");

		byte textureVariation = 1;
		if(tag.hasKey("TextureVariation")) {
			textureVariation = tag.getByte("TextureVariation");
		} else {
			System.out.println("Importing old level, overrode texture variation for " + this.getClass() + " with default.");
		}
		this.setTextureVariation(textureVariation);
	}

	@Override
	protected int getDropItemId() {
		return Item.arrow.shiftedIndex;
	}

	@Override
	public boolean getCanSpawnHere() {
		int blockX = MathHelper.floor_double(this.posX);
		int blockY = MathHelper.floor_double(this.boundingBox.minY);
		int blockZ = MathHelper.floor_double(this.posZ);
		return this.posY > 60.0D && this.worldObj.isBlockOpaqueCube(blockX, blockY, blockZ) ? true : (this.rand.nextInt(4) == 1 ? super.getCanSpawnHere() : false);
	}

	@Override
	protected void despawnEntity() {
		if(this.isDead) {
			super.despawnEntity();
		}
	}

	@Override
	protected Entity findPlayerToAttack() {
		// If something hit us, retaliate before looking for a fresh target.
		if(this.lastAttackingEntity != null && this.lastAttackingEntity != this.currentTarget) {
			return this.lastAttackingEntity;
		}

		EntityPlayer player = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
		if(player != null && !player.isCreative && this.canEntityBeSeen(player) && (!player.dressedAsAPirate() || this.angryAtPlayer)) {
			return player;
		}

		return null;
	}

	@Override
	public void onDeath(Entity source) {
		if(source instanceof EntityPlayer) {
			((EntityPlayer)source).triggerAchievement(AchievementList.pirateKill);
		}

		super.onDeath(source);
	}
}
