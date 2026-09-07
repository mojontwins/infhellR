package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 50: Pre-Chunk (also known as "Chunk Unload/Load announcement").
 * <p>
 * Sent by the server to inform the client whether it should initialize or
 * discard a chunk column at the given chunk coordinates <em>before</em> the
 * actual chunk data arrives. The {@code mode} flag controls load
 * ({@code true}) versus unload ({@code false}). This packet is marked
 * {@code isChunkDataPacket = false} because it is metadata about a chunk,
 * not chunk payload data.
 */
public class Packet50PreChunk extends Packet {
	/** Chunk X coordinate (in chunk-space, not block-space). */
	public int xPosition;

	/** Chunk Z coordinate (in chunk-space, not block-space). */
	public int yPosition;

	/** {@code true} to load/initialize the chunk, {@code false} to unload it. */
	public boolean mode;

	public Packet50PreChunk() {
		this.isChunkDataPacket = false;
	}

	/**
	 * Constructs a pre-chunk packet for the given chunk column.
	 *
	 * @param xPos the chunk X coordinate
	 * @param zPos the chunk Z coordinate
	 * @param mode {@code true} to load, {@code false} to unload
	 */
	public Packet50PreChunk(int xPos, int zPos, boolean mode) {
		this.isChunkDataPacket = false;
		this.xPosition = xPos;
		this.yPosition = zPos;
		this.mode = mode;
	}

	/**
	 * Reads the chunk coordinates and load/unload flag from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.xPosition = dataInputStream.readInt();
		this.yPosition = dataInputStream.readInt();
		this.mode = dataInputStream.read() != 0;
	}

	/**
	 * Writes the chunk coordinates and load/unload flag to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.xPosition);
		dataOutputStream.writeInt(this.yPosition);
		dataOutputStream.write(this.mode ? 1 : 0);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handlePreChunk}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handlePreChunk(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 + 4 + 1 = 9 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 9;
	}
}
