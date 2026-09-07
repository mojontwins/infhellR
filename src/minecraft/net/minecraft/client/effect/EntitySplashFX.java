package net.minecraft.client.effect;

import net.minecraft.game.world.World;

public class EntitySplashFX extends EntityRainFX {
	public EntitySplashFX(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z);
		this.particleGravity = 0.04F;
		++this.particleTextureIndex;
		if(motionY == 0.0D && (motionX != 0.0D || motionZ != 0.0D)) {
			this.motionX = motionX;
			this.motionY = motionY + 0.1D;
			this.motionZ = motionZ;
		}

	}
}
