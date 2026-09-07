
package net.minecraft.game.world.terrain.generate.city;

public abstract class BuildingDynamic extends Building {
	protected byte[] blocks;
	protected byte[] meta;

	protected void setArrays(byte[] blocks, byte[] meta) {
		this.blocks = blocks;
		this.meta = meta;
	}

	public int coords2Idx(int x, int y, int z) {
		return x << 11 | z << 7 | y;
	}

	public void setBlockIdAndMetadata(int x, int y, int z, int blockID, int metadata) {
		int idx = this.coords2Idx(x, y, z);
		this.blocks[idx] = (byte) blockID;
		this.meta[idx] = (byte) metadata;
	}

	public void setBlockId(int x, int y, int z, int blockID) {
		this.blocks[this.coords2Idx(x, y, z)] = (byte) blockID;
	}

	public void setBlockIdColumn(int x, int y, int z, int[] blockIDs) {
		for (int i = 0; i < blockIDs.length; i++) {
			this.blocks[this.coords2Idx(x, y++, z)] = (byte) blockIDs[i];
		}
	}

}
