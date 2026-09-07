package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;

/**
 * Named entity spawn packet - tells clients about a new player entity.
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Introduce other players when they enter visible range</li>
 *   <li>Re-introduce players after respawn/dimension change</li>
 *   <li>Include username for player name rendering</li>
 *   <li>Include current held item for hand rendering</li>
 * </ul>
 * 
 * <p><b>Coordinate encoding:</b> Position multiplied by 32 and floored (fixed-point).</p>
 * 
 * <p><b>Rotation encoding:</b> Yaw/pitch in degrees * 256/360, truncated to byte.</p>
 */
public class Packet20NamedEntitySpawn extends Packet {

    /** New player entity ID */
    public int entityId;

    /** Player username (max 16 chars) */
    public String name;

    /** Player X coordinate (fixed-point: actual * 32) */
    public int xPosition;

    /** Player Y coordinate (fixed-point: actual * 32) */
    public int yPosition;

    /** Player Z coordinate (fixed-point: actual * 32) */
    public int zPosition;

    /** Player yaw rotation (byte: 256/360 of degree) */
    public byte rotation;

    /** Player pitch rotation (byte: 256/360 of degree) */
    public byte pitch;

    /** Currently held item ID (0 if none) */
    public int currentItem;

    /**
     * Default constructor for deserialization.
     */
    public Packet20NamedEntitySpawn() {
    }

    /**
     * Creates a named entity spawn packet from a player.
     * 
     * <p>Converts player position from double to fixed-point and rotation from
     * degrees to byte encoding.</p>
     * 
     * @param entityPlayer The player to spawn
     */
    public Packet20NamedEntitySpawn(EntityPlayer entityPlayer) {
        this.entityId = entityPlayer.entityId;
        this.name = entityPlayer.username;

        // Convert double coords to fixed-point (32x) ints
        this.xPosition = MathHelper.floor_double(entityPlayer.posX * 32.0D);
        this.yPosition = MathHelper.floor_double(entityPlayer.posY * 32.0D);
        this.zPosition = MathHelper.floor_double(entityPlayer.posZ * 32.0D);

        // Convert degrees to byte (256/360 scale)
        this.rotation = (byte) ((int) (entityPlayer.rotationYaw * 256.0F / 360.0F));
        this.pitch = (byte) ((int) (entityPlayer.rotationPitch * 256.0F / 360.0F));

        // Get currently held item ID (0 if none)
        ItemStack heldItem = entityPlayer.inventory.getCurrentItem();
        this.currentItem = heldItem == null ? 0 : heldItem.itemID;
    }

    /**
     * Reads spawn data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.name = readString(dataInputStream, 16);
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.readInt();
        this.zPosition = dataInputStream.readInt();
        this.rotation = dataInputStream.readByte();
        this.pitch = dataInputStream.readByte();
        this.currentItem = dataInputStream.readShort();
    }

    /**
     * Writes spawn data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        writeString(this.name, dataOutputStream);
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.writeInt(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
        dataOutputStream.writeByte(this.rotation);
        dataOutputStream.writeByte(this.pitch);
        dataOutputStream.writeShort(this.currentItem);
    }

    /**
     * Processes named entity spawn packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleNamedEntitySpawn(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 28 bytes
     */
    public int getPacketSize() {
        return 28;
    }
}
