package net.minecraft.client.model;

import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.animal.EntityPig;

public class ModelPig extends ModelQuadruped {
	public ModelPig() {
		super(6, 0.0F);
	}

	public ModelPig(float defaultScale) {
		super(6, defaultScale);
	}

	@Override
	public void setLivingAnimations(EntityLiving entity, float limbSwing, float limbSwingAmount, float partialTick) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTick);

		this.animateHead((EntityPig) entity, partialTick);
	}

	public void animateHead(EntityPig pig, float partialTick) {
		this.head.rotateAngleZ = pig.getInterestedAngle(partialTick);
	}
}