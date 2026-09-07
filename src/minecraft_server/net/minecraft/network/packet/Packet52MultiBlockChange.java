package net.minecraft.network.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.network.NetHandler;

/**
 * Packet 52: Multi-Block Change (custom encoding variant).
 * <p>
 * Informs the client that multiple blocks in a single chunk have changed
 * state simultaneously. Unlike the vanilla variant ({@link Packet52MultiBlockChangeOrig}),
 * this version encodes each block change using exactly 4 bytes: a 2-byte
 * XZY coordinate and a 2-byte block-type+metadata pair. This results in
 * slightly larger payloads than the original, but is simpler to decode on the
 * client side because all four bytes are fixed-size.
 * <p>
 * This packet is marked as a chunk-data packet.
 *
 * @see Packet52MultiBlockChangeOrig
 * @see Packet53BlockChange
 */
public class Packet52MultiBlockChange extends Packet {
	/** Chunk X coordinate (in chunk-space, not block-space). */
	public int xPosition;

	/** Chunk Z coordinate (in chunk-space, not block-space). */
	public int zPosition;

	/**
	 * Encoded block change data. Each block occupies exactly 4 bytes:
	 * 2 bytes for the XZY coordinate and 2 bytes for (blockId &lt;&lt; 8) | metadata.
	 */
	public byte[] encodedBlocks;

	/** Number of blocks being updated in this packet. */
	public int size;

	public Packet52MultiBlockChange() {
		this.isChunkDataPacket = true;
	}

	/**
	 * Constructs a multi-block-change packet for the given chunk and list of
	 * affected block coordinates.
	 *
	 * @param xPos             chunk X coordinate
	 * @param zPos             chunk Z coordinate
	 * @param encodedData      array of encoded XZY coordinates (one short per block)
	 * @param numBlocksToUpdate number of entries in {@code encodedData} to send
	 * @param world            the world to read current block state from
	 */
	public Packet52MultiBlockChange(int xPos, int zPos, short[] encodedData, int numBlocksToUpdate, World world) {
		this.isChunkDataPacket = true;
		this.xPosition = xPos;
		this.zPosition = zPos;
		this.size = numBlocksToUpdate;

		// 4 bytes per block: 2-byte coordinate + 2-byte type/meta.
		int byteSize = 4 * numBlocksToUpdate;

		Chunk chunk = world.getChunkFromChunkCoords(xPos, zPos);

		try {
			if (numBlocksToUpdate >= 64) {
				// Sanity check: a single chunk column should never produce this many
				// block changes in one packet. Log and skip encoding.
				System.out.println("Something wrong - ChunkTilesUpdatePacket compress " + numBlocksToUpdate);
			} else {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byteSize);
				DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

				for (int i = 0; i < numBlocksToUpdate; ++i) {
					// Decode the compact XZY coordinate from the encoded short.
					// Bits 12-15: local X (0-15), Bits 8-11: local Z (0-15),
					// Bits 0-7:   local Y (0-255).
					int x = encodedData[i] >> 12 & 15;
					int z = encodedData[i] >> 8 & 15;
					int y = encodedData[i] & 255;

					// Write the raw coordinate short (still encoded as XZZY).
					outputStream.writeShort(encodedData[i]);

					// Encode block id (upper 8 bits) and metadata (lower 8 bits).
					outputStream.writeShort(
							(short)((chunk.getBlockID(x, y, z) & 255) << 8 | chunk.getBlockMetadata(x, y, z) & 255));
				}

				this.encodedBlocks = byteArrayOutputStream.toByteArray();

				// Sanity check: ensure the buffer size matches our expectation.
				if (this.encodedBlocks.length != byteSize) {
					throw new RuntimeException(
							"Expected length " + byteSize + " doesn't match received length " + this.encodedBlocks.length);
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			this.encodedBlocks = null;
		}
	}

	/**
	 * Reads the chunk coordinates, block count, and the encoded block data.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.xPosition = dataInputStream.readInt();
		this.zPosition = dataInputStream.readInt();
		this.size = dataInputStream.readShort() & 65535;

		int encodedDataLength = dataInputStream.readInt();
		if (encodedDataLength > 0) {
			this.encodedBlocks = new byte[encodedDataLength];
			dataInputStream.readFully(this.encodedBlocks);
		}
	}

	/**
	 * Writes the chunk coordinates, block count, and the encoded block data.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.xPosition);
		dataOutputStream.writeInt(this.zPosition);
		dataOutputStream.writeShort((short)this.size);

		if (this.encodedBlocks != null) {
			dataOutputStream.writeInt(this.encodedBlocks.length);
			dataOutputStream.write(this.encodedBlocks);
		} else {
			dataOutputStream.writeInt(0);
		}
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleMultiBlockChange}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleMultiBlockChange(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 + 4 + 2 + 4 for the length prefix + 4 * {@link #size} for data)
	 */
	@Override
	public int getPacketSize() {
		return 10 + this.size * 4;
	}
}
