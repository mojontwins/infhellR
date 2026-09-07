package net.minecraft.client.particle;

import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;
import net.minecraft.client.render.Tessellator;

public class EntityReddustFX extends EntityFX {
	float baseReddustParticleScale;

	public EntityReddustFX(World world, double x, double y, double z, float red, float green, float blue) {
		this(world, x, y, z, 1.0F, red, green, blue);
	}

	public EntityReddustFX(World world, double x, double y, double z, float scaleFactor, float red, float green, float blue) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX *= (double)0.1F;
		this.motionY *= (double)0.1F;
		this.motionZ *= (double)0.1F;
		if(red == 0.0F) {
			red = 1.0F;
		}

		float brightness = (float)Math.random() * 0.4F + 0.6F;
		this.particleRed = ((float)(Math.random() * (double)0.2F) + 0.8F) * red * brightness;
		this.particleGreen = ((float)(Math.random() * (double)0.2F) + 0.8F) * green * brightness;
		this.particleBlue = ((float)(Math.random() * (double)0.2F) + 0.8F) * blue * brightness;
		this.particleScale *= 0.75F;
		this.particleScale *= scaleFactor;
		this.baseReddustParticleScale = this.particleScale;
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

		this.particleScale = this.baseReddustParticleScale * sizeProgress;
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
