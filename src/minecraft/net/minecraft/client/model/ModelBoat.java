package net.minecraft.client.model;

public class ModelBoat extends ModelBase {
	public ModelRenderer[] boatSides = new ModelRenderer[5];

	public ModelBoat() {
		this.boatSides[0] = new ModelRenderer(0, 8);
		this.boatSides[1] = new ModelRenderer(0, 0);
		this.boatSides[2] = new ModelRenderer(0, 0);
		this.boatSides[3] = new ModelRenderer(0, 0);
		this.boatSides[4] = new ModelRenderer(0, 0);
		byte lengthX = 24;
		byte heightY = 6;
		byte lengthZ = 20;
		byte yOffset = 4;
		this.boatSides[0].addBox((float)(-lengthX / 2), (float)(-lengthZ / 2 + 2), -3.0F, lengthX, lengthZ - 4, 4, 0.0F);
		this.boatSides[0].setRotationPoint(0.0F, (float)(0 + yOffset), 0.0F);
		this.boatSides[1].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.boatSides[1].setRotationPoint((float)(-lengthX / 2 + 1), (float)(0 + yOffset), 0.0F);
		this.boatSides[2].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.boatSides[2].setRotationPoint((float)(lengthX / 2 - 1), (float)(0 + yOffset), 0.0F);
		this.boatSides[3].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.boatSides[3].setRotationPoint(0.0F, (float)(0 + yOffset), (float)(-lengthZ / 2 + 1));
		this.boatSides[4].addBox((float)(-lengthX / 2 + 2), (float)(-heightY - 1), -1.0F, lengthX - 4, heightY, 2, 0.0F);
		this.boatSides[4].setRotationPoint(0.0F, (float)(0 + yOffset), (float)(lengthZ / 2 - 1));
		this.boatSides[0].rotateAngleX = (float)Math.PI / 2F;
		this.boatSides[1].rotateAngleY = 4.712389F;
		this.boatSides[2].rotateAngleY = (float)Math.PI / 2F;
		this.boatSides[3].rotateAngleY = (float)Math.PI;
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		for(int i = 0; i < 5; ++i) {
			this.boatSides[i].render(scale);
		}

	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
	}
}