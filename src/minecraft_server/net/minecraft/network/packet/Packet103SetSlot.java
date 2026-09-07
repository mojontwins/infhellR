package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;

/**
 * Packet sent to update a single slot in an open window.
 * Used by the server to notify the client when a specific slot's contents change,
 * such as after processing a click or from external modifications.
 *
 * <p>Note: For updating multiple slots at once, use Packet104WindowItems instead.</p>
 *
 * @see Packet100OpenWindow
 * @see Packet102WindowClick
 * @see Packet104WindowItems
 */
public class Packet103SetSlot extends Packet {
    /** The window ID this slot belongs to (0 = player inventory) */
    public int windowId;

    /**
     * The slot index within the window.
     * For player inventory: 0-8 = hotbar, 9-35 = main inventory, 36+ = armor/offhand.
     */
    public int itemSlot;

    /**
     * The item stack now in this slot.
     * Null if the slot should be empty.
     */
    public ItemStack myItemStack;

    public Packet103SetSlot() {
    }

    /**
     * Constructs a Packet103SetSlot to update a single slot.
     *
     * @param windowId The window ID containing this slot
     * @param itemSlot The slot index to update
     * @param itemStack The new item stack (null for empty)
     */
    public Packet103SetSlot(int windowId, int itemSlot, ItemStack itemStack) {
        this.windowId = windowId;
        this.itemSlot = itemSlot;
        // Copy the item stack to prevent external modifications from affecting the packet
        this.myItemStack = itemStack == null ? itemStack : itemStack.copy();
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleSetSlot(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: window ID (byte), slot index (short), and item data.
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.windowId = dataInputStream.readByte();
        this.itemSlot = dataInputStream.readShort();

        // Read item: -1 indicates empty slot
        short itemId = dataInputStream.readShort();
        if (itemId >= 0) {
            byte stackSize = dataInputStream.readByte();
            short damage = dataInputStream.readShort();
            this.myItemStack = new ItemStack(itemId, stackSize, damage);
        } else {
            this.myItemStack = null;
        }
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: window ID (byte), slot index (short), and item data.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.windowId);
        dataOutputStream.writeShort(this.itemSlot);

        if (this.myItemStack == null) {
            dataOutputStream.writeShort(-1);
        } else {
            dataOutputStream.writeShort(this.myItemStack.itemID);
            dataOutputStream.writeByte(this.myItemStack.stackSize);
            dataOutputStream.writeShort(this.myItemStack.getItemDamage());
        }
    }

    /**
     * Returns the size of this packet in bytes.
     *
     * @return Always returns 8 bytes
     */
    public int getPacketSize() {
        return 8;
    }
}
