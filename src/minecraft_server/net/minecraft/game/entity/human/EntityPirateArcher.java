package net.minecraft.game.entity.human;

import net.minecraft.game.entity.status.Status;
import net.minecraft.game.entity.status.StatusEffect;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.projectile.EntityArrowWithEffect;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntityPirateArcher extends EntityMob implements ISentient {
	private ItemStack heldItem;;
	protected boolean angryAtPlayer = false;
	
	public EntityPirateArcher(World world) {
		super(world);
		this.attackStrength = this.worldObj == null ? 7 : 5 + this.worldObj.difficultySetting;
		this.texture = "/mob/pirate1.png";
		this.setHeldItem(new ItemStack(Item.bow));
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
			return "/mob/pirate" + this.getTextureVariation() + ".png";		
		} else return this.texture;
	}

	@Override
	public boolean setHeldItem(ItemStack itemStack) {
		this.heldItem = itemStack;
		return true;
	}
	
	@Override
	public ItemStack getHeldItem() {
		return this.heldItem;
	}
	
	public int getFullHealth() {
		return this.worldObj == null ? 27 : 25 + this.worldObj.difficultySetting;
	}

	protected String getLivingSound() {
		return "mob.zombie";
	}

	protected String getHurtSound() {
		return "mob.zombiehurt";
	}

	protected String getDeathSound() {
		return "mob.zombiedeath";
	}

	protected void attackEntity(Entity entity, float f) {
		if(f < 10.0F) {
			double d = entity.posX - this.posX;
			double d1 = entity.posZ - this.posZ;
			if(this.attackTime == 0) {
			
				EntityArrow entityArrow7 = this.selectArrow();
				
				++entityArrow7.posY;
				double d8 = entity.posY + (double)entity.getEyeHeight() - (double)0.2F - entityArrow7.posY;
				float f10 = MathHelper.sqrt_double(d * d + d1 * d1) * 0.2F;
				this.worldObj.spawnEntityInWorld(entityArrow7);
				entityArrow7.setArrowHeading(d, d8 + (double)f10, d1, 0.6F, 12.0F);
				
				this.worldObj.playSoundAtEntity(this, "random.bow", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
				
				this.attackTime = 100;
			}

			this.rotationYaw = (float)(Math.atan2(d1, d) * 180.0D / (double)(float)Math.PI) - 90.0F;
			this.hasAttacked = true;
		}

	}
	
	public EntityArrow selectArrow() {
		return new EntityArrowWithEffect(this.worldObj, this)
				.withStatusEffect(new StatusEffect(Status.statusBlind.id, 100, 1));
	}

	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		nbttagcompound.setByte("TextureVariation", this.getTextureVariation());
	}

	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		this.angryAtPlayer = nbttagcompound.getBoolean("AntryAtPlayer");
		
		byte textureVariation = 1;
		if(nbttagcompound.hasKey("TextureVariation")) {
			textureVariation = nbttagcompound.getByte("TextureVariation");
		} else {
			System.out.println("Importing old level, overrode texture variation for " + this.getClass() + " with default.");
		}
		this.setTextureVariation(textureVariation);
	}

	protected int getDropItemId() {
		return Item.arrow.shiftedIndex;
	}

	protected void dropFewItems(boolean flag, int i) {
		int j = this.rand.nextInt(3 + i);

		int l;
		for(l = 0; l < j; ++l) {
			this.dropItem(Item.arrow.shiftedIndex, 1);
		}

		j = this.rand.nextInt(3 + i);

		for(l = 0; l < j; ++l) {
			this.dropItem(Item.ingotGold.shiftedIndex, 2);
		}

	}

	public boolean getCanSpawnHere() {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);
		return 
				this.posY > 60.0D && this.worldObj.isBlockOpaqueCube(i, j, k) ? true
			: 
				(this.rand.nextInt(4) == 1 ? super.getCanSpawnHere() : false);
	}

	protected void despawnEntity() {
		if(this.isDead) {
			super.despawnEntity();
		}

	}
	
	@Override
	protected Entity findPlayerToAttack() {
		// New logic - if player is disguised and pirates are not angry at player, don't attack.
		
		if(this.lastAttackingEntity != null && this.lastAttackingEntity != this.currentTarget) {
			return this.lastAttackingEntity;
		}
		
		EntityPlayer entityPlayer1 = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
		return 
				entityPlayer1 != null && 
				!entityPlayer1.isCreative && 
				this.canEntityBeSeen(entityPlayer1) && 
				(!entityPlayer1.dressedAsAPirate() || this.angryAtPlayer) ? 
						entityPlayer1 
					: 
						null;
	}

	@Override
	public void onDeath(Entity entity) {
		if(entity instanceof EntityPlayer) {
			EntityPlayer entityPlayer = (EntityPlayer) entity;
			entityPlayer.triggerAchievement(AchievementList.pirateKill);
		}
		
		super.onDeath(entity);
	}
}
