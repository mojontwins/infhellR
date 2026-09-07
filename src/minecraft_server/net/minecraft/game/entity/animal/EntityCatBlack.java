package net.minecraft.game.entity.animal;

import net.minecraft.game.world.World;

public class EntityCatBlack extends EntityBetaOcelot {

	public EntityCatBlack(World world1) {
		super(world1);
		this.texture = "/mob/cat_black.png";
		this.setSize(0.8F, 0.8F);
		this.moveSpeed = 1.1F;
		this.health = this.getFullHealth();
	}
	
	public boolean isWet() {
		return false;
	}
	
	// Only spawns on city chunks
	public boolean isUrban() {
		return true;
	}
	
	@Override
	public boolean getCanSpawnHere() {
		return this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}
}
