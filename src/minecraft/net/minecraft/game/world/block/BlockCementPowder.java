package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockCementPowder extends BlockSand {

	public BlockCementPowder(int id, int blockIndex) {
		super(id, blockIndex);
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if (!this.tryToBecomeCement(world, x, y, z)) super.onBlockAdded(world, x, y, z);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		if (!this.tryToBecomeCement(world, x, y, z)) super.onNeighborBlockChange(world, x, y, z, id);
	}
	
	public boolean tryToBecomeCement(World world, int x, int y, int z) {
		if (this.isTouchingWater(world, x, y, z)) {
			world.setBlock (x, y, z, Block.cement.blockID);
			return true;
		}
		
		return false;
	}
	
	public boolean isTouchingWater(World world, int x, int y, int z) {
		return (
				world.getBlockMaterial(x - 1, y, z) == Material.water || 
				world.getBlockMaterial(x + 1, y, z) == Material.water || 
				world.getBlockMaterial(x, y, z - 1) == Material.water || 
				world.getBlockMaterial(x, y, z + 1) == Material.water ||
				//world.getBlockMaterial(x, y - 1, z) == Material.water ||
				world.getBlockMaterial(x, y + 1, z) == Material.water 
		);
	}
}
