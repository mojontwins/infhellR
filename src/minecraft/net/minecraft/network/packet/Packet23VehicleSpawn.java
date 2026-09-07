package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.network.NetHandler;

/**
 * Vehicle spawn packet - tells clients about vehicle entities (boats, minecarts).
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Spawn boats, minecarts, and other vehicle entities</li>
 *   <li>Can include thrower info if entity was thrown (snowballs, arrows)</li>
 *   <li>Velocity included for thrown entities</li>
 * </ul>
 * 
 * <p><b>Thrower velocity:</b> Encoded as short * 8000, clamped to [-3.9, 3.9] per axis.</p>
 * 
 * <p><b>Type values:</b></p>
 * <ul>
 *   <li>1 = Boat</li>
 *   <li>2 = Minecart (and variants via metadata)</li>
 *   <li>10 = Minecart with chest</li>
 *   <li>50 = Activated TNT (rare)</li>
 *   <li>60 = Arrow (for tracking)</li>
 *   <li>70 = Thrown snowball</li>
 *   <li>71 = Thrown egg</li>
 *   <li>90 = Thrown ender pearl</li>
 *   <li>91 = Fireball</li>
 * </ul>
 */
public class Packet23VehicleSpawn extends Packet {

    /** Vehicle entity ID */
    public int entityId;

    /** X coordinate (fixed-point: actual * 32) */
    public int xPosition;

    /** Y coordinate (fixed-point: actual * 32) */
    public int yPosition;

    /** Z coordinate (fixed-point: actual * 32) */
    public int zPosition;

    /** X motion encoded (only sent if throwerEntityId > 0) */
    public int motionXencoded;

    /** Y motion encoded (only sent if throwerEntityId > 0) */
    public int motionYencoded;

    /** Z motion encoded (only sent if throwerEntityId > 0) */
    public int motionZencoded;

    /** Vehicle type (1=boat, 2=minecart, 60=arrow, 70=snowball, etc.) */
    public int type;

    /** Thrower player entity ID (0 if not thrown) */
    public int throwerEntityId;

    /** Additional metadata (minecart type variant, etc.) */
    public int metadata;

    /**
     * Default constructor for deserialization.
     */
    public Packet23VehicleSpawn() {
    }

    /**
     * Creates a vehicle spawn packet (basic).
     * 
     * @param entity Vehicle entity
     * @param type   Vehicle type code
     */
    public Packet23VehicleSpawn(Entity entity, int type) {
        this(entity, type, 0);
    }

    /**
     * Creates a vehicle spawn packet with thrower.
     * 
     * @param entity          Vehicle entity
     * @param type            Vehicle type code
     * @param throwerEntityId Thrower player entity ID (0 if none)
     */
    public Packet23VehicleSpawn(Entity entity, int type, int throwerEntityId) {
        this(entity, type, throwerEntityId, 0);
    }

    /**
     * Creates a vehicle spawn packet with thrower and metadata.
     * 
     * @param entity          Vehicle entity
     * @param type            Vehicle type code
     * @param throwerEntityId Thrower player entity ID (0 if none)
     * @param metadata        Additional metadata (minecart variant, etc.)
     */
    public Packet23VehicleSpawn(Entity entity, int type, int throwerEntityId, int metadata) {
        this.entityId = entity.entityId;
        this.xPosition = MathHelper.floor_double(entity.posX * 32.0D);
        this.yPosition = MathHelper.floor_double(entity.posY * 32.0D);
        this.zPosition = MathHelper.floor_double(entity.posZ * 32.0D);
        this.type = type;
        this.throwerEntityId = throwerEntityId;

        // Include motion only if thrown by player
        if (throwerEntityId > 0) {
            // Clamp velocity to [-3.9, 3.9] per axis
            double clampedMotionX = entity.motionX;
            double clampedMotionY = entity.motionY;
            double clampedMotionZ = entity.motionZ;
            double maxSpeed = 3.9D;
            if (clampedMotionX < -maxSpeed) {
                clampedMotionX = -maxSpeed;
            }
            if (clampedMotionY < -maxSpeed) {
                clampedMotionY = -maxSpeed;
            }
            if (clampedMotionZ < -maxSpeed) {
                clampedMotionZ = -maxSpeed;
            }
            if (clampedMotionX > maxSpeed) {
                clampedMotionX = maxSpeed;
            }
            if (clampedMotionY > maxSpeed) {
                clampedMotionY = maxSpeed;
            }
            if (clampedMotionZ > maxSpeed) {
                clampedMotionZ = maxSpeed;
            }

            // Encode velocity as short (multiply by 8000)
            this.motionXencoded = (int) (clampedMotionX * 8000.0D);
            this.motionYencoded = (int) (clampedMotionY * 8000.0D);
            this.motionZencoded = (int) (clampedMotionZ * 8000.0D);
        }

        this.metadata = metadata;
    }

    /**
     * Reads vehicle spawn data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.type = dataInputStream.readByte();
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.readInt();
        this.zPosition = dataInputStream.readInt();
        this.throwerEntityId = dataInputStream.readInt();
        this.metadata = dataInputStream.readInt();

        // Motion only included if throwerEntityId > 0
        if (this.throwerEntityId > 0) {
            this.motionXencoded = dataInputStream.readShort();
            this.motionYencoded = dataInputStream.readShort();
            this.motionZencoded = dataInputStream.readShort();
        }
    }

    /**
     * Writes vehicle spawn data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        dataOutputStream.writeByte(this.type);
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.writeInt(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
        dataOutputStream.writeInt(this.throwerEntityId);
        dataOutputStream.writeInt(this.metadata);

        // Motion only sent if throwerEntityId > 0
        if (this.throwerEntityId > 0) {
            dataOutputStream.writeShort(this.motionXencoded);
            dataOutputStream.writeShort(this.motionYencoded);
            dataOutputStream.writeShort(this.motionZencoded);
        }
    }

    /**
     * Processes vehicle spawn packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleVehicleSpawn(this);
    }

    /**
     * Returns packet size.
     * 
     * @return Base 21 + 4 (thrower/metadata fields) + 6 if throwerEntityId > 0
     */
    public int getPacketSize() {
        return 21 + 4 + (this.throwerEntityId > 0 ? 6 : 0);
    }
}
