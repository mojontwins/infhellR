package net.minecraft.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet17Sleep;
import net.minecraft.network.packet.Packet20NamedEntitySpawn;
import net.minecraft.network.packet.Packet21PickupSpawn;
import net.minecraft.network.packet.Packet23VehicleSpawn;
import net.minecraft.network.packet.Packet24MobSpawn;
import net.minecraft.network.packet.Packet25EntityPainting;
import net.minecraft.network.packet.Packet28EntityVelocity;
import net.minecraft.network.packet.Packet29DestroyEntity;
import net.minecraft.network.packet.Packet31RelEntityMove;
import net.minecraft.network.packet.Packet32EntityLook;
import net.minecraft.network.packet.Packet33RelEntityMoveLook;
import net.minecraft.network.packet.Packet34EntityTeleport;
import net.minecraft.network.packet.Packet40EntityMetadata;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet5PlayerInventory;
import net.minecraft.game.entity.DataWatcher;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityFish;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.IAnimals;
import net.minecraft.game.entity.misc.EntityBoat;
import net.minecraft.game.entity.misc.EntityFallingSand;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.entity.misc.EntityMinecart;
import net.minecraft.game.entity.misc.EntityTNTPrimed;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.projectile.EntityEgg;
import net.minecraft.game.entity.projectile.EntityFireball;
import net.minecraft.game.entity.projectile.EntityPebble;
import net.minecraft.game.entity.projectile.EntitySnowball;
import net.minecraft.game.entity.projectile.EntityThrowablePotion;

/**
 * Tracks a single entity for network synchronization with nearby players.
 * Manages the set of players who can see this entity, sends spawn packets
 * when players come into range, and periodically sends position/rotation
 * update packets based on configurable frequency and distance thresholds.
 *
 * Uses fixed-point encoding (multiply by 32) for positions and
 * 0..255 byte angles (multiply by 256/360) for rotations.
 */
public class EntityTrackerEntry {

	/** The entity being tracked. */
	public Entity trackedEntity;

	/** Maximum distance (in world units) a player must be within to see this entity. */
	public int trackingDistanceThreshold;

	/** How many ticks between position update packets. */
	public int updateFrequency;

	/** Last known position encoded as fixed-point integers (world * 32). */
	public int encodedPosX;
	public int encodedPosY;
	public int encodedPosZ;

	/** Last known rotation encoded as 0..255 byte angles (rotation * 256 / 360). */
	public int encodedRotationYaw;
	public int encodedRotationPitch;

	/** Last known velocity components. */
	public double lastTrackedEntityMotionX;
	public double lastTrackedEntityMotionY;
	public double lastTrackedEntityMotionZ;

	/** Tick counter for determining when to send updates. */
	public int updateCounter = 0;

	/** Last position used for determining whether the entity moved enough to re-evaluate visibility. */
	private double lastTrackedEntityPosX;
	private double lastTrackedEntityPosY;
	private double lastTrackedEntityPosZ;

	/** True after the first update (used to initialize last position). */
	private boolean firstUpdateDone = false;

	/** If true, velocity packets are sent whenever motion changes. */
	private boolean shouldSendMotionUpdates;

	/** Tracks time since last large positional change (for teleport threshold). */
	private int ticksFromLastUpdate = 0;

	/** Set to true by updatePlayerList if the player set changed this tick. */
	public boolean playerEntitiesUpdated = false;

	/** Set of players currently tracking this entity. */
	public Set<EntityPlayer> trackedPlayers = new HashSet<EntityPlayer>();

	/**
	 * Creates a new tracker for the given entity.
	 *
	 * @param entity                           the entity to track
	 * @param trackingDistance                 maximum tracking distance
	 * @param updateFreq                       ticks between updates
	 * @param sendMotionUpdates                whether to send velocity packets
	 */
	public EntityTrackerEntry(Entity entity, int trackingDistance, int updateFreq, boolean sendMotionUpdates) {
		this.trackedEntity = entity;
		this.trackingDistanceThreshold = trackingDistance;
		this.updateFrequency = updateFreq;
		this.shouldSendMotionUpdates = sendMotionUpdates;
		this.encodedPosX = MathHelper.floor_double(entity.posX * 32.0D);
		this.encodedPosY = MathHelper.floor_double(entity.posY * 32.0D);
		this.encodedPosZ = MathHelper.floor_double(entity.posZ * 32.0D);
		this.encodedRotationYaw = MathHelper.floor_float(entity.rotationYaw * 256.0F / 360.0F);
		this.encodedRotationPitch = MathHelper.floor_float(entity.rotationPitch * 256.0F / 360.0F);
	}

	public boolean equals(Object other) {
		return other instanceof EntityTrackerEntry
				? ((EntityTrackerEntry) other).trackedEntity.entityId == this.trackedEntity.entityId
				: false;
	}

	public int hashCode() {
		return this.trackedEntity.entityId;
	}

	/**
	 * Called each tick from EntityTracker.updateTrackedEntities.
	 * Evaluates visibility (add/remove players), and sends position/velocity/metadata
	 * packets at the configured frequency.
	 */
	public void updatePlayerList(List<EntityPlayer> playerList) {
		this.playerEntitiesUpdated = false;
		if (!this.firstUpdateDone || this.trackedEntity.getDistanceSq(this.lastTrackedEntityPosX, this.lastTrackedEntityPosY, this.lastTrackedEntityPosZ) > 16.0D) {
			this.lastTrackedEntityPosX = this.trackedEntity.posX;
			this.lastTrackedEntityPosY = this.trackedEntity.posY;
			this.lastTrackedEntityPosZ = this.trackedEntity.posZ;
			this.firstUpdateDone = true;
			this.playerEntitiesUpdated = true;
			this.updatePlayerEntities(playerList);
		}

		++this.ticksFromLastUpdate;
		if (++this.updateCounter % this.updateFrequency == 0) {
			// Encode current position as fixed-point (multiply by 32).
			int curPosX = MathHelper.floor_double(this.trackedEntity.posX * 32.0D);
			int curPosY = MathHelper.floor_double(this.trackedEntity.posY * 32.0D);
			int curPosZ = MathHelper.floor_double(this.trackedEntity.posZ * 32.0D);
			int curYaw = MathHelper.floor_float(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
			int curPitch = MathHelper.floor_float(this.trackedEntity.rotationPitch * 256.0F / 360.0F);

			// Delta since last encoded position (relative position).
			int deltaX = curPosX - this.encodedPosX;
			int deltaY = curPosY - this.encodedPosY;
			int deltaZ = curPosZ - this.encodedPosZ;
			Packet packet = null;

			// Determine if position/rotation changes are large enough to require a teleport.
			boolean largePositionChange = Math.abs(curPosX) >= 8 || Math.abs(curPosY) >= 8 || Math.abs(curPosZ) >= 8;
			boolean largeRotationChange = Math.abs(curYaw - this.encodedRotationYaw) >= 8 || Math.abs(curPitch - this.encodedRotationPitch) >= 8;

			// Choose the most efficient packet type.
			// Relative move packets only work if delta fits in signed byte (-128..127).
			if (deltaX >= -128 && deltaX < 128 && deltaY >= -128 && deltaY < 128
					&& deltaZ >= -128 && deltaZ < 128 && this.ticksFromLastUpdate <= 400) {
				if (largePositionChange && largeRotationChange) {
					packet = new Packet33RelEntityMoveLook(this.trackedEntity.entityId, (byte) deltaX, (byte) deltaY, (byte) deltaZ,
							(byte) curYaw, (byte) curPitch);
				} else if (largePositionChange) {
					packet = new Packet31RelEntityMove(this.trackedEntity.entityId, (byte) deltaX, (byte) deltaY, (byte) deltaZ);
				} else if (largeRotationChange) {
					packet = new Packet32EntityLook(this.trackedEntity.entityId, (byte) curYaw, (byte) curPitch);
				}
			} else {
				// Entity went too far or timed out: full teleport packet.
				this.ticksFromLastUpdate = 0;
				this.trackedEntity.posX = (double) curPosX / 32.0D;
				this.trackedEntity.posY = (double) curPosY / 32.0D;
				this.trackedEntity.posZ = (double) curPosZ / 32.0D;
				packet = new Packet34EntityTeleport(this.trackedEntity.entityId, curPosX, curPosY, curPosZ, (byte) curYaw, (byte) curPitch);
			}

			// Send velocity update if the entity's motion changed significantly.
			if (this.shouldSendMotionUpdates) {
				double motionDeltaX = this.trackedEntity.motionX - this.lastTrackedEntityMotionX;
				double motionDeltaY = this.trackedEntity.motionY - this.lastTrackedEntityMotionY;
				double motionDeltaZ = this.trackedEntity.motionZ - this.lastTrackedEntityMotionZ;
				double motionThreshold = 0.02D;
				double motionSquared = motionDeltaX * motionDeltaX + motionDeltaY * motionDeltaY + motionDeltaZ * motionDeltaZ;
				if (motionSquared > motionThreshold * motionThreshold || (motionSquared > 0.0D
						&& this.trackedEntity.motionX == 0.0D && this.trackedEntity.motionY == 0.0D && this.trackedEntity.motionZ == 0.0D)) {
					this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
					this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
					this.lastTrackedEntityMotionZ = this.trackedEntity.motionZ;
					this.sendPacketToTrackedPlayers(new Packet28EntityVelocity(this.trackedEntity.entityId,
							this.lastTrackedEntityMotionX, this.lastTrackedEntityMotionY, this.lastTrackedEntityMotionZ));
				}
			}

			if (packet != null) {
				this.sendPacketToTrackedPlayers(packet);
			}

			DataWatcher watcher = this.trackedEntity.getDataWatcher();
			if (watcher.hasObjectChanged()) {
				this.sendPacketToTrackedPlayersAndTrackedEntity(new Packet40EntityMetadata(this.trackedEntity.entityId, watcher));
			}

			if (largePositionChange) {
				this.encodedPosX = curPosX;
				this.encodedPosY = curPosY;
				this.encodedPosZ = curPosZ;
			}
			if (largeRotationChange) {
				this.encodedRotationYaw = curYaw;
				this.encodedRotationPitch = curPitch;
			}
		}

		// Send velocity packet when entity takes damage.
		if (this.trackedEntity.beenAttacked) {
			this.sendPacketToTrackedPlayersAndTrackedEntity(new Packet28EntityVelocity(this.trackedEntity));
			this.trackedEntity.beenAttacked = false;
		}
	}

	/** Sends a packet to all players tracking this entity. */
	public void sendPacketToTrackedPlayers(Packet packet) {
		for (EntityPlayer player : this.trackedPlayers) {
			((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
		}
	}

	/** Sends a packet to all tracking players and to the entity itself (if it's a player). */
	public void sendPacketToTrackedPlayersAndTrackedEntity(Packet packet) {
		this.sendPacketToTrackedPlayers(packet);
		if (this.trackedEntity instanceof EntityPlayerMP) {
			((EntityPlayerMP) this.trackedEntity).playerNetServerHandler.sendPacket(packet);
		}
	}

	/** Sends a destroy-entity packet to all tracking players. */
	public void sendDestroyEntityPacketToTrackedPlayers() {
		this.sendPacketToTrackedPlayers(new Packet29DestroyEntity(this.trackedEntity.entityId));
	}

	/** Removes a player from this entity's tracking list. */
	public void removeFromTrackedPlayers(EntityPlayerMP player) {
		if (this.trackedPlayers.contains(player)) {
			this.trackedPlayers.remove(player);
		}
	}

	/**
	 * Evaluates whether the given player is within tracking distance and
	 * adds/removes them from the tracking list accordingly. Sends spawn
	 * packets when a player comes into range, and destroy packets
	 * when they leave.
	 */
	public void updatePlayerEntity(EntityPlayerMP player) {
		if (player != this.trackedEntity) {
			double distX = player.posX - (double) (this.encodedPosX / 32);
			double distZ = player.posZ - (double) (this.encodedPosZ / 32);
			if (distX >= (double) (-this.trackingDistanceThreshold) && distX <= (double) this.trackingDistanceThreshold
					&& distZ >= (double) (-this.trackingDistanceThreshold) && distZ <= (double) this.trackingDistanceThreshold) {
				if (!this.trackedPlayers.contains(player)) {
					this.trackedPlayers.add(player);
					player.playerNetServerHandler.sendPacket(this.getSpawnPacket());
					if (this.shouldSendMotionUpdates) {
						player.playerNetServerHandler.sendPacket(
								new Packet28EntityVelocity(this.trackedEntity.entityId,
										this.trackedEntity.motionX, this.trackedEntity.motionY, this.trackedEntity.motionZ));
					}
					ItemStack[] inventory = this.trackedEntity.getInventory();
					if (inventory != null) {
						for (int i = 0; i < inventory.length; ++i) {
							player.playerNetServerHandler.sendPacket(new Packet5PlayerInventory(this.trackedEntity.entityId, i, inventory[i]));
						}
					}
					if (this.trackedEntity instanceof EntityPlayer) {
						EntityPlayer ep = (EntityPlayer) this.trackedEntity;
						if (ep.isPlayerSleeping()) {
							player.playerNetServerHandler.sendPacket(new Packet17Sleep(this.trackedEntity, 0,
									MathHelper.floor_double(this.trackedEntity.posX),
									MathHelper.floor_double(this.trackedEntity.posY),
									MathHelper.floor_double(this.trackedEntity.posZ)));
						}
					}
					if (this.trackedEntity instanceof EntityLiving) {
						EntityLiving living = (EntityLiving) this.trackedEntity;
						Iterator<StatusEffect> effects = living.getActiveStatusEffects().iterator();
						while (effects.hasNext()) {
							StatusEffect effect = effects.next();
							player.playerNetServerHandler.sendPacket(new Packet41EntityEffect(living.entityId, effect));
						}
					}
				}
			} else if (this.trackedPlayers.contains(player)) {
				this.trackedPlayers.remove(player);
				player.playerNetServerHandler.sendPacket(new Packet29DestroyEntity(this.trackedEntity.entityId));
			}
		}
	}

	/** Updates visibility for a list of players. */
	public void updatePlayerEntities(List<EntityPlayer> playerList) {
		for (int i = 0; i < playerList.size(); ++i) {
			this.updatePlayerEntity((EntityPlayerMP) playerList.get(i));
		}
	}

	/**
	 * Returns the appropriate spawn packet for the tracked entity type.
	 * Handles all entity types: items, players, mobs, vehicles, projectiles, etc.
	 */
	private Packet getSpawnPacket() {
		if (this.trackedEntity instanceof EntityItem) {
			EntityItem item = (EntityItem) this.trackedEntity;
			Packet21PickupSpawn pickup = new Packet21PickupSpawn(item);
			item.posX = (double) pickup.xPosition / 32.0D;
			item.posY = (double) pickup.yPosition / 32.0D;
			item.posZ = (double) pickup.zPosition / 32.0D;
			return pickup;
		} else if (this.trackedEntity instanceof EntityPlayerMP) {
			return new Packet20NamedEntitySpawn((EntityPlayer) this.trackedEntity);
		} else {
			if (this.trackedEntity instanceof EntityMinecart) {
				EntityMinecart cart = (EntityMinecart) this.trackedEntity;
				if (cart.minecartType == 0) return new Packet23VehicleSpawn(this.trackedEntity, 10);
				if (cart.minecartType == 1) return new Packet23VehicleSpawn(this.trackedEntity, 11);
				if (cart.minecartType == 2) return new Packet23VehicleSpawn(this.trackedEntity, 12);
			}
			if (this.trackedEntity instanceof EntityBoat) {
				return new Packet23VehicleSpawn(this.trackedEntity, 1);
			} else if (this.trackedEntity instanceof IAnimals) {
				return new Packet24MobSpawn((EntityLiving) this.trackedEntity);
			} else if (this.trackedEntity instanceof EntityFish) {
				return new Packet23VehicleSpawn(this.trackedEntity, 90);
			} else if (this.trackedEntity instanceof EntityArrow) {
				EntityLiving shooter = ((EntityArrow) this.trackedEntity).shootingEntity;
				return new Packet23VehicleSpawn(this.trackedEntity, 60, shooter != null ? shooter.entityId : this.trackedEntity.entityId);
			} else if (this.trackedEntity instanceof EntityThrowablePotion) {
				EntityLiving shooter = ((EntityThrowablePotion) this.trackedEntity).thrower;
				return new Packet23VehicleSpawn(this.trackedEntity, 101,
						shooter != null ? shooter.entityId : this.trackedEntity.entityId,
						((EntityThrowablePotion) this.trackedEntity).getPotionAsIndex());
			} else if (this.trackedEntity instanceof EntityPebble) {
				return new Packet23VehicleSpawn(this.trackedEntity, 100);
			} else if (this.trackedEntity instanceof EntitySnowball) {
				return new Packet23VehicleSpawn(this.trackedEntity, 61);
			} else if (this.trackedEntity instanceof EntityFireball) {
				EntityFireball fireball = (EntityFireball) this.trackedEntity;
				Packet23VehicleSpawn fireballPacket = new Packet23VehicleSpawn(this.trackedEntity, 63, fireball.shootingEntity.entityId);
				fireballPacket.motionXencoded = (int) (fireball.accelerationX * 8000.0D);
				fireballPacket.motionYencoded = (int) (fireball.accelerationY * 8000.0D);
				fireballPacket.motionZencoded = (int) (fireball.accelerationZ * 8000.0D);
				return fireballPacket;
			} else if (this.trackedEntity instanceof EntityEgg) {
				return new Packet23VehicleSpawn(this.trackedEntity, 62);
			} else if (this.trackedEntity instanceof EntityTNTPrimed) {
				return new Packet23VehicleSpawn(this.trackedEntity, 50);
			} else if (this.trackedEntity instanceof EntityFallingSand) {
				EntityFallingSand falling = (EntityFallingSand) this.trackedEntity;
				if (falling.blockID == Block.sand.blockID) return new Packet23VehicleSpawn(this.trackedEntity, 70);
				if (falling.blockID == Block.gravel.blockID) return new Packet23VehicleSpawn(this.trackedEntity, 71);
				if (falling.blockID == Block.cementPowder.blockID) return new Packet23VehicleSpawn(this.trackedEntity, 72);
			} else if (this.trackedEntity instanceof EntityPainting) {
				return new Packet25EntityPainting((EntityPainting) this.trackedEntity);
			}
			throw new IllegalArgumentException("Don't know how to add " + this.trackedEntity.getClass() + "!");
		}
	}

	/**
	 * Removes a player from tracking and sends a destroy packet.
	 * Called when a player disconnects so all nearby entities remove them.
	 */
	public void removeTrackedPlayerSymmetric(EntityPlayerMP player) {
		if (this.trackedPlayers.contains(player)) {
			this.trackedPlayers.remove(player);
			player.playerNetServerHandler.sendPacket(new Packet29DestroyEntity(this.trackedEntity.entityId));
		}
	}
}