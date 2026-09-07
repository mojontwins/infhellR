package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Block item switch packet - player selects different hotbar slot.
 * 
 * <p><b>Direction:</b> Client -> Server only.</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Player scrolls through hotbar (mouse wheel)</li>
 *   <li>Player presses 1-9 to select hotbar slot</li>
 *   <li>Server tracks currently held item for use/place packets</li>
 * </ul>
 * 
 * <p><b>Slot range:</b> 0-8 for hotbar slots 1-9</p>
 */
public class Packet16BlockItemSwitch extends Packet {

    /** Selected hotbar slot (0-8) */
    public int id;

    /**
     * Default constructor for deserialization.
     */
    public Packet16BlockItemSwitch() {
    }

    /**
     * Creates a hotbar switch packet.
     * 
     * @param id Hotbar slot index (0-8)
     */
    public Packet16BlockItemSwitch(int id) {
        this.id = id;
    }

    /**
     * Reads hotbar slot from stream.
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.id = dataInputStream.readShort();
    }

    /**
     * Writes hotbar slot to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.id);
    }

    /**
     * Processes hotbar switch packet.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleBlockItemSwitch(this);
    }

    /**
     * Returns packet size.
     * 
     * @return 2 bytes
     */
    public int getPacketSize() {
        return 2;
    }
}
