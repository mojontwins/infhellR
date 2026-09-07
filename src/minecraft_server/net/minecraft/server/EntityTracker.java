package net.minecraft.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.game.MCHash;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityFish;
import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.IAnimals;
import net.minecraft.game.entity.animal.EntitySquid;
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
import net.minecraft.network.packet.Packet;

/**
 * Per-dimension entity tracker. Manages a set of EntityTrackerEntry
 * objects, one per tracked entity, that handle spawn/update/teleport
 * packets to nearby players.
 *
 * The tracker chooses appropriate tracking distance and update frequency
 * based on the entity type. Projectiles use short tracking distances and
 * high update frequencies; mobs use larger distances and lower rates.
 */
public class EntityTracker {

	/** Set of all currently tracked entity entries. */
	private Set<EntityTrackerEntry> trackedEntitySet = new HashSet<EntityTrackerEntry>();

	/** Hash table from entity ID to its tracker entry (for fast lookup). */
	private MCHash trackedEntityHashTable = new MCHash();

	/** The owning MinecraftServer. */
	private MinecraftServer server;

	/** The maximum tracking distance, inherited from server config. */
	private int maxTrackingDistanceThreshold;

	/** The dimension this tracker is associated with. */
	private int dimension;

	/**
	 * Creates a new entity tracker.
	 *
	 * @param server1 the MinecraftServer instance
	 * @param dimensionId2 the dimension ID (e.g. 0 for overworld, -1 for nether)
	 */
	public EntityTracker(MinecraftServer server1, int dimensionId2) {
		this.server = server1;
		this.dimension = dimensionId2;
		this.maxTrackingDistanceThreshold = server1.configManager.getMaxTrackingDistance();
	}

	/**
	 * Begins tracking an entity, choosing the appropriate tracking distance
	 * and update frequency based on the entity's class. Players always
	 * get the maximum tracking distance.
	 */
	public void trackEntity(Entity entity) {
		if (entity instanceof EntityPlayerMP) {
			this.trackEntity(entity, 512, 2);
			EntityPlayerMP player = (EntityPlayerMP) entity;
			Iterator<EntityTrackerEntry> iter = this.trackedEntitySet.iterator();
			while (iter.hasNext()) {
				EntityTrackerEntry entry = iter.next();
				if (entry.trackedEntity != player) {
					entry.updatePlayerEntity(player);
				}
			}
		} else if (entity instanceof EntityFish) {
			this.trackEntity(entity, 64, 5, true);
		} else if (entity instanceof EntityArrow) {
			this.trackEntity(entity, 64, 20, false);
		} else if (entity instanceof EntityThrowablePotion) {
			this.trackEntity(entity, 64, 20, false);
		} else if (entity instanceof EntityFireball) {
			this.trackEntity(entity, 64, 10, false);
		} else if (entity instanceof EntityPebble) {
			this.trackEntity(entity, 64, 10, true);
		} else if (entity instanceof EntitySnowball) {
			this.trackEntity(entity, 64, 10, true);
		} else if (entity instanceof EntityEgg) {
			this.trackEntity(entity, 64, 10, true);
		} else if (entity instanceof EntityItem) {
			this.trackEntity(entity, 64, 20, true);
		} else if (entity instanceof EntityMinecart) {
			this.trackEntity(entity, 160, 5, true);
		} else if (entity instanceof EntityBoat) {
			this.trackEntity(entity, 160, 5, true);
		} else if (entity instanceof EntitySquid) {
			this.trackEntity(entity, 160, 3, true);
		} else if (entity instanceof IAnimals) {
			this.trackEntity(entity, 160, 3);
		} else if (entity instanceof EntityTNTPrimed) {
			this.trackEntity(entity, 160, 10, true);
		} else if (entity instanceof EntityFallingSand) {
			this.trackEntity(entity, 160, 20, true);
		} else if (entity instanceof EntityPainting) {
			this.trackEntity(entity, 160, Integer.MAX_VALUE, false);
		}
	}

	/** Tracks an entity with explicit tracking distance and update frequency. */
	public void trackEntity(Entity entity, int trackingDistanceThreshold, int updateFrequency) {
		this.trackEntity(entity, trackingDistanceThreshold, updateFrequency, false);
	}

	/**
	 * Creates a new EntityTrackerEntry for the entity and starts
	 * tracking it.
	 */
	public void trackEntity(Entity entity, int trackingDistanceThreshold, int updateFrequency, boolean shouldSendMotionUpdates) {
		if (trackingDistanceThreshold > this.maxTrackingDistanceThreshold) {
			trackingDistanceThreshold = this.maxTrackingDistanceThreshold;
		}
		if (this.trackedEntityHashTable.containsItem(entity.entityId)) {
			throw new IllegalStateException("Entity is already tracked!");
		} else {
			EntityTrackerEntry entry = new EntityTrackerEntry(entity, trackingDistanceThreshold, updateFrequency, shouldSendMotionUpdates);
			this.trackedEntitySet.add(entry);
			this.trackedEntityHashTable.addKey(entity.entityId, entry);
			entry.updatePlayerEntities(this.server.getWorldManager(this.dimension).playerEntities);
		}
	}

	/** Stops tracking the given entity and broadcasts a destroy-entity packet. */
	public void untrackEntity(Entity entity) {
		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			Iterator<EntityTrackerEntry> iter = this.trackedEntitySet.iterator();
			while (iter.hasNext()) {
				EntityTrackerEntry entry = iter.next();
				entry.removeFromTrackedPlayers(player);
			}
		}
		EntityTrackerEntry entry = (EntityTrackerEntry) this.trackedEntityHashTable.removeObject(entity.entityId);
		if (entry != null) {
			this.trackedEntitySet.remove(entry);
			entry.sendDestroyEntityPacketToTrackedPlayers();
		}
	}

	/**
	 * Called each tick. Updates all tracked entities and re-evaluates which
	 * players each entity should be visible to. If a player gains visibility
	 * on tracked players, that player's other tracked entities are notified
	 * (so they can include the new player in their packet broadcasts).
	 */
	public void updateTrackedEntities() {
		ArrayList<EntityPlayerMP> playersToUpdate = new ArrayList<EntityPlayerMP>();
		Iterator<EntityTrackerEntry> iter = this.trackedEntitySet.iterator();
		while (iter.hasNext()) {
			EntityTrackerEntry entry = iter.next();
			entry.updatePlayerList(this.server.getWorldManager(this.dimension).playerEntities);
			if (entry.playerEntitiesUpdated && entry.trackedEntity instanceof EntityPlayerMP) {
				playersToUpdate.add((EntityPlayerMP) entry.trackedEntity);
			}
		}

		for (int i = 0; i < playersToUpdate.size(); ++i) {
			EntityPlayerMP player = (EntityPlayerMP) playersToUpdate.get(i);
			Iterator<EntityTrackerEntry> iter2 = this.trackedEntitySet.iterator();
			while (iter2.hasNext()) {
				EntityTrackerEntry entry = iter2.next();
				if (entry.trackedEntity != player) {
					entry.updatePlayerEntity(player);
				}
			}
		}
	}

	/** Sends a packet to all players tracking the given entity. */
	public void sendPacketToTrackedPlayers(Entity entity, Packet packet) {
		EntityTrackerEntry entry = (EntityTrackerEntry) this.trackedEntityHashTable.lookup(entity.entityId);
		if (entry != null) {
			entry.sendPacketToTrackedPlayers(packet);
		}
	}

	/** Sends a packet to all players tracking the entity AND to the entity itself (if a player). */
	public void sendPacketToTrackedPlayersAndTrackedEntity(Entity entity, Packet packet) {
		EntityTrackerEntry entry = (EntityTrackerEntry) this.trackedEntityHashTable.lookup(entity.entityId);
		if (entry != null) {
			entry.sendPacketToTrackedPlayersAndTrackedEntity(packet);
		}
	}

	/** Removes a player from all entities' tracked-players lists (e.g. on disconnect). */
	public void removeTrackedPlayerSymmetric(EntityPlayerMP player) {
		Iterator<EntityTrackerEntry> iter = this.trackedEntitySet.iterator();
		while (iter.hasNext()) {
			EntityTrackerEntry entry = iter.next();
			entry.removeTrackedPlayerSymmetric(player);
		}
	}
}