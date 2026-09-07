package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.entity.misc.EntityTNTPrimed;

public class RenderTNTPrimed extends Render {
	private RenderBlocks blockRenderer = new RenderBlocks();

	public RenderTNTPrimed() {
		this.shadowSize = 0.5F;
	}

	public void renderTNT(EntityTNTPrimed tnt, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		float scale;
		if((float)tnt.fuse - partialTicks + 1.0F < 10.0F) {
			scale = 1.0F - ((float)tnt.fuse - partialTicks + 1.0F) / 10.0F;
			if(scale < 0.0F) {
				scale = 0.0F;
			}

			if(scale > 1.0F) {
				scale = 1.0F;
			}

			scale *= scale;
			scale *= scale;
			float scaleAll = 1.0F + scale * 0.3F;
			GL11.glScalef(scaleAll, scaleAll, scaleAll);
		}

		scale = (1.0F - ((float)tnt.fuse - partialTicks + 1.0F) / 100.0F) * 0.8F;
		this.loadTexture("/terrain.png");
		this.blockRenderer.renderBlockOnInventory(Block.tnt, 0, tnt.getEntityBrightness(partialTicks));
		if(tnt.fuse / 5 % 2 == 0) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, scale);
			this.blockRenderer.renderBlockOnInventory(Block.tnt, 0, 1.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		GL11.glPopMatrix();
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderTNT((EntityTNTPrimed)entity, x, y, z, yaw, partialTicks);
	}
}
