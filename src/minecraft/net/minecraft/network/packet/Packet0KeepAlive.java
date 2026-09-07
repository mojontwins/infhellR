package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Keep-alive packet used to verify the connection is still active.
 * 
 * <p><b>Direction:</b> Both client and server can send this packet.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Maintains connection alive through NATs and proxies</li>
 *   <li>Detects dead connections without waiting for TCP timeout</li>
 *   <li>No data is carried - just presence of the packet indicates liveness</li>
 * </ul>
 * 
 * <p><b>Protocol:</b></p>
 * <ul>
 *   <li>Server periodically sends to all connected clients</li>
 *   <li>Client responds with its own keep-alive</li>
 *   <li>If no keep-alive received for timeout period, connection is terminated</li>
 * </ul>
 * 
 * <p><b>Packet size:</b> 0 bytes (no payload)</p>
 * 
 * @see NetHandler#handleLogin(Packet1Login)
 */
public class Packet0KeepAlive extends Packet {

    /**
     * Processes this keep-alive packet.
     * 
     * <p>In vanilla protocol, the handler might track last ping time here.
     * The actual timeout detection is typically done at a higher level.</p>
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        // Keep-alive acknowledgment - connection is alive
        // In some implementations, this might update a "last ping" timestamp
    }

    /**
     * Reads packet data (no data to read for keep-alive).
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        // No data to read - keep-alive has no payload
    }

    /**
     * Writes packet data (no data to write for keep-alive).
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        // No data to write - keep-alive has no payload
    }

    /**
     * Returns the size of this packet in bytes.
     * 
     * @return Always 0 for keep-alive packets
     */
    public int getPacketSize() {
        return 0;
    }
}
