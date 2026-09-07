package net.minecraft.client;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.game.MCHash;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.IWorldAccess;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldProvider;
import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.network.packet.Packet255KickDisconnect;
import net.minecraft.game.Seasons;

public class WorldClient extends World {
	private NetClientHandler sendQueue;
	private ChunkProviderClient chunkProviderClient;
	private MCHash entityHashSet = new MCHash();
	private Set<Entity> entityList = new HashSet<Entity>();
	private Set<Entity> entitySpawnQueue = new HashSet<Entity>();

	public WorldClient(NetClientHandler netClientHandler, WorldSettings worldSettings, int dimension) {
		super(new SaveHandlerMP(), "MpServer", WorldProvider.getProviderForDimension(dimension), (WorldSettings)worldSettings);
		this.sendQueue = netClientHandler;
		this.setSpawnPoint(new ChunkCoordinates(8, 64, 8));
		this.mapStorage = netClientHandler.mapStorage;
		
	}

	public void tick() {

		this.updateWeather();
		long worldTime = this.getWorldTime() + 1L;
		int hourOfTheDay = (int)(worldTime % 24000L);
	
		this.setWorldTime(worldTime);
		int newSkylight = this.calculateSkylightSubtracted(1.0F);
		int i;
		if(newSkylight != this.skylightSubtracted) {
			this.skylightSubtracted = newSkylight;

			for(i = 0; i < this.worldAccesses.size(); ++i) {
				((IWorldAccess)this.worldAccesses.get(i)).updateAllRenderers();
			}
		}
	
		this.updateDailyTasks(worldTime, hourOfTheDay);

		for(i = 0; i < 10 && !this.entitySpawnQueue.isEmpty(); ++i) {
			Entity entity = (Entity)this.entitySpawnQueue.iterator().next();
			if(!this.getLoadedEntityList().contains(entity)) {
				this.spawnEntityInWorld(entity);
			}
		}

		this.sendQueue.processReadPackets();
		this.chunkProviderClient.unload100OldestChunks();
	}

	protected IChunkProvider getChunkProvider() {
		this.chunkProviderClient = new ChunkProviderClient(this);
		return this.chunkProviderClient;
	}

	public void setSpawnLocation() {
		this.setSpawnPoint(new ChunkCoordinates(8, 64, 8));
	}

	protected void updateBlocksAndPlayCaveSounds() {
	}

	public void scheduleBlockUpdate(int x, int y, int z, int blockId, int metadata) {
	}

	public boolean TickUpdates(boolean doProfiling) {
		return false;
	}

	public void doPreChunk(int x, int z, boolean load) {
		if(load) {
			this.chunkProviderClient.prepareChunk(x, z);
		} else {
			this.chunkProviderClient.unloadChunk(x, z);
		}

		if(!load) {
			this.markBlocksDirty(x * 16, 0, z * 16, x * 16 + 15, 128, z * 16 + 15);
		}

	}

	public boolean spawnEntityInWorld(Entity entity) {
		boolean wasAdded = super.spawnEntityInWorld(entity);
		this.entityList.add(entity);
		if(!wasAdded) {
			this.entitySpawnQueue.add(entity);
		}

		return wasAdded;
	}

	public void setEntityDead(Entity entity) {
		super.setEntityDead(entity);
		this.entityList.remove(entity);
	}

	protected void obtainEntitySkin(Entity entity) {
		super.obtainEntitySkin(entity);
		if(this.entitySpawnQueue.contains(entity)) {
			this.entitySpawnQueue.remove(entity);
		}

	}

	protected void releaseEntitySkin(Entity entity) {
		super.releaseEntitySkin(entity);
		if(this.entityList.contains(entity)) {
			this.entitySpawnQueue.add(entity);
		}

	}

	public void addEntityToWorld(int entityId, Entity entity) {
		Entity existing = this.getEntityByID(entityId);
		if(existing != null) {
			this.setEntityDead(existing);
		}

		this.entityList.add(entity);
		entity.entityId = entityId;
		if(!this.spawnEntityInWorld(entity)) {
			this.entitySpawnQueue.add(entity);
		}

		this.entityHashSet.addKey(entityId, entity);
	}

	public Entity getEntityByID(int entityId) {
		return (Entity)this.entityHashSet.lookup(entityId);
	}

	public Entity removeEntityFromWorld(int entityId) {
		Entity entity = (Entity)this.entityHashSet.removeObject(entityId);
		if(entity != null) {
			this.entityList.remove(entity);
			this.setEntityDead(entity);
		}

		return entity;
	}

	public boolean setBlockAndMetadataAndInvalidate(int x, int y, int z, int blockID, int metadata) {
		return super.setBlockAndMetadataWithNotify(x, y, z, blockID, metadata);
	}

	public void sendQuittingDisconnectingPacket() {
		this.sendQueue.quitWithPacket(new Packet255KickDisconnect("Quitting"));
	}
	
	protected void badMoonDecide(long worldTime, int hourOfTheDay) {
		// Done in the server
	}
	
	protected void updateDailyTasks(long worldTime, int hourOfTheDay) {
		// Blood moon
		if(hourOfTheDay == Seasons.dayLengthTicks && this.worldInfo.isBloodMoon()) {
			if (this.badMoonText == false) {
				this.getWorldAccess(0).showString("Bad Moon Rising");
				// TODO :: ADD SOUND EFFECT
			}
			this.badMoonText = true;
		} else this.badMoonText = false;

		if(hourOfTheDay == 0) this.worldInfo.setBloodMoon(false);
	}

	public void performDayOfTheYearUpdate(int dayOfTheYear) {
		int oldCurrentSeason = Seasons.currentSeason;
		
		Seasons.dayOfTheYear = dayOfTheYear;
		Seasons.updateSeasonCounters();
		
		if(Seasons.currentSeason != oldCurrentSeason) {
			if(Seasons.currentSeason == Seasons.WINTER) {
				if(!this.worldInfo.getSnowing()) {
					int newSnowingTime = Weather.getTimeForNextSnow(this.rand);
					if(newSnowingTime < this.worldInfo.getSnowingTime()) {
						this.worldInfo.setSnowingTime(newSnowingTime);
					}
				}
				if(this.worldInfo.getRaining()) {
					int newRainingTime = 3000 + this.rand.nextInt(3000);
					if(this.worldInfo.getRainTime() > newRainingTime) this.worldInfo.setRainTime(newRainingTime);
				}
			}
			
			if(!this.worldInfo.getRaining() && (Seasons.currentSeason == Seasons.SPRING || Seasons.currentSeason == Seasons.AUTUMN)) {
				int newRainingTime = Weather.getTimeForNextRain(this.rand);
				if(newRainingTime < this.worldInfo.getRainTime()) {
					this.worldInfo.setRainTime(newRainingTime);
				}
			}
			
			this.getWorldAccess(0).showString(Seasons.seasonNames[Seasons.currentSeason]);
		}
	}
	
	protected void updateWeather() {
		if(!this.worldProvider.hasNoSky) {
			if(this.lastLightningBolt > 0) {
				--this.lastLightningBolt;
			}

			this.prevThunderingStrength = this.thunderingStrength;
			
			if(this.worldInfo.getThundering()) {
				this.thunderingStrength = (float)((double)this.thunderingStrength + 0.01D);
			} else {
				this.thunderingStrength = (float)((double)this.thunderingStrength - 0.01D);
			}

			if(this.thunderingStrength < 0.0F) {
				this.thunderingStrength = 0.0F;
			}

			if(this.thunderingStrength > 1.0F) {
				this.thunderingStrength = 1.0F;
			}
			
			this.prevSnowingStrength = this.snowingStrength;
			
			if(this.worldInfo.getSnowing()) {
				this.snowingStrength = (float)((double)this.snowingStrength + 0.01D);
			} else {
				this.snowingStrength = (float)((double)this.snowingStrength - 0.01D);
			}

			if(this.snowingStrength < 0.0F) {
				this.snowingStrength = 0.0F;
			}

			if(this.snowingStrength > 1.0F) {
				this.snowingStrength = 1.0F;
			}
			
			this.prevRainingStrength = this.rainingStrength;
			
			if(this.worldInfo.getRaining()) {
				this.rainingStrength = (float)((double)this.rainingStrength + 0.01D);
			} else {
				this.rainingStrength = (float)((double)this.rainingStrength - 0.01D);
			}

			if(this.rainingStrength < 0.0F) {
				this.rainingStrength = 0.0F;
			}

			if(this.rainingStrength > 1.0F) {
				this.rainingStrength = 1.0F;
			}

		}
	}
}