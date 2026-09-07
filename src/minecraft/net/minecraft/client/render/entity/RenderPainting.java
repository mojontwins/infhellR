package net.minecraft.client.render.entity;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityPainting;
import net.minecraft.game.entity.EnumArt;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.client.render.Tessellator;

public class RenderPainting extends Render {
	private Random rand = new Random();

	public void renderPainting(EntityPainting painting, double x, double y, double z, float yaw, float partialTicks) {
		this.rand.setSeed(187L);
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		this.loadTexture("/art/kz.png");
		EnumArt art = painting.art;
		float artScale = 0.0625F;
		GL11.glScalef(artScale, artScale, artScale);
		this.renderPaintingTile(painting, art.sizeX, art.sizeY, art.offsetX, art.offsetY);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	private void renderPaintingTile(EntityPainting painting, int sizeX, int sizeY, int texOffsetX, int texOffsetY) {
		float xStart = (float)(-sizeX) / 2.0F;
		float yStart = (float)(-sizeY) / 2.0F;
		float zBack = -0.5F;
		float zFront = 0.5F;

		for(int xTile = 0; xTile < sizeX / 16; ++xTile) {
			for(int yTile = 0; yTile < sizeY / 16; ++yTile) {
				float xRight = xStart + (float)((xTile + 1) * 16);
				float xLeft = xStart + (float)(xTile * 16);
				float yTop = yStart + (float)((yTile + 1) * 16);
				float yBottom = yStart + (float)(yTile * 16);
				this.setPaintingLightmap(painting, (xRight + xLeft) / 2.0F, (yTop + yBottom) / 2.0F);
				float u1 = (float)(texOffsetX + sizeX - xTile * 16) / 256.0F;
				float u2 = (float)(texOffsetX + sizeX - (xTile + 1) * 16) / 256.0F;
				float v1 = (float)(texOffsetY + sizeY - yTile * 16) / 256.0F;
				float v2 = (float)(texOffsetY + sizeY - (yTile + 1) * 16) / 256.0F;
				float frameU1 = 0.75F;
				float frameU2 = 0.8125F;
				float frameV1 = 0.0F;
				float frameV2 = 0.0625F;
				float frameLeftU1 = 0.75F;
				float frameLeftU2 = 0.8125F;
				float frameLeftV1 = 0.001953125F;
				float frameLeftV2 = 0.001953125F;
				float frameLeftU3 = 0.7519531F;
				float frameLeftU4 = 0.7519531F;
				float frameLeftV3 = 0.0F;
				float frameLeftV4 = 0.0625F;
				Tessellator tessellator = Tessellator.instance;
				tessellator.startDrawingQuads();
				tessellator.setNormal(0.0F, 0.0F, -1.0F);
				tessellator.addVertexWithUV((double)xRight, (double)yBottom, (double)zBack, (double)u2, (double)v1);
				tessellator.addVertexWithUV((double)xLeft, (double)yBottom, (double)zBack, (double)u1, (double)v1);
				tessellator.addVertexWithUV((double)xLeft, (double)yTop, (double)zBack, (double)u1, (double)v2);
				tessellator.addVertexWithUV((double)xRight, (double)yTop, (double)zBack, (double)u2, (double)v2);
				tessellator.setNormal(0.0F, 0.0F, 1.0F);
				tessellator.addVertexWithUV((double)xRight, (double)yTop, (double)zFront, (double)frameU1, (double)frameV1);
				tessellator.addVertexWithUV((double)xLeft, (double)yTop, (double)zFront, (double)frameU2, (double)frameV1);
				tessellator.addVertexWithUV((double)xLeft, (double)yBottom, (double)zFront, (double)frameU2, (double)frameV2);
				tessellator.addVertexWithUV((double)xRight, (double)yBottom, (double)zFront, (double)frameU1, (double)frameV2);
				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.addVertexWithUV((double)xRight, (double)yTop, (double)zBack, (double)frameLeftU1, (double)frameLeftV1);
				tessellator.addVertexWithUV((double)xLeft, (double)yTop, (double)zBack, (double)frameLeftU2, (double)frameLeftV1);
				tessellator.addVertexWithUV((double)xLeft, (double)yTop, (double)zFront, (double)frameLeftU2, (double)frameLeftV2);
				tessellator.addVertexWithUV((double)xRight, (double)yTop, (double)zFront, (double)frameLeftU1, (double)frameLeftV2);
				tessellator.setNormal(0.0F, -1.0F, 0.0F);
				tessellator.addVertexWithUV((double)xRight, (double)yBottom, (double)zFront, (double)frameLeftU1, (double)frameLeftV1);
				tessellator.addVertexWithUV((double)xLeft, (double)yBottom, (double)zFront, (double)frameLeftU2, (double)frameLeftV1);
				tessellator.addVertexWithUV((double)xLeft, (double)yBottom, (double)zBack, (double)frameLeftU2, (double)frameLeftV2);
				tessellator.addVertexWithUV((double)xRight, (double)yBottom, (double)zBack, (double)frameLeftU1, (double)frameLeftV2);
				tessellator.setNormal(-1.0F, 0.0F, 0.0F);
				tessellator.addVertexWithUV((double)xRight, (double)yTop, (double)zFront, (double)frameLeftU4, (double)frameLeftV3);
				tessellator.addVertexWithUV((double)xRight, (double)yBottom, (double)zFront, (double)frameLeftU4, (double)frameLeftV4);
				tessellator.addVertexWithUV((double)xRight, (double)yBottom, (double)zBack, (double)frameLeftU3, (double)frameLeftV4);
				tessellator.addVertexWithUV((double)xRight, (double)yTop, (double)zBack, (double)frameLeftU3, (double)frameLeftV3);
				tessellator.setNormal(1.0F, 0.0F, 0.0F);
				tessellator.addVertexWithUV((double)xLeft, (double)yTop, (double)zBack, (double)frameLeftU4, (double)frameLeftV3);
				tessellator.addVertexWithUV((double)xLeft, (double)yBottom, (double)zBack, (double)frameLeftU4, (double)frameLeftV4);
				tessellator.addVertexWithUV((double)xLeft, (double)yBottom, (double)zFront, (double)frameLeftU3, (double)frameLeftV4);
				tessellator.addVertexWithUV((double)xLeft, (double)yTop, (double)zFront, (double)frameLeftU3, (double)frameLeftV3);
				tessellator.draw();
			}
		}

	}

	private void setPaintingLightmap(EntityPainting painting, float tileX, float tileY) {
		int bx = MathHelper.floor_double(painting.posX);
		int by = MathHelper.floor_double(painting.posY + (double)(tileY / 16.0F));
		int bz = MathHelper.floor_double(painting.posZ);
		if(painting.direction == 0) {
			bx = MathHelper.floor_double(painting.posX + (double)(tileX / 16.0F));
		}

		if(painting.direction == 1) {
			bz = MathHelper.floor_double(painting.posZ - (double)(tileX / 16.0F));
		}

		if(painting.direction == 2) {
			bx = MathHelper.floor_double(painting.posX - (double)(tileX / 16.0F));
		}

		if(painting.direction == 3) {
			bz = MathHelper.floor_double(painting.posZ + (double)(tileX / 16.0F));
		}

		int lightValue = this.renderManager.worldObj.getLightBrightnessForSkyBlocks(bx, by, bz, 0);
		int skyLight = lightValue % 65536;
		int blockLight = lightValue / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)skyLight, (float)blockLight);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderPainting((EntityPainting)entity, x, y, z, yaw, partialTicks);
	}
}
