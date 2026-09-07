package net.minecraft.game.world;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.BlockStairs;
import net.minecraft.game.world.block.BlockStep;
import net.minecraft.game.world.block.tileentity.TileEntity;
import net.minecraft.game.world.chunk.Chunk;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.entity.EntityBlockEntity;

/**
 * A snapshot of a rectangular region of chunks used to answer block queries
 * during world-generation and other off-thread operations.
 *
 * <p>Implements {@link IBlockAccess} so it can be used as a drop-in for
 * {@link World} during terrain-population passes where the full world is not
 * yet available.</p>
 *
 * <p>Blocks outside the loaded chunk region return {@code 0} (air).</p>
 *
 * @see World
 */
public class ChunkCache implements IBlockAccess {
    /** Origin chunk coordinates. */
    private final int chunkX;
    private final int chunkZ;
    /** Origin block coordinates (= chunkX << 4, chunkZ << 4). */
    private final int originBlockX;
    private final int originBlockZ;
    /** 2D array of chunks covering the region [origin, origin+size]. */
    private final Chunk[][] chunkArray;
    private final World world;

    /**
     * @param world the world
     * @param x1    min block X of the region
     * @param y1    min block Y (unused — always 0)
     * @param z1    min block Z of the region
     * @param x2    max block X of the region
     * @param y2    max block Y (unused)
     * @param z2    max block Z of the region
     */
    public ChunkCache(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;
        this.chunkX = x1 >> 4;
        this.chunkZ = z1 >> 4;
        int maxChunkX = x2 >> 4;
        int maxChunkZ = z2 >> 4;
        this.chunkArray = new Chunk[maxChunkX - this.chunkX + 1][maxChunkZ - this.chunkZ + 1];

        for (int cx = this.chunkX; cx <= maxChunkX; ++cx) {
            for (int cz = this.chunkZ; cz <= maxChunkZ; ++cz) {
                this.chunkArray[cx - this.chunkX][cz - this.chunkZ] = world.getChunkFromChunkCoords(cx, cz);
            }
        }

        this.originBlockX = this.chunkX << 4;
        this.originBlockZ = this.chunkZ << 4;
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        if (y < 0 || y >= 128) {
            return 0;
        }
        if ((x & ~15) == this.originBlockX && (z & ~15) == this.originBlockZ) {
            Chunk origin = this.chunkArray[0][0];
            return origin == null ? 0 : origin.getBlockID(x & 15, y, z & 15);
        }

        int cx = (x >> 4) - this.chunkX;
        int cz = (z >> 4) - this.chunkZ;
        if (cx >= 0 && cx < this.chunkArray.length
                && cz >= 0 && cz < this.chunkArray[cx].length) {
            Chunk chunk = this.chunkArray[cx][cz];
            return chunk == null ? 0 : chunk.getBlockID(x & 15, y, z & 15);
        }
        return 0;
    }

    @Override
    public TileEntity getBlockTileEntity(int x, int y, int z) {
        int cx = (x >> 4) - this.chunkX;
        int cz = (z >> 4) - this.chunkZ;
        return this.chunkArray[cx][cz].getChunkBlockTileEntity(x & 15, y, z & 15);
    }

    @Override
    public EntityBlockEntity getBlockEntity(int x, int y, int z) {
        int cx = (x >> 4) - this.chunkX;
        int cz = (z >> 4) - this.chunkZ;
        return this.chunkArray[cx][cz].getChunkBlockEntity(x & 15, y, z & 15);
    }

    @Override
    public float getBrightness(int x, int y, int z, int face) {
        int light = this.getLightValue(x, y, z);
        if (light < face) {
            light = face;
        }
        return this.world.worldProvider.lightBrightnessTable[light];
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int skyLight) {
        int skyLightVal = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockLightVal = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);
        if (blockLightVal < skyLight) {
            blockLightVal = skyLight;
        }
        return skyLightVal << 20 | blockLightVal << 4;
    }

    @Override
    public float getLightBrightness(int x, int y, int z) {
        return this.world.worldProvider.lightBrightnessTable[this.getLightValue(x, y, z)];
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        if (y < 0 || y >= 128) {
            return 0;
        }
        if ((x & ~15) == this.originBlockX && (z & ~15) == this.originBlockZ) {
            return this.chunkArray[0][0].getBlockMetadata(x & 15, y, z & 15);
        }
        int cx = (x >> 4) - this.chunkX;
        int cz = (z >> 4) - this.chunkZ;
        return this.chunkArray[cx][cz].getBlockMetadata(x & 15, y, z & 15);
    }

    @Override
    public Material getBlockMaterial(int x, int y, int z) {
        int id = this.getBlockId(x, y, z);
        Block block = Block.blocksList[id];
        return block == null ? Material.air : block.blockMaterial;
    }

    @Override
    public WorldChunkManager getWorldChunkManager() {
        return this.world.getWorldChunkManager();
    }

    @Override
    public boolean isBlockOpaqueCube(int x, int y, int z) {
        Block block = Block.blocksList[this.getBlockId(x, y, z)];
        return block != null && block.isOpaqueCube();
    }

    @Override
    public boolean isBlockNormalCube(int x, int y, int z) {
        Block block = Block.blocksList[this.getBlockId(x, y, z)];
        return block != null && block.blockMaterial.getIsSolid() && block.renderAsNormalBlock();
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        Block block = Block.blocksList[this.getBlockId(x, y, z)];
        return block == null;
    }

    // ─── Internal helpers ────────────────────────────────────────────────────────

    /**
     * Computes the combined sky + block light value at a block position.
     * Returns the higher of face-light and centre-light.
     */
    public int getLightValue(int x, int y, int z) {
        return this.getLightValueExt(x, y, z, true);
    }

    /**
     * Extended light query: optionally skips block-emitted light (for neighbour checks).
     *
     * @param checkBlockEmit if true, include block light; if false, only sky light
     */
    public int getLightValueExt(int x, int y, int z, boolean checkBlockEmit) {
        if (x >= -32000000 && z >= -32000000 && x < 32000000 && z <= 32000000) {
            if (checkBlockEmit) {
                int id = this.getBlockId(x, y, z);
                if (id == Block.stairSingle.blockID
                        || id == Block.tilledField.blockID
                        || id == Block.stairCompactPlanks.blockID
                        || id == Block.stairCompactCobblestone.blockID) {
                    // Thin blocks: use neighbour-maximum (no self-emission).
                    int lightN = this.getLightValueExt(x + 1, y, z, false);
                    int lightS = this.getLightValueExt(x - 1, y, z, false);
                    int lightE = this.getLightValueExt(x, y, z + 1, false);
                    int lightW = this.getLightValueExt(x, y, z - 1, false);
                    int maxNeighbour = Math.max(Math.max(lightN, lightS), Math.max(lightE, lightW));
                    int above = this.getLightValueExt(x, y + 1, z, false);
                    return Math.max(maxNeighbour, above);
                }
            }

            if (y < 0) {
                return 0;
            }
            if (y >= 128) {
                int light = 15 - this.world.skylightSubtracted;
                return Math.max(light, 0);
            }

            int cx = (x >> 4) - this.chunkX;
            int cz = (z >> 4) - this.chunkZ;
            return this.chunkArray[cx][cz].getBlockLightValue(
                    x & 15, y, z & 15, this.world.skylightSubtracted);
        }
        return 15;
    }

    /**
     * Returns the stored light level for a sky or block light propagation type
     * at the given position. Uses neighbour-block brightness when
     * {@link Block#useNeighborBrightness} is true.
     */
    public int getSkyBlockTypeBrightness(EnumSkyBlock type, int x, int y, int z) {
        if (y < 0) y = 0;
        if (y >= 256) y = 255;

        if (y >= 0 && y < 256) {
            int id = this.getBlockId(x, y, z);
            if (Block.useNeighborBrightness[id]) {
                Block block = Block.blocksList[id];

                // For upside-down slabs/stairs, read from below; otherwise from above.
                int neighbourY = y;
                if ((block instanceof BlockStep && (this.getBlockMetadata(x, y, z) & 8) != 0)
                        || (block instanceof BlockStairs && (this.getBlockMetadata(x, y, z) & 4) != 0)) {
                    neighbourY = y - 1;
                } else {
                    neighbourY = y + 1;
                }

                int bAbove = this.getSpecialBlockBrightness(type, x, neighbourY, z);
                int bEast  = this.getSpecialBlockBrightness(type, x + 1, y, z);
                int bWest  = this.getSpecialBlockBrightness(type, x - 1, y, z);
                int bNorth = this.getSpecialBlockBrightness(type, x, y, z + 1);
                int bSouth = this.getSpecialBlockBrightness(type, x, y, z - 1);

                int max = bAbove;
                if (bEast  > max) max = bEast;
                if (bWest  > max) max = bWest;
                if (bNorth > max) max = bNorth;
                if (bSouth > max) max = bSouth;
                return max;
            } else {
                int cx = (x >> 4) - this.chunkX;
                int cz = (z >> 4) - this.chunkZ;
                return this.chunkArray[cx][cz].getSavedLightValue(type, x & 15, y, z & 15);
            }
        }
        return type.defaultLightValue;
    }

    /**
     * Reads the stored light value from the chunk data without neighbour-brightness lookup.
     */
    public int getSpecialBlockBrightness(EnumSkyBlock type, int x, int y, int z) {
        if (y < 0) y = 0;
        if (y >= 256) y = 255;

        if (y >= 0 && y < 256) {
            int cx = (x >> 4) - this.chunkX;
            int cz = (z >> 4) - this.chunkZ;
            return this.chunkArray[cx][cz].getSavedLightValue(type, x & 15, y, z & 15);
        }
        return type.defaultLightValue;
    }
}
