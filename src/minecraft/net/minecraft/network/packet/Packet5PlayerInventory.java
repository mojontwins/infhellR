package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;

/**
 * Player inventory packet - updates specific inventory slot for an entity.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Updates a single inventory slot for any entity (usually player)</li>
 *   <li>Used for hotbar updates, equipment changes, armor swaps</li>
 *   <li>Sent when inventory state changes that client needs to know about</li>
 * </ul>
 * 
 * <p><b>Note:</b> This is distinct from Packet103SetSlot which handles GUI window slots.
 * This packet is for the player's own inventory slots.</p>
 * 
 * @see Packet103SetSlot
 */
public class Packet5PlayerInventory extends Packet {

    /** Entity ID whose inventory is being updated */
    public int entityID;

    /** Slot index (0 = main inventory, 100+ = armor) */
    public int slot;

    /** Item ID (-1 if slot is empty) */
    public int itemID;

    /** Item damage value (for tools, wool color, etc.) */
    public int itemDamage;

    /**
     * Default constructor for deserialization.
     */
    public Packet5PlayerInventory() {
    }

    /**
     * Creates a player inventory update packet.
     * 
     * @param entityID Entity whose inventory changed
     * @param slot     Slot index
     * @param itemStack New item in slot (null for empty)
     */
    public Packet5PlayerInventory(int entityID, int slot, ItemStack itemStack) {
        this.entityID = entityID;
        this.slot = slot;

        // Encode null stack as -1 item ID
        if (itemStack == null) {
            this.itemID = -1;
            this.itemDamage = 0;
        } else {
            this.itemID = itemStack.itemID;
            this.itemDamage = itemStack.getItemDamage();
        }
    }

    /**
     * Reads inventory update from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityID = dataInputStream.readInt();
        this.slot = dataInputStream.readShort();
        this.itemID = dataInputStream.readShort();
        this.itemDamage = dataInputStream.readShort();
    }

    /**
     * Writes inventory update to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityID);
        dataOutputStream.writeShort(this.slot);
        dataOutputStream.writeShort(this.itemID);
        dataOutputStream.writeShort(this.itemDamage);
    }

    /**
     * Processes inventory update packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handlePlayerInventory(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 8 bytes
     */
    public int getPacketSize() {
        return 8;
    }
}
