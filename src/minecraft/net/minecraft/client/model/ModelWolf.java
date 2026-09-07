package net.minecraft.client.model;

import org.lwjgl.opengl.GL11;
import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.animal.EntityWolf;

public class ModelWolf extends ModelBase {
	public ModelRenderer wolfHeadMain;
	public ModelRenderer wolfBody;
	public ModelRenderer wolfLeg1;
	public ModelRenderer wolfLeg2;
	public ModelRenderer wolfLeg3;
	public ModelRenderer wolfLeg4;
	ModelRenderer wolfRightEar;
	ModelRenderer wolfLeftEar;
	ModelRenderer wolfSnout;
	ModelRenderer wolfTail;
	ModelRenderer wolfMane;

	public ModelWolf() {
		float defaultScale = 0.0F;
		float headYOffset = 13.5F;
		this.wolfHeadMain = new ModelRenderer(0, 0);
		this.wolfHeadMain.addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4, defaultScale);
		this.wolfHeadMain.setRotationPoint(-1.0F, headYOffset, -7.0F);
		this.wolfBody = new ModelRenderer(18, 14);
		this.wolfBody.addBox(-4.0F, -2.0F, -3.0F, 6, 9, 6, defaultScale);
		this.wolfBody.setRotationPoint(0.0F, 14.0F, 2.0F);
		this.wolfMane = new ModelRenderer(21, 0);
		this.wolfMane.addBox(-4.0F, -3.0F, -3.0F, 8, 6, 7, defaultScale);
		this.wolfMane.setRotationPoint(-1.0F, 14.0F, 2.0F);
		this.wolfLeg1 = new ModelRenderer(0, 18);
		this.wolfLeg1.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, defaultScale);
		this.wolfLeg1.setRotationPoint(-2.5F, 16.0F, 7.0F);
		this.wolfLeg2 = new ModelRenderer(0, 18);
		this.wolfLeg2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, defaultScale);
		this.wolfLeg2.setRotationPoint(0.5F, 16.0F, 7.0F);
		this.wolfLeg3 = new ModelRenderer(0, 18);
		this.wolfLeg3.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, defaultScale);
		this.wolfLeg3.setRotationPoint(-2.5F, 16.0F, -4.0F);
		this.wolfLeg4 = new ModelRenderer(0, 18);
		this.wolfLeg4.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, defaultScale);
		this.wolfLeg4.setRotationPoint(0.5F, 16.0F, -4.0F);
		this.wolfTail = new ModelRenderer(9, 18);
		this.wolfTail.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, defaultScale);
		this.wolfTail.setRotationPoint(-1.0F, 12.0F, 8.0F);
		this.wolfRightEar = new ModelRenderer(16, 14);
		this.wolfRightEar.addBox(-3.0F, -5.0F, 0.0F, 2, 2, 1, defaultScale);
		this.wolfRightEar.setRotationPoint(-1.0F, headYOffset, -7.0F);
		this.wolfLeftEar = new ModelRenderer(16, 14);
		this.wolfLeftEar.addBox(1.0F, -5.0F, 0.0F, 2, 2, 1, defaultScale);
		this.wolfLeftEar.setRotationPoint(-1.0F, headYOffset, -7.0F);
		this.wolfSnout = new ModelRenderer(0, 10);
		this.wolfSnout.addBox(-2.0F, 0.0F, -5.0F, 3, 3, 4, defaultScale);
		this.wolfSnout.setRotationPoint(-0.5F, headYOffset, -7.0F);
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.render(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.wolfHeadMain.renderWithRotation(scale);
		this.wolfBody.render(scale);
		this.wolfLeg1.render(scale);
		this.wolfLeg2.render(scale);
		this.wolfLeg3.render(scale);
		this.wolfLeg4.render(scale);
		this.wolfRightEar.renderWithRotation(scale);
		this.wolfLeftEar.renderWithRotation(scale);
		this.wolfSnout.renderWithRotation(scale);
		this.wolfTail.renderWithRotation(scale);
		this.wolfMane.render(scale);
	}

	public void setLivingAnimations(EntityLiving entity, float limbSwing, float limbSwingAmount, float partialTick) {
		EntityWolf wolf = (EntityWolf)entity;
		if(wolf.isWolfAngry()) {
			this.wolfTail.rotateAngleY = 0.0F;
		} else {
			this.wolfTail.rotateAngleY = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		}

		if(wolf.getIsSitting()) {
			this.wolfMane.setRotationPoint(-1.0F, 16.0F, -3.0F);
			this.wolfMane.rotateAngleX = 1.2566371F;
			this.wolfMane.rotateAngleY = 0.0F;
			this.wolfBody.setRotationPoint(0.0F, 18.0F, 0.0F);
			this.wolfBody.rotateAngleX = 0.7853982F;
			this.wolfTail.setRotationPoint(-1.0F, 21.0F, 6.0F);
			this.wolfLeg1.setRotationPoint(-2.5F, 22.0F, 2.0F);
			this.wolfLeg1.rotateAngleX = 4.712389F;
			this.wolfLeg2.setRotationPoint(0.5F, 22.0F, 2.0F);
			this.wolfLeg2.rotateAngleX = 4.712389F;
			this.wolfLeg3.rotateAngleX = 5.811947F;
			this.wolfLeg3.setRotationPoint(-2.49F, 17.0F, -4.0F);
			this.wolfLeg4.rotateAngleX = 5.811947F;
			this.wolfLeg4.setRotationPoint(0.51F, 17.0F, -4.0F);
		} else {
			this.wolfBody.setRotationPoint(0.0F, 14.0F, 2.0F);
			this.wolfBody.rotateAngleX = (float)Math.PI / 2F;
			this.wolfMane.setRotationPoint(-1.0F, 14.0F, -3.0F);
			this.wolfMane.rotateAngleX = this.wolfBody.rotateAngleX;
			this.wolfTail.setRotationPoint(-1.0F, 12.0F, 8.0F);
			this.wolfLeg1.setRotationPoint(-2.5F, 16.0F, 7.0F);
			this.wolfLeg2.setRotationPoint(0.5F, 16.0F, 7.0F);
			this.wolfLeg3.setRotationPoint(-2.5F, 16.0F, -4.0F);
			this.wolfLeg4.setRotationPoint(0.5F, 16.0F, -4.0F);
			this.wolfLeg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
			this.wolfLeg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			this.wolfLeg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			this.wolfLeg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		}

		float headAngle = wolf.getInterestedAngle(partialTick) + wolf.getShakeAngle(partialTick, 0.0F);
		this.wolfHeadMain.rotateAngleZ = headAngle;
		this.wolfRightEar.rotateAngleZ = headAngle;
		this.wolfLeftEar.rotateAngleZ = headAngle;
		this.wolfSnout.rotateAngleZ = headAngle;
		this.wolfMane.rotateAngleZ = wolf.getShakeAngle(partialTick, -0.08F);
		this.wolfBody.rotateAngleZ = wolf.getShakeAngle(partialTick, -0.16F);
		this.wolfTail.rotateAngleZ = wolf.getShakeAngle(partialTick, -0.2F);
		if(wolf.getWolfShaking()) {
			float shakeShade = wolf.getEntityBrightness(partialTick) * wolf.getShadingWhileShaking(partialTick);
			GL11.glColor3f(shakeShade, shakeShade, shakeShade);
		}

	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.wolfHeadMain.rotateAngleX = headPitch / 57.295776F;
		this.wolfHeadMain.rotateAngleY = headYaw / 57.295776F;
		this.wolfRightEar.rotateAngleY = this.wolfHeadMain.rotateAngleY;
		this.wolfRightEar.rotateAngleX = this.wolfHeadMain.rotateAngleX;
		this.wolfLeftEar.rotateAngleY = this.wolfHeadMain.rotateAngleY;
		this.wolfLeftEar.rotateAngleX = this.wolfHeadMain.rotateAngleX;
		this.wolfSnout.rotateAngleY = this.wolfHeadMain.rotateAngleY;
		this.wolfSnout.rotateAngleX = this.wolfHeadMain.rotateAngleX;
		this.wolfTail.rotateAngleX = ageInTicks;
	}
}