package net.minecraft.game.world;

import java.util.Arrays;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;

/**
 * A flat {@link WorldChunkManager} for the Nether and Sky dimensions.
 *
 * <p>All positions return the same fixed biome, temperature, and humidity —
 * no Perlin noise is used. This makes terrain generation fast and ensures
 * the nether/sky biomes are uniform throughout.</p>
 */
public class WorldChunkManagerHell extends WorldChunkManager {
    private final BiomeGenBase fixedBiome;
    private final double temperatureHell;
    private final double humidityHell;

    public WorldChunkManagerHell(BiomeGenBase biome, double temperature, double humidity) {
        this.fixedBiome = biome;
        this.temperatureHell = temperature;
        this.humidityHell = humidity;
    }

    @Override
    public BiomeGenBase getBiomeGenAtChunkCoord(ChunkCoordIntPair chunk) {
        return this.fixedBiome;
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        return this.fixedBiome;
    }

    @Override
    public double getTemperature(int x, int z) {
        return this.temperatureHell;
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(int x, int z, int width, int length) {
        this.generatedBiomes = this.loadBlockGeneratorData(this.generatedBiomes, x, z, width, length);
        return this.generatedBiomes;
    }

    @Override
    public double[] getTemperatures(double[] buffer, int x, int z, int width, int stride) {
        int size = stride * stride;
        if (buffer == null || buffer.length < size) {
            buffer = new double[size];
        }
        Arrays.fill(buffer, 0, size, this.temperatureHell);
        return buffer;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomeArray,
            int x, int z, int width, int length) {
        int size = width * length;
        if (biomeArray == null || biomeArray.length < size) {
            biomeArray = new BiomeGenBase[size];
        }

        if (this.temperature == null || this.temperature.length < size) {
            this.temperature = new double[size];
            this.humidity = new double[size];
        }

        Arrays.fill(biomeArray, 0, size, this.fixedBiome);
        Arrays.fill(this.humidity,     0, size, this.humidityHell);
        Arrays.fill(this.temperature, 0, size, this.temperatureHell);
        return biomeArray;
    }
}
