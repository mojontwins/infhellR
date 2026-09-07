package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 94: Freeze Level.
 * <p>
 * Custom InfHell packet used to inform the client about the current "freeze"
 * level of the simulation, typically used to control client-side rendering of
 * snow/ice accumulation or seasonal transitions.
 */
public class Packet94FreezeLevel extends Packet {
	/**
	 * The current freeze level (0 = no freeze, higher = more frozen).
	 * Defaults to 0 to match a freshly created packet.
	 */
	public int freezeLevel = 0;

	public Packet94FreezeLevel() {
	}

	/**
	 * Constructs a freeze-level packet with the given level value.
	 *
	 * @param freezeLevel the freeze level to broadcast
	 */
	public Packet94FreezeLevel(int freezeLevel) {
		this.freezeLevel = freezeLevel;
	}

	/**
	 * Reads the freeze level from the stream as a single 4-byte int.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.freezeLevel = dataInputStream.readInt();
	}

	/**
	 * Writes the freeze level to the stream as a single 4-byte int.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.freezeLevel);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleFreezeLevel}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleFreezeLevel(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (4 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 4;
	}
}
