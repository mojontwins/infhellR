package net.minecraft.game.world.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.IBlockAccess;
import net.minecraft.game.world.World;
import net.minecraft.game.container.creativetab.CreativeTabs;
import net.minecraft.game.entity.RedstoneUpdateInfo;

public class BlockRedstoneTorch extends BlockTorch {
	private boolean torchActive = false;
	private static List<RedstoneUpdateInfo> torchUpdates = new ArrayList<RedstoneUpdateInfo>();

	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
		return side == 1 ? Block.redstoneWire.getBlockTextureFromSideAndMetadata(side, metadata) : super.getBlockTextureFromSideAndMetadata(side, metadata);
	}

	private boolean checkForBurnout(World world, int x, int y, int z, boolean z5) {
		if(z5) {
			torchUpdates.add(new RedstoneUpdateInfo(x, y, z, world.getWorldTime()));
		}

		int i6 = 0;

		for(int i7 = 0; i7 < torchUpdates.size(); ++i7) {
			RedstoneUpdateInfo redstoneUpdateInfo8 = (RedstoneUpdateInfo)torchUpdates.get(i7);
			if(redstoneUpdateInfo8.x == x && redstoneUpdateInfo8.y == y && redstoneUpdateInfo8.z == z) {
				++i6;
				if(i6 >= 8) {
					return true;
				}
			}
		}

		return false;
	}

	protected BlockRedstoneTorch(int id, int blockIndex, boolean torchActive) {
		super(id, blockIndex);
		this.torchActive = torchActive;
		this.setTickOnLoad(true);
		
		this.displayOnCreativeTab = CreativeTabs.tabRedstone;
	}

	public int tickRate() {
		return 2;
	}

	public void onBlockAdded(World world, int x, int y, int z) {
		if(world.getBlockMetadata(x, y, z) == 0) {
			super.onBlockAdded(world, x, y, z);
		}

		if(this.torchActive) {
			world.notifyBlocksOfNeighborChange(x, y - 1, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x, y + 1, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x - 1, y, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x + 1, y, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x, y, z - 1, this.blockID);
			world.notifyBlocksOfNeighborChange(x, y, z + 1, this.blockID);
		}

	}

	public void onBlockRemoval(World world, int x, int y, int z) {
		if(this.torchActive) {
			world.notifyBlocksOfNeighborChange(x, y - 1, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x, y + 1, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x - 1, y, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x + 1, y, z, this.blockID);
			world.notifyBlocksOfNeighborChange(x, y, z - 1, this.blockID);
			world.notifyBlocksOfNeighborChange(x, y, z + 1, this.blockID);
		}

	}

	public boolean isPoweringTo(IBlockAccess blockAccess, int x, int y, int z, int metadata) {
		if(!this.torchActive) {
			return false;
		} else {
			int i6 = blockAccess.getBlockMetadata(x, y, z);
			return i6 == 5 && metadata == 1 ? false : (i6 == 3 && metadata == 3 ? false : (i6 == 4 && metadata == 2 ? false : (i6 == 1 && metadata == 5 ? false : i6 != 2 || metadata != 4)));
		}
	}

	private boolean isIndirectlyPowered(World world, int x, int y, int z) {
		int i5 = world.getBlockMetadata(x, y, z);
		return i5 == 5 && world.isBlockIndirectlyProvidingPowerTo(x, y - 1, z, 0) ? true : (i5 == 3 && world.isBlockIndirectlyProvidingPowerTo(x, y, z - 1, 2) ? true : (i5 == 4 && world.isBlockIndirectlyProvidingPowerTo(x, y, z + 1, 3) ? true : (i5 == 1 && world.isBlockIndirectlyProvidingPowerTo(x - 1, y, z, 4) ? true : i5 == 2 && world.isBlockIndirectlyProvidingPowerTo(x + 1, y, z, 5))));
	}

	public void updateTick(World world, int x, int y, int z, Random rand) {
		boolean z6 = this.isIndirectlyPowered(world, x, y, z);

		while(torchUpdates.size() > 0 && world.getWorldTime() - ((RedstoneUpdateInfo)torchUpdates.get(0)).updateTime > 100L) {
			torchUpdates.remove(0);
		}

		if(this.torchActive) {
			if(z6) {
				world.setBlockAndMetadataWithNotify(x, y, z, Block.torchRedstoneIdle.blockID, world.getBlockMetadata(x, y, z));
				if(this.checkForBurnout(world, x, y, z, true)) {
					world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

					for(int i7 = 0; i7 < 5; ++i7) {
						double d8 = (double)x + rand.nextDouble() * 0.6D + 0.2D;
						double d10 = (double)y + rand.nextDouble() * 0.6D + 0.2D;
						double d12 = (double)z + rand.nextDouble() * 0.6D + 0.2D;
						world.spawnParticle("smoke", d8, d10, d12, 0.0D, 0.0D, 0.0D);
					}
				}
			}
		} else if(!z6 && !this.checkForBurnout(world, x, y, z, false)) {
			world.setBlockAndMetadataWithNotify(x, y, z, Block.torchRedstoneActive.blockID, world.getBlockMetadata(x, y, z));
		}

	}

	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		super.onNeighborBlockChange(world, x, y, z, id);
		world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
	}

	public boolean isIndirectlyPoweringTo(World world, int x, int y, int z, int side) {
		return side == 0 ? this.isPoweringTo(world, x, y, z, side) : false;
	}

	public int idDropped(int metadata, Random rand) {
		return Block.torchRedstoneActive.blockID;
	}

	public boolean canProvidePower() {
		return true;
	}

	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if(this.torchActive) {
			int i6 = world.getBlockMetadata(x, y, z);
			double d7 = (double)((float)x + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
			double d9 = (double)((float)y + 0.7F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
			double d11 = (double)((float)z + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
			double d13 = (double)0.22F;
			double d15 = (double)0.27F;
			if(i6 == 1) {
				world.spawnParticle("reddust", d7 - d15, d9 + d13, d11, 0.0D, 0.0D, 0.0D);
			} else if(i6 == 2) {
				world.spawnParticle("reddust", d7 + d15, d9 + d13, d11, 0.0D, 0.0D, 0.0D);
			} else if(i6 == 3) {
				world.spawnParticle("reddust", d7, d9 + d13, d11 - d15, 0.0D, 0.0D, 0.0D);
			} else if(i6 == 4) {
				world.spawnParticle("reddust", d7, d9 + d13, d11 + d15, 0.0D, 0.0D, 0.0D);
			} else {
				world.spawnParticle("reddust", d7, d9, d11, 0.0D, 0.0D, 0.0D);
			}

		}
	}
}
