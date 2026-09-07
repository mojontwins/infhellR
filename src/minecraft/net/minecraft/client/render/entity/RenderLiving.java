package net.minecraft.client.render.entity;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityCreature;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.client.render.Tessellator;

public class RenderLiving extends Render {
	protected ModelBase mainModel;
	protected ModelBase renderPassModel;

	public RenderLiving(ModelBase model, float shadowSize) {
		this.mainModel = model;
		this.shadowSize = shadowSize;
	}

	public void setRenderPassModel(ModelBase model) {
		this.renderPassModel = model;
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_CULL_FACE);
		this.mainModel.swingProgress = this.renderSwingProgress(entity, partialTicks);
		if(this.renderPassModel != null) {
			this.renderPassModel.swingProgress = this.mainModel.swingProgress;
		}

		this.mainModel.isRiding = entity.isRiding();
		if(this.renderPassModel != null) {
			this.renderPassModel.isRiding = this.mainModel.isRiding;
		}

		this.mainModel.isChild = entity.isChild();
		if(this.renderPassModel != null) {
			this.renderPassModel.isChild = this.mainModel.isChild;
		}

		try {
			float renderYawOffset = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialTicks;
			float bodyYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
			float bodyPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
			this.renderLivingAt(entity, x, y, z);
			float limbSwingTime = this.handleRotationFloat(entity, partialTicks);
			this.rotateCorpse(entity, limbSwingTime, renderYawOffset, partialTicks);
			float scale = 0.0625F;
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glScalef(-1.0F, -1.0F, 1.0F);
			this.preRenderCallback(entity, partialTicks);
			GL11.glTranslatef(0.0F, -24.0F * scale - 0.0078125F, 0.0F);
			float limbSwing = entity.prevLimbYaw + (entity.limbYaw - entity.prevLimbYaw) * partialTicks;
			float limbSwingAmount = entity.limbSwing - entity.limbYaw * (1.0F - partialTicks);
			if(entity.isChild()) {
				limbSwingAmount *= 3.0F;
			}

			if(limbSwing > 1.0F) {
				limbSwing = 1.0F;
			}

			this.loadDownloadableImageTexture(entity.skinUrl, entity.getEntityTexture());
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			this.mainModel.setLivingAnimations(entity, limbSwingAmount, limbSwing, partialTicks);
			this.mainModel.render(limbSwingAmount, limbSwing, limbSwingTime, bodyYaw - renderYawOffset, bodyPitch, scale);

			for(int pass = 0; pass < 5; ++pass) {
				if(this.shouldRenderPass(entity, pass, partialTicks)) {
					this.renderPassModel.setLivingAnimations(entity, limbSwingAmount, limbSwing, partialTicks);
					this.renderPassModel.render(limbSwingAmount, limbSwing, limbSwingTime, bodyYaw - renderYawOffset, bodyPitch, scale);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_ALPHA_TEST);
				}
			}

			this.renderEquippedItems(entity, partialTicks);
			float brightness = entity.getEntityBrightness(partialTicks);
			int color = this.getColorMultiplier(entity, brightness, partialTicks);

			OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

			if((color >> 24 & 255) > 0 || entity.hurtTime > 0 || entity.deathTime > 0) {
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDepthFunc(GL11.GL_EQUAL);
				if(entity.hurtTime > 0 || entity.deathTime > 0) {
					GL11.glColor4f(brightness, 0.0F, 0.0F, 0.4F);
					this.mainModel.render(limbSwingAmount, limbSwing, limbSwingTime, bodyYaw - renderYawOffset, bodyPitch, scale);

					for(int i = 0; i < 4; ++i) {
						if(this.inheritRenderPass(entity, i, partialTicks)) {
							GL11.glColor4f(brightness, 0.0F, 0.0F, 0.4F);
							this.renderPassModel.render(limbSwingAmount, limbSwing, limbSwingTime, bodyYaw - renderYawOffset, bodyPitch, scale);
						}
					}
				}

				if((color >> 24 & 255) > 0) {
					float red = (float)(color >> 16 & 255) / 255.0F;
					float green = (float)(color >> 8 & 255) / 255.0F;
					float blue = (float)(color & 255) / 255.0F;
					float alpha = (float)(color >> 24 & 255) / 255.0F;
					GL11.glColor4f(red, green, blue, alpha);
					this.mainModel.render(limbSwingAmount, limbSwing, limbSwingTime, bodyYaw - renderYawOffset, bodyPitch, scale);

					for(int i = 0; i < 4; ++i) {
						if(this.inheritRenderPass(entity, i, partialTicks)) {
							GL11.glColor4f(red, green, blue, alpha);
							this.renderPassModel.render(limbSwingAmount, limbSwing, limbSwingTime, bodyYaw - renderYawOffset, bodyPitch, scale);
						}
					}
				}

				GL11.glDepthFunc(GL11.GL_LEQUAL);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			}

			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		} catch (Exception e) {
			e.printStackTrace();
		}

		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		this.passSpecialRender(entity, x, y, z);
	}

	protected void renderLivingAt(EntityLiving entity, double x, double y, double z) {
		GL11.glTranslatef((float)x, (float)y, (float)z);
	}

	protected void rotateCorpse(EntityLiving entity, float limbSwingTime, float renderYawOffset, float partialTicks) {
		GL11.glRotatef(180.0F - renderYawOffset, 0.0F, 1.0F, 0.0F);
		if(entity.deathTime > 0) {
			float deathProgress = ((float)entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
			deathProgress = MathHelper.sqrt_float(deathProgress);
			if(deathProgress > 1.0F) {
				deathProgress = 1.0F;
			}

			GL11.glRotatef(deathProgress * this.getDeathMaxRotation(entity), 0.0F, 0.0F, 1.0F);
		}

	}

	protected float renderSwingProgress(EntityLiving entity, float partialTicks) {
		return entity.getSwingProgress(partialTicks);
	}

	protected float handleRotationFloat(EntityLiving entity, float partialTicks) {
		return (float)entity.ticksExisted + partialTicks;
	}

	protected void renderEquippedItems(EntityLiving entity, float partialTicks) {
	}

	protected boolean inheritRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.shouldRenderPass(entity, pass, partialTicks);
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return false;
	}

	protected float getDeathMaxRotation(EntityLiving entity) {
		return 90.0F;
	}

	protected int getColorMultiplier(EntityLiving entity, float brightness, float partialTicks) {
		return 0;
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
	}

	protected void passSpecialRender(EntityLiving entity, double x, double y, double z) {
		if(Minecraft.isDebugInfoEnabled()) {
		} else {
			if(entity instanceof EntityCreature) {
				EntityCreature creature = (EntityCreature)entity;
				if(creature.getName() != null && !"".equals(creature.getName())) {
					this.renderLivingLabel(creature, creature.getName(), x, y, z, 16);
				}
			}
		}
	}

	protected void renderLivingLabel(EntityLiving entity, String text, double x, double y, double z, int maxDistance) {
		float distance = entity.getDistanceToEntity(this.renderManager.livingPlayer);
		if(distance <= (float)maxDistance) {
			FontRenderer fontRenderer = this.getFontRendererFromRenderManager();
			float textScale = 1.6F;
			float inverseTextScale = 0.016666668F * textScale;
			GL11.glPushMatrix();
			GL11.glTranslatef((float)x + 0.0F, (float)y + 2.3F, (float)z);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-inverseTextScale, -inverseTextScale, inverseTextScale);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Tessellator tessellator = Tessellator.instance;
			byte yOffset = (byte)((entity instanceof EntityPlayer) ? 0 : 20);
			if(text.equals("deadmau5")) {
				yOffset = -10;
			}

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			tessellator.startDrawingQuads();
			int textWidth = fontRenderer.getStringWidth(text) / 2;
			tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
			tessellator.addVertex((double)(-textWidth - 1), (double)(-1 + yOffset), 0.0D);
			tessellator.addVertex((double)(-textWidth - 1), (double)(8 + yOffset), 0.0D);
			tessellator.addVertex((double)(textWidth + 1), (double)(8 + yOffset), 0.0D);
			tessellator.addVertex((double)(textWidth + 1), (double)(-1 + yOffset), 0.0D);
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, 553648127);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2, yOffset, -1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.doRenderLiving((EntityLiving)entity, x, y, z, yaw, partialTicks);
	}
}
