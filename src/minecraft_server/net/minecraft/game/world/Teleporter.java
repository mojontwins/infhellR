package net.minecraft.game.world;

import java.util.Random;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.block.Block;

/**
 * Manages portal teleportation between dimensions (Nether / Overworld).
 *
 * <p>Responsible for finding an existing portal near the destination coordinates
 * and, if none is found, creating a new portal frame out of obsidian and
 * filling it with portal blocks.</p>
 *
 * <p>Portal exit positions are snapped to the centre of the nearest portal
 * block column, adjusting for which side of the portal frame the entity
 * enters from.</p>
 */
public class Teleporter {
    private final Random rand = new Random();

    /**
     * Finds or creates a portal exit and teleports the entity to it.
     *
     * @param world the destination world
     * @param entity the entity to teleport
     */
    public void setExitLocation(World world, Entity entity) {
        if (!this.findExitLocation(world, entity)) {
            this.createExitLocation(world, entity);
            this.findExitLocation(world, entity);
        }
    }

    /**
     * Searches for the nearest portal block within 128 blocks of the entity's
     * current position, then sets the entity's position to the portal centre.
     *
     * @return true if a portal was found within range
     */
    public boolean findExitLocation(World world, Entity entity) {
        final int searchRadius = 128;
        double closestDistSq = -1.0D;
        int portalX = 0;
        int portalY = 0;
        int portalZ = 0;
        int centerX = MathHelper.floor_double(entity.posX);
        int centerZ = MathHelper.floor_double(entity.posZ);

        for (int x = centerX - searchRadius; x <= centerX + searchRadius; ++x) {
            double dx = (double) x + 0.5D - entity.posX;

            for (int z = centerZ - searchRadius; z <= centerZ + searchRadius; ++z) {
                double dz = (double) z + 0.5D - entity.posZ;

                for (int y = 127; y >= 0; --y) {
                    if (world.getBlockId(x, y, z) == Block.portal.blockID) {
                        // Descend to the bottom of the portal column.
                        while (world.getBlockId(x, y - 1, z) == Block.portal.blockID) {
                            --y;
                        }

                        double dy = (double) y + 0.5D - entity.posY;
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (closestDistSq < 0.0D || distSq < closestDistSq) {
                            closestDistSq = distSq;
                            portalX = x;
                            portalY = y;
                            portalZ = z;
                        }
                    }
                }
            }
        }

        if (closestDistSq >= 0.0D) {
            double exitX = (double) portalX + 0.5D;
            double exitY = (double) portalY + 0.5D;
            double exitZ = (double) portalZ + 0.5D;

            // Adjust for which side of the portal the entity entered from.
            if (world.getBlockId(portalX - 1, portalY, portalZ) == Block.portal.blockID) {
                exitX -= 0.5D;
            }
            if (world.getBlockId(portalX + 1, portalY, portalZ) == Block.portal.blockID) {
                exitX += 0.5D;
            }
            if (world.getBlockId(portalX, portalY, portalZ - 1) == Block.portal.blockID) {
                exitZ -= 0.5D;
            }
            if (world.getBlockId(portalX, portalY, portalZ + 1) == Block.portal.blockID) {
                exitZ += 0.5D;
            }

            entity.setLocationAndAngles(exitX, exitY, exitZ, entity.rotationYaw, 0.0F);
            entity.motionX = entity.motionY = entity.motionZ = 0.0D;
            return true;
        }
        return false;
    }

    /**
     * Creates a new portal at the best available location near the entity's
     * destination coordinates. Searches for a suitable open space, then
     * builds an obsidian frame and fills it with portal blocks.
     *
     * @return true always (a portal is always attempted)
     */
    public boolean createExitLocation(World world, Entity entity) {
        final byte searchRadius = 16;
        double closestDistSq = -1.0D;
        int originX = MathHelper.floor_double(entity.posX);
        int originY = MathHelper.floor_double(entity.posY);
        int originZ = MathHelper.floor_double(entity.posZ);
        int bestX = originX;
        int bestY = originY;
        int bestZ = originZ;
        int bestPortalOffset = 0;

        // First pass: search for the largest open space, favouring the standard
        // 4×5 portal layout.
        final int portalWidth = 4;
        final int portalHeight = 5;
        int orientation = this.rand.nextInt(4);  // 0–3 selects frame orientation.

        outerFirstPass:
        for (int x = originX - searchRadius; x <= originX + searchRadius; ++x) {
            double dx = (double) x + 0.5D - entity.posX;

            for (int z = originZ - searchRadius; z <= originZ + searchRadius; ++z) {
                double dz = (double) z + 0.5D - entity.posZ;

                for (int y = 127; y >= 0; --y) {
                    if (!world.isAirBlock(x, y, z)) {
                        continue;
                    }
                    // Descend to the first solid block below this air column.
                    while (y > 0 && world.isAirBlock(x, y - 1, z)) {
                        --y;
                    }

                    for (int offset = orientation; offset < orientation + portalWidth; ++offset) {
                        int xOff = offset % 2;
                        int zOff = 1 - xOff;
                        if (offset % 4 >= 2) {
                            xOff = -xOff;
                            zOff = -zOff;
                        }

                        for (int depth = 0; depth < 3; ++depth) {
                            for (int across = 0; across < 4; ++across) {
                                for (int height = -1; height < portalHeight; ++height) {
                                    int bx = x + (across - 1) * xOff + depth * zOff;
                                    int by = y + height;
                                    int bz = z + (across - 1) * zOff - depth * xOff;
                                    boolean isEdge = height < 0;
                                    if (isEdge && !world.getBlockMaterial(bx, by, bz).isSolid()) {
                                        continue;
                                    }
                                    if (!isEdge && !world.isAirBlock(bx, by, bz)) {
                                        continue outerFirstPass;
                                    }
                                }
                            }
                        }

                        double dy = (double) y + 0.5D - entity.posY;
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (closestDistSq < 0.0D || distSq < closestDistSq) {
                            closestDistSq = distSq;
                            bestX = x;
                            bestY = y;
                            bestZ = z;
                            bestPortalOffset = offset % 4;
                        }
                    }
                }
            }
        }

        // Second pass: fall back to a narrower 2×3 portal if the first pass found nothing.
        if (closestDistSq < 0.0D) {
            for (int x = originX - searchRadius; x <= originX + searchRadius; ++x) {
                double dx = (double) x + 0.5D - entity.posX;

                for (int z = originZ - searchRadius; z <= originZ + searchRadius; ++z) {
                    double dz = (double) z + 0.5D - entity.posZ;

                    for (int y = 127; y >= 0; --y) {
                        if (!world.isAirBlock(x, y, z)) {
                            continue;
                        }
                        while (world.isAirBlock(x, y - 1, z)) {
                            --y;
                        }

                        for (int offset = orientation; offset < orientation + 2; ++offset) {
                            int xOff = offset % 2;
                            int zOff = 1 - xOff;

                            for (int across = 0; across < 4; ++across) {
                                for (int height = -1; height < 3; ++height) {
                                    int bx = x + (across - 1) * xOff;
                                    int by = y + height;
                                    int bz = z + (across - 1) * zOff;
                                    if (height < 0 && !world.getBlockMaterial(bx, by, bz).isSolid()) {
                                        continue;
                                    }
                                    if (!world.isAirBlock(bx, by, bz)) {
                                        continue;
                                    }
                                }
                            }

                            double dy = (double) y + 0.5D - entity.posY;
                            double distSq = dx * dx + dy * dy + dz * dz;
                            if (closestDistSq < 0.0D || distSq < closestDistSq) {
                                closestDistSq = distSq;
                                bestX = x;
                                bestY = y;
                                bestZ = z;
                                bestPortalOffset = offset % 2;
                            }
                        }
                    }
                }
            }
        }

        int destX = bestX;
        int destY = bestY;
        int destZ = bestZ;
        int portalOffset = bestPortalOffset;
        int xSign = portalOffset % 2;
        int zSign = 1 - xSign;
        if (portalOffset % 4 >= 2) {
            xSign = -xSign;
            zSign = -zSign;
        }

        // Place obsidian floor if no suitable floor was found.
        if (closestDistSq < 0.0D) {
            if (bestY < 70) bestY = 70;
            if (bestY > 118) bestY = 118;
            destY = bestY;

            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    int bx = destX + (1) * xSign + dx * zSign;
                    int by = destY + dz;
                    int bz = destZ + (1) * zSign - dx * xSign;
                    boolean isFloor = dz < 0;
                    world.setBlockWithNotify(bx, by, bz, isFloor ? Block.obsidian.blockID : 0);
                }
            }
        }

        // Build the portal frame and fill with portal blocks.
        for (int frameX = 0; frameX < portalWidth; ++frameX) {
            world.editingBlocks = true;

            for (int frameZ = 0; frameZ < portalWidth; ++frameZ) {
                for (int height = -1; height < portalHeight; ++height) {
                    int bx = destX + (frameZ - 1) * xSign;
                    int by = destY + height;
                    int bz = destZ + (frameZ - 1) * zSign;
                    boolean isFrame = frameZ == 0 || frameZ == 3 || height == -1 || height == portalHeight - 1;
                    world.setBlockWithNotify(bx, by, bz, isFrame ? Block.obsidian.blockID : Block.portal.blockID);
                }
            }

            world.editingBlocks = false;

            for (int frameZ = 0; frameZ < portalWidth; ++frameZ) {
                for (int height = -1; height < portalHeight; ++height) {
                    int bx = destX + (frameZ - 1) * xSign;
                    int by = destY + height;
                    int bz = destZ + (frameZ - 1) * zSign;
                    world.notifyBlocksOfNeighborChange(bx, by, bz, world.getBlockId(bx, by, bz));
                }
            }
        }

        return true;
    }
}
