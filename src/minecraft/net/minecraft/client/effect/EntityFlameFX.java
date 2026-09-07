package net.minecraft.client.effect;

import net.minecraft.game.world.World;
import net.minecraft.client.render.Tessellator;

public class EntityFlameFX extends EntityFX {
	private float baseParticleScale;

	public EntityFlameFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.motionX = this.motionX * (double)0.01F + motionX;
		this.motionY = this.motionY * (double)0.01F + motionY;
		this.motionZ = this.motionZ * (double)0.01F + motionZ;
		this.baseParticleScale = this.particleScale;
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
		this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
		this.noClip = true;
		this.particleTextureIndex = 48;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float ageFraction = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
		this.particleScale = this.baseParticleScale * (1.0F - ageFraction * ageFraction * 0.5F);
		super.renderParticle(tessellator, partialTicks, cosYaw, cosPitch, sinYaw, cosPitchNegSinYaw, cosPitchCosYaw);
	}

	public int getBrightnessForRender(float partialTicks) {
		float ageFraction = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
		if(ageFraction < 0.0F) {
			ageFraction = 0.0F;
		}

		if(ageFraction > 1.0F) {
			ageFraction = 1.0F;
		}

		int baseBrightness = super.getBrightnessForRender(partialTicks);
		int skyLight = baseBrightness & 255;
		int blockLight = baseBrightness >> 16 & 255;
		skyLight += (int)(ageFraction * 15.0F * 16.0F);
		if(skyLight > 240) {
			skyLight = 240;
		}

		return skyLight | blockLight << 16;
	}
	
	public float getEntityBrightness(float partialTicks) {
		float ageFraction = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
		if(ageFraction < 0.0F) {
			ageFraction = 0.0F;
		}

		if(ageFraction > 1.0F) {
			ageFraction = 1.0F;
		}

		float baseBrightness = super.getEntityBrightness(partialTicks);
		return baseBrightness * ageFraction + (1.0F - ageFraction);
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if(this.particleAge++ >= this.particleMaxAge) {
			this.setEntityDead();
		}

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= (double)0.96F;
		this.motionY *= (double)0.96F;
		this.motionZ *= (double)0.96F;
		if(this.onGround) {
			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

	}
}
