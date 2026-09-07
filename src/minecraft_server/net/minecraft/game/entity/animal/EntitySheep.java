package net.minecraft.game.entity.animal;

import java.util.Random;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.misc.EntityItem;

public class EntitySheep extends EntityAnimal {
	public static final float[][] fleeceColorTable = new float[][]{{1.0F, 1.0F, 1.0F}, {0.95F, 0.7F, 0.2F}, {0.9F, 0.5F, 0.85F}, {0.6F, 0.7F, 0.95F}, {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.7F, 0.8F}, {0.3F, 0.3F, 0.3F}, {0.6F, 0.6F, 0.6F}, {0.3F, 0.6F, 0.7F}, {0.7F, 0.4F, 0.9F}, {0.2F, 0.4F, 0.8F}, {0.5F, 0.4F, 0.3F}, {0.4F, 0.5F, 0.2F}, {0.8F, 0.3F, 0.3F}, {0.1F, 0.1F, 0.1F}};
	protected boolean looksWithInterest;
	private float prevHeadRoll;
	private float headRoll;
	
	public EntitySheep(World world1) {
		super(world1);
		this.texture = "/mob/sheep.png";
		this.setSize(0.9F, 1.3F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, new Byte((byte)0));
	}

	@Override
	public boolean attackEntityFrom(Entity entity1, int i2) {
		// Reinstated for b1.6.6 - punch sheep to get cloth. {
		if(!this.worldObj.isRemote && !this.getSheared() && entity1 instanceof EntityLiving) {
			this.setSheared(true);
			int i3 = 1 + this.rand.nextInt(3);

			for(int i4 = 0; i4 < i3; ++i4) {
				EntityItem entityItem5 = this.entityDropItem(new ItemStack(Block.cloth.blockID, 1, this.getFleeceColor()), 1.0F);
				entityItem5.motionY += (double)(this.rand.nextFloat() * 0.05F);
				entityItem5.motionX += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
				entityItem5.motionZ += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
			}
		}
		// }
		
		return super.attackEntityFrom(entity1, i2);
	}

	@Override
	protected void dropFewItems() {
		if(!this.getSheared()) {
			this.entityDropItem(new ItemStack(Block.cloth.blockID, 1, this.getFleeceColor()), 0.0F);
		}

	}

	@Override
	protected int getDropItemId() {
		return Block.cloth.blockID;
	}

	@Override
	public boolean interact(EntityPlayer entityPlayer) {
		if (super.interact(entityPlayer)) {
			return false;
		}
		
		ItemStack itemStack2 = entityPlayer.inventory.getCurrentItem();
		if(itemStack2 != null && itemStack2.itemID == Item.shears.shiftedIndex && !this.getSheared()) {
			if(!this.worldObj.isRemote) {
				this.setSheared(true);
				int i3 = 2 + this.rand.nextInt(3);

				for(int i4 = 0; i4 < i3; ++i4) {
					EntityItem entityItem5 = this.entityDropItem(new ItemStack(Block.cloth.blockID, 1, this.getFleeceColor()), 1.0F);
					entityItem5.motionY += (double)(this.rand.nextFloat() * 0.05F);
					entityItem5.motionX += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
					entityItem5.motionZ += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
				}
			}

			itemStack2.damageItem(1, entityPlayer);
		}

		return false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setBoolean("Sheared", this.getSheared());
		nBTTagCompound1.setByte("Color", (byte)this.getFleeceColor());
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.setSheared(nBTTagCompound1.getBoolean("Sheared"));
		this.setFleeceColor(nBTTagCompound1.getByte("Color"));
	}

	@Override
	protected String getLivingSound() {
		return "mob.sheep";
	}

	@Override
	protected String getHurtSound() {
		return "mob.sheep";
	}

	@Override
	protected String getDeathSound() {
		return "mob.sheep";
	}

	public int getFleeceColor() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 15;
	}

	public void setFleeceColor(int i1) {
		byte b2 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 & 240 | i1 & 15));
	}

	public boolean getSheared() {
		return (this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) & 16) != 0;
	}

	public void setSheared(boolean z1) {
		byte b2 = this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		if(z1) {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 | 16));
		} else {
			this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)(b2 & -17));
		}

	}

	public static int getRandomFleeceColor(Random random0) {
		int i1 = random0.nextInt(100);
		return i1 < 5 ? 15 : (i1 < 10 ? 7 : (i1 < 15 ? 8 : (i1 < 18 ? 12 : (random0.nextInt(500) == 0 ? 6 : 0))));
	}
	
	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		
		Entity target = null;
		
		EntityPlayer closestPlayer = this.worldObj.getClosestPlayerToEntity(this, 8.0D); 
		if (closestPlayer != null) {
			ItemStack heldItem = closestPlayer.inventory.getCurrentItem();
			
			// Start following player?
			if (heldItem != null && heldItem.itemID == Item.wheat.shiftedIndex) {
				target = closestPlayer;
			}
		}
		
		this.setTarget(target);
	}
	
	@Override
	public void onLivingUpdate() {
		// Eat grass / tall grass to regrow wool
		super.onLivingUpdate();
		
		if(this.rand.nextInt(1000) == 0 && !this.worldObj.isRemote) {
			int i1 = MathHelper.floor_double(this.posX);
			int i2 = MathHelper.floor_double(this.posY);
			int i3 = MathHelper.floor_double(this.posZ);
			if(this.worldObj.getBlockId(i1, i2, i3) == Block.tallGrass.blockID) {
				this.worldObj.playAuxSFX(2001, i1, i2, i3, Block.tallGrass.blockID + 4096);
				this.worldObj.setBlockWithNotify(i1, i2, i3, 0);
				this.regrowWool();
			} else if(this.worldObj.getBlockId(i1, i2 - 1, i3) == Block.grass.blockID) {
				this.worldObj.playAuxSFX(2001, i1, i2 - 1, i3, Block.grass.blockID);
				this.worldObj.setBlockWithNotify(i1, i2 - 1, i3, Block.dirt.blockID);
				this.regrowWool();
			}
		}
		
		this.looksWithInterest = false;
		
		if (this.hasCurrentTarget() && !this.hasPath()) {
			Entity currentTargetEntity = this.getCurrentTarget();
			if (currentTargetEntity instanceof EntityPlayer) {
				EntityPlayer entityPlayer = (EntityPlayer) currentTargetEntity;
				ItemStack heldItem = entityPlayer.inventory.getCurrentItem();
				if (heldItem != null && heldItem.itemID == Item.wheat.shiftedIndex) {
					this.looksWithInterest = true;
				}
			}
		}
	}
	
	public void regrowWool() {
		this.health = this.getFullHealth();
		this.setSheared(false);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		this.prevHeadRoll = this.headRoll;
		
		if(this.looksWithInterest) {
			this.headRoll += (1.0F - this.headRoll) * 0.4F;
		} else {
			this.headRoll += (0.0F - this.headRoll) * 0.4F;
		}
		
		if(this.looksWithInterest) {
			this.numTicksToChaseTarget = 10;
		}
	}
	
	public float getInterestedAngle(float f1) {
		return (this.prevHeadRoll + (this.headRoll - this.prevHeadRoll) * f1) * 0.15F * (float)Math.PI;
	}
}
