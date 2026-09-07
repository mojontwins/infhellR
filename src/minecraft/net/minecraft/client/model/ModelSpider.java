package net.minecraft.client.model;

import net.minecraft.game.MathHelper;

public class ModelSpider extends ModelBase {
	public ModelRenderer spiderHead;
	public ModelRenderer spiderNeck;
	public ModelRenderer spiderBody;
	public ModelRenderer spiderLeg1;
	public ModelRenderer spiderLeg2;
	public ModelRenderer spiderLeg3;
	public ModelRenderer spiderLeg4;
	public ModelRenderer spiderLeg5;
	public ModelRenderer spiderLeg6;
	public ModelRenderer spiderLeg7;
	public ModelRenderer spiderLeg8;

	public ModelSpider() {
		float defaultScale = 0.0F;
		byte headYOffset = 15;
		this.spiderHead = new ModelRenderer(32, 4);
		this.spiderHead.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, defaultScale);
		this.spiderHead.setRotationPoint(0.0F, (float)(0 + headYOffset), -3.0F);
		this.spiderNeck = new ModelRenderer(0, 0);
		this.spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, defaultScale);
		this.spiderNeck.setRotationPoint(0.0F, (float)headYOffset, 0.0F);
		this.spiderBody = new ModelRenderer(0, 12);
		this.spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, defaultScale);
		this.spiderBody.setRotationPoint(0.0F, (float)(0 + headYOffset), 9.0F);
		this.spiderLeg1 = new ModelRenderer(18, 0);
		this.spiderLeg1.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg1.setRotationPoint(-4.0F, (float)(0 + headYOffset), 2.0F);
		this.spiderLeg2 = new ModelRenderer(18, 0);
		this.spiderLeg2.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg2.setRotationPoint(4.0F, (float)(0 + headYOffset), 2.0F);
		this.spiderLeg3 = new ModelRenderer(18, 0);
		this.spiderLeg3.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg3.setRotationPoint(-4.0F, (float)(0 + headYOffset), 1.0F);
		this.spiderLeg4 = new ModelRenderer(18, 0);
		this.spiderLeg4.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg4.setRotationPoint(4.0F, (float)(0 + headYOffset), 1.0F);
		this.spiderLeg5 = new ModelRenderer(18, 0);
		this.spiderLeg5.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg5.setRotationPoint(-4.0F, (float)(0 + headYOffset), 0.0F);
		this.spiderLeg6 = new ModelRenderer(18, 0);
		this.spiderLeg6.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg6.setRotationPoint(4.0F, (float)(0 + headYOffset), 0.0F);
		this.spiderLeg7 = new ModelRenderer(18, 0);
		this.spiderLeg7.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg7.setRotationPoint(-4.0F, (float)(0 + headYOffset), -1.0F);
		this.spiderLeg8 = new ModelRenderer(18, 0);
		this.spiderLeg8.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, defaultScale);
		this.spiderLeg8.setRotationPoint(4.0F, (float)(0 + headYOffset), -1.0F);
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.spiderHead.render(scale);
		this.spiderNeck.render(scale);
		this.spiderBody.render(scale);
		this.spiderLeg1.render(scale);
		this.spiderLeg2.render(scale);
		this.spiderLeg3.render(scale);
		this.spiderLeg4.render(scale);
		this.spiderLeg5.render(scale);
		this.spiderLeg6.render(scale);
		this.spiderLeg7.render(scale);
		this.spiderLeg8.render(scale);
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.spiderHead.rotateAngleY = headYaw / 57.295776F;
		this.spiderHead.rotateAngleX = headPitch / 57.295776F;
		float legBaseAngle = 0.7853982F;
		this.spiderLeg1.rotateAngleZ = -legBaseAngle;
		this.spiderLeg2.rotateAngleZ = legBaseAngle;
		this.spiderLeg3.rotateAngleZ = -legBaseAngle * 0.74F;
		this.spiderLeg4.rotateAngleZ = legBaseAngle * 0.74F;
		this.spiderLeg5.rotateAngleZ = -legBaseAngle * 0.74F;
		this.spiderLeg6.rotateAngleZ = legBaseAngle * 0.74F;
		this.spiderLeg7.rotateAngleZ = -legBaseAngle;
		this.spiderLeg8.rotateAngleZ = legBaseAngle;
		float legYawBase = -0.0F;
		float legYawSpread = 0.3926991F;
		this.spiderLeg1.rotateAngleY = legYawSpread * 2.0F + legYawBase;
		this.spiderLeg2.rotateAngleY = -legYawSpread * 2.0F - legYawBase;
		this.spiderLeg3.rotateAngleY = legYawSpread * 1.0F + legYawBase;
		this.spiderLeg4.rotateAngleY = -legYawSpread * 1.0F - legYawBase;
		this.spiderLeg5.rotateAngleY = -legYawSpread * 1.0F + legYawBase;
		this.spiderLeg6.rotateAngleY = legYawSpread * 1.0F - legYawBase;
		this.spiderLeg7.rotateAngleY = -legYawSpread * 2.0F + legYawBase;
		this.spiderLeg8.rotateAngleY = legYawSpread * 2.0F - legYawBase;
		float swingY1 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
		float swingY2 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + (float)Math.PI) * 0.4F) * limbSwingAmount;
		float swingY3 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + (float)Math.PI / 2F) * 0.4F) * limbSwingAmount;
		float swingY4 = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + 4.712389F) * 0.4F) * limbSwingAmount;
		float swingZ1 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + 0.0F) * 0.4F) * limbSwingAmount;
		float swingZ2 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + (float)Math.PI) * 0.4F) * limbSwingAmount;
		float swingZ3 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + (float)Math.PI / 2F) * 0.4F) * limbSwingAmount;
		float swingZ4 = Math.abs(MathHelper.sin(limbSwing * 0.6662F + 4.712389F) * 0.4F) * limbSwingAmount;
		this.spiderLeg1.rotateAngleY += swingY1;
		this.spiderLeg2.rotateAngleY += -swingY1;
		this.spiderLeg3.rotateAngleY += swingY2;
		this.spiderLeg4.rotateAngleY += -swingY2;
		this.spiderLeg5.rotateAngleY += swingY3;
		this.spiderLeg6.rotateAngleY += -swingY3;
		this.spiderLeg7.rotateAngleY += swingY4;
		this.spiderLeg8.rotateAngleY += -swingY4;
		this.spiderLeg1.rotateAngleZ += swingZ1;
		this.spiderLeg2.rotateAngleZ += -swingZ1;
		this.spiderLeg3.rotateAngleZ += swingZ2;
		this.spiderLeg4.rotateAngleZ += -swingZ2;
		this.spiderLeg5.rotateAngleZ += swingZ3;
		this.spiderLeg6.rotateAngleZ += -swingZ3;
		this.spiderLeg7.rotateAngleZ += swingZ4;
		this.spiderLeg8.rotateAngleZ += -swingZ4;
	}
}