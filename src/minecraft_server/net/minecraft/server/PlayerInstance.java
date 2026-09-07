package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet50PreChunk;
import net.minecraft.network.packet.Packet51MapChunk;
import net.minecraft.network.packet.Packet52MultiBlockChange;
import net.minecraft.network.packet.Packet53BlockChange;

/**
 * Represents a chunk visible to a specific player.
 * Tracks which players are within range, buffers block-change
 * notifications, and flushes them to the player list when the
 * buffer is full or on an explicit update tick.
 *
 * Each PlayerInstance manages one chunk column (x, z) and
 * maintains an axis-aligned bounding region of dirty blocks
 * for efficient multi-block-change packets.
 */
class PlayerInstance {

	/** Players currently in this chunk column. */
	private List<EntityPlayerMP> players;

	/** Chunk X coordinate. */
	private int chunkX;

	/** Chunk Z coordinate. */
	private int chunkZ;

	/** The chunk coordinate pair for quick lookup. */
	private ChunkCoordIntPair currentChunk;

	/** Encoded block positions needing an update (packed as short). */
	private short[] blocksToUpdate;

	/** Number of pending block updates in the buffer. */
	private int numBlocksToUpdate;

	/** Bounding-box min/max for the dirty region. */
	private int minX, maxX, minY, maxY, minZ, maxZ;

	/** Owning PlayerManager for access to server instance. */
	final PlayerManager playerManager;

	/**
	 * Creates a new chunk instance and prepares the chunk in the server's chunk provider.
	 *
	 * @param playerManager1  the owning PlayerManager
	 * @param i2              chunk X
	 * @param i3              chunk Z
	 */
	public PlayerInstance(PlayerManager playerManager1, int i2, int i3) {
		this.playerManager = playerManager1;
		this.players = new ArrayList<EntityPlayerMP>();
		this.blocksToUpdate = new short[64];
		this.numBlocksToUpdate = 0;
		this.chunkX = i2;
		this.chunkZ = i3;
		this.currentChunk = new ChunkCoordIntPair(i2, i3);
		playerManager1.getMinecraftServer().chunkProviderServer.prepareChunk(i2, i3);
	}

	/**
	 * Adds a player to this chunk's tracking list and sends a
	 * Packet50PreChunk to load the chunk on the client.
	 */
	public void addPlayer(EntityPlayerMP entityPlayerMP1) {
		if (this.players.contains(entityPlayerMP1)) {
			throw new IllegalStateException("Failed to add player. " + entityPlayerMP1 + " already is in chunk " + this.chunkX + ", " + this.chunkZ);
		} else {
			entityPlayerMP1.listeningChunks.add(this.currentChunk);
			entityPlayerMP1.playerNetServerHandler.sendPacket(new Packet50PreChunk(this.currentChunk.chunkXPos, this.currentChunk.chunkZPos, true));
			this.players.add(entityPlayerMP1);
			entityPlayerMP1.loadedChunks.add(this.currentChunk);
		}
	}

	/** Removes a player; if no players remain, drops the chunk from the server. */
	public void removePlayer(EntityPlayerMP entityPlayerMP1) {
		if (this.players.contains(entityPlayerMP1)) {
			this.players.remove(entityPlayerMP1);
			if (this.players.size() == 0) {
				long key = (long) this.chunkX + 2147483647L | ((long) this.chunkZ + 2147483647L) << 32;
				PlayerManager.getPlayerInstances(this.playerManager).remove(key);
				if (this.numBlocksToUpdate > 0) {
					PlayerManager.getPlayerInstancesToUpdate(this.playerManager).remove(this);
				}
				this.playerManager.getMinecraftServer().chunkProviderServer.dropChunk(this.chunkX, this.chunkZ);
			}
			entityPlayerMP1.loadedChunks.remove(this.currentChunk);
			if (entityPlayerMP1.listeningChunks.contains(this.currentChunk)) {
				entityPlayerMP1.playerNetServerHandler.sendPacket(new Packet50PreChunk(this.chunkX, this.chunkZ, false));
			}
		}
	}

	/** Marks a block as needing a network update; updates the bounding box of dirty blocks. */
	public void markBlockNeedsUpdate(int x, int y, int z) {
		if (this.numBlocksToUpdate == 0) {
			PlayerManager.getPlayerInstancesToUpdate(this.playerManager).add(this);
			this.minX = this.maxX = x;
			this.minY = this.maxY = y;
			this.minZ = this.maxZ = z;
		}
		if (this.minX > x) this.minX = x;
		if (this.maxX < x) this.maxX = x;
		if (this.minY > y) this.minY = y;
		if (this.maxY < y) this.maxY = y;
		if (this.minZ > z) this.minZ = z;
		if (this.maxZ < z) this.maxZ = z;
		if (this.numBlocksToUpdate < 64) {
			short encodedPos = (short) (x << 12 | z << 8 | y);
			for (int i = 0; i < this.numBlocksToUpdate; ++i) {
				if (this.blocksToUpdate[i] == encodedPos) return;
			}
			this.blocksToUpdate[this.numBlocksToUpdate++] = encodedPos;
		}
	}

	/** Sends a packet to all players in this chunk who are listening. */
	public void sendPacketToPlayersInInstance(Packet packet1) {
		for (int i2 = 0; i2 < this.players.size(); ++i2) {
			EntityPlayerMP player3 = (EntityPlayerMP) this.players.get(i2);
			if (player3.listeningChunks.contains(this.currentChunk) && !player3.loadedChunks.contains(this.currentChunk)) {
				player3.playerNetServerHandler.sendPacket(packet1);
			}
		}
	}

	/** Flushes buffered block changes to all watching players. */
	public void onUpdate() {
		WorldServer worldServer = this.playerManager.getMinecraftServer();
		if (this.numBlocksToUpdate != 0) {
			int x, y, z;
			if (this.numBlocksToUpdate == 1) {
				x = this.chunkX * 16 + this.minX;
				y = this.minY;
				z = this.chunkZ * 16 + this.minZ;
				this.sendPacketToPlayersInInstance(new Packet53BlockChange(x, y, z, worldServer));
				if (Block.isBlockContainer[worldServer.getBlockId(x, y, z)]) {
					this.updateTileEntity(worldServer.getBlockTileEntity(x, y, z));
				}
			} else {
				if (this.numBlocksToUpdate == 64) {
					this.minY = this.minY / 2 * 2;
					this.maxY = (this.maxY / 2 + 1) * 2;
					x = this.minX + this.chunkX * 16;
					y = this.minY;
					z = this.minZ + this.chunkZ * 16;
					int xSize = this.maxX - this.minX + 1;
					int ySize = this.maxY - this.minY + 2;
					int zSize = this.maxZ - this.minZ + 1;
					this.sendPacketToPlayersInInstance(new Packet51MapChunk(x, y, z, xSize, ySize, zSize, worldServer));
					List<TileEntity> tileEntities = worldServer.getTileEntityList(x, y, z, x + xSize, y + ySize, z + zSize);
					for (int i = 0; i < tileEntities.size(); ++i) {
						this.updateTileEntity((TileEntity) tileEntities.get(i));
					}
				} else {
					this.sendPacketToPlayersInInstance(new Packet52MultiBlockChange(this.chunkX, this.chunkZ, this.blocksToUpdate, this.numBlocksToUpdate, worldServer));
					for (int i = 0; i < this.numBlocksToUpdate; ++i) {
						x = this.chunkX * 16 + (this.blocksToUpdate[i] >> 12 & 15);
						y = this.blocksToUpdate[i] & 255;
						z = this.chunkZ * 16 + (this.blocksToUpdate[i] >> 8 & 15);
						if (Block.isBlockContainer[worldServer.getBlockId(x, y, z)]) {
							this.updateTileEntity(worldServer.getBlockTileEntity(x, y, z));
						}
					}
				}
			}
			this.numBlocksToUpdate = 0;
		}
	}

	/** Sends a tile entity description packet if the tile entity is present. */
	private void updateTileEntity(TileEntity tileEntity1) {
		if (tileEntity1 != null) {
			Packet packet2 = tileEntity1.getDescriptionPacket();
			if (packet2 != null) {
				this.sendPacketToPlayersInInstance(packet2);
			}
		}
	}
}