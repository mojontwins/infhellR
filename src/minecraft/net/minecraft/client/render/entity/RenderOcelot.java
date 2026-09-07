package net.minecraft.client.render.entity;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;

public class RenderOcelot extends RenderLiving {
	public RenderOcelot(ModelBase model, float shadowSize) {
		super(model, shadowSize);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.doRenderLiving((EntityLiving)entity, x, y, z, yaw, partialTicks);
	}
}
