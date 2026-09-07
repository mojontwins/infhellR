package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 96: Bad Moon Decide.
 * <p>
 * Custom InfHell packet used to broadcast the result of a "bad moon" event
 * decision. The {@code badMoonDecide} flag, when {@code true}, triggers the
 * special "bad moon" event on the client (typically increased mob spawns or
 * special weather). When {@code false}, the event is cancelled.
 */
public class Packet96BadMoonDecide extends Packet {
	/**
	 * The bad-moon decision: {@code true} = bad moon event, {@code false} = no event.
	 * Defaults to {@code false}.
	 */
	public boolean badMoonDecide = false;

	public Packet96BadMoonDecide() {
	}

	/**
	 * Constructs a bad-moon-decide packet.
	 *
	 * @param badMoonDecide the decision to broadcast
	 */
	public Packet96BadMoonDecide(boolean badMoonDecide) {
		this.badMoonDecide = badMoonDecide;
	}

	/**
	 * Reads the bad-moon-decide flag as a single byte from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.badMoonDecide = (dataInputStream.read() == 1);
	}

	/**
	 * Writes the bad-moon-decide flag as a single byte to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(this.badMoonDecide ? 1 : 0);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleBadMoonDecide}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleBadMoonDecide(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (1 byte)
	 */
	@Override
	public int getPacketSize() {
		return 1;
	}
}
