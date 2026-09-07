package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent to disconnect a player from the server, or vice versa.
 * This terminates the connection with a human-readable reason that is
 * displayed to the player (or logged on the server).
 *
 * <p>Maximum reason length: 100 characters.</p>
 */
public class Packet255KickDisconnect extends Packet {
    /**
     * The reason for disconnection, displayed as a chat message.
     * Typically localized (e.g., "You were kicked from the game").
     */
    public String reason;

    public Packet255KickDisconnect() {
    }

    /**
     * Constructs a Packet255KickDisconnect with the specified reason.
     *
     * @param disconnectReason The reason message (max 100 chars)
     */
    public Packet255KickDisconnect(String disconnectReason) {
        this.reason = disconnectReason;
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads: disconnect reason string (max 100 characters).
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.reason = readString(dataInputStream, 100);
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes: disconnect reason string.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        writeString(this.reason, dataOutputStream);
    }

    /**
     * Processes this packet by delegating to the network handler.
     * This triggers connection termination with the specified reason.
     *
     * @param netHandler The network handler to process this packet
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleKickDisconnect(this);
    }

    /**
     * Returns the approximate size of this packet in bytes.
     *
     * @return Length of the reason string
     */
    public int getPacketSize() {
        return this.reason.length();
    }
}
