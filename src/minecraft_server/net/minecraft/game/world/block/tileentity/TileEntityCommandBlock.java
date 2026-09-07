package net.minecraft.game.world.block.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet91UpdateCommandBlock;

public class TileEntityCommandBlock extends TileEntity {
	// This tile entity only serves as a means to contain a command
	public String command = "";
	public boolean blocked;
	public boolean looper; 		// Set and block will re-schedule itself on success
	public String commandResults = "";
	
	public void writeToNBT(NBTTagCompound nBTTagCompound) {
		super.writeToNBT(nBTTagCompound);
		nBTTagCompound.setString("Command", this.command);
		nBTTagCompound.setBoolean("Looper", this.looper);
	}
	
	public void readFromNBT(NBTTagCompound nBTTagCompound) {
		super.readFromNBT(nBTTagCompound);
		this.command = nBTTagCompound.getString("Command");
		this.looper = nBTTagCompound.getBoolean("Looper");
	}
	
	public Packet getDescriptionPacket() {
		return new Packet91UpdateCommandBlock(this.xCoord, this.yCoord, this.zCoord, this.command);
	}
}
