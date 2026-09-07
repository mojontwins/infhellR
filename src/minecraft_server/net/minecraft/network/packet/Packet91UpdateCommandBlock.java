package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent to update the command stored in a command block.
 * When a player uses a command block GUI to set a command, this packet
 * is sent to the server which then updates the block entity in the world.
 *
 * <p>This packet is marked as a chunk data packet since it modifies
 * block entity data that needs synchronization.</p>
 */
public class Packet91UpdateCommandBlock extends Packet {
    /** X coordinate of the command block in world space */
    public int x;

    /** Y coordinate of the command block in world space */
    public int y;

    /** Z coordinate of the command block in world space */
    public int z;

    /**
     * The command string to be executed when the command block is activated.
     * Maximum length: 32767 characters.
     */
    public String command;

    public Packet91UpdateCommandBlock() {
        this.isChunkDataPacket = true;
    }

    /**
     * Constructs a Packet91UpdateCommandBlock with position and command data.
     *
     * @param x X coordinate of the command block
     * @param y Y coordinate of the command block
     * @param z Z coordinate of the command block
     * @param command The command to store in the block
     */
    public Packet91UpdateCommandBlock(int x, int y, int z, String command) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isChunkDataPacket = true;
        this.command = command;
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: X (int), Y (short), Z (int), and command string (max 32767 chars).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.x = dataInputStream.readInt();
        this.y = dataInputStream.readShort();
        this.z = dataInputStream.readInt();
        this.command = Packet.readString(dataInputStream, 32767);
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: X (int), Y (short), Z (int), and command string.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(x);
        dataOutputStream.writeShort(y);
        dataOutputStream.writeInt(z);
        Packet.writeString(this.command, dataOutputStream);
    }

    /**
     * Processes this packet by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    @Override
    public void processPacket(NetHandler netHandler) {
        netHandler.handleUpdateCommandBlock(this);
    }

    /**
     * Returns the approximate size of this packet in bytes.
     * Includes fixed size (10 bytes) plus command string length.
     *
     * @return Total size in bytes
     */
    @Override
    public int getPacketSize() {
        return 4 + 2 + 4 + 2 + this.command.length();
    }
}
