package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelTFWraith;

public class RenderTFWraith extends RenderBiped {

	public RenderTFWraith(ModelBiped model, float shadowSize) {
		super(model, shadowSize);
		this.setRenderPassModel(new ModelTFWraith());
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		if(pass == 2) {
			this.loadTexture("/mob/wraith.png");
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			return true;
		} else {
			return false;
		}
	}

}
