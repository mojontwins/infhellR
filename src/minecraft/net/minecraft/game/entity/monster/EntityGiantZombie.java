package net.minecraft.game.entity.monster;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.StepSound;

public class EntityGiantZombie extends EntityZombie {
	public EntityGiantZombie(World world1) {
		super(world1);
		this.moveSpeed = 0.5F;
		this.attackStrength = 50;
		this.health = this.getFullHealth();
		this.yOffset *= 6.0F;
		this.setSize(this.width * 6.0F, this.height * 6.0F);
	}

	@Override
	protected boolean fall(float distance) {
		boolean rebound = super.fall(distance);
		
		int i2 = (int)Math.ceil((double)(distance - 8.0F));
		
		int x = MathHelper.floor_double(this.posX);
		int y = MathHelper.floor_double(this.posY - (double)0.2F - (double)this.yOffset);
		int z = MathHelper.floor_double(this.posZ);
		
		int i3 = this.worldObj.getBlockId(x, y, z);
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		
		if(i3 == Block.slimeBlock.blockID) {
			this.motionY = -(this.motionY * 0.9D);
			rebound = true;
		} else if(i3 == Block.snow.blockID && meta >= 8 || i3 == Block.blockSnow.blockID) {
			this.motionY = 0;
			rebound = true;
		} else if(i2 > 0) {
			this.attackEntityFrom((Entity)null, i2);
		}
		
		if(i3 > 2 || (i3 > 0 && rebound)) {
			Block block = Block.blocksList[i3];
			if (block != null) {
				StepSound stepSound4 = block.stepSound;
				this.worldObj.playSoundAtEntity(this, stepSound4.getStepSound(), stepSound4.getVolume() * 0.5F, stepSound4.getPitch() * 0.75F);
			}
		}
		
		if(this.isFlying()) this.distanceWalkedModified = 0;

		return rebound;
	}
	
	@Override
	public boolean burnsOnDaylight() {
		return false;
	}

	@Override
	public int getFullHealth() {
		return 100;
	}
}
