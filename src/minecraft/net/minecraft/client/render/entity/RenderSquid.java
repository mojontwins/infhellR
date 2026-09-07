package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.animal.EntitySquid;

public class RenderSquid extends RenderLiving {
	public RenderSquid(ModelBase model, float shadowSize) {
		super(model, shadowSize);
	}

	public void renderSquid(EntitySquid squid, double x, double y, double z, float yaw, float partialTicks) {
		super.doRenderLiving(squid, x, y, z, yaw, partialTicks);
	}

	protected void rotateSquidBody(EntitySquid squid, float limbSwing, float limbSwingAmount, float partialTicks) {
		float bodyPitch = squid.field_21088_b + (squid.field_21089_a - squid.field_21088_b) * partialTicks;
		float bodyYaw = squid.field_21086_f + (squid.field_21087_c - squid.field_21086_f) * partialTicks;
		GL11.glTranslatef(0.0F, 0.5F, 0.0F);
		GL11.glRotatef(180.0F - limbSwingAmount, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(bodyPitch, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(bodyYaw, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(0.0F, -1.2F, 0.0F);
	}

	protected void squidPreRenderCallback(EntitySquid squid, float partialTicks) {
	}

	protected float getTentacleRotation(EntitySquid squid, float partialTicks) {
		float angle = squid.lastTentacleAngle + (squid.tentacleAngle - squid.lastTentacleAngle) * partialTicks;
		return angle;
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.squidPreRenderCallback((EntitySquid)entity, partialTicks);
	}

	protected float handleRotationFloat(EntityLiving entity, float partialTicks) {
		return this.getTentacleRotation((EntitySquid)entity, partialTicks);
	}

	protected void rotateCorpse(EntityLiving entity, float limbSwing, float limbSwingAmount, float partialTicks) {
		this.rotateSquidBody((EntitySquid)entity, limbSwing, limbSwingAmount, partialTicks);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderSquid((EntitySquid)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderSquid((EntitySquid)entity, x, y, z, yaw, partialTicks);
	}
}
