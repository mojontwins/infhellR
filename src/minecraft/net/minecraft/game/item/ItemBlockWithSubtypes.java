package net.minecraft.game.item;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.IBlockWithSubtypes;

public class ItemBlockWithSubtypes extends ItemBlock {
	IBlockWithSubtypes refBlock = null;
	
	// Id is set so it substitutes the ItemBlock
	public ItemBlockWithSubtypes(IBlockWithSubtypes ref, int id) {
		super(id - 256);
		this.refBlock = ref;
		
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		System.out.println ("Assigned ItemBlockWithSubtypes to " +
				Block.blocksList[this.blockID].getBlockName() + " tab = " + this.getCreativeTab() +".");
	}

	// Placed block metadata as is
	@Override
	public int getPlacedBlockMetadata(int damage) {
		return damage;
	}
	
	// Icon from damage depends on the block type
	@Override
	public int getIconFromDamage(int damage) {
		return refBlock.getIndexInTextureFromMeta(damage);
	}
	
	@Override
	public String getItemNameIS(ItemStack stack) {
		return "tile." + this.refBlock.getNameFromMeta(stack.getItemDamage());
	}

	@Override
	public int getColorFromDamage(int damage) {
		return refBlock.getRenderColor(damage);
	}
}
