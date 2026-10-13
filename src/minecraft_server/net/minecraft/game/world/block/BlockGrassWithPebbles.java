package net.minecraft.game.world.block;

import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;

public class BlockGrassWithPebbles extends BlockGrass {
	public BlockGrassWithPebbles(int blockID) {
		super(blockID);
	}

	@Override
	protected int getSpreadGrassBlock(int substrateID) {
		return Block.grass.blockID;
	}

	@Override
	public int getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side) {
		if(side == 1) {
			return 167;
		}

		return super.getBlockTexture(blockAccess, x, y, z, side);
	}

	@Override
	public int getBlockTextureFromSide(int side) {
		if(side == 1) {
			return 167;
		}

		return super.getBlockTextureFromSide(side);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
		if(side == 1) {
			return 167;
		}

		return super.getBlockTextureFromSideAndMetadata(side, meta);
	}

	@Override
	public int getIndexInTextureFromMeta(int meta) {
		return 167;
	}

	@Override
	public String getNameFromMeta(int meta) {
		return "grassWithPebbles";
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float chance) {
		if(!world.isRemote) {
			if(world.rand.nextFloat() <= chance) {
				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Block.dirt));
			}

			if(world.rand.nextFloat() <= chance) {
				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Item.pebble));
			}
		}
	}
}