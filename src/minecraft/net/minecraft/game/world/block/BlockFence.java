package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockFence extends Block {
	public BlockFence(int i1, int i2) {
		super(i1, i2, Material.wood);
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}

	public BlockFence(int blockID, int blockIndex, Material material) {
		super(blockID, blockIndex, material);
	}
	
	/*
	public void getCollidingBoundingBoxes(World world, int x, int y, int z, AxisAlignedBB aabb, ArrayList<AxisAlignedBB> collidingBoundingBoxes) {
		collidingBoundingBoxes.add(AxisAlignedBB.getBoundingBoxFromPool((double)x, (double)y, (double)z, (double)(x + 1), (double)y + 1.5D, (double)(z + 1)));
	}
	*/

	/*
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return world.getBlockId(x, y - 1, z) == this.blockID ? true : (!world.getBlockMaterial(x, y - 1, z).isSolid() ? false : super.canPlaceBlockAt(world, x, y, z));
	}
	*/
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world1, int i2, int i3, int i4) {
		boolean z5 = this.canConnectFenceTo(world1, i2, i3, i4 - 1);
		boolean z6 = this.canConnectFenceTo(world1, i2, i3, i4 + 1);
		boolean z7 = this.canConnectFenceTo(world1, i2 - 1, i3, i4);
		boolean z8 = this.canConnectFenceTo(world1, i2 + 1, i3, i4);
		float f9 = 0.375F;
		float f10 = 0.625F;
		float f11 = 0.375F;
		float f12 = 0.625F;
		if(z5) {
			f11 = 0.0F;
		}

		if(z6) {
			f12 = 1.0F;
		}

		if(z7) {
			f9 = 0.0F;
		}

		if(z8) {
			f10 = 1.0F;
		}

		return AxisAlignedBB.getBoundingBoxFromPool((double)((float)i2 + f9), (double)i3, (double)((float)i4 + f11), (double)((float)i2 + f10), (double)((float)i3 + 1.5F), (double)((float)i4 + f12));
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		boolean z5 = this.canConnectFenceTo(iBlockAccess1, i2, i3, i4 - 1);
		boolean z6 = this.canConnectFenceTo(iBlockAccess1, i2, i3, i4 + 1);
		boolean z7 = this.canConnectFenceTo(iBlockAccess1, i2 - 1, i3, i4);
		boolean z8 = this.canConnectFenceTo(iBlockAccess1, i2 + 1, i3, i4);
		float f9 = 0.375F;
		float f10 = 0.625F;
		float f11 = 0.375F;
		float f12 = 0.625F;
		if(z5) {
			f11 = 0.0F;
		}

		if(z6) {
			f12 = 1.0F;
		}

		if(z7) {
			f9 = 0.0F;
		}

		if(z8) {
			f10 = 1.0F;
		}

		this.setBlockBounds(f9, 0.0F, f11, f10, 1.0F, f12);
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
		return 11;
	}
	
	public boolean seeThrough() {
		return true; 
	}
	
	@Override
	public boolean getBlocksMovement(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		return false;
	}
	
	public boolean canConnectFenceTo(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		/*
		int i5 = iBlockAccess1.getBlockId(i2, i3, i4);
		if(i5 != this.blockID) {
			Block block6 = Block.blocksList[i5];
			return block6 != null && block6.isOpaqueCube() && block6.renderAsNormalBlock() ? block6.blockMaterial != Material.pumpkin : false;
		} else {
			return true;
		}
		*/
		return iBlockAccess1.getBlockId(i2, i3, i4) == this.blockID;
	}
	
	// Only wooden fences burn; the iron fence (iron material) does not.
	@Override
	public int getEncouragementToFire() {
		return this.blockMaterial == Material.wood ? 5 : 0;
	}

	@Override
	public int getAbilityToCatchFire() {
		return this.blockMaterial == Material.wood ? 20 : 0;
	}
}
