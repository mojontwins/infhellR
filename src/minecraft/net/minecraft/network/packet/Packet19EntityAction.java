package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.Entity;
import net.minecraft.network.NetHandler;

/**
 * Entity action packet - player state changes (crouch, sleep, etc.).
 * 
 * <p><b>Direction:</b> Client -> Server only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Player starts/stops sneaking</li>
 *   <li>Player starts/stops sprinting</li>
 *   <li>Player leaves bed (wake up)</li>
 *   <li>Player starts/stop sleeping</li>
 *   <li>Server validates and broadcasts state to other clients</li>
 * </ul>
 * 
 * <p><b>State values:</b></p>
 * <ul>
 *   <li>1 = Start sneaking</li>
 *   <li>2 = Stop sneaking</li>
 *   <li>3 = Leave bed</li>
 *   <li>4 = Start sprinting</li>
 *   <li>5 = Stop sprinting</li>
 * </ul>
 */
public class Packet19EntityAction extends Packet {

    /** Entity performing the action (usually player) */
    public int entityId;

    /** Action type code (see class javadoc) */
    public int state;

    /**
     * Default constructor for deserialization.
     */
    public Packet19EntityAction() {
    }

    /**
     * Creates an entity action packet.
     * 
     * @param entity Entity performing action
     * @param state  Action type code
     */
    public Packet19EntityAction(Entity entity, int state) {
        this.entityId = entity.entityId;
        this.state = state;
    }

    /**
     * Reads entity action from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.state = dataInputStream.readByte();
    }

    /**
     * Writes entity action to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        dataOutputStream.writeByte(this.state);
    }

    /**
     * Processes entity action packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleEntityAction(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 5 bytes
     */
    public int getPacketSize() {
        return 5;
    }
}
