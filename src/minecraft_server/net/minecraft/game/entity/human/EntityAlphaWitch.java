package net.minecraft.game.entity.human;

import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.projectile.EntityThrowablePotion;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemPotion;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntityAlphaWitch extends EntityArmoredMob {
	private Item possiblePotions[] = new Item[] {
		Item.potionPoison, Item.potionPoison, Item.potionPoison, Item.potionPoison, 
		Item.potionSlowness, Item.potionInstantDamage, Item.potionDizzy, Item.potionDizzy
	};
	
	private Item potionsMap[] = new Item[] {
		null, 
		Item.potionPoison,
		Item.potionSlowness,
		Item.potionInstantDamage,
		Item.potionAutoHealing,
		Item.potionDizzy
	};
		
	public EntityAlphaWitch(World world1) {
		super(world1);
		this.texture = "/mob/witch.png";
		this.health = this.getFullHealth();
		this.attackStrength = 4;
		this.moveSpeed = 0.8F;
		this.inventory.setInventorySlotContents(0, null);
		this.attackTime = 80;
	}
	
	protected void entityInit() {
		super.entityInit();
		
		// DataWatcher object 16 seems to be a "status byte" to keep Server and Client synchronized.
		// I'm using it to store the potion the witch is about to launch (bits 0-3), if the witch has
		// run out of potions (bit 5) and if the witch has already healed herself (bit 4).
		
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, new Byte((byte)0));
	}
	
	public void onUpdate() {
		super.onUpdate();
		Item nextPotion = this.getNextPotion();
		this.inventory.setInventorySlotContents(0, nextPotion == null ? null : new ItemStack(nextPotion));
	}
	
	public Item getNextPotion() {
		byte b = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		b &= 15;
		return b < 15 ? this.potionsMap[b] : null;
	}
	
	public void setNextPotion(Item potion) {
		byte b = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS); b &= 0xf0;
		for(int i = 0; i < this.potionsMap.length; i ++) {
			if(this.potionsMap[i] == potion) {
				b |= (i & 15); 
				break;
			}
		}
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, b);
	}
	
	public boolean isHealed() {
		byte b = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		return (b & 0x10) != 0;
	}
	
	public void setHealed(boolean healed) {
		byte b = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)((b & 0xEF) | (healed ? 0x10 : 0x00)));
	}
	
	public boolean isExhausted() {
		byte b = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		return (b & 0x20) != 0;
	}
	
	public void setExhausted(boolean exhausted) {
		byte b = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)((b & 0xDF) | (exhausted ? 0x20 : 0x00)));
	}

	public void fillInventory() {
		// Fill inventory with potions
		for(int i = 0; i < this.inventory.getSizesecondaryinventory(); i ++) {
			this.inventory.setSecondaryInventorySlotContents(
				i, 
				new ItemStack(this.possiblePotions[this.rand.nextInt(this.possiblePotions.length)])
			);
		}
	}
	
	@Override
	protected Entity findPlayerToAttack() {
		if(this.isExhausted()) return null;
		
		for(int i = 0; i < this.inventory.getSizesecondaryinventory(); i ++) {
			if(this.inventory.getSecondaryInventorySlotContents(i) != null) return super.findPlayerToAttack();
		} 
		
		this.setExhausted(true);
		return null;
	}
	
	@Override
	public void onLivingUpdate() {
		// Refill!
		if(!this.worldObj.isRemote) {
			if(this.isExhausted() && this.rand.nextInt(10000) == 0) {
				this.fillInventory();
				this.setExhausted(false);
				this.setHealed(false);
			}
		}
		super.onLivingUpdate();
	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		
		if(this.entityToAttack != null && this.entityToAttack.getDistanceSqToEntity(this) < 16.0D) {
			this.moveForward = -this.moveSpeed;
		}
	}

	@Override
	protected void attackEntity(Entity entity1, float f2) {
		if(f2 < 10.0F) {
			// Attempt to heal
			if(this.health < 10 && this.isHealed() == false) {
				this.setNextPotion(Item.potionAutoHealing);
				this.attackTime = 29;
				this.setHealed(true);
			} 
			
			double d3 = entity1.posX - this.posX;
			double d5 = entity1.posZ - this.posZ;

			if(this.attackTime == 30 && this.getNextPotion() == null) {

				ItemStack itemStack = null;
				for(int i = 0; i < this.inventory.getSizesecondaryinventory(); i ++) {
					itemStack = this.inventory.getSecondaryInventorySlotContents(i);
					if(itemStack != null) {
						this.setNextPotion(itemStack.getItem());
						this.inventory.setSecondaryInventorySlotContents(i, null);
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
					EntityThrowablePotion entityThrowablePotion = new EntityThrowablePotion(this.worldObj, this, (ItemPotion)this.getNextPotion());
					++entityThrowablePotion.posY;
					double d8 = entity1.posY + (double)entity1.getEyeHeight() - (double)0.2F - entityThrowablePotion.posY;
					float f10 = MathHelper.sqrt_double(d3 * d3 + d5 * d5) * 0.2F;
					this.worldObj.playSoundAtEntity(this, "mob.witch.throw", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
					this.worldObj.spawnEntityInWorld(entityThrowablePotion);
					entityThrowablePotion.setThrowableHeading(d3, d8 + (double)f10, d5, 0.6F, 12.0F);
				}
				this.setNextPotion(null);
				this.attackTime = 80;
			}

			this.rotationYaw = (float)(Math.atan2(d5, d3) * 180.0D / (double)(float)Math.PI) - 90.0F;
			this.hasAttacked = true;
		}

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setBoolean("Exhausted", this.isExhausted());
		nBTTagCompound1.setBoolean("Healed", this.isHealed());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.setExhausted(nBTTagCompound1.getBoolean("Exhausted"));
		this.setHealed(nBTTagCompound1.getBoolean("Healed"));
	}

	@Override
	protected int getDropItemId() {
		return Item.arrow.shiftedIndex;
	}

	@Override
	protected void dropFewItems() {
		int i1 = this.rand.nextInt(3);

		int i2;
		for(i2 = 0; i2 < i1; ++i2) {
			this.dropItem(Item.arrow.shiftedIndex, 1);
		}

		i1 = this.rand.nextInt(3);

		for(i2 = 0; i2 < i1; ++i2) {
			this.dropItem(Item.bone.shiftedIndex, 1);
		}

	}

	@Override
	public ItemStack getHeldItem() {
		return this.inventory.getHeldItem();
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
