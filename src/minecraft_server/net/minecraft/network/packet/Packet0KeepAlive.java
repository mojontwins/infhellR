package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Keep-alive packet used to verify the connection is still active.
 *
 * <p><b>Direction:</b> Server to client to solicit a reply; client to server as the reply.
 * This is the mechanism that guarantees the wire is never silent, which in turn is what
 * makes the 30 second socket read timeout in {@link net.minecraft.network.NetworkManager}
 * safe: as long as the tick loop is running, a keep-alive goes out at least once a second.</p>
 *
 * <p><b>Protocol (restored from vanilla 1.2.5, {@code net.minecraft.src.Packet0KeepAlive}):</b></p>
 * <ol>
 *     <li>Server sends this packet carrying a fresh random {@link #randomId} whenever
 *         the connection has been quiet for 20 ticks (1 second).</li>
 *     <li>Client echoes the same {@code randomId} straight back.</li>
 *     <li>Server compares the returned nonce against the one it sent and, on a match,
 *         folds the round-trip time into the player's smoothed ping.</li>
 * </ol>
 *
 * <p>The nonce is what distinguishes a live peer from a stale or spoofed reply: a
 * duplicate or delayed echo of an old id is ignored, so a connection that is
 * alive but lagging cannot be mistaken for one that has stopped responding.</p>
 *
 * <p><b>Packet size:</b> 4 bytes (a single int nonce).</p>
 *
 * @see NetHandler#handleKeepAlive(Packet0KeepAlive)
 */
public class Packet0KeepAlive extends Packet {

    /**
     * Random nonce identifying a specific keep-alive challenge. The server generates it
     * when it sends the packet; the client must return the exact same value.
     */
    public int randomId;

    /** Creates a keep-alive with a zero nonce. Used when decoding. */
    public Packet0KeepAlive() {
    }

    /**
     * Creates a keep-alive challenge carrying the given nonce.
     *
     * @param randomId the nonce to send; the peer is expected to echo it back unchanged
     */
    public Packet0KeepAlive(int randomId) {
        this.randomId = randomId;
    }

    /**
     * Dispatches the packet to the handler, which either echoes the nonce back
     * (client side) or validates it against the outstanding challenge (server side).
     *
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleKeepAlive(this);
    }

    /**
     * Reads the nonce from the wire.
     *
     * @param dataInputStream the input stream to read from
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.randomId = dataInputStream.readInt();
    }

    /**
     * Writes the nonce to the wire.
     *
     * @param dataOutputStream the output stream to write to
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.randomId);
    }

    /**
     * @return Always 4 for keep-alive packets (the int nonce)
     */
    public int getPacketSize() {
        return 4;
    }
}
