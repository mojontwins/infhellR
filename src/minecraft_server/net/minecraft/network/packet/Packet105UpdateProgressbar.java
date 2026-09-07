package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent to update a progress bar in a window (typically furnaces).
 * Used for displaying furnace smelting progress, brewing progress, or
 * other container-specific progress indicators.
 *
 * @see Packet100OpenWindow
 * @see Packet103SetSlot
 */
public class Packet105UpdateProgressbar extends Packet {
    /** The window ID containing the progress bar */
    public int windowId;

    /**
     * Which progress bar to update.
     * For furnaces: 0 = smelting progress, 1 = fuel remaining.
     */
    public int progressBar;

    /** Current value of the progress bar */
    public int progressBarValue;

    public Packet105UpdateProgressbar() {
    }

    /**
     * Constructs a Packet105UpdateProgressbar to update a specific progress indicator.
     *
     * @param windowId The window ID containing the progress bar
     * @param progressBar The progress bar index to update
     * @param progressBarValue The new value for the progress bar
     */
    public Packet105UpdateProgressbar(int windowId, int progressBar, int progressBarValue) {
        this.windowId = windowId;
        this.progressBar = progressBar;
        this.progressBarValue = progressBarValue;
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleUpdateProgressBar(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: window ID (byte), progress bar index (short), value (short).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.windowId = dataInputStream.readByte();
        this.progressBar = dataInputStream.readShort();
        this.progressBarValue = dataInputStream.readShort();
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: window ID (byte), progress bar index (short), value (short).
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.windowId);
        dataOutputStream.writeShort(this.progressBar);
        dataOutputStream.writeShort(this.progressBarValue);
    }

    /**
     * Returns the size of this packet in bytes.
     *
     * @return Always returns 5 bytes
     */
    public int getPacketSize() {
        return 5;
    }
}
