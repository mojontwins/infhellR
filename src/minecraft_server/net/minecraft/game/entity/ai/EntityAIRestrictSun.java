package net.minecraft.game.entity.ai;

import net.minecraft.game.entity.EntityCreature;

public class EntityAIRestrictSun extends EntityAIBase {
	private EntityCreature theEntity;

	public EntityAIRestrictSun(EntityCreature entityCreature1) {
		this.theEntity = entityCreature1;
	}

	public boolean shouldExecute() {
		return this.theEntity.worldObj.isDaytime();
	}

	public void startExecuting() {
		this.theEntity.getNavigator().setAvoidSun(true);
	}

	public void resetTask() {
		this.theEntity.getNavigator().setAvoidSun(false);
	}
}
