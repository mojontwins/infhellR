package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Update health packet - syncs player health with client.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Updates the heart display in HUD</li>
 *   <li>Sent whenever player takes damage or heals</li>
 *   <li>Value of 0 indicates player death</li>
 * </ul>
 * 
 * <p><b>Note:</b> This is a 16-bit signed short, so max health is 32767.
 * In vanilla Beta 1.7.3, max is 20 (10 hearts).</p>
 */
public class Packet8UpdateHealth extends Packet {

    /** Player health value (0 = dead, 20 = full in vanilla) */
    public int healthMP;

    /**
     * Default constructor for deserialization.
     */
    public Packet8UpdateHealth() {
    }

    /**
     * Creates an update health packet.
     * 
     * @param health New health value
     */
    public Packet8UpdateHealth(int health) {
        this.healthMP = health;
    }

    /**
     * Reads health value from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.healthMP = dataInputStream.readShort();
    }

    /**
     * Writes health value to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.healthMP);
    }

    /**
     * Processes health update packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleUpdateHealth(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 2 bytes
     */
    public int getPacketSize() {
        return 2;
    }
}
