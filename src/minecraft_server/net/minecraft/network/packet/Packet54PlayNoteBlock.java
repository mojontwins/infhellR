package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 54: Play Note Block.
 * <p>
 * Sent by the server to the client when a note block (block id 25) should
 * play a note. The pitch value encodes both the instrument type (harp,
 * double-bass, snare, etc.) and the actual note pitch within the instrument's
 * range. The client triggers the appropriate sound effect and particle effect
 * at the given block position.
 */
public class Packet54PlayNoteBlock extends Packet {
	/** X coordinate of the note block. */
	public int xLocation;

	/** Y coordinate of the note block (0-255). */
	public int yLocation;

	/** Z coordinate of the note block. */
	public int zLocation;

	/**
	 * Instrument type encoded in the upper bits, note pitch in the lower bits.
	 * The server typically constructs this as a single byte value.
	 */
	public int instrumentType;

	/**
	 * The note pitch value (0-24 in vanilla, depending on the instrument).
	 * Together with {@link #instrumentType} it determines the final pitch.
	 */
	public int pitch;

	public Packet54PlayNoteBlock() {
	}

	/**
	 * Constructs a note-block packet from raw values.
	 *
	 * @param x             block X coordinate
	 * @param y             block Y coordinate
	 * @param z             block Z coordinate
	 * @param instrumentType the instrument type byte
	 * @param pitch         the note pitch byte
	 */
	public Packet54PlayNoteBlock(int x, int y, int z, int instrumentType, int pitch) {
		this.xLocation = x;
		this.yLocation = y;
		this.zLocation = z;
		this.instrumentType = instrumentType;
		this.pitch = pitch;
	}

	/**
	 * Reads the block coordinates, instrument type, and pitch from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.xLocation = dataInputStream.readInt();
		this.yLocation = dataInputStream.readShort();
		this.zLocation = dataInputStream.readInt();
		this.instrumentType = dataInputStream.read();
		this.pitch = dataInputStream.read();
	}

	/**
	 * Writes the block coordinates, instrument type, and pitch to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.xLocation);
		dataOutputStream.writeShort(this.yLocation);
		dataOutputStream.writeInt(this.zLocation);
		dataOutputStream.write(this.instrumentType);
		dataOutputStream.write(this.pitch);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleNotePlay}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleNotePlay(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (12 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 12;
	}
}
