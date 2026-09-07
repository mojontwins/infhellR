package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.entity.Entity;
import net.minecraft.network.NetHandler;

/**
 * Entity velocity packet - applies physics knockback/impulse to an entity.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Apply knockback when entity takes damage</li>
 *   <li>Set entity velocity after explosion</li>
 *   <li>Launch entity with specific velocity (pistons, etc.)</li>
 * </ul>
 * 
 * <p><b>Velocity encoding:</b> Double velocity clamped to [-3.9, 3.9], 
 * then multiplied by 8000 and stored as short.</p>
 * 
 * <p><b>Decoding:</b> velocity / 8000.0 = actual motion per tick</p>
 */
public class Packet28EntityVelocity extends Packet {

    /** Entity ID to apply velocity to */
    public int entityId;

    /** X velocity (encoded as short: actual = value / 8000.0) */
    public int motionX;

    /** Y velocity (encoded as short: actual = value / 8000.0) */
    public int motionY;

    /** Z velocity (encoded as short: actual = value / 8000.0) */
    public int motionZ;

    /**
     * Default constructor for deserialization.
     */
    public Packet28EntityVelocity() {
    }

    /**
     * Creates a velocity packet from an entity's current motion.
     * 
     * @param entity The entity to get velocity from
     */
    public Packet28EntityVelocity(Entity entity) {
        this(entity.entityId, entity.motionX, entity.motionY, entity.motionZ);
    }

    /**
     * Creates a velocity packet with explicit values.
     * 
     * @param entityId Entity ID
     * @param motionX  X velocity (double)
     * @param motionY  Y velocity (double)
     * @param motionZ  Z velocity (double)
     */
    public Packet28EntityVelocity(int entityId, double motionX, double motionY, double motionZ) {
        this.entityId = entityId;

        // Clamp velocity to [-3.9, 3.9] per axis
        double clampedX = motionX;
        double clampedY = motionY;
        double clampedZ = motionZ;
        double maxSpeed = 3.9D;

        if (clampedX < -maxSpeed) {
            clampedX = -maxSpeed;
        }
        if (clampedY < -maxSpeed) {
            clampedY = -maxSpeed;
        }
        if (clampedZ < -maxSpeed) {
            clampedZ = -maxSpeed;
        }
        if (clampedX > maxSpeed) {
            clampedX = maxSpeed;
        }
        if (clampedY > maxSpeed) {
            clampedY = maxSpeed;
        }
        if (clampedZ > maxSpeed) {
            clampedZ = maxSpeed;
        }

        // Encode as short (* 8000)
        this.motionX = (int) (clampedX * 8000.0D);
        this.motionY = (int) (clampedY * 8000.0D);
        this.motionZ = (int) (clampedZ * 8000.0D);
    }

    /**
     * Reads velocity data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.motionX = dataInputStream.readShort();
        this.motionY = dataInputStream.readShort();
        this.motionZ = dataInputStream.readShort();
    }

    /**
     * Writes velocity data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        dataOutputStream.writeShort(this.motionX);
        dataOutputStream.writeShort(this.motionY);
        dataOutputStream.writeShort(this.motionZ);
    }

    /**
     * Processes velocity packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleEntityVelocity(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 10 bytes
     */
    public int getPacketSize() {
        return 10;
    }
}
