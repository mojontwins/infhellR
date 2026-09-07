package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet 97: Set Inventory Slot.
 * <p>
 * Used by the creative-mode inventory (and the server) to force a new
 * ItemStack into a specific inventory slot. The packet carries the slot index,
 * the item id, the item amount, and the item damage value. The receiving side
 * (typically the player's open container) will overwrite the slot with the
 * new ItemStack, even if the player is in creative mode and the slot was
 * previously empty.
 */
public class Packet97SetInventorySlot extends Packet {
	/** The slot index to overwrite in the receiving inventory. */
	public int slot;

	/** The item id to place in the slot. */
	public short itemID;

	/** The item stack size. */
	public byte itemAmount;

	/** The item damage value. */
	public short itemDamage;

	public Packet97SetInventorySlot() {
	}

	/**
	 * Constructs a set-inventory-slot packet.
	 *
	 * @param slot        the slot index
	 * @param itemID      the item id
	 * @param itemAmount  the stack size
	 * @param itemDamage  the item damage value
	 */
	public Packet97SetInventorySlot(int slot, short itemID, byte itemAmount, short itemDamage) {
		this.slot = slot;
		this.itemID = itemID;
		this.itemAmount = itemAmount;
		this.itemDamage = itemDamage;
	}

	/**
	 * Reads the slot, item id, amount, and damage from the stream.
	 *
	 * @param dataInputStream the stream to read from
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.slot = dataInputStream.readInt();
		this.itemID = dataInputStream.readShort();
		this.itemAmount = dataInputStream.readByte();
		this.itemDamage = dataInputStream.readShort();
	}

	/**
	 * Writes the slot, item id, amount, and damage to the stream.
	 *
	 * @param dataOutputStream the stream to write to
	 * @throws IOException if the underlying stream fails
	 */
	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.slot);
		dataOutputStream.writeShort(this.itemID);
		dataOutputStream.writeByte(this.itemAmount);
		dataOutputStream.writeShort(this.itemDamage);
	}

	/**
	 * Dispatches this packet to {@link NetHandler#handleSetInventorySlot}.
	 *
	 * @param netHandler the receiving handler
	 */
	@Override
	public void processPacket(NetHandler netHandler) {
		netHandler.handleSetInventorySlot(this);
	}

	/**
	 * @return the number of bytes this packet occupies on the wire
	 *         (4 + 2 + 1 + 2 = 9 bytes)
	 */
	@Override
	public int getPacketSize() {
		return 9;
	}
}
