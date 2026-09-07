package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class EntityExplodingZombie extends EntityMob {
	public int collidingTicks = 0;
	public int ticksToExplode = 20;

	public EntityExplodingZombie(World world1) {
		super(world1);
		this.texture = "/mob/zombie_exploding.png";
		this.moveSpeed = 0.5F;
		this.attackStrength = 5;
		this.scoreValue = 15;
	}

	public void onLivingUpdate() {
		if (this.isCollidedHorizontally) {
			this.collidingTicks ++;
		} else {
			if (this.collidingTicks > 0) this.collidingTicks --;
		}
		
		if (this.entityToAttack != null && this.collidingTicks >= this.ticksToExplode) {
			this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 3.0F);
			this.setEntityDead();
		}
		
		super.onLivingUpdate();
	}

	protected String getLivingSound() {
		return "mob.zombie";
	}

	protected String getHurtSound() {
		return "mob.zombiehurt";
	}

	protected String getDeathSound() {
		return "mob.zombiedeath";
	}

	protected int getDropItemId() {
		return Block.tnt.blockID;
	}	
	
	// Only spawns on city chunks
	public boolean isUrban() {
		return true;
	}
}
