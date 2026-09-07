package net.minecraft.game.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.IMob;
import net.minecraft.game.entity.IWaterMob;
import net.minecraft.game.entity.animal.EntityAnimal;
import net.minecraft.game.entity.player.EntityPlayer;

/**
 * Owns the world's live-entity bookkeeping: the {@link #loadedEntityList}, the entities queued
 * for removal at the next sweep ({@link #unloadedEntityList}), and cached per-category entity
 * counts, together with the entity lifecycle operations — spawning, marking dead, player removal,
 * bulk chunk-load/unload, and the per-frame list maintenance.
 *
 * <p>The cached counters ({@link #getMobCount()}, {@link #getAnimalCount()},
 * {@link #getWaterMobCount()}, {@link #getCachedEntityCount(Class)}) are kept in sync with every
 * add/remove of {@link #loadedEntityList} so the creature spawner can size caps without an O(n)
 * scan per type. Extracted from {@link World} in the refactor; {@code World} keeps its public
 * {@code spawnEntityInWorld}/{@code setEntityDead}/{@code getLoadedEntityList}/{@code countEntities}
 * (&amp; co.) entry points as thin delegates, so external callers are unchanged.
 */
public final class EntityManager {

	/** The owning world, used for chunk lookups, the player list and the (virtual) skin hooks. */
	private final World world;

	/** Every live entity in the world, in no particular order. */
	private final List<Entity> loadedEntityList = new ArrayList<Entity>();

	/** Entities queued to be removed at the next sweep (from {@code updateEntities} / {@code updateEntityList}). */
	private final List<Entity> unloadedEntityList = new ArrayList<Entity>();

	/** Cached count of {@link IMob} instances in {@link #loadedEntityList}. */
	private int mobCount;

	/** Cached count of {@link EntityAnimal} instances in {@link #loadedEntityList}. */
	private int animalCount;

	/** Cached count of {@link IWaterMob} instances in {@link #loadedEntityList}. */
	private int waterMobCount;

	/** Constructs an entity manager bound to the given world. */
	EntityManager(World world) {
		this.world = world;
	}

	/** Returns the world's live entity list. */
	List<Entity> getLoadedEntityList() {
		return this.loadedEntityList;
	}

	/** Returns the entity with the given id, or {@code null} if not loaded. */
	Entity getEntityById(int id) {
		Iterator<Entity> it = this.loadedEntityList.iterator();
		while(it.hasNext()) {
			Entity e = it.next();
			if(e.entityId == id) {
				return e;
			}
		}
		return null;
	}

	/** Returns the cached count of hostile (IMob) entities. */
	int getMobCount() {
		return this.mobCount;
	}

	/** Returns the cached count of peaceful (EntityAnimal) entities. */
	int getAnimalCount() {
		return this.animalCount;
	}

	/** Returns the cached count of aquatic (IWaterMob) entities. */
	int getWaterMobCount() {
		return this.waterMobCount;
	}

	/**
	 * Returns a cached entity count for the given class, avoiding a full-list scan where possible.
	 * Dispatches to the three maintained counters for the spawner's creature types (hostile,
	 * peaceful, aquatic) and falls back to an O(n) scan for any other class.
	 */
	int getCachedEntityCount(Class<?> entityClass) {
		if(entityClass == IMob.class) {
			return this.mobCount;
		} else if(entityClass == EntityAnimal.class) {
			return this.animalCount;
		} else if(entityClass == IWaterMob.class) {
			return this.waterMobCount;
		}
		return this.countEntities(entityClass);
	}

	/** Counts every entity in the world whose class is assignable from {@code entityClass}. */
	int countEntities(Class<?> entityClass) {
		int count = 0;
		for(int i = 0; i < this.loadedEntityList.size(); ++i) {
			Entity entity = this.loadedEntityList.get(i);
			if(entityClass.isAssignableFrom(entity.getClass())) {
				++count;
			}
		}
		return count;
	}

	/**
	 * Adds an entity to the world: it goes into its chunk's entity list, the world entity list and
	 * the cached counters, and the world-access hooks are notified so the renderer can request the
	 * entity's skins. Players additionally join the player list and refresh the sleeping flag.
	 *
	 * <p>Returns {@code false} if a non-player entity's chunk is not present; the base world method
	 * refuses such spawns and {@code WorldClient}'s override then queues the entity for a retry.
	 */
	boolean spawnEntityInWorld(Entity entity) {
		int chunkX = MathHelper.floor_double(entity.posX / 16.0D);
		int chunkZ = MathHelper.floor_double(entity.posZ / 16.0D);
		boolean isPlayer = entity instanceof EntityPlayer;
		if(!isPlayer && !this.world.chunkExists(chunkX, chunkZ)) {
			return false;
		}

		if(isPlayer) {
			EntityPlayer player = (EntityPlayer)entity;
			this.world.playerEntities.add(player);
			this.world.updateAllPlayersSleepingFlag();
		}

		this.world.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(entity);
		this.loadedEntityList.add(entity);
		this.updateEntityCountOnAdd(entity);
		this.world.obtainEntitySkin(entity);
		return true;
	}

	/**
	 * Marks an entity dead, dismounting it and removing players from the player list (which
	 * refreshes the sleeping flag). The entity is left in {@link #loadedEntityList} until the next
	 * sweep; the sweep releases its chunk reference and its skin.
	 */
	void setEntityDead(Entity entity) {
		if(entity.riddenByEntity != null) {
			entity.riddenByEntity.mountEntity((Entity)null);
		}

		if(entity.ridingEntity != null) {
			entity.mountEntity((Entity)null);
		}

		entity.setEntityDead();
		if(entity instanceof EntityPlayer) {
			this.world.playerEntities.remove((EntityPlayer)entity);
			this.world.updateAllPlayersSleepingFlag();
		}
	}

	/**
	 * Removes a player outright: marks it dead, detaches it from its chunk and from
	 * {@link #loadedEntityList}, adjusts the cached counters and releases its skin.
	 */
	void removePlayer(Entity entity) {
		entity.setEntityDead();
		if(entity instanceof EntityPlayer) {
			this.world.playerEntities.remove((EntityPlayer)entity);
			this.world.updateAllPlayersSleepingFlag();
		}

		int chunkX = entity.chunkCoordX;
		int chunkZ = entity.chunkCoordZ;
		if(entity.addedToChunk && this.world.chunkExists(chunkX, chunkZ)) {
			this.world.getChunkFromChunkCoords(chunkX, chunkZ).removeEntity(entity);
		}

		if(this.loadedEntityList.remove(entity)) {
			this.updateEntityCountOnRemove(entity);
		}

		this.world.releaseEntitySkin(entity);
	}

	/** Adds every entity in the list to the world in one bulk operation, updating counters and skins. */
	void addLoadedEntities(List<Entity> entities) {
		this.loadedEntityList.addAll(entities);
		for(int i = 0; i < entities.size(); ++i) {
			this.updateEntityCountOnAdd(entities.get(i));
		}
		for(int i = 0; i < entities.size(); ++i) {
			this.world.obtainEntitySkin(entities.get(i));
		}
	}

	/**
	 * Queues every entity in the list for removal at the next sweep (from
	 * {@code updateEntities} / {@code updateEntityList}); the actual list/counter adjustment and
	 * skin release happen in {@link #sweepUnloaded()}.
	 */
	void unloadEntities(List<Entity> entities) {
		this.unloadedEntityList.addAll(entities);
	}

	/** Adds an entity to the loaded list if it is not already present, updating counters. */
	void addIfAbsent(Entity entity) {
		if(!this.loadedEntityList.contains(entity)) {
			this.loadedEntityList.add(entity);
			this.updateEntityCountOnAdd(entity);
		}
	}

	/**
	 * Performs the deferred unload sweep: detaches every queued entity from its chunk, removes it
	 * from {@link #loadedEntityList} and the cached counters, releases its skin, and clears the
	 * pending queue. Called once per frame from the world's entity pass.
	 */
	void sweepUnloaded() {
		for(int i = 0; i < this.unloadedEntityList.size(); ++i) {
			this.removeFromLoadedList(this.unloadedEntityList.get(i));
		}
		for(int i = 0; i < this.unloadedEntityList.size(); ++i) {
			Entity entity = this.unloadedEntityList.get(i);
			int chunkX = entity.chunkCoordX;
			int chunkZ = entity.chunkCoordZ;
			if(entity.addedToChunk && this.world.chunkExists(chunkX, chunkZ)) {
				this.world.getChunkFromChunkCoords(chunkX, chunkZ).removeEntity(entity);
			}
		}
		for(int i = 0; i < this.unloadedEntityList.size(); ++i) {
			this.world.releaseEntitySkin(this.unloadedEntityList.get(i));
		}

		this.unloadedEntityList.clear();
	}

	/**
	 * Removes the entity at the given index of {@link #loadedEntityList} (used by the in-loop dead
	 * sweep), adjusting the cached counters and releasing the entity's skin.
	 */
	void removeEntityFromWorldList(Entity entity, int index) {
		this.loadedEntityList.remove(index);
		this.updateEntityCountOnRemove(entity);
		this.world.releaseEntitySkin(entity);
	}

	/** Removes a single entity from {@link #loadedEntityList}, dropping the cached counters if it was present. */
	private void removeFromLoadedList(Entity entity) {
		while(this.loadedEntityList.remove(entity)) {
			this.updateEntityCountOnRemove(entity);
		}
	}

	/** Bumps the per-category cached counters after a single entity is added to {@link #loadedEntityList}. */
	private void updateEntityCountOnAdd(Entity entity) {
		if(entity instanceof IMob) {
			++this.mobCount;
		}
		if(entity instanceof EntityAnimal) {
			++this.animalCount;
		}
		if(entity instanceof IWaterMob) {
			++this.waterMobCount;
		}
	}

	/** Drops the per-category cached counters after a single entity is removed from {@link #loadedEntityList}. */
	private void updateEntityCountOnRemove(Entity entity) {
		if(entity instanceof IMob) {
			--this.mobCount;
		}
		if(entity instanceof EntityAnimal) {
			--this.animalCount;
		}
		if(entity instanceof IWaterMob) {
			--this.waterMobCount;
		}
	}
}