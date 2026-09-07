package net.minecraft.game.world.block;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockPistonExtension extends Block {
	private int headTexture = -1;

	public BlockPistonExtension(int i1, int i2) {
		super(i1, i2, Material.piston);
		this.setStepSound(soundStoneFootstep);
		this.setHardness(0.5F);
	}

	public void setHeadTexture(int i1) {
		this.headTexture = i1;
	}

	public void clearHeadTexture() {
		this.headTexture = -1;
	}

	public void onBlockRemoval(World world1, int i2, int i3, int i4) {
		super.onBlockRemoval(world1, i2, i3, i4);
		int i5 = world1.getBlockMetadata(i2, i3, i4);
		int i6 = PistonBlockTextures.faceToSide[getDirectionMeta(i5)];
		i2 += PistonBlockTextures.offsetsXForSide[i6];
		i3 += PistonBlockTextures.offsetsYForSide[i6];
		i4 += PistonBlockTextures.offsetsZForSide[i6];
		int i7 = world1.getBlockId(i2, i3, i4);
		if(i7 == Block.pistonBase.blockID || i7 == Block.pistonStickyBase.blockID) {
			i5 = world1.getBlockMetadata(i2, i3, i4);
			if(BlockPistonBase.isPowered(i5)) {
				Block.blocksList[i7].dropBlockAsItem(world1, i2, i3, i4, i5);
				world1.setBlockWithNotify(i2, i3, i4, 0);
			}
		}

	}

	public int getBlockTextureFromSideAndMetadata(int i1, int i2) {
		int i3 = getDirectionMeta(i2);
		return i1 == i3 ? (this.headTexture >= 0 ? this.headTexture : ((i2 & 8) != 0 ? this.blockIndexInTexture - 1 : this.blockIndexInTexture)) : (i1 == PistonBlockTextures.faceToSide[i3] ? 107 : 108);
	}

	public int getRenderType() {
		return 17;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean canPlaceBlockAt(World world1, int i2, int i3, int i4) {
		return false;
	}

	public boolean canPlaceBlockOnSide(World world1, int i2, int i3, int i4, int i5) {
		return false;
	}

	public int quantityDropped(Random random1) {
		return 0;
	}

	public void getCollidingBoundingBoxes(World world1, int i2, int i3, int i4, AxisAlignedBB axisAlignedBB5, ArrayList<AxisAlignedBB> arrayList6) {
		int i7 = world1.getBlockMetadata(i2, i3, i4);
		switch(getDirectionMeta(i7)) {
		case 0:
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			this.setBlockBounds(0.375F, 0.25F, 0.375F, 0.625F, 1.0F, 0.625F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			break;
		case 1:
			this.setBlockBounds(0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			this.setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 0.75F, 0.625F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			break;
		case 2:
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.25F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			this.setBlockBounds(0.25F, 0.375F, 0.25F, 0.75F, 0.625F, 1.0F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			break;
		case 3:
			this.setBlockBounds(0.0F, 0.0F, 0.75F, 1.0F, 1.0F, 1.0F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			this.setBlockBounds(0.25F, 0.375F, 0.0F, 0.75F, 0.625F, 0.75F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			break;
		case 4:
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.25F, 1.0F, 1.0F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			this.setBlockBounds(0.375F, 0.25F, 0.25F, 0.625F, 0.75F, 1.0F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			break;
		case 5:
			this.setBlockBounds(0.75F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
			this.setBlockBounds(0.0F, 0.375F, 0.25F, 0.75F, 0.625F, 0.75F);
			super.getCollidingBoundingBoxes(world1, i2, i3, i4, axisAlignedBB5, arrayList6);
		}

		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	public void setBlockBoundsBasedOnState(IBlockAccess iBlockAccess1, int i2, int i3, int i4) {
		int i5 = iBlockAccess1.getBlockMetadata(i2, i3, i4);
		switch(getDirectionMeta(i5)) {
		case 0:
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
			break;
		case 1:
			this.setBlockBounds(0.0F, 0.75F, 0.0F, 1.0F, 1.0F, 1.0F);
			break;
		case 2:
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.25F);
			break;
		case 3:
			this.setBlockBounds(0.0F, 0.0F, 0.75F, 1.0F, 1.0F, 1.0F);
			break;
		case 4:
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.25F, 1.0F, 1.0F);
			break;
		case 5:
			this.setBlockBounds(0.75F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}

	}

	public void onNeighborBlockChange(World world1, int i2, int i3, int i4, int i5) {
		int i6 = getDirectionMeta(world1.getBlockMetadata(i2, i3, i4));
		int i7 = world1.getBlockId(i2 - PistonBlockTextures.offsetsXForSide[i6], i3 - PistonBlockTextures.offsetsYForSide[i6], i4 - PistonBlockTextures.offsetsZForSide[i6]);
		if(i7 != Block.pistonBase.blockID && i7 != Block.pistonStickyBase.blockID) {
			world1.setBlockWithNotify(i2, i3, i4, 0);
		} else {
			Block.blocksList[i7].onNeighborBlockChange(world1, i2 - PistonBlockTextures.offsetsXForSide[i6], i3 - PistonBlockTextures.offsetsYForSide[i6], i4 - PistonBlockTextures.offsetsZForSide[i6], i5);
		}

	}

	public static int getDirectionMeta(int i0) {
		return i0 & 7;
	}
	
	// Softlocked for b1.6.6
	public boolean softLocked() {
		return true;
	}
}
