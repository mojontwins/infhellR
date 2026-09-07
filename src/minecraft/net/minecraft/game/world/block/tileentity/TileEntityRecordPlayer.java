package net.minecraft.game.world.block.tileentity;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityRecordPlayer extends TileEntity {
	public int record;

	public void readFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readFromNBT(nBTTagCompound1);
		this.record = nBTTagCompound1.getInteger("Record");
	}

	public void writeToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeToNBT(nBTTagCompound1);
		if(this.record > 0) {
			nBTTagCompound1.setInteger("Record", this.record);
		}

	}
}
