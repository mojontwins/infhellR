package net.minecraft.client.particle;

import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;
import net.minecraft.client.render.Tessellator;

public class EntityNoteFX extends EntityFX {
	float baseNoteParticleScale;

	public EntityNoteFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		this(world, x, y, z, motionX, motionY, motionZ, 2.0F);
	}

	public EntityNoteFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ, float scaleFactor) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX *= (double)0.01F;
		this.motionY *= (double)0.01F;
		this.motionZ *= (double)0.01F;
		this.motionY += 0.2D;
		this.particleRed = MathHelper.sin(((float)motionX + 0.0F) * (float)Math.PI * 2.0F) * 0.65F + 0.35F;
		this.particleGreen = MathHelper.sin(((float)motionX + 0.33333334F) * (float)Math.PI * 2.0F) * 0.65F + 0.35F;
		this.particleBlue = MathHelper.sin(((float)motionX + 0.6666667F) * (float)Math.PI * 2.0F) * 0.65F + 0.35F;
		this.particleScale *= 0.75F;
		this.particleScale *= scaleFactor;
		this.baseNoteParticleScale = this.particleScale;
		this.particleMaxAge = 6;
		this.noClip = false;
		this.particleTextureIndex = 64;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float sizeProgress = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
		if(sizeProgress < 0.0F) {
			sizeProgress = 0.0F;
		}

		if(sizeProgress > 1.0F) {
			sizeProgress = 1.0F;
		}

		this.particleScale = this.baseNoteParticleScale * sizeProgress;
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

		this.motionX *= (double)0.66F;
		this.motionY *= (double)0.66F;
		this.motionZ *= (double)0.66F;
		if(this.onGround) {
			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

	}
}
