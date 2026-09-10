# Minecraft Server Multiplayer Architecture

> A technical deep-dive into how Minecraft Beta 1.7.3 multiplayer works, based on the InfHell 2 source code.

---

## Overview

Minecraft multiplayer is a client-server architecture where a single dedicated server hosts the authoritative world state, and clients connect over TCP to participate. The server is the single source of truth: it owns the world, validates all actions, and broadcasts changes to all connected clients. Clients are essentially "dumb terminals" that render what the server tells them and send player input back.

---

## The Big Picture

```
  +--------+                           +------------------+
  | Client |---[TCP/IP]----------------| Dedicated Server |
  +--------+                           +------------------+
       |                                       |
       |  Owns: local world rendering,         |  Owns: authoritative world,
       |  player input, camera, sound          |  block placement/breaking,
       |  local player entity prediction        |  mob AI, chunk generation,
       |                                        |  player positions, inventory
       |  Connects via NetClientHandler        |  Connects via NetServerHandler
       +---------------------------------------+

  Multiple clients can connect to the same server simultaneously.
  Each client connection is managed by a separate NetServerHandler on the server.
```

### Key Design Decisions

1. **Server-authoritative** — The client never modifies world state directly. Every block break, block place, entity spawn, and world change originates from the server.

2. **Tick-based simulation** — Both server and client run a game loop at ~20 ticks per second (50ms per tick). The server processes world updates; the client renders them.

3. **Chunk-based world transfer** — The world is divided into 16×16×128 column chunks. The server sends chunks to clients as they explore. Clients do not generate terrain.

4. **TCP for reliability** — All packets travel over TCP. Order and delivery are guaranteed, which simplifies game logic at the cost of latency.

5. **Entity state synchronization** — Entities (players, mobs, items, projectiles) are tracked server-side and their state is broadcast to nearby clients via a tracking radius.

---

## Connection Lifecycle

A complete multiplayer session goes through these phases:

```
1. HANDSHAKE
   Client                     Server
    |------- Packet2Handshake ----->|
    |<------ Packet1Login ----------|
    |                               (Server verifies / skips session)
2. AUTHENTICATION (if online-mode)
    |<------ Session Verify Thread --|
3. LOGIN
    |<------ Packet1Login OK -------|
    |------- Packet1Login ----------->|
    |<------ Packet03Chat (MOTD) ----|
    |<------ Chunk data (near spawn)-|
    |<------ Player spawn ----------|
    |<------ Entity spawns (nearby)--|
4. GAMEPLAY
    |<====== Continuous packet stream =====>|
    Server broadcasts: entity moves, block changes,
                       mob AI updates, chat, weather, etc.
    Client sends: player position/rotation, block interactions,
                  chat messages, creative actions, etc.
5. DISCONNECT
    |------- Packet255Disconnect --->|
```

### Handshake (Packet2Handshake)

The client initiates the connection by sending a `Packet2Handshake`. This packet carries the client's username and a "server ID" string. In offline mode the server ID is arbitrary; in online mode it is used for session verification against `minecraft.net`.

### Login (Packet1Login)

After receiving the handshake, the server sends `Packet1Login` with:
- The client's entity ID (used by the client to identify itself as an entity in the world)
- The world's seed (for biome/terrain consistency)
- The world type (DEFAULT, FLAT, LARGE_BIOMES, etc.)
- The server's difficulty setting
- The "reduced debug" flag (creative mode restriction)

If the server is in online mode, a `ThreadLoginVerifier` background thread queries the Mojang session server to confirm the player owns the account. If verification fails or the username is banned, the server kicks the client with `Packet255Disconnect`.

### World State Transfer

Once logged in, the server begins sending world data:

1. **Spawn chunks** — The server pre-generates or loads chunks around the spawn point (configurable radius). These are sent as `Packet51MapChunk` packets, each containing a compressed zlib payload of the entire 16×128×16 block volume.

2. **Player spawn** — `Packet10Flying` or `Packet0Login` triggers the client's local player entity to be positioned at the spawn coordinates.

3. **Nearby entities** — The server's `EntityTracker` sends spawn packets for all entities within tracking range: other players (`Packet12Packet200Entity`), mobs (`Packet24Packet103DropActor`), items (`Packet23Packet21VehicleSpawn`), etc.

---

## Network Architecture

### Client Side (`NetClientHandler`)

`NetClientHandler` receives all packets from the server and dispatches them to the appropriate handler method. It also manages the local player's movement by sending position/rotation packets as the player moves.

Key responsibilities:
- Handle incoming chunk data (`Packet51MapChunk`, `Packet52MultiBlockChange`, `Packet53BlockChange`)
- Handle entity spawn and despawn
- Handle chat messages (`Packet3Chat`)
- Handle player inventory updates
- Send player movement (`Packet10Flying`, `Packet11PlayerPosition`, `Packet12PlayerRotation`)
- Handle login result and kick/disconnect

### Server Side (`NetServerHandler`)

Each connected player has a dedicated `NetServerHandler` instance. It runs in the server's main tick loop, calling `handlePackets()` to read from the TCP socket and dispatch to game logic.

Key responsibilities:
- Validate incoming player movement (anti-speedhack, anti-fly)
- Process block interactions (break, place, activate)
- Process inventory transactions
- Process chat commands
- Broadcast entity state changes to other nearby players
- Handle player disconnect cleanly

### Network I/O (`NetworkManager`)

Both client and server share `NetworkManager` for the actual TCP read/write. It runs three threads:
- **Reader thread** (`NetworkReaderThread`) — blocks on `read()` from the socket, parses packets by ID, dispatches to `NetHandler`
- **Writer thread** (`NetworkWriterThread`) — pulls packets from a synchronized send queue and writes them to the socket
- **Monitor thread** (`ThreadMonitorConnection`) — watches for connection stalls (no data for too long) and triggers timeouts

The `sendQueueByteLength` field tracks the pending outbound data to detect and throttle clients that are receiving faster than they can process.

### Packet ID Space

Packets are numbered 0–255. Minecraft Beta 1.7.3 uses approximately:
- 0–50: Core game (login, handshake, movement, chat, spawning)
- 50–79: World/chunk data (pre-chunk, map chunk, multi-block change, block change)
- 80–99: Inventory and window operations
- 100–130: Various game state (explosion, sound, particle, weather)
- 130–255: Extended or mod-specific data

InfHell extends this with custom packet IDs 94–99 for server features (freeze level, day-of-year, weather, creative mode).

---

## Coordinate Encoding

Minecraft uses a fixed-point representation for positions and angles on the wire to minimize bandwidth:

### Positions

World coordinates (doubles) are encoded as integers by multiplying by 32.0:

```
wire_value = floor(world_pos * 32.0)
```

So a position of X=10.5 becomes wire value 336. This gives sub-block precision (1/32 block = 0.03125 blocks).

On the wire, positions are typically sent as three separate 3-byte varints (or three packed ints depending on the packet).

### Rotations

Yaw and pitch (floats, 0–360° and -90° to +90°) are encoded as unsigned bytes (0–255):

```
wire_byte = floor(angle * 256.0 / 360.0)
```

So 90° = 64, 180° = 128, 270° = 192.

### Velocities

Entity velocities are sent as short integers representing units of 1/8000 of a block per tick:

```
wire_value = floor(velocity * 8000.0)
```

### Multi-Block Change Bit-Packing

When sending a block change within a chunk, the coordinates are packed into a single integer:

```java
int packed = (x << 12) | (z << 8) | y;
// x: bits 12-23 (4096 range = ±2048 blocks = ±2 chunks)
// z: bits 8-11  (16 range = ±8 blocks)
// y: bits 0-7   (256 range = full height)
```

---

## Chunk System

### Chunk Format

A chunk is a 16×128×16 volume of blocks stored as a byte array. The format is:
- **Primary bitmap** (1024 bytes): one bit per block position indicating whether the block is air (0) or non-air (1)
- **Block IDs** (4096 bytes): block type for each position (only stored for non-air blocks, indexed by bit position)
- **Metadata** (2048 bytes): block damage value per 2 blocks
- **Block light** (2048 bytes): light level per 2 blocks
- **Sky light** (2048 bytes): sky light per 2 blocks (may be absent in old chunks)

Chunks are compressed with zlib before being sent over the wire.

### Chunk Transfer Protocol

1. Client sends `Packet50PreChunk` with chunk coordinates and `true` (load)
2. Server sends `Packet51MapChunk` with the compressed chunk data
3. If the chunk later becomes unloadable, server sends `Packet50PreChunk` with `false` (unload)

The client maintains a `ChunkProviderClient` that caches received chunks. It never generates terrain — the server provides everything.

---

## Player Movement

### Client → Server

The client continuously sends movement packets as the player moves:

- `Packet10Flying` — sent when only the player's look direction changes (no positional data)
- `Packet11PlayerPosition` — sent when the player moves (contains X, Y, feet position, stance)
- `Packet12PlayerRotation` — sent when only the player's rotation changes
- `Packet10Flying` with `Packet13PlayerLook` combined — the server reads both from a single tick's data

The `stance` field is a float representing the player's eye height relative to feet Y. It is used for basic anti-cheat validation.

### Server Validation

`NetServerHandler` checks every incoming movement packet:
- **Position bounds** — player cannot be more than a few blocks outside the loaded world
- **Stance validation** — feet Y must be ≤ stance ≤ feet Y + 1.65
- **Speed check** — distance moved per tick cannot exceed a threshold (unless player is in creative mode)
- **Teleport queue** — when the server teleports a player, it queues a "confirm teleport" ID; the client must echo it back before the next movement

### Server → Client

The server broadcasts player positions of nearby players via `EntityTrackerEntry`. It encodes positions using the fixed-point scheme and sends `Packet30Packet200Entity` (or the more compact relative update packet `Packet31Packet201RelEntityMove`/`Packet32Packet202EntityLook`) to all clients tracking the entity.

---

## Chat System

Chat messages travel as `Packet3Chat` with a 119-character (119 bytes) limit. The server prefixes player names; the client renders them. Message formatting supports color codes via section sign (`§`) characters embedded in the string.

The server has a command system (built on top of the chat packet) triggered by messages starting with `/`. Commands are processed by `ICommandManager` and `ICommandListener`.

---

## What's Next

This document will be expanded to cover:
- [ ] Entity tracking and synchronization
- [ ] Inventory and window system
- [ ] Block interaction (breaking, placing, activation)
- [ ] The entity tracking radius and entity tracker
- [ ] World saving and chunk loading
- [ ] Spawn protection and world borders
- [ ] The InfHell custom packets (94–99)
- [ ] The seasons and weather system
- [ ] City generation and MapGenCity
