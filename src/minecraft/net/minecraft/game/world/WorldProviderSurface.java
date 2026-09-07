package net.minecraft.game.world;

/**
 * WorldProvider for the standard Overworld (dimension 0).
 *
 * <p>All behaviour (chunk manager, spawn rules, cloud height) is inherited from
 * the base {@link WorldProvider}, which handles the Overworld defaults:
 * sand-only spawn, surface biome generation, full day/night cycle, 108-block cloud height.</p>
 *
 * @see WorldProvider
 */
public class WorldProviderSurface extends WorldProvider {
}
