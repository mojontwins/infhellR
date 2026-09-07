package net.minecraft.client.particle;

import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;
import net.minecraft.client.render.Tessellator;

public class EntityPortalFX extends EntityFX {
	private float basePortalParticleScale;
	private double portalStartX;
	private double portalStartY;
	private double portalStartZ;

	public EntityPortalFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.portalStartX = this.posX = x;
		this.portalStartY = this.posY = y;
		this.portalStartZ = this.posZ = z;
		float colorMult = this.rand.nextFloat() * 0.6F + 0.4F;
		this.basePortalParticleScale = this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F * colorMult;
		this.particleGreen *= 0.3F;
		this.particleRed *= 0.9F;
		this.particleMaxAge = (int)(Math.random() * 10.0D) + 40;
		this.noClip = true;
		this.particleTextureIndex = (int)(Math.random() * 8.0D);
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float fadeOut = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
		fadeOut = 1.0F - fadeOut;
		fadeOut *= fadeOut;
		fadeOut = 1.0F - fadeOut;
		this.particleScale = this.basePortalParticleScale * fadeOut;
		super.renderParticle(tessellator, partialTicks, cosYaw, cosPitch, sinYaw, cosPitchNegSinYaw, cosPitchCosYaw);
	}

	public int getBrightnessForRender(float partialTicks) {
		int baseBrightness = super.getBrightnessForRender(partialTicks);
		float fadeFactor = (float)this.particleAge / (float)this.particleMaxAge;
		fadeFactor *= fadeFactor;
		fadeFactor *= fadeFactor;
		int skyLight = baseBrightness & 255;
		int blockLight = baseBrightness >> 16 & 255;
		blockLight += (int)(fadeFactor * 15.0F * 16.0F);
		if(blockLight > 240) {
			blockLight = 240;
		}

		return skyLight | blockLight << 16;
	}
	
	public float getEntityBrightness(float partialTicks) {
		float baseBrightness = super.getEntityBrightness(partialTicks);
		float fadeFactor = (float)this.particleAge / (float)this.particleMaxAge;
		fadeFactor *= fadeFactor;
		fadeFactor *= fadeFactor;
		return baseBrightness * (1.0F - fadeFactor) + fadeFactor;
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		float ageFraction = (float)this.particleAge / (float)this.particleMaxAge;
		float yFloat = ageFraction;
		yFloat = -yFloat + yFloat * yFloat * 2.0F;
		yFloat = 1.0F - yFloat;
		this.posX = this.portalStartX + this.motionX * (double)yFloat;
		this.posY = this.portalStartY + this.motionY * (double)yFloat + (double)(1.0F - yFloat);
		this.posZ = this.portalStartZ + this.motionZ * (double)yFloat;
		if(this.particleAge++ >= this.particleMaxAge) {
			this.setEntityDead();
		}

	}
}
