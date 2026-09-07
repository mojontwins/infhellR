package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Respawn packet - handles dimension changes and respawn after death.
 * 
 * <p><b>Direction:</b> Both client and server can send.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Client -> Server: trigger respawn after death</li>
 *   <li>Server -> Client: confirm respawn and provide new dimension info</li>
 *   <li>Teleport player to respawn point in new dimension</li>
 * </ul>
 * 
 * <p><b>Dimension values:</b></p>
 * <ul>
 *   <li>-1 = The End</li>
 *   <li>0 = Overworld (surface)</li>
 *   <li>1 = Nether</li>
 * </ul>
 * 
 * @see Packet1Login
 */
public class Packet9Respawn extends Packet {

    /** Dimension ID (-1=end, 0=overworld, 1=nether) */
    public byte dimension;

    /** X coordinate of last death position (for respawn anchoring) */
    public int lastDeathX;

    /** Y coordinate of last death position */
    public int lastDeathY;

    /** Z coordinate of last death position */
    public int lastDeathZ;

    /**
     * Default constructor for deserialization.
     */
    public Packet9Respawn() {
    }

    /**
     * Creates a respawn packet.
     * 
     * @param dimension  Target dimension ID
     * @param lastDeathX X coord of death position
     * @param lastDeathY Y coord of death position
     * @param lastDeathZ Z coord of death position
     */
    public Packet9Respawn(byte dimension, int lastDeathX, int lastDeathY, int lastDeathZ) {
        this.dimension = dimension;
        this.lastDeathX = lastDeathX;
        this.lastDeathY = lastDeathY;
        this.lastDeathZ = lastDeathZ;
    }

    /**
     * Processes respawn packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleRespawn(this);
    }

    /**
     * Reads respawn data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.dimension = dataInputStream.readByte();

        this.lastDeathX = dataInputStream.readInt();
        this.lastDeathY = dataInputStream.readInt();
        this.lastDeathZ = dataInputStream.readInt();
    }

    /**
     * Writes respawn data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.dimension);

        dataOutputStream.writeInt(this.lastDeathX);
        dataOutputStream.writeInt(this.lastDeathY);
        dataOutputStream.writeInt(this.lastDeathZ);
    }

    /**
     * Returns packet size.
     * 
     * @return 13 bytes
     */
    public int getPacketSize() {
        return 13;
    }
}
