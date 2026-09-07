package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Flying packet - base class for player position/rotation updates.
 * 
 * <p><b>Direction:</b> Both client and server.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Base class for player movement packets (11, 12, 13)</li>
 *   <li>This base packet carries only on-ground state</li>
 *   <li>Subclasses add position and/or rotation data</li>
 * </ul>
 * 
 * <p><b>Subclasses:</b></p>
 * <ul>
 *   <li>{@link Packet11PlayerPosition} - position only</li>
 *   <li>{@link Packet12PlayerLook} - rotation only</li>
 *   <li>{@link Packet13PlayerLookMove} - both position and rotation</li>
 * </ul>
 * 
 * <p><b>State flags:</b> moving, rotating indicate which subclass fields are populated.</p>
 */
public class Packet10Flying extends Packet {

    /** Player X position */
    public double xPosition;

    /** Player Y position (feet) */
    public double yPosition;

    /** Player Z position */
    public double zPosition;

    /** Player stance (Y position of eyes, = yPosition + 1.62 typically) */
    public double stance;

    /** Player yaw rotation (degrees, 0=south, increasing clockwise) */
    public float yaw;

    /** Player pitch rotation (degrees, -90=up, 90=down) */
    public float pitch;

    /** True if player is on ground */
    public boolean onGround;

    /** True if position fields are populated (set by position subclass) */
    public boolean moving;

    /** True if rotation fields are populated (set by look subclass) */
    public boolean rotating;

    /**
     * Default constructor for deserialization.
     */
    public Packet10Flying() {
    }

    /**
     * Creates a minimal flying packet with on-ground state only.
     * 
     * @param onGround True if player is on ground
     */
    public Packet10Flying(boolean onGround) {
        this.onGround = onGround;
    }

    /**
     * Processes flying packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleFlying(this);
    }

    /**
     * Reads on-ground flag from stream.
     * 
     * <p>Subclasses override to read additional fields and call super.readPacketData
     * for the on-ground flag at the end.</p>
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.onGround = dataInputStream.read() != 0;
    }

    /**
     * Writes on-ground flag to stream.
     * 
     * <p>Subclasses override to write additional fields and call super.writePacketData
     * for the on-ground flag at the end.</p>
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(this.onGround ? 1 : 0);
    }

    /**
     * Returns packet size.
     * 
     * @return 1 byte (on-ground flag only)
     */
    public int getPacketSize() {
        return 1;
    }
}
