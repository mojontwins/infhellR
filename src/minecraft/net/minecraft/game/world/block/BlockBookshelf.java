package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.material.Material;

public class BlockBookshelf extends Block {
	public BlockBookshelf(int id, int blockIndex) {
		super(id, blockIndex, Material.wood);
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int getBlockTextureFromSide(int side) {
		return side <= 1 ? 4 : this.blockIndexInTexture;
	}

	public int quantityDropped(Random rand) {
		return 3;
	}
	
	public int idDrooped(int meta) {
		return Item.book.shiftedIndex;
	}
	
	@Override
	public int getEncouragementToFire() {
		return 30;
	}

	@Override
	public int getAbilityToCatchFire() {
		return 20;
	}
}
