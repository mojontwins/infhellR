package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Entity packet - base packet for entity updates with relative movement.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Base class for entity movement packets (31, 32, 33, 34)</li>
 *   <li>Contains entity ID</li>
 *   <li>Subclasses add position/rotation data</li>
 * </ul>
 * 
 * <p><b>Note:</b> This packet alone only contains the entity ID.
 * The actual movement data comes from subclasses.</p>
 * 
 * <p><b>Subclasses:</b></p>
 * <ul>
 *   <li>{@link Packet31RelEntityMove} - relative position update</li>
 *   <li>{@link Packet32EntityLook} - rotation update</li>
 *   <li>{@link Packet33RelEntityMoveLook} - relative position + rotation</li>
 *   <li>{@link Packet34EntityTeleport} - absolute position update</li>
 * </ul>
 */
public class Packet30Entity extends Packet {

    /** Entity ID */
    public int entityId;

    /** X offset from last position (byte, -128 to 127) */
    public byte xPosition;

    /** Y offset from last position (byte, -128 to 127) */
    public byte yPosition;

    /** Z offset from last position (byte, -128 to 127) */
    public byte zPosition;

    /** Yaw rotation (byte) */
    public byte yaw;

    /** Pitch rotation (byte) */
    public byte pitch;

    /** True if this packet contains rotation data */
    public boolean rotating = false;

    /**
     * Default constructor for deserialization.
     */
    public Packet30Entity() {
    }

    /**
     * Creates an entity packet with given ID.
     * 
     * @param entityId The entity ID
     */
    public Packet30Entity(int entityId) {
        this.entityId = entityId;
    }

    /**
     * Reads entity ID from stream.
     * 
     * <p>Subclasses override to read additional fields.</p>
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
     * <p>Subclasses override to write additional fields.</p>
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
    }

    /**
     * Processes entity packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleEntity(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 4 bytes (entity ID only)
     */
    public int getPacketSize() {
        return 4;
    }
}
