package net.minecraft.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.Seasons;
import net.minecraft.network.NetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet255KickDisconnect;
import net.minecraft.network.packet.Packet2Handshake;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet4UpdateTime;
import net.minecraft.network.packet.Packet6SpawnPosition;
import net.minecraft.network.packet.Packet92SetCustomWorldInfo;
import net.minecraft.network.packet.Packet95UpdateDayOfTheYear;
import net.minecraft.network.packet.Packet99SetCreativeMode;

/**
 * Handles the login phase for an incoming TCP connection.
 * Receives the handshake and login packets, verifies the session
 * (if online mode is enabled), creates the player entity, and then
 * upgrades the connection to a NetServerHandler for gameplay.
 *
 * Runs in its own thread (ThreadLoginVerifier) for session verification.
 */
public class NetLoginHandler extends NetHandler {

	/** Logger for login events. */
	public static Logger logger = Logger.getLogger("Minecraft");

	/** Shared random number generator for server IDs. */
	private static Random random = new Random();

	/** The network manager for this connection. */
	public NetworkManager netManager;

	/** Set to true once login processing completes (success or failure). */
	public boolean finishedProcessing = false;

	/** The owning MinecraftServer instance. */
	private MinecraftServer server;

	/** Timer used to kick clients that take too long to login. */
	private int loginTimer = 0;

	/** The username sent by the client (null until login packet arrives). */
	private String username = null;

	/** The login packet received from the client (processed once). */
	private Packet1Login loginPacket = null;

	/** The server ID sent to the client during handshake (for session verification). */
	private String serverId = "";

	/**
	 * Creates a new login handler for the given socket.
	 *
	 * @param minecraftServer1 the MinecraftServer instance
	 * @param socket2          the client socket
	 * @param description3     a description for logging (e.g. "Connection #42")
	 * @throws IOException if the NetworkManager cannot be created
	 */
	public NetLoginHandler(MinecraftServer minecraftServer1, Socket socket2, String description3) throws IOException {
		this.server = minecraftServer1;
		this.netManager = new NetworkManager(socket2, description3, this);
		this.netManager.chunkDataSendCounter = 0;
	}

	/**
	 * Called each tick from NetworkListenThread.handleNetworkListenThread.
	 * If a login packet has been received, process it. Otherwise, read
	 * incoming packets (expecting a handshake). Kick after 600 ticks (~30s).
	 */
	public void tryLogin() {
		if (this.loginPacket != null) {
			this.processLogin(this.loginPacket);
			this.loginPacket = null;
		}

		if (this.loginTimer++ == 600) {
			this.kickUser("Took too long to log in");
		} else {
			this.netManager.processReadPackets();
		}
	}

	/**
	 * Disconnects the client with a kick message.
	 * Sends a Packet255KickDisconnect, shuts down the network manager,
	 * and marks this handler as finished.
	 */
	public void kickUser(String message) {
		try {
			logger.info("Disconnecting " + this.getUserAndIpString() + ": " + message);
			this.netManager.addToSendQueue(new Packet255KickDisconnect(message));
			this.netManager.serverShutdown();
			this.finishedProcessing = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles the initial handshake packet.
	 * In online mode, generates a random server ID for session verification.
	 * In offline mode, sends "-" to indicate no verification is needed.
	 */
	public void handleHandshake(Packet2Handshake handshakePacket) {
		if (this.server.onlineMode) {
			this.serverId = Long.toHexString(random.nextLong());
			this.netManager.addToSendQueue(new Packet2Handshake(this.serverId));
		} else {
			this.netManager.addToSendQueue(new Packet2Handshake("-"));
		}
	}

	/**
	 * Handles the login packet from the client.
	 * Checks protocol version, then either processes login immediately
	 * (offline mode) or starts a session verification thread (online mode).
	 */
	public void handleLogin(Packet1Login loginPacket) {
		this.username = loginPacket.username;
		if (loginPacket.protocolVersion != 14) {
			if (loginPacket.protocolVersion > 14) {
				this.kickUser("Outdated server!");
			} else {
				this.kickUser("Outdated client!");
			}
		} else {
			if (!this.server.onlineMode) {
				this.processLogin(loginPacket);
			} else {
				(new ThreadLoginVerifier(this, loginPacket)).start();
			}
		}
	}

	/**
	 * Completes the login process: creates the player entity, sends
	 * initial world and player packets, and adds the player to the server.
	 */
	public void processLogin(Packet1Login loginPacket) {
		EntityPlayerMP player = this.server.configManager.login(this, loginPacket.username);
		if (player != null) {
			this.server.configManager.readPlayerDataFromFile(player);
			player.setWorldHandler(this.server.getWorldManager(player.dimension));
			
			logger.info(this.getUserAndIpString() + " logged in with entity id " + player.entityId + " at (" + player.posX + ", " + player.posY + ", " + player.posZ + ")");
			WorldServer world = this.server.getWorldManager(player.dimension);
			ChunkCoordinates spawn = world.getSpawnPoint();
			NetServerHandler serverHandler = new NetServerHandler(this.server, this.netManager, player);
			
			serverHandler.sendPacket(new Packet1Login("", player.entityId, world.getRandomSeed(), (byte)world.worldProvider.worldType));
			serverHandler.sendPacket(new Packet6SpawnPosition(spawn.posX, spawn.posY, spawn.posZ));
			
			this.server.configManager.joinNewPlayerManager(player, world);
			this.server.configManager.sendPacketToAllPlayers(new Packet3Chat("\u00a7e" + player.username + " joined the game."));
			this.server.configManager.playerLoggedIn(player);
			
			serverHandler.teleportTo(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
			
			// Send creative mode flag
			serverHandler.sendPacket(new Packet99SetCreativeMode(player.isCreative));
			
			// Send crafting guide enabled flag
			serverHandler.sendPacket(new Packet92SetCustomWorldInfo(this.server.configManager.enableCraftingGuide));
			
			this.server.networkServer.addPlayer(serverHandler);
			serverHandler.sendPacket(new Packet4UpdateTime(world.getWorldTime()));
			serverHandler.sendPacket(new Packet95UpdateDayOfTheYear(Seasons.dayOfTheYear));
			player.sendUpdateTimeAndWeather();
			
			Iterator<StatusEffect> effects = player.getActiveStatusEffects().iterator();
			while (effects.hasNext()) {
				StatusEffect effect = effects.next();
				serverHandler.sendPacket(new Packet41EntityEffect(player.entityId, effect));
			}
			
			player.deadManChest = this.server.configManager.deadMansChest;
		}

		this.finishedProcessing = true;
	}

	/** Handles unexpected errors during login (e.g. malformed packets). */
	public void handleErrorMessage(String message, Object[] args) {
		logger.info(this.getUserAndIpString() + " lost connection");
		this.finishedProcessing = true;
	}

	/** Registers unexpected packets as protocol errors. */
	public void registerPacket(Packet packet) {
		this.kickUser("Protocol error");
	}

	/**
	 * Returns a string suitable for logging: "username [IP]" or just "[IP]"
	 * if the username is not yet known.
	 */
	public String getUserAndIpString() {
		return this.username != null ? this.username + " [" + this.netManager.getRemoteAddress().toString() + "]" : this.netManager.getRemoteAddress().toString();
	}

	/** NetLoginHandler is a server-side handler (not client). */
	public boolean isServerHandler() {
		return true;
	}

	/** Returns the server ID for this login attempt (used by ThreadLoginVerifier). */
	static String getServerId(NetLoginHandler handler0) {
		return handler0.serverId;
	}

	/** Sets the login packet on the given handler (used by ThreadLoginVerifier). */
	static Packet1Login setLoginPacket(NetLoginHandler handler0, Packet1Login packet1) {
		return handler0.loginPacket = packet1;
	}
}