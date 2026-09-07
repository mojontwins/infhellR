package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.Entity;
import net.minecraft.network.NetHandler;

/**
 * Animation packet - triggers entity animation.
 * 
 * <p><b>Direction:</b> Both client and server.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Player swings arm to attack</li>
 *   <li>Player takes damage (hurt animation)</li>
 *   <li>Entity performs various visual animations</li>
 *   <li>Server broadcasts to all nearby clients</li>
 * </ul>
 * 
 * <p><b>Animation values:</b></p>
 * <ul>
 *   <li>0 = No animation</li>
 *   <li>1 = Swing arm (attack)</li>
 *   <li>2 = Damage (hurt animation)</li>
 *   <li>3 = Leave bed</li>
 *   <li>4 = Crouch (start)</li>
 *   <li>5 = Uncrouch (stop)</li>
 *   <li>104 = Crouch (alternative, Beta+)</li>
 *   <li>105 = Uncrouch (alternative, Beta+)</li>
 * </ul>
 */
public class Packet18Animation extends Packet {

    /** Entity performing the animation */
    public int entityId;

    /** Animation type code (see class javadoc) */
    public int animate;

    /**
     * Default constructor for deserialization.
     */
    public Packet18Animation() {
    }

    /**
     * Creates an animation packet.
     * 
     * @param entity  Entity performing animation
     * @param animate Animation type code
     */
    public Packet18Animation(Entity entity, int animate) {
        this.entityId = entity.entityId;
        this.animate = animate;
    }

    /**
     * Reads animation data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.animate = dataInputStream.readByte();
    }

    /**
     * Writes animation data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        dataOutputStream.writeByte(this.animate);
    }

    /**
     * Processes animation packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleAnimation(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 5 bytes
     */
    public int getPacketSize() {
        return 5;
    }
}
