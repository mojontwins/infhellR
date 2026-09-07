package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Chat message packet - sends player chat between client and server.
 * 
 * <p><b>Direction:</b> Both client and server.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Client -> Server: player sent a chat message</li>
 *   <li>Server -> Client: server is broadcasting a chat message to player</li>
 *   <li>Server can also broadcast system messages, player join/leave notices</li>
 * </ul>
 * 
 * <p><b>Length limit:</b> 119 characters maximum (older Minecraft client limit)</p>
 * 
 * <p><b>Note:</b> Modern clients may not display messages longer than this.
 * The constructor truncates overlong messages to prevent protocol errors.</p>
 * 
 * @see Packet3Chat for the message class itself
 */
public class Packet3Chat extends Packet {

    /** The chat message text to send/receive (max 119 chars) */
    public String message;

    /**
     * Default constructor for deserialization.
     */
    public Packet3Chat() {
    }

    /**
     * Creates a chat packet, truncating messages longer than 119 chars.
     * 
     * @param message The chat message
     */
    public Packet3Chat(String message) {
        // Truncate to fit older protocol limits
        if (message.length() > 119) {
            message = message.substring(0, 119);
        }

        this.message = message;
    }

    /**
     * Reads chat message from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.message = readString(dataInputStream, 119);
    }

    /**
     * Writes chat message to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        writeString(this.message, dataOutputStream);
    }

    /**
     * Processes chat packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleChat(this);
    }

    /**
     * Returns packet size.
     * 
     * @return Size in bytes
     */
    public int getPacketSize() {
        return this.message.length();
    }
}
