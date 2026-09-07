package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent to update a map item's data.
 * Used when a player holds a map and the explored area changes.
 * The server sends map texture data updates to the client for rendering.
 *
 * <p>This packet is marked as a chunk data packet due to its potentially
 * large payload size (map texture data can be substantial).</p>
 */
public class Packet131MapData extends Packet {
    /**
     * The item ID of the map (typically 358 for filled map in modern versions,
     * but the specific ID is sent in this packet).
     */
    public short itemID;

    /**
     * Unique identifier for the specific map instance.
     * Each map has a unique ID used to track which map this data belongs to.
     */
    public short uniqueID;

    /**
     * Raw map texture data bytes.
     * Contains color index values for the map pixels.
     */
    public byte[] itemData;

    public Packet131MapData() {
        this.isChunkDataPacket = true;
    }

    /**
     * Constructs a Packet131MapData with the specified map information.
     *
     * @param itemId The map item's item ID
     * @param mapId The unique map identifier
     * @param mapData The map's pixel data bytes
     */
    public Packet131MapData(short itemId, short mapId, byte[] mapData) {
        this.isChunkDataPacket = true;
        this.itemID = itemId;
        this.uniqueID = mapId;
        this.itemData = mapData;
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: item ID (short), map unique ID (short), data length (byte), and data bytes.
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.itemID = dataInputStream.readShort();
        this.uniqueID = dataInputStream.readShort();
        // Read data length as byte (& 255 to ensure unsigned interpretation)
        this.itemData = new byte[dataInputStream.readByte() & 255];
        dataInputStream.readFully(this.itemData);
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: item ID (short), map unique ID (short), data length (byte), and data bytes.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.itemID);
        dataOutputStream.writeShort(this.uniqueID);
        dataOutputStream.writeByte(this.itemData.length);
        dataOutputStream.write(this.itemData);
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleMapData(this);
    }

    /**
     * Returns the approximate size of this packet in bytes.
     *
     * @return Base size (4 bytes) plus the length of the map data
     */
    public int getPacketSize() {
        return 4 + this.itemData.length;
    }
}
