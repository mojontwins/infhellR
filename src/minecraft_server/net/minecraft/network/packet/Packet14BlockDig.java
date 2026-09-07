package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Block dig packet - player starts/stops digging a block.
 * 
 * <p><b>Direction:</b> Client -> Server only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Player starts digging a block</li>
 *   <li>Player stops/cancels digging</li>
 *   <li>Block breaks (after digging completes)</li>
 *   <li>Server validates and applies block break</li>
 * </ul>
 * 
 * <p><b>Status values:</b></p>
 * <ul>
 *   <li>0 = Started digging</li>
 *   <li>1 = Stopped digging / cancelled</li>
 *   <li>2 = Block broken (completed)</li>
 *   <li>3 = Drop item (creative mode)</li>
 * </ul>
 * 
 * <p><b>Face values (0-5):</b> bottom, top, north, south, west, east</p>
 */
public class Packet14BlockDig extends Packet {

    /** Block X coordinate (int) */
    public int xPosition;

    /** Block Y coordinate (byte, since 0-127) */
    public int yPosition;

    /** Block Z coordinate (int) */
    public int zPosition;

    /** Block face being dug: 0=bottom, 1=top, 2=north, 3=south, 4=west, 5=east */
    public int face;

    /** Dig status: 0=started, 1=cancelled, 2=broken, 3=drop item */
    public int status;

    /**
     * Default constructor for deserialization.
     */
    public Packet14BlockDig() {
    }

    /**
     * Creates a block dig packet.
     * 
     * @param status    Dig status (0=started, 1=cancelled, 2=broken, 3=drop)
     * @param xPosition Block X coordinate
     * @param yPosition Block Y coordinate
     * @param zPosition Block Z coordinate
     * @param face      Block face being dug
     */
    public Packet14BlockDig(int status, int xPosition, int yPosition, int zPosition, int face) {
        this.status = status;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.zPosition = zPosition;
        this.face = face;
    }

    /**
     * Reads block dig data from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        // Status is byte (single byte, 0-255)
        this.status = dataInputStream.read();
        this.xPosition = dataInputStream.readInt();
        // Y is byte since world height is 0-127
        this.yPosition = dataInputStream.read();
        this.zPosition = dataInputStream.readInt();
        this.face = dataInputStream.read();
    }

    /**
     * Writes block dig data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(this.status);
        dataOutputStream.writeInt(this.xPosition);
        dataOutputStream.write(this.yPosition);
        dataOutputStream.writeInt(this.zPosition);
        dataOutputStream.write(this.face);
    }

    /**
     * Processes block dig packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleBlockDig(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 11 bytes
     */
    public int getPacketSize() {
        return 11;
    }
}
