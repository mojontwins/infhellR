package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.entity.misc.EntityFallingSand;

public class RenderFallingSand extends Render {
	private RenderBlocks renderBlocks = new RenderBlocks();

	public RenderFallingSand() {
		this.shadowSize = 0.5F;
	}

	public void doRenderFallingSand(EntityFallingSand fallingSand, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		this.loadTexture("/terrain.png");
		Block block = Block.blocksList[fallingSand.blockID];
		World world = fallingSand.getWorld();
		GL11.glDisable(GL11.GL_LIGHTING);
		this.renderBlocks.renderBlockFallingSand(block, world, MathHelper.floor_double(fallingSand.posX), MathHelper.floor_double(fallingSand.posY), MathHelper.floor_double(fallingSand.posZ));
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.doRenderFallingSand((EntityFallingSand)entity, x, y, z, yaw, partialTicks);
	}
}
