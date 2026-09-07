package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.game.entity.misc.EntityBoat;

public class RenderBoat extends Render {
	protected ModelBase modelBoat;

	public RenderBoat() {
		this.shadowSize = 0.5F;
		this.modelBoat = new ModelBoat();
	}

	public void renderBoat(EntityBoat boat, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glRotatef(180.0F - yaw, 0.0F, 1.0F, 0.0F);
		float timeSinceHit = (float)boat.boatTimeSinceHit - partialTicks;
		float damageTaken = (float)boat.boatCurrentDamage - partialTicks;
		if(damageTaken < 0.0F) {
			damageTaken = 0.0F;
		}

		if(timeSinceHit > 0.0F) {
			GL11.glRotatef(MathHelper.sin(timeSinceHit) * timeSinceHit * damageTaken / 10.0F * (float)boat.boatRockDirection, 1.0F, 0.0F, 0.0F);
		}

		this.loadTexture("/terrain.png");
		float boatScale = 0.75F;
		GL11.glScalef(boatScale, boatScale, boatScale);
		GL11.glScalef(1.0F / boatScale, 1.0F / boatScale, 1.0F / boatScale);
		this.loadTexture("/item/boat.png");
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		this.modelBoat.render(0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderBoat((EntityBoat)entity, x, y, z, yaw, partialTicks);
	}
}
