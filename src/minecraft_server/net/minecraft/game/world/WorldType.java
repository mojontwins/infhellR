package net.minecraft.game.world;

import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.ChunkProviderGenerate;

public class WorldType {
	public static final WorldType[] worldTypes = new WorldType[16];
	public static final WorldType DEFAULT = (new WorldType(0, "default", 1)).setVersioned();
	public static final WorldType FLAT = (new WorldType(1, "flat")).setCanBeCreated(false);
	public static final WorldType SKY = (new WorldType(3, "sky", 1)).setVersioned();
	public static final WorldType COLD = (new WorldType(2, "cold", 1)).setVersioned();

	private final String worldType;
	private final int generatorVersion;
	private boolean canBeCreated;
	private boolean versioned;

	protected WorldType(int par1, String par2Str) {
		this(par1, par2Str, 0);
	}

	protected WorldType(int par1, String par2Str, int par3) {
		this.worldType = par2Str;
		this.generatorVersion = par3;
		this.canBeCreated = true;
		worldTypes[par1] = this;
	}

	public String toString() {
		return this.worldType;
	}

	public String getTranslateName() {
		return "generator." + this.worldType;
	}

	public int getGeneratorVersion() {
		return this.generatorVersion;
	}

	private WorldType setCanBeCreated(boolean par1) {
		this.canBeCreated = par1;
		return this;
	}

	public boolean getCanBeCreated() {
		return this.canBeCreated;
	}

	private WorldType setVersioned() {
		this.versioned = true;
		return this;
	}

	public boolean isVersioned() {
		return this.versioned;
	}

	public static WorldType parseWorldType(String par0Str) {
		for(int var1 = 0; var1 < worldTypes.length; ++var1) {
			if(worldTypes[var1] != null && worldTypes[var1].worldType.equalsIgnoreCase(par0Str)) {
				return worldTypes[var1];
			}
		}

		return null;
	}

	public WorldChunkManager getChunkManager(World var1) {
		return (WorldChunkManager)(this == SKY ? new WorldChunkManager(var1) : new WorldChunkManager(var1));
	}

	public IChunkProvider getChunkGenerator(World var1) {
		return (IChunkProvider)(this == SKY ? 
				new ChunkProviderSky(var1, var1.getRandomSeed(), var1.getWorldInfo().isMapFeaturesEnabled(), var1.getWorldInfo().getGenerateCities()) 
			: 
				new ChunkProviderGenerate(var1, var1.getRandomSeed(), var1.getWorldInfo().isMapFeaturesEnabled(), var1.getWorldInfo().getGenerateCities())
		);
	}

	public int getSeaLevel(World var1) {
		return this.getMinimumSpawnHeight(var1);
	}

	public int getMinimumSpawnHeight(World world) {
		return this == FLAT ? 4 : 64;
	}

	public double getHorizon(World world) {
		return this == FLAT ? 0.0D : 63.0D;
	}

	public boolean hasVoidParticles(boolean var1) {
		return this != FLAT && !var1;
	}

	public double voidFadeMagnitude() {
		return this == FLAT ? 1.0D : 8.0D / 256D;
	}

	public void onGUICreateWorldPress() {
	}
}
