package net.minecraft.game.world.chunk;

import java.util.Random;
import net.minecraft.game.IProgressUpdate;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.MapGenBase;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorOctaves;
import net.minecraft.game.world.block.BlockSand;
import net.minecraft.game.world.terrain.generate.MapGenCavesHell;
import net.minecraft.game.world.terrain.generate.WorldGenFire;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenGlowStone1;
import net.minecraft.game.world.terrain.generate.WorldGenGlowStone2;
import net.minecraft.game.world.terrain.generate.WorldGenHellLava;

public class ChunkProviderHell implements IChunkProvider {
	private Random rand;
	private NoiseGeneratorOctaves maxLimitNoise;
	private NoiseGeneratorOctaves minLimitNoise;
	private NoiseGeneratorOctaves mainNoise;
	private NoiseGeneratorOctaves noiseGenSandOrGravel;
	private NoiseGeneratorOctaves noiseStone;
	public NoiseGeneratorOctaves scaleNoise;
	public NoiseGeneratorOctaves depthNoise;
	private World worldObj;
	private double[] terrainNoise;
	private double[] sandNoise = new double[256];
	private double[] gravelNoise = new double[256];
	private double[] stoneNoise = new double[256];
	private MapGenBase caveGenerator = new MapGenCavesHell();
	
	double[] mainArray;
	double[] minLimitArray;
	double[] maxLimitArray;
	double[] scaleArray;
	double[] depthArray;

	public ChunkProviderHell(World world1, long j2) {
		this.worldObj = world1;
		this.rand = new Random(j2);
		this.maxLimitNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.minLimitNoise = new NoiseGeneratorOctaves(this.rand, 16);
		this.mainNoise = new NoiseGeneratorOctaves(this.rand, 8);
		this.noiseGenSandOrGravel = new NoiseGeneratorOctaves(this.rand, 4);
		this.noiseStone = new NoiseGeneratorOctaves(this.rand, 4);
		this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
		this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
	}

	public void generateTerrain(int chunkX, int chunkZ, byte[] blocks) {
		double noiseScale = 0.125D;
		double scalingFactor = 0.25D;
		double densityVariationSpeed = 0.25D;
						
		byte quadrantSize = 4;
		byte lavaLevel = 32;
		int cellSize = quadrantSize + 1;
		byte columnSize = 17;
		int cellSize2 = quadrantSize + 1;
		short chunkHeight = 128;

		this.terrainNoise = this.initializeNoiseField(this.terrainNoise, chunkX * quadrantSize, 0, chunkZ * quadrantSize, cellSize, columnSize, cellSize2);

		for(int xSection = 0; xSection < quadrantSize; ++xSection) {
			for(int zSection = 0; zSection < quadrantSize; ++zSection) {
				for(int ySection = 0; ySection < 16; ++ySection) {
					
					double noiseA = this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 0) * columnSize + ySection + 0];
					double noiseB = this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 1) * columnSize + ySection + 0];
					double noiseC = this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 0) * columnSize + ySection + 0];
					double noiseD = this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 1) * columnSize + ySection + 0];
					double noiseAinc = (this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 0) * columnSize + ySection + 1] - noiseA) * noiseScale;
					double noiseBinc = (this.terrainNoise[((xSection + 0) * cellSize2 + zSection + 1) * columnSize + ySection + 1] - noiseB) * noiseScale;
					double noiseCinc = (this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 0) * columnSize + ySection + 1] - noiseC) * noiseScale;
					double noiseDinc = (this.terrainNoise[((xSection + 1) * cellSize2 + zSection + 1) * columnSize + ySection + 1] - noiseD) * noiseScale;

					for(int y = 0; y < 8; ++y) {
						double curNoiseA = noiseA;
						double curNoiseB = noiseB;
						double curNoiseAinc = (noiseC - noiseA) * scalingFactor;
						double curNoiseBinc = (noiseD - noiseB) * scalingFactor;

						int yy = ySection * 8 + y;

						for(int x = 0; x < 4; ++x) {
							int indexInBlockArray = (x + (xSection << 2)) << 11 | (0 + (zSection << 2)) << 7 | (ySection << 3) + y;
							
							double density = curNoiseA;
							double densityIncrement = (curNoiseB - curNoiseA) * densityVariationSpeed;

							for(int z = 0; z < 4; ++z) {
								int blockID = 0;
								if(yy < lavaLevel) {
									blockID = Block.lavaStill.blockID;
								}

								if(density > 0.0D) {
									blockID = Block.bloodStone.blockID;
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

	public void replaceBlocksForBiome(int chunkX, int chunkZ, byte[] blocks) {
		byte lavaLevel = 64;
		double d5 = 8.0D / 256D;
		this.sandNoise = this.noiseGenSandOrGravel.generateNoiseOctaves(this.sandNoise, (double)(chunkX * 16), (double)(chunkZ * 16), 0.0D, 16, 16, 1, d5, d5, 1.0D);
		this.gravelNoise = this.noiseGenSandOrGravel.generateNoiseOctaves(this.gravelNoise, (double)(chunkX * 16), 109.0134D, (double)(chunkZ * 16), 16, 1, 16, d5, 1.0D, d5);
		this.stoneNoise = this.noiseStone.generateNoiseOctaves(this.stoneNoise, (double)(chunkX * 16), (double)(chunkZ * 16), 0.0D, 16, 16, 1, d5 * 2.0D, d5 * 2.0D, d5 * 2.0D);

		for(int x = 0; x < 16; ++x) {
			for(int z = 0; z < 16; ++z) {
				boolean generateSand = this.sandNoise[x + z * 16] + this.rand.nextDouble() * 0.2D > 0.0D;
				boolean generateGravel = this.gravelNoise[x + z * 16] + this.rand.nextDouble() * 0.2D > 0.0D;
				int i11 = (int)(this.stoneNoise[x + z * 16] / 3.0D + 3.0D + this.rand.nextDouble() * 0.25D);
				int i12 = -1;
				byte topBlock = (byte)Block.bloodStone.blockID;
				byte fillerBlock = (byte)Block.bloodStone.blockID;

				for(int y = 127; y >= 0; --y) {
					int index = x << 11 | z << 7 | y; // (x * 16 + z) * 128 + y
					if(y >= 127 - this.rand.nextInt(5)) {
						blocks[index] = (byte)Block.bedrock.blockID;
					} else if(y <= 0 + this.rand.nextInt(5)) {
						blocks[index] = (byte)Block.bedrock.blockID;
					} else {
						byte blockID = blocks[index];
						if(blockID == 0) {
							i12 = -1;
						} else if(blockID == Block.bloodStone.blockID) {
							if(i12 == -1) {
								if(i11 <= 0) {
									topBlock = 0;
									fillerBlock = (byte)Block.bloodStone.blockID;
								} else if(y >= lavaLevel - 4 && y <= lavaLevel + 1) {
									topBlock = (byte)Block.bloodStone.blockID;
									fillerBlock = (byte)Block.bloodStone.blockID;
									if(generateGravel) {
										topBlock = (byte)Block.gravel.blockID;
										fillerBlock = (byte)Block.bloodStone.blockID;
									}

									if(generateSand) {
										topBlock = (byte)Block.slowSand.blockID;
										fillerBlock = (byte)Block.slowSand.blockID;
									}
								}

								if(y < lavaLevel && topBlock == 0) {
									topBlock = (byte)Block.lavaStill.blockID;
								}

								i12 = i11;
								if(y >= lavaLevel - 1) {
									blocks[index] = topBlock;
								} else {
									blocks[index] = fillerBlock;
								}
							} else if(i12 > 0) {
								--i12;
								blocks[index] = fillerBlock;
							}
						}
					}
				}
			}
		}

	}

	public Chunk prepareChunk(int i1, int i2) {
		return this.provideChunk(i1, i2);
	}

	public Chunk provideChunk(int chunkX, int chunkZ) {
		this.rand.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);
		
		// Empty block array & new Chunk
		byte[] blockArray = new byte[32768];
		byte[] metadata = new byte[32768];
		Chunk chunk = new Chunk(this.worldObj, blockArray, metadata, chunkX, chunkZ);

		// Generate terrain for this chunk
		this.generateTerrain(chunkX, chunkZ, blockArray);
		
		// Replace blocks
		this.replaceBlocksForBiome(chunkX, chunkZ, blockArray);

		// Generate caves
		this.caveGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);

		// Terrain generation is done; slice the flat buffers into runtime subchunks so the light
		// engine (initLightingForRealNotJustHeightmap) can write per-section light values.
		chunk.loadFlatBlocks(blockArray, metadata);

		// Done
		return chunk;	
	}
	
	public Chunk justGenerateForHeight(int chunkX, int chunkZ) {
		this.rand.setSeed((long)chunkX * 341873128712L + (long)chunkZ * 132897987541L);
		
		// Empty block array & new Chunk
		byte[] blockArray = new byte[32768];
		byte[] metadata = new byte[32768];
		Chunk chunk = new Chunk(this.worldObj, blockArray, metadata, chunkX, chunkZ);
		this.generateTerrain(chunkX, chunkZ, blockArray);
		this.caveGenerator.generate(this, this.worldObj, chunkX, chunkZ, blockArray);
		
		return chunk;
	}

	private double[] initializeNoiseField(double[] densityMapArray, int x, int y, int z, int xSize, int ySize, int zSize) {
		if(densityMapArray == null) {
			densityMapArray = new double[xSize * ySize * zSize];
		}

		double scaleXZ = 684.412D;
		double scaleY = 2053.236D;
		this.scaleArray = this.scaleNoise.generateNoiseOctaves(this.scaleArray, (double)x, (double)y, (double)z, xSize, 1, zSize, 1.0D, 0.0D, 1.0D);
		this.depthArray = this.depthNoise.generateNoiseOctaves(this.depthArray, (double)x, (double)y, (double)z, xSize, 1, zSize, 100.0D, 0.0D, 100.0D);
		this.mainArray = this.mainNoise.generateNoiseOctaves(this.mainArray, (double)x, (double)y, (double)z, xSize, ySize, zSize, scaleXZ / 80.0D, scaleY / 60.0D, scaleXZ / 80.0D);
		this.minLimitArray = this.maxLimitNoise.generateNoiseOctaves(this.minLimitArray, (double)x, (double)y, (double)z, xSize, ySize, zSize, scaleXZ, scaleY, scaleXZ);
		this.maxLimitArray = this.minLimitNoise.generateNoiseOctaves(this.maxLimitArray, (double)x, (double)y, (double)z, xSize, ySize, zSize, scaleXZ, scaleY, scaleXZ);
		
		int mainIndex = 0;
		int depthScaleIndex = 0;
		
		double[] densityHoleArray = new double[ySize];

		for(int dy = 0; dy < ySize; ++dy) {
			densityHoleArray[dy] = Math.cos((double)dy * Math.PI * 6.0D / (double)ySize) * 2.0D;
			double distanceToBorder = (double)dy;
			if(dy > ySize / 2) {
				distanceToBorder = (double)(ySize - 1 - dy);
			}

			if(distanceToBorder < 4.0D) {
				distanceToBorder = 4.0D - distanceToBorder;
				densityHoleArray[dy] -= distanceToBorder * distanceToBorder * distanceToBorder * 10.0D;
			}
		}

		for(int dx = 0; dx < xSize; ++dx) {
			for(int dz = 0; dz < zSize; ++dz) {
				double scale = (this.scaleArray[depthScaleIndex] + 256.0D) / 512.0D;
				if(scale > 1.0D) {
					scale = 1.0D;
				}

				double d19 = 0.0D;

				double depth = this.depthArray[depthScaleIndex] / 8000.0D;
				if(depth < 0.0D) {
					depth = -depth;
				}

				depth = depth * 3.0D - 3.0D;
				if(depth < 0.0D) {
					depth /= 2.0D;
					if(depth < -1.0D) {
						depth = -1.0D;
					}

					depth /= 1.4D;
					depth /= 2.0D;
					scale = 0.0D;
				} else {
					if(depth > 1.0D) {
						depth = 1.0D;
					}

					depth /= 6.0D;
				}

				scale += 0.5D;
				depth = depth * (double)ySize / 16.0D;
				++depthScaleIndex;

				for(int dy = 0; dy < ySize; ++dy) {
					double density = 0.0D;
					double densitySubtracted = densityHoleArray[dy];
					double minDensity = this.minLimitArray[mainIndex] / 512.0D;
					double maxDensity = this.maxLimitArray[mainIndex] / 512.0D;
					double mainDensity = (this.mainArray[mainIndex] / 10.0D + 1.0D) / 2.0D;

					if(mainDensity < 0.0D) {
						density = minDensity;
					} else if(mainDensity > 1.0D) {
						density = maxDensity;
					} else {
						density = minDensity + (maxDensity - minDensity) * mainDensity;
					}

					density -= densitySubtracted;

					double d34;
					if(dy > ySize - 4) {
						d34 = (double)((float)(dy - (ySize - 4)) / 3.0F);
						density = density * (1.0D - d34) + -10.0D * d34;
					}

					// This will never happen! d19 is always 0!
					if((double)dy < d19) {
						d34 = (d19 - (double)dy) / 4.0D;
						if(d34 < 0.0D) {
							d34 = 0.0D;
						}

						if(d34 > 1.0D) {
							d34 = 1.0D;
						}

						density = density * (1.0D - d34) + -10.0D * d34;
					}

					densityMapArray[mainIndex] = density;
					++mainIndex;
				}
			}
		}

		return densityMapArray;
	}


	public boolean chunkExists(int i1, int i2) {
		return true;
	}

	public void populate(IChunkProvider iChunkProvider1, int i2, int i3) {
		BlockSand.fallInstantly = true;
		int i4 = i2 * 16;
		int i5 = i3 * 16;

		int i6;
		int i7;
		int i8;
		int i9;
		for(i6 = 0; i6 < 8; ++i6) {
			i7 = i4 + this.rand.nextInt(16) + 8;
			i8 = this.rand.nextInt(120) + 4;
			i9 = i5 + this.rand.nextInt(16) + 8;
			(new WorldGenHellLava(Block.lavaMoving.blockID)).generate(this.worldObj, this.rand, i7, i8, i9);
		}

		i6 = this.rand.nextInt(this.rand.nextInt(10) + 1) + 1;

		int i10;
		for(i7 = 0; i7 < i6; ++i7) {
			i8 = i4 + this.rand.nextInt(16) + 8;
			i9 = this.rand.nextInt(120) + 4;
			i10 = i5 + this.rand.nextInt(16) + 8;
			(new WorldGenFire()).generate(this.worldObj, this.rand, i8, i9, i10);
		}

		i6 = this.rand.nextInt(this.rand.nextInt(10) + 1);

		for(i7 = 0; i7 < i6; ++i7) {
			i8 = i4 + this.rand.nextInt(16) + 8;
			i9 = this.rand.nextInt(120) + 4;
			i10 = i5 + this.rand.nextInt(16) + 8;
			(new WorldGenGlowStone1()).generate(this.worldObj, this.rand, i8, i9, i10);
		}

		for(i7 = 0; i7 < 10; ++i7) {
			i8 = i4 + this.rand.nextInt(16) + 8;
			i9 = this.rand.nextInt(128);
			i10 = i5 + this.rand.nextInt(16) + 8;
			(new WorldGenGlowStone2()).generate(this.worldObj, this.rand, i8, i9, i10);
		}

		if(this.rand.nextInt(1) == 0) {
			i7 = i4 + this.rand.nextInt(16) + 8;
			i8 = this.rand.nextInt(128);
			i9 = i5 + this.rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.mushroomBrown.blockID)).generate(this.worldObj, this.rand, i7, i8, i9);
		}

		if(this.rand.nextInt(1) == 0) {
			i7 = i4 + this.rand.nextInt(16) + 8;
			i8 = this.rand.nextInt(128);
			i9 = i5 + this.rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.mushroomRed.blockID)).generate(this.worldObj, this.rand, i7, i8, i9);
		}

		BlockSand.fallInstantly = false;
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
		return "HellRandomLevelSource";
	}
}
