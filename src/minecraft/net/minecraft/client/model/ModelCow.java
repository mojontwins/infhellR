package net.minecraft.client.model;

import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.animal.EntityCow;

public class ModelCow extends ModelQuadruped {
	ModelRenderer udders;
	ModelRenderer horn1;
	ModelRenderer horn2;

	public ModelCow() {
		super(12, 0.0F);
		this.head = new ModelRenderer(0, 0);
		this.head.addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6, 0.0F);
		this.head.setRotationPoint(0.0F, 4.0F, -8.0F);
		this.horn1 = new ModelRenderer(22, 0);
		this.horn1.addBox(-5.0F, -5.0F, -4.0F, 1, 3, 1, 0.0F);
		this.horn1.setRotationPoint(0.0F, 3.0F, -7.0F);
		this.horn2 = new ModelRenderer(22, 0);
		this.horn2.addBox(4.0F, -5.0F, -4.0F, 1, 3, 1, 0.0F);
		this.horn2.setRotationPoint(0.0F, 3.0F, -7.0F);
		this.udders = new ModelRenderer(52, 0);
		this.udders.addBox(-2.0F, -3.0F, 0.0F, 4, 6, 2, 0.0F);
		this.udders.setRotationPoint(0.0F, 14.0F, 6.0F);
		this.udders.rotateAngleX = (float)Math.PI / 2F;
		this.body = new ModelRenderer(18, 4);
		this.body.addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10, 0.0F);
		this.body.setRotationPoint(0.0F, 5.0F, 2.0F);
		--this.leg1.rotationPointX;
		++this.leg2.rotationPointX;
		this.leg1.rotationPointZ += 0.0F;
		this.leg2.rotationPointZ += 0.0F;
		--this.leg3.rotationPointX;
		++this.leg4.rotationPointX;
		--this.leg3.rotationPointZ;
		--this.leg4.rotationPointZ;
	}

	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.render(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.horn1.render(scale);
		this.horn2.render(scale);
		this.udders.render(scale);
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);
		this.horn1.rotateAngleY = this.head.rotateAngleY;
		this.horn1.rotateAngleX = this.head.rotateAngleX;
		this.horn2.rotateAngleY = this.head.rotateAngleY;
		this.horn2.rotateAngleX = this.head.rotateAngleX;
	}

	@Override
	public void setLivingAnimations(EntityLiving entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTick);

		this.animateHead((EntityCow) entity, partialTick);
	}

	public void animateHead(EntityCow cow, float partialTick) {
		float angle = cow.getInterestedAngle(partialTick);
		this.head.rotateAngleZ = angle;
		this.horn1.rotateAngleZ = angle;
		this.horn2.rotateAngleZ = angle;
	}
}