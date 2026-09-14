package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.minecraft.game.world.World;
import net.minecraft.network.NetHandler;

/**
 * Packet 51: Map Chunk.
 * <p>
 * Carries the full compressed block data for a single chunk column (up to 16x16x256
 * blocks, i.e. {@code Chunk.SECTION_HEIGHT}) from the server to the client. The packet begins with the
 * three-dimensional origin of the chunk and its dimensions, followed by a
 * Zlib-compressed payload. The compression uses a preset deflater (level -1)
 * for maximum compatibility with the vanilla protocol.
 * <p>
 * The {@link #chunkSize} field tracks how many bytes of compressed data were
 * actually produced, since the deflater output size varies slightly between
 * invocations.
 *
 * @see Packet50PreChunk
 * @see Packet52MultiBlockChange
 * @see Packet53BlockChange
 */
public class Packet51MapChunk extends Packet {
	/** Chunk X coordinate in block-space (multiplied by 16 to get block-space X). */
	public int xPosition;

	/** Y base coordinate (in blocks, e.g. the minimum Y of the chunk). */
	public int yPosition;

	/** Chunk Z coordinate in block-space (multiplied by 16 to get block-space Z). */
	public int zPosition;

	/** Size of the chunk along the X axis in blocks (normally 16). */
	public int xSize;

	/** Size of the chunk along the Y axis in blocks (normally {@code Chunk.SECTION_HEIGHT} = 256). */
	public int ySize;

	/** Size of the chunk along the Z axis in blocks (normally 16). */
	public int zSize;

	/** The decompressed block data payload. */
	public byte[] chunk;

	/** Number of bytes actually written to {@link #chunk} during compression. */
	private int chunkSize;

	public Packet51MapChunk() {
		this.isChunkDataPacket = true;
	}

	/**
	 * Constructs a MapChunk packet by extracting and compressing chunk data from
	 * the live world.
	 *
	 * @param xPos   chunk X origin in block-space
	 * @param yPos   chunk Y origin in block-space
	 * @param zPos   chunk Z origin in block-space
	 * @param xSize  chunk width in blocks
	 * @param ySize  chunk height in blocks
	 * @param zSize  chunk depth in blocks
	 * @param world  the world to read chunk data from
	 */
	public Packet51MapChunk(int xPos, int yPos, int zPos, int xSize, int ySize, int zSize, World world) {
		this.isChunkDataPacket = true;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.zPosition = zPos;
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;

		// Retrieve the raw chunk data; the world may return a pre-compressed
		// buffer that already contains type + metadata + light data per block.
		byte[] allChunkData = world.getChunkData(xPos, yPos, zPos, xSize, ySize, zSize);

		// Level -1 means use the default Zlib preset (compatible with vanilla).
		Deflater deflater = new Deflater(-1);

		try {
			deflater.setInput(allChunkData);
			deflater.finish();

			// Allocate enough space for the worst-case compressed output
			// (3 bytes per block is a safe upper bound).
			this.chunk = new byte[xSize * ySize * zSize * 3];
			this.chunkSize = deflater.deflate(this.chunk);
		} finally {
			deflater.end();
		}
	}

	/**
	 * Reads the chunk metadata and the compressed data payload, then
	 * decompresses it into {@link #chunk}.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails or the data is corrupt
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.xPosition = dataInputStream.readInt();
		this.yPosition = dataInputStream.readShort();
		this.zPosition = dataInputStream.readInt();

		// Chunk sizes are stored as size-1 to save one byte (size of 16 stored as 15).
		this.xSize = dataInputStream.read() + 1;
		this.ySize = dataInputStream.read() + 1;
		this.zSize = dataInputStream.read() + 1;

		this.chunkSize = dataInputStream.readInt();

		// Read all compressed bytes into a temporary buffer before inflating.
		byte[] compressedData = new byte[this.chunkSize];
		dataInputStream.readFully(compressedData);

		// Allocate space for the decompressed output and inflate.
		this.chunk = new byte[this.xSize * this.ySize * this.zSize * 3];
		Inflater inflater = new Inflater();
		inflater.setInput(compressedData);

		try {
			inflater.inflate(this.chunk);
		} catch (DataFormatException e) {
			throw new IOException("Bad compressed data format");
		} finally {
			inflater.end();
		}
	}

	/**
	 * Writes the chunk metadata and compressed data payload to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.xPosition);
		dataOutputStream.writeShort(this.yPosition);
		dataOutputStream.writeInt(this.zPosition);

		// Store sizes as size-1 to save a byte.
		dataOutputStream.write(this.xSize - 1);
		dataOutputStream.write(this.ySize - 1);
		dataOutputStream.write(this.zSize - 1);

		dataOutputStream.writeInt(this.chunkSize);
		dataOutputStream.write(this.chunk, 0, this.chunkSize);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleMapChunk}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleMapChunk(this);
	}

	/**
	 * @return the total number of bytes this packet occupies on the wire
	 *         (17 bytes of header/metadata + the compressed chunk data)
	 */
	@Override
	public int getPacketSize() {
		return 17 + this.chunkSize;
	}
}
