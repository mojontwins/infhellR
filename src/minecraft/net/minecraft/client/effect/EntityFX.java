package net.minecraft.client.effect;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;

public class EntityFX extends Entity {
	protected int particleTextureIndex;
	protected float particleTextureJitterX;
	protected float particleTextureJitterY;
	protected int particleAge = 0;
	protected int particleMaxAge = 0;
	protected float particleScale;
	protected float particleGravity;
	protected float particleRed;
	protected float particleGreen;
	protected float particleBlue;
	public static double interpPosX;
	public static double interpPosY;
	public static double interpPosZ;

	public EntityFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world);
		this.setSize(0.2F, 0.2F);
		this.yOffset = this.height / 2.0F;
		this.setPosition(x, y, z);
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
		this.motionX = motionX + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.4F);
		this.motionY = motionY + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.4F);
		this.motionZ = motionZ + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.4F);
		float speedScale = (float)(Math.random() + Math.random() + 1.0D) * 0.15F;
		float motionLength = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		this.motionX = this.motionX / (double)motionLength * (double)speedScale * (double)0.4F;
		this.motionY = this.motionY / (double)motionLength * (double)speedScale * (double)0.4F + (double)0.1F;
		this.motionZ = this.motionZ / (double)motionLength * (double)speedScale * (double)0.4F;
		this.particleTextureJitterX = this.rand.nextFloat() * 3.0F;
		this.particleTextureJitterY = this.rand.nextFloat() * 3.0F;
		this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
		this.particleMaxAge = (int)(4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
		this.particleAge = 0;
	}

	public EntityFX multiplyVelocity(float factor) {
		this.motionX *= (double)factor;
		this.motionY = (this.motionY - (double)0.1F) * (double)factor + (double)0.1F;
		this.motionZ *= (double)factor;
		return this;
	}

	public EntityFX func_405_d(float factor) {
		this.setSize(0.2F * factor, 0.2F * factor);
		this.particleScale *= factor;
		return this;
	}

	protected boolean canTriggerWalking() {
		return false;
	}

	protected void entityInit() {
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if(this.particleAge++ >= this.particleMaxAge) {
			this.setEntityDead();
		}

		this.motionY -= 0.04D * (double)this.particleGravity;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= (double)0.98F;
		this.motionY *= (double)0.98F;
		this.motionZ *= (double)0.98F;
		if(this.onGround) {
			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		AtlasTexel.calc(this.particleTextureIndex, TextureAtlas.TERRAIN);
		float u0 = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u);
		float u1 = u0 + TexelScale.u(TextureAtlas.TERRAIN, 15.984F);
		float v0 = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v);
		float v1 = v0 + TexelScale.v(TextureAtlas.TERRAIN, 15.984F);
		float halfSize = 0.1F * this.particleScale;
		float renderX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float renderY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float renderZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
		float brightness = 1.0F; // this.getEntityBrightness(partialTicks);
		tessellator.setColorOpaque_F(this.particleRed * brightness, this.particleGreen * brightness, this.particleBlue * brightness);
		tessellator.addVertexWithUV((double)(renderX - cosYaw * halfSize - cosPitchNegSinYaw * halfSize), (double)(renderY - cosPitch * halfSize), (double)(renderZ - sinYaw * halfSize - cosPitchCosYaw * halfSize), (double)u1, (double)v1);
		tessellator.addVertexWithUV((double)(renderX - cosYaw * halfSize + cosPitchNegSinYaw * halfSize), (double)(renderY + cosPitch * halfSize), (double)(renderZ - sinYaw * halfSize + cosPitchCosYaw * halfSize), (double)u1, (double)v0);
		tessellator.addVertexWithUV((double)(renderX + cosYaw * halfSize + cosPitchNegSinYaw * halfSize), (double)(renderY + cosPitch * halfSize), (double)(renderZ + sinYaw * halfSize + cosPitchCosYaw * halfSize), (double)u0, (double)v0);
		tessellator.addVertexWithUV((double)(renderX + cosYaw * halfSize - cosPitchNegSinYaw * halfSize), (double)(renderY - cosPitch * halfSize), (double)(renderZ + sinYaw * halfSize - cosPitchCosYaw * halfSize), (double)u0, (double)v1);
	}

	public int getFXLayer() {
		return 0;
	}

	public void writeEntityToNBT(NBTTagCompound nbt) {
	}

	public void readEntityFromNBT(NBTTagCompound nbt) {
	}

	public void setParticleTextureIndex(int index) {
		this.particleTextureIndex = index;
	}

	public int getParticleTextureIndex() {
		return this.particleTextureIndex;
	}
	
}
