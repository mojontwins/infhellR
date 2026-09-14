package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;

/**
 * Place packet - player places a block/item in the world.
 * 
 * <p><b>Direction:</b> Client -> Server only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Player right-clicks to place a block</li>
 *   <li>Player right-clicks to use an item (food, bow, etc.)</li>
 *   <li>Server validates placement and creates the block/entity</li>
 * </ul>
 * 
 * <p><b>Position data:</b></p>
 * <ul>
 *   <li>X, Z as int (full world coordinates)</li>
 *   <li>Y as unsigned byte (0-255)</li>
 *   <li>Direction as byte (face clicked)</li>
 *   <li>X/Y/Z offset within face (for slabs, fences, etc.)</li>
 * </ul>
 * 
 * <p><b>Note:</b> For block placement at -1,-1,-1 (outside world bounds),
 * this signals item use rather than block placement.</p>
 */
public class Packet15Place extends Packet {

    /** Block X coordinate (int) */
    public int xPosition;

    /** Block Y coordinate (byte) */
    public int yPosition;

    /** Block Z coordinate (int) */
    public int zPosition;

    /** Block face direction (0=bottom, 1=top, 2=north, 3=south, 4=west, 5=east) */
    public int direction;

    /** Item stack being placed/used (null if empty hand) */
    public ItemStack itemStack;

    /** X offset within clicked face (0.0-1.0) */
    public float xWithinFace;

    /** Y offset within clicked face (0.0-1.0) */
    public float yWithinFace;

    /** Z offset within clicked face (0.0-1.0) */
    public float zWithinFace;

    /** Player sneak state at time of click (for sneaking placement) */
    public byte shift;

    /**
     * Default constructor for deserialization.
     */
    public Packet15Place() {
    }

    /**
     * Creates a place packet.
     * 
     * @param xPosition    Block X coordinate
     * @param yPosition    Block Y coordinate
     * @param zPosition    Block Z coordinate
     * @param direction    Face direction
     * @param itemStack    Item being placed
     * @param xWithinFace  X offset within face
     * @param yWithinFace  Y offset within face
     * @param zWithinFace  Z offset within face
     * @param shift        Sneak state
     */
    public Packet15Place(int xPosition, int yPosition, int zPosition, int direction, ItemStack itemStack, float xWithinFace, float yWithinFace, float zWithinFace, byte shift) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.zPosition = zPosition;
        this.direction = direction;
        this.itemStack = itemStack;
        this.xWithinFace = xWithinFace;
        this.yWithinFace = yWithinFace;
        this.zWithinFace = zWithinFace;
        this.shift = shift;
    }

    /**
     * Reads place data from stream.
     * 
     * <p>Reads position, direction, offsets, then item stack data.</p>
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.read();
        this.zPosition = dataInputStream.readInt();
        this.direction = dataInputStream.read();
        this.xWithinFace = dataInputStream.readFloat();
        this.yWithinFace = dataInputStream.readFloat();
        this.zWithinFace = dataInputStream.readFloat();
        this.shift = dataInputStream.readByte();

        // Read item stack (-1 ID = no item)
        short itemID = dataInputStream.readShort();
        if (itemID >= 0) {
            byte stackSize = dataInputStream.readByte();
            short itemDamage = dataInputStream.readShort();
            this.itemStack = new ItemStack(itemID, stackSize, itemDamage);
        } else {
            this.itemStack = null;
        }
    }

    /**
     * Writes place data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.write(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
        dataOutputStream.write(this.direction);
        dataOutputStream.writeFloat(this.xWithinFace);
        dataOutputStream.writeFloat(this.yWithinFace);
        dataOutputStream.writeFloat(this.zWithinFace);
        dataOutputStream.writeByte(this.shift);

        // Write item stack (null = -1)
        if (this.itemStack == null) {
            dataOutputStream.writeShort(-1);
        } else {
            dataOutputStream.writeShort(this.itemStack.itemID);
            dataOutputStream.writeByte(this.itemStack.stackSize);
            dataOutputStream.writeShort(this.itemStack.getItemDamage());
        }
    }

    /**
     * Processes place packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handlePlace(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 28 bytes (position + direction + offsets + shift + item data)
     */
    public int getPacketSize() {
        return 15 + 12 + 1;
    }
}
