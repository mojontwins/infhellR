package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Spawn position packet - sets the world's spawn/respawn point.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Tells client where the world's spawn point is (compass points here)</li>
 *   <li>Used for compass HUD needle direction</li>
 *   <li>Sets player respawn point on death</li>
 *   <li>Initially sent on player join and when spawn changes</li>
 * </ul>
 * 
 * <p><b>Coordinates:</b> Block coordinates of the spawn point center.</p>
 */
public class Packet6SpawnPosition extends Packet {

    /** Spawn X coordinate (block coords) */
    public int xPosition;

    /** Spawn Y coordinate (block coords) */
    public int yPosition;

    /** Spawn Z coordinate (block coords) */
    public int zPosition;

    /**
     * Default constructor for deserialization.
     */
    public Packet6SpawnPosition() {
    }

    /**
     * Creates a spawn position packet.
     * 
     * @param xPosition X coordinate of spawn
     * @param yPosition Y coordinate of spawn
     * @param zPosition Z coordinate of spawn
     */
    public Packet6SpawnPosition(int xPosition, int yPosition, int zPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.zPosition = zPosition;
    }

    /**
     * Reads spawn position from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.readInt();
        this.zPosition = dataInputStream.readInt();
    }

    /**
     * Writes spawn position to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.writeInt(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
    }

    /**
     * Processes spawn position packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleSpawnPosition(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 12 bytes (3 ints)
     */
    public int getPacketSize() {
        return 12;
    }
}
