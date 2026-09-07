package net.minecraft.game.world.block;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockWorkbench extends Block {
	protected BlockWorkbench(int id) {
		super(id, Material.wood);
		this.blockIndexInTexture = 59;
		
		this.displayOnCreativeTab = CreativeTabs.tabDeco;
	}

	public int getBlockTextureFromSide(int side) {
		return side == 1 ? this.blockIndexInTexture - 16 : (side == 0 ? Block.planks.getBlockTextureFromSide(0) : (side != 2 && side != 4 ? this.blockIndexInTexture : this.blockIndexInTexture + 1));
	}

	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		if(world.isRemote) {
			return true;
		} else {
			entityPlayer.displayWorkbenchGUI(x, y, z);
			return true;
		}
	}
}
