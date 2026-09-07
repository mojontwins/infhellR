package net.minecraft.game.entity;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

public class EntityMeatBlock extends EntityBlockEntity {
	public static final int MAXDURATION = 24000;
	public int meatDuration;
	public int prevMeta;
	
	public EntityMeatBlock(World world) {
		super(world);
		this.texture = "/mob/invisible.png";
		this.setSize(0.5F,1.0F);
		this.meatDuration = 0;
		this.prevMeta = 0;
	}

	@Override
	public void onUpdate() {
		++this.meatDuration;
		int meta = (int)(meatDuration * 15 / 24000);
		
		if (this.meatDuration >= MAXDURATION) {
			this.setEntityDead();
		}
		
		if(meta != this.prevMeta) {
			this.prevMeta = meta;
			System.out.println ("Setting meta " + meta + " to " + this.xTile + " " + this.yTile + " " + this.zTile);
			this.worldObj.setBlockMetadataWithNotify(this.xTile, this.yTile, this.zTile, meta);
		}
	}
	
	@Override
	public boolean attackEntityFrom(Entity entity, int damage) {
		return false;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setInteger("MeatDuration", this.meatDuration);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.meatDuration = nBTTagCompound1.getInteger("MeatDuration");
		System.out.println ("md" + this.meatDuration);
	}
	
	@Override
	public boolean interact(EntityPlayer entityPlayer1) {
		System.out.println ("This meat has existed for " + this.meatDuration);
		return false;
	}
}
