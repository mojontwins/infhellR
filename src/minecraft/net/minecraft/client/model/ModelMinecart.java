package net.minecraft.client.model;

public class ModelMinecart extends ModelBase {
	public ModelRenderer[] sideModels = new ModelRenderer[7];

	public ModelMinecart() {
		this.sideModels[0] = new ModelRenderer(0, 10);
		this.sideModels[1] = new ModelRenderer(0, 0);
		this.sideModels[2] = new ModelRenderer(0, 0);
		this.sideModels[3] = new ModelRenderer(0, 0);
		this.sideModels[4] = new ModelRenderer(0, 0);
		this.sideModels[5] = new ModelRenderer(44, 10);
		byte lengthX = 20;
		byte heightY = 8;
		byte lengthZ = 16;
		byte yOffset = 4;
		this.sideModels[0].addBox((float)(-lengthX / 2), (float)(-lengthZ / 2), -1.0F, lengthX, lengthZ, 2, 0.0F);
		this.sideModels[0].setRotationPoint(0.0F, (float)(0 + yOffset), 0.0F);
		this.sideModels[5].addBox((float)(-lengthX / 2 + 1), (float)(-lengthZ / 2 + 1), -1.0F, lengthX - 2, lengthZ - 2, 1, 0.0F);
		this.sideModels[5].setRotationPoint(0.0F, (float)(0 + yOffset), 0.0F);
		this.sideModels[1].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.sideModels[1].setRotationPoint((float)(-lengthX / 2 + 1), (float)(0 + yOffset), 0.0F);
		this.sideModels[2].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.sideModels[2].setRotationPoint((float)(lengthX / 2 - 1), (float)(0 + yOffset), 0.0F);
		this.sideModels[3].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.sideModels[3].setRotationPoint(0.0F, (float)(0 + yOffset), (float)(-lengthZ / 2 + 1));
		this.sideModels[4].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.sideModels[4].setRotationPoint(0.0F, (float)(0 + yOffset), (float)(lengthZ / 2 - 1));
		this.sideModels[0].rotateAngleX = (float)Math.PI / 2F;
		this.sideModels[1].rotateAngleY = 4.712389F;
		this.sideModels[2].rotateAngleY = (float)Math.PI / 2F;
		this.sideModels[3].rotateAngleY = (float)Math.PI;
		this.sideModels[5].rotateAngleX = -1.5707964F;
	}

	public void render(float limbSwing, float limbSwingAmount, float yOffset, float headYaw, float headPitch, float scale) {
		this.sideModels[5].rotationPointY = 4.0F - yOffset;

		for(int i = 0; i < 6; ++i) {
			this.sideModels[i].render(scale);
		}

	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
	}
}