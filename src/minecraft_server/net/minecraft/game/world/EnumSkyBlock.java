package net.minecraft.game.world;

/**
 * Controls how the game computes block-sky light propagation. Used by
 * {@link net.minecraft.game.world.World#func_4107_c(EnumSkyBlock, int, int, int)} to
 * determine the default light level for each propagation type.
 */
public enum EnumSkyBlock {
    /**
     * Sky light propagates from the top of the world downward. Default brightness is 15
     * (full daylight).
     */
	Sky(15),

    /**
     * Block light propagates from blocks that emit light (torches, lava, etc.)
     * outward in all directions. Default brightness is 0.
     */
	Block(0);

	public final int defaultLightValue;

    private EnumSkyBlock(int defaultLight) {
        this.defaultLightValue = defaultLight;
	}
}
