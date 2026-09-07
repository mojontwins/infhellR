package net.minecraft.client.effect;

import net.minecraft.game.world.World;
import net.minecraft.client.render.Tessellator;

public class EntityLavaFX extends EntityFX {
	private float baseLavaParticleScale;

	public EntityLavaFX(World world, double x, double y, double z) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX *= (double)0.8F;
		this.motionY *= (double)0.8F;
		this.motionZ *= (double)0.8F;
		this.motionY = (double)(this.rand.nextFloat() * 0.4F + 0.05F);
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
		this.particleScale *= this.rand.nextFloat() * 2.0F + 0.2F;
		this.baseLavaParticleScale = this.particleScale;
		this.particleMaxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
		this.noClip = false;
		this.particleTextureIndex = 49;
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
		short skyLight = 240;
		int blockLight = baseBrightness >> 16 & 255;
		return skyLight | blockLight << 16;
	}
	
	public float getEntityBrightness(float partialTicks) {
		return 1.0F;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float ageFraction = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
		this.particleScale = this.baseLavaParticleScale * (1.0F - ageFraction * ageFraction);
		super.renderParticle(tessellator, partialTicks, cosYaw, cosPitch, sinYaw, cosPitchNegSinYaw, cosPitchCosYaw);
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if(this.particleAge++ >= this.particleMaxAge) {
			this.setEntityDead();
		}

		float ageFraction = (float)this.particleAge / (float)this.particleMaxAge;
		if(this.rand.nextFloat() > ageFraction) {
			this.worldObj.spawnParticle("smoke", this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
		}

		this.motionY -= 0.03D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= (double)0.999F;
		this.motionY *= (double)0.999F;
		this.motionZ *= (double)0.999F;
		if(this.onGround) {
			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

	}
}
