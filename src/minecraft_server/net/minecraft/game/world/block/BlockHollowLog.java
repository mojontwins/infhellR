package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockHollowLog extends Block {
	public static final int[] textureIndex = new int[] { 13*16 + 9, 13*16 + 8, 20 };
	
	protected BlockHollowLog(int id, int textureIndex) {
		super(id, textureIndex, Material.wood);	
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}
	
	public int getBlockTextureFromSide(int side) {
		// The custom renderer will ask for:
		// side 0 meaning inside,
		// side 1 meaning ends,
		// side 2 meaning side,
		return textureIndex[side];
	}
	
	public void onBlockPlaced(World world, int x, int y, int z, int side) {
		// If side == 0 or side == 1 it will set metadata = 0, as in vanilla (vertical)
		// Otherwise, it will set metadata to side (horizontal).
		world.setBlockMetadata(x, y, z, side <= 1 ? 0 : side);
	}
    
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	public boolean isOpaqueCube() {
		return false;
	}

	public int getRenderType() {
		return 102;
	}
	
	public boolean canGrowMushrooms() {
		return true;
	}
}
