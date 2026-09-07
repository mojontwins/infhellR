package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.network.NetHandler;

/**
 * Packet 41: Entity Effect.
 * <p>
 * Sent by the server to inform the client that a potion/status effect has
 * been applied to an entity. The client uses the supplied amplifier and
 * duration to render the appropriate particle overlay and HUD icon. The
 * effect id is the standard StatusEffect type id, masked to fit in a byte.
 */
public class Packet41EntityEffect extends Packet {
	/** The id of the entity receiving the effect. */
	public int entityId;

	/** The status effect type id (see {@link StatusEffect#statusID}). */
	public byte effectId;

	/** The effect amplifier (0 = level I, 1 = level II, etc.). */
	public byte effectAmp;

	/** The remaining duration of the effect in ticks. */
	public short duration;

	public Packet41EntityEffect() {
	}

	/**
	 * Constructs an entity-effect packet from a live StatusEffect instance.
	 *
	 * @param entityId     the id of the entity
	 * @param statusEffect the effect to send (its id, amplifier, and duration are copied)
	 */
	public Packet41EntityEffect(int entityId, StatusEffect statusEffect) {
		this.entityId = entityId;
		this.effectId = (byte)(statusEffect.statusID & 255);
		this.effectAmp = (byte)(statusEffect.amplifier & 255);
		this.duration = (short)statusEffect.duration;
	}

	/**
	 * Reads the entity id, effect id, amplifier, and duration from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityId = dataInputStream.readInt();
		this.effectId = dataInputStream.readByte();
		this.effectAmp = dataInputStream.readByte();
		this.duration = dataInputStream.readShort();
	}

	/**
	 * Writes the entity id, effect id, amplifier, and duration to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityId);
		dataOutputStream.writeByte(this.effectId);
		dataOutputStream.writeByte(this.effectAmp);
		dataOutputStream.writeShort(this.duration);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleEntityEffect}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleEntityEffect(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 + 1 + 1 + 2 = 8 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 8;
	}
}
