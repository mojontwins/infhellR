package net.minecraft.game.world.path;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.Vec3D;

public class PathEntity {
	private final PathPoint[] points;
	public int pathLength;
	public int pathIndex;

	public PathEntity(PathPoint[] pathPoint1) {
		this.points = pathPoint1;
		this.pathLength = pathPoint1.length;
	}

	public void incrementPathIndex() {
		++this.pathIndex;
	}

	public boolean isFinished() {
		return this.pathIndex >= this.points.length;
	}

	public PathPoint getPathPoint() {
		return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
	}

	public Vec3D getPosition(Entity entity1) {
		double d2 = (double)this.points[this.pathIndex].xCoord + (double)((int)(entity1.width + 1.0F)) * 0.5D;
		double d4 = (double)this.points[this.pathIndex].yCoord;
		double d6 = (double)this.points[this.pathIndex].zCoord + (double)((int)(entity1.width + 1.0F)) * 0.5D;
		return Vec3D.createVector(d2, d4, d6);
	}
	
	public int getCurrentPathIndex() {
		return this.pathIndex;
	}
	
	public void setCurrentPathIndex(int i1) {
		this.pathIndex = i1;
	}
	
	public PathPoint getPathPointFromIndex(int par1) {
		return points[par1];
	}
	
	public boolean isEqualTo(PathEntity pathEntity1) {
		if(pathEntity1 == null) {
			return false;
		} else if(pathEntity1.points.length != this.points.length) {
			return false;
		} else {
			for(int i2 = 0; i2 < this.points.length; ++i2) {
				if(this.points[i2].xCoord != pathEntity1.points[i2].xCoord || this.points[i2].yCoord != pathEntity1.points[i2].yCoord || this.points[i2].zCoord != pathEntity1.points[i2].zCoord) {
					return false;
				}
			}

			return true;
		}
	}
	
	public int getCurrentPathLength() {
		return this.pathLength;
	}

	public void setCurrentPathLength(int i1) {
		this.pathLength = i1;
	}
	
	public Vec3D getVectorFromIndex(Entity entity1, int i2) {
		double d3 = (double)this.points[i2].xCoord + (double)((int)(entity1.width + 1.0F)) * 0.5D;
		double d5 = (double)this.points[i2].yCoord;
		double d7 = (double)this.points[i2].zCoord + (double)((int)(entity1.width + 1.0F)) * 0.5D;
		return Vec3D.createVector(d3, d5, d7);
	}
	
	public Vec3D getCurrentNodeVec3d(Entity entity1) {
		return this.getVectorFromIndex(entity1, this.pathIndex);
	}
	
	public PathPoint getFinalPathPoint() {
		return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
	}

	public boolean func_48639_a(Vec3D vec3D1) {
		PathPoint pathPoint2 = this.getFinalPathPoint();
		return pathPoint2 == null ? false : pathPoint2.xCoord == (int)vec3D1.xCoord && pathPoint2.zCoord == (int)vec3D1.zCoord;
	}
}
