package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;

/**
 * Packet sent to synchronize all items in an open window.
 * Used by the server to send the complete state of a container's inventory
 * to the client, such as when first opening a chest or after complex transactions.
 *
 * <p>Note: For updating a single slot, use Packet103SetSlot instead.</p>
 *
 * @see Packet100OpenWindow
 * @see Packet102WindowClick
 * @see Packet103SetSlot
 */
public class Packet104WindowItems extends Packet {
    /** The window ID whose items are being synchronized (0 = player inventory) */
    public int windowId;

    /**
     * Array of item stacks in the window.
     * Each index corresponds to a slot in the container.
     * Null entries indicate empty slots.
     */
    public ItemStack[] itemStack;

    public Packet104WindowItems() {
    }

    /**
     * Constructs a Packet104WindowItems from a list of item stacks.
     *
     * @param windowId The window ID whose items are being sent
     * @param itemList List of item stacks (will be copied)
     */
    public Packet104WindowItems(int windowId, List<ItemStack> itemList) {
        this.windowId = windowId;
        this.itemStack = new ItemStack[itemList.size()];

        for (int i = 0; i < this.itemStack.length; ++i) {
            ItemStack stack = itemList.get(i);
            // Copy each stack to prevent external modifications
            this.itemStack[i] = stack == null ? null : stack.copy();
        }
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: window ID (byte), item count (short), and array of item stacks.
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.windowId = dataInputStream.readByte();

        // Read the number of slots
        short slotCount = dataInputStream.readShort();
        this.itemStack = new ItemStack[slotCount];

        for (int i = 0; i < slotCount; ++i) {
            short itemId = dataInputStream.readShort();
            if (itemId >= 0) {
                byte stackSize = dataInputStream.readByte();
                short damage = dataInputStream.readShort();
                this.itemStack[i] = new ItemStack(itemId, stackSize, damage);
            }
            // Null entries remain as null (empty slot)
        }
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: window ID (byte), item count (short), and array of item stacks.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.windowId);
        dataOutputStream.writeShort(this.itemStack.length);

        for (int i = 0; i < this.itemStack.length; ++i) {
            if (this.itemStack[i] == null) {
                // Write -1 to indicate empty slot
                dataOutputStream.writeShort(-1);
            } else {
                dataOutputStream.writeShort((short) this.itemStack[i].itemID);
                dataOutputStream.writeByte((byte) this.itemStack[i].stackSize);
                dataOutputStream.writeShort((short) this.itemStack[i].getItemDamage());
            }
        }
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleWindowItems(this);
    }

    /**
     * Returns the approximate size of this packet in bytes.
     *
     * @return Base size (3 bytes) plus item data size (5 bytes per slot)
     */
    public int getPacketSize() {
        return 3 + this.itemStack.length * 5;
    }
}
