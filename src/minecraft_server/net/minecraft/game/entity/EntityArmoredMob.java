package net.minecraft.game.entity;

import net.minecraft.game.container.IInventory;
import net.minecraft.game.container.InventoryMob;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A humanoid that carries a real inventory, so it can wear armour and hold an
 * item in separate slots (used by zombies, skeletons, amazons, traders, ...).
 */
public class EntityArmoredMob extends EntityHumanoid implements IArmoredMob {
	/** Nine-slot inventory holding what the mob wears and wields. */
	public InventoryMob inventory;

	/** Damage in excess of what the armour absorbed, carried to the next hit. */
	protected int carryoverDamage = 0;

	public EntityArmoredMob(World world) {
		super(world);
		this.inventory = new InventoryMob(this, this.getSecondaryInventorySize());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.inventory.readFromNBT(tag.getCompoundTag("Inventory"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setTag("Inventory", this.inventory.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public IInventory getIInventory() {
		return this.inventory;
	}

	public void setInventory(IInventory inventory) {
		this.inventory = (InventoryMob) inventory;
	}

	@Override
	protected int getTotalArmorValue() {
		return this.inventory.getTotalArmorValue();
	}

	public int getSecondaryInventorySize() {
		return 9;
	}

	/** Sets the armour piece in the given slot (0 = boots .. 3 = helmet). */
	public void setArmor(int type, ItemStack itemStack) {
		this.inventory.setArmorItemInSlot(3 - type, itemStack);
	}

	/** Returns the armour piece in the given slot (0 = boots .. 3 = helmet). */
	public ItemStack getArmor(int type) {
		return this.inventory.getArmorItemInSlot(3 - type);
	}

	@Override
	public ItemStack getHeldItem() {
		return this.inventory.getHeldItem();
	}

	@Override
	public boolean setHeldItem(ItemStack itemStack) {
		this.inventory.setHeldItem(itemStack);
		return true;
	}
}
