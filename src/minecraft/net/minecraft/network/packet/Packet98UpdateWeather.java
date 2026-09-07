package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 98: Update Weather.
 * <p>
 * Custom InfHell packet that broadcasts a weather state change to the client.
 * Carries the three booleans (raining, snowing, thundering) directly. This
 * is the preferred modern packet for weather state synchronization; the older
 * {@link Packet70Bed} also carries weather state but is a more general
 * sync packet.
 */
public class Packet98UpdateWeather extends Packet {
	/** Whether it should be raining. */
	public boolean raining;

	/** Whether it should be snowing. */
	public boolean snowing;

	/** Whether there should be a thunderstorm. */
	public boolean thundering;

	public Packet98UpdateWeather() {
	}

	/**
	 * Constructs an update-weather packet.
	 *
	 * @param raining   whether it should be raining
	 * @param snowing   whether it should be snowing
	 * @param thundering whether there should be a thunderstorm
	 */
	public Packet98UpdateWeather(boolean raining, boolean snowing, boolean thundering) {
		this.raining = raining;
		this.snowing = snowing;
		this.thundering = thundering;
	}

	/**
	 * Reads the three weather booleans from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.raining = (dataInputStream.read() == 1);
		this.snowing = (dataInputStream.read() == 1);
		this.thundering = (dataInputStream.read() == 1);
	}

	/**
	 * Writes the three weather booleans to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.write(this.raining ? 1 : 0);
		dataOutputStream.write(this.snowing ? 1 : 0);
		dataOutputStream.write(this.thundering ? 1 : 0);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleUpdateWeather}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleUpdateWeather(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (3 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 3;
	}
}
