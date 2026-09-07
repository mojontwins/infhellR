package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;

public class RenderEntity extends Render {
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		renderOffsetAABB(entity.boundingBox, x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ);
		GL11.glPopMatrix();
	}
}
