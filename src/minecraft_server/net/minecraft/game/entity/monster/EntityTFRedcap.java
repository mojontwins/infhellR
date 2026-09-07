package net.minecraft.game.entity.monster;

import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntityTFRedcap extends EntityMob implements ISentient {
	private static final ItemStack defaultHeldItem = new ItemStack(Item.pickaxeSteel, 1);
	protected ItemStack heldItem;
	protected boolean lefty;
	protected boolean redirect;

	public EntityTFRedcap(World world) {
		super(world);
		this.texture = "/mob/redcap1.png";
		this.moveSpeed = 0.5F;
		this.setSize(0.9F, 1.4F);
		this.attackStrength = 2;
		this.lefty = this.rand.nextBoolean();
		this.setHeldItem(defaultHeldItem);
	}

	public EntityTFRedcap(World world, double x, double y, double z) {
		this(world);
		this.setPosition(x, y, z);
	}
	
	protected int getMaxTextureVariations() {
		return 3;
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_TYPE, (byte)0);
		this.setTextureVariation((byte) (1 + rand.nextInt(this.getMaxTextureVariations())));
	}
	
	public void setTextureVariation(byte variation) {
		this.dataWatcher.updateObject(Datawatchers.DW_TYPE, variation);
	}
	
	public byte getTextureVariation() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_TYPE);
	}
	
	@Override
	public String getEntityTexture() {
		if(this.getMaxTextureVariations() > 1) {
			return "/mob/redcap" + this.getTextureVariation() + ".png";		
		} else return this.texture;
	}
	
	@Override
	protected Entity findPlayerToAttack() {
		if(this.lastAttackingEntity != null && this.lastAttackingEntity != this.currentTarget) {
			return this.lastAttackingEntity;
		}
		
		return super.findPlayerToAttack();
	}

	@Override
	public int getFullHealth() {
		return 20;
	}

	@Override
	public ItemStack getHeldItem() {
		return this.heldItem;
	}
	
	@Override
	public boolean setHeldItem(ItemStack itemStack) {
		this.heldItem = itemStack;
		return true;
	}

	@Override
	protected String getLivingSound() {
		return "mob.redcap";
	}

	@Override
	protected String getHurtSound() {
		return "mob.redcaphurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.redcapdie";
	}

	@Override
	protected int getDropItemId() {
		return Item.bootsSteel.shiftedIndex;
	}

	@Override
	protected void dropFewItems() {
		switch(this.rand.nextInt(6)) {
			case 0: this.dropItem(Item.bootsSteel.shiftedIndex, 1); break;
			case 1: this.dropItem(Item.plateSteel.shiftedIndex, 1); break;
			case 2: this.dropItem(Item.legsSteel.shiftedIndex, 1); break;
			case 3: this.dropItem(Item.helmetSteel.shiftedIndex, 1); break;			
		}

		if(this.rand.nextInt(9) == 0) {
			this.dropItem(Item.pickaxeSteel.shiftedIndex, 1);
		}

	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		
		if(this.entityToAttack != null) {
			float dist = this.entityToAttack.getDistanceToEntity(this);
			if(dist >= 4.0F) {
				this.moveSpeed = 0.5F;
			} else {
				this.moveSpeed = 0.8F;
			}

			if(dist > 4.0F && dist < 6.0F && this.isTargetLookingAtMe()) {
				this.moveStrafing = this.lefty ? this.moveForward : -this.moveForward;
				this.moveForward = 0.0F;
			}
		}

	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbtTagCompound) {
		super.readEntityFromNBT(nbtTagCompound);
		byte textureVariation = 1;
		if(nbtTagCompound.hasKey("TextureVariation")) {
			textureVariation = nbtTagCompound.getByte("TextureVariation");
		} else {
			System.out.println("Importing old level, overrode texture variation for " + this.getClass() + " with default.");
		}
		this.setTextureVariation(textureVariation);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbtTagCompound) {
		super.writeEntityToNBT(nbtTagCompound);
		nbtTagCompound.setByte("TextureVariation", this.getTextureVariation());
	}

	public boolean isTargetLookingAtMe() {
		double dx = this.posX - this.entityToAttack.posX;
		double dz = this.posZ - this.entityToAttack.posZ;
		float angle = (float)(Math.atan2(dz, dx) * 180.0D / (double)(float) Math.PI) - 90.0F;
		float difference = MathHelper.abs((this.entityToAttack.rotationYaw - angle) % 360.0F);
		return difference < 60.0F || difference > 300.0F;
	}
}
