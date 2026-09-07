package net.minecraft.client.particle;

import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;
import net.minecraft.client.render.Tessellator;

public class EntityHeartFX extends EntityFX {
	float baseHeartParticleScale;

	public EntityHeartFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		this(world, x, y, z, motionX, motionY, motionZ, 2.0F);
	}

	public EntityHeartFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float scaleFactor) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX *= (double)0.01F;
		this.motionY *= (double)0.01F;
		this.motionZ *= (double)0.01F;
		this.motionY += 0.1D;
		this.particleScale *= 0.75F;
		this.particleScale *= scaleFactor;
		this.baseHeartParticleScale = this.particleScale;
		this.particleMaxAge = 16;
		this.noClip = false;
		this.particleTextureIndex = 80;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float sizeProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
		if(sizeProgress < 0.0F) {
			sizeProgress = 0.0F;
		}

		if(sizeProgress > 1.0F) {
			sizeProgress = 1.0F;
		}

		this.particleScale = this.baseHeartParticleScale * sizeProgress;
		super.renderParticle(tessellator, partialTicks, cosYaw, cosPitch, sinYaw, cosPitchNegSinYaw, cosPitchCosYaw);
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if(this.particleAge++ >= this.particleMaxAge) {
			this.setEntityDead();
		}

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		if(this.posY == this.prevPosY) {
			this.motionX *= 1.1D;
			this.motionZ *= 1.1D;
		}

		this.motionX *= (double)0.86F;
		this.motionY *= (double)0.86F;
		this.motionZ *= (double)0.86F;
		if(this.onGround) {
			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

	}
}
