package net.minecraft.client.render.entity;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.monster.EntityHauntedCow;

public class RenderHauntedCow extends RenderLiving {
	public RenderHauntedCow(ModelBase model, float shadowSize) {
		super(model, shadowSize);
	}

	public void renderCow(EntityHauntedCow cow, double x, double y, double z, float yaw, float partialTicks) {
		super.doRenderLiving(cow, x, y, z, yaw, partialTicks);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderCow((EntityHauntedCow)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderCow((EntityHauntedCow)entity, x, y, z, yaw, partialTicks);
	}
}
