package net.minecraft.game.world.terrain.generate;

public abstract class WorldGenRotable extends WorldGenerator {
	public boolean rotated;
	
	public WorldGenRotable(boolean rotated) {
		this.rotated = rotated;
	}
	
	public int getX(int x, int z) {
		return rotated ? z : x;
	}
	
	public int getZ(int x, int z) {
		return rotated ? x : z;
	}
}
