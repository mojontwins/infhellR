package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent when a player closes an open window.
 * This notifies the server that the client has closed the GUI,
 * allowing the server to update inventory state and release resources.
 *
 * @see Packet100OpenWindow
 * @see Packet102WindowClick
 * @see Packet106Transaction
 */
public class Packet101CloseWindow extends Packet {
    /**
     * The window ID of the window being closed.
     * This must match the ID from the corresponding Packet100OpenWindow.
     */
    public int windowId;

    public Packet101CloseWindow() {
    }

    /**
     * Constructs a Packet101CloseWindow for the specified window.
     *
     * @param windowId The ID of the window to close
     */
    public Packet101CloseWindow(int windowId) {
        this.windowId = windowId;
    }

    /**
     * Processes this packet on the server side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleCloseWindow(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads only the window ID as a single byte.
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.windowId = dataInputStream.readByte();
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes only the window ID as a single byte.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.windowId);
    }

    /**
     * Returns the size of this packet in bytes.
     *
     * @return Always returns 1 (single byte for window ID)
     */
    public int getPacketSize() {
        return 1;
    }
}
