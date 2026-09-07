package net.minecraft.client.render.entity;

import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.animal.EntityPig;

public class RenderPig extends RenderLiving {
	public RenderPig(ModelBase model, ModelBase saddleModel, float shadowSize) {
		super(model, shadowSize);
		this.setRenderPassModel(saddleModel);
	}

	protected boolean renderSaddledPig(EntityPig pig, int pass, float partialTicks) {
		this.loadTexture("/mob/saddle.png");
		return pass == 0 && pig.getSaddled();
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.renderSaddledPig((EntityPig)entity, pass, partialTicks);
	}
}
