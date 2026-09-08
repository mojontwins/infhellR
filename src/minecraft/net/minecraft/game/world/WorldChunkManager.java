package net.minecraft.game.world;

import java.util.Random;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.biome.BiomeGenForest;
import net.minecraft.game.world.biome.BiomeGenTaiga;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorOctaves2;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorPerlin;

/**
 * Manages biome assignment and terrain-noise generation for world chunks.
 *
 * <p>Biomes are determined by a two-axis noise map (temperature + humidity)
 * computed using Perlin-noise generators seeded from the world seed. The
 * {@link #loadBlockGeneratorData} method applies scale/exponent curves,
 * clamps values, then looks up the biome via {@link BiomeGenBase#getBiomeFromLookup}.</p>
 *
 * <p>After biome lookup, special biome overrides are applied at large spatial
 * intervals (every 8192 blocks) to inject mushroom-island, mangrove, dark-forest,
 * and flower-field biomes.</p>
 *
 * @see BiomeGenBase
 */
public class WorldChunkManager {
    private NoiseGeneratorOctaves2 tempNoise;
    private NoiseGeneratorOctaves2 humidityNoise;
    private NoiseGeneratorOctaves2 variationNoise;
    private NoiseGeneratorOctaves2 biomeAmplitudeNoise;
    private NoiseGeneratorPerlin cityNoise;

    public double[] temperature;
    public double[] humidity;
    public double[] variation;
    public double[] bigamplitude;

    /** Cached biome array for the last queried region. */
    public BiomeGenBase[] generatedBiomes;

    /** When true, city generation is suppressed for this chunk (e.g. mushroom fields nearby). */
    public boolean invalidateCity;

    /** True if the world is the COLD generator type. */
    public boolean cold;

    /** City noise threshold: chunks with Perlin value > cityChance are urban. */
    public float cityChance = 0.0F;

    protected WorldChunkManager() {
    }

    public WorldChunkManager(World world) {
        long seed = world.getRandomSeed();
        this.tempNoise          = new NoiseGeneratorOctaves2(new Random(seed * 9871L),    4);
        this.humidityNoise      = new NoiseGeneratorOctaves2(new Random(seed * 39811L),   4);
        this.variationNoise     = new NoiseGeneratorOctaves2(new Random(seed * 543321L),  2);
        this.biomeAmplitudeNoise = new NoiseGeneratorOctaves2(new Random(seed * 39811L), 2);
        this.cityNoise          = new NoiseGeneratorPerlin(new Random(seed * 9871L));

        this.cold = world.getWorldInfo().isCold();
        this.cityChance = world.getWorldInfo().getCityChance();
    }

    /** @return the biome at the centre of the given chunk. */
    public BiomeGenBase getBiomeGenAtChunkCoord(ChunkCoordIntPair chunk) {
        return this.getBiomeGenAt(chunk.chunkXPos << 4, chunk.chunkZPos << 4);
    }

    /** @return the biome at the given block coordinate (1×1 sample). */
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        return this.getBiomesForGeneration(x, z, 1, 1)[0];
    }

    /**
     * Computes the raw Perlin temperature value at a block coordinate.
     * No fuzz or clamping is applied.
     */
    public double getTemperature(int x, int z) {
        if (this.tempNoise == null) return 0.0D;
        this.temperature = this.tempNoise.generateNoiseOctaves(
                this.temperature, x, z, 1, 1, 0.025D, 0.025D, 0.5D);
        return this.temperature[0];
    }

    /**
     * Computes the biome temperature and humidity at a single block coordinate,
     * mirroring {@link #loadBlockGeneratorData} exactly (same generators,
     * variation noise, fuzz, squash curve, cold offset and clamps) without
     * touching the shared cached arrays.
     *
     * @return a new two-element array {temperature, humidity}, each in [0, 1]
     */
    public double[] getTemperatureAndHumidityAt(int x, int z) {
        double[] temperatureArray = this.tempNoise.generateNoiseOctaves(
                null, x, z, 1, 1, this.temperatureScale, this.temperatureScale, this.temperatureExponent);
        double[] humidityArray = this.humidityNoise.generateNoiseOctaves(
                null, x, z, 1, 1, this.humidityScale, this.humidityScale, this.humidityExponent);
        double[] variationArray = this.variationNoise.generateNoiseOctaves(
                null, x, z, 1, 1, this.variationScale, this.variationScale, this.variationExponent);

        double v = variationArray[0] * 1.1D + 0.5D;

        // Temperature branch (same fuzz + squash curve as loadBlockGeneratorData).
        double fuzz = this.temperatureFuzzPercent;
        double noFuzz = 1.0D - fuzz;
        double temperature = (temperatureArray[0] * 0.15D + 0.7D) * noFuzz + v * fuzz;
        temperature = 1.0D - (1.0D - temperature) * (1.0D - temperature);
        if (this.cold) temperature -= 0.5D;
        temperature = Math.max(0.0D, Math.min(1.0D, temperature));

        // Humidity branch.
        fuzz = this.humidityFuzzPercent;
        noFuzz = 1.0D - fuzz;
        double humidity = (humidityArray[0] * 0.15D + 0.5D) * noFuzz + v * fuzz;
        humidity = Math.max(0.0D, Math.min(1.0D, humidity));

        return new double[] { temperature, humidity };
    }

    /**
     * Returns the biome array for a terrain-generation pass (coarse grid).
     *
     * @see #loadBlockGeneratorData
     */
    public BiomeGenBase[] getBiomesForGeneration(int x, int z, int width, int length) {
        this.generatedBiomes = this.loadBlockGeneratorData(this.generatedBiomes, x, z, width, length);
        return this.generatedBiomes;
    }

    /**
     * Determines whether a chunk should contain city structures.
     * Uses a separate Perlin noise field with its own threshold.
     *
     * @return true if city structures should generate in this chunk
     */
    public boolean isUrbanChunk(int chunkX, int chunkZ) {
        if (this.invalidateCity) return false;
        double cityNoiseVal = this.cityNoise.generateNoise(chunkX / 16.0F, chunkZ / 16.0F);
        return cityNoiseVal > this.cityChance;
    }

    /**
     * Computes temperature values for a rectangular array, applying variation
     * noise and fuzz. Returned values are in [0, 1].
     *
     * @param buffer reuse an existing double array if large enough (pass null for auto-allocate)
     * @param x      block X of the top-left corner
     * @param z      block Z of the top-left corner
     * @param width  width in blocks
     * @param stride stride between rows (pass width)
     */
    public double[] getTemperatures(double[] buffer, int x, int z, int width, int stride) {
        if (buffer == null || buffer.length < stride * stride) {
            buffer = new double[stride * stride];
        }
        buffer = this.tempNoise.generateNoiseOctaves(buffer, x, z, stride, stride,
                this.temperatureScale, this.temperatureScale, this.temperatureExponent);
        this.variation = this.variationNoise.generateNoiseOctaves(this.variation, x, z, stride, stride,
                0.25D, 0.25D, 0.5882352941176471D);

        int idx = 0;
        for (int ix = 0; ix < stride; ++ix) {
            for (int iz = 0; iz < stride; ++iz) {
                double v = this.variation[idx] * 1.1D + 0.5D;
                double fuzz = this.temperatureFuzzPercent;
                double noFuzz = 1.0D - fuzz;
                double temp = (buffer[idx] * 0.15D + 0.7D) * noFuzz + v * fuzz;
                temp = 1.0D - (1.0D - temp) * (1.0D - temp);
                buffer[idx] = Math.max(0.0D, Math.min(1.0D, temp));
                ++idx;
            }
        }
        return buffer;
    }

    // ─── Noise scaling parameters ────────────────────────────────────────────────
    // These were tuned by hand to produce biome sizes roughly 2× larger than
    // vanilla Beta 1.7.3 while maintaining the same distribution shape.

    /** Noise frequency multiplier for temperature. */
    public double temperatureScale       = 0.0125D;
    /** Noise exponent for temperature (curves the distribution). */
    public double temperatureExponent   = 0.25D;
    /** Fraction of variation noise mixed into temperature (0.01 = 1 %). */
    public double temperatureFuzzPercent = 0.01D;

    /** Noise frequency multiplier for humidity. */
    public double humidityScale       = 0.025D;
    /** Noise exponent for humidity. */
    public double humidityExponent   = 0.3D;
    /** Fraction of variation noise mixed into humidity. */
    public double humidityFuzzPercent = 0.01D;

    /** Noise frequency multiplier for variation (large-scale biome boundaries). */
    public double variationScale    = 8.0D;
    /** Noise exponent for variation. */
    public double variationExponent = 0.025D;

    // ─── Biome loading ────────────────────────────────────────────────────────────

    /**
     * Computes the biome grid for a rectangular region, applying:
     * <ol>
     *   <li>Perlin noise for temperature, humidity, variation, and biome-amplitude</li>
     *   <li>Clamping and squashing curves</li>
     *   <li>Biome lookup from the 2D temperature/humidity table</li>
     *   <li>Special biome overrides at large spatial intervals</li>
     * </ol>
     *
     * @param biomeArray  reuse existing array if non-null and large enough
     * @param xPos        block X of the top-left corner
     * @param zPos        block Z of the top-left corner
     * @param width       width in blocks
     * @param length      length in blocks
     */
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomeArray,
            int xPos, int zPos, int width, int length) {
        if (biomeArray == null || biomeArray.length < width * length) {
            biomeArray = new BiomeGenBase[width * length];
        }

        this.temperature = this.tempNoise.generateNoiseOctaves(
                this.temperature, xPos, zPos, width, length,
                this.temperatureScale, this.temperatureScale, this.temperatureExponent);
        this.humidity = this.humidityNoise.generateNoiseOctaves(
                this.humidity, xPos, zPos, width, length,
                this.humidityScale, this.humidityScale, this.humidityExponent);
        this.variation = this.variationNoise.generateNoiseOctaves(
                this.variation, xPos, zPos, width, length,
                this.variationScale, this.variationScale, this.variationExponent);
        this.bigamplitude = this.biomeAmplitudeNoise.generateNoiseOctaves(
                this.bigamplitude, xPos, zPos, width, length,
                0.025D, 0.025D, 0.25D);

        this.invalidateCity = false;
        int idx = 0;

        for (int bx = 0; bx < width; ++bx) {
            for (int bz = 0; bz < length; ++bz) {
                double v = this.variation[idx] * 1.1D + 0.5D;

                // Temperature branch.
                double fuzz = this.temperatureFuzzPercent;
                double noFuzz = 1.0D - fuzz;
                double temperature = (this.temperature[idx] * 0.15D + 0.7D) * noFuzz + v * fuzz;

                // Humidity branch.
                fuzz = this.humidityFuzzPercent;
                noFuzz = 1.0D - fuzz;
                double humidity = (this.humidity[idx] * 0.15D + 0.5D) * noFuzz + v * fuzz;

                // Squash curve (approximates squared falloff).
                temperature = 1.0D - (1.0D - temperature) * (1.0D - temperature);

                if (this.cold) temperature -= 0.5D;

                temperature = Math.max(0.0D, Math.min(1.0D, temperature));
                humidity     = Math.max(0.0D, Math.min(1.0D, humidity));

                this.temperature[idx] = temperature;
                this.humidity[idx] = humidity;

                BiomeGenBase biome = BiomeGenBase.getBiomeFromLookup(temperature, humidity, 0.0);

                // Special biome overrides at every 8192-block interval.
                int wx = Math.abs(xPos + bx);
                int wz = Math.abs(zPos + bz);

                // Mushroom fields: every 8K in the far corner (all three flags set).
                if ((wx & 0x1c00) == 0x1c00 && (wz & 0x1c00) == 0x1c00
                        && biome instanceof BiomeGenForest && biome.weather == Weather.normal) {
                    biome = BiomeGenBase.biomeMycelium;
                    this.invalidateCity = true;
                }
                // Mangrove: every 8K in the 3rd-K strip, when humidity > 0.5.
                else if (((wx & 0x1c00) == 0x0800 || (wz & 0x1c00) == 0x0800)
                        && humidity > 0.5F && biome.weather == Weather.normal) {
                    biome = BiomeGenBase.biomeMangrove;
                }
                // Dark forest: every 8K in the 2nd-K strip, in forest/taiga.
                else if (((wx & 0x1c00) == 0x0400 ^ (wz & 0x1c00) == 0x0400)
                        && (biome == BiomeGenBase.biomeForest
                                || biome instanceof BiomeGenTaiga)) {
                    biome = biome.weather == Weather.cold
                            ? BiomeGenBase.biomeDarkForestCold
                            : BiomeGenBase.biomeDarkForest;
                    this.invalidateCity = true;
                }
                // Flower fields: every 8K in the 5th-K strip, in forest.
                else if ((wx & 0x1c00) == 0x1000 && (wz & 0x1c00) == 0x1000
                        && biome instanceof BiomeGenForest) {
                    biome = biome.weather == Weather.cold
                            ? BiomeGenBase.biomeFlowerFieldsCold
                            : BiomeGenBase.biomeFlowerFields;
                    this.invalidateCity = true;
                }

                biomeArray[idx] = biome;
                ++idx;
            }
        }

        return biomeArray;
    }
}
