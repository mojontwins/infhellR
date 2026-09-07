package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.game.world.WorldProvider;

/**
 * Per-dimension player tracking and chunk subscription manager.
 * Maps each player to the chunks they can see, and provides efficient
 * lookups for broadcasting block updates to only the players who need them.
 *
 * Each dimension has its own PlayerManager instance. Players are subscribed
 * to chunks in a square region around their position. When a block changes,
 * the manager finds the PlayerInstance for that chunk and marks the block
 * as dirty so it can be batched and sent as a network packet.
 */
public class PlayerManager {

	/** All players currently in this dimension. */
	public List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();

	/** Hash table mapping (chunkX, chunkZ) to the PlayerInstance for that chunk. */
	private PlayerHash chunkInstances = new PlayerHash();

	/** List of PlayerInstances that have pending block updates to flush. */
	private List<PlayerInstance> dirtyChunks = new ArrayList<PlayerInstance>();

	/** Reference to the owning MinecraftServer. */
	private MinecraftServer mcServer;

	/** The dimension ID this manager handles. */
	private int playerDimension;

	/** The radius (in chunks) around each player that they can see. */
	private int playerViewRadius;

	/**
	 * The four cardinal directions used for spiral traversal when
	 * subscribing a player to chunks around their position.
	 */
	private final int[][] spiralDirections = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

	/**
	 * Creates a new PlayerManager for a dimension.
	 *
	 * @param server   the MinecraftServer instance
	 * @param dimension the dimension ID
	 * @param viewRadius the view radius in chunks (3..15)
	 * @throws IllegalArgumentException if viewRadius is outside 3..15
	 */
	public PlayerManager(MinecraftServer server, int dimension, int viewRadius) {
		if (viewRadius > 15) {
			throw new IllegalArgumentException("Too big view radius!");
		} else if (viewRadius < 3) {
			throw new IllegalArgumentException("Too small view radius!");
		}
		this.playerViewRadius = viewRadius;
		this.mcServer = server;
		this.playerDimension = dimension;
	}

	/** Returns the WorldServer for this dimension. */
	public WorldServer getMinecraftServer() {
		return this.mcServer.getWorldManager(this.playerDimension);
	}

	/**
	 * Called each tick. Flushes pending block updates from dirty PlayerInstances
	 * and, if the dimension has no players, unloads all chunks (for nether
	 * where the overworld player count determines chunk retention).
	 */
	public void updatePlayerInstances() {
		for (int i = 0; i < this.dirtyChunks.size(); ++i) {
			((PlayerInstance) this.dirtyChunks.get(i)).onUpdate();
		}
		this.dirtyChunks.clear();
		if (this.players.isEmpty()) {
			WorldServer world = this.mcServer.getWorldManager(this.playerDimension);
			WorldProvider provider = world.worldProvider;
			if (!provider.canRespawnHere()) {
				world.chunkProviderServer.unloadAllChunks();
			}
		}
	}

	/**
	 * Gets or creates the PlayerInstance for a chunk column.
	 *
	 * @param chunkX  chunk coordinate
	 * @param chunkZ  chunk coordinate
	 * @param create  if true, creates the instance if it doesn't exist
	 * @return the PlayerInstance, or null if not found and create is false
	 */
	private PlayerInstance getPlayerInstance(int chunkX, int chunkZ, boolean create) {
		// Encode chunk coords into a single long key for the hash table.
		long key = (long) chunkX + 2147483647L | ((long) chunkZ + 2147483647L) << 32;
		PlayerInstance instance = (PlayerInstance) this.chunkInstances.getValueByKey(key);
		if (instance == null && create) {
			instance = new PlayerInstance(this, chunkX, chunkZ);
			this.chunkInstances.add(key, instance);
		}
		return instance;
	}

	/**
	 * Marks a block as needing a network update. Finds the PlayerInstance
	 * for the block's chunk and queues the update.
	 *
	 * @param blockX block world X coordinate
	 * @param blockY block world Y coordinate
	 * @param blockZ block world Z coordinate
	 */
	public void markBlockNeedsUpdate(int blockX, int blockY, int blockZ) {
		int chunkX = blockX >> 4;
		int chunkZ = blockZ >> 4;
		PlayerInstance instance = this.getPlayerInstance(chunkX, chunkZ, false);
		if (instance != null) {
			instance.markBlockNeedsUpdate(blockX & 15, blockY, blockZ & 15);
		}
	}

	/**
	 * Adds a player and subscribes them to all chunks within the view radius.
	 * Uses a spiral traversal to subscribe in an expanding square pattern.
	 */
	public void addPlayer(EntityPlayerMP player) {
		int playerChunkX = (int) player.posX >> 4;
		int playerChunkZ = (int) player.posZ >> 4;
		player.managedPosX = player.posX;
		player.managedPosZ = player.posZ;

		int directionIndex = 0;
		int viewRadius = this.playerViewRadius;
		int offsetX = 0;
		int offsetZ = 0;

		// Subscribe the player's home chunk.
		this.getPlayerInstance(playerChunkX, playerChunkZ, true).addPlayer(player);

		// Spiral outward: expand the square one layer at a time.
		for (int layer = 1; layer <= viewRadius * 2; ++layer) {
			for (int arm = 0; arm < 2; ++arm) {
				int[] dir = this.spiralDirections[directionIndex++ % 4];
				for (int step = 0; step < layer; ++step) {
					offsetX += dir[0];
					offsetZ += dir[1];
					this.getPlayerInstance(playerChunkX + offsetX, playerChunkZ + offsetZ, true).addPlayer(player);
				}
			}
		}

		// Finish the outermost ring (the last incomplete edge).
		directionIndex %= 4;
		for (int i = 0; i < viewRadius * 2; ++i) {
			offsetX += this.spiralDirections[directionIndex][0];
			offsetZ += this.spiralDirections[directionIndex][1];
			this.getPlayerInstance(playerChunkX + offsetX, playerChunkZ + offsetZ, true).addPlayer(player);
		}

		this.players.add(player);
	}

	/** Removes a player from all chunk subscriptions. */
	public void removePlayer(EntityPlayerMP player) {
		int playerChunkX = (int) player.managedPosX >> 4;
		int playerChunkZ = (int) player.managedPosZ >> 4;

		// Remove from every chunk in the view radius.
		for (int cx = playerChunkX - this.playerViewRadius; cx <= playerChunkX + this.playerViewRadius; ++cx) {
			for (int cz = playerChunkZ - this.playerViewRadius; cz <= playerChunkZ + this.playerViewRadius; ++cz) {
				PlayerInstance instance = this.getPlayerInstance(cx, cz, false);
				if (instance != null) {
					instance.removePlayer(player);
				}
			}
		}

		this.players.remove(player);
	}

	/**
	 * Checks whether a chunk is outside the player's view radius.
	 */
	private boolean isOutsideViewRadius(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
		int dx = chunkX - playerChunkX;
		int dz = chunkZ - playerChunkZ;
		return dx >= -this.playerViewRadius && dx <= this.playerViewRadius
				? dz < -this.playerViewRadius || dz > this.playerViewRadius
				: true;
	}

	/**
	 * Called each tick for each player. If the player has moved more than one
	 * chunk since their last managed position, this method adds and removes
	 * chunk subscriptions as needed to keep the visible region centered on
	 * the player.
	 */
	public void updateMountedMovingPlayer(EntityPlayerMP player) {
		int playerChunkX = (int) player.posX >> 4;
		int playerChunkZ = (int) player.posZ >> 4;
		double dx = player.managedPosX - player.posX;
		double dz = player.managedPosZ - player.posZ;
		double movedSq = dx * dx + dz * dz;

		if (movedSq >= 64.0D) {
			int oldChunkX = (int) player.managedPosX >> 4;
			int oldChunkZ = (int) player.managedPosZ >> 4;
			int deltaChunkX = playerChunkX - oldChunkX;
			int deltaChunkZ = playerChunkZ - oldChunkZ;

			if (deltaChunkX != 0 || deltaChunkZ != 0) {
				// Add subscriptions for newly entered chunks.
				for (int cx = playerChunkX - this.playerViewRadius; cx <= playerChunkX + this.playerViewRadius; ++cx) {
					for (int cz = playerChunkZ - this.playerViewRadius; cz <= playerChunkZ + this.playerViewRadius; ++cz) {
						if (!this.isOutsideViewRadius(cx, cz, oldChunkX, oldChunkZ)) {
							this.getPlayerInstance(cx, cz, true).addPlayer(player);
						}
					}
				}

				// Remove subscriptions for chunks that the player left.
				for (int cx = oldChunkX - this.playerViewRadius; cx <= oldChunkX + this.playerViewRadius; ++cx) {
					for (int cz = oldChunkZ - this.playerViewRadius; cz <= oldChunkZ + this.playerViewRadius; ++cz) {
						if (!this.isOutsideViewRadius(cx - deltaChunkX, cz - deltaChunkZ, playerChunkX, playerChunkZ)) {
							PlayerInstance instance = this.getPlayerInstance(cx - deltaChunkX, cz - deltaChunkZ, false);
							if (instance != null) {
								instance.removePlayer(player);
							}
						}
					}
				}

				player.managedPosX = player.posX;
				player.managedPosZ = player.posZ;
			}
		}
	}

	/** Returns the maximum tracking distance used by entity trackers (viewRadius * 16 - 16). */
	public int getMaxTrackingDistance() {
		return this.playerViewRadius * 16 - 16;
	}

	/** Package-private accessor used by PlayerInstance. */
	static PlayerHash getPlayerInstances(PlayerManager mgr) {
		return mgr.chunkInstances;
	}

	/** Package-private accessor used by PlayerInstance. */
	static List<PlayerInstance> getPlayerInstancesToUpdate(PlayerManager mgr) {
		return mgr.dirtyChunks;
	}
}