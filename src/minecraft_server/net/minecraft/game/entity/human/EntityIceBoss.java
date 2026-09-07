package net.minecraft.game.entity.human;


import net.minecraft.game.world.block.Block;
import net.minecraft.game.achievements.AchievementList;
import net.minecraft.game.entity.Datawatchers;
import net.minecraft.game.entity.Entity;
import net.minecraft.game.entity.monster.EntityIceBall;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.item.Item;
import net.minecraft.game.item.ItemStack;
import net.minecraft.game.world.World;

public class EntityIceBoss extends EntityHumanBase {
	
	private Entity targetedEntity = null;
	private int aggroCooldown = 0;
	public int prevAttackCounter = 0;
	public int attackCounter = 0;

	public EntityIceBoss(World world) {
		super(world);
		this.texture = "/mob/ice_boss.png";
		this.setLevel(4);
		this.health = this.getFullHealth();
		this.setHeldItem(new ItemStack(Block.ice));
	}

	@Override
	public void entityInit() {
		super.entityInit();
		
		// Store state bits in DWO #Datawatchers.DW_STATUS
		this.dataWatcher.addObject(Datawatchers.DW_STATUS, new Byte((byte) 0));
	}

	@Override
	public void configureAttributesBasedOnLevel() {
		this.armorTexture = "/armor/ice_diamond";	
	}
	
	public void setAttacking(boolean attacking) {
		byte data = (byte)this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		this.dataWatcher.updateObject(Datawatchers.DW_STATUS, (byte)((data & 0xfe) | (attacking ? 1 : 0)));
	}
	
	public boolean getAttacking() {
		byte data = (byte)this.dataWatcher.getWatchableObjectByte(Datawatchers.DW_STATUS);
		return (data & 1) == 1;
	}
	
	public boolean teleport() {
		// Currently here
		double curX = this.posX;
		double curY = this.posY;
		double curZ = this.posZ;
		
		// Select a new x, z
		double dX = (this.rand.nextDouble() - 0.5D) * 10D;
		double dZ = (this.rand.nextDouble() - 0.5D) * 10D;
		
		double destX = this.posX + dX;
		double destZ = this.posZ + dZ;
		
		int x = (int) Math.floor(destX);
		int z = (int) Math.floor(destZ);
		int y = (int) Math.floor(this.posY);
		
		//System.out.println("Attempt to teleport to " + x + " " + y + " " + z);
		
		// on floor?
		Block block = Block.blocksList[this.worldObj.getBlockId(x, y - 1, z)];
		if(block == null || !block.blockMaterial.getIsSolid()) {
			return false;
		}
		
		// Collide somethign?
		this.setLocationAndAngles(destX, this.posY, destZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
		
		if(this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.getIsAnyLiquid(this.boundingBox)) {
			
			// Puff
			double d = 2.0F;
			
			for(int i = 0; i < 10; ++i) {
				this.worldObj.spawnParticle("largesmoke", curX + (double)this.rand.nextFloat() - 0.5D, curY, curZ + (double)this.rand.nextFloat() - 0.5D, (destX - curX) / d, (this.posY - curY) / d , (destZ - curZ) / d);
			}
			
			this.worldObj.playSoundEffect(curX, curY, curZ, "random.breath", 1.0F, 1.0F);
			this.worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.0F);
			return true;
		}
		
		// Not success, repos
		this.setLocationAndAngles(curX, curY, curZ, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
		return false;
	}
	
	@Override
	public boolean attackEntityFrom(Entity entity, int i2) {
		if(entity instanceof EntityIceBall) {
			return super.attackEntityFrom(entity, 5);
		} else return false;
	}
	
	@Override
	protected void attackEntity(Entity entity, float f) {
		super.attackEntity(entity, f);

	}
	
	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		
		if(this.entityToAttack != null && this.entityToAttack.getDistanceSqToEntity(this) < 49.0D) {
			this.moveForward = -this.moveSpeed;
		}
		
		if(this.targetedEntity != null && this.targetedEntity.isDead) {
			this.targetedEntity = null;
		}

		if(this.targetedEntity == null || this.aggroCooldown-- <= 0) {
			this.targetedEntity = this.worldObj.getClosestPlayerToEntity(this, 16.0D);
			if(this.targetedEntity != null) {
				this.aggroCooldown = 20;
			}
		}
		
		if (this.targetedEntity instanceof EntityPlayer && ((EntityPlayer) this.targetedEntity).isCreative) this.targetedEntity = null; 

		double d9 = 16.0D;
		if(this.targetedEntity != null && this.targetedEntity.getDistanceSqToEntity(this) < d9 * d9) {
			double d11 = this.targetedEntity.posX - this.posX;
			double d13 = this.targetedEntity.boundingBox.minY + (double)(this.targetedEntity.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
			double d15 = this.targetedEntity.posZ - this.posZ;
			this.renderYawOffset = this.rotationYaw = -((float)Math.atan2(d11, d15)) * 180.0F / (float)Math.PI;
			if(this.canEntityBeSeen(this.targetedEntity)) {
				if(this.attackCounter == 10) {
					this.worldObj.playSoundAtEntity(this, "mob.ice.idle", this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
				}

				++this.attackCounter;
				if(this.attackCounter == 20) {
					this.worldObj.playSoundAtEntity(this, "mob.ice.hurt", this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
					EntityIceBall entityIceBall17 = new EntityIceBall(this.worldObj, this, d11, d13, d15);
					
					entityIceBall17.posX = this.posX;
					entityIceBall17.posY = this.posY + 1.0D;
					entityIceBall17.posZ = this.posZ;
					this.worldObj.spawnEntityInWorld(entityIceBall17);
					this.attackCounter = -60;
				}
			} else if(this.attackCounter > 0) {
				--this.attackCounter;
			}
		}
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		if(!this.worldObj.isRemote) {
			if(rand.nextInt(25) == 0) {
				this.teleport();
			}
		}
	}
	
	@Override
	public int getFullHealth() {
		return 100;
	}
	
	@Override
	public void updateArmor() {
		int maxHealth = this.getFullHealth();
		if(this.health < maxHealth / 5) {
			this.armor = 0;
		} else if(this.health < maxHealth / 5 * 2) {
			this.armor = 1;
		} else if(this.health < maxHealth / 5 * 3) {
			this.armor = 2;
		} else if(this.health < maxHealth / 5 * 4) {
			this.armor = 3;
		} else {
			this.armor = 4;
		}
	}
	
	@Override
	protected String getHurtSound() {
		return "mob.ice.hurt";
	}
	
	@Override
	protected String getDeathSound() {
		return "mob.ice.hurt";
	}
	
	@Override
	protected String getLivingSound() {
		return "mob.ice.idle";
	}
	
	@Override
	public void onDeath(Entity entity) {
		if(entity instanceof EntityPlayer) {
			EntityPlayer entityPlayer = (EntityPlayer) entity;
			entityPlayer.triggerAchievement(AchievementList.iceBoss);
			
			if(!this.worldObj.isRemote) {
				this.dropItem(Item.pirateSigil.shiftedIndex, 1);
			}
		}
		
		super.onDeath(entity);
	}
}
