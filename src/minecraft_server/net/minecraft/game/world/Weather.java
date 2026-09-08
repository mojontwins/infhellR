package net.minecraft.game.world;

import java.util.Random;

import net.minecraft.game.Seasons;
import net.minecraft.game.world.biome.BiomeGenBase;

/**
 * Per-biome weather classification and weather-duration lookup tables.
 *
 * <p>Weather duration is keyed by season index ({@code 0–3}: spring, summer, autumn, winter).
 * Each season has independent min/max ranges for how long rain, snow, and thunder
 * persists before switching state.</p>
 *
 * <p>Note: season indices map to {@link Seasons#currentSeason}: 0 = spring, 1 = summer,
 * 2 = autumn, 3 = winter.</p>
 *
 * @see Seasons
 */
public class Weather {
    public static final int RAIN = 1;
    public static final int SNOW = 2;

    /** Cold biomes: snow or rain depending on season. */
    public static final Weather cold   = new Weather().setName("Cold");
    /** Normal biomes: rain only (or snow in winter). */
    public static final Weather normal = new Weather().setName("Normal");
    /** Hot biomes: rain only (never snow). */
    public static final Weather hot   = new Weather().setName("Hot");
    /** Desert biomes: no precipitation. */
    public static final Weather desert = new Weather().setName("Desertic");

    /** Display name for the GUI. */
    public String name = "Weather";

    // ─── Duration lookup tables (indexed by season) ──────────────────────────────
    // Each entry: [spring, summer, autumn, winter]

    /** Min game-ticks until snow stops. */
    public static final int[] snowingMinTimeToStop  = {12000, 6000, 6000, 8000};
    /** Max game-ticks until snow stops. */
    public static final int[] snowingMaxTimeToStop  = {28000, 16000, 12000, 12000};
    /** Min game-ticks until next snow starts. */
    public static final int[] snowingMinTimeToStart = {6000, 9000, 12000, 9000};
    /** Max game-ticks until next snow starts. */
    public static final int[] snowingMaxTimeToStart = {18000, 50000, 72000, 24000};

    /** Min game-ticks until rain stops. */
    public static final int[] rainingMinTimeToStop  = {6000, 6000, 3000, 8000};
    /** Max game-ticks until rain stops. */
    public static final int[] rainingMaxTimeToStop  = {12000, 12000, 6000, 24000};
    /** Min game-ticks until next rain starts. */
    public static final int[] rainingMinTimeToStart = {2000, 12000, 60000, 1000};
    /** Max game-ticks until next rain starts. */
    public static final int[] rainingMaxTimeToStart = {6000, 24000, 168000, 3000};

    /** Min game-ticks until thunder stops. */
    public static final int[] thunderingMinTimeToStop  = {3000, 3000, 6000, 3000};
    /** Max game-ticks until thunder stops. */
    public static final int[] thunderingMaxTimeToStop  = {9000, 6000, 12000, 9000};
    /** Min game-ticks until next thunder starts. */
    public static final int[] thunderingMinTimeToStart = {12000, 12000, 12000, 12000};
    /** Max game-ticks until next thunder starts. */
    public static final int[] thunderingMaxTimeToStart = {168000, 168000, 72000, 168000};

    // ─── Duration helpers ────────────────────────────────────────────────────────

    public static int getTimeForSnowingEnd(Random rand) {
        int season = Seasons.currentSeason;
        return rand.nextInt(snowingMaxTimeToStop[season] - snowingMinTimeToStop[season])
                + snowingMinTimeToStop[season];
    }

    public static int getTimeForNextSnow(Random rand) {
        int season = Seasons.currentSeason;
        return rand.nextInt(snowingMaxTimeToStart[season] - snowingMinTimeToStart[season])
                + snowingMinTimeToStart[season];
    }

    public static int getTimeForRainingEnd(Random rand) {
        int season = Seasons.currentSeason;
        return rand.nextInt(rainingMaxTimeToStop[season] - rainingMinTimeToStop[season])
                + rainingMinTimeToStop[season];
    }

    public static int getTimeForNextRain(Random rand) {
        int season = Seasons.currentSeason;
        return rand.nextInt(rainingMaxTimeToStart[season] - rainingMinTimeToStart[season])
                + rainingMinTimeToStart[season];
    }

    public static int getTimeForThunderingEnd(Random rand) {
        int season = Seasons.currentSeason;
        return rand.nextInt(thunderingMaxTimeToStop[season] - thunderingMinTimeToStop[season])
                + thunderingMinTimeToStop[season];
    }

    public static int getTimeForNextThunder(Random rand) {
        int season = Seasons.currentSeason;
        return rand.nextInt(thunderingMaxTimeToStart[season] - thunderingMinTimeToStart[season])
                + thunderingMinTimeToStart[season];
    }

    /**
     * Decides which weather particle to render based on the current biome and
     * weather-state variables on the world.
     *
     * @param biome the biome at the camera position
     * @param world the active world
     * @return {@link #RAIN}, {@link #SNOW}, or 0 (none)
     */
    public static int particleDecide(BiomeGenBase biome, World world) {
        if (biome == null) {
            return 0;
        }

        if (biome.weather == Weather.cold) {
            if (world.snowingStrength > 0.0F) {
                return Seasons.currentSeason == Seasons.SUMMER ? RAIN : SNOW;
            }
            if (world.rainingStrength > 0.0F) {
                return Seasons.currentSeason == Seasons.WINTER ? SNOW : RAIN;
            }
        } else if (biome.weather == Weather.normal) {
            if (world.rainingStrength > 0.0F) return RAIN;
            if (world.snowingStrength > 0.0F && Seasons.currentSeason == Seasons.WINTER) return SNOW;
        } else if (biome.weather == Weather.hot) {
            if (world.rainingStrength > 0.0F) return RAIN;
        }
        // desert: always 0

        return 0;
    }

    public Weather setName(String name) {
        this.name = name;
        return this;
    }
}
