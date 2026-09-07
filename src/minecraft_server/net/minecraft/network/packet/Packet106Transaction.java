package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent in response to Packet102WindowClick to confirm or reject a transaction.
 * The server sends this after processing a click to indicate whether the action
 * was successful. If rejected, the client should revert the item display.
 *
 * <p>Transaction flow:
 * <ol>
 *   <li>Client sends Packet102WindowClick with action number</li>
 *   <li>Server processes and validates the click</li>
 *   <li>Server responds with Packet106Transaction (accepted/rejected)</li>
 * </ol>
 *
 * @see Packet102WindowClick
 */
public class Packet106Transaction extends Packet {
    /** The window ID involved in this transaction */
    public int windowId;

    /**
     * The action number from the corresponding click packet.
     * Used to match this response to the correct click action.
     */
    public short shortWindowId;

    /**
     * Whether the transaction was accepted by the server.
     * True = accepted, false = rejected (client should revert).
     */
    public boolean packetBoolean;

    public Packet106Transaction() {
    }

    /**
     * Constructs a Packet106Transaction response.
     *
     * @param windowId The window ID
     * @param actionNumber The action number from the click packet
     * @param accepted Whether the server accepted this action
     */
    public Packet106Transaction(int windowId, short actionNumber, boolean accepted) {
        this.windowId = windowId;
        this.shortWindowId = actionNumber;
        this.packetBoolean = accepted;
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleTransaction(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: window ID (byte), action number (short), accepted flag (boolean).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.windowId = dataInputStream.readByte();
        this.shortWindowId = dataInputStream.readShort();
        this.packetBoolean = dataInputStream.readByte() != 0;
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: window ID (byte), action number (short), accepted flag (byte 0/1).
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.windowId);
        dataOutputStream.writeShort(this.shortWindowId);
        dataOutputStream.writeByte(this.packetBoolean ? 1 : 0);
    }

    /**
     * Returns the size of this packet in bytes.
     *
     * @return Always returns 4 bytes
     */
    public int getPacketSize() {
        return 4;
    }
}
