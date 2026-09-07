package net.minecraft.client.render.camera;

public class ClippingHelper {
	public float[][] frustum = new float[16][16];
	public float[] projectionMatrix = new float[16];
	public float[] modelviewMatrix = new float[16];
	public float[] clippingMatrix = new float[16];

	// Optifine

	public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		for(int plane = 0; plane < 6; ++plane) {
			float minXf = (float)minX;
			float minYf = (float)minY;
			float minZf = (float)minZ;
			float maxXf = (float)maxX;
			float maxYf = (float)maxY;
			float maxZf = (float)maxZ;
			if(
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F
			) {
				return false;
			}
		}

		return true;
	}

	public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		for(int plane = 0; plane < 6; ++plane) {
			float minXf = (float)minX;
			float minYf = (float)minY;
			float minZf = (float)minZ;
			float maxXf = (float)maxX;
			float maxYf = (float)maxY;
			float maxZf = (float)maxZ;
			if(plane < 4) {
				if(
						this.frustum[plane][0] * minXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F ||
						this.frustum[plane][0] * maxXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F ||
						this.frustum[plane][0] * minXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F ||
						this.frustum[plane][0] * maxXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F ||
						this.frustum[plane][0] * minXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F ||
						this.frustum[plane][0] * maxXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F ||
						this.frustum[plane][0] * minXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F ||
						this.frustum[plane][0] * maxXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F
				) {
					return false;
				}
			} else if(
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * minZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * minYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * minXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F &&
					this.frustum[plane][0] * maxXf + this.frustum[plane][1] * maxYf + this.frustum[plane][2] * maxZf + this.frustum[plane][3] <= 0.0F
			) {
				return false;
			}
		}

		return true;
	}
}
