package net.minecraft.game.world.block;

import net.minecraft.game.MathHelper;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockPumpkin extends Block {
	private boolean isLantern;

	protected BlockPumpkin(int i1, int i2, boolean z3) {
		super(i1, Material.pumpkin);
		this.blockIndexInTexture = i2;
		this.setTickOnLoad(true);
		this.isLantern = z3;
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		if(side == 1) {
			return this.blockIndexInTexture;
		} else if(side == 0) {
			return this.blockIndexInTexture;
		} else {
			int var3 = this.blockIndexInTexture + 1 + 16;
			if(this.isLantern) {
				++var3;
			}

			if(meta == 0) meta = 3;
			
			return side != meta ? this.blockIndexInTexture + 16 : var3;
		}
	}

	public int getBlockTextureFromSide(int i1) {
		return i1 == 1 ? this.blockIndexInTexture : (i1 == 0 ? this.blockIndexInTexture : (i1 == 3 ? this.blockIndexInTexture + 1 + 16 : this.blockIndexInTexture + 16));
	}

	public void onBlockAdded(World world1, int i2, int i3, int i4) {
		super.onBlockAdded(world1, i2, i3, i4);
	}

	public boolean canPlaceBlockAt(World world1, int i2, int i3, int i4) {
		int i5 = world1.getBlockId(i2, i3, i4);
		return (i5 == 0 || Block.blocksList[i5].blockMaterial.getIsGroundCover()) && world1.isBlockNormalCube(i2, i3 - 1, i4);
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
}
