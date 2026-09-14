package net.minecraft.game.world.biome;

import java.util.Random;

import net.minecraft.game.entity.animal.EntityBetaOcelot;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.game.world.terrain.generate.WorldGenFlowers;
import net.minecraft.game.world.terrain.generate.tree.EnumTreeType;
import net.minecraft.game.world.terrain.generate.tree.WorldGenCypress;
import net.minecraft.game.world.terrain.generate.tree.WorldGenFir;
import net.minecraft.game.world.terrain.generate.tree.WorldGenHugeTrees;
import net.minecraft.game.world.terrain.generate.WorldGenLilypad;
import net.minecraft.game.world.terrain.generate.tree.WorldGenPineTree;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.WorldGenVines;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenThickForest extends BiomeGenForest {
	public BiomeGenThickForest() {
		super();
		this.bigTreesEach10Trees = 5;
		this.treeBaseAttemptsModifier = 7;
		this.tallGrassAttempts = 128;
		this.redFlowersAttempts = 16;
		this.yellowFlowersAttempts = 24;
		this.copperLumpAttempts = 4;
		
		this.spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 2));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityBetaOcelot.class, 5, true));
	}
	
	public int getAlgaeAmount() {
		return 96;
	}
	
	public int getCoralAmount() {
		return 64;
	}
	
	public int getNetherVinesPerChunk() {
		return 64;
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		if(rand.nextInt(100) == 0) {
			if(rand.nextBoolean()) {
				this.bo3Tree.setTreeName("ancientjungle:shroomjunglemed" + (1 + rand.nextInt(2)));
			} else {
				this.bo3Tree.setTreeName("ancientjungle:shroomjunglesmall1");
			}
			this.bo3Tree.withLeavesMeta(EnumTreeType.FANCYFIR.getLeafMetadata());
			return this.bo3Tree;
		} else if(rand.nextInt(3) != 0) {
			this.bo3Tree.setTreeName("Spruce:SpruceFirSmall" + (1 + rand.nextInt(15)));
			this.bo3Tree.withLeavesMeta(EnumTreeType.FANCYFIR.getLeafMetadata());
			return this.bo3Tree;
		} else if(rand.nextBoolean()) {
			return new WorldGenTrees();
		} else {
			//return new WorldGenFir(3+rand.nextInt(3), false);
			return new WorldGenFir(3 + rand.nextInt(3), false);
		}
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		if(rand.nextInt(100) == 0) {
			this.bo3Tree.setTreeName("ancientjungle:shroomjunglelarge" + (1 + rand.nextInt(3)));
			this.bo3Tree.withLeavesMeta(EnumTreeType.FANCYFIR.getLeafMetadata());
			return this.bo3Tree;
		} else if(rand.nextInt(64) == 0) {
			return new WorldGenHugeTrees(16 + rand.nextInt(16));
		} else if(rand.nextInt(3) != 0) {
			this.bo3Tree.setTreeName("Spruce:SpruceFir" + (1 + rand.nextInt(21)));
			this.bo3Tree.withLeavesMeta(EnumTreeType.FANCYFIR.getLeafMetadata());
			return this.bo3Tree;
		} else if(rand.nextBoolean()) {
			return new WorldGenPineTree(5 + rand.nextInt(5), true); // WorldGenFir(5+rand.nextInt(5), true);
		} else {
			return new WorldGenCypress(5+rand.nextInt(5));
		}
	}
	
	public void prePopulate(World world, Random rand, int x0, int z0) {
		super.prePopulate(world, rand, x0, z0);
	}
	
	public void populate(World world, Random rand, int chunkX, int chunkZ) {	
		super.populate(world, rand, chunkX, chunkZ);
		int x, y, z;
		
		// Generate vines
		for(int i = 0; i < 120; i++) {
			x = chunkX + rand.nextInt(16) + 8;
			y = 32;
			z = chunkZ + rand.nextInt(16) + 8;
			
			(new WorldGenVines()).generate(world, rand, x, y, z);
		}
		
		// Generate Lilypads
		for(int i = 0; i < 8; i ++) {
			x = chunkX + rand.nextInt(16) + 8;
			z = chunkZ + rand.nextInt(16) + 8;
			
			for(y = rand.nextInt(128); y > 0 && world.getBlockId(x, y - 1, z) == 0; y --) {}
			
			(new WorldGenLilypad()).generate(world, rand, x, y, z);
		}
		
		// Paeonias
		for(int i = 0; i < 12; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.paeonia.blockID)).generate(world, rand, x, y, z);
		}
		
		// Blue Flowers
		for(int i = 0; i < 10; ++i) {
			x = chunkX + rand.nextInt(16) + 8;
			y = rand.nextInt(128);
			z = chunkZ + rand.nextInt(16) + 8;
			(new WorldGenFlowers(Block.blueFlower.blockID)).generate(world, rand, x, y, z);
		}
	}
	
	@Override
	public void replaceBlocksForBiome(IChunkProvider generator, World world, Random rand, int chunkX, int chunkZ, int x, int z, byte[] blocks, byte[] metadata, int seaLevel, double sandNoise, double gravelNoise, double stoneNoise) {
		// This version uses meta 1 for grass
		
		boolean generateSand = sandNoise + rand.nextDouble() * 0.2D > 0.0D;
		boolean generateGravel = gravelNoise + rand.nextDouble() * 0.2D > 3.0D;
		int height = (int)(stoneNoise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);

		int stoneHeight = -1;
		byte topBlock = (byte) Block.grass.blockID;
		byte topMeta = (byte) (rand.nextBoolean() ? 1 : 0);
		byte fillerBlock = this.fillerBlock;

		int index = x << 11 | z << 7 | 127;
		boolean firstFiller = false;
		
		for(int y = 127; y >= 0; --y) {
			if(y <= 0 + rand.nextInt(5) && !(generator instanceof ChunkProviderSky)) {
				blocks[index] = (byte)Block.bedrock.blockID;
			} else {
				byte blockID = blocks[index];
				byte meta = metadata[index];
				if(blockID == 0) {
					stoneHeight = -1;
				} else if(blockID == Block.stone.blockID && meta == 0) {
					if(stoneHeight == -1) {
						if(height <= 0) {
							topBlock = 0;
							topMeta = 0;
							fillerBlock = (byte)Block.stone.blockID;
						} else if(y >= seaLevel - 4 && y <= seaLevel + 1 && !(generator instanceof ChunkProviderSky)) {
							topBlock = (byte) Block.grass.blockID;
							topMeta = 1;
							fillerBlock = this.fillerBlock;
							if(generateGravel) {
								topBlock = 0;
								topMeta = 0;
							}
							
							if(this.genBeaches) {
								if(generateGravel) {
									fillerBlock = (byte)Block.gravel.blockID;
								}

								if(generateSand) {
									topBlock = (byte)Block.sand.blockID;
									topMeta = 0;
									fillerBlock = (byte)Block.sand.blockID;
								}
							}
						}

						if(y < seaLevel && topBlock == 0 && !(generator instanceof ChunkProviderSky)) {
							if(this.weather == Weather.cold) {
								topBlock = (byte)Block.ice.blockID;
								topMeta = 0;
							} else {
								topBlock = (byte)Block.waterStill.blockID;
								topMeta = 0;
							}
						}

						stoneHeight = height;
						
						if(y >= seaLevel - 1 || generator instanceof ChunkProviderSky) {
							blocks[index] = topBlock;
							metadata[index] = topMeta;
						} else {
							blocks[index] = fillerBlock;
						}
						
						firstFiller = true;
					} else if(stoneHeight > 0) {
						--stoneHeight;
						if(firstFiller) {
							if (y < 127 && metadata[index + 1] == 1) {
								blocks[index] = (byte) Block.grass.blockID;
							} else {
								blocks[index] = (byte) Block.dirt.blockID;
							}
							firstFiller = false;
						} else {
							blocks[index] = fillerBlock;
						}
						
						if(stoneHeight == 0 && fillerBlock == this.sandstoneGenTriggerer()) {
							stoneHeight = rand.nextInt(4);
							fillerBlock = this.sandstoneGenBlock();
						}
					}
				} else if(blockID == Block.waterStill.blockID && y == seaLevel - 1 && this.weather == Weather.cold) {
					blocks[index] = (byte)Block.ice.blockID;
				}
			}
			
			index --;
		}
	}
	
	@Override
	public boolean isHumid() {
		return true;
	}
}
