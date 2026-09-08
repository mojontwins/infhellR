package net.minecraft.game.world;

import net.minecraft.game.MathHelper;
import net.minecraft.game.Seasons;
import net.minecraft.game.physics.Vec3D;

/**
 * Static helpers that compute the world's atmosphere: the sky, fog and cloud colours plus the
 * sun and star brightness, all derived from the celestial angle and the current weather strengths.
 *
 * <p>Each helper takes the {@link World} it should read from as its first argument and keeps the
 * {@code Seasons}/{@code colouredAthmospherics} overrides intact — when {@code colouredAthmospherics}
 * is set, the sky (and via {@link WorldProvider}, the fog) colour comes from {@link Seasons} instead
 * of the fixed blue. Extracted from {@link World} in the refactor; {@code World} keeps its public
 * {@code getSunBrightness}/{@code getStarBrightness}/{@code getSkyColor}/{@code getCloudColor}/
 * {@code getFogColor} entry points as thin delegates, so external callers are unchanged.
 */
public final class AtmosphereCalculator {

	/** Base cloud colour (opaque white); heaviness of weather darkens it. */
	private static final long CLOUD_COLOUR = 16777215L;

	private AtmosphereCalculator() {
	}

	/**
	 * Returns how bright the sun appears, from the celestial angle, dimmed by rain and thunder.
	 *
	 * @param world        the world to read the time/weather from
	 * @param partialTick  the partial tick for interpolating the celestial angle
	 * @return a brightness in the range 0.2–1.0
	 */
	public static float getSunBrightness(World world, float partialTick) {
		float celestialAngle = world.getCelestialAngle(partialTick);
		float brightness = 1.0F - (MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.2F);
		if(brightness < 0.0F) {
			brightness = 0.0F;
		}

		if(brightness > 1.0F) {
			brightness = 1.0F;
		}

		brightness = 1.0F - brightness;
		brightness = (float)((double)brightness * (1.0D - (double)(world.getRainStrength(partialTick) * 5.0F) / 16.0D));
		brightness = (float)((double)brightness * (1.0D - (double)(world.getWeightedThunderStrength(partialTick) * 5.0F) / 16.0D));
		return brightness * 0.8F + 0.2F;
	}

	/**
	 * Returns the world's diffuse sky colour for the given partial tick: the fixed (or seasonally
	 * tinted) blue scaled by the current daylight level, darkened by rain/thunder and flashed white
	 * by a recent lightning strike.
	 *
	 * @param world            the world to read the time/weather/lightning state from
	 * @param renderPartialTick the partial tick for interpolating the celestial angle
	 * @return the sky colour as a 3-component vector
	 */
	public static Vec3D getSkyColor(World world, float renderPartialTick) {
		float celestialAngle = world.getCelestialAngle(renderPartialTick);
		float celestialLight = MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(celestialLight < 0.0F) {
			celestialLight = 0.0F;
		}

		if(celestialLight > 1.0F) {
			celestialLight = 1.0F;
		}

		int skyColor;
		if(world.colouredAthmospherics) {
			skyColor = Seasons.getSkyColorForToday();
		} else {
			skyColor = 0x88BBFF;
		}

		float r = (float)(skyColor >> 16 & 255L) / 255.0F;
		float g = (float)(skyColor >> 8 & 255L) / 255.0F;
		float b = (float)(skyColor & 255L) / 255.0F;
		r *= celestialLight;
		g *= celestialLight;
		b *= celestialLight;
		float attenuationStrength = world.getRainStrength(renderPartialTick) + world.getWeightedThunderStrength(renderPartialTick) - world.getSnowStrength(renderPartialTick);
		if(attenuationStrength >= 0.0F) {
			if(attenuationStrength >= 1.0F) attenuationStrength = 1.0F;
			float skyColorComponent = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
			float skyColorAtenuation = 1.0F - attenuationStrength * 0.75F;
			r = r * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			g = g * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
			b = b * skyColorAtenuation + skyColorComponent * (1.0F - skyColorAtenuation);
		}

		if(world.lightningFlash > 0) {
			float lightning = (float)world.lightningFlash - renderPartialTick;
			if(lightning > 1.0F) {
				lightning = 1.0F;
			}

			lightning *= 0.45F;
			r = r * (1.0F - lightning) + 0.8F * lightning;
			g = g * (1.0F - lightning) + 0.8F * lightning;
			b = b * (1.0F - lightning) + 1.0F * lightning;
		}

		return Vec3D.createVector((double)r, (double)g, (double)b);
	}

	/**
	 * Returns the world's cloud colour for the given partial tick: the base cloud colour scaled by
	 * the daylight level and progressively greyed out by rain and thunder.
	 *
	 * @param world        the world to read the time/weather from
	 * @param partialTick  the partial tick for interpolating the celestial angle
	 * @return the cloud colour as a 3-component vector
	 */
	public static Vec3D getCloudColor(World world, float partialTick) {
		float celestialAngle = world.getCelestialAngle(partialTick);
		float daylightFactor = MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(daylightFactor < 0.0F) {
			daylightFactor = 0.0F;
		}

		if(daylightFactor > 1.0F) {
			daylightFactor = 1.0F;
		}

		float r = (float)(CLOUD_COLOUR >> 16 & 255L) / 255.0F;
		float g = (float)(CLOUD_COLOUR >> 8 & 255L) / 255.0F;
		float b = (float)(CLOUD_COLOUR & 255L) / 255.0F;
		float rainStrength = world.getRainStrength(partialTick);
		float thunderStrength;
		float attenuation;
		if(rainStrength > 0.0F) {
			thunderStrength = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.6F;
			attenuation = 1.0F - rainStrength * 0.95F;
			r = r * attenuation + thunderStrength * (1.0F - attenuation);
			g = g * attenuation + thunderStrength * (1.0F - attenuation);
			b = b * attenuation + thunderStrength * (1.0F - attenuation);
		}

		// Clouds dim with the daylight level (blue channel slightly more).
		r *= daylightFactor * 0.9F + 0.1F;
		g *= daylightFactor * 0.9F + 0.1F;
		b *= daylightFactor * 0.85F + 0.15F;
		thunderStrength = world.getWeightedThunderStrength(partialTick);
		if(thunderStrength > 0.0F) {
			attenuation = (r * 0.3F + g * 0.59F + b * 0.11F) * 0.2F;
			float thunderAttenuation = 1.0F - thunderStrength * 0.95F;
			r = r * thunderAttenuation + attenuation * (1.0F - thunderAttenuation);
			g = g * thunderAttenuation + attenuation * (1.0F - thunderAttenuation);
			b = b * thunderAttenuation + attenuation * (1.0F - thunderAttenuation);
		}

		return Vec3D.createVector((double)r, (double)g, (double)b);
	}

	/**
	 * Returns the world's fog colour for the given partial tick, delegated to the {@link WorldProvider}
	 * (nether/sky/surface variants + seasons/blood-moon overrides) using the celestial angle.
	 *
	 * @param world        the world to read the time/weather from
	 * @param partialTick  the partial tick for interpolating the celestial angle
	 * @return the fog colour as a 3-component vector
	 */
	public static Vec3D getFogColor(World world, float partialTick) {
		float celestialAngle = world.getCelestialAngle(partialTick);
		return world.worldProvider.getFogColor(celestialAngle, partialTick, world.worldInfo.isBloodMoon(), world.colouredAthmospherics);
	}

	/**
	 * Returns how bright stars appear, from the celestial angle: brightest at night, fading to zero
	 * during the day.
	 *
	 * @param world        the world to read the time from
	 * @param partialTick  the partial tick for interpolating the celestial angle
	 * @return a brightness in the range 0.0–0.5
	 */
	public static float getStarBrightness(World world, float partialTick) {
		float celestialAngle = world.getCelestialAngle(partialTick);
		float brightness = 1.0F - (MathHelper.cos(celestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.75F);
		if(brightness < 0.0F) {
			brightness = 0.0F;
		}

		if(brightness > 1.0F) {
			brightness = 1.0F;
		}

		return brightness * brightness * 0.5F;
	}
}