package net.minecraft.client.effect;

import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.game.world.material.Material;

public class EntityBubbleFX extends EntityFX {
	public EntityBubbleFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.particleTextureIndex = 32;
		this.setSize(0.02F, 0.02F);
		this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
		this.motionX = motionX * (double)0.2F + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
		this.motionY = motionY * (double)0.2F + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
		this.motionZ = motionZ * (double)0.2F + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
		this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY += 0.002D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= (double)0.85F;
		this.motionY *= (double)0.85F;
		this.motionZ *= (double)0.85F;
		if(this.worldObj.getBlockMaterial(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) != Material.water) {
			this.setEntityDead();
		}

		if(this.particleMaxAge-- <= 0) {
			this.setEntityDead();
		}

	}
}
