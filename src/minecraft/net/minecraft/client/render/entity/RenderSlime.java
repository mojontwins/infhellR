package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.monster.EntitySlime;

public class RenderSlime extends RenderLiving {
	private ModelBase scaleModel;

	public RenderSlime(ModelBase model, ModelBase slimeModel, float shadowSize) {
		super(model, shadowSize);
		this.scaleModel = slimeModel;
	}

	protected boolean renderSlimePassModel(EntitySlime slime, int pass, float partialTicks) {
		if(pass == 0) {
			this.setRenderPassModel(this.scaleModel);
			GL11.glEnable(GL11.GL_NORMALIZE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			return true;
		} else {
			if(pass == 1) {
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			}

			return false;
		}
	}

	protected void scaleSlime(EntitySlime slime, float partialTicks) {
		int slimeSize = slime.getSlimeSize();
		float squish = (slime.prevSquishFactor + (slime.squishFactor - slime.prevSquishFactor) * partialTicks) / ((float)slimeSize * 0.5F + 1.0F);
		float scale = 1.0F / (squish + 1.0F);
		float size = (float)slimeSize;
		GL11.glScalef(scale * size, 1.0F / scale * size, scale * size);
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.scaleSlime((EntitySlime)entity, partialTicks);
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.renderSlimePassModel((EntitySlime)entity, pass, partialTicks);
	}
}
