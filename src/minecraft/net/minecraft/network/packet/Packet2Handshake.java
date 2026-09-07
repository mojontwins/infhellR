package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Handshake packet - initiates connection and determines login vs play state.
 * 
 * <p><b>Direction:</b> Both client and server can send.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>First packet sent when connecting</li>
 *   <li>Server sends username ("ENFORCED" for online mode)</li>
 *   <li>Determines whether to enter login or play state</li>
 * </ul>
 * 
 * <p><b>Protocol flow:</b></p>
 * <ol>
 *   <li>Client connects to server</li>
 *   <li>Client sends handshake with username</li>
 *   <li>Server responds with handshake containing "ENFORCED" (online) or username (offline)</li>
 *   <li>Client proceeds to login packet or play state</li>
 * </ol>
 * 
 * @see Packet1Login
 */
public class Packet2Handshake extends Packet {

    /** Player's username (or server response string) */
    public String username;

    /**
     * Default constructor for deserialization.
     */
    public Packet2Handshake() {
    }

    /**
     * Creates a handshake packet.
     * 
     * @param username The username to send
     */
    public Packet2Handshake(String username) {
        this.username = username;
    }

    /**
     * Reads username from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.username = readString(dataInputStream, 32);
    }

    /**
     * Writes username to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        writeString(this.username, dataOutputStream);
    }

    /**
     * Processes handshake packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleHandshake(this);
    }

    /**
     * Returns packet size for statistics.
     * 
     * @return Size in bytes
     */
    public int getPacketSize() {
        return 4 + this.username.length() + 4;
    }
}
