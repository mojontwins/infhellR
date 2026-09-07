package net.minecraft.client.render.entity;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.animal.EntityWolf;

public class RenderWolf extends RenderLiving {
	public RenderWolf(ModelBase model, float shadowSize) {
		super(model, shadowSize);
	}

	public void renderWolf(EntityWolf wolf, double x, double y, double z, float yaw, float partialTicks) {
		super.doRenderLiving(wolf, x, y, z, yaw, partialTicks);
	}

	protected float getTailRotation(EntityWolf wolf, float partialTicks) {
		return wolf.setTailRotation();
	}

	protected void wolfPreRenderCallback(EntityWolf wolf, float partialTicks) {
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.wolfPreRenderCallback((EntityWolf)entity, partialTicks);
	}

	protected float handleRotationFloat(EntityLiving entity, float partialTicks) {
		return this.getTailRotation((EntityWolf)entity, partialTicks);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderWolf((EntityWolf)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderWolf((EntityWolf)entity, x, y, z, yaw, partialTicks);
	}
}
