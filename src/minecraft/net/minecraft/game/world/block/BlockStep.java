package net.minecraft.game.world.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.Facing;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockStep extends Block {
	public static final String[] blockStepTypes = new String[]{"stone", "sand", "wood", "cobble"};
	private boolean blockIsDouble;

	public BlockStep(int id, boolean blockIsDouble) {
		super(id, 6, Material.rock);
		this.blockIsDouble = blockIsDouble;
		if(!blockIsDouble) {
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		}

		this.setLightOpacity(255);
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
		public void setBlockBoundsBasedOnState (IBlockAccess blockAccess, int x, int y, int z) {
		if (this.blockIsDouble) {
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		} else {
			if ((blockAccess.getBlockMetadata(x, y, z) & 8) != 0) {
				// Upper half
				this.setBlockBounds(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
			} else {
				// Bottom half
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
			}
		}
	}
	
	public void setBlockBoundsForItemRender() {
		if (this.blockIsDouble) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        } else {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        }
	}
	
	public void getCollidingBoundingBoxes(World world, int x, int y, int z, AxisAlignedBB bb, ArrayList<AxisAlignedBB> bbList) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		super.getCollidingBoundingBoxes(world, x, y, z, bb, bbList);
	}

	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
		switch(metadata & 7) {
			case 1: return 192;
			case 2: return 4;
			case 3: return 16;
			case 4: return 6;			
			default: return side <= 1 ? 6 : 5;
		}
	}

	public int getBlockTextureFromSide(int i1) {
		return this.getBlockTextureFromSideAndMetadata(i1, 0);
	}

	public boolean isOpaqueCube() {
		return this.blockIsDouble;
	}

	/*
	public void onBlockAdded(World world1, int i2, int i3, int i4) {
		if(this != Block.stairSingle) {
			super.onBlockAdded(world1, i2, i3, i4);
		}

		int i5 = world1.getBlockId(i2, i3 - 1, i4);
		int i6 = world1.getBlockMetadata(i2, i3, i4);
		int i7 = world1.getBlockMetadata(i2, i3 - 1, i4);

		if(i6 == i7) {
			if(i5 == stairSingle.blockID) {
				world1.setBlockWithNotify(i2, i3, i4, 0);
				world1.setBlockAndMetadataWithNotify(i2, i3 - 1, i4, Block.stairDouble.blockID, i6);
			}

		}
	}
	*/
	
	public void onBlockPlaced(World world, int x, int y, int z, int side, float xWithinSide, float yWithinSide, float zWithinSide)	{
		if (this.blockIsDouble) return;
		if (side == 0 || side != 1 && yWithinSide > 0.5F) {
			int i = world.getBlockMetadata(x, y, z) & 7;
			world.setBlockMetadataWithNotify(x, y, z, i | 8);
		}
	}
	
	public boolean renderAsNormalBlock() {
		return this.blockIsDouble;
	}
	
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		if(this != Block.stairSingle) {
			super.shouldSideBeRendered(blockAccess, x, y, z, side);
		}

		//return side == 1 ? true : (!super.shouldSideBeRendered(blockAccess, x, y, z, side) ? false : (side == 0 ? true : blockAccess.getBlockId(x, y, z) != this.blockID));
		
		if (side != 1 && side != 0 && !super.shouldSideBeRendered(blockAccess, x, y, z, side)) {
			return false;
		}

		int i = x;
		int j = y;
		int k = z;
		i += Facing.offsetsXForSide[Facing.faceToSide[side]];
		j += Facing.offsetsYForSide[Facing.faceToSide[side]];
		k += Facing.offsetsZForSide[Facing.faceToSide[side]];
		boolean flag = (blockAccess.getBlockMetadata(i, j, k) & 8) != 0;

		if (!flag) {
			if (side == 1) {
				return true;
			}

			if (side == 0 && super.shouldSideBeRendered(blockAccess, x, y, z, side)) {
				return true;
			} else {
				return blockAccess.getBlockId(x, y, z) != blockID || (blockAccess.getBlockMetadata(x, y, z) & 8) != 0;
			}
		}

		if (side == 0) {
			return true;
		}

		if (side == 1 && super.shouldSideBeRendered(blockAccess, x, y, z, side)) {
			return true;
		} else {
			return blockAccess.getBlockId(x, y, z) != blockID || (blockAccess.getBlockMetadata(x, y, z) & 8) == 0;
		}
	}

	public int idDropped(int metadata, Random rand) {
		return Block.stairSingle.blockID;
	}

	public int damageDropped(int meta) {
		return meta & 7;
	}

	public int quantityDropped(Random rand) {
		return this.blockIsDouble ? 2 : 1;
	}

	@Override
	public Material getBlockMaterialBasedOnmetaData(int meta) {
		Material material = this.blockMaterial;
		
		if((meta & 7) == 2) {
			material = Material.wood;
		}
		
		return material;
	}
	
	public boolean seeThrough() {
		return !blockIsDouble; 
	}
	
    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
    	if(this.blockIsDouble) {
    		par3List.add(new ItemStack(par1, 1, 0));
    	} else {
			for(int i = 0; i < 4; i ++) {
				par3List.add(new ItemStack(par1, 1, i));
			}
    	}
	}
}
