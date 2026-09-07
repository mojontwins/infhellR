package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.game.entity.DataWatcher;
import net.minecraft.game.entity.WatchableObject;
import net.minecraft.network.NetHandler;

/**
 * Packet 40: Entity Metadata.
 * <p>
 * Carries a list of changed {@link WatchableObject data watcher entries} for
 * an entity. Used to keep per-entity stateful data (such as an entity's
 * custom name, on-fire flag, baby state, sitting flag, or potion-particle
 * data) synchronized between the server and its clients. Only the entries
 * that have actually changed since the previous update are sent.
 */
public class Packet40EntityMetadata extends Packet {
	/** The id of the entity whose metadata is being sent. */
	public int entityId;

	/** The list of changed metadata entries to apply on the client. */
	private List<WatchableObject> metadata;

	public Packet40EntityMetadata() {
	}

	/**
	 * Constructs a metadata packet that snapshots the dirty entries from the
	 * given DataWatcher.
	 *
	 * @param entityId   the id of the entity
	 * @param dataWatcher the DataWatcher to snapshot
	 */
	public Packet40EntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.metadata = dataWatcher.getChangedObjects();
	}

	/**
	 * Reads the entity id followed by the metadata list from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.entityId = dataInputStream.readInt();
		this.metadata = DataWatcher.readWatchableObjects(dataInputStream);
	}

	/**
	 * Writes the entity id followed by the metadata list to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.entityId);
		DataWatcher.writeObjectsInListToStream(this.metadata, dataOutputStream);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleEntityMetadata}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleEntityMetadata(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 for the entity id + 1 for the metadata terminator)
	 */
	@Override
	public int getPacketSize() {
		return 5;
	}

	/**
	 * @return the metadata list carried by this packet, used by the
	 *         NetHandler to apply the changes to the corresponding entity
	 */
	public List<WatchableObject> getMetadata() {
		return this.metadata;
	}
}
