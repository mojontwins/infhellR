package net.minecraft.client;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;

public class EntityOtherPlayerMP extends EntityPlayer {
	private boolean isItemInUse = false;
	private int interpPosRotationIncrements;
	private double interpTargetX;
	private double interpTargetY;
	private double interpTargetZ;
	private double interpTargetYaw;
	private double interpTargetPitch;

	public EntityOtherPlayerMP(World world, String username) {
		super(world);
		this.username = username;
		this.yOffset = 0.0F;
		this.stepHeight = 0.0F;
		if (username != null && username.length() > 0) {
			this.skinUrl = "http://s3.amazonaws.com/MinecraftSkins/" + username + ".png";
		}
		this.noClip = true;
		this.bedAdjustPosY = 0.25F;
		this.renderDistanceWeight = 10.0D;
	}

	protected void resetHeight() {
		this.yOffset = 0.0F;
	}

	public boolean attackEntityFrom(Entity source, int damage) {
		return true;
	}

	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int increments) {
		this.interpTargetX = x;
		this.interpTargetY = y;
		this.interpTargetZ = z;
		this.interpTargetYaw = (double) yaw;
		this.interpTargetPitch = (double) pitch;
		this.interpPosRotationIncrements = increments;
	}

	public void onUpdate() {
		this.bedAdjustPosY = 0.0F;
		super.onUpdate();
		this.prevLimbYaw = this.limbYaw;
		double dx = this.posX - this.prevPosX;
		double dz = this.posZ - this.prevPosZ;
		float limbDelta = MathHelper.sqrt_double(dx * dx + dz * dz) * 4.0F;
		if (limbDelta > 1.0F) {
			limbDelta = 1.0F;
		}
		this.limbYaw += (limbDelta - this.limbYaw) * 0.4F;
		this.limbSwing += this.limbYaw;

		if (!this.isItemInUse && this.isEating() && this.inventory.mainInventory[this.inventory.currentItem] != null) {
			ItemStack held = this.inventory.mainInventory[this.inventory.currentItem];
			this.setItemInUse(this.inventory.mainInventory[this.inventory.currentItem], Item.itemsList[held.itemID].getMaxItemUseDuration(held));
			this.isItemInUse = true;
		} else if (this.isItemInUse && !this.isEating()) {
			this.clearItemInUse();
			this.isItemInUse = false;
		}
	}

	public float getShadowSize() {
		return 0.0F;
	}

	public void onLivingUpdate() {
		super.updateEntityActionState();
		if (this.interpPosRotationIncrements > 0) {
			double nx = this.posX + (this.interpTargetX - this.posX) / (double) this.interpPosRotationIncrements;
			double ny = this.posY + (this.interpTargetY - this.posY) / (double) this.interpPosRotationIncrements;
			double nz = this.posZ + (this.interpTargetZ - this.posZ) / (double) this.interpPosRotationIncrements;

			double yawDelta;
			for (yawDelta = this.interpTargetYaw - (double) this.rotationYaw; yawDelta < -180.0D; yawDelta += 360.0D) {
			}
			while (yawDelta >= 180.0D) {
				yawDelta -= 360.0D;
			}
			this.rotationYaw = (float) ((double) this.rotationYaw + yawDelta / (double) this.interpPosRotationIncrements);
			this.rotationPitch = (float) ((double) this.rotationPitch + (this.interpTargetPitch - (double) this.rotationPitch) / (double) this.interpPosRotationIncrements);
			--this.interpPosRotationIncrements;
			this.setPosition(nx, ny, nz);
			this.setRotation(this.rotationYaw, this.rotationPitch);
		}

		this.prevCameraYaw = this.cameraYaw;
		float moveSpeed = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		float pitchSpeed = (float) Math.atan(-this.motionY * (double) 0.2F) * 15.0F;
		if (moveSpeed > 0.1F) moveSpeed = 0.1F;
		if (!this.onGround || this.health <= 0) moveSpeed = 0.0F;
		if (this.onGround || this.health <= 0) pitchSpeed = 0.0F;
		this.cameraYaw += (moveSpeed - this.cameraYaw) * 0.4F;
		this.field_9328_R += (pitchSpeed - this.field_9328_R) * 0.8F;
	}

	public void outfitWithItem(int slot, int itemId, int meta) {
		ItemStack item = null;
		if (itemId >= 0) {
			item = new ItemStack(itemId, 1, meta);
		}
		if (slot == 0) {
			this.inventory.mainInventory[this.inventory.currentItem] = item;
		} else {
			this.inventory.armorInventory[slot - 1] = item;
		}
	}

	public void func_6420_o() {
	}
}
