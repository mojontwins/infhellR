package net.minecraft.client.effect;

import net.minecraft.game.world.World;
import net.minecraft.client.render.Tessellator;

public class EntitySmokeFX extends EntityFX {
	float baseSmokeParticleScale;

	public EntitySmokeFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		this(world, x, y, z, motionX, motionY, motionZ, 1.0F);
	}

	public EntitySmokeFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float scaleFactor) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX *= (double)0.1F;
		this.motionY *= (double)0.1F;
		this.motionZ *= (double)0.1F;
		this.motionX += motionX;
		this.motionY += motionY;
		this.motionZ += motionZ;
		this.particleRed = this.particleGreen = this.particleBlue = (float)(Math.random() * (double)0.3F);
		this.particleScale *= 0.75F;
		this.particleScale *= scaleFactor;
		this.baseSmokeParticleScale = this.particleScale;
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

		this.particleScale = this.baseSmokeParticleScale * sizeProgress;
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
		this.motionY += 0.004D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		if(this.posY == this.prevPosY) {
			this.motionX *= 1.1D;
			this.motionZ *= 1.1D;
		}

		this.motionX *= (double)0.96F;
		this.motionY *= (double)0.96F;
		this.motionZ *= (double)0.96F;
		if(this.onGround) {
			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

	}
}
