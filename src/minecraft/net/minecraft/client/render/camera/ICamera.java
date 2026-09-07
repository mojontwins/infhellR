package net.minecraft.client.render.camera;

import net.minecraft.game.physics.AxisAlignedBB;

public interface ICamera {
	boolean isBoundingBoxInFrustum(AxisAlignedBB boundingBox);

	boolean isBoundingBoxInFrustumFully(AxisAlignedBB boundingBox);

	void setPosition(double x, double y, double z);
}
