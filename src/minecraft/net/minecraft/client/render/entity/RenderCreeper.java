package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.game.entity.monster.EntityCreeper;

public class RenderCreeper extends RenderLiving {
	private ModelBase creeperOverlayModel = new ModelCreeper(2.0F);

	public RenderCreeper() {
		super(new ModelCreeper(), 0.5F);
	}

	protected void updateCreeperScale(EntityCreeper creeper, float partialTicks) {
		float flashTime = creeper.setCreeperFlashTime(partialTicks);
		float scaleX = 1.0F + MathHelper.sin(flashTime * 100.0F) * flashTime * 0.01F;
		if(flashTime < 0.0F) {
			flashTime = 0.0F;
		}

		if(flashTime > 1.0F) {
			flashTime = 1.0F;
		}

		flashTime *= flashTime;
		flashTime *= flashTime;
		float scaleY = (1.0F + flashTime * 0.4F) * scaleX;
		float scaleZ = (1.0F + flashTime * 0.1F) / scaleX;
		GL11.glScalef(scaleY, scaleZ, scaleY);
	}

	protected int updateCreeperColorMultiplier(EntityCreeper creeper, float brightness, float partialTicks) {
		float flashTime = creeper.setCreeperFlashTime(partialTicks);
		if((int)(flashTime * 10.0F) % 2 == 0) {
			return 0;
		} else {
			int alpha = (int)(flashTime * 0.2F * 255.0F);
			if(alpha < 0) {
				alpha = 0;
			}

			if(alpha > 255) {
				alpha = 255;
			}

			short r = 255;
			short g = 255;
			short b = 255;
			return alpha << 24 | r << 16 | g << 8 | b;
		}
	}

	protected boolean renderPoweredOverlay(EntityCreeper creeper, int pass, float partialTicks) {
		if(creeper.getPowered()) {
			if(pass == 1) {
				float time = (float)creeper.ticksExisted + partialTicks;
				this.loadTexture("/armor/power.png");
				GL11.glMatrixMode(GL11.GL_TEXTURE);
				GL11.glLoadIdentity();
				float texOffsetS = time * 0.01F;
				float texOffsetT = time * 0.01F;
				GL11.glTranslatef(texOffsetS, texOffsetT, 0.0F);
				this.setRenderPassModel(this.creeperOverlayModel);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glEnable(GL11.GL_BLEND);
				float color = 0.5F;
				GL11.glColor4f(color, color, color, 1.0F);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
				return true;
			}

			if(pass == 2) {
				GL11.glMatrixMode(GL11.GL_TEXTURE);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_BLEND);
			}
		}

		return false;
	}

	protected boolean inheritRenderPass(EntityCreeper creeper, int pass, float partialTicks) {
		return false;
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.updateCreeperScale((EntityCreeper)entity, partialTicks);
	}

	protected int getColorMultiplier(EntityLiving entity, float brightness, float partialTicks) {
		return this.updateCreeperColorMultiplier((EntityCreeper)entity, brightness, partialTicks);
	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.renderPoweredOverlay((EntityCreeper)entity, pass, partialTicks);
	}

	protected boolean inheritRenderPass(EntityLiving entity, int pass, float partialTicks) {
		return this.inheritRenderPass((EntityCreeper)entity, pass, partialTicks);
	}
}
