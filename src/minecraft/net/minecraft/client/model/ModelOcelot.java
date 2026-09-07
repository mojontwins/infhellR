package net.minecraft.client.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.animal.EntityBetaOcelot;

public class ModelOcelot extends ModelBase {
	ModelRenderer ocelotLeg1;
	ModelRenderer ocelotLeg2;
	ModelRenderer ocelotLeg3;
	ModelRenderer ocelotLeg4;
	ModelRenderer ocelotTailA;
	ModelRenderer ocelotTailB;
	ModelRenderer ocelotHead;
	ModelRenderer ocelotBody;
	int pose = 1;

	public ModelOcelot() {
		this.ocelotHead = new ModelRenderer();
		this.ocelotHead.setTextureOffset(0, 0).addBox(-2.5F, -2.0F, -3.0F, 5, 4, 5); 	// Main
		this.ocelotHead.setTextureOffset(0, 24).addBox(-1.5F, 0.0F, -4.0F, 3, 2, 2);	// Nose
		this.ocelotHead.setTextureOffset(0, 10).addBox(-2.0F, -3.0F, 0.0F, 1, 1, 2);	// Ear1
		this.ocelotHead.setTextureOffset(6, 10).addBox(1.0F, -3.0F, 0.0F, 1, 1, 2); 	// Ear2
		this.ocelotHead.setRotationPoint(0.0F, 15.0F, -9.0F);

		this.ocelotBody = new ModelRenderer(20, 0);
		this.ocelotBody.addBox(-2.0F, 3.0F, -8.0F, 4, 16, 6, 0.0F);
		this.ocelotBody.setRotationPoint(0.0F, 12.0F, -10.0F);

		this.ocelotTailA = new ModelRenderer(0, 15);
		this.ocelotTailA.addBox(-0.5F, 0.0F, 0.0F, 1, 8, 1);
		this.ocelotTailA.rotateAngleX = 0.9F;
		this.ocelotTailA.setRotationPoint(0.0F, 15.0F, 8.0F);

		this.ocelotTailB = new ModelRenderer(4, 15);
		this.ocelotTailB.addBox(-0.5F, 0.0F, 0.0F, 1, 8, 1);
		this.ocelotTailB.setRotationPoint(0.0F, 20.0F, 14.0F);

		this.ocelotLeg1 = new ModelRenderer(8, 13);
		this.ocelotLeg1.addBox(-1.0F, 0.0F, 1.0F, 2, 6, 2);
		this.ocelotLeg1.setRotationPoint(1.1F, 18.0F, 5.0F);

		this.ocelotLeg2 = new ModelRenderer(8, 13);
		this.ocelotLeg2.addBox(-1.0F, 0.0F, 1.0F, 2, 6, 2);
		this.ocelotLeg2.setRotationPoint(-1.1F, 18.0F, 5.0F);

		this.ocelotLeg3 = new ModelRenderer( 40, 0);
		this.ocelotLeg3.addBox(-1.0F, 0.0F, 0.0F, 2, 10, 2);
		this.ocelotLeg3.setRotationPoint(1.2F, 13.8F, -5.0F);

		this.ocelotLeg4 = new ModelRenderer( 40, 0);
		this.ocelotLeg4.addBox(-1.0F, 0.0F, 0.0F, 2, 10, 2);
		this.ocelotLeg4.setRotationPoint(-1.2F, 13.8F, -5.0F);
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		if(this.isChild) {
			float childScale = 2.0F;
			GL11.glPushMatrix();
			GL11.glScalef(1.5F / childScale, 1.5F / childScale, 1.5F / childScale);
			GL11.glTranslatef(0.0F, 10.0F * scale, 4.0F * scale);
			this.ocelotHead.render(scale);
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glScalef(1.0F / childScale, 1.0F / childScale, 1.0F / childScale);
			GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
			this.ocelotBody.render(scale);
			this.ocelotLeg1.render(scale);
			this.ocelotLeg2.render(scale);
			this.ocelotLeg3.render(scale);
			this.ocelotLeg4.render(scale);
			this.ocelotTailA.render(scale);
			this.ocelotTailB.render(scale);
			GL11.glPopMatrix();
		} else {
			this.ocelotHead.render(scale);
			this.ocelotBody.render(scale);
			this.ocelotTailA.render(scale);
			this.ocelotTailB.render(scale);
			this.ocelotLeg1.render(scale);
			this.ocelotLeg2.render(scale);
			this.ocelotLeg3.render(scale);
			this.ocelotLeg4.render(scale);
		}

	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.ocelotHead.rotateAngleX = headPitch / 57.295776F;
		this.ocelotHead.rotateAngleY = headYaw / 57.295776F;
		if(this.pose != 3) {
			this.ocelotBody.rotateAngleX = (float)Math.PI / 2F;
			if(this.pose == 2) {
				this.ocelotLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
				this.ocelotLeg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 0.3F) * 1.0F * limbSwingAmount;
				this.ocelotLeg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI + 0.3F) * 1.0F * limbSwingAmount;
				this.ocelotLeg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.0F * limbSwingAmount;
				this.ocelotTailB.rotateAngleX = 1.7278761F + 0.31415927F * MathHelper.cos(limbSwing) * limbSwingAmount;
			} else {
				this.ocelotLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
				this.ocelotLeg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.0F * limbSwingAmount;
				this.ocelotLeg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.0F * limbSwingAmount;
				this.ocelotLeg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
				if(this.pose == 1) {
					this.ocelotTailB.rotateAngleX = 1.7278761F + 0.7853982F * MathHelper.cos(limbSwing) * limbSwingAmount;
				} else {
					this.ocelotTailB.rotateAngleX = 1.7278761F + 0.47123894F * MathHelper.cos(limbSwing) * limbSwingAmount;
				}
			}
		}

	}

	public void setLivingAnimations(EntityLiving entity, float limbSwing, float limbSwingAmount, float partialTick) {
		EntityBetaOcelot ocelot = (EntityBetaOcelot)entity;
		this.ocelotBody.rotationPointY = 12.0F;
		this.ocelotBody.rotationPointZ = -10.0F;
		this.ocelotHead.rotationPointY = 15.0F;
		this.ocelotHead.rotationPointZ = -9.0F;
		this.ocelotTailA.rotationPointY = 15.0F;
		this.ocelotTailA.rotationPointZ = 8.0F;
		this.ocelotTailB.rotationPointY = 20.0F;
		this.ocelotTailB.rotationPointZ = 14.0F;
		this.ocelotLeg3.rotationPointY = this.ocelotLeg4.rotationPointY = 13.8F;
		this.ocelotLeg3.rotationPointZ = this.ocelotLeg4.rotationPointZ = -5.0F;
		this.ocelotLeg1.rotationPointY = this.ocelotLeg2.rotationPointY = 18.0F;
		this.ocelotLeg1.rotationPointZ = this.ocelotLeg2.rotationPointZ = 5.0F;
		this.ocelotTailA.rotateAngleX = 0.9F;
		if(ocelot.isSneaking()) {
			++this.ocelotBody.rotationPointY;
			this.ocelotHead.rotationPointY += 2.0F;
			++this.ocelotTailA.rotationPointY;
			this.ocelotTailB.rotationPointY += -4.0F;
			this.ocelotTailB.rotationPointZ += 2.0F;
			this.ocelotTailA.rotateAngleX = (float)Math.PI / 2F;
			this.ocelotTailB.rotateAngleX = (float)Math.PI / 2F;
			this.pose = 0;
		} else if(ocelot.isSprinting()) {
			this.ocelotTailB.rotationPointY = this.ocelotTailA.rotationPointY;
			this.ocelotTailB.rotationPointZ += 2.0F;
			this.ocelotTailA.rotateAngleX = (float)Math.PI / 2F;
			this.ocelotTailB.rotateAngleX = (float)Math.PI / 2F;
			this.pose = 2;
		} else if(ocelot.getIsSitting()) {
			this.ocelotBody.rotateAngleX = 0.7853982F;
			this.ocelotBody.rotationPointY += -4.0F;
			this.ocelotBody.rotationPointZ += 5.0F;
			this.ocelotHead.rotationPointY += -3.3F;
			++this.ocelotHead.rotationPointZ;
			this.ocelotTailA.rotationPointY += 8.0F;
			this.ocelotTailA.rotationPointZ += -2.0F;
			this.ocelotTailB.rotationPointY += 2.0F;
			this.ocelotTailB.rotationPointZ += -0.8F;
			this.ocelotTailA.rotateAngleX = 1.7278761F;
			this.ocelotTailB.rotateAngleX = 2.670354F;
			this.ocelotLeg3.rotateAngleX = this.ocelotLeg4.rotateAngleX = -0.15707964F;
			this.ocelotLeg3.rotationPointY = this.ocelotLeg4.rotationPointY = 15.8F;
			this.ocelotLeg3.rotationPointZ = this.ocelotLeg4.rotationPointZ = -7.0F;
			this.ocelotLeg1.rotateAngleX = this.ocelotLeg2.rotateAngleX = -1.5707964F;
			this.ocelotLeg1.rotationPointY = this.ocelotLeg2.rotationPointY = 21.0F;
			this.ocelotLeg1.rotationPointZ = this.ocelotLeg2.rotationPointZ = 1.0F;
			this.pose = 3;
		} else {
			this.pose = 1;
		}

	}
}