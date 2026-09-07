package net.minecraft.server;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.loader.ISaveFormat;
import net.minecraft.game.world.chunk.loader.SaveConverterMcRegion;
import net.minecraft.game.world.chunk.loader.SaveOldDir;
import net.minecraft.network.packet.Packet4UpdateTime;
import net.minecraft.network.packet.Packet95UpdateDayOfTheYear;
import net.minecraft.server.console.ConsoleCommandHandler;
import net.minecraft.server.console.ConsoleLogManager;
import net.minecraft.server.console.ConvertProgressUpdater;
import net.minecraft.server.console.IUpdatePlayerListBox;
import net.minecraft.server.console.PropertyManager;
import net.minecraft.server.console.ServerCommand;
import net.minecraft.server.console.ServerConfigurationManager;
import net.minecraft.server.console.ServerGUI;
import net.minecraft.game.IProgressUpdate;
import net.minecraft.client.render.RenderEngineMin;
import net.minecraft.game.Seasons;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.Version;
import net.minecraft.game.achievements.StatList;
import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.WorldType;

/**
 * MinecraftServer - The core server application class.
 *
 * <role>: This is the main entry point and central orchestrator for the Minecraft server.
 *        It implements Runnable to run the server loop in a dedicated thread, and
 *        ICommandListener to allow console commands to be issued programmatically.
 *
 * Responsibilities:
 *   - Initialize and manage the network listener thread (NetworkListenThread)
 *   - Load and manage world instances (WorldServer[])
 *   - Manage player connections via ServerConfigurationManager
 *   - Process the main server tick loop with timekeeping and sleep logic
 *   - Handle console command parsing and dispatch
 *   - Manage server lifecycle: start, tick, stop, and shutdown
 *   - Track and report server performance metrics
 *
 * Integration points:
 *   - NetworkListenThread for incoming player connections
 *   - ServerConfigurationManager for player authentication and management
 *   - WorldServer instances for each dimension (overworld, nether)
 *   - EntityTracker for tracking entities across dimensions
 *   - ServerGUI for optional graphical interface
 *   - PropertyManager for server configuration
 */
public class MinecraftServer implements Runnable, ICommandListener {

	/** Global logger instance used for all server-side logging. */
	public static Logger logger = Logger.getLogger("Minecraft");

	/**
	 * HashMap mapping command names to their remaining cooldown ticks.
	 * Used to enforce command cooldowns on the server.
	 */
	public static HashMap<String,Integer> commandCooldowns = new HashMap<String,Integer>();

	/** Network listener thread that accepts incoming player connections. */
	public NetworkListenThread networkServer;

	/** Manager for server.properties configuration settings. */
	public PropertyManager propertyManagerObj;

	/**
	 * Array of WorldServer instances, one per dimension.
	 * Index 0: Overworld, Index 1: Nether.
	 */
	public WorldServer[] worldMngr;

	/** Manager responsible for player login, logout, and player data persistence. */
	public ServerConfigurationManager configManager;

	/** Handler for processing console commands typed into the server console. */
	private ConsoleCommandHandler commandHandler;

	/**
	 * Volatile flag indicating whether the server should continue running.
	 * Set to false when initiateShutdown() is called.
	 */
	private boolean serverRunning = true;

	/** Flag indicating that the server has completed its shutdown sequence. */
	public boolean serverStopped = false;

	/**
	 * Counter used for tick-based operations; increments every server tick.
	 * Currently used to trigger time-related packets at intervals.
	 */
	int ticksRan = 0;

	/**
	 * Description of the current task being performed during world loading.
	 * Updated by outputPercentRemaining() and cleared by clearCurrentTask().
	 */
	public String currentTask;

	/** Percentage completed of the current loading task (0-100). */
	public int percentDone;

	/**
	 * List of IUpdatePlayerListBox objects that need their update() method called
	 * each server tick. Used for GUI and UI elements that tick independently.
	 */
	private List<IUpdatePlayerListBox> tickableGuiElements = new ArrayList<IUpdatePlayerListBox>();

	/** Thread-safe list of pending console commands waiting to be parsed. */
	private List<ServerCommand> pendingCommands = Collections.synchronizedList(new ArrayList<ServerCommand>());

	/**
	 * Array of EntityTracker instances, one per dimension.
	 * Used to track and sync entities with nearby players.
	 */
	public EntityTracker[] entityTracker = new EntityTracker[2];

	/** Whether the server is running in online mode (authenticates usernames). */
	public boolean onlineMode;

	/** Whether peaceful mobs should spawn in the world. */
	public boolean spawnPeacefulMobs;

	/** Whether player-versus-player combat is enabled. */
	public boolean pvpOn;

	/** Whether flight is allowed for players. */
	public boolean allowFlight;

	/** Render engine used for loading textures and resources. */
	public RenderEngineMin renderEngine;

	/** Whether the crafting guide feature is enabled. */
	public boolean enableCraftingGuide;

	/** Whether the dead man's chest feature is enabled. */
	public boolean deadMansChest;

	/**
	 * Constructs a new MinecraftServer instance.
	 * Starts a daemon thread that runs the server application.
	 */
	public MinecraftServer() {
		new ThreadSleepForeverServer(this);
	}

	/**
	 * Initializes the server by loading properties, binding the network port,
	 * setting up the world, and preparing all subsystems.
	 *
	 * This method is called once during server startup. It performs the following:
	 *   1. Creates the console command handler and starts the command reader thread
	 *   2. Initializes the console logging manager
	 *   3. Loads server.properties settings (IP, port, game modes)
	 *   4. Sets up the network listener thread on the configured port
	 *   5. Creates the ServerConfigurationManager and EntityTracker instances
	 *   6. Loads or creates the world with the specified settings
	 *
	 * @return true if the server initialized successfully, false otherwise
	 * @throws UnknownHostException if the server IP address cannot be resolved
	 */
	private boolean startServer() throws UnknownHostException {
		this.commandHandler = new ConsoleCommandHandler(this);

		ThreadCommandReader commandReader = new ThreadCommandReader(this);
		commandReader.setDaemon(true);
		commandReader.start();

		ConsoleLogManager.init();

		logger.info("Starting minecraft server " + Version.getVersion());

		// Warn if the JVM is allocated less than 512MB.
		if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
			logger.warning("**** NOT ENOUGH RAM!");
			logger.warning("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
		}

		logger.info("Loading properties");
		this.propertyManagerObj = new PropertyManager(new File("server.properties"));

		// Parse server IP address (empty string means bind to all interfaces).
		String serverIpAddress = this.propertyManagerObj.getStringProperty("server-ip", "");

		this.onlineMode = this.propertyManagerObj.getBooleanProperty("online-mode", true);
		this.spawnPeacefulMobs = this.propertyManagerObj.getBooleanProperty("spawn-animals", true);
		this.pvpOn = this.propertyManagerObj.getBooleanProperty("pvp", true);
		this.allowFlight = this.propertyManagerObj.getBooleanProperty("allow-flight", false);

		// Resolve the server IP address if specified.
		InetAddress serverInetAddress = null;
		if (serverIpAddress.length() > 0) {
			serverInetAddress = InetAddress.getByName(serverIpAddress);
		}

		// Load the biome lookup table texture.
		logger.info("Loading biome LUT");
		this.renderEngine = new RenderEngineMin();
		BiomeGenBase.setBuffer(this.renderEngine.getTextureContents("/biomelut.png"));
		BiomeGenBase.loadBiomeLookup();

		int serverPort = this.propertyManagerObj.getIntProperty("server-port", 25565);
		logger.info("Starting Minecraft server on " + (serverIpAddress.length() == 0 ? "*" : serverIpAddress) + ":" + serverPort);

		try {
			this.networkServer = new NetworkListenThread(this, serverInetAddress, serverPort);
		} catch (IOException ioException) {
			logger.warning("**** FAILED TO BIND TO PORT!");
			logger.log(Level.WARNING, "The exception was: " + ioException.toString());
			logger.warning("Perhaps a server is already running on that port?");
			return false;
		}

		if (!this.onlineMode) {
			logger.warning("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
			logger.warning("The server will make no attempt to authenticate usernames. Beware.");
			logger.warning("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
			logger.warning("To change this, set \"online-mode\" to \"true\" in the server.settings file.");
		}

		this.enableCraftingGuide = this.propertyManagerObj.getBooleanProperty("crafting-guide", true);
		this.deadMansChest = this.propertyManagerObj.getBooleanProperty("dead-mans-chest", false);

		this.configManager = new ServerConfigurationManager(this);

		this.entityTracker[0] = new EntityTracker(this, 0);
		this.entityTracker[1] = new EntityTracker(this, -1);

		long worldLoadStartTime = System.nanoTime();

		String worldName = this.propertyManagerObj.getStringProperty("level-name", "world");
		String worldSeedStr = this.propertyManagerObj.getStringProperty("level-seed", "");
		String worldType = this.propertyManagerObj.getStringProperty("level-type", "DEFAULT");

		long seedValue = (new Random()).nextLong();
		if (worldSeedStr.length() > 0) {
			try {
				seedValue = Long.parseLong(worldSeedStr);
			} catch (NumberFormatException e) {
				seedValue = (long) worldSeedStr.hashCode();
			}
		}

		WorldType parsedWorldType = WorldType.parseWorldType(worldType);
		if (parsedWorldType == null) {
			parsedWorldType = WorldType.DEFAULT;
		}

		logger.info("Preparing level \"" + worldName + "\"");
		this.initWorld(new SaveConverterMcRegion(new File(".")), worldName, seedValue, parsedWorldType);
		logger.info("Done (" + (System.nanoTime() - worldLoadStartTime) + "ns)! For help, type \"help\" or \"?\"");
		return true;
	}

	/**
	 * Initializes the world instances for the server.
	 *
	 * Creates WorldServer objects for each dimension, sets up world access listeners,
	 * configures difficulty settings, and registers the player manager for each world.
	 * If the world is in an old map format, it converts it to the MCRegion format first.
	 *
	 * @param saveFormat the save format handler
	 * @param worldName  the name of the world
	 * @param seed       the world seed
	 * @param worldType  the world type
	 */
	private void initWorld(ISaveFormat saveFormat, String worldName, long seed, WorldType worldType) {
		if (saveFormat.isOldMapFormat(worldName)) {
			logger.info("Converting map!");
			saveFormat.converMapToMCRegion(worldName, new ConvertProgressUpdater(this));
		}

		// Two dimensions: overworld (0) and nether (-1).
		this.worldMngr = new WorldServer[2];

		boolean generateStructures = this.propertyManagerObj.getBooleanProperty("generate-structures", true);
		boolean generateCities = this.propertyManagerObj.getBooleanProperty("generate-cities", true);

		WorldSettings worldSettings = new WorldSettings(seed, 0, generateStructures, false, generateCities, 0.0F, worldType);
		SaveOldDir saveHandler = new SaveOldDir(new File("."), worldName, true);

		for (int dimension = 0; dimension < this.worldMngr.length; ++dimension) {
			if (dimension == 0) {
				this.worldMngr[dimension] = new WorldServer(this, saveHandler, worldName, dimension, worldSettings);
			} else {
				// The nether shares map storage with the overworld.
				this.worldMngr[dimension] = new WorldServerMulti(this, saveHandler, worldName, dimension, worldSettings, this.worldMngr[0]);
			}
			this.worldMngr[dimension].addWorldAccess(new WorldManager(this, this.worldMngr[dimension]));
			this.worldMngr[dimension].difficultySetting = this.propertyManagerObj.getBooleanProperty("spawn-monsters", true) ? 1 : 0;
			this.worldMngr[dimension].setAllowedMobSpawns(this.propertyManagerObj.getBooleanProperty("spawn-monsters", true), this.spawnPeacefulMobs);
			this.configManager.setPlayerManager(this.worldMngr);
		}

		// Pre-generate chunks around the spawn point so the first player gets a fast join.
		short spawnRadius = 196;
		long spawnPrepareStartTime = System.currentTimeMillis();

		for (int dimension = 0; dimension < this.worldMngr.length; ++dimension) {
			logger.info("Preparing start region for level " + dimension);
			if (dimension == 0 || this.propertyManagerObj.getBooleanProperty("allow-nether", true)) {
				WorldServer world = this.worldMngr[dimension];
				ChunkCoordinates spawn = world.getSpawnPoint();
				for (int x = -spawnRadius; x <= spawnRadius && this.serverRunning; x += 16) {
					for (int z = -spawnRadius; z <= spawnRadius && this.serverRunning; z += 16) {
						long now = System.currentTimeMillis();
						if (now < spawnPrepareStartTime) {
							spawnPrepareStartTime = now;
						}
						if (now > spawnPrepareStartTime + 1000L) {
							int totalChunks = (spawnRadius * 2 + 1) * (spawnRadius * 2 + 1);
							int currentChunk = (x + spawnRadius) * (spawnRadius * 2 + 1) + z + 1;
							this.outputPercentRemaining("Preparing spawn area", currentChunk * 100 / totalChunks);
							spawnPrepareStartTime = now;
						}
						world.chunkProviderServer.prepareChunk(spawn.posX + x >> 4, spawn.posZ + z >> 4);
					}
				}
			}
		}

		this.clearCurrentTask();
	}

	/**
	 * Updates the current task description and progress percentage.
	 * Used during world loading to show progress to the console.
	 *
	 * @param taskDescription human-readable description of the current task
	 * @param percentComplete percentage of completion (0-100)
	 */
	private void outputPercentRemaining(String taskDescription, int percentComplete) {
		this.currentTask = taskDescription;
		this.percentDone = percentComplete;
		logger.info(taskDescription + ": " + percentComplete + "%");
	}

	/** Clears the current task description and resets the progress indicator. */
	private void clearCurrentTask() {
		this.currentTask = null;
		this.percentDone = 0;
	}

	/** Saves all world data to disk. */
	private void saveServerWorld() {
		logger.info("Saving chunks");
		for (int dimension = 0; dimension < this.worldMngr.length; ++dimension) {
			WorldServer world = this.worldMngr[dimension];
			world.saveWorld(true, (IProgressUpdate) null);
			world.s_func_30006_w();
		}
	}

	/** Stops the server gracefully. Saves all player data and world chunks. */
	private void stopServer() {
		logger.info("Stopping server");
		if (this.configManager != null) {
			this.configManager.savePlayerStates();
		}
		for (int dimension = 0; dimension < this.worldMngr.length; ++dimension) {
			WorldServer world = this.worldMngr[dimension];
			if (world != null) {
				this.saveServerWorld();
			}
		}
	}

	/** Signals the server to begin its shutdown sequence. */
	public void initiateShutdown() {
		this.serverRunning = false;
	}

	/**
	 * The main server run loop.
	 *
	 * Performs the following:
	 *   1. Calls startServer() to initialize all subsystems
	 *   2. Enters the main tick loop, sleeping 1ms between iterations
	 *   3. If all players are asleep, runs a tick immediately; otherwise runs ticks
	 *      based on elapsed real-time to maintain ~20 ticks/second
	 *   4. Handles exceptions and performs a graceful shutdown on termination
	 */
	public void run() {
		try {
			if (this.startServer()) {
				long previousTickTime = System.currentTimeMillis();
				long accumulatedTickTime = 0L;
				for (long currentTime = 0L; this.serverRunning; Thread.sleep(1L)) {
					currentTime = System.currentTimeMillis();
					long elapsedTime = currentTime - previousTickTime;

					// Detect if the system clock jumped forward (server overload).
					if (elapsedTime > 2000L) {
						logger.warning("Can't keep up! Did the system time change, or is the server overloaded?");
						elapsedTime = 2000L;
					}

					// Detect if the system clock went backwards.
					if (elapsedTime < 0L) {
						logger.warning("Time ran backwards! Did the system time change?");
						elapsedTime = 0L;
					}

					accumulatedTickTime += elapsedTime;
					previousTickTime = currentTime;

					// If all players are asleep, run a single tick immediately (don't catch up).
					if (this.worldMngr[0].isAllPlayersFullyAsleep()) {
						this.doTick();
						accumulatedTickTime = 0L;
					} else {
						// Run ticks at ~20 tps (50ms per tick) to catch up.
						while (accumulatedTickTime > 50L) {
							accumulatedTickTime -= 50L;
							this.doTick();
						}
					}
				}
			} else {
				// Server failed to start: just keep reading console commands.
				while (this.serverRunning) {
					this.commandLineParser();
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Throwable fatalException) {
			fatalException.printStackTrace();
			logger.log(Level.SEVERE, "Unexpected exception", fatalException);
			while (this.serverRunning) {
				this.commandLineParser();
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			try {
				this.stopServer();
				this.serverStopped = true;
			} catch (Throwable cleanupException) {
				cleanupException.printStackTrace();
			} finally {
				System.exit(0);
			}
		}
	}

	/**
	 * Performs a single server tick.
	 *
	 * Called approximately 20 times per second. Performs:
	 *   1. Decrements command cooldown timers
	 *   2. Clears the bounding box and vector pools (memory optimization)
	 *   3. Ticks each world (entity updates, chunk management)
	 *   4. Sends time/day updates to players at intervals
	 *   5. Processes network packets and player connections
	 *   6. Updates tracked entities
	 *   7. Parses any pending console commands
	 */
	private void doTick() {
		// Decrement command cooldowns.
		ArrayList<String> expiredCommands = new ArrayList<String>();
		Iterator<String> cmdIter = commandCooldowns.keySet().iterator();
		while (cmdIter.hasNext()) {
			String cmdName = cmdIter.next();
			int remaining = commandCooldowns.get(cmdName).intValue();
			if (remaining > 0) {
				commandCooldowns.put(cmdName, remaining - 1);
			} else {
				expiredCommands.add(cmdName);
			}
		}
		for (int i = 0; i < expiredCommands.size(); ++i) {
			commandCooldowns.remove(expiredCommands.get(i));
		}

		// Memory hygiene: clear transient object pools.
		AxisAlignedBB.clearBoundingBoxPool();
		Vec3D.initialize();

		++this.ticksRan;

		// Tick each world.
		for (int dimension = 0; dimension < this.worldMngr.length; ++dimension) {
			if (dimension == 0 || this.propertyManagerObj.getBooleanProperty("allow-nether", true)) {
				WorldServer world = this.worldMngr[dimension];

				// Send time update every second (20 ticks).
				if (this.ticksRan % 20 == 0) {
					this.configManager.sendPacketToAllPlayersInDimension(
							new Packet4UpdateTime(world.getWorldTime()),
							world.worldProvider.worldType);
				}

				int previousDayOfYear = Seasons.dayOfTheYear;
				world.tick();

				if (Seasons.dayOfTheYear != previousDayOfYear) {
					this.configManager.sendPacketToAllPlayersInDimension(
							new Packet95UpdateDayOfTheYear(Seasons.dayOfTheYear),
							world.worldProvider.worldType);
				}

				world.updateEntities();
			}
		}

		// Process incoming packets.
		this.networkServer.handleNetworkListenThread();

		// Tick the player configuration manager (handles logins, disconnects, etc.).
		this.configManager.onTick();

		// Update all entity trackers.
		for (int i = 0; i < this.entityTracker.length; ++i) {
			this.entityTracker[i].updateTrackedEntities();
		}

		// Update all registered GUI elements.
		for (int i = 0; i < this.tickableGuiElements.size(); ++i) {
			this.tickableGuiElements.get(i).update();
		}

		// Parse pending console commands.
		try {
			this.commandLineParser();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unexpected exception while parsing console command", e);
		}
	}

	/**
	 * Adds a console command to the server's command queue.
	 *
	 * @param command  the command string (without the leading slash)
	 * @param listener the ICommandListener that will process the command
	 */
	public void addCommand(String command, ICommandListener listener) {
		this.pendingCommands.add(new ServerCommand(command, listener));
	}

	/** Parses and executes all pending console commands. */
	public void commandLineParser() {
		while (this.pendingCommands.size() > 0) {
			ServerCommand serverCommand = (ServerCommand) this.pendingCommands.remove(0);
			this.commandHandler.handleCommand(serverCommand);
		}
	}

	/**
	 * Adds an IUpdatePlayerListBox object to the list of GUI elements
	 * that are updated each server tick.
	 *
	 * @param listBox the object to add to the update list
	 */
	public void addToOnlinePlayerList(IUpdatePlayerListBox listBox) {
		this.tickableGuiElements.add(listBox);
	}

	/**
	 * The main entry point for the Minecraft server application.
	 *
	 * @param args command-line arguments (currently only "nogui" is recognized)
	 */
	public static void main(String[] args) {
		StatList.preInit();
		try {
			MinecraftServer server = new MinecraftServer();
			// Initialize the GUI unless running headless or with "nogui" argument.
			if (!GraphicsEnvironment.isHeadless() && (args.length <= 0 || !args[0].equals("nogui"))) {
				ServerGUI.initGui(server);
			}
			(new ThreadServerApplication("Server thread", server)).start();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to start the minecraft server", e);
		}
	}

	/**
	 * Returns a File object for the specified path string.
	 *
	 * @param path the file path
	 * @return a File object for the path
	 */
	public File getFile(String path) {
		return new File(path);
	}

	/** Logs an informational message to the server console. */
	public void log(String message) {
		logger.info(message);
	}

	/** Logs a warning message to the server console. */
	public void logWarning(String message) {
		logger.warning(message);
	}

	/**
	 * Returns the username associated with this command listener.
	 * For the console, this always returns "CONSOLE".
	 */
	public String getUsername() {
		return "CONSOLE";
	}

	/**
	 * Returns the WorldServer instance for the specified dimension.
	 *
	 * @param dimension the dimension ID (-1 for nether, 0 for overworld)
	 * @return the WorldServer instance
	 */
	public WorldServer getWorldManager(int dimension) {
		return dimension == -1 ? this.worldMngr[1] : this.worldMngr[0];
	}

	/**
	 * Returns the EntityTracker instance for the specified dimension.
	 *
	 * @param dimension the dimension ID (-1 for nether, 0 for overworld)
	 * @return the EntityTracker instance
	 */
	public EntityTracker getEntityTracker(int dimension) {
		return dimension == -1 ? this.entityTracker[1] : this.entityTracker[0];
	}

	/**
	 * Checks whether the server is currently running.
	 *
	 * @param server the MinecraftServer instance
	 * @return true if running, false otherwise
	 */
	public static boolean isServerRunning(MinecraftServer server) {
		return server.serverRunning;
	}
}