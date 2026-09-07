package net.minecraft.game.world;

/**
 * Callback interface through which the server notifies the client of world events.
 *
 * <p>Each method corresponds to a specific world-change event that the client
 * must handle — updating renderers, playing sounds, spawning particles, etc.
 * The client registers its implementation via
 * {@link World#addWorldAccess(IWorldAccess)}.</p>
 */
public interface IWorldAccess {
    /**
     * Notifies the client that a block has changed at the given coordinates,
     * invalidating its cached render data.
     */
    void markBlockNeedsUpdate(int x, int y, int z);

    /**
     * Notifies the client that a range of blocks has changed. Used for
     * batch updates (e.g. large terrain modifications).
     */
    void markBlockRangeNeedsUpdate(int x1, int y1, int z1, int x2, int y2, int z2);

    /**
     * Plays a positional sound at the given world coordinates.
     *
     * @param name  sound identifier (e.g. "random.click")
     * @param x     world x
     * @param y     world y
     * @param z     world z
     * @param volume  0.0–1.0
     * @param pitch   0.5–2.0
     */
    void playSound(String name, double x, double y, double z, float volume, float pitch);

    /**
     * Spawns a named particle effect at the given coordinates.
     *
     * @param name     particle identifier
     * @param x1-x3    position
     * @param y1-y3    velocity (displacement per tick)
     */
    void spawnParticle(String name, double x1, double y1, double z1, double x2, double y2, double x3);

    /**
     * Tells the client to start tracking the given entity's position for
     * rendering purposes (entity bounding-box updates).
     */
    void obtainEntitySkin(net.minecraft.game.entity.Entity entity);

    /**
     * Tells the client to stop tracking the given entity.
     */
    void releaseEntitySkin(net.minecraft.game.entity.Entity entity);

    /**
     * Requests a full re-render of all world renderers.
     */
    void updateAllRenderers();

    /**
     * Plays a record (music disc) at the given block position.
     *
     * @param recordName sound event name, or null to stop the current record
     * @param x         block x
     * @param y         block y
     * @param z         block z
     */
    void playRecord(String recordName, int x, int y, int z);

    /**
     * Called when a tile entity is removed or invalidated.
     */
    void doNothingWithTileEntity(int x, int y, int z, net.minecraft.game.world.block.tileentity.TileEntity tileEntity);

    /**
     * Plays a GUI aux-effect (e.g. anvil break, enchantment sparkle, furnace spark).
     *
     * @param player    the player who triggered the effect
     * @param effectId  which effect to play (see SFX constants)
     * @param x         world x
     * @param y         world y
     * @param z         world z
     * @param a         extra data word (effect-specific)
     * @param b         extra data word
     */
    void playAuxSFX(net.minecraft.game.entity.player.EntityPlayer player, int effectId, int x, int y, int z, int a);

    /**
     * Sets the overlay title string for titles set by the world provider.
     */
    void showString(String text);
}
