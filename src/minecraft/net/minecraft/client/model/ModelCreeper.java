package net.minecraft.client.model;

import net.minecraft.game.MathHelper;

public class ModelCreeper extends ModelBase {
	public ModelRenderer head;
	public ModelRenderer headwear;
	public ModelRenderer body;
	public ModelRenderer leg1;
	public ModelRenderer leg2;
	public ModelRenderer leg3;
	public ModelRenderer leg4;

	public ModelCreeper() {
		this(0.0F);
	}

	public ModelCreeper(float defaultScale) {
		byte headYOffset = 4;
		this.head = new ModelRenderer(0, 0);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, defaultScale);
		this.head.setRotationPoint(0.0F, (float)headYOffset, 0.0F);
		this.headwear = new ModelRenderer(32, 0);
		this.headwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, defaultScale + 0.5F);
		this.headwear.setRotationPoint(0.0F, (float)headYOffset, 0.0F);
		this.body = new ModelRenderer(16, 16);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, defaultScale);
		this.body.setRotationPoint(0.0F, (float)headYOffset, 0.0F);
		this.leg1 = new ModelRenderer(0, 16);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, defaultScale);
		this.leg1.setRotationPoint(-2.0F, (float)(12 + headYOffset), 4.0F);
		this.leg2 = new ModelRenderer(0, 16);
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, defaultScale);
		this.leg2.setRotationPoint(2.0F, (float)(12 + headYOffset), 4.0F);
		this.leg3 = new ModelRenderer(0, 16);
		this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, defaultScale);
		this.leg3.setRotationPoint(-2.0F, (float)(12 + headYOffset), -4.0F);
		this.leg4 = new ModelRenderer(0, 16);
		this.leg4.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, defaultScale);
		this.leg4.setRotationPoint(2.0F, (float)(12 + headYOffset), -4.0F);
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
		this.head.rotateAngleY = headYaw / 57.295776F;
		this.head.rotateAngleX = headPitch / 57.295776F;
		this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
	}
}