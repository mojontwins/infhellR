package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.material.Material;

public class BlockBone extends BlockLog {

	public BlockBone(int i1) {
		super(i1, Material.bone);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		boolean horizontal = (meta & 4) != 0;
		
		int outTextureIndex = 11 * 16 + 15;
		int endTextureIndex = 11 * 16 + 9;
		
		if (!horizontal) {
			// Vanilla logs:
			return side <= 1 ? endTextureIndex : outTextureIndex;
		} else {
			// Horizontal logs:
			if(side <= 1) return outTextureIndex;
			
			if((meta & 8) != 0) {
				return (side == 4 || side == 5) ? endTextureIndex : outTextureIndex;
			} else {
				return (side == 2 || side == 3) ? endTextureIndex : outTextureIndex;
			}
		}
	}
}
