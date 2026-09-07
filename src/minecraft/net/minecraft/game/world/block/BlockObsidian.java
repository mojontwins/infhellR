package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;

public class BlockObsidian extends BlockStone {
	public BlockObsidian(int i1, int i2) {
		super(i1, i2);
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int quantityDropped(Random rand) {
		return 1;
	}

	public int idDropped(int metadata, Random rand) {
		return Block.obsidian.blockID;
	}
}
