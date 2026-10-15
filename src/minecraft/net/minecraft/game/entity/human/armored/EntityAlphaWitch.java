package net.minecraft.game.entity.human.armored;

import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.IHostileToPlayer;
import net.minecraft.game.entity.projectile.EntityThrowablePotion;
import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemPotion;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.world.World;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Potion-throwing witch. She keeps a stock of potions in her secondary
 * inventory, throws one when a target is in range, drinks a healing potion when
 * badly hurt, and slowly refills once she runs dry.
 */
public class EntityAlphaWitch extends EntityArmoredMob implements IHostileToPlayer {
	/** Pool of potions she can be restocked with. */
	private Item[] possiblePotions = new Item[] {
		Item.potionPoison, Item.potionPoison, Item.potionPoison, Item.potionPoison,
		Item.potionSlowness, Item.potionInstantDamage, Item.potionDizzy, Item.potionDizzy
	};

	/** Maps the 4-bit "next potion" field to an actual potion. */
	private Item[] potionsMap = new Item[] {
		null,
		Item.potionPoison,
		Item.potionSlowness,
		Item.potionInstantDamage,
		Item.potionAutoHealing,
		Item.potionDizzy
	};

	public EntityAlphaWitch(World world) {
		super(world);
		this.texture = "/mob/witch.png";
		this.health = this.getFullHealth();
		this.attackStrength = 4;
		this.moveSpeed = 0.8F;
		this.inventory.setInventorySlotContents(0, null);
		this.attackTime = 80;
	}

	@Override
	protected void entityInit() {
		super.entityInit();

		// DW_STATUS packs the state in one byte:
		//   bits 0-3 : the potion she is about to throw (index into potionsMap)
		//   bit  4   : she has already healed herself this cycle
		//   bit  5   : she has run out of potions
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, (byte)0);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		// Keep the held slot in sync with the potion she is preparing.
		Item nextPotion = this.getNextPotion();
		this.inventory.setInventorySlotContents(0, nextPotion == null ? null : new ItemStack(nextPotion));
	}

	public Item getNextPotion() {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		int potionIndex = status & 15;
		return potionIndex < 15 ? this.potionsMap[potionIndex] : null;
	}

	public void setNextPotion(Item potion) {
		byte status = (byte)(this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 0xf0);
		for(int i = 0; i < this.potionsMap.length; ++i) {
			if(this.potionsMap[i] == potion) {
				status |= (i & 15);
				break;
			}
		}
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, status);
	}

	public boolean isHealed() {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		return (status & 0x10) != 0;
	}

	public void setHealed(boolean healed) {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)((status & 0xEF) | (healed ? 0x10 : 0x00)));
	}

	public boolean isExhausted() {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		return (status & 0x20) != 0;
	}

	public void setExhausted(boolean exhausted) {
		byte status = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)((status & 0xDF) | (exhausted ? 0x20 : 0x00)));
	}

	public void fillInventory() {
		for(int slot = 0; slot < this.inventory.getSizesecondaryinventory(); ++slot) {
			this.inventory.setSecondaryInventorySlotContents(slot, new ItemStack(this.possiblePotions[this.rand.nextInt(this.possiblePotions.length)]));
		}
	}

	@Override
	protected Entity findPlayerToAttack() {
		if(this.isExhausted()) {
			return null;
		}

		for(int slot = 0; slot < this.inventory.getSizesecondaryinventory(); ++slot) {
			if(this.inventory.getSecondaryInventorySlotContents(slot) != null) {
				return super.findPlayerToAttack();
			}
		}

		this.setExhausted(true);
		return null;
	}

	@Override
	public void onLivingUpdate() {
		// Slowly restock once all potions are gone.
		if(!this.worldObj.isRemote && this.isExhausted() && this.rand.nextInt(10000) == 0) {
			this.fillInventory();
			this.setExhausted(false);
			this.setHealed(false);
		}

		super.onLivingUpdate();
	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();

		// Back away from a target standing right on top of us.
		if(this.entityToAttack != null && this.entityToAttack.getDistanceSqToEntity(this) < 16.0D) {
			this.moveForward = -this.moveSpeed;
		}
	}

	@Override
	protected void attackEntity(Entity target, float distance) {
		if(distance < 10.0F) {
			// Drink a healing potion when low and not already healed this cycle.
			if(this.health < 10 && !this.isHealed()) {
				this.setNextPotion(Item.potionAutoHealing);
				this.attackTime = 29;
				this.setHealed(true);
			}

			double deltaX = target.posX - this.posX;
			double deltaZ = target.posZ - this.posZ;

			// Queue up the next potion from the inventory.
			if(this.attackTime == 30 && this.getNextPotion() == null) {
				for(int slot = 0; slot < this.inventory.getSizesecondaryinventory(); ++slot) {
					ItemStack potionStack = this.inventory.getSecondaryInventorySlotContents(slot);
					if(potionStack != null) {
						this.setNextPotion(potionStack.getItem());
						this.inventory.setSecondaryInventorySlotContents(slot, null);
						break;
					}
				}
			}

			if(this.attackTime < 15 && this.getNextPotion() == Item.potionAutoHealing) {
				this.worldObj.playSoundAtEntity(this, "mob.witch.drink", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
			}

			if(this.attackTime == 0) {
				if(this.getNextPotion() == Item.potionAutoHealing) {
					this.addStatusEffect(new StatusEffect(((ItemPotion)this.getNextPotion()).getPotionType(), 80, 2));
				} else if(this.getNextPotion() != null) {
					EntityThrowablePotion thrownPotion = new EntityThrowablePotion(this.worldObj, this, (ItemPotion)this.getNextPotion());
					++thrownPotion.posY;

					double deltaY = target.posY + (double)target.getEyeHeight() - (double)0.2F - thrownPotion.posY;
					float arc = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ) * 0.2F;
					this.worldObj.playSoundAtEntity(this, "mob.witch.throw", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
					this.worldObj.spawnEntityInWorld(thrownPotion);
					thrownPotion.setThrowableHeading(deltaX, deltaY + (double)arc, deltaZ, 0.6F, 12.0F);
				}

				this.setNextPotion(null);
				this.attackTime = 80;
			}

			this.rotationYaw = (float)(Math.atan2(deltaZ, deltaX) * 180.0D / (double)(float)Math.PI) - 90.0F;
			this.hasAttacked = true;
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setBoolean("Exhausted", this.isExhausted());
		tag.setBoolean("Healed", this.isHealed());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.setExhausted(tag.getBoolean("Exhausted"));
		this.setHealed(tag.getBoolean("Healed"));
	}

	@Override
	protected int getDropItemId() {
		return Item.arrow.shiftedIndex;
	}

	@Override
	protected void dropFewItems() {
		int count = this.rand.nextInt(3);
		for(int i = 0; i < count; ++i) {
			this.dropItem(Item.arrow.shiftedIndex, 1);
		}

		count = this.rand.nextInt(3);
		for(int i = 0; i < count; ++i) {
			this.dropItem(Item.bone.shiftedIndex, 1);
		}
	}

	@Override
	protected String getLivingSound() {
		return "mob.witch.idle";
	}

	@Override
	protected String getHurtSound() {
		return "mob.witch.hurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.witch.death";
	}

	@Override
	public int getFullHealth() {
		return 50;
	}

	@Override
	public int getSecondaryInventorySize() {
		return 20;
	}
}
