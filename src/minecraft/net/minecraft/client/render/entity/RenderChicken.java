package net.minecraft.client.render.entity;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.animal.EntityChicken;

public class RenderChicken extends RenderLiving {
	public RenderChicken(ModelBase model, float shadowSize) {
		super(model, shadowSize);
	}

	public void renderChicken(EntityChicken chicken, double x, double y, double z, float yaw, float partialTicks) {
		super.doRenderLiving(chicken, x, y, z, yaw, partialTicks);
	}

	protected float getWingRotation(EntityChicken chicken, float partialTicks) {
		float wingAngle = chicken.field_756_e + (chicken.field_752_b - chicken.field_756_e) * partialTicks;
		float wingFlapSpeed = chicken.field_757_d + (chicken.destPos - chicken.field_757_d) * partialTicks;
		return (MathHelper.sin(wingAngle) + 1.0F) * wingFlapSpeed;
	}

	protected float handleRotationFloat(EntityLiving entity, float partialTicks) {
		return this.getWingRotation((EntityChicken)entity, partialTicks);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderChicken((EntityChicken)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderChicken((EntityChicken)entity, x, y, z, yaw, partialTicks);
	}
}
