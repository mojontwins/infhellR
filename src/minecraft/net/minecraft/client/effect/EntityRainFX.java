package net.minecraft.client.effect;

import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.BlockFluid;
import net.minecraft.game.world.material.Material;
import net.minecraft.client.render.Tessellator;

public class EntityRainFX extends EntityFX {
	public EntityRainFX(World world, double x, double y, double z) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX *= (double)0.3F;
		this.motionY = (double)((float)Math.random() * 0.2F + 0.1F);
		this.motionZ *= (double)0.3F;
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.particleTextureIndex = 19 + this.rand.nextInt(4);
		this.setSize(0.01F, 0.01F);
		this.particleGravity = 0.06F;
		this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		super.renderParticle(tessellator, partialTicks, cosYaw, cosPitch, sinYaw, cosPitchNegSinYaw, cosPitchCosYaw);
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= (double)this.particleGravity;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= (double)0.98F;
		this.motionY *= (double)0.98F;
		this.motionZ *= (double)0.98F;
		if(this.particleMaxAge-- <= 0) {
			this.setEntityDead();
		}

		if(this.onGround) {
			if(Math.random() < 0.5D) {
				this.setEntityDead();
			}

			this.motionX *= (double)0.7F;
			this.motionZ *= (double)0.7F;
		}

		Material material = this.worldObj.getBlockMaterial(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
		if(material.getIsLiquid() || material.isSolid()) {
			double waterSurface = (double)((float)(MathHelper.floor_double(this.posY) + 1) - BlockFluid.getPercentAir(this.worldObj.getBlockMetadata(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))));
			if(this.posY < waterSurface) {
				this.setEntityDead();
			}
		}

	}
}
