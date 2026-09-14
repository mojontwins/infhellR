package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Position packet - detailed player movement state for server-side validation.
 * 
 * <p><b>Direction:</b> Client -> Server only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Detailed player movement state (strafing, forward motion)</li>
 *   <li>Used for anti-cheat validation</li>
 *   <li>More detailed than standard position packets</li>
 *   <li>Server can verify player isn't moving too fast</li>
 * </ul>
 * 
 * <p><b>Note:</b> This is an extended movement packet, not part of the
 * standard protocol. Used for server-side movement validation.</p>
 */
public class Packet27Position extends Packet {

    /** Strafe movement input (-1.0 to 1.0) */
    private float strafeMovement;

    /** Forward movement input (-1.0 to 1.0) */
    private float forwardMovement;

    /** True if player is sneaking */
    private boolean isSneaking;

    /** True if player is jumping */
    private boolean isInJump;

    /** Player pitch rotation */
    private float pitchRotation;

    /** Player yaw rotation */
    private float yawRotation;

    /**
     * Reads position data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.strafeMovement = dataInputStream.readFloat();
        this.forwardMovement = dataInputStream.readFloat();
        this.pitchRotation = dataInputStream.readFloat();
        this.yawRotation = dataInputStream.readFloat();
        this.isSneaking = dataInputStream.readBoolean();
        this.isInJump = dataInputStream.readBoolean();
    }

    /**
     * Writes position data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeFloat(this.strafeMovement);
        dataOutputStream.writeFloat(this.forwardMovement);
        dataOutputStream.writeFloat(this.pitchRotation);
        dataOutputStream.writeFloat(this.yawRotation);
        dataOutputStream.writeBoolean(this.isSneaking);
        dataOutputStream.writeBoolean(this.isInJump);
    }

    /**
     * Processes position packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handlePosition(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 18 bytes
     */
    public int getPacketSize() {
        return 18;
    }

    /**
     * Gets the strafe movement value.
     * 
     * @return Strafe input (-1.0 to 1.0)
     */
    public float getStrafeMovement() {
        return this.strafeMovement;
    }

    /**
     * Gets the pitch rotation.
     * 
     * @return Pitch in degrees
     */
    public float getPitchRotation() {
        return this.pitchRotation;
    }

    /**
     * Gets the forward movement value.
     * 
     * @return Forward input (-1.0 to 1.0)
     */
    public float getForwardMovement() {
        return this.forwardMovement;
    }

    /**
     * Gets the yaw rotation.
     * 
     * @return Yaw in degrees
     */
    public float getYawRotation() {
        return this.yawRotation;
    }

    /**
     * Checks if player is sneaking.
     * 
     * @return True if sneaking
     */
    public boolean isSneaking() {
        return this.isSneaking;
    }

    /**
     * Checks if player is jumping.
     * 
     * @return True if jumping
     */
    public boolean isInJump() {
        return this.isInJump;
    }

    /**
     * Sets the strafe movement input.
     * 
     * @param strafeMovement Strafe input (-1.0 to 1.0)
     */
    public void setStrafeMovement(float strafeMovement) {
        this.strafeMovement = strafeMovement;
    }

    /**
     * Sets the forward movement input.
     * 
     * @param forwardMovement Forward input (-1.0 to 1.0)
     */
    public void setForwardMovement(float forwardMovement) {
        this.forwardMovement = forwardMovement;
    }

    /**
     * Sets the sneaking flag.
     * 
     * @param isSneaking True if player is sneaking
     */
    public void setSneaking(boolean isSneaking) {
        this.isSneaking = isSneaking;
    }

    /**
     * Sets the jumping flag.
     * 
     * @param isInJump True if player is jumping
     */
    public void setInJump(boolean isInJump) {
        this.isInJump = isInJump;
    }

    /**
     * Sets the pitch rotation.
     * 
     * @param pitchRotation Pitch in degrees
     */
    public void setPitchRotation(float pitchRotation) {
        this.pitchRotation = pitchRotation;
    }

    /**
     * Sets the yaw rotation.
     * 
     * @param yawRotation Yaw in degrees
     */
    public void setYawRotation(float yawRotation) {
        this.yawRotation = yawRotation;
    }
}
