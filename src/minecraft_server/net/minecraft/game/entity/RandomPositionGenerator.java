package net.minecraft.game.entity;

import java.util.Random;
import net.minecraft.game.MathHelper;
import net.minecraft.game.physics.Vec3D;

public class RandomPositionGenerator {
	private static Vec3D field_48624_a = Vec3D.createVectorHelper(0.0D, 0.0D, 0.0D);

	public static Vec3D findRandomTarget(EntityCreature entityCreature0, int i1, int i2) {
		return findRandomTargetBlock(entityCreature0, i1, i2, (Vec3D)null);
	}

	public static Vec3D findRandomTargetBlockTowards(EntityCreature entityCreature0, int i1, int i2, Vec3D vec3D3) {
		field_48624_a.xCoord = vec3D3.xCoord - entityCreature0.posX;
		field_48624_a.yCoord = vec3D3.yCoord - entityCreature0.posY;
		field_48624_a.zCoord = vec3D3.zCoord - entityCreature0.posZ;
		return findRandomTargetBlock(entityCreature0, i1, i2, field_48624_a);
	}

	public static Vec3D findRandomTargetBlockAwayFrom(EntityCreature entityCreature0, int i1, int i2, Vec3D vec3D3) {
		field_48624_a.xCoord = entityCreature0.posX - vec3D3.xCoord;
		field_48624_a.yCoord = entityCreature0.posY - vec3D3.yCoord;
		field_48624_a.zCoord = entityCreature0.posZ - vec3D3.zCoord;
		return findRandomTargetBlock(entityCreature0, i1, i2, field_48624_a);
	}

	private static Vec3D findRandomTargetBlock(EntityCreature entityCreature0, int i1, int i2, Vec3D vec3D3) {
		Random random4 = entityCreature0.rand;
		boolean z5 = false;
		int i6 = 0;
		int i7 = 0;
		int i8 = 0;
		float f9 = -99999.0F;
		boolean z10;
		/*if(entityCreature0.hasHome()) {
			double d11 = entityCreature0.getHomePosition().getEuclideanDistanceTo(MathHelper.floor_double(entityCreature0.posX), MathHelper.floor_double(entityCreature0.posY), MathHelper.floor_double(entityCreature0.posZ)) + 4.0D;
			z10 = d11 < (double)(entityCreature0.getMaximumHomeDistance() + (float)i1);
		} else*/ {
			z10 = false;
		}

		for(int i16 = 0; i16 < 10; ++i16) {
			int i12 = random4.nextInt(2 * i1) - i1;
			int i13 = random4.nextInt(2 * i2) - i2;
			int i14 = random4.nextInt(2 * i1) - i1;
			if(vec3D3 == null || (double)i12 * vec3D3.xCoord + (double)i14 * vec3D3.zCoord >= 0.0D) {
				i12 += MathHelper.floor_double(entityCreature0.posX);
				i13 += MathHelper.floor_double(entityCreature0.posY);
				i14 += MathHelper.floor_double(entityCreature0.posZ);
				if(!z10/* || entityCreature0.isWithinHomeDistance(i12, i13, i14)*/) {
					float f15 = entityCreature0.getBlockPathWeight(i12, i13, i14);
					if(f15 > f9) {
						f9 = f15;
						i6 = i12;
						i7 = i13;
						i8 = i14;
						z5 = true;
					}
				}
			}
		}

		if(z5) {
			return Vec3D.createVector((double)i6, (double)i7, (double)i8);
		} else {
			return null;
		}
	}
}
