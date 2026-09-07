package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 38: Entity Status.
 * <p>
 * Broadcast a single-byte status code for an entity. Status codes are used to
 * trigger client-side visual effects such as entity hurt animations, death,
 * respawn, tamable-taming hearts, sheep eating grass, and so on. The exact
 * meaning of each code is defined in the entity's client-side handler.
 */
public class Packet38EntityStatus extends Packet {
	/** The id of the entity the status applies to. */
	public int entityId;

	/** The status code. Interpretation depends on the entity type. */
	public byte entityStatus;

	public Packet38EntityStatus() {
	}

	/**
	 * Constructs a status packet for the given entity.
	 *
	 * @param entityId     the id of the entity
	 * @param entityStatus the status code to send
	 */
	public Packet38EntityStatus(int entityId, byte entityStatus) {
		this.entityId = entityId;
		this.entityStatus = entityStatus;
	}

	/**
	 * Reads the entity id and the status byte from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityId = dataInputStream.readInt();
		this.entityStatus = dataInputStream.readByte();
	}

	/**
	 * Writes the entity id and the status byte to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityId);
		dataOutputStream.writeByte(this.entityStatus);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleEntityStatus}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleEntityStatus(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 for the entity id + 1 for the status byte)
	 */
	@Override
	public int getPacketSize() {
		return 5;
	}
}
