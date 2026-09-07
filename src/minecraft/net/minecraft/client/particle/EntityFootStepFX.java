package net.minecraft.client.particle;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.client.effect.EntityFX;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.Tessellator;

public class EntityFootStepFX extends EntityFX {
	private int ticksAlive = 0;
	private int ticksToLive = 0;
	private RenderEngine footStepRenderer;

	public EntityFootStepFX(RenderEngine renderer, World world, double x, double y, double z) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.footStepRenderer = renderer;
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.ticksToLive = 200;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float progress = ((float)this.ticksAlive + partialTicks) / (float)this.ticksToLive;
		progress *= progress;
		float alpha = 2.0F - progress * 2.0F;
		if(alpha > 1.0F) {
			alpha = 1.0F;
		}

		alpha *= 0.2F;
		GL11.glDisable(GL11.GL_LIGHTING);
		float halfSize = 0.125F;
		float renderX = (float)(this.posX - interpPosX);
		float renderY = (float)(this.posY - interpPosY);
		float renderZ = (float)(this.posZ - interpPosZ);
		float brightness = this.worldObj.getLightBrightness(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
		this.footStepRenderer.bindTexture(this.footStepRenderer.getTexture("/misc/footprint.png"));
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(brightness, brightness, brightness, alpha);
		tessellator.addVertexWithUV((double)(renderX - halfSize), (double)renderY, (double)(renderZ + halfSize), 0.0D, 1.0D);
		tessellator.addVertexWithUV((double)(renderX + halfSize), (double)renderY, (double)(renderZ + halfSize), 1.0D, 1.0D);
		tessellator.addVertexWithUV((double)(renderX + halfSize), (double)renderY, (double)(renderZ - halfSize), 1.0D, 0.0D);
		tessellator.addVertexWithUV((double)(renderX - halfSize), (double)renderY, (double)(renderZ - halfSize), 0.0D, 0.0D);
		tessellator.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public void onUpdate() {
		++this.ticksAlive;
		if(this.ticksAlive == this.ticksToLive) {
			this.setEntityDead();
		}

	}

	public int getFXLayer() {
		return 3;
	}
}
