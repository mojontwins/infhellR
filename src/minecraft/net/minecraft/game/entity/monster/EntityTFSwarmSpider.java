package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.status.Status;
import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntityTFSwarmSpider extends EntitySpider {
	public EntityTFSwarmSpider(World world) {
		this(world, true);
	}

	public EntityTFSwarmSpider(World world, boolean spawnMore) {
		super(world);
		this.setSize(0.8F, 0.4F);
		this.attackStrength = 0;
		this.setSpawnMore(spawnMore);
		this.texture = "/mob/swarmspider.png";
		this.health = 3;
	}

	public EntityTFSwarmSpider(World world, double x, double y, double z) {
		this(world);
		this.setPosition(x, y, z);
	}

	@Override
	public int getFullHealth() {
		return 3;
	}

	@Override
	public float spiderScaleAmount() {
		return 0.5F;
	}

	@Override
	public void onUpdate() {
		if(this.shouldSpawnMore()) {
			if(!this.worldObj.isRemote) {
				int more = 1 + this.rand.nextInt(2);
	
				for(int i = 0; i < more; ++i) {
					EntityTFSwarmSpider another = new EntityTFSwarmSpider(this.worldObj, false);
					double sx = this.posX + (this.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
					double sy = this.posY + (double)this.rand.nextInt(3) - 1.0D;
					double sz = this.posZ + (this.worldObj.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
					another.setLocationAndAngles(sx, sy, sz, this.rand.nextFloat() * 360.0F, 0.0F);
					
					this.worldObj.spawnEntityInWorld(another);
				}
			}

			this.setSpawnMore(false);
		}

		super.onUpdate();
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
	}

	@Override
	protected void attackEntity(Entity entity, float f) {
		this.attackStrength = !this.onGround && this.rand.nextInt(4) == 0 ? 1 : 0;
		if(this.attackStrength > 0 && entity instanceof EntityLiving) {
			if(!((EntityLiving)entity).isStatusActive(Status.statusPoisoned)) {
				((EntityLiving)entity).addStatusEffect(new StatusEffect(Status.statusPoisoned.id, 100, 1));
			}
		}
		super.attackEntity(entity, f);
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("SpawnMore", this.shouldSpawnMore());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		this.setSpawnMore(nbttagcompound.getBoolean("SpawnMore"));
	}
	
	public boolean shouldSpawnMore() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 1) != 0;
	}

	public void setSpawnMore(boolean flag) {
		byte byte0 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(flag) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(byte0 | 1));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(byte0 & -2));
		}

	}

}
