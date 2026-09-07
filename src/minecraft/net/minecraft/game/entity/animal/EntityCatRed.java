package net.minecraft.game.entity.animal;

import net.minecraft.game.world.World;

public class EntityCatRed extends EntityBetaOcelot {

	public EntityCatRed(World world1) {
		super(world1);
		this.texture = "/mob/cat_red.png";
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
}
