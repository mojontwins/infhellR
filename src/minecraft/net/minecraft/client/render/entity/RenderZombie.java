package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.render.OpenGlHelper;

public class RenderZombie extends RenderBiped {
	private String renderPassTexture;

	public RenderZombie(ModelBiped model, float shadowSize, String type) {
		super(model, shadowSize);
		this.setRenderPassModel(model);
		this.renderPassTexture = "/mob/" + type + "_eyes.png";
	}

	protected boolean setZombieEyeBrightness(EntityLiving entity, int pass, float partialTicks) {
		if(pass == 4 && entity.worldObj.worldInfo.isBloodMoon()) {
			this.setRenderPassModel(this.mainModel);
			this.loadTexture(this.renderPassTexture);
			float alpha = 1.0F;
			int lightValue = 61680;
			int skyLight = lightValue % 65536;
			int blockLight = lightValue / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)skyLight / 1.0F, (float)blockLight / 1.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
			return true;
		} else return false;
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		if(super.shouldRenderPass(entity, pass, partialTicks)) return true;

		return this.setZombieEyeBrightness(entity, pass, partialTicks);
	}
}
