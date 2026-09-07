package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.container.IInventory;
import net.minecraft.game.container.InventoryMob;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.item.ItemArmor;

public class RenderBiped extends RenderLiving {
	protected ModelBiped modelBipedMain;

	protected ModelBiped modelArmorChestplate;
	protected ModelBiped modelArmor;

	public RenderBiped(ModelBiped model, float shadowSize) {
		super(model, shadowSize);
		this.modelBipedMain = model;
		this.modelArmorChestplate = new ModelBiped(1.0F);
		this.modelArmor = new ModelBiped(0.5F);
		this.modelArmorChestplate.aimedBow = this.modelBipedMain.aimedBow;
	}


	protected void renderEquippedItems(EntityLiving entity, float partialTicks) {
		ItemStack heldItem = entity.getHeldItem();
		if(heldItem != null && heldItem.itemID != 0) {
			GL11.glPushMatrix();
			this.modelBipedMain.bipedRightArm.postRender(0.0625F);
			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
			float scale;

			Block block = null;
			if(heldItem.itemID < 256) block = Block.blocksList[heldItem.itemID];

			if(block != null && RenderBlocks.renderItemIn3d(block.getRenderType())) {
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
			} else if(Item.itemsList[heldItem.itemID] != null && Item.itemsList[heldItem.itemID].isFull3D()) {
				scale = 0.625F;
				GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
				GL11.glScalef(scale, -scale, scale);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else {
				scale = 0.375F;
				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
				GL11.glScalef(scale, scale, scale);
				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
			}

			this.renderManager.itemRenderer.renderItem(entity, heldItem);
			GL11.glPopMatrix();
		}

	}

	protected boolean shouldRenderPass(EntityLiving entity, int pass, float partialTicks) {
		if(pass < 4) {

			IInventory inventory = entity.getIInventory();
			if(inventory != null && inventory instanceof InventoryMob) {

				ItemStack armorItem = ((InventoryMob)inventory).getArmorItemInSlot(3 - pass);

				if(armorItem != null) {
					Item item = armorItem.getItem();
					if(item instanceof ItemArmor) {
						ItemArmor armor = (ItemArmor)item;
						this.loadTexture("/armor/" + ItemArmor.getArmorFilenamePrefix(armor.renderIndex) + "_" + (pass == 2 ? 2 : 1) + ".png");

						ModelBiped armorModel = pass == 2 ? this.modelArmor : this.modelArmorChestplate;

						armorModel.bipedHead.showModel = pass == 0;
						armorModel.bipedHeadwear.showModel = pass == 0;

						armorModel.bipedBody.showModel = pass == 1 || pass == 2;
						armorModel.bipedRightArm.showModel = pass == 1;
						armorModel.bipedLeftArm.showModel = pass == 1;

						armorModel.bipedRightLeg.showModel = pass == 2 || pass == 3;
						armorModel.bipedLeftLeg.showModel = pass == 2 || pass == 3;

						this.setRenderPassModel(armorModel);

						return true;
					} else if(item.shiftedIndex < 256) {
						GL11.glPushMatrix();
						this.modelBipedMain.bipedHead.postRender(0.0625F);
						if(RenderBlocks.renderItemIn3d(Block.blocksList[item.shiftedIndex].getRenderType())) {
							float scale = 0.625F;
							GL11.glTranslatef(0.0F, -0.25F, 0.0F);
							GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
							GL11.glScalef(scale, -scale, scale);
						}

						this.renderManager.itemRenderer.renderItem(entity, armorItem);
						GL11.glPopMatrix();
					}
				}
			}
		}

		return false;
	}
}
