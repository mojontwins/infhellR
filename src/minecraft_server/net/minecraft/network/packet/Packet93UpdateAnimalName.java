package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 93: Update Animal Name.
 * <p>
 * Sent by the server to update the displayed custom name of a tameable
 * animal (e.g. wolf, ocelot, horse-like mount). The packet carries the
 * entity id and a UTF-16 string with the new name. The {@code isChunkDataPacket}
 * flag is set so this packet is queued behind other chunk-data packets
 * during the initial chunk load, preventing name flicker.
 */
public class Packet93UpdateAnimalName extends Packet {
	/** The id of the entity whose name should be updated. */
	public int entityId;

	/** The new name to display above the entity. */
	public String name;

	public Packet93UpdateAnimalName() {
		this.isChunkDataPacket = true;
	}

	/**
	 * Constructs a name-update packet.
	 *
	 * @param entityId the id of the entity
	 * @param name     the new name (max 32 characters when read from the wire)
	 */
	public Packet93UpdateAnimalName(int entityId, String name) {
		this.isChunkDataPacket = true;
		this.entityId = entityId;
		this.name = name;
	}

	/**
	 * Reads the entity id and the UTF-16 string from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityId = dataInputStream.readInt();
		this.name = readString(dataInputStream, 32);
	}

	/**
	 * Writes the entity id and the UTF-16 string to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityId);
		writeString(this.name, dataOutputStream);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleUpdateAnimalName}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleUpdateAnimalName(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 for the entity id + 2 for the string length + the string itself)
	 */
	@Override
	public int getPacketSize() {
		return 4 + 2 + this.name.length();
	}
}
