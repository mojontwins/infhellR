package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.entity.misc.EntityMinecart;

public class RenderMinecart extends Render {
	protected ModelBase modelMinecart;

	public RenderMinecart() {
		this.shadowSize = 0.5F;
		this.modelMinecart = new ModelMinecart();
	}

	public void renderMinecart(EntityMinecart minecart, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		double interpolatedX = minecart.lastTickPosX + (minecart.posX - minecart.lastTickPosX) * (double)partialTicks;
		double interpolatedY = minecart.lastTickPosY + (minecart.posY - minecart.lastTickPosY) * (double)partialTicks;
		double interpolatedZ = minecart.lastTickPosZ + (minecart.posZ - minecart.lastTickPosZ) * (double)partialTicks;
		double railLookAhead = (double)0.3F;
		Vec3D railCenter = minecart.func_514_g(interpolatedX, interpolatedY, interpolatedZ);
		float pitch = minecart.prevRotationPitch + (minecart.rotationPitch - minecart.prevRotationPitch) * partialTicks;
		if(railCenter != null) {
			Vec3D railAhead = minecart.func_515_a(interpolatedX, interpolatedY, interpolatedZ, railLookAhead);
			Vec3D railBehind = minecart.func_515_a(interpolatedX, interpolatedY, interpolatedZ, -railLookAhead);
			if(railAhead == null) {
				railAhead = railCenter;
			}

			if(railBehind == null) {
				railBehind = railCenter;
			}

			x += railCenter.xCoord - interpolatedX;
			y += (railAhead.yCoord + railBehind.yCoord) / 2.0D - interpolatedY;
			z += railCenter.zCoord - interpolatedZ;
			Vec3D trackDir = railBehind.addVector(-railAhead.xCoord, -railAhead.yCoord, -railAhead.zCoord);
			if(trackDir.lengthVector() != 0.0D) {
				trackDir = trackDir.normalize();
				yaw = (float)(Math.atan2(trackDir.zCoord, trackDir.xCoord) * 180.0D / Math.PI);
				pitch = (float)(Math.atan(trackDir.yCoord) * 73.0D);
			}
		}

		GL11.glTranslatef((float)x, (float)y, (float)z);
		GL11.glRotatef(180.0F - yaw, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-pitch, 0.0F, 0.0F, 1.0F);
		float timeSinceHit = (float)minecart.minecartTimeSinceHit - partialTicks;
		float damageTaken = (float)minecart.minecartCurrentDamage - partialTicks;
		if(damageTaken < 0.0F) {
			damageTaken = 0.0F;
		}

		if(timeSinceHit > 0.0F) {
			GL11.glRotatef(MathHelper.sin(timeSinceHit) * timeSinceHit * damageTaken / 10.0F * (float)minecart.minecartRockDirection, 1.0F, 0.0F, 0.0F);
		}

		if(minecart.minecartType != 0) {
			this.loadTexture("/terrain.png");
			float scale = 0.75F;
			GL11.glScalef(scale, scale, scale);
			GL11.glTranslatef(0.0F, 0.3125F, 0.0F);
			GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
			if(minecart.minecartType == 1) {
				(new RenderBlocks()).renderBlockOnInventory(Block.chest, 0, minecart.getEntityBrightness(partialTicks));
			} else if(minecart.minecartType == 2) {
				(new RenderBlocks()).renderBlockOnInventory(Block.stoneOvenIdle, 0, minecart.getEntityBrightness(partialTicks));
			}

			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -0.3125F, 0.0F);
			GL11.glScalef(1.0F / scale, 1.0F / scale, 1.0F / scale);
		}

		this.loadTexture("/item/cart.png");
		GL11.glScalef(-1.0F, -1.0F, 1.0F);
		this.modelMinecart.render(0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderMinecart((EntityMinecart)entity, x, y, z, yaw, partialTicks);
	}
}
