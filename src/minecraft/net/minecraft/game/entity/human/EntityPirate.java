package net.minecraft.game.entity.human;

import java.util.List;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.IMobWithLevel;
import net.minecraft.game.entity.ISentient;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntityPirate extends EntityHumanBase implements IMobWithLevel, ISentient {
	protected boolean angryAtPlayer = false;
	
	public EntityPirate(World world) {
		super(world);
		this.armorIndex = 4;
		this.attackStrength = 8;
		this.texture = "/mob/pirate1.png";
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
	public void configureAttributesBasedOnLevel() {
		super.configureAttributesBasedOnLevel();
		switch(this.getLvl()) {
		case 0:
			this.setHeldItem(new ItemStack(Item.axeWood, 1)); 
			break;
		case 1:
			this.setHeldItem(new ItemStack(Item.axeStone, 1));
			break;
		case 2:
			this.setHeldItem(new ItemStack(Item.axeSteel, 1));
			break;
		case 3:
			this.setHeldItem(new ItemStack(Item.axeGold, 1));
			break;
		case 4:
			this.setHeldItem(new ItemStack(Item.axeDiamond, 1)); 
			break;
		default:
			this.setHeldItem(new ItemStack(Item.axeGold, 1));
			break;
		}
	}
	
	@Override
	public int getFullHealth() {
		return 25 + this.armorIndex * 4;
	}

	@Override
	public void onLivingUpdate() {
		if(this.rand.nextInt(1000) == 1) {
			for(int r = 0; r < 10; ++r) {
				this.worldObj.spawnParticle("smoke", this.posX + (double)this.rand.nextFloat() - 0.5D, this.posY + 1.0D + (double)this.rand.nextFloat(), this.posZ + (double)this.rand.nextFloat() - 0.5D, 0.0D, 0.0D, 0.0D);
			}
		}

		super.onLivingUpdate();
	}

	@Override
	public boolean attackEntityFrom(Entity entity, int i) {
		if(this.rand.nextInt(10) == 0) {
			for(int r = 0; r < 10; ++r) {
				this.worldObj.spawnParticle("splash", this.posX + (double)this.rand.nextFloat() - 0.5D, this.posY + 1.0D + (double)this.rand.nextFloat(), this.posZ + (double)this.rand.nextFloat() - 0.5D, 0.0D, 0.0D, 0.0D);
			}
		}
		
		if(entity instanceof EntityPlayer) {
			List<Entity> list4 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 32.0D, 32.0D));

			for(int i5 = 0; i5 < list4.size(); ++i5) {
				Entity entity6 = (Entity)list4.get(i5);
				if(entity6 instanceof EntityPirate) {
					EntityPirate entityPirate = (EntityPirate)entity6;
					entityPirate.angryAtPlayer = true;
				} else if(entity6 instanceof EntityPirateArcher) {
					EntityPirateArcher entityPirate = (EntityPirateArcher)entity6;
					entityPirate.angryAtPlayer = true;
				} else if(entity6 instanceof EntityPirateBoss) {
					EntityPirateBoss entityPirate = (EntityPirateBoss)entity6;
					entityPirate.angryAtPlayer = true;
				}
			}

			this.angryAtPlayer = true;
		}

		return super.attackEntityFrom(entity, i);
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		return super.attackEntityAsMob(entity);
	}

	@Override
	public boolean getCanSpawnHere() {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);
		/*
		return this.posY > 60.0D && 
				(this.worldObj.getBlockId(i, j, k) == Block.planks.blockID || 
				this.worldObj.getBlockId(i, j, k) == Block.stone.blockID || 
				this.worldObj.getBlockId(i, j, k) == Block.wood.blockID || 
				this.worldObj.getBlockId(i, j, k) == Block.stoneBricks.blockID || 
				this.worldObj.getBlockId(i, j, k) == Block.dirt.blockID || 
				this.worldObj.getBlockId(i, j, k) == Block.grass.blockID) ? true : (this.rand.nextInt(4) == 1 ? super.getCanSpawnHere() : false);
		*/
		
		for (int y = j - 1; y <= j; y ++) {
			Material material = this.worldObj.getBlockMaterial(i, y, k);
			int blockID = this.worldObj.getBlockId(i, y, k); 
			
			 System.out.println (i + " " + y + " " + k + " Material: " + material.name  + " id " + blockID);
			
			if (this.posY > 60.0D)
				if (material == Material.wood ||
					material == Material.grass || 
					material == Material.ground ||
					blockID == Block.stoneBricks.blockID) return true;
				if (this.rand.nextInt(4) == 1 && super.getCanSpawnHere()) return true;
		}
		
		return false;
	}

	@Override
	protected String getLivingSound() {
		return "mob.zombie";
	}

	@Override
	protected String getHurtSound() {
		return "mob.zombiehurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.zombiedeath";
	}

	@Override
	public void dropFewItems() {
		int j = this.rand.nextInt(3);

		int l;
		for(l = 0; l < j; ++l) {
			this.dropItem(Item.appleRed.shiftedIndex, 1);
		}

		if(this.getLvl() == 2) {
			j = this.rand.nextInt(3);

			for(l = 0; l < j; ++l) {
				this.dropItem(Item.ingotIron.shiftedIndex, 1);
			}
		}

		if(this.getLvl() == 3) {
			j = this.rand.nextInt(3);

			for(l = 0; l < j; ++l) {
				this.dropItem(Item.ingotGold.shiftedIndex, 1);
			}
		}

		if(this.getLvl() == 4) {
			j = this.rand.nextInt(3);

			for(l = 0; l < j; ++l) {
				this.dropItem(Item.diamond.shiftedIndex, 1);
			}
		}

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		nbttagcompound.setByte("TextureVariation", this.getTextureVariation());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		this.angryAtPlayer = nbttagcompound.getBoolean("AntryAtPlayer");
		this.configureAttributesBasedOnLevel();
		
		byte textureVariation = 1;
		if(nbttagcompound.hasKey("TextureVariation")) {
			textureVariation = nbttagcompound.getByte("TextureVariation");
		} else {
			System.out.println("Importing old level, overrode texture variation for " + this.getClass() + " with default.");
		}
		this.setTextureVariation(textureVariation);
	}

	@Override
	protected int getDropItemId() {
		return Item.feather.shiftedIndex;
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
