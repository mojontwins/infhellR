package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.network.NetHandler;

/**
 * Packet 42: Remove Entity Effect.
 * <p>
 * Sent by the server to inform the client that a previously-applied
 * status/potion effect should be removed from the given entity. The client
 * uses this to stop rendering the corresponding particle overlay and HUD
 * icon.
 */
public class Packet42RemoveEntityEffect extends Packet {
	/** The id of the entity losing the effect. */
	public int entityId;

	/** The status effect type id to remove. */
	public byte effectId;

	public Packet42RemoveEntityEffect() {
	}

	/**
	 * Constructs a remove-effect packet that targets a specific effect.
	 *
	 * @param entityId     the id of the entity
	 * @param statusEffect the effect to remove (only its {@code statusID} is used)
	 */
	public Packet42RemoveEntityEffect(int entityId, StatusEffect statusEffect) {
		this.entityId = entityId;
		this.effectId = (byte)(statusEffect.statusID & 255);
	}

	/**
	 * Reads the entity id and effect id from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityId = dataInputStream.readInt();
		this.effectId = dataInputStream.readByte();
	}

	/**
	 * Writes the entity id and effect id to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityId);
		dataOutputStream.writeByte(this.effectId);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleRemoveEntityEffect}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleRemoveEntityEffect(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 for the entity id + 1 for the effect id)
	 */
	@Override
	public int getPacketSize() {
		return 5;
	}
}
