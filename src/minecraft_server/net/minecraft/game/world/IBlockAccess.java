package net.minecraft.game.world;

/**
 * Abstract interface for reading block state at any world coordinate.
 *
 * <p>Implemented by {@link net.minecraft.game.world.World} and
 * {@link net.minecraft.game.world.ChunkCache} so both can be used
 * interchangeably by renderers, physics, and world-generation code.</p>
 */
public interface IBlockAccess {
    int getBlockId(int x, int y, int z);

    net.minecraft.game.world.block.tileentity.TileEntity getBlockTileEntity(int x, int y, int z);
	
    net.minecraft.game.entity.EntityBlockEntity getBlockEntity(int x, int y, int z);
	
    /**
     * Returns the combined brightness (block + sky) at the given face of the block.
     *
     * @param x     block x
     * @param y     block y
     * @param z     block z
     * @param face  which of the 6 block faces to check (0-5)
     * @return brightness in [0, 1]
     */
    float getBrightness(int x, int y, int z, int face);

    /**
     * Returns the sky-subtracted brightness at the block centre.
     */
    float getLightBrightness(int x, int y, int z);

    int getBlockMetadata(int x, int y, int z);

    net.minecraft.game.world.material.Material getBlockMaterial(int x, int y, int z);

    boolean isBlockOpaqueCube(int x, int y, int z);

    boolean isBlockNormalCube(int x, int y, int z);

	WorldChunkManager getWorldChunkManager();

    /**
     * Returns the brightness value to use for sky-block propagation at this position.
     *
     * @param x       block x
     * @param y       block y
     * @param z       block z
     * @param skyLight current sky light value at this position
     * @return the effective sky light for propagation purposes
     */
    int getLightBrightnessForSkyBlocks(int x, int y, int z, int skyLight);

    boolean isAirBlock(int x, int y, int z);
}
