package net.minecraft.game.world.block;

import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.misc.EntityFallingSand;

public class BlockSand extends Block {
	public static boolean fallInstantly = false;

	public BlockSand(int id, int blockIndex) {
		super(id, blockIndex, Material.sand);
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	/*
	public void onBlockAdded(World world, int x, int y, int z) {
		world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
	}

	public void updateTick(World world, int x, int y, int z, Random rand) {
		this.tryToFall(world, x, y, z);
	}
	
	public int tickRate() {
		return 3;
	}

	*/
	
	// Non-ticking version
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		this.tryToFall(world, x, y, z);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		this.tryToFall(world, x, y, z);
	}
	
	private void tryToFall(World world, int x, int y, int z) {
		if(canFallBelow(world, x, y - 1, z) && y >= 0) {
			byte b8 = 32;
			if(!fallInstantly && world.checkChunksExist(x - b8, y - b8, z - b8, x + b8, y + b8, z + b8)) {
				EntityFallingSand entityFallingSand9 = new EntityFallingSand(world, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, this.blockID);
				world.spawnEntityInWorld(entityFallingSand9);
			} else {
				world.setBlockWithNotify(x, y, z, 0);

				while(canFallBelow(world, x, y - 1, z) && y > 0) {
					--y;
				}

				if(y > 0) {
					world.setBlockWithNotify(x, y, z, this.blockID);
				}
			}
		}

	}

	public static boolean canFallBelow(World world0, int i1, int i2, int i3) {
		int i4 = world0.getBlockId(i1, i2, i3);
		if(i4 == 0) {
			return true;
		} else if(i4 == Block.fire.blockID) {
			return true;
		} else {
			Block block = Block.blocksList[i4];
			Material material5 = block == null ? Material.air : block.blockMaterial;
			return material5 == Material.water ? true : material5 == Material.lava;
		}
	}
}
