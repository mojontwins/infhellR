package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;

/**
 * Packet sent when a player uses the creative inventory to place or take an item.
 * Unlike Packet102WindowClick (used for survival inventory), this packet is used
 * specifically in creative mode where items can be freely placed/removed.
 *
 * <p>This packet uses the helper methods Packet.readItemStack and Packet.writeItemStack
 * for reading/writing item data.</p>
 *
 * @see Packet102WindowClick
 * @see Packet103SetSlot
 */
public class Packet107CreativeSetSlot extends Packet {
    /**
     * The slot index being modified.
     * In creative mode: 0-8 = hotbar, 9-35 = main inventory.
     * -1 = cursor/temporary slot.
     */
    public int slot;

    /**
     * The item stack in this slot.
     * Null if the slot is empty.
     */
    public ItemStack itemStack;

    public Packet107CreativeSetSlot() {
    }

    /**
     * Constructs a Packet107CreativeSetSlot for a creative inventory action.
     *
     * @param slot The slot being modified
     * @param itemStack The item to place (null for empty)
     */
    public Packet107CreativeSetSlot(int slot, ItemStack itemStack) {
        this.slot = slot;
        this.itemStack = itemStack;
    }

    /**
     * Processes this packet on the server side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleCreativeSetSlot(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Uses Packet.readItemStack for standardized item deserialization.
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.slot = dataInputStream.readShort();
        this.itemStack = Packet.readItemStack(dataInputStream);
    }

    /**
     * Writes this packet's data to the output stream.
     * Uses Packet.writeItemStack for standardized item serialization.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.slot);
        Packet.writeItemStack(this.itemStack, dataOutputStream);
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
