package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;

public class EntityElementalCreeper extends EntityCreeper {
	public static final int ELEMENT_DIRT = 0;
	public static final int ELEMENT_FIRE = 1;
	public static final int ELEMENT_ICE = 2;
	public static final int ELEMENT_WATER = 3;
	
	public static final String elementalTexture[] = new String[] { 
		"/mob/creeper_dirt.png", 
		"/mob/creeper_fire.png",
		"/mob/creeper_ice.png",
		"/mob/creeper_water.png"
	};
	
	public static final Block elementalBlocks[] = new Block[] {
		Block.dirt,
		Block.fire, 
		Block.blockSnow,
		Block.waterMoving
	};
	
	public int element;
	
	public EntityElementalCreeper(World world1) {
		super(world1);
		switch(this.rand.nextInt(10)) {
		case 0:
		case 1:
		case 2:
		case 6:
			this.element = ELEMENT_FIRE; break;
		case 3:
		case 4:
		case 5:
			this.element = ELEMENT_ICE; break;
		case 7:
		case 8:
			this.element = ELEMENT_DIRT; break;
		case 9:
			this.element = ELEMENT_WATER; break;
		}
	}
	
	public EntityElementalCreeper(World world1, int element) {
		super(world1);
		this.element = element;
	}
	
	public String getEntityTexture() {
		return elementalTexture[this.element];
	}

	protected void attackEntity(Entity entity, float distance) {
		if(this.getCreeperState() <= 0 && distance < 3.0F || this.getCreeperState() > 0 && distance < 7.0F) {
			if(this.timeSinceIgnited == 0) {
				this.worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
			}

			this.setCreeperState(1);
			++this.timeSinceIgnited;
			if(this.timeSinceIgnited == EntityCreeper.fuseDuration) {
				this.worldObj.createBlockExplosion(this, this.posX, this.posY, this.posZ, 3.0F, elementalBlocks[this.element].blockID);
				this.setEntityDead();
			}

			this.hasAttacked = true;
		}

	}
	
	@Override
	public boolean getCanSpawnHere() {
		return this.rand.nextInt(4) == 0 && super.getCanSpawnHere();
	}
}
