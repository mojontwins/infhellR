package net.minecraft.game.world.block;

import java.util.Random;

import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class BlockSponge extends Block {
	static final byte radius = 2;

	protected BlockSponge(int i) {
		super(i, Material.sponge);
		this.blockIndexInTexture = 48;
		
		this.displayOnCreativeTab = CreativeTabs.tabBlock;
	}

	public void onBlockAdded(World world, int a, int b, int c) {
		super.onBlockAdded(world, a, b, c);

		for(int x = a - 2; x <= a + 2; ++x) {
			for(int y = b - 2; y <= b + 2; ++y) {
				for(int z = c - 2; z <= c + 2; ++z) {
					if (world.getBlockId(x, y, z) == Block.waterMoving.blockID || world.getBlockId(x, y, z) == Block.waterStill.blockID) {
						world.setBlockWithNotify(x, y, z, 0);
					}
				}
			}
		}

	}

	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		if (this.isReceivingRedstonePower(world, i, j, k, l)) {
			this.dropBlockAsItemWithChance(world, i, j, k, 19, 1.0F);
			world.setBlockWithNotify(i, j, k, 87);
		}

	}

	private boolean isReceivingRedstonePower(World world, int i, int j, int k, int l) {
		if (l != 0 && world.isBlockIndirectlyProvidingPowerTo(i, j - 1, k, 0)) {
			return true;
		} else if (l != 1 && world.isBlockIndirectlyProvidingPowerTo(i, j + 1, k, 1)) {
			return true;
		} else if (l != 2 && world.isBlockIndirectlyProvidingPowerTo(i, j, k - 1, 2)) {
			return true;
		} else if (l != 3 && world.isBlockIndirectlyProvidingPowerTo(i, j, k + 1, 3)) {
			return true;
		} else if (l != 5 && world.isBlockIndirectlyProvidingPowerTo(i + 1, j, k, 5)) {
			return true;
		} else if (l != 4 && world.isBlockIndirectlyProvidingPowerTo(i - 1, j, k, 4)) {
			return true;
		} else if (world.isBlockIndirectlyProvidingPowerTo(i, j, k, 0)) {
			return true;
		} else if (world.isBlockIndirectlyProvidingPowerTo(i, j + 2, k, 1)) {
			return true;
		} else if (world.isBlockIndirectlyProvidingPowerTo(i, j + 1, k - 1, 2)) {
			return true;
		} else if (world.isBlockIndirectlyProvidingPowerTo(i, j + 1, k + 1, 3)) {
			return true;
		} else {
			return world.isBlockIndirectlyProvidingPowerTo(i - 1, j + 1, k, 4) ? true : world.isBlockIndirectlyProvidingPowerTo(i + 1, j + 1, k, 5);
		}
	}

	public void dropWater(World world, int x, int y, int z) {
		for(int i = y; i > y - 3 && (world.getBlockId(x, i, z) == 0 || world.getBlockId(x, i, z) == Block.waterStill.blockID) && getSurroundSponge(world, x, i, z); --i) {
		}

	}

	public void flowWater(World world, int x, int y, int z, int sx, int sy, int sz) {
		int radius = 3;
		if ((world.getBlockId(x, y, z) == 0 || world.getBlockId(x, y, z) == Block.waterStill.blockID) && !getSurroundSponge(world, x, y, z)) {
			this.dropWater(world, x, y, z);
			if (x + 1 < sx + radius && (world.getBlockId(x + 1, y, z) == 0 || world.getBlockId(x + 1, y, z) == Block.waterStill.blockID) && !getSurroundSponge(world, x + 1, y, z)) {
				world.notifyBlocksOfNeighborChange(x + 1, y, z, 1);
			}

			if (z + 1 < sz + radius && (world.getBlockId(x, y, z + 1) == 0 || world.getBlockId(x, y, z + 1) == Block.waterStill.blockID) && !getSurroundSponge(world, x, y, z + 1)) {
				world.notifyBlocksOfNeighborChange(x, y, z + 1, 1);
			}

			if (x - 1 > sx - radius && (world.getBlockId(x - 1, y, z) == 0 || world.getBlockId(x - 1, y, z) == Block.waterStill.blockID) && !getSurroundSponge(world, x - 1, y, z)) {
				world.notifyBlocksOfNeighborChange(x - 1, y, z, 1);
			}

			if (z - 1 > sz - radius && (world.getBlockId(x, y, z - 1) == 0 || world.getBlockId(x, y, z - 1) == Block.waterStill.blockID) && !getSurroundSponge(world, x, y, z - 1)) {
				world.notifyBlocksOfNeighborChange(x, y, z + 1, 1);
			}
		}

	}

	public void updateTick(World world, int a, int b, int c, Random random) {
		super.updateTick(world, a, b, c, random);

		for(int x = a - 2; x <= a + 2; ++x) {
			for(int y = b - 2; y <= b + 2; ++y) {
				for(int z = c - 2; z <= c + 2; ++z) {
					if (world.getBlockId(x, y, z) == Block.waterMoving.blockID || world.getBlockId(x, y, z) == Block.waterStill.blockID) {
						world.setBlockWithNotify(x, y, z, 0);
					}
				}
			}
		}

	}

	public void fillWater(World world, int x, int y, int z) {
		int radius = 3;

		for(int x1 = x - radius; x1 <= x + radius; ++x1) {
			for(int y1 = y; y1 > y - radius; --y1) {
				for(int z1 = z - radius; z1 <= z + radius; ++z1) {
					if ((world.getBlockId(x1, y1, z1) == 0 || world.getBlockId(x1, y1, z1) == Block.waterStill.blockID || world.getBlockId(x1, y1, z1) == Block.waterMoving.blockID) && !getSurroundSponge(world, x, y, z)) {
						world.setBlockWithNotify(x1, y1, z1, Block.waterMoving.blockID);
					}
				}
			}
		}

	}

	public static boolean getSurroundSponge(World world, int x, int y, int z) {
		for(int x1 = x - 2; x1 <= x + 2; ++x1) {
			for(int y1 = y - 2; y1 <= y + 2; ++y1) {
				for(int z1 = z - 2; z1 <= z + 2; ++z1) {
					if (world.getBlockId(x1, y1, z1) == Block.sponge.blockID) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public void dropBlockAsItemWithChance(World world, int a, int b, int c, int bid, float chance) {
		super.dropBlockAsItemWithChance(world, a, b, c, bid, chance);
		int radius = 3;

		for(int x = a - radius; x <= a + radius; ++x) {
			for(int y = b + radius; y > b - radius; --y) {
				for(int z = c - radius; z <= c + radius; ++z) {
					if (world.getBlockId(x, y, z) == Block.waterMoving.blockID || world.getBlockId(x, y, z) == Block.waterStill.blockID) {
						this.flowWater(world, x, y, z, a, b, c);
					}
				}
			}
		}

	}
}
