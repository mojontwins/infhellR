package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.game.entity.animal.EntityCow;

public class RenderMooshroom extends RenderLiving {
	public RenderMooshroom(ModelBase model, float shadowSize) {
		super(model, shadowSize);
	}

	public void renderCow(EntityCow cow, double x, double y, double z, float yaw, float partialTicks) {
		super.doRenderLiving(cow, x, y, z, yaw, partialTicks);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderCow((EntityCow)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderCow((EntityCow)entity, x, y, z, yaw, partialTicks);
	}

	protected void renderEquippedItems(EntityLiving entity, float partialTicks) {
		loadTexture("/terrain.png");
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPushMatrix();
		GL11.glScalef(1.0F, -1F, 1.0F);
		GL11.glTranslatef(0.2F, 0.4F, 0.5F);
		GL11.glRotatef(42F, 0.0F, 1.0F, 0.0F);
		renderBlocks.renderBlockOnInventory(Block.mushroomRed, 0, 1.0F);
		GL11.glTranslatef(0.1F, 0.0F, -0.6F);
		GL11.glRotatef(42F, 0.0F, 1.0F, 0.0F);
		renderBlocks.renderBlockOnInventory(Block.mushroomRed, 0, 1.0F);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		((ModelQuadruped)mainModel).head.postRender(0.0625F);
		GL11.glScalef(1.0F, -1F, 1.0F);
		GL11.glTranslatef(0.0F, 0.75F, -0.2F);
		GL11.glRotatef(12F, 0.0F, 1.0F, 0.0F);
		renderBlocks.renderBlockOnInventory(Block.mushroomRed, 0, 1.0F);
		GL11.glPopMatrix();
		GL11.glDisable(GL11.GL_CULL_FACE);
		return;
	}
}
