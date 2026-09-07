package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent when a player earns a statistic (e.g., "mobs killed", "blocks placed").
 * Used to update the statistics counter on the client when the server tracks stats.
 *
 * <p>The statistic ID and increment value identify which statistic was updated
 * and by how much. The client then displays this in the statistics screen.</p>
 */
public class Packet200Statistic extends Packet {
    /**
     * The statistic ID (corresponds to entries in StatBase or similar registry).
     * Identifies which statistic is being updated.
     */
    public int statId;

    /**
     * The amount to increment the statistic by.
     * Can be positive (gaining) or negative (losing).
     */
    public int statIncrement;

    public Packet200Statistic() {
    }

    /**
     * Constructs a Packet200Statistic to update a specific statistic.
     *
     * @param statId The statistic ID to update
     * @param increment The amount to increment the statistic by
     */
    public Packet200Statistic(int statId, int increment) {
        this.statId = statId;
        this.statIncrement = increment;
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleStatistic(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: statistic ID (int) and increment value (byte).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.statId = dataInputStream.readInt();
        this.statIncrement = dataInputStream.readByte();
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: statistic ID (int) and increment value (byte).
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.statId);
        dataOutputStream.writeByte(this.statIncrement);
    }

    /**
     * Returns the size of this packet in bytes.
     *
     * @return Always returns 6 bytes
     */
    public int getPacketSize() {
        return 6;
    }
}
