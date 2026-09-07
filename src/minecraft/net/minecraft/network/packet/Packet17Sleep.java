package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.Entity;
import net.minecraft.network.NetHandler;

/**
 * Sleep packet - player enters/leaves a bed.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Notifies client that a player is entering or leaving a bed</li>
 *   <li>Client renders the sleeping animation and resets spawn point</li>
 *   <li>Status code distinguishes enter vs leave</li>
 * </ul>
 * 
 * <p><b>field_22046_e (status) values:</b></p>
 * <ul>
 *   <li>0 = Player entered bed (start sleeping)</li>
 *   <li>2 = Player left bed (wake up)</li>
 *   <li>1 or other = Animation states</li>
 * </ul>
 * 
 * <p><b>Coordinate encoding:</b> X and Z as int (full coords), Y as byte (block height).</p>
 */
public class Packet17Sleep extends Packet {

    /** Entity ID of player sleeping */
    public int entityID;

    /** Bed X coordinate (int) */
    public int bedX;

    /** Bed Y coordinate (byte, block height 0-127) */
    public int bedY;

    /** Bed Z coordinate (int) */
    public int bedZ;

    /** Sleep state: 0=enter bed, 2=leave bed, etc. */
    public int field_22046_e;

    /**
     * Default constructor for deserialization.
     */
    public Packet17Sleep() {
    }

    /**
     * Creates a sleep packet for an entity entering/leaving bed.
     * 
     * @param entity   Player entity
     * @param status   Sleep status code
     * @param bedX     Bed X coordinate
     * @param bedY     Bed Y coordinate
     * @param bedZ     Bed Z coordinate
     */
    public Packet17Sleep(Entity entity, int status, int bedX, int bedY, int bedZ) {
        this.field_22046_e = status;
        this.bedX = bedX;
        this.bedY = bedY;
        this.bedZ = bedZ;
        this.entityID = entity.entityId;
    }

    /**
     * Reads sleep data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityID = dataInputStream.readInt();
        this.field_22046_e = dataInputStream.readByte();
        this.bedX = dataInputStream.readInt();
        this.bedY = dataInputStream.readByte();
        this.bedZ = dataInputStream.readInt();
    }

    /**
     * Writes sleep data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityID);
        dataOutputStream.writeByte(this.field_22046_e);
        dataOutputStream.writeInt(this.bedX);
        dataOutputStream.writeByte(this.bedY);
        dataOutputStream.writeInt(this.bedZ);
    }

    /**
     * Processes sleep packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleSleep(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 14 bytes
     */
    public int getPacketSize() {
        return 14;
    }
}
