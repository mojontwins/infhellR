package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Use entity packet - player interaction with another entity.
 * 
 * <p><b>Direction:</b> Client -> Server only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Player attacking an entity (left click)</li>
 *   <li>Player right-clicking on entity (mount, interact, etc.)</li>
 *   <li>Server validates action and applies damage/interaction</li>
 * </ul>
 * 
 * <p><b>Click type:</b> 0 = right click (interact), 1 = left click (attack)</p>
 */
public class Packet7UseEntity extends Packet {

    /** Entity ID of player performing the action */
    public int playerEntityId;

    /** Entity ID of target being acted upon */
    public int targetEntity;

    /** Click type: 0=right click (interact), 1=left click (attack) */
    public int isLeftClick;

    /**
     * Default constructor for deserialization.
     */
    public Packet7UseEntity() {
    }

    /**
     * Creates a use entity packet.
     * 
     * @param playerEntityId Player performing action
     * @param targetEntity   Target entity
     * @param isLeftClick    Click type (0=right, 1=left)
     */
    public Packet7UseEntity(int playerEntityId, int targetEntity, int isLeftClick) {
        this.playerEntityId = playerEntityId;
        this.targetEntity = targetEntity;
        this.isLeftClick = isLeftClick;
    }

    /**
     * Reads use entity data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.playerEntityId = dataInputStream.readInt();
        this.targetEntity = dataInputStream.readInt();
        this.isLeftClick = dataInputStream.readByte();
    }

    /**
     * Writes use entity data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.playerEntityId);
        dataOutputStream.writeInt(this.targetEntity);
        dataOutputStream.writeByte(this.isLeftClick);
    }

    /**
     * Processes use entity packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleUseEntity(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 9 bytes
     */
    public int getPacketSize() {
        return 9;
    }
}
