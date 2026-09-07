package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent to update the text on a placed sign in the world.
 * When a player edits a sign and presses "Done", this packet is sent to
 * notify the server, which then broadcasts the updated text to all nearby players.
 *
 * <p>This packet is marked as a chunk data packet since it affects
 * world block entity data that may need special handling during chunk loading.</p>
 */
public class Packet130UpdateSign extends Packet {
    /** X coordinate of the sign in world space */
    public int xPosition;

    /** Y coordinate of the sign in world space */
    public int yPosition;

    /** Z coordinate of the sign in world space */
    public int zPosition;

    /**
     * The text lines on the sign.
     * Always 4 lines, each up to 15 characters in vanilla (longer in modded).
     */
    public String[] signLines;

    public Packet130UpdateSign() {
        this.isChunkDataPacket = true;
    }

    /**
     * Constructs a Packet130UpdateSign with position and text data.
     *
     * @param x X coordinate of the sign
     * @param y Y coordinate of the sign
     * @param z Z coordinate of the sign
     * @param signLines The 4 text lines for the sign
     */
    public Packet130UpdateSign(int x, int y, int z, String[] signLines) {
        this.isChunkDataPacket = true;
        this.xPosition = x;
        this.yPosition = y;
        this.zPosition = z;
        this.signLines = signLines;
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: X (int), Y (short), Z (int), and 4 sign text lines (strings up to 15 chars each).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.readShort();
        this.zPosition = dataInputStream.readInt();

        this.signLines = new String[4];
        for (int i = 0; i < 4; ++i) {
            this.signLines[i] = readString(dataInputStream, 15);
        }
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: X (int), Y (short), Z (int), and 4 sign text lines.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.writeShort(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);

        for (int i = 0; i < 4; ++i) {
            writeString(this.signLines[i], dataOutputStream);
        }
    }

    /**
     * Processes this packet on the receiving side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleUpdateSign(this);
    }

    /**
     * Returns the approximate size of this packet in bytes.
     * Calculated as the sum of all sign line lengths.
     *
     * @return Total length of all sign text lines
     */
    public int getPacketSize() {
        int totalSize = 0;
        for (int i = 0; i < 4; ++i) {
            totalSize += this.signLines[i].length();
        }
        return totalSize;
    }
}
