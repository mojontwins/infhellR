package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import net.minecraft.game.entity.Entity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.atlas.AtlasTexel;
import net.minecraft.client.render.atlas.TexelScale;
import net.minecraft.client.render.atlas.TextureAtlas;

public class RenderSnowball extends Render {
	private int itemIconIndex;

	public RenderSnowball(int iconIndex) {
		this.itemIconIndex = iconIndex;
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		this.loadTexture("/gui/items.png");
		Tessellator tessellator = Tessellator.instance;
		AtlasTexel.calc(this.itemIconIndex, TextureAtlas.ITEMS);
		float texU1 = TexelScale.u(TextureAtlas.ITEMS, (float)AtlasTexel.u);
		float texU2 = TexelScale.u(TextureAtlas.ITEMS, (float)(AtlasTexel.u + TextureAtlas.TILE));
		float texV1 = TexelScale.v(TextureAtlas.ITEMS, (float)AtlasTexel.v);
		float texV2 = TexelScale.v(TextureAtlas.ITEMS, (float)(AtlasTexel.v + TextureAtlas.TILE));
		float width = 1.0F;
		float height = 0.5F;
		float depth = 0.25F;
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
	}
}
