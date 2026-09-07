package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.game.entity.animal.EntitySheep;

public class RenderSheep extends RenderLiving {
	public RenderSheep(ModelBase model, ModelBase woolModel, float shadowSize) {
		super(model, shadowSize);
		this.setRenderPassModel(woolModel);
	}

	protected boolean setWoolColorAndRender(EntitySheep sheep, int pass, float partialTicks) {
		if(pass == 0 && !sheep.getSheared()) {
			this.loadTexture("/mob/sheep_fur.png");
			float brightness = 1.0F;
			int fleeceColor = sheep.getFleeceColor();
			GL11.glColor3f(brightness * EntitySheep.fleeceColorTable[fleeceColor][0], brightness * EntitySheep.fleeceColorTable[fleeceColor][1], brightness * EntitySheep.fleeceColorTable[fleeceColor][2]);
			return true;
		} else {
			return false;
		}
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.setWoolColorAndRender((EntitySheep)entity, pass, partialTicks);
	}
}
