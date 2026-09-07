package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.entity.projectile.EntityArrow;

public class RenderArrow extends Render {
	public void renderArrow(EntityArrow arrow, double x, double y, double z, float yaw, float partialTicks) {
		if(arrow.prevRotationYaw != 0.0F || arrow.prevRotationPitch != 0.0F) {
			this.loadTexture("/item/arrows.png");
			GL11.glPushMatrix();
			GL11.glTranslatef((float)x, (float)y, (float)z);
			GL11.glRotatef(arrow.prevRotationYaw + (arrow.rotationYaw - arrow.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(arrow.prevRotationPitch + (arrow.rotationPitch - arrow.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
			Tessellator tessellator = Tessellator.instance;
			byte type = 0;
			float texU = 0.0F;
			float texU2 = 0.5F;
			float texV1 = (float)(0 + type * 10) / 32.0F;
			float texV2 = (float)(5 + type * 10) / 32.0F;
			float texU3 = 0.0F;
			float texU4 = 0.15625F;
			float texV3 = (float)(5 + type * 10) / 32.0F;
			float texV4 = (float)(10 + type * 10) / 32.0F;
			float scale = 0.05625F;
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			float shake = (float)arrow.arrowShake - partialTicks;
			if(shake > 0.0F) {
				float shakeRot = -MathHelper.sin(shake * 3.0F) * shake;
				GL11.glRotatef(shakeRot, 0.0F, 0.0F, 1.0F);
			}

			GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(scale, scale, scale);
			GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
			GL11.glNormal3f(scale, 0.0F, 0.0F);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)texU3, (double)texV3);
			tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)texU4, (double)texV3);
			tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)texU4, (double)texV4);
			tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)texU3, (double)texV4);
			tessellator.draw();
			GL11.glNormal3f(-scale, 0.0F, 0.0F);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)texU3, (double)texV3);
			tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)texU4, (double)texV3);
			tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)texU4, (double)texV4);
			tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)texU3, (double)texV4);
			tessellator.draw();

			for(int i = 0; i < 4; ++i) {
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glNormal3f(0.0F, 0.0F, scale);
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(-8.0D, -2.0D, 0.0D, (double)texU, (double)texV1);
				tessellator.addVertexWithUV(8.0D, -2.0D, 0.0D, (double)texU2, (double)texV1);
				tessellator.addVertexWithUV(8.0D, 2.0D, 0.0D, (double)texU2, (double)texV2);
				tessellator.addVertexWithUV(-8.0D, 2.0D, 0.0D, (double)texU, (double)texV2);
				tessellator.draw();
			}

			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glPopMatrix();
		}
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderArrow((EntityArrow)entity, x, y, z, yaw, partialTicks);
	}
}
