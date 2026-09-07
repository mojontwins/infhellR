package net.minecraft.client.particle;

import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;

public class EntityAuraFX extends EntityFX {
	public EntityAuraFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		float color = rand.nextFloat() * 0.1F + 0.2F;
		particleRed = color;
		particleGreen = color;
		particleBlue = color;
		this.particleTextureIndex = 0;
		setSize(0.02F, 0.02F);
		particleScale = particleScale * (rand.nextFloat() * 0.6F + 0.5F);
		motionX *= 0.019999999552965164D;
		motionY *= 0.019999999552965164D;
		motionZ *= 0.019999999552965164D;
		particleMaxAge = (int)(20D / (Math.random() * 0.80000000000000004D + 0.20000000000000001D));
		noClip = true;
	}

	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		moveEntity(motionX, motionY, motionZ);
		motionX *= 0.98999999999999999D;
		motionY *= 0.98999999999999999D;
		motionZ *= 0.98999999999999999D;

		if (particleMaxAge-- <= 0)
		{
			this.setEntityDead();
		}
	}
}
