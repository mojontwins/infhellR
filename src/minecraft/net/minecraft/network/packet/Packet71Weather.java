package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLightningBolt;
import net.minecraft.network.NetHandler;

/**
 * Packet 71: Weather.
 * <p>
 * Sent by the server to inform the client about a weather-related entity
 * spawn. The most common use case is to spawn a visual lightning bolt at a
 * given position; in that case the {@link #lightning} flag is set to 1 and
 * the entity referenced is an {@link EntityLightningBolt}. Position is encoded
 * as fixed-point integers where one block equals 32 units.
 */
public class Packet71Weather extends Packet {
	/** The id of the weather-related entity (typically a lightning bolt). */
	public int entityID;

	/** X position of the entity, in fixed-point units (1 block = 32 units). */
	public int encodedPosX;

	/** Y position of the entity, in fixed-point units (1 block = 32 units). */
	public int encodedPosY;

	/** Z position of the entity, in fixed-point units (1 block = 32 units). */
	public int encodedPosZ;

	/**
	 * 1 if the entity is a lightning bolt (triggers a flash + sound effect on
	 * the client), 0 otherwise (only the entity spawn is processed).
	 */
	public int lightning;

	public Packet71Weather() {
	}

	/**
	 * Constructs a weather packet from a live entity, automatically detecting
	 * whether it is a lightning bolt.
	 *
	 * @param entity the weather-related entity to send
	 */
	public Packet71Weather(Entity entity) {
		this.entityID = entity.entityId;
		this.encodedPosX = MathHelper.floor_double(entity.posX * 32.0D);
		this.encodedPosY = MathHelper.floor_double(entity.posY * 32.0D);
		this.encodedPosZ = MathHelper.floor_double(entity.posZ * 32.0D);

		// Lightning bolts get the "1" flag so the client knows to play the
		// flash + thunder sound in addition to spawning the entity.
		if (entity instanceof EntityLightningBolt) {
			this.lightning = 1;
		}
	}

	/**
	 * Reads the entity id, lightning flag, and the three fixed-point positions
	 * from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityID = dataInputStream.readInt();
		this.lightning = dataInputStream.readByte();
		this.encodedPosX = dataInputStream.readInt();
		this.encodedPosY = dataInputStream.readInt();
		this.encodedPosZ = dataInputStream.readInt();
	}

	/**
	 * Writes the entity id, lightning flag, and the three fixed-point positions
	 * to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityID);
		dataOutputStream.writeByte(this.lightning);
		dataOutputStream.writeInt(this.encodedPosX);
		dataOutputStream.writeInt(this.encodedPosY);
		dataOutputStream.writeInt(this.encodedPosZ);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleWeather}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleWeather(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 + 1 + 4 + 4 + 4 = 17 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 17;
	}
}
