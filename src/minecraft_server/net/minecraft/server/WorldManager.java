package net.minecraft.server;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.IWorldAccess;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.network.packet.Packet61DoorChange;

/**
 * Server-side implementation of IWorldAccess. Acts as a bridge between
 * the World object and the server's networking layer. When blocks,
 * entities, or world events occur in WorldServer, the corresponding
 * IWorldAccess callbacks are invoked so that this class can broadcast
 * the changes to connected clients.
 *
 * Most visual/audio callbacks are no-ops on the server; the real
 * broadcasting is done by the configuration manager.
 */
public class WorldManager implements IWorldAccess {

	/** The MinecraftServer instance (for accessing configManager, entity trackers). */
	private MinecraftServer mcServer;

	/** The WorldServer whose events this manager listens to. */
	private WorldServer worldServer;

	/**
	 * Creates a new WorldManager that will route world events
	 * through the given server to the given world.
	 */
	public WorldManager(MinecraftServer minecraftServer1, WorldServer worldServer2) {
		this.mcServer = minecraftServer1;
		this.worldServer = worldServer2;
	}

	/** Spawns a particle effect on the client. (No-op on server.) */
	public void spawnParticle(String name, double x, double y, double z, double motionX, double motionY, double motionZ) {
	}

	/** Starts tracking an entity on the server's entity tracker. */
	public void obtainEntitySkin(Entity entity) {
		this.mcServer.getEntityTracker(this.worldServer.worldProvider.worldType).trackEntity(entity);
	}

	/** Stops tracking an entity on the server's entity tracker. */
	public void releaseEntitySkin(Entity entity) {
		this.mcServer.getEntityTracker(this.worldServer.worldProvider.worldType).untrackEntity(entity);
	}

	/** Plays a sound at the given world position. (No-op on server.) */
	public void playSound(String name, double x, double y, double z, float volume, float pitch) {
	}

	/** Marks a block range for a render update. (No-op on server.) */
	public void markBlockRangeNeedsUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
	}

	/** Notifies all renderers that chunks have changed. (No-op on server.) */
	public void updateAllRenderers() {
	}

	/** Marks a single block for a render update (sends to nearby players). */
	public void markBlockNeedsUpdate(int x, int y, int z) {
		this.mcServer.configManager.markBlockNeedsUpdate(x, y, z, this.worldServer.worldProvider.worldType);
	}

	/** Plays a music disc record. (No-op on server.) */
	public void playRecord(String name, int x, int y, int z) {
	}

	/** Sends a tile entity update packet to nearby players. */
	public void doNothingWithTileEntity(int x, int y, int z, TileEntity tileEntity4) {
		this.mcServer.configManager.sentTileEntityToPlayer(x, y, z, tileEntity4);
	}

	/** Plays an auxiliary SFX (e.g. door opening) and broadcasts it to all players in the world. */
	public void playAuxSFX(EntityPlayer player, int sfxId, int x, int y, int z, int data) {
		this.mcServer.configManager.s_func_28171_a(player, (double) x, (double) y, (double) z, 64.0D, this.worldServer.worldProvider.worldType,
				new Packet61DoorChange(sfxId, x, y, z, data));
	}

	/** Broadcasts a chat message to all players. */
	public void showString(String message) {
		this.mcServer.configManager.sendChatMessageToAllPlayers(message);
	}
}