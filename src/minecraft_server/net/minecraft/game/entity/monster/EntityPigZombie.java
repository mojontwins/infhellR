package net.minecraft.game.entity.monster;

import java.util.Iterator;
import java.util.List;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityArmoredMob;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.game.item.ItemGolden;

/**
 * Neutral zombie-pigman. Not a zombie: it only fights back once provoked and
 * carries its own gold weapon. It no longer shares the zombie class, so it does
 * not break doors, does not burn in daylight and is not afraid of blood moons.
 */
public class EntityPigZombie extends EntityArmoredMob {
	private int angerLevel = 0;
	private int randomSoundDelay = 0;
	private ItemStack heldItem;
	
	private int spawnObjectCounter;
	private boolean preparingToDrop = false;
	private boolean dropAGoodOne = false;
	
	public EntityPigZombie(World world1) {
		super(world1);
		this.texture = "/mob/pigzombie.png";
		this.texturePrefix = "pigzombie";
		this.moveSpeed = 0.5F;
		this.attackStrength = 5;
		this.isImmuneToFire = true;
		
		switch(world1.rand.nextInt(8)) {
		case 0: this.heldItem = new ItemStack(Item.maceGold, 1);
		case 1: this.heldItem = new ItemStack(Item.battleGold, 1);
		default: this.heldItem = new ItemStack(Item.swordGold, 1);
		}
	}

	// Texture variations. TODO: this block is duplicated from EntityZombie and
	// is planned to move to a shared EntityLiving once all mobs support skins.
	protected String texturePrefix;

	protected int getMaxTextureVariations() {
		return 1;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(Datawatchers.DW_TYPE, (byte)0);
		this.setTextureVariation((byte)(1 + this.rand.nextInt(this.getMaxTextureVariations())));
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
			return "/mob/" + this.texturePrefix + this.getTextureVariation() + ".png";
		}
		return this.texture;
	}

	@Override
	public void onUpdate() {
		this.moveSpeed = this.entityToAttack != null ? 0.95F : 0.5F;
		if(this.randomSoundDelay > 0 && --this.randomSoundDelay == 0) {
			this.worldObj.playSoundAtEntity(this, "mob.zombiepig.zpigangry", this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
		}

		super.onUpdate();
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.worldObj.difficultySetting > 0 && this.worldObj.checkIfAABBIsClear(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setShort("Anger", (short)this.angerLevel);
		nBTTagCompound1.setByte("TextureVariation", this.getTextureVariation());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		this.angerLevel = nBTTagCompound1.getShort("Anger");
		this.setTextureVariation(nBTTagCompound1.hasKey("TextureVariation") ? nBTTagCompound1.getByte("TextureVariation") : (byte)1);
	}

	@Override
	protected Entity findPlayerToAttack() {
		// new behaviour:
		
		// First look for golden items around, if not angry
		if(this.angerLevel == 0) {
			List<Entity> list = this.worldObj.getEntitiesWithinAABB(EntityItem.class, this.boundingBox.expand(16.0D, 4.0D, 16.0D));
			Iterator<Entity> iterator = list.iterator();
			while(iterator.hasNext()) {
				EntityItem entityItem = (EntityItem)iterator.next();
				if(entityItem != null && entityItem.item != null && entityItem.item.getItem() instanceof ItemGolden) {
					return entityItem;
				}
			}
		}
		
		// Then look for players. nearest player found not wearing gold becomes the target.
		Entity entityToAttack = super.findPlayerToAttack();
		if(entityToAttack instanceof EntityPlayer) {
			EntityPlayer entityPlayer = (EntityPlayer) entityToAttack;
			if(!entityPlayer.wearingGold()) return entityToAttack;
		}
		
		return this.angerLevel == 0 ? null : entityToAttack;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		// Pick up golden stuff & throw shit at random
		if(!this.worldObj.isRemote) {
			if(!this.preparingToDrop && this.angerLevel == 0 && this.rand.nextInt(32) == 0) {
				List<Entity> list = this.worldObj.getEntitiesWithinAABB(EntityItem.class, this.boundingBox);
				
				Iterator<Entity> iterator = list.iterator();
				while(iterator.hasNext()) {
					EntityItem entityItem = (EntityItem)iterator.next();
					if(entityItem != null && entityItem.item != null && entityItem.item.getItem() instanceof ItemGolden) {
						entityItem.setEntityDead();
						this.spawnObjectCounter = 50;
						this.dropAGoodOne = entityItem.item.itemID == Item.ingotGold.shiftedIndex;
						this.preparingToDrop = true;
					}
				}
			} 
			
			if(this.angerLevel > 0) {
				this.preparingToDrop = false;
			}
		
			if(this.preparingToDrop) {
				
				this.spawnObjectCounter --; if (this.spawnObjectCounter <= 0) {
					this.preparingToDrop = false;
					
					// drop object
					int itemId = 0;
					
					if(this.dropAGoodOne) {
						int chance = this.rand.nextInt(50);
						if(chance == 0) {
							itemId = Item.hammerDiamond.shiftedIndex;
						} else if(chance == 1) {
							itemId = Item.battleDiamond.shiftedIndex;				
						} else if(chance < 7) {
							itemId = Item.hammerSteel.shiftedIndex;
						} else if(chance < 12) {
							itemId = Item.battleSteel.shiftedIndex;				
						} else if(chance < 17) {
							itemId = Block.cryingObsidian.blockID;
						}
					}

					if(itemId == 0) {
						switch(this.rand.nextInt(8)) {
							case 0:
								itemId = Item.ingotCopper.shiftedIndex;
								break;
							case 1:
								itemId = Item.silk.shiftedIndex;
								break;
							case 2:
								itemId = Block.obsidian.blockID;
								break;
							case 3:
								itemId = Item.emerald.shiftedIndex;
								break;
							case 4:
								itemId = Item.arrow.shiftedIndex;
								break;
							case 5:
								itemId = Item.brick.shiftedIndex;
								break;
							case 6:
								itemId = this.rand.nextBoolean() ? Block.mushroomRed.blockID : Block.mushroomBrown.blockID;
								break;
							default:
								itemId = Item.ingotIron.shiftedIndex;
						}
					}

					this.dropItem(itemId, 1);
				}
			}
		}		
	}

	@Override
	public boolean attackEntityFrom(Entity entity1, int i2) {
		if(entity1 instanceof EntityPlayer) {
			List<Entity> list3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 32.0D, 32.0D));

			for(int i4 = 0; i4 < list3.size(); ++i4) {
				Entity entity5 = (Entity)list3.get(i4);
				if(entity5 instanceof EntityPigZombie) {
					EntityPigZombie entityPigZombie6 = (EntityPigZombie)entity5;
					entityPigZombie6.becomeAngryAt(entity1);
				}
			}

			this.becomeAngryAt(entity1);
		}

		return super.attackEntityFrom(entity1, i2);
	}
	
	@Override
	protected void attackEntity(Entity entity1, float f2) {
		if(entity1 instanceof EntityItem) {
		} else {
			super.attackEntity(entity1, f2);
		}
	}

	private void becomeAngryAt(Entity entity1) {
		this.entityToAttack = entity1;
		this.angerLevel = 400 + this.rand.nextInt(400);
		this.randomSoundDelay = this.rand.nextInt(40);
	}

	@Override
	protected String getLivingSound() {
		return "mob.zombiepig.zpig";
	}

	@Override
	protected String getHurtSound() {
		return "mob.zombiepig.zpighurt";
	}

	@Override
	protected String getDeathSound() {
		return "mob.zombiepig.zpigdeath";
	}

	@Override
	protected int getDropItemId() {
		return Item.porkCooked.shiftedIndex;
	}

	@Override
	public ItemStack getHeldItem() {
		return heldItem;
	}
	
	@Override
	public boolean setHeldItem(ItemStack itemStack) {
		this.heldItem = itemStack;
		return true;
	}
}
