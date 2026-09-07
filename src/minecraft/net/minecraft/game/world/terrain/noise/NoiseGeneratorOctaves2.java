package net.minecraft.game.world.terrain.noise;

import java.util.Random;

public class NoiseGeneratorOctaves2 extends NoiseGenerator {
	private NoiseGenerator2[] noiseGenerator2;
	private int iterations;

	public NoiseGeneratorOctaves2(Random var1, int var2) {
		this.iterations = var2;
		this.noiseGenerator2 = new NoiseGenerator2[var2];

		for(int var3 = 0; var3 < var2; ++var3) {
			this.noiseGenerator2[var3] = new NoiseGenerator2(var1);
		}

	}

	public double[] generateNoiseOctaves(double[] noise, double xPos, double zPos, int xSize, int zSize, double xScale, double zScale, double exponent) {
		return this.generateNoiseOctaves(noise, xPos, zPos, xSize, zSize, xScale, zScale, exponent, 0.5D);
	}

	public double[] generateNoiseOctaves(double[] noise, double xPos, double zPos, int xSize, int zSize, double xScale, double zScale, double exponent, double ratio) {
		xScale /= 1.5D;
		zScale /= 1.5D;
		if(noise != null && noise.length >= xSize * zSize) {
			for(int i = 0; i < noise.length; ++i) {
				noise[i] = 0.0D;
			}
		} else {
			noise = new double[xSize * zSize];
		}

		double levelScaleModifier = 1.0D;
		double scaleModifier = 1.0D;

		for(int var20 = 0; var20 < this.iterations; ++var20) {
			this.noiseGenerator2[var20].generateNoise(noise, xPos, zPos, xSize, zSize, xScale * scaleModifier, zScale * scaleModifier, 0.55D / levelScaleModifier);
			scaleModifier *= exponent;
			levelScaleModifier *= ratio;
		}

		return noise;
	}
}
