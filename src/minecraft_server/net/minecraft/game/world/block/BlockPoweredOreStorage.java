package net.minecraft.game.world.block;

import net.minecraft.game.world.World;

public class BlockPoweredOreStorage extends BlockOreStorage {

	public BlockPoweredOreStorage(int id, int indexInTexture) {
		super(id, indexInTexture);
	}
	
	@Override
	public boolean canProvidePower() {
		return true;
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		world.notifyBlocksOfNeighborChange(x, y - 1, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x, y + 1, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x - 1, y, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x + 1, y, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x, y, z - 1, this.blockID);
		world.notifyBlocksOfNeighborChange(x, y, z + 1, this.blockID);
	}

	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
		world.notifyBlocksOfNeighborChange(x, y - 1, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x, y + 1, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x - 1, y, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x + 1, y, z, this.blockID);
		world.notifyBlocksOfNeighborChange(x, y, z - 1, this.blockID);
		world.notifyBlocksOfNeighborChange(x, y, z + 1, this.blockID);
	}

	@Override
	public boolean isPoweringTo(World iBlockAccess1, int i2, int i3, int i4, int i5) {
		return true;
	}

	@Override
	public boolean isIndirectlyPoweringTo(World world1, int i2, int i3, int i4, int i5) {
		return true;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
