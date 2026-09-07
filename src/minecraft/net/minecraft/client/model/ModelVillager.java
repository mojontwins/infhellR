package net.minecraft.client.model;

import net.minecraft.game.MathHelper;

public class ModelVillager extends ModelBase
{
	public ModelRenderer villagerHead;
	public ModelRenderer villagerBody;
	public ModelRenderer villagerArms;
	public ModelRenderer rightVillagerLeg;
	public ModelRenderer leftVillagerLeg;
	public ModelRenderer villagerNose;

	public ModelVillager(float defaultScale) {
		this(defaultScale, 0.0F, 64, 128);
	}

	public ModelVillager(float defaultScale, float headYOffset, int textureWidth, int textureHeight) {
		this.villagerHead = new ModelRenderer(0,0).setTextureSize(textureWidth, textureHeight);
		this.villagerHead.setRotationPoint(0.0F, 0.0F + headYOffset, 0.0F);
		this.villagerHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, defaultScale);

		this.villagerNose = new ModelRenderer(24, 0).setTextureSize(textureWidth, textureHeight);
		this.villagerNose.setRotationPoint(0.0F, headYOffset - 2.0F, 0.0F);
		this.villagerNose.addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2, defaultScale);
		this.villagerHead.addChild(this.villagerNose);

		this.villagerBody = new ModelRenderer().setTextureSize(textureWidth, textureHeight);
		this.villagerBody.setRotationPoint(0.0F, 0.0F + headYOffset, 0.0F);
		this.villagerBody.setTextureOffset(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, defaultScale);
		this.villagerBody.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, defaultScale + 0.5F);

		this.villagerArms = new ModelRenderer().setTextureSize(textureWidth, textureHeight);
		this.villagerArms.setRotationPoint(0.0F, 0.0F + headYOffset + 2.0F, 0.0F);
		this.villagerArms.setTextureOffset(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4, defaultScale);
		this.villagerArms.setTextureOffset(44, 22).addBox(4.0F, -2.0F, -2.0F, 4, 8, 4, defaultScale);
		this.villagerArms.setTextureOffset(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4, defaultScale);

		this.rightVillagerLeg = new ModelRenderer(0, 22).setTextureSize(textureWidth, textureHeight);
		this.rightVillagerLeg.setRotationPoint(-2.0F, 12.0F + headYOffset, 0.0F);
		this.rightVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, defaultScale);

		this.leftVillagerLeg = new ModelRenderer(0, 22).setTextureSize(textureWidth, textureHeight);
		this.leftVillagerLeg.mirror = true;
		this.leftVillagerLeg.setRotationPoint(2.0F, 12.0F + headYOffset, 0.0F);
		this.leftVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, defaultScale);
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.villagerHead.render(scale);
		this.villagerBody.render(scale);
		this.rightVillagerLeg.render(scale);
		this.leftVillagerLeg.render(scale);
		this.villagerArms.render(scale);
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.villagerHead.rotateAngleY = headYaw / (180F / (float)Math.PI);
		this.villagerHead.rotateAngleX = headPitch / (180F / (float)Math.PI);
		this.villagerArms.rotationPointY = 3.0F;
		this.villagerArms.rotationPointZ = -1.0F;
		this.villagerArms.rotateAngleX = -0.75F;
		this.rightVillagerLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
		this.leftVillagerLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * 0.5F;
		this.rightVillagerLeg.rotateAngleY = 0.0F;
		this.leftVillagerLeg.rotateAngleY = 0.0F;
	}
}