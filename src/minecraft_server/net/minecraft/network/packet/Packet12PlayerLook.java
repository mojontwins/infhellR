package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Player look packet - rotation update without position change.
 * 
 * <p><b>Direction:</b> Both client and server.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Sent when player rotates but doesn't move</li>
 *   <li>More efficient than Packet13PlayerLookMove</li>
 *   <li>Server uses this to update other clients viewing this player</li>
 * </ul>
 * 
 * <p><b>Note:</b> Inherits from Packet10Flying. Sets rotating=true to indicate rotation data valid.</p>
 * 
 * @see Packet10Flying
 * @see Packet11PlayerPosition
 * @see Packet13PlayerLookMove
 */
public class Packet12PlayerLook extends Packet10Flying {

    /**
     * Default constructor for deserialization.
     * Marks as rotating (yaw/pitch fields populated).
     */
    public Packet12PlayerLook() {
        this.rotating = true;
    }

    /**
     * Creates a player look packet.
     * 
     * @param yaw      Player yaw (degrees)
     * @param pitch    Player pitch (degrees)
     * @param onGround True if player is on ground
     */
    public Packet12PlayerLook(float yaw, float pitch, boolean onGround) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.rotating = true;
    }

    /**
     * Reads rotation data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.yaw = dataInputStream.readFloat();
        this.pitch = dataInputStream.readFloat();
        // Read the on-ground flag from parent class
        super.readPacketData(dataInputStream);
    }

    /**
     * Writes rotation data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeFloat(this.yaw);
        dataOutputStream.writeFloat(this.pitch);
        // Write the on-ground flag from parent class
        super.writePacketData(dataOutputStream);
    }

    /**
     * Returns packet size.
     * 
     * @return 9 bytes (2 floats + 1 byte)
     */
    public int getPacketSize() {
        return 9;
    }
}
