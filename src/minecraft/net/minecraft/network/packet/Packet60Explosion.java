package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.game.world.ChunkPosition;
import net.minecraft.network.NetHandler;

/**
 * Packet 60: Explosion.
 * <p>
 * Sent by the server to trigger the explosion visual effect on the client. The
 * packet includes the explosion origin, the explosion radius/size, and a list
 * of all block positions that were destroyed by the explosion. The client uses
 * this data to render the explosion particles, the screen shake, and to update
 * the world by removing the destroyed blocks.
 * <p>
 * Block positions are stored as relative byte offsets from the explosion origin
 * to save bandwidth (each block within a {@code +/-127} range in each axis).
 */
public class Packet60Explosion extends Packet {
	/** X coordinate of the explosion center. */
	public double explosionX;

	/** Y coordinate of the explosion center. */
	public double explosionY;

	/** Z coordinate of the explosion center. */
	public double explosionZ;

	/** Explosion radius used for rendering particle spread. */
	public float explosionSize;

	/**
	 * Set of block positions destroyed by this explosion, stored as
	 * {@link ChunkPosition} objects. Note: this is a separate set from the
	 * particle-destruction list, as some blocks may be destroyed without
	 * appearing in this set in certain server configurations.
	 */
	public Set<ChunkPosition> destroyedBlockPositions;

	/**
	 * The primary block type of the explosion source. Used by the client to
	 * determine which explosion texture to display (e.g. tnt vs. creeper).
	 */
	public int blockID;

	public Packet60Explosion() {
	}

	/**
	 * Full constructor for the explosion packet.
	 *
	 * @param x                     explosion X coordinate
	 * @param y                     explosion Y coordinate
	 * @param z                     explosion Z coordinate
	 * @param size                  explosion radius
	 * @param destroyedBlockPositions list of block positions destroyed
	 * @param blockID              primary block type of the explosion source
	 */
	public Packet60Explosion(double x, double y, double z, float size,
			Set<ChunkPosition> destroyedBlockPositions, int blockID) {
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
		this.explosionSize = size;
		this.destroyedBlockPositions = new HashSet<ChunkPosition>(destroyedBlockPositions);
		this.blockID = blockID;
	}

	/**
	 * Reads the explosion parameters and the list of destroyed block offsets.
	 * <p>
	 * The position of each destroyed block is encoded as three signed bytes
	 * relative to {@code (explosionX, explosionY, explosionZ)}.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.explosionX = dataInputStream.readDouble();
		this.explosionY = dataInputStream.readDouble();
		this.explosionZ = dataInputStream.readDouble();
		this.explosionSize = dataInputStream.readFloat();
		this.blockID = dataInputStream.readByte();

		int numDestroyedBlocks = dataInputStream.readInt();
		this.destroyedBlockPositions = new HashSet<ChunkPosition>();

		// Cache the integer parts of the explosion origin as the base for relative offsets.
		int baseX = (int)this.explosionX;
		int baseY = (int)this.explosionY;
		int baseZ = (int)this.explosionZ;

		for (int i = 0; i < numDestroyedBlocks; ++i) {
			// Read the signed byte offsets and add them to the base coordinates.
			int blockX = dataInputStream.readByte() + baseX;
			int blockY = dataInputStream.readByte() + baseY;
			int blockZ = dataInputStream.readByte() + baseZ;
			this.destroyedBlockPositions.add(new ChunkPosition(blockX, blockY, blockZ));
		}
	}

	/**
	 * Writes the explosion parameters and the list of destroyed block offsets.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeDouble(this.explosionX);
		dataOutputStream.writeDouble(this.explosionY);
		dataOutputStream.writeDouble(this.explosionZ);
		dataOutputStream.writeFloat(this.explosionSize);
		dataOutputStream.writeByte(this.blockID);

		dataOutputStream.writeInt(this.destroyedBlockPositions.size());

		// Use the integer parts of the explosion center as the relative base.
		int baseX = (int)this.explosionX;
		int baseY = (int)this.explosionY;
		int baseZ = (int)this.explosionZ;

		Iterator<ChunkPosition> iterator = this.destroyedBlockPositions.iterator();
		while (iterator.hasNext()) {
			ChunkPosition pos = iterator.next();
			// Encode each block position as a signed byte offset from the base.
			dataOutputStream.writeByte(pos.x - baseX);
			dataOutputStream.writeByte(pos.y - baseY);
			dataOutputStream.writeByte(pos.z - baseZ);
		}
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleExplosion}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleExplosion(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (8*3 doubles + 4 float + 1 byte + 4 int + numBlocks*3)
	 */
	@Override
	public int getPacketSize() {
		return 32 + this.destroyedBlockPositions.size() * 3 + 1;
	}
}
