package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.GameSettingsValues;
import net.minecraft.client.render.Tessellator;
import net.minecraft.game.entity.EntityFish;

public class RenderFish extends Render {
	public void doRenderFishHook(EntityFish fishHook, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		byte texX = 1;
		byte texY = 2;
		this.loadTexture("/particles.png");
		Tessellator tessellator = Tessellator.instance;
		float texU1 = (float)(texX * 8 + 0) / 128.0F;
		float texU2 = (float)(texX * 8 + 8) / 128.0F;
		float texV1 = (float)(texY * 8 + 0) / 128.0F;
		float texV2 = (float)(texY * 8 + 8) / 128.0F;
		float width = 1.0F;
		float height = 0.5F;
		float depth = 0.5F;
		GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV((double)(0.0F - height), (double)(0.0F - depth), 0.0D, (double)texU1, (double)texV2);
		tessellator.addVertexWithUV((double)(width - height), (double)(0.0F - depth), 0.0D, (double)texU2, (double)texV2);
		tessellator.addVertexWithUV((double)(width - height), (double)(1.0F - depth), 0.0D, (double)texU2, (double)texV1);
		tessellator.addVertexWithUV((double)(0.0F - height), (double)(1.0F - depth), 0.0D, (double)texU1, (double)texV1);
		tessellator.draw();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
		if(fishHook.angler != null) {
			float anglerYaw = (fishHook.angler.prevRotationYaw + (fishHook.angler.rotationYaw - fishHook.angler.prevRotationYaw) * partialTicks) * (float)Math.PI / 180.0F;
			double sinYaw = (double)MathHelper.sin(anglerYaw);
			double cosYaw = (double)MathHelper.cos(anglerYaw);
			float swingProgress = fishHook.angler.getSwingProgress(partialTicks);
			float flapAngle = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
			Vec3D rodEnd = Vec3D.createVector(-0.5D, 0.03D, 0.8D);
			rodEnd.rotateAroundX(-(fishHook.angler.prevRotationPitch + (fishHook.angler.rotationPitch - fishHook.angler.prevRotationPitch) * partialTicks) * (float)Math.PI / 180.0F);
			rodEnd.rotateAroundY(-(fishHook.angler.prevRotationYaw + (fishHook.angler.rotationYaw - fishHook.angler.prevRotationYaw) * partialTicks) * (float)Math.PI / 180.0F);
			rodEnd.rotateAroundY(flapAngle * 0.5F);
			rodEnd.rotateAroundX(-flapAngle * 0.7F);
			double hookX = fishHook.angler.prevPosX + (fishHook.angler.posX - fishHook.angler.prevPosX) * (double)partialTicks + rodEnd.xCoord;
			double hookY = fishHook.angler.prevPosY + (fishHook.angler.posY - fishHook.angler.prevPosY) * (double)partialTicks + rodEnd.yCoord;
			double hookZ = fishHook.angler.prevPosZ + (fishHook.angler.posZ - fishHook.angler.prevPosZ) * (double)partialTicks + rodEnd.zCoord;
			if(GameSettingsValues.thirdPersonView) {
				anglerYaw = (fishHook.angler.prevRenderYawOffset + (fishHook.angler.renderYawOffset - fishHook.angler.prevRenderYawOffset) * partialTicks) * (float)Math.PI / 180.0F;
				sinYaw = (double)MathHelper.sin(anglerYaw);
				cosYaw = (double)MathHelper.cos(anglerYaw);
				hookX = fishHook.angler.prevPosX + (fishHook.angler.posX - fishHook.angler.prevPosX) * (double)partialTicks - cosYaw * 0.35D - sinYaw * 0.85D;
				hookY = fishHook.angler.prevPosY + (fishHook.angler.posY - fishHook.angler.prevPosY) * (double)partialTicks - 0.45D;
				hookZ = fishHook.angler.prevPosZ + (fishHook.angler.posZ - fishHook.angler.prevPosZ) * (double)partialTicks - sinYaw * 0.35D + cosYaw * 0.85D;
			}

			double fishX = fishHook.prevPosX + (fishHook.posX - fishHook.prevPosX) * (double)partialTicks;
			double fishY = fishHook.prevPosY + (fishHook.posY - fishHook.prevPosY) * (double)partialTicks + 0.25D;
			double fishZ = fishHook.prevPosZ + (fishHook.posZ - fishHook.prevPosZ) * (double)partialTicks;
			double dx = (double)((float)(hookX - fishX));
			double dy = (double)((float)(hookY - fishY));
			double dz = (double)((float)(hookZ - fishZ));
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			tessellator.startDrawing(3);
			tessellator.setColorOpaque_I(0);
			byte segments = 16;

			for(int i = 0; i <= segments; ++i) {
				float t = (float)i / (float)segments;
				tessellator.addVertex(x + dx * (double)t, y + dy * (double)(t * t + t) * 0.5D + 0.25D, z + dz * (double)t);
			}

			tessellator.draw();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.doRenderFishHook((EntityFish)entity, x, y, z, yaw, partialTicks);
	}
}
