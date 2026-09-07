package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.entity.monster.EntityExplodingZombie;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBiped;

public class RenderExplodingZombie extends RenderZombie {

	public RenderExplodingZombie(ModelBiped model, float shadowSize) {
		super(model, shadowSize, "zombie");
	}

	private boolean tintRedWithCounter(EntityExplodingZombie zombie) {
		if(zombie.collidingTicks > 0) {
			this.loadTexture("/mob/zombie_red.png");
			float intensity = (float)zombie.collidingTicks / (float)zombie.ticksToExplode;
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, intensity);
			return true;
		} else return false;
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.tintRedWithCounter((EntityExplodingZombie)entity);
	}
}
