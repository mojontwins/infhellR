package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent to transmit a custom payload between client and server.
 * Used by mods to send arbitrary data over the Minecraft protocol
 * without requiring a custom packet class.
 *
 * <p>The channel name identifies the mod/plugin that owns the payload,
 * and the data field contains the custom byte data to be processed.
 * Maximum payload size is 32K bytes.</p>
 *
 * <p>Common uses include:
 * <ul>
 *   <li>Mod-specific synchronization (energy, mana, etc.)</li>
 *   <li>Plugin communication</li>
 *   <li>Custom GUI data</li>
 * </ul>
 */
public class Packet250CustomPayload extends Packet {
    /**
     * The channel/identifier for this payload.
     * Typically namespaced like "mymod:mymessage" or just a short identifier.
     * Maximum length: 20 characters.
     */
    public String channel;

    /** Length of the data payload in bytes */
    public int length;

    /**
     * The custom payload data.
     * Format depends entirely on the channel's specification.
     */
    public byte[] data;

    public Packet250CustomPayload() {
    }

    /**
     * Constructs a Packet250CustomPayload with channel and data.
     *
     * @param channel The channel identifier (max 20 chars)
     * @param data The payload bytes
     * @throws IllegalArgumentException if data exceeds 32K bytes
     */
    public Packet250CustomPayload(String channel, byte[] data) {
        this.channel = channel;
        this.data = data;

        if (data != null) {
            this.length = data.length;
            // Enforce maximum payload size
            if (this.length > 32767) {
                throw new IllegalArgumentException("Payload too big (max 32K)");
            }
        }
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: channel name (string, max 20 chars), data length (short), and data bytes.
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.channel = readString(dataInputStream, 20);
        this.length = dataInputStream.readShort();

        // Read data only if valid length
        if (this.length > 0 && this.length < 32767) {
            this.data = new byte[this.length];
            dataInputStream.readFully(this.data);
        }
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: channel name, data length, and data bytes.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        writeString(this.channel, dataOutputStream);
        dataOutputStream.writeShort((short) this.length);

        if (this.data != null) {
            dataOutputStream.write(this.data);
        }
    }

    /**
     * Processes this packet by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    @Override
    public void processPacket(NetHandler netHandler) {
        netHandler.handleCustomPayload(this);
    }

    /**
     * Returns the approximate size of this packet in bytes.
     * Includes channel name (UTF-16 = 2 bytes per char) plus data length.
     *
     * @return Total size in bytes
     */
    @Override
    public int getPacketSize() {
        return 2 + this.channel.length() * 2 + 2 + this.length;
    }
}
