package net.minecraft.game.entity.human;

import net.minecraft.game.world.block.Block;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.EntityMob;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.MathHelper;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.game.world.World;

public class EntityPirateBoss extends EntityMob {
	int invisibleCD = 10;
	protected boolean angryAtPlayer = false;

	public EntityPirateBoss(World world) {
		super(world);
		this.texture = "/mob/pirateBoss.png";
		this.attackStrength = 10;
		this.health = this.getFullHealth();
	}
	
	protected void entityInit() {
		super.entityInit();
		
		// Store "invisible" here to make this work in SMP
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, new Byte((byte) 0));
	}
	
	public void setInvisible(boolean invisible) {
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte) (invisible ? 1 : 0));
	}

	public boolean isInvisible() {
		return this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS) == 1;
	}

	@Override
	public String getEntityTexture() {
		return this.isInvisible() ? "/mob/invisible.png" : "/mob/pirateBoss.png";
	}

	@Override
	protected void attackEntity(Entity entity, float f) {
		super.attackEntity(entity, f);
		if(this.attackTime <= 0 && f < 2.0F && entity.boundingBox.maxY > this.boundingBox.minY && entity.boundingBox.minY < this.boundingBox.maxY) {
			this.attackTime = 20;
			this.attackEntityAsMob(entity);
		}

		if(this.health < 200 && !this.isInvisible() && this.invisibleCD < 0) {
			this.setInvisible(true);

			for(int n = 0; n < 3; ++n) {
				this.worldObj.spawnParticle("spell", this.posX + (double)this.rand.nextFloat() - 0.5D, this.posY + 1.0D, this.posZ + (double)this.rand.nextFloat() - 0.5D, 1.0D, 1.0D, 1.0D);
			}

			this.castTeleport(entity);
		}

		--this.invisibleCD;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		return super.attackEntityAsMob(entity);
	}

	public int getFullHealth() {
		return 100;
	}

	@Override
	public boolean attackEntityFrom(Entity damagesource, int i) {
		if(this.isInvisible()) {
			this.setInvisible(false);
			this.invisibleCD = 30;
		}

		return super.attackEntityFrom(damagesource, i);
	}

	protected boolean castTeleport(Entity entity) {
		// Attempt to teleport here:
		double dX, dZ;
		do {
			dX = (this.rand.nextDouble() - 0.5) * 16.0D;
			dZ = (this.rand.nextDouble() - 0.5) * 16.0D;
		} while((dX >= -4 && dX <= 4) || (dZ >= -4 && dZ <= 4));
		
		double destX = entity.posX + dX;
		double destZ = entity.posZ + dZ;
		double destY = entity.posY;
		
		// Currently here
		double curX = this.posX;
		double curY = this.posY;
		double curZ = this.posZ;
		
		// Flag
		boolean hasTeleported = false;
		
		// Integer destination coordinates
		int x = MathHelper.floor_double(destX);
		int y = MathHelper.floor_double(destY);
		int z = MathHelper.floor_double(destZ);

		if(this.worldObj.blockExists(x, y, z)) {
			boolean foundSolid = false;

			while(true) {
				while(!foundSolid && y > 0) {
					Block block = Block.blocksList[this.worldObj.getBlockId(x, y - 1, z)];
					if(block != null && block.blockMaterial.getIsSolid()) {
						foundSolid = true;
					} else {
						// Sink until on floor
						--this.posY;
						--y;
					}
				}

				if(foundSolid) {
					// Move this to new location
					this.setLocationAndAngles(destX, destY, destZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
					
					// Collided?
					if(this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox)) {
						
						// Puff
						double d = 2.0F;
						
						for(int i = 0; i < 10; ++i) {
							this.worldObj.spawnParticle("largesmoke", curX + (double)this.rand.nextFloat() - 0.5D, curY, curZ + (double)this.rand.nextFloat() - 0.5D, (destX - curX) / d, (destY - curY) / d , (destZ - curZ) / d);
						}

						// And success!
						hasTeleported = true;
					}
				}
				break;
			}
		}

		if(!hasTeleported) {
			// If it failed, get back.
			this.setLocationAndAngles(curX, curY, curZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
			return false;
		} else {
			// SFX
			this.worldObj.playSoundEffect(curX, curY, curZ, "random.breath", 1.0F, 1.0F);
			this.worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.0F);
			return true;
		}
	}

	@Override
	public void dropFewItems() {
		super.dropFewItems();
		if((this.rand.nextInt(5) == 0 || this.rand.nextInt(2) > 0)) {
			this.dropItem(Item.diamond.shiftedIndex, 2);
		}
	}

	@Override
	public ItemStack getHeldItem() {
		/*return !this.isInvisible() ? new ItemStack(Item.axeDiamond, 1) : null; */
		return this.isInvisible() ? new ItemStack(Item.knifeDiamond, 1) : new ItemStack(Item.swordDiamond, 1);
	}

	@Override
	protected void despawnEntity() {
		if(this.isDead) {
			super.despawnEntity();
		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("AngryAtPlayer", this.angryAtPlayer);
		nbttagcompound.setBoolean("Invisible", this.isInvisible());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		this.angryAtPlayer = nbttagcompound.getBoolean("AntryAtPlayer");
		this.setInvisible(nbttagcompound.getBoolean("Invisible"));
	}
	
	@Override
	protected Entity findPlayerToAttack() {
		// New logic - if player is disguised and pirates are not angry at player, don't attack.
		
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
			entityPlayer.triggerAchievement(AchievementList.pirateKing);
			
			if(!this.worldObj.isRemote) {
				this.dropItem(Item.pirateSigil.shiftedIndex, 1);
			}
		}
		
		super.onDeath(entity);
	}
}
