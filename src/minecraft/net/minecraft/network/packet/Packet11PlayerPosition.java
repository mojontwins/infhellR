package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Player position packet - full XYZ position update without rotation.
 * 
 * <p><b>Direction:</b> Both client and server.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Sent when player moves but doesn't change rotation</li>
 *   <li>More efficient than Packet13PlayerLookMove (less data)</li>
 *   <li>Server uses this to validate player position and update nearby entities</li>
 * </ul>
 * 
 * <p><b>Note:</b> Inherits xPosition, yPosition, zPosition, stance, onGround, moving, rotating
 * from Packet10Flying. Sets moving=true to indicate position data is valid.</p>
 * 
 * @see Packet10Flying
 * @see Packet12PlayerLook
 * @see Packet13PlayerLookMove
 */
public class Packet11PlayerPosition extends Packet10Flying {

    /**
     * Default constructor for deserialization.
     * Marks as moving (position fields populated).
     */
    public Packet11PlayerPosition() {
        this.moving = true;
    }

    /**
     * Creates a player position packet.
     * 
     * @param xPosition Player X coordinate
     * @param yPosition Player Y coordinate (feet)
     * @param stance    Player stance (eyes, usually yPosition + 1.62)
     * @param zPosition Player Z coordinate
     * @param onGround  True if player is on ground
     */
    public Packet11PlayerPosition(double xPosition, double yPosition, double stance, double zPosition, boolean onGround) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.stance = stance;
        this.zPosition = zPosition;
        this.onGround = onGround;
        this.moving = true;
    }

    /**
     * Reads position data from stream.
     * 
     * <p>Reads X, Y, stance, Z, then calls super.readPacketData for on-ground flag.</p>
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.xPosition = dataInputStream.readDouble();
        this.yPosition = dataInputStream.readDouble();
        this.stance = dataInputStream.readDouble();
        this.zPosition = dataInputStream.readDouble();
        // Read the on-ground flag from parent class
        super.readPacketData(dataInputStream);
    }

    /**
     * Writes position data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeDouble(this.xPosition);
        dataOutputStream.writeDouble(this.yPosition);
        dataOutputStream.writeDouble(this.stance);
        dataOutputStream.writeDouble(this.zPosition);
        // Write the on-ground flag from parent class
        super.writePacketData(dataOutputStream);
    }

    /**
     * Returns packet size.
     * 
     * @return 33 bytes (4 doubles + 1 byte)
     */
    public int getPacketSize() {
        return 33;
    }
}
