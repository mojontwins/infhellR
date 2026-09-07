package net.minecraft.game.world;

import java.util.Arrays;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;

public class WorldChunkManagerHell extends WorldChunkManager {
	private BiomeGenBase biomeHell;
	private double temperatureHell;
	private double humidityHell;

	public WorldChunkManagerHell(BiomeGenBase biomeGenBase1, double d2, double d4) {
		this.biomeHell = biomeGenBase1;
		this.temperatureHell = d2;
		this.humidityHell = d4;
	}

	public BiomeGenBase getBiomeGenAtChunkCoord(ChunkCoordIntPair chunkCoordIntPair1) {
		return this.biomeHell;
	}

	public BiomeGenBase getBiomeGenAt(int i1, int i2) {
		return this.biomeHell;
	}

	public double getTemperature(int i1, int i2) {
		return this.temperatureHell;
	}

	public BiomeGenBase[] getBiomesForGeneration(int i1, int i2, int i3, int i4) {
		this.generatedBiomes = this.loadBlockGeneratorData(this.generatedBiomes, i1, i2, i3, i4);
		return this.generatedBiomes;
	}

	public double[] getTemperatures(double[] d1, int i2, int i3, int i4, int i5) {
		if(d1 == null || d1.length < i4 * i5) {
			d1 = new double[i4 * i5];
		}

		Arrays.fill(d1, 0, i4 * i5, this.temperatureHell);
		return d1;
	}

	public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomeGenBase1, int i2, int i3, int i4, int i5) {
		if(biomeGenBase1 == null || biomeGenBase1.length < i4 * i5) {
			biomeGenBase1 = new BiomeGenBase[i4 * i5];
		}

		if(this.temperature == null || this.temperature.length < i4 * i5) {
			this.temperature = new double[i4 * i5];
			this.humidity = new double[i4 * i5];
		}

		Arrays.fill(biomeGenBase1, 0, i4 * i5, this.biomeHell);
		Arrays.fill(this.humidity, 0, i4 * i5, this.humidityHell);
		Arrays.fill(this.temperature, 0, i4 * i5, this.temperatureHell);
		return biomeGenBase1;
	}
}
