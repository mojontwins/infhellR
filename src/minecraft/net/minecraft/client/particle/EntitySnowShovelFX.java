package net.minecraft.client.particle;

import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;
import net.minecraft.client.render.Tessellator;

public class EntitySnowShovelFX extends EntityFX {
	float baseSnowShovelParticleScale;

	public EntitySnowShovelFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		this(world, x, y, z, motionX, motionY, motionZ, 1.0F);
	}

	public EntitySnowShovelFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float scaleFactor) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.motionX *= (double)0.1F;
		this.motionY *= (double)0.1F;
		this.motionZ *= (double)0.1F;
		this.motionX += motionX;
		this.motionY += motionY;
		this.motionZ += motionZ;
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F - (float)(Math.random() * (double)0.3F);
		this.particleScale *= 0.75F;
		this.particleScale *= scaleFactor;
		this.baseSnowShovelParticleScale = this.particleScale;
		this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
		this.particleMaxAge = (int)((float)this.particleMaxAge * scaleFactor);
		this.noClip = false;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float sizeProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
		if(sizeProgress < 0.0F) {
			sizeProgress = 0.0F;
		}

		if(sizeProgress > 1.0F) {
			sizeProgress = 1.0F;
		}

		this.particleScale = this.baseSnowShovelParticleScale * sizeProgress;
		super.renderParticle(tessellator, partialTicks, cosYaw, cosPitch, sinYaw, cosPitchNegSinYaw, cosPitchCosYaw);
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if(this.particleAge++ >= this.particleMaxAge) {
			this.setEntityDead();
		}

		this.particleTextureIndex = 7 - this.particleAge * 8 / this.particleMaxAge;
		this.motionY -= 0.03D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= (double)0.99F;
		this.motionY *= (double)0.99F;
		this.motionZ *= (double)0.99F;
		if(this.onGround) {
			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

	}
}
