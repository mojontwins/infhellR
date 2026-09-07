package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet 31: Relative Entity Move.
 * <p>
 * Sent by the server to inform clients that an entity has moved a small
 * relative amount. The translation is encoded as signed bytes where each
 * unit represents 1/32 of a block. The companion absolute-position packet
 * is {@link Packet34EntityTeleport} which is used for large jumps or
 * teleports.
 * <p>
 * This packet does <em>not</em> contain rotation data. If the entity is also
 * rotating, see {@link Packet33RelEntityMoveLook}.
 *
 * @see Packet30Entity
 * @see Packet33RelEntityMoveLook
 * @see Packet34EntityTeleport
 */
public class Packet31RelEntityMove extends Packet30Entity {
	public Packet31RelEntityMove() {
	}

	/**
	 * Constructs a relative-move packet for the given entity.
	 *
	 * @param entityId the id of the entity that moved
	 * @param dx       signed relative X displacement in 1/32-block units
	 * @param dy       signed relative Y displacement in 1/32-block units
	 * @param dz       signed relative Z displacement in 1/32-block units
	 */
	public Packet31RelEntityMove(int entityId, byte dx, byte dy, byte dz) {
		super(entityId);
		this.xPosition = dx;
		this.yPosition = dy;
		this.zPosition = dz;
	}

	/**
	 * Reads the three signed-byte deltas from the wire, after the entity id
	 * has been consumed by the superclass.
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
	}

	/**
	 * Writes the three signed-byte deltas to the wire, after the entity id
	 * has been written by the superclass.
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
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 for the entity id plus 3 single-byte deltas)
	 */
	@Override
	public int getPacketSize() {
		return 7;
	}
}
