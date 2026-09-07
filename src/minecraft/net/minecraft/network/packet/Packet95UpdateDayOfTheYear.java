package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 95: Update Day Of The Year.
 * <p>
 * Custom InfHell packet used to synchronize the "day of the year" counter
 * between the server and the client. The client uses this value to drive
 * seasonal effects (textures, weather, foliage color, etc.). The value is
 * stored as a single signed byte (range -128 to 127) to match the seasonal
 * cycle granularity.
 */
public class Packet95UpdateDayOfTheYear extends Packet {
	/**
	 * The current day-of-year value (modulo 128). Defaults to 0.
	 */
	public int dayOfTheYear = 0;

	public Packet95UpdateDayOfTheYear() {
	}

	/**
	 * Constructs a day-of-year update packet.
	 *
	 * @param dayOfTheYear the new day-of-year value
	 */
	public Packet95UpdateDayOfTheYear(int dayOfTheYear) {
		this.dayOfTheYear = dayOfTheYear;
	}

	/**
	 * Reads the day-of-year value as a single signed byte from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.dayOfTheYear = dataInputStream.readByte();
	}

	/**
	 * Writes the day-of-year value as a single signed byte to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeByte(this.dayOfTheYear);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleUpdateDayOfTheYear}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleUpdateDayOfTheYear(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire (1 byte)
	 */
	@Override
	public int getPacketSize() {
		return 1;
	}
}
