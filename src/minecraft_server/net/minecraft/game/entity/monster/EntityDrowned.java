package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.IWaterMob;
import net.minecraft.game.item.Item;
import net.minecraft.game.world.World;

public class EntityDrowned extends EntityZombie implements IWaterMob {
	public EntityDrowned(World world) {
		super(world);
		this.texture = "/mob/drowned1.png";
		this.texturePrefix = "drowned";
		this.moveSpeed = 0.5F;
		this.attackStrength = 6;	
		this.scoreValue = 30;
	}
	
	public int getFullHealth() {
		return 22;
	}
	
	protected int getMaxTextureVariations() {
		return 2;
	}
	
	protected int getDropItemId() {
		switch(this.rand.nextInt(8)) {
			case 0:
			case 1:
			case 2:
			case 3: return Item.rottenFlesh.shiftedIndex;
			case 4:
			case 5:
			case 6: return Item.kelp.shiftedIndex;
			default: return Item.porkRaw.shiftedIndex;
		}
	}
	
	public boolean getCanSpawnHere() {
		return this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxesExcludingWater(this, this.boundingBox).size() == 0;
	}

	public boolean canBreatheUnderwater() {
		return true;
	}
	
	public boolean triesToFloat() {
		return false; 
	}
	
	protected String getLivingSound() {
		return "mob.triton.idle";
	}

	protected String getHurtSound() {
		return "mob.triton.hurt";
	}

	protected String getDeathSound() {
		return "mob.triton.death";
	}
}
