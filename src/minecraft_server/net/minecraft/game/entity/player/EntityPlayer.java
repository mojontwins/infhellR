package net.minecraft.game.entity.player;
import net.minecraft.game.world.block.BlockBed;
import net.minecraft.game.Seasons;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.achievements.StatBase;
import net.minecraft.game.achievements.StatList;

import java.util.Iterator;
import java.util.List;

import net.minecraft.game.world.block.tileentity.TileEntityCommandBlock;
import net.minecraft.game.trading.ITrader;
import net.minecraft.game.MathHelper;
import net.minecraft.game.container.Container;
import net.minecraft.game.container.ContainerPlayer;
import net.minecraft.game.container.IInventory;
import net.minecraft.game.container.InventoryPlayer;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityCreature;
import net.minecraft.game.entity.EntityLiving;
import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.entity.EnumAction;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.physics.AxisAlignedBB;
import net.minecraft.game.physics.Vec3D;
import net.minecraft.game.world.Weather;
import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.block.tileentity.TileEntityChest;
import net.minecraft.game.world.block.tileentity.TileEntityDispenser;
import net.minecraft.game.world.block.tileentity.TileEntityFurnace;
import net.minecraft.game.world.block.tileentity.TileEntitySign;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.material.Material;
import net.minecraft.game.world.biome.BiomeGenBase;
import net.minecraft.game.entity.EntityFish;
import net.minecraft.game.entity.misc.EntityMinecart;
import net.minecraft.game.entity.projectile.EntityArrow;
import net.minecraft.game.entity.animal.EntityPig;
import net.minecraft.game.entity.misc.EntityBoat;
import net.minecraft.game.entity.monster.EntityGhast;
import net.minecraft.game.entity.animal.EntityWolf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.entity.monster.EntityCreeper;
import net.minecraft.game.entity.monster.EntitySecretBoss;
import net.minecraft.game.entity.misc.EntityItem;
import net.minecraft.nbt.NBTTagList;

public abstract class EntityPlayer extends EntityLiving {
	public InventoryPlayer inventory = new InventoryPlayer(this);
	public Container inventorySlots;
	public Container craftingInventory;
	public int score = 0;
	public float prevCameraYaw;
	public float cameraYaw;
	public boolean isSwinging = false;
	public int swingProgressInt = 0;
	public String username;
	public int dimension;
	public String playerClNORMALUrl;
	
	public double altPrevPosX;
	public double altPrevPosY;
	public double altPrevPosZ;
	public double altPosX;
	public double altPosY;
	public double altPosZ;

	protected boolean sleeping;
	public ChunkCoordinates bedChunkCoordinates;
	private int sleepTimer;
	public float bedAdjustPosX;
	public float bedAdjustPosY;
	public float bedAdjustPosZ;
	private ChunkCoordinates playerSpawnCoordinate;
	private ChunkCoordinates startMinecartRidingCoordinate;
	private ChunkCoordinates playerLastDeathCoordinate;
	private boolean dontCheckSpawnCoordinates = false;
	public int timeUntilPortal = 20;
	protected boolean inPortal = false;
	public float timeInPortal;
	public float prevTimeInPortal;
	public EntityFish fishEntity = null;
	
	// Backported from release
	
	private ItemStack itemInUse;
	private int itemInUseCount;

	// Custom stuff
	
	public boolean isFlying = false;
	public boolean isCreative = false;
	public boolean enableCheats = false;
	public boolean deadManChest = false;
	public boolean enableCraftingGuide = false;
	public boolean creativeSprinting = false;
	public int freezeLevel = 0;
	
	public float speedOnGround = 0.1F;
	public float speedInAir = 0.02F;
	
	public EntityPlayer(World world1) {
		super(world1);
		this.inventorySlots = new ContainerPlayer(this.inventory, !world1.isRemote);
		this.craftingInventory = this.inventorySlots;
		this.yOffset = 1.62F;
		ChunkCoordinates chunkCoordinates2 = world1.getSpawnPoint();
		this.setLocationAndAngles((double)chunkCoordinates2.posX + 0.5D, (double)(chunkCoordinates2.posY + 1), (double)chunkCoordinates2.posZ + 0.5D, 0.0F, 0.0F);
		this.health = 20;
		this.entityType = "humanoid";
		this.unusedRotation = 180.0F;
		this.fireResistance = 20;
		this.texture = "/mob/char.png";
	}

	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, (byte)0);
	}

	public ItemStack getItemInUse() {
		return this.itemInUse;
	}

	public int getItemInUseCount() {
		return this.itemInUseCount;
	}

	public boolean isUsingItem() {
		return this.itemInUse != null;
	}

	public int getItemInUseDuration() {
		return this.isUsingItem() ? this.itemInUse.getMaxItemUseDuration() - this.itemInUseCount : 0;
	}

	public void stopUsingItem() {
		if(this.itemInUse != null) {
			this.itemInUse.onPlayerStoppedUsing(this.worldObj, this, this.itemInUseCount);
		}

		this.clearItemInUse();
	}

	public void clearItemInUse() {
		this.itemInUse = null;
		this.itemInUseCount = 0;
		if(!this.worldObj.isRemote) {
			this.setEating(false);
		}

	}

	public void onUpdate() {
		if(this.itemInUse != null) {
			ItemStack itemStack1 = this.inventory.getCurrentItem();
			if(itemStack1 != this.itemInUse) {
				this.clearItemInUse();
			} else {
				// Added (I think this may be forge's ?
				this.itemInUse.getItem().onUsingItemTick(this.itemInUse, this, this.itemInUseCount);
				
				if(this.itemInUseCount <= 25 && this.itemInUseCount % 4 == 0) {
					this.updateItemUse(itemStack1, 5);
				}

				if(--this.itemInUseCount == 0 && !this.worldObj.isRemote) {
					this.onItemUseFinish();
				}
			}
		}
		
		if(this.isPlayerSleeping()) {
			++this.sleepTimer; 
			if(this.sleepTimer > 100) {
				this.sleepTimer = 100;
			}

			if(!this.worldObj.isRemote) {
				if(!this.isInBed()) {
					this.wakeUpPlayer(true, true, false);
				} else if(this.worldObj.isDaytime()) {
					this.wakeUpPlayer(false, true, true);
				}
			}
		} else if(this.sleepTimer > 0) {
			++this.sleepTimer;
			if(this.sleepTimer >= 110) {
				this.sleepTimer = 0;
			}
		}

		super.onUpdate();
		if(!this.worldObj.isRemote && this.craftingInventory != null && !this.craftingInventory.canInteractWith(this)) {
			this.closeScreen();
			this.craftingInventory = this.inventorySlots;
		}

		this.altPrevPosX = this.altPosX;
		this.altPrevPosY = this.altPosY;
		this.altPrevPosZ = this.altPosZ;
		double d1 = this.posX - this.altPosX;
		double d3 = this.posY - this.altPosY;
		double d5 = this.posZ - this.altPosZ;
		double d7 = 10.0D;
		if(d1 > d7) {
			this.altPrevPosX = this.altPosX = this.posX;
		}

		if(d5 > d7) {
			this.altPrevPosZ = this.altPosZ = this.posZ;
		}

		if(d3 > d7) {
			this.altPrevPosY = this.altPosY = this.posY;
		}

		if(d1 < -d7) {
			this.altPrevPosX = this.altPosX = this.posX;
		}

		if(d5 < -d7) {
			this.altPrevPosZ = this.altPosZ = this.posZ;
		}

		if(d3 < -d7) {
			this.altPrevPosY = this.altPosY = this.posY;
		}

		this.altPosX += d1 * 0.25D;
		this.altPosZ += d5 * 0.25D;
		this.altPosY += d3 * 0.25D;
		this.addStat(StatList.minutesPlayedStat, 1);
		if(this.ridingEntity == null) {
			this.startMinecartRidingCoordinate = null;
		}

	}

	protected void updateItemUse(ItemStack itemStack1, int i2) {
		if(itemStack1.getItemUseAction() == EnumAction.drink) {
			this.worldObj.playSoundAtEntity(this, "random.drink", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
		}

		if(itemStack1.getItemUseAction() == EnumAction.eat) {
			for(int i3 = 0; i3 < i2; ++i3) {
				Vec3D vec3D4 = Vec3D.createVector(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
				vec3D4.rotateAroundX(-this.rotationPitch * (float)Math.PI / 180.0F);
				vec3D4.rotateAroundY(-this.rotationYaw * (float)Math.PI / 180.0F);
				Vec3D vec3D5 = Vec3D.createVector(((double)this.rand.nextFloat() - 0.5D) * 0.3D, (double)(-this.rand.nextFloat()) * 0.6D - 0.3D, 0.6D);
				vec3D5.rotateAroundX(-this.rotationPitch * (float)Math.PI / 180.0F);
				vec3D5.rotateAroundY(-this.rotationYaw * (float)Math.PI / 180.0F);
				vec3D5 = vec3D5.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
				this.worldObj.spawnParticle("iconcrack_" + itemStack1.getItem().shiftedIndex, vec3D5.xCoord, vec3D5.yCoord, vec3D5.zCoord, vec3D4.xCoord, vec3D4.yCoord + 0.05D, vec3D4.zCoord);
			}

			this.worldObj.playSoundAtEntity(this, "random.eat", 0.5F + 0.5F * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
		}

	}

	protected void onItemUseFinish() {
		if(this.itemInUse != null) {
			this.updateItemUse(this.itemInUse, 16);
			int i1 = this.itemInUse.stackSize;
			ItemStack itemStack2 = this.itemInUse.onFoodEaten(this.worldObj, this);
			if(itemStack2 != this.itemInUse || itemStack2 != null && itemStack2.stackSize != i1) {
				this.inventory.mainInventory[this.inventory.currentItem] = itemStack2;
				if(itemStack2.stackSize == 0) {
					this.inventory.mainInventory[this.inventory.currentItem] = null;
				}
			}

			this.clearItemInUse();
		}

	}

	public void handleHealthUpdate(byte b1) {
		if(b1 == 9) {
			this.onItemUseFinish();
		} else {
			super.handleHealthUpdate(b1);
		}

	}
	
	protected boolean isMovementBlocked() {
		return this.health <= 0 || this.isPlayerSleeping();
	}

	protected void closeScreen() {
		this.craftingInventory = this.inventorySlots;
	}

	public void updateClNORMAL() {
		this.playerClNORMALUrl = "http://s3.amazonaws.com/MinecraftClNORMALs/" + this.username + ".png";
		this.clNORMALUrl = this.playerClNORMALUrl;
	}

	public void updateRidden() {
		double d1 = this.posX;
		double d3 = this.posY;
		double d5 = this.posZ;
		super.updateRidden();
		this.prevCameraYaw = this.cameraYaw;
		this.cameraYaw = 0.0F;
		this.addMountedMovementStat(this.posX - d1, this.posY - d3, this.posZ - d5);
	}

	public void preparePlayerToSpawn() {
		this.yOffset = 1.62F;
		this.setSize(0.6F, 1.8F);
		super.preparePlayerToSpawn();
		this.health = 20;
		this.deathTime = 0;
	}

	protected void updateEntityActionState() {
		int swingSpeed = this.inventory.getSwingSpeed();

		if(this.isSwinging) {
			++this.swingProgressInt;
			if(this.swingProgressInt >= swingSpeed) {
				this.swingProgressInt = 0;
				this.isSwinging = false;
			}
		} else {
			this.swingProgressInt = 0;
		}

		this.swingProgress = (float)this.swingProgressInt / swingSpeed; // 8.0F;
	}

	public void onLivingUpdate() {
		
		if(this.posY <= -8.0D) {
			//this.posY = -8.0D;
			//this.motionY = 0.0D;
			this.attackEntityFrom((Entity)null, 4);
		}
		
		if(this.worldObj.difficultySetting == 0 && this.health < 20 && this.ticksExisted % 20 * 12 == 0) {
			this.heal(1);
		}

		this.inventory.decrementAnimations();
		this.prevCameraYaw = this.cameraYaw;
		
		// Sprinting affects speed factors, but only with (near) max health
		this.landMovementFactor = this.speedOnGround;
		this.airMovementFactor = this.speedInAir;
		
		if(!this.isCreative && this.health < 5) {
			// Cripple!
			this.landMovementFactor = (float)((double)this.landMovementFactor - (double)this.speedOnGround * 0.3D);

		} else if(this.isSprinting()) {
			double sprintingFactor = this.isCreative || this.health >= 18 ? 0.3D : 0.08D;
			
			this.landMovementFactor = (float)((double)this.landMovementFactor + (double)this.speedOnGround * sprintingFactor);
			this.airMovementFactor = (float)((double)this.airMovementFactor + (double)this.speedInAir * sprintingFactor);
		}
		
		super.onLivingUpdate();
		float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		float f2 = (float)Math.atan(-this.motionY * (double)0.2F) * 15.0F;
		if(f1 > 0.1F) {
			f1 = 0.1F;
		}

		if(!this.onGround || this.health <= 0) {
			f1 = 0.0F;
		}

		if(this.onGround || this.health <= 0) {
			f2 = 0.0F;
		}

		this.cameraYaw += (f1 - this.cameraYaw) * 0.4F;
		this.field_9328_R += (f2 - this.field_9328_R) * 0.8F;
		if(this.health > 0) {
			List<Entity> list3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(1.0D, 0.0D, 1.0D));
			if(list3 != null) {
				for(int i4 = 0; i4 < list3.size(); ++i4) {
					Entity entity5 = (Entity)list3.get(i4);
					if(!entity5.isDead) {
						this.collideWithPlayer(entity5);
					}
				}
			}

			// Freeze
			if(!this.worldObj.isRemote) {
				if(this.isCreative || this.worldObj.difficultySetting <= 1) {
					this.freezeLevel = 0;
				} else {
					if(this.ticksExisted % 20 == 0 /*&& this.world.freeze*/) {
						// New freeze logic which works like this:
						// During Winter on Cold biomes, increase freezeLevel.
						// Covered and in proximity of a fire block, decrease freezeLevel.
						// If freezeLevel goes above 200, decrease life once per two seconds.
						int x = (int)this.posX; 
						int y = (int)this.posY;
						int z = (int)this.posZ;
						
						BiomeGenBase biomeGen = this.worldObj.getBiomeGenAt(x, z);
						
						if(this.freezeLevel > 0 && Seasons.currentSeason != Seasons.WINTER && Seasons.dayOfTheYear < 23) {
							this.freezeLevel -= 4;				
						} else if(this.freezeLevel > 0 && !this.worldObj.canBlockSeeTheSky(x, y, z)) {
							// Indoors, recover with a fireplace
							if(this.worldObj.getIsAnyBlockID(this.boundingBox.expand(8, 4, 8), Block.fire.blockID)) {
								this.triggerAchievement(AchievementList.warmed);
								this.freezeLevel -= 10;
							} else if(this.worldObj.getIsAnyBlockID(this.boundingBox.expand(2, 2, 2), Block.stoneOvenActive.blockID)) { 
								this.triggerAchievement(AchievementList.warmedByOven);
								this.freezeLevel -= 4;
							} else if(this.moveForward != 0 || this.moveStrafing != 0) {
								this.freezeLevel --;
							} else if (biomeGen.weather == Weather.cold) {
								this.freezeLevel ++;
							}
						} else {
							
							int particle = Weather.particleDecide(biomeGen, this.worldObj);
							if(
									!this.isCreative && 
									(Seasons.currentSeason == Seasons.WINTER || Seasons.dayOfTheYear >= 23) && 
									biomeGen.weather == Weather.cold && 
									this.worldObj.canBlockSeeTheSky(x, y, z)
							) {
								this.freezeLevel ++;
								
								if(!this.worldObj.isDaytime()) {
									this.freezeLevel ++;
								} 
								
								if (particle == Weather.RAIN || particle == Weather.SNOW) {
									this.freezeLevel ++;
								}
								
								if(this.isInsideOfMaterial(Material.water)) {
									this.freezeLevel += 8;
								}
								
								if(freezeLevel > 100) this.triggerAchievement(AchievementList.gotChilly);
								
								if(this.dressedInRags()) this.freezeLevel -= 2;
							}
						}	
						
					}
					
					if(this.freezeLevel < 0.0) {
						this.freezeLevel = 0;
					}
					
					if(this.freezeLevel > 200.0) {
						if(this.ticksExisted % 40 == 0) this.attackEntityFrom((Entity)null, 1);
					}
					if(this.freezeLevel > 256) {
						this.freezeLevel = 256;
					}
				}
			}
		}

	}

	private void collideWithPlayer(Entity entity1) {
		entity1.onCollideWithPlayer(this);
	}

	public int getScore() {
		return this.score;
	}

	public void setDeadManChest() {
		// Dead man's chest
		if(this.deadManChest) {
			// Place chest here
			int x = (int)this.posX;
			int y = (int)this.posY;
			int z = (int)this.posZ;
			while(!this.worldObj.isBlockOpaqueCube(x, y - 1, z) && y > 0) --y;
			
			if (y > 0) {
				this.worldObj.setBlock(x, y, z, Block.chest.blockID);
				this.worldObj.setBlock(x, y + 1, z, Block.torchWood.blockID);
				
				TileEntityChest chest = (TileEntityChest)this.worldObj.getBlockTileEntity(x, y, z);
				
				// Attempt to add as many inventory items as possible
				int slotIndex = 0;
				int armorInventoryIndex = 0;
				while(armorInventoryIndex < 4) {
					ItemStack itemStack = this.inventory.armorInventory[armorInventoryIndex];
					if(itemStack != null) {
						chest.setStackInSlot(slotIndex ++, itemStack);
					}
					this.inventory.armorInventory[armorInventoryIndex ++] = null;
				}	
				
				int mainInventoryIndex = 0;
				while(mainInventoryIndex < 36 && slotIndex < 27) {
					ItemStack itemStack = this.inventory.mainInventory[mainInventoryIndex];
					if(itemStack != null) {
						chest.setStackInSlot(slotIndex ++, itemStack);
					}
					this.inventory.mainInventory[mainInventoryIndex ++] = null;
				}
								
				// Rest: just drop remaining
			}
		}
	}

	public void rememberCoordinates() {
		// Remember coordinates
		System.out.println ("Remembering last death coordinates");
		this.setPlayerLastDeathCoordinate(new ChunkCoordinates((int)this.posX, (int)this.posY, (int)this.posZ));
	}
	
	public void onDeath(Entity entity1) {
		this.rememberCoordinates();
		
		super.onDeath(entity1);
		this.setSize(0.2F, 0.2F);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionY = (double)0.1F;
		if(this.username.equals("Notch") || this.username.equals("Popoch")) {
			this.dropPlayerItemWithRandomChoice(new ItemStack(Item.appleRed, 1), true);
		}
		
		this.setDeadManChest();
		this.inventory.dropAllItems();
		
		if(entity1 != null) {
			this.motionX = (double)(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
			this.motionZ = (double)(-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
		} else {
			this.motionX = this.motionZ = 0.0D;
		}

		this.yOffset = 0.1F;
		this.addStat(StatList.deathsStat, 1);
		
	}

	public void addToPlayerScore(Entity entity1, int i2) {
		this.score += i2;
		if(entity1 instanceof EntityPlayer) {
			this.addStat(StatList.playerKillsStat, 1);
		} else {
			this.addStat(StatList.mobKillsStat, 1);
		}

	}
	
	protected int decreaseAirSupply(int i1) {
		ItemStack itemStack = this.inventory.armorItemInSlot(3);
		if(itemStack != null && itemStack.itemID == Block.divingHelmet.blockID && this.rand.nextInt(5) != 0) return i1;
		return super.decreaseAirSupply(i1);
	}

	public EntityItem dropCurrentItem() {
		return this.dropPlayerItemWithRandomChoice(this.inventory.decrStackSize(this.inventory.currentItem, 1), false);
	}

	public EntityItem dropPlayerItem(ItemStack itemStack1) {
		return this.dropPlayerItemWithRandomChoice(itemStack1, false);
	}

	public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStack1, boolean z2) {
		if(itemStack1 != null) {
			EntityItem entityItem3 = new EntityItem(this.worldObj, this.posX, this.posY - (double)0.3F + (double)this.getEyeHeight(), this.posZ, itemStack1);
			entityItem3.delayBeforeCanPickup = 40;
			float f4 = 0.1F;
			float f5;
			if(z2) {
				f5 = this.rand.nextFloat() * 0.5F;
				float f6 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
				entityItem3.motionX = (double)(-MathHelper.sin(f6) * f5);
				entityItem3.motionZ = (double)(MathHelper.cos(f6) * f5);
				entityItem3.motionY = (double)0.2F;
			} else {
				f4 = 0.3F;
				entityItem3.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f4);
				entityItem3.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f4);
				entityItem3.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI) * f4 + 0.1F);
				f4 = 0.02F;
				f5 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
				f4 *= this.rand.nextFloat();
				entityItem3.motionX += Math.cos((double)f5) * (double)f4;
				entityItem3.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
				entityItem3.motionZ += Math.sin((double)f5) * (double)f4;
			}

			this.joinEntityItemWithWorld(entityItem3);
			this.addStat(StatList.dropStat, 1);
			
			return entityItem3;
		}
		
		return null;
	}

	protected void joinEntityItemWithWorld(EntityItem entityItem1) {
		this.worldObj.spawnEntityInWorld(entityItem1);
	}

	public float getCurrentPlayerStrVsBlock(Block block1) {
		float f2 = this.inventory.getStrVsBlock(block1);
		if(this.isInsideOfMaterial(Material.water)) {
			f2 /= 5.0F;
		}

		if(!this.onGround) {
			f2 /= 5.0F;
		}

		return f2;
	}

	public boolean canHarvestBlock(Block block, int metadata) {
		return this.inventory.canHarvestBlock(block, metadata);
	}

	public void readEntityFromNBT(NBTTagCompound nBTTagCompound1) {
		super.readEntityFromNBT(nBTTagCompound1);
		NBTTagList nBTTagList2 = nBTTagCompound1.getTagList("Inventory");
		this.inventory.readFromNBT(nBTTagList2);
		this.dimension = nBTTagCompound1.getInteger("Dimension");
		this.sleeping = nBTTagCompound1.getBoolean("Sleeping");
		this.sleepTimer = nBTTagCompound1.getShort("SleepTimer");
		if(this.sleeping) {
			this.bedChunkCoordinates = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
			this.wakeUpPlayer(true, true, false);
		}

		if(nBTTagCompound1.hasKey("SpawnX") && nBTTagCompound1.hasKey("SpawnY") && nBTTagCompound1.hasKey("SpawnZ")) {
			this.playerSpawnCoordinate = new ChunkCoordinates(nBTTagCompound1.getInteger("SpawnX"), nBTTagCompound1.getInteger("SpawnY"), nBTTagCompound1.getInteger("SpawnZ"));
			this.dontCheckSpawnCoordinates = nBTTagCompound1.getBoolean("DontVerifySpawn");
		}
		
		if(nBTTagCompound1.hasKey("DeathX") && nBTTagCompound1.hasKey("DeathY") && nBTTagCompound1.hasKey("DeathZ")) {
			this.playerLastDeathCoordinate = new ChunkCoordinates(nBTTagCompound1.getInteger("DeathX"), nBTTagCompound1.getInteger("DeathY"), nBTTagCompound1.getInteger("DeathZ"));
		}
	
		this.isCreative = nBTTagCompound1.getBoolean("isCreative");
		this.isFlying = nBTTagCompound1.getBoolean("isFlying");
		this.enableCheats = nBTTagCompound1.getBoolean("enableCheats");
		this.deadManChest = nBTTagCompound1.getBoolean("deadManChest");
		this.enableCraftingGuide = nBTTagCompound1.getBoolean("enableCraftingGuide");
		this.freezeLevel = nBTTagCompound1.getInteger("freezeLevel");
	}

	public void writeEntityToNBT(NBTTagCompound nBTTagCompound1) {
		super.writeEntityToNBT(nBTTagCompound1);
		nBTTagCompound1.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
		nBTTagCompound1.setInteger("Dimension", this.dimension);
		nBTTagCompound1.setBoolean("Sleeping", this.sleeping);
		nBTTagCompound1.setShort("SleepTimer", (short)this.sleepTimer);
		if(this.playerSpawnCoordinate != null) {
			nBTTagCompound1.setInteger("SpawnX", this.playerSpawnCoordinate.posX);
			nBTTagCompound1.setInteger("SpawnY", this.playerSpawnCoordinate.posY);
			nBTTagCompound1.setInteger("SpawnZ", this.playerSpawnCoordinate.posZ);
			nBTTagCompound1.setBoolean("DontVerifySpawn", this.dontCheckSpawnCoordinates);
		}
		if(this.playerLastDeathCoordinate != null) {
			nBTTagCompound1.setInteger("DeathX", this.playerLastDeathCoordinate.posX);
			nBTTagCompound1.setInteger("DeathY", this.playerLastDeathCoordinate.posY);
			nBTTagCompound1.setInteger("DeathZ", this.playerLastDeathCoordinate.posZ);
		}

		nBTTagCompound1.setBoolean("isCreative", this.isCreative);
		nBTTagCompound1.setBoolean("isFlying", this.isFlying);
		nBTTagCompound1.setBoolean("enableCheats", this.enableCheats);
		nBTTagCompound1.setBoolean("deadManChest", this.deadManChest);
		nBTTagCompound1.setBoolean("enableCraftingGuide", this.enableCraftingGuide);
		nBTTagCompound1.setInteger("freezeLevel", this.freezeLevel);
	}

	public void displayGUIChest(IInventory iInventory1) {
	}

	public void displayWorkbenchGUI(int i1, int i2, int i3) {
	}
	
	public void displayGUITrading(InventoryPlayer inventory2, ITrader entityTrader) {
	}

	public void onItemPickup(Entity entity1, int i2) {
	}

	public float getEyeHeight() {
		return 0.12F;
	}

	protected void resetHeight() {
		this.yOffset = 1.62F;
	}

	public boolean attackEntityFrom(Entity entity1, int i2) {
		this.entityAge = 0;
		
		if (this.isCreative) return false;
		
		if(this.health <= 0) {
			return false;
		} else {
			if(this.isPlayerSleeping() && !this.worldObj.isRemote) {
				this.wakeUpPlayer(true, true, false);
			}

			if(entity1 instanceof EntityMob || entity1 instanceof EntityArrow) {
				if(this.worldObj.difficultySetting == 0) {
					i2 = 0;
				}

				if(this.worldObj.difficultySetting == 1) {
					i2 = i2 / 3 + 1;
				}

				if(this.worldObj.difficultySetting == 3) {
					i2 = i2 * 3 / 2;
				}
			}

			if(i2 == 0) {
				return false;
			} else {
				Object object3 = entity1;
				if(entity1 instanceof EntityArrow && ((EntityArrow)entity1).shootingEntity != null) {
					object3 = ((EntityArrow)entity1).shootingEntity;
				}

				if(object3 instanceof EntityLiving) {
					this.alertWolves((EntityLiving)object3, false);
				}

				this.addStat(StatList.damageTakenStat, i2);
				return super.attackEntityFrom(entity1, i2);
			}
		}
	}

	protected boolean isPVPEnabled() {
		return false;
	}

	protected void alertWolves(EntityLiving entityLiving1, boolean z2) {
		if(!(entityLiving1 instanceof EntityCreeper) && !(entityLiving1 instanceof EntityGhast)) {
			if(entityLiving1 instanceof EntityWolf) {
				EntityWolf entityWolf3 = (EntityWolf)entityLiving1;
				if(entityWolf3.isTamed() && this.username.equals(entityWolf3.getOwner())) {
					return;
				}
			}

			if(!(entityLiving1 instanceof EntityPlayer) || this.isPVPEnabled()) {
				List<Entity> list7 = this.worldObj.getEntitiesWithinAABB(EntityWolf.class, AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
				Iterator<Entity> iterator4 = list7.iterator();

				while(true) {
					EntityWolf entityWolf6;
					do {
						do {
							do {
								do {
									if(!iterator4.hasNext()) {
										return;
									}

									Entity entity5 = (Entity)iterator4.next();
									entityWolf6 = (EntityWolf)entity5;
								} while(!entityWolf6.isTamed());
							} while(entityWolf6.getTarget() != null);
						} while(!this.username.equals(entityWolf6.getOwner()));
					} while(z2 && entityWolf6.getIsSitting());

					entityWolf6.setIsSitting(false);
					entityWolf6.setTarget(entityLiving1);
				}
			}
		}
	}

	/*
	protected void damageEntity(int i1) {
		if(!this.isCreative) {
			int i2 = 25 - this.inventory.getTotalArmorValue();
			int i3 = i1 * i2 + this.damageRemainder;
			this.inventory.damageArmor(i1);
			i1 = i3 / 25;
			this.damageRemainder = i3 % 25;
			super.damageEntity(i1);
		}
	}
	*/
	
	@Override
	protected int getTotalArmorValue() {
		return this.inventory.getTotalArmorValue();
	}

	public void displayGUIFurnace(TileEntityFurnace tileEntityFurnace1) {
	}

	public void displayGUIDispenser(TileEntityDispenser tileEntityDispenser1) {
	}

	public void displayGUIEditSign(TileEntitySign tileEntitySign1) {
	}
	
	public void displayGUIGiveName(EntityCreature entity) {
	}
	
	public void displayGUICommandBlock(TileEntityCommandBlock tileEntityCommandBlock) {
	}

	public void useCurrentItemOnEntity(Entity entity1) {
		if(!entity1.interact(this)) {
			ItemStack itemStack2 = this.getCurrentEquippedItem();
			if(itemStack2 != null && entity1 instanceof EntityLiving) {
				itemStack2.useItemOnEntity((EntityLiving)entity1);
				if(itemStack2.stackSize <= 0) {
					itemStack2.onItemDestroyedByUse(this);
					this.destroyCurrentEquippedItem();
				}
			}

		}
	}

	public ItemStack getCurrentEquippedItem() {
		return this.inventory.getCurrentItem();
	}

	public void destroyCurrentEquippedItem() {
		this.inventory.setInventorySlotContents(this.inventory.currentItem, (ItemStack)null);
	}

	public double getYOffset() {
		return (double)(this.yOffset - 0.5F);
	}

	public void swingItem() {
		if(!this.isSwinging || this.swingProgress >= this.inventory.getSwingSpeed() / 8 || this.swingProgressInt < 0) {
			this.swingProgressInt = -1;
			this.isSwinging = true;
		}
	}

	public void attackTargetEntityWithCurrentItem(Entity victim) {
		int damage = this.inventory.getDamageVsEntity(victim);
		if(damage > 0) {
			if(this.motionY < 0.0D) {
				++damage;
			}

			if(victim.attackEntityFrom(this, damage)) {
				float knockBack = this.inventory.getExtraKnockbackVsEntity(victim);
				
				if(this.isSprinting()) {
					++knockBack;
				}

				if(knockBack > 0.0F) {
					victim.addVelocity(
							(double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * knockBack * 0.5F), 
							0.1D, 
							(double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * knockBack * 0.5F)
						);
					this.motionX *= 0.6D;
					this.motionZ *= 0.6D;
					this.setSprinting(false);
				}
			}
			
			ItemStack equippedItem = this.getCurrentEquippedItem();
			if(equippedItem != null && victim instanceof EntityLiving) {
				equippedItem.hitEntity((EntityLiving)victim, this);
				if(equippedItem.stackSize <= 0) {
					equippedItem.onItemDestroyedByUse(this);
					this.destroyCurrentEquippedItem();
				}
			}

			if(victim instanceof EntityLiving) {
				if(victim.isEntityAlive()) {
					this.alertWolves((EntityLiving)victim, true);
				}

				this.addStat(StatList.damageDealtStat, damage);
			}
		}
		
    }
	
	public void respawnPlayer() {
	}

	public abstract void func_6420_o();

	public void onItemStackChanged(ItemStack itemStack1) {
	}
	
	public void setEntityDead() {
		super.setEntityDead();
		this.inventorySlots.onCraftGuiClosed(this);
		if(this.craftingInventory != null) {
			this.craftingInventory.onCraftGuiClosed(this);
		}

	}

	public boolean isEntityInsideOpaqueBlock() {
		return !this.sleeping && super.isEntityInsideOpaqueBlock();
	}

	public EnumStatus sleepInBedAt(int i1, int i2, int i3) {
		if(!this.worldObj.isRemote) {
			label53: {
				if(!this.isPlayerSleeping() && this.isEntityAlive()) {
					if(this.worldObj.worldProvider.isNether) {
						return EnumStatus.NOT_POSSIBLE_HERE;
					}

					if(this.worldObj.isDaytime()) {
						return EnumStatus.NOT_POSSIBLE_NOW;
					}

					if(Math.abs(this.posX - (double)i1) <= 3.0D && Math.abs(this.posY - (double)i2) <= 2.0D && Math.abs(this.posZ - (double)i3) <= 3.0D) {
						break label53;
					}

					return EnumStatus.TOO_FAR_AWAY;
				}

				return EnumStatus.OTHER_PROBLEM;
			}
		}

		this.setSize(0.2F, 0.2F);
		this.yOffset = 0.2F;
		if(this.worldObj.blockExists(i1, i2, i3)) {
			int i4 = this.worldObj.getBlockMetadata(i1, i2, i3);
			int i5 = BlockBed.getDirectionFromMetadata(i4);
			float f6 = 0.5F;
			float f7 = 0.5F;
			switch(i5) {
			case 0:
				f7 = 0.9F;
				break;
			case 1:
				f6 = 0.1F;
				break;
			case 2:
				f7 = 0.1F;
				break;
			case 3:
				f6 = 0.9F;
			}

			this.adjustForBed(i5);
			this.setPosition((double)((float)i1 + f6), (double)((float)i2 + 0.9375F), (double)((float)i3 + f7));
		} else {
			this.setPosition((double)((float)i1 + 0.5F), (double)((float)i2 + 0.9375F), (double)((float)i3 + 0.5F));
		}

		this.sleeping = true;
		this.sleepTimer = 0;
		this.bedChunkCoordinates = new ChunkCoordinates(i1, i2, i3);
		this.motionX = this.motionZ = this.motionY = 0.0D;
		if(!this.worldObj.isRemote) {
			this.worldObj.updateAllPlayersSleepingFlag();
		}

		return EnumStatus.OK;
	}

	private void adjustForBed(int i1) {
		this.bedAdjustPosX = 0.0F;
		this.bedAdjustPosZ = 0.0F;
		switch(i1) {
		case 0:
			this.bedAdjustPosZ = -1.8F;
			break;
		case 1:
			this.bedAdjustPosX = 1.8F;
			break;
		case 2:
			this.bedAdjustPosZ = 1.8F;
			break;
		case 3:
			this.bedAdjustPosX = -1.8F;
		}

	}

	public void wakeUpPlayer(boolean z1, boolean z2, boolean z3) {
		this.setSize(0.6F, 1.8F);
		this.resetHeight();
		ChunkCoordinates chunkCoordinates4 = this.bedChunkCoordinates;
		ChunkCoordinates chunkCoordinates5 = this.bedChunkCoordinates;
		if(chunkCoordinates4 != null && this.worldObj.getBlockId(chunkCoordinates4.posX, chunkCoordinates4.posY, chunkCoordinates4.posZ) == Block.blockBed.blockID) {
			BlockBed.setBedOccupied(this.worldObj, chunkCoordinates4.posX, chunkCoordinates4.posY, chunkCoordinates4.posZ, false);
			chunkCoordinates5 = BlockBed.getNearestEmptyChunkCoordinates(this.worldObj, chunkCoordinates4.posX, chunkCoordinates4.posY, chunkCoordinates4.posZ, 0);
			if(chunkCoordinates5 == null) {
				chunkCoordinates5 = new ChunkCoordinates(chunkCoordinates4.posX, chunkCoordinates4.posY + 1, chunkCoordinates4.posZ);
			}

			this.setPosition((double)((float)chunkCoordinates5.posX + 0.5F), (double)((float)chunkCoordinates5.posY + this.yOffset + 0.1F), (double)((float)chunkCoordinates5.posZ + 0.5F));
		}

		this.sleeping = false;
		if(!this.worldObj.isRemote && z2) {
			this.worldObj.updateAllPlayersSleepingFlag();
		}

		if(z1) {
			this.sleepTimer = 0;
		} else {
			this.sleepTimer = 100;
		}

		if(z3) {
			this.setPlayerSpawnCoordinate(this.bedChunkCoordinates);
			this.setDontCheckSpawnCoordinates(false);
		}

	}

	private boolean isInBed() {
		return this.worldObj.getBlockId(this.bedChunkCoordinates.posX, this.bedChunkCoordinates.posY, this.bedChunkCoordinates.posZ) == Block.blockBed.blockID;
	}

	public static ChunkCoordinates verifyRespawnCoordinates(World world0, ChunkCoordinates chunkCoordinates1) {
		IChunkProvider iChunkProvider2 = world0.getIChunkProvider();
		iChunkProvider2.prepareChunk(chunkCoordinates1.posX - 3 >> 4, chunkCoordinates1.posZ - 3 >> 4);
		iChunkProvider2.prepareChunk(chunkCoordinates1.posX + 3 >> 4, chunkCoordinates1.posZ - 3 >> 4);
		iChunkProvider2.prepareChunk(chunkCoordinates1.posX - 3 >> 4, chunkCoordinates1.posZ + 3 >> 4);
		iChunkProvider2.prepareChunk(chunkCoordinates1.posX + 3 >> 4, chunkCoordinates1.posZ + 3 >> 4);
		/*int blockID = world0.getBlockId(chunkCoordinates1.posX, chunkCoordinates1.posY, chunkCoordinates1.posZ);
		if(blockID != Block.blockBed.blockID) {
			return null;
		} else*/ {
			ChunkCoordinates chunkCoordinates3 = BlockBed.getNearestEmptyChunkCoordinates(world0, chunkCoordinates1.posX, chunkCoordinates1.posY, chunkCoordinates1.posZ, 0);
			return chunkCoordinates3;
		}
	}

	public float getBedOrientationInDegrees() {
		if(this.bedChunkCoordinates != null) {
			int i1 = this.worldObj.getBlockMetadata(this.bedChunkCoordinates.posX, this.bedChunkCoordinates.posY, this.bedChunkCoordinates.posZ);
			int i2 = BlockBed.getDirectionFromMetadata(i1);
			switch(i2) {
			case 0:
				return 90.0F;
			case 1:
				return 0.0F;
			case 2:
				return 270.0F;
			case 3:
				return 180.0F;
			}
		}

		return 0.0F;
	}

	public boolean isPlayerSleeping() {
		return this.sleeping;
	}

	public boolean isPlayerFullyAsleep() {
		return this.sleeping && this.sleepTimer >= 100;
	}

	public int getSleepTimer() {
		return this.sleepTimer;
	}

	public void addChatMessage(String string1) {
	}

	public ChunkCoordinates getPlayerSpawnCoordinate() {
		return this.playerSpawnCoordinate;
	}
	

	public ChunkCoordinates getPlayerLastDeathCoordinate() {
		if (this.playerLastDeathCoordinate == null) return this.worldObj.getSpawnPoint();
		return this.playerLastDeathCoordinate;
	}

	public void setPlayerSpawnCoordinate(ChunkCoordinates chunkCoordinates1) {
		if(chunkCoordinates1 != null) {
			this.playerSpawnCoordinate = new ChunkCoordinates(chunkCoordinates1);
		} else {
			this.playerSpawnCoordinate = null;
		}

	}
	
	public void setPlayerLastDeathCoordinate(ChunkCoordinates chunkCoordinates) {
		if(chunkCoordinates != null) {
			this.playerLastDeathCoordinate = new ChunkCoordinates(chunkCoordinates);
		} else {
			this.playerLastDeathCoordinate = null;
		}
	}

	public void triggerAchievement(StatBase statBase1) {
		this.addStat(statBase1, 1);
	}

	public void addStat(StatBase statBase1, int i2) {
	}

	protected void jump() {
		super.jump();
		this.addStat(StatList.jumpStat, 1);
	}

	public void moveEntityWithHeading(float f1, float f2) {
		double d3 = this.posX;
		double d5 = this.posY;
		double d7 = this.posZ;
		super.moveEntityWithHeading(f1, f2);
		this.addMovementStat(this.posX - d3, this.posY - d5, this.posZ - d7);
	}

	private void addMovementStat(double d1, double d3, double d5) {
		if(this.ridingEntity == null) {
			int i7;
			if(this.isInsideOfMaterial(Material.water)) {
				i7 = Math.round(MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5) * 100.0F);
				if(i7 > 0) {
					this.addStat(StatList.distanceDoveStat, i7);
				}
			} else if(this.isInWater()) {
				i7 = Math.round(MathHelper.sqrt_double(d1 * d1 + d5 * d5) * 100.0F);
				if(i7 > 0) {
					this.addStat(StatList.distanceSwumStat, i7);
				}
			} else if(this.isOnLadder()) {
				if(d3 > 0.0D) {
					this.addStat(StatList.distanceClimbedStat, (int)Math.round(d3 * 100.0D));
				}
			} else if(this.onGround) {
				i7 = Math.round(MathHelper.sqrt_double(d1 * d1 + d5 * d5) * 100.0F);
				if(i7 > 0) {
					this.addStat(StatList.distanceWalkedStat, i7);
				}
			} else {
				i7 = Math.round(MathHelper.sqrt_double(d1 * d1 + d5 * d5) * 100.0F);
				if(i7 > 25) {
					this.addStat(StatList.distanceFlownStat, i7);
				}
			}

		}
	}

	private void addMountedMovementStat(double d1, double d3, double d5) {
		if(this.ridingEntity != null) {
			int i7 = Math.round(MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5) * 100.0F);
			if(i7 > 0) {
				if(this.ridingEntity instanceof EntityMinecart) {
					this.addStat(StatList.distanceByMinecartStat, i7);
					if(this.startMinecartRidingCoordinate == null) {
						this.startMinecartRidingCoordinate = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
					} else if(this.startMinecartRidingCoordinate.getSqDistanceTo(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) >= 1000.0D) {
						this.addStat(AchievementList.onARail, 1);
					}
				} else if(this.ridingEntity instanceof EntityBoat) {
					this.addStat(StatList.distanceByBoatStat, i7);
				} else if(this.ridingEntity instanceof EntityPig) {
					this.addStat(StatList.distanceByPigStat, i7);
				}
			}
		}

	}

	protected boolean fall(float f1) {
		if(f1 >= 2.0F) {
			this.addStat(StatList.distanceFallenStat, (int)Math.round((double)f1 * 100.0D));
		}

		return super.fall(f1);
	}

	public void onKillEntity(EntityLiving entityLiving1) {
		if(entityLiving1 instanceof EntityMob) {
			this.triggerAchievement(AchievementList.killEnemy);
		}

		if(entityLiving1 instanceof EntitySecretBoss) {
			this.triggerAchievement(AchievementList.slimeBoss);
		}
	}

	public int getItemIcon(ItemStack itemStack1) {
		int i2 = super.getItemIcon(itemStack1);
		if(itemStack1.itemID == Item.fishingRod.shiftedIndex && this.fishEntity != null) {
			i2 = itemStack1.getIconIndex() + 16;
		}

		return i2;
	}

	public void setInPortal() {
		if(this.timeUntilPortal > 0) {
			this.timeUntilPortal = 10;
		} else {
			this.inPortal = true;
		}
	}
	
	public boolean isFlying() {
		return this.isFlying;
	}
	
	private Item getInventoryItem(int i) {
		if(this.inventory.armorInventory[i] == null) return null;
		return this.inventory.armorInventory[i].getItem();
	}
	
	public boolean dressedInRags() {
		return this.getInventoryItem(3) == Item.helmetRags && (
					this.getInventoryItem(2) == Item.plateRags ||
					this.getInventoryItem(1) == Item.legsRags
				);
	}
	
	public boolean dressedAsAPirate() {
		boolean arrr =
				this.getInventoryItem(3) == Item.helmetPirate &&
				this.getInventoryItem(2) == Item.platePirate &&
				this.getInventoryItem(1) == Item.legsPirate &&
				this.getInventoryItem(0) == Item.bootsPirate;
		
		if(arrr) this.triggerAchievement(AchievementList.pirateDress);
		
		return arrr;
	}
	
	public boolean wearingGold() {
		boolean oink = 
				this.getInventoryItem(3) == Item.helmetGold ||
				this.getInventoryItem(2) == Item.plateGold ||
				this.getInventoryItem(1) == Item.legsGold || 
				this.getInventoryItem(0) == Item.bootsGold;
		
		return oink;
	}
	
	public boolean bootsOfLeather() {
		return this.getInventoryItem(0) == Item.bootsLeather ||
				this.getInventoryItem(0) == Item.bootsPirate;
	}
	
	public boolean divingHelmetOn() {
		ItemStack itemStack = this.inventory.armorItemInSlot(3);
		return itemStack != null && itemStack.itemID == Block.divingHelmet.blockID;
	}

	public boolean isDontCheckSpawnCoordinates() {
		return dontCheckSpawnCoordinates;
	}

	public void setDontCheckSpawnCoordinates(boolean dontCheckSpawnCoordinates) {
		this.dontCheckSpawnCoordinates = dontCheckSpawnCoordinates;
	}
	
	public int getFullHealth() {
		return 20;
	}

	public boolean canEat(boolean z1) {
		return (z1 || this.health < this.getFullHealth()) && !this.isCreative;
	}

	public void setItemInUse(ItemStack itemStack1, int i2) {
		System.out.println("setItemInUse");
		if(itemStack1 != this.itemInUse) {
			System.out.println("Setting " + itemStack1 + " for " +i2);
			this.itemInUse = itemStack1;
			this.itemInUseCount = i2;
			if(!this.worldObj.isRemote) {
				this.setEating(true);
			}

		}
	}

	public ChunkCoordinates getPlayerCoordinates() {
		return new ChunkCoordinates((int)this.posX, (int)this.posY, (int)this.posZ);
	}
	
	@Override
	public boolean isSprinting() {
		return super.isSprinting() || this.creativeSprinting;
	}
}
