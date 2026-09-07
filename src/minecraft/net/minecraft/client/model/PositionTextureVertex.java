package net.minecraft.client.model;

import net.minecraft.game.physics.Vec3D;

public class PositionTextureVertex {
	public Vec3D vector3D;
	public float texturePositionX;
	public float texturePositionY;

	public PositionTextureVertex(float x, float y, float z, float u, float v) {
		this(Vec3D.createVectorHelper((double)x, (double)y, (double)z), u, v);
	}

	public PositionTextureVertex setTexturePosition(float u, float v) {
		return new PositionTextureVertex(this, u, v);
	}

	public PositionTextureVertex(PositionTextureVertex copy, float u, float v) {
		this.vector3D = copy.vector3D;
		this.texturePositionX = u;
		this.texturePositionY = v;
	}

	public PositionTextureVertex(Vec3D vec3D, float u, float v) {
		this.vector3D = vec3D;
		this.texturePositionX = u;
		this.texturePositionY = v;
	}
}