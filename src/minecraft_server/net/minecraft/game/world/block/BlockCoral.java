package net.minecraft.game.world.block;

import java.util.List;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockCoral extends Block implements IBlockWithSubtypes {
	/*
	 * Coral metadata will be 8, 9, 10 to make it compatible with flowing water.
	 */
	public BlockCoral(int id, int blockIndex) {
		super(id, blockIndex, Material.water); 

		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}
	
	@Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) == Block.waterStill.blockID 
        		&& world.getBlockId(x, y + 1, z) == Block.waterStill.blockID 
        		&& canThisPlantGrowOnThisBlockID(world.getBlockId(x, y - 1, z));
    }
    
    protected boolean canThisPlantGrowOnThisBlockID(int par1) {
    	return Block.opaqueCubeLookup[par1];
    }
    
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
        if (!canBlockStay(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
            world.setBlockWithNotify(x, y, z, Block.waterStill.blockID);
        }
    }
    
    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
    	int blockOnTop = world.getBlockId(x, y + 1, z);
    	if(! (blockOnTop == Block.waterStill.blockID || blockOnTop == Block.waterMoving.blockID)) return false;
    	return canThisPlantGrowOnThisBlockID(world.getBlockId(x, y - 1, z));
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int i) {
        return null;
    }
    
    @Override
    public boolean isOpaqueCube() {
        return false;
    }
    
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }
    
    @Override
    public int getRenderType() {
        return 1;
    }
    
    @Override
    public int getBlockTextureFromSideAndMetadata(int side, int meta) {
    	return this.blockIndexInTexture + (meta & 7);
    }
    
    @Override
	protected int damageDropped(int meta) {
		return meta;
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
		for(int i = 0; i < 3; i ++) {
			par3List.add(new ItemStack(par1, 1, 8 + i));
		}
	}

	@Override
	public String getNameFromMeta(int meta) {
		return "coral";
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return this.blockIndexInTexture + (meta & 7);
	}
}
