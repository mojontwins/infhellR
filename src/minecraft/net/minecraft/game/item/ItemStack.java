package net.minecraft.game.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.game.achievements.StatList;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EnumAction;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public final class ItemStack {
	public int stackSize;
	public int animationsToGo;
	public int itemID;
	public int itemDamage;

	public ItemStack(Block block1) {
		this((Block)block1, 1);
	}

	public ItemStack(Block block1, int i2) {
		this(block1.blockID, i2, 0);
	}

	public ItemStack(Block block1, int i2, int i3) {
		this(block1.blockID, i2, i3);
	}

	public ItemStack(Item item1) {
		this(item1.shiftedIndex, 1, 0);
	}

	public ItemStack(Item item1, int i2) {
		this(item1.shiftedIndex, i2, 0);
	}

	public ItemStack(Item item1, int i2, int i3) {
		this(item1.shiftedIndex, i2, i3);
	}

	public ItemStack(int itemID) {
		this(itemID, 1);
	}

	public ItemStack(int itemID, int stackSize) {
		this.itemID = itemID;
		this.stackSize = stackSize;
	}

	public ItemStack(int itemID, int stackSize, int itemDamage) {
		this.itemID = itemID;
		this.stackSize = stackSize;
		this.itemDamage = itemDamage;
	}

	public ItemStack(NBTTagCompound nBTTagCompound1) {
		this.stackSize = 0;
		this.readFromNBT(nBTTagCompound1);
	}

	public ItemStack splitStack(int i1) {
		this.stackSize -= i1;
		return new ItemStack(this.itemID, i1, this.itemDamage);
	}

	public Item getItem() {
		return Item.itemsList[this.itemID];
	}

	public int getIconIndex() {
		return this.getItem().getIconIndex(this);
	}

	public boolean useItem(EntityPlayer entityPlayer, World world, int x, int y, int z, int face, float xWithinFace, float yWithinFace, float zWithinFace) {
		boolean z7 = this.getItem().onItemUse(this, entityPlayer, world, x, y, z, face, xWithinFace, yWithinFace, zWithinFace);
		if(z7) {
			entityPlayer.addStat(StatList.objectUseStats[this.itemID], 1);
		}

		return z7;
	}

	public float getStrVsBlock(Block block1) {
		return this.getItem().getStrVsBlock(this, block1);
	}

	public ItemStack useItemRightClick(World world1, EntityPlayer entityPlayer2) {
		return this.getItem().onItemRightClick(this, world1, entityPlayer2);
	}

	public ItemStack onFoodEaten(World world1, EntityPlayer entityPlayer2) {
		return this.getItem().onFoodEaten(this, world1, entityPlayer2);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nBTTagCompound1) {
		nBTTagCompound1.setShort("id", (short)this.itemID);
		nBTTagCompound1.setByte("Count", (byte)this.stackSize);
		nBTTagCompound1.setShort("Damage", (short)this.itemDamage);
		return nBTTagCompound1;
	}

	public void readFromNBT(NBTTagCompound nBTTagCompound1) {
		this.itemID = nBTTagCompound1.getShort("id");
		this.stackSize = nBTTagCompound1.getByte("Count");
		this.itemDamage = nBTTagCompound1.getShort("Damage");
	}

	public int getMaxStackSize() {
		return this.getItem().getItemStackLimit();
	}

	public boolean isStackable() {
		return this.getMaxStackSize() > 1 && (!this.isItemStackDamageable() || !this.isItemDamaged());
	}

	public boolean isItemStackDamageable() {
		return Item.itemsList[this.itemID].getMaxDamage() > 0;
	}

	public boolean getHasSubtypes() {
		return Item.itemsList[this.itemID].getHasSubtypes();
	}

	public boolean isItemDamaged() {
		return this.isItemStackDamageable() && this.itemDamage > 0;
	}

	public int getItemDamageForDisplay() {
		return this.itemDamage;
	}

	public int getItemDamage() {
		return this.itemDamage;
	}

	public void setItemDamage(int i1) {
		this.itemDamage = i1;
	}

	public int getMaxDamage() {
		return Item.itemsList[this.itemID].getMaxDamage();
	}

	public void damageItem(int i1, Entity entity2) {
		if(entity2 instanceof EntityPlayer && ((EntityPlayer)entity2).isCreative) return;
		if(this.isItemStackDamageable()) {
			this.itemDamage += i1;
			if(this.itemDamage > this.getMaxDamage()) {
				if(entity2 instanceof EntityPlayer) {
					((EntityPlayer)entity2).addStat(StatList.objectBreakStats[this.itemID], 1);
				}

				--this.stackSize;
				if(this.stackSize < 0) {
					this.stackSize = 0;
				}

				this.itemDamage = 0;
			}

		}
	}

	public void hitEntity(EntityLiving entityLiving1, EntityPlayer entityPlayer2) {
		boolean z3 = Item.itemsList[this.itemID].hitEntity(this, entityLiving1, entityPlayer2);
		if(z3) {
			entityPlayer2.addStat(StatList.objectUseStats[this.itemID], 1);
		}

	}

	public void onDestroyBlock(int i1, int i2, int i3, int i4, EntityPlayer entityPlayer5) {
		boolean z6 = Item.itemsList[this.itemID].onBlockDestroyed(this, i1, i2, i3, i4, entityPlayer5);
		if(z6) {
			entityPlayer5.addStat(StatList.objectUseStats[this.itemID], 1);
		}

	}

	public int getDamageVsEntity(Entity entity1) {
		return Item.itemsList[this.itemID].getDamageVsEntity(entity1);
	}

	public float getExtraKnockbackVsEntity(Entity entity1) {
		return Item.itemsList[this.itemID].getExtraKnockbackVsEntity(entity1);
	}
	
	public int getSwingSpeed() {
		return Item.itemsList[this.itemID].getSwingSpeed();
	}

	public boolean canHarvestBlock(Block block1) {
		return Item.itemsList[this.itemID].canHarvestBlock(block1);
	}

	public void onItemDestroyedByUse(EntityPlayer entityPlayer1) {
	}

	public void useItemOnEntity(EntityLiving entityLiving1) {
		Item.itemsList[this.itemID].saddleEntity(this, entityLiving1);
	}

	public ItemStack copy() {
		return new ItemStack(this.itemID, this.stackSize, this.itemDamage);
	}

	public static boolean areItemStacksEqual(ItemStack itemStack0, ItemStack itemStack1) {
		return itemStack0 == null && itemStack1 == null ? true : (itemStack0 != null && itemStack1 != null ? itemStack0.isItemStackEqual(itemStack1) : false);
	}

	private boolean isItemStackEqual(ItemStack itemStack1) {
		return this.stackSize != itemStack1.stackSize ? false : (this.itemID != itemStack1.itemID ? false : this.itemDamage == itemStack1.itemDamage);
	}

	public boolean isItemEqual(ItemStack itemStack1) {
		return this.itemID == itemStack1.itemID && this.itemDamage == itemStack1.itemDamage;
	}

	public String getItemName() {
		return Item.itemsList[this.itemID].getItemNameIS(this);
	}

	public static ItemStack copyItemStack(ItemStack itemStack0) {
		return itemStack0 == null ? null : itemStack0.copy();
	}

	public String toString() {
		return this.stackSize + "x" + Item.itemsList[this.itemID].getItemName() + "@" + this.itemDamage;
	}

	public void updateAnimation(World world1, Entity entity2, int i3, boolean z4) {
		if(this.animationsToGo > 0) {
			--this.animationsToGo;
		}

		Item.itemsList[this.itemID].onUpdate(this, world1, entity2, i3, z4);
	}

	public void onCrafting(World world1, EntityPlayer entityPlayer2) {
		entityPlayer2.addStat(StatList.objectCraftStats[this.itemID], this.stackSize);
		Item.itemsList[this.itemID].onCreated(this, world1, entityPlayer2);
	}

	public boolean isStackEqual(ItemStack itemStack1) {
		return this.itemID == itemStack1.itemID && this.stackSize == itemStack1.stackSize && this.itemDamage == itemStack1.itemDamage;
	}
	
	public int getMaxItemUseDuration() {
		return this.getItem().getMaxItemUseDuration(this);
	}

	public EnumAction getItemUseAction() {
		return this.getItem().getItemUseAction(this);
	}

	public void onPlayerStoppedUsing(World world1, EntityPlayer entityPlayer2, int i3) {
		this.getItem().onPlayerStoppedUsing(this, world1, entityPlayer2, i3);
	}

	public static boolean areItemStacksCompatibleForTrading(ItemStack fromRecipe, ItemStack itemStack) {
		if(fromRecipe == null && itemStack == null) return true;
		
		if(fromRecipe != null && itemStack != null) {
			if(fromRecipe.stackSize <= itemStack.stackSize && 
					fromRecipe.itemID == itemStack.itemID &&
					fromRecipe.itemDamage == itemStack.itemDamage) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> getItemNameandInformation() {
		ArrayList<String> arrayList1 = new ArrayList<String>();
		Item item2 = Item.itemsList[this.itemID];
		arrayList1.add(item2.getItemNameIS(this));
		
		return arrayList1;
	}
}
