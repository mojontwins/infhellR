package net.minecraft.game.container;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.ItemStack;

public abstract class Container {
	public List<ItemStack> inventoryItemStacks = new ArrayList<ItemStack>();
	public List<Slot> inventorySlots = new ArrayList<Slot>();
	public int windowId = 0;
	private short transactionID = 0;
	protected List<ICrafting> crafters = new ArrayList<ICrafting>();
	private Set<EntityPlayer> crafterPlayers = new HashSet<EntityPlayer>();
	
	protected void addSlot(Slot slot) {
		this.addSlotToContainer(slot);
	}
	
	protected Slot addSlotToContainer(Slot slot) {
		slot.id = this.inventorySlots.size();
		this.inventorySlots.add(slot);
		this.inventoryItemStacks.add((ItemStack)null);
		return slot;
	}

	public void onCraftGuiOpened(ICrafting iCrafting1) {
		if(this.crafters.contains(iCrafting1)) {
			throw new IllegalArgumentException("Listener already listening");
		} else {
			this.crafters.add(iCrafting1);
			iCrafting1.updateCraftingInventory(this, this.getInventoryStacks());
			this.updateCraftingResults();
		}
	}

	public List<ItemStack> getInventoryStacks() {
		ArrayList<ItemStack> arrayList1 = new ArrayList<ItemStack>();

		for(int i2 = 0; i2 < this.inventorySlots.size(); ++i2) {
			arrayList1.add(((Slot)this.inventorySlots.get(i2)).getStack());
		}

		return arrayList1;
	}

	public void updateCraftingResults() {
		for(int i1 = 0; i1 < this.inventorySlots.size(); ++i1) {
			ItemStack itemStack2 = ((Slot)this.inventorySlots.get(i1)).getStack();
			ItemStack itemStack3 = (ItemStack)this.inventoryItemStacks.get(i1);
			if(!ItemStack.areItemStacksEqual(itemStack3, itemStack2)) {
				itemStack3 = itemStack2 == null ? null : itemStack2.copy();
				this.inventoryItemStacks.set(i1, itemStack3);

				for(int i4 = 0; i4 < this.crafters.size(); ++i4) {
					((ICrafting)this.crafters.get(i4)).updateCraftingInventorySlot(this, i1, itemStack3);
				}
			}
		}

	}

	public Slot findCurrentItem(IInventory iInventory1, int i2) {
		for(int i3 = 0; i3 < this.inventorySlots.size(); ++i3) {
			Slot slot4 = (Slot)this.inventorySlots.get(i3);
			if(slot4.isSlotInInventory(iInventory1, i2)) {
				return slot4;
			}
		}

		return null;
	}

	public Slot getSlot(int i1) {
		return (Slot)this.inventorySlots.get(i1);
	}

	public ItemStack getStackInSlot(int i1) {
		Slot slot2 = (Slot)this.inventorySlots.get(i1);
		return slot2 != null ? slot2.getStack() : null;
	}

	public ItemStack slotClick(int slotId, int mouseB, boolean z3, EntityPlayer entityPlayer4) {
		ItemStack stackInSlotOrig = null;
		if(mouseB == 0 || mouseB == 1) {
			InventoryPlayer inventoryPlayer = entityPlayer4.inventory;
			if(slotId == -999) {
				if(inventoryPlayer.getItemStack() != null && slotId == -999) {
					if(mouseB == 0) {
						entityPlayer4.dropPlayerItem(inventoryPlayer.getItemStack());
						inventoryPlayer.setItemStack((ItemStack)null);
					}

					if(mouseB == 1) {
						entityPlayer4.dropPlayerItem(inventoryPlayer.getItemStack().splitStack(1));
						if(inventoryPlayer.getItemStack().stackSize == 0) {
							inventoryPlayer.setItemStack((ItemStack)null);
						}
					}
				}
			} else {
				int amount;
				if(z3) {
					ItemStack stackInSlot = this.getStackInSlot(slotId);
					if(stackInSlot != null) {
						int i8 = stackInSlot.stackSize;
						stackInSlotOrig = stackInSlot.copy();
						Slot slot9 = (Slot)this.inventorySlots.get(slotId);
						if(slot9 != null && slot9.getStack() != null) {
							amount = slot9.getStack().stackSize;
							if(amount < i8) {
								this.slotClick(slotId, mouseB, z3, entityPlayer4);
							}
						}
					}
				} else {
					Slot curSlot = (Slot)this.inventorySlots.get(slotId);
					if(curSlot != null) {
						curSlot.onSlotChanged();
						ItemStack stackInSlot = curSlot.getStack();
						ItemStack stackPlayer = inventoryPlayer.getItemStack();
						if(stackInSlot != null) {
							stackInSlotOrig = stackInSlot.copy();
						}

						if(stackInSlot == null) {
							if(stackPlayer != null && curSlot.isItemValid(stackPlayer)) {
								amount = mouseB == 0 ? stackPlayer.stackSize : 1;
								if(amount > curSlot.getSlotStackLimit()) {
									amount = curSlot.getSlotStackLimit();
								}

								curSlot.putStack(stackPlayer.splitStack(amount));
								if(stackPlayer.stackSize == 0) {
									inventoryPlayer.setItemStack((ItemStack)null);
								}
							}
						} else if(stackPlayer == null) {
							amount = mouseB == 0 ? stackInSlot.stackSize : (stackInSlot.stackSize + 1) / 2;
							ItemStack itemStack11 = curSlot.decrStackSize(amount);
							inventoryPlayer.setItemStack(itemStack11);
							if(stackInSlot.stackSize == 0) {
								curSlot.putStack((ItemStack)null);
							}

							curSlot.onPickupFromSlot(inventoryPlayer.getItemStack());
						} else if(curSlot.isItemValid(stackPlayer)) {
							// Stack player not null,
							// Stack in slot not null.

							if(stackInSlot.itemID == stackPlayer.itemID && (!stackInSlot.getHasSubtypes() || stackInSlot.getItemDamage() == stackPlayer.getItemDamage())) {
								// If same type (id + meta/damage)

								amount = mouseB == 0 ? stackPlayer.stackSize : 1;
								if(amount > curSlot.getSlotStackLimit() - stackInSlot.stackSize) {
									amount = curSlot.getSlotStackLimit() - stackInSlot.stackSize;
								}

								if(amount > stackPlayer.getMaxStackSize() - stackInSlot.stackSize) {
									amount = stackPlayer.getMaxStackSize() - stackInSlot.stackSize;
								}

								stackPlayer.splitStack(amount);
								if(stackPlayer.stackSize == 0) {
									inventoryPlayer.setItemStack((ItemStack)null);
								}

								stackInSlot.stackSize += amount;
								
								// New here: this should trigger an onChanged or sumthin
								curSlot.onSlotChanged();
								
							} else if(stackPlayer.stackSize <= curSlot.getSlotStackLimit()) {
								curSlot.putStack(stackPlayer);
								inventoryPlayer.setItemStack(stackInSlot);
							}
						} else if(stackInSlot.itemID == stackPlayer.itemID && stackPlayer.getMaxStackSize() > 1 && (!stackInSlot.getHasSubtypes() || stackInSlot.getItemDamage() == stackPlayer.getItemDamage())) {
							amount = stackInSlot.stackSize;
							if(amount > 0 && amount + stackPlayer.stackSize <= stackPlayer.getMaxStackSize()) {
								stackPlayer.stackSize += amount;
								stackInSlot.splitStack(amount);
								if(stackInSlot.stackSize == 0) {
									curSlot.putStack((ItemStack)null);
								}

								curSlot.onPickupFromSlot(inventoryPlayer.getItemStack());
							}
						}
					}
				}
			}
		}

		return stackInSlotOrig;
	}
	
	public boolean func_94530_a(ItemStack var1, Slot var2) {
		return true;
	}
	
	public void onCraftGuiClosed(EntityPlayer entityPlayer1) {
		InventoryPlayer inventoryPlayer2 = entityPlayer1.inventory;
		if(inventoryPlayer2.getItemStack() != null) {
			entityPlayer1.dropPlayerItem(inventoryPlayer2.getItemStack());
			inventoryPlayer2.setItemStack((ItemStack)null);
		}

	}

	public void onCraftMatrixChanged(IInventory iInventory1) {
		this.updateCraftingResults();
	}

	public boolean getCanCraft(EntityPlayer entityPlayer1) {
		return !this.crafterPlayers.contains(entityPlayer1);
	}

	public void setCanCraft(EntityPlayer entityPlayer1, boolean z2) {
		if(z2) {
			this.crafterPlayers.remove(entityPlayer1);
		} else {
			this.crafterPlayers.add(entityPlayer1);
		}

	}
	
	public void putStackInSlot(int i1, ItemStack itemStack2) {
		this.getSlot(i1).putStack(itemStack2);
	}

	public void putStacksInSlots(ItemStack[] itemStack1) {
		for(int i2 = 0; i2 < itemStack1.length; ++i2) {
			this.getSlot(i2).putStack(itemStack1[i2]);
		}

	}

	public void setFurnaceTime(int i1, int i2) {
	}

	public short func_20111_a(InventoryPlayer inventoryPlayer1) {
		++this.transactionID;
		return this.transactionID;
	}

	public void func_20113_a(short s1) {
	}

	public void func_20110_b(short s1) {
	}

	public abstract boolean canInteractWith(EntityPlayer entityPlayer1);

	protected boolean mergeItemStack(ItemStack itemStack1, int i2, int i3, boolean z4) {
		boolean res = false;
		
		int i5 = i2;
		if(z4) {
			i5 = i3 - 1;
		}

		Slot slot6;
		ItemStack itemStack7;
		if(itemStack1.isStackable()) {
			while(itemStack1.stackSize > 0 && (!z4 && i5 < i3 || z4 && i5 >= i2)) {
				slot6 = (Slot)this.inventorySlots.get(i5);
				itemStack7 = slot6.getStack();
				if(itemStack7 != null && itemStack7.itemID == itemStack1.itemID && (!itemStack1.getHasSubtypes() || itemStack1.getItemDamage() == itemStack7.getItemDamage())) {
					int i8 = itemStack7.stackSize + itemStack1.stackSize;
					if(i8 <= itemStack1.getMaxStackSize()) {
						itemStack1.stackSize = 0;
						itemStack7.stackSize = i8;
						slot6.onSlotChanged();
						res = true;
					} else if(itemStack7.stackSize < itemStack1.getMaxStackSize()) {
						itemStack1.stackSize -= itemStack1.getMaxStackSize() - itemStack7.stackSize;
						itemStack7.stackSize = itemStack1.getMaxStackSize();
						slot6.onSlotChanged();
						res = true;
					}
				}

				if(z4) {
					--i5;
				} else {
					++i5;
				}
			}
		}

		if(itemStack1.stackSize > 0) {
			if(z4) {
				i5 = i3 - 1;
			} else {
				i5 = i2;
			}

			while(!z4 && i5 < i3 || z4 && i5 >= i2) {
				slot6 = (Slot)this.inventorySlots.get(i5);
				itemStack7 = slot6.getStack();
				if(itemStack7 == null) {
					slot6.putStack(itemStack1.copy());
					slot6.onSlotChanged();
					itemStack1.stackSize = 0;
					res = true;
					break;
				}

				if(z4) {
					--i5;
				} else {
					++i5;
				}
			}
		}

		return res;
	}
	
	public boolean func_94531_b(Slot var1) {
		return true;
	}
}
