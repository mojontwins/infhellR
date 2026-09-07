package net.minecraft.game.world.block.tileentity;

import net.minecraft.game.MathHelper;
import net.minecraft.game.container.InventoryMob;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.EntityList;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.human.EntityIceArcher;
import net.minecraft.game.entity.human.EntityIceWarrior;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.world.SpawnerAnimals;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.item.ItemArmor;

public class TileEntityMobSpawner extends TileEntity {
	public int delay = -1;
	public String mobID = "Pig";
	public double yaw;
	public double prevYaw = 0.0D;

	private int minArmorTier = 0;
	private int maxArmorTier = 0;

	public TileEntityMobSpawner() {
		this.delay = 20;
	}

	public String getMobID() {
		return this.mobID;
	}

	public void setMobID(String string1) {
		this.mobID = string1;
	}

	public boolean anyPlayerInRange() {
		return this.worldObj.getClosestPlayer((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D, 16.0D) != null;
	}

	public void updateEntity() {
		this.prevYaw = this.yaw;
		if(this.anyPlayerInRange()) {
			double d1 = (double)((float)this.xCoord + this.worldObj.rand.nextFloat());
			double d3 = (double)((float)this.yCoord + this.worldObj.rand.nextFloat());
			double d5 = (double)((float)this.zCoord + this.worldObj.rand.nextFloat());
			this.worldObj.spawnParticle("smoke", d1, d3, d5, 0.0D, 0.0D, 0.0D);
			this.worldObj.spawnParticle("flame", d1, d3, d5, 0.0D, 0.0D, 0.0D);

			for(this.yaw += (double)(1000.0F / ((float)this.delay + 200.0F)); this.yaw > 360.0D; this.prevYaw -= 360.0D) {
				this.yaw -= 360.0D;
			}

			if(!this.worldObj.isRemote) {
				if(this.delay == -1) {
					this.updateDelay();
				}

				if(this.delay > 0) {
					--this.delay;
					return;
				}

				byte b7 = 4;

				for(int i8 = 0; i8 < b7; ++i8) {
					EntityLiving entityLiving9;

					if(this.mobID.equals("Ice")) {
						entityLiving9 = this.worldObj.rand.nextBoolean() ? new EntityIceWarrior(this.worldObj) : new EntityIceArcher(this.worldObj);
					} else {
						entityLiving9 = (EntityLiving)EntityList.createEntityByName(this.mobID, this.worldObj);
					}
					
					if(entityLiving9 == null) {
						return;
					}

					int i10 = this.worldObj.getEntitiesWithinAABB(entityLiving9.getClass(), AxisAlignedBB.getBoundingBoxFromPool((double)this.xCoord, (double)this.yCoord, (double)this.zCoord, (double)(this.xCoord + 1), (double)(this.yCoord + 1), (double)(this.zCoord + 1)).expand(8.0D, 4.0D, 8.0D)).size();
					if(i10 >= 6) {
						this.updateDelay();
						return;
					}
					
					double d11 = (double)this.xCoord + (this.worldObj.rand.nextDouble() - this.worldObj.rand.nextDouble()) * 4.0D;
					double d13 = (double)(this.yCoord + this.worldObj.rand.nextInt(3) - 1);
					double d15 = (double)this.zCoord + (this.worldObj.rand.nextDouble() - this.worldObj.rand.nextDouble()) * 4.0D;
					
					// This seems to be needed!
					int xI = MathHelper.floor(d11);
					int yI = MathHelper.floor(d13);
					int zI = MathHelper.floor(d15);
					if(this.worldObj.isBlockOpaqueCube(xI, yI, zI) || this.worldObj.isBlockOpaqueCube(xI, yI + 1, zI)) {
						return;
					}
					
					entityLiving9.setLocationAndAngles(d11, d13, d15, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
		
					if(entityLiving9.getCanSpawnHere()) {
						// Special inits
						SpawnerAnimals.creatureSpecificInit(entityLiving9, this.worldObj, (int)d11, (int)d13, (int)d15);
						
						this.worldObj.spawnEntityInWorld(entityLiving9);

						for(int i17 = 0; i17 < 20; ++i17) {
							d1 = (double)this.xCoord + 0.5D + ((double)this.worldObj.rand.nextFloat() - 0.5D) * 2.0D;
							d3 = (double)this.yCoord + 0.5D + ((double)this.worldObj.rand.nextFloat() - 0.5D) * 2.0D;
							d5 = (double)this.zCoord + 0.5D + ((double)this.worldObj.rand.nextFloat() - 0.5D) * 2.0D;
							this.worldObj.spawnParticle("smoke", d1, d3, d5, 0.0D, 0.0D, 0.0D);
							this.worldObj.spawnParticle("flame", d1, d3, d5, 0.0D, 0.0D, 0.0D);
						}

						entityLiving9.spawnExplosionParticle();
						this.updateDelay();
						
						// Set armor
						if (entityLiving9 instanceof EntityArmoredMob && this.maxArmorTier > this.minArmorTier && this.maxArmorTier > 0) {
							InventoryMob inventory = new InventoryMob (entityLiving9);
							for (int k = 0; k < 4; k ++) {
								inventory.setArmorItemInSlot(3 - k, ItemArmor.getArmorPieceForTier(this.minArmorTier + this.worldObj.rand.nextInt (1 + this.maxArmorTier - this.minArmorTier), k));
							}
							((EntityArmoredMob) entityLiving9).setInventory(inventory);
						}
					}
				}
			}

			super.updateEntity();
		}
	}

	private void updateDelay() {
		this.delay = 200 + this.worldObj.rand.nextInt(600);
	}

	public void readFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readFromNBT(nBTTagCompound1);
		this.mobID = nBTTagCompound1.getString("EntityId");
		this.delay = nBTTagCompound1.getShort("Delay");
		this.setMinArmorTier(nBTTagCompound1.getByte("MinArmorTier"));
		this.setMaxArmorTier(nBTTagCompound1.getByte("MaxArmorTier"));
	}

	public void writeToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeToNBT(nBTTagCompound1);
		nBTTagCompound1.setString("EntityId", this.mobID);
		nBTTagCompound1.setShort("Delay", (short)this.delay);
		nBTTagCompound1.setByte("MinArmorTier", (byte)this.getMinArmorTier());
		nBTTagCompound1.setByte("MaxArmorTier", (byte)this.getMaxArmorTier());
	}
	
	public int getMinArmorTier() {
		return minArmorTier;
	}

	public void setMinArmorTier(int minArmorTier) {
		this.minArmorTier = minArmorTier;
	}

	public int getMaxArmorTier() {
		return maxArmorTier;
	}

	public void setMaxArmorTier(int maxArmorTier) {
		this.maxArmorTier = maxArmorTier;
	}
}
