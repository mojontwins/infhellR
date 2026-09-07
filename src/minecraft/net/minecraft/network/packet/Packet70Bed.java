package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 70: Bed / Weather Sync.
 * <p>
 * Despite the class name, this packet is used in InfHell to synchronize
 * weather state between the server and the client. The {@code setRainingAction}
 * field doubles as a "set" flag (1) versus a "begin/end" toggle, while the
 * {@code raining}, {@code snowing}, and {@code thundering} booleans carry the
 * actual weather state. The {@link #errorMessageArr} table contains the
 * localized "tile.bed.notValid" message used in the legacy bed-related code
 * path that shares this packet.
 */
public class Packet70Bed extends Packet {
	/**
	 * Localized messages for the legacy bed error path. Index 0 is the
	 * translation key for "Not a valid bed"; the other slots are reserved.
	 */
	public static final String[] errorMessageArr = new String[]{"tile.bed.notValid", null, null};

	/** Action code: 1 = set the weather state to the provided booleans. */
	public int setRainingAction;

	/** Whether it should be raining. */
	public boolean raining;

	/** Whether it should be snowing. */
	public boolean snowing;

	/** Whether there should be a thunderstorm. */
	public boolean thundering;

	public Packet70Bed() {
	}

	/**
	 * Constructs a weather-sync packet. The action is implicitly set to 1.
	 *
	 * @param raining   whether it should be raining
	 * @param snowing   whether it should be snowing
	 * @param thundering whether there should be a thunderstorm
	 */
	public Packet70Bed(boolean raining, boolean snowing, boolean thundering) {
		this.raining = raining;
		this.snowing = snowing;
		this.thundering = thundering;
		this.setRainingAction = 1;
	}

	/**
	 * Constructs a packet with only an action code. The boolean fields are
	 * left at their defaults and should be filled in later or read from the
	 * wire.
	 *
	 * @param action the action code
	 */
	public Packet70Bed(int action) {
		this.setRainingAction = action;
	}

	/**
	 * Reads the action byte and the three weather booleans from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.setRainingAction = dataInputStream.readByte();
		this.raining = (dataInputStream.read() == 1);
		this.snowing = (dataInputStream.read() == 1);
		this.thundering = (dataInputStream.read() == 1);
	}

	/**
	 * Writes the action byte and the three weather booleans to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeByte(this.setRainingAction);
		dataOutputStream.write(this.raining ? 1 : 0);
		dataOutputStream.write(this.snowing ? 1 : 0);
		dataOutputStream.write(this.thundering ? 1 : 0);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleBed}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleBed(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (4 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 4;
	}
}
