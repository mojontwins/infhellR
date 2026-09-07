package net.minecraft.client.model;

import java.util.Random;
import net.minecraft.game.MathHelper;

public class ModelGhast extends ModelBase {
	ModelRenderer body;
	ModelRenderer[] tentacles = new ModelRenderer[9];

	public ModelGhast() {
		byte headYOffset = -16;
		this.body = new ModelRenderer(0, 0);
		this.body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
		this.body.rotationPointY += (float)(24 + headYOffset);
		Random random = new Random(1660L);

		for(int i = 0; i < this.tentacles.length; ++i) {
			this.tentacles[i] = new ModelRenderer(0, 0);
			float posX = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
			float posZ = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
			int tentacleHeight = random.nextInt(7) + 8;
			this.tentacles[i].addBox(-1.0F, 0.0F, -1.0F, 2, tentacleHeight, 2);
			this.tentacles[i].rotationPointX = posX;
			this.tentacles[i].rotationPointZ = posZ;
			this.tentacles[i].rotationPointY = (float)(31 + headYOffset);
		}

	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		for(int i = 0; i < this.tentacles.length; ++i) {
			this.tentacles[i].rotateAngleX = 0.2F * MathHelper.sin(ageInTicks * 0.3F + (float)i) + 0.4F;
		}

	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.body.render(scale);

		for(int i = 0; i < this.tentacles.length; ++i) {
			this.tentacles[i].render(scale);
		}

	}
}