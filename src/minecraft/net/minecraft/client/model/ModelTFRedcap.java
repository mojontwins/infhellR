package net.minecraft.client.model;


public class ModelTFRedcap extends ModelBiped {
	ModelRenderer goblinRightEar;
	ModelRenderer goblinLeftEar;

	public ModelTFRedcap() {
		this.bipedHead = new ModelRenderer(0, 0);
		this.bipedHead.addBox(-3.4F, 1.0F, -4.0F, 7, 7, 7, 0.0F);
		this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedHeadwear = new ModelRenderer(32, 0);
		this.bipedHeadwear.addBox(-2.0F, 0.0F, -3.0F, 4, 5, 7, 0.0F);
		this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedBody = new ModelRenderer(12, 19);
		this.bipedBody.addBox(-4.0F, 6.0F, -2.0F, 8, 9, 4, 0.0F);
		this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedRightArm = new ModelRenderer(36, 17);
		this.bipedRightArm.addBox(-2.0F, 1.0F, -2.0F, 3, 12, 3, 0.0F);
		this.bipedRightArm.setRotationPoint(-5.0F, 5.0F, 0.0F);
		this.bipedLeftArm = new ModelRenderer(36, 17);
		this.bipedLeftArm.addBox(-1.0F, 1.0F, -2.0F, 3, 12, 3, 0.0F);
		this.bipedLeftArm.setRotationPoint(5.0F, 5.0F, 0.0F);
		this.bipedRightLeg = new ModelRenderer(0, 20);
		this.bipedRightLeg.addBox(-2.0F, 2.0F, -1.0F, 3, 9, 3, 0.0F);
		this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		this.bipedLeftLeg = new ModelRenderer(0, 20);
		this.bipedLeftLeg.addBox(-1.0F, 3.0F, -1.0F, 3, 9, 3, 0.0F);
		this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		this.goblinRightEar = new ModelRenderer(48, 20);
		this.goblinRightEar.addBox(3.0F, -2.0F, -1.0F, 2, 3, 1, 0.0F);
		this.goblinRightEar.setRotationPoint(0.0F, 3.0F, 0.0F);
		this.goblinLeftEar = new ModelRenderer(48, 24);
		this.goblinLeftEar.addBox(-5.0F, -2.0F, -1.0F, 2, 3, 1, 0.0F);
		this.goblinLeftEar.setRotationPoint(0.0F, 3.0F, 0.0F);
		this.goblinLeftEar.mirror = true;
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.goblinRightEar.rotateAngleX = this.bipedHead.rotateAngleX;
		this.goblinRightEar.rotateAngleY = this.bipedHead.rotateAngleY;
		this.goblinRightEar.rotateAngleZ = this.bipedHead.rotateAngleZ;
		this.goblinLeftEar.rotateAngleX = this.bipedHead.rotateAngleX;
		this.goblinLeftEar.rotateAngleY = this.bipedHead.rotateAngleY;
		this.goblinLeftEar.rotateAngleZ = this.bipedHead.rotateAngleZ;
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.render(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.goblinRightEar.render(scale);
		this.goblinLeftEar.render(scale);
	}
}