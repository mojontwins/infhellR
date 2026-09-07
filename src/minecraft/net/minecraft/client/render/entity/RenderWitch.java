package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.human.EntityAlphaWitch;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.model.ModelWitch;

public class RenderWitch extends RenderLiving {
	private ModelWitch currentWitchModel;

	public RenderWitch() {
		super(new ModelWitch(0.0F), 0.5F);
		this.currentWitchModel = (ModelWitch)this.mainModel;
	}

	public void renderWitch(EntityAlphaWitch witch, double x, double y, double z, float yaw, float partialTicks) {
		ItemStack heldItem = witch.getHeldItem();

		this.currentWitchModel.isHoldingItem = heldItem != null;
		super.doRenderLiving(witch, x, y, z, yaw, partialTicks);
	}

	protected void renderWitchEquippedItems(EntityAlphaWitch witch, float partialTicks) {
		float brightness = 1.0F;
		GL11.glColor3f(brightness, brightness, brightness);
		super.renderEquippedItems(witch, partialTicks);
		ItemStack heldItem = witch.getHeldItem();

		if(heldItem != null) {
			GL11.glPushMatrix();
			float scale;

			this.currentWitchModel.villagerNose.postRender(0.0625F);
			GL11.glTranslatef(-0.0625F, 0.53125F, 0.21875F);

			if(heldItem.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[heldItem.itemID].getRenderType())) {
				scale = 0.5F;
				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
				scale *= 0.75F;
				GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(scale, -scale, scale);
			} else if(heldItem.itemID == Item.bow.shiftedIndex) {
				scale = 0.625F;
				GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
				GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(scale, -scale, scale);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else if(Item.itemsList[heldItem.itemID].isFull3D()) {
				scale = 0.625F;

				if(Item.itemsList[heldItem.itemID].shouldRotateAroundWhenRendering()) {
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
				}

				this.func_82410_b();
				GL11.glScalef(scale, -scale, scale);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else {
				int color = Item.itemsList[heldItem.itemID].getColorFromDamage(0);
				float r = (float)(color >> 16 & 255) / 255.0F;
				float g = (float)(color >> 8 & 255) / 255.0F;
				float b = (float)(color & 255) / 255.0F;
				float lighting = witch.getEntityBrightness(partialTicks);
				GL11.glColor4f(r * lighting, g * lighting, b * lighting, 1.0F);

				scale = 0.375F;
				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
				GL11.glScalef(scale, scale, scale);
				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
			}

			GL11.glRotatef(-15.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(40.0F, 0.0F, 0.0F, 1.0F);
			this.renderManager.itemRenderer.renderItem(witch, heldItem);

			GL11.glPopMatrix();
		}
	}

	protected void func_82410_b() {
		GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
	}

	protected void witchModelScale(EntityAlphaWitch witch, float partialTicks) {
		float scale = 0.9375F;
		GL11.glScalef(scale, scale, scale);
	}

	protected void preRenderCallback(EntityLiving entity, float partialTicks) {
		this.witchModelScale((EntityAlphaWitch)entity, partialTicks);
	}

	protected void renderEquippedItems(EntityLiving entity, float partialTicks) {
		this.renderWitchEquippedItems((EntityAlphaWitch)entity, partialTicks);
	}

	public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderWitch((EntityAlphaWitch)entity, x, y, z, yaw, partialTicks);
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		this.renderWitch((EntityAlphaWitch)entity, x, y, z, yaw, partialTicks);
	}
}
