package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 61: Door Change.
 * <p>
 * A custom packet used by InfHell to notify the client about changes to doors.
 * The exact meaning of the fields depends on the context in which the server
 * sends this packet (e.g., door open/close state, door type change, or
 * redstone-coupled door activation). Check the {@link NetHandler#handleDoorChange}
 * implementation for the client-specific interpretation.
 */
public class Packet61DoorChange extends Packet {
	/** Primary X coordinate parameter (meaning depends on context). */
	public int xPosition;

	/** Primary Y coordinate parameter (meaning depends on context). */
	public int yPosition;

	/** Primary Z coordinate parameter (meaning depends on context). */
	public int zPosition;

	/** Additional parameter, often a block metadata value or sub-type. */
	public int metadata;

	/** Auxiliary parameter, often the block type id or a custom flag. */
	public int auxData;

	public Packet61DoorChange() {
	}

	/**
	 * Constructs a door-change packet from raw field values.
	 *
	 * @param x   primary X coordinate
	 * @param y   primary Y coordinate
	 * @param z   primary Z coordinate
	 * @param m   metadata or sub-type value
	 * @param aux auxiliary parameter
	 */
	public Packet61DoorChange(int x, int y, int z, int m, int aux) {
		this.xPosition = x;
		this.yPosition = y;
		this.zPosition = z;
		this.metadata = m;
		this.auxData = aux;
	}

	/**
	 * Reads all five integer fields from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.xPosition = dataInputStream.readInt();
		this.yPosition = dataInputStream.readInt();
		this.zPosition = dataInputStream.readByte();
		this.metadata = dataInputStream.readInt();
		this.auxData = dataInputStream.readInt();
	}

	/**
	 * Writes all five fields to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.xPosition);
		dataOutputStream.writeInt(this.yPosition);
		dataOutputStream.writeByte(this.zPosition);
		dataOutputStream.writeInt(this.metadata);
		dataOutputStream.writeInt(this.auxData);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleDoorChange}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleDoorChange(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (20 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 20;
	}
}
