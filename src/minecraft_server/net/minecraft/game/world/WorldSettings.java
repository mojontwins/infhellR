package net.minecraft.game.world;

/**
 * Immutable world-configuration settings passed when creating a new world.
 *
 * <p>Captures everything the chunk generator needs to know: random seed,
 * game mode, which structure generators to enable (caves, strongholds, etc.),
 * the generator preset, and the city generation probability.</p>
 *
 * @see WorldInfo
 */
public final class WorldSettings {
    private final long seed;
    private final int gameType;
    private final boolean mapFeaturesEnabled;
    private final boolean generateCities;
    private final float cityChance;
    private final boolean hardcoreEnabled;
    private final WorldType terrainType;

    public WorldSettings(long seed, int gameType, boolean mapFeaturesEnabled,
            boolean hardcoreEnabled, boolean generateCities, float cityChance,
            WorldType terrainType) {
        this.seed = seed;
        this.gameType = gameType;
        this.mapFeaturesEnabled = mapFeaturesEnabled;
        this.hardcoreEnabled = hardcoreEnabled;
        this.generateCities = generateCities;
        this.terrainType = terrainType;
        this.cityChance = cityChance;
    }

    public long getSeed() {
        return this.seed;
    }

    public int getGameType() {
        return this.gameType;
    }

    public boolean getHardcoreEnabled() {
        return this.hardcoreEnabled;
    }

    public boolean isMapFeaturesEnabled() {
        return this.mapFeaturesEnabled;
    }

    public boolean isGenerateCities() {
        return this.generateCities;
    }

    public WorldType getTerrainType() {
        return this.terrainType;
    }

    /**
     * Returns a valid game-type ID, clamping unknown values to survival (0).
     *
     * @param gameType raw game-type ID (0 = survival, 1 = creative)
     * @return 0 or 1
     */
    public static int validGameType(int gameType) {
        if (gameType == 0 || gameType == 1) {
            return gameType;
        }
        return 0;
    }

    public float getCityChance() {
        return this.cityChance;
    }
}
