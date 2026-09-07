package net.minecraft.game.world;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.chunk.ChunkProviderSky;
import net.minecraft.game.world.chunk.IChunkProvider;

/**
 * WorldProvider for the Sky dimension (dimension 1, used as the portal destination
 * for the default Overworld).
 *
 * <ul>
 *   <li>Uses {@link ChunkProviderSky} for terrain</li>
 *   <li>Flat surface spawn validation (any solid block)</li>
 *   <li>No sunrise/sunset colours</li>
 *   <li>Low cloud height (8 blocks above spawn)</li>
 * </ul>
 */
public class WorldProviderSky extends WorldProvider {
    @Override
    public IChunkProvider getChunkProvider() {
        return new ChunkProviderSky(this.worldObj, this.worldObj.getRandomSeed());
    }

    /** @return null (no sunrise/sunset gradient in the sky dimension) */
    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTick) {
        return null;
    }

    /** @return false (void is not rendered in the sky dimension) */
    @Override
    public boolean func_28112_c() {
        return false;
    }

    @Override
    public float getCloudHeight() {
        return 8.0F;
    }

    /**
     * Sky spawn: the top block must be solid (any block, unlike the overworld's sand-only rule).
     */
    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        int topId = this.worldObj.getFirstUncoveredBlock(x, z);
        return topId != 0 && Block.blocksList[topId].blockMaterial.getIsSolid();
    }
}
