package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.monster.EntityGiantZombie;

public class RenderGiantZombie extends RenderLiving {
	private float scale;

	public RenderGiantZombie(ModelBase model, float shadowSize, float scale) {
		super(model, shadowSize * scale);
		this.scale = scale;
	}

	protected void preRenderScale(EntityGiantZombie giant, float partialTicks) {
		GL11.glScalef(this.scale, this.scale, this.scale);
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.preRenderScale((EntityGiantZombie)entity, partialTicks);
	}
}
