package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.game.entity.monster.EntitySpider;

public class RenderSpider extends RenderLiving {
	public RenderSpider() {
		super(new ModelSpider(), 1.0F);
		this.setRenderPassModel(new ModelSpider());
	}

	protected float setSpiderDeathMaxRotation(EntitySpider spider) {
		return 180.0F;
	}

	protected boolean setSpiderEyeBrightness(EntitySpider spider, int pass, float partialTicks) {
		if(pass != 0) {
			return false;
		} else {
			this.loadTexture("/mob/spider_eyes.png");
			float alpha = 1.0F;
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			int lightValue = 61680;
			int skyLight = lightValue % 65536;
			int blockLight = lightValue / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)skyLight / 1.0F, (float)blockLight / 1.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
			return true;
		}
	}

	protected void scaleSpider(EntitySpider spider, float partialTicks) {
		float scale = spider.spiderScaleAmount();
		GL11.glScalef(scale, scale, scale);
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.scaleSpider((EntitySpider)entity, partialTicks);
	}

	protected float getDeathMaxRotation(EntityLiving entity) {
		return this.setSpiderDeathMaxRotation((EntitySpider)entity);
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.setSpiderEyeBrightness((EntitySpider)entity, pass, partialTicks);
	}
}
