package net.minecraft.game.trading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.game.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;

public class TradingRecipeList extends ArrayList<TradingRecipe> {
	private static final long serialVersionUID = -9135202036026348639L;

	public void readFromNBT(NBTTagList nBTTagList) {
		this.clear();
		for(int i = 0; i < nBTTagList.tagCount(); i ++) {
			NBTTagCompound nBTRecipe = (NBTTagCompound)nBTTagList.tagAt(i);
			TradingRecipe tradingRecipe = new TradingRecipe(nBTRecipe);
			this.add(tradingRecipe);
		}
	}
	
	public NBTTagList writeToNBT(NBTTagList nBTTagList) {
		for(int i = 0; i < this.size(); i ++) {
			NBTTagCompound nBTRecipe = new NBTTagCompound();
			this.get(i).writeToNBT(nBTRecipe);
			nBTTagList.setTag(nBTRecipe);
		}
		
		return nBTTagList;
	}
	
	// These methods serialize / unserialize this trading recipe list so it can be sent server->client
	public void writeRecipiesToStream(DataOutputStream stream) throws IOException {
		// First byte: list size
		stream.writeByte(this.size() & 0xff);
		
		// Serialized recipe list
		for(int i = 0; i < this.size(); i ++) {
			TradingRecipe tradingRecipe = this.get(i);
			Packet.writeItemStack(tradingRecipe.getItemStack1(), stream);
			Packet.writeItemStack(tradingRecipe.getItemStackResult(), stream);
			if(tradingRecipe.hasSecondItem()) {
				stream.writeBoolean(true);
				Packet.writeItemStack(tradingRecipe.getItemStack2(), stream);
			} else {
				stream.writeBoolean(false);
			}
			stream.writeShort(tradingRecipe.getStock());
		}
	}
	
	public static TradingRecipeList readRecipesFromStream(DataInputStream stream) throws IOException {
		TradingRecipeList tradingRecipeList = new TradingRecipeList();
		
		// First byte: list size
		int size = (int)stream.readByte() & 0xff;
		
		// Serialized recipe list
		for(int i = 0; i < size; i ++) {
			ItemStack itemStack1 = Packet.readItemStack(stream);
			ItemStack itemStackResult = Packet.readItemStack(stream);
			ItemStack itemStack2 = null;
			
			if(stream.readBoolean()) {
				itemStack2 = Packet.readItemStack(stream);
			}
			
			int stock = stream.readShort();
			
			tradingRecipeList.add(new TradingRecipe(itemStack1, itemStack2, itemStackResult, stock));
		}
		
		return tradingRecipeList;
	}
}
