package net.minecraft.client.model;

public class ModelSlime extends ModelBase {
	ModelRenderer slimeBodies;
	ModelRenderer slimeRightEye;
	ModelRenderer slimeLeftEye;
	ModelRenderer slimeMouth;

	public ModelSlime(int slimeType) {
		this.slimeBodies = new ModelRenderer(0, slimeType);
		this.slimeBodies.addBox(-4.0F, 16.0F, -4.0F, 8, 8, 8);
		if(slimeType > 0) {
			this.slimeBodies = new ModelRenderer(0, slimeType);
			this.slimeBodies.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
			this.slimeRightEye = new ModelRenderer(32, 0);
			this.slimeRightEye.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
			this.slimeLeftEye = new ModelRenderer(32, 4);
			this.slimeLeftEye.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
			this.slimeMouth = new ModelRenderer(32, 8);
			this.slimeMouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
		}

	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.slimeBodies.render(scale);
		if(this.slimeRightEye != null) {
			this.slimeRightEye.render(scale);
			this.slimeLeftEye.render(scale);
			this.slimeMouth.render(scale);
		}

	}
}