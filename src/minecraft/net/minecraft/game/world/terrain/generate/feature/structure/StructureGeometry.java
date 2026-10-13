package net.minecraft.game.world.terrain.generate.feature.structure;

import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;

/** Builds a rotated box (size given before rotation) anchored at a piece's corner. */
public final class StructureGeometry {
	private StructureGeometry() {
	}

	public static FeatureAABB boxFrom(int x, int y, int z, int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ, int orientation) {
		switch(orientation) {
		case 0:
			return new FeatureAABB(x + offsetX, y + offsetY, z + offsetZ, x + sizeX - 1 + offsetX, y + sizeY - 1 + offsetY, z + sizeZ - 1 + offsetZ);
		case 1:
			return new FeatureAABB(x - sizeZ + 1 + offsetZ, y + offsetY, z + offsetX, x + offsetZ, y + sizeY - 1 + offsetY, z + sizeX - 1 + offsetX);
		case 2:
			return new FeatureAABB(x + offsetX, y + offsetY, z - sizeZ + 1 + offsetZ, x + sizeX - 1 + offsetX, y + sizeY - 1 + offsetY, z + offsetZ);
		case 3:
			return new FeatureAABB(x + offsetZ, y + offsetY, z + offsetX, x + sizeZ - 1 + offsetZ, y + sizeY - 1 + offsetY, z + sizeX - 1 + offsetX);
		default:
			return new FeatureAABB(x + offsetX, y + offsetY, z + offsetZ, x + sizeX - 1 + offsetX, y + sizeY - 1 + offsetY, z + sizeZ - 1 + offsetZ);
		}
	}
}
