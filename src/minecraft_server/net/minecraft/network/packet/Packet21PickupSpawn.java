package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.network.NetHandler;

/**
 * Pickup spawn packet - tells clients about a dropped item entity.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Spawn dropped items in the world</li>
 *   <li>Initial position and velocity from item throw</li>
 *   <li>Includes item type, count, and damage for rendering</li>
 * </ul>
 * 
 * <p><b>Coordinate encoding:</b> Position multiplied by 32 (fixed-point).</p>
 * 
 * <p><b>Velocity encoding:</b> motionX/Y/Z multiplied by 128 (signed byte).</p>
 */
public class Packet21PickupSpawn extends Packet {

    /** Item entity ID */
    public int entityId;

    /** X coordinate (fixed-point: actual * 32) */
    public int xPosition;

    /** Y coordinate (fixed-point: actual * 32) */
    public int yPosition;

    /** Z coordinate (fixed-point: actual * 32) */
    public int zPosition;

    /** X-axis rotation/spin (encoded as byte from motion * 128) */
    public byte rotation;

    /** Y-axis rotation/spin (encoded as byte from motion * 128) */
    public byte pitch;

    /** Z-axis rotation/spin (encoded as byte from motion * 128) */
    public byte roll;

    /** Item type ID */
    public int itemID;

    /** Number of items in stack */
    public int count;

    /** Item damage value */
    public int itemDamage;

    /**
     * Default constructor for deserialization.
     */
    public Packet21PickupSpawn() {
    }

    /**
     * Creates a pickup spawn packet from an item entity.
     * 
     * @param entityItem The item entity to spawn
     */
    public Packet21PickupSpawn(EntityItem entityItem) {
        this.entityId = entityItem.entityId;
        this.itemID = entityItem.item.itemID;
        this.count = entityItem.item.stackSize;
        this.itemDamage = entityItem.item.getItemDamage();

        // Convert position to fixed-point
        this.xPosition = MathHelper.floor_double(entityItem.posX * 32.0D);
        this.yPosition = MathHelper.floor_double(entityItem.posY * 32.0D);
        this.zPosition = MathHelper.floor_double(entityItem.posZ * 32.0D);

        // Encode motion as byte (* 128, can be negative)
        this.rotation = (byte) ((int) (entityItem.motionX * 128.0D));
        this.pitch = (byte) ((int) (entityItem.motionY * 128.0D));
        this.roll = (byte) ((int) (entityItem.motionZ * 128.0D));
    }

    /**
     * Reads pickup spawn data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.itemID = dataInputStream.readShort();
        this.count = dataInputStream.readByte();
        this.itemDamage = dataInputStream.readShort();
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.readInt();
        this.zPosition = dataInputStream.readInt();
        this.rotation = dataInputStream.readByte();
        this.pitch = dataInputStream.readByte();
        this.roll = dataInputStream.readByte();
    }

    /**
     * Writes pickup spawn data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        dataOutputStream.writeShort(this.itemID);
        dataOutputStream.writeByte(this.count);
        dataOutputStream.writeShort(this.itemDamage);
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.writeInt(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
        dataOutputStream.writeByte(this.rotation);
        dataOutputStream.writeByte(this.pitch);
        dataOutputStream.writeByte(this.roll);
    }

    /**
     * Processes pickup spawn packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handlePickupSpawn(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 24 bytes
     */
    public int getPacketSize() {
        return 24;
    }
}
