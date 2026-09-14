package net.minecraft.client;
// ============================================================================================
// NetClientHandler.java
// --------------------------------------------------------------------------------------------
// Client-side packet handler. This class is the receiving end of the Minecraft Beta 1.7.3
// multiplayer protocol: it owns the TCP socket connection to a remote server, dispatches
// every inbound packet to the appropriate world/UI update routine, and exposes a small
// outbound API (addToSendQueue / quitWithPacket) used by the rest of the client to send
// packets back to the server.
//
// Lifecycle:
//   1. Constructor opens a TCP socket to (host, port) and starts the read/write threads
//      inside NetworkManager.
//   2. The server replies with a Packet2Handshake (connection string).
//   3. We either login directly (singleplayer-style username "-") or authenticate against
//      minecraft.net and then send Packet1Login.
//   4. The server replies with Packet1Login, after which handleLogin swaps the local
//      singleplayer world for a WorldClient and shows the "Downloading terrain" GUI.
//   5. The remaining handle* methods are invoked from NetworkManager.processReadPackets
//      once per inbound packet. They mutate the WorldClient and the local player in place.
//
// All position-based packets use fixed-point integers on the wire (multiplied by 32) and
// are converted to doubles by dividing by 32.0D. All yaw/pitch values are sent as 0..255
// "byte angles" and converted to 0..360 degrees by multiplying by 360 and dividing by 256.
// Velocity values are sent as integers divided by 8000.0D to recover a small double.
// ============================================================================================
import net.minecraft.game.item.ItemMap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.block.tileentity.TileEntityCommandBlock;
import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.trading.Currency;
import net.minecraft.client.gui.GuiTrading;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.trading.NpcTrader;
import net.minecraft.game.trading.TradingRecipeList;

import net.minecraft.client.player.EntityPlayerSP;
import net.minecraft.game.MapStorage;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.StatList;
import net.minecraft.game.container.Container;
import net.minecraft.game.container.InventoryBasic;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityCreature;
import net.minecraft.game.entity.EntityList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.WatchableObject;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.Explosion;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.WorldType;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.block.tileentity.TileEntityDispenser;
import net.minecraft.game.world.block.tileentity.TileEntityFurnace;
import net.minecraft.game.world.block.tileentity.TileEntitySign;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.loader.ISaveHandler;
import net.minecraft.network.NetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet100OpenWindow;
import net.minecraft.network.packet.Packet101CloseWindow;
import net.minecraft.network.packet.Packet103SetSlot;
import net.minecraft.network.packet.Packet104WindowItems;
import net.minecraft.network.packet.Packet105UpdateProgressbar;
import net.minecraft.network.packet.Packet106Transaction;
import net.minecraft.network.packet.Packet10Flying;
import net.minecraft.network.packet.Packet130UpdateSign;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet17Sleep;
import net.minecraft.network.packet.Packet18Animation;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet200Statistic;
import net.minecraft.network.packet.Packet20NamedEntitySpawn;
import net.minecraft.network.packet.Packet21PickupSpawn;
import net.minecraft.network.packet.Packet22Collect;
import net.minecraft.network.packet.Packet23VehicleSpawn;
import net.minecraft.network.packet.Packet24MobSpawn;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet255KickDisconnect;
import net.minecraft.network.packet.Packet25EntityPainting;
import net.minecraft.network.packet.Packet28EntityVelocity;
import net.minecraft.network.packet.Packet29DestroyEntity;
import net.minecraft.network.packet.Packet2Handshake;
import net.minecraft.network.packet.Packet30Entity;
import net.minecraft.network.packet.Packet34EntityTeleport;
import net.minecraft.network.packet.Packet38EntityStatus;
import net.minecraft.network.packet.Packet39AttachEntity;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.network.packet.Packet40EntityMetadata;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet42RemoveEntityEffect;
import net.minecraft.network.packet.Packet4UpdateTime;
import net.minecraft.network.packet.Packet50PreChunk;
import net.minecraft.network.packet.Packet51MapChunk;
import net.minecraft.network.packet.Packet52MultiBlockChange;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.network.packet.Packet54PlayNoteBlock;
import net.minecraft.network.packet.Packet5PlayerInventory;
import net.minecraft.network.packet.Packet60Explosion;
import net.minecraft.network.packet.Packet61DoorChange;
import net.minecraft.network.packet.Packet6SpawnPosition;
import net.minecraft.network.packet.Packet70Bed;
import net.minecraft.network.packet.Packet71Weather;
import net.minecraft.network.packet.Packet8UpdateHealth;
import net.minecraft.network.packet.Packet91UpdateCommandBlock;
import net.minecraft.network.packet.Packet92SetCustomWorldInfo;
import net.minecraft.network.packet.Packet93UpdateAnimalName;
import net.minecraft.network.packet.Packet94FreezeLevel;
import net.minecraft.network.packet.Packet95UpdateDayOfTheYear;
import net.minecraft.network.packet.Packet96BadMoonDecide;
import net.minecraft.network.packet.Packet98UpdateWeather;
import net.minecraft.network.packet.Packet99SetCreativeMode;
import net.minecraft.network.packet.Packet9Respawn;
import net.minecraft.client.controller.PlayerControllerMP;
import net.minecraft.client.effect.EntityPickupFX;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.game.entity.EntityFish;
import net.minecraft.game.entity.EntityLightningBolt;
import net.minecraft.game.entity.misc.EntityBoat;
import net.minecraft.game.entity.misc.EntityFallingSand;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.entity.misc.EntityMinecart;
import net.minecraft.game.entity.misc.EntityTNTPrimed;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.projectile.EntityEgg;
import net.minecraft.game.entity.projectile.EntityFireball;
import net.minecraft.game.entity.projectile.EntityPebble;
import net.minecraft.game.entity.projectile.EntitySnowball;
import net.minecraft.game.entity.projectile.EntityThrowablePotion;

public class NetClientHandler extends NetHandler {
	// ==================== Class fields ====================
	/** Set to true after disconnect()/handleKickDisconnect()/handleErrorMessage so that
	 *  further packet dispatch becomes a no-op. */
	private boolean disconnected = false;
	/** Low-level TCP read/write thread pool that owns the actual socket. */
	private NetworkManager netManager;
	/** Human-readable server name (kept for display in the disconnect GUI). */
	public String serverName;
	/** Back-reference to the local Minecraft client instance (used to swap GUIs,
	 *  access thePlayer, write stats, etc.). */
	private Minecraft mc;
	/** The remote-world proxy. Created in handleLogin and replaced in handleRespawn
	 *  when the player crosses dimensions. */
	private WorldClient worldClient;
	/** This flag is used to keep the GUI screen on ("loading...") until a Packet10Flying
	 *  packet is received. The first server position/rotation packet is what actually
	 *  tells us "the world is ready, drop the loading GUI". */
	private boolean hasUpdatedPosition = false;
	/** Holds per-mapId map data sent by the server via Packet131MapData. */
	public MapStorage mapStorage = new MapStorage((ISaveHandler) null);
	/** Shared RNG used for pickup pop sound pitch variation. */
	Random rand = new Random();
	// ==================== End fields ====================

	/**
	 * Opens a TCP connection to the given server. Does not start the login handshake -
	 * that happens automatically when the server sends its Packet2Handshake.
	 *
	 * @param minecraft  the local Minecraft client instance
	 * @param serverAddr hostname or IP to connect to
	 * @param port       TCP port of the remote server
	 * @throws UnknownHostException if the address cannot be resolved
	 * @throws IOException         if the socket cannot be opened
	 */
	public NetClientHandler(Minecraft minecraft, String serverAddr, int port) throws UnknownHostException, IOException {
		this.mc = minecraft;
		// Open the raw TCP socket to the server. DNS resolution is performed here.
		Socket socket = new Socket(InetAddress.getByName(serverAddr), port);
		// Wrap the socket in a NetworkManager which spawns the read/write threads
		// and dispatches incoming packets back to "this" (us) via the NetHandler API.
		this.netManager = new NetworkManager(socket, "Client", this);
	}
	
	/**
	 * Returns the underlying NetworkManager so that Minecraft.java can query its
	 * connection status (connected/disconnected).
	 *
	 * @return the NetworkManager wrapping the TCP socket
	 */
	public NetworkManager getNetworkManager() {
		return this.netManager;
	}

	/**
	 * Called every frame from Minecraft.runGameLoop(). Pumps the packet I/O threads
	 * (reading incoming data, writing outbound queue) and dispatches each decoded
	 * packet to the appropriate handle* method on this handler.
	 * After processing it wakes the I/O threads so they don't stall waiting for work.
	 */
	public void processReadPackets() {
		if (!this.disconnected) {
			this.netManager.processReadPackets();
		}

		this.netManager.wakeThreads();
	}

	// =================================================================
	// Section: Connection Handshake
	// =================================================================

	/**
	 * Handles Packet1Login - the server's response to our login attempt.
	 * This is called after either:
	 *   - We sent Packet1Login directly (singleplayer / integrated server, username = "-")
	 *   - We authenticated with minecraft.net and sent Packet1Login (multiplayer)
	 *
	 * Creates a new WorldClient proxy for the target dimension and swaps the local
	 * singleplayer world out, replacing it with the remote world. Shows a
	 * GuiDownloadTerrain screen while the client chunks are streamed in.
	 *
	 * @param loginPacket packet fields:
	 *   - dimension: the target dimension (0 = overworld, -1 = nether, 1 = end)
	 *   - protocolVersion: the entity ID assigned to our player by the server
	 */
	public void handleLogin(Packet1Login loginPacket) {
		// Replace the singleplayer PlayerControllerMP with a multiplayer-aware one.
		this.mc.playerController = new PlayerControllerMP(this.mc, this);
		// Record the "join multiplayer" achievement stat.
		this.mc.statFileWriter.readStat(StatList.joinMultiplayerStat, 1);
		// Build a fresh WorldClient for the target dimension. The WorldSettings flags
		// (survival/creative, hard/peaceful, etc.) are all false/0 here because the
		// server is the authoritative source for world state; we just mirror it.
		this.worldClient = new WorldClient(this,
				new WorldSettings(0L, 0, false, false, false, 0.0F, WorldType.DEFAULT),
				loginPacket.dimension);
		// Mark as a remote (server-driven) world so chunk loading uses the network path.
		this.worldClient.isRemote = true;
		// Swap the world's GuiScreen to "Downloading Terrain".
		this.mc.clearWorld(this.worldClient);
		// Assign the server-assigned entity ID to the local player so that entity
		// spawn/despawn packets referencing our own entity work correctly.
		this.mc.thePlayer.dimension = loginPacket.dimension;
		this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
		this.mc.thePlayer.entityId = loginPacket.protocolVersion;
	}

	// =================================================================
	// Section: Entity Spawn Handlers
	// =================================================================

	/**
	 * Handles Packet21PickupSpawn - the server tells us "an item entity is now in
	 * the world at (x,y,z) flying with velocity (rotation,pitch,roll)". We build
	 * an EntityItem with the right ItemStack and add it to the client world.
	 *
	 * @param pickupPacket contains:
	 *   - entityId, xPosition, yPosition, zPosition (fixed-point: /32.0D to get doubles)
	 *   - rotation, pitch, roll: packet-side "byte velocity" components, divided by 128.0D
	 *   - itemID, count, itemDamage: which ItemStack to give the EntityItem
	 */
	public void handlePickupSpawn(Packet21PickupSpawn pickupPacket) {
		// Position is sent as a fixed-point integer; divide by 32 to recover the double.
		double d2 = (double) pickupPacket.xPosition / 32.0D;
		double d4 = (double) pickupPacket.yPosition / 32.0D;
		double d6 = (double) pickupPacket.zPosition / 32.0D;
		// Construct the EntityItem from the world's local ItemStack registry.
		EntityItem pickupItem = new EntityItem(this.worldClient, d2, d4, d6, new ItemStack(pickupPacket.itemID,
				pickupPacket.count, pickupPacket.itemDamage));
		// Item pickup motion is encoded as 0..127 short values; /128.0D recovers the
		// signed fractional component used for entity motion vectors.
		pickupItem.motionX = (double) pickupPacket.rotation / 128.0D;
		pickupItem.motionY = (double) pickupPacket.pitch / 128.0D;
		pickupItem.motionZ = (double) pickupPacket.roll / 128.0D;
		// Stash the server's raw integer position so the entity-move reconciliation
		// code (Packet30Entity deltas) can stay in sync.
		pickupItem.serverPosX = pickupPacket.xPosition;
		pickupItem.serverPosY = pickupPacket.yPosition;
		pickupItem.serverPosZ = pickupPacket.zPosition;
		this.worldClient.addEntityToWorld(pickupPacket.entityId, pickupItem);
	}

	/**
	 * Handles Packet23VehicleSpawn - the server spawns one of many "vehicle" entity
	 * types (minecart, boat, arrow, snowball, fireball, falling sand, etc.). The packet
	 * uses a numeric "type" field to discriminate which concrete Entity class to
	 * instantiate, since they all share a common position+velocity+entityId wire format.
	 *
	 * @param vehiclePacket the type + position + (optional) thrower/motion data
	 */
	public void handleVehicleSpawn(Packet23VehicleSpawn vehiclePacket) {
		// Position conversion (fixed-point int / 32.0D to doubles).
		double posX = (double) vehiclePacket.xPosition / 32.0D;
		double posY = (double) vehiclePacket.yPosition / 32.0D;
		double posZ = (double) vehiclePacket.zPosition / 32.0D;
		Object entityToSpawn = null;

		// Switch on the "vehicle type" discriminator. The magic numbers (10,11,12,etc.)
		// match the server-side MobSpawner/EntityTracker conventions.
		switch (vehiclePacket.type) {
		case 10:
			entityToSpawn = new EntityMinecart(this.worldClient, posX, posY, posZ, 0);  // 0 = normal
			break;
		case 11:
			entityToSpawn = new EntityMinecart(this.worldClient, posX, posY, posZ, 1);  // 1 = chest
			break;
		case 12:
			entityToSpawn = new EntityMinecart(this.worldClient, posX, posY, posZ, 2);  // 2 = powered
			break;
		case 90:
			// Fishing rod bobber.
			entityToSpawn = new EntityFish(this.worldClient, posX, posY, posZ);
			break;
		case 60:
			// Shot arrow. We pass position only; velocity is applied after this switch.
			entityToSpawn = new EntityArrow(this.worldClient, posX, posY, posZ);
			break;
		case 61:
			// Snowball.
			entityToSpawn = new EntitySnowball(this.worldClient, posX, posY, posZ);
			break;
		case 63:
			// Ghast fireball: position + initial velocity. The fireball's motion is
			// sent as a fixed-point short / 8000.0D (same encoding as entity velocity).
			entityToSpawn = new EntityFireball(this.worldClient, posX, posY, posZ,
					(double) vehiclePacket.motionXencoded / 8000.0D,
					(double) vehiclePacket.motionYencoded / 8000.0D,
					(double) vehiclePacket.motionZencoded / 8000.0D);
			vehiclePacket.throwerEntityId = 0;  // No thrower for fireballs.
			break;
		case 62:
			// Thrown egg.
			entityToSpawn = new EntityEgg(this.worldClient, posX, posY, posZ);
			break;
		case 1:
			// Boat.
			entityToSpawn = new EntityBoat(this.worldClient, posX, posY, posZ);
			break;
		case 50:
			// Primed TNT about to explode.
			entityToSpawn = new EntityTNTPrimed(this.worldClient, posX, posY, posZ);
			break;
		case 70:
			// Falling sand block (gravity-affected entity).
			entityToSpawn = new EntityFallingSand(this.worldClient, posX, posY, posZ, Block.sand.blockID);
			break;
		case 71:
			entityToSpawn = new EntityFallingSand(this.worldClient, posX, posY, posZ, Block.gravel.blockID);
			break;
		case 72:
			entityToSpawn = new EntityFallingSand(this.worldClient, posX, posY, posZ, Block.cementPowder.blockID);
			break;

		// Custom InfHell-specific projectile types.

		case 100:
			// Thrown pebble (custom projectile).
			entityToSpawn = new EntityPebble(this.worldClient, posX, posY, posZ);
			break;

		case 101:
			// Throwable potion: the metadata field carries the potion damage value.
			entityToSpawn = new EntityThrowablePotion(this.worldClient, posX, posY, posZ,
					vehiclePacket.metadata);
			break;
		}

		// If a concrete Entity was constructed, finalize its server-state fields and
		// insert it into the WorldClient's entity map.
		if (entityToSpawn != null) {
			// Stash the raw integer server position for delta reconciliation.
			((Entity) entityToSpawn).serverPosX = vehiclePacket.xPosition;
			((Entity) entityToSpawn).serverPosY = vehiclePacket.yPosition;
			((Entity) entityToSpawn).serverPosZ = vehiclePacket.zPosition;
			// Spawned vehicles have not yet received a rotation packet; default to upright.
			((Entity) entityToSpawn).rotationYaw = 0.0F;
			((Entity) entityToSpawn).rotationPitch = 0.0F;
			((Entity) entityToSpawn).entityId = vehiclePacket.entityId;
			this.worldClient.addEntityToWorld(vehiclePacket.entityId, (Entity) entityToSpawn);
			// If the thrower is present (e.g. an arrow fired by a player) hook it up
			// so the client can play the correct owner damage / knockback effects.
			if (vehiclePacket.throwerEntityId > 0) {
				switch (vehiclePacket.type) {
				case 60:
					// Arrow: look up the shooter so the arrow tracks + damages them.
					Entity entity = this.getEntityByID(vehiclePacket.throwerEntityId);
					if (entity instanceof EntityLiving) {
						((EntityArrow) entityToSpawn).shootingEntity = (EntityLiving) entity;
					}
					break;
				case 101:
					// Throwable potion: store the thrower for the same reason.
					Entity thrower = this.getEntityByID(vehiclePacket.throwerEntityId);
					if (thrower instanceof EntityLiving) {
						((EntityThrowablePotion) entityToSpawn).thrower = (EntityLiving) thrower;
					}
					break;
				}

				// Apply the initial velocity to the projectile (motion encoded as
				// fixed-point short / 8000.0D, same as Packet28EntityVelocity).
				((Entity) entityToSpawn).setVelocity((double) vehiclePacket.motionXencoded / 8000.0D,
						(double) vehiclePacket.motionYencoded / 8000.0D,
						(double) vehiclePacket.motionZencoded / 8000.0D);
			}
		}

	}

	/**
	 * Handles Packet71Weather - a single lightning bolt entity (or rain/snow particle
	 * effect area, represented by a temporary entity). The server tells us where the
	 * strike happened so the client can play the flash+thunder sound and render the bolt.
	 *
	 * @param weatherPacket fields:
	 *   - lightning: 1 = spawn a lightning bolt entity, 0 = no bolt (rain continues)
	 *   - encodedPosX/Y/Z: fixed-point position (/32.0D)
	 *   - entityID: server-assigned entity ID for the lightning bolt
	 */
	public void handleWeather(Packet71Weather weatherPacket) {
		double d2 = (double) weatherPacket.encodedPosX / 32.0D;
		double d4 = (double) weatherPacket.encodedPosY / 32.0D;
		double d6 = (double) weatherPacket.encodedPosZ / 32.0D;
		EntityLightningBolt lightning = null;
		// Only spawn a bolt entity if the server explicitly asks for it.
		if (weatherPacket.lightning == 1) {
			lightning = new EntityLightningBolt(this.worldClient, d2, d4, d6);
		}

		if (lightning != null) {
			// Record raw server position for delta reconciliation.
			lightning.serverPosX = weatherPacket.encodedPosX;
			lightning.serverPosY = weatherPacket.encodedPosY;
			lightning.serverPosZ = weatherPacket.encodedPosZ;
			lightning.rotationYaw = 0.0F;
			lightning.rotationPitch = 0.0F;
			lightning.entityId = weatherPacket.entityID;
			// Weather effects are not part of the normal entity map but do trigger
			// world-level sound/render effects.
			this.worldClient.addWeatherEffect(lightning);
		}

	}

	/**
	 * Handles Packet25EntityPainting - places a hanging painting entity (by a player
	 * using the item) into the world at the given coordinates facing the specified
	 * direction. The painting's art title determines which texture to display.
	 *
	 * @param paintingPacket fields:
	 *   - xPosition, yPosition, zPosition: block coords of the nail block (the
	 *     block the painting hangs from)
	 *   - direction: 0=+X, 1=-X, 2=+Z, 3=-Z (which wall face the painting faces)
	 *   - title: the art resource key, e.g. "Kebab", "Pool", "Creebet"
	 *   - entityId: server-assigned entity ID for the painting
	 */
	public void handleEntityPainting(Packet25EntityPainting paintingPacket) {
		EntityPainting entityPainting2 = new EntityPainting(this.worldClient, paintingPacket.xPosition,
				paintingPacket.yPosition, paintingPacket.zPosition, paintingPacket.direction,
				paintingPacket.title);
		this.worldClient.addEntityToWorld(paintingPacket.entityId, entityPainting2);
	}

	/**
	 * Handles Packet28EntityVelocity - applies a one-time impulse to an entity
	 * (e.g. when hit, pushed by water, or blown by an explosion). The velocity is
	 * sent as fixed-point components in the range [-32768..32767] and divided by
	 * 8000.0D to recover the per-tick velocity multiplier.
	 *
	 * @param velPacket fields: entityId, motionX, motionY, motionZ
	 */
	public void handleEntityVelocity(Packet28EntityVelocity velPacket) {
		Entity entity = this.getEntityByID(velPacket.entityId);
		if (entity != null) {
			// Same encoding as vehicle spawn motion: signed short / 8000.0D.
			entity.setVelocity((double) velPacket.motionX / 8000.0D,
					(double) velPacket.motionY / 8000.0D,
					(double) velPacket.motionZ / 8000.0D);
		}
	}

	/**
	 * Handles Packet40EntityMetadata - streams a list of "watchable object" data
	 * for an entity into its DataWatcher. Used to sync things like health, horse
	 * flags, horse stats, painting variant, etc. without a dedicated packet per field.
	 *
	 * @param metaPacket fields: entityId, and a DataWatcher payload read from the packet
	 */
	public void handleEntityMetadata(Packet40EntityMetadata metaPacket) {
		Entity entity = this.getEntityByID(metaPacket.entityId);
		if (entity != null && metaPacket.getMetadata() != null) {
			// updateWatchedObjectsFromList() merges the received data into the entity's
			// DataWatcher, firing field-change callbacks as needed.
			entity.getDataWatcher().updateWatchedObjectsFromList(metaPacket.getMetadata());
		}

	}

	/**
	 * Handles Packet20NamedEntitySpawn - spawns another player's avatar (an
	 * EntityOtherPlayerMP). Used for every human player visible to us in the world.
	 *
	 * @param spawnPacket fields:
	 *   - entityId: server-assigned entity ID for this player
	 *   - name: the player's username
	 *   - xPosition, yPosition, zPosition: fixed-point position (/32.0D)
	 *   - rotation: yaw as 0..255 byte angle (multiply by 360/256 for degrees)
	 *   - pitch: pitch as 0..255 byte angle (multiply by 360/256 for degrees)
	 *   - currentItem: the item ID held in the player's main hand (0 = empty)
	 */
	public void handleNamedEntitySpawn(Packet20NamedEntitySpawn spawnPacket) {
		// Position from fixed-point integer to double.
		double d2 = (double) spawnPacket.xPosition / 32.0D;
		double d4 = (double) spawnPacket.yPosition / 32.0D;
		double d6 = (double) spawnPacket.zPosition / 32.0D;
		// Byte angle to degrees: 0..255 -> 0..360 degrees.
		float f8 = (float) (spawnPacket.rotation * 360) / 256.0F;
		float f9 = (float) (spawnPacket.pitch * 360) / 256.0F;
		EntityOtherPlayerMP otherPlayer = new EntityOtherPlayerMP(this.mc.theWorld,
				spawnPacket.name);
		// Initialise both last-tick and previous-position to the spawn position so
		// interpolation from tick 0 looks correct.
		otherPlayer.prevPosX = otherPlayer.lastTickPosX = (double) (otherPlayer.serverPosX = spawnPacket.xPosition);
		otherPlayer.prevPosY = otherPlayer.lastTickPosY = (double) (otherPlayer.serverPosY = spawnPacket.yPosition);
		otherPlayer.prevPosZ = otherPlayer.lastTickPosZ = (double) (otherPlayer.serverPosZ = spawnPacket.zPosition);
		// If the player is holding an item, put it in their selected hotbar slot.
		int i11 = spawnPacket.currentItem;
		if (i11 == 0) {
			otherPlayer.inventory.mainInventory[otherPlayer.inventory.currentItem] = null;
		} else {
			otherPlayer.inventory.mainInventory[otherPlayer.inventory.currentItem] = new ItemStack(
					i11, 1, 0);
		}

		otherPlayer.setPositionAndRotation(d2, d4, d6, f8, f9);
		this.worldClient.addEntityToWorld(spawnPacket.entityId, otherPlayer);
	}

	/**
	 * Handles Packet34EntityTeleport - a "hard" teleport: the entity is placed at
	 * an absolute world position (rather than a relative delta from the last known
	 * position). Used when the entity has moved too far for normal delta compression.
	 * The extra +0.015625D on the Y coordinate corrects for a sub-block alignment
	 * adjustment in the server's position math.
	 *
	 * @param teleportPacket fields:
	 *   - entityId: which entity to teleport
	 *   - xPosition, yPosition, zPosition: fixed-point absolute position (/32.0D)
	 *   - yaw, pitch: 0..255 byte angles converted to degrees (/256.0F * 360)
	 */
	public void handleEntityTeleport(Packet34EntityTeleport teleportPacket) {
		Entity entity = this.getEntityByID(teleportPacket.entityId);
		if (entity != null) {
			entity.serverPosX = teleportPacket.xPosition;
			entity.serverPosY = teleportPacket.yPosition;
			entity.serverPosZ = teleportPacket.zPosition;
			// Fixed-point to double, with the Y correction (0.015625 = 1/64, a half-step).
			double x = (double) entity.serverPosX / 32.0D;
			double y = (double) entity.serverPosY / 32.0D + 0.015625D;
			double z = (double) entity.serverPosZ / 32.0D;
			// Byte angles to degrees.
			float yaw = (float) (teleportPacket.yaw * 360) / 256.0F;
			float pitch = (float) (teleportPacket.pitch * 360) / 256.0F;
			// setPositionAndRotation2 queues the interpolation; the final parameter (3)
			// is the number of ticks over which to smooth the transition.
			entity.setPositionAndRotation2(x, y, z, yaw, pitch, 3);
		}
	}

	/**
	 * Handles Packet30Entity - a "soft" relative update: the entity's position and
	 * rotation are adjusted by small signed deltas from the last confirmed server
	 * position. This is the standard update path for nearby entities each tick.
	 * If the "rotating" flag is set, yaw and pitch are sent as 0..255 byte angles;
	 * otherwise the entity keeps its previous rotation.
	 *
	 * @param entityPacket fields:
	 *   - entityId: which entity to update
	 *   - xPosition, yPosition, zPosition: signed byte deltas to apply to serverPos
	 *   - rotating: whether yaw/pitch are included in this packet
	 *   - yaw, pitch: present only if rotating==true (byte angles 0..255)
	 */
	public void handleEntity(Packet30Entity entityPacket) {
		Entity entity = this.getEntityByID(entityPacket.entityId);
		if (entity != null) {
			// Apply the signed delta directly to the raw integer server position,
			// then reconvert to a double for the entity's actual posX/Y/Z fields.
			entity.serverPosX += entityPacket.xPosition;
			entity.serverPosY += entityPacket.yPosition;
			entity.serverPosZ += entityPacket.zPosition;
			double x = (double) entity.serverPosX / 32.0D;
			double y = (double) entity.serverPosY / 32.0D;
			double z = (double) entity.serverPosZ / 32.0D;
			// If no rotation update, keep the entity's current yaw/pitch.
			float yaw = entityPacket.rotating ? (float) (entityPacket.yaw * 360) / 256.0F : entity.rotationYaw;
			float pitch = entityPacket.rotating ? (float) (entityPacket.pitch * 360) / 256.0F
					: entity.rotationPitch;
			entity.setPositionAndRotation2(x, y, z, yaw, pitch, 3);
		}
	}

	/**
	 * Handles Packet29DestroyEntity - removes an entity from the world immediately.
	 * Called when the entity has died, been collected, left the server's tracking
	 * range, or been consumed by a portal.
	 *
	 * @param destroyPacket field: entityId - the entity to remove from the world
	 */
	public void handleDestroyEntity(Packet29DestroyEntity destroyPacket) {
		this.worldClient.removeEntityFromWorld(destroyPacket.entityId);
	}

	// =================================================================
	// Section: World State Update Handlers
	// =================================================================

	/**
	 * Handles Packet10Flying - the server acknowledges the player's current
	 * position and rotation each tick. This packet both confirms the client's
	 * position and carries the server's authoritative world state snapshot for
	 * that tick (e.g. if the player was pushed by water). The client echoes
	 * the packet back to the server to confirm receipt.
	 *
	 * The first Packet10Flying received after handleLogin dismisses the
	 * "Downloading Terrain" GUI (hasUpdatedPosition flip).
	 *
	 * @param flyingPacket fields:
	 *   - moving: if true, xPosition/yPosition/zPosition contain the new position
	 *   - rotating: if true, yaw/pitch contain the new rotation
	 *   - stance: the player's "stance" (posY while jumping/crouching)
	 */
	public void handleFlying(Packet10Flying flyingPacket) {
		EntityPlayerSP thePlayer = this.mc.thePlayer;
		// Snapshot the player's current position before the packet is applied.
		double d3 = thePlayer.posX;
		double d5 = thePlayer.posY;
		double d7 = thePlayer.posZ;
		float f9 = thePlayer.rotationYaw;
		float f10 = thePlayer.rotationPitch;
		if (flyingPacket.moving) {
			// Only the first three fields are used for position; ySize is reset below.
			d3 = flyingPacket.xPosition;
			d5 = flyingPacket.yPosition;
			d7 = flyingPacket.zPosition;
		}

		if (flyingPacket.rotating) {
			f9 = flyingPacket.yaw;
			f10 = flyingPacket.pitch;
		}

		// ySize compensates for the player entity's half-block eye-height offset.
		thePlayer.ySize = 0.0F;
		// Clear any client-predicted motion so the server position takes over.
		thePlayer.motionX = thePlayer.motionY = thePlayer.motionZ = 0.0D;
		// Apply the confirmed server position to the local player entity.
		thePlayer.setPositionAndRotation(d3, d5, d7, f9, f10);
		// Reflect the confirmed position back into the packet so the server knows
		// we received it and can do ping/cheat detection.
		flyingPacket.xPosition = thePlayer.posX;
		flyingPacket.yPosition = thePlayer.boundingBox.minY;
		flyingPacket.zPosition = thePlayer.posZ;
		flyingPacket.stance = thePlayer.posY;
		this.netManager.addToSendQueue(flyingPacket);
		// First flying packet after login = terrain is ready, drop the loading GUI.
		if (!this.hasUpdatedPosition) {
			this.mc.thePlayer.prevPosX = this.mc.thePlayer.posX;
			this.mc.thePlayer.prevPosY = this.mc.thePlayer.posY;
			this.mc.thePlayer.prevPosZ = this.mc.thePlayer.posZ;
			this.hasUpdatedPosition = true;
			this.mc.displayGuiScreen((GuiScreen) null);
		}

	}

	/**
	 * Handles Packet50PreChunk - announces that a chunk column at (x, z) will
	 * soon be needed by the client. The server sends this before Packet51MapChunk
	 * so the client can allocate the chunk data structure and start mesh building.
	 * If mode==1 the chunk is loaded; if mode==0 it is unloaded.
	 *
	 * @param chunkPacket fields: xPosition (=chunkX), yPosition (=chunkZ), mode (1=load/0=unload)
	 */
	public void handlePreChunk(Packet50PreChunk chunkPacket) {
		this.worldClient.doPreChunk(chunkPacket.xPosition, chunkPacket.yPosition, chunkPacket.mode);
	}

	/**
	 * Handles Packet52MultiBlockChange - updates multiple blocks inside a single
	 * 16x16 chunk column slice in one packet. The block IDs and metadata are packed
	 * into a binary blob inside the packet. The blob is decoded as a series of
	 * (short encodedPos, short encodedBlockMeta) pairs.
	 *
	 *   - encodedPos  (16-bit): upper 4 bits = x (0..15), next 4 bits = z (0..15),
	 *                         lower 8 bits = y (0..255)
	 *   - encodedBlockMeta (16-bit): upper 8 bits = block ID (0..255), lower 8 bits = metadata
	 *
	 * This replaces multiple Packet53BlockChange packets and is used for things like
	 * TNT detonation chains, world generation, and mass block updates.
	 *
	 * @param multiBlockPacket fields:
	 *   - xPosition, zPosition: chunk coordinates (multiply by 16 to get world block coords)
	 *   - size: number of block records in the encoded blob
	 *   - encodedBlocks: binary blob containing (encodedPos, encodedBlockMeta) pairs
	 */
	public void handleMultiBlockChange(Packet52MultiBlockChange multiBlockPacket) {
		// World block coordinates: chunk coords * 16 = first block in that chunk column.
		int x0 = multiBlockPacket.xPosition * 16;
		int z0 = multiBlockPacket.zPosition * 16;

		if(multiBlockPacket.encodedBlocks != null) {
			// Wrap the byte array in a DataInputStream for big-endian reading of shorts.
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(multiBlockPacket.encodedBlocks));

			try {
				// Loop over each block record in the blob.
				for(int i = 0; i < multiBlockPacket.size; ++i) {
					// Read two signed shorts per block: position encoding + block/meta encoding.
					short encodedPos = dis.readShort();
					short encodedBlockMeta = dis.readShort();

					// Unpack block ID from upper 8 bits of the metadata short: (val >> 8) & 255.
					int blockID = (encodedBlockMeta >> 8) & 255;
					// Unpack block metadata from lower 8 bits: val & 255.
					int metadata = encodedBlockMeta & 255;

					// Decode the 16-bit position short into x, z, y coordinates:
					//   x = bits 15..12  (encodedPos >> 12 & 15)
					//   z = bits 11..8   (encodedPos >> 8  & 15)
					//   y = bits  7..0   (encodedPos       & 255)
					int x = encodedPos >> 12 & 15;
					int z = encodedPos >> 8 & 15;
					int y = encodedPos & 255;

					// Apply the block change to the world (also invalidates the render chunk).
					this.worldClient.setBlockAndMetadataAndInvalidate(x + x0, y, z + z0, blockID, metadata);
				}
			} catch (IOException iOException13) {
				// Fail silently - chunk data corruption will be resolved by a later resync.
				System.out.println ("IO Exception");
				iOException13.printStackTrace ();
			}

		}
	}

	/**
	 * Handles Packet51MapChunk - raw compressed chunk data for a 16x16x256 block
	 * column (or a sub-volume within it). The data is a zlib-compressed NBT-like
	 * blob containing block IDs, sky-light, block-light, and biome data.
	 *
	 * @param mapChunkPacket fields:
	 *   - xPosition, zPosition: chunk coordinates
	 *   - yPosition, xSize, ySize, zSize: sub-volume origin and size (usually full)
	 *   - chunk: raw compressed chunk data
	 */
	public void handleMapChunk(Packet51MapChunk mapChunkPacket) {
		this.worldClient.setChunkData(mapChunkPacket.xPosition, mapChunkPacket.yPosition,
				mapChunkPacket.zPosition, mapChunkPacket.xSize, mapChunkPacket.ySize, mapChunkPacket.zSize,
				mapChunkPacket.chunk);
	}

	/**
	 * Handles Packet53BlockChange - a single block change at an absolute world
	 * coordinate. The most common update: client sends a block-interact packet,
	 * server validates it, then broadcasts this to all nearby players.
	 *
	 * @param blockChangePacket fields:
	 *   - xPosition, yPosition, zPosition: absolute world block coordinates
	 *   - type: new block ID
	 *   - metadata: new block metadata value
	 */
	public void handleBlockChange(Packet53BlockChange blockChangePacket) {
		this.worldClient.setBlockAndMetadataAndInvalidate(blockChangePacket.xPosition,
				blockChangePacket.yPosition, blockChangePacket.zPosition, blockChangePacket.type,
				blockChangePacket.metadata);
	}

	// =================================================================
	// Section: Lifecycle / Disconnect Handlers
	// =================================================================

	/**
	 * Handles Packet255KickDisconnect - the server is kicking the player out with
	 * a reason string. Shuts down the connection, tears down the world, and shows
	 * the "Disconnected" GUI with the given reason.
	 *
	 * @param kickPacket field: reason - the human-readable kick message
	 */
	public void handleKickDisconnect(Packet255KickDisconnect kickPacket) {
		// Tell the network manager to close the connection (sends a final TCP shutdown
		// and frees the I/O threads).
		this.netManager.networkShutdown("disconnect.kicked", new Object[0]);
		this.disconnected = true;
		// Tear down the remote world and switch back to the "no world" state.
		this.mc.clearWorld((World) null);
		// Show the disconnected GUI with the reason text translated by the client.
		this.mc.displayGuiScreen(new GuiDisconnected("disconnect.disconnected", "disconnect.genericReason",
				new Object[] { kickPacket.reason }));
	}

	/**
	 * Called by the network layer when the socket has been closed unexpectedly
	 * (or the server timed out the connection). Shows a "Connection lost" GUI.
	 * No-op if the player is already considered disconnected.
	 *
	 * @param message i18n key for the disconnect reason
	 * @param args    format arguments for the i18n string
	 */
	public void handleErrorMessage(String message, Object[] args) {
		if (!this.disconnected) {
			this.disconnected = true;
			this.mc.clearWorld((World) null);
			this.mc.displayGuiScreen(new GuiDisconnected("disconnect.lost", message, args));
		}
	}

	/**
	 * Sends the given packet and then immediately shuts the connection down cleanly.
	 * Used for graceful exit scenarios (e.g. disconnect from menu, kick a player, etc).
	 * No-op if we are already disconnected.
	 *
	 * @param packet the final packet to send before closing the socket
	 */
	public void quitWithPacket(Packet packet) {
		if (!this.disconnected) {
			this.netManager.addToSendQueue(packet);
			this.netManager.serverShutdown();
		}
	}

	/**
	 * Enqueues a packet for sending to the server. No-op if the connection has
	 * already been torn down. Most gameplay code uses this to send movement,
	 * chat, block-interact, window-click, etc.
	 *
	 * @param packet the packet to send
	 */
	public void addToSendQueue(Packet packet) {
		if (!this.disconnected) {
			this.netManager.addToSendQueue(packet);
		}
	}

	// =================================================================
	// Section: Event Handlers (chat, animation, sleep, collect, attach, status)
	// =================================================================

	/**
	 * Handles Packet22Collect - a player (or other living entity) has just picked up
	 * an item entity. Plays the pickup pop sound and a particle effect, and removes
	 * the item entity from the world.
	 *
	 * @param collectPacket fields:
	 *   - collectedEntityId: the item entity being picked up
	 *   - collectorEntityId: the entity that picked it up
	 */
	public void handleCollect(Packet22Collect collectPacket) {
		Entity entity = this.getEntityByID(collectPacket.collectedEntityId);
		Object collector = (EntityLiving) this.getEntityByID(collectPacket.collectorEntityId);
		// If the collector is missing (e.g. a player who has just left the server's
		// tracking range), assume it's us - the local player.
		if (collector == null) {
			collector = this.mc.thePlayer;
		}

		if (entity != null) {
			// Play the "random.pop" sound at the item's position with a randomized pitch
			// for variation: (rand - rand) * 0.7 + 1.0 spans [0.3, 1.7] roughly, then * 2.
			this.worldClient.playSoundAtEntity(entity, "random.pop", 0.2F,
					((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			// Spawn the visual pickup particle (a small white box that flies into the collector).
			this.mc.effectRenderer.addEffect(new EntityPickupFX(this.mc.theWorld, entity, (Entity) collector, -0.5F));
			// Remove the item entity now that it has been collected.
			this.worldClient.removeEntityFromWorld(collectPacket.collectedEntityId);
		}

	}

	/**
	 * Handles Packet3Chat - shows the server-sent message in the in-game chat HUD.
	 *
	 * @param chatPacket field: message - the raw chat string (may include formatting codes)
	 */
	public void handleChat(Packet3Chat chatPacket) {
		this.mc.ingameGUI.addChatMessage(chatPacket.message);
	}

	/**
	 * Handles Packet18Animation - plays a one-shot animation on an entity.
	 * The animation type is encoded as:
	 *   1 = swing arm
	 *   2 = hurt flash
	 *   3 = wake up (players only)
	 *   4 = critical hit / "magic" crit effect (players only)
	 *   5 = unknown (no-op for other-player class)
	 *
	 * @param animPacket fields: entityId, animate (animation type)
	 */
	public void handleAnimation(Packet18Animation animPacket) {
		System.out.println ("Got packet 18 animation " + animPacket.animate);
		Entity entity = this.getEntityByID(animPacket.entityId);
		if (entity != null) {
			EntityPlayer entityPlayer;
			if (animPacket.animate == 1) {
				// Arm swing (e.g. mining or attacking).
				entityPlayer = (EntityPlayer) entity;
				entityPlayer.swingItem();
			} else if (animPacket.animate == 2) {
				// Generic hurt visual (red flash + knockback animation).
				entity.performHurtAnimation();
			} else if (animPacket.animate == 3) {
				// Wake from bed.
				entityPlayer = (EntityPlayer) entity;
				entityPlayer.wakeUpPlayer(false, false, false);
			} else if (animPacket.animate == 4) {
				// Magic critical effect (player-only particles).
				entityPlayer = (EntityPlayer) entity;
				entityPlayer.func_6420_o();
			} else if(animPacket.animate == 5 && entity instanceof EntityOtherPlayerMP) {
				; // Reserved animation slot - no client-side behavior.
			}

		}
	}

	/**
	 * Handles Packet17Sleep - the server tells a player to enter or leave their bed.
	 * If field_22046_e == 0 the player is going to sleep; the client shows the bed
	 * camera transition. The other field values are no-ops on the client side.
	 *
	 * @param sleepPacket fields:
	 *   - entityID: the player going to sleep
	 *   - bedX, bedY, bedZ: the bed's block coordinates
	 *   - field_22046_e: action code (0 = enter bed)
	 */
	public void handleSleep(Packet17Sleep sleepPacket) {
		Entity entity = this.getEntityByID(sleepPacket.entityID);
		if (entity != null) {
			if (sleepPacket.field_22046_e == 0) {
				// Make the player "lay down" visually and start the bed-screen render.
				EntityPlayer entityPlayer = (EntityPlayer) entity;
				entityPlayer.sleepInBedAt(sleepPacket.bedX, sleepPacket.bedY, sleepPacket.bedZ);
			}

		}
	}

	/**
	 * Handles Packet2Handshake - the server's first message after the TCP connection
	 * is established. The "username" field is actually a server-issued connection
	 * string (hash) used to verify the client against the minecraft.net session API.
	 * If the username is "-" we are talking to an integrated server (singleplayer)
	 * and skip authentication entirely; otherwise we POST the credentials to
	 * minecraft.net to validate the session.
	 */
	public void handleHandshake(Packet2Handshake handshakePacket) {
		if (handshakePacket.username.equals("-")) {
			// Singleplayer / integrated: skip auth, send login immediately.
			this.addToSendQueue(new Packet1Login(this.mc.session.username, 14));
		} else {
			try {
				// Multiplayer: hit minecraft.net's joinserver.jsp to validate that this
				// session owns the username on this server connection string.
				URL url = new URL("http://www.minecraft.net/game/joinserver.jsp?user=" + this.mc.session.username
						+ "&sessionId=" + this.mc.session.sessionId + "&serverId=" + handshakePacket.username);
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				String response = reader.readLine();
				reader.close();
				if (response.equalsIgnoreCase("ok")) {
					// Auth succeeded - send our login packet.
					this.addToSendQueue(new Packet1Login(this.mc.session.username, 14));
				} else {
					// Auth failed: kill the connection and show a disconnected GUI.
					this.netManager.networkShutdown("disconnect.loginFailedInfo", new Object[] { response });
				}
			} catch (Exception e) {
				// Network error talking to minecraft.net: log and disconnect.
				e.printStackTrace();
				this.netManager.networkShutdown("disconnect.genericReason",
						new Object[] { "Internal client error: " + e.toString() });
			}
		}

	}

	/**
	 * Closes the connection locally without sending any final packet. Called by
	 * the menu/UI when the user cancels the connection or the client is shutting
	 * down gracefully.
	 */
	public void disconnect() {
		this.disconnected = true;
		this.netManager.wakeThreads();
		this.netManager.networkShutdown("disconnect.closed", new Object[0]);
	}

	/**
	 * Handles Packet24MobSpawn - spawns a living mob (a non-player EntityLiving)
	 * in the world. The mob class is looked up by numeric type from the entity
	 * registry. The initial DataWatcher metadata (health, name, status flags, etc.)
	 * is applied to the entity after construction.
	 *
	 * @param mobPacket fields:
	 *   - type: numeric entity type ID
	 *   - entityId: server-assigned entity ID
	 *   - xPosition, yPosition, zPosition: fixed-point position (/32.0D)
	 *   - yaw, pitch: 0..255 byte angles
	 *   - additional metadata: DataWatcher payload (optional)
	 */
	public void handleMobSpawn(Packet24MobSpawn mobPacket) {
		double d2 = (double) mobPacket.xPosition / 32.0D;
		double d4 = (double) mobPacket.yPosition / 32.0D;
		double d6 = (double) mobPacket.zPosition / 32.0D;
		float f8 = (float) (mobPacket.yaw * 360) / 256.0F;
		float f9 = (float) (mobPacket.pitch * 360) / 256.0F;
		// Look up the concrete Entity class from the entity registry and instantiate.
		EntityLiving mob = (EntityLiving) EntityList.createEntityByID(mobPacket.type, this.mc.theWorld);
		mob.serverPosX = mobPacket.xPosition;
		mob.serverPosY = mobPacket.yPosition;
		mob.serverPosZ = mobPacket.zPosition;
		mob.entityId = mobPacket.entityId;
		mob.setPositionAndRotation(d2, d4, d6, f8, f9);
		// Flag this as a network-spawned mob (used for things like name visibility
		// in the tab list, death drops, etc).
		mob.isMultiplayerEntity = true;
		this.worldClient.addEntityToWorld(mobPacket.entityId, mob);
		// Apply any datawatcher metadata the server sent with the spawn packet.
		List<WatchableObject> metadata = mobPacket.getMetadata();
		if (metadata != null) {
			mob.getDataWatcher().updateWatchedObjectsFromList(metadata);
		}

	}

	/**
	 * Handles Packet4UpdateTime - the server-authoritative world time. The
	 * overworld time is exposed to the renderer for sky color and ambient light.
	 *
	 * @param timePacket field: time - the current world time in ticks
	 */
	public void handleUpdateTime(Packet4UpdateTime timePacket) {
		this.mc.theWorld.setWorldTime(timePacket.time);
	}

	/**
	 * Handles Packet6SpawnPosition - sets the world's "spawn point" (compass needle
	 * target and respawn location). Sent once after login and any time a player
	 * sleeps in a bed the first time.
	 *
	 * @param spawnPosPacket fields: xPosition, yPosition, zPosition - spawn block coords
	 */
	public void handleSpawnPosition(Packet6SpawnPosition spawnPosPacket) {
		this.mc.thePlayer.setPlayerSpawnCoordinate(new ChunkCoordinates(spawnPosPacket.xPosition,
				spawnPosPacket.yPosition, spawnPosPacket.zPosition));
		this.mc.theWorld.getWorldInfo().setSpawn(spawnPosPacket.xPosition, spawnPosPacket.yPosition,
				spawnPosPacket.zPosition);
	}

	/**
	 * Handles Packet39AttachEntity - sets the vehicle (mount) of an entity, or
	 * un-mounts if vehicleEntityId == -1. Used for pigs, horses, minecarts, boats.
	 *
	 * @param attachPacket fields:
	 *   - entityId: the rider
	 *   - vehicleEntityId: the mount (-1 = dismount)
	 */
	public void handleAttachEntity(Packet39AttachEntity attachPacket) {
		Object entity = this.getEntityByID(attachPacket.entityId);
		Entity vehicle = this.getEntityByID(attachPacket.vehicleEntityId);
		// The local player is not in the world's entity map (it's a special
		// EntityPlayerSP), so substitute it explicitly if needed.
		if (attachPacket.entityId == this.mc.thePlayer.entityId) {
			entity = this.mc.thePlayer;
		}

		if (entity != null) {
			// mountEntity(null) detaches the entity from its current vehicle.
			((Entity) entity).mountEntity(vehicle);
		}
	}

	/**
	 * Handles Packet38EntityStatus - a one-byte status code sent to a specific
	 * entity. Examples: entity death, hurt animation toggle, breeding heart
	 * particles, taming hearts, etc. The meaning is per-entity-class and the
	 * entity's own handleHealthUpdate() decides what to do.
	 *
	 * @param statusPacket fields: entityId, entityStatus (byte code)
	 */
	public void handleEntityStatus(Packet38EntityStatus statusPacket) {
		Entity entity = this.getEntityByID(statusPacket.entityId);
		if (entity != null) {
			entity.handleHealthUpdate(statusPacket.entityStatus);
		}

	}

	/**
	 * Helper that resolves an entity by its server-assigned integer ID.
	 * The local player (EntityPlayerSP) is not in the world's entity list, so
	 * the special case is handled here.
	 *
	 * @param entityId the entity ID to look up
	 * @return the matching Entity, or null if no entity with that ID is known
	 */
	private Entity getEntityByID(int entityId) {
		return (Entity) (entityId == this.mc.thePlayer.entityId ? this.mc.thePlayer : this.worldClient.getEntityByID(entityId));
	}

	// =================================================================
	// Section: Health / Respawn / Explosion
	// =================================================================

	/**
	 * Handles Packet8UpdateHealth - the server updates the local player's HP and
	 * hunger values. Triggered by damage, healing, hunger tick changes, and similar
	 * events.
	 *
	 * @param healthPacket field: healthMP - the new HP/hunger snapshot
	 */
	public void handleUpdateHealth(Packet8UpdateHealth healthPacket) {
		this.mc.thePlayer.setHealth(healthPacket.healthMP);
	}

	public void handleRespawn(Packet9Respawn respawnPacket) {
		// Cross-dimension respawn requires tearing down the worldClient and re-creating
		// it for the new dimension. Within the same dimension, just respawn the player.
		if (respawnPacket.dimension != this.mc.thePlayer.dimension) {
			// Re-show the "Downloading Terrain" GUI for the new dimension.
			this.hasUpdatedPosition = false;
			this.worldClient = new WorldClient(this, new WorldSettings(0L, 0, false, false, false, 0.0F, WorldType.DEFAULT),
					respawnPacket.dimension);
			this.worldClient.isRemote = true;
			this.mc.clearWorld(this.worldClient);
			this.mc.thePlayer.dimension = respawnPacket.dimension;
			// Record the death coordinate so the player can be shown a "you died here" arrow.
			this.mc.thePlayer.setPlayerLastDeathCoordinate(new ChunkCoordinates(respawnPacket.lastDeathX, respawnPacket.lastDeathY, respawnPacket.lastDeathZ));
			this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
		}

		// Restore the player entity (HP, hunger, position, etc.) for the new dimension.
		this.mc.respawn(true, respawnPacket.dimension);
	}

	/**
	 * Handles Packet60Explosion - the server tells the client an explosion happened
	 * (typically distant from the player, so the explosion wasn't run locally).
	 * We don't recreate damage/knockback - just the visual/audio effect and the
	 * list of block positions that were destroyed.
	 *
	 * @param explosionPacket fields:
	 *   - explosionX/Y/Z: absolute world coords of the explosion center
	 *   - explosionSize: radius
	 *   - blockID: the block placed in the air briefly (vanilla: nothing, custom mod uses this)
	 *   - destroyedBlockPositions: list of block coords removed by the explosion
	 */
	public void handleExplosion(Packet60Explosion explosionPacket) {
		// Pass null for the entity parameter: we are a remote observer, not the
		// entity that caused the explosion.
		Explosion explosion2 = new Explosion(this.mc.theWorld, (Entity) null, explosionPacket.explosionX,
				explosionPacket.explosionY, explosionPacket.explosionZ, explosionPacket.explosionSize,
				explosionPacket.blockID);
		// Apply the server-provided block destruction list directly.
		explosion2.destroyedBlockPositions = explosionPacket.destroyedBlockPositions;
		// doEffects(true) plays the explosion sound/particles and removes the blocks
		// from the world, but does NOT apply damage/knockback (those are local).
		explosion2.doEffects(true);
	}

	// =================================================================
	// Section: Container / Window Handlers
	// =================================================================

	/**
	 * Handles Packet100OpenWindow - the server tells us to open a container GUI.
	 * The "inventoryType" discriminator selects which container implementation to
	 * use (vanilla chest=0, workbench=1, furnace=2, dispenser=3, custom trading=6).
	 * In all cases the new window ID is recorded so subsequent SetSlot/WindowItems
	 * packets can be routed correctly.
	 *
	 * @param windowPacket fields:
	 *   - windowId: server-assigned window ID
	 *   - inventoryType: 0=chest, 1=workbench, 2=furnace, 3=dispenser, 6=trader
	 *   - windowTitle: localized title (or trader name for the trading GUI)
	 *   - slotsCount: number of slots in the container (reused as currency selector for trading)
	 */
	public void handleOpenWindow(Packet100OpenWindow windowPacket) {
		if (windowPacket.inventoryType == 0) {
			// Vanilla chest: create a generic inventory with the given title + slot count.
			InventoryBasic inventory = new InventoryBasic(windowPacket.windowTitle,
					windowPacket.slotsCount);
			this.mc.thePlayer.displayGUIChest(inventory);
			this.mc.thePlayer.craftingInventory.windowId = windowPacket.windowId;
		} else if (windowPacket.inventoryType == 2) {
			// Furnace GUI: a real TileEntityFurnace is created; the server will
			// fill it with the actual furnace's data via subsequent SetSlot/ProgressBar packets.
			TileEntityFurnace furnace = new TileEntityFurnace();
			this.mc.thePlayer.displayGUIFurnace(furnace);
			this.mc.thePlayer.craftingInventory.windowId = windowPacket.windowId;
		} else if (windowPacket.inventoryType == 3) {
			// Dispenser GUI.
			TileEntityDispenser dispenser = new TileEntityDispenser();
			this.mc.thePlayer.displayGUIDispenser(dispenser);
			this.mc.thePlayer.craftingInventory.windowId = windowPacket.windowId;
		} else if (windowPacket.inventoryType == 1) {
			// Crafting table (workbench). The position is not actually used because
			// the workbench is a 3x3 grid centered on the player, but we floor the
			// coordinates for consistency.
			EntityPlayerSP thePlayer = this.mc.thePlayer;
			this.mc.thePlayer.displayWorkbenchGUI(MathHelper.floor_double(thePlayer.posX),
					MathHelper.floor_double(thePlayer.posY), MathHelper.floor_double(thePlayer.posZ));
			this.mc.thePlayer.craftingInventory.windowId = windowPacket.windowId;
		} else if (windowPacket.inventoryType == 6) {
			// Custom InfHell trader GUI. The windowPacket.slotsCount field is reused
			// as a currency selector: 0 = emerald, 1 = ruby.
			EntityPlayerSP entityPlayerSP = this.mc.thePlayer;
			// Jankily reusing existing fields for my purposes
			Currency currency = windowPacket.slotsCount == 0 ? Currency.currencyEmerald : Currency.currencyRuby;
			String traderName = windowPacket.windowTitle;
			this.mc.thePlayer.displayGUITrading(entityPlayerSP.inventory, new NpcTrader(traderName, currency));
			this.mc.thePlayer.craftingInventory.windowId = windowPacket.windowId;
		}

	}

	/**
	 * Handles Packet103SetSlot - updates a single slot in a container. Used for
	 * per-slot changes (cursor, hotbar swap, etc.). The "windowId == -1" case
	 * updates the local player's cursor item (the one the mouse is currently holding).
	 *
	 * @param setSlotPacket fields:
	 *   - windowId: -1 for cursor, 0 for player inventory, or the window ID of an open container
	 *   - itemSlot: the slot index within that container
	 *   - myItemStack: the new contents of the slot (null = empty)
	 */
	public void handleSetSlot(Packet103SetSlot setSlotPacket) {
		if (setSlotPacket.windowId == -1) {
			// Cursor (held-on-mouse) item.
			this.mc.thePlayer.inventory.setItemStack(setSlotPacket.myItemStack);
		} else if (setSlotPacket.windowId == 0 && setSlotPacket.itemSlot >= 36
				&& setSlotPacket.itemSlot < 45) {
			// 36..44 is the player's main hotbar+inventory range. If the new stack
			// size is larger than the previous one, play the "item gained" bop animation
			// by setting animationsToGo = 5 (5 ticks of bounce).
			ItemStack slotItem = this.mc.thePlayer.inventorySlots.getSlot(setSlotPacket.itemSlot).getStack();
			if (setSlotPacket.myItemStack != null
					&& (slotItem == null || slotItem.stackSize < setSlotPacket.myItemStack.stackSize)) {
				setSlotPacket.myItemStack.animationsToGo = 5;
			}

			this.mc.thePlayer.inventorySlots.putStackInSlot(setSlotPacket.itemSlot, setSlotPacket.myItemStack);
		} else if (setSlotPacket.windowId == this.mc.thePlayer.craftingInventory.windowId) {
			// Slot inside the currently open container (chest, furnace, etc.).
			this.mc.thePlayer.craftingInventory.putStackInSlot(setSlotPacket.itemSlot,
					setSlotPacket.myItemStack);
		}

	}

	/**
	 * Handles Packet106Transaction - the server confirms or rejects a click in a
	 * container. The Minecraft transaction protocol works like this:
	 *   - Client sends a click with (windowId, shortWindowId, accepted=false).
	 *   - Server processes the click and replies with (windowId, shortWindowId, accepted=?)
	 *   - If accepted=true, the client "confirms" the slot change.
	 *   - If accepted=false, the client "rolls back" the slot change and
	 *     re-acknowledges the server (with accepted=true) so both sides stay in sync.
	 *
	 * @param transactionPacket fields:
	 *   - windowId: which container
	 *   - shortWindowId: per-click short ID used to correlate
	 *   - packetBoolean: true = accept, false = reject
	 */
	public void handleTransaction(Packet106Transaction transactionPacket) {
		Container container = null;
		if (transactionPacket.windowId == 0) {
			container = this.mc.thePlayer.inventorySlots;
		} else if (transactionPacket.windowId == this.mc.thePlayer.craftingInventory.windowId) {
			container = this.mc.thePlayer.craftingInventory;
		}

		if (container != null) {
			if (transactionPacket.packetBoolean) {
				// Server accepted the click: confirm the predicted slot change.
				container.func_20113_a(transactionPacket.shortWindowId);
			} else {
				// Server rejected the click: roll back the slot change AND acknowledge
				// back to the server so the rejection is logged.
				container.func_20110_b(transactionPacket.shortWindowId);
				this.addToSendQueue(new Packet106Transaction(transactionPacket.windowId,
						transactionPacket.shortWindowId, true));
			}
		}

	}

	/**
	 * Handles Packet104WindowItems - bulk replacement of every slot in a container.
	 * Sent when a container is first opened, or when its entire contents change
	 * (e.g. a chest being broken and re-formed).
	 *
	 * @param windowItemsPacket fields:
	 *   - windowId: which container (0 = player inventory, or the open container's ID)
	 *   - itemStack: array of ItemStacks, one per slot
	 */
	public void handleWindowItems(Packet104WindowItems windowItemsPacket) {
		if (windowItemsPacket.windowId == 0) {
			this.mc.thePlayer.inventorySlots.putStacksInSlots(windowItemsPacket.itemStack);
		} else if (windowItemsPacket.windowId == this.mc.thePlayer.craftingInventory.windowId) {
			this.mc.thePlayer.craftingInventory.putStacksInSlots(windowItemsPacket.itemStack);
		}

	}

	public void handleUpdateSign(Packet130UpdateSign signPacket) {
		if (this.mc.theWorld.blockExists(signPacket.xPosition, signPacket.yPosition,
				signPacket.zPosition)) {
			TileEntity tileEntity = this.mc.theWorld.getBlockTileEntity(signPacket.xPosition,
					signPacket.yPosition, signPacket.zPosition);
			if (tileEntity instanceof TileEntitySign) {
				TileEntitySign sign = (TileEntitySign) tileEntity;

				for (int i = 0; i < 4; ++i) {
					sign.signText[i] = signPacket.signLines[i];
				}

				sign.onInventoryChanged();
			}
		}

	}

	/**
	 * Handles Packet91UpdateCommandBlock - server pushes a new command string for a
	 * command block tile entity. Triggered by an admin typing into a command block
	 * or by WorldEdit setting one. Looks up the existing TileEntityCommandBlock in
	 * the world and updates its command.
	 *
	 * @param cmdBlockPacket fields:
	 *   - x, y, z: block coordinates of the command block
	 *   - command: the new command string
	 */
	public void handleUpdateCommandBlock(Packet91UpdateCommandBlock cmdBlockPacket) {
		int x = cmdBlockPacket.x;
		int y = cmdBlockPacket.y;
		int z = cmdBlockPacket.z;

		if(this.mc.theWorld.blockExists(x, y, z)) {
			TileEntity te = this.mc.theWorld.getBlockTileEntity(x, y, z);
			if(te instanceof TileEntityCommandBlock) {
				TileEntityCommandBlock cmdBlock = (TileEntityCommandBlock) te;

				cmdBlock.command = cmdBlockPacket.command;

				cmdBlock.onInventoryChanged();
			}
		}
	}

	/**
	 * Handles Packet93UpdateAnimalName - server tells the client a tamable animal
	 * has been renamed. Sets the name on the EntityCreature (which propagates it
	 * to the on-screen name tag). If the entity no longer exists, this is a no-op.
	 *
	 * @param namePacket fields: entityId, name
	 */
	public void handleUpdateAnimalName(Packet93UpdateAnimalName namePacket) {
		Entity creature = this.getEntityByID(namePacket.entityId);
		if(creature == null || !(creature instanceof EntityCreature)) {
			// System.out.println ("Received name " + namePacket.name + " for non existing creature " + namePacket.entityId);
		} else {
			((EntityCreature) creature).setName(namePacket.name);
		}
	}

	/**
	 * Handles Packet105UpdateProgressbar - a single update to a furnace's progress
	 * bar (the burning arrow, smelting progress, or fuel gauge). Triggers the
	 * registerPacket hook so the network layer can record this packet for stats.
	 *
	 * @param progressPacket fields:
	 *   - windowId: which furnace's window
	 *   - progressBar: which bar (0=burn, 1=progress, 2=fuel)
	 *   - progressBarValue: the new value
	 */
	public void handleUpdateProgressBar(Packet105UpdateProgressbar progressPacket) {
		this.registerPacket(progressPacket);
		if (this.mc.thePlayer.craftingInventory != null
				&& this.mc.thePlayer.craftingInventory.windowId == progressPacket.windowId) {
			this.mc.thePlayer.craftingInventory.setFurnaceTime(progressPacket.progressBar,
					progressPacket.progressBarValue);
		}

	}

	/**
	 * Handles Packet5PlayerInventory - server tells the client that a player is
	 * wearing/holding an item in a specific slot. Used to sync armor and held items
	 * for other players so the renderer can draw them.
	 *
	 * @param invPacket fields:
	 *   - entityID: the player whose inventory changed
	 *   - slot: which slot (armor or held)
	 *   - itemID, itemDamage: the item now in that slot
	 */
	public void handlePlayerInventory(Packet5PlayerInventory invPacket) {
		Entity invEntity = this.getEntityByID(invPacket.entityID);
		if (invEntity != null) {
			invEntity.outfitWithItem(invPacket.slot, invPacket.itemID,
					invPacket.itemDamage);
		}

	}

	/**
	 * Handles Packet101CloseWindow - the server tells the client to close any
	 * open container GUI. Called when a chest/furnace is broken, when the player
	 * disconnects, or when the user moves too far from the container.
	 *
	 * @param closeWindowPacket field: windowId - which window to close
	 */
	public void handleCloseWindow(Packet101CloseWindow closeWindowPacket) {
		this.mc.thePlayer.closeScreen();
	}

	// =================================================================
	// Section: World Interaction Handlers
	// =================================================================

	/**
	 * Handles Packet54PlayNoteBlock - a note block has been struck (right-clicked).
	 * Plays the corresponding note pitch using the world as the audio source.
	 *
	 * @param notePacket fields:
	 *   - xLocation, yLocation, zLocation: block coordinates
	 *   - instrumentType: 0=piano, 1=bass drum, 2=snare, 3=sticks, 4=bass guitar
	 *   - pitch: 0..24, the note's pitch
	 */
	public void handleNotePlay(Packet54PlayNoteBlock notePacket) {
		this.mc.theWorld.playNoteAt(notePacket.xLocation, notePacket.yLocation,
				notePacket.zLocation, notePacket.instrumentType, notePacket.pitch);
	}

	/**
	 * Handles Packet70Bed - the server sends a chat message related to sleeping in
	 * a bed, AND (InfHell-specific) pushes updates to the rain/snow/thunder state
	 * when action==1. The "action" field uses vanilla's bed-related codes; we
	 * reuse 1 as a custom "weather update" code.
	 *
	 * @param bedPacket fields:
	 *   - setRainingAction: action code; 0..n indexes into errorMessageArr,
	 *                       1 means "update weather" in the InfHell encoding
	 *   - raining, snowing, thundering: the new weather booleans (if action==1)
	 */
	public void handleBed(Packet70Bed bedPacket) {
		int action = bedPacket.setRainingAction;
		// Look up the localized chat message for the given bed action.
		if (action >= 0 && action < Packet70Bed.errorMessageArr.length && Packet70Bed.errorMessageArr[action] != null) {
			this.mc.thePlayer.addChatMessage(Packet70Bed.errorMessageArr[action]);
		}

		/*
		 * if(i2 == 1) { this.worldClient.getWorldInfo().setRaining(true);
		 * this.worldClient.setRainStrength(1.0F); } else if(i2 == 2) {
		 * this.worldClient.getWorldInfo().setRaining(false);
		 * this.worldClient.setRainStrength(0.0F); }
		 */

		// This is encoded differently to vanilla
		// InfHell: action==1 is a weather sync packet - the booleans tell us the
		// current raining/snowing/thundering state and we set the strength to either
		// 0 (off) or 1 (on, full intensity).
		if (action == 1) {
			this.worldClient.getWorldInfo().setRaining(bedPacket.raining);
			this.worldClient.setRainStrength(bedPacket.raining ? 1.0F : 0.0F);
			this.worldClient.getWorldInfo().setSnowing(bedPacket.snowing);
			this.worldClient.setSnowingStrength(bedPacket.snowing ? 1.0F : 0.0F);
			this.worldClient.getWorldInfo().setThundering(bedPacket.thundering);
			this.worldClient.setThunderingStrength(bedPacket.thundering ? 1.0F : 0.0F);
		}

	}

	/**
	 * Handles Packet131MapData - streams updated pixel data for a map item. The
	 * server sends the data when a player holds a filled map. Currently only
	 * standard map items are supported.
	 *
	 * @param mapDataPacket fields:
	 *   - itemID: the item type (must be Item.mapItem.shiftedIndex)
	 *   - uniqueID: the map's unique ID (used as the texture filename)
	 *   - itemData: the raw pixel/color data for the map
	 */
	public void handleMapData(Packet131MapData mapDataPacket) {
		if (mapDataPacket.itemID == Item.mapItem.shiftedIndex) {
			// Look up the MP map data block for this map ID and append the new
			// pixel/color data to it.
			ItemMap.getMPMapData(mapDataPacket.uniqueID, this.mc.theWorld).func_28171_a(mapDataPacket.itemData);
		} else {
			System.out.println("Unknown itemid: " + mapDataPacket.uniqueID);
		}

	}

	/**
	 * Handles Packet61DoorChange - the server tells us about a door (or trapdoor,
	 * fence gate) opening/closing, which we play as an aux SFX (the door's open/close
	 * sound). The SFX ID encodes both the kind of door and the action (open/close).
	 *
	 * @param doorPacket fields:
	 *   - field_28050_a: aux SFX ID (1000+open, 1001+close, 1002=trapdoor etc.)
	 *   - field_28053_c, field_28052_d, field_28051_e: world block coords
	 *   - field_28049_b: pitch/sound variant
	 */
	public void handleDoorChange(Packet61DoorChange doorPacket) {
		this.mc.theWorld.playAuxSFX(doorPacket.xPosition, doorPacket.yPosition,
				doorPacket.zPosition, doorPacket.metadata,
				doorPacket.auxData);
	}

	/**
	 * Handles Packet200Statistic - the server tells the client that the local
	 * player has earned (or set) an achievement/stat. Used for one-shot stats
	 * (e.g. "play 100 ticks", "open inventory").
	 */
	public void handleStatistic(Packet200Statistic statPacket) {
		((EntityClientPlayerMP) this.mc.thePlayer).func_27027_b(
				StatList.getOneShotStat(statPacket.statId), statPacket.statIncrement);
	}

	// =================================================================
	// Section: World State Handlers (weather, creative, freeze, day-of-year, etc.)
	// =================================================================

	/**
	 * Handles Packet98UpdateWeather - server sends a coarse rain/snow/thunder toggle.
	 * Unlike the InfHell-specific handleBed(action==1), this packet only updates
	 * the booleans; the visual strength is left unchanged.
	 *
	 * @param weatherPacket fields: raining, snowing, thundering - the new weather booleans
	 */
	public void handleUpdateWeather(Packet98UpdateWeather weatherPacket) {
		this.worldClient.getWorldInfo().setRaining(weatherPacket.raining);
		this.worldClient.getWorldInfo().setSnowing(weatherPacket.snowing);
		this.worldClient.getWorldInfo().setThundering(weatherPacket.thundering);
	}

	/**
	 * Handles Packet99SetCreativeMode - the server grants or revokes creative mode
	 * for the local player. When leaving creative mode, flight is also turned off
	 * to prevent the player from getting stuck in midair.
	 *
	 * @param creativePacket field: isCreative - the new creative mode flag
	 */
	public void handleSetCreative(Packet99SetCreativeMode creativePacket) {
		EntityPlayerSP thePlayer = this.mc.thePlayer;
		thePlayer.isCreative = creativePacket.isCreative;
		if (!thePlayer.isCreative)
			thePlayer.isFlying = false;
	}

	/**
	 * Handles Packet96BadMoonDecide - InfHell custom: a one-bit decision related to
	 * the "bad moon" event (e.g. spawning a werewolf, hostile mob wave, etc.).
	 * The boolean is stored on the WorldClient for any subsystem that wants to read it.
	 *
	 * @param badMoonPacket field: badMoonDecide - true if the bad moon event should occur
	 */
	public void handleBadMoonDecide(Packet96BadMoonDecide badMoonPacket) {
		//System.out.println("Got bad moon decide = " + windowPacket.badMoonDecide);
		this.worldClient.badMoonDecide = badMoonPacket.badMoonDecide;
	}

	/**
	 * Handles Packet95UpdateDayOfTheYear - InfHell custom: server tells us the
	 * current day-of-year (1..360) so the client can show season-specific textures
	 * and ambient effects.
	 *
	 * @param dayPacket field: dayOfTheYear - day within the year
	 */
	public void handleUpdateDayOfTheYear(Packet95UpdateDayOfTheYear dayPacket) {
		//System.out.println("Got day of the year = " + windowPacket.dayOfTheYear);
		this.worldClient.performDayOfTheYearUpdate(dayPacket.dayOfTheYear);
	}

	/**
	 * Handles Packet94FreezeLevel - InfHell custom: server sets the player's
	 * "freeze" counter. Used to make the player unable to move briefly (e.g. when
	 * hit by an ice attack, frozen by a snow golem, etc.). The local player will
	 * tick down the freeze value in its update code.
	 *
	 * @param freezePacket field: freezeLevel - the new freeze value
	 */
	public void handleFreezeLevel(Packet94FreezeLevel freezePacket) {
		this.mc.thePlayer.freezeLevel = freezePacket.freezeLevel;
	}

	// =================================================================
	// Section: Custom Payload + Status Effects + World Info
	// =================================================================

	/**
	 * Handles Packet250CustomPayload - a custom channel packet. Currently the only
	 * channel we react to is "MC|TrList": it carries the list of trade offers for
	 * a villager/custom trader GUI. The payload starts with the window ID, then a
	 * serialized TradingRecipeList stream.
	 *
	 * @param payloadPacket fields:
	 *   - channel: the channel name (e.g. "MC|TrList")
	 *   - data: the channel-specific binary payload
	 */
	public void handleCustomPayload(Packet250CustomPayload payloadPacket) {
		if ("MC|TrList".equals(payloadPacket.channel)) {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payloadPacket.data));

			try {
				// First field of the payload is the window ID this trade list belongs to.
				int windowId = dis.readInt();
				GuiScreen guiScreen = this.mc.currentScreen;

				// Only update the currently-open trading GUI for the matching window.
				if (guiScreen != null && guiScreen instanceof GuiTrading
						&& windowId == this.mc.thePlayer.craftingInventory.windowId) {
					ITrader entityTrader = ((GuiTrading) guiScreen).getEntityTrader();
					TradingRecipeList tradingRecipeList = TradingRecipeList.readRecipesFromStream(dis);
					entityTrader.setRecipes(tradingRecipeList);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Handles Packet41EntityEffect - apply a status effect (potion effect) to a
	 * living entity. The effect is added to the entity's status effect list with
	 * the given duration and amplifier.
	 *
	 * @param effectPacket fields:
	 *   - entityId: the target entity (must be EntityLiving)
	 *   - effectId: numeric effect ID (e.g. 1=speed, 2=slowness, 8=invisibility)
	 *   - duration: effect duration in ticks
	 *   - effectAmp: amplifier (0=level I, 1=level II, etc.)
	 */
	public void handleEntityEffect(Packet41EntityEffect effectPacket) {
		Entity effectEntity = this.getEntityByID(effectPacket.entityId);
		if(effectEntity != null && effectEntity instanceof EntityLiving) {
			((EntityLiving)effectEntity).addStatusEffect(new StatusEffect(effectPacket.effectId, effectPacket.duration, effectPacket.effectAmp));
		}
	}

	/**
	 * Handles Packet42RemoveEntityEffect - remove a previously applied status effect
	 * from a living entity. Used when the effect expires naturally or is removed by
	 * drinking milk.
	 *
	 * @param removeEffectPacket fields:
	 *   - entityId: the target entity (must be EntityLiving)
	 *   - effectId: which effect to remove
	 */
	public void handleRemoveEntityEffect(Packet42RemoveEntityEffect removeEffectPacket) {
		Entity effectEntity = this.getEntityByID(removeEffectPacket.entityId);
		if(effectEntity != null && effectEntity instanceof EntityLiving) {
			((EntityLiving)effectEntity).removeStatusEffect(removeEffectPacket.effectId);
		}
	}

	/**
	 * Handles Packet92SetCustomWorldInfo - InfHell custom: server toggles a
	 * per-player flag that controls whether the crafting guide helper UI is
	 * enabled for this player. Currently only the crafting guide flag is sent.
	 *
	 * @param customWorldPacket field: enableCraftingGuide - true to show the guide
	 */
	public void handleSetCustomWorldInfo(Packet92SetCustomWorldInfo customWorldPacket) {
		this.mc.thePlayer.enableCraftingGuide = customWorldPacket.enableCraftingGuide;
	}

	/**
	 * NetHandler dispatch helper. Returns false because we are a client-side
	 * handler; the network layer uses this to choose between server and client
	 * handler classes in shared packet code.
	 *
	 * @return always false to indicate "this is a client handler"
	 */
	public boolean isServerHandler() {
		return false;
	}

}

