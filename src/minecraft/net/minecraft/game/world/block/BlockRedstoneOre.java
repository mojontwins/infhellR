package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockRedstoneOre extends Block {
	private boolean glowing;
	private int type;

	public BlockRedstoneOre(int id, int blockIndex, boolean glowing, int type) {
		super(id, blockIndex, Material.rock);
		if(glowing) {
			this.setTickOnLoad(true);
		}

		this.glowing = glowing;
		this.type = type;
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public int tickRate() {
		return 30;
	}

	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		this.glow(world, x, y, z);
		super.onBlockClicked(world, x, y, z, entityPlayer);
	}

	public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
		this.glow(world, x, y, z);
		super.onEntityWalking(world, x, y, z, entity);
	}

	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		this.glow(world, x, y, z);
		return super.blockActivated(world, x, y, z, entityPlayer);
	}

	private void glow(World world, int x, int y, int z) {
		this.sparkle(world, x, y, z);
		if(this.blockID == Block.oreRedstone.blockID) {
			world.setBlockWithNotify(x, y, z, Block.oreRedstoneGlowing.blockID);
		} else if(this.blockID == Block.oreGlow.blockID) {
			world.setBlockWithNotify(x, y, z, Block.oreGlowGlowing.blockID);
		}

	}

	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(this.blockID == Block.oreRedstoneGlowing.blockID) {
			world.setBlockWithNotify(x, y, z, Block.oreRedstone.blockID);
		}
	}

	public int idDropped(int metadata, Random rand) {
		if(this.type == 0) return Item.redstone.shiftedIndex;
		else return Item.lightStoneDust.shiftedIndex;
	}

	public int quantityDropped(Random rand) {
		return 4 + rand.nextInt(2);
	}

	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if(this.glowing) {
			this.sparkle(world, x, y, z);
		}

	}

	private void sparkle(World world, int x, int y, int z) {
		Random random5 = world.rand;
		double d6 = 0.0625D;

		for(int i8 = 0; i8 < 6; ++i8) {
			double d9 = (double)((float)x + random5.nextFloat());
			double d11 = (double)((float)y + random5.nextFloat());
			double d13 = (double)((float)z + random5.nextFloat());
			if(i8 == 0 && !world.isBlockNormalCube(x, y + 1, z)) {
				d11 = (double)(y + 1) + d6;
			}

			if(i8 == 1 && !world.isBlockNormalCube(x, y - 1, z)) {
				d11 = (double)(y + 0) - d6;
			}

			if(i8 == 2 && !world.isBlockNormalCube(x, y, z + 1)) {
				d13 = (double)(z + 1) + d6;
			}

			if(i8 == 3 && !world.isBlockNormalCube(x, y, z - 1)) {
				d13 = (double)(z + 0) - d6;
			}

			if(i8 == 4 && !world.isBlockNormalCube(x + 1, y, z)) {
				d9 = (double)(x + 1) + d6;
			}

			if(i8 == 5 && !world.isBlockNormalCube(x - 1, y, z)) {
				d9 = (double)(x + 0) - d6;
			}

			if(d9 < (double)x || d9 > (double)(x + 1) || d11 < 0.0D || d11 > (double)(y + 1) || d13 < (double)z || d13 > (double)(z + 1)) {
				if(this.type == 0) {
				world.spawnParticle("reddust", d9, d11, d13, 0.0D, 0.0D, 0.0D);
				} else if(this.type == 1) {
					world.spawnParticle("glowdust", d9, d11, d13, 0.0D, 0.0D, 0.0D);
				}
			}
		}

	}
}
