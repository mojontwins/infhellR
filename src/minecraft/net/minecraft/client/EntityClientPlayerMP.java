package net.minecraft.client;

import net.minecraft.client.player.EntityPlayerSP;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.network.packet.Packet101CloseWindow;
import net.minecraft.network.packet.Packet10Flying;
import net.minecraft.network.packet.Packet11PlayerPosition;
import net.minecraft.network.packet.Packet12PlayerLook;
import net.minecraft.network.packet.Packet13PlayerLookMove;
import net.minecraft.network.packet.Packet14BlockDig;
import net.minecraft.network.packet.Packet18Animation;
import net.minecraft.network.packet.Packet19EntityAction;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.network.packet.Packet9Respawn;
import net.minecraft.game.entity.misc.EntityItem;

public class EntityClientPlayerMP extends EntityPlayerSP {
	public NetClientHandler sendQueue;
	private int inventoryUpdateTickCounter = 0;
	private boolean updatingHealth = false;
	private double oldPosX;
	private double oldBasePos;
	private double oldPosY;
	private double oldPosZ;
	private float oldRotationYaw;
	private float oldRotationPitch;
	private boolean wasOnGround = false;
	private boolean wasSneaking = false;
	private boolean wasSprinting = false;
	private int ticksIdle = 0;

	public EntityClientPlayerMP(Minecraft mc, World world, Session session, NetClientHandler sendQueue) {
		super(mc, world, session, 0);
		this.sendQueue = sendQueue;
	}

	public boolean attackEntityFrom(Entity source, int damage) {
		return false;
	}

	public void heal(int amount) {
	}

	public void onUpdate() {
		if (this.worldObj.blockExists(MathHelper.floor_double(this.posX), 64, MathHelper.floor_double(this.posZ))) {
			super.onUpdate();
			this.sendMotionUpdates();
		}
	}

	public void sendMotionUpdates() {
		if (this.inventoryUpdateTickCounter++ == 20) {
			this.sendInventoryChanged();
			this.inventoryUpdateTickCounter = 0;
		}

		boolean isSprinting = this.isSprinting();
		if (isSprinting != this.wasSprinting) {
			if (isSprinting) {
				this.sendQueue.addToSendQueue(new Packet19EntityAction(this, 4));
			} else {
				this.sendQueue.addToSendQueue(new Packet19EntityAction(this, 5));
			}
			this.wasSprinting = isSprinting;
		}

		boolean isSneaking = this.isSneaking();
		if (isSneaking != this.wasSneaking) {
			if (isSneaking) {
				this.sendQueue.addToSendQueue(new Packet19EntityAction(this, 1));
			} else {
				this.sendQueue.addToSendQueue(new Packet19EntityAction(this, 2));
			}
			this.wasSneaking = isSneaking;
		}

		double dx = this.posX - this.oldPosX;
		double dBaseY = this.boundingBox.minY - this.oldBasePos;
		double dy = this.posY - this.oldPosY;
		double dz = this.posZ - this.oldPosZ;
		double dYaw = (double) (this.rotationYaw - this.oldRotationYaw);
		double dPitch = (double) (this.rotationPitch - this.oldRotationPitch);
		boolean positionChanged = dBaseY != 0.0D || dy != 0.0D || dx != 0.0D || dz != 0.0D;
		boolean lookChanged = dYaw != 0.0D || dPitch != 0.0D;

		if (this.ridingEntity != null) {
			if (lookChanged) {
				this.sendQueue.addToSendQueue(new Packet11PlayerPosition(this.motionX, -999.0D, -999.0D, this.motionZ, this.onGround));
			} else {
				this.sendQueue.addToSendQueue(new Packet13PlayerLookMove(this.motionX, -999.0D, -999.0D, this.motionZ, this.rotationYaw, this.rotationPitch, this.onGround));
			}
			positionChanged = false;
		} else if (positionChanged && lookChanged) {
			this.sendQueue.addToSendQueue(new Packet13PlayerLookMove(this.posX, this.boundingBox.minY, this.posY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround));
			this.ticksIdle = 0;
		} else if (positionChanged) {
			this.sendQueue.addToSendQueue(new Packet11PlayerPosition(this.posX, this.boundingBox.minY, this.posY, this.posZ, this.onGround));
			this.ticksIdle = 0;
		} else if (lookChanged) {
			this.sendQueue.addToSendQueue(new Packet12PlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
			this.ticksIdle = 0;
		} else {
			this.sendQueue.addToSendQueue(new Packet10Flying(this.onGround));
			if (this.wasOnGround == this.onGround && this.ticksIdle <= 200) {
				++this.ticksIdle;
			} else {
				this.ticksIdle = 0;
			}
		}

		this.wasOnGround = this.onGround;
		if (positionChanged) {
			this.oldPosX = this.posX;
			this.oldBasePos = this.boundingBox.minY;
			this.oldPosY = this.posY;
			this.oldPosZ = this.posZ;
		}
		if (lookChanged) {
			this.oldRotationYaw = this.rotationYaw;
			this.oldRotationPitch = this.rotationPitch;
		}
	}

	public EntityItem dropCurrentItem() {
		this.sendQueue.addToSendQueue(new Packet14BlockDig(4, 0, 0, 0, 0));
		return null;
	}

	private void sendInventoryChanged() {
	}

	protected void joinEntityItemWithWorld(EntityItem item) {
	}

	public void sendChatMessage(String text) {
		this.sendQueue.addToSendQueue(new Packet3Chat(text));
	}

	public void swingItem() {
		super.swingItem();
		this.sendQueue.addToSendQueue(new Packet18Animation(this, 1));
	}

	public void respawnPlayer() {
		this.sendInventoryChanged();
		ChunkCoordinates lastDeath = this.getPlayerLastDeathCoordinate();
		if (lastDeath == null) lastDeath = this.getPlayerSpawnCoordinate();
		this.sendQueue.addToSendQueue(new Packet9Respawn((byte) this.dimension, lastDeath.posX, lastDeath.posY, lastDeath.posZ));
	}

	protected void damageEntity(int amount) {
		this.health -= amount;
	}

	public void closeScreen() {
		this.sendQueue.addToSendQueue(new Packet101CloseWindow(this.craftingInventory.windowId));
		this.inventory.setItemStack((ItemStack) null);
		super.closeScreen();
	}

	public void setHealth(int health) {
		if (this.updatingHealth) {
			super.setHealth(health);
		} else {
			this.health = health;
			this.updatingHealth = true;
		}
	}

	public void addStat(StatBase stat, int amount) {
		if (stat != null && stat.isIndependent) {
			super.addStat(stat, amount);
		}
	}

	public void func_27027_b(StatBase stat, int amount) {
		if (stat != null && !stat.isIndependent) {
			super.addStat(stat, amount);
		}
	}
}
