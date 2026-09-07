package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;

/**
 * Packet sent when a player clicks on a slot in an open window.
 * This handles all mouse interactions including left-click, right-click,
 * shift-click, and dragging items between slots.
 *
 * <p>Packet flow:
 * <ol>
 *   <li>Client sends click info including slot, mouse button, and held item</li>
 *   <li>Server processes the action and validates the transaction</li>
 *   <li>Server responds with Packet106Transaction (accept/reject)</li>
 *   <li>If accepted, server may send Packet103SetSlot or Packet104WindowItems</li>
 * </ol>
 *
 * @see Packet100OpenWindow
 * @see Packet101CloseWindow
 * @see Packet103SetSlot
 * @see Packet104WindowItems
 * @see Packet106Transaction
 */
public class Packet102WindowClick extends Packet {
    /** Unique identifier for the window being interacted with */
    public int window_Id;

    /** The slot index that was clicked (-999 for outside/creativeGive/drop) */
    public int inventorySlot;

    /** Mouse button used: 0 = left click, 1 = right click */
    public int mouseClick;

    /**
     * Action number for transaction tracking.
     * Incremented with each click, must be echoed in the response packet.
     */
    public short action;

    /**
     * The ItemStack being held/placed during this click operation.
     * Null if the clicked slot was empty.
     */
    public ItemStack itemStack;

    /**
     * Whether shift is held during the click (shift-click for quick move).
     * This modifies the behavior: shift+click moves items to/from hotbar/inventory.
     */
    public boolean packetBoolean;

    public Packet102WindowClick() {
    }

    /**
     * Constructs a Packet102WindowClick with all click parameters.
     *
     * @param windowId Window being interacted with
     * @param inventorySlot Slot that was clicked
     * @param mouseClick Mouse button used (0=left, 1=right)
     * @param holdingShift Whether shift key was held
     * @param itemStack The item being held during the click
     * @param action Action number for transaction tracking
     */
    public Packet102WindowClick(int windowId, int inventorySlot, int mouseClick, boolean holdingShift, ItemStack itemStack, short action) {
        this.window_Id = windowId;
        this.inventorySlot = inventorySlot;
        this.mouseClick = mouseClick;
        this.itemStack = itemStack;
        this.action = action;
        this.packetBoolean = holdingShift;
    }

    /**
     * Processes this packet on the server side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleWindowClick(this);
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: window ID, slot, mouse button, action, holding shift flag,
     * and optionally item data (item ID, stack size, damage).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.window_Id = dataInputStream.readByte();
        this.inventorySlot = dataInputStream.readShort();
        this.mouseClick = dataInputStream.readByte();
        this.action = dataInputStream.readShort();
        this.packetBoolean = dataInputStream.readBoolean();

        // Read item data: -1 indicates no item (empty slot)
        short itemId = dataInputStream.readShort();
        if (itemId >= 0) {
            byte stackSize = dataInputStream.readByte();
            short damage = dataInputStream.readShort();
            this.itemStack = new ItemStack(itemId, stackSize, damage);
        } else {
            this.itemStack = null;
        }
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: window ID, slot, mouse button, action, holding shift flag,
     * and optionally item data.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.window_Id);
        dataOutputStream.writeShort(this.inventorySlot);
        dataOutputStream.writeByte(this.mouseClick);
        dataOutputStream.writeShort(this.action);
        dataOutputStream.writeBoolean(this.packetBoolean);

        // Write item data: -1 indicates no item
        if (this.itemStack == null) {
            dataOutputStream.writeShort(-1);
        } else {
            dataOutputStream.writeShort(this.itemStack.itemID);
            dataOutputStream.writeByte(this.itemStack.stackSize);
            dataOutputStream.writeShort(this.itemStack.getItemDamage());
        }
    }

    /**
     * Returns the approximate size of this packet in bytes.
     *
     * @return Always returns 11 bytes
     */
    public int getPacketSize() {
        return 11;
    }
}
