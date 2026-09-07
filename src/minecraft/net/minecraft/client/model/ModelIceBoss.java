package net.minecraft.client.model;


public class ModelIceBoss extends ModelHuman {
	public boolean isAttacking = true;

	public ModelIceBoss() {
		super();
	}

	public ModelIceBoss(float defaultScale) {
		super(defaultScale);
	}

	public ModelIceBoss(float defaultScale, float headYOffset) {
		super(defaultScale, headYOffset);
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale);

		if(this.isAttacking) {
			this.bipedLeftArm.rotateAngleY = 1.0F;
			this.bipedRightArm.rotateAngleY = 1.0F;
		}
	}

}