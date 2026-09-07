package net.minecraft.game.world.block.tileentity;

import net.minecraft.game.entity.EntityList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.IMobWithLevel;
import net.minecraft.game.entity.human.EntityAlphaWitch;

public class TileEntityMobSpawnerOneshot extends TileEntityMobSpawner {
	// Just spawns its mob once when the player approaches.
	public void updateEntity() {
		this.prevYaw = this.yaw;
		if(this.anyPlayerInRange() && !this.worldObj.isRemote) {				
			// Generate entity in place
			EntityLiving entityLiving = (EntityLiving)EntityList.createEntityByName(this.mobID, this.worldObj);
			if(entityLiving == null) {
				return;
			}
			
			double d11 = (double)this.xCoord + 0.5D;
			double d13 = (double)this.yCoord + 1.0F;
			double d15 = (double)this.zCoord + 0.5D;
			
			entityLiving.setLocationAndAngles(d11, d13, d15, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
			
			//if(!entityLiving.getCanSpawnHere()) return;
			
			// If entity supports levels, set level with metadata
			if(entityLiving instanceof IMobWithLevel) {
				((IMobWithLevel)entityLiving).setLevel(this.getBlockMetadata());
			}
			
			// Special inits
			if(entityLiving instanceof EntityAlphaWitch) {
				((EntityAlphaWitch)entityLiving).fillInventory();
			}
			
			this.worldObj.spawnEntityInWorld(entityLiving);

			for(int i17 = 0; i17 < 20; ++i17) {
				double d1 = (double)this.xCoord + 0.5D + ((double)this.worldObj.rand.nextFloat() - 0.5D) * 2.0D;
				double d3 = (double)this.yCoord + 0.5D + ((double)this.worldObj.rand.nextFloat() - 0.5D) * 2.0D;
				double d5 = (double)this.zCoord + 0.5D + ((double)this.worldObj.rand.nextFloat() - 0.5D) * 2.0D;
				this.worldObj.spawnParticle("smoke", d1, d3, d5, 0.0D, 0.0D, 0.0D);
				this.worldObj.spawnParticle("flame", d1, d3, d5, 0.0D, 0.0D, 0.0D);
			}

			entityLiving.spawnExplosionParticle();
			
			// Remove itself
			this.worldObj.setBlockWithNotify(this.xCoord, this.yCoord, this.zCoord, 0);
		}
		
		super.updateEntity();
	}
}
