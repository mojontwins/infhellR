package net.minecraft.game.world;

import java.util.List;

import net.minecraft.game.Seasons;
import net.minecraft.game.command.CommandProcessor;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Stores all persistent world state: spawn point, time of day, weather,
 * dimension, save metadata, and the server command registry.
 *
 * <p>WorldInfo is serialised into the {@code level.dat} NBT file. It is
 * separate from {@link WorldSettings}, which carries creation-time settings
 * without persisting beyond the world-creation screen.</p>
 *
 * @see WorldSettings
 */
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

    /** Probability (0.0–1.0) that a chunk generates a city. */
    private float cityChance = 0.0F;

    /** Reads all world state from an NBT tag loaded from {@code level.dat}. */
    public WorldInfo(NBTTagCompound nbt) {
        this.randomSeed = nbt.getLong("RandomSeed");
        if (nbt.hasKey("generatorName")) {
            String generatorName = nbt.getString("generatorName");
            this.terrainType = WorldType.parseWorldType(generatorName);
            if (this.terrainType == null) {
                this.terrainType = WorldType.DEFAULT;
            }
        }

        if (nbt.hasKey("MapFeatures")) {
            this.mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
        } else {
            this.mapFeaturesEnabled = true;
        }

        if (nbt.hasKey("GenerateCities")) {
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
        if (nbt.hasKey("Player")) {
            this.playerTag = nbt.getCompoundTag("Player");
            this.dimension = this.playerTag.getInteger("Dimension");
        }
        this.cold = nbt.getBoolean("Cold");
        this.cityChance = nbt.getFloat("CityChance");

        CommandProcessor.readFromNBT(nbt);
    }

    /** Creates a WorldInfo from creation-time settings and a level name. */
    public WorldInfo(WorldSettings settings, String levelName) {
        this.randomSeed = settings.getSeed();
        this.mapFeaturesEnabled = settings.isMapFeaturesEnabled();
        this.generateCities = settings.isGenerateCities();
        this.levelName = levelName;
        this.terrainType = settings.getTerrainType();
        if (this.terrainType == WorldType.SKY) {
            this.dimension = 1;
        }
        if (this.terrainType == WorldType.COLD) {
            this.cold = true;
        }
        this.cityChance = settings.getCityChance();
    }

    /** Copy-constructor for snapshotting world state. */
    public WorldInfo(WorldInfo other) {
        this.randomSeed = other.randomSeed;
        this.mapFeaturesEnabled = other.mapFeaturesEnabled;
        this.generateCities = other.generateCities;
        this.spawnX = other.spawnX;
        this.spawnY = other.spawnY;
        this.spawnZ = other.spawnZ;
        this.worldTime = other.worldTime;
        this.lastTimePlayed = other.lastTimePlayed;
        this.sizeOnDisk = other.sizeOnDisk;
        this.playerTag = other.playerTag;
        this.dimension = other.dimension;
        this.levelName = other.levelName;
        this.saveVersion = other.saveVersion;
        this.rainTime = other.rainTime;
        this.raining = other.raining;
        this.thunderTime = other.thunderTime;
        this.thundering = other.thundering;
        this.snowingTime = other.snowingTime;
        this.snowing = other.snowing;
        this.bloodMoon = other.bloodMoon;
        this.cold = other.cold;
        this.cityChance = other.cityChance;
    }

    /**
     * Serialises this world info into a new NBTTagCompound.
     *
     * @return an NBT tag representing the entire world data section (excludes player)
     */
    public NBTTagCompound getNBTTagCompound() {
        NBTTagCompound tag = new NBTTagCompound();
        this.updateTagCompound(tag, this.playerTag);
        return tag;
    }

    /**
     * Serialises world info plus a single player's data into an NBTTagCompound.
     * Used when saving a single-player world.
     *
     * @param players the list of players (uses the first element)
     * @return an NBT tag representing world data plus one player
     */
    public NBTTagCompound getNBTTagCompoundWithPlayer(List<EntityPlayer> players) {
        NBTTagCompound tag = new NBTTagCompound();
        EntityPlayer firstPlayer = null;
        NBTTagCompound playerNbt = null;
        if (!players.isEmpty()) {
            firstPlayer = players.get(0);
        }
        if (firstPlayer != null) {
            playerNbt = new NBTTagCompound();
            firstPlayer.writeToNBT(playerNbt);
        }
        this.updateTagCompound(tag, playerNbt);
        return tag;
    }

    /**
     * Serialises all fields into the given NBTTagCompound.
     *
     * @param worldNBT the tag to write into
     * @param playerNBT the player NBT to embed (may be null for server-side)
     */
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
        if (playerNBT != null) {
            worldNBT.setCompoundTag("Player", playerNBT);
        }
        worldNBT.setBoolean("Cold", this.cold);
        worldNBT.setFloat("CityChance", this.cityChance);

        CommandProcessor.writeToNBT(worldNBT);
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public long getRandomSeed() { return this.randomSeed; }
    public int getSpawnX() { return this.spawnX; }
    public int getSpawnY() { return this.spawnY; }
    public int getSpawnZ() { return this.spawnZ; }
    public long getWorldTime() { return this.worldTime; }
    public long getSizeOnDisk() { return this.sizeOnDisk; }
    public NBTTagCompound getPlayerNBTTagCompound() { return this.playerTag; }
    public int getDimension() { return this.dimension; }
    public String getWorldName() { return this.levelName; }
    public int getSaveVersion() { return this.saveVersion; }
    public long getLastTimePlayed() { return this.lastTimePlayed; }
    public boolean getThundering() { return this.thundering; }
    public int getThunderTime() { return this.thunderTime; }
    public boolean getRaining() { return this.raining; }
    public int getRainTime() { return this.rainTime; }
    public boolean getSnowing() { return this.snowing; }
    public int getSnowingTime() { return this.snowingTime; }
    public boolean isMapFeaturesEnabled() { return this.mapFeaturesEnabled; }
    public boolean getGenerateCities() { return this.generateCities; }
    public WorldType getTerrainType() { return this.terrainType; }
    public boolean isBloodMoon() { return this.bloodMoon; }
    public boolean isMeltBuild() { return this.meltBuild; }
    public boolean isCold() { return this.cold; }
    public float getCityChance() { return this.cityChance; }

    // ─── Setters ───────────────────────────────────────────────────────────────

    public void setSpawnX(int x) { this.spawnX = x; }
    public void setSpawnY(int y) { this.spawnY = y; }
    public void setSpawnZ(int z) { this.spawnZ = z; }
    public void setSpawn(int x, int y, int z) { this.spawnX = x; this.spawnY = y; this.spawnZ = z; }
    public void setWorldTime(long time) { this.worldTime = time; }
    public void setSizeOnDisk(long size) { this.sizeOnDisk = size; }
    public void setPlayerNBTTagCompound(NBTTagCompound tag) { this.playerTag = tag; }
    public void setWorldName(String name) { this.levelName = name; }
    public void setSaveVersion(int version) { this.saveVersion = version; }
    public void setThundering(boolean thundering) { this.thundering = thundering; }
    public void setThunderTime(int time) { this.thunderTime = time; }
    public void setRaining(boolean raining) { this.raining = raining; }
    public void setRainTime(int time) { this.rainTime = time; }
    public void setSnowing(boolean snowing) { this.snowing = snowing; }
    public void setSnowingTime(int time) { this.snowingTime = time; }
    public void setTerrainType(WorldType type) { this.terrainType = type; }
    public void setRandomSeed(long seed) { this.randomSeed = seed; }
    public void setBloodMoon(boolean bloodMoon) { this.bloodMoon = bloodMoon; }
    public void setMeltBuild(boolean meltBuild) { this.meltBuild = meltBuild; }
    public void setCold(boolean cold) { this.cold = cold; }
    public void setCityChance(float chance) { this.cityChance = chance; }
}
