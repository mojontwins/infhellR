package net.minecraft.game.world.terrain.generate.feature;

public class FeatureAABB {
	public int x1, y1, z1, x2, y2, z2;
	
	public FeatureAABB(int x1, int y1, int z1, int x2, int y2, int z2) {
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
	}

	public boolean collidesWith(FeatureAABB other) {
		return (
				this.x1 <= other.x2 && this.x2 >= other.x1 && 
				this.y1 <= other.y2 && this.y2 >= other.y1 &&
				this.z1 <= other.z2 && this.z2 >= other.z1
		);
	}
	
	public boolean containsFully(FeatureAABB other) {
		return (
				this.x1 <= other.x1 && this.x2 >= other.x2 &&
				this.y1 <= other.y1 && this.y2 >= other.y2 &&
				this.z1 <= other.z1 && this.z2 >= other.z2
		);
	}

	/*
	 * An inverted box that expands to fit anything it is combined with - handy as the
	 * seed of an "overall bounding box" accumulation.
	 */
	public static FeatureAABB empty() {
		return new FeatureAABB(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public boolean intersectsWith(FeatureAABB other) {
		return this.collidesWith(other);
	}

	/* Horizontal (X/Z only) intersection, used when clipping drawing to a chunk column. */
	public boolean intersectsWith(int minX, int minZ, int maxX, int maxZ) {
		return this.x2 >= minX && this.x1 <= maxX && this.z2 >= minZ && this.z1 <= maxZ;
	}

	public boolean isVecInside(int x, int y, int z) {
		return x >= this.x1 && x <= this.x2 && z >= this.z1 && z <= this.z2 && y >= this.y1 && y <= this.y2;
	}

	public boolean isVecInsideHorizontally(int x, int y, int z) {
		return x >= this.x1 && x <= this.x2 && z >= this.z1 && z <= this.z2 && y >= 0 && y <= 255;
	}

	public void expandTo(FeatureAABB other) {
		this.x1 = Math.min(this.x1, other.x1);
		this.y1 = Math.min(this.y1, other.y1);
		this.z1 = Math.min(this.z1, other.z1);
		this.x2 = Math.max(this.x2, other.x2);
		this.y2 = Math.max(this.y2, other.y2);
		this.z2 = Math.max(this.z2, other.z2);
	}

	public void offset(int dx, int dy, int dz) {
		this.x1 += dx;
		this.y1 += dy;
		this.z1 += dz;
		this.x2 += dx;
		this.y2 += dy;
		this.z2 += dz;
	}

	public int getXSize() {
		return this.x2 - this.x1 + 1;
	}

	public int getYSize() {
		return this.y2 - this.y1 + 1;
	}

	public int getZSize() {
		return this.z2 - this.z1 + 1;
	}

	public int getCenterX() {
		return this.x1 + (this.x2 - this.x1 + 1) / 2;
	}

	public int getCenterY() {
		return this.y1 + (this.y2 - this.y1 + 1) / 2;
	}

	public int getCenterZ() {
		return this.z1 + (this.z2 - this.z1 + 1) / 2;
	}
	
	public String toString() {
		return "(" + this.x1 + ", " + this.y1 + ", " + this.z1 + ") -> (" + this.x2 + ", " + this.y2 + ", " + this.z2 + ")";
	}
	
}
