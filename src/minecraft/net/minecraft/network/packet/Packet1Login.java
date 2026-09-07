package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.NetHandler;

/**
 * Login packet - initiates game connection with server authentication.
 * 
 * <p><b>Direction:</b> Client -> Server (and server responds with login success/failure)</p>
 * 
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Client announces protocol version and username to server</li>
 *   <li>Server validates version compatibility</li>
 *   <li>Server initializes player session and spawns into world</li>
 *   <li>Map seed and dimension allow client to pre-generate terrain</li>
 * </ul>
 * 
 * <p><b>Protocol flow:</b></p>
 * <ol>
 *   <li>Client sends handshake (Packet2Handshake)</li>
 *   <li>Client sends login (Packet1Login)</li>
 *   <li>Server validates and responds</li>
 *   <li>If version mismatch, server kicks with error</li>
 *   <li>If success, server sends spawn position, time, and chunks</li>
 * </ol>
 * 
 * @see Packet2Handshake
 * @see Packet9Respawn
 */
public class Packet1Login extends Packet {

    /** Protocol version number - client/server must match for connection */
    public int protocolVersion;

    /** Player's username/display name */
    public String username;

    /** World seed for client-side terrain pre-generation */
    public long mapSeed;

    /** Dimension ID: 0=nether, 1=surface, -1=end */
    public byte dimension;

    /**
     * Default constructor for packet deserialization.
     */
    public Packet1Login() {
    }

    /**
     * Creates a login packet with basic credentials.
     * 
     * @param username        Player's username
     * @param protocolVersion Protocol version number
     */
    public Packet1Login(String username, int protocolVersion) {
        this.username = username;
        this.protocolVersion = protocolVersion;
    }

    /**
     * Creates a full login packet with world information.
     * 
     * @param username        Player's username
     * @param protocolVersion Protocol version number
     * @param mapSeed         World seed for client
     * @param dimension       Dimension ID
     */
    public Packet1Login(String username, int protocolVersion, long mapSeed, byte dimension) {
        this.username = username;
        this.protocolVersion = protocolVersion;
        this.mapSeed = mapSeed;
        this.dimension = dimension;
    }

    /**
     * Reads login data from stream.
     * 
     * <p>Reads in order: protocol version, username, map seed, dimension.</p>
     * 
     * @param dataInputStream The input stream
     * @throws IOException If reading fails
     */
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.protocolVersion = dataInputStream.readInt();
        this.username = readString(dataInputStream, 16);
        this.mapSeed = dataInputStream.readLong();
        this.dimension = dataInputStream.readByte();
    }

    /**
     * Writes login data to stream.
     * 
     * @param dataOutputStream The output stream
     * @throws IOException If writing fails
     */
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.protocolVersion);
        writeString(this.username, dataOutputStream);
        dataOutputStream.writeLong(this.mapSeed);
        dataOutputStream.writeByte(this.dimension);
    }

    /**
     * Processes login packet - dispatches to handler.
     * 
     * @param netHandler The network handler
     */
    public void processPacket(NetHandler netHandler) {
        netHandler.handleLogin(this);
    }

    /**
     * Calculates packet size for statistics.
     * 
     * @return Approximate size in bytes
     */
    public int getPacketSize() {
        return 4 + this.username.length() + 4 + 5;
    }
}
