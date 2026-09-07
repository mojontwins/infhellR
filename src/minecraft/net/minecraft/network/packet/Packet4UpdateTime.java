package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Update time packet - syncs server time-of-day with clients.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Syncs world time so day/night cycles match across all clients</li>
 *   <li>Sent periodically (e.g., every 20 ticks)</li>
 *   <li>Used for time-based effects (skeleton burning, crop growth)</li>
 * </ul>
 * 
 * <p><b>Time encoding:</b> Long representing total world time in ticks.
 * Lower bits determine day phase (day vs night), higher bits determine day count.</p>
 */
public class Packet4UpdateTime extends Packet {

    /** World time in ticks (24000 = one full day cycle) */
    public long time;

    /**
     * Default constructor for deserialization.
     */
    public Packet4UpdateTime() {
    }

    /**
     * Creates a time update packet.
     * 
     * @param time Current world time in ticks
     */
    public Packet4UpdateTime(long time) {
        this.time = time;
    }

    /**
     * Reads time value from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.time = dataInputStream.readLong();
    }

    /**
     * Writes time value to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeLong(this.time);
    }

    /**
     * Processes time update packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleUpdateTime(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 8 bytes (long)
     */
    public int getPacketSize() {
        return 8;
    }
}
