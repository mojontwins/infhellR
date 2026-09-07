package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.network.NetHandler;

/**
 * Packet 52: Multi-Block Change (original/vanilla encoding).
 * <p>
 * The original vanilla protocol used a different encoding from
 * {@link Packet52MultiBlockChange}: three separate parallel arrays (coordinates,
 * block types, and metadata) are transmitted in-band rather than interleaving
 * them. This is the original variant retained for compatibility.
 * <p>
 * Coordinate encoding (per short): bits 12-15 = local X, bits 8-11 = local Z,
 * bits 0-7 = local Y.
 *
 * @see Packet52MultiBlockChange
 */
public class Packet52MultiBlockChangeOrig extends Packet {
	/** Chunk X coordinate (in chunk-space). */
	public int xPosition;

	/** Chunk Z coordinate (in chunk-space). */
	public int zPosition;

	/** Parallel array of XZY-encoded coordinate shorts. */
	public short[] coordinateArray;

	/** Parallel array of block type ids. */
	public byte[] typeArray;

	/** Parallel array of block metadata values. */
	public byte[] metadataArray;

	/** Number of blocks being updated. */
	public int size;

	public Packet52MultiBlockChangeOrig() {
		this.isChunkDataPacket = true;
	}

	/**
	 * Constructs a multi-block-change packet for the given chunk.
	 *
	 * @param xPos    chunk X coordinate
	 * @param zPos    chunk Z coordinate
	 * @param s       array of XZY-encoded coordinate shorts
	 * @param size    number of entries to process
	 * @param world   the world to read block state from
	 */
	public Packet52MultiBlockChangeOrig(int xPos, int zPos, short[] s, int size, World world) {
		this.isChunkDataPacket = true;
		this.xPosition = xPos;
		this.zPosition = zPos;
		this.size = size;

		this.coordinateArray = new short[size];
		this.typeArray = new byte[size];
		this.metadataArray = new byte[size];

		Chunk chunk = world.getChunkFromChunkCoords(xPos, zPos);

		for (int i = 0; i < size; ++i) {
			// Decode the XZY coordinate.
			int localX = s[i] >> 12 & 15;
			int localZ = s[i] >> 8 & 15;
			int localY = s[i] & 255;

			this.coordinateArray[i] = s[i];
			this.typeArray[i] = (byte)chunk.getBlockID(localX, localY, localZ);
			this.metadataArray[i] = (byte)chunk.getBlockMetadata(localX, localY, localZ);
		}
	}

	/**
	 * Reads the chunk coordinates, block count, and the three parallel arrays.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.xPosition = dataInputStream.readInt();
		this.zPosition = dataInputStream.readInt();
		this.size = dataInputStream.readShort() & 65535;

		this.coordinateArray = new short[this.size];
		this.typeArray = new byte[this.size];
		this.metadataArray = new byte[this.size];

		// Read the coordinate shorts.
		for (int i = 0; i < this.size; ++i) {
			this.coordinateArray[i] = dataInputStream.readShort();
		}

		// Read the type and metadata as raw byte arrays.
		dataInputStream.readFully(this.typeArray);
		dataInputStream.readFully(this.metadataArray);
	}

	/**
	 * Writes the chunk coordinates, block count, and the three parallel arrays.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.xPosition);
		dataOutputStream.writeInt(this.zPosition);
		dataOutputStream.writeShort((short)this.size);

		for (int i = 0; i < this.size; ++i) {
			dataOutputStream.writeShort(this.coordinateArray[i]);
		}

		dataOutputStream.write(this.typeArray);
		dataOutputStream.write(this.metadataArray);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleMultiBlockChange}.
	 * Note: the body is currently empty, meaning this variant is not processed
	 * on the server-side NetHandler (the {@link Packet52MultiBlockChange} variant
	 * is used instead).
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		// netHandler1.handleMultiBlockChange(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 + 4 + 2 + size * (2 + 1 + 1) = 10 + size * 4)
	 */
	@Override
	public int getPacketSize() {
		return 10 + this.size * 4;
	}
}
