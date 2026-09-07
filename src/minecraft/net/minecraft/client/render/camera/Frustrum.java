package net.minecraft.client.render.camera;

import net.minecraft.game.physics.AxisAlignedBB;

public class Frustrum implements ICamera {
	private ClippingHelper clippingHelper = ClippingHelperImpl.getInstance();
	private double xPosition;
	private double yPosition;
	private double zPosition;

	public void setPosition(double x, double y, double z) {
		this.xPosition = x;
		this.yPosition = y;
		this.zPosition = z;
	}

	public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return this.clippingHelper.isBoxInFrustum(minX - this.xPosition, minY - this.yPosition, minZ - this.zPosition, maxX - this.xPosition, maxY - this.yPosition, maxZ - this.zPosition);
	}

	public boolean isBoundingBoxInFrustum(AxisAlignedBB boundingBox) {
		return this.isBoxInFrustum(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
	}

	public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return this.clippingHelper.isBoxInFrustumFully(minX - this.xPosition, minY - this.yPosition, minZ - this.zPosition, maxX - this.xPosition, maxY - this.yPosition, maxZ - this.zPosition);
	}

	public boolean isBoundingBoxInFrustumFully(AxisAlignedBB boundingBox) {
		return this.isBoxInFrustumFully(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
	}
}
