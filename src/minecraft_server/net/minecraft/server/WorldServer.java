package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.game.MCHash;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.Explosion;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldProvider;
import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.chunk.loader.IChunkLoader;
import net.minecraft.game.world.chunk.loader.ISaveHandler;
import net.minecraft.network.packet.Packet4UpdateTime;
import net.minecraft.network.packet.Packet38EntityStatus;
import net.minecraft.network.packet.Packet54PlayNoteBlock;
import net.minecraft.network.packet.Packet60Explosion;
import net.minecraft.network.packet.Packet71Weather;
import net.minecraft.network.packet.Packet96BadMoonDecide;
import net.minecraft.network.packet.Packet98UpdateWeather;
import net.minecraft.game.entity.EntityWaterMob;
import net.minecraft.game.entity.animal.EntityAnimal;

/**
 * Server-side implementation of World. Extends the base World class
 * with server-specific behaviors: chunk provider management, entity
 * tracking, weather state broadcasting, and spawn protection.
 *
 * Each WorldServer is associated with one MinecraftServer and owns
 * its own chunk provider, entity tracker, and configuration manager.
 */
public class WorldServer extends World {

	/** The chunk provider that loads/generates chunks for this world. */
	public ChunkProviderServer chunkProviderServer;

	/** If true, spawn protection around the world spawn is disabled. */
	public boolean disableSpawnProtection = false;

	/** True while the world is being saved. */
	public boolean levelSaving;

	/** Reference to the owning MinecraftServer. */
	private MinecraftServer mcServer;

	/**
	 * Hash table used to track entity IDs for removal/lookup operations.
	 *
	 * <p>Not initialised inline: {@link World}'s constructor runs the spawn-location probe
	 * (which can fully generate and populate a chunk — including city graveyards that drop
	 * items when a flower loses support), and entity spawning during that probe calls
	 * {@link #obtainEntitySkin}, which would dereference this field before this class's field
	 * initialisers have run. It is therefore created lazily on first use (and sealed in the
	 * constructor body), so entities registered during construction are never lost.
	 */
	private MCHash entityRemoval;

	/**
	 * Constructs a new server world.
	 *
	 * @param minecraftServer1  the owning MinecraftServer
	 * @param iSaveHandler2     the save handler
	 * @param string3           world name
	 * @param i4                dimension ID
	 * @param worldSettings5    world settings
	 */
	public WorldServer(MinecraftServer minecraftServer1, ISaveHandler iSaveHandler2, String string3, int i4, WorldSettings worldSettings5) {
		super(iSaveHandler2, string3, worldSettings5, WorldProvider.getProviderForDimension(i4));
		this.mcServer = minecraftServer1;
		if (this.entityRemoval == null) {
			this.entityRemoval = new MCHash();
		}
	}

	/**
	 * Updates an entity, with optional server-side filtering.
	 * Peaceful mobs are removed if the server config forbids spawning them.
	 */
	public void updateEntityWithOptionalForce(Entity entity1, boolean flag2) {
		if (!this.mcServer.spawnPeacefulMobs && (entity1 instanceof EntityAnimal || entity1 instanceof EntityWaterMob)) {
			entity1.setEntityDead();
		}
		if (entity1.riddenByEntity == null || !(entity1.riddenByEntity instanceof EntityPlayer)) {
			super.updateEntityWithOptionalForce(entity1, flag2);
		}
	}

	/** Delegates directly to the parent method (wrapper for client↔server dispatch). */
	public void s_func_12017_b(Entity entity1, boolean flag2) {
		super.updateEntityWithOptionalForce(entity1, flag2);
	}

	/** Creates and returns the chunk provider for this server world. */
	protected IChunkProvider getChunkProvider() {
		IChunkLoader chunkLoader1 = this.saveHandler.getChunkLoader(this.worldProvider);
		this.chunkProviderServer = new ChunkProviderServer(this, chunkLoader1, this.worldProvider.getChunkProvider());
		return this.chunkProviderServer;
	}

	/** Returns tile entities within a given bounding box. */
	public List<TileEntity> getTileEntityList(int x1, int y2, int z3, int x4, int y5, int z6) {
		ArrayList<TileEntity> result7 = new ArrayList<TileEntity>();
		for (int i8 = 0; i8 < this.loadedTileEntityList.size(); ++i8) {
			TileEntity tileEntity9 = (TileEntity) this.loadedTileEntityList.get(i8);
			if (tileEntity9.xCoord >= x1 && tileEntity9.yCoord >= y2 && tileEntity9.zCoord >= z3
					&& tileEntity9.xCoord < x4 && tileEntity9.yCoord < y5 && tileEntity9.zCoord < z6) {
				result7.add(tileEntity9);
			}
		}
		return result7;
	}

	/** Checks whether the given player is allowed to mine at the specified coordinates. */
	public boolean canMineBlock(EntityPlayer entityPlayer1, int x2, int y3, int z4) {
		int distX = (int) MathHelper.abs((float) (x2 - this.worldInfo.getSpawnX()));
		int distZ = (int) MathHelper.abs((float) (z4 - this.worldInfo.getSpawnZ()));
		int dist = distX > distZ ? distX : distZ;
		return dist > 16 || this.mcServer.configManager.isOp(entityPlayer1.username);
	}

	/** Adds an entity to the entity removal hash for ID tracking. */
	protected void obtainEntitySkin(Entity entity1) {
		super.obtainEntitySkin(entity1);
		if (this.entityRemoval == null) {
			this.entityRemoval = new MCHash();
		}
		this.entityRemoval.addKey(entity1.entityId, entity1);
	}

	/** Removes an entity from the ID tracking hash. */
	protected void releaseEntitySkin(Entity entity1) {
		super.releaseEntitySkin(entity1);
		if (this.entityRemoval != null) {
			this.entityRemoval.removeObject(entity1.entityId);
		}
	}

	/** Looks up an entity by its numeric ID in this world. */
	public Entity getEntityByID(int id1) {
		return this.entityRemoval == null ? null : (Entity) this.entityRemoval.lookup(id1);
	}

	/** Adds a weather effect (lightning) and broadcasts it to nearby players. */
	public boolean addWeatherEffect(Entity entity1) {
		if (super.addWeatherEffect(entity1)) {
			this.mcServer.configManager.sendPacketToPlayersAroundPoint(entity1.posX, entity1.posY, entity1.posZ, 512.0D,
					this.worldProvider.worldType, new Packet71Weather(entity1));
			return true;
		} else {
			return false;
		}
	}

	/** Sets an entity's state and broadcasts it to all tracked players. */
	public void setEntityState(Entity entity1, byte data2) {
		Packet38EntityStatus packet3 = new Packet38EntityStatus(entity1.entityId, data2);
		this.mcServer.getEntityTracker(this.worldProvider.worldType).sendPacketToTrackedPlayersAndTrackedEntity(entity1, packet3);
	}

	/** Creates a new explosion at the given coordinates. */
	public Explosion newExplosion(Entity entity1, double x2, double y4, double z6, float radius8, boolean flaming9) {
		Explosion explosion10 = new Explosion(this, entity1, x2, y4, z6, radius8);
		explosion10.isFlaming = flaming9;
		explosion10.doExplosion();
		explosion10.doEffects(false);
		this.mcServer.configManager.sendPacketToPlayersAroundPoint(x2, y4, z6, 64.0D, this.worldProvider.worldType,
				new Packet60Explosion(x2, y4, z6, radius8, explosion10.destroyedBlockPositions, 0));
		return explosion10;
	}

	/** Creates an explosion with an associated block ID. */
	public Explosion newBlockExplosion(Entity entity1, double x2, double y4, double z6, float radius8, int blockID, boolean flaming9) {
		Explosion explosion10 = new Explosion(this, entity1, x2, y4, z6, radius8);
		explosion10.isFlaming = flaming9;
		explosion10.doExplosion();
		explosion10.doEffects(false);
		this.mcServer.configManager.sendPacketToPlayersAroundPoint(x2, y4, z6, 64.0D, this.worldProvider.worldType,
				new Packet60Explosion(x2, y4, z6, radius8, explosion10.destroyedBlockPositions, blockID));
		return explosion10;
	}

	/** Plays a note block sound at the given coordinates and broadcasts it. */
	public void playNoteAt(int x1, int y2, int z3, int instrument4, int pitch5) {
		super.playNoteAt(x1, y2, z3, instrument4, pitch5);
		this.mcServer.configManager.sendPacketToPlayersAroundPoint((double) x1, (double) y2, (double) z3, 64.0D,
				this.worldProvider.worldType, new Packet54PlayNoteBlock(x1, y2, z3, instrument4, pitch5));
	}

	/**
	 * Sets the world time and immediately broadcasts the new time to every
	 * client in this dimension. Without this, a dramatic time jump (such as
	 * the haunted-cow curse advancing the clock to midnight) would only reach
	 * clients on the periodic one-second time sync, so the sky would not snap
	 * to night at the moment it happens.
	 */
	@Override
	public void setWorldTime(long worldTime) {
		super.setWorldTime(worldTime);
		if (this.mcServer != null && this.mcServer.configManager != null) {
			this.mcServer.configManager.sendPacketToAllPlayersInDimension(
					new Packet4UpdateTime(worldTime), this.worldProvider.worldType);
		}
	}

	/** Flushes the save handler's player data. */
	public void s_func_30006_w() {
		this.saveHandler.s_func_22093_e();
	}

	/** Updates weather state and broadcasts changes to all players. */
	protected void updateWeather() {
		boolean wasSnowing = this.worldInfo.getSnowing();
		boolean wasRaining = this.worldInfo.getRaining();
		boolean wasThundering = this.worldInfo.getThundering();

		super.updateWeather();

		if (wasSnowing != this.worldInfo.getSnowing() || wasRaining != this.worldInfo.getRaining() || wasThundering != this.worldInfo.getThundering()) {
			this.mcServer.configManager.sendPacketToAllPlayers(
					new Packet98UpdateWeather(this.worldInfo.getRaining(), this.worldInfo.getSnowing(), this.worldInfo.getThundering()));
		}
	}

	/** Handles the "bad moon" event and broadcasts it to all players when it changes. */
	protected void badMoonDecide(long worldTime, int hourOfTheDay) {
		boolean prevBadMoonDecide = this.badMoonDecide;
		super.badMoonDecide(worldTime, hourOfTheDay);
		if (prevBadMoonDecide != this.badMoonDecide) {
			this.mcServer.configManager.sendPacketToAllPlayers(new Packet96BadMoonDecide(this.badMoonDecide));
		}
	}
}