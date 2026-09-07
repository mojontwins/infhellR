package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet 32: Entity Look.
 * <p>
 * Sent by the server to inform clients that an entity has rotated without
 * changing its position. The yaw and pitch are stored as single signed bytes
 * (representations of the 0-256 byte-encoded range). The {@link #rotating}
 * flag is forced to {@code true} by the constructors so the receiving
 * NetHandler knows to apply the rotation.
 *
 * @see Packet30Entity
 * @see Packet33RelEntityMoveLook
 */
public class Packet32EntityLook extends Packet30Entity {
	public Packet32EntityLook() {
		this.rotating = true;
	}

	/**
	 * Constructs a rotation-only packet for the given entity.
	 *
	 * @param entityId the id of the entity that rotated
	 * @param yaw      encoded yaw (byte, full circle = 256)
	 * @param pitch    encoded pitch (byte, 0 = -90, 128 = 0, 256 = +90)
	 */
	public Packet32EntityLook(int entityId, byte yaw, byte pitch) {
		super(entityId);
		this.yaw = yaw;
		this.pitch = pitch;
		this.rotating = true;
	}

	/**
	 * Reads the yaw and pitch bytes after the entity id has been consumed.
	 *
	 * @param dataInputStream the input stream positioned just after the entity id
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		super.readPacketData(dataInputStream);
		this.yaw = dataInputStream.readByte();
		this.pitch = dataInputStream.readByte();
	}

	/**
	 * Writes the yaw and pitch bytes after the entity id has been written.
	 *
	 * @param dataOutputStream the output stream positioned just after the entity id
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		super.writePacketData(dataOutputStream);
		dataOutputStream.writeByte(this.yaw);
		dataOutputStream.writeByte(this.pitch);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 for the entity id plus 2 rotation bytes)
	 */
	@Override
	public int getPacketSize() {
		return 6;
	}
}
