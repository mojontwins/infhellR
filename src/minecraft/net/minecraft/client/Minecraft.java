package net.minecraft.client;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.io.File;
import java.net.URL;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

import net.minecraft.client.controller.PlayerController;
import net.minecraft.client.controller.PlayerControllerTest;
import net.minecraft.client.effect.EffectRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiAchievement;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiConflictWarning;
import net.minecraft.client.gui.GuiConnecting;
import net.minecraft.client.gui.GuiCreativeInventory;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMinimap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.GuiUnused;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.container.GuiContainerCreative;
import net.minecraft.client.gui.container.GuiInventory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.player.EntityPlayerSP;
import net.minecraft.client.player.MovementInputFromOptions;
import net.minecraft.client.render.EntityRenderer;
import net.minecraft.client.render.GLAllocation;
import net.minecraft.client.render.GraphicsMode;
import net.minecraft.client.render.ItemRenderer;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.RenderManager;
import net.minecraft.client.render.texture.TextureAnimatedFX;
import net.minecraft.client.render.texture.TextureCompassFX;
import net.minecraft.client.render.texture.TextureFlamesFX;
import net.minecraft.client.render.texture.TextureLavaFX;
import net.minecraft.client.render.texture.TextureLavaFlowFX;
import net.minecraft.client.render.texture.TexturePackList;
import net.minecraft.client.render.texture.TexturePortalFX;
import net.minecraft.client.render.texture.TextureRecoveryCompassFX;
import net.minecraft.client.render.texture.TextureWatchFX;
import net.minecraft.client.render.texture.TextureWaterFX;
import net.minecraft.client.render.texture.TextureWaterFlowFX;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.game.EnumMovingObjectType;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.game.MathHelper;
import net.minecraft.game.MinecraftException;
import net.minecraft.game.Version;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.achievements.StatList;
import net.minecraft.game.command.CommandProcessor;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.MovingObjectPosition;
//import net.minecraft.client.ThreadCheckHasPaid;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.Teleporter;
import net.minecraft.game.world.World;
import net.minecraft.game.world.WorldProvider;
import net.minecraft.game.world.WorldSettings;
import net.minecraft.game.world.WorldType;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.ColorizerFoliage;
import net.minecraft.game.world.block.ColorizerGrass;
import net.minecraft.game.world.block.ColorizerWater;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.ChunkProviderLoadOrGenerate;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.chunk.loader.ISaveFormat;
import net.minecraft.game.world.chunk.loader.ISaveHandler;
import net.minecraft.game.world.chunk.loader.SaveConverterMcRegion;
import net.minecraft.game.worldedit.WorldEdit;


/**
 * Main client game class — the central entry point and heartbeat of the InfHell client.
 *
 * <p>This abstract class implements {@link Runnable} so the game loop can be driven
 * on its own thread. Concrete implementations (e.g. {@code MinecraftImpl}) provide
 * the applet/standalone host-specific glue such as {@link #displayUnexpectedThrowable}
 * and crash-screen wiring.</p>
 *
 * <p>Responsibilities include:</p>
 * <ul>
 *   <li>Bootstrapping LWJGL, OpenGL state, and the rendering pipeline.</li>
 *   <li>Loading game settings, save format, texture packs, sounds, and resources.</li>
 *   <li>Driving the main game loop: timing, ticks, input polling, rendering.</li>
 *   <li>Managing the active {@link World}, the local {@link EntityPlayerSP}, the GUI screen stack,
 *       and dimension transitions (e.g. {@link #usePortal()}).</li>
 *   <li>Routing user input (mouse, keyboard, controllers) to the world, GUI, and player.</li>
 * </ul>
 *
 * <p>The class is intentionally large because it acts as a singleton facade: nearly every
 * subsystem ({@link RenderEngine}, {@link World}, {@link EntityRenderer}, etc.) is reachable
 * via direct fields. The companion static {@link #getMinecraft()} accessor returns the
 * single live instance for use by classes that are not passed an explicit reference.</p>
 */
public abstract class Minecraft implements Runnable {
	// ==================== Static singletons & memory ====================

	/**
	 * Pre-allocated byte buffer that can be released to recover memory in low-memory
	 * situations. See {@link #freeMemory()}.
	 */
	public static byte[] reservedBytes = new byte[10485760];

	/** The single live {@code Minecraft} instance, set by the constructor. */
	private static Minecraft theMinecraft;

	// ==================== Core game systems ====================

	/**
	 * Player-side controller that translates input into block/entity actions.
	 * Re-initialized each time the world changes (see {@link #changeWorld}).
	 */
	public PlayerController playerController;

	/** True when the game is currently running in fullscreen mode. */
	private boolean fullscreen = false;

	/** Flag indicating the game has crashed; prevents {@link System#exit} on shutdown. */
	private boolean hasCrashed = false;

	/** Current framebuffer width in pixels. */
	public int displayWidth;

	/** Current framebuffer height in pixels. */
	public int displayHeight;

	/** Cached GL capability probe (multitexture, anisotropic, etc.). */
	private OpenGlCapsChecker glCapabilities;

	/** 20 TPS game timer; tracks elapsed ticks and partial tick for interpolation. */
	private Timer timer = new Timer(20.0F);

	/** Currently loaded world, or {@code null} when at the main menu. */
	public World theWorld;

	/** World renderer (chunk rebuilds, clouds, sky, global render list). */
	public RenderGlobal renderGlobal;

	/** Local player entity (single-player or remote proxy on a multiplayer server). */
	public EntityPlayerSP thePlayer;

	/** Entity used as the camera origin (usually the player, but can be detached). */
	public EntityLiving renderViewEntity;

	/** Particle/splash effect renderer. */
	public EffectRenderer effectRenderer;

	// ==================== Session & display host ====================

	/** Authenticated session for the local user (username + session token). */
	public Session session = null;

	/** URI of the homepage shown on the main menu. */
	public String minecraftUri;

	/** AWT canvas hosting the LWJGL display, when embedded in an applet/window. */
	public Canvas mcCanvas;

	/** Whether the in-game quit button should be hidden (applet mode). */
	public boolean hideQuitButton = false;

	/** True while the game is paused (e.g. a GUI pauses the game). */
	public volatile boolean isGamePaused = false;

	// ==================== Rendering & UI ====================

	/** Texture manager — owns the GL texture cache and animated texture FX. */
	public RenderEngine renderEngine;

	/** Default bitmap font renderer. */
	public FontRenderer fontRenderer;

	/** Currently displayed GUI screen, or {@code null} when in-game. */
	public GuiScreen currentScreen = null;

	/** Loading-screen renderer used during world transitions. */
	public LoadingScreenRenderer loadingScreen = new LoadingScreenRenderer(this);

	/** World-view renderer (camera, hand, lighting, weather). */
	public EntityRenderer entityRenderer;

	// ==================== Resource I/O & timing ====================

	/** Background thread that downloads/caches remote resources. */
	private ThreadDownloadResources downloadResourcesThread;

	/** Total ticks elapsed since the game started (monotonic counter). */
	private int ticksRan = 0;

	/** Counts down between successive left-clicks to throttle block breaking. */
	private int leftClickCounter = 0;

	/** Windowed-mode display dimensions remembered when toggling fullscreen. */
	private int tempDisplayWidth;
	private int tempDisplayHeight;

	/** Achievement toast UI manager. */
	public GuiAchievement guiAchievement = new GuiAchievement(this);

	/** In-game HUD overlay (chat, hotbar, health, debug text, etc.). */
	public GuiIngame ingameGUI;

	/** Set to skip world rendering for a single frame (used during world swaps). */
	public boolean skipRenderWorld = false;

	/** Default biped model used for the local player in inventory screens. */
	public ModelBiped playerModelBiped = new ModelBiped(0.0F);

	/** Result of the latest mouse-over raycast (block, entity, or miss). */
	public MovingObjectPosition objectMouseOver = null;

	// ==================== Settings, applet, sound ====================

	/** Persisted user settings (video options, controls, etc.). */
	public GameSettings gameSettings;

	/** Applet host when running inside a browser, otherwise {@code null}. */
	protected MinecraftApplet mcApplet;

	/** Sound engine managing music, sound effects, and streaming audio. */
	public SoundManager sndManager = new SoundManager();

	/** Helper that translates OS mouse input to in-game look deltas. */
	public MouseHelper mouseHelper;

	/** Manages the list of available texture packs and the active pack. */
	public TexturePackList texturePackList;

	// ==================== Filesystem & saves ====================

	/** Root data directory (~/.minecraft or %APPDATA%/.minecraft, etc.). */
	private File mcDataDir;

	/** Save format converter / loader (handles Anvil/Region migration). */
	private ISaveFormat saveLoader;

	// ==================== Profiling ====================

	/** Rolling buffer of recent frame timestamps (nanoseconds), for profiling. */
	public static long[] frameTimes = new long[512];

	/** Rolling buffer of recent tick timestamps, for profiling. */
	public static long[] tickTimes = new long[512];

	/** How many entries in {@link #frameTimes} / {@link #tickTimes} are valid. */
	public static int numRecordedFrameTimes = 0;

	/** Legacy paid-account check timer (currently unused). */
	public static long hasPaidCheckTime = 0L;

	/** Persistent stats / achievements writer. */
	public StatFileWriter statFileWriter;

	// ==================== Auto-connect target ====================

	/** Server hostname to auto-connect to after startup (may be {@code null}). */
	private String serverName;

	/** Server port to auto-connect to after startup. */
	private int serverPort;

	// ==================== Animated textures ====================

	/** Animated water texture FX instance, registered with the render engine. */
	private TextureWaterFX textureWaterFX = new TextureWaterFX();

	/** Animated lava texture FX instance, registered with the render engine. */
	private TextureLavaFX textureLavaFX = new TextureLavaFX();

	// ==================== Misc ====================

	/** Cached Minecraft data directory (resolved lazily by {@link #getMinecraftDir()}). */
	private static File minecraftDir = null;

	/** Main loop flag — set to false to gracefully exit the run loop. */
	public volatile boolean running = true;

	/** Current debug string displayed on the F3 overlay (FPS, chunk updates). */
	public String debug = "";

	/** Latch preventing auto-repeat while F2 is held. */
	boolean isTakingScreenshot = false;

	/** Timestamp of the previous frame in nanoseconds. */
	long prevFrameTime = -1L;

	/** True when the game (not the GUI) currently owns input focus. */
	public boolean inGameHasFocus = false;

	/** Ticks counter used to throttle continuous mouse-click actions. */
	private int mouseTicksRan = 0;

	/** Legacy weather flag — most weather now lives in the world's weather data. */
	public boolean raining = false;

	/** Last {@link System#currentTimeMillis()} seen by {@link #runTick()} (used to detect stalls). */
	long systemTime = System.currentTimeMillis();

	/** Counter used to periodically trigger ambient entity joins near the player. */
	private int joinPlayerCounter = 0;

	// ==================== Compatibility flags ====================

	/**
	 * Keep this true for the time being — forces a legacy OpenGL pipeline branch
	 * used by older InfHell render paths.
	 */
	public boolean legacyOpenGL = true;

	/** Zan's in-game minimap overlay. */
	public ZanMinimap zanMinimap;
	/**
	 * Constructs a new {@code Minecraft} client host.
	 *
	 * <p>Note that this does <em>not</em> actually start the game — call
	 * {@link #startGame()} (typically invoked by {@link #run()}) to perform
	 * initialization of the renderer, world, sound, and input subsystems.</p>
	 *
	 * @param component   the AWT component (e.g. applet or {@link Frame}) that owns the canvas,
	 *                    or {@code null} for a standalone window
	 * @param canvas      the AWT canvas hosting the LWJGL display, or {@code null} to create
	 *                    a fullscreen LWJGL display
	 * @param applet      the applet host when running inside a browser, otherwise {@code null}
	 * @param width       initial framebuffer width in pixels
	 * @param height      initial framebuffer height in pixels
	 * @param fullscreen  whether to request fullscreen mode at startup
	 */
	public Minecraft(Component component, Canvas canvas, MinecraftApplet applet, int width, int height, boolean fullscreen) {
		// Ensure achievement/stat strings are registered before any GUI can render them.
		StatList.preInit();
		this.tempDisplayHeight = height;
		this.fullscreen = fullscreen;
		this.mcApplet = applet;
		// Spawn a daemon thread that runs forever — used to keep the JVM alive / yield
		// CPU when the main thread is blocked. The "Timer hack thread" name is historical.
		new ThreadSleepForeverClient(this, "Timer hack thread");
		this.mcCanvas = canvas;
		this.displayWidth = width;
		this.displayHeight = height;
		this.fullscreen = fullscreen;

		this.hideQuitButton = false;

		// Register as the singleton instance for static lookups (see getMinecraft()).
		theMinecraft = this;
	}

	/**
	 * Returns the live {@code Minecraft} instance. Set automatically by the constructor;
	 * will be {@code null} until construction completes.
	 *
	 * @return the singleton client instance, or {@code null} if not yet constructed
	 */
	public static Minecraft getMinecraft() {
		return theMinecraft;
	}

	/**
	 * Called when an uncaught exception propagates out of the game loop.
	 * Records the crash flag, then delegates to the concrete implementation
	 * to display the crash dialog/applet screen.
	 *
	 * @param unexpectedThrowable the fatal exception with its stack trace
	 */
	public void onMinecraftCrash(UnexpectedThrowable unexpectedThrowable) {
		this.hasCrashed = true;
		this.displayUnexpectedThrowable(unexpectedThrowable);
	}

	/**
	 * Displays the given fatal throwable to the user.
	 * Implemented by the concrete subclass (applet or standalone) to show
	 * an appropriate GUI.
	 *
	 * @param unexpectedThrowable the fatal exception to display
	 */
	public abstract void displayUnexpectedThrowable(UnexpectedThrowable unexpectedThrowable);

	/**
	 * Sets the server hostname and port to auto-connect to after startup.
	 * Used when launching from a multiplayer shortcut.
	 *
	 * @param name the server address (IP or hostname)
	 * @param port the server port
	 */
	public void setServer(String name, int port) {
		this.serverName = name;
		this.serverPort = port;
	}

	/**
	 * Initializes LWJGL, OpenGL, the render engine, sounds, and all client subsystems,
	 * then shows either the main menu or the connect-to-server dialog.
	 *
	 * <p>This is the primary startup routine, called once by {@link #run()}.
	 * It sets up the entire rendering pipeline and resource loading chain.</p>
	 *
	 * @throws LWJGLException if the display or OpenGL context could not be created
	 */
	public void startGame() throws LWJGLException {
		// =====================================================================
		// Step 1: Display initialization
		// =====================================================================

		// Paint a black rectangle while LWJGL initializes to avoid a white flash.
		if(this.mcCanvas != null) {
			Graphics g = this.mcCanvas.getGraphics();
			if(g != null) {
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, this.displayWidth, this.displayHeight);
				g.dispose();
			}

			// Embed the LWJGL display inside the AWT canvas (applet / embedded mode).
			Display.setParent(this.mcCanvas);
		} else if(this.fullscreen) {
			// Request exclusive fullscreen mode.
			Display.setFullscreen(true);
			this.displayWidth = Display.getDisplayMode().getWidth();
			this.displayHeight = Display.getDisplayMode().getHeight();
			if(this.displayWidth <= 0) {
				this.displayWidth = 1;
			}

			if(this.displayHeight <= 0) {
				this.displayHeight = 1;
			}
		} else {
			// Windowed mode: use the dimensions passed to the constructor.
			Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
		}

		Display.setTitle("Minecraft " + Version.getVersion());

		// =====================================================================
		// Step 2: Create the OpenGL context
		// =====================================================================

		// Request a 24-bit depth buffer for proper 3D rendering.
		// Fall back to the default pixel format if this fails.
		try {
			PixelFormat pixelFormat = new PixelFormat();
			pixelFormat = pixelFormat.withDepthBits(24);
			Display.create(pixelFormat);
		} catch (LWJGLException e) {
			e.printStackTrace();

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ignored) {
			}

			Display.create();
		}

		// Initialize OpenGL helper utilities (multi-texture coordinates, etc.).
		OpenGlHelper.initializeTextures();

		// =====================================================================
		// Step 3: File system, settings, and resource management
		// =====================================================================

		this.mcDataDir = getMinecraftDir();
		// Save handler points to <minecraftDir>/saves/
		this.saveLoader = new SaveConverterMcRegion(new File(this.mcDataDir, "saves"));
		this.gameSettings = new GameSettings(this, this.mcDataDir);
		this.zanMinimap = new ZanMinimap(this, this.mcDataDir);
		// Texture pack manager resolves and opens zip/folder resource packs.
		this.texturePackList = new TexturePackList(this, this.mcDataDir);

		// =====================================================================
		// Step 4: Render engine, fonts, and colorizers
		// =====================================================================

		this.renderEngine = new RenderEngine(this.texturePackList, this.gameSettings);
		this.fontRenderer = new FontRenderer(this.gameSettings, "/font/default.png", this.renderEngine);

		// Load biome/foliage/water color ramps used by terrain shaders.
		ColorizerWater.setColorRamp(this.renderEngine.getTextureContents("/misc/watercolor.png"));
		ColorizerGrass.setColorRamp(this.renderEngine.getTextureContents("/misc/grasscolor.png"));
		ColorizerFoliage.setColorRamp(this.renderEngine.getTextureContents("/misc/foliagecolor.png"));

		// Build the biome color lookup table from the biomelut.png asset.
		BiomeGenBase.setBuffer(this.renderEngine.getTextureContents("/biomelut.png"));
		BiomeGenBase.loadBiomeLookup();

		// =====================================================================
		// Step 5: Command system registration
		// =====================================================================

		CommandProcessor.registerCommands();
		WorldEdit.registerCommands();

		// =====================================================================
		// Step 6: Entity & item renderers
		// =====================================================================

		this.entityRenderer = new EntityRenderer(this);
		RenderManager.instance.itemRenderer = new ItemRenderer(this);

		// =====================================================================
		// Step 7: Statistics / achievements
		// =====================================================================

		this.statFileWriter = new StatFileWriter(this.session, this.mcDataDir);
		// Wire the "Open Inventory" achievement to the key-inventory formatter.
		AchievementList.openInventory.setStatStringFormatter(new StatStringFormatKeyInv(this));

		// Render the mojang logo splash screen before any further work.
		this.loadScreen();

		// =====================================================================
		// Step 8: Input subsystem (keyboard, mouse, gamepads)
		// =====================================================================

		Keyboard.create();
		Mouse.create();
		this.mouseHelper = new MouseHelper(this.mcCanvas);

		// Initialize the gamepad/controller subsystem.
		// Silently ignored on systems without controllers.
		try {
			Controllers.create();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// =====================================================================
		// Step 9: OpenGL state setup (default rendering state)
		// =====================================================================

		this.checkGLError("Pre startup");
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);          // Smooth (Gouraud) shading for 3D faces.
		GL11.glClearDepth(1.0D);                    // Maximum clear depth.
		GL11.glEnable(GL11.GL_DEPTH_TEST);          // Depth buffer test (z-fighting prevention).
		GL11.glDepthFunc(GL11.GL_LEQUAL);           // Pass if new depth <= old depth.
		GL11.glEnable(GL11.GL_ALPHA_TEST);          // Alpha test for transparent textures.
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);   // Discard fragments with alpha <= 0.1.
		GL11.glCullFace(GL11.GL_BACK);              // Back-face culling (performance).
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		this.checkGLError("Startup");

		// Cache OpenGL extension capabilities (multi-texture, anisotropic filtering, etc.).
		this.glCapabilities = new OpenGlCapsChecker();

		// =====================================================================
		// Step 10: Sound engine
		// =====================================================================

		this.sndManager.loadSoundSettings(this.gameSettings);

		// =====================================================================
		// Step 11: Register animated texture FX with the render engine
		// =====================================================================

		// Animated water and lava textures.
		this.renderEngine.registerTextureFX(this.textureLavaFX);
		this.renderEngine.registerTextureFX(this.textureWaterFX);
		// Nether portal swirl effect.
		this.renderEngine.registerTextureFX(new TexturePortalFX());
		// Compass (points toward spawn).
		this.renderEngine.registerTextureFX(new TextureCompassFX(this, Item.compass.getIconFromDamage(0)));
		// Recovery compass (points toward death point).
		this.renderEngine.registerTextureFX(new TextureRecoveryCompassFX(this, Item.recoveryCompass.getIconFromDamage(0)));
		// Watch/hourglass animation on held clock.
		this.renderEngine.registerTextureFX(new TextureWatchFX(this));
		// Flowing water and lava variants.
		this.renderEngine.registerTextureFX(new TextureWaterFlowFX());
		this.renderEngine.registerTextureFX(new TextureLavaFlowFX());
		// Animated flame textures (for furnaces, portals).
		this.renderEngine.registerTextureFX(new TextureFlamesFX(0));
		this.renderEngine.registerTextureFX(new TextureFlamesFX(1));

		// Custom texture-atlas based seaweed animation.
		this.renderEngine.registerTextureFX(new TextureAnimatedFX(15*16+12, 0, "/animated/block_seaweed.png", 1));

		// =====================================================================
		// Step 12: Global renderer and viewport
		// =====================================================================

		this.renderGlobal = new RenderGlobal(this, this.renderEngine);
		GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);
		this.effectRenderer = new EffectRenderer(this.theWorld, this.renderEngine);

		// =====================================================================
		// Step 13: Background resource download thread
		// =====================================================================

		try {
			this.downloadResourcesThread = new ThreadDownloadResources(this.mcDataDir, this);
			this.downloadResourcesThread.start();
		} catch (Exception e) {
			// Non-fatal: the thread will retry or use cached resources.
		}

		// Install any extra bundled resources (custom textures, shaders, etc.).
		MoreResourcesInstaller moreResourcesInstaller = new MoreResourcesInstaller(this);
		moreResourcesInstaller.installResources();

		this.checkGLError("Post startup");

		// =====================================================================
		// Step 14: In-game HUD and initial screen
		// =====================================================================

		this.ingameGUI = new GuiIngame(this);

		// Show either the server-connect dialog or the main menu.
		if(this.serverName != null) {
			this.displayGuiScreen(new GuiConnecting(this, this.serverName, this.serverPort));
		} else {
			this.displayGuiScreen(new GuiMainMenu());
		}

	}

	/**
	 * Renders the Mojang logo splash screen shown briefly during startup.
	 * Uses 2D orthographic projection so the textured quad can be drawn
	 * at exact pixel coordinates.
	 *
	 * @throws LWJGLException if the display cannot be swapped to present the buffer
	 */
	private void loadScreen() throws LWJGLException {
		ScaledResolution scaledResolution = new ScaledResolution(this.gameSettings, this.displayWidth, this.displayHeight);
		// Clear both color and depth buffers in case anything has been drawn before.
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Switch to orthographic projection: pixel-space coordinates for the splash quad.
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		// Origin at top-left, Y grows downward, Z range 1000..3000 places the quad in front.
		GL11.glOrtho(0.0D, scaledResolution.scaledWidthD, scaledResolution.scaledHeightD, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		// Pull the camera back so that the ortho quad with z=0 falls inside the depth range.
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

		GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);

		// Black background for the splash screen.
		GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);

		Tessellator tessellator = Tessellator.instance;
		// Disable 3D lighting so the texture is rendered at full brightness.
		GL11.glDisable(GL11.GL_LIGHTING);
		// Enable texturing to render the background image.
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// Disable fog so the splash image is rendered with crisp colors.
		GL11.glDisable(GL11.GL_FOG);

		// Draw the background Mojang image stretched to fill the screen.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/title/mojang.png"));
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0xFFFFFF);
		tessellator.addVertexWithUV(0.0D, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
		tessellator.addVertexWithUV((double)this.displayWidth, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
		tessellator.addVertexWithUV((double)this.displayWidth, 0.0D, 0.0D, 0.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
		tessellator.draw();

		// Overlay the centered logo on top of the background.
		short logoWidth = 256;
		short logoHeight = 256;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		tessellator.setColorOpaque_I(0xFFFFFF);
		this.scaledTessellator((scaledResolution.getScaledWidth() - logoWidth) / 2, (scaledResolution.getScaledHeight() - logoHeight) / 2, 0, 0, logoWidth, logoHeight);

		// Re-enable alpha testing for the logo (it contains transparent pixels).
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

		// Present the splash frame to the user immediately.
		Display.swapBuffers();
	}

	/**
	 * Helper that draws a textured quad at 2D screen coordinates using the standard
	 * 1/256 UV unit (each pixel maps to 0.00390625 of texture space — i.e. 1/256).
	 *
	 * @param x    screen-space X of the top-left corner (pre-scale pixels)
	 * @param y    screen-space Y of the top-left corner (pre-scale pixels)
	 * @param u    texture U coordinate in 1/256-pixel units (origin top-left)
	 * @param v    texture V coordinate in 1/256-pixel units (origin top-left)
	 * @param texW quad width in 1/256-pixel units (u-increment)
	 * @param texH quad height in 1/256-pixel units (v-increment)
	 */
	public void scaledTessellator(int x, int y, int u, int v, int texW, int texH) {
		float texU = 0.00390625F;   // = 1/256 — pixel-to-UV conversion factor
		float texV = 0.00390625F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		// Bottom-left
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + texH), 0.0D, (double)((float)(u + 0) * texU), (double)((float)(v + texH) * texV));
		// Bottom-right
		tessellator.addVertexWithUV((double)(x + texW), (double)(y + texH), 0.0D, (double)((float)(u + texW) * texU), (double)((float)(v + texH) * texV));
		// Top-right
		tessellator.addVertexWithUV((double)(x + texW), (double)(y + 0), 0.0D, (double)((float)(u + texW) * texU), (double)((float)(v + 0) * texV));
		// Top-left
		tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), 0.0D, (double)((float)(u + 0) * texU), (double)((float)(v + 0) * texV));
		tessellator.draw();
	}

	/**
	 * Returns the Minecraft data directory (~/.minecraft on Linux/macOS,
	 * %APPDATA%/.minecraft on Windows, etc.).
	 *
	 * <p>The directory is resolved lazily on first call.</p>
	 *
	 * @return the resolved directory
	 */
	public static File getMinecraftDir() {
		if(minecraftDir == null) {
			minecraftDir = getAppDir("minecraft");
		}

		return minecraftDir;
	}

	/**
	 * Resolves a per-user application directory appropriate for the current OS:
	 * <ul>
	 *   <li>Linux/Solaris: {@code ~/.appName/}</li>
	 *   <li>Windows: {@code %APPDATA%\.appName/} or {@code ~/.appName/}</li>
	 *   <li>macOS: {@code ~/Library/Application Support/appName}</li>
	 * </ul>
	 * Creates the directory if it does not exist.
	 *
	 * @param appName the application name (used as the directory basename)
	 * @return the resolved/created directory
	 * @throws RuntimeException if the directory could not be created
	 */
	public static File getAppDir(String appName) {
		String userHome = System.getProperty("user.home", ".");
		File dir;
		switch(getOs()) {
		case linux:
		case solaris:
			dir = new File(userHome, '.' + appName + '/');
			break;
		case windows:
			String appdata = System.getenv("APPDATA");
			if(appdata != null) {
				dir = new File(appdata, "." + appName + '/');
			} else {
				dir = new File(userHome, '.' + appName + '/');
			}
			break;
		case macos:
			dir = new File(userHome, "Library/Application Support/" + appName);
			break;
		default:
			dir = new File(userHome, appName + '/');
		}

		if(!dir.exists() && !dir.mkdirs()) {
			throw new RuntimeException("The working directory could not be created: " + dir);
		} else {
			return dir;
		}
	}

	/**
	 * Detects the host operating system from the {@code os.name} system property.
	 *
	 * @return the matching {@link EnumOS2} value, or {@link EnumOS2#unknown} if no match
	 */
	public static EnumOS2 getOs() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("win") ? EnumOS2.windows : (osName.contains("mac") ? EnumOS2.macos : (osName.contains("solaris") ? EnumOS2.solaris : (osName.contains("sunos") ? EnumOS2.solaris : (osName.contains("linux") ? EnumOS2.linux : (osName.contains("unix") ? EnumOS2.linux : EnumOS2.unknown)))));
	}

	/**
	 * Returns the save format used to load/convert saves.
	 *
	 * @return the active {@link ISaveFormat} (typically {@link SaveConverterMcRegion})
	 */
	public ISaveFormat getSaveLoader() {
		return this.saveLoader;
	}

	/**
	 * Replaces the currently displayed GUI screen. Handles closing the old screen,
	 * flushing stats, picking fallback screens (game-over, main menu), and resetting
	 * the input focus state.
	 *
	 * <p>Special cases:</p>
	 * <ul>
	 *   <li>If the current screen is a {@link GuiUnused}, this is a no-op (the
	 *       unused-screen guard is used during teardown).</li>
	 *   <li>Closing the current screen triggers its {@link GuiScreen#onGuiClosed()} callback.</li>
	 *   <li>Returning to the main menu saves stats and clears chat.</li>
	 *   <li>If the requested screen is {@code null} and the player is dead,
	 *       the death screen is shown instead.</li>
	 * </ul>
	 *
	 * @param guiScreen the screen to display, or {@code null} to return to in-game
	 */
	public void displayGuiScreen(GuiScreen guiScreen) {
		// Don't allow screen swaps while the unused-screen guard is active
		// (used during shutdown / world tear-down).
		if(!(this.currentScreen instanceof GuiUnused)) {
			if(this.currentScreen != null) {
				this.currentScreen.onGuiClosed();
			}

			// Returning to the main menu flushes achievement/score stats.
			if(guiScreen instanceof GuiMainMenu) {
				this.statFileWriter.func_27175_b();
			}

			this.statFileWriter.syncStats();

			// Default fallback screens when none is provided.
			if(guiScreen == null && this.theWorld == null) {
				guiScreen = new GuiMainMenu();
			} else if(guiScreen == null && this.thePlayer.health <= 0) {
				guiScreen = new GuiGameOver();
			}

			// Clear chat history when returning to the main menu.
			if(guiScreen instanceof GuiMainMenu) {
				this.ingameGUI.clearChatMessages();
			}

			this.currentScreen = (GuiScreen)guiScreen;
			if(guiScreen != null) {
				// Releasing in-game focus prevents the game from eating keystrokes
				// intended for the GUI.
				this.setIngameNotInFocus();
				ScaledResolution scaledResolution = new ScaledResolution(this.gameSettings, this.displayWidth, this.displayHeight);
				int scaledWidth = scaledResolution.getScaledWidth();
				int scaledHeight = scaledResolution.getScaledHeight();
				// Hand the GUI the world reference and the current scaled viewport.
				((GuiScreen)guiScreen).setWorldAndResolution(this, scaledWidth, scaledHeight);
				this.skipRenderWorld = false;
			} else {
				// Returning in-game: re-grab the cursor.
				this.setIngameFocus();
			}

		}
	}

	/**
	 * Polls the current OpenGL error flag and prints a human-readable message
	 * to stdout if an error is set.
	 *
	 * @param context a short tag (e.g. "Pre startup") identifying when the check ran
	 */
	public void checkGLError(String context) {
		int error = GL11.glGetError();
		if(error != 0) {
			String errorString = GLU.gluErrorString(error);
			System.out.println("########## GL ERROR ##########");
			System.out.println("@ " + context);
			System.out.println(error + ": " + errorString);
		}

	}

	/**
	 * Performs a clean shutdown of the client: flushes stats, saves the world,
	 * releases GL textures/display lists, closes sound and input, destroys the
	 * display, and exits the JVM.
	 *
	 * <p>Safe to call from crash paths — every step is wrapped in
	 * {@code try/catch(Throwable)} so a failure in one step does not skip later steps.
	 * The final {@link Display#destroy()} and {@link System#exit} run in a
	 * {@code finally} block, so the display is always destroyed.</p>
	 */
	public void shutdownMinecraftApplet() {
		try {
			// Persist achievements / stats before exit.
			this.statFileWriter.func_27175_b();
			this.statFileWriter.syncStats();
			if(this.mcApplet != null) {
				this.mcApplet.clearApplet();
			}

			try {
				if(this.downloadResourcesThread != null) {
					this.downloadResourcesThread.closeMinecraft();
				}
			} catch (Exception e) {
			}

			System.out.println("Stopping!");

			try {
				// Tear down the world (and the player) cleanly.
				this.clearWorld((World)null);
			} catch (Throwable t) {
			}

			try {
				// Release all GL textures and display lists we allocated.
				GLAllocation.deleteTexturesAndDisplayLists();
			} catch (Throwable t) {
			}

			// Close audio and input devices.
			this.sndManager.closeMinecraft();
			Mouse.destroy();
			Keyboard.destroy();
		} finally {
			// Destroy the display and exit (unless the crash path is handling it).
			Display.destroy();
			if(!this.hasCrashed) {
				System.exit(0);
			}

		}

		// Final GC pass to encourage native resource release.
		System.gc();
	}

	/**
	 * The main client game loop. Runs on its own high-priority thread (set to priority 10
	 * in {@link #startMainThread}).
	 *
	 * <p>Structure of each frame:</p>
	 * <ol>
	 *   <li>Handle applet lifecycle (abort if the applet is no longer active).</li>
	 *   <li>Clear per-frame memory pools ({@link AxisAlignedBB}, {@link Vec3D}).</li>
	 *   <li>Detect display close requests and abort if requested.</li>
	 *   <li>Update the game timer; run one or more ticks if not paused.</li>
	 *   <li>Render the world (if not paused and not skipped).</li>
	 *   <li>Handle focus loss (toggle fullscreen or sleep).</li>
	 *   <li>Present the frame with {@link Display#update()}.</li>
	 *   <li>Handle screenshots, canvas resize, and debug overlay updates.</li>
	 * </ol>
	 *
	 * @see #runTick()
	 * @see #entityRenderer
	 */
	public void run() {
		// Provide the running instance to the helper that bridges event listeners.
		MainClientAid.setMinecraft(this);

		this.running = true;

		// Bootstrap: initialize the display, GL context, textures, sounds, etc.
		try {
			this.startGame();
		} catch (Exception e) {
			e.printStackTrace();
			this.onMinecraftCrash(new UnexpectedThrowable("Failed to start game", e));
			return;
		}

		// =====================================================================
		// Main game loop
		// =====================================================================

		try {
			long lastDebugUpdate = System.currentTimeMillis();
			int frameCount = 0;

			while(this.running) {
				// -----------------------------------------------------------------
				// 1. Applet lifecycle check — abort if the browser unloaded the applet.
				// -----------------------------------------------------------------
				if(this.mcApplet != null && !this.mcApplet.isActive()) {
					break;
				}

				// -----------------------------------------------------------------
				// 2. Clear per-frame memory pools so they don't grow indefinitely.
				// -----------------------------------------------------------------
				AxisAlignedBB.clearBoundingBoxPool();
				Vec3D.initialize();

				// -----------------------------------------------------------------
				// 3. Detect window close button and shut down gracefully.
				// -----------------------------------------------------------------
				if(this.mcCanvas == null && Display.isCloseRequested()) {
					this.shutdown();
				}

				// -----------------------------------------------------------------
				// 4. Update the game timer.
				// -----------------------------------------------------------------
				// When paused, preserve the render partial-ticks so rendering
				// does not jump when the game resumes.
				if(this.isGamePaused && this.theWorld != null) {
					float prevTicks = this.timer.renderPartialTicks;
					this.timer.updateTimer();
					this.timer.renderPartialTicks = prevTicks;
				} else {
					this.timer.updateTimer();
				}

				// -----------------------------------------------------------------
				// 5. Run one or more world ticks (20 TPS).
				// -----------------------------------------------------------------
				for(int tick = 0; tick < this.timer.elapsedTicks; ++tick) {
					++this.ticksRan;

					try {
						this.runTick();
					} catch (MinecraftException e) {
						// Network conflict or world error: drop the world and show the
						// conflict warning screen, which allows the user to reconnect.
						this.theWorld = null;
						this.clearWorld((World)null);
						this.displayGuiScreen(new GuiConflictWarning());
					}
				}

				// -----------------------------------------------------------------
				// 6. Pre-render GL state
				// -----------------------------------------------------------------
				this.checkGLError("Pre render");
				// Hard-disable fancy grass (compatibility with older render pipeline).
				RenderBlocks.fancyGrass = false;
				// Update the audio listener position for doppler/shading effects.
				this.sndManager.setListener(this.thePlayer, this.timer.renderPartialTicks);

				GL11.glEnable(GL11.GL_TEXTURE_2D);

				/*
				 * Threaded lighting is currently disabled.
				 * If re-enabled, it should run here while the world is loaded.
				 */

				// -----------------------------------------------------------------
				// 7. Present the frame to the display.
				// F7 held skips the present (used for screenshot rendering).
				// -----------------------------------------------------------------
				if(!Keyboard.isKeyDown(Keyboard.KEY_F7)) {
					Display.update();
				}

				// -----------------------------------------------------------------
				// 8. Force third-person view off when the player's camera is inside
				//    a block (prevents clipping through solid blocks).
				// -----------------------------------------------------------------
				if(this.thePlayer != null && this.thePlayer.isEntityInsideOpaqueBlock()) {
					GameSettingsValues.thirdPersonView = false;
				}

				// -----------------------------------------------------------------
				// 9. Render the world if not skipped.
				// -----------------------------------------------------------------
				if(!this.skipRenderWorld) {
					if(this.playerController != null) {
						// Pass partial ticks to the controller for smooth animation.
						this.playerController.setPartialTime(this.timer.renderPartialTicks);
					}

					// Camera update + 3D world render.
					this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks);

					// Zan's minimap tick (only when not showing debug info and either
					// in-game or viewing the minimap overlay).
					if (this.theWorld != null && this.thePlayer != null) {
						if(!GameSettingsValues.showDebugInfo && (this.currentScreen == null || this.currentScreen instanceof GuiMinimap)) {
							zanMinimap.OnTickInGame(this);
						}
					}
				}

				// -----------------------------------------------------------------
				// 10. Handle display focus loss.
				// -----------------------------------------------------------------
				if(!Display.isActive()) {
					if(this.fullscreen) {
						// Exit fullscreen when the window loses focus.
						this.toggleFullscreen();
					}

					// Sleep briefly to avoid busy-waiting while minimized.
					Thread.sleep(10L);
				}

				// Record frame timestamp for profiling.
				this.prevFrameTime = System.nanoTime();

				// Update the achievement notification queue.
				this.guiAchievement.updateAchievementWindow();

				// Yield to other threads (audio, download) to keep the CPU sane.
				Thread.yield();

				// Second present when F7 held (enables proper screenshot capture).
				if(Keyboard.isKeyDown(Keyboard.KEY_F7)) {
					Display.update();
				}

				// -----------------------------------------------------------------
				// 11. Screenshot capture (F2)
				// -----------------------------------------------------------------
				this.screenshotListener();

				// -----------------------------------------------------------------
				// 12. Canvas resize: if the AWT canvas has changed size (e.g. user
				//    dragged the window edge), update the display and resize the GUI.
				// -----------------------------------------------------------------
				if(this.mcCanvas != null && !this.fullscreen && (this.mcCanvas.getWidth() != this.displayWidth || this.mcCanvas.getHeight() != this.displayHeight)) {
					this.displayWidth = this.mcCanvas.getWidth();
					this.displayHeight = this.mcCanvas.getHeight();
					if(this.displayWidth <= 0) {
						this.displayWidth = 1;
					}

					if(this.displayHeight <= 0) {
						this.displayHeight = 1;
					}

					this.resize(this.displayWidth, this.displayHeight);
				}

				// -----------------------------------------------------------------
				// 13. Debug info overlay: update FPS and chunk-update counters.
				// -----------------------------------------------------------------
				this.checkGLError("Post render");
				++frameCount;

				// Update debug string every second.
				for(this.isGamePaused = !this.isRemote() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame(); System.currentTimeMillis() >= lastDebugUpdate + 1000L; frameCount = 0) {
					this.debug = frameCount + " fps, " + WorldRenderer.chunksUpdated + " chunk updates";
					WorldRenderer.chunksUpdated = 0;
					lastDebugUpdate += 1000L;
				}
			}
		} catch (MinecraftError e) {
			// MinecraftError is unrecoverable — exit silently.
		} catch (Throwable e) {
			// Unexpected fatal error: try to free memory, print stack, and show crash screen.
			this.freeMemory();
			e.printStackTrace();
			this.onMinecraftCrash(new UnexpectedThrowable("Unexpected error", e));
		} finally {
			// Always perform cleanup, even after a fatal error.
			this.shutdownMinecraftApplet();
		}

	}

	/**
	 * Emergency memory-recovery routine. Called when the JVM raises an
	 * {@link OutOfMemoryError}, or any other catastrophic failure.
	 *
	 * <p>Releases the {@link #reservedBytes} buffer, deletes GL display lists,
	 * drops world references, and forces several GC passes.</p>
	 */
	public void freeMemory() {
		try {
			// Drop the pre-allocated 10 MB recovery buffer.
			reservedBytes = new byte[0];
			// Release chunk-renderer display lists.
			this.renderGlobal.deleteDisplayLists();
		} catch (Throwable t) {
		}

		try {
			System.gc();
			AxisAlignedBB.clearBoundingBoxes();
			Vec3D.clearVectorList();
		} catch (Throwable t) {
		}

		try {
			System.gc();
			// Detach from the current world so it can be collected.
			this.clearWorld((World)null);
		} catch (Throwable t) {
		}

		System.gc();
	}

	/**
	 * Listens for the screenshot key (F2) and saves a PNG when pressed.
	 * Latched via {@link #isTakingScreenshot} so a single press only triggers one save.
	 */
	private void screenshotListener() {
		if(Keyboard.isKeyDown(Keyboard.KEY_F2)) {
			if(!this.isTakingScreenshot) {
				this.isTakingScreenshot = true;
				this.ingameGUI.addChatMessage(ScreenShotHelper.saveScreenshot(minecraftDir, this.displayWidth, this.displayHeight));
			}
		} else {
			this.isTakingScreenshot = false;
		}

	}

	/**
	 * Requests the main game loop to exit on the next iteration.
	 * Safe to call from any thread — {@code running} is volatile.
	 */
	public void shutdown() {
		this.running = false;
	}

	/**
	 * Marks the in-game view as having input focus, which:
	 * <ul>
	 *   <li>Grabs the mouse cursor (locked to the window).</li>
	 *   <li>Dismisses any open GUI by switching back to {@code null}.</li>
	 *   <li>Resets the {@link #leftClickCounter} to prevent an immediate click-through.</li>
	 *   <li>Sets {@link #mouseTicksRan} to suppress auto-repeat clicks for a moment.</li>
	 * </ul>
	 * Only takes effect when the display is currently the focused window.
	 */
	public void setIngameFocus() {
		if(Display.isActive()) {
			if(!this.inGameHasFocus) {
				this.inGameHasFocus = true;
				this.mouseHelper.grabMouseCursor();
				this.displayGuiScreen((GuiScreen)null);
				this.leftClickCounter = 10000;
				this.mouseTicksRan = this.ticksRan + 10000;
			}
		}
	}

	/**
	 * Releases in-game input focus when a GUI takes over:
	 * <ul>
	 *   <li>Resets the player's held-key state.</li>
	 *   <li>Ungrabs the mouse cursor.</li>
	 * </ul>
	 */
	public void setIngameNotInFocus() {
		if(this.inGameHasFocus) {
			if(this.thePlayer != null) {
				this.thePlayer.resetPlayerKeyState();
			}

			this.inGameHasFocus = false;
			this.mouseHelper.ungrabMouseCursor();
		}
	}

	/**
	 * Opens the in-game pause menu if no other screen is currently displayed.
	 */
	public void displayInGameMenu() {
		if(this.currentScreen == null) {
			this.displayGuiScreen(new GuiIngameMenu());
		}
	}

	/**
	 * Called every tick to handle left-mouse-button held-down block interaction
	 * (the "dragging" part of block breaking).
	 *
	 * <p>When {@code rightClick} is false, resets the {@link #leftClickCounter} so the
	 * next {@code clickMouse(0)} call registers immediately. When the player is looking at
	 * a block, forwards the continuous break to the {@link PlayerController}.</p>
	 *
	 * @param button      the mouse button (0 = left, 1 = right)
	 * @param rightClick  true if this is a right-click drag; false for left-click
	 */
	private void sendClickBlockToController(int button, boolean rightClick) {
		if(!this.playerController.isInTestMode) {
			// Reset the click throttle only on the initial left-press event.
			if(!rightClick) {
				this.leftClickCounter = 0;
			}

			// Throttle: only process block interactions at least every leftClickCounter ticks.
			if(button != 0 || this.leftClickCounter <= 0) {
				if(rightClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE && button == 0) {
					// Player is holding right mouse while looking at a block — show block-breaking
					// particles and tell the controller to advance the block-break animation.
					int bx = this.objectMouseOver.blockX;
					int by = this.objectMouseOver.blockY;
					int bz = this.objectMouseOver.blockZ;
					this.playerController.sendBlockRemoving(bx, by, bz, this.objectMouseOver.sideHit);
					this.effectRenderer.addBlockHitEffects(bx, by, bz, this.objectMouseOver.sideHit);
				} else {
					// No block targeted or button not relevant: tell controller to cancel any ongoing break.
					this.playerController.resetBlockRemoving();
				}

			}
		}
	}

	/**
	 * Dispatches a mouse click to the appropriate world or GUI handler:
	 * <ul>
	 *   <li>Left-click on entity → {@link PlayerController#attackEntity}</li>
	 *   <li>Right-click on entity → {@link PlayerController#interactWithEntity}</li>
	 *   <li>Left-click on block → {@link PlayerController#clickBlock} (initiates mining)</li>
	 *   <li>Right-click on block → {@link PlayerController#sendPlaceBlock} (places or uses item)</li>
	 *   <li>Right-click with item → {@link PlayerController#sendUseItem}</li>
	 * </ul>
	 *
	 * @param button  0 = left click, 1 = right click, 2 = middle click
	 */
	private void clickMouse(int button) {

		if(button != 0 || this.leftClickCounter <= 0) {
			// Animate the player's arm swing on left-click.
			if(button == 0) {
				this.thePlayer.swingItem();
			}

			boolean flag = true;
			if(this.objectMouseOver == null) {
				// Clicked empty space: throttle subsequent clicks.
				if (button == 0) {
					this.leftClickCounter = this.thePlayer.isCreative ? 3 : 10;
				}
			} else if(this.objectMouseOver.typeOfHit == EnumMovingObjectType.ENTITY) {
				// Clicked an entity.
				if(button == 0) {
					// Left-click on entity: attack it.
					this.playerController.attackEntity(this.thePlayer, this.objectMouseOver.entityHit);
				}

				if(button == 1) {
					// Right-click on entity: interact (e.g. use item on entity).
					this.playerController.interactWithEntity(this.thePlayer, this.objectMouseOver.entityHit);
				}
			} else if(this.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
				// Clicked a block.
				int x = this.objectMouseOver.blockX;
				int y = this.objectMouseOver.blockY;
				int z = this.objectMouseOver.blockZ;
				int face = this.objectMouseOver.sideHit;

				// Exact hit coordinates within the face for item placement.
				float xWithinFace = (float) (this.objectMouseOver.hitVec.xCoord - (float)x);
				float yWithinFace = (float) (this.objectMouseOver.hitVec.yCoord - (float)y);
				float zWithinFace = (float) (this.objectMouseOver.hitVec.zCoord - (float)z);

				if(button == 0) {
					// Left-click: initiate block breaking.
					this.playerController.clickBlock(x, y, z, this.objectMouseOver.sideHit);
				} else {
					// Right-click: place or interact with block.
					ItemStack itemStack = this.thePlayer.inventory.getCurrentItem();
					int stackSize = itemStack != null ? itemStack.stackSize : 0;
					// Shift-click flag: used by the controller to determine placement behavior.
					byte shift = (byte)(this.thePlayer.isSneaking() ? 1 : 0);
					if(this.playerController.sendPlaceBlock(this.thePlayer, this.theWorld, itemStack, x, y, z, face, xWithinFace, yWithinFace, zWithinFace, shift)) {
						flag = false;
						this.thePlayer.swingItem();
					}

					if(itemStack == null) {
						return;
					}

					// Item was fully consumed — clear the slot.
					if(itemStack.stackSize == 0) {
						this.thePlayer.inventory.mainInventory[this.thePlayer.inventory.currentItem] = null;
					} else if(itemStack.stackSize != stackSize) {
						// Partially used — reset the equipped-item animation.
						this.entityRenderer.itemRenderer.resetEquippedProgress();
					}
				}
			}

			// Right-click on empty space: use the held item (e.g. bow, food, potion).
			if(flag && button == 1) {
				ItemStack currentItem = this.thePlayer.inventory.getCurrentItem();
				if(currentItem != null && this.playerController.sendUseItem(this.thePlayer, this.theWorld, currentItem)) {
					this.entityRenderer.itemRenderer.resetEquippedProgress();
				}
			}

		}
	}

	/**
	 * Toggles between windowed and fullscreen display mode, preserving the GUI
	 * resolution across the transition.
	 *
	 * <p>When entering fullscreen, the resolution is determined by either
	 * the desktop mode or a configured {@link GraphicsMode} from settings.
	 * When exiting fullscreen, the previous windowed dimensions are restored.</p>
	 *
	 * <p>After changing the display mode, the current GUI screen is resized
	 * so it continues to render correctly in the new resolution.</p>
	 */
	public void toggleFullscreen() {
		try {
			this.fullscreen = !this.fullscreen;
			if(this.fullscreen) {
				// Enter fullscreen — try a user-configured mode first, then fall back to desktop.
				if(GameSettingsValues.displayMode == null || "DEFAULT".equals(GameSettingsValues.displayMode)) {
					Display.setDisplayMode(Display.getDesktopDisplayMode());
				} else {
					GraphicsMode graphicsMode = new GraphicsMode(GameSettingsValues.displayMode);
					final DisplayMode[] displayModes = Display.getAvailableDisplayModes();
					boolean found = false;
					for(int i = 0; i < displayModes.length && !found; i ++) {
						DisplayMode mode = displayModes[i];
						if(mode.getWidth() == graphicsMode.w && mode.getHeight() == graphicsMode.h &&
								mode.getBitsPerPixel() == graphicsMode.d && mode.getFrequency() == graphicsMode.f) {
							System.out.println("Setting mode " + mode.getWidth() + "x" + mode.getHeight() + "x" + mode.getBitsPerPixel() + " " + mode.getFrequency() + "Hz");
							Display.setDisplayMode(mode);
							found = true;
						}
					}

					if(!found) Display.setDisplayMode(Display.getDesktopDisplayMode());
				}

				this.displayWidth = Display.getDisplayMode().getWidth();
				this.displayHeight = Display.getDisplayMode().getHeight();
				if(this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if(this.displayHeight <= 0) {
					this.displayHeight = 1;
				}
			} else {
				// Exit fullscreen — restore windowed dimensions.
				if(this.mcCanvas != null) {
					this.displayWidth = this.mcCanvas.getWidth();
					this.displayHeight = this.mcCanvas.getHeight();
				} else {
					this.displayWidth = this.tempDisplayWidth;
					this.displayHeight = this.tempDisplayHeight;
				}

				if(this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if(this.displayHeight <= 0) {
					this.displayHeight = 1;
				}
			}

			// Notify the current GUI so it can rebuild its scaled resolution.
			if(this.currentScreen != null) {
				this.resize(this.displayWidth, this.displayHeight);
			}

			Display.setFullscreen(this.fullscreen);
			Display.update();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Handles canvas/window resize events by updating the display dimensions and
	 * rebuilding the GUI's scaled resolution so controls continue to render correctly.
	 *
	 * @param w new width in pixels
	 * @param h new height in pixels
	 */
	private void resize(int w, int h) {
		if(w <= 0) {
			w = 1;
		}

		if(h <= 0) {
			h = 1;
		}

		this.displayWidth = w;
		this.displayHeight = h;
		if(this.currentScreen != null) {
			ScaledResolution scaledRes = new ScaledResolution(this.gameSettings, w, h);
			this.currentScreen.setWorldAndResolution(this, scaledRes.getScaledWidth(), scaledRes.getScaledHeight());
		}

	}

	/**
	 * Middle-mouse-button "pick block" action: selects the block under the crosshair
	 * as the current held item, allowing instant access to the block for placement.
	 *
	 * <p>Special cases:</p>
	 * <ul>
	 *   <li>Grass → converts to Dirt (legacy Beta behavior).</li>
	 *   <li>Double slab → picks the single slab variant.</li>
	 *   <li>Bedrock → picks Stone (prevents picking the indestructible block).</li>
	 * </ul>
	 *
	 * <p>Skipped in creative-test controller mode.</p>
	 */
	private void clickMiddleMouseButton() {
		if(this.objectMouseOver != null) {
			int blockId = this.theWorld.getBlockId(this.objectMouseOver.blockX, this.objectMouseOver.blockY, this.objectMouseOver.blockZ);
			if(blockId == Block.grass.blockID) {
				blockId = Block.dirt.blockID;
			}

			if(blockId == Block.stairDouble.blockID) {
				blockId = Block.stairSingle.blockID;
			}

			if(blockId == Block.bedrock.blockID) {
				blockId = Block.stone.blockID;
			}

			this.thePlayer.inventory.setCurrentItem(blockId, this.playerController instanceof PlayerControllerTest);
		}

	}

	/*
	private void startCheckingForTheCheque() {
		(new ThreadCheckHasPaid(this)).start();
	}
	*/

	/**
	 * Runs a single game tick: input polling, controller updates, world ticking,
	 * particle updates, etc.
	 *
	 * <p>Major phases:</p>
	 * <ol>
	 *   <li>Update HUD particles and stat counters.</li>
	 *   <li>Update player mouse-over (raycast) for the current frame.</li>
	 *   <li>Advance the player controller if the game is not paused.</li>
	 *   <li>Rebind the terrain texture and update animated textures.</li>
	 *   <li>Auto-switch GUI screens on death/awake transitions.</li>
	 *   <li>Drain mouse and keyboard event queues, dispatching to GUI/in-game handlers.</li>
	 *   <li>Update the world (entities, lightning, weather, particles) when not paused.</li>
	 * </ol>
	 *
	 * <p>The input handling uses nested {@code do/while} loops with {@code label301}
	 * to break out when both the mouse and keyboard queues are exhausted.</p>
	 */
	public void runTick() {
		this.statFileWriter.func_27178_d();
		this.ingameGUI.updateTick();
		// Recompute the world raycast under the crosshair (1.0F reach).
		this.entityRenderer.getMouseOver(1.0F);
		int wheelDelta;

		// Advance the player controller (input -> player motion).
		if(!this.isGamePaused && this.theWorld != null) {
			this.playerController.updateController();
		}

		// Make sure the terrain atlas is bound for upcoming texture lookups.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderEngine.getTexture("/terrain.png"));
		if(!this.isGamePaused) {
			// Tick animated water/lava/portal/compass textures.
			this.renderEngine.updateDynamicTextures();
		}

		// ---------------------------------------------------------------------
		// Auto screen transitions based on player state
		// ---------------------------------------------------------------------
		if(this.currentScreen == null && this.thePlayer != null) {
			if(this.thePlayer.health <= 0) {
				// Player died — display the game-over screen (defaulted by displayGuiScreen).
				this.displayGuiScreen((GuiScreen)null);
			} else if(this.thePlayer.isPlayerSleeping() && this.theWorld != null && this.theWorld.isRemote) {
				// Player went to bed on a multiplayer server — show the wake-up screen.
				this.displayGuiScreen(new GuiSleepMP(this));
			}
		} else if(this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.thePlayer.isPlayerSleeping()) {
			// Player woke up — close the sleep screen.
			this.displayGuiScreen((GuiScreen)null);
		}

		// While a GUI is up, suppress in-game click auto-repeat to avoid
		// "clicking through" when the GUI closes.
		if(this.currentScreen != null) {
			this.leftClickCounter = 10000;
			this.mouseTicksRan = this.ticksRan + 10000;
		}

		// Dispatch GUI tick logic (animations, button hover states, particles).
		if(this.currentScreen != null) {
			this.currentScreen.handleInput();
			if(this.currentScreen != null) {
				this.currentScreen.guiParticles.update();
				this.currentScreen.updateScreen();
			}
		}

		// ---------------------------------------------------------------------
		// Input handling: process mouse + keyboard event queues.
		// Only when the current screen allows user input (or no screen is open).
		// ---------------------------------------------------------------------
		if(this.currentScreen == null || this.currentScreen.allowUserInput) {
			label301:
			while(true) {
				while(true) {
					while(true) {
						long tickDelta;
					do {
							// ==================================================================
							// Drain mouse events
							// ==================================================================
							if(!Mouse.next()) {
								// Mouse queue drained: tick down the click throttle.
								if(this.leftClickCounter > 0) {
									--this.leftClickCounter;
								}

								while(true) {
									while(true) {

										do {
											// ==========================================================
											// Drain keyboard events
											// ==========================================================
											if(!Keyboard.next()) {
												// ======================================================
												// No more events: handle held-key behaviors
												// ======================================================
												// Beta-era fix: stop using item when right mouse is released.
												if(this.thePlayer.isUsingItem()) {
													if(!Mouse.isButtonDown(1)) {
														this.playerController.onStoppedUsingItem(this.thePlayer);
													}
												} else {
													// Auto-click while the user holds the mouse button.
													if(this.currentScreen == null) {
														if(Mouse.isButtonDown(0) && (float)(this.ticksRan - this.mouseTicksRan) >= this.timer.ticksPerSecond / 4.0F && this.inGameHasFocus) {
															this.clickMouse(0);
															this.mouseTicksRan = this.ticksRan;
														}

														if(Mouse.isButtonDown(1) && (float)(this.ticksRan - this.mouseTicksRan) >= this.timer.ticksPerSecond / 4.0F && this.inGameHasFocus) {
															this.clickMouse(1);
															this.mouseTicksRan = this.ticksRan;
														}
													}
												}

												// Notify the controller of the held-mouse state for block breaking.
												this.sendClickBlockToController(0, this.currentScreen == null && Mouse.isButtonDown(0) && this.inGameHasFocus);
												break label301; // done with both event queues
											}

											// Forward key events to the player for movement/key bindings.
											this.thePlayer.handleKeyPress(Keyboard.getEventKey(), Keyboard.getEventKeyState());
										} while(!Keyboard.getEventKeyState()); // only react to key-down

										// ======================================================
										// Keyboard event dispatch
										// ======================================================
										if(Keyboard.getEventKey() == Keyboard.KEY_F11) {
											// F11 toggles fullscreen.
											this.toggleFullscreen();
										} else {
											if(this.currentScreen != null) {
												// Forward keys to the GUI.
												this.currentScreen.handleKeyboardInput();
											} else {
												// In-game key bindings.
												if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
													this.displayInGameMenu();
												}

												// F3+S: force a resource reload.
												if(Keyboard.getEventKey() == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
													this.forceReload();
												}

												// F1: hide GUI.
												if(Keyboard.getEventKey() == Keyboard.KEY_F1) {
													GameSettingsValues.hideGUI = !GameSettingsValues.hideGUI;
												}

												// F3: toggle debug overlay.
												if(Keyboard.getEventKey() == Keyboard.KEY_F3) {
													GameSettingsValues.showDebugInfo = !GameSettingsValues.showDebugInfo;
												}

												// F5: cycle through camera views (front/back).
												if(Keyboard.getEventKey() == Keyboard.KEY_F5) {
													GameSettingsValues.thirdPersonView = !GameSettingsValues.thirdPersonView;
												}

												// F8: smooth camera toggle.
												if(Keyboard.getEventKey() == Keyboard.KEY_F8) {
													GameSettingsValues.smoothCamera = !GameSettingsValues.smoothCamera;
												}

												// Inventory key: opens creative or survival inventory.
												if(Keyboard.getEventKey() == GameSettingsKeys.keyBindInventory.keyCode) {
													if (this.thePlayer.isCreative) {
														this.displayGuiScreen(new GuiContainerCreative(this.thePlayer));
													} else {
														this.displayGuiScreen(new GuiInventory(this.thePlayer));
													}
												}

												// Drop key: drops the currently selected item.
												if(Keyboard.getEventKey() == GameSettingsKeys.keyBindDrop.keyCode) {
													this.thePlayer.dropCurrentItem();
												}

												// Creative inventory picker (creative mode only).
												if(Keyboard.getEventKey() == GameSettingsKeys.keyBindCreative.keyCode && this.thePlayer.isCreative) {
													this.displayGuiScreen(new GuiCreativeInventory(this));
												}

												// Chat key: multiplayer or singleplayer with cheats enabled.
												if((this.isRemote() || this.thePlayer.enableCheats) && Keyboard.getEventKey() == GameSettingsKeys.keyBindChat.keyCode) {
													this.displayGuiScreen(new GuiChat(this));
												}
											}

											// Hotbar slot selection: keys 1..9 pick slot 0..8.
											for(int slotIndex = 0; slotIndex < 9; ++slotIndex) {
												if(Keyboard.getEventKey() == Keyboard.KEY_1 + slotIndex) {
													this.thePlayer.inventory.currentItem = slotIndex;
												}
											}

											// Render-distance hotkey (shift = decrease, no-shift = increase).
											if(Keyboard.getEventKey() == GameSettingsKeys.keyBindToggleFog.keyCode) {
												this.gameSettings.setOptionValue(EnumOptions.RENDER_DISTANCE, !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 1 : -1);
											}
										}
									}
								}
								}

							// ==================================================================
							// Detect unusually long frame intervals (used to throttle mouse polling).
							// ==================================================================
							tickDelta = System.currentTimeMillis() - this.systemTime;
						} while(tickDelta > 200L);

						// ==================================================================
						// Mouse wheel: hotbar scrolling + noclip speed (creative cheat).
						// ==================================================================
						wheelDelta = Mouse.getEventDWheel();
						if(wheelDelta != 0) {
							this.thePlayer.inventory.changeCurrentItem(wheelDelta);
							if(GameSettingsValues.noclip) {
								if(wheelDelta > 0) {
									wheelDelta = 1;
							}

								if(wheelDelta < 0) {
									wheelDelta = -1;
							}

								// Adjust noclip movement speed in 0.25-unit increments.
								GameSettingsValues.noclipRate += (float)wheelDelta * 0.25F;
						}
						}

						// ==================================================================
						// Mouse click events (left/right/middle)
						// ==================================================================
						if(this.currentScreen == null) {
							if(!this.inGameHasFocus && Mouse.getEventButtonState()) {
								// No focus + any button click: return focus to the game.
								this.setIngameFocus();
						} else {
								// Forward clicks to the world/interaction handlers.
								if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
									this.clickMouse(0);
									this.mouseTicksRan = this.ticksRan;
							}

								if(Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
									this.clickMouse(1);
									this.mouseTicksRan = this.ticksRan;
							}

								if(Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
									this.clickMiddleMouseButton();
							}
						}
						} else if(this.currentScreen != null) {
							// Forward mouse events to the GUI when one is open.
							this.currentScreen.handleMouseInput();
						}
					}
				}
			}
		}

		// ---------------------------------------------------------------------
		// World ticking
		// ---------------------------------------------------------------------
		if(this.theWorld != null) {
			if(this.thePlayer != null) {
				// Periodically join entities in the surrounding chunks to the client.
				++this.joinPlayerCounter;
				if(this.joinPlayerCounter == 30) {
					this.joinPlayerCounter = 0;
					this.theWorld.joinEntityInSurroundings(this.thePlayer);
				}
			}

			// Apply user-selected difficulty; force peaceful=0 on remote worlds.
			this.theWorld.difficultySetting = GameSettingsValues.difficulty;
			if(this.theWorld.isRemote) {
				this.theWorld.difficultySetting = 3;
			}

			this.theWorld.colouredAthmospherics = GameSettingsValues.colouredAthmospherics;

			if(!this.isGamePaused) {
				// Update the renderer (sky/lighting/clouds/hand bobbing).
				this.entityRenderer.updateRenderer();
			}

			if(!this.isGamePaused) {
				// Tick cloud geometry.
				this.renderGlobal.updateClouds();
			}

			if(!this.isGamePaused) {
				// Tick the lightning flash timer down.
				if(this.theWorld.lightningFlash > 0) {
					--this.theWorld.lightningFlash;
				}

				// Tick all entities (movement, AI, physics).
				this.theWorld.updateEntities();
			}

			// World tick (scheduled updates, weather, block ticks, etc.)
			// Runs even when paused in singleplayer — but not when paused in multiplayer
			// (to keep server state stable).
			if(!this.isGamePaused || this.isRemote()) {
				this.theWorld.setAllowedMobSpawns(GameSettingsValues.difficulty > 0, true);
				this.theWorld.tick();
			}

			// Random ambient block updates (smoke, fire, etc.) near the player.
			if(!this.isGamePaused && this.theWorld != null) {
				this.theWorld.randomDisplayUpdates(MathHelper.floor_double(this.thePlayer.posX), MathHelper.floor_double(this.thePlayer.posY), MathHelper.floor_double(this.thePlayer.posZ));
			}

			// Particle updates (splash, smoke, etc.).
			if(!this.isGamePaused) {
				this.effectRenderer.updateEffects();
			}
		}

		// Record the end-of-tick timestamp.
		this.systemTime = System.currentTimeMillis();
	}

	/**
	 * Forces a reload of the sound system and remote resources.
	 * Triggered by the F3+S keybind.
	 */
	private void forceReload() {
		System.out.println("FORCING RELOAD!");
		this.sndManager = new SoundManager();
		this.sndManager.loadSoundSettings(this.gameSettings);
		this.downloadResourcesThread.reloadResources();
	}

	/**
	 * Returns true if the current world is a multiplayer world.
	 *
	 * @return true when {@link #theWorld} is non-null and {@link World#isRemote} is true
	 */
	public boolean isRemote() {
		return this.theWorld != null && this.theWorld.isRemote;
	}

	/**
	 * Loads a saved world or generates a new one, performing format conversion if necessary,
	 * then transitions the client to the new world.
	 *
	 * @param saveName     the internal save name (directory name under {@code saves/})
	 * @param folderName   the display name shown in the level info GUI
	 * @param worldSettings the {@link WorldSettings} describing game mode, seed, etc.
	 */
	public void startWorld(String saveName, String folderName, WorldSettings worldSettings) {
		// Tear down any existing world first.
		this.clearWorld((World)null);
		System.gc();

		if(this.saveLoader.isOldMapFormat(saveName)) {
			// Old Alpha-format world detected — migrate to the newer format first.
			this.convertMapToMCRegion(saveName, folderName);
		} else {
			ISaveHandler saveHandler = this.saveLoader.getSaveLoader(saveName, false);
			World newWorld = null;
			newWorld = new World(saveHandler, folderName, worldSettings);
			if(newWorld.isNewWorld) {
				// Newly generated world: record generation achievement.
				this.statFileWriter.readStat(StatList.createWorldStat, 1);
				this.statFileWriter.readStat(StatList.startGameStat, 1);
				this.transitionToWorld(newWorld, "Generating level");
			} else {
				// Loaded existing world: record load achievement.
				this.statFileWriter.readStat(StatList.loadWorldStat, 1);
				this.statFileWriter.readStat(StatList.startGameStat, 1);
				this.transitionToWorld(newWorld, "Loading level");
			}
		}

	}

	/**
	 * Teleports the player between dimensions (overworld ↔ nether).
	 *
	 * <p>Uses the coordinate scaling factor of 8:1 between the overworld and the nether,
	 * as per Minecraft's portal mechanics. After the teleport, the player is placed in
	 * a new {@link World} instance backed by the appropriate {@link WorldProvider}.</p>
	 *
	 * <p>The sequence is:</p>
	 * <ol>
	 *   <li>Toggle the player's dimension.</li>
	 *   <li>Remove the player from the old world.</li>
	 *   <li>Scale X/Z coordinates by 1/8 (to nether) or ×8 (from nether).</li>
	 *   <li>Create the destination world.</li>
	 *   <li>Swap worlds via {@link #changeWorld}.</li>
	 *   <li>Use {@link Teleporter} to find/set the exit portal location.</li>
	 * </ol>
	 */
	public void usePortal() {
		System.out.println("Toggling dimension!!");
		if(this.thePlayer.dimension == -1) {
			// Currently in the nether → enter the overworld.
			this.thePlayer.dimension = 0;
		} else {
			// Currently in the overworld → enter the nether.
			this.thePlayer.dimension = -1;
		}

		// Detach the player from the old world (but keep it alive).
		this.theWorld.setEntityDead(this.thePlayer);
		this.thePlayer.isDead = false;
		double portalX = this.thePlayer.posX;
		double portalZ = this.thePlayer.posZ;
		double portalScale = 8.0D;
		World teleportWorld;
		if(this.thePlayer.dimension == -1) {
			// Leaving overworld for the nether: scale coordinates down by 8.
			portalX /= portalScale;
			portalZ /= portalScale;
			this.thePlayer.setLocationAndAngles(portalX, this.thePlayer.posY, portalZ, this.thePlayer.rotationYaw, this.thePlayer.rotationPitch);
			if(this.thePlayer.isEntityAlive()) {
				this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
			}

			teleportWorld = null;
			teleportWorld = new World(this.theWorld, WorldProvider.getProviderForDimension(-1));
			this.changeWorld(teleportWorld, "Entering the Nether", this.thePlayer);
		} else {
			// Leaving the nether for the overworld: scale coordinates up by 8.
			portalX *= portalScale;
			portalZ *= portalScale;
			this.thePlayer.setLocationAndAngles(portalX, this.thePlayer.posY, portalZ, this.thePlayer.rotationYaw, this.thePlayer.rotationPitch);
			if(this.thePlayer.isEntityAlive()) {
				this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
			}

			teleportWorld = null;
			teleportWorld = new World(this.theWorld, WorldProvider.getProviderForDimension(0));
			this.changeWorld(teleportWorld, "Leaving the Nether", this.thePlayer);
		}

		// Update the player's worldObj reference to match the new world.
		this.thePlayer.worldObj = this.theWorld;
		if(this.thePlayer.isEntityAlive()) {
			this.thePlayer.setLocationAndAngles(portalX, this.thePlayer.posY, portalZ, this.thePlayer.rotationYaw, this.thePlayer.rotationPitch);
			this.theWorld.updateEntityWithOptionalForce(this.thePlayer, false);
			// Find the nearest portal or carve a new one.
			(new Teleporter()).setExitLocation(this.theWorld, this.thePlayer);
		}

	}

	/**
	 * Releases the current world and prepares for a null world (main menu state).
	 *
	 * @param w ignored — always null in this path; see {@link #changeWorld}
	 */
	public void clearWorld(World w) {
		this.transitionToWorld(w, "");
	}

	/**
	 * Intermediate world-change helper that delegates to {@link #changeWorld}.
	 *
	 * @param w   the new world, or {@code null} to return to the main menu
	 * @param msg the loading message to display on the progress screen
	 */
	public void transitionToWorld(World w, String msg) {
		this.changeWorld(w, msg, (EntityPlayer)null);
	}

	/**
	 * Transitions from one world to another (or to no world at all).
	 *
	 * <p>This is the central world-switch routine. It:</p>
	 * <ol>
	 *   <li>Flushes and saves the old world if present.</li>
	 *   <li>Swaps the {@link #theWorld} reference.</li>
	 *   <li>Notifies the player controller and renders global renderer.</li>
	 *   <li>Respawns or reuses the local player entity.</li>
	 *   <li>Rebuilds the chunk loading queue around the player.</li>
	 *   <li>Triggers world saving for newly generated worlds.</li>
	 * </ol>
	 *
	 * @param w      the new world, or {@code null} to go to the main menu
	 * @param msg    loading-screen message
	 * @param player if non-null, this player entity is used instead of the default local player
	 */
	public void changeWorld(World w, String msg, EntityPlayer player) {
		// Flush achievement queue.
		this.statFileWriter.func_27175_b();
		this.statFileWriter.syncStats();
		// Detach camera from the old player while we rebuild state.
		this.renderViewEntity = null;
		this.loadingScreen.printText(msg);
		this.loadingScreen.displayLoadingString("");
		// Stop all music.
		this.sndManager.playStreaming((String)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

		// -----------------------------------------------------------------
		// Save the old world before swapping
		// -----------------------------------------------------------------
		if(this.theWorld != null) {
			this.theWorld.saveWorldIndirectly(this.loadingScreen);
		}

		// -----------------------------------------------------------------
		// Swap worlds
		// -----------------------------------------------------------------
		this.theWorld = w;
		if(w != null) {
			// Notify the player controller of the world change
			// (updates chunk loading, block reaching distance, etc.).
			this.playerController.onWorldChange(w);

			// -----------------------------------------------------------------
			// Player entity setup
			// -----------------------------------------------------------------
			if(!this.isRemote()) {
				// Single-player: retrieve or create the singleplayer player.
				if(player == null) {
					this.thePlayer = (EntityPlayerSP)w.getEntityByClass(EntityPlayerSP.class);
				}
			} else if(this.thePlayer != null) {
				// Multiplayer: detach and re-spawn the player in the new world.
				this.thePlayer.preparePlayerToSpawn();
				if(w != null) {
					w.spawnEntityInWorld(this.thePlayer);
				}
			}

			// Preload terrain before allowing the player to move.
			if(!w.isRemote) {
				this.preloadWorld(msg);
			}

			// If no player was provided (single-player or first world load),
			// create a new one using the controller.
			if(this.thePlayer == null) {
				this.thePlayer = (EntityPlayerSP)this.playerController.createPlayer(w);
				this.thePlayer.preparePlayerToSpawn();
				this.thePlayer.isCreative = GameSettingsValues.isCreative;
				this.thePlayer.enableCheats = GameSettingsValues.enableCheats;
				this.thePlayer.deadManChest = GameSettingsValues.deadManChest;
				this.thePlayer.enableCraftingGuide = GameSettingsValues.craftGuide;
				// Orient the player to face the correct direction in the world.
				this.playerController.flipPlayer(this.thePlayer);
			}

			// Rebind movement input to the new world settings.
			this.thePlayer.movementInput = new MovementInputFromOptions();

			// -----------------------------------------------------------------
			// Renderer updates
			// -----------------------------------------------------------------
			if(this.renderGlobal != null) {
				this.renderGlobal.changeWorld(w);
			}

			if(this.effectRenderer != null) {
				this.effectRenderer.clearEffects(w);
			}

			// Notify the controller of the final player state.
			this.playerController.func_6473_b(this.thePlayer);
			if(player != null) {
				w.emptyMethod1();
			}

			// -----------------------------------------------------------------
			// Chunk loading focus
			// -----------------------------------------------------------------
			// Point the chunk loading center at the player's current position
			// so new chunks load in the right direction.
			IChunkProvider chunkProvider = w.getIChunkProvider();
			if(chunkProvider instanceof ChunkProviderLoadOrGenerate) {
				ChunkProviderLoadOrGenerate chunkProviderGen = (ChunkProviderLoadOrGenerate)chunkProvider;
				int playerChunkX = MathHelper.floor_float((float)((int)this.thePlayer.posX)) >> 4;
				int playerChunkZ = MathHelper.floor_float((float)((int)this.thePlayer.posZ)) >> 4;
				chunkProviderGen.setCurrentChunkOver(playerChunkX, playerChunkZ);
			}

			// Spawn the player into the world (loads surrounding chunks around spawn).
			w.spawnPlayerWithLoadedChunks(this.thePlayer);

			// Save newly generated worlds immediately so progress is not lost.
			if(w.isNewWorld) {
				w.saveWorldIndirectly(this.loadingScreen);
			}

			this.renderViewEntity = this.thePlayer;
		} else {
			// No world: the player no longer exists in any world.
			this.thePlayer = null;
		}

		System.gc();
		this.systemTime = 0L;
	}

	/**
	 * Converts an old-format world (pre-Region) to the current format,
	 * then loads the converted world.
	 *
	 * @param saveName   the internal save name
	 * @param folderName the display name
	 */
	private void convertMapToMCRegion(String saveName, String folderName) {
		this.loadingScreen.printText("Converting World to " + this.saveLoader.getFormatName());
		this.loadingScreen.displayLoadingString("This may take a while :)");
		this.saveLoader.converMapToMCRegion(saveName, this.loadingScreen);
		this.startWorld(saveName, folderName, new WorldSettings(0L, 0, true, false, true, 0.0F, WorldType.DEFAULT));
	}

	/**
	 * Preloads terrain around the spawn point while showing a loading progress bar.
	 *
	 * <p>Iterates over chunks in a 128-block radius (centered on the player's position
	 * or the world's spawn point) and touches each chunk to force generation. Then runs
	 * {@link World#dropOldChunks} to evict stale chunks from memory.</p>
	 *
	 * @param msg the message displayed on the loading screen during preloading
	 */
	private void preloadWorld(String msg) {
		this.loadingScreen.printText(msg);
		this.loadingScreen.displayLoadingString("Building terrain");
		short radius = 128;
		int progress = 0;
		int totalLoads = radius * 2 / 16 + 1;
		totalLoads *= totalLoads;
		IChunkProvider chunkProv = this.theWorld.getIChunkProvider();
		ChunkCoordinates spawn = this.theWorld.getSpawnPoint();
		if(this.thePlayer != null) {
			// Center chunk loading on the player instead of world spawn.
			spawn.posX = (int)this.thePlayer.posX;
			spawn.posZ = (int)this.thePlayer.posZ;
		}

		if(chunkProv instanceof ChunkProviderLoadOrGenerate) {
			ChunkProviderLoadOrGenerate cplg = (ChunkProviderLoadOrGenerate)chunkProv;
			cplg.setCurrentChunkOver(spawn.posX >> 4, spawn.posZ >> 4);
		}

		// Touch each chunk in the radius to trigger lazy generation.
		for(int x = -radius; x <= radius; x += 16) {
			for(int z = -radius; z <= radius; z += 16) {
				this.loadingScreen.setLoadingProgress(progress++ * 100 / totalLoads);
				this.theWorld.getBlockId(spawn.posX + x, 64, spawn.posZ + z);
			}
		}

		this.loadingScreen.displayLoadingString("Simulating world for a bit");
		this.theWorld.dropOldChunks();
	}

	/**
	 * Installs a resource file downloaded from the resource server or bundled in the jar.
	 *
	 * <p>Paths are prefixed with a type identifier:</p>
	 * <ul>
	 *   <li>{@code sound/} → registered as a sound effect with the {@link SoundManager}</li>
	 *   <li>{@code newsound/} → same as {@code sound/}</li>
	 *   <li>{@code streaming/} → registered as a streaming (OGG Vorbis) track</li>
	 *   <li>{@code music/} → registered as background music</li>
	 *   <li>{@code newmusic/} → same as {@code music/}</li>
	 * </ul>
	 *
	 * @param path    the resource path with type prefix, e.g. "sound/mob/zombiepig/zpig1.ogg"
	 * @param resFile the downloaded/extracted file to register
	 */
	public void installResource(String path, File resFile) {
		int slashIdx = path.indexOf("/");
		String type = path.substring(0, slashIdx);
		path = path.substring(slashIdx + 1);
		if(type.equalsIgnoreCase("sound")) {
			this.sndManager.addSound(path, resFile);
		} else if(type.equalsIgnoreCase("newsound")) {
			this.sndManager.addSound(path, resFile);
		} else if(type.equalsIgnoreCase("streaming")) {
			this.sndManager.addStreaming(path, resFile);
		} else if(type.equalsIgnoreCase("music")) {
			this.sndManager.addMusic(path, resFile);
		} else if(type.equalsIgnoreCase("newmusic")) {
			this.sndManager.addMusic(path, resFile);
		}

	}

	/**
	 * Installs a sound resource from a remote URL (for streaming from the internet).
	 *
	 * @param name the sound name (without type prefix)
	 * @param url  the URL of the sound file
	 */
	public void installResourceURL(String name, URL url) {
		int slashIndex = name.indexOf("/");
		name = name.substring(slashIndex + 1);
		this.sndManager.addSoundURL(name, url);
	}

	/**
	 * Returns the OpenGL capabilities checker, which caches the results of
	 * {@code glGetString()} extension queries.
	 *
	 * @return the capabilities checker
	 */
	public OpenGlCapsChecker getOpenGlCapsChecker() {
		return this.glCapabilities;
	}

	/**
	 * Returns a debug string describing the current renderer statistics.
	 *
	 * @return e.g. "R: 12, D: 3, E: 0"
	 */
	public String debugInfoRenders() {
		return this.renderGlobal.getDebugInfoRenders();
	}

	/**
	 * Returns a debug string describing the current entity rendering statistics.
	 *
	 * @return e.g. "E: 45"
	 */
	public String getEntityDebug() {
		return this.renderGlobal.getDebugInfoEntities();
	}

	/**
	 * Returns the name of the current world's {@link WorldProvider}.
	 *
	 * @return the provider name, or empty string if no world is loaded
	 */
	public String getWorldProviderName() {
		return this.theWorld.getProviderName();
	}

	/**
	 * Returns a debug string describing particle and entity counts.
	 *
	 * @return e.g. "P: 12. T: 34"
	 */
	public String debugInfoEntities() {
		return "P: " + this.effectRenderer.getStatistics() + ". T: " + this.theWorld.getDebugLoadedEntities();
	}

	/**
	 * Respawns the player after death, preserving creative mode flags and cheats,
	 * restoring the bed spawn point if valid, and restoring the last death location.
	 *
	 * <p>If the current dimension does not support respawning (e.g. the nether in some
	 * configurations), the player is teleported through a portal first.</p>
	 *
	 * @param sameDimensionDeath  true if the death occurred in the same dimension as respawn
	 * @param dimension           the target respawn dimension
	 */
	public void respawn(boolean sameDimensionDeath, int dimension) {
		// Teleport through a portal if the current dimension cannot respawn.
		if(!this.theWorld.isRemote && !this.theWorld.worldProvider.canRespawnHere()) {
			this.usePortal();
		}

		ChunkCoordinates spawnCoordinates = null;
		ChunkCoordinates newSpawnCoordinates = null;
		ChunkCoordinates deathCoordinates = null;

		boolean keepSpawn = true;
		if(this.thePlayer != null) {
			if(!sameDimensionDeath) {
				// Retrieve the player's bed spawn point.
				spawnCoordinates = this.thePlayer.getPlayerSpawnCoordinate();
				if(spawnCoordinates != null) {
					if(this.thePlayer.isDontCheckSpawnCoordinates()) {
						newSpawnCoordinates = spawnCoordinates;
					} else {
						// Verify the bed is not obstructed.
						newSpawnCoordinates = EntityPlayer.verifyRespawnCoordinates(this.theWorld, spawnCoordinates);
						if(newSpawnCoordinates == null) {
							this.thePlayer.addChatMessage("tile.bed.notValid");
						}
					}
				}
			}

			// Store the last death position so the recovery compass can point to it.
			deathCoordinates = this.thePlayer.getPlayerLastDeathCoordinate();
		}

		if(newSpawnCoordinates == null) {
			// No valid bed: fall back to world spawn.
			newSpawnCoordinates = this.theWorld.getSpawnPoint();
			keepSpawn = false;
		}

		// Center the chunk loader on the respawn point.
		IChunkProvider chunkProvider = this.theWorld.getIChunkProvider();
		if(chunkProvider instanceof ChunkProviderLoadOrGenerate) {
			ChunkProviderLoadOrGenerate chunkProviderLoG = (ChunkProviderLoadOrGenerate)chunkProvider;
			chunkProviderLoG.setCurrentChunkOver(newSpawnCoordinates.posX >> 4, newSpawnCoordinates.posZ >> 4);
		}

		this.theWorld.setSpawnLocation();
		this.theWorld.updateEntityList();

		// Preserve the player's entity ID so network state remains consistent.
		int oldEntityId = 0;
		if(this.thePlayer != null) {
			oldEntityId = this.thePlayer.entityId;
			this.theWorld.setEntityDead(this.thePlayer);
		}

		this.renderViewEntity = null;

		// Create the respawned player and copy permissions from the old one.
		EntityPlayer oldPlayer = this.thePlayer;
		this.thePlayer = (EntityPlayerSP)this.playerController.createPlayer(this.theWorld);

		this.thePlayer.isCreative = oldPlayer.isCreative;
		this.thePlayer.enableCheats = oldPlayer.enableCheats;
		this.thePlayer.deadManChest = oldPlayer.deadManChest;
		this.thePlayer.enableCraftingGuide = oldPlayer.enableCraftingGuide;

		this.thePlayer.dimension = dimension;
		this.renderViewEntity = this.thePlayer;
		this.thePlayer.preparePlayerToSpawn();
		if(keepSpawn) {
			// Restore the bed spawn point and teleport to it.
			this.thePlayer.setPlayerSpawnCoordinate(spawnCoordinates);
			this.thePlayer.setLocationAndAngles((double)((float)newSpawnCoordinates.posX + 0.5F), (double)((float)newSpawnCoordinates.posY + 0.1F), (double)((float)newSpawnCoordinates.posZ + 0.5F), 0.0F, 0.0F);
		}

		// Store the death coordinates for the recovery compass.
		this.thePlayer.setPlayerLastDeathCoordinate(deathCoordinates);

		this.playerController.flipPlayer(this.thePlayer);
		this.theWorld.spawnPlayerWithLoadedChunks(this.thePlayer);
		this.thePlayer.movementInput = new MovementInputFromOptions();
		this.thePlayer.entityId = oldEntityId;
		this.thePlayer.func_6420_o();
		this.playerController.func_6473_b(this.thePlayer);
		this.preloadWorld("Respawning");

		// Close the game-over screen automatically.
		if(this.currentScreen instanceof GuiGameOver) {
			this.displayGuiScreen((GuiScreen)null);
		}

	}

	/**
	 * Entry point for launching the game as a standalone application with a
	 * default username and no server.
	 *
	 * @param user    the player username
	 * @param session the session token
	 */
	public static void startMainThread(String user, String session) {
		startMainThread(user, session, (String)null);
	}

	/**
	 * Creates the game window, sets up the LWJGL display, and starts the game
	 * on a new high-priority thread.
	 *
	 * @param user        the player username
	 * @param session     the session token (may be "-" for offline mode)
	 * @param serverAddr  optional server address in {@code host:port} format, or {@code null}
	 */
	public static void startMainThread(String user, String session, String serverAddr) {
		boolean isFullscreen = false;
		Frame frame = new Frame("Minecraft");
		Canvas canvas = new Canvas();
		frame.setLayout(new BorderLayout());
		frame.add(canvas, "Center");
		canvas.setPreferredSize(new Dimension(854, 480));
		frame.pack();
		frame.setLocationRelativeTo((Component)null);

		// Create the Minecraft client instance with the canvas.
		MinecraftImpl minecraft = new MinecraftImpl(frame, canvas, (MinecraftApplet)null, 854, 480, isFullscreen, frame);

		// Name the thread and set it to high priority for smoother input.
		Thread mainThread = new Thread(minecraft, "Minecraft main thread");
		mainThread.setPriority(10);
		minecraft.minecraftUri = "www.minecraft.net";

		// Set up the session (username + token for online play).
		if(user != null && session != null) {
			minecraft.session = new Session(user, session);
		} else {
			minecraft.session = new Session("Player" + System.currentTimeMillis() % 1000L, "");
		}

		// Optional auto-connect to a multiplayer server.
		if(serverAddr != null) {
			String[] parts = serverAddr.split(":");
			minecraft.setServer(parts[0], Integer.parseInt(parts[1]));
		}

		frame.setVisible(true);
		frame.addWindowListener(new GameWindowListener(minecraft, mainThread));
		mainThread.start();
	}

	/**
	 * Returns the network send queue for the local player (multiplayer only).
	 * Used by the {@link EntityRenderer} to interpolate player movement.
	 *
	 * @return the player's client packet handler, or {@code null} in single-player
	 */
	public NetClientHandler getSendQueue() {
		return this.thePlayer instanceof EntityClientPlayerMP ? ((EntityClientPlayerMP)this.thePlayer).sendQueue : null;
	}

	/**
	 * CLI entry point: creates a default session and starts the main thread.
	 *
	 * @param args command-line arguments; {@code args[0]} = username, {@code args[1]} = session token
	 */
	public static void main(String[] args) {
		String user = null;
		String sess = null;
		user = "Player" + System.currentTimeMillis() % 1000L;
		if(args.length > 0) {
			user = args[0];
		}

		sess = "-";
		if(args.length > 1) {
			sess = args[1];
		}

		startMainThread(user, sess);
	}

	/**
	 * Returns true if the HUD (hotbar, health, chat) should be rendered.
	 *
	 * @return true when the game is running and {@link GameSettingsValues#hideGUI} is false
	 */
	public static boolean isGuiEnabled() {
		return theMinecraft == null || !GameSettingsValues.hideGUI;
	}

	/**
	 * Returns true if fancy (transparent) grass rendering is enabled.
	 *
	 * @return true when the game is running and {@link GameSettingsValues#fancyGraphics} is true
	 */
	public static boolean isFancyGraphicsEnabled() {
		return theMinecraft != null && GameSettingsValues.fancyGraphics;
	}

	/**
	 * Returns true if ambient occlusion is enabled.
	 *
	 * @return true when the game is running and {@link GameSettingsValues#ambientOcclusion} is true
	 */
	public static boolean isAmbientOcclusionEnabled() {
		return theMinecraft != null && GameSettingsValues.ambientOcclusion;
	}

	/**
	 * Returns true if the F3 debug overlay is enabled.
	 *
	 * @return true when the game is running and {@link GameSettingsValues#showDebugInfo} is true
	 */
	public static boolean isDebugInfoEnabled() {
		return theMinecraft != null && GameSettingsValues.showDebugInfo;
	}

	/**
	 * Checks whether the given chat string is a command (starts with '/').
	 * Currently a stub that always returns false — commands are handled by the
	 * {@link CommandProcessor}.
	 *
	 * @param cmd the chat string to check
	 * @return false (stub)
	 */
	public boolean lineIsCommand(String cmd) {
		if(cmd.startsWith("/")) {
			;
		}

		return false;
	}

}
