package net.minecraft.client.particle;

import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;

public class EntityStatusEffectFX extends EntityFX {
	public EntityStatusEffectFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		
		this.particleTextureIndex = 128;
		
		this.motionY *= 0.20000000298023224D;
		if (motionX == 0.0D && motionZ == 0.0D) {
			this.motionX *= 0.10000000149011612D;
			this.motionZ *= 0.10000000149011612D;
		}
		this.particleScale *= 0.75F;
		this.particleMaxAge = (int)(8D / (Math.random() * 0.80000000000000004D + 0.20000000000000001D));
		this.noClip = false;
	}

	public void setParticleColor (float r, float g, float b) {
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
	}
	
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge) setEntityDead();

		particleTextureIndex = 128 + (7 - (particleAge * 8) / particleMaxAge);

		motionY += 0.0040000000000000001D;
		moveEntity(motionX, motionY, motionZ);

		if (posY == prevPosY) {
			motionX *= 1.1000000000000001D;
			motionZ *= 1.1000000000000001D;
		}

		motionX *= 0.95999997854232788D;
		motionY *= 0.95999997854232788D;
		motionZ *= 0.95999997854232788D;

		if (onGround) {
			motionX *= 0.69999998807907104D;
			motionZ *= 0.69999998807907104D;
		}
	}
}
