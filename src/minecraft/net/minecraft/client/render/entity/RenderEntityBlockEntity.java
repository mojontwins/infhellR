package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.entity.EntityBlockEntity;

public class RenderEntityBlockEntity extends Render {
	private RenderBlocks renderBlocks = new RenderBlocks();

	public RenderEntityBlockEntity() {
		this.shadowSize = 0.5F;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.doRenderBlockEntity((EntityBlockEntity)entity, x, y, z, yaw, partialTicks);
	}

	private void doRenderBlockEntity(EntityBlockEntity blockEntity, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		this.loadTexture("/terrain.png");
		Block block = Block.blocksList[blockEntity.blockID];
		if(block == null) block = Block.stone;

		this.renderBlocks.renderBlockOnInventory(block, 0, blockEntity.getEntityBrightness(partialTicks));

		GL11.glPopMatrix();
	}

}
