package net.minecraft.client.model;

public class ModelSquid extends ModelBase {
	ModelRenderer squidBody;
	ModelRenderer[] squidTentacles = new ModelRenderer[8];

	public ModelSquid() {
		byte headYOffset = -16;
		this.squidBody = new ModelRenderer(0, 0);
		this.squidBody.addBox(-6.0F, -8.0F, -6.0F, 12, 16, 12);
		this.squidBody.rotationPointY += (float)(24 + headYOffset);

		for(int i = 0; i < this.squidTentacles.length; ++i) {
			this.squidTentacles[i] = new ModelRenderer(48, 0);
			double angle = (double)i * Math.PI * 2.0D / (double)this.squidTentacles.length;
			float posX = (float)Math.cos(angle) * 5.0F;
			float posZ = (float)Math.sin(angle) * 5.0F;
			this.squidTentacles[i].addBox(-1.0F, 0.0F, -1.0F, 2, 18, 2);
			this.squidTentacles[i].rotationPointX = posX;
			this.squidTentacles[i].rotationPointZ = posZ;
			this.squidTentacles[i].rotationPointY = (float)(31 + headYOffset);
			angle = (double)i * Math.PI * -2.0D / (double)this.squidTentacles.length + Math.PI / 2D;
			this.squidTentacles[i].rotateAngleY = (float)angle;
		}

	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		for(int i = 0; i < this.squidTentacles.length; ++i) {
			this.squidTentacles[i].rotateAngleX = ageInTicks;
		}

	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.squidBody.render(scale);

		for(int i = 0; i < this.squidTentacles.length; ++i) {
			this.squidTentacles[i].render(scale);
		}

	}
}