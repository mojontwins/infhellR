package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.material.Material;

public class BlockDirtPath extends Block {
	protected BlockDirtPath(int blockID, int textureIndex) {
		super(blockID, textureIndex, Material.grass);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	public int getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side) {
		if(side == 1) {
			return this.blockIndexInTexture + 1;
		} else if(side == 0) {
			return 2;
		} else {
			Material material6 = blockAccess.getBlockMaterial(x, y + 1, z);
			return material6 != Material.snow && material6 != Material.builtSnow ? this.blockIndexInTexture : 68;
		}
	}
	
	public int getBlockTextureFromSide(int side) {
		if(side == 1) {
			return this.blockIndexInTexture + 1;
		} else if(side == 0) {
			return 2;
		} else {
			return this.blockIndexInTexture;
		}
	}
	
	public int idDropped(int metadata, Random rand) {
		return Block.dirt.idDropped(0, rand);
	}
	
    public int getRenderType() {
    	return 103;
    }
    
    public boolean isOpaqueCube() {
        return false;
    }
}
