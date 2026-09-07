package net.minecraft.client.particle;

import net.minecraft.game.world.World;

public class EntityGlowdustFX extends EntityReddustFX {
	public EntityGlowdustFX(World world, double x, double y, double z) {
		this(world, x, y, z, 1.0F);
	}
	
	public EntityGlowdustFX(World world, double x, double y, double z, float colorMultiplier) {
		super(world, x, y, z, colorMultiplier, 1.0F, 1.0F, 1.0F);
		this.particleRed = this.particleGreen = (float)(Math.random() * (double)0.3F) + 0.7F;
		this.particleBlue = (float)(Math.random() * (double)0.1F);
	}

}
