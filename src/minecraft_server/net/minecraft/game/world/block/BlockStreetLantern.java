package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockStreetLantern extends Block {
	private boolean broken = false;

	protected BlockStreetLantern(int id, int blockIndexInTexture, boolean broken) {
		super(id, blockIndexInTexture, Material.glass);
		this.broken = broken;
		
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}
	
	public int getBlockTextureFromSide(int side) {
		switch (side) {
			case 0:
			case 1:
				return 14*16+10;
			default:
				return 14*16+(this.broken ? 8 : 9);
		}
	}
	
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		float f5 = 0.0625F;
		return AxisAlignedBB.getBoundingBoxFromPool((double)((float)x + f5), (double)y, (double)((float)z + f5), (double)((float)(x + 1) - f5), (double)((float)(y + 1) - f5), (double)((float)(z + 1) - f5));
	}

	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		float f5 = 0.0625F;
		return AxisAlignedBB.getBoundingBoxFromPool((double)((float)x + f5), (double)y, (double)((float)z + f5), (double)((float)(x + 1) - f5), (double)(y + 1), (double)((float)(z + 1) - f5));
	}
	
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public int getRenderType() {
		return 104;
	}
	
	public int getRenderBlockPass() {
		return 2;
	}
	
	public int idDropped(int metadata, Random rand) {
		return Block.streetLanternBroken.blockID;
	}
}
