package net.minecraft.game.world.biome;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.noise.NoiseGeneratorPerlin;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityChickenBlack;
import net.minecraft.game.entity.monster.EntityPigZombieVolcanoes;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBigTreeDead;
import net.minecraft.game.world.terrain.generate.WorldGenDesertFlowers;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenMesa extends BiomeGenBase {
	private byte[] clayLayers;
	private long randomSeed;
	private NoiseGeneratorPerlin layerNoiseGen;
	private boolean forested = false;
	
	public BiomeGenMesa(boolean forested) {
		super();
		this.forested = forested;
		
		this.treeBaseAttemptsModifier = -10;
		this.deadBushAttempts = 20;
		this.reedAttempts = 3;
		this.cactusAttempts = 5;
		this.redFlowersAttempts = 0;
		this.yellowFlowersAttempts = 0;
		this.tallGrassAttempts = 1;

		this.weather = Weather.desert;

		this.fillerBlock = (byte) Block.terracotta.blockID;

		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntityChickenBlack.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityPigZombieVolcanoes.class, 30));
				
		this.maxHeight = 1.8F;
		this.minHeight = 0.1F;	
	}
	
	public WorldGenerator getBigTreeGen() {
		return new WorldGenBigTreeDead();
	}
	
	@Override
	public byte getTopBlock(Random rand) {
		return (byte)Block.stainedTerracotta.blockID;
	}
	
	@Override
	public void replaceBlocksForBiome(IChunkProvider generator, World world, Random rand, int chunkX, int chunkZ, int x, int z, byte[] blocks, byte[] metadata, int seaLevel, double sandNoise, double gravelNoise, double stoneNoise) {
		if(this.randomSeed != world.getRandomSeed() || this.clayLayers == null) {
			this.calculateClayLayers(world.getRandomSeed());
		}

		this.randomSeed = world.getRandomSeed();
		
		int xAbs = (chunkX << 4) + x;
		int zAbs = (chunkZ << 4) + z;
		
		int i13 = (int)(stoneNoise / 3.0D + 3.0D + rand.nextDouble() * 0.25D); 
		int i14 = -1;
		boolean dirtOrGrass = Math.cos(stoneNoise / 3.0D * Math.PI) > 0.0D;
		
		byte topBlock = this.getTopBlock(rand);
		byte fillerBlock = this.fillerBlock;
		
		boolean flagWtf = false;

		for(int y = 127; y >= 0; --y) {
			int index = x << 11 | z << 7 | y; 
			
			if(y <= 0 + rand.nextInt(5) && !(generator instanceof ChunkProviderSky)) {
				blocks[index] = (byte)Block.bedrock.blockID;
			} else {
				byte blockID = blocks[index];
				if(blockID == 0) {
					i14 = -1;
				} else if(blockID == Block.stone.blockID) {
					byte terracottaVariant;
					
					if(i14 == -1) {
						if(i13 <= 0) {
							topBlock = 0;
							fillerBlock = (byte)Block.stone.blockID;
						} else if(y >= seaLevel - 4 && y <= seaLevel + 1 && !(generator instanceof ChunkProviderSky)) {
							topBlock = (byte)Block.sand.blockID; //this.getTopBlock(rand);
							fillerBlock = this.fillerBlock;
						}

						if(y < seaLevel - 1 && topBlock == 0 && !(generator instanceof ChunkProviderSky)) {
							topBlock = (byte)Block.waterStill.blockID;
						}

						i14 = i13 + Math.max(0, y - 63);
						
						if(y >= seaLevel - 2 || generator instanceof ChunkProviderSky) {
							if(this.forested && y >= 86 + i13 * 2) {
								if(dirtOrGrass) {
									blocks[index] = (byte)Block.dirtPath.blockID;
								} else {
									blocks[index] = (byte)Block.grass.blockID;
								}
							} else if(y > 66 + i13) {
								terracottaVariant = 16;
								
								if(y >= 64) {
									if(!dirtOrGrass) {
										terracottaVariant = this.getClayLayerMetadata(xAbs, y, zAbs);
									}
								}
								
								if(terracottaVariant < 16) {
									blocks[index] = (byte)Block.stainedTerracotta.blockID;
									metadata[index] = terracottaVariant;
								} else {
									blocks[index] = (byte)Block.terracotta.blockID;
								}
							} else {
								blocks[index] = (byte)Block.sand.blockID; //this.getTopBlock(rand);
								metadata[index] = 1;
								flagWtf = true;
							}
						} else {
							blocks[index] = fillerBlock;
							if(fillerBlock == Block.stainedTerracotta.blockID) {
								metadata[index] = 1;
							}
						}
					} else if(i14 > 0) {
						--i14;
						
						if(flagWtf) {
							blocks[index] = (byte)Block.stainedTerracotta.blockID;
							metadata[index] = 1;
						} else {
							terracottaVariant = this.getClayLayerMetadata(xAbs, y, zAbs);
							
							if(terracottaVariant < 16) {
								blocks[index] = (byte)Block.stainedTerracotta.blockID;
								metadata[index] = terracottaVariant;
							} else {
								blocks[index] = (byte)Block.terracotta.blockID;
							}
						}
					}
				} 
			}
		}
	}

	private void calculateClayLayers(long seed) {
		this.clayLayers = new byte[64];
		Arrays.fill(this.clayLayers, (byte) 16);
		Random rand = new Random(seed);
		this.layerNoiseGen = new NoiseGeneratorPerlin(rand);
		
		// Add meta 1
		for (int layer = 0; layer < 64; ++layer) {
			layer += rand.nextInt(5) + 1;

			if (layer < 64) {
				this.clayLayers[layer] = 1;
			}
		}

		// Add meta 4
		int attempts = rand.nextInt(4) + 2;
		
		for (int i = 0; i < attempts; ++i) {
			int totalLayers = rand.nextInt(3) + 1;
			int baseLayer = rand.nextInt(64);

			for (int layer = 0; baseLayer + layer < 64 && layer < totalLayers; ++layer) {
				this.clayLayers[baseLayer + layer] = 4;
			}
		}

		// Add meta 12
		attempts = rand.nextInt(4) + 2;

		for (int i = 0; i < attempts; ++i) {
			int totalLayers = rand.nextInt(3) + 2;
			int baseLayer = rand.nextInt(64);

			for (int layer = 0; baseLayer + layer < 64 && layer < totalLayers; ++layer) {
				this.clayLayers[baseLayer + layer] = 12;
			}
		}

		// Add meta 14
		attempts = rand.nextInt(4) + 2;

		for (int i = 0; i < attempts; ++i) {
			int totalLayers = rand.nextInt(3) + 1;
			int baseLayer = rand.nextInt(64);

			for (int layer = 0; baseLayer + layer < 64 && layer < totalLayers; ++layer) {
				this.clayLayers[baseLayer + layer] = 14;
			}
		}

		// Add meta 0 / 8
		attempts = rand.nextInt(3) + 3;
		int baseLayer = 0;

		for (int i = 0; i < attempts; ++i) {
			byte var12 = 1;
			baseLayer += rand.nextInt(16) + 4;

			for (int layer = 0; baseLayer + layer < 64 && layer < var12; ++layer) {
				this.clayLayers[baseLayer + layer] = 0;

				if (baseLayer + layer > 1 && rand.nextBoolean()) {
					this.clayLayers[baseLayer + layer - 1] = 8;
				}

				if (baseLayer + layer < 63 && rand.nextBoolean()) {
					this.clayLayers[baseLayer + layer + 1] = 8;
				}
			}
		}
	}

	private byte getClayLayerMetadata(int x, int y, int z) {
		int var4 = (int) Math.round(
				this.layerNoiseGen.generateNoise(
						(double) x * 1.0D / 512.0D, (double) x * 1.0D / 512.0D
				) * 2.0D
			);
		return this.clayLayers[(y + var4 + 64) % 64];
	}
	
	public void populate(World world, Random rand, int x0, int z0) {
		super.populate(world, rand, x0, z0);
		
		int x, y, z, i;
		
		// Generate tall grass
		for(i = 0; i < 32; ++i) {
			x = x0 + rand.nextInt(16) + 8;
			y = rand.nextInt(64) + 64;
			z = z0 + rand.nextInt(16) + 8;
			(new WorldGenDesertFlowers(Block.tallGrass.blockID, 0x20, 0xA0)).generate(world, rand, x, y, z);
		}
	}
}
