package net.minecraft.game.world;

import java.util.List;

import net.minecraft.game.command.CommandProcessor;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.Seasons;

public class WorldInfo {
	private long randomSeed;
	private WorldType terrainType = WorldType.DEFAULT;
	private int spawnX;
	private int spawnY;
	private int spawnZ;
	private long worldTime;
	private long lastTimePlayed;
	private long sizeOnDisk;
	private NBTTagCompound playerTag;
	private int dimension;
	private String levelName;
	private int saveVersion;
	
	private boolean raining;
	private int rainTime;
	
	private boolean thundering;
	private int thunderTime;

	private boolean snowing;
	private int snowingTime;
	
	private boolean mapFeaturesEnabled;
	private boolean generateCities;
	
	private boolean bloodMoon;
	private boolean meltBuild;
	
	private boolean cold;
	
	private float cityChance = 0.0F;
	
	public WorldInfo(NBTTagCompound nbt) {
		this.randomSeed = nbt.getLong("RandomSeed");
		if(nbt.hasKey("generatorName")) {
			String generatorName = nbt.getString("generatorName");
			this.terrainType = WorldType.parseWorldType(generatorName);
			if(this.terrainType == null) {
				this.terrainType = WorldType.DEFAULT;
			} 
		}

		if(nbt.hasKey("MapFeatures")) {
			this.mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
		} else {
			this.mapFeaturesEnabled = true;
		}
		
		if(nbt.hasKey("GenerateCities")) {
			this.generateCities = nbt.getBoolean("GenerateCities");
		} else {
			this.generateCities = true;
		}

		this.spawnX = nbt.getInteger("SpawnX");
		this.spawnY = nbt.getInteger("SpawnY");
		this.spawnZ = nbt.getInteger("SpawnZ");
		this.worldTime = nbt.getLong("Time");
		this.lastTimePlayed = nbt.getLong("LastPlayed");
		this.sizeOnDisk = nbt.getLong("SizeOnDisk");
		this.levelName = nbt.getString("LevelName");
		this.saveVersion = nbt.getInteger("version");
		this.rainTime = nbt.getInteger("rainTime");
		this.raining = nbt.getBoolean("raining");
		this.thunderTime = nbt.getInteger("thunderTime");
		this.thundering = nbt.getBoolean("thundering");
		this.snowingTime = nbt.getInteger("snowingTime");
		this.snowing = nbt.getBoolean("snowing");
		this.bloodMoon = nbt.getBoolean("BloodMoon"); 
		Seasons.dayOfTheYear = nbt.getInteger("DayOfTheYear");
		if(nbt.hasKey("Player")) {
			this.playerTag = nbt.getCompoundTag("Player");
			this.dimension = this.playerTag.getInteger("Dimension");
		}
		this.cold = nbt.getBoolean("Cold");
		this.cityChance = nbt.getFloat("CityChance");

		CommandProcessor.readFromNBT(nbt);
	}

	public WorldInfo(WorldSettings settings, String levelName) {
		this.randomSeed = settings.getSeed();
		this.mapFeaturesEnabled = settings.isMapFeaturesEnabled();
		this.generateCities = settings.isGenerateCities();
		this.levelName = levelName;
		this.terrainType = settings.getTerrainType();
		if(this.terrainType == WorldType.SKY) this.dimension = 1;
		if(this.terrainType == WorldType.COLD) this.cold = true; 
		this.cityChance = settings.getCityChance();
	}

	public WorldInfo(WorldInfo worldInfo) {
		this.randomSeed = worldInfo.randomSeed;
		this.mapFeaturesEnabled = worldInfo.mapFeaturesEnabled;
		this.generateCities = worldInfo.generateCities;
		this.spawnX = worldInfo.spawnX;
		this.spawnY = worldInfo.spawnY;
		this.spawnZ = worldInfo.spawnZ;
		this.worldTime = worldInfo.worldTime;
		this.lastTimePlayed = worldInfo.lastTimePlayed;
		this.sizeOnDisk = worldInfo.sizeOnDisk;
		this.playerTag = worldInfo.playerTag;
		this.dimension = worldInfo.dimension;
		this.levelName = worldInfo.levelName;
		this.saveVersion = worldInfo.saveVersion;
		this.rainTime = worldInfo.rainTime;
		this.raining = worldInfo.raining;
		this.thunderTime = worldInfo.thunderTime;
		this.thundering = worldInfo.thundering;
		this.snowingTime = worldInfo.snowingTime;
		this.snowing = worldInfo.snowing;
		this.bloodMoon = worldInfo.bloodMoon;
		this.cold = worldInfo.cold;
		this.cityChance = worldInfo.cityChance;
	}

	public NBTTagCompound getNBTTagCompound() {
		NBTTagCompound nBTTagCompound1 = new NBTTagCompound();
		this.updateTagCompound(nBTTagCompound1, this.playerTag);
		return nBTTagCompound1;
	}

	public NBTTagCompound getNBTTagCompoundWithPlayer(List<EntityPlayer> list1) {
		NBTTagCompound nBTTagCompound2 = new NBTTagCompound();
		EntityPlayer entityPlayer3 = null;
		NBTTagCompound nBTTagCompound4 = null;
		if(list1.size() > 0) {
			entityPlayer3 = (EntityPlayer)list1.get(0);
		}

		if(entityPlayer3 != null) {
			nBTTagCompound4 = new NBTTagCompound();
			entityPlayer3.writeToNBT(nBTTagCompound4);
		}

		this.updateTagCompound(nBTTagCompound2, nBTTagCompound4);
		return nBTTagCompound2;
	}

	private void updateTagCompound(NBTTagCompound worldNBT, NBTTagCompound playerNBT) {
		worldNBT.setLong("RandomSeed", this.randomSeed);
		worldNBT.setString("generatorName", this.terrainType.toString());
		worldNBT.setInteger("generatorVersion", this.terrainType.getGeneratorVersion());
		worldNBT.setBoolean("MapFeatures", this.mapFeaturesEnabled);
		worldNBT.setBoolean("GenerateCities", this.generateCities);
		worldNBT.setInteger("SpawnX", this.spawnX);
		worldNBT.setInteger("SpawnY", this.spawnY);
		worldNBT.setInteger("SpawnZ", this.spawnZ);
		worldNBT.setLong("Time", this.worldTime);
		worldNBT.setLong("SizeOnDisk", this.sizeOnDisk);
		worldNBT.setLong("LastPlayed", System.currentTimeMillis());
		worldNBT.setString("LevelName", this.levelName);
		worldNBT.setInteger("version", this.saveVersion);
		worldNBT.setInteger("rainTime", this.rainTime);
		worldNBT.setBoolean("raining", this.raining);
		worldNBT.setInteger("thunderTime", this.thunderTime);
		worldNBT.setBoolean("thundering", this.thundering);
		worldNBT.setInteger("snowingTime", this.snowingTime);
		worldNBT.setBoolean("snowing", this.snowing);
		worldNBT.setBoolean("BloodMoon", this.bloodMoon);
		worldNBT.setInteger("DayOfTheYear", Seasons.dayOfTheYear);
		if(playerNBT != null) {
			worldNBT.setCompoundTag("Player", playerNBT);
		}
		worldNBT.setBoolean("Cold", cold);
		worldNBT.setFloat("CityChance", cityChance);
		
		CommandProcessor.writeToNBT(worldNBT);
	}

	public long getRandomSeed() {
		return this.randomSeed;
	}

	public int getSpawnX() {
		return this.spawnX;
	}

	public int getSpawnY() {
		return this.spawnY;
	}

	public int getSpawnZ() {
		return this.spawnZ;
	}

	public long getWorldTime() {
		return this.worldTime;
	}

	public long getSizeOnDisk() {
		return this.sizeOnDisk;
	}

	public NBTTagCompound getPlayerNBTTagCompound() {
		return this.playerTag;
	}

	public int getDimension() {
		return this.dimension;
	}

	public void setSpawnX(int i1) {
		this.spawnX = i1;
	}

	public void setSpawnY(int i1) {
		this.spawnY = i1;
	}

	public void setSpawnZ(int i1) {
		this.spawnZ = i1;
	}

	public void setWorldTime(long j1) {
		this.worldTime = j1;
	}

	public void setSizeOnDisk(long j1) {
		this.sizeOnDisk = j1;
	}

	public void setPlayerNBTTagCompound(NBTTagCompound nBTTagCompound1) {
		this.playerTag = nBTTagCompound1;
	}

	public void setSpawn(int i1, int i2, int i3) {
		this.spawnX = i1;
		this.spawnY = i2;
		this.spawnZ = i3;
	}

	public String getWorldName() {
		return this.levelName;
	}

	public void setWorldName(String string1) {
		this.levelName = string1;
	}

	public int getSaveVersion() {
		return this.saveVersion;
	}

	public void setSaveVersion(int i1) {
		this.saveVersion = i1;
	}

	public long getLastTimePlayed() {
		return this.lastTimePlayed;
	}

	public boolean getThundering() {
		return this.thundering;
	}

	public void setThundering(boolean z1) {
		this.thundering = z1;
	}

	public int getThunderTime() {
		return this.thunderTime;
	}

	public void setThunderTime(int i1) {
		this.thunderTime = i1;
	}

	public boolean getRaining() {
		return this.raining;
	}

	public void setRaining(boolean z1) {
		this.raining = z1;
	}

	public int getRainTime() {
		return this.rainTime;
	}

	public void setRainTime(int i1) {
		this.rainTime = i1;
	}
	
	public boolean getSnowing() {
		return this.snowing;
	}

	public void setSnowing(boolean z1) {
		this.snowing = z1;
	}

	public int getSnowingTime() {
		return this.snowingTime;
	}

	public void setSnowingTime(int i1) {
		this.snowingTime = i1;
	}

	public boolean isMapFeaturesEnabled() {
		return this.mapFeaturesEnabled;
	}
	
	public boolean getGenerateCities() {
		return this.generateCities;
	}
	
	public WorldType getTerrainType() {
		return this.terrainType;
	}

	public void setTerrainType(WorldType worldType1) {
		this.terrainType = worldType1;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public boolean isBloodMoon() {
		return this.bloodMoon;
	}

	public void setBloodMoon(boolean bloodMoon) {
		this.bloodMoon = bloodMoon;
	}

	public boolean isMeltBuild() {
		return meltBuild;
	}

	public void setMeltBuild(boolean meltBuild) {
		this.meltBuild = meltBuild;
	}

	public boolean isCold() {
		return cold;
	}

	public void setCold(boolean cold) {
		this.cold = cold;
	}

	public float getCityChance() {
		return cityChance;
	}

	public void setCityChance(float cityChance) {
		this.cityChance = cityChance;
	}
}
