package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Destroy entity packet - tells clients an entity is being removed.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</p>
 * <ul>
 *   <li>Remove entity from client world</li>
 *   <li>Used when entity dies, is collected, or leaves render distance</li>
 *   <li>Client removes entity from its entity list</li>
 * </ul>
 * 
 * <p><b>Note:</b> For death, server typically sends this after health reaches 0.</p>
 */
public class Packet29DestroyEntity extends Packet {

    /** Entity ID to destroy */
    public int entityId;

    /**
     * Default constructor for deserialization.
     */
    public Packet29DestroyEntity() {
    }

    /**
     * Creates a destroy entity packet.
     * 
     * @param entityId Entity ID to remove
     */
    public Packet29DestroyEntity(int entityId) {
        this.entityId = entityId;
    }

    /**
     * Reads entity ID from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
    }

    /**
     * Writes entity ID to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
    }

    /**
     * Processes destroy entity packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleDestroyEntity(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 4 bytes
     */
    public int getPacketSize() {
        return 4;
    }
}
