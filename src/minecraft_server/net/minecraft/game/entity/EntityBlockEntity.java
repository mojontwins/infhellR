package net.minecraft.game.entity;

import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

public class EntityBlockEntity extends EntityLiving {
	public int xTile;
	public int yTile;
	public int zTile;
	public int blockID;

	public EntityBlockEntity(World world1) {
		super(world1);
	}

	public void setTilePosition(int xTile, int yTile, int zTile) {
		this.xTile = xTile;
		this.yTile = yTile;
		this.zTile = zTile;
	}
	
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setInteger("xTile", this.xTile);
		nBTTagCompound1.setInteger("yTile", this.yTile);
		nBTTagCompound1.setInteger("zTile", this.zTile);
		nBTTagCompound1.setInteger("BlockID", this.blockID);
	}

	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.xTile = nBTTagCompound1.getInteger("xTile");
		this.yTile = nBTTagCompound1.getInteger("yTile");
		this.zTile = nBTTagCompound1.getInteger("zTile");
		this.blockID = nBTTagCompound1.getInteger("BlockID");
	}

	public World getWorld() {
		return this.worldObj;
	}

	public float getShadowSize() {
		return 0.0F;
	}
}
