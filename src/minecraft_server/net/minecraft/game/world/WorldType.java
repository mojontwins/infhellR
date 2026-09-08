package net.minecraft.game.world;

/**
 * Defines a world-generation preset type (default, flat, sky, cold, etc.).
 *
 * <p>Each type controls:</p>
 * <ul>
 *   <li>Which {@link net.minecraft.game.world.chunk.ChunkProviderGenerate} variant to use</li>
 *   <li>Sea level and spawn height</li>
 *   <li>Whether void particles are shown</li>
 *   <li>Whether the world is "versioned" (stores a generator version in the save)</li>
 * </ul>
 */
public class WorldType {
    private static final int MAX_TYPES = 16;
    public static final WorldType[] worldTypes = new WorldType[MAX_TYPES];

    public static final WorldType DEFAULT = (new WorldType(0, "default", 1)).setVersioned();
    public static final WorldType FLAT    = (new WorldType(1, "flat")).setCanBeCreated(false);
    public static final WorldType COLD   = (new WorldType(2, "cold", 1)).setVersioned();
    public static final WorldType SKY    = (new WorldType(3, "sky", 1)).setVersioned();

    private final String worldTypeName;
    private final int generatorVersion;
    private boolean canBeCreated;
    private boolean versioned;

    protected WorldType(int id, String name) {
        this(id, name, 0);
    }

    protected WorldType(int id, String name, int generatorVersion) {
        this.worldTypeName = name;
        this.generatorVersion = generatorVersion;
        this.canBeCreated = true;
        worldTypes[id] = this;
    }

    @Override
    public String toString() {
        return this.worldTypeName;
    }

    /** @return the lang key for this generator type, e.g. "generator.flat" */
    public String getTranslateName() {
        return "generator." + this.worldTypeName;
    }

    /** @return the generator version stored in the world save */
    public int getGeneratorVersion() {
        return this.generatorVersion;
    }

    private WorldType setCanBeCreated(boolean canCreate) {
        this.canBeCreated = canCreate;
        return this;
    }

    /** @return true if this type can be selected when creating a new world */
    public boolean getCanBeCreated() {
        return this.canBeCreated;
    }

    private WorldType setVersioned() {
        this.versioned = true;
        return this;
    }

    /** @return true if the world save stores a generator version */
    public boolean isVersioned() {
        return this.versioned;
    }

    /**
     * Looks up a world type by its string name.
     *
     * @param name the generator name (e.g. "default", "flat")
     * @return the matching WorldType, or null
     */
    public static WorldType parseWorldType(String name) {
        for (int i = 0; i < worldTypes.length; ++i) {
            if (worldTypes[i] != null && worldTypes[i].worldTypeName.equalsIgnoreCase(name)) {
                return worldTypes[i];
            }
        }
        return null;
    }

    /**
     * Returns the {@link WorldChunkManager} for this generator type.
     * Currently all types use the same manager class.
     */
    public WorldChunkManager getChunkManager(World world) {
        return new WorldChunkManager(world);
    }

    /**
     * Returns the appropriate chunk generator for this world type.
     *
     * @param world the world being generated
     */
    public net.minecraft.game.world.chunk.IChunkProvider getChunkGenerator(World world) {
        if (this == SKY) {
            return new net.minecraft.game.world.chunk.ChunkProviderSky(
                    world, world.getRandomSeed(),
                    world.getWorldInfo().isMapFeaturesEnabled(),
                    world.getWorldInfo().getGenerateCities());
        }
        return new net.minecraft.game.world.terrain.ChunkProviderGenerate(
                world, world.getRandomSeed(),
                world.getWorldInfo().isMapFeaturesEnabled(),
                world.getWorldInfo().getGenerateCities());
    }

    /** @return sea level for this generator type */
    public int getSeaLevel(World world) {
        return this.getMinimumSpawnHeight(world);
    }

    /** @return minimum y-level for player/entity spawning in this generator */
    public int getMinimumSpawnHeight(World world) {
        return this == FLAT ? 4 : 64;
    }

    /**
     * @return the y-level of the horizon/dividing line between the world and the void.
     *         For the default world this is 63 (sea level).
     */
    public double getHorizon(World world) {
        return this == FLAT ? 0.0D : 63.0D;
    }

    /**
     * @param hasNoSky true when rendering in the Nether
     * @return true if void particles should be rendered
     */
    public boolean hasVoidParticles(boolean hasNoSky) {
        return this != FLAT && !hasNoSky;
    }

    /** @return the rate at which the void fog fades in (1.0 = instant, smaller = gradual) */
    public double voidFadeMagnitude() {
        return this == FLAT ? 1.0D : 8.0D / 256D;
    }

    /** Hook called when the "Create New World" GUI is first opened (unused by default) */
    public void onGUICreateWorldPress() {
    }
}
