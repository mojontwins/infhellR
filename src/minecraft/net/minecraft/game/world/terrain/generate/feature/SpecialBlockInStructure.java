package net.minecraft.game.world.terrain.generate.feature;

public class SpecialBlockInStructure {
	public final int x, y, z;
	public final int blockID;
	public final int metadata;
	/**
	 * Optional sky-gating probe {probeX, probeY, probeZ}, or null. When set, the
	 * block is only placed if the probe cell cannot see the sky (the mineshaft
	 * hills variant drops a whole corridor section, special blocks included,
	 * when its beam centre is exposed).
	 */
	public int[] gate;
	
	public SpecialBlockInStructure(int x, int y, int z, int blockID, int metadata) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockID = blockID;
		this.metadata = metadata;
	}

}
