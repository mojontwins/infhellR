package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet 33: Relative Entity Move and Look.
 * <p>
 * Combines a relative position update ({@link Packet31RelEntityMove}) with a
 * rotation update ({@link Packet32EntityLook}). All five values are encoded as
 * signed bytes. Used as a bandwidth-friendly way to keep many distant entities
 * in sync without having to send a full {@link Packet34EntityTeleport}.
 *
 * @see Packet30Entity
 * @see Packet31RelEntityMove
 * @see Packet32EntityLook
 * @see Packet34EntityTeleport
 */
public class Packet33RelEntityMoveLook extends Packet30Entity {
	public Packet33RelEntityMoveLook() {
		this.rotating = true;
	}

	/**
	 * Constructs a relative-move-and-look packet.
	 *
	 * @param entityId the id of the entity
	 * @param dx       signed relative X displacement in 1/32-block units
	 * @param dy       signed relative Y displacement in 1/32-block units
	 * @param dz       signed relative Z displacement in 1/32-block units
	 * @param yaw      encoded yaw (byte)
	 * @param pitch    encoded pitch (byte)
	 */
	public Packet33RelEntityMoveLook(int entityId, byte dx, byte dy, byte dz, byte yaw, byte pitch) {
		super(entityId);
		this.xPosition = dx;
		this.yPosition = dy;
		this.zPosition = dz;
		this.yaw = yaw;
		this.pitch = pitch;
		this.rotating = true;
	}

	/**
	 * Reads all five signed-byte values (3 deltas + yaw + pitch) after the
	 * entity id has been consumed by the superclass.
	 *
	 * @param dataInputStream the input stream positioned just after the entity id
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		super.readPacketData(dataInputStream);
		this.xPosition = dataInputStream.readByte();
		this.yPosition = dataInputStream.readByte();
		this.zPosition = dataInputStream.readByte();
		this.yaw = dataInputStream.readByte();
		this.pitch = dataInputStream.readByte();
	}

	/**
	 * Writes all five signed-byte values (3 deltas + yaw + pitch) after the
	 * entity id has been written by the superclass.
	 *
	 * @param dataOutputStream the output stream positioned just after the entity id
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		super.writePacketData(dataOutputStream);
		dataOutputStream.writeByte(this.xPosition);
		dataOutputStream.writeByte(this.yPosition);
		dataOutputStream.writeByte(this.zPosition);
		dataOutputStream.writeByte(this.yaw);
		dataOutputStream.writeByte(this.pitch);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 for the entity id plus 5 single-byte fields)
	 */
	@Override
	public int getPacketSize() {
		return 9;
	}
}
