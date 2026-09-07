package net.minecraft.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.game.item.ItemStack;
import net.minecraft.network.NetHandler;
import net.minecraft.network.PacketCounter;

/**
 * Base class for all network packets in the Minecraft protocol.
 * 
 * <p>Each packet has a unique ID and can be sent from either client or server
 * (or both). The static initializer registers all known packet types with their
 * IDs and directional permissions.</p>
 * 
 * <p>Packet flow:</p>
 * <ul>
 *   <li>{@code readPacket(DataInputStream, boolean isServer)} - reads packet ID and delegates to
 *       concrete packet's {@link #readPacketData(DataInputStream)}</li>
 *   <li>{@code writePacket(Packet, DataOutputStream)} - writes packet ID and delegates to
 *       concrete packet's {@link #writePacketData(DataOutputStream)}</li>
 *   <li>{@code processPacket(NetHandler)} - dispatches to appropriate handler method on NetHandler</li>
 * </ul>
 * 
 * <p>Direction flags in {@link #addIdClassMapping(int, boolean, boolean, Class)}:</p>
 * <ul>
 *   <li>Second boolean: packet can be sent <b>from server to client</b> (clientPacketIdList)</li>
 *   <li>Third boolean: packet can be sent <b>from client to server</b> (serverPacketIdList)</li>
 * </ul>
 * 
 * @see <a href="http://wiki.vg/Protocol">Protocol Documentation</a>
 */
public abstract class Packet {

    /** Maps packet ID to packet class for deserialization */
    private static Map<Integer, Class<? extends Packet>> packetIdToClassMap = new HashMap<Integer, Class<? extends Packet>>();

    /** Maps packet class to packet ID for serialization */
    private static Map<Class<? extends Packet>, Integer> packetClassToIdMap = new HashMap<Class<? extends Packet>, Integer>();

    /** Set of packet IDs that can be received by client (server-to-client packets) */
    private static Set<Integer> clientPacketIdList = new HashSet<Integer>();

    /** Set of packet IDs that can be received by server (client-to-server packets) */
    private static Set<Integer> serverPacketIdList = new HashSet<Integer>();

    /** Timestamp in milliseconds when this packet instance was created */
    public final long creationTimeMillis = System.currentTimeMillis();

    /** Flag indicating this packet contains chunk data (used for compression/splitting) */
    public boolean isChunkDataPacket = false;

    /** Statistics tracking: packet ID -> counter for monitoring packet frequency/size */
    private static HashMap<Integer, PacketCounter> packetStats;

    /** Total packets processed since server/client startup */
    private static int totalPacketsCount;

    /**
     * Registers a packet class with its ID and direction permissions.
     * 
     * @param packetId          Unique packet identifier
     * @param canBeReceivedByClient  True if server can send this packet to client
     * @param canBeReceivedByServer  True if client can send this packet to server
     * @param packetClass       The packet class to instantiate for this ID
     * @throws IllegalArgumentException if packetId or packetClass is already registered
     */
    static void addIdClassMapping(int packetId, boolean canBeReceivedByClient, boolean canBeReceivedByServer, Class<? extends Packet> packetClass) {
        // Validate no duplicate packet ID
        if (packetIdToClassMap.containsKey(packetId)) {
            throw new IllegalArgumentException("Duplicate packet id:" + packetId);
        }
        // Validate no duplicate packet class
        else if (packetClassToIdMap.containsKey(packetClass)) {
            throw new IllegalArgumentException("Duplicate packet class:" + packetClass);
        } else {
            // Create bidirectional mappings for fast lookup in both directions
            packetIdToClassMap.put(packetId, packetClass);
            packetClassToIdMap.put(packetClass, packetId);

            // Track which direction this packet travels
            if (canBeReceivedByClient) {
                clientPacketIdList.add(packetId);
            }
            if (canBeReceivedByServer) {
                serverPacketIdList.add(packetId);
            }
        }
    }

    /**
     * Creates a new packet instance for the given packet ID.
     * 
     * @param packetId The protocol packet ID
     * @return New packet instance, or null if ID is not registered
     */
    public static Packet getNewPacket(int packetId) {
        try {
            // Look up class by packet ID and instantiate via reflection
            Class<? extends Packet> packetClass = packetIdToClassMap.get(packetId);
            return packetClass == null ? null : (Packet) packetClass.newInstance();
        } catch (Exception e) {
            // Log and skip unknown/broken packets rather than crashing
            e.printStackTrace();
            System.out.println("Skipping packet with id " + packetId);
            return null;
        }
    }

    /**
     * Gets the protocol ID for this packet class.
     * 
     * @return The registered packet ID
     */
    public final int getPacketId() {
        return packetClassToIdMap.get(this.getClass()).intValue();
    }

    /**
     * Reads a packet from the input stream, validating direction permissions.
     * 
     * <p>This is the main entry point for receiving packets from the network.</p>
     * 
     * @param dataInputStream   The input stream to read from
     * @param isServerSide      True if this is the server receiving, false if client
     * @return The parsed packet, or null if end of stream reached
     * @throws IOException If packet data is invalid or direction check fails
     */
    public static Packet readPacket(DataInputStream dataInputStream, boolean isServerSide) throws IOException {
        Packet packet = null;

        int packetId;
        try {
            // Read packet ID as single byte
            packetId = dataInputStream.read();
            if (packetId == -1) {
                // Clean disconnect - end of stream
                return null;
            }

            // Validate packet direction: ensure sender is allowed to send this packet type
            // Server-side: check clientPacketIdList (packets server can send)
            // Client-side: check serverPacketIdList (packets client can send)
            if (isServerSide && !serverPacketIdList.contains(packetId) || !isServerSide && !clientPacketIdList.contains(packetId)) {
                throw new IOException("Bad packet id " + packetId);
            }

            // Instantiate and read packet data
            packet = getNewPacket(packetId);
            if (packet == null) {
                throw new IOException("Bad packet id " + packetId);
            }

            packet.readPacketData(dataInputStream);
        } catch (EOFException e) {
            // Handle clean disconnect gracefully
            System.out.println("Reached end of stream");
            return null;
        }

        // Update packet statistics for monitoring
        PacketCounter packetCounter = packetStats.get(packetId);
        if (packetCounter == null) {
            packetCounter = new PacketCounter();
            packetStats.put(packetId, packetCounter);
        }
        packetCounter.addPacket(packet.getPacketSize());

        // Periodic statistics logging (every 1000 packets)
        ++totalPacketsCount;
        if (totalPacketsCount % 1000 == 0) {
            // Placeholder for periodic stats logging
            ;
        }

        return packet;
    }

    /**
     * Writes a packet to the output stream, prefixed with its ID.
     * 
     * @param packet            The packet to write
     * @param dataOutputStream  The output stream to write to
     * @throws IOException If writing fails
     */
    public static void writePacket(Packet packet, DataOutputStream dataOutputStream) throws IOException {
        // Write packet ID first so receiver knows which class to instantiate
        dataOutputStream.write(packet.getPacketId());
        packet.writePacketData(dataOutputStream);
    }

    /**
     * Writes a UTF-16 string to the output stream.
     * 
     * <p>Format: 2-byte length prefix (short), followed by char sequence.</p>
     * 
     * @param string            The string to write
     * @param dataOutputStream  The output stream
     * @throws IOException If string exceeds maximum length or writing fails
     */
    public static void writeString(String string, DataOutputStream dataOutputStream) throws IOException {
        if (string.length() > 32767) {
            throw new IOException("String too big");
        } else {
            dataOutputStream.writeShort(string.length());
            dataOutputStream.writeChars(string);
        }
    }

    /**
     * Reads a UTF-16 string from the input stream.
     * 
     * <p>Format: 2-byte length prefix, followed by char sequence.</p>
     * 
     * @param dataInputStream   The input stream
     * @param maxLength         Maximum allowed string length (in characters)
     * @return The decoded string
     * @throws IOException If length exceeds maxLength or is negative
     */
    public static String readString(DataInputStream dataInputStream, int maxLength) throws IOException {
        short stringLength = dataInputStream.readShort();

        // Validate string length bounds
        if (stringLength > maxLength) {
            throw new IOException("Received string length longer than maximum allowed (" + stringLength + " > " + maxLength + ")");
        } else if (stringLength < 0) {
            throw new IOException("Received string length is less than zero! Weird string!");
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            // Read UTF-16 character pairs
            for (int i = 0; i < stringLength; ++i) {
                stringBuilder.append(dataInputStream.readChar());
            }

            return stringBuilder.toString();
        }
    }

    /**
     * Reads an ItemStack from the input stream.
     * 
     * <p>Format:</p>
     * <ul>
     *   <li>Short: item ID (-1 if no item)</li>
     *   <li>If ID >= 0: byte for stack size, short for item damage</li>
     * </ul>
     * 
     * @param stream The input stream
     * @return The parsed ItemStack, or null if ID was -1
     * @throws IOException If reading fails
     */
    public static ItemStack readItemStack(DataInputStream stream) throws IOException {
        ItemStack itemStack = null;

        // Read item ID first (determines if item exists)
        short itemID = stream.readShort();

        // Only read additional fields if item exists
        if (itemID > 0) {
            byte stackSize = stream.readByte();
            short itemDamage = stream.readShort();
            itemStack = new ItemStack(itemID, stackSize, itemDamage);
        }

        return itemStack;
    }

    /**
     * Writes an ItemStack to the output stream.
     * 
     * <p>Writes -1 for item ID if stack is null, otherwise writes full item data.</p>
     * 
     * @param itemStack         The item to write (may be null)
     * @param stream            The output stream
     * @throws IOException If writing fails
     */
    public static void writeItemStack(ItemStack itemStack, DataOutputStream stream) throws IOException {
        if (itemStack == null) {
            // Signal no item with -1
            stream.writeShort(-1);
        } else {
            stream.writeShort(itemStack.itemID);
            stream.writeByte(itemStack.stackSize);
            stream.writeShort(itemStack.getItemDamage());
        }
    }

    /**
     * Reads packet-specific data from the input stream.
     * 
     * <p>Each concrete packet implementation overrides this to read its specific fields
     * in the order they were written.</p>
     * 
     * @param dataInputStream The input stream to read from
     * @throws IOException If reading fails or data is invalid
     */
    public abstract void readPacketData(DataInputStream dataInputStream) throws IOException;

    /**
     * Writes packet-specific data to the output stream.
     * 
     * <p>Each concrete packet implementation overrides this to write its specific fields
     * in a deterministic order matching readPacketData.</p>
     * 
     * @param dataOutputStream The output stream to write to
     * @throws IOException If writing fails
     */
    public abstract void writePacketData(DataOutputStream dataOutputStream) throws IOException;

    /**
     * Processes this packet on the receiving end.
     * 
     * <p>Called after readPacketData completes. Dispatches to the appropriate
     * handler method on NetHandler based on packet type.</p>
     * 
     * @param netHandler The handler to process this packet
     */
    public abstract void processPacket(NetHandler netHandler);

    /**
     * Gets the approximate size of this packet in bytes.
     * 
     * <p>Used for bandwidth monitoring and possibly for buffer sizing.</p>
     * 
     * @return Packet size in bytes
     */
    public abstract int getPacketSize();

    /**
     * Static initializer - registers all known packet types with their IDs.
     * 
     * <p>Format: addIdClassMapping(packetId, clientReceive, serverReceive, class)</p>
     * <ul>
     *   <li>Second param true = server can send to client</li>
     *   <li>Third param true = client can send to server</li>
     * </ul>
     */
    static {
        // ============ Core Protocol Packets ============

        // 0: Keep alive - both directions, used for connection health checks
        addIdClassMapping(0, true, true, Packet0KeepAlive.class);

        // 1: Login - client->server with protocol version, username, map seed, dimension
        addIdClassMapping(1, true, true, Packet1Login.class);

        // 2: Handshake - both directions for connection setup
        addIdClassMapping(2, true, true, Packet2Handshake.class);

        // 3: Chat message - both directions
        addIdClassMapping(3, true, true, Packet3Chat.class);

        // 4: Update time - server->client only, sync game time
        addIdClassMapping(4, true, false, Packet4UpdateTime.class);

        // 5: Player inventory update - server->client only
        addIdClassMapping(5, true, false, Packet5PlayerInventory.class);

        // 6: Spawn position - server->client only, bed/respawn point
        addIdClassMapping(6, true, false, Packet6SpawnPosition.class);

        // 7: Use entity - client->server only, player interacting with entity
        addIdClassMapping(7, false, true, Packet7UseEntity.class);

        // 8: Update health - server->client only
        addIdClassMapping(8, true, false, Packet8UpdateHealth.class);

        // 9: Respawn - both directions
        addIdClassMapping(9, true, true, Packet9Respawn.class);

        // 10: Flying (base) - both directions, position checks
        addIdClassMapping(10, true, true, Packet10Flying.class);

        // 11: Player position - both directions, full position update
        addIdClassMapping(11, true, true, Packet11PlayerPosition.class);

        // 12: Player look - both directions, rotation only
        addIdClassMapping(12, true, true, Packet12PlayerLook.class);

        // 13: Player look + move - both directions, combined position/rotation
        addIdClassMapping(13, true, true, Packet13PlayerLookMove.class);

        // 14: Block dig - client->server only, player breaking block
        addIdClassMapping(14, false, true, Packet14BlockDig.class);

        // 15: Place block/item - client->server only
        addIdClassMapping(15, false, true, Packet15Place.class);

        // 16: Block item switch - client->server only, hotbar selection
        addIdClassMapping(16, false, true, Packet16BlockItemSwitch.class);

        // 17: Sleep - server->client only, player entering bed
        addIdClassMapping(17, true, false, Packet17Sleep.class);

        // 18: Animation - both directions
        addIdClassMapping(18, true, true, Packet18Animation.class);

        // 19: Entity action - client->server only (crouch, sprint, etc.)
        addIdClassMapping(19, false, true, Packet19EntityAction.class);

        // 20: Named entity spawn - server->client only, other player appearance
        addIdClassMapping(20, true, false, Packet20NamedEntitySpawn.class);

        // 21: Pickup spawn - server->client only, item drops
        addIdClassMapping(21, true, false, Packet21PickupSpawn.class);

        // 22: Collect item - server->client only, pickup confirmation
        addIdClassMapping(22, true, false, Packet22Collect.class);

        // 23: Vehicle (boat/minecart) spawn - server->client only
        addIdClassMapping(23, true, false, Packet23VehicleSpawn.class);

        // 24: Mob spawn - server->client only, hostile/neutral mobs
        addIdClassMapping(24, true, false, Packet24MobSpawn.class);

        // 25: Painting spawn - server->client only
        addIdClassMapping(25, true, false, Packet25EntityPainting.class);

        // 27: Position (custom) - client->server only, movement details
        addIdClassMapping(27, false, true, Packet27Position.class);

        // 28: Entity velocity - server->client only, physics knockback
        addIdClassMapping(28, true, false, Packet28EntityVelocity.class);

        // 29: Destroy entity - server->client only
        addIdClassMapping(29, true, false, Packet29DestroyEntity.class);

        // 30: Entity (base) - server->client only
        addIdClassMapping(30, true, false, Packet30Entity.class);

        // 31-34: Entity relative movement variants
        addIdClassMapping(31, true, false, Packet31RelEntityMove.class);
        addIdClassMapping(32, true, false, Packet32EntityLook.class);
        addIdClassMapping(33, true, false, Packet33RelEntityMoveLook.class);
        addIdClassMapping(34, true, false, Packet34EntityTeleport.class);

        // 38: Entity status - server->client only (damage, death, etc.)
        addIdClassMapping(38, true, false, Packet38EntityStatus.class);

        // 39: Attach entity - server->client only (leash, vehicle mounting)
        addIdClassMapping(39, true, false, Packet39AttachEntity.class);

        // 40: Entity metadata - server->client only, dynamic entity properties
        addIdClassMapping(40, true, false, Packet40EntityMetadata.class);

        // 41: Entity effect - server->client only, potion effects
        addIdClassMapping(41, true, false, Packet41EntityEffect.class);

        // 42: Remove entity effect - server->client only
        addIdClassMapping(42, true, false, Packet42RemoveEntityEffect.class);

        // ============ Chunk/Map Data Packets ============

        // 50: Pre-chunk - server->client only, chunk loading notification
        addIdClassMapping(50, true, false, Packet50PreChunk.class);

        // 51: Map chunk - server->client only, bulk chunk data
        addIdClassMapping(51, true, false, Packet51MapChunk.class);

        // 52: Multi-block change - server->client only
        addIdClassMapping(52, true, false, Packet52MultiBlockChange.class);

        // 53: Single block change - server->client only
        addIdClassMapping(53, true, false, Packet53BlockChange.class);

        // 54: Play note block - server->client only, jukebox/noteblock
        addIdClassMapping(54, true, false, Packet54PlayNoteBlock.class);

        // 60-61: Explosion and door change
        addIdClassMapping(60, true, false, Packet60Explosion.class);
        addIdClassMapping(61, true, false, Packet61DoorChange.class);

        // 70: Bed - server->client only
        addIdClassMapping(70, true, false, Packet70Bed.class);

        // 71: Weather - server->client only
        addIdClassMapping(71, true, false, Packet71Weather.class);

        // ============ GUI/Window Packets ============

        // 100: Open window - server->client only
        addIdClassMapping(100, true, false, Packet100OpenWindow.class);

        // 101: Close window - both directions
        addIdClassMapping(101, true, true, Packet101CloseWindow.class);

        // 102: Window click - client->server only
        addIdClassMapping(102, false, true, Packet102WindowClick.class);

        // 103: Set slot - server->client only, update single inventory slot
        addIdClassMapping(103, true, false, Packet103SetSlot.class);

        // 104: Window items - server->client only, full inventory contents
        addIdClassMapping(104, true, false, Packet104WindowItems.class);

        // 105: Update progress bar - server->client only
        addIdClassMapping(105, true, false, Packet105UpdateProgressbar.class);

        // 106: Transaction (confirm window action) - both directions
        addIdClassMapping(106, true, true, Packet106Transaction.class);

        // 107: Creative set slot - both directions (creative mode)
        addIdClassMapping(107, true, true, Packet107CreativeSetSlot.class);

        // ============ Sign/Map Data ============

        // 130: Update sign - both directions
        addIdClassMapping(130, true, true, Packet130UpdateSign.class);

        // 131: Map data - server->client only
        addIdClassMapping(131, true, false, Packet131MapData.class);

        // ============ Statistics ============

        // 200: Statistics - server->client only
        addIdClassMapping(200, true, false, Packet200Statistic.class);

        // ============ Custom/Extended Protocol ============

        // 250: Custom payload - both directions, plugin channels
        addIdClassMapping(250, true, true, Packet250CustomPayload.class);

        // 255: Kick/disconnect - both directions
        addIdClassMapping(255, true, true, Packet255KickDisconnect.class);

        // ============ InfHell Custom Packets ============

        // 91: Update command block
        addIdClassMapping(91, true, true, Packet91UpdateCommandBlock.class);

        // 92: Set custom world info
        addIdClassMapping(92, true, false, Packet92SetCustomWorldInfo.class);

        // 93: Update animal name (mo'creatures compatibility)
        addIdClassMapping(93, true, true, Packet93UpdateAnimalName.class);

        // 94: Freeze level
        addIdClassMapping(94, true, false, Packet94FreezeLevel.class);

        // 95: Update day of year (seasons)
        addIdClassMapping(95, true, false, Packet95UpdateDayOfTheYear.class);

        // 96: Bad moon decide (seasons)
        addIdClassMapping(96, true, false, Packet96BadMoonDecide.class);

        // 97: Set inventory slot
        addIdClassMapping(97, true, true, Packet97SetInventorySlot.class);

        // 98: Update weather
        addIdClassMapping(98, true, false, Packet98UpdateWeather.class);

        // 99: Set creative mode
        addIdClassMapping(99, true, false, Packet99SetCreativeMode.class);

        // Initialize statistics tracking
        packetStats = new HashMap<Integer, PacketCounter>();
        totalPacketsCount = 0;
    }
}
