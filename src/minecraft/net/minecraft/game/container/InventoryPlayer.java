	package net.minecraft.game.container;

import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.game.item.ItemArmor;

public class InventoryPlayer implements IInventory {
	public ItemStack[] mainInventory = new ItemStack[36];
	public ItemStack[] armorInventory = new ItemStack[4];
	public int currentItem = 0;
	public EntityPlayer player;
	public ItemStack itemStack;
	public boolean inventoryChanged = false;

	public InventoryPlayer(EntityPlayer entityPlayer1) {
		this.player = entityPlayer1;
	}

	public ItemStack getCurrentItem() {
		return this.currentItem < 9 && this.currentItem >= 0 ? this.mainInventory[this.currentItem] : null;
	}

	public static int mainInventoryWidth() {
		return 9;
	}

	private int getInventorySlotContainItem(int i1) {
		for(int i2 = 0; i2 < this.mainInventory.length; ++i2) {
			if(this.mainInventory[i2] != null && this.mainInventory[i2].itemID == i1) {
				return i2;
			}
		}

		return -1;
	}

	private int storeItemStack(ItemStack itemStack1) {
		for(int i2 = 0; i2 < this.mainInventory.length; ++i2) {
			if(this.mainInventory[i2] != null && this.mainInventory[i2].itemID == itemStack1.itemID && this.mainInventory[i2].isStackable() && this.mainInventory[i2].stackSize < this.mainInventory[i2].getMaxStackSize() && this.mainInventory[i2].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[i2].getHasSubtypes() || this.mainInventory[i2].getItemDamage() == itemStack1.getItemDamage())) {
				return i2;
			}
		}

		return -1;
	}

	private int getFirstEmptyStack() {
		for(int i1 = 0; i1 < this.mainInventory.length; ++i1) {
			if(this.mainInventory[i1] == null) {
				return i1;
			}
		}

		return -1;
	}

	public void setCurrentItem(int i1, boolean z2) {
		int i3 = this.getInventorySlotContainItem(i1);
		if(i3 >= 0 && i3 < 9) {
			this.currentItem = i3;
		}
	}

	public void changeCurrentItem(int i1) {
		if(i1 > 0) {
			i1 = 1;
		}

		if(i1 < 0) {
			i1 = -1;
		}

		for(this.currentItem -= i1; this.currentItem < 0; this.currentItem += 9) {
		}

		while(this.currentItem >= 9) {
			this.currentItem -= 9;
		}

	}

	private int storePartialItemStack(ItemStack itemStack1) {
		int i2 = itemStack1.itemID;
		int i3 = itemStack1.stackSize;
		int i4 = this.storeItemStack(itemStack1);
		if(i4 < 0) {
			i4 = this.getFirstEmptyStack();
		}

		if(i4 < 0) {
			return i3;
		} else {
			if(this.mainInventory[i4] == null) {
				this.mainInventory[i4] = new ItemStack(i2, 0, itemStack1.getItemDamage());
			}

			int i5 = i3;
			if(i3 > this.mainInventory[i4].getMaxStackSize() - this.mainInventory[i4].stackSize) {
				i5 = this.mainInventory[i4].getMaxStackSize() - this.mainInventory[i4].stackSize;
			}

			if(i5 > this.getInventoryStackLimit() - this.mainInventory[i4].stackSize) {
				i5 = this.getInventoryStackLimit() - this.mainInventory[i4].stackSize;
			}

			if(i5 == 0) {
				return i3;
			} else {
				i3 -= i5;
				this.mainInventory[i4].stackSize += i5;
				this.mainInventory[i4].animationsToGo = 5;
				return i3;
			}
		}
	}

	public void decrementAnimations() {
		for(int i1 = 0; i1 < this.mainInventory.length; ++i1) {
			if(this.mainInventory[i1] != null) {
				this.mainInventory[i1].updateAnimation(this.player.worldObj, this.player, i1, this.currentItem == i1);
			}
		}

	}

	public boolean consumeInventoryItem(int i1) {
		int i2 = this.getInventorySlotContainItem(i1);
		if(i2 < 0) {
			return false;
		} else {
			if(--this.mainInventory[i2].stackSize <= 0) {
				this.mainInventory[i2] = null;
			}

			return true;
		}
	}

	public boolean addItemStackToInventory(ItemStack itemStack1) {
		int i2;
		if(itemStack1.isItemDamaged()) {
			i2 = this.getFirstEmptyStack();
			if(i2 >= 0) {
				this.mainInventory[i2] = ItemStack.copyItemStack(itemStack1);
				this.mainInventory[i2].animationsToGo = 5;
				itemStack1.stackSize = 0;
				return true;
			} else {
				return false;
			}
		} else {
			do {
				i2 = itemStack1.stackSize;
				itemStack1.stackSize = this.storePartialItemStack(itemStack1);
			} while(itemStack1.stackSize > 0 && itemStack1.stackSize < i2);

			return itemStack1.stackSize < i2;
		}
	}

	public ItemStack decrStackSize(int i1, int i2) {
		ItemStack[] itemStack3 = this.mainInventory;
		if(i1 >= this.mainInventory.length) {
			itemStack3 = this.armorInventory;
			i1 -= this.mainInventory.length;
		}

		if(itemStack3[i1] != null) {
			ItemStack itemStack4;
			if(itemStack3[i1].stackSize <= i2) {
				itemStack4 = itemStack3[i1];
				itemStack3[i1] = null;
				return itemStack4;
			} else {
				itemStack4 = itemStack3[i1].splitStack(i2);
				if(itemStack3[i1].stackSize == 0) {
					itemStack3[i1] = null;
				}

				return itemStack4;
			}
		} else {
			return null;
		}
	}

	public void setInventorySlotContents(int i1, ItemStack itemStack2) {
		ItemStack[] itemStack3 = this.mainInventory;
		if(i1 >= itemStack3.length) {
			i1 -= itemStack3.length;
			itemStack3 = this.armorInventory;
		}

		itemStack3[i1] = itemStack2;
	}

	public float getStrVsBlock(Block block1, int metadata) {
		float f2 = 1.0F;
		if(this.mainInventory[this.currentItem] != null) {
			f2 *= this.mainInventory[this.currentItem].getStrVsBlock(block1, metadata);
		}

		return f2;
	}

	public NBTTagList writeToNBT(NBTTagList nBTTagList1) {
		int i2;
		NBTTagCompound nBTTagCompound3;
		for(i2 = 0; i2 < this.mainInventory.length; ++i2) {
			if(this.mainInventory[i2] != null) {
				nBTTagCompound3 = new NBTTagCompound();
				nBTTagCompound3.setByte("Slot", (byte)i2);
				this.mainInventory[i2].writeToNBT(nBTTagCompound3);
				nBTTagList1.setTag(nBTTagCompound3);
			}
		}

		for(i2 = 0; i2 < this.armorInventory.length; ++i2) {
			if(this.armorInventory[i2] != null) {
				nBTTagCompound3 = new NBTTagCompound();
				nBTTagCompound3.setByte("Slot", (byte)(i2 + 100));
				this.armorInventory[i2].writeToNBT(nBTTagCompound3);
				nBTTagList1.setTag(nBTTagCompound3);
			}
		}

		return nBTTagList1;
	}

	public void readFromNBT(NBTTagList nBTTagList1) {
		this.mainInventory = new ItemStack[36];
		this.armorInventory = new ItemStack[4];

		for(int i2 = 0; i2 < nBTTagList1.tagCount(); ++i2) {
			NBTTagCompound nBTTagCompound3 = (NBTTagCompound)nBTTagList1.tagAt(i2);
			int i4 = nBTTagCompound3.getByte("Slot") & 255;
			ItemStack itemStack5 = new ItemStack(nBTTagCompound3);
			if(itemStack5.getItem() != null) {
				if(i4 >= 0 && i4 < this.mainInventory.length) {
					this.mainInventory[i4] = itemStack5;
				}

				if(i4 >= 100 && i4 < this.armorInventory.length + 100) {
					this.armorInventory[i4 - 100] = itemStack5;
				}
			}
		}

	}

	public int getSizeInventory() {
		return this.mainInventory.length + 4;
	}

	public ItemStack getStackInSlot(int i1) {
		ItemStack[] itemStack2 = this.mainInventory;
		if(i1 >= itemStack2.length) {
			i1 -= itemStack2.length;
			itemStack2 = this.armorInventory;
		}

		return itemStack2[i1];
	}

	public String getInvName() {
		return "Inventory";
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public int getDamageVsEntity(Entity entity1) {
		ItemStack itemStack2 = this.getStackInSlot(this.currentItem);
		return itemStack2 != null ? itemStack2.getDamageVsEntity(entity1) : 1;
	}

	public float getExtraKnockbackVsEntity(Entity entity1) {
		ItemStack itemStack2 = this.getStackInSlot(this.currentItem);
		return itemStack2 != null ? itemStack2.getExtraKnockbackVsEntity(entity1) : 0.0F;
	}
	
	public int getSwingSpeed() {
		ItemStack itemStack2 = this.getStackInSlot(this.currentItem);
		return itemStack2 != null ? itemStack2.getSwingSpeed() : 6;
	}	

	public boolean canHarvestBlock(Block block, int metadata) {
		Material material = block.getBlockMaterialBasedOnmetaData(metadata);
		if(material.getIsHarvestable()) {
			return true;
		} else {
			ItemStack itemStack2 = this.getStackInSlot(this.currentItem);
			return itemStack2 != null ? itemStack2.canHarvestBlock(block) : false;
		}
	}

	public ItemStack armorItemInSlot(int i1) {
		return this.armorInventory[i1];
	}

	public int getTotalArmorValue() {
		int armorValue = 0;
		int totalProtection = 0;
		int totalMaxDamage = 0;

		for(int i = 0; i < this.armorInventory.length; ++i) {
			if(this.armorInventory[i] != null && this.armorInventory[i].getItem() instanceof ItemArmor) {
				int maxDamage = this.armorInventory[i].getMaxDamage();
				int curDamage = this.armorInventory[i].itemDamage;
				int protection = maxDamage - curDamage;
				totalProtection += protection;
				totalMaxDamage += maxDamage;

				int armorPieceValue = ((ItemArmor)this.armorInventory[i].getItem()).damageReduceAmount;
				armorValue += armorPieceValue;
			}
		}

		if(totalMaxDamage == 0) {
			return 0;
		} else {
			return (armorValue - 1) * totalProtection / totalMaxDamage + 1;
		}
	}
	
	public void damageArmor(int i1) {
		for(int i2 = 0; i2 < this.armorInventory.length; ++i2) {
			if(this.armorInventory[i2] != null && this.armorInventory[i2].getItem() instanceof ItemArmor) {
				this.armorInventory[i2].damageItem(i1, this.player);
				if(this.armorInventory[i2].stackSize == 0) {
					this.armorInventory[i2].onItemDestroyedByUse(this.player);
					this.armorInventory[i2] = null;
				}
			}
		}

	}

	public void dropAllItems() {
		int i1;
		for(i1 = 0; i1 < this.mainInventory.length; ++i1) {
			if(this.mainInventory[i1] != null) {
				this.player.dropPlayerItemWithRandomChoice(this.mainInventory[i1], true);
				this.mainInventory[i1] = null;
			}
		}

		for(i1 = 0; i1 < this.armorInventory.length; ++i1) {
			if(this.armorInventory[i1] != null) {
				this.player.dropPlayerItemWithRandomChoice(this.armorInventory[i1], true);
				this.armorInventory[i1] = null;
			}
		}

	}

	public void onInventoryChanged() {
		this.inventoryChanged = true;
	}

	public void setItemStack(ItemStack itemStack1) {
		this.itemStack = itemStack1;
		this.player.onItemStackChanged(itemStack1);
	}

	public ItemStack getItemStack() {
		return this.itemStack;
	}

	public boolean canInteractWith(EntityPlayer entityPlayer1) {
		return this.player.isDead ? false : entityPlayer1.getDistanceSqToEntity(this.player) <= 64.0D;
	}

	public boolean hasItemStack(ItemStack itemStack1) {
		int i2;
		for(i2 = 0; i2 < this.armorInventory.length; ++i2) {
			if(this.armorInventory[i2] != null && this.armorInventory[i2].isStackEqual(itemStack1)) {
				return true;
			}
		}

		for(i2 = 0; i2 < this.mainInventory.length; ++i2) {
			if(this.mainInventory[i2] != null && this.mainInventory[i2].isStackEqual(itemStack1)) {
				return true;
			}
		}

		return false;
	}
	
	public int clearInventory(int var1, int var2) {
		int var3 = 0;

		int var4;
		ItemStack var5;
		for(var4 = 0; var4 < this.mainInventory.length; ++var4) {
			var5 = this.mainInventory[var4];
			if(var5 != null && (var1 <= -1 || var5.itemID == var1) && (var2 <= -1 || var5.getItemDamage() == var2)) {
				var3 += var5.stackSize;
				this.mainInventory[var4] = null;
			}
		}

		for(var4 = 0; var4 < this.armorInventory.length; ++var4) {
			var5 = this.armorInventory[var4];
			if(var5 != null && (var1 <= -1 || var5.itemID == var1) && (var2 <= -1 || var5.getItemDamage() == var2)) {
				var3 += var5.stackSize;
				this.armorInventory[var4] = null;
			}
		}

		return var3;
	}
}
