package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.Direction;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.item.ItemSword;

public class BlockVine extends Block {
	public BlockVine(int par1, int par2) {
        super(par1, par2, Material.vine);
        
        this.displayOnCreativeTab = CreativeTabs.tabDeco;
    }

	public boolean findForbiddenBlock(int blockID) {
		return /*blockID == 0 ||*/
				blockID == Block.leaves.blockID ||
				blockID == Block.mycelium.blockID ||
				blockID == Block.ice.blockID ||
				blockID == Block.blockSnow.blockID ||
				blockID == Block.chest.blockID ||
				blockID == Block.mobSpawner.blockID ||
				blockID == Block.mobSpawnerOneshot.blockID;	
	}
	
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public int getRenderType() {
        return 20;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
        int i = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
        float f = 1.0F;
        float f1 = 1.0F;
        float f2 = 1.0F;
        float f3 = 0.0F;
        float f4 = 0.0F;
        float f5 = 0.0F;
        boolean flag = i > 0;

        if ((i & 2) != 0) {
            f3 = Math.max(f3, 0.0625F);
            f = 0.0F;
            f1 = 0.0F;
            f4 = 1.0F;
            f2 = 0.0F;
            f5 = 1.0F;
            flag = true;
        }

        if ((i & 8) != 0) {
            f = Math.min(f, 0.9375F);
            f3 = 1.0F;
            f1 = 0.0F;
            f4 = 1.0F;
            f2 = 0.0F;
            f5 = 1.0F;
            flag = true;
        }

        if ((i & 4) != 0) {
            f5 = Math.max(f5, 0.0625F);
            f2 = 0.0F;
            f = 0.0F;
            f3 = 1.0F;
            f1 = 0.0F;
            f4 = 1.0F;
            flag = true;
        }

        if ((i & 1) != 0) {
            f2 = Math.min(f2, 0.9375F);
            f5 = 1.0F;
            f = 0.0F;
            f3 = 1.0F;
            f1 = 0.0F;
            f4 = 1.0F;
            flag = true;
        }

        if (!flag && canBePlacedOn(par1IBlockAccess.getBlockId(par2, par3 + 1, par4))) {
            f1 = Math.min(f1, 0.9375F);
            f4 = 1.0F;
            f = 0.0F;
            f3 = 1.0F;
            f2 = 0.0F;
            f5 = 1.0F;
        }

        setBlockBounds(f, f1, f2, f3, f4, f5);
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int i) {
        return null;
    }

    public boolean canPlaceBlockOnSide(World par1World, int par2, int par3, int par4, int par5) {
        switch (par5) {
            default:
                return false;

            case 1:
                return canBePlacedOn(par1World.getBlockId(par2, par3 + 1, par4));

            case 2:
                return canBePlacedOn(par1World.getBlockId(par2, par3, par4 + 1));

            case 3:
                return canBePlacedOn(par1World.getBlockId(par2, par3, par4 - 1));

            case 5:
                return canBePlacedOn(par1World.getBlockId(par2 - 1, par3, par4));

            case 4:
                return canBePlacedOn(par1World.getBlockId(par2 + 1, par3, par4));
        }
    }

    private boolean canBePlacedOn(int par1) {
        if (this.findForbiddenBlock(par1)) {
            return false;
        }

        Block block = Block.blocksList[par1];
        return block != null && block.renderAsNormalBlock() && block.blockMaterial.getIsSolid();
    }

    private boolean canVineStay(World par1World, int par2, int par3, int par4) {
        int i = par1World.getBlockMetadata(par2, par3, par4);
        int j = i;

        if (j > 0) {
            for (int k = 0; k <= 3; k++) {
                int l = 1 << k;

                if (
                	(i & l) != 0 && 
                	!canBePlacedOn(par1World.getBlockId(par2 + Direction.offsetX[k], par3, par4 + Direction.offsetZ[k])) && 
                	(par1World.getBlockId(par2, par3 + 1, par4) != blockID || (par1World.getBlockMetadata(par2, par3 + 1, par4) & l) == 0)
                ) {
                    j &= ~l;
                }
            }
        }

        if (j == 0 && !canBePlacedOn(par1World.getBlockId(par2, par3 + 1, par4))) {
            return false;
        }

        if (j != i) {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, j);
        }

        return true;
    }

    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
        if (!par1World.isRemote && !canVineStay(par1World, par2, par3, par4)) {
            this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4));
            par1World.setBlockWithNotify(par2, par3, par4, 0);
        }
    }

    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (!par1World.isRemote && par1World.rand.nextInt(4) == 0) {
            byte byte0 = 4;
            int i = 5;
            boolean flag = false;
            int j = par2 - byte0;
            label0:

            do {
                if (j > par2 + byte0) {
                    break;
                }

                label1:

                for (int k = par4 - byte0; k <= par4 + byte0; k++) {
                    int i1 = par3 - 1;

                    do {
                        if (i1 > par3 + 1) {
                            continue label1;
                        }

                        if (par1World.getBlockId(j, i1, k) == blockID && --i <= 0) {
                            flag = true;
                            break label0;
                        }

                        i1++;
                    } while (true);
                }

                j++;
            } while (true);

            j = par1World.getBlockMetadata(par2, par3, par4);
            int l = par1World.rand.nextInt(6);
            int j1 = Direction.vineGrowth[l];

            if (l == 1 && par3 < 255 && par1World.getBlockId(par2, par3 + 1, par4) == 0) {
                if (flag) {
                    return;
                }

                int k1 = par1World.rand.nextInt(16) & j;

                if (k1 > 0) {
                    for (int j2 = 0; j2 <= 3; j2++) {
                        if (!canBePlacedOn(par1World.getBlockId(par2 + Direction.offsetX[j2], par3 + 1, par4 + Direction.offsetZ[j2]))) {
                            k1 &= ~(1 << j2);
                        }
                    }

                    if (k1 > 0) {
                        par1World.setBlockAndMetadataWithNotify(par2, par3 + 1, par4, blockID, k1);
                    }
                }
            } else if (l >= 2 && l <= 5 && (j & 1 << j1) == 0) {
                if (flag) {
                    return;
                }

                int l1 = par1World.getBlockId(par2 + Direction.offsetX[j1], par3, par4 + Direction.offsetZ[j1]);

                if (l1 == 0 || Block.blocksList[l1] == null) {
                    int k2 = j1 + 1 & 3;
                    int j3 = j1 + 3 & 3;

                    if ((j & 1 << k2) != 0 && canBePlacedOn(par1World.getBlockId(par2 + Direction.offsetX[j1] + Direction.offsetX[k2], par3, par4 + Direction.offsetZ[j1] + Direction.offsetZ[k2]))) {
                        par1World.setBlockAndMetadataWithNotify(par2 + Direction.offsetX[j1], par3, par4 + Direction.offsetZ[j1], blockID, 1 << k2);
                    } else if ((j & 1 << j3) != 0 && canBePlacedOn(par1World.getBlockId(par2 + Direction.offsetX[j1] + Direction.offsetX[j3], par3, par4 + Direction.offsetZ[j1] + Direction.offsetZ[j3]))) {
                        par1World.setBlockAndMetadataWithNotify(par2 + Direction.offsetX[j1], par3, par4 + Direction.offsetZ[j1], blockID, 1 << j3);
                    } else if ((j & 1 << k2) != 0 && 0 == par1World.getBlockId(par2 + Direction.offsetX[j1] + Direction.offsetX[k2], par3, par4 + Direction.offsetZ[j1] + Direction.offsetZ[k2]) && canBePlacedOn(par1World.getBlockId(par2 + Direction.offsetX[k2], par3, par4 + Direction.offsetZ[k2]))) {
                        par1World.setBlockAndMetadataWithNotify(par2 + Direction.offsetX[j1] + Direction.offsetX[k2], par3, par4 + Direction.offsetZ[j1] + Direction.offsetZ[k2], blockID, 1 << (j1 + 2 & 3));
                    } else if ((j & 1 << j3) != 0 && 0 == par1World.getBlockId(par2 + Direction.offsetX[j1] + Direction.offsetX[j3], par3, par4 + Direction.offsetZ[j1] + Direction.offsetZ[j3]) && canBePlacedOn(par1World.getBlockId(par2 + Direction.offsetX[j3], par3, par4 + Direction.offsetZ[j3]))) {
                        par1World.setBlockAndMetadataWithNotify(par2 + Direction.offsetX[j1] + Direction.offsetX[j3], par3, par4 + Direction.offsetZ[j1] + Direction.offsetZ[j3], blockID, 1 << (j1 + 2 & 3));
                    } else if (canBePlacedOn(par1World.getBlockId(par2 + Direction.offsetX[j1], par3 + 1, par4 + Direction.offsetZ[j1]))) {
                        par1World.setBlockAndMetadataWithNotify(par2 + Direction.offsetX[j1], par3, par4 + Direction.offsetZ[j1], blockID, 0);
                    }
                } else if (Block.blocksList[l1].blockMaterial.getIsSolid() && Block.blocksList[l1].renderAsNormalBlock()) {
                    par1World.setBlockMetadataWithNotify(par2, par3, par4, j | 1 << j1);
                }
            } else if (par3 > 1) {
                int i2 = par1World.getBlockId(par2, par3 - 1, par4);

                if (i2 == 0) {
                    int l2 = par1World.rand.nextInt(16) & j;

                    if (l2 > 0) {
                        par1World.setBlockAndMetadataWithNotify(par2, par3 - 1, par4, blockID, l2);
                    }
                } else if (i2 == blockID) {
                    int i3 = par1World.rand.nextInt(16) & j;
                    int k3 = par1World.getBlockMetadata(par2, par3 - 1, par4);

                    if (k3 != (k3 | i3)) {
                        par1World.setBlockMetadataWithNotify(par2, par3 - 1, par4, k3 | i3);
                    }
                }
            }
        }
    }

    /**
     * Called when a block is placed using an item. Used often for taking the facing and figuring out how to position
     * the item. Args: x, y, z, facing
     */
    public void onBlockPlaced(World par1World, int par2, int par3, int par4, int par5) {
        byte byte0 = 0;

        switch (par5) {
            case 2:
                byte0 = 1;
                break;

            case 3:
                byte0 = 4;
                break;

            case 4:
                byte0 = 8;
                break;

            case 5:
                byte0 = 2;
                break;
        }

        if (byte0 != 0) {
            par1World.setBlockMetadataWithNotify(par2, par3, par4, byte0);
        }
    }

    public int idDropped(int par1, Random par2Random, int par3) {
        return 0;
    }

    public int quantityDropped(Random par1Random) {
        return 0;
    }

    public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6) {
    	ItemStack itemStack = par2EntityPlayer.getCurrentEquippedItem();
    	if (itemStack != null) {
    		Item item = itemStack.getItem();
	        if (!par1World.isRemote && item != null && item instanceof ItemSword) {
	            dropBlockAsItem_do(par1World, par3, par4, par5, new ItemStack(this.blockID, 1, 0));
	            return;
	        }
    	}
        
        super.harvestBlock(par1World, par2EntityPlayer, par3, par4, par5, par6);
    }
    
	public boolean isClimbable() {
		return true;
	}
	
	public boolean seeThrough() {
		return true; 
	}
}
