package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.item.ItemTool;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.World;

public class BlockTFMazestone extends Block {
	
	static int[] mimicIDs = new int[]{
			Block.stoneBricks.blockID, 
			Block.cobblestone.blockID, 
			Block.cobblestoneMossy.blockID
		};

	public BlockTFMazestone(int id, int texture) {
		super(id, texture, Material.rock);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		Block mimic = Block.blocksList[mimicIDs[meta]];
		return mimic != null && mimic.isOpaqueCube() ? mimic.getBlockTextureFromSide(side) : super.getBlockTextureFromSideAndMetadata(side, meta);
	}

	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer entityplayer, int x, int y, int z, int meta) {
		ItemStack itemStack = entityplayer.getCurrentEquippedItem();
		if(itemStack != null && itemStack.getItem() instanceof ItemTool) {
			itemStack.damageItem(8, entityplayer);
		}

		Block mimic = Block.blocksList[mimicIDs[meta]];
		if(mimic != null) {
			mimic.harvestBlock(world, entityplayer, x, y, z, 0);
		} else {
			super.harvestBlock(world, entityplayer, x, y, z, meta);
		}

	}
}
