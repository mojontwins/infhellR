package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.network.NetHandler;

/**
 * Packet 34: Entity Teleport.
 * <p>
 * Sent by the server to inform clients of the absolute position and rotation
 * of an entity. Used for fresh spawns, large jumps, or respawns where the
 * bandwidth-friendly relative packets ({@link Packet31RelEntityMove},
 * {@link Packet33RelEntityMoveLook}) cannot encode the magnitude of the
 * change. Position is encoded as fixed-point integers where one block equals
 * 32 units, and yaw/pitch are encoded as single bytes.
 */
public class Packet34EntityTeleport extends Packet {
	/** The id of the entity that was teleported. */
	public int entityId;

	/** X position in fixed-point units (1 block = 32 units). */
	public int xPosition;

	/** Y position in fixed-point units (1 block = 32 units). */
	public int yPosition;

	/** Z position in fixed-point units (1 block = 32 units). */
	public int zPosition;

	/** Yaw encoded as a single byte (256 = full circle). */
	public byte yaw;

	/** Pitch encoded as a single byte (0 = -90 deg, 128 = 0, 256 = +90 deg). */
	public byte pitch;

	public Packet34EntityTeleport() {
	}

	/**
	 * Convenience constructor that fills the packet from a live entity,
	 * converting its floating-point position and rotation to the wire format.
	 *
	 * @param entity the entity whose state should be sent
	 */
	public Packet34EntityTeleport(Entity entity) {
		this.entityId = entity.entityId;
		this.xPosition = MathHelper.floor_double(entity.posX * 32.0D);
		this.yPosition = MathHelper.floor_double(entity.posY * 32.0D);
		this.zPosition = MathHelper.floor_double(entity.posZ * 32.0D);
		this.yaw = (byte)((int)(entity.rotationYaw * 256.0F / 360.0F));
		this.pitch = (byte)((int)(entity.rotationPitch * 256.0F / 360.0F));
	}

	/**
	 * Constructs a teleport packet from raw wire-format values.
	 *
	 * @param entityId the id of the entity
	 * @param x        X position in fixed-point units
	 * @param y        Y position in fixed-point units
	 * @param z        Z position in fixed-point units
	 * @param yaw      encoded yaw byte
	 * @param pitch    encoded pitch byte
	 */
	public Packet34EntityTeleport(int entityId, int x, int y, int z, byte yaw, byte pitch) {
		this.entityId = entityId;
		this.xPosition = x;
		this.yPosition = y;
		this.zPosition = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	/**
	 * Reads the entity id, three fixed-point positions, and the two rotation
	 * bytes from the input stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityId = dataInputStream.readInt();
		this.xPosition = dataInputStream.readInt();
		this.yPosition = dataInputStream.readInt();
		this.zPosition = dataInputStream.readInt();
		this.yaw = (byte)dataInputStream.read();
		this.pitch = (byte)dataInputStream.read();
	}

	/**
	 * Writes the entity id, three fixed-point positions, and the two rotation
	 * bytes to the output stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityId);
		dataOutputStream.writeInt(this.xPosition);
		dataOutputStream.writeInt(this.yPosition);
		dataOutputStream.writeInt(this.zPosition);
		dataOutputStream.write(this.yaw);
		dataOutputStream.write(this.pitch);
	}

	/**
	 * Dispatches this packet to the appropriate handler method on the given
	 * NetHandler.
	 *
	 * @param netHandler the NetHandler that will receive this packet
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleEntityTeleport(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 ints + 2 bytes = 18, plus 16 bytes for the fixed header)
	 */
	@Override
	public int getPacketSize() {
		return 34;
	}
}
