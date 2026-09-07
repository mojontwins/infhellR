package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Player look + move packet - combined position and rotation update.
 * 
 * <p><b>Direction:</b> Both client and server.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Sent when player moves AND rotates in same tick</li>
 *   <li>Most common movement packet during gameplay</li>
 *   <li>Sets both moving and rotating flags to true</li>
 * </ul>
 * 
 * <p><b>Data order:</b> X, Y, stance, Z, yaw, pitch, onGround</p>
 * 
 * @see Packet10Flying
 * @see Packet11PlayerPosition
 * @see Packet12PlayerLook
 */
public class Packet13PlayerLookMove extends Packet10Flying {

    /**
     * Default constructor for deserialization.
     * Marks as both moving and rotating.
     */
    public Packet13PlayerLookMove() {
        this.rotating = true;
        this.moving = true;
    }

    /**
     * Creates a full look + move packet.
     * 
     * @param xPosition Player X coordinate
     * @param yPosition Player Y coordinate (feet)
     * @param stance    Player stance (eyes)
     * @param zPosition Player Z coordinate
     * @param yaw       Player yaw (degrees)
     * @param pitch     Player pitch (degrees)
     * @param onGround  True if player is on ground
     */
    public Packet13PlayerLookMove(double xPosition, double yPosition, double stance, double zPosition, float yaw, float pitch, boolean onGround) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.stance = stance;
        this.zPosition = zPosition;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.rotating = true;
        this.moving = true;
    }

    /**
     * Reads full position and rotation data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.xPosition = dataInputStream.readDouble();
        this.yPosition = dataInputStream.readDouble();
        this.stance = dataInputStream.readDouble();
        this.zPosition = dataInputStream.readDouble();
        this.yaw = dataInputStream.readFloat();
        this.pitch = dataInputStream.readFloat();
        // Read the on-ground flag from parent class
        super.readPacketData(dataInputStream);
    }

    /**
     * Writes full position and rotation data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeDouble(this.xPosition);
        dataOutputStream.writeDouble(this.yPosition);
        dataOutputStream.writeDouble(this.stance);
        dataOutputStream.writeDouble(this.zPosition);
        dataOutputStream.writeFloat(this.yaw);
        dataOutputStream.writeFloat(this.pitch);
        // Write the on-ground flag from parent class
        super.writePacketData(dataOutputStream);
    }

    /**
     * Returns packet size.
     * 
     * @return 41 bytes (4 doubles + 2 floats + 1 byte)
     */
    public int getPacketSize() {
        return 41;
    }
}
