package net.minecraft.client.model;


public class ModelWitch extends ModelVillager
{
	public boolean isHoldingItem = false;
	private ModelRenderer witchWart = new ModelRenderer().setTextureSize(64, 128);
	private ModelRenderer witchCap;

	public ModelWitch(float defaultScale) {
		super(defaultScale, 0.0F, 64, 128);

		this.witchWart.setRotationPoint(0.0F, -2.0F, 0.0F);
		this.witchWart.setTextureOffset(0, 0).addBox(0.0F, 3.0F, -6.75F, 1, 1, 1, -0.25F);
		this.villagerNose.addChild(this.witchWart);

		this.witchCap = new ModelRenderer().setTextureSize(64, 128);
		this.witchCap.setRotationPoint(-5.0F, -10.03125F, -5.0F);
		this.witchCap.setTextureOffset(0, 64).addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
		this.villagerHead.addChild(this.witchCap);

		ModelRenderer capMid = new ModelRenderer().setTextureSize(64, 128);
		capMid.setRotationPoint(1.75F, -4.0F, 2.0F);
		capMid.setTextureOffset(0, 76).addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
		capMid.rotateAngleX = -0.05235988F;
		capMid.rotateAngleZ = 0.02617994F;
		this.witchCap.addChild(capMid);

		ModelRenderer capTop = new ModelRenderer().setTextureSize(64, 128);
		capTop.setRotationPoint(1.75F, -4.0F, 2.0F);
		capTop.setTextureOffset(0, 87).addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
		capTop.rotateAngleX = -0.10471976F;
		capTop.rotateAngleZ = 0.05235988F;
		capMid.addChild(capTop);

		ModelRenderer capTip = new ModelRenderer().setTextureSize(64, 128);
		capTip.setRotationPoint(1.75F, -2.0F, 2.0F);
		capTip.setTextureOffset(0, 95).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
		capTip.rotateAngleX = -0.20943952F;
		capTip.rotateAngleZ = 0.10471976F;
		capTop.addChild(capTip);
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.villagerNose.translateX = this.villagerNose.translateY = this.villagerNose.translateZ = 0.0F;

		if (this.isHoldingItem) {
			this.villagerNose.rotateAngleX = -0.9F;
			this.villagerNose.translateZ = -0.09375F;
			this.villagerNose.translateY = 0.1875F;
		} else {
			this.villagerNose.rotateAngleX = -0.0F;
			this.villagerNose.translateZ = -0.0F;
			this.villagerNose.translateY = 0.0F;
		}
	}

	public int modelId() {
		return 0;
	}
}