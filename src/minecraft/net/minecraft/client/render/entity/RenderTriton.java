package net.minecraft.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemArmor;
import net.minecraft.game.item.ItemStack;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.game.entity.misc.EntityTriton;
import net.minecraft.client.model.ModelTriton;

public class RenderTriton extends RenderLiving {
	private ModelTriton modelBipedMain = (ModelTriton)this.mainModel;
	private ModelBiped modelArmorChestplate = new ModelBiped(1.0F);
	private ModelBiped modelArmor = new ModelBiped(0.5F);
	private static final String[] armorFilenamePrefix = new String[]{"cloth", "chain", "iron", "diamond", "gold", "pirate", "rags"};
	
	public RenderTriton() {
		super(new ModelTriton(0.0F), 0.5F);
	}
	
	protected boolean setArmorModel(EntityTriton theTriton, int pass, float renderPartialTicks) {
		if(pass < 4) {
			ItemStack itemStack = theTriton.inventory.armorItemInSlot(3 - pass);
			if(itemStack != null) {
				Item item = itemStack.getItem();
				if(item instanceof ItemArmor) {
					ItemArmor itemArmor = (ItemArmor)item;
					this.loadTexture("/armor/" + armorFilenamePrefix[itemArmor.renderIndex] + "_" + (pass == 2 ? 2 : 1) + ".png");
					ModelBiped modelBiped7 = pass == 2 ? this.modelArmor : this.modelArmorChestplate;
					modelBiped7.bipedHead.showModel = pass == 0;
					modelBiped7.bipedHeadwear.showModel = pass == 0;
					modelBiped7.bipedBody.showModel = pass == 1 || pass == 2;
					modelBiped7.bipedRightArm.showModel = pass == 1;
					modelBiped7.bipedLeftArm.showModel = pass == 1;
					modelBiped7.bipedRightLeg.showModel = pass == 2 || pass == 3;
					modelBiped7.bipedLeftLeg.showModel = pass == 2 || pass == 3;
				this.setRenderPassModel(modelBiped7);
				return true;
				}
			}
		}

		return false;
	}

	public void renderTriton(EntityTriton entityTriton1, double d2, double d4, double d6, float f8, float f9) {
		ItemStack itemStack10 = entityTriton1.inventory.getCurrentItem();
		
		this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = itemStack10 != null;
		this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = entityTriton1.isSneaking();
			
		double d11 = d4 - (double)entityTriton1.yOffset;
		if(entityTriton1.isSneaking()) {
			d11 -= 0.125D;
		} 

		super.doRenderLiving(entityTriton1, d2, d11, d6, f8, f9);
		this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = false;
		this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = false;
		this.modelArmorChestplate.isSitting = this.modelArmor.isSitting = this.modelBipedMain.isSitting = false;;
		
	}

	protected void renderSpecials(EntityTriton entityTriton1, float f2) {
		ItemStack itemStack3 = entityTriton1.inventory.armorItemInSlot(3);
		if(itemStack3 != null && itemStack3.getItem().shiftedIndex < 256) {
			GL11.glPushMatrix();
			this.modelBipedMain.bipedHead.postRender(0.0625F);
			if(RenderBlocks.renderItemIn3d(Block.blocksList[itemStack3.itemID].getRenderType())) {
				float f4 = 0.625F;
				GL11.glTranslatef(0.0F, -0.25F, 0.0F);
				GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(f4, -f4, f4);
			}

			this.renderManager.itemRenderer.renderItem(entityTriton1, itemStack3);
			GL11.glPopMatrix();
		}

		float f5;

		ItemStack itemStack21 = entityTriton1.inventory.getCurrentItem();
		if(itemStack21 != null) {
			GL11.glPushMatrix();
			this.modelBipedMain.bipedRightArm.postRender(0.0625F);
			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

			if(itemStack21.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[itemStack21.itemID].getRenderType())) {
				f5 = 0.5F;
				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
				f5 *= 0.75F;
				GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(f5, -f5, f5);
			} else if(Item.itemsList[itemStack21.itemID].isFull3D()) {
				f5 = 0.625F;
				if(Item.itemsList[itemStack21.itemID].shouldRotateAroundWhenRendering()) {
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
				}

				GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
				GL11.glScalef(f5, -f5, f5);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else {
				f5 = 0.375F;
				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
				GL11.glScalef(f5, f5, f5);
				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
			}

			this.renderManager.itemRenderer.renderItem(entityTriton1, itemStack21);
			GL11.glPopMatrix();
		}

	}

	protected void renderPlayerScale(EntityTriton entityTriton1, float f2) {
		float f3 = 0.9375F;
		GL11.glScalef(f3, f3, f3);
	}

	public void drawFirstPersonHand() {
		this.modelBipedMain.swingProgress = 0.0F;
		this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		this.modelBipedMain.bipedRightArm.render(0.0625F);
	}

	protected void renderPlayerSleep(EntityTriton entityTriton1, double d2, double d4, double d6) {
		super.renderLivingAt(entityTriton1, d2, d4, d6);
	}

	protected void rotatePlayer(EntityTriton entityTriton1, float f2, float f3, float f4) {
		super.rotateCorpse(entityTriton1, f2, f3, f4);
	}

	protected void preRenderCallback(EntityLiving entityLiving1, float f2) {
		this.renderPlayerScale((EntityTriton)entityLiving1, f2);
	}

	protected boolean shouldRenderPass(EntityLiving entityLiving1, int i2, float f3) {
		return this.setArmorModel((EntityTriton)entityLiving1, i2, f3);
	}

	protected void renderEquippedItems(EntityLiving entityLiving1, float f2) {
		this.renderSpecials((EntityTriton)entityLiving1, f2);
	}

	protected void rotateCorpse(EntityLiving entityLiving1, float f2, float f3, float f4) {
		this.rotatePlayer((EntityTriton)entityLiving1, f2, f3, f4);
	}

	protected void renderLivingAt(EntityLiving entityLiving1, double d2, double d4, double d6) {
		this.renderPlayerSleep((EntityTriton)entityLiving1, d2, d4, d6);
	}

	public void doRenderLiving(EntityLiving entityLiving1, double d2, double d4, double d6, float f8, float f9) {
		this.renderTriton((EntityTriton)entityLiving1, d2, d4, d6, f8, f9);
	}

	public void doRender(Entity entity1, double d2, double d4, double d6, float f8, float f9) {
		this.renderTriton((EntityTriton)entity1, d2, d4, d6, f8, f9);
	}
}
