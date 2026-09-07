package net.minecraft.client.model;

import net.minecraft.game.MathHelper;

public class ModelQuadruped extends ModelBase {
	public ModelRenderer head = new ModelRenderer(0, 0);
	public ModelRenderer body;
	public ModelRenderer leg1;
	public ModelRenderer leg2;
	public ModelRenderer leg3;
	public ModelRenderer leg4;

	public ModelQuadruped(int legHeight, float defaultScale) {
		this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, defaultScale);
		this.head.setRotationPoint(0.0F, (float)(18 - legHeight), -6.0F);
		this.body = new ModelRenderer(28, 8);
		this.body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, defaultScale);
		this.body.setRotationPoint(0.0F, (float)(17 - legHeight), 2.0F);
		this.leg1 = new ModelRenderer(0, 16);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, legHeight, 4, defaultScale);
		this.leg1.setRotationPoint(-3.0F, (float)(24 - legHeight), 7.0F);
		this.leg2 = new ModelRenderer(0, 16);
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, legHeight, 4, defaultScale);
		this.leg2.setRotationPoint(3.0F, (float)(24 - legHeight), 7.0F);
		this.leg3 = new ModelRenderer(0, 16);
		this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, legHeight, 4, defaultScale);
		this.leg3.setRotationPoint(-3.0F, (float)(24 - legHeight), -5.0F);
		this.leg4 = new ModelRenderer(0, 16);
		this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, legHeight, 4, defaultScale);
		this.leg4.setRotationPoint(3.0F, (float)(24 - legHeight), -5.0F);
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.head.render(scale);
		this.body.render(scale);
		this.leg1.render(scale);
		this.leg2.render(scale);
		this.leg3.render(scale);
		this.leg4.render(scale);
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.head.rotateAngleX = headPitch / 57.295776F;
		this.head.rotateAngleY = headYaw / 57.295776F;
		this.body.rotateAngleX = (float)Math.PI / 2F;
		this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
	}
}