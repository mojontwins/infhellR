package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class EntityBuilderZombie extends EntityMob {
	private int collidingTicks;
	public int ticksToBuild = 10;

	public EntityBuilderZombie(World world1) {
		super(world1);
		this.texture = "/mob/zombie1.png";
		this.moveSpeed = 0.5F;
		this.attackStrength = 5;
		this.scoreValue = 15;
	}

	public void onLivingUpdate() {
		if (this.isCollidedHorizontally) {
			this.collidingTicks ++;
		} else {
			this.collidingTicks --;
		}
		
		if (this.entityToAttack != null && this.collidingTicks >= this.ticksToBuild) {
			int x = (int)((this.boundingBox.minX + this.boundingBox.maxX) / 2.0D);
			int y = (int)this.posY;
			int z = (int)((this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D); 
			System.out.println ("Build @ " + x + " " + y + " " + z);
			this.worldObj.setBlock(x, y, z, Block.cobblestone.blockID);
			posY -= 1.0F;
			this.collidingTicks = 0;
		}
		
		super.onLivingUpdate();
	}

	protected Entity findPlayerToAttack() {
		double d2 = 16.0D;
		return this.worldObj.getClosestPlayerToEntity(this, d2);
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
		return Item.feather.shiftedIndex;
	}	
}
