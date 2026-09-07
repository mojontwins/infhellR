package net.minecraft.game.trading;

import net.minecraft.game.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TradingRecipe {
	private ItemStack itemStack1;
	private ItemStack itemStack2;
	private ItemStack itemStackResult;
	private int stock;
	
	private boolean isCurrencyChange;
	private float changeFactor;
		
	public TradingRecipe(ItemStack itemStack1, ItemStack itemStack2, ItemStack itemStackResult, int stock) {
		this.itemStack1 = itemStack1;
		this.itemStack2 = itemStack2;
		this.itemStackResult = itemStackResult;
		this.stock = stock;
		this.isCurrencyChange = false;
	}
	
	public TradingRecipe(float changeFactor) {
		this.changeFactor = changeFactor;
		this.isCurrencyChange = true;
	}
	
	public TradingRecipe(ItemStack itemStack, ItemStack itemStackResult, int stock) {
		this(itemStack, null, itemStackResult, stock);
	}
	
	public TradingRecipe(ItemStack itemStack, ItemStack itemStackResult) {
		this(itemStack, null, itemStackResult, 1);
	}
	
	public TradingRecipe(NBTTagCompound nBTTagCompound) {
		this.readFromNBT(nBTTagCompound);
	}
	
	public boolean isTradingRecipeEquals(TradingRecipe tradingRecipe) {
		if(this.isCurrencyChange) {
			if(!tradingRecipe.isCurrencyChange()) return false;
			return this.changeFactor == tradingRecipe.changeFactor;
		} else {
			if(tradingRecipe.isCurrencyChange()) return false;
			if(!ItemStack.areItemStacksEqual(this.itemStack1, tradingRecipe.getItemStack1())) return false;
			if(!ItemStack.areItemStacksEqual(this.itemStack2, tradingRecipe.getItemStack2())) return false;
			if(!ItemStack.areItemStacksEqual(this.itemStackResult, tradingRecipe.getItemStackResult())) return false;
			return true;
		}
	}
	
	public String toString() {
		if(this.isCurrencyChange) {
			return "Trading currency with change Factor " + this.changeFactor;
		} else {
			return "Trading " + this.itemStack1 + " + " + this.itemStack2 + " -> " + this.itemStackResult + " Stock " + this.stock;
		}
	}
	
	public void readFromNBT(NBTTagCompound nBTTagCompound) {
		this.isCurrencyChange = nBTTagCompound.getBoolean("IsCurrencyChange");
		if(this.isCurrencyChange) {
			this.changeFactor = nBTTagCompound.getFloat("ChangeFactor");
		} else {
			this.itemStack1 = new ItemStack(nBTTagCompound.getCompoundTag("Wants1"));
			this.itemStack2 = nBTTagCompound.hasKey("Wants2") ? 
					new ItemStack(nBTTagCompound.getCompoundTag("Wants2"))
				:
					null;
			this.itemStackResult = new ItemStack(nBTTagCompound.getCompoundTag("Offers"));
			this.stock = nBTTagCompound.getShort("Stock");
		}
	}

	public void writeToNBT(NBTTagCompound nBTTagCompound) {
		nBTTagCompound.setBoolean("IsCurrencyChange", this.isCurrencyChange);
		if(this.isCurrencyChange) {
			nBTTagCompound.setFloat("ChangeFactor", this.changeFactor);
		} else {
			nBTTagCompound.setCompoundTag("Wants1", this.itemStack1.writeToNBT(new NBTTagCompound()));
			if(this.itemStack2 != null) {
				nBTTagCompound.setCompoundTag("Wants2", this.itemStack1.writeToNBT(new NBTTagCompound()));
			}
			nBTTagCompound.setCompoundTag("Offers", this.itemStackResult.writeToNBT(new NBTTagCompound()));
			nBTTagCompound.setShort("Stock", (short)this.stock);
		}
	}
	
	public boolean hasSecondItem() {
		return itemStack2 != null;
	}
	
	public ItemStack getItemStack1() {
		return itemStack1;
	}

	public void setItemStack1(ItemStack itemStack1) {
		this.itemStack1 = itemStack1;
	}

	public ItemStack getItemStack2() {
		return itemStack2;
	}

	public void setItemStack2(ItemStack itemStack2) {
		this.itemStack2 = itemStack2;
	}

	public ItemStack getItemStackResult() {
		return itemStackResult;
	}

	public void setItemStackResult(ItemStack itemStackResult) {
		this.itemStackResult = itemStackResult;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public boolean isCurrencyChange() {
		return isCurrencyChange;
	}

	public void setCurrencyChange(boolean isCurrencyChange) {
		this.isCurrencyChange = isCurrencyChange;
	}

	public float getChangeFactor() {
		return changeFactor;
	}

	public void setChangeFactor(float changeFactor) {
		this.changeFactor = changeFactor;
	}
	
	
}
