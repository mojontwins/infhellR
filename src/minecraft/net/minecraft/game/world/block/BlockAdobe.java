package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.material.Material;

public class BlockAdobe extends Block {
	protected BlockAdobe(int id, int blockIndexInTexture) {
		super(id, blockIndexInTexture, Material.grass);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int idDropped(int metadata, Random rand) {
		return Block.dirt.blockID;
	}
}
