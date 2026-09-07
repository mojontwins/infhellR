package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.MathHelper;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockWall extends Block {
	protected BlockWall(int blockID, int blockIndex) {
		super(blockID, blockIndex, Material.iron);
		
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}
	
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		int i5 = world.getBlockMetadata(x, y, z);
		float f6 = 0.125F;
		if(i5 == 2 || i5 == 3) {
			this.setBlockBounds(0.0F, 0.0F, 0.5F - f6, 1.0F, 1.0F, 0.5F + f6);
		} else if(i5 == 4 || i5 == 5) {
			this.setBlockBounds(0.5F - f6, 0.0F, 0.0F, 0.5F + f6, 1.0F, 1.0F);
		} 

		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}
	
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		int i5 = world.getBlockMetadata(x, y, z);
		float f6 = 0.125F;
		if(i5 == 2 || i5 == 3) {
			this.setBlockBounds(0.0F, 0.0F, 0.5F - f6, 1.0F, 1.0F, 0.5F + f6);
		} else if(i5 == 4 || i5 == 5) {
			this.setBlockBounds(0.5F - f6, 0.0F, 0.0F, 0.5F + f6, 1.0F, 1.0F);
		} 

		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}
	
	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return 100;
	}
	
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityLiving) {
        int i = MathHelper.floor_double((double)((entityLiving.rotationYaw * 4F) / 360F) + 0.5D) & 3;
        switch (i) {
        	case 0: world.setBlockMetadata(x, y, z, 2); break;
        	case 1: world.setBlockMetadata(x, y, z, 5); break;
        	case 2: world.setBlockMetadata(x, y, z, 3); break;
        	case 3: world.setBlockMetadata(x, y, z, 4); break;
        }
    }
    
	public int quantityDropped(Random rand) {
		return 1;
	}
}
