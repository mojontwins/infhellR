package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.Item;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockReed extends Block {
	protected BlockReed(int id, int blockIndex) {
		super(id, Material.plants);
		this.blockIndexInTexture = blockIndex;
		float f3 = 0.375F;
		this.setBlockBounds(0.5F - f3, 0.0F, 0.5F - f3, 0.5F + f3, 1.0F, 0.5F + f3);
		this.setTickOnLoad(true);

		this.displayOnCreativeTab = CreativeTabs.tabDeco;	
	}

	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(world.getBlockId(x, y + 1, z) == 0) {
			int i6;
			for(i6 = 1; world.getBlockId(x, y - i6, z) == this.blockID; ++i6) {
			}

			if(i6 < 3) {
				int i7 = world.getBlockMetadata(x, y, z);
				if(i7 == 15) {
					world.setBlockWithNotify(x, y + 1, z, this.blockID);
					world.setBlockMetadataWithNotify(x, y, z, 0);
				} else {
					world.setBlockMetadataWithNotify(x, y, z, i7 + 1);
				}
			}
		}

	}

	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		int i5 = world.getBlockId(x, y - 1, z);
		return i5 == this.blockID ? true : (i5 != Block.grass.blockID && i5 != Block.dirt.blockID ? false : (world.getBlockMaterial(x - 1, y - 1, z) == Material.water ? true : (world.getBlockMaterial(x + 1, y - 1, z) == Material.water ? true : (world.getBlockMaterial(x, y - 1, z - 1) == Material.water ? true : world.getBlockMaterial(x, y - 1, z + 1) == Material.water))));
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		this.checkBlockCoordValid(world, x, y, z);
	}

	protected final void checkBlockCoordValid(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
			world.setBlockWithNotify(x, y, z, 0);
		}

	}

	public boolean canBlockStay(World world, int x, int y, int z) {
		return this.canPlaceBlockAt(world, x, y, z);
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	public int idDropped(int metadata, Random rand) {
		return Item.reed.shiftedIndex;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return 1;
	}
	
	public boolean seeThrough() {
		return true; 
	}
}
