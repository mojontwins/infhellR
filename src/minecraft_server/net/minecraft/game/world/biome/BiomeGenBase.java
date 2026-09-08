package net.minecraft.game.world.biome;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import net.minecraft.game.entity.animal.EntityGoat;
import net.minecraft.game.world.terrain.generate.bo3.WorldGenBo3Tree;
import net.minecraft.game.entity.animal.EntityCatBlack;
import net.minecraft.game.entity.animal.EntityCatRed;
import net.minecraft.game.entity.animal.EntityCatSiamese;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.entity.EnumCreatureType;
import net.minecraft.game.entity.SpawnListEntry;
import net.minecraft.game.entity.animal.EntityChicken;
import net.minecraft.game.entity.animal.EntityCow;
import net.minecraft.game.entity.animal.EntityPig;
import net.minecraft.game.entity.animal.EntitySheep;
import net.minecraft.game.entity.human.EntityCowman;
import net.minecraft.game.entity.human.EntityPigman;
import net.minecraft.game.entity.monster.EntityCityHusk;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntityDrowned;
import net.minecraft.game.entity.monster.EntitySkeleton;
import net.minecraft.game.entity.monster.EntitySlime;
import net.minecraft.game.entity.monster.EntitySpider;
import net.minecraft.game.entity.monster.EntityToxicZombie;
import net.minecraft.game.entity.monster.EntityZombie;
import net.minecraft.game.entity.monster.EntityZombieAlex;
import net.minecraft.game.world.terrain.generate.tree.WorldGenBigTree;
import net.minecraft.game.world.terrain.generate.tree.WorldGenTrees;
import net.minecraft.game.world.terrain.generate.WorldGenerator;




public class BiomeGenBase {
	public static final List<BiomeGenBase> biomeList = new ArrayList<BiomeGenBase>();
	
	public static final BiomeGenBase biomeDefault = new BiomeGenBase().setColor(0x18FF00).setBiomeName("Default Alpha");
	public static final BiomeGenBase biomeDesert = new BiomeGenDesert().setColor(0xE8DCB1).setBiomeName("Desert");
	public static final BiomeGenBase biomeDesertOutskirts = new BiomeGenDesertOutskirts().setColor(0xB6AC8A).setBiomeName("Desert outskirts");
	public static final BiomeGenBase biomeRocky = new BiomeGenRocky().setColor(0x8F8F8F).setBiomeName("Rocky");
	public static final BiomeGenBase biomeRockyDesert = new BiomeGenRockyDesert().setColor(0xB4B3B2).setBiomeName("Rocky Desert");
	public static final BiomeGenBase biomeColdRocky = new BiomeGenColdRocky().setColor(0xABB8C2).setBiomeName("Rocky cold");
	public static final BiomeGenBase biomePlains = new BiomeGenPlains().setColor(0x96C566).setBiomeName("Warm plains");
	public static final BiomeGenBase biomeColdPlains = new BiomeGenColdPlains().setColor(0x66C5A5).setBiomeName("Cold plains");
	public static final BiomeGenBase biomeForest = new BiomeGenForest().setColor(0x28D200).setBiomeName("Forest");
	public static final BiomeGenBase biomeHotForest = new BiomeGenHotForest().setColor(0x8AD200).setBiomeName("Hot forest");
	public static final BiomeGenBase biomeThickForest = new BiomeGenThickForest().setColor(0x19A601).setBiomeName("Thick forest");
	public static final BiomeGenBase biomeColdForest = new BiomeGenColdForest().setColor(0x01A666).setBiomeName("Cold forest");
	public static final BiomeGenBase biomeTundra = new BiomeGenTundra().setColor(0x00C679).setBiomeName("Tundra");
	public static final BiomeGenBase biomeSavanna = new BiomeGenSavanna().setColor(0x9ED547).setBiomeName("Savanna");
	public static final BiomeGenBase biomeTaiga = new BiomeGenTaiga().setColor(0x53E9A8).setBiomeName("Taiga");
	public static final BiomeGenBase biomeMesa = new BiomeGenMesa(false).setColor(0xD07C14).setBiomeName("Mesa");
	public static final BiomeGenBase biomeMesaSurroundings = new BiomeGenMesaSurroundings(false).setColor(0xD0B314).setBiomeName("Mesa");
	public static final BiomeGenBase biomeTundraFlat = new BiomeGenTundraFlat().setColor(0x00FFD8).setBiomeName("Tundra");
	public static final BiomeGenBase biomeGlacier = new BiomeGenGlacier().setColor(0x0066FF).setBiomeName("Frozen hell");
	public static final BiomeGenBase biomeTaigaBorder = new BiomeGenTaigaBorder().setColor(0xE4FF00).setBiomeName("Taiga");
	public static final BiomeGenBase biomeDesertWithGrass = new BiomeGenDesertWithGrass().setColor(0xFFF3B7).setBiomeName("Desert");
	public static final BiomeGenBase biomeWillowForest = new BiomeGenWillowForest().setColor(0x167600).setBiomeName("Willow forest");
	
	// This biomes are special and will be generated magically replacing the forest biome in a localized area.
	public static final BiomeGenBase biomeMycelium = new BiomeGenMycelium().setBiomeName("Mycelium");
	public static final BiomeGenBase biomeMangrove = new BiomeGenMangrove().setBiomeName("Mangrove");
	public static final BiomeGenBase biomeDarkForest = new BiomeGenDarkForest().setBiomeName("Dark forest");
	public static final BiomeGenBase biomeDarkForestCold = new BiomeGenDarkForest().setWeather(Weather.cold).setBiomeName("Dark forest");
	public static final BiomeGenBase biomeFlowerFields = new BiomeGenFlowerFields().setBiomeName("Flower Fields");
	public static final BiomeGenBase biomeFlowerFieldsCold = new BiomeGenFlowerFields().setWeather(Weather.cold).setBiomeName("Flower Fields");
	
	// Leave these for compatibility
	public static final BiomeGenBase hell = (new BiomeGenHell()).setColor(16711680).setBiomeName("Hell").setCode(100);
	public static final BiomeGenBase sky = (new BiomeGenSky()).setColor(8421631).setBiomeName("Sky").setCode(200);
	
	public String biomeName = "Default Alpha";
	public int biomeCode = 0;
	public static int currentBiomeCode = 0;
	
	private static BiomeGenBase biomeLookupTable[] = new BiomeGenBase[64*64];
	private static int biomeBuffer[] = new int[64*64];
	
	public byte topBlock = (byte)Block.grass.blockID;
	public byte fillerBlock = (byte)Block.dirt.blockID;
	
	public int biomeColor = 5169201;
	public int color = 5169201;
	protected List<SpawnListEntry> spawnableMonsterList = new ArrayList<SpawnListEntry>();
	protected List<SpawnListEntry> spawnableCreatureList = new ArrayList<SpawnListEntry>();
	protected List<SpawnListEntry> spawnableWaterCreatureList = new ArrayList<SpawnListEntry>();
	private boolean enableSnow;
	
	public WorldGenBo3Tree bo3Tree = new WorldGenBo3Tree();

	protected BiomeGenBase() {
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 8));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombieAlex.class, 2));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 10));
		this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 10));
		
		this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityPig.class, 10));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityChicken.class, 10));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityCow.class, 8));
		
		this.spawnableCreatureList.add(new SpawnListEntry(EntityGoat.class, 4));
		
		// Softlocked for a1.X.X
		// this.spawnableWaterCreatureList.add(new SpawnListEntry(EntitySquid.class, 10));
		this.spawnableWaterCreatureList.add(new SpawnListEntry(EntityDrowned.class, 10));
		
		this.addCityCreatures();
		
		this.biomeCode = BiomeGenBase.currentBiomeCode ++;
		biomeList.add(this);
	}
	
	public void addCityCreatures() {
		// City specific monsters. Add for all biomes. Will only spawn in city chunks.
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCityHusk.class, 15, true));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityToxicZombie.class, 10, true));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityPigman.class, 4, true));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityCowman.class, 4, true));
		
		// City specific mobs. Add for all biomes. Will only spawn in city chunks.
		this.spawnableCreatureList.add(new SpawnListEntry(EntityCatBlack.class, 5, true));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityCatRed.class, 5, true));
		this.spawnableCreatureList.add(new SpawnListEntry(EntityCatSiamese.class, 5, true));

	}
	
	public BiomeGenBase setCode(int code) {
		this.biomeCode = code;
		return this;
	}
	
	/** Lazily-built reverse lookup of biome by {@link #biomeCode}. */
	private static Map<Integer, BiomeGenBase> codeToBiomeMap = null;
	
	/** Returns the biome registered with the given code, or {@link #biomeDefault} if unknown. */
	public static BiomeGenBase getBiomeByCode(int code) {
		if(codeToBiomeMap == null) {
			codeToBiomeMap = new HashMap<Integer, BiomeGenBase>();
			for(BiomeGenBase biome : biomeList) {
				codeToBiomeMap.put(biome.biomeCode, biome);
			}
		}
		BiomeGenBase biome = codeToBiomeMap.get(code);
		return biome == null ? biomeDefault : biome;
	}
	
	public boolean isPermaFrost() {
		return false;
	}
	
	// Biome related values for ChunkProviderGenerate.populate
	public int dungeonAttempts = 8;
	public int clayAttempts = 10;
	public int dirtLumpAttempts = 20;
	public int gravelLumpAttempts = 10;
	public int coalLumpAttempts = 20;
	public int glowLumpAttempts = 10;
	public int ironLumpAttempts = 24;
	public int copperLumpAttempts = 16;
	public int goldLumpAttempts = 2;
	public int goldLumpMaxHeight = 32;
	public int redstoneLumpAttempts = 8;
	public int redstoneLumpMaxHeight = 16;
	public int diamondLumpAttempts = 1;
	public int diamondLumpMaxHeight = 16;
	public int treeBaseAttemptsModifier = 0;
	public int bigTreesEach10Trees = 5;
	public int yellowFlowersAttempts = 4;
	public int redFlowersAttempts = 1;
	public int mushroomBrownChance = 4;
	public int mushroomRedChance = 8;
	public int reedAttempts = 10;
	public int cactusAttempts = 1;
	public int waterFallAttempts = 50;
	public int waterFallMaxHeight = 128;
	public int waterFallMinHeight = 8;
	public int lavaAttempts = 20;
	public int tallGrassAttempts = 0;
	public int pumpkinChance = 32;			// Chance is 1 in N
	public int deadBushAttempts = 0;
	public boolean genBeaches = true;

	public Weather weather = Weather.normal; 
	
	// Set this to a different colour so leaves are tinted differently for this biome.
	// This is an index to an array in BlockLeaves. 7 means "seasonal", i.e. color will change with seasons.
	public int foliageColorizer = 0;
	
	public float maxHeight = 1.0F;
	public float minHeight = 0.0F;
	
	// Unused, as of yet.
	private boolean canSpawnLightningBolt = true;;
	
	public BiomeGenBase setWeather(Weather weather) {
		this.weather = weather;
		return this;
	}

	public WorldGenerator genTreeTryFirst(Random rand) {
		return null;
	}
	
	public WorldGenerator getTreeGen(Random rand) {
		return new WorldGenTrees();
	}
	
	public WorldGenerator getBigTreeGen(Random rand) {
		return new WorldGenBigTree();
	}
	
	public byte getTopBlock(Random rand) {
		return this.topBlock;
	}
	
	// To select themed spawners in dungeons
	public String getPreferedSpawner() {
		return null;
	}
	
	public int getPreferedSpawnerChance() {
		return 1;
	}
	
	public int getPreferedSpawnerChanceOffset() {
		return 1;
	}

	public WorldGenerator getRandomWorldGenForTrees(Random random1) {
		return (WorldGenerator)(random1.nextInt(10) == 0 ? new WorldGenBigTree() : new WorldGenTrees());
	}

	/*
	 * Not used anymore!
	 */
	public static void generateBiomeLookup() {
		for(int t = 0; t < 64; ++t) {
			for(int h = 0; h < 64; ++h) {
				biomeLookupTable[t + h * 64] = getBiome((float)t / 63.0F, (float)h / 63.0F);
			}
		}
		
		// Post process
		for(int t = 0; t < 64; ++t) {
			for(int h = 0; h < 64; ++h) {
				if(
					t > 0 && t < 63 && h > 0 && h < 63 && 
					biomeLookupTable[t + h * 64] != biomeMesa  && (
						biomeLookupTable[(t - 1) + h * 64] == biomeMesa ||
						biomeLookupTable[(t + 1) + h * 64] == biomeMesa ||
						biomeLookupTable[t + (h - 1) * 64] == biomeMesa ||
						biomeLookupTable[t + (h + 1) * 64] == biomeMesa
					)
				) {
					biomeLookupTable[t + h * 64] = biomeMesaSurroundings;
				}
			}
		}
	}

	/*
	 * Not used anymore!
	 */
	public static void writeBiomeLookupToFile(String filename) {
		BufferedImage bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < 64; x ++) {
			for(int y = 0; y < 64; y ++) {
				bi.setRGB(x, y, biomeLookupTable[x + y * 64].color | 0xFF000000);
			}
		}

		Graphics2D ig2 = bi.createGraphics();
		
		Font font = new Font("Monospaced", Font.PLAIN, 9);
		ig2.setFont(font);
		
		for(int i = 0; i < biomeList.size(); i ++) {
			ig2.setColor(new Color(biomeList.get(i).color));
			ig2.fillRect(8, 70 + i * 8, 15, 77 + i * 8);
			ig2.setPaint(Color.black);
			ig2.drawString(biomeList.get(i).biomeName, 24, 78 + i * 8);
		}
		
		try {
			ImageIO.write(bi, "PNG", new File(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setBuffer(int[] buffer) {
		biomeBuffer = buffer;
	}
	
	public static void loadBiomeLookup() {
		for(int x = 0; x < 64; x ++) {
			for(int y = 0; y < 64; y ++) {
				int color = biomeBuffer[x + y * 64] & 0xFFFFFF;
				for(int i = 0; i < biomeList.size(); i ++) {
					BiomeGenBase biome = biomeList.get(i);
					if(biome.color == color) {
						biomeLookupTable[x + y * 64] = biome;
						break;
					}
				}
			}
		}
	}
	
	/*
	 * Not used anymore!
	 */
	public static BiomeGenBase getBiome(float temperature, float humidity) {
		humidity *= temperature;
		
		if(humidity > 0.75F) return biomeHotForest;
		
		if(humidity > 0.5F) {
			if(temperature >= 0.95F) {
				return biomeSavanna;
			} else if(temperature >= 0.9F) {
				return biomeForest;
			} else {
				return biomeThickForest;
			}
		}
		
		if(temperature >= 0.95) {
			if(humidity > 0.4F) {
				return biomeSavanna;
			} else if(humidity > 0.2F) {
				return biomeDesertOutskirts;
			} else {
				return biomeDesert;
			}
		}
		
		if(temperature >= 0.7F) {
			if(humidity > 0.4F) {
				return biomeForest;
			} else if(humidity > 0.2F) {
				if(temperature > 0.9F) {
					return biomeSavanna;
				} else if(temperature - 0.7F > humidity - 0.2F) {
					return biomePlains;
				} else {
					return biomeForest;					
				}
			} else if(humidity > 0.125F) {
				if(temperature >= 0.91F) {
					return biomeSavanna;
				} else {
					return biomeMesa; // was biomeRockyDesert;
				} 
			} else {
				if(temperature > 0.9F) {
					return biomeDesertOutskirts;
				} else if(temperature > 0.8F) {
					return biomeRockyDesert;
				} else {
					return biomeMesa; // was biomeRocky;
				}
			}
		}
		
		if(temperature >= 0.69F && humidity >= 0.125F && humidity < 0.2F) {
			return biomeMesaSurroundings;
		}
		
		if(temperature >= 0.6F) {
			if(humidity > 0.25F) {
				return biomeForest;
			} else if(humidity > 0.075F) {
				return biomePlains;
			} else {
				return biomeRocky;
			}
		}
		
		if(humidity > 0.2F) {
			return biomeColdForest;
		} else if(humidity > 0.1F) {
			if(temperature > 0.55F) {
				return biomeColdPlains;
			} else {
				return biomeTaiga;
			}
		} else {
			if(temperature >= 0.5F) {
				return biomeColdRocky;
			} else if(temperature >= 0.4F) {
				return biomeColdPlains;
			} else {
				return biomeTundra;
			}
		}
	}

	protected BiomeGenBase setEnableSnow() {
		this.enableSnow = true;
		return this;
	}

	protected BiomeGenBase setBiomeName(String string1) {
		this.biomeName = string1;
		return this;
	}

	protected BiomeGenBase setBiomeColor(int i1) {
		this.biomeColor = i1;
		return this;
	}

	protected BiomeGenBase setColor(int i1) {
		this.color = i1;
		return this;
	}

	public static BiomeGenBase getBiomeFromLookup(double temperature, double humidity, double zone) {
		int var4 = (int)(temperature * 63.0D);
		int var5 = (int)(humidity * 63.0D);
		return biomeLookupTable[var4 + var5 * 64];
	}
	
	// Called during generation
	public void generate(Random rand, int y0, Chunk chunk) {
	}
	
	// Called before standard population (i.e. gen lakes etc).
	public void prePopulate(World world, Random rand, int x0, int z0) {
	}
	
	// Called after standard population
	public void populate(World world, Random rand, int x0, int z0) {
	}

	public int getSkyColorByTemp(float f1) {
		f1 /= 3.0F;
		if(f1 < -1.0F) {
			f1 = -1.0F;
		}

		if(f1 > 1.0F) {
			f1 = 1.0F;
		}

		return Color.getHSBColor(0.62222224F - f1 * 0.05F, 0.5F + f1 * 0.1F, 1.0F).getRGB();
	}

	public List<SpawnListEntry> getSpawnableList(EnumCreatureType enumCreatureType1) {
		return enumCreatureType1 == EnumCreatureType.monster ? this.spawnableMonsterList : (enumCreatureType1 == EnumCreatureType.creature ? this.spawnableCreatureList : (enumCreatureType1 == EnumCreatureType.waterCreature ? this.spawnableWaterCreatureList : null));
	}

	public boolean getEnableSnow() {
		return this.enableSnow;
	}

	public boolean canSpawnLightningBolt() {
		return this.canSpawnLightningBolt ;
	}
	
	public void replaceBlocksForBiome(IChunkProvider generator, World world, Random rand, int chunkX, int chunkZ, int x, int z, 
		byte[] blocks, byte[] metadata, int seaLevel, double sandNoise, double gravelNoise, double stoneNoise) {
		boolean generateSand = sandNoise + rand.nextDouble() * 0.2D > 0.0D;
		boolean generateGravel = gravelNoise + rand.nextDouble() * 0.2D > 3.0D;
		int height = (int)(stoneNoise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int monolithNoise = (int)(4D * (stoneNoise / 3.0D + 3.0D));

		int stoneHeight = -1;
		byte topBlock = this.getTopBlock(rand);
		byte fillerBlock = this.fillerBlock;

		for(int y = 127; y >= 0; --y) {
			int index = x << 11 | z << 7 | y; // (x * 16 + z) * 128 + y
			
			if (y > 127 - monolithNoise) {
				blocks[index] = (byte)0;
				stoneHeight = -1;
			} else if(y <= 0 + rand.nextInt(5) && !(generator instanceof ChunkProviderSky)) {
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
							fillerBlock = (byte)Block.stone.blockID;
						} else if(y >= seaLevel - 4 && y <= seaLevel + 1 && !(generator instanceof ChunkProviderSky)) {
							topBlock = this.topBlock;
							fillerBlock = this.fillerBlock;
							if(generateGravel) {
								topBlock = 0;
							}
							
							if(this.genBeaches) {
								if(generateGravel) {
									fillerBlock = (byte)Block.gravel.blockID;
								}

								if(generateSand) {
									topBlock = (byte)Block.sand.blockID;
									fillerBlock = (byte)Block.sand.blockID;
								}
							}
						}

						if(y < seaLevel && topBlock == 0 && !(generator instanceof ChunkProviderSky)) {
							if(this.weather == Weather.cold) {
								topBlock = (byte)Block.ice.blockID;
							} else {
								topBlock = (byte)Block.waterStill.blockID;
							}
						}

						stoneHeight = height;
						if(y >= seaLevel - 1 || generator instanceof ChunkProviderSky) {
							blocks[index] = topBlock;
						} else {
							blocks[index] = fillerBlock;
						}
					} else if(stoneHeight > 0) {
						--stoneHeight;
						blocks[index] = fillerBlock;
						if(stoneHeight == 0 && fillerBlock == this.sandstoneGenTriggerer()) {
							stoneHeight = rand.nextInt(4);
							fillerBlock = this.sandstoneGenBlock();
						}
					}
				} else if(blockID == Block.waterStill.blockID && y == seaLevel - 1 && this.weather == Weather.cold) {
					blocks[index] = (byte)Block.ice.blockID;
				}
			}
		}
	}
	
	public byte sandstoneGenTriggerer() {
		return (byte)Block.sand.blockID;
	}
	
	public byte sandstoneGenBlock() {
		return (byte)Block.sandStone.blockID;
	}

	public boolean isHumid() {
		return false;
	}
}
