package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.projectile.EntityFireball;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.physics.MovingObjectPosition;
import net.minecraft.game.world.World;

public class EntityIceBall extends EntityFireball {

	private int ballDmage = 5;

	public EntityIceBall(World world1) {
		super(world1);
	}

	public EntityIceBall(World world1, double d2, double d4, double d6, double d8, double d10, double d12) {
		super(world1, d2, d4, d6, d8, d10, d12);
	}

	public EntityIceBall(World world1, EntityLiving entityLiving2, double d3, double d5, double d7) {
		super(world1, entityLiving2, d3, d5, d7);
	}

	@Override
	protected void throwableHitEntity(MovingObjectPosition movingObjectPosition3) {
		if(!this.worldObj.isRemote) {
			if(movingObjectPosition3.entityHit != null && movingObjectPosition3.entityHit.attackEntityFrom(this, this.ballDmage)) {
				;
			}
		}

		this.worldObj.playSoundAtEntity(this, "random.explode", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
		this.setEntityDead();		
	}
	
	@Override
	public int minTicksInAir() {
		return 10;
	}
}
