package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.world.World;
import net.minecraft.network.NetHandler;

/**
 * Packet 53: Block Change.
 * <p>
 * Informs the client that a single block in the world has changed. The packet
 * contains the absolute block coordinates, the new block type, and the new
 * block metadata. This is the wire-equivalent of calling
 * {@code World.setBlockIdWithMetadata()} for a single block.
 */
public class Packet53BlockChange extends Packet {
	/** Block X coordinate in world-space. */
	public int xPosition;

	/** Block Y coordinate in world-space (0-255). */
	public int yPosition;

	/** Block Z coordinate in world-space. */
	public int zPosition;

	/** The new block type id for this position. */
	public int type;

	/** The new block metadata value for this position. */
	public int metadata;

	public Packet53BlockChange() {
		this.isChunkDataPacket = true;
	}

	/**
	 * Convenience constructor that looks up the current block state from the
	 * world and fills the packet fields accordingly.
	 *
	 * @param x    block X coordinate
	 * @param y    block Y coordinate
	 * @param z    block Z coordinate
	 * @param world the world to query
	 */
	public Packet53BlockChange(int x, int y, int z, World world) {
		this.isChunkDataPacket = true;
		this.xPosition = x;
		this.yPosition = y;
		this.zPosition = z;
		this.type = world.getBlockId(x, y, z);
		this.metadata = world.getBlockMetadata(x, y, z);
	}

	/**
	 * Reads the block coordinates, type, and metadata from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.xPosition = dataInputStream.readInt();
		this.yPosition = dataInputStream.read();
		this.zPosition = dataInputStream.readInt();
		this.type = dataInputStream.read();
		this.metadata = dataInputStream.read();
	}

	/**
	 * Writes the block coordinates, type, and metadata to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.xPosition);
		dataOutputStream.write(this.yPosition);
		dataOutputStream.writeInt(this.zPosition);
		dataOutputStream.write(this.type);
		dataOutputStream.write(this.metadata);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleBlockChange}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleBlockChange(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (11 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 11;
	}
}
