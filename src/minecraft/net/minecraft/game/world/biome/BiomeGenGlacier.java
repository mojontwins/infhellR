package net.minecraft.game.world.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.terrain.noise.PerlinNoiseGenerator;
import net.minecraft.game.world.terrain.noise.SkippableRandom;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityColdCow;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityIceSkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.world.terrain.generate.WorldGenIceSpike;
import net.minecraft.game.world.terrain.generate.WorldGenIgloos;
import net.minecraft.game.world.terrain.generate.WorldGenMinable;

public class BiomeGenGlacier extends BiomeGenBase {
	private static final byte PACKED_ICE = (byte) Block.packedIce.blockID;
	private static final byte SNOW_BLOCK = (byte) Block.blockSnow.blockID;
	private static final byte AIR = (byte) 0;
	private static final byte GRAVEL = (byte) Block.gravel.blockID;
	private static final byte ICE = (byte) Block.ice.blockID;
	private static final byte WATER = (byte) Block.waterStill.blockID;
	private static final List<Integer> octaves1 = new ArrayList<Integer>();
	private static final List<Integer> octaves2 = new ArrayList<Integer>();

	private long randomSeed;

	private PerlinNoiseGenerator noiseGen1;
	private PerlinNoiseGenerator noiseGen2;

	public BiomeGenGlacier() {
		this.topBlock = (byte) Block.blockSnow.blockID;
		this.fillerBlock = (byte) Block.ice.blockID;

		this.weather = Weather.cold;

		this.minHeight = -0.5F;
		this.maxHeight = 0.5F;

		this.spawnableCreatureList.clear();
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityColdCow.class, 8));

		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 5));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombieAlex.class, 5));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityIceSkeleton.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));

		this.addCityCreatures();
	}

	public boolean isPermaFrost() {
		return true;
	}

	public byte sandstoneGenTriggerer() {
		return (byte) Block.ice.blockID;
	}

	public byte sandstoneGenBlock() {
		return (byte) Block.packedIce.blockID;
	}

	public void replaceBlocksForBiome(IChunkProvider generator, World world, Random rand, int chunkX, int chunkZ, int x,
			int z, byte[] blocks, byte[] metadata, int seaLevel, double sandNoise, double gravelNoise,
			double stoneNoise) {
		byte defaultBlock = (byte) Block.stone.blockID;
		boolean generateIce = gravelNoise + rand.nextDouble() * 0.2D > 2.5D;

		int xAbs = chunkX << 4 | x;
		int zAbs = chunkZ << 4 | z;

		if (this.randomSeed != world.getRandomSeed() || this.noiseGen1 == null) {
			this.randomSeed = world.getRandomSeed();
			SkippableRandom randIce = new SkippableRandom(this.randomSeed);
			this.noiseGen1 = new PerlinNoiseGenerator(randIce, octaves1);
			this.noiseGen2 = new PerlinNoiseGenerator(randIce, octaves2);
		}

		double packedIceTop = 0.0D;
		double packedIceBottom = 0.0D;

		double density = Math.min(Math.abs(stoneNoise),
				this.noiseGen1.noiseAt((double) xAbs * 0.1D, (double) zAbs * 0.1D, false) * 15.0D);

		if (density > 1.8D) {
			double d4 = Math
					.abs(this.noiseGen2.noiseAt((double) xAbs * 0.09765625D, (double) zAbs * 0.09765625D, false));
			packedIceTop = density * density * 1.2D;
			double d5 = Math.ceil(d4 * 40.0D) + 14.0D;

			if (packedIceTop > d5) {
				packedIceTop = d5;
			}

			if (packedIceTop > 2.0D) {
				packedIceBottom = (double) seaLevel - packedIceTop - 7.0D;
				packedIceTop = packedIceTop + (double) seaLevel;
			} else {
				packedIceTop = 0.0D;
			}
		}

		byte biomeFillBlock = this.fillerBlock;
		byte biomeTopBlock = this.topBlock;

		byte fillBlock = biomeFillBlock;
		byte topBlock = biomeTopBlock;

		int height = (int) (stoneNoise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int stoneHeight = -1;

		int snowCounter = 0;
		int snowMax = 2 + rand.nextInt(4);
		int minSnowHeight = seaLevel + 18 + rand.nextInt(10);

		for (int y = 127; y >= 0; --y) {
			int index = x << 11 | z << 7 | y;

			if (y <= 0 + rand.nextInt(5) && !(generator instanceof ChunkProviderSky)) {
				blocks[index] = (byte) Block.bedrock.blockID;
			} else {
				byte blockID = blocks[index];

				if (blockID == AIR && y < (int) packedIceTop && rand.nextDouble() > 0.01D) {
					blockID = blocks[index] = PACKED_ICE;
				} else if (blockID == WATER && y > (int) packedIceBottom && y < seaLevel && packedIceBottom != 0.0D
						&& rand.nextDouble() > 0.15D) {
					blockID = blocks[index] = PACKED_ICE;
				}

				if (blockID == AIR) {
					stoneHeight = -1;
				} else if (blockID != defaultBlock) {
					if (blockID == PACKED_ICE && snowCounter <= snowMax && y > minSnowHeight) {
						blocks[index] = SNOW_BLOCK;
						++snowCounter;
					} else if (blockID == WATER && y == seaLevel - 1) {
						if (generateIce) {
							blocks[index] = ICE;
						}
					}
				} else if (stoneHeight == -1) {
					if (height <= 0) {
						topBlock = AIR;
						fillBlock = defaultBlock;
					} else if (y >= seaLevel - 4 && y <= seaLevel + 1) {
						topBlock = biomeTopBlock;
						fillBlock = biomeFillBlock;
					}

					if (y < seaLevel && topBlock == AIR) {
						topBlock = ICE;
					}

					stoneHeight = height;

					if (y >= seaLevel - 1) {
						blocks[index] = topBlock;
					} else if (y < seaLevel - 7 - height) {
						topBlock = AIR;
						fillBlock = defaultBlock;
						blocks[index] = GRAVEL;
					} else {
						blocks[index] = fillBlock;
					}
				} else if (stoneHeight > 0) {
					--stoneHeight;
					blocks[index] = fillBlock;
				}
			}
		}
	}

	public void populate(World world, Random rand, int chunkX, int chunkZ) {
		int x, y, z;

		// Generate IceSpike
		for (int i = 0; i < 8; i++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			WorldGenIceSpike worldgenIceSpike = new WorldGenIceSpike();
			worldgenIceSpike.generate(world, rand, x, world.getHeightValue(x, z) + 1, z);
		}

		// Generate igloos
		if (rand.nextInt(64) == 0) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			WorldGenIgloos worldgenIgloos = new WorldGenIgloos();
			worldgenIgloos.generate(world, rand, x, world.getHeightValue(x, z), z);
		}

		// Generate snow pools
		for (int i = 0; i < 20; ++i) {
			x = chunkX + rand.nextInt(16);
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16);
			(new WorldGenMinable(Block.blockSnow.blockID, 32)).generate(world, rand, x, y, z);
		}
	}

	static {
		for (int i = -3; i <= 0; i++) {
			octaves1.add(i);
		}

		octaves2.add(0);
	}
}
