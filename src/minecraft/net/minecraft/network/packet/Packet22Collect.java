package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Collect packet - tells clients an entity picked up an item.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Notify clients that an item entity was collected</li>
 *   <li>Play collect "pop" animation flying toward collector</li>
 *   <li>Remove the item entity from world</li>
 * </ul>
 * 
 * <p><b>Flow:</b> Server sends this packet, then sends Packet29DestroyEntity
 * to remove the item entity.</p>
 */
public class Packet22Collect extends Packet {

    /** Entity ID of the item being collected */
    public int collectedEntityId;

    /** Entity ID of the entity collecting the item (usually player) */
    public int collectorEntityId;

    /**
     * Default constructor for deserialization.
     */
    public Packet22Collect() {
    }

    /**
     * Creates a collect packet.
     * 
     * @param collectedEntityId Item entity being picked up
     * @param collectorEntityId Entity picking up the item
     */
    public Packet22Collect(int collectedEntityId, int collectorEntityId) {
        this.collectedEntityId = collectedEntityId;
        this.collectorEntityId = collectorEntityId;
    }

    /**
     * Reads collect data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.collectedEntityId = dataInputStream.readInt();
        this.collectorEntityId = dataInputStream.readInt();
    }

    /**
     * Writes collect data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.collectedEntityId);
        dataOutputStream.writeInt(this.collectorEntityId);
    }

    /**
     * Processes collect packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleCollect(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 8 bytes
     */
    public int getPacketSize() {
        return 8;
    }
}
