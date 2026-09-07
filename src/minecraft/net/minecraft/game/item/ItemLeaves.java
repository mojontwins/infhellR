package net.minecraft.game.item;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.ColorizerFoliage;

public class ItemLeaves extends ItemBlock {
	public ItemLeaves(int i1) {
		super(i1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	public int getPlacedBlockMetadata(int i1) {
		return i1 | 8;
	}

	public int getIconFromDamage(int i1) {
		return Block.leaves.getBlockTextureFromSideAndMetadata(0, i1);
	}

	public int getColorFromDamage(int i1) {
		return (i1 & 1) == 1 ? ColorizerFoliage.getFoliageColorPine() : ((i1 & 2) == 2 ? ColorizerFoliage.getFoliageColorBirch() : ColorizerFoliage.getFoliageColorBasic());
	}
}
