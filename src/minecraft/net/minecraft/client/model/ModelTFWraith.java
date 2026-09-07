package net.minecraft.client.model;


public class ModelTFWraith extends ModelZombie {
	public ModelRenderer dress;

	public ModelTFWraith() {
		float defaultScale = 0.0F;
		this.dress = new ModelRenderer(40, 16);
		this.dress.addBox(-4.0F, 12.0F, -2.0F, 8, 12, 4, defaultScale);
		this.dress.setRotationPoint(0.0F, 0.0F, 0.0F);
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.bipedHead.render(scale);
		this.bipedBody.render(scale);
		this.bipedRightArm.render(scale);
		this.bipedLeftArm.render(scale);
		this.dress.render(scale);
	}
}