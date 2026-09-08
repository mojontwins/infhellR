package net.minecraft.game.world;

import net.minecraft.game.MathHelper;

/**
 * Owns the world's sky-light dial — the {@code skylightSubtracted} counter and the day/night
 * computation that maintains it.
 *
 * <p>Starlight ({@link World#blockLight}/{@link World#skyLight}) takes care of per-block light
 * propagation; what remains for the world is this single scalar that represents how much of the
 * sun's light is being subtracted at the current time of day (0 at noon … 11 at night, further
 * dimmed by rain and thunder). It is re-evaluated once per tick and on world creation, and is
 * consumed by block-light lookups, the client lightmap and {@code isDaytime()}. Extracted from
 * {@link World} in the refactor; {@code World} keeps the public {@code getSkylightSubtracted} /
 * {@code setSkylightSubtracted} / {@code calculateSkylightSubtracted} entry points (and
 * {@code calculateInitialSkylight}) as thin delegates, so external callers are unchanged.
 */
public final class SkylightTracker {

	/** The owning world, for the celestial angle and weather-strength queries. */
	private final World world;

	/** Current sky-light subtraction: 0 (full sunlight) up to 11 (midnight, worst case). */
	private int skylightSubtracted;

	/** Constructs a skylight tracker bound to the given world, starting at full daylight. */
	SkylightTracker(World world) {
		this.world = world;
	}

	/** Returns the current sky-light subtraction counter. */
	int getSkylightSubtracted() {
		return this.skylightSubtracted;
	}

	/** Sets the sky-light subtraction counter directly. */
	void setSkylightSubtracted(int skylightSubtracted) {
		this.skylightSubtracted = skylightSubtracted;
	}

	/**
	 * Computes the sky-light subtraction for the given partial tick from the celestial angle,
	 * reduced by rain strength (snow does not darken the day) and, more weakly, thunder.
	 *
	 * @param renderPartialTick the partial tick for interpolating the celestial angle
	 * @return the computed subtraction in the range 0–11
	 */
	int calculateSkylightSubtracted(float renderPartialTick) {
		float f2 = this.world.getCelestialAngle(renderPartialTick);
		float f3 = 1.0F - (MathHelper.cos(f2 * (float)Math.PI * 2.0F) * 2.0F + 0.5F);
		if(f3 < 0.0F) {
			f3 = 0.0F;
		}

		if(f3 > 1.0F) {
			f3 = 1.0F;
		}

		f3 = 1.0F - f3;
		float rainStrength = this.world.getRainStrength(renderPartialTick) - this.world.getSnowStrength(renderPartialTick);
		if(rainStrength < 0.0F) rainStrength = 0.0F;
		f3 = (float)((double)f3 * (1.0D - (double)(rainStrength * 5F) / 16D));
		float factor = 6F - 3 * rainStrength;
		f3 = (float)((double)f3 * (1.0D - (double)(this.world.getWeightedThunderStrength(renderPartialTick) * factor) / 16D));

		f3 = 1.0F - f3;

		return (int)(f3 * 11.0F);
	}

	/**
	 * Recomputes the sky-light subtraction for the given partial tick and stores it if it changed.
	 *
	 * @param renderPartialTick the partial tick for interpolating the celestial angle
	 * @return {@code true} if the stored value changed as a result of this call
	 */
	boolean updateSkylightSubtracted(float renderPartialTick) {
		int i1 = this.calculateSkylightSubtracted(renderPartialTick);
		if(i1 != this.skylightSubtracted) {
			this.skylightSubtracted = i1;
			return true;
		}

		return false;
	}
}