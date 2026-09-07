package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.DataWatcher;
import net.minecraft.game.entity.EntityList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.WatchableObject;
import net.minecraft.network.NetHandler;

/**
 * Mob spawn packet - tells clients about a living entity (mobs, animals).
 * 
 * <p><b>Direction:</b> Server -> Client only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Spawn hostile mobs (zombies, skeletons, creepers)</li>
 *   <li>Spawn neutral mobs (pigs, cows, sheep)</li>
 *   <li>Spawn ambient mobs (bats)</li>
 *   <li>Include entity type and initial metadata</li>
 * </ul>
 * 
 * <p><b>Metadata:</b> DataWatcher contains dynamic properties like health,
 * potion effects, held item, armor, etc.</p>
 * 
 * @see Packet24MobSpawn
 * @see DataWatcher
 */
public class Packet24MobSpawn extends Packet {

    /** Mob entity ID */
    public int entityId;

    /** Mob type (from EntityList registry) */
    public int type;

    /** X coordinate (fixed-point: actual * 32) */
    public int xPosition;

    /** Y coordinate (fixed-point: actual * 32) */
    public int yPosition;

    /** Z coordinate (fixed-point: actual * 32) */
    public int zPosition;

    /** Yaw rotation (byte: 256/360 of degree) */
    public byte yaw;

    /** Pitch rotation (byte: 256/360 of degree) */
    public byte pitch;

    /** Entity metadata for writing (set during construction) */
    private DataWatcher metaData;

    /** Entity metadata for reading (populated during deserialization) */
    private List<WatchableObject> receivedMetadata;

    /**
     * Default constructor for deserialization.
     */
    public Packet24MobSpawn() {
    }

    /**
     * Creates a mob spawn packet from a living entity.
     * 
     * @param entityLiving The living entity to spawn
     */
    public Packet24MobSpawn(EntityLiving entityLiving) {
        this.entityId = entityLiving.entityId;
        this.type = EntityList.getEntityID(entityLiving);
        this.xPosition = MathHelper.floor_double(entityLiving.posX * 32.0D);
        this.yPosition = MathHelper.floor_double(entityLiving.posY * 32.0D);
        this.zPosition = MathHelper.floor_double(entityLiving.posZ * 32.0D);
        this.yaw = (byte) ((int) (entityLiving.rotationYaw * 256.0F / 360.0F));
        this.pitch = (byte) ((int) (entityLiving.rotationPitch * 256.0F / 360.0F));
        this.metaData = entityLiving.getDataWatcher();
    }

    /**
     * Reads mob spawn data from stream.
     * 
     * <p>Note: type is read as unsigned byte to handle full range of mob IDs.</p>
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.entityId = dataInputStream.readInt();
        this.type = (int) dataInputStream.readByte() & 0xff;
        this.xPosition = dataInputStream.readInt();
        this.yPosition = dataInputStream.readInt();
        this.zPosition = dataInputStream.readInt();
        this.yaw = dataInputStream.readByte();
        this.pitch = dataInputStream.readByte();
        this.receivedMetadata = DataWatcher.readWatchableObjects(dataInputStream);
    }

    /**
     * Writes mob spawn data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.entityId);
        dataOutputStream.writeByte((byte) this.type);
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.writeInt(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
        dataOutputStream.writeByte(this.yaw);
        dataOutputStream.writeByte(this.pitch);
        this.metaData.writeWatchableObjects(dataOutputStream);
    }

    /**
     * Processes mob spawn packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleMobSpawn(this);
    }

    /**
     * Returns packet size.
     * 
     * @return Base size (metadata variable, but we report minimum)
     */
    public int getPacketSize() {
        return 20;
    }

    /**
     * Gets the entity metadata received from server.
     * 
     * @return List of watchable objects containing entity state
     */
    public List<WatchableObject> getMetadata() {
        return this.receivedMetadata;
    }
}
