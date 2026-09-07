package net.minecraft.client.render.entity;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.animal.EntityCow;

public class RenderCow extends RenderLiving {
	public RenderCow(ModelBase model, float shadowSize) {
		super(model, shadowSize);
	}

	public void renderCow(EntityCow cow, double x, double y, double z, float yaw, float partialTicks) {
		super.doRenderLiving(cow, x, y, z, yaw, partialTicks);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderCow((EntityCow)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderCow((EntityCow)entity, x, y, z, yaw, partialTicks);
	}
}
