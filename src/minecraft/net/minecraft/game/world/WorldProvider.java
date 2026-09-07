package net.minecraft.game.world;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.Seasons;

/**
 * Abstract base class for world dimension providers.
 *
 * <p>Each dimension (overworld, nether, sky) has a {@link WorldProvider} that
 * controls:</p>
 * <ul>
 *   <li>Which {@link IChunkProvider} generates terrain for this dimension</li>
 *   <li>Which {@link WorldChunkManager} assigns biomes</li>
 *   <li>Sky angle, fog colour, and sunrise/sunset colour gradients</li>
 *   <li>Cloud height</li>
 *   <li>Respawnability</li>
 *   <li>Spawn-location validation</li>
 * </ul>
 *
 * <p>Set on a {@link World} via {@link #registerWorld(World)}.</p>
 *
 * @see WorldProviderSurface
 * @see WorldProviderHell
 * @see WorldProviderSky
 */
public abstract class WorldProvider {
    public World worldObj;
    public WorldChunkManager worldChunkMgr;
    public boolean isNether = false;
    public boolean isHellWorld = false;
    public boolean hasNoSky = false;

    /**
     * Lookup table from sky-light level [0–15] to a brightness scalar in [0, 1].
     * Computed by {@link #generateLightBrightnessTable()}.
     */
    public float[] lightBrightnessTable = new float[16];

    /** Cached world-type index for the GUI / terrain generator. */
    public int worldType = 0;

    private float[] colorsSunriseSunset = new float[4];

    /**
     * Initialises this provider with a world reference. Called by
     * {@link World#World(WorldInfo)} before any other method.
     */
    public final void registerWorld(World world) {
        this.worldObj = world;
        this.registerWorldChunkManager();
        this.generateLightBrightnessTable();
    }

    /**
     * Builds the {@link #lightBrightnessTable} using a smooth decay curve:
     * {@code brightness[i] = (1 - i/15) / (i/15 * 3 + 1) * 0.95 + 0.05}.
     * This gives approximately linear visual brightness across the 16 light levels.
     */
    protected void generateLightBrightnessTable() {
        float minBrightness = 0.05F;
        for (int level = 0; level <= 15; ++level) {
            float darkness = 1.0F - (float) level / 15.0F;
            this.lightBrightnessTable[level] =
                    (darkness) / (darkness * 3.0F + 1.0F) * (1.0F - minBrightness) + minBrightness;
        }
    }

    /** Subclasses override to set the correct {@link WorldChunkManager}. */
    protected void registerWorldChunkManager() {
        this.worldChunkMgr = new WorldChunkManager(this.worldObj);
    }

    /** @return the terrain chunk generator for this dimension. */
    public IChunkProvider getChunkProvider() {
        return this.worldObj.worldInfo.getTerrainType().getChunkGenerator(worldObj);
    }

    /**
     * Checks whether a surface block at the given coordinate is a valid
     * player spawn surface.
     *
     * @param x block X
     * @param z block Z
     * @return true if this block type is acceptable for spawning
     */
    public boolean canCoordinateBeSpawn(int x, int z) {
        int topId = this.worldObj.getFirstUncoveredBlock(x, z);
        return topId == Block.sand.blockID;
    }

    /**
     * Computes the sun/moon angle for the current world time, used to rotate
     * the sky and drive the day/night cycle.
     *
     * @param worldTime         total elapsed game ticks
     * @param renderPartialTick partial tick for smooth animation
     * @return angle in [0, 1) (wraps once per full day)
     */
    public float calculateCelestialAngle(long worldTime, float renderPartialTick) {
        float tickWithinCycle = (int) (worldTime % 24000L) + renderPartialTick;

        boolean isDay = tickWithinCycle < Seasons.dayLengthTicks;

        float partProgress = isDay
                ? tickWithinCycle / (float) Seasons.dayLengthTicks
                : (tickWithinCycle - Seasons.dayLengthTicks) / (float) Seasons.nightLengthTicks;

        float dayProgress = isDay
                ? partProgress / 2.0F
                : 0.5F + partProgress / 2.0F;

        dayProgress -= 0.25F;
        if (dayProgress < 0.0F) dayProgress++;
        if (dayProgress > 1.0F) dayProgress--;

        float prevProgress = dayProgress;
        dayProgress = 1.0F - (float) ((Math.cos(dayProgress * Math.PI) + 1.0D) / 2.0D);
        dayProgress = prevProgress + (dayProgress - prevProgress) / 3.0F;

        return dayProgress;
    }

    /**
     * Computes sunrise/sunset colour gradient for the sky renderer.
     *
     * @param celestialAngle the current celestial angle
     * @param partialTick    unused (historical parameter)
     * @return RGBA colour values or null if not currently in sunrise/sunset window
     */
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTick) {
        float window = 0.4F;
        float cosAngle = MathHelper.cos(celestialAngle * (float) Math.PI * 2.0F);
        float threshold = 0.0F;
        if (cosAngle >= threshold - window && cosAngle <= threshold + window) {
            float t = (cosAngle - threshold) / window * 0.5F + 0.5F;
            float bright = 1.0F - (1.0F - MathHelper.sin(t * (float) Math.PI)) * 0.99F;
            bright *= bright;
            this.colorsSunriseSunset[0] = t * 0.3F + 0.7F;
            this.colorsSunriseSunset[1] = t * t * 0.7F + 0.2F;
            this.colorsSunriseSunset[2] = t * t * 0.0F + 0.2F;
            this.colorsSunriseSunset[3] = bright;
            return this.colorsSunriseSunset;
        }
        return null;
    }

    /**
     * Returns the sky/fog colour for rendering.
     *
     * @param celestialAngle        sun angle (0–1)
     * @param partialTick           partial tick
     * @param bloodMoon             true during a blood moon (red tint applied)
     * @param colouredAtmospherics  true to use season-based fog colour
     * @return RGB vector for the fog/sky colour
     */
    public Vec3D getFogColor(float celestialAngle, float partialTick,
            boolean bloodMoon, boolean colouredAtmospherics) {
        float brightness = MathHelper.cos(celestialAngle * (float) Math.PI * 2.0F) * 2.0F + 0.5F;
        if (brightness < 0.0F) brightness = 0.0F;
        if (brightness > 1.0F) brightness = 1.0F;

        float r, g, b;

        if (colouredAtmospherics) {
            int fogColor = Seasons.getFogColorForToday();
            r = (float) (fogColor >> 16 & 255L) / 255.0F;
            g = (float) (fogColor >> 8  & 255L) / 255.0F;
            b = (float) (fogColor & 255L)             / 255.0F;
            r *= brightness * 0.90F + 0.06F;
            g *= brightness * 0.90F + 0.10F;
            b *= brightness * 0.80F + 0.20F;
        } else {
            // Sky blue: 0xC0D8FF → r=192, g=216, b=255
            r = 0.7529412F;
            g = 0.84705883F;
            b = 1.0F;
            r *= brightness * 0.94F + 0.06F;
            g *= brightness * 0.94F + 0.06F;
            b *= brightness * 0.91F + 0.09F;
        }

        if (bloodMoon) {
            r += 0.50F * (1.0F - brightness);
            if (r > 1.0F) r = 1.0F;
        }

        return Vec3D.createVector(r, g, b);
    }

    /** @return true if players can respawn in this dimension. */
    public boolean canRespawnHere() {
        return true;
    }

    /**
     * Returns the {@link WorldProvider} for the given dimension ID.
     *
     * @param dimensionId -1 = Nether, 0 = Overworld, 1 = Sky, else = null
     */
    public static WorldProvider getProviderForDimension(int dimensionId) {
        if (dimensionId == -1) return new WorldProviderHell();
        if (dimensionId == 0)  return new WorldProviderSurface();
        if (dimensionId == 1) return new WorldProviderSky();
        return null;
    }

    /** @return the y-level at which clouds are rendered. */
    public float getCloudHeight() {
        return 108.0F;
    }

    /** @return true if the void should be rendered. */
    public boolean func_28112_c() {
        return false;
    }

    /**
     * Computes the RGBA colour values for the 16×16 lightmap texture.
     * Each entry encodes RGBA as {@code 0xFF << 24 | r << 16 | g << 8 | b}.
     *
     * @param player     the viewing player (for night-vision/water-diving checks)
     * @param gammaSetting the player's gamma setting (brightness preference)
     * @return a 256-element int array, one colour per light-level pair
     */
    public int[] updateLightmap(EntityPlayer player, float gammaSetting) {
        int[] lightmapColors = new int[256];
        World world = this.worldObj;
        float sunBrightness = world.getSunBrightness(1.0F);

        for (int i = 0; i < 256; ++i) {
            float skyBase = sunBrightness * 0.95F + 0.05F;
            float skyBrightness = world.worldProvider.lightBrightnessTable[i / 16] * skyBase;
            float blockBrightness = world.worldProvider.lightBrightnessTable[i % 16];

            if (world.lightningFlash > 0) {
                skyBrightness = world.worldProvider.lightBrightnessTable[i / 16];
            }

            float combined = skyBrightness * (sunBrightness * 0.65F + 0.35F) + blockBrightness;
            combined = combined * 0.96F + 0.03F;

            // Flat world: reduced dynamic range.
            if (world.worldProvider.worldType == 1) {
                combined = 0.22F + blockBrightness * 0.75F;
            }

            // Night vision + water-diving helmet boost.
            if (player.divingHelmetOn() && player.isInsideOfMaterial(Material.water)) {
                float nightVision = 0.5F;
                float inv = 1.0F / combined;
                combined = combined * (1.0F - nightVision) + combined * inv * nightVision;
            }

            if (combined > 1.0F) combined = 1.0F;
            if (combined < 0.0F) combined = 0.0F;

            // Apply gamma / contrast curve.
            float gamma = gammaSetting - 0.55F;
            float invBrightness = 1.0F - combined;
            invBrightness = 1.0F - invBrightness * invBrightness * invBrightness * invBrightness;
            combined = combined * (1.0F - gamma) + invBrightness * gamma;
            //combined = combined * 0.96F + 0.03F;
            combined = combined * 0.90F + 0.09F;
            combined = Math.max(0.0F, Math.min(1.0F, combined));

            int byteVal = (int) (combined * 255.0F);
            lightmapColors[i] = 0xFF000000 | byteVal << 16 | byteVal << 8 | byteVal;
        }

        return lightmapColors;
    }
}
