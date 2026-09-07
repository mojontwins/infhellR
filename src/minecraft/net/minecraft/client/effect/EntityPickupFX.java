package net.minecraft.client.effect;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.World;
import net.minecraft.client.render.OpenGlHelper;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.RenderManager;

public class EntityPickupFX extends EntityFX {
	private Entity entityToPickUp;
	private Entity entityPickingUp;
	private int age = 0;
	private int maxAge = 0;
	private float yOffset;

	public EntityPickupFX(World world, Entity entityToPickUp, Entity entityPickingUp, float yOffset) {
		super(world, entityToPickUp.posX, entityToPickUp.posY, entityToPickUp.posZ, entityToPickUp.motionX, entityToPickUp.motionY, entityToPickUp.motionZ);
		this.entityToPickUp = entityToPickUp;
		this.entityPickingUp = entityPickingUp;
		this.maxAge = 3;
		this.yOffset = yOffset;
	}

	public void renderParticle(Tessellator tessellator, float partialTicks, float cosYaw, float cosPitch, float sinYaw, float cosPitchNegSinYaw, float cosPitchCosYaw) {
		float progress = ((float)this.age + partialTicks) / (float)this.maxAge;
		progress *= progress;
		double startX = this.entityToPickUp.posX;
		double startY = this.entityToPickUp.posY;
		double startZ = this.entityToPickUp.posZ;
		double interpX = this.entityPickingUp.lastTickPosX + (this.entityPickingUp.posX - this.entityPickingUp.lastTickPosX) * (double)partialTicks;
		double interpY = this.entityPickingUp.lastTickPosY + (this.entityPickingUp.posY - this.entityPickingUp.lastTickPosY) * (double)partialTicks + (double)this.yOffset;
		double interpZ = this.entityPickingUp.lastTickPosZ + (this.entityPickingUp.posZ - this.entityPickingUp.lastTickPosZ) * (double)partialTicks;
		double renderX = startX + (interpX - startX) * (double)progress;
		double renderY = startY + (interpY - startY) * (double)progress;
		double renderZ = startZ + (interpZ - startZ) * (double)progress;

		int brightness = this.getBrightnessForRender(partialTicks);
		int skyLight = brightness % 65536;
		int blockLight = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)skyLight / 1.0F, (float)blockLight / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		renderX -= interpPosX;
		renderY -= interpPosY;
		renderZ -= interpPosZ;
		RenderManager.instance.renderEntityWithPosYaw(this.entityToPickUp, (double)((float)renderX), (double)((float)renderY), (double)((float)renderZ), this.entityToPickUp.rotationYaw, partialTicks);
	}

	public void onUpdate() {
		++this.age;
		if(this.age == this.maxAge) {
			this.setEntityDead();
		}

	}

	public int getFXLayer() {
		return 3;
	}
}
