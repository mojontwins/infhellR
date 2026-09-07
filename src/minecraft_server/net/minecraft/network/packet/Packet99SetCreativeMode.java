package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 99: Set Creative Mode.
 * <p>
 * Custom InfHell packet used to inform the client whether the local player
 * is in creative mode. The server toggles this when the player crosses the
 * survival/creative boundary; the client enables or disables creative-only
 * behavior (infinite blocks, flight, no-clip) accordingly.
 */
public class Packet99SetCreativeMode extends Packet {
	/**
	 * {@code true} if the player should be in creative mode, {@code false} for survival.
	 */
	public boolean isCreative;

	public Packet99SetCreativeMode() {
	}

	/**
	 * Constructs a set-creative-mode packet.
	 *
	 * @param isCreative {@code true} for creative mode, {@code false} for survival
	 */
	public Packet99SetCreativeMode(boolean isCreative) {
		this.isCreative = isCreative;
	}

	/**
	 * Reads the creative-mode flag as a single byte from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.isCreative = (dataInputStream.read() == 1);
	}

	/**
	 * Writes the creative-mode flag as a single byte to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(this.isCreative ? 1 : 0);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleSetCreative}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleSetCreative(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (1 byte)
	 */
	@Override
	public int getPacketSize() {
		return 1;
	}
}
