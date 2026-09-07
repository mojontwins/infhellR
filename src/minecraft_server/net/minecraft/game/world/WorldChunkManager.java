package net.minecraft.game.world;

import java.util.Random;

import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorOctaves2;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorPerlin;
import net.minecraft.game.world.biome.BiomeGenForest;
import net.minecraft.game.world.biome.BiomeGenTaiga;
import net.minecraft.game.world.chunk.ChunkCoordIntPair;

public class WorldChunkManager {
	private NoiseGeneratorOctaves2 ngo1;
	private NoiseGeneratorOctaves2 ngo2;
	private NoiseGeneratorOctaves2 ngo3;

	public double[] temperature;
	public double[] humidity;
	public double[] variation;
	public double[] bigamplitude;
	
	public BiomeGenBase[] generatedBiomes;
	private NoiseGeneratorPerlin ngp;
	private NoiseGeneratorOctaves2 ngz;
	public boolean invalidateCity;
	
	public static double min = 999.0F, max = -999.0F;
	
	// Alter those to make bigger / smaller biomes
	/*
	// Vanilla values (0.025, 0.25) (0.05, 0.33...) (0.25, 0.5882352941176471) produce too much variation for InfHell which has lots of biomes.
	public double temperatureScale = 0.02500000037252903D;
	public double temperatureExponent = 0.25D;
	public double temperatureFuzzPercent = 0.01D;
	
	public double humidityScale = 0.05000000074505806D;
	public double humidityExponent = 0.3333333333333333D;
	public double humidityFuzzPercent = 0.002D;
	
	public double variationScale = 0.25D;
	public double variationExponent = 0.5882352941176471D;
	*/
	
	/*
	// BTA values for the extended default, with bigger biomes:
	public double temperatureScale = 0.00625D;
	public double temperatureExponent = 0.25D;
	public double temperatureFuzzPercent = 0.01D;
	
	public double humidityScale = 0.0125D;
	public double humidityExponent = 0.3D;
	public double humidityFuzzPercent = 0.01D;
	
	public double variationScale = 8.0D;
	public double variationExponent = 0.025D;
	*/
	
	// Adjusted by hand to get better results - it's almost like 2xBTA (twice as small), vanilla/2 (twice as big), sorta
	public double temperatureScale = 0.0125D;
	public double temperatureExponent = 0.25D;
	public double temperatureFuzzPercent = 0.01D;
	
	public double humidityScale = 0.025D;
	public double humidityExponent = 0.3D;
	public double humidityFuzzPercent = 0.01D;
	
	public double variationScale = 8.0D;
	public double variationExponent = 0.025D;
	
	public boolean cold;
	
	public float cityChance = 0.0F;
	
	protected WorldChunkManager() {
	}

	public WorldChunkManager(World world1) {
		this.ngo1 = new NoiseGeneratorOctaves2(new Random(world1.getRandomSeed() * 9871L), 4);
		this.ngo2 = new NoiseGeneratorOctaves2(new Random(world1.getRandomSeed() * 39811L), 4);
		this.ngo3 = new NoiseGeneratorOctaves2(new Random(world1.getRandomSeed() * 543321L), 2);
		this.ngz = new NoiseGeneratorOctaves2(new Random(world1.getRandomSeed() * 39811L), 2);
		this.ngp = new NoiseGeneratorPerlin(new Random(world1.getRandomSeed() * 9871L));
		
		this.cold = world1.getWorldInfo().isCold();
		this.cityChance = world1.getWorldInfo().getCityChance();
	}

	public BiomeGenBase getBiomeGenAtChunkCoord(ChunkCoordIntPair chunkCoordIntPair1) {
		return this.getBiomeGenAt(chunkCoordIntPair1.chunkXPos << 4, chunkCoordIntPair1.chunkZPos << 4);
	}

	public BiomeGenBase getBiomeGenAt(int i1, int i2) {
		return this.getBiomesForGeneration(i1, i2, 1, 1)[0];
	}

	public double getTemperature(int i1, int i2) {
		this.temperature = this.ngo1.generateNoiseOctaves(this.temperature, (double)i1, (double)i2, 1, 1, 0.02500000037252903D, 0.02500000037252903D, 0.5D);
		return this.temperature[0];
	}

	public BiomeGenBase[] getBiomesForGeneration(int i1, int i2, int i3, int i4) {
		this.generatedBiomes = this.loadBlockGeneratorData(this.generatedBiomes, i1, i2, i3, i4);
		return this.generatedBiomes;
	}
	
	public boolean isUrbanChunk(int chunkX, int chunkZ) {
		if(this.invalidateCity) return false;
		
		double cityNoise = this.ngp.generateNoise(chunkX / 16.0F, chunkZ / 16.0F);
		
		/*
		double min = WorldChunkManager.min, max = WorldChunkManager.max;
		if (WorldChunkManager.min > cityNoise) WorldChunkManager.min = cityNoise;
		if (WorldChunkManager.max < cityNoise) WorldChunkManager.max = cityNoise;
		if (min != WorldChunkManager.min || max != WorldChunkManager.max) System.out.println (min + " - " + max); 
		*/
		
		return cityNoise > this.cityChance;
	}

	public double[] getTemperatures(double[] d1, int i2, int i3, int i4, int i5) {
		if(d1 == null || d1.length < i4 * i5) {
			d1 = new double[i4 * i5];
		}

		d1 = this.ngo1.generateNoiseOctaves(d1, (double)i2, (double)i3, i4, i5, this.temperatureScale, this.temperatureScale, this.temperatureExponent);
		this.variation = this.ngo3.generateNoiseOctaves(this.variation, (double)i2, (double)i3, i4, i5, 0.25D, 0.25D, 0.5882352941176471D);
		int i6 = 0;

		for(int i7 = 0; i7 < i4; ++i7) {
			for(int i8 = 0; i8 < i5; ++i8) {
				double d9 = this.variation[i6] * 1.1D + 0.5D;
				double d11 = this.temperatureFuzzPercent;
				double d13 = 1.0D - d11;
				double d15 = (d1[i6] * 0.15D + 0.7D) * d13 + d9 * d11;
				d15 = 1.0D - (1.0D - d15) * (1.0D - d15);
				if(d15 < 0.0D) {
					d15 = 0.0D;
				}

				if(d15 > 1.0D) {
					d15 = 1.0D;
				}

				d1[i6] = d15;
				++i6;
			}
		}

		return d1;
	}

	// Calculate which biome
	public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomeGenArray, int xPos, int zPos, int width, int length) {
		if(biomeGenArray == null || biomeGenArray.length < width * length) {
			biomeGenArray = new BiomeGenBase[width * length];
		}

		this.temperature = this.ngo1.generateNoiseOctaves(this.temperature, (double)xPos, (double)zPos, width, length, this.temperatureScale, this.temperatureScale, this.temperatureExponent);
		this.humidity = this.ngo2.generateNoiseOctaves(this.humidity, (double)xPos, (double)zPos, width, length, this.humidityScale, this.humidityScale, this.humidityExponent);
		this.variation = this.ngo3.generateNoiseOctaves(this.variation, (double)xPos, (double)zPos, width, length, this.variationScale, this.variationScale, this.variationExponent);
		this.bigamplitude = this.ngz.generateNoiseOctaves(this.bigamplitude, (double)xPos, (double)zPos, width, length, 0.025D, 0.025D, 0.25D);
		
		this.invalidateCity = false;
		int biomeIndex = 0;

		for(int x = 0; x < width; ++x) {
			for(int z = 0; z < length; ++z) {
				double d9 = this.variation[biomeIndex] * 1.1D + 0.5D;
				double d11 = this.temperatureFuzzPercent;
				double d13 = 1.0D - d11;
				double temperature = (this.temperature[biomeIndex] * 0.15D + 0.7D) * d13 + d9 * d11;
				
				d11 = this.humidityFuzzPercent;
				d13 = 1.0D - d11;
				double humidity = (this.humidity[biomeIndex] * 0.15D + 0.5D) * d13 + d9 * d11;
				temperature = 1.0D - (1.0D - temperature) * (1.0D - temperature);

				if(this.cold) temperature -= 0.5D; 
				
				if(temperature < 0.0D) {
					temperature = 0.0D;
				}

				if(humidity < 0.0D) {
					humidity = 0.0D;
				}

				if(temperature > 1.0D) {
					temperature = 1.0D;
				}

				if(humidity > 1.0D) {
					humidity = 1.0D;
				}
				
				/*
				double zone = this.bigamplitude [biomeIndex] + 1.5 / 3.0F; //(this.bigamplitude [biomeIndex] * 1.33D + 1.0) / 1.45D;
				if (zone < 0.0D) zone = 0.0D;
				if (zone > 1.0D) zone = 1.0D;
				*/
				
				/*
				if (temperature < min) min = temperature;
				if (temperature > max) max = temperature;
				System.out.println(temperature + " --- " + min + " - " + max);
				*/
				
				this.temperature[biomeIndex] = temperature;
				this.humidity[biomeIndex] = humidity;
				
				BiomeGenBase biome = BiomeGenBase.getBiomeFromLookup(temperature, humidity, 0.0);
				
				int xx = Math.abs(xPos + x); int zz = Math.abs(zPos + z);
				
				// Patch in special biome(s)				
				if ((xx & 0x1c00) == 0x1c00 && (zz & 0x1c00) == 0x1c00 && biome instanceof BiomeGenForest && biome.weather == Weather.normal) {
					// Every 8K, in the last K (sectors)
					biome = BiomeGenBase.biomeMycelium;
					this.invalidateCity = true;
				} else if(((xx & 0x1c00) == 0x0800 || (zz & 0x1c00) == 0x0800) && humidity > 0.5F && biome.weather == Weather.normal) {		
					// Every 8K, in the 3rd K, when humidity > 0.5F (stripes)
					biome = BiomeGenBase.biomeMangrove;
				} else if(((xx & 0x1c00) == 0x0400 ^ (zz & 0x1c00) == 0x0400) && (biome == BiomeGenBase.biomeForest || (biome instanceof BiomeGenTaiga))) {
					// Every 8K, in the 2nd K (stripes)
					biome = biome.weather == Weather.cold ? BiomeGenBase.biomeDarkForestCold : BiomeGenBase.biomeDarkForest;
					this.invalidateCity = true; 
				} else if(((xx & 0x1c00) == 0x1000 && (zz & 0x1c00) == 0x1000) && /*(biome == BiomeGenBase.biomeSavanna || biome == BiomeGenBase.biomeHotForest)*/ biome instanceof BiomeGenForest) {		
					// Every 8K, in the 5th K (sectors)
					biome = biome.weather == Weather.cold ? BiomeGenBase.biomeFlowerFieldsCold : BiomeGenBase.biomeFlowerFields;
					this.invalidateCity = true;
				}
				
				biomeGenArray[biomeIndex] = biome;
				biomeIndex ++;
			}
		}

		return biomeGenArray;
	}
}
