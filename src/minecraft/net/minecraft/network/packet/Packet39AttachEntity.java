package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.Entity;
import net.minecraft.network.NetHandler;

/**
 * Packet 39: Attach Entity.
 * <p>
 * Informs the client that one entity should be parented to another (for
 * example a player mounting a minecart, a boat, or a horse-like mount) or,
 * conversely, that an entity should be detached. The "vehicle" id is set to
 * {@code -1} to signal detachment, which is more compact than emitting a
 * separate detach packet.
 */
public class Packet39AttachEntity extends Packet {
	/** The id of the entity that is being attached (e.g. the player). */
	public int entityId;

	/** The id of the vehicle to attach to, or {@code -1} to detach. */
	public int vehicleEntityId;

	public Packet39AttachEntity() {
	}

	/**
	 * Convenience constructor that builds the packet from two live entities.
	 * A {@code null} vehicle is treated as a detach request.
	 *
	 * @param entity  the entity to attach
	 * @param vehicle the vehicle to attach it to, or {@code null} to detach
	 */
	public Packet39AttachEntity(Entity entity, Entity vehicle) {
		this.entityId = entity.entityId;
		this.vehicleEntityId = vehicle != null ? vehicle.entityId : -1;
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (two ints = 8 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 8;
	}

	/**
	 * Reads the entity id and the vehicle id from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityId = dataInputStream.readInt();
		this.vehicleEntityId = dataInputStream.readInt();
	}

	/**
	 * Writes the entity id and the vehicle id to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityId);
		dataOutputStream.writeInt(this.vehicleEntityId);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleAttachEntity}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleAttachEntity(this);
	}
}
