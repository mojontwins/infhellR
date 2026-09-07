package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Packet sent by the server to inform the client about custom world information.
 * This packet provides world-specific settings that the client should be aware of,
 * such as whether the crafting guide feature is enabled.
 *
 * <p>This is a custom packet added for the mod, used to synchronize
 * non-vanilla world configuration settings.</p>
 */
public class Packet92SetCustomWorldInfo extends Packet {
    /**
     * Whether the crafting guide feature is enabled in this world.
     * When true, the client displays additional crafting guidance UI.
     */
    public boolean enableCraftingGuide;

    public Packet92SetCustomWorldInfo() {
    }

    /**
     * Constructs a Packet92SetCustomWorldInfo with the specified crafting guide setting.
     *
     * @param enableCraftingGuide Whether the crafting guide should be enabled
     */
    public Packet92SetCustomWorldInfo(boolean enableCraftingGuide) {
        this.enableCraftingGuide = enableCraftingGuide;
    }

    /**
     * Reads this packet's data from the input stream.
     * Reads a single byte (0 or 1) to determine the crafting guide state.
     *
     * @param dataInputStream The input stream to read from
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.enableCraftingGuide = (dataInputStream.read() == 1);
    }

    /**
     * Writes this packet's data to the output stream.
     * Writes a single byte (0 or 1) for the crafting guide state.
     *
     * @param dataOutputStream The output stream to write to
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(this.enableCraftingGuide ? 1 : 0);
    }

    /**
     * Processes this packet on the client side by delegating to the network handler.
     *
     * @param netHandler The network handler to process this packet
     */
    @Override
    public void processPacket(NetHandler netHandler) {
        netHandler.handleSetCustomWorldInfo(this);
    }

    /**
     * Returns the size of this packet in bytes.
     *
     * @return Always returns 1 (single boolean byte)
     */
    @Override
    public int getPacketSize() {
        return 1;
    }
}
