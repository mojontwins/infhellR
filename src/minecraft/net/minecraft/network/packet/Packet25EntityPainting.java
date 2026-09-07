package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.EnumArt;
import net.minecraft.network.NetHandler;

/**
 * Entity painting packet - tells clients about a painting entity.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Spawn paintings in the world</li>
 *   <li>Include painting title for correct texture selection</li>
 *   <li>Include position and facing direction</li>
 * </ul>
 * 
 * <p><b>Direction values:</b></p>
 * <ul>
 *   <li>0 = Facing west (painting points east)</li>
 *   <li>1 = Facing north (painting points south)</li>
 *   <li>2 = Facing east (painting points west)</li>
 *   <li>3 = Facing south (painting points north)</li>
 * </ul>
 * 
 * @see EnumArt
 */
public class Packet25EntityPainting extends Packet {

    /** Painting entity ID */
    public int entityId;

    /** Painting X coordinate (block coords) */
    public int xPosition;

    /** Painting Y coordinate (block coords) */
    public int yPosition;

    /** Painting Z coordinate (block coords) */
    public int zPosition;

    /** Direction the painting faces (0-3: see class javadoc) */
    public int direction;

    /** Painting title (determines texture) */
    public String title;

    /**
     * Default constructor for deserialization.
     */
    public Packet25EntityPainting() {
    }

    /**
     * Creates an entity painting packet from a painting entity.
     * 
     * @param entityPainting The painting entity
     */
    public Packet25EntityPainting(EntityPainting entityPainting) {
        this.entityId = entityPainting.entityId;
        this.xPosition = entityPainting.xPosition;
        this.yPosition = entityPainting.yPosition;
        this.zPosition = entityPainting.zPosition;
        this.direction = entityPainting.direction;
        this.title = entityPainting.art.title;
    }

    /**
     * Reads painting spawn data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.title = readString(dataInputStream, EnumArt.maxArtTitleLength);
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.readInt();
        this.zPosition = dataInputStream.readInt();
        this.direction = dataInputStream.readInt();
    }

    /**
     * Writes painting spawn data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        writeString(this.title, dataOutputStream);
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.writeInt(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
        dataOutputStream.writeInt(this.direction);
    }

    /**
     * Processes painting spawn packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleEntityPainting(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 24 bytes
     */
    public int getPacketSize() {
        return 24;
    }
}
