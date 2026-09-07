package net.minecraft.game.world.terrain.generate;

import java.util.Random;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

// Adapted from r1.2.5

public class WorldGenBigMushroom extends WorldGenerator {
    private int mushroomType; // 0 for brown, 1 for red.

    public WorldGenBigMushroom(int mushroomType) {
        this.mushroomType = mushroomType;
    }

    public WorldGenBigMushroom() {
    	this (0);
    }

    public boolean generate(World world, Random rand, int x, int y, int z)
    {
    	int mushroomBlockID;
    	switch(mushroomType) {
    		case 1: mushroomBlockID = Block.mushroomCapRed.blockID; break;
    		case 2: mushroomBlockID = Block.mushroomCapGreen.blockID; break;
    		default: mushroomBlockID = Block.mushroomCapBrown.blockID; break;
    	}

        int j = rand.nextInt(3) + 4;

        if (y < 1 || y + j + 1 >= 127) {
            return false;
        }

        // Enough room ?

        for (int yy = y; yy <= y + 1 + j; yy++) {
        	/*
            byte byte0 = 3;

            if (yy == y) {
                byte0 = 0;
            }
            */
        	byte byte0 = (byte) ((yy < y + j - 3) ? 0 : 3); 

            for (int xx = x - byte0; xx <= x + byte0; xx++) {
                for (int zz = z - byte0; zz <= z + byte0; zz++) {
                    if (yy >= 0 && yy < 127) {
                        int blockID = world.getBlockId(xx, yy, zz);

                        if (blockID != 0 && blockID != Block.leaves.blockID) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }

        // Dirt, grass or podzol below?
        int blockIDbelow = world.getBlockId(x, y - 1, z);
        if (blockIDbelow == 0) return false;
        
        Block block = Block.blocksList[blockIDbelow];
        if(block == null) return false;
        if(!block.canGrowPlants() && !block.canGrowMushrooms() && blockIDbelow != Block.stone.blockID) return false;

        // Change for dirt
        world.setBlock(x, y - 1, z, Block.dirt.blockID);
        world.setBlockMetadata(x, y - 1, z, 0);
        
        // Draw the cap
        int i1 = y + j;

        if (mushroomType != 0) {
            i1 = (y + j) - 3;
        }

        for (int k1 = i1; k1 <= y + j; k1++) {
            int j2 = 1;

            if (k1 < y + j) {
                j2++;
            }

            if (mushroomType == 0) {
                j2 = 3;
            }

            for (int i3 = x - j2; i3 <= x + j2; i3++) {
                for (int j3 = z - j2; j3 <= z + j2; j3++) {
                    int k3 = 5;

                    if (i3 == x - j2) {
                        k3--;
                    }

                    if (i3 == x + j2) {
                        k3++;
                    }

                    if (j3 == z - j2) {
                        k3 -= 3;
                    }

                    if (j3 == z + j2) {
                        k3 += 3;
                    }

                    if (mushroomType == 0 || k1 < y + j) {
                        if ((i3 == x - j2 || i3 == x + j2) && (j3 == z - j2 || j3 == z + j2)) {
                            continue;
                        }

                        if ((i3 == x - (j2 - 1) && j3 == z - j2) || (i3 == x - j2 && j3 == z - (j2 - 1))) {
                            k3 = 1;
                        }

                        if ((i3 == x + (j2 - 1) && j3 == z - j2) || (i3 == x + j2 && j3 == z - (j2 - 1))) {
                            k3 = 3;
                        }

                        if ((i3 == x - (j2 - 1) && j3 == z + j2) || (i3 == x - j2 && j3 == z + (j2 - 1))) {
                            k3 = 7;
                        }

                        if ((i3 == x + (j2 - 1) && j3 == z + j2) || (i3 == x + j2 && j3 == z + (j2 - 1))) {
                            k3 = 9;
                        }
                    }

                    if (k3 == 5 && k1 < y + j) {
                        k3 = 0;
                    }

                    if ((k3 != 0 || y >= (y + j) - 1) && !Block.opaqueCubeLookup[world.getBlockId(i3, k1, j3)]) {
                        world.setBlockWithNotify(i3, k1, j3, mushroomBlockID);
                        world.setBlockMetadata(i3, k1, j3, k3);
                    }
                }
            }
        }

        // Trunk
        for (int l1 = 0; l1 < j; l1++) {
            int k2 = world.getBlockId(x, y + l1, z);

            if (!Block.opaqueCubeLookup[k2]) {
                world.setBlockWithNotify(x, y + l1, z, mushroomBlockID);
                world.setBlockMetadata(x, y + l1, z, 10);
            }
        }

        return true;
    }
}
