package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent when a player opens a window (e.g., chest, crafting table, furnace).
 * This tells the client to open a GUI for the specified window type and displays
 * the window title and slot count.
 *
 * @see Packet101CloseWindow
 * @see Packet102WindowClick
 * @see Packet103SetSlot
 * @see Packet104WindowItems
 * @see Packet105UpdateProgressbar
 * @see Packet106Transaction
 * @see Packet107CreativeSetSlot
 */
public class Packet100OpenWindow extends Packet {
    /**
     * Unique identifier for this window instance.
     * Used to correlate subsequent window-related packets.
     */
    public int windowId;

    /**
     * Type of inventory window being opened.
     * Corresponds to constants like WINDOW_CRAFTING, WINDOW_FURNACE, WINDOW_CHEST, etc.
     */
    public int inventoryType;

    /**
     * Display title of the window, shown in the GUI header.
     * For vanilla containers, this is typically a localized string key.
     */
    public String windowTitle;

    /**
     * Total number of slots in this window.
     * Includes both player inventory slots and container slots.
     */
    public int slotsCount;

    public Packet100OpenWindow() {
    }

    /**
     * Constructs a Packet100OpenWindow with all required parameters.
     *
     * @param windowId Unique identifier for this window
     * @param inventoryType Type of inventory (e.g., chest, furnace, crafting)
     * @param windowTitle Title displayed in the window header
     * @param slotsCount Total number of slots in the window
     */
    public Packet100OpenWindow(int windowId, int inventoryType, String windowTitle, int slotsCount) {
        this.windowId = windowId;
        this.inventoryType = inventoryType;
        this.windowTitle = windowTitle;
        this.slotsCount = slotsCount;
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleOpenWindow(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: window ID (byte), inventory type (byte), window title (UTF string),
     * and slots count (byte).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.windowId = dataInputStream.readByte();
        this.inventoryType = dataInputStream.readByte();
        this.windowTitle = dataInputStream.readUTF();
        this.slotsCount = dataInputStream.readByte();
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: window ID (byte), inventory type (byte), window title (UTF string),
     * and slots count (byte).
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.windowId);
        dataOutputStream.writeByte(this.inventoryType);
        dataOutputStream.writeUTF(this.windowTitle);
        dataOutputStream.writeByte(this.slotsCount);
    }

    /**
     * Returns the approximate size of this packet in bytes.
     * Used for bandwidth estimation and packet queue management.
     *
     * @return Packet size in bytes (3 bytes + UTF string length)
     */
    public int getPacketSize() {
        return 3 + this.windowTitle.length();
    }
}
