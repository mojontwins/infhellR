package net.minecraft.client.effect;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;

public class EntityDiggingFX extends EntityFX {
	private Block blockInstance;
	public EntityDiggingFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ, Block block, int side, int metadata) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.blockInstance = block;
		this.particleTextureIndex = block.getBlockTextureFromSideAndMetadata(0, metadata);
		this.particleGravity = block.blockParticleGravity;
		this.particleRed = this.particleGreen = this.particleBlue = 0.6F;
		this.particleScale /= 2.0F;
	}

	public EntityDiggingFX func_4041_a(int x, int y, int z) {
		if(this.blockInstance == Block.grass) {
			return this;
		} else {
			int color = this.blockInstance.colorMultiplier(this.worldObj, x, y, z);
			this.particleRed *= (float)(color >> 16 & 255) / 255.0F;
			this.particleGreen *= (float)(color >> 8 & 255) / 255.0F;
			this.particleBlue *= (float)(color & 255) / 255.0F;
			return this;
		}
	}

	public int getFXLayer() {
		return 1;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		AtlasTexel.calc(this.particleTextureIndex, TextureAtlas.TERRAIN);
		float u0 = TexelScale.u(TextureAtlas.TERRAIN, (float)AtlasTexel.u + this.particleTextureJitterX * 4.0F);
		float u1 = u0 + TexelScale.u(TextureAtlas.TERRAIN, 3.996F);
		float v0 = TexelScale.v(TextureAtlas.TERRAIN, (float)AtlasTexel.v + this.particleTextureJitterY * 4.0F);
		float v1 = v0 + TexelScale.v(TextureAtlas.TERRAIN, 3.996F);
		float halfSize = 0.1F * this.particleScale;
		float renderX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float renderY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float renderZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
		float brightness = this.getEntityBrightness(partialTicks);
		tessellator.setColorOpaque_F(brightness * this.particleRed, brightness * this.particleGreen, brightness * this.particleBlue);
		tessellator.addVertexWithUV((double)(renderX - cosYaw * halfSize - cosPitchNegSinYaw * halfSize), (double)(renderY - cosPitch * halfSize), (double)(renderZ - sinYaw * halfSize - cosPitchCosYaw * halfSize), (double)u0, (double)v1);
		tessellator.addVertexWithUV((double)(renderX - cosYaw * halfSize + cosPitchNegSinYaw * halfSize), (double)(renderY + cosPitch * halfSize), (double)(renderZ - sinYaw * halfSize + cosPitchCosYaw * halfSize), (double)u0, (double)v0);
		tessellator.addVertexWithUV((double)(renderX + cosYaw * halfSize + cosPitchNegSinYaw * halfSize), (double)(renderY + cosPitch * halfSize), (double)(renderZ + sinYaw * halfSize + cosPitchCosYaw * halfSize), (double)u1, (double)v0);
		tessellator.addVertexWithUV((double)(renderX + cosYaw * halfSize - cosPitchNegSinYaw * halfSize), (double)(renderY - cosPitch * halfSize), (double)(renderZ + sinYaw * halfSize - cosPitchCosYaw * halfSize), (double)u1, (double)v1);
	}
}
