package net.minecraft.game.world.chunk;

import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.ChunkProviderGenerate;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;

public class ChunkProviderSky extends ChunkProviderGenerate implements IChunkProvider {
	
	public ChunkProviderSky(World world1, long j2) {
		this(world1, j2, true, true);
	}
	
	public ChunkProviderSky(World world1, long j2, boolean b, boolean c) {
		super(world1, j2, b, c);
	}

	public void generateTerrain(int chunkX, int chunkZ, byte[] blocks) {
		double noiseScale = 0.25D;
		double yscalingFactor = 0.125D;
		double densityVariationSpeed = 0.125D;
		
		byte quadrantSize = 2;
		
		int cellSize = quadrantSize + 1;
		byte columnSize = 33;
		int cellSize2 = quadrantSize + 1;
		short chunkHeight = 128;
		
		this.terrainNoise = this.initializeNoiseField(this.terrainNoise, chunkX * quadrantSize, 0, chunkZ * quadrantSize, cellSize, columnSize, cellSize2);

		for(int xSection = 0; xSection < quadrantSize; ++xSection) {
			for(int zSection = 0; zSection < quadrantSize; ++zSection) {
				for(int ySection = 0; ySection < 32; ++ySection) {
					
					double noiseA = this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 0) * columnSize + ySection + 0];
					double noiseB = this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 1) * columnSize + ySection + 0];
					double noiseC = this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 0) * columnSize + ySection + 0];
					double noiseD = this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 1) * columnSize + ySection + 0];
					double noiseAinc = (this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 0) * columnSize + ySection + 1] - noiseA) * noiseScale;
					double noiseBinc = (this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 1) * columnSize + ySection + 1] - noiseB) * noiseScale;
					double noiseCinc = (this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 0) * columnSize + ySection + 1] - noiseC) * noiseScale;
					double noiseDinc = (this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 1) * columnSize + ySection + 1] - noiseD) * noiseScale;

					for(int y = 0; y < 4; ++y) {
						
						double curNoiseA = noiseA;
						double curNoiseB = noiseB;
						double curNoiseAinc = (noiseC - noiseA) * yscalingFactor;
						double curNoiseBinc = (noiseD - noiseB) * yscalingFactor;

						for(int x = 0; x < 8; ++x) {
							int indexInBlockArray = (x + (xSection << 3)) << 11 | (0 + (zSection << 3)) << 7 | (ySection << 2) + y;
														
							double density = curNoiseA;
							double densityIncrement = (curNoiseB - curNoiseA) * densityVariationSpeed;

							for(int z = 0; z < 8; ++z) {
								int blockID = 0;
								if(density > 0.0D) {
									blockID = Block.stone.blockID;
								}

								blocks[indexInBlockArray] = (byte)blockID;
								indexInBlockArray += chunkHeight;
								density += densityIncrement;
							}

							curNoiseA += curNoiseAinc;
							curNoiseB += curNoiseBinc;
						}

						noiseA += noiseAinc;
						noiseB += noiseBinc;
						noiseC += noiseCinc;
						noiseD += noiseDinc;
					}
				}
			}
		}

	}

	public Chunk prepareChunk(int i1, int i2) {
		return this.provideChunk(i1, i2);
	}
	
	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		this.rand.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);

		// No block storage: the land-surface height map is derived straight from the density
		// lattice below (Solution 2-A of docs/height_query_caching.md). Same observable members
		// as the pre-2-A path: landSurfaceHeightMap, isOcean (always false for the sky dimension),
		// isUrbanChunk; getBlockID resolves every cell above y 127 to air, so the height-map scan
		// of the old path was exactly "topmost positive density".
		Chunk chunk = new Chunk(this.worldObj, chunkX, chunkZ);
		this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
		chunk.isUrbanChunk = this.worldObj.getWorldChunkManager().isUrbanChunk(chunkX, chunkZ);

		double noiseScale = 0.25D;
		double yscalingFactor = 0.125D;
		double densityVariationSpeed = 0.125D;

		byte quadrantSize = 2;
		int cellSize = quadrantSize + 1;
		byte columnSize = 33;
		int cellSize2 = quadrantSize + 1;

		this.terrainNoise = this.initializeNoiseField(this.terrainNoise, chunkX * quadrantSize, 0, chunkZ * quadrantSize, cellSize, columnSize, cellSize2);

		byte[] topY = new byte[256];
		for(int xSection = 0; xSection < quadrantSize; ++xSection) {
			for(int zSection = 0; zSection < quadrantSize; ++zSection) {
				for(int ySection = 0; ySection < 32; ++ySection) {
					double noiseA = this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 0) * columnSize + ySection + 0];
					double noiseB = this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 1) * columnSize + ySection + 0];
					double noiseC = this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 0) * columnSize + ySection + 0];
					double noiseD = this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 1) * columnSize + ySection + 0];
					double noiseAinc = (this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 0) * columnSize + ySection + 1] - noiseA) * noiseScale;
					double noiseBinc = (this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 1) * columnSize + ySection + 1] - noiseB) * noiseScale;
					double noiseCinc = (this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 0) * columnSize + ySection + 1] - noiseC) * noiseScale;
					double noiseDinc = (this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 1) * columnSize + ySection + 1] - noiseD) * noiseScale;

					for(int y = 0; y < 4; ++y) {
						double curNoiseA = noiseA;
						double curNoiseB = noiseB;
						double curNoiseAinc = (noiseC - noiseA) * yscalingFactor;
						double curNoiseBinc = (noiseD - noiseB) * yscalingFactor;

						for(int x = 0; x < 8; ++x) {
							double density = curNoiseA;
							double densityIncrement = (curNoiseB - curNoiseA) * densityVariationSpeed;

							for(int z = 0; z < 8; ++z) {
								// Sky terrain is stone wherever density is positive and air elsewhere.
								if(density > 0.0D) {
									topY[(((zSection << 3) | z) << 4) | ((xSection << 3) | x)] = (byte)((ySection << 2) + y);
								}

								density += densityIncrement;
							}

							curNoiseA += curNoiseAinc;
							curNoiseB += curNoiseBinc;
						}

						noiseA += noiseAinc;
						noiseB += noiseBinc;
						noiseC += noiseCinc;
						noiseD += noiseDinc;
					}
				}
			}
		}

		System.arraycopy(topY, 0, chunk.landSurfaceHeightMap, 0, topY.length);
		chunk.isOcean = this.isOcean;
		return chunk;
	}

	private double[] initializeNoiseField(double[] d1, int i2, int i3, int i4, int i5, int i6, int i7) {
		if(d1 == null) {
			d1 = new double[i5 * i6 * i7];
		}

		double d8 = 684.412D;
		double d10 = 684.412D;
		double[] d12 = this.worldObj.getWorldChunkManager().temperature;
		double[] d13 = this.worldObj.getWorldChunkManager().humidity;
		this.scaleArray = this.scaleNoise.generateNoiseOctaves(this.scaleArray, i2, i4, i5, i7, 1.121D, 1.121D, 0.5D);
		this.depthArray = this.depthNoise.generateNoiseOctaves(this.depthArray, i2, i4, i5, i7, 200.0D, 200.0D, 0.5D);
		d8 *= 2.0D; 	// This makes the difference between overworld & sky dimension
		this.mainArray = this.mainNoise.generateNoiseOctaves(this.mainArray, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8 / 80.0D, d10 / 160.0D, d8 / 80.0D);
		this.minLimitArray = this.minLimitNoise.generateNoiseOctaves(this.minLimitArray, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8, d10, d8);
		this.maxLimitArray = this.maxLimitNoise.generateNoiseOctaves(this.maxLimitArray, (double)i2, (double)i3, (double)i4, i5, i6, i7, d8, d10, d8);
		int i14 = 0;
		int i15 = 0;
		int i16 = 16 / i5;

		for(int i17 = 0; i17 < i5; ++i17) {
			int i18 = i17 * i16 + i16 / 2;

			for(int i19 = 0; i19 < i7; ++i19) {
				int i20 = i19 * i16 + i16 / 2;
				double d21 = d12[i18 * 16 + i20];
				double d23 = d13[i18 * 16 + i20] * d21;
				double d25 = 1.0D - d23;
				d25 *= d25;
				d25 *= d25;
				d25 = 1.0D - d25;
				double d27 = (this.scaleArray[i15] + 256.0D) / 512.0D;
				d27 *= d25;
				if(d27 > 1.0D) {
					d27 = 1.0D;
				}

				double d29 = this.depthArray[i15] / 8000.0D;
				if(d29 < 0.0D) {
					d29 = -d29 * 0.3D;
				}

				d29 = d29 * 3.0D - 2.0D;
				if(d29 > 1.0D) {
					d29 = 1.0D;
				}

				d29 /= 8.0D;
				d29 = 0.0D;
				if(d27 < 0.0D) {
					d27 = 0.0D;
				}

				d27 += 0.5D;
				d29 = d29 * (double)i6 / 16.0D;
				++i15;
				double d31 = (double)i6 / 2.0D;

				for(int i33 = 0; i33 < i6; ++i33) {
					double d34 = 0.0D;
					double d36 = ((double)i33 - d31) * 8.0D / d27;
					if(d36 < 0.0D) {
						d36 *= -1.0D;
					}

					double d38 = this.minLimitArray[i14] / 512.0D;
					double d40 = this.maxLimitArray[i14] / 512.0D;
					double d42 = (this.mainArray[i14] / 10.0D + 1.0D) / 2.0D;
					if(d42 < 0.0D) {
						d34 = d38;
					} else if(d42 > 1.0D) {
						d34 = d40;
					} else {
						d34 = d38 + (d40 - d38) * d42;
					}

					d34 -= 8.0D;
					byte b44 = 32;
					double d45;
					if(i33 > i6 - b44) {
						d45 = (double)((float)(i33 - (i6 - b44)) / ((float)b44 - 1.0F));
						d34 = d34 * (1.0D - d45) + -30.0D * d45;
					}

					b44 = 8;
					if(i33 < b44) {
						d45 = (double)((float)(b44 - i33) / ((float)b44 - 1.0F));
						d34 = d34 * (1.0D - d45) + -30.0D * d45;
					}

					d1[i14] = d34;
					++i14;
				}
			}
		}

		return d1;
	}
	
	public void populateOres(int x0, int z0, BiomeGenBase biomeGen) {
		int i, x, y, z;
		
		for(i = 0; i < biomeGen.coalLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			(new WorldGenMinable(Block.oreCoal.blockID, 16)).generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < biomeGen.glowLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			(new WorldGenMinable(Block.oreGlow.blockID, 8)).generate(this.worldObj, this.rand, x, y, z);
		}
				
		for(i = 0; i < 2*biomeGen.ironLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			(new WorldGenMinable(Block.oreIron.blockID, 8)).generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < 4*biomeGen.goldLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			(new WorldGenMinable(Block.oreGold.blockID, 8)).generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < 6*biomeGen.redstoneLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			(new WorldGenMinable(Block.oreRedstone.blockID, 7)).generate(this.worldObj, this.rand, x, y, z);
		}

		for(i = 0; i < 6*biomeGen.diamondLumpAttempts; ++i) {
			x = x0 + this.rand.nextInt(16);
			y = this.rand.nextInt(128);
			z = z0 + this.rand.nextInt(16);
			(new WorldGenMinable(Block.oreDiamond.blockID, 7)).generate(this.worldObj, this.rand, x, y, z);
		}
	}
	
	public void generateMapFeatures(int chunkX, int chunkZ) {
		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ, true);
			this.mineshaftMesaGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ, true);
			this.strongholdGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ, true);
		}
	}

	public boolean chunkExists(int i1, int i2) {
		return true;
	}
	
	public boolean saveChunks(boolean z1, IProgressUpdate iProgressUpdate2) {
		return true;
	}

	public boolean unload100OldestChunks() {
		return false;
	}

	public boolean canSave() {
		return true;
	}

	public String makeString() {
		return "RandomLevelSource";
	}
}
